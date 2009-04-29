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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.Name;
import javax.naming.NameParser;

/**
 * This is an AttributeModifier for modifying LDAP attribute values that are known to hold
 * DN strings. This class currently assumes that the "no value" and the values it is
 * initialized with via the attribute are valid DN strings. No validation is currently
 * done to enforce this.
 */
public class DnAttributeModifier extends AttributeModifier {

  /**
   * Name parser to use for converting DN strings to Name objects.
   */
  private NameParser parser;

  /**
   * Constructs a <code>DnAttributeModifier</code> for the attribute name without a
   * "no value".
   * 
   * @param parser
   *          Name parser
   * @param attributeName
   *          Name of the attribute
   */
  public DnAttributeModifier(NameParser parser, String attributeName) {
    this(parser, attributeName, DEFAULT_NO_VALUE);
  }

  /**
   * Constructs a <code>DnAttributeModifier</code> for the attribute name with the given
   * "no value" value.
   * 
   * @param parser
   *          Name parser
   * @param attributeName
   *          Name of the attribute
   * @param noValue
   *          "no value" value (null if the attribute is not required).
   */
  public DnAttributeModifier(NameParser parser, String attributeName, String noValue) {
    super(attributeName, noValue);
    this.parser = parser;
  }

  /**
   * {@inheritDoc}
   */
  protected String makeComparisonString(String value) {
    Name name = null;
    try {
      name = parser.parse(value.toLowerCase());
      if (name.size() == 0) {
        return "";
      }
      String rdn = name.get(name.size() - 1);
      String[] parts = rdn.split("\\+");
      List<String> list = new ArrayList<String>();
      for (String element : parts) {
        list.add(element);
      }
      Collections.sort(list);
      rdn = list.get(0);
      for (int i = 1; i < list.size(); i++) {
        rdn = rdn + "+" + list.get(i);
      }
      name.remove(name.size() - 1);
      name.add(rdn);
      return name.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return value.toLowerCase();
    }
  }
}
