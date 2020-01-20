package cn.aethli.filter.commen.enums;

/** @author 93162 */
public enum ResponseStatus {
  OK("正常"),
  FAIL("失败"),
  ERROR("错误");
  private String desc;
  private int value;

  ResponseStatus(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return this.desc;
  }
}
