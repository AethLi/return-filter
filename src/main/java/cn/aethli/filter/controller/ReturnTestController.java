package cn.aethli.filter.controller;

import cn.aethli.filter.annotation.ReturnExclude;
import cn.aethli.filter.commen.enums.ResponseStatus;
import cn.aethli.filter.model.ResponseModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** @author Termite */
@RestControllerAdvice
@RequestMapping("return/test")
public class ReturnTestController {

  @GetMapping
  @ReturnExclude(names = {"msg"})
  public Object lockTest() {
    return new ResponseModel(ResponseStatus.OK);
  }
}
