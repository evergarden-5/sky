package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
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
                 shoppingCartMapper.updateNumberById(cart);
             }
         }
         //false 添加到数据库
         else{
             //判断此次添加的是菜品还是套餐
             Long dishId = shoppingCart.getDishId();
             Long setmealId = shoppingCart.getSetmealId();
             if(setmealId==null){
                 //不是套餐，是菜品
                 Dish dish = dishMapper.getById(dishId);
                 shoppingCart.setName(dish.getName());
                 shoppingCart.setImage(dish.getImage());
                 shoppingCart.setAmount(dish.getPrice());
             }
             else{
                 //是套餐
                 Setmeal setmeal = setmealMapper.getById(setmealId);
                 shoppingCart.setName(setmeal.getName());
                 shoppingCart.setImage(setmeal.getImage());
                 shoppingCart.setAmount(setmeal.getPrice());
             }
             shoppingCart.setNumber(1);
             shoppingCart.setCreateTime(LocalDateTime.now());
             shoppingCartMapper.insert(shoppingCart);
         }
    }
    public void sub(ShoppingCartDTO shoppingCartDTO){
        //如果number>1则number--，再更新数据库
        ShoppingCart cart = shoppingCartMapper.getById(shoppingCartDTO);
        Integer number=cart.getNumber();
        if(number>0) {
            number--;
            //如果number==0，则把这个菜品或者套餐从cart列表中删除
            if(number==0){
                shoppingCartMapper.deleteById(cart);
            }
            else {//否则就更新cart表
                cart.setNumber(number);
                shoppingCartMapper.updateNumberById(cart);
            }
        }
        else{
            return;
        }
    }
    public List<ShoppingCart> showShoppingCart(){
         ShoppingCart shoppingCart=new ShoppingCart();
         shoppingCart.setUserId(BaseContext.getCurrentId());
         return shoppingCartMapper.list(shoppingCart);
    }

    public void clean(){
        Long currentId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(currentId);
    }
}
