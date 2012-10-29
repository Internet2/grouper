/*******************************************************************************
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
 ******************************************************************************/
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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.NotNullValidator;

/**
 * Find groups within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupFinder.java,v 1.62 2009-11-17 02:52:29 mchyzer Exp $
 */
public class GroupFinder {

  // PRIVATE CLASS CONSTANTS //
  /** error for finding by attribute */
  private static final String ERR_FINDBYATTRIBUTE = "could not find group by attribute: ";

  /** error for finding by type */
  @SuppressWarnings("unused")
  private static final String ERR_FINDBYTYPE      = "could not find group by type: ";
  
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
   * @deprecated use the overload
   */
  @Deprecated
  public static Group findByAttribute(GrouperSession s, String attr, String val)
    throws  GroupNotFoundException,
            IllegalArgumentException {

    return findByAttribute(s, attr, val, true);

  }

  /**
   * Find <tt>Group</tt> by attribute value.
   * <pre class="eg">
   *   Group g = GroupFinder.findByAttribute(s, "description", "some value", true);
   * </pre>
   * @param   s     Search within this session context.
   * @param   attr  Search on this attribute.
   * @param   val   Search for this value.
   * @param exceptionOnNull true if there should be an exception on null
   * @return  Matching {@link Group}.
   * @throws  GroupNotFoundException
   * @throws  IllegalArgumentException
   * @since   1.1.0
   */
  public static Group findByAttribute(GrouperSession s, String attr, String val, 
      boolean exceptionOnNull)
    throws  GroupNotFoundException, IllegalArgumentException {

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
    Group g = GrouperDAOFactory.getFactory().getGroup().findByAttribute(attr, val, exceptionOnNull);
    if (g != null) {
      if ( s.getMember().canView(g) ) {
        return g;
      }
    }
    if (exceptionOnNull) {
      throw new GroupNotFoundException( ERR_FINDBYATTRIBUTE + Quote.single(attr) );
    }
    return null;
  } 

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
   * @Deprecated
   */
  @Deprecated
  public static Group findByName(GrouperSession s, String name) 
      throws GroupNotFoundException {
    return findByName(s, name, true);
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
   * @param exceptionIfNotFound 
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public static Group findByName(GrouperSession s, String name, boolean exceptionIfNotFound) 
    throws GroupNotFoundException {
    return findByName(s, name, exceptionIfNotFound, null);
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
   * @param exceptionIfNotFound 
   * @param queryOptions paging, sorting, caching options
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public static Group findByName(GrouperSession s, String name, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws GroupNotFoundException {
    
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Group g = null;
    g = GrouperDAOFactory.getFactory().getGroup().findByName(name, exceptionIfNotFound, queryOptions) ;
    
    if (g == null) {
      return g;
    }
    
    //2007-10-16: Gary Brown
    //https://bugs.internet2.edu/jira/browse/GRP-36
    //Ugly... and probably breaks the abstraction but quick and easy to 
    //remove when a more elegant solution found.
    if(s.getSubject().equals(SubjectFinder.findRootSubject()))
      return g;
    
    if ( PrivilegeHelper.canView( s.internal_getRootSession(), g, s.getSubject() ) ) {
      return g;
    }
    LOG.error(E.GF_FBNAME + E.CANNOT_VIEW + ", name: " + name);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + name);
  } 

  /**
   * Find a group within the registry by its current name.
   * <pre class="eg">
   * try {
   *   Group g = GroupFinder.findByCurrentName(name, true);
   * }
   * catch (GroupNotFoundException e) {
   *   // Group not found
   * }
   * </pre>
   * @param   s     Find group within this session context.
   * @param   name  Name of group to find.
   * @param exceptionIfNotFound 
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public static Group findByCurrentName(GrouperSession s, String name, boolean exceptionIfNotFound) 
    throws GroupNotFoundException {
    
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Group g = null;
    g = GrouperDAOFactory.getFactory().getGroup().findByCurrentName(name, exceptionIfNotFound) ;
    
    if (g == null) {
      return g;
    }
    
    //2007-10-16: Gary Brown
    //https://bugs.internet2.edu/jira/browse/GRP-36
    //Ugly... and probably breaks the abstraction but quick and easy to 
    //remove when a more elegant solution found.
    if(s.getSubject().equals(SubjectFinder.findRootSubject()))
      return g;
    
    if ( PrivilegeHelper.canView( s.internal_getRootSession(), g, s.getSubject() ) ) {
      return g;
    }
    LOG.error(E.GF_FBNAME + E.CANNOT_VIEW);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + name);
  } 
  
  /**
   * Find a group within the registry by its alternate name.
   * <pre class="eg">
   * try {
   *   Group g = GroupFinder.findByAlternateName(name, true);
   * }
   * catch (GroupNotFoundException e) {
   *   // Group not found
   * }
   * </pre>
   * @param   s     Find group within this session context.
   * @param   name  Name of group to find.
   * @param exceptionIfNotFound 
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public static Group findByAlternateName(GrouperSession s, String name, boolean exceptionIfNotFound) 
    throws GroupNotFoundException {
    
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Group g = null;
    g = GrouperDAOFactory.getFactory().getGroup().findByAlternateName(name, exceptionIfNotFound) ;
    
    if (g == null) {
      return g;
    }
    
    //2007-10-16: Gary Brown
    //https://bugs.internet2.edu/jira/browse/GRP-36
    //Ugly... and probably breaks the abstraction but quick and easy to 
    //remove when a more elegant solution found.
    if(s.getSubject().equals(SubjectFinder.findRootSubject()))
      return g;
    
    if ( PrivilegeHelper.canView( s.internal_getRootSession(), g, s.getSubject() ) ) {
      return g;
    }
    LOG.error(E.GF_FBNAME + E.CANNOT_VIEW);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by alternate name: " + name);
  } 
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GroupFinder.class);

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
   *   Group g = GroupFinder.findByUuid(s, uuid);
   * </pre>
   * @param   s     Find group within this session context.
   * @param   uuid  UUID of group to find.
   * @return  A {@link Group}
   * @throws GroupNotFoundException
   * @Deprecated use the overload
   */
  @Deprecated
  public static Group findByUuid(GrouperSession s, String uuid) throws GroupNotFoundException {
    return findByUuid(s, uuid, true);
  }

  /**
   * Find a group within the registry by UUID.
   * <pre class="eg">
   *   Group g = GroupFinder.findByUuid(s, uuid);
   * </pre>
   * @param   s     Find group within this session context.
   * @param   uuid  UUID of group to find.
   * @param exceptionIfNotFound true if exception if not found
   * @return  A {@link Group}
   * @throws GroupNotFoundException if not found an exceptionIfNotFound is true
   */
  public static Group findByUuid(GrouperSession s, String uuid, boolean exceptionIfNotFound) 
      throws GroupNotFoundException {
    return findByUuid(s, uuid, exceptionIfNotFound, null);
  }
  /**
   * Find a group within the registry by UUID.
   * <pre class="eg">
   *   Group g = GroupFinder.findByUuid(s, uuid);
   * </pre>
   * @param   s     Find group within this session context.
   * @param   uuid  UUID of group to find.
   * @param exceptionIfNotFound true if exception if not found
   * @param queryOptions 
   * @return  A {@link Group}
   * @throws GroupNotFoundException if not found an exceptionIfNotFound is true
   */
  public static Group findByUuid(GrouperSession s, String uuid, boolean exceptionIfNotFound,  QueryOptions queryOptions) 
      throws GroupNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    try {
      Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid, true, queryOptions);
      if ( PrivilegeHelper.canView( s.internal_getRootSession(), g, s.getSubject() ) ) {
        return g;
      }
    } catch (GroupNotFoundException gnfe) {
      if (exceptionIfNotFound) {
        throw gnfe;
      }
    }
    return null;
  } 

  /**
   * Find a group within the registry by ID index.
   * @param idIndex id index of group to find.
   * @param exceptionIfNotFound true if exception if not found
   * @param queryOptions 
   * @return  A {@link Group}
   * @throws GroupNotFoundException if not found an exceptionIfNotFound is true
   */
  public static Group findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound,  QueryOptions queryOptions) 
      throws GroupNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(GrouperSession.staticGrouperSession());
    Group g = GrouperDAOFactory.getFactory().getGroup().findByIdIndexSecure(idIndex, exceptionIfNotFound, queryOptions);
    return g;
  } 

}

