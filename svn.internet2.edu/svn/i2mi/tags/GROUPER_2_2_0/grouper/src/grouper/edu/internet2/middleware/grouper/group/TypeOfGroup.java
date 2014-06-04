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
/**
 * 
 */
package edu.internet2.middleware.grouper.group;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 * @author mchyzer
 */
public enum TypeOfGroup {
  
  /** group (normal group of subjects) */
  group {

    /**
     * @see TypeOfGroup#supportsField(Field)
     */
    @Override
    public boolean supportsField(Field field) {
      return true;
    }
  },
  
  /** can be assigned groups or other subjects, and also privileges */
  role {

    /**
     * @see TypeOfGroup#supportsField(Field)
     */
    @Override
    public boolean supportsField(Field field) {
      return true;
    }
  },
   
  /** can be used as a subject which is not in a subject source, e.g. a service principal, schema, server, etc */
  entity {

    /**
     * @see TypeOfGroup#supportsField(Field)
     */
    @Override
    public boolean supportsField(Field field) {
      //only access privileges, admins or viewers
      return field.getType() == FieldType.ACCESS && (StringUtils.equals(Field.FIELD_NAME_ADMINS, field.getName()) || StringUtils.equals(Field.FIELD_NAME_VIEWERS, field.getName()));
    }
  };

  /**
   * name for javabeans
   * @return the name
   */
  public String getName() {
    return this.name();
  }
  
  /** set with group or role */
  public final static Set<TypeOfGroup> GROUP_OR_ROLE_SET = Collections.unmodifiableSet(GrouperUtil.toSet(group, role));
  
  /** set with entity */
  public final static Set<TypeOfGroup> ENTITY_SET = Collections.unmodifiableSet(GrouperUtil.toSet(entity));
  
  /**
   * append the typeOfGroup part into an hql group query
   * @param groupAlias is the alias in the group hql query e.g. theGroup
   * @param typeOfGroups the set of TypeOfGroup or null for all
   * @param hql query so far
   * @param hqlQuery object to append the stored params to
   */
  public static void appendHqlQuery(String groupAlias, Set<TypeOfGroup> typeOfGroups, StringBuilder hql, HqlQuery hqlQuery) {
    if (GrouperUtil.length(typeOfGroups) > 0) {
      if (hql.indexOf(" where ") > 0) {
        String lowerTrimmed = hql.toString().trim().toLowerCase();
        if (!lowerTrimmed.endsWith(" and") && !lowerTrimmed.endsWith(" where")) {
          hql.append(" and ");
        }
      } else {
        hql.append(" where ");
      }
      hql.append(groupAlias).append(".typeOfGroupDb in ( ");
      Set<String> typeOfGroupStrings = new LinkedHashSet<String>();
      for (TypeOfGroup typeOfGroup : typeOfGroups) {
        typeOfGroupStrings.add(typeOfGroup.name());
      }
      hql.append(HibUtils.convertToInClause(typeOfGroupStrings, hqlQuery));
      hql.append(" ) ");
    }

  }
  
  /**
   * if this type of group supports this field (i.e. entities dont have READ privilege or members list)
   * @param field
   * @return true if supports
   */
  public abstract boolean supportsField(Field field);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static TypeOfGroup valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(TypeOfGroup.class, 
        string, exceptionOnNull);

  }

}
