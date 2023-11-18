package edu.internet2.middleware.grouper.plugins;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * we have two classes from different classloaders, and we want to dynamically translate methods across
 * @author mchyzer
 *
 */
public class DynamicClassProxy implements MethodInterceptor {
  
  /**
   * instance from another classloader
   */
  private final Object instanceThatNeedsInterface;

  /**
   * cache methods from the class
   */
  private Map<String, Method> methodMap = new HashMap<String, Method>();

  /**
   * construct based on another class, catalog the methods
   * @param providerService1
   */
  public DynamicClassProxy(Object providerService1) {
    this.instanceThatNeedsInterface = providerService1;
    for (Method method : this.instanceThatNeedsInterface.getClass().getMethods()) {
      this.methodMap.put(method.getName(), method);
    }
  }

  /**
   * intercept a method and delegate to the proxy
   * @param methodProxy 
   */
  @Override
  public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
    return this.methodMap.get(method.getName()).invoke(this.instanceThatNeedsInterface, args);
  }
}

