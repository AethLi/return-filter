package cn.aethli.filter.vo;

import lombok.Data;

/** @author Termite */
@Data
public class Department {
  private String id;
  private String departmentName;
  private User user;
}
