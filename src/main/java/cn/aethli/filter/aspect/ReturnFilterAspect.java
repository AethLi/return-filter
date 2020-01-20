package cn.aethli.filter.aspect;

import cn.aethli.filter.annotation.ReturnExclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/** @author Termite */
@Aspect
@Component
@Slf4j
public class ReturnFilterAspect {

  @Around(value = "@annotation(cn.aethli.filter.annotation.ReturnExclude)")
  public Object ReturnExcludeAfter(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ReturnExclude returnExclude = method.getAnnotation(ReturnExclude.class);
    ObjectMapper mapper = new ObjectMapper();
    // 获取名单列表
    String[] names = returnExclude.names();
    FilterProvider filterProvider =
        new SimpleFilterProvider()
            .setDefaultFilter(SimpleBeanPropertyFilter.filterOutAllExcept(names));
    mapper.setFilterProvider(filterProvider);
    Object proceed = joinPoint.proceed();
    JsonNode node = mapper.valueToTree(proceed);
    return node;
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
