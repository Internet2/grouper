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
 * @version $Id: GroupFinder.java,v 1.7 2005-12-03 17:46:22 blair Exp $
 */
public class GroupFinder {

  // Private Class Constants
  private static final String E_GNF = "unable to find group";


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
    GrouperSession.validate(s);
    Group g = _findByName(name);
    g.setSession(s);
    try {
      PrivilegeResolver.getInstance().canVIEW(s, g, s.getSubject());
      return g;
    }
    catch (InsufficientPrivilegeException eIP) {
      // Ignore
    }  
    throw new GroupNotFoundException(E_GNF + " by name: " + name);
  } // public static Group findByName(s, name)

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
    GrouperSession.validate(s);
    Group g = _findByUuid(uuid);
    g.setSession(s);
    try {
      PrivilegeResolver.getInstance().canVIEW(s, g, s.getSubject());
      return g;
    }
    catch (InsufficientPrivilegeException eIP) {
      // Ignore
    }  
    throw new GroupNotFoundException(E_GNF + " by uuid: " + uuid);
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
    return PrivilegeResolver.getInstance().canVIEW(s, new LinkedHashSet(groups));
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
    return PrivilegeResolver.getInstance().canVIEW(s, new LinkedHashSet(groups));
  } // protected static Set findByCreatedBefore(s, d)

  protected static Set findByAnyApproximateAttr(GrouperSession s, String val) 
    throws  QueryException
  {
    GrouperSession.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = hs.find(
        "from Attribute as a where lower(a.value) like  ?",
        "%" + val.toLowerCase() + "%",
        Hibernate.STRING
      ).iterator();
      hs.close();
      while (iter.hasNext()) {
        Group g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage()
      );
    }
    return PrivilegeResolver.getInstance().canVIEW(s, groups);
  } // protected static Set findByAnyApproximateAttr(s, val)

  protected static Set findByApproximateAttr(GrouperSession s, String attr, String val) 
    throws  QueryException
  {
    GrouperSession.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = hs.find(
        "from Attribute as a where    "
        + "a.field.name       =     ? "
        + "and lower(a.value) like  ? "
        ,
        new Object[] {
          attr, "%" + val.toLowerCase() + "%"
        },
        new Type[] {
          Hibernate.STRING, Hibernate.STRING
        }
      ).iterator();
      hs.close();
      while (iter.hasNext()) {
        Group g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage()
      );
    }
    return PrivilegeResolver.getInstance().canVIEW(s, groups);
  } // protected static Set findByApproximateAttr(s, attr, val)

  protected static Set findByApproximateName(GrouperSession s, String name) 
    throws  QueryException
  {
    GrouperSession.validate(s);
    String    approx  = "%" + name.toLowerCase() + "%";
    Set       groups  = new LinkedHashSet();
    try {
      Session   hs      = HibernateHelper.getSession();
      Iterator  iter    = hs.find(
        "from Attribute as a where                                      "
        + "( a.field.name = 'name'              and lower(a.value) like ? )"
        + " or "
        + "( a.field.name = 'displayName'       and lower(a.value) like ? )"
        + " or "
        + "( a.field.name = 'extension'         and lower(a.value) like ? )"
        + " or " 
        + "( a.field.name = 'displayExtension'  and lower(a.value) like ? )"
        ,
        new Object[] {
          approx, approx, approx, approx
        },
        new Type[] {
          Hibernate.STRING, Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
        }
      ).iterator();
      hs.close();
      while (iter.hasNext()) {
        Group g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage()
      );
    }
    return PrivilegeResolver.getInstance().canVIEW(s, groups);
  } // protected static Set findByApproximateName(s, name)


  // Private Class Methods
  private static Group _findByName(String name)
    throws  GroupNotFoundException
  {
    try {
      Group   g       = null;
      Session hs      = HibernateHelper.getSession();
      List    attrs   = hs.find(
                          "from Attribute as a where  "
                          + "a.field.name = 'name'    "
                          + "and a.value  = ?         ",
                          name,
                          Hibernate.STRING
                        )
                        ;
      if (attrs.size() == 1) {
        g = ( (Attribute) attrs.get(0) ).getGroup();
      }
      hs.close();
      if (g == null) {
        throw new GroupNotFoundException(E_GNF + " by name: " + name);
      }
      return g; 
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(
        E_GNF + " by name: " + name + "(" + eH.getMessage() + ")"
      );
    }
  } // private static Group _findByName(name)

  private static Group _findByUuid(String uuid)
    throws  GroupNotFoundException
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
        throw new GroupNotFoundException(E_GNF + " by uuid: " + uuid);
      }
      return g; 
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(
        E_GNF + " by uuid: " + uuid + "(" + eH.getMessage() + ")"
      );  
    }
  } // private static Group _findByUuid(uuid)

}

