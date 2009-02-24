/*
 * @author mchyzer
 * $Id: GroupQuery.java,v 1.1.2.1 2009-02-24 19:19:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.poc;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class GroupQuery {

  /** */
  private static final String KLASS = GroupQuery.class.getName();

  /**
   * @param args
   */
  public static void main(String[] args) {
    List<Group> groups = HibernateSession.byHqlStatic().createQuery(
        "select g from Group as g, Attribute as a, Field as field " +
        "where a.groupUuid = g.uuid " +
        "and field.uuid = a.fieldId and field.name = :field and lower(a.value) like :value"
      ).setString("field", "name")
      .setString( "value", "%Group%".toLowerCase() ).list(Group.class);
    System.out.println(GrouperUtil.toStringForLog(groups));
  }

}
