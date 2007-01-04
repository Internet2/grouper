/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import  java.util.ArrayList;
import  java.util.Date;
import  java.util.Set;

/**
 * Find groups within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupFinder.java,v 1.37 2007-01-04 17:17:45 blair Exp $
 */
public class GroupFinder {

  // PRIVATE CLASS CONSTANTS //
  private static final String ERR_FINDBYATTRIBUTE = "could not find group by attribute: ";
  private static final String ERR_FINDBYTYPE      = "could not find group by type: ";
  
  
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
    Group g = HibernateGroupDAO.findByAttribute(attr, val);
    if (g != null) {
      g.setSession(s);
      if ( s.getMember().canView(g) ) {
        return g;
      }
    }
    throw new GroupNotFoundException( ERR_FINDBYATTRIBUTE + U.q(attr) );
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
    Group g = internal_findByName(name);
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
    try {
      Set groups = internal_findAllByType(s, type);
      if (groups.size() == 1) {
        return (Group) new ArrayList(groups).get(0);
      }
    }
    catch (QueryException eQ) {
      throw new GroupNotFoundException(ERR_FINDBYTYPE + eQ.getMessage(), eQ);
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

  // @since   1.2.0
  protected static Set internal_findAllByAnyApproximateAttr(GrouperSession s, String val) 
    throws  QueryException
  {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByAnyApproximateAttr(val) );
  } // protected static Set internal_findAllByAnyApproximateAttr(s, val)

  // @since   1.2.0
  protected static Set internal_findAllByApproximateAttr(GrouperSession s, String attr, String val) 
    throws  QueryException
  {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByApproximateAttr(attr, val) );
  } // protected static Set internal_findAllByApproximateAttr(s, attr, val)

  // @since   1.2.0
  protected static Set internal_findAllByApproximateName(GrouperSession s, String name) 
    throws  QueryException
  {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByApproximateName(name) );
  } // protected static Set internal_findAllByApproximateName(s, name)

  // @since   1.2.0
  protected static Set internal_findAllByCreatedAfter(GrouperSession s, Date d) 
    throws QueryException 
  {
    // @filtered  true
    // @session   true
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByCreatedAfter(d) );
  } // protected static Set internal_findAllByCreatedAfter(s, d)
    
  // @since   1.2.0
  protected static Set internal_findAllByCreatedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    // @filtered  true
    // @session   true
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByCreatedBefore(d) );
  } // protected static Set internal_findAllByCreatedBefore(s, d)
    
  // @since   1.2.0
  protected static Set internal_findAllByModifiedAfter(GrouperSession s, Date d) 
    throws QueryException 
  {
    // @filtered  true
    // @session   true
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByModifiedAfter(d) );
  } // protected static Set internal_findAllByModifiedAfter(s, d)
    
  // @since   1.2.0
  protected static Set internal_findAllByModifiedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    // @filtered  true
    // @session   true
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByModifiedBefore(d) );
  } // protected static Set internal_findAllByModifiedBefore(s, d)
    
  // @since   1.2.0
  protected static Set internal_findAllByType(GrouperSession s, GroupType type) 
    throws  QueryException
  {
    // @filtered  true
    // @session   true
    return PrivilegeResolver.canViewGroups( s, HibernateGroupDAO.findAllByType(type) );
  } // protected static Set internal_findAllByType(s, type)

  // @since   1.2.0
  protected static Group internal_findByName(String name)
    throws  GroupNotFoundException
  {
    // @filtered  false
    // @session   false
    Group g = HibernateGroupDAO.findByName(name);
    if (g == null) {
      throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + name);
    }
    return g;
  } // protected static Group internal_findByname(name)


  // PRIVATE CLASS METHODS //

  private static Group _findByUuid(String uuid)
    throws  GroupNotFoundException
  {
    // @filtered  false
    // @session   false
    Group g = HibernateGroupDAO.findByUuid(uuid);
    if (g == null) {
      throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by uuid: " + uuid);
    }
    return g;
  } // private static Group _findByUuid(uuid)

} // public class GroupFinder

