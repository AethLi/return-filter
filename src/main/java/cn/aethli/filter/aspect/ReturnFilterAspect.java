package cn.aethli.filter.aspect;

import cn.aethli.filter.annotation.ReturnExclude;
import cn.aethli.filter.annotation.ReturnInclude;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** @author Termite */
@Aspect
@Component
@Slf4j
public class ReturnFilterAspect {

  private final ObjectMapper defaultMapper;

  @Autowired
  public ReturnFilterAspect(ObjectMapper defaultMapper) {
    this.defaultMapper = defaultMapper;
  }

  @Around(value = "@annotation(cn.aethli.filter.annotation.ReturnExclude)")
  public Object ReturnExcludeAfter(ProceedingJoinPoint joinPoint) throws Throwable {
    ObjectMapper mapper = defaultMapper.copy();
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ReturnExclude returnExclude = method.getAnnotation(ReturnExclude.class);
    // 获取名单列表
    List<String> names = Arrays.asList(returnExclude.names());
    //    FilterProvider filterProvider =
    //        new SimpleFilterProvider()
    //            .addFilter("ResponseModel", SimpleBeanPropertyFilter.serializeAllExcept(names));
    //    mapper.setFilterProvider(filterProvider);
    //    mapper.addMixIn(Object.class, DataMixIn.class);
    FilterProvider filterProvider =
        new SimpleFilterProvider()
            .addFilter(
                "filter",
                new SimpleBeanPropertyFilter() {
                  @Override
                  public void serializeAsField(
                      Object pojo,
                      JsonGenerator gen,
                      SerializerProvider prov,
                      PropertyWriter writer)
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
                });
    mapper.setFilterProvider(filterProvider);
    mapper.addMixIn(Object.class, DataMixIn.class);

    //    SimpleModule module =
    //        new SimpleModule() {
    //          @Override
    //          public String getModuleName() {
    //            return "multi_layer_filter";
    //          }
    //
    //          @Override
    //          public Version version() {
    //            return VersionUtil.parseVersion(
    //                "2.10.1", "com.fasterxml.jackson.datatype", "jackson-datatype-jdk8");
    //          }
    //
    //          @Override
    //          public void setupModule(SetupContext context) {
    //            super.setupModule(context);
    //            BeanSerializerModifier modifier =
    //                new BeanSerializerModifier() {
    //                  @Override
    //                  public List<BeanPropertyWriter> changeProperties(
    //                      SerializationConfig config,
    //                      BeanDescription beanDesc,
    //                      List<BeanPropertyWriter> beanProperties) {
    //                    return super.changeProperties(config, beanDesc, beanProperties);
    //                  }
    //                };
    //            context.addBeanSerializerModifier(modifier);
    //          }
    //        };
    //    mapper.registerModule(module);

    Object proceed = joinPoint.proceed();
    return mapper.valueToTree(proceed);
  }

  @Around(value = "@annotation(cn.aethli.filter.annotation.ReturnInclude)")
  public Object ReturnIncludeAfter(ProceedingJoinPoint joinPoint) throws Throwable {
    ObjectMapper mapper = defaultMapper.copy();
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ReturnInclude returnInclude = method.getAnnotation(ReturnInclude.class);
    String[] names = returnInclude.names();
    FilterProvider filterProvider =
        new SimpleFilterProvider()
            .addFilter("ResponseModel", SimpleBeanPropertyFilter.filterOutAllExcept(names));
    mapper.setFilterProvider(filterProvider);
    mapper.addMixIn(Object.class, DataMixIn.class);
    Object proceed = joinPoint.proceed();
    return mapper.valueToTree(proceed);
  }

  @JsonFilter("filter")
  static class DataMixIn {}
}
