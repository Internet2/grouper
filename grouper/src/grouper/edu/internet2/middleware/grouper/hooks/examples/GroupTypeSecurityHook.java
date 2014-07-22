/**
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
 */
/*
 * @author mchyzer
 * $Id: GroupTypeSecurityHook.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * you can security certain group types which can only be added/removed to/from a group
 * based on if the user doing the work is in a certain group (or wheel), or if the user is only a wheel group member.
 * 
 * normally a user with admin rights on a group can edit the group type associations
 * 
 * Log debug with log4j setting
 * log4j.logger.edu.internet2.middleware.grouper.hooks.examples.GroupTypeSecurityHook = DEBUG
 * </pre>
 */
public class GroupTypeSecurityHook extends GroupTypeTupleHooks {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GroupTypeSecurityHook.class);

  /**
   * only register once
   */
  private static boolean registered = false;

  /** if the hook was registered and being used */
  private static boolean registeredSuccess = false;
  
  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   * @param tryAgainIfNotBefore 
   */
  public static void registerHookIfNecessary(boolean tryAgainIfNotBefore) {
    
    if (registered && !tryAgainIfNotBefore) {
      return;
    }
    
    //if trying again, but already registered, fine
    if (tryAgainIfNotBefore && registeredSuccess) {
      return;
    }

    if (resetCacheSettings()) {
      
      registeredSuccess = true;
      LOG.debug("Registering hooks GroupTypeSecurityHook and AttributeSecurityFromTypeHook since configured in grouper.properties");
      //register this hooks
      GrouperHooksUtils.addHookManual(GrouperHookType.GROUP_TYPE_TUPLE.getPropertyFileKey(), GroupTypeSecurityHook.class);
      GrouperHooksUtils.addHookManual(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), AttributeSecurityFromTypeHook.class);
    }
    
    registered = true;

  }

  /**
   * reset cached settings from config, return true if found some
   * @return true if found some
   */
  public static boolean resetCacheSettings() {
    //these are the settings from the fastConfig
    Map<String, String> typeSettings = GrouperUtil.nonNull(GrouperCheckConfig.typeSecuritySettings());
    
    //clear these, might have removed something
    groupTypeToGroupMap.clear();
    groupTypeWheelOnly.clear();
    
    //register the hook
    if (typeSettings.size() > 0) {
      
      //lets calculate and cache the settings from the config
      for (String key: typeSettings.keySet()) {
        
        Matcher matcher = GrouperCheckConfig.typeSecurityPattern.matcher(key);
        matcher.matches();
        
        String typeName = matcher.group(1);
        String settingType = matcher.group(2);
        if (StringUtils.equalsIgnoreCase("allowOnlyGroup", settingType)) {
          
          String group = typeSettings.get(key);
          groupTypeToGroupMap.put(typeName, group);

          LOG.debug("Registering and caching setting to secure group type '" + typeName 
              + "' to be editable only be group name: '" + group);
           
        } else if  (StringUtils.equalsIgnoreCase("wheelOnly", settingType)) {

          groupTypeWheelOnly.add(typeName);
          
          LOG.debug("Registering and caching setting to secure group type '" + typeName 
              + "' to be editable only by wheel group members");

        } else {
          throw new RuntimeException("Setting type: " + settingType + " not supported!");
        }
        
      }
      return true;
    }
    return false;

  }
  
  /** map of group type name to group name allowed to edit */
  private static Map<String, String> groupTypeToGroupMap = new LinkedHashMap<String, String>();
  
  /** set of group type names to only let wheel edit */
  private static Set<String> groupTypeWheelOnly = new LinkedHashSet<String>();
  
  /**
   * veto a type or attribute edit if necessary
   * @param groupUuid
   * @param typeUuid
   * @param descriptionForLog
   * @throws HookVeto if there is a problem
   */
  public static void vetoIfNecessary(String groupUuid, String typeUuid, String descriptionForLog) throws HookVeto {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Subject subject = null;
    String groupTypeName = null;
    String groupNameUuidEdited = groupUuid;
    GrouperSession currentSession = GrouperSession.staticGrouperSession();

    try {
      subject = grouperSession.getSubject();

      //lets get the type and see if it is protected.  this better exist
      GroupType groupType = GroupTypeFinder.findByUuid(typeUuid, true);
      String groupName = groupTypeToGroupMap.get(groupType.getName());
      groupTypeName = groupType.getName();
      
      boolean hasGroupConstraint = !StringUtils.isBlank(groupName);
      boolean hasWheelConstraint = groupTypeWheelOnly.contains(groupTypeName);
      
      //only need this if debugging
      if (LOG.isDebugEnabled()) {
        try {
          Group groupEdited = GroupFinder.findByUuid(currentSession.internal_getRootSession(), groupUuid, true);
          groupNameUuidEdited = StringUtils.defaultIfEmpty(groupEdited.getName(), groupNameUuidEdited);
          
        } catch (Exception e) {
          //dont worry if cant find this
        }
      }
      
      //if nothing
      if (!hasGroupConstraint && !hasWheelConstraint) {
        if (LOG.isDebugEnabled()) {
          //wasnt configured to check, just return
          LOG.debug("Allowing since cant find rule for groupType: " + groupTypeName + ", " + descriptionForLog 
              + ", on group: " + groupNameUuidEdited + " only have rules for wheel: " 
              + GrouperUtil.setToString(groupTypeWheelOnly) + ", and groups: " + GrouperUtil.mapToString(groupTypeToGroupMap));
        }
        return;
      }

      if (hasGroupConstraint) {
        
        Group groupAllowed = GroupFinder.findByName(currentSession.internal_getRootSession(), groupName, false);
        if (groupAllowed == null) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Cant find group: " + groupName + " which holds security for type: " + groupTypeName);
          }
        } else {
          if (groupAllowed.hasMember(subject)) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Allowed to edit type: " + groupTypeName + " on group: " + groupNameUuidEdited 
                  + " since user: " + GrouperUtil.subjectToString(subject) 
                + " is in group: " + groupName);
            }
            return;
          } 
        }
      } 
      
      //see if root or admin
      if (PrivilegeHelper.isWheel(currentSession) || PrivilegeHelper.isRoot(currentSession)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Allowed to edit type: " + groupTypeName + " on group: " + groupNameUuidEdited 
              + " since user: " + GrouperUtil.subjectToString(subject) 
            + " is in group: " + groupName);
        }
        return;
        
      }
      
      String vetoDescription = "Not allowed to edit type: " + groupTypeName + ", " + descriptionForLog + " since the user "
        + GrouperUtil.subjectToString(subject)  + " is not in group: " + groupName;
      throw new HookVeto("cantEditTypeNotInGroup", vetoDescription);
      
    } catch (Exception e) {
      if (e instanceof HookVeto) {
        HookVeto hookVeto = (HookVeto)e;

        if (LOG.isDebugEnabled()) {
          LOG.debug("Veto for " + descriptionForLog + " on group: " + groupNameUuidEdited  
              + " based on session subject: " + GrouperUtil.subjectToString(subject)  + ": " + hookVeto.getReason());
        }
        throw (HookVeto)e;
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("Error finding if veto for " + descriptionForLog  + " on group: " + groupNameUuidEdited 
            + " for subject: " + GrouperUtil.subjectToString(subject) , e);
      }
      
      //this will pass through the vetos
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    }
  }
  
  /**
   * 
   * @param attributeName
   * @return the group name from the config file
   */
  public static String groupNameFromAndGroupAttributeName(String attributeName) {
    int i=0;
    while (true) {
      String propertyName = "grouperIncludeExclude.requireGroup.name." + i;
      String propertyValue = GrouperConfig.retrieveConfig().propertyValueString(propertyName);
      if (StringUtils.isBlank(propertyValue)) {
        break;
      }
      if (StringUtils.equals(attributeName, propertyValue)) {
        //this is the right index, get the group name
        return GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroup.group." + i);
      }
      i++;
    }
    throw new RuntimeException("Cant find config entry for andGroup attribute name: " + attributeName 
        + ", e.g. config name: grouperIncludeExclude.requireGroup.name.{i}");  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePostDelete(HooksContext hooksContext,
      HooksGroupTypeTupleBean postDeleteBean) {
    groupTypeTupleHelper(postDeleteBean, "removing type");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePostInsert(HooksContext hooksContext,
      HooksGroupTypeTupleBean postInsertBean) {
    
    groupTypeTupleHelper(postInsertBean, "adding type");
  
  }

  /**
   * @param postInsertBean
   * @param reason is to describe what is going on
   */
  private void groupTypeTupleHelper(HooksGroupTypeTupleBean postInsertBean, String reason) {

    GroupTypeTuple groupTypeTuple = postInsertBean.getGroupTypeTuple();
    vetoIfNecessary(groupTypeTuple.getGroupUuid(), groupTypeTuple.getTypeUuid(), reason);
  }

  /**
   * If the hook was registered due to settings in the grouper.properties
   * @return true/false
   */
  public static boolean isRegisteredSuccess() {
    return registeredSuccess;
  }


}
