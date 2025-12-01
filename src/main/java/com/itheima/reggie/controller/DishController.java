package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.DishFlavorService;
  import com.itheima.reggie.service.DishService;
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.web.bind.annotation.*;
  /**
   * 菜品管理
   */
  @RestController
  @RequestMapping("/dish")
  @Slf4j
  public class DishController {
      @Autowired
      private DishService dishService;
  
      @Autowired
      private DishFlavorService dishFlavorService;

      /**
       * 分页查询
       * @param page
       * @param pageSize
       * @return
       */
      @GetMapping("/page")
      public R<Page> page(int page, int pageSize){
          //分页构造器
          Page<Dish> pageInfo = new Page<>(page,pageSize);
          //条件构造器
          LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
          //添加排序条件，根据sort进行排序
          queryWrapper.orderByAsc(Dish::getSort);

          //分页查询
          dishService.page(pageInfo,queryWrapper);
          return R.success(pageInfo);
      }

      @PostMapping
      public R<String> save(@RequestBody DishDto dishDto) {
          log.info(dishDto.toString());

          dishService.saveWithFlavor(dishDto);

          return R.success("保存菜品成功");
      }
  }    