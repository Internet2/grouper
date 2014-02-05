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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Find memberships within the Groups Registry.
 * 
 * A membership is the object which represents a join of member
 * and group.  Has metadata like type and creator,
 * and, if an effective membership, the parent membership
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.108 2009-12-17 06:57:57 mchyzer Exp $
 */
public class MembershipFinder {

  /**
   * return memberships where the member has this field, note, it will return all the memberships for those members
   */
  private boolean hasMembershipTypeForMember;

  /**
   * return memberships where the member has this field, note, it will return all the memberships for those members
   * @param theHasMembershipType
   * @return this for chaining
   */
  public MembershipFinder assignHasMembershipTypeForMember(boolean theHasMembershipType) {
    this.hasMembershipTypeForMember = theHasMembershipType;
    return this;
  }

  /** 
   * return memberships where the member has this field, note, it will return all the memberships for those members 
   */
  private boolean hasFieldForMember;

  /** 
   * return memberships where the group has this field, note, it will return all the memberships for those groups 
   */
  private boolean hasFieldForGroup;

  /**
   * return memberships where the group has this field, note, it will return all the memberships for those members 
   * @return this for chaining
   */
  public MembershipFinder assignHasFieldForGroup(boolean theHasFieldForGroup) {
    this.hasFieldForGroup = theHasFieldForGroup;
    return this;
  }
  
  /**
   * return memberships where the member has this field, note, it will return all the memberships for those members 
   * @param theHasField
   * @return this for chaining
   */
  public MembershipFinder assignHasFieldForMember(boolean theHasField) {
    this.hasFieldForMember = theHasField;
    return this;
  }

  /** membership ids to search for */
  private Collection<String> membershipIds;

  /** membership type to look for */
  private MembershipType membershipType;

  /** fields to look for */
  private Collection<Field> fields = new HashSet<Field>();

  /** sql like string to limit the results of the owner */
  private String scope;
  
  /**
   * sql like string to limit the results of the owner
   * @param scope1
   * @return this for chaining
   */
  public MembershipFinder assignScope(String scope1) {
    this.scope = scope1;
    return this;
  }
  
  /**
   * assign a field to filter by
   * @param theField
   * @return this for chaining
   */
  public MembershipFinder assignField(Field theField) {
    this.fields.clear();
    this.fields.add(theField);
    return this;
  }
  
  /**
   * assign a field to filter by, or a privilege name
   * @param theField
   * @return this for chaining
   */
  public MembershipFinder assignFieldName(String theFieldOrPrivilegeName) {
    Field theField = FieldFinder.find(theFieldOrPrivilegeName, true);
    this.assignField(theField);
    return this;
  }
  
  /** sources to look in */
  private Set<Source> sources;

  /** stem to look in */
  private Stem stem;

  /** stem scope to look in */
  private Scope stemScope;
  
  /**
   * field type to look for, mutually exclusive with fieldId
   */
  private FieldType fieldType;

  /**
   * assign a field type, mutually exclusive with fieldId
   * @param theFieldType
   * @return this for chaining
   */
  public MembershipFinder assignFieldType(FieldType theFieldType) {
    this.fieldType = theFieldType;
    return this;
  }
  
  /** if we should check security */
  private boolean checkSecurity = true;

  /** if filtering by service role, this is the service id (id of the attributeDefName for service */
  private String serviceId = null; 

  /**
   * if filtering by serviceRole, this is the role, e.g. user or admin
   */
  private ServiceRole serviceRole = null;

  /**
   * if filtering by service role, this is the service id (id of the attributeDefName for service
   * @param serviceId1
   * @return this for chaining
   */
  public MembershipFinder assignServiceId(String serviceId1) {
    this.serviceId = serviceId1;
    return this;
  }
  
  /**
   * if filtering by service role, this is the service id (id of the attributeDefName for service
   * @param serviceRole1
   * @return this for chaining
   */
  public MembershipFinder assignServiceRole(ServiceRole serviceRole1) {
    this.serviceRole = serviceRole1;
    return this;
  }
  
  /**
   * assign a stem scope to look in
   * @param theStemScope
   * @return this for chaining
   */
  public MembershipFinder assignStemScope(Scope theStemScope) {
    this.stemScope = theStemScope;
    return this;
  }
  
  /**
   * assign a stem to search in
   * @param theStem
   * @return this for chaining
   */
  public MembershipFinder assignStem(Stem theStem) {
    this.stem = theStem;
    return this;
  }
  
  /**
   * assign if this should check security or run as grouper system
   * @param shouldCheckSecurity
   * @return this for chaining
   */
  public MembershipFinder assignCheckSecurity(boolean shouldCheckSecurity) {
    this.checkSecurity = shouldCheckSecurity;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> memberIds = null;
  
  /**
   * add a member id to the search criteria
   * @param memberId
   * @return this for chaining
   */
  public MembershipFinder addMemberId(String memberId) {
    if (this.memberIds == null) {
      this.memberIds = new ArrayList<String>();
    }
    //no need to look for dupes
    if (!this.memberIds.contains(memberId)) {
      this.memberIds.add(memberId);
    }
    return this;
  }

  /**
   * add a membership id to the search criteria
   * @param membershipId
   * @return this for chaining
   */
  public MembershipFinder addMembershipId(String membershipId) {
    if (this.membershipIds == null) {
      this.membershipIds = new ArrayList<String>();
    }
    //no need to look for dupes
    if (!this.membershipIds.contains(membershipId)) {
      this.membershipIds.add(membershipId);
    }
    return this;
  }

  /**
   * add subjects
   * @param subjects
   * @return this for chaining
   */
  public MembershipFinder addSubjects(Collection<Subject> subjects) {
    
    Set<Member> members = MemberFinder.findBySubjects(subjects, false);
    
    for (Member member : GrouperUtil.nonNull(members)) {
      this.addMemberId(member.getUuid());
    }
    return this;
  }
  
  /**
   * assign a collection of member ids to look for
   * @param theMemberIds
   * @return this for chaining
   */
  public MembershipFinder assignMemberIds(Collection<String> theMemberIds) {
    this.memberIds = theMemberIds;
    return this;
  }
  
  /**
   * assign a collection of group ids to look for
   * @param theGroupIds
   * @return this for chaining
   */
  public MembershipFinder assignGroupIds(Collection<String> theGroupIds) {
    this.groupIds = theGroupIds;
    return this;
  }
  
  /**
   * assign a collection of fields to look for
   * @param theFields
   * @return this for chaining
   */
  public MembershipFinder assignFields(Collection<Field> theFields) {
    this.fields = theFields;
    return this;
  }
  
  /**
   * assign a collection of fields to look for
   * @param theFieldNames
   * @return this for chaining
   */
  public MembershipFinder assignFieldsByName(Collection<String> theFieldNames) {
    
    Set<Field> theFields = new HashSet<Field>();

    for (String fieldName : GrouperUtil.nonNull(theFieldNames)) {
      Field field = FieldFinder.find(fieldName, true);
      theFields.add(field);
    }
    
    this.fields = theFields;
    
    return this;
  }
  
  /**
   * assign a collection of stem ids to look for
   * @param theStemIds
   * @return this for chaining
   */
  public MembershipFinder assignStemIds(Collection<String> theStemIds) {
    this.stemIds = theStemIds;
    return this;
  }

  /**
   * assign a collection of attributeDef ids to look for
   * @param theAttributeDefIds
   * @return this for chaining
   */
  public MembershipFinder assignAttributeDefIds(Collection<String> theAttributeDefIds) {
    this.attributeDefIds = theAttributeDefIds;
    return this;
  }
  

  /**
   * assign a membership type
   * @param theMembershipType
   * @return this for chaining
   */
  public MembershipFinder assignMembershipType(MembershipType theMembershipType) {
    this.membershipType = theMembershipType;
    return this;
  }
  
  /**
   * assign a collection of membership ids to look for
   * @param theMembershipIds
   * @return this for chaining
   */
  public MembershipFinder assignMembershipIds(Collection<String> theMembershipIds) {
    this.membershipIds = theMembershipIds;
    return this;
  }
  
  /**
   * add a subject to look for.
   * @param subject
   * @return this for chaining
   */
  public MembershipFinder addSubject(Subject subject) {
    
    //note, since we are chaining, we need to add if not found, since if we dont, it will find for
    //all subjects if no more are added
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return this.addMemberId(member.getUuid());
  }
  
  /**
   * 
   */
  private Collection<String> groupIds = null;
  
  /**
   * add a role id to the search criteria
   * @param groupId
   * @return this for chaining
   */
  public MembershipFinder addGroupId(String groupId) {
    if (!StringUtils.isBlank(groupId)) {
      if (this.groupIds == null) {
        this.groupIds = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.groupIds.contains(groupId)) {
        this.groupIds.add(groupId);
      }
    }
    return this;
  }

  /**
   * assign a collection of role ids to look for
   * @param theRoleIds
   * @return this for chaining
   */
  public MembershipFinder assignRoleIds(Collection<String> theRoleIds) {
    this.groupIds = theRoleIds;
    return this;
  }
  
  /**
   * assign a collection of sources to look for
   * @param theSources
   * @return this for chaining
   */
  public MembershipFinder assignSources(Set<Source> theSources) {
    this.sources = theSources;
    return this;
  }
  
  /**
   * add a role to look for.
   * @param group
   * @return this for chaining
   */
  public MembershipFinder addGroup(Group group) {
    
    return this.addGroupId(group.getId());
  }
  
  /**
   * add a role to look for by name.
   * @param name
   * @return this for chaining
   */
  public MembershipFinder addGroup(String name) {
    
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), name, true);
    
    return this.addGroupId(group.getId());
  }
  
  /**
   * add a field to filter by
   * @param name
   * @return this for chaining
   */
  public MembershipFinder addField(String name) {
    Field field = FieldFinder.find(name, false);
    this.addField(field);
    return this;
  }
  
  /**
   * add a field to filter by
   * @param field
   * @return this for chaining
   */
  public MembershipFinder addField(Field field) {
    this.fields.add(field);
    return this;
  }
  
  /** if we should look for all, or enabled only.  default is all */
  private Boolean enabled;
  
  /**
   * 
   */
  private Collection<String> stemIds = null;

  /**
   * 
   */
  private Collection<String> attributeDefIds = null;
  
  /**
   * true means enabled only, false, means disabled only, and null means all
   * @param theEnabled
   * @return this for chaining
   */
  public MembershipFinder assignEnabled(Boolean theEnabled) {
    this.enabled = theEnabled;
    return this;
  }
  
  /**
   * based on what you are querying for, see if has membership.
   * Note, you should be looking for one subject, 
   * one group, one field, etc
   * If you are looking for multiple, it will see if anyone has that membership or any group
   * @return true if has membership, false if not
   */
  public boolean hasMembership() {
    
    return GrouperUtil.length(findMembershipsGroupsMembers()) > 0;
  }
  
  /**
   * membership result gives helper methods in processing the results
   * @return the membership result
   */
  public MembershipResult findMembershipResult() {
    
    Set<Object[]> membershipsOwnersMembers = this.findMembershipsMembers();
    Field field = this.field(false);
    String theFieldId = field == null ? null : field.getUuid();
    return new MembershipResult(membershipsOwnersMembers, theFieldId, this.hasFieldForGroup ? null : this.fields, 
        this.hasFieldForGroup ? false : this.includeInheritedPrivileges);
  }
  
  /**
   * if inherited effective privileges should be included.  i.e. if you query for UPDATE, then also query for ADMIN
   */
  private boolean includeInheritedPrivileges = false;
  
  /**
   * if inherited effective privileges should be included.  i.e. if you query for UPDATE, then also query for ADMIN
   * @param theIncludeInheritedPrivileges
   * @return this for chaining
   */
  public MembershipFinder assignIncludeInheritedPrivileges(boolean theIncludeInheritedPrivileges) {
    this.includeInheritedPrivileges = theIncludeInheritedPrivileges;
    return this;
  }
  
  /**
   * assuming one field
   * @param firstFieldIfMultiple true if should get first field if multiple
   * @return the field or null
   */
  private Field field(boolean firstFieldIfMultiple) {
    if (!firstFieldIfMultiple) {
      this.assertOneOrNoFields();
    }
    if (GrouperUtil.length(this.fields) == 0) {
      return null;
    }
    if (GrouperUtil.length(this.fields) == 1) {
      return this.fields.iterator().next();
    }
    //at this point there are multiple... make sure same type?  or dont worry?
    FieldType fieldType = null;
    for (Field field : this.fields) {
      if (fieldType == null) {
        fieldType = field.getType();
      } else {
        if (fieldType != field.getType()) {
          throw new RuntimeException("Expecting field type of: " + fieldType + ", but received: " + field.getType());
        }
      }
    }
    return this.fields.iterator().next();
  }
  
  /**
   * make sure there is only one or no fields here
   */
  private void assertOneOrNoFields() {
    if (GrouperUtil.length(this.fields) <= 1) {
      return;
    }
    throw new RuntimeException("Expecting 0 or 1 fields but got: " + GrouperUtil.length(this.fields));
  }
  
  /**
   * find a set of object arrays which have a membership, group|stem|attributeDef, and member inside
   * @return the set of arrays never null
   */
  public Set<Object[]> findMembershipsMembers() {
    
    Field field = this.field(true);
    if ((this.fieldType != null && this.fieldType == FieldType.NAMING )
        || (field != null && field.isStemListField())
        || GrouperUtil.length(this.stemIds) > 0) {
      return this.findMembershipsStemsMembers();
    } else if ((this.fieldType != null && this.fieldType == FieldType.ATTRIBUTE_DEF )
        || (field != null && field.isAttributeDefListField())
        || GrouperUtil.length(this.attributeDefIds) > 0) {
      Set<Object[]> result = this.findMembershipsAttributeDefsMembers();
      return result;
    } else if ((field == null && this.fieldType == null) 
        || this.fieldType == FieldType.ACCESS || this.fieldType == FieldType.LIST
        || (field != null && field.isGroupListField())
        || GrouperUtil.length(this.groupIds) > 0) {
      return this.findMembershipsGroupsMembers();
    } else {
      throw new RuntimeException("Not expecting field / fieldType: " + field + ", " + this.fieldType);
    }
  }
  
  /**
   * find a set of object arrays which have a membership, group, and member inside
   * @return the set of arrays never null
   */
  private Set<Object[]> findMembershipsGroupsMembers() {

    //validate that we are looking at groups
    Field field = this.field(true);
    if (field != null && !field.isGroupAccessField() && !field.isGroupListField()) {
      throw new RuntimeException("Not expecting field: " + field +
          ", expecting a group field since other part of the query involve group memberships");
    }

    if (this.fieldType != null && this.fieldType != FieldType.ACCESS && this.fieldType != FieldType.LIST) {
      throw new RuntimeException("Not expecting fieldType: " + this.fieldType +
          ", expecting a group field type since other part of the query involve group memberships");
    }

    if (GrouperUtil.length(this.stemIds) > 0) {
      throw new RuntimeException("Not expecting stem lookups, since other parts of the query "
          + " involve group memberships");
      
    }
    
    if (GrouperUtil.length(this.attributeDefIds) > 0) {
      throw new RuntimeException("Not expecting attribute definition lookups, since other parts of the query "
          + " involve group memberships");
    }

    Collection<Field> inheritedFields = Field.calculateInheritedPrivileges(this.fields, this.includeInheritedPrivileges);
    
    return GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(this.groupIds, this.memberIds,
        this.membershipIds, this.membershipType, inheritedFields, this.sources, this.scope, this.stem, this.stemScope, 
        this.enabled, this.checkSecurity, this.fieldType, this.serviceId, this.serviceRole,
        this.queryOptionsForMember, this.scopeForMember, this.splitScopeForMember, 
        this.hasFieldForMember, this.hasMembershipTypeForMember, this.queryOptionsForGroup, 
        this.scopeForGroup, this.splitScopeForGroup, this.hasFieldForGroup,
        this.hasMembershipTypeForGroup);  




  }

  /**
   * find a set of object arrays which have a membership, stem, and member inside
   * @return the set of arrays never null
   */
  private Set<Object[]> findMembershipsStemsMembers() {

    Collection<Field> inheritedFields = Field.calculateInheritedPrivileges(this.fields, this.includeInheritedPrivileges);
    
    //validate that we are looking at stems
    Field field = this.field(true);
    if (field != null && !field.isStemListField()) {
      throw new RuntimeException("Not expecting field: " + field +
          ", expecting a stem field since other part of the query involve stem memberships");
    }

    if (this.fieldType != null && this.fieldType != FieldType.NAMING) {
      throw new RuntimeException("Not expecting fieldType: " + this.fieldType +
          ", expecting a stem field type since other part of the query involve stem memberships");
    }

    if (GrouperUtil.length(this.groupIds) > 0) {
      throw new RuntimeException("Not expecting group lookups, since other parts of the query "
          + " involve stem memberships");
      
    }
    
    if (GrouperUtil.length(this.attributeDefIds) > 0) {
      throw new RuntimeException("Not expecting attribute definition lookups, since other parts of the query "
          + " involve stem memberships");
    }

    return GrouperDAOFactory.getFactory().getMembership().findAllByStemOwnerOptions(this.stemIds, this.memberIds,
        this.membershipIds, this.membershipType, inheritedFields, this.sources, 
        this.scope, this.stem, this.stemScope, this.enabled, this.checkSecurity, 
        this.queryOptionsForMember, this.scopeForMember, this.splitScopeForMember, 
        this.hasFieldForMember, this.hasMembershipTypeForMember, this.queryOptionsForStem, 
        this.scopeForStem, this.splitScopeForStem, this.hasFieldForStem,
        this.hasMembershipTypeForStem);  

  }

  /**
   * find a membership
   * @param exceptionIfNotFound true if exception should be thrown if permission not found
   * @return the permission or null
   */
  public Membership findMembership(boolean exceptionIfNotFound) {

    Set<Object[]> memberships = findMembershipsGroupsMembers();
    
    //this should find one if it is there...
    Membership membership = null;
    
    if (GrouperUtil.length(memberships) > 1) {
      throw new RuntimeException("Why is there more than one membership found? " + this);
    }
    
    if (GrouperUtil.length(memberships) == 1) {
      membership = (Membership)memberships.iterator().next()[0];
    }
    
    if (membership == null && exceptionIfNotFound) {
      throw new RuntimeException("could not find membership: " 
          + this);
    }
    return membership;
    
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (enabled != null) {
      result.append("enabled: ").append(this.enabled);
    }
    if (GrouperUtil.length(this.memberIds) > 0) {
      result.append("memberIds: ").append(GrouperUtil.toStringForLog(this.memberIds, 100));
    }
    if (GrouperUtil.length(this.fields) > 0) {
      result.append("fields: ").append(GrouperUtil.toStringForLog(this.fields, 100));
    }
    if (GrouperUtil.length(this.groupIds) > 0) {
      result.append("groupIds: ").append(GrouperUtil.toStringForLog(this.groupIds, 100));
    }
    if (GrouperUtil.length(this.membershipIds) > 0) {
      result.append("membershipIds: ").append(GrouperUtil.toStringForLog(this.membershipIds, 100));
    }
    if (GrouperUtil.length(this.membershipType) > 0) {
      result.append("membershipType: ").append(this.membershipType);
    }
    if (GrouperUtil.length(this.sources) > 0) {
      result.append("sources: ").append(GrouperUtil.toStringForLog(this.sources, 100));
    }
    if (GrouperUtil.length(this.stem) > 0) {
      result.append("stem: ").append(this.stem);
    }
    if (GrouperUtil.length(this.stemScope) > 0) {
      result.append("membershipType: ").append(this.membershipType);
    }
    return result.toString();
  }

  /**
   * add a stem to look for.
   * @param stem
   * @return this for chaining
   */
  public MembershipFinder addStem(Stem stem) {
    
    return this.addStemId(stem.getUuid());
  }

  /**
   * add a stem to look for by name.
   * @param name
   * @return this for chaining
   */
  public MembershipFinder addStem(String name) {
    
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), name, true);
    
    return this.addGroupId(group.getId());
  }

  /**
   * add a stem id to the search criteria
   * @param stemId
   * @return this for chaining
   */
  public MembershipFinder addStemId(String stemId) {
    if (!StringUtils.isBlank(stemId)) {
      if (this.stemIds == null) {
        this.stemIds = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.stemIds.contains(stemId)) {
        this.stemIds.add(stemId);
      }
    }
    return this;
  }

  /**
   * add a sourceId to the search criteria
   * @param sourceId
   * @return this for chaining
   */
  public MembershipFinder addSourceId(String sourceId) {
    
    if (!StringUtils.isBlank(sourceId)) {
      Source source = SourceManager.getInstance().getSource(sourceId);
      addSource(source);
    }
    return this;
  }

  /**
   * add a source to the search criteria
   * @param source
   * @return this for chaining
   */
  public MembershipFinder addSource(Source source) {
    if (source != null) {
      if (this.sources == null) {
        this.sources = new HashSet<Source>();
      }
      //no need to look for dupes
      this.sources.add(source);
    }
    return this;
  }

  /**
   * find a set of object arrays which have a membership, attributeDef, and member inside
   * @return the set of arrays never null
   */
  private Set<Object[]> findMembershipsAttributeDefsMembers() {
  
    Collection<Field> inheritedFields = Field.calculateInheritedPrivileges(this.fields, this.includeInheritedPrivileges);

    //validate that we are looking at attribute definitions
    Field field = this.field(true);
    if (field != null && !field.isAttributeDefListField()) {
      throw new RuntimeException("Not expecting field: " + field +
          ", expecting an attribute definition field since other part of the query involve attribute definition memberships");
    }

    if (this.fieldType != null && this.fieldType != FieldType.ATTRIBUTE_DEF) {
      throw new RuntimeException("Not expecting fieldType: " + this.fieldType +
          ", expecting an attribute def field type since other part of the query involve attributeDef memberships");
    }

    if (GrouperUtil.length(this.groupIds) > 0) {
      throw new RuntimeException("Not expecting group lookups, since other parts of the query "
          + " involve attributeDef memberships");
      
    }
    
    if (GrouperUtil.length(this.stemIds) > 0) {
      throw new RuntimeException("Not expecting stem lookups, since other parts of the query "
          + " involve attributeDef memberships");
    }

    Set<Object[]> result = GrouperDAOFactory.getFactory().getMembership().findAllByAttributeDefOwnerOptions(this.attributeDefIds, this.memberIds,
        this.membershipIds, this.membershipType, inheritedFields, this.sources, this.scope, this.stem, this.stemScope, 
        this.enabled, this.checkSecurity, 
        this.queryOptionsForAttributeDef, this.scopeForAttributeDef, this.splitScopeForAttributeDef, this.hasFieldForAttributeDef, 
        this.hasMembershipTypeForAttributeDef);  
    return result;
  }

  /**
   * add a attributeDef to look for.
   * @param attributeDef
   * @return this for chaining
   */
  public MembershipFinder addAttributeDef(AttributeDef attributeDef) {
    
    return this.addAttributeDefId(attributeDef.getId());
  }

  /**
   * add a attributeDef to look for by name.
   * @param name
   * @return this for chaining
   */
  public MembershipFinder addAttributeDef(String name) {
    
    AttributeDef attributeDef = AttributeDefFinder.findByName(name, true);
    
    return this.addGroupId(attributeDef.getId());
  }

  /**
   * add a attributeDef id to the search criteria
   * @param attributeDefId
   * @return this for chaining
   */
  public MembershipFinder addAttributeDefId(String attributeDefId) {
    if (!StringUtils.isBlank(attributeDefId)) {
      if (this.attributeDefIds == null) {
        this.attributeDefIds = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.attributeDefIds.contains(attributeDefId)) {
        this.attributeDefIds.add(attributeDefId);
      }
    }
    return this;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param stemIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findStemMemberships(Collection<String> stemIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean shouldCheckSecurity) {
    return GrouperDAOFactory.getFactory().getMembership().findAllByStemOwnerOptions(stemIds, memberIds,
        membershipIds, membershipType, field, sources, scope, stem, stemScope, enabled, shouldCheckSecurity);  
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param groupIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findMemberships(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean shouldCheckSecurity) {
    return findMemberships(groupIds, memberIds, membershipIds, membershipType, field, sources, scope, stem, stemScope, enabled, 
        shouldCheckSecurity, null);
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param groupIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @param fieldType is access or list
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findMemberships(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean shouldCheckSecurity, FieldType fieldType) {
    return findMemberships(groupIds, memberIds, membershipIds, membershipType, field, sources, scope,
        stem, stemScope, enabled, shouldCheckSecurity, fieldType, null, null);
  }

  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a stem.
   * A stem can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   stem     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Stem stem, Subject subj, Field f, boolean exceptionIfNotFound
  ) throws  MembershipNotFoundException, SchemaException {
    //note, no need for GrouperSession inverse of control
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType( 
          stem.getUuid(), m.getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getOwnerStem(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)         {
      if (exceptionIfNotFound) {
        throw mnfe;
      }
      return null;
    } catch (StemNotFoundException eGNF)         {
      //not sure why this should happen in a non-corrupt db
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param groupIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findMemberships(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled) {
    
    return findMemberships(groupIds, memberIds, membershipIds, membershipType, field, sources, scope, stem, stemScope, enabled, null, null);
    
  }
  
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param attributeDefIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findAttributeDefMemberships(Collection<String> attributeDefIds, 
      Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, 
      Boolean shouldCheckSecurity) {

    return GrouperDAOFactory.getFactory().getMembership().findAllByAttributeDefOwnerOptions(attributeDefIds, memberIds,
        membershipIds, membershipType, field, sources, scope, stem, stemScope, enabled, shouldCheckSecurity);  
  
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param groupIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @param serviceId is the id of the service (attributeDefName) if filtering memberships of people in a service
   * @param serviceRole is the user/admin role of the user in the service
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findMemberships(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean shouldCheckSecurity,
      FieldType fieldType, String serviceId, ServiceRole serviceRole) {    
    
    return GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(groupIds, memberIds,
        membershipIds, membershipType, GrouperUtil.toSet(field), sources, scope, stem, stemScope, enabled, shouldCheckSecurity, 
        fieldType, serviceId, serviceRole, null, null, false, false, false, null, null, false, false, false);  
  }
  
  /**
   * Return the composite membership if it exists. 
   *
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Composite membership has this group.
   * @param   subj  Composite membership has this subject.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @since   1.0
   * @deprecated see overload
   */
  @Deprecated
  public static Membership findCompositeMembership(GrouperSession s, Group g, Subject subj)
      throws  MembershipNotFoundException, SchemaException {
    
    return findCompositeMembership(s, g, subj, true);
    
  }

  /**
   * Return the composite membership if it exists. 
   *
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Composite membership has this group.
   * @param   subj  Composite membership has this subject.
   * @param   exceptionOnNull 
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @since   1.0
   */
  public static Membership findCompositeMembership(GrouperSession s, Group g, Subject subj, boolean exceptionOnNull)
    throws  MembershipNotFoundException, SchemaException {

    //note, no need for GrouperSession inverse of control
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    try {
      Field       f   = Group.getDefaultList();
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, MembershipType.COMPOSITE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getOwnerGroup(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)  {
      if (exceptionOnNull) {
        throw mnfe;
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionOnNull) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }

  /**
   * Return effective memberships.  
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a 
   * composite group are effective members (since the composite 
   * group has two groups and a set operator and no other immediate 
   * members).  Note that a member can have an immediate membership 
   * and an effective membership.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Effective membership has this group.
   * @param   subj  Effective membership has this subject.
   * @param   f     Effective membership has this list.
   * @param   via   Effective membership has this via group.
   * @param   depth Effective membership has this depth.
   * @return  A set of {@link Membership} objects.
   * @throws  MembershipNotFoundException
   * @throws  SchemaException
   */
  public static Set<Membership> findEffectiveMemberships(
    GrouperSession s, Group g, Subject subj, Field f, Group via, int depth
  )
    throws  MembershipNotFoundException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    Set mships = new LinkedHashSet();
    Member m = MemberFinder.findBySubject(s, subj, true);
    try {
      PrivilegeHelper.dispatch( s, g, s.getSubject(), f.getReadPriv() );
      Iterator  it    = GrouperDAOFactory.getFactory().getMembership().findAllEffectiveByGroupOwner(
        g.getUuid(), m.getUuid(), f, via.getUuid(), depth, true
      ).iterator();
      Membership eff;
      while (it.hasNext()) {
        eff = (Membership) it.next();
        mships.add(eff);
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      // ??? ignore
    }
    return mships;
  } // public static Membership findEffectiveMembership(s, g, subj, f, via, depth)


  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @deprecated see overload
   */
  @Deprecated
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
    ) throws  MembershipNotFoundException, SchemaException {
    return findImmediateMembership(s, g, subj, f, true);
  }

  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, boolean exceptionIfNotFound) 
      throws  MembershipNotFoundException, SchemaException {
    return findImmediateMembership(s, g, subj, Group.getDefaultList(), exceptionIfNotFound);
  }

  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f, boolean exceptionIfNotFound
  ) throws  MembershipNotFoundException, SchemaException {
    //note, no need for GrouperSession inverse of control
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getOwnerGroup(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)         {
      if (exceptionIfNotFound) {
        throw mnfe;
      }
      return null;
    } catch (GroupNotFoundException eGNF)         {
      //not sure why this should happen in a non-corrupt db
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }


  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to an attributeDef.
   * An attributeDef can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param    SchemaException     Immediate membership has this attribute def.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, AttributeDef attributeDef, Subject subj, Field f, boolean exceptionIfNotFound
  ) throws  MembershipNotFoundException, SchemaException {
    //note, no need for GrouperSession inverse of control
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByAttrDefOwnerAndMemberAndFieldAndType(
          attributeDef.getUuid(), m.getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getOwnerAttributeDef(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)         {
      if (exceptionIfNotFound) {
        throw mnfe;
      }
      return null;
    } catch (AttributeDefNotFoundException eGNF)         {
      //not sure why this should happen in a non-corrupt db
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }

  /**
   * @param dto
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllChildrenNoPriv(Membership dto) {
    Set           children  = new LinkedHashSet();
    Membership child;
    Iterator      it        = GrouperDAOFactory.getFactory().getMembership().findAllChildMemberships(dto, true).iterator();
    while (it.hasNext()) {
      child = (Membership) it.next();
      children.addAll( internal_findAllChildrenNoPriv(child) );
      children.add(child);
    }
    return children;
  } // protected static Set internal_findAllChildrenNoPriv(dto)
  
  /** 
   * @param group 
   * @param field 
   * @return  A set of all <code>Member</code>'s in <i>group</i>'s list <i>field</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static Set<Member> findMembers(Group group, Field field)
    throws  IllegalArgumentException {
    return findMembers(group, field, null);
  }

  /** 
   * @param group 
   * @param field 
   * @param queryOptions 
   * @return  A set of all <code>Member</code>'s in <i>group</i>'s list <i>field</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static Set<Member> findMembers(Group group, Field field, QueryOptions queryOptions)
    throws  IllegalArgumentException
  {
    return findMembers(group, field, null, queryOptions);
  }
  /** 
   * @param group 
   * @param field 
   * @param sources set of sources to retrieve from or null for all
   * @param queryOptions 
   * @return  A set of all <code>Member</code>'s in <i>group</i>'s list <i>field</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static Set<Member> findMembers(Group group, Field field, Set<Source> sources, QueryOptions queryOptions)
    throws  IllegalArgumentException
  {
    //note, no need for GrouperSession inverse of control
    if (group == null) { // TODO 20070814 ParameterHelper
      throw new IllegalArgumentException("null Group");
    }
    if (field == null) { // TODO 20070814 ParameterHelper
      throw new IllegalArgumentException("null Field");
    }
    Set<Member> members = null;
    try {
      GrouperSession  s   = GrouperSession.staticGrouperSession();
      PrivilegeHelper.dispatch( s, group, s.getSubject(), field.getReadPriv() );
      members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByGroupOwnerAndField( 
          group.getUuid(), field, sources, queryOptions, true);
    }
    catch (InsufficientPrivilegeException eIP) {
      // ignore  
    }
    catch (SchemaException eSchema) {
      String groupName = null;
      try {
        groupName = group.getName();
      } catch (Exception e) {
        LOG.error("error getting group name", e);
      }
      throw new RuntimeException("Error retrieving members for group: " + groupName, eSchema);
    }
    return members;
  } 

  /**
   * 
   * @param s
   * @param group
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findGroupSubjects(GrouperSession s, Group group, Field f) 
    throws  GrouperException
  {
    GrouperSession.validate(s);
    Set       subjs = new LinkedHashSet();
    Iterator  it    = PrivilegeHelper.canViewMemberships(
      s, GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField(group.getUuid(), f, true)
    ).iterator();
    try {
      while (it.hasNext()) {
    	//2007-12-18 Gary Brown
        //Instantiating all the Subjects can be very slow. LazySubjects
    	//only make expensive calls when necessary - so a client can page 
        //results.
    	//A partial alternative may have been to always instantiate the Member of
    	//a Membership when the latter is created - assuming one query.
    	try {
    		subjs.add ( new LazySubject((Membership) it.next()) );
    	}catch(GrouperException gre) {
    		if(gre.getCause() instanceof MemberNotFoundException) {
    			throw (MemberNotFoundException) gre.getCause();
    		}
    		if(gre.getCause() instanceof SubjectNotFoundException) {
    			throw (SubjectNotFoundException) gre.getCause();
    		}
    	}
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = "internal_findSubjects: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findSubjects: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    }
    return subjs;
  } // public static Set internal_findSubjects(s, o, f)

  /**
   * 
   * @param s
   * @param attributeDef
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findAttributeDefSubjectsImmediateOnly(GrouperSession s,
      AttributeDef attributeDef, Field f) throws GrouperException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, attributeDef, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = null; 
      //TODO 20090919 fix this
//        GrouperDAOFactory.getFactory().getMembership()
//          .findAllMembersByGroupOwnerAndFieldAndType(group.getUuid(), f,
//              MembershipType.IMMEDIATE.getTypeString(), null, true).iterator();

      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    }
    return subjs;
  } 
  
  /**
   * 
   * @param s
   * @param stem
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findStemSubjectsImmediateOnly(GrouperSession s,
      Stem stem, Field f) throws GrouperException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, stem, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = GrouperDAOFactory.getFactory().getMembership()
          .findAllMembersByStemOwnerAndFieldAndType(stem.getUuid(), f,
              MembershipType.IMMEDIATE.getTypeString(), null, true).iterator();

      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    }
    return subjs;
  } 
  

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MemberFinder.class);

  /**
   * 
   * @param s
   * @param stem
   * @param f
   * @return set of subjects
   */
  public static Set<Subject> internal_findSubjectsStemPriv(GrouperSession s, Stem stem, Field f) {
     // @filtered  false
     // @session   true 
    GrouperSession.validate(s);
    Membership mbs;
    Set           subjs = new LinkedHashSet();
    Iterator      it    = GrouperDAOFactory.getFactory().getMembership().findAllByStemOwnerAndField(stem.getUuid(), f, true).iterator();
    while (it.hasNext()) {
      mbs = (Membership) it.next();
      try {
    	  subjs.add ( new LazySubject(mbs) );
        //_m = dao.findByUuid( ms.getMemberUuid() );
        //subjs.add( SubjectFinder.findById( _m.getSubjectId(), _m.getSubjectTypeId(), _m.getSubjectSourceId() ) );
      }
      catch (Exception e) {
        // @exception MemberNotFoundException
        // @exception SubjectNotFoundException
        LOG.error(E.MSF_FINDSUBJECTS + e.getMessage());
      }
    }
    return subjs;
  } // public static Set internal_findSubjectsNoPriv(s, o, f)

  /**
   * 
   * @param s
   * @param g
   * @param f
   * @param type
   * @return set of members
   */
  public static Set<Member> internal_findMembersByType(GrouperSession s, Group g, Field f, String type) {
    GrouperSession.validate(s);
    Set         members = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = internal_findAllByGroupOwnerAndFieldAndType(s, g, f, type).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // public static Set internal_findMembersByType(s, g, f, type)

  /**
   * query options for member.  must include paging.  if sorting then sort by member
   */
  private QueryOptions queryOptionsForMember;

  /**
   * if paging for member, then also filter for member
   */
  private String scopeForMember;

  /**
   * if paging for member, then also filter for member
   * @param theFilterForMember
   * @return this for chaining
   */
  public MembershipFinder assignScopeForMember(String theFilterForMember) {
    this.scopeForMember = theFilterForMember;
    return this;
  }
  
  /**
   * if the scope for member has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private boolean splitScopeForMember;

  /**
   * query options for group.  must include paging.  if sorting then sort by group
   */
  private QueryOptions queryOptionsForGroup;

  /**
   * query options for group.  must include paging.  if sorting then sort by group
   * @param theQueryOptionsForGroup
   * @return this for chaining
   */
  public MembershipFinder assignQueryOptionsForGroup(QueryOptions theQueryOptionsForGroup) {
    this.queryOptionsForGroup = theQueryOptionsForGroup;
    return this;
  }
  
  /**
   * if the scope for group has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private boolean splitScopeForGroup;

  /**
   * return memberships where the group has this field, note, it will return all the memberships for those groups
   */
  private boolean hasMembershipTypeForGroup;

  /**
   * if paging for group, then also filter for group
   */
  private String scopeForGroup;
  
  /**
   * return memberships where the stem has this field, note, it will return all the memberships for those stems 
   */
  private boolean hasFieldForStem;

  /**
   * return memberships where the stem has this field, note, it will return all the memberships for those stems 
   * @param theHasFieldForStem
   * @return this for chaining
   */
  public MembershipFinder assignHasFieldForStem(boolean theHasFieldForStem) {
    this.hasFieldForStem = theHasFieldForStem;
    return this;
  }
  
  /**
   * return memberships where the stem has this field, note, it will return all the memberships for those stems
   */
  private boolean hasMembershipTypeForStem;
  
  /**
   * return memberships where the stem has this field, note, it will return all the memberships for those stems
   * @return this for chaining
   */
  public MembershipFinder assignHasMembershipTypeForStem(boolean theHasMembershipTypeForStem) {
    this.hasMembershipTypeForStem = theHasMembershipTypeForStem;
    return this;
  }
  
  /**
   * query options for stem.  must include paging.  if sorting then sort by stem
   */
  private QueryOptions queryOptionsForStem;

  /**
   * query options for stem.  must include paging.  if sorting then sort by stem
   * @param theQueryOptionsForStem
   * @return this for chaining
   */
  public MembershipFinder assignQueryOptionsForStem(QueryOptions theQueryOptionsForStem) {
    this.queryOptionsForStem = theQueryOptionsForStem;
    return this;
  }
  
  /**
   * if paging for stem, then also filter for stem
   */
  private String scopeForStem;

  /**
   * if paging for stem, then also filter for stem
   * @param theScopeForStem
   * @return this for chaining
   */
  public MembershipFinder assignScopeForStem(String theScopeForStem) {
    this.scopeForStem = theScopeForStem;
    return this;
  }
  
  /**
   * if the scope for stem has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private boolean splitScopeForStem;

  /** 
   * return memberships where the attributeDef has this field, note, it will return all the memberships for those attributeDefs 
   */
  private boolean hasFieldForAttributeDef;

  /**
   * return memberships where the attributeDef has this field, note, it will return all the memberships for those attributeDefs 
   * @param theHasFieldForAttributeDef
   * @return this for chaining
   */
  public MembershipFinder assignHasFieldForAttributeDef(boolean theHasFieldForAttributeDef) {
    this.hasFieldForAttributeDef = theHasFieldForAttributeDef;
    return this;
  }
  
  /**
   * return memberships where the attributeDef has this field, note, it will return all the memberships for those attributeDefs
   */
  private boolean hasMembershipTypeForAttributeDef;

  /**
   * return memberships where the attributeDef has this field, note, it will return all the memberships for those attributeDefs
   * @param theHasMembershipTypeForAttributeDef
   * @return this for chaining
   */
  public MembershipFinder assignHasMembershipTypeForAttributeDef(boolean theHasMembershipTypeForAttributeDef) {
    this.hasMembershipTypeForAttributeDef = theHasMembershipTypeForAttributeDef;
    return this;
  }
  
  /**
   * query options for attributeDef.  must include paging.  if sorting then sort by attributeDef
   */
  private QueryOptions queryOptionsForAttributeDef;

  /**
   * query options for attributeDef.  must include paging.  if sorting then sort by attributeDef
   * @param theQueryOptionsForAttributeDef
   * @return this for chaining
   */
  public MembershipFinder assignQueryOptionsForAttributeDef(QueryOptions theQueryOptionsForAttributeDef) {
    this.queryOptionsForAttributeDef = theQueryOptionsForAttributeDef;
    return this;
  }
  
  /**
   * if paging for attributeDef, then also filter for group
   */
  private String scopeForAttributeDef;

  /**
   * if paging for attributeDef, then also filter for group
   * @param theScopeForAttributeDef
   * @return this for chaining
   */
  public MembershipFinder assignScopeforAttributeDef(String theScopeForAttributeDef) {
    this.scopeForAttributeDef = theScopeForAttributeDef;
    return this;
  }
  
  /**
   * if the scope for attributeDef has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private boolean splitScopeForAttributeDef;

  /**
   * if the scope for attributeDef has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScopeForAttributeDef
   * @return this
   */
  public MembershipFinder assignSplitScopeForAttributeDef(boolean theSplitScopeForAttributeDef) {
    this.splitScopeForAttributeDef = theSplitScopeForAttributeDef;
    return this;
  }
  
  /**
   * if the scope for stem has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScopeForStem
   * @return this for chaining
   */
  public MembershipFinder assignSplitScopeForStem(boolean theSplitScopeForStem) {
    this.splitScopeForStem = theSplitScopeForStem;
    return this;
  }
  
  /**
   * if paging for group, then also filter for member
   * @param theScopeForGroup
   * @return this for chaining
   */
  public MembershipFinder assignScopeForGroup(String theScopeForGroup) {
    this.scopeForGroup = theScopeForGroup;
    return this;
  }
  
  /**
   * return memberships where the group has this field, note, it will return all the memberships for those groups
   * @param theHasMembershipTypeForGroup
   * @return this for chaining
   */
  public MembershipFinder assignHasMembershipTypeForGroup(boolean theHasMembershipTypeForGroup) {
    this.hasMembershipTypeForGroup = theHasMembershipTypeForGroup;
    return this;
  }
  
  /**
   * if the scope for group has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScopeForGroup
   * @return this for chaining
   */
  public MembershipFinder assignSplitScopeForGroup(boolean theSplitScopeForGroup) {
    this.splitScopeForGroup = theSplitScopeForGroup;
    return this;
  }
  
  /**
   * if the scope for member has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScopeForMember
   * @return if splitting scope for member
   */
  public MembershipFinder assignSplitScopeForMember(boolean theSplitScopeForMember) {
    this.splitScopeForMember = theSplitScopeForMember;
    return this;
  }

  /**
   * 
   * @param theQueryOptions
   * @return
   */
  public MembershipFinder assignQueryOptionsForMember(QueryOptions theQueryOptions) {
    this.queryOptionsForMember = theQueryOptions;
    return this;
  }
  
  /**
   * 
   * @param s
   * @param d
   * @param f
   * @return set of memberships
   * @throws QueryException
   */
  public static Set<Membership> internal_findAllByCreatedAfter(GrouperSession s, Date d, Field f) 
    throws QueryException 
  {
    //note, no need for GrouperSession inverse of control
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(d, f, true).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      mships.add(ms);
    }
    return mships;
  } 

  /**
   * 
   * @param s
   * @param d
   * @param f
   * @return set of memberships
   * @throws QueryException
   */
  public static Set<Membership> internal_findAllByCreatedBefore(GrouperSession s, Date d, Field f) 
    throws QueryException {
    //note, no need for GrouperSession inverse of control
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = GrouperDAOFactory.getFactory().getMembership().findAllByCreatedBefore(d, f, true).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      mships.add(ms);
    }
    return mships;
  } // public static Set internal_findAllByCreatedBefore(s, d, f)

  /**
   * 
   * @param s
   * @param groupOwner
   * @param f
   * @param type
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllByGroupOwnerAndFieldAndType(GrouperSession s, Group groupOwner, Field f, String type) {
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships(
      s, GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndFieldAndType(groupOwner.getUuid(), f, type, true)
    );
  } // public static Set internal_findAllByOwnerAndFieldAndType(s, o, f, type)

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllEffectiveByMemberAndField(
    GrouperSession s, Member m, Field f
  ) 
  {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllEffectiveByMemberAndField(m.getUuid(), f, true) 
    );
  } // public static Set internal_findAllEffectiveByMemberAndField(s, m, f)

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllImmediateByMemberAndField(GrouperSession s, Member m, Field f) {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField(m.getUuid(), f, true) 
    );
  } 

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllNonImmediateByMemberAndField(GrouperSession s, Member m, Field f) {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllNonImmediateByMemberAndField(m.getUuid(), f, true) 
    );
  } 

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findMemberships(GrouperSession s, Member m, Field f) {
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();
    return dao.findMembershipsByMemberAndFieldSecure(s, m.getUuid(), f, true);
  } // public static Set internal_findMemberships(s, m, f)

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param field
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllImmediateByGroupAndFieldAndPage(Group group,
      Field field, int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getImmediateMembers(field, queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }

    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getImmediateMemberships(field);
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getImmediateMembers(field, queryOptions);
      allChildren = group.getImmediateMemberships(field, members);
    }
    return allChildren;
  }

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllCompositeByGroupAndPage(Group group,
      int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getCompositeMembers(queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }

    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getCompositeMemberships();
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getCompositeMembers(queryOptions);
      allChildren = group.getCompositeMemberships(members);
    }
    return allChildren;
  }

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param field
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllEffectiveByGroupAndFieldAndPage(Group group,
      Field field, int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getEffectiveMembers(field, queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }

    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getEffectiveMemberships(field);
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getEffectiveMembers(field, queryOptions);
      allChildren = group.getEffectiveMemberships(field, members);
    }
    return allChildren;
  }

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param field
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllByGroupAndFieldAndPage(Group group,
      Field field, int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getMembers(field, queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }
    
    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getMemberships(field);
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getMembers(field, queryOptions);
      allChildren = group.getMemberships(field, members);
    }
    return allChildren;
    }

  /**
   * 
   * @param s
   * @param attributeDef
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findAttributeDefSubjects(GrouperSession s, AttributeDef attributeDef, Field f) 
    throws  GrouperException {
    GrouperSession.validate(s);
    Set       subjs = new LinkedHashSet();
    Iterator  it    = null;
    //TODO 20090919 fix this
//    PrivilegeHelper.canViewAttributeDefs(
//      s, GrouperDAOFactory.getFactory().getMembership().findAllByAttrDefOwnerAndField(attributeDef.getId(), f, true)
//    ).iterator();
    try {
      while (it.hasNext()) {
    	//2007-12-18 Gary Brown
        //Instantiating all the Subjects can be very slow. LazySubjects
    	//only make expensive calls when necessary - so a client can page 
        //results.
    	//A partial alternative may have been to always instantiate the Member of
    	//a Membership when the latter is created - assuming one query.
    	try {
    		subjs.add ( new LazySubject((Membership) it.next()) );
    	}catch(GrouperException gre) {
    		if(gre.getCause() instanceof MemberNotFoundException) {
    			throw (MemberNotFoundException) gre.getCause();
    		}
    		if(gre.getCause() instanceof SubjectNotFoundException) {
    			throw (SubjectNotFoundException) gre.getCause();
    		}
    	}
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = "internal_findSubjects: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findSubjects: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    }
    return subjs;
  } // public static Set internal_findSubjects(s, o, f)

  /**
   * 
   * @param s
   * @param group
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findGroupSubjectsImmediateOnly(GrouperSession s,
      Group group, Field f) throws GrouperException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, group, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = GrouperDAOFactory.getFactory().getMembership()
          .findAllMembersByGroupOwnerAndFieldAndType(group.getUuid(), f,
              MembershipType.IMMEDIATE.getTypeString(), null, true).iterator();
  
      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    }
    return subjs;
  }

  /**
   * Find a membership within the registry by UUID.  This will be either the uuid or immediate uuid.
   * Security will be checked to see if the grouper session is allowed to see the membership
   * <pre class="eg">
   *   Membership membership = MembershipFinder.findByUuid(grouperSession, uuid);
   * </pre>
   * @param   grouperSession     Find membership within this session context.
   * @param   uuid  UUID of membership to find.
   * @param exceptionIfNotFound true if exception if not found
   * @param enabledOnly true for enabled only
   * @return  A {@link Membership}
   * @throws MembershipNotFoundException if not found an exceptionIfNotFound is true
   */
  public static Membership findByUuid(GrouperSession grouperSession, String uuid, boolean exceptionIfNotFound, boolean enabledOnly) 
      throws MembershipNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(uuid, exceptionIfNotFound, enabledOnly);
    if ( PrivilegeHelper.canViewMembership( grouperSession.internal_getRootSession(), membership ) ) {
      return membership;
    }
    if (exceptionIfNotFound) {
      throw new MembershipNotFoundException("Not allowed to view membership: " + uuid);
    }
    return null;
  }

} // public class MembershipFinder

