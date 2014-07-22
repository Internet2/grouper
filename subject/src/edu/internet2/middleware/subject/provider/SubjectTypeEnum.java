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
/*--
$Id: SubjectTypeEnum.java,v 1.3 2009-03-22 02:49:27 mchyzer Exp $
$Date: 2009-03-22 02:49:27 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject.provider;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import edu.internet2.middleware.subject.SubjectType;

/**
 * SubjectType enum for person, group, and organization.
 * 
 */
public class SubjectTypeEnum extends SubjectType {

  /** */
  protected static final List<SubjectType> PRIVATE_VALUES = new ArrayList<SubjectType>();

  /** */
  public static final List<SubjectType> VALUES = Collections.unmodifiableList(PRIVATE_VALUES);

  /** */
  public static final SubjectTypeEnum PERSON = new SubjectTypeEnum("person");

  /** */
  public static final SubjectTypeEnum GROUP = new SubjectTypeEnum("group");

  /** */
  public static final SubjectTypeEnum APPLICATION = new SubjectTypeEnum("application");

  static {
    PRIVATE_VALUES.add(PERSON);
    PRIVATE_VALUES.add(GROUP);
    PRIVATE_VALUES.add(APPLICATION);
  }

  /**
     * The name of this enum constant, as declared in the enum declaration.
     */
  private final String name;

  /**
   * (non-Javadoc)
   * Sole constructor.
   * @param name1 - 
   *    The name of this enum constant, which is the identifier used
   *    to declare it.
   */
  protected SubjectTypeEnum(String name1) {
    this.name = name1;
  }

  /**
   * (non-Javadoc)
   * @see edu.internet2.middleware.subject.SubjectType#getName()
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * Factory method for returning instance of datatype from the
   * pool of valid objects.
   * @param value 
   * @return subject type
   */
  public static SubjectType valueOf(String value) {
    if (value == null) {
      return null;
    }
    for (Iterator i = PRIVATE_VALUES.iterator(); i.hasNext();) {
      SubjectTypeEnum validValue = (SubjectTypeEnum) i.next();
      if (value.equalsIgnoreCase(validValue.getName())) {
        return validValue;
      }
    }
    throw new IllegalArgumentException("Unrecognized SubjectType '" + value
        + "', expecting one of " + PRIVATE_VALUES);
  }

}
