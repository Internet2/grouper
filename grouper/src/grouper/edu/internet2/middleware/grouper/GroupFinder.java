/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupFinder.java,v 1.23 2006-08-22 18:58:10 blair Exp $
 */
public class GroupFinder {

  // PUBLIC INSTANCE METHODS //

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
    Group g = findByName(name);
    g.setSession(s);
    if (PrivilegeResolver.canVIEW(GrouperSession.startTransient(), g, s.getSubject())) {
      return g;
    }
    ErrorLog.error(GroupFinder.class, E.GF_FBNAME + E.CANNOT_VIEW);
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + name);
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
    if (PrivilegeResolver.canVIEW(GrouperSession.startTransient(), g, s.getSubject())) {
      return g;
    }
    ErrorLog.error(GroupFinder.class, E.GF_FBUUID + E.CANNOT_VIEW);
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by uuid: " + uuid);
  } // public static Group findByUuid(s, uuid)


  // PROTECTED CLASS METHODS //

  // @return  groups created after this date
  protected static Set findByCreatedAfter(GrouperSession s, Date d) 
    throws QueryException 
  {
    List groups = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.create_time > :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBCA);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
      hs.close();
      Group     g;
      Iterator  iter  = l.iterator();
      while (iter.hasNext()) {
        g = (Group) iter.next();
        g.setSession(s);
        groups.add(g);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );  
    }
    return PrivilegeResolver.canViewGroups(s, new LinkedHashSet(groups));
  } // protected static Set findByCreatedAfter(s, d)

  // @return  groups created before this date
  protected static Set findByCreatedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    List groups = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.create_time < :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBCB);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
      hs.close();
      Group     g;   
      Iterator  iter  = l.iterator();
      while (iter.hasNext()) {
        g = (Group) iter.next();
        g.setSession(s);
        groups.add(g);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );  
    }
    return PrivilegeResolver.canViewGroups(s, new LinkedHashSet(groups));
  } // protected static Set findByCreatedBefore(s, d)

  protected static Set findByAnyApproximateAttr(GrouperSession s, String val) 
    throws  QueryException
  {
    GrouperSession.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Attribute as a where lower(a.value) like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBAAA);
      qry.setString("value", "%" + val.toLowerCase() + "%");
      Group     g;
      Iterator  iter  = qry.iterate();
      while (iter.hasNext()) {
        g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );
    }
    return PrivilegeResolver.canViewGroups(s, groups);
  } // protected static Set findByAnyApproximateAttr(s, val)

  protected static Set findByApproximateAttr(GrouperSession s, String attr, String val) 
    throws  QueryException
  {
    GrouperSession.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Query     qry   = hs.createQuery(
        "from Attribute as a where "
        + "a.field.name = :field and lower(a.value) like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBAA);
      qry.setString("field", attr);
      qry.setString("value", "%" + val.toLowerCase() + "%");
      Group     g;
      Iterator  iter  = qry.iterate();
      while (iter.hasNext()) {
        g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );
    }
    return PrivilegeResolver.canViewGroups(s, groups);
  } // protected static Set findByApproximateAttr(s, attr, val)

  protected static Set findByApproximateName(GrouperSession s, String name) 
    throws  QueryException
  {
    GrouperSession.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Query     qry   = hs.createQuery(
        "from Attribute as a where "
        + "   (a.field.name = 'name'              and lower(a.value) like :value) "
        + "or (a.field.name = 'displayName'       and lower(a.value) like :value) "
        + "or (a.field.name = 'extension'         and lower(a.value) like :value) "
        + "or (a.field.name = 'displayExtension'  and lower(a.value) like :value)"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBAN);
      qry.setString("value", "%" + name.toLowerCase() + "%");
      Group     g;
      Iterator  iter  = qry.iterate();
      while (iter.hasNext()) {
        g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );
    }
    return PrivilegeResolver.canViewGroups(s, groups);
  } // protected static Set findByApproximateName(s, name)

  // @return  groups modifed after this date
  // @since   1.1.0
  protected static Set findByModifiedAfter(GrouperSession s, Date d) 
    throws QueryException 
  {
    List groups = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.modify_time > :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBMA);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
      hs.close();
      Group     g;
      Iterator  iter  = l.iterator();
      while (iter.hasNext()) {
        g = (Group) iter.next();
        g.setSession(s);
        groups.add(g);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );  
    }
    return PrivilegeResolver.canViewGroups(s, new LinkedHashSet(groups));
  } // protected static Set findByModifiedAfter(s, d)

  // @return  groups modifed before this date
  // @since   1.1.0
  protected static Set findByModifiedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    List groups = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.modify_time < :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBMB);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
      hs.close();
      Group     g;   
      Iterator  iter  = l.iterator();
      while (iter.hasNext()) {
        g = (Group) iter.next();
        g.setSession(s);
        groups.add(g);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );  
    }
    return PrivilegeResolver.canViewGroups(s, new LinkedHashSet(groups));
  } // protected static Set findByModifiedBefore(s, d)

  // Needs _protected_ access so that _Stem.addChildGroup()_ can check
  // to see if the group exists before creating it.
  // @caller: Stem.addChildGroup()
  protected static Group findByName(String name)
    throws  GroupNotFoundException
  {
    try {
      Group   g     = null;
      Session hs    = HibernateHelper.getSession();
      Query   qry   = hs.createQuery(
        "from Attribute as a where "
        + "a.field.name = 'name' and a.value = :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBN);
      qry.setString("value", name);
      List    attrs = qry.list();
      if (attrs.size() == 1) {
        g = ( (Attribute) attrs.get(0) ).getGroup();
      }
      hs.close();
      if (g == null) {
        throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + name);
      }
      return g; 
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(
        E.GROUP_NOTFOUND + " by name: " + name + "(" + eH.getMessage() + ")", eH
      );
    }
  } // protected static Group findByName(name)


  // PRIVATE CLASS METHODS //
  private static Group _findByUuid(String uuid)
    throws  GroupNotFoundException
  {
    try {
      Group   g       = null;
      Session hs      = HibernateHelper.getSession();
      Query   qry   = hs.createQuery(
        "from Group as g where g.uuid = :uuid"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBU);
      qry.setString("uuid", uuid);
      List    groups  = qry.list();
      if (groups.size() == 1) {
        g = (Group) groups.get(0);
      }
      hs.close();
      if (g == null) {
        throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by uuid: " + uuid);
      }
      return g; 
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(
        E.GROUP_NOTFOUND + " by uuid: " + uuid + "(" + eH.getMessage() + ")", eH
      );  
    }
  } // private static Group _findByUuid(uuid)

}

