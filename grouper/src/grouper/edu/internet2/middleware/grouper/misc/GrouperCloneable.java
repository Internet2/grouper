/**
 * Copyright 2012 Internet2
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
 * $Id: GrouperCloneable.java,v 1.1 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;


/**
 * Implement this interface if the object is cloneable
 */
public interface GrouperCloneable  {

  /** 
   * clone an object (deep clone, on fields that make sense)
   * @see Object#clone()
   * @return the clone of the object
   */
  public Object clone();
  
}
