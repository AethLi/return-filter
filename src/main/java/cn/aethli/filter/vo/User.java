package cn.aethli.filter.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** @author Termite */
@Data
public class User {
  private String id;
  private String userName;
  @JsonIgnore
  private Department department;
}
