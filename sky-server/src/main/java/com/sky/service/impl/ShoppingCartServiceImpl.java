package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        // 判断当前加入购物车的商品是否已经存在了【动态SQL】
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long currentId = BaseContext.getCurrentId();        // 从进程池中获取当前用户ID
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 如果存在，则只需要将数量加一
        if (list != null && list.size() > 0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);

            // 将更新后的数据插入到购物车中
            shoppingCartMapper.updateNumberById(cart);

        }else {
            // 如果不存在，需要插入一条购物车数据【需要判断添加的是菜品还是套餐】
            Long dishId = shoppingCart.getDishId();
            if (dishId != null){
                // 本次添加到购物车的是菜品【根据菜品ID获取菜品信息】
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
//                shoppingCart.setNumber(1);  // 新添加的，所以数量为1
//                shoppingCart.setCreateTime(LocalDateTime.now());
            } else {
                // 本次添加到购物车的是套餐【根据套餐ID获取套餐信息】
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
//                shoppingCart.setNumber(1);  // 新添加的，所以数量为1
//                shoppingCart.setCreateTime(LocalDateTime.now());
            }
            shoppingCart.setNumber(1);  // 新添加的，所以数量为1
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查询购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        // 获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(currentId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {

        // 判断当前加入购物车的商品是否已经存在了【动态SQL】
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long currentId = BaseContext.getCurrentId();        // 从进程池中获取当前用户ID
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 判断该菜品/套餐的数量是否为1
        ShoppingCart cart = list.get(0);
        if (cart.getNumber() > 1){
            // 数量大于1，则更新该菜品/套餐的数量为1
            cart.setNumber(cart.getNumber() - 1);
            // 将更新后的数据插入到购物车中
            shoppingCartMapper.updateNumberById(cart);

        }else {
            // 数量等于1，则删除该菜品/套餐
            shoppingCartMapper.deleteById(cart);

        }
    }

}