/**
 * 
 */
package edu.internet2.middleware.grouper.group;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 * @author mchyzer
 */
public enum TypeOfGroup {
  
  /** group (normal group of subjects) */
  group,
  
  /** can be assigned groups or other subjects, and also privileges */
  role;

  /** set with group or role */
  public final static Set<TypeOfGroup> GROUP_OR_ROLE_SET = Collections.unmodifiableSet(GrouperUtil.toSet(group, role));

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
        hql.append(" and ");
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
