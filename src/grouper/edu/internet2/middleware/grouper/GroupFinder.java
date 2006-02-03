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
import  org.apache.commons.logging.*;


/**
 * Find groups within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupFinder.java,v 1.10 2006-02-03 19:38:53 blair Exp $
 */
public class GroupFinder {

  // Private Class Constants
  private static final String ERR_GNF = "unable to find group";
  private static final Log    LOG     = LogFactory.getLog(GroupFinder.class);


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
    String msg = "findByName '" + name + "'";
    GrouperLog.debug(LOG, s, msg);
    Group g = findByName(name);
    GrouperLog.debug(LOG, s, msg + ": found");
    // Attach root session for VIEW check
    // TODO What problems might this cause?
    g.setSession(GrouperSessionFinder.getRootSession());
    try {
      PrivilegeResolver.getInstance().canVIEW(s, g, s.getSubject());
      GrouperLog.debug(LOG, s, msg + ": visible");
      // Now attach proper session
      g.setSession(s);
      return g;
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, msg + ": not visible");
    }  
    throw new GroupNotFoundException(ERR_GNF + " by name: " + name);
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
    // Attach root session for VIEW check
    // TODO What problems might this cause?
    g.setSession(GrouperSessionFinder.getRootSession());
    try {
      PrivilegeResolver.getInstance().canVIEW(s, g, s.getSubject());
      // Now attach proper session
      g.setSession(s);
      return g;
    }
    catch (InsufficientPrivilegeException eIP) {
      // Ignore
    }  
    throw new GroupNotFoundException(ERR_GNF + " by uuid: " + uuid);
  } // public static Group findByUuid(s, uuid)


  // Protected Class Methods

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
      qry.setCacheable(GrouperConfig.QRY_GF_FBCA);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBCA);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
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
      Query   qry = hs.createQuery(
        "from Group as g where g.create_time < :time"
      );
      qry.setCacheable(GrouperConfig.QRY_GF_FBCB);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBCB);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
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
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Attribute as a where lower(a.value) like :value"
      );
      qry.setCacheable(GrouperConfig.QRY_GF_FBAAA);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBAAA);
      qry.setString("value", "%" + val.toLowerCase() + "%");
      Iterator  iter  = qry.iterate();
      while (iter.hasNext()) {
        Group g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
      hs.close();
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
      Query     qry   = hs.createQuery(
        "from Attribute as a where "
        + "a.field.name = :field and lower(a.value) like :value"
      );
      qry.setCacheable(GrouperConfig.QRY_GF_FBAA);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBAA);
      qry.setString("field", attr);
      qry.setString("value", "%" + val.toLowerCase() + "%");
      Iterator  iter  = qry.iterate();
      while (iter.hasNext()) {
        Group g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
      hs.close();
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
      qry.setCacheable(GrouperConfig.QRY_GF_FBAN);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBAN);
      qry.setString("value", "%" + name.toLowerCase() + "%");
      Iterator  iter  = qry.iterate();
      while (iter.hasNext()) {
        Group g = ( (Attribute) iter.next() ).getGroup();
        g.setSession(s);
        groups.add(g);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage()
      );
    }
    return PrivilegeResolver.getInstance().canVIEW(s, groups);
  } // protected static Set findByApproximateName(s, name)

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
      qry.setCacheable(GrouperConfig.QRY_GF_FBN);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBN);
      qry.setString("value", name);
      List    attrs = qry.list();
      if (attrs.size() == 1) {
        g = ( (Attribute) attrs.get(0) ).getGroup();
      }
      hs.close();
      if (g == null) {
        throw new GroupNotFoundException(ERR_GNF + " by name: " + name);
      }
      return g; 
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(
        ERR_GNF + " by name: " + name + "(" + eH.getMessage() + ")"
      );
    }
  } // protected static Group findByName(name)


  // Private Class Methods
  private static Group _findByUuid(String uuid)
    throws  GroupNotFoundException
  {
    try {
      Group   g       = null;
      Session hs      = HibernateHelper.getSession();
      Query   qry   = hs.createQuery(
        "from Group as g where g.group_id = :value"
      );
      qry.setCacheable(GrouperConfig.QRY_GF_FBU);
      qry.setCacheRegion(GrouperConfig.QCR_GF_FBU);
      qry.setString("value", uuid);
      List    groups  = qry.list();
      if (groups.size() == 1) {
        g = (Group) groups.get(0);
      }
      hs.close();
      if (g == null) {
        throw new GroupNotFoundException(ERR_GNF + " by uuid: " + uuid);
      }
      return g; 
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(
        ERR_GNF + " by uuid: " + uuid + "(" + eH.getMessage() + ")"
      );  
    }
  } // private static Group _findByUuid(uuid)

}

