package cn.aethli.filter.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import java.util.List;

/** @author Termite */
public class MultipleExcludeFilter extends SimpleBeanPropertyFilter {

  final List<String> names;

  protected MultipleExcludeFilter(List<String> names) {
    super();
    this.names = names;
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
    if (names.contains(fullNameBuilder.toString())) {
      return;
    }
    super.serializeAsField(pojo, gen, prov, writer);
  }
}
