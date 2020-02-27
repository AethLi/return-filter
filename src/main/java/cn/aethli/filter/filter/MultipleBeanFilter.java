package cn.aethli.filter.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import java.util.List;

/** @author Termite */
public class MultipleBeanFilter extends SimpleBeanPropertyFilter {

  final List<String> names;
  final boolean eInclude;

  public MultipleBeanFilter(List<String> names, boolean eInclude) {
    super();
    this.names = names;
    this.eInclude = eInclude;
  }

  @Override
  public void serializeAsField(
      Object pojo, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer)
      throws Exception {
    // 获取"."语法的全限定名
    StringBuilder fullNameBuilder = new StringBuilder();
    fullNameBuilder.append(writer.getName());
    JsonStreamContext context = gen.getOutputContext();
    do {
      context = context.getParent();
      if (context == null) {
        break;
      }
      String name = context.toString();
      if (name.matches("(\\[).*?(\\])") || name.matches("[/]?")) {
        continue;
      }
      name = name.replaceAll("[{]?[}]?[\"]?", "");
      fullNameBuilder.insert(0, name + ".");
    } while (true);
    if (include(fullNameBuilder.toString())) {
      writer.serializeAsField(pojo, gen, prov);
    } else if (!gen.canOmitFields()) { // 占位符判断，即使无需输出，详见super的函数
      writer.serializeAsOmittedField(pojo, gen, prov);
    }
  }

  private boolean include(String s) {
    System.out.println(s);
    if (eInclude) {
      return names.stream().anyMatch(name -> name.indexOf(s) == 0);
    } else {
      return !names.contains(s);
    }
  }
}
