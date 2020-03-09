package cn.aethli.filter.aspect;

import cn.aethli.filter.annotation.ReturnExclude;
import cn.aethli.filter.annotation.ReturnInclude;
import cn.aethli.filter.filter.MultipleBeanFilter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
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
    // 获取目标方法的注解参数
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ReturnExclude returnExclude = method.getAnnotation(ReturnExclude.class);
    // 获取名单列表
    List<String> names = Arrays.asList(returnExclude.names());
    return invoke(joinPoint, false, names);
  }

  @Around(value = "@annotation(cn.aethli.filter.annotation.ReturnInclude)")
  public Object ReturnIncludeAfter(ProceedingJoinPoint joinPoint) throws Throwable {
    // 获取目标方法的注解参数
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ReturnInclude returnInclude = method.getAnnotation(ReturnInclude.class);
    // 获取名单列表
    List<String> names = Arrays.asList(returnInclude.names());
    return invoke(joinPoint, true, names);
  }

  private Object invoke(ProceedingJoinPoint joinPoint, boolean include, List<String> names)
      throws Throwable {
    // 复制一份容器中的objectMapper
    ObjectMapper mapper = defaultMapper.copy();
    // 添加过滤器
    FilterProvider filterProvider =
        new SimpleFilterProvider().addFilter("filter", new MultipleBeanFilter(names, include));
    mapper.setFilterProvider(filterProvider);
    mapper.addMixIn(Object.class, DataMixIn.class);

    Object proceed = joinPoint.proceed();
//    return mapper.valueToTree(proceed);
        return mapper.writeValueAsString(proceed);
  }

  @JsonFilter("filter")
  static class DataMixIn {}
}
