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
/**
 * @author mchyzer
 * $Id: GrouperSetElement.java,v 1.1 2009-09-16 08:52:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperSet;


/**
 *
 */
public interface GrouperSetElement {

  /**
   * name of this object (for logging)
   * @return name
   */
  public String __getName();
  
  /**
   * if of this object
   * @return id
   */
  public String __getId();
  
}
