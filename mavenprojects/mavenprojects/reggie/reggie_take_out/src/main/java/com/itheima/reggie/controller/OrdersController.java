package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);

        ordersService.submit(orders);

        return R.success("下单成功");
    }

    /**
     * 手机端订单信息分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/orderPage")
    public R<Page> orderPage(int page, int pageSize){

        //构造分页对象
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        //添加条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //执行分页查询
        ordersService.page(ordersPage, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        List<Orders> records = ordersPage.getRecords();

        List<OrdersDto> ordersDtos = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item, ordersDto);

            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper);

            ordersDto.setOrderDetails(orderDetails);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtos);

        return R.success(ordersDtoPage);
    }

    /**
     * 管理端分页查询订单数据
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){

        //构造分页对象
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        //添加条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //按订单号模糊查询
        queryWrapper.like(number != null,Orders::getNumber,number);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //订单创建时间在beginTime和endTime之间
        queryWrapper.between((beginTime != null && endTime != null),Orders::getOrderTime,beginTime,endTime);
        //订单创建时间在beginTime之后（ge():大于等于）
        queryWrapper.ge((beginTime != null && endTime == null),Orders::getOrderTime,beginTime);
        //订单创建时间在endTime之前（le():小于等于）
        queryWrapper.le((beginTime == null && endTime != null),Orders::getOrderTime,endTime);

        //执行分页查询
        ordersService.page(ordersPage, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        List<Orders> records = ordersPage.getRecords();

        List<OrdersDto> ordersDtos = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item, ordersDto);

            ordersDto.setUserName(item.getConsignee());

            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper);

            ordersDto.setOrderDetails(orderDetails);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtos);

        return R.success(ordersDtoPage);
    }

    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> status(@RequestBody Orders orders){
//        log.info("要修改状态订单为：{}",orders);

        ordersService.updateById(orders);

        return R.success("订单状态修改成功");
    }

    /**
     * 再来一单
     * @return
     */
    @PostMapping("/again")
    public R<String> orderAgain(){
        return R.success("不想写了...");
    }

}
