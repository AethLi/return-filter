package cn.aethli.filter.aspect;

import cn.aethli.filter.annotation.ReturnExclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

  final ObjectMapper jacksonObjectMapper;

  @Autowired
  public ReturnFilterAspect(ObjectMapper jacksonObjectMapper) {
    this.jacksonObjectMapper = jacksonObjectMapper;
  }

  @Around(value = "@annotation(cn.aethli.filter.annotation.ReturnExclude)")
  public Object ReturnExcludeAfter(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    ReturnExclude returnExclude = method.getAnnotation(ReturnExclude.class);

    // 获取名单列表
    String[] names = returnExclude.names();
    ObjectMapper mapper = jacksonObjectMapper;
    SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
    String filterId = UUID.randomUUID().toString();
    simpleFilterProvider.addFilter(filterId,
        SimpleBeanPropertyFilter.filterOutAllExcept(names));

    mapper.setFilterProvider(simpleFilterProvider);
    //    mapper.setMixIns()
    //    return joinPoint.proceed();
    Object proceed = joinPoint.proceed();
    return mapper.writeValueAsString(proceed);
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
    return null;
  }
}
