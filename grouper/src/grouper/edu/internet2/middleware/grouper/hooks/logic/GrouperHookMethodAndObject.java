/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: GrouperHookMethodAndObject.java,v 1.1 2008-07-10 05:55:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.lang.reflect.Method;


/**
 * structure to hold method and class to be called for hook
 */
public class GrouperHookMethodAndObject {

  /**
   * method, cached
   */
  private Method hookMethod = null;
  
  /**
   * isntance of logic class, cached
   */
  private Object hookLogicInstance = null;

  /**
   * @param hookMethod
   * @param hookLogicInstance
   */
  public GrouperHookMethodAndObject(Method hookMethod, Object hookLogicInstance) {
    super();
    this.hookMethod = hookMethod;
    this.hookLogicInstance = hookLogicInstance;
  }

  
  /**
   * @return the hookMethod
   */
  public Method getHookMethod() {
    return this.hookMethod;
  }

  
  /**
   * @return the hookLogicInstance
   */
  public Object getHookLogicInstance() {
    return this.hookLogicInstance;
  }
  
  
}
