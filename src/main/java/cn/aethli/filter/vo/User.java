package cn.aethli.filter.vo;

import lombok.Data;

/** @author Termite */
@Data
public class User {
  private String id;
  private String userName;
  private Department department;
}
