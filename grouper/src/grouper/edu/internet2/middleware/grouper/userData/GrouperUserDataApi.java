/**
 * Copyright 2014 Internet2
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.userData;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * api for grouper user data
 * @author mchyzer
 *
 */
public class GrouperUserDataApi {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject subject = SubjectFinder.findRootSubject();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:rules:ruleCheckOwnerId", true);
    GrouperUserDataApi.favoriteAttributeDefNameAdd("etc:grouperUi:grouperUiUserData", subject, attributeDefName);

    attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:permissionLimits:limitWeekday9to5", true);
    GrouperUserDataApi.favoriteAttributeDefNameAdd("etc:grouperUi:grouperUiUserData", subject, attributeDefName);

  }
  
  /** this is the max user data objects in json to fit in a 4k field */
  public static final int MAX_USER_DATA_OBJECTS = 30;
  
  /**
   * type of user data
   *
   */
  static enum GrouperUserDataType {
    
    favoriteAttributeDefName {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataFavoriteAttributeDefNamesAttributeDefName();
      }
      
    },
    
    favoriteAttributeDef {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataFavoriteAttributeDefsAttributeDefName();
      }
      
    },
    
    favoriteGroup {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataFavoriteGroupsAttributeDefName();
      }
      
    },
    
    favoriteStem {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataFavoriteStemsAttributeDefName();
      }
      
    },
    
    favoriteMember {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataFavoriteSubjectsAttributeDefName();
      }
      
    },

    preferences {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataPreferencesAttributeDefName();
      }
      
    },
    
    recentAttributeDefName {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataRecentAttributeDefNamesAttributeDefName();
      }
      
    },
    
    recentAttributeDef {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataRecentAttributeDefsAttributeDefName();
      }
      
    },
    
    recentGroup {
      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataRecentGroupsAttributeDefName();
      }
      
    },
    
    recentStem {

      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataRecentStemsAttributeDefName();
      }
    },

    /**
     * recent subject
     */
    recentMember {

      /**
       * @see GrouperUserDataType#attributeDefName()
       */
      @Override
      public AttributeDefName attributeDefName() {
        return GrouperUserDataUtils.grouperUserDataRecentSubjectsAttributeDefName();
      }
    };
    
    /**
     * 
     * @return
     */
    public abstract AttributeDefName attributeDefName();
    
    /**
     * get the list of data
     * @param userDataGroupName
     * @param subject
     * @param uuid
     */
    public UserDataList retrieve(String userDataGroupName, Subject subject) {
      
      AttributeDefName attributeDefName = this.attributeDefName();
      
      AttributeAssignValue attributeAssignValue = userDataAttributeAssignValue(userDataGroupName, subject, attributeDefName, true, false);
      
      String value = null;
      
      if (attributeAssignValue != null) {
        value = attributeAssignValue.getValueString();
      }
      
      UserDataList userDataList = UserDataList.jsonMarshalFrom(value);
      
      return userDataList;
    }
    
    /**
     * get the list of data
     * @param userDataGroupName
     * @param subject
     * @param uuid
     */
    public <T> T retrieve(String userDataGroupName, Subject subject, Class<T> objectClass) {
      
      AttributeDefName attributeDefName = this.attributeDefName();
      
      AttributeAssignValue attributeAssignValue = userDataAttributeAssignValue(userDataGroupName, subject, attributeDefName, true, false);
      
      String value = null;
      
      if (attributeAssignValue != null) {
        value = attributeAssignValue.getValueString();
      }
      
      if (StringUtils.isBlank(value)) {
        return null;
      }
      return GrouperUtil.jsonConvertFrom(value, objectClass);
    }
    
    /**
     * add a uuid to a list of user data objects
     * @param userDataGroupName
     * @param subjectToAddTo
     * @param uuid
     */
    public void add(String userDataGroupName, Subject subjectToAddTo, String uuid) {
      
      AttributeDefName attributeDefName = this.attributeDefName();
      
      AttributeAssignValue attributeAssignValue = userDataAttributeAssignValue(userDataGroupName, subjectToAddTo, attributeDefName, false, true);
      
      String value = null;
      
      if (attributeAssignValue != null) {
        value = attributeAssignValue.getValueString();
      }
      
      UserDataList userDataList = UserDataList.jsonMarshalFrom(value);
      
      if (userDataList == null) {
        userDataList = new UserDataList();
      }
      
      boolean changed = userDataList.addUserDataObject(new UserDataObject(uuid, System.currentTimeMillis()), MAX_USER_DATA_OBJECTS);
      
      if (changed) {
        //get the json
        String json = userDataList.jsonConvertTo();
        if (attributeAssignValue == null) {
          
        } else {
          attributeAssignValue.setValueString(json);
          
          attributeAssignValue.saveOrUpdate();
        }
      }


    }
    
    
    /**
     * replace uuids with a list
     * @param userDataGroupName
     * @param subject
     * @param uuids to replace
     */
    public void replace(String userDataGroupName, Subject subject, Set<String> uuids) {
      
      AttributeDefName attributeDefName = this.attributeDefName();
      
      AttributeAssignValue attributeAssignValue = userDataAttributeAssignValue(userDataGroupName, subject, attributeDefName, false, true);
      
      String value = null;

      if (attributeAssignValue != null) {
        value = attributeAssignValue.getValueString();
      }
      
      UserDataList userDataList = UserDataList.jsonMarshalFrom(value);
      
      if (userDataList == null) {
        
        //no data, no uuids
        if (GrouperUtil.length(uuids) == 0) {
          return;
        }
        
        userDataList = new UserDataList();
      }
      
      boolean changed = userDataList.replaceUserDataObjectsWithSubset(uuids, MAX_USER_DATA_OBJECTS);
      
      if (changed) {
        //get the json
        String json = userDataList.jsonConvertTo();
        if (attributeAssignValue == null) {
          attributeAssignValue = userDataAttributeAssignValue(userDataGroupName, subject, attributeDefName, false, true);
        }
        attributeAssignValue.setValueString(json);
        
        attributeAssignValue.saveOrUpdate();
      }
      
    }
    
    /**
     * replace attribute value with another value
     * @param userDataGroupName
     * @param subject
     * @param uuids to replace
     */
    public void replace(String userDataGroupName, Subject subject, String newValue) {
      
      AttributeDefName attributeDefName = this.attributeDefName();
      
      AttributeAssignValue attributeAssignValue = userDataAttributeAssignValue(userDataGroupName, subject, attributeDefName, false, true);
      
      String value = null;

      if (attributeAssignValue != null) {
        value = attributeAssignValue.getValueString();
      }

      if (!StringUtils.equals(value, newValue)) {
        attributeAssignValue.setValueString(newValue);
        
        attributeAssignValue.saveOrUpdate();
      }
      
      
    }
    
    /**
     * add a uuid to a list of user data objects
     * @param userDataGroupName
     * @param subject
     * @param uuid
     */
    public void remove(String userDataGroupName, Subject subject, String uuid) {

      AttributeDefName attributeDefName = this.attributeDefName();
      
      AttributeAssignValue attributeAssignValue = userDataAttributeAssignValue(userDataGroupName, subject, attributeDefName, false, false);
      
      String value = null;
      
      if (attributeAssignValue != null) {
        value = attributeAssignValue.getValueString();
      }
      
      UserDataList userDataList = UserDataList.jsonMarshalFrom(value);
      
      if (userDataList != null) {
        
        boolean changed = userDataList.removeUuid(uuid);
        
        if (changed) {
          //get the json
          String json = userDataList.jsonConvertTo();
          attributeAssignValue.setValueString(json);
          
          attributeAssignValue.saveOrUpdate();
        }
      }
      
    }
    
  }
  
  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param group
   */
  public static void favoriteGroupAdd(final String userDataGroupName, final Subject subjectToAddTo, final Group group) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (group == null) {
          throw new NullPointerException("Why is group null?");
        }
        
        //check security
        if (!group.canHavePrivilege(subjectToAddTo, AccessPrivilege.VIEW.getName(), false)) {
          throw new InsufficientPrivilegeException("Subject: " + GrouperUtil.subjectToString(subjectToAddTo) 
              + " does not have view on group " + group.getName());
        }
        
        GrouperUserDataType.favoriteGroup.add(userDataGroupName, subjectToAddTo, group.getUuid());
        
        return null;
      }
    });

  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param group
   */
  public static void favoriteGroupRemove(final String userDataGroupName, final Subject subjectToAddTo, final Group group) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (group == null) {
          throw new NullPointerException("Why is group null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.favoriteGroup.remove(userDataGroupName, subjectToAddTo, group.getUuid());
        return null;
      }
    });

    
  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the favorite groups for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<Group> favoriteGroups(final String userDataGroupName, final Subject subject) {

    return (Set<Group>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return groupList(GrouperUserDataType.favoriteGroup, userDataGroupName, subject);
      }
    });

  }

  /**
   * list groups
   * @param groupUserDataType
   * @param userDataGroupName
   * @param subject
   * @return the groups
   */
  private static Set<Group> groupList(GrouperUserDataType groupUserDataType, String userDataGroupName, Subject subject) {
    UserDataList userDataList = groupUserDataType.retrieve(userDataGroupName, subject);

    //convert to groups
    Set<Group> groups = new LinkedHashSet<Group>();

    if (userDataList != null) {

      final Set<String> uuids = new LinkedHashSet<String>();
      
      //these are the ones currently in the list
      for (UserDataObject userDataObject : GrouperUtil.nonNull(userDataList.getList(), UserDataObject.class)) {
        
        String uuid = userDataObject.getUuid();
        uuids.add(uuid);

      }

      if (uuids.size() > 0) {
      
        //these are the ones the user is allowed to see
        GrouperSession userSession = GrouperSession.start(subject, false);
  
        try {
          groups = (Set<Group>)GrouperSession.callbackGrouperSession(userSession, new GrouperSessionHandler() {
  
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
  
              return GrouperDAOFactory.getFactory().getGroup().findByUuidsSecure(uuids, null, TypeOfGroup.GROUP_OR_ROLE_SET);
  
            }
          });
        } finally {
          GrouperSession.stopQuietly(userSession);
        }
        
        //reorder them to be in id order
        groups = GrouperUtil.sortByIds(uuids, groups);
        
      }
      
      //remove the not allowed ones?
      if (groups.size() != uuids.size()) {

        //lets remove stuff that isnt allowed
        Set<String> allowedGroupIds = new HashSet<String>();
        for (Group group : GrouperUtil.nonNull(groups)) {
          allowedGroupIds.add(group.getId());
        }

        groupUserDataType.replace(userDataGroupName, subject, allowedGroupIds);
        
      }
      
    }
    
    return groups;
    
  }
  
  /**
   * cache the group that the memberships are in
   */
  private static GrouperCache<String, Group> userDataGroupCache = null;

  /**
   * get the group that user data uses, will cache this for 10 minutes by default
   * @param groupName
   * @return the group
   */
  private static Group userDataGroup(String groupName) {
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    if (!PrivilegeHelper.isRoot(grouperSession)) {
      throw new RuntimeException("Grouper session must be root in user data! " + GrouperUtil.subjectToString(grouperSession.getSubject()));
    }
    
    Group group = userDataGroupCache().get(groupName);
    
    if (group == null) {
      
      synchronized (GrouperUserDataUtils.class) {
        group = userDataGroupCache().get(groupName);
        
        if (group == null) {
          
          group = new GroupSave(grouperSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).assignDescription(
              "Internal group for grouper which has user data stored in membership attributes for " + GrouperUtil.extensionFromName(groupName) ).save();
  
          //this is an internal group, other people do not need to read or view it
          group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
          group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
          
          userDataGroupCache().put(groupName, group);
          
        }
      }
      
    }
    
    return group;
    
  }

  /**
   * get the cache for groups and init if needed
   * @return the cache
   */
  private static GrouperCache<String, Group> userDataGroupCache() {
    if (userDataGroupCache == null) {
      synchronized (GrouperUserDataUtils.class) {
        if (userDataGroupCache == null) {
          userDataGroupCache = new GrouperCache<String, Group>(GrouperUserDataUtils.class.getName() + ".userDataGroupCache");
        }        
      }
    }
    return userDataGroupCache;
  }

  /**
   * cache the memberships for a group and subject.  MultiKey is the group name, subject source, and subject
   */
  private static GrouperCache<MultiKey, Membership> userDataMembershipCache = null;

  /**
   * get the membership that user data uses, will cache this for 10 minutes by default
   * @param groupName
   * @return the membership
   */
  private static Membership userDataMembership(String groupName, Subject subject) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    if (!PrivilegeHelper.isRoot(grouperSession)) {
      throw new RuntimeException("Grouper session must be root in user data! " + GrouperUtil.subjectToString(grouperSession.getSubject()));
    }
    
    MultiKey multiKey = new MultiKey(groupName, subject.getSourceId(), subject.getId());
    
    Membership membership = userDataMembershipCache().get(multiKey);
    
    if (membership == null) {
      
      synchronized (GrouperUserDataUtils.class) {
        membership = userDataMembershipCache().get(multiKey);
        
        if (membership == null) {
          
          Group group = userDataGroup(groupName);
          
          membership = new MembershipFinder().addGroupId(group.getId()).addSubject(subject)
              .assignMembershipType(MembershipType.IMMEDIATE).findMembership(false);
          
          if (membership == null) {
  
            group.addMember(subject, false);
            
            membership = new MembershipFinder().addGroupId(group.getId()).addSubject(subject)
                .assignMembershipType(MembershipType.IMMEDIATE).findMembership(true);
  
          }
                    
          userDataMembershipCache().put(multiKey, membership);
          
        }
      }
      
    }
    
    return membership;
    
  }

  /**
   * get the cache for memberships and init if needed
   * @return the cache
   */
  private static GrouperCache<MultiKey, Membership> userDataMembershipCache() {
    if (userDataMembershipCache == null) {
      synchronized (GrouperUserDataUtils.class) {
        if (userDataMembershipCache == null) {
          userDataMembershipCache = new GrouperCache<MultiKey, Membership>(GrouperUserDataUtils.class.getName() + ".userDataMembershipCache");
        }        
      }
    }
    return userDataMembershipCache;
  }

  

  /**
   * cache the attribute values for a group, subject, attribute def name.  MultiKey is the group name, subject source, subject id, name of attribute def name
   */
  private static GrouperCache<MultiKey, AttributeAssignValue> userDataAttributeValueCache = null;

  /**
   * get the attributeAssignValue for this group, user, and attributeDefName.  return null if it is not there.
   * cache this for two minutes
   * @param groupName
   * @param subject
   * @param attributeDefName
   * @param useCache
   * @param createIfNotThere
   * @return the group
   */
  private static AttributeAssignValue userDataAttributeAssignValue(String groupName, Subject subject, AttributeDefName attributeDefName, 
      boolean useCache, boolean createIfNotThere) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    if (!PrivilegeHelper.isRoot(grouperSession)) {
      throw new RuntimeException("Grouper session must be root in user data! " + GrouperUtil.subjectToString(grouperSession.getSubject()));
    }
    
    AttributeAssignValue attributeAssignValue = null;
    
    MultiKey multiKey = new MultiKey(groupName, subject.getSourceId(), subject.getId(), attributeDefName.getName());

    //if using cache
    if (useCache) {
      attributeAssignValue = userDataAttributeValueCache().get(multiKey);
      //if its null, and has the key, then it is null, and if not create, then done
      if (attributeAssignValue == null && userDataAttributeValueCache().containsKey(multiKey) && !createIfNotThere) {
        return null;
      }
    }
    
    if (attributeAssignValue == null) {
      
      synchronized (GrouperUserDataUtils.class) {

        //if using cache
        if (useCache) {
          attributeAssignValue = userDataAttributeValueCache().get(multiKey);
          //if its null, and has the key, then it is null, and if not create, then done
          if (attributeAssignValue == null && userDataAttributeValueCache().containsKey(multiKey) && !createIfNotThere) {
            return null;
          }
        }
        
        if (attributeAssignValue == null) {
          
          Membership membership = userDataMembership(groupName, subject);
          
          //get it from not cache
          AttributeAssign attributeAssign = membership.getAttributeDelegate()
              .retrieveAssignment("assign", GrouperUserDataUtils.grouperUserDataAttributeDefName(), false, false);
          
          if (attributeAssign == null) {
            AttributeAssignResult attributeAssignResult = membership.getAttributeDelegate().assignAttribute(GrouperUserDataUtils.grouperUserDataAttributeDefName());
            attributeAssign = attributeAssignResult.getAttributeAssign();
          }
          
          attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(attributeDefName.getName());
          
          if (attributeAssignValue == null && createIfNotThere) {
            
            attributeAssignValue = attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null)
                .getAttributeAssignValueResult().getAttributeAssignValue();
            
          }
          
          //add this to cache so it is the most recent
          userDataAttributeValueCache().put(multiKey, attributeAssignValue);
          
        }
      }
      
    }
    
    return attributeAssignValue;
    
  }

  /**
   * get the cache for attribute values and init if needed
   * @return the cache
   */
  private static GrouperCache<MultiKey, AttributeAssignValue> userDataAttributeValueCache() {
    if (userDataAttributeValueCache == null) {
      synchronized (GrouperUserDataUtils.class) {
        if (userDataAttributeValueCache == null) {
          userDataAttributeValueCache = new GrouperCache<MultiKey, AttributeAssignValue>(GrouperUserDataUtils.class.getName() + ".userDataAttributeValueCache");
        }        
      }
    }
    return userDataAttributeValueCache;
  }

  /**
   * cache the results for a user, type, group.  MultiKey is the group name, subject source, subject, and grouperUserDataType
   */
  private static GrouperCache<MultiKey, Set<Object>> userDataResultCache = null;

  /**
   * get the membership that user data uses, will cache this for 10 minutes by default
   * @param groupName
   * @return the membership
   */
  private static Set<Object> userDataResult(String groupName, Subject subject, GrouperUserDataType grouperUserDataType) {

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    if (!PrivilegeHelper.isRoot(grouperSession)) {
      throw new RuntimeException("Grouper session must be root in user data! " + GrouperUtil.subjectToString(grouperSession.getSubject()));
    }

    MultiKey multiKey = new MultiKey(groupName, subject.getSourceId(), subject.getId(), grouperUserDataType);

//    Set<Group> groups = userDataResultCache().get(multiKey);
//
//    if (membership == null) {
//      
//      synchronized (GrouperUserDataUtils.class) {
//        membership = userDataMembershipCache().get(multiKey);
//        
//        if (membership == null) {
//          
//          Group group = userDataGroup(groupName);
//          
//          membership = new MembershipFinder().addGroupId(group.getId()).addSubject(subject)
//              .assignMembershipType(MembershipType.IMMEDIATE).findMembership(false);
//          
//          if (membership == null) {
//  
//            group.addMember(subject, false);
//            
//            membership = new MembershipFinder().addGroupId(group.getId()).addSubject(subject)
//                .assignMembershipType(MembershipType.IMMEDIATE).findMembership(true);
//  
//          }
//                    
//          userDataMembershipCache().put(multiKey, membership);
//          
//        }
//      }
//      
//    }
//    
//    return membership;
//TODO fix this
    return null;
  }

  /**
   * get the cache for results and init if needed
   * @return the cache
   */
  private static GrouperCache<MultiKey, Set<Object>> userDataResultCache() {
    if (userDataResultCache == null) {
      synchronized (GrouperUserDataUtils.class) {
        if (userDataResultCache == null) {
          userDataResultCache = new GrouperCache(GrouperUserDataUtils.class.getName() + ".userDataResultCache");
        }        
      }
    }
    return userDataResultCache;
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param group
   */
  public static void recentlyUsedGroupAdd(final String userDataGroupName, final Subject subjectToAddTo, final Group group) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (group == null) {
          throw new NullPointerException("Why is group null?");
        }
        
        //check security
        if (!group.canHavePrivilege(subjectToAddTo, AccessPrivilege.VIEW.getName(), false)) {
          throw new InsufficientPrivilegeException("Subject: " + GrouperUtil.subjectToString(subjectToAddTo) 
              + " does not have view on group " + group.getName());
        }
        
        GrouperUserDataType.recentGroup.add(userDataGroupName, subjectToAddTo, group.getUuid());
        return null;
      }
    });
  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the recently used groups for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<Group> recentlyUsedGroups(final String userDataGroupName, final Subject subject) {

    return (Set<Group>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return groupList(GrouperUserDataType.recentGroup, userDataGroupName, subject);
      }
    });

    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param group
   */
  public static void recentlyUsedGroupRemove(final String userDataGroupName, final Subject subjectToAddTo, final Group group) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (group == null) {
          throw new NullPointerException("Why is group null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.recentGroup.remove(userDataGroupName, subjectToAddTo, group.getUuid());

        return null;
      }
    });
    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDef
   */
  public static void favoriteAttributeDefAdd(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDef attributeDef) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (attributeDef == null) {
          throw new NullPointerException("Why is attributeDef null?");
        }
        
        //check security
        if (!attributeDef.getPrivilegeDelegate().canAttrView(subjectToAddTo)) {
          throw new InsufficientPrivilegeException("Subject: " + GrouperUtil.subjectToString(subjectToAddTo) 
              + " does not have view on attributeDef " + attributeDef.getName());
        }
        
        GrouperUserDataType.favoriteAttributeDef.add(userDataGroupName, subjectToAddTo, attributeDef.getUuid());
        
        return null;
      }
    });

  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDef
   */
  public static void favoriteAttributeDefRemove(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDef attributeDef) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (attributeDef == null) {
          throw new NullPointerException("Why is attributeDef null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.favoriteAttributeDef.remove(userDataGroupName, subjectToAddTo, attributeDef.getUuid());
        return null;
      }
    });

    
  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the favorite attributeDefs for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<AttributeDef> favoriteAttributeDefs(final String userDataGroupName, final Subject subject) {
  
    return (Set<AttributeDef>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return attributeDefList(GrouperUserDataType.favoriteAttributeDef, userDataGroupName, subject);
      }
    });

    
  }

  /**
   * attribute def list
   * @param attributeDefUserDataType
   * @param userDataGroupName
   * @param subject
   * @return the set of attribute defs
   */
  private static Set<AttributeDef> attributeDefList(GrouperUserDataType attributeDefUserDataType, String userDataGroupName, Subject subject) {
    
    UserDataList userDataList = attributeDefUserDataType.retrieve(userDataGroupName, subject);
    
    //convert to groups
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
  
    if (userDataList != null) {
  
      final Set<String> uuids = new LinkedHashSet<String>();
      
      //these are the ones currently in the list
      for (UserDataObject userDataObject : GrouperUtil.nonNull(userDataList.getList(), UserDataObject.class)) {
        
        String uuid = userDataObject.getUuid();
        uuids.add(uuid);
  
      }
      
      if (uuids.size() > 0) {
        //these are the ones the user is allowed to see
        GrouperSession userSession = GrouperSession.start(subject, false);
    
        try {
          attributeDefs = (Set<AttributeDef>)GrouperSession.callbackGrouperSession(userSession, new GrouperSessionHandler() {
    
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
    
              return GrouperDAOFactory.getFactory().getAttributeDef().findByIdsSecure(uuids, null);
    
            }
          });
        } finally {
          GrouperSession.stopQuietly(userSession);
        }
        
        //reorder them to be in id order
        attributeDefs = GrouperUtil.sortByIds(uuids, attributeDefs);

      }
      
      //remove the not allowed ones?
      if (attributeDefs.size() != uuids.size()) {
  
        //lets remove stuff that isnt allowed
        Set<String> allowedAttributeDefIds = new HashSet<String>();
        for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
          allowedAttributeDefIds.add(attributeDef.getId());
        }
  
        attributeDefUserDataType.replace(userDataGroupName, subject, allowedAttributeDefIds);
        
      }
      
    }
    return attributeDefs;

    
  }
  
  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDefName
   */
  public static void favoriteAttributeDefNameAdd(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDefName attributeDefName) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (attributeDefName == null) {
          throw new NullPointerException("Why is attributeDefName null?");
        }
        
        //check security
        if (!attributeDefName.getAttributeDef().getPrivilegeDelegate().canAttrView(subjectToAddTo)) {
          throw new InsufficientPrivilegeException("Subject: " + GrouperUtil.subjectToString(subjectToAddTo) 
              + " does not have view on attributeDef " + attributeDefName.getAttributeDef().getName()
              + " for attributeDefName: " + attributeDefName.getName());
        }
        
        GrouperUserDataType.favoriteAttributeDefName.add(userDataGroupName, subjectToAddTo, attributeDefName.getId());
        return null;
      }
    });

    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDefName
   */
  public static void favoriteAttributeDefNameRemove(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDefName attributeDefName) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (attributeDefName == null) {
          throw new NullPointerException("Why is attributeDefName null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.favoriteAttributeDefName.remove(userDataGroupName, subjectToAddTo, attributeDefName.getId());
        
        return null;
      }
    });

  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the favorite attributeDefNames for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<AttributeDefName> favoriteAttributeDefNames(final String userDataGroupName, final Subject subject) {

    return (Set<AttributeDefName>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return attributeDefNameList(GrouperUserDataType.favoriteAttributeDefName, userDataGroupName, subject);
      }
    });

    
  }

  /**
   * list attribute def names base on the user data type
   * @param attributeDefNameUserDataType
   * @param userDataGroupName
   * @param subject
   * @return the attribute def names
   */
  private static Set<AttributeDefName> attributeDefNameList(GrouperUserDataType attributeDefNameUserDataType, String userDataGroupName, Subject subject) {
    UserDataList userDataList = attributeDefNameUserDataType.retrieve(userDataGroupName, subject);
    
    //convert to groups
    Set<AttributeDefName> attributeDefNames = new LinkedHashSet<AttributeDefName>();

    if (userDataList != null) {
  
      final Set<String> uuids = new LinkedHashSet<String>();
      
      //these are the ones currently in the list
      for (UserDataObject userDataObject : GrouperUtil.nonNull(userDataList.getList(), UserDataObject.class)) {
        
        String uuid = userDataObject.getUuid();
        uuids.add(uuid);
  
      }

      if (uuids.size() > 0) {
        //these are the ones the user is allowed to see
        GrouperSession userSession = GrouperSession.start(subject, false);
    
        try {
          attributeDefNames = (Set<AttributeDefName>)GrouperSession.callbackGrouperSession(userSession, new GrouperSessionHandler() {
    
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
    
              return GrouperDAOFactory.getFactory().getAttributeDefName().findByIdsSecure(uuids, null);
    
            }
          });
        } finally {
          GrouperSession.stopQuietly(userSession);
        }

        //reorder them to be in id order
        attributeDefNames = GrouperUtil.sortByIds(uuids, attributeDefNames);

      }
      
      //remove the not allowed ones?
      if (attributeDefNames.size() != uuids.size()) {
  
        //lets remove stuff that isnt allowed
        Set<String> allowedAttributeDefNameIds = new HashSet<String>();
        for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
          allowedAttributeDefNameIds.add(attributeDefName.getId());
        }
  
        attributeDefNameUserDataType.replace(userDataGroupName, subject, allowedAttributeDefNameIds);
        
      }
      
    }
    return attributeDefNames;

  }
  
  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param stem
   */
  public static void favoriteStemAdd(final String userDataGroupName, final Subject subjectToAddTo, final Stem stem) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (stem == null) {
          throw new NullPointerException("Why is stem null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.favoriteStem.add(userDataGroupName, subjectToAddTo, stem.getUuid());

        return null;
      }
    });
    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param preferences
   */
  public static void preferencesAssign(final String userDataGroupName, final Subject subjectToAddTo, final Object preferences) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        String preferencesString = null;
        
        if (preferences != null) {
          preferencesString = GrouperUtil.jsonConvertTo(preferences, false);
        }
        
        //no need to check security
        
        GrouperUserDataType.preferences.replace(userDataGroupName, subjectToAddTo, preferencesString);

        return null;
      }
    });
    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param preferences
   */
  @SuppressWarnings("unchecked")
  public static <T> T preferences(final String userDataGroupName, final Subject subjectToAddTo, final Class<T> preferencesClass) {
    
    return (T)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        
        //no need to check security
        return (T)GrouperUserDataType.preferences.retrieve(userDataGroupName, subjectToAddTo, preferencesClass);

      }
    });
    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param stem
   */
  public static void favoriteStemRemove(final String userDataGroupName, final Subject subjectToAddTo, final Stem stem) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (stem == null) {
          throw new NullPointerException("Why is stem null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.favoriteStem.remove(userDataGroupName, subjectToAddTo, stem.getUuid());

        return null;
      }
    });
    
  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the favorite stems for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<Stem> favoriteStems(final String userDataGroupName, final Subject subject) {
  
    return (Set<Stem>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return stemList(GrouperUserDataType.favoriteStem, userDataGroupName, subject);
      }
    });

  }

  /**
   * handle a listing of stems
   * @param stemDataType
   * @param userDataGroupName
   * @param subject
   * @return the stems
   */
  @SuppressWarnings("unchecked")
  private static Set<Stem> stemList(GrouperUserDataType stemDataType, String userDataGroupName, Subject subject) {
    UserDataList userDataList = stemDataType.retrieve(userDataGroupName, subject);
    
    //convert to stems
    Set<Stem> stems = new LinkedHashSet<Stem>();
  
    if (userDataList != null) {
  
      final Set<String> uuids = new LinkedHashSet<String>();
      
      //these are the ones currently in the list
      for (UserDataObject userDataObject : GrouperUtil.nonNull(userDataList.getList(), UserDataObject.class)) {
        
        String uuid = userDataObject.getUuid();
        uuids.add(uuid);
  
      }
      
      if (uuids.size() > 0) {
      
        //these are the ones the user is allowed to see
        GrouperSession userSession = GrouperSession.start(subject, false);
    
        try {
          stems = (Set<Stem>)GrouperSession.callbackGrouperSession(userSession, new GrouperSessionHandler() {
    
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
    
              return GrouperDAOFactory.getFactory().getStem().findByUuids(uuids, null);
    
            }
          });
        } finally {
          GrouperSession.stopQuietly(userSession);
        }

        //reorder them to be in id order
        stems = GrouperUtil.sortByIds(uuids, stems);

      }
      
      //remove the not allowed ones?
      if (stems.size() != uuids.size()) {
  
        //lets remove stuff that isnt allowed
        Set<String> allowedStemIds = new HashSet<String>();
        for (Stem stem : GrouperUtil.nonNull(stems)) {
          allowedStemIds.add(stem.getUuid());
        }
  
        stemDataType.replace(userDataGroupName, subject, allowedStemIds);
        
      }
      
    }
    return stems;

  }

  /**
   * @param subjectToRemoveFrom
   * @param userDataGroupName
   * @param subjectThatIsFavorite
   */
  public static void favoriteMemberRemove(final String userDataGroupName, final Subject subjectToRemoveFrom, 
      final Subject subjectThatIsFavorite) {
    Member member = (Member)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return MemberFinder.findBySubject(grouperSession, subjectThatIsFavorite, true);
      }
        });
    favoriteMemberRemove(userDataGroupName, subjectToRemoveFrom, member);
  }

    
    
  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param subjectThatIsFavorite
   */
  public static void favoriteMemberAdd(final String userDataGroupName, final Subject subjectToAddTo, final Subject subjectThatIsFavorite) {
    Member member = (Member)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return MemberFinder.findBySubject(grouperSession, subjectThatIsFavorite, true);
      }
        });
    favoriteMemberAdd(userDataGroupName, subjectToAddTo, member);
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param subjectThatIsRecentlyUsed
   */
  public static void recentlyUsedMemberAdd(final String userDataGroupName, final Subject subjectToAddTo, final Subject subjectThatIsRecentlyUsed) {
    Member member = (Member)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return MemberFinder.findBySubject(grouperSession, subjectThatIsRecentlyUsed, true);
      }
        });
    recentlyUsedMemberAdd(userDataGroupName, subjectToAddTo, member);
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param member
   */
  public static void favoriteMemberAdd(final String userDataGroupName, final Subject subjectToAddTo, final Member member) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (member == null) {
          throw new NullPointerException("Why is member null?");
        }
        
        //no need to check security
        GrouperUserDataType.favoriteMember.add(userDataGroupName, subjectToAddTo, member.getUuid());
        
        return null;
      }
    });

  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param member
   */
  public static void favoriteMemberRemove(final String userDataGroupName, final Subject subjectToAddTo, final Member member) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (member == null) {
          throw new NullPointerException("Why is member null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.favoriteMember.remove(userDataGroupName, subjectToAddTo, member.getUuid());
        return null;
      }
    });

    
  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the favorite members for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<Member> favoriteMembers(final String userDataGroupName, final Subject subject) {
    
    return (Set<Member>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return memberList(GrouperUserDataType.favoriteMember, userDataGroupName, subject);
        
      }
    });

  }

  private static Set<Member> memberList(GrouperUserDataType memberUserDataType, String userDataGroupName, Subject subject) {
    UserDataList userDataList = memberUserDataType.retrieve(userDataGroupName, subject);
    
    //convert to subjects
    Set<Member> members = new LinkedHashSet<Member>();
  
    if (userDataList != null) {
  
      final Set<String> uuids = new LinkedHashSet<String>();
      
      //these are the ones currently in the list
      for (UserDataObject userDataObject : GrouperUtil.nonNull(userDataList.getList(), UserDataObject.class)) {
        
        String uuid = userDataObject.getUuid();
        uuids.add(uuid);

      }
      
      if (uuids.size() > 0) {
        //these are the ones the user is allowed to see
        GrouperSession userSession = GrouperSession.start(subject, false);
    
        try {
          members = (Set<Member>)GrouperSession.callbackGrouperSession(userSession, new GrouperSessionHandler() {
    
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
    
              return GrouperDAOFactory.getFactory().getMember().findByIds(uuids, null);
    
            }
          });
          
          //reorder them to be in id order
          members = GrouperUtil.sortByIds(uuids, members);

        } finally {
          GrouperSession.stopQuietly(userSession);
        }
      }
      
      //remove the not allowed ones?
      if (members.size() != uuids.size()) {
  
        //lets remove stuff that isnt allowed
        Set<String> allowedMemberIds = new HashSet<String>();
        for (Member member : GrouperUtil.nonNull(members)) {
          allowedMemberIds.add(member.getUuid());
        }
  
        memberUserDataType.replace(userDataGroupName, subject, allowedMemberIds);
        
      }
      
    }
    return members;

  }
  
  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param stem
   */
  public static void recentlyUsedStemAdd(final String userDataGroupName, final Subject subjectToAddTo, final Stem stem) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (stem == null) {
          throw new NullPointerException("Why is stem null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.recentStem.add(userDataGroupName, subjectToAddTo, stem.getUuid());
        
        return null;
      }
    });

    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param stem
   */
  public static void recentlyUsedStemRemove(final String userDataGroupName, final Subject subjectToAddTo, final Stem stem) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (stem == null) {
          throw new NullPointerException("Why is stem null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.recentStem.remove(userDataGroupName, subjectToAddTo, stem.getUuid());
        return null;
      }
    });
    
  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the recently used stems for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<Stem> recentlyUsedStems(final String userDataGroupName, final Subject subject) {
  
    return (Set<Stem>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return stemList(GrouperUserDataType.recentStem, userDataGroupName, subject);
        
      }
    });

  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDef
   */
  public static void recentlyUsedAttributeDefAdd(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDef attributeDef) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (attributeDef == null) {
          throw new NullPointerException("Why is attributeDef null?");
        }
        
        //check security
        if (!attributeDef.getPrivilegeDelegate().canAttrView(subjectToAddTo)) {
          throw new InsufficientPrivilegeException("Subject: " + GrouperUtil.subjectToString(subjectToAddTo) 
              + " does not have view on attributeDef " + attributeDef.getName());
        }
        
        GrouperUserDataType.recentAttributeDef.add(userDataGroupName, subjectToAddTo, attributeDef.getUuid());
        
        return null;
      }
    });

  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDef
   */
  public static void recentlyUsedAttributeDefRemove(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDef attributeDef) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (attributeDef == null) {
          throw new NullPointerException("Why is attributeDef null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.recentAttributeDef.remove(userDataGroupName, subjectToAddTo, attributeDef.getUuid());

        return null;
      }
    });
    
  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the favorite attributeDefs for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<AttributeDef> recentlyUsedAttributeDefs(final String userDataGroupName, final Subject subject) {
  
    return (Set<AttributeDef>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        return attributeDefList(GrouperUserDataType.recentAttributeDef, userDataGroupName, subject);
      }
    });
    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDefName
   */
  public static void recentlyUsedAttributeDefNameAdd(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDefName attributeDefName) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (attributeDefName == null) {
          throw new NullPointerException("Why is attributeDefName null?");
        }
        
        //check security
        if (!attributeDefName.getAttributeDef().getPrivilegeDelegate().canAttrView(subjectToAddTo)) {
          throw new InsufficientPrivilegeException("Subject: " + GrouperUtil.subjectToString(subjectToAddTo) 
              + " does not have view on attributeDef " + attributeDefName.getAttributeDef().getName()
              + " for attributeDefName: " + attributeDefName.getName());
        }
        
        GrouperUserDataType.recentAttributeDefName.add(userDataGroupName, subjectToAddTo, attributeDefName.getId());
        return null;
      }
    });

    
  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param attributeDefName
   */
  public static void recentlyUsedAttributeDefNameRemove(final String userDataGroupName, final Subject subjectToAddTo, final AttributeDefName attributeDefName) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (attributeDefName == null) {
          throw new NullPointerException("Why is attributeDefName null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.recentAttributeDefName.remove(userDataGroupName, subjectToAddTo, attributeDefName.getId());
        
        return null;
      }
    });

  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the recent attributeDefNames for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<AttributeDefName> recentlyUsedAttributeDefNames(final String userDataGroupName, final Subject subject) {

    return (Set<AttributeDefName>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return attributeDefNameList(GrouperUserDataType.recentAttributeDefName, userDataGroupName, subject);
        
      }
    });

  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param member
   */
  public static void recentlyUsedMemberAdd(final String userDataGroupName, final Subject subjectToAddTo, final Member member) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (member == null) {
          throw new NullPointerException("Why is member null?");
        }
        
        //no need to check security
        GrouperUserDataType.recentMember.add(userDataGroupName, subjectToAddTo, member.getUuid());
        
        return null;
      }
    });

  }

  /**
   * @param subjectToAddTo
   * @param userDataGroupName
   * @param member
   */
  public static void recentlyUsedMemberRemove(final String userDataGroupName, final Subject subjectToAddTo, final Member member) {
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (member == null) {
          throw new NullPointerException("Why is member null?");
        }
        
        //no need to check security
        
        GrouperUserDataType.recentMember.remove(userDataGroupName, subjectToAddTo, member.getUuid());
        
        return null;
      }
    });

  }

  /**
   * @param subject
   * @param userDataGroupName
   * @return the favorite members for a user
   */
  @SuppressWarnings("unchecked")
  public static Set<Member> recentlyUsedMembers(final String userDataGroupName, final Subject subject) {
    
    return ( Set<Member>)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return memberList(GrouperUserDataType.recentMember, userDataGroupName, subject);
      }
    });

    
  }

  /**
   * @param subjectToRemoveFrom
   * @param userDataGroupName
   * @param subjectThatIsRecentlyUsed
   */
  public static void recentlyUsedMemberRemove(final String userDataGroupName, final Subject subjectToRemoveFrom, 
      final Subject subjectThatIsRecentlyUsed) {
    Member member = (Member)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return MemberFinder.findBySubject(grouperSession, subjectThatIsRecentlyUsed, true);
      }
        });
    recentlyUsedMemberRemove(userDataGroupName, subjectToRemoveFrom, member);
  }

}


