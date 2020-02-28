package cn.aethli.filter.controller;

import cn.aethli.filter.commen.enums.ResponseStatus;
import cn.aethli.filter.model.ResponseModel;
import cn.aethli.filter.vo.Department;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseController {

  private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

  protected <T extends Department> ResponseModel buildHttpResult(
      Iterable<T> page, String... properties) {
    ResponseModel result = new ResponseModel(ResponseStatus.OK);

    if (page == null || properties == null) {
      result.setData(page);
      return result;
    }

    page.forEach(
        item -> {
          filterProperties(item, properties);
        });

    result.setData(page);
    return result;
  }

  protected <T extends Department> ResponseModel buildHttpResult(
      Collection<T> entities, String... properties) {
    ResponseModel result = new ResponseModel(ResponseStatus.OK);

    if (entities == null || entities.isEmpty() || properties == null) {
      result.setData(entities);
      return result;
    }

    for (T entity : entities) {
      filterProperties(entity, properties);
    }

    result.setData(entities);
    return result;
  }

  protected <T extends Department> ResponseModel buildHttpResult(T entity, String... properties) {
    ResponseModel result = new ResponseModel(ResponseStatus.OK);
    if (entity == null || properties == null) {
      return result;
    }

    // 过滤
    this.filterProperties(entity, properties);

    result.setData(entity);
    return result;
  }

  protected <T extends Department> void filterProperties(T entity, String... properties) {
    if (entity == null || properties == null) {
      return;
    }

    /*
     * 首先要对初始输入的属性列表进行初步处理： 1、排序 2、确定这个属性是在第几层对象中
     */
    // 1、排序
    Arrays.sort(properties);

    // 2、递归排除指定的属性
    Stack<Class<?>> stackClasses = new Stack<>();
    try {
      for (String property : properties) {
        stackClasses.push(entity.getClass());
        filterProperty(property, entity, stackClasses);
        stackClasses.pop();
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private void filterProperty(String property, Object currentObject, Stack<Class<?>> stackClasses)
      throws NoSuchMethodException, SecurityException, IllegalAccessException,
          IllegalArgumentException, InvocationTargetException {
    // 如果条件成立，说明还要进入下一级对象，否则就是操作本级对象
    int nodeIndex;
    if ((nodeIndex = property.indexOf(".")) != -1) {
      String currentFieldName = property.substring(0, nodeIndex);
      String nextFieldName = property.substring(nodeIndex + 1);
      Field currentField;
      Class<?> fieldClass;
      Class<?> currentClass;
      try {
        currentField = this.findField(currentFieldName, currentObject.getClass());
        if (currentField == null) {
          return;
        }
        fieldClass = currentField.getType();
        currentClass = currentObject.getClass();
      } catch (NoSuchFieldException | SecurityException e) {
        throw new IllegalArgumentException(
            "not found property: "
                + currentFieldName
                + " in object "
                + currentObject.getClass().getName());
      }

      // 取得下一级对象
      char[] chars = currentFieldName.toCharArray();
      chars[0] -= 32;
      Method getMethod = currentClass.getMethod("get" + String.valueOf(chars));
      Object nextObject = getMethod.invoke(currentObject);

      /*
       * 那么是不是进入内部呢？还要以以下判断条件为准: 1、这个属性必须是UuidEntity的子类 2、这个属性本来不为null 3、这个属性所对应的类没有在已进入的递归列表中
       */
      // 如果条件成立，说明是单一对象
      if (nextObject instanceof Department && !stackClasses.contains(fieldClass)) {
        stackClasses.push(fieldClass);
        filterProperty(nextFieldName, nextObject, stackClasses);
        stackClasses.pop();
      }
      // 如果条件成立，说明这个属性是一个集合
      else if (nextObject instanceof Collection) {
        Collection<?> collections = (Collection<?>) nextObject;
        for (Object propertyObject : collections) {
          Class<?> propertyClass = propertyObject.getClass();
          if (!(propertyObject instanceof Department)) {
            break;
          }
          stackClasses.push(propertyClass);
          filterProperty(nextFieldName, propertyObject, stackClasses);
          stackClasses.pop();
        }
      }
    }
    // 就在本级对象进行属性排除
    else {
      Field currentField;
      Class<?> fieldClass;
      Class<?> currentClass;
      try {
        currentField = this.findField(property, currentObject.getClass());
        if (currentField == null) {
          return;
        }
        fieldClass = currentField.getType();
        currentClass = currentObject.getClass();
      } catch (NoSuchFieldException | SecurityException e) {
        throw new IllegalArgumentException(
            "not found property: " + property + " in object " + currentObject.getClass().getName());
      }

      // 如果执行到这里，就可以将属性设置为null了
      char[] chars = property.toCharArray();
      chars[0] -= 32;
      Method getMethod = currentClass.getMethod("set" + String.valueOf(chars), fieldClass);
      getMethod.invoke(currentObject, new Object[] {null});
    }
  }

  private Field findField(String currentFieldName, Class<?> targetClass)
      throws NoSuchFieldException {
    Field currentField = null;
    try {
      currentField = targetClass.getDeclaredField(currentFieldName);
    } catch (NoSuchFieldException | SecurityException e) {

    }

    if (currentField == null) {
      Class<?> superClass = targetClass.getSuperclass();
      if (superClass != null) {
        return this.findField(currentFieldName, superClass);
      } else {
        throw new NoSuchFieldException(
            "not found property " + currentFieldName + " in class " + targetClass.getSimpleName());
      }
    }

    return currentField;
  }
}
