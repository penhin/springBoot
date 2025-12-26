package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
    void updateWithDish(SetmealDto setmealDto);
    void updateStatus(List<Long> ids, Integer status);
    void removeWithDish(List<Long> ids);
    SetmealDto getByIdWithDish(Long id);
}