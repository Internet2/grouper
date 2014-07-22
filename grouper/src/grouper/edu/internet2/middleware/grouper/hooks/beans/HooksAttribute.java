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
 * $Id: HooksAttribute.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;


/**
 * attribute, and if threadsafe
 */
public class HooksAttribute {

  /**
   * if this is threadsafe
   */
  private boolean threadSafe;
  
  /**
   * value of the attribute
   */
  private Object value;

  /**
   * @param threadSafe
   * @param value
   */
  public HooksAttribute(boolean threadSafe, Object value) {
    super();
    this.threadSafe = threadSafe;
    this.value = value;
  }

  /**
   * @return the threadSafe
   */
  boolean isThreadSafe() {
    return this.threadSafe;
  }

  
  /**
   * @return the value
   */
  Object getValue() {
    return this.value;
  }

  
}
