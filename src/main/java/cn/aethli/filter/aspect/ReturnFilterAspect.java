package cn.aethli.filter.aspect;

import cn.aethli.filter.annotation.ReturnExclude;
import cn.aethli.filter.model.ResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ReturnExclude returnExclude = method.getAnnotation(ReturnExclude.class);
    ObjectMapper mapper = defaultMapper.copy();
    // 获取名单列表
    String[] names = returnExclude.names();
    FilterProvider filterProvider =
        new SimpleFilterProvider()
            .addFilter("ResponseModel",SimpleBeanPropertyFilter.serializeAllExcept(names));
    mapper.setFilterProvider(filterProvider);
    Object proceed = joinPoint.proceed();
    return mapper.valueToTree(proceed);
  }

  public Map<String, Object> convert(String[] names) {
    Map<String, Object> m = new HashMap<>();
    for (String name : names) {
      String[] splits = name.split(".");
      if (splits.length == 1) {
        m.put(splits[0], splits[0]);
      } else if (splits.length > 1) {
        m.put(splits[0], convert(splits));
      }
    }
    return m;
  }
}
