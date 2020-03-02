package cn.aethli.filter.controller;

import cn.aethli.filter.annotation.ReturnExclude;
import cn.aethli.filter.annotation.ReturnInclude;
import cn.aethli.filter.commen.enums.ResponseStatus;
import cn.aethli.filter.model.ResponseModel;
import cn.aethli.filter.vo.Department;
import cn.aethli.filter.vo.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** @author Termite */
@RestControllerAdvice
@RequestMapping("return/test")
public class ReturnTestController /*extends BaseController*/ {

  // 务必设置produce，防止返回值格式不正确
  @GetMapping(value = "include", produces = "application/json;utf-8")
  @ReturnInclude(names = {"msg", "data", "data.id", "data.user.department"})
  public Object includeTest() {
    return getResult();
  }

  @GetMapping(value = "exclude", produces = "application/json;utf-8")
  @ReturnExclude(names = {"msg", "data.id"})
  public Object excludeTest() {
    return getResult();
  }

  private Object getResult() {
    List<Department> departments = new ArrayList<>();
    for (int x = 0; x <= 5; x++) {
      Department department = new Department();
      department.setId(String.valueOf(x));
      department.setDepartmentName("d" + x);
      User user = new User();
      user.setId(String.valueOf(x));
      user.setUserName("u" + x);
      user.setDepartment(department);
      department.setUser(user);
      departments.add(department);
    }
    return new ResponseModel(ResponseStatus.OK, departments);
  }
}
