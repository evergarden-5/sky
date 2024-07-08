package com.sky.service;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService{
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
     public void add(ShoppingCartDTO shoppingCartDTO){
         ShoppingCart shoppingCart=new ShoppingCart();
         BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
         Long userId= BaseContext.getCurrentId();
         shoppingCart.setUserId(userId);
         //判断当前加入购物车的商品是否已经存在
         List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
         //true number+1
         if(list!=null&&list.size()>0){
             for(ShoppingCart cart:list){
                 Integer number = cart.getNumber();
                 number++;
                 cart.setNumber(number);
                 shoppingCartMapper.updateNumberById(shoppingCart);
             }
         }
         //false 添加到数据库
         else{
             shoppingCartMapper.insert(shoppingCart);
         }
    }
}
