package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
  @RestController
  @RequestMapping("/employee")
  public class EmployeeController {
  
      @Autowired
      private EmployeeService employeeService;

      @RequestMapping("/login")
      public R<Employee> login(@RequestBody Employee employee, HttpSession session) {
          String password = employee.getPassword();
          String md5password = DigestUtils.md5DigestAsHex(password.getBytes());

          LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(Employee::getUsername, employee.getUsername());
          Employee one = employeeService.getOne(queryWrapper);

          if (one == null || !one.getPassword().equals(md5password) || one.getStatus() == 0) {
              return R.error("登录失败");
          }

          session.setAttribute("employee", one.getId());
          return R.success(one);
      }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());

        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    @GetMapping("/page")
    public R<Page<Employee>> page(Integer page, Integer pageSize, String name) {
        //1.创建分页对象
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        //2.创建查询对象，添加条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        //最后修改的在后面
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //3.执行page方法
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
  }    