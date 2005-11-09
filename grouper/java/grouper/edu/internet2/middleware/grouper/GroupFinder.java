/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;

import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;


/**
 * Find groups within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupFinder.java,v 1.1.2.9 2005-11-09 23:20:03 blair Exp $
 */
public class GroupFinder {

  // Public Instance Methods

  /**
   * Find a group within the registry by name.
   * <pre class="eg">
   * try {
   *   Group g = GroupFinder.findByName(name);
   * }
   * catch (GroupNotFoundException e) {
   *   // Group not found
   * }
   * </pre>
   * @param   s     Find group within this session context.
   * @param   name  Name of group to find.
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public static Group findByName(GrouperSession s, String name) 
    throws GroupNotFoundException
  {
    Group g = findByName(name);
    g.setSession(s);
    return g;
  }

  /**
   * Find a group within the registry by UUID.
   * <pre class="eg">
   * try {
   *   Group g = GroupFinder.findByUuid(uuid);
   * }
   * catch (GroupNotFoundException e) {
   *   // Group not found
   * }
   * </pre>
   * @param   s     Find group within this session context.
   * @param   uuid  UUID of group to find.
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public static Group findByUuid(GrouperSession s, String uuid) 
    throws GroupNotFoundException
  {
    Group g = findByUuid(uuid);
    g.setSession(s);
    return g;
  } // public static Group findByUuid(s, uuid)


  // Protected Class Methods

  // @return  groups created after this date
  protected static Set findByCreatedAfter(GrouperSession s, Date d) 
    throws QueryException 
  {
    List groups = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Group as g where  "
                      + "g.create_time > ?    ",
                      new Long(d.getTime()),
                      Hibernate.LONG
                    )
                    ;
      hs.close();
      groups.addAll( Group.setSession(s, l) );
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage()
      );  
    }
    return new LinkedHashSet(groups);
  } // protected static Set findByCreatedAfter(s, d)

  // @return  groups created before this date
  protected static Set findByCreatedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    List groups = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Group as g where  "
                      + "g.create_time < ?    ",
                      new Long(d.getTime()),
                      Hibernate.LONG
                      )
                      ;
      hs.close();
      groups.addAll( Group.setSession(s, l) );
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage()
      );  
    }
    return new LinkedHashSet(groups);
  } // protected static Set findByCreatedBefore(s, d)

  protected static Group findByName(String name)
    throws GroupNotFoundException
  {
    try {
      Group   g       = null;
      Session hs      = HibernateHelper.getSession();
      List    groups  = hs.find(
                          "from Group as g where  "
                          + "g.group_name = ?     ",
                          name,
                          Hibernate.STRING
                        )
                        ;
      if (groups.size() == 1) {
        g = (Group) groups.get(0);
      }
      hs.close();
      if (g == null) {
        throw new GroupNotFoundException("group not found");
      }
      return g; 
    }
    catch (HibernateException e) {
      throw new GroupNotFoundException(
        "error finding group: " + e.getMessage()
      );  
    }
  } // protected static Group findByName(s, name)

  protected static Group findByUuid(String uuid)
    throws GroupNotFoundException
  {
    try {
      Group   g       = null;
      Session hs      = HibernateHelper.getSession();
      List    groups  = hs.find(
                          "from Group as g where  "
                          + "g.group_id = ?       ",
                          uuid,
                          Hibernate.STRING
                        )
                        ;
      if (groups.size() == 1) {
        g = (Group) groups.get(0);
      }
      hs.close();
      if (g == null) {
        throw new GroupNotFoundException("group not found");
      }
      return g; 
    }
    catch (HibernateException e) {
      throw new GroupNotFoundException(
        "error finding group: " + e.getMessage()
      );  
    }
  } // protected static Group findByUuid(s, uuid)

}

