package cn.aethli.filter.controller;

import cn.aethli.filter.annotation.ReturnExclude;
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
public class ReturnTestController {

  @GetMapping
  @ReturnExclude(names = {"msg","data.id","data.user.id"})
  public Object lockTest() {
    List<Department> departments = new ArrayList<>();
    for (int x = 0; x <= 5; x++) {
      Department department = new Department();
      department.setId(String.valueOf(x));
      department.setDepartmentName("d" + x);
      User user = new User();
      user.setId(String.valueOf(x));
      user.setUserName("u" + x);
      department.setUser(user);
      departments.add(department);
    }
    return new ResponseModel(ResponseStatus.OK, departments);
    //    return new ResponseModel(ResponseStatus.OK, "sadfasdf");
  }
}
