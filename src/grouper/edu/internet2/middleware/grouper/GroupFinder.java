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

/**
 * Find groups within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupFinder.java,v 1.33 2006-11-27 18:38:43 blair Exp $
 */
public class GroupFinder {

  // PRIVATE CLASS CONSTANTS //
  private static final String ERR_FINDBYATTRIBUTE = "could not find group by attribute: ";
  private static final String ERR_FINDBYTYPE      = "could not find group by type: ";
  private static final String KLASS               = GroupFinder.class.getName();
  
  
  // PUBLIC INSTANCE METHODS //

  /**
   * Find <tt>Group</tt> by attribute value.
   * <pre class="eg">
   * try {
   *   Group g = GroupFinder.findByAttribute(s, "description", "some value");
   * }
   * catch (GroupNotFoundException eGNF) {
   * }
   * </pre>
   * @param   s     Search within this session context.
   * @param   attr  Search on this attribute.
   * @param   val   Search for this value.
   * @return  Matching {@link Group}.
   * @throws  GroupNotFoundException
   * @throws  IllegalArgumentException
   * @since   1.1.0
   */
  public static Group findByAttribute(GrouperSession s, String attr, String val)
    throws  GroupNotFoundException,
            IllegalArgumentException
  {
    Validator.argNotNull( s,    "null session"   );
    Validator.argNotNull( attr, "null attribute" );
    Validator.argNotNull( val,  "null value"     );
    try {
      Group   g   = null;
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Attribute as a where a.field.name = :field and a.value like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByAttribute");
      qry.setString("field", attr);
      qry.setString("value", val);
      Attribute a = (Attribute) qry.uniqueResult();
      hs.close();
      if (a != null) {
        g = a.getGroup();
        g.setSession(s);
        if (s.getMember().canView(g)) {
          return g;
        }
      }
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(ERR_FINDBYATTRIBUTE + eH.getMessage(), eH);
    }
    throw new GroupNotFoundException(ERR_FINDBYATTRIBUTE + U.q(attr));
  } // public static Group findByAttribute(s, attr, val)

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
    GrouperSessionValidator.validate(s);
    Group g = findByName(name);
    g.setSession(s);
    if (RootPrivilegeResolver.canVIEW(g, s.getSubject())) {
      return g;
    }
    ErrorLog.error(GroupFinder.class, E.GF_FBNAME + E.CANNOT_VIEW);
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + name);
  } // public static Group findByName(s, name)

  /**
   * Find a group within the registry by its {@link GroupType}.
   * <pre class="eg">
   * try {
   *   Group g = GroupFinder.findByType( s, GroupTypeFinder.find("your type") );
   * }
   * catch (GroupNotFoundException eGNF) {
   *   // Unable to find group by type
   * }
   * </pre>
   * @param   s     Find group within this session context.
   * @param   type  Find group with this {@link GroupType}.
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   * @throws  IllegalArgumentException
   */
  public static Group findByType(GrouperSession s, GroupType type)
    throws  GroupNotFoundException,
            IllegalArgumentException
  {
    Validator.argNotNull( s,    "null session" );
    Validator.argNotNull( type, "null type"    );
    // TODO 20061127 can i use a variant of this query in `GroupType.delete()`?
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where :type in elements(g.group_types)"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByType");
      qry.setParameter("type", type);
      Group g = (Group) qry.uniqueResult();
      hs.close();
      if (g != null) {
        g.setSession(s);
        if (s.getMember().canView(g)) {
          return g;
        }
      }
    }
    catch (HibernateException eH) {
      throw new GroupNotFoundException(ERR_FINDBYTYPE + eH.getMessage(), eH);
    }
    throw new GroupNotFoundException(ERR_FINDBYTYPE + U.q( type.toString() ));
  } // public static Group findByType(s, type)

  /**
   * Find a group within the registry by UUID.
   * <pre class="eg">
   * try {
   *   Group g = GroupFinder.findByUuid(s, uuid);
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
    GrouperSessionValidator.validate(s);
    Group g = _findByUuid(uuid);
    g.setSession(s);
    if (RootPrivilegeResolver.canVIEW(g, s.getSubject())) {
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
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.create_time > :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByCreatedAfter");
      return _findByDate(s, hs, qry, d);
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );  
    }
  } // protected static Set findByCreatedAfter(s, d)

  // @return  groups created before this date
  protected static Set findByCreatedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.create_time < :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByCreatedBefore");
      return _findByDate(s, hs, qry, d);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding groups: " + eH.getMessage(), eH);  
    }
  } // protected static Set findByCreatedBefore(s, d)

  protected static Set findByAnyApproximateAttr(GrouperSession s, String val) 
    throws  QueryException
  {
    GrouperSessionValidator.validate(s);
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Attribute as a where lower(a.value) like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByAnyApproximateAttr");
      return _findByAttribute(s, hs, qry, val);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding groups: " + eH.getMessage(), eH);
    }
  } // protected static Set findByAnyApproximateAttr(s, val)

  protected static Set findByApproximateAttr(GrouperSession s, String attr, String val) 
    throws  QueryException
  {
    GrouperSessionValidator.validate(s);
    try {
      Session   hs    = HibernateHelper.getSession();
      Query     qry   = hs.createQuery(
        "from Attribute as a where "
        + "a.field.name = :field and lower(a.value) like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateAttr");
      qry.setString("field", attr);
      return _findByAttribute(s, hs, qry, val);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding groups: " + eH.getMessage(), eH);
    }
  } // protected static Set findByApproximateAttr(s, attr, val)

  protected static Set findByApproximateName(GrouperSession s, String name) 
    throws  QueryException
  {
    GrouperSessionValidator.validate(s);
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
      qry.setCacheRegion(KLASS + ".FindByApproximateName");
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
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.modify_time > :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByModifiedAfter");
      return _findByDate(s, hs, qry, d);
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding groups: " + eH.getMessage(), eH
      );  
    }
  } // protected static Set findByModifiedAfter(s, d)

  // @return  groups modifed before this date
  // @since   1.1.0
  protected static Set findByModifiedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Group as g where g.modify_time < :time"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByModifiedBefore");
      return _findByDate(s, hs, qry, d);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding groups: " + eH.getMessage(), eH);  
    }
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
      qry.setCacheRegion(KLASS + ".FindByName");
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

  // @since   1.1.0
  private static Set _findByAttribute(GrouperSession s, Session hs, Query qry, String val) 
    throws  HibernateException
  {
    qry.setString( "value", "%" + val.toLowerCase() + "%" );
    Group     g;
    Set       groups  = new LinkedHashSet();
    Iterator  it      = qry.iterate();
    while (it.hasNext()) {
      g = ( (Attribute) it.next() ).getGroup();
      g.setSession(s);
      groups.add(g);
    }
    hs.close();
    return PrivilegeResolver.canViewGroups(s, groups);
  } // private static Set _findByAttribute(s, hs, qry, val)

  // @since   1.1.0
  private static Set _findByDate(GrouperSession s, Session hs, Query qry, Date d)
    throws  HibernateException
  {
    qry.setLong( "time", d.getTime() );
    List        l       = qry.list();
    hs.close();
    Group       g;
    Set         groups  = new LinkedHashSet();
    Iterator    it      = l.iterator();
    while (it.hasNext()) {
      g = (Group) it.next();
      g.setSession(s);
      groups.add(g);
    }
    return PrivilegeResolver.canViewGroups(s, groups);
  } // private static Set _findByDate(s, hs, qry, d)

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
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      g = (Group) qry.uniqueResult();
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

} // public class GroupFinder

