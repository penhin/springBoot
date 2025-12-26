package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long dishID = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        List<DishFlavor> flavorList = flavors.stream().map(item -> {
            item.setDishId(dishID);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavorList);
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((falvor) -> {
            falvor.setDishId(dishDto.getId());
            return falvor;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    public void updateStatus(List<Long> ids, Integer status) {
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids).set(Dish::getStatus, status);

        this.update(updateWrapper);
    }

    @Override
    public void removeDishByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, 1);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("起售中的菜品不能删除");
        }

        LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(SetmealDish::getDishId, ids);
        int count2 =  setmealDishService.count(queryWrapper2);
        if (count2 > 0) {
            throw new CustomException("该菜品已关联套餐，不能删除");
        }

        super.removeByIds(ids);
    }

    @Override
    public List<DishDto> getDishesById(Long id) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, id);

        List<Dish> dishList = this.list(queryWrapper);

        List<DishDto> dishDtos = dishList.stream().map(item -> {
           DishDto dishDto = new DishDto();
           BeanUtils.copyProperties(item, dishDto);
           return dishDto;
        }).collect(Collectors.toList());

        return dishDtos;
    }
}