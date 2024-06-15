package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存数据
        cleanCache("dish_" + dishDTO.getCategoryId());
//        redisTemplate.delete("dish_" + dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "批量删除菜品")
    public Result deleteByIds(@RequestParam List<Long> ids){
        log.info("批量删除菜品:{}", ids);
        dishService.deletaBatch(ids);

        // 将所有的菜品缓存数据清理掉，所有以dish_开头的key【先获取所有相关的key，再批量删除缓存】
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据ID查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询菜品信息")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据ID查询菜品信息: {}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);

        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 可能涉及多个菜品分类，将所有的菜品缓存数据清理掉，所有以dish_开头的key【先获取所有相关的key，再批量删除缓存】
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 菜品的起售和停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("status/{status}")
    @ApiOperation("菜品的起售和停售")
    public Result<String> startOrStop(@PathVariable Integer status,Long id){
        dishService.startOrStop(status,id);

        // 可能涉及多个菜品分类，将所有的菜品缓存数据清理掉，所有以dish_开头的key【先获取所有相关的key，再批量删除缓存】
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 统一删除所有缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
