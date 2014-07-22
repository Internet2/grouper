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
 * $Id: GroupQuery.java,v 1.2 2009-03-15 08:18:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.poc;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 *
 */
public class GroupQuery {

  /** */
  @SuppressWarnings("unused")
  private static final String KLASS = GroupQuery.class.getName();

  /**
   * @param args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    List<Group> groups = HibernateSession.byHqlStatic().createQuery(
        "select g from Group as g, Attribute as a, Field as field " +
        "where a.groupUuid = g.uuid " +
        "and field.uuid = a.fieldId and field.name = :field and lower(a.value) like :value"
      ).setString("field", "name")
      .setString( "value", "%Group%".toLowerCase() ).list(Group.class);
//    System.out.println(GrouperUtil.toStringForLog(groups));
  }

}
