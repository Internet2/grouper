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
/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.synchronize;

import javax.naming.InvalidNameException;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.LdapUtil;

/**
 * This is an AttributeModifier for modifying LDAP attribute values that are known to hold
 * DN strings. This class currently assumes that the "no value" and the values it is
 * initialized with via the attribute are valid DN strings. No validation is currently
 * done to enforce this.
 */
public class DnAttributeModifier extends AttributeModifier {

  /**
   * Constructs a <code>DnAttributeModifier</code> for the attribute name without a
   * "no value".
   * 
   * @param attributeName
   *          Name of the attribute
   */
  public DnAttributeModifier(String attributeName) {
    this(attributeName, DEFAULT_NO_VALUE);
  }

  /**
   * Constructs a <code>DnAttributeModifier</code> for the attribute name with the given
   * "no value" value.
   * 
   * @param attributeName
   *          Name of the attribute
   * @param noValue
   *          "no value" value (null if the attribute is not required).
   */
  public DnAttributeModifier(String attributeName, String noValue) {
    super(attributeName, noValue);
  }

  /**
   * {@inheritDoc}
   * 
   * The dn returned is lowercase.
   */
  protected String makeComparisonString(String value) {

    try {
      return LdapUtil.canonicalizeDn(value).toLowerCase();
    } catch (InvalidNameException e) {
      throw new LdappcException(e);
    }
  }
}
