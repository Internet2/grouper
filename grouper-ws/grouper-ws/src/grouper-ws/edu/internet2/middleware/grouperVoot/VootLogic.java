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

package edu.internet2.middleware.grouperVoot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;
import edu.internet2.middleware.grouperVoot.beans.VootPerson;
import edu.internet2.middleware.grouperVoot.messages.VootGetGroupsResponse;
import edu.internet2.middleware.grouperVoot.messages.VootGetMembersResponse;
import edu.internet2.middleware.subject.Subject;

/**
 * Business logic to implement the VOOT protocol.
 * 
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
public class VootLogic {
  /**
   * Helper for find by approximate name queries.
   * This isn't in 2.0.0.
   * 
   * @param name the name of the group to be searched
   * @param scope the scope of the group to be searched
   * @param currentNames search only within current names of the groups
   * @param alternateNames search also in alternate names of the groups
   * @param queryOptions options for the hibernate query
   * @param typeOfGroups types of group to be included in the research
   * @return Set of Group object representing found groups satisfying search parameters.
   * @throws GrouperDAOException in case of a data access error
   */
  private static Set<Group> findAllByApproximateNameSecureHelper(final String name, final String scope,
      final boolean currentNames, final boolean alternateNames, final QueryOptions queryOptions,
      final Set<TypeOfGroup> typeOfGroups) throws GrouperDAOException {

    @SuppressWarnings("unchecked")
    Set<Group> resultGroups = (Set<Group>) HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
            StringBuilder hql = new StringBuilder("select distinct theGroup from Group theGroup ");
            ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();

            // see if we are adding more to the query
            boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                grouperSession.getSubject(), byHqlStatic, hql, "theGroup.uuid",
                AccessPrivilege.VIEW_PRIVILEGES);

            hql.append((!changedQuery) ? " where " : " and ");
            hql.append(" ( ");
            
            String lowerName = StringUtils.defaultString(name).toLowerCase();
            if (currentNames) {
              hql.append(" lower(theGroup.nameDb) like :theName or lower(theGroup.displayNameDb) like :theDisplayName ");
              byHqlStatic.setString("theName", "%" + lowerName + "%");
              byHqlStatic.setString("theDisplayName", "%" + lowerName + "%");
            }

            if (alternateNames) {
              if (currentNames) hql.append(" or ");
              hql.append(" theGroup.alternateNameDb like :theAlternateName ");
              byHqlStatic.setString("theAlternateName", "%" + lowerName + "%");
            }

            hql.append(" ) ");

            if (scope != null) {
              hql.append(" and theGroup.nameDb like :theStemScope ");
              byHqlStatic.setString("theStemScope", scope + "%");
            }

            // add in the typeOfGroups part
            appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
            byHqlStatic.setCacheable(false);

            // reset sorting
            if (queryOptions != null) {
              byHqlStatic.options(queryOptions);
            }

            byHqlStatic.createQuery(hql.toString());
            Set<Group> groups = byHqlStatic.listSet(Group.class);

            return groups;
          }
        });
    return resultGroups;
  }

  /**
   * Append the type of groups part to the HQL query for searching groups.
   * Note: this isn't in 2.0.0
   * 
   * @param groupAlias is the alias in the group hql query e.g. theGroup
   * @param typeOfGroups the set of TypeOfGroup or null for all
   * @param hql query so far
   * @param hqlQuery object to append the stored params to
   */
  private static void appendHqlQuery(String groupAlias, Set<TypeOfGroup> typeOfGroups, StringBuilder hql, HqlQuery hqlQuery) {
    if (GrouperUtil.length(typeOfGroups) <= 0) return;
    
    hql.append((hql.indexOf(" where ") > 0) ? " and ": " where ");
    hql.append(groupAlias).append(".typeOfGroupDb in ( ");
    
    Set<String> typeOfGroupStrings = new LinkedHashSet<String>();
    for (TypeOfGroup typeOfGroup : typeOfGroups) {
      typeOfGroupStrings.add(typeOfGroup.name());
    }
    
    hql.append(HibUtils.convertToInClause(typeOfGroupStrings, hqlQuery));
    hql.append(" ) ");
  }

  /**
   * Get the members for a group based on the VOOT group.
   * 
   * @param subject the subject querying the VOOT interface.
   * @param vootGroup the group to be looked to find members.
   * @param sortBy the field name to be used for sorting or null of no sorting.
   * @param start the first element in the result set (0 means start from beginning).
   * @param count the number of elements in the result set (-1 or 0 means find all).
   * @return the response to be sent back to user in JSON format.
   */
  public static VootGetMembersResponse getMembers(Subject subject, VootGroup vootGroup, String sortBy, int start, int count) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    // note the name is the id
    String groupName = vootGroup.getId();
    // throws exception if the group is not found
    Group group = GroupFinder.findByName(grouperSession, groupName, true);
    Set<Subject> memberSubjects = new HashSet<Subject>();

    Set<Subject> admins = new HashSet<Subject>();
    Set<Subject> updaters = new HashSet<Subject>();
    
    Set<Member> members = group.getMembers();
    for (Member member : members) {
      memberSubjects.add(member.getSubject());
      
      Set<Group> isAdminOf = member.getGroups(FieldFinder.find(Field.FIELD_NAME_ADMINS, true));
      if (isAdminOf.contains(group)) {
        admins.add(member.getSubject());
      }
      
      Set<Group> isUpdaterOf = member.getGroups(FieldFinder.find(Field.FIELD_NAME_UPDATERS, true));
      if (isUpdaterOf.contains(group)) {
        updaters.add(member.getSubject());
      }
    }
    
    // lets keep track of the subjects
    // since subjects have a composite key, then keep track with multikey
    Map<MultiKey, Subject> multiKeyToSubject = new HashMap<MultiKey, Subject>();

    // member, admin, manager (descriebd in VootGroup.GroupRoles enum)
    Map<MultiKey, String> memberToRole = new HashMap<MultiKey, String>();

    boolean subjectInGroup = false;
    for (Subject curSubject : memberSubjects) {
      if (curSubject.getSourceId().equals(subject.getSourceId()) && curSubject.getId().equals(subject.getId()))
        subjectInGroup = true;
      MultiKey subjectMultiKey = new MultiKey(curSubject.getSourceId(), curSubject.getId());
      multiKeyToSubject.put(subjectMultiKey, curSubject);
      memberToRole.put(subjectMultiKey, VootGroup.GroupRoles.MEMBER.toString());
    }
    for (Subject curSubject : updaters) {
      if (curSubject.getSourceId().equals(subject.getSourceId()) && curSubject.getId().equals(subject.getId()))
        subjectInGroup = true;
      MultiKey subjectMultiKey = new MultiKey(subject.getSourceId(), curSubject.getId());
      multiKeyToSubject.put(subjectMultiKey, curSubject);
      memberToRole.put(subjectMultiKey, VootGroup.GroupRoles.MANAGER.toString());
    }
    for (Subject curSubject : admins) {
      if (curSubject.getSourceId().equals(subject.getSourceId()) && curSubject.getId().equals(subject.getId()))
        subjectInGroup = true;
      MultiKey subjectMultiKey = new MultiKey(curSubject.getSourceId(), curSubject.getId());
      multiKeyToSubject.put(subjectMultiKey, curSubject);
      memberToRole.put(subjectMultiKey, VootGroup.GroupRoles.ADMIN.toString());
    }

    if (!GrouperSession.staticGrouperSession().getSubject().equals(subject) && !subjectInGroup) {
      throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + groupName);
    }

    VootGetMembersResponse vootGetMembersResponse = new VootGetMembersResponse();
    VootPerson[] result = new VootPerson[memberToRole.size()];

    // lets put them all back and make the person subjects
    int index = 0;
    for (MultiKey multiKey : memberToRole.keySet()) {
      Subject curSubject = multiKeyToSubject.get(multiKey);
      String role = memberToRole.get(multiKey);
      VootPerson vootPerson = new VootPerson(curSubject);
      vootPerson.setVoot_membership_role(role);
      result[index] = vootPerson;

      index++;
    }

    result = VootGetMembersResponse.sort(result, sortBy);
    vootGetMembersResponse.paginate(result, start, count);
    vootGetMembersResponse.setEntry(result, start, count);
    
    return vootGetMembersResponse;
  }

  /**
   * Get the groups that a person is in.
   * 
   * @param subject the subject representing the person used for search.
   * @param sortBy the field name to be used for sorting or null of no sorting.
   * @param start the first element in the result set (0 means start from beginning).
   * @param count the number of elements in the result set (-1 or 0 means find all).
   * @return the groups the subject passed as a parameter is part of.
   */
  public static VootGetGroupsResponse getGroups(Subject subject, String sortBy, int start, int count) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Member member = MemberFinder.findBySubject(grouperSession, subject, false);

    VootGetGroupsResponse vootGetGroupsResponse = new VootGetGroupsResponse();

    if (member == null) {
      vootGetGroupsResponse.paginate(null, start, count);
      return vootGetGroupsResponse;
    }

    // member, admin, manager
    Set<Group> groups = member.getGroups();
    Set<Group> admins = member.getGroups(FieldFinder.find(Field.FIELD_NAME_ADMINS, true));
    Set<Group> updaters = member.getGroups(FieldFinder.find(Field.FIELD_NAME_UPDATERS, true));

    Map<Group, String> groupToRole = new TreeMap<Group, String>();

    // if you are a member, and not an admin or updater, then you are a member
    for (Group group : GrouperUtil.nonNull(groups)) {
      groupToRole.put(group, VootGroup.GroupRoles.MEMBER.toString());
    }

    // if you are an updater and not an admin, then you are a manager
    for (Group group : GrouperUtil.nonNull(updaters)) {
      groupToRole.put(group, VootGroup.GroupRoles.MANAGER.toString());
    }

    // if you are an admin, then you are an admin
    for (Group group : GrouperUtil.nonNull(admins)) {
      groupToRole.put(group, VootGroup.GroupRoles.ADMIN.toString());
    }
    
    if (groupToRole.size() == 0) {
      vootGetGroupsResponse.paginate(null, start, count);
      return vootGetGroupsResponse;
    }

    VootGroup[] result = new VootGroup[groupToRole.size()];
    int index = 0;
    for (Group group : groupToRole.keySet()) {
      VootGroup vootGroup = new VootGroup(group);
      vootGroup.setVoot_membership_role(groupToRole.get(group));

      result[index] = vootGroup;

      index++;
    }

    result = VootGetGroupsResponse.sort(result, sortBy);
    vootGetGroupsResponse.paginate(result, start, count);
    vootGetGroupsResponse.setEntry(result, start, count);
    return vootGetGroupsResponse;
  }

  /**
   * Get the groups that a person is in, searching by their name.
   * 
   * @param search the search term to be searched in group name.
   * @param sortBy the field name to be used for sorting or null of no sorting.
   * @param start the first element in the result set (0 means start from beginning).
   * @param count the number of elements in the result set (-1 or 0 means find all).
   * @return the groups found satisfying search criteria.
   */
  public static VootGetGroupsResponse getGroups(String search, String sortBy, int start, int count) {
    VootGetGroupsResponse vootGetGroupsResponse = new VootGetGroupsResponse();

    // this isnt in 2.0.0
    String searchString = "%";
    if (search != null) {
      searchString = "%" + search + "%";
    }
    Set<Group> groups = findAllByApproximateNameSecureHelper(searchString, null, true, true, null, null);

    if (GrouperUtil.length(groups) == 0) {
      vootGetGroupsResponse.paginate(null, start, count);
      return vootGetGroupsResponse;
    }

    VootGroup[] result = new VootGroup[groups.size()];

    int index = 0;
    for (Group group : groups) {
      VootGroup vootGroup = new VootGroup(group);
      result[index] = vootGroup;
      index++;
    }

    result = VootGetGroupsResponse.sort(result, sortBy);
    vootGetGroupsResponse.paginate(result, start, count);
    vootGetGroupsResponse.setEntry(result, start, count);
    return vootGetGroupsResponse;
  }
}
