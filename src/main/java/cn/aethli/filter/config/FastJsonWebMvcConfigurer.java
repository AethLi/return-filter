//package cn.aethli.filter.config;
//
//import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
//import java.util.List;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * 使用FastJson代替Jackson来处理 @RequestBody,@ResponseBody,@RestController等
// *
// * @author Termite
// */
//@Configuration
//public class FastJsonWebMvcConfigurer implements WebMvcConfigurer {
//  @Override
//  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//    FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
//    converters.add(0, converter);
//  }
//}
