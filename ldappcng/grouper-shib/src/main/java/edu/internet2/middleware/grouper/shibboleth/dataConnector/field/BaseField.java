/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.shibboleth.dataConnector.field;

import java.util.List;

import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class BaseField {

  /** the identifier delimiter */
  public static final String DELIMITER = ":";

  /** the identifier */
  private String id;

  /** the identifier as its component elements */
  private List<String> idElements;

  /**
   * Constructs a representation of an attribute with the given id.
   * 
   * Valid ids are the following :
   * 
   * <pre class="eg">
   * admins
   * optins
   * optouts
   * readers
   * updaters
   * viewers
   * members
   * members:all
   * members:immediate
   * members:effective
   * members:composite
   * members:all:customList
   * members:immediate:customList
   * members:effective:customList
   * members:composite:customList
   * groups
   * groups:all
   * groups:immediate
   * groups:effective
   * groups:composite
   * groups:all:customList
   * groups:immediate:customList
   * roups:effective:customList
   * groups:composite:customList
   * </pre>
   * 
   * @param id
   *          the identifier
   * 
   * @throws GrouperException
   *           if the identifier is invalid
   */
  public BaseField(String id) throws GrouperException {
    if (id == null || id.equals("")) {
      throw new GrouperException("The id must not be null nor empty.");
    }
    this.id = id;
    this.idElements = GrouperUtil.splitTrimToList(id, BaseField.DELIMITER);
    if (idElements.size() > 3) {
      throw new GrouperException("Invalid id '" + id + "', there should be a maximum of 3 elements.");
    }
  }

  /**
   * Get the attribute id.
   * 
   * @return the name of the underlying attribute
   */
  public String getId() {
    return id;
  }

  /**
   * Get the identifier as a list of component elements.
   * 
   * @return the identifier as a list of component elements
   */
  public List<String> getIdElements() {
    return idElements;
  }

  /**
   * Get the first identifier element. Should never be null. Should be "groups", "members", or an
   * {@link AccessPrivilege} name.
   * 
   * @return "groups", "members", or an {@link AccessPrivilege} name
   */
  public String getFirstIdElement() {
    return idElements.get(0);
  }

  /**
   * Get the possibly null second identifier element. If the first id element is "groups" or "members", then the second
   * element should be one of "all", "immediate", "effective" or "composite".
   * 
   * @return "all", "immediate", "effective" or "composite" or null
   */
  public String getSecondIdElement() {

    return idElements.get(1);
  }

  /**
   * Get the possibly null third identifier element which is the name of the custom list field.
   * 
   * @return the name of the custom list field or null
   */
  public String getThirdIdElement() {
    return idElements.get(2);
  }
}
