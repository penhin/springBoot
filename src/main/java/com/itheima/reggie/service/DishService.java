package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dishDto);
    void updateWithFlavor(DishDto dishDto);
    DishDto getByIdWithFlavor(Long id);
}