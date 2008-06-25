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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Find groups within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupFinder.java,v 1.52 2008-06-25 05:46:05 mchyzer Exp $
 */
public class GroupFinder {

  // PRIVATE CLASS CONSTANTS //
  /** error for finding by attribute */
  private static final String ERR_FINDBYATTRIBUTE = "could not find group by attribute: ";

  /** error for finding by type */
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
    //note, no need for GrouperSession inverse of control
    NotNullValidator v = NotNullValidator.validate(s);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null session");
    }
    v = NotNullValidator.validate(attr);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null attribute");
    }
    v = NotNullValidator.validate(val);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null value");
    }
    Group g = GrouperDAOFactory.getFactory().getGroup().findByAttribute(attr, val);
    if (g != null) {
      if ( s.getMember().canView(g) ) {
        return g;
      }
    }
    throw new GroupNotFoundException( ERR_FINDBYATTRIBUTE + Quote.single(attr) );
  } // public static Group findByAttribute(s, attr, val)

  /**
   * Find <tt>Group</tt>s by attribute value.  Returns groups or empty set if none (never null)
   * <pre class="eg">
   *   Set<Group> groups = GroupFinder.findAllByAttribute(s, "description", "some value");
   * </pre>
   * @param   s     Search within this session context.
   * @param   attr  Search on this attribute.
   * @param   val   Search for this value.
   * @return  Matching {@link Group}.
   * @throws  IllegalArgumentException
   * @since   1.1.0
   */
  public static Set<Group> findAllByAttribute(GrouperSession s, String attr, String val)
      throws  IllegalArgumentException {
    //note, no need for GrouperSession inverse of control
    NotNullValidator v = NotNullValidator.validate(s);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null session");
    }
    v = NotNullValidator.validate(attr);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null attribute");
    }
    v = NotNullValidator.validate(val);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null value");
    }
    Set<Group> groupsDb = GrouperDAOFactory.getFactory().getGroup().findAllByAttr(attr, val);
    Set<Group> groups= new LinkedHashSet<Group>();
    if (groupsDb != null && groupsDb.size() > 0) {
      for (Group group : groupsDb) {
        if ( s.getMember().canView(group) ) {
          groups.add(group);
        }
      }
    }
    return groups;
  } 

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
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Group g = GrouperDAOFactory.getFactory().getGroup().findByName(name) ;
    //2007-10-16: Gary Brown
    //https://bugs.internet2.edu/jira/browse/GRP-36
    //Ugly... and probably breaks the abstraction but quick and easy to 
    //remove when a more elegant solution found.
    if(s.getSubject().equals(SubjectFinder.findRootSubject()))
    	return g;
    
    if ( PrivilegeHelper.canView( s.internal_getRootSession(), g, s.getSubject() ) ) {
      return g;
    }
    ErrorLog.error(GroupFinder.class, E.GF_FBNAME + E.CANNOT_VIEW);
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + name);
  } 

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
    //note, no need for GrouperSession inverse of control
    NotNullValidator v = NotNullValidator.validate(s);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null session");
    }
    v = NotNullValidator.validate(type);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null type");
    }
    Set groups = PrivilegeHelper.canViewGroups(
      s, GrouperDAOFactory.getFactory().getGroup().findAllByType( type )
    );
    if (groups.size() == 1) {
      return (Group) new ArrayList(groups).get(0);
    }
    throw new GroupNotFoundException(ERR_FINDBYTYPE + Quote.single( type.toString() ));
  } // public static Group findByType(s, type)

  /**
   * Find all groups within the registry by their {@link GroupType}.  Or empty set if none (never null).
   * <pre class="eg">
   *   Set<Group> groups = GroupFinder.findAllByType( s, GroupTypeFinder.find("your type") );
   * </pre>
   * @param   s     Find group within this session context.
   * @param   type  Find group with this {@link GroupType}.
   * @return  A set of {@link Group}s
   * @throws  IllegalArgumentException
   */
  public static Set<Group> findAllByType(GrouperSession s, GroupType type) throws IllegalArgumentException {
    //note, no need for GrouperSession inverse of control
    NotNullValidator v = NotNullValidator.validate(s);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null session");
    }
    v = NotNullValidator.validate(type);
    if (v.isInvalid()) {
      throw new IllegalArgumentException("null type");
    }
    Set<Group> groups = PrivilegeHelper.canViewGroups(
      s, GrouperDAOFactory.getFactory().getGroup().findAllByType( type)
    );
    return GrouperUtil.nonNull(groups);
  } 

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
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid);
    if ( PrivilegeHelper.canView( s.internal_getRootSession(), g, s.getSubject() ) ) {
      return g;
    }
    ErrorLog.error(GroupFinder.class, E.GF_FBUUID + E.CANNOT_VIEW);
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by uuid: " + uuid);
  } 

}

