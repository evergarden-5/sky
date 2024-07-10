package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.webSocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        orderService.userCancelById(id);
        return Result.success();
    }
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        orderService.repetition(id);
        return Result.success();
    }
    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }
    /**
     * 历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(Integer page ,Integer pageSize,Integer status){
        PageResult pageResult=orderService.pageQuery(page,pageSize,status);
        return Result.success(pageResult);
    }
    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);

        //OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO=new OrderPaymentVO();

        log.info("生成预支付交易单：{}", orderPaymentVO);
        //个人无法调用微信支付功能，采用将order的status直接改为complete的方式

        orderMapper.updateStatusByNum(ordersPaymentDTO);
        Orders order = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        //点击支付按钮以后通过websocketserver向前端发送支付成功信息
        Map map=new HashMap();
        map.put("type",1);//1表示来单提醒，2表示客户催单
        map.put("orderId",order.getId());
        map.put("content","订单号"+ordersPaymentDTO.getOrderNumber());

        //前端需要接受json格式的字符串
        String json = JSON.toJSONString(map);
        //将json字符串推送到前端页面
        webSocketServer.sendToAllClient(json);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/reminder/{id}")
    public void remind(@PathVariable Long id){
        //通过id查询到是哪一单
        Orders order = orderMapper.getById(id);

        //根据前端规定的格式建立hashmap，再转成字符串格式
        Map map=new HashMap();//1表示来单提醒，2表示客户催单
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号"+order.getNumber());

        String json = JSON.toJSONString(map);
        //通过websocketserver把json字符串推送到前端
        webSocketServer.sendToAllClient(json);
    }
}
