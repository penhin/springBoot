package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);

        Long setmealID = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> setmealDishList = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealID);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        Long setmealID = setmealDto.getId();

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealID);

        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> setmealDishList = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealID);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    public void updateStatus(List<Long> ids, Integer status) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids).set(Setmeal::getStatus, status);

        this.update(updateWrapper);
    }

    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {

        // 1. 校验是否有起售套餐
        boolean exists = this.lambdaQuery()
                .in(Setmeal::getId, ids)
                .eq(Setmeal::getStatus, 1)
                .count() > 0;
        if (exists) {
            throw new CustomException("起售中的套餐不能删除");
        }

        // 2. 删除套餐
        this.lambdaUpdate()
                .in(Setmeal::getId, ids)
                .eq(Setmeal::getStatus, 0)
                .remove();

        // 3. 删除关联菜品
        LambdaQueryWrapper<SetmealDish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishWrapper);
    }

}