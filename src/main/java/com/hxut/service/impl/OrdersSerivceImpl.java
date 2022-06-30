package com.hxut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxut.common.BaseContext;
import com.hxut.common.exception.CustomException;
import com.hxut.entity.*;
import com.hxut.mapper.OrdersMapper;
import com.hxut.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * description: OrdersSerivceImpl
 * date: 2022/6/27 20:28
 * author: MR.孙
 */
@Service
public class OrdersSerivceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * @description:  用户下单
     * @param orders
     * @return: void
     * @author: MR.孙
     * @date: 2022/6/27 21:42
    */
    @Transactional
    @Override
    public void submit(Orders orders) {
        //查询当前用户id
        Long userId = BaseContext.getCurreantId();
        //根据当前用户id查询购物车信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        if(shoppingCartList==null || shoppingCartList.size()==0){
            throw new CustomException("购物车为空,无法下单");
        }

        //查询用户信息
        User user = userService.getById(userId);

        //查询地址簿信息,没写地址不能下单
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook==null){
            throw new CustomException("用户地址信息有误,无法下单");
        }

        long orderId = IdWorker.getId();//生成订单id

        //金额
        AtomicInteger amount = new AtomicInteger(0);

        //封装order_detail表数据
        List<OrderDetail> orderDetails=shoppingCartList.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());



        //封装order表信息
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setPhone(addressBook.getPhone());
//        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
//                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
//                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
//                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        orders.setAddress(addressBook.getDetail());
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        //向订单表插入一条数据
        this.save(orders);

        //向订单明细表插入多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车
        shoppingCartService.remove(queryWrapper);
    }
}
