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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;


/**
 * This object is able to have attributes assigned
 */
public interface AttributeAssignable {

  /**
   * get the logic delegate
   * @return the delegate
   */
  public AttributeAssignBaseDelegate getAttributeDelegate();
  
  /**
   * deal directly with attribute values
   * @return the delegate to deal with attribute values
   */
  public AttributeValueDelegate getAttributeValueDelegate();
}
