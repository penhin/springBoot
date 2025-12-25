package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    void updateStatus(List<Long> ids, Integer status);
    void removeByIds(List<Long> ids);
    void saveWithFlavor(DishDto dishDto);
    void updateWithFlavor(DishDto dishDto);
    DishDto getByIdWithFlavor(Long id);
    List<DishDto> getDishesById(Long id);
}