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
/*
 * @author mchyzer
 * $Id: GroupTypeTupleIncludeExcludeHook.java,v 1.9 2009-10-18 16:30:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * you can auto create groups to facilitate include and exclude lists
 * 
 * to debug this add these two entries to log4j.properties
 * 
 * log4j.logger.edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook = DEBUG
 * log4j.logger.edu.internet2.middleware.grouper.Group = DEBUG
 * </pre>
 */
public class GroupTypeTupleIncludeExcludeHook extends GroupTypeTupleHooks {
  
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
    
    //see if there are config entries
    Properties grouperProperties = GrouperConfig.retrieveConfig().properties();

    boolean useIncludeExcludeOverride = StringUtils.equals(GrouperConfig.retrieveConfig().propertiesOverrideMap().get("grouperIncludeExclude.use"), "true");
    
    boolean useGrouperIncludeExclude = useIncludeExcludeOverride || GrouperUtil.propertiesValueBoolean(
        grouperProperties, "grouperIncludeExclude.use", false);

    boolean useRequireGroupsOverride = StringUtils.equals(GrouperConfig.retrieveConfig().propertiesOverrideMap().get("grouperIncludeExclude.requireGroups.use"), "true");
    
    boolean useRequireGroups = useRequireGroupsOverride || GrouperUtil.propertiesValueBoolean(
        grouperProperties, "grouperIncludeExclude.requireGroups.use", false);

    //register the hook
    if (useGrouperIncludeExclude || useRequireGroups) {
      registeredSuccess = true;
      //register this hooks
      GrouperHooksUtils.addHookManual(GrouperHookType.GROUP_TYPE_TUPLE.getPropertyFileKey(), GroupTypeTupleIncludeExcludeHook.class);
      GrouperHooksUtils.addHookManual(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), AttributeIncludeExcludeHook.class);
    }
    
    registered = true;

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
    groupTypeTupleHelper(postDeleteBean, false);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePostInsert(HooksContext hooksContext,
      HooksGroupTypeTupleBean postInsertBean) {
    
    groupTypeTupleHelper(postInsertBean, true);
  
  }

  /**
   * @param postInsertBean
   * @param isInsert
   */
  private void groupTypeTupleHelper(HooksGroupTypeTupleBean postInsertBean, boolean isInsert) {
    boolean useGrouperIncludeExclude = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.use", false);
    boolean useRequireGroups = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    //dont do anything if not using this type
    if (!useGrouperIncludeExclude && !useRequireGroups) {
      return;
    }

    GroupTypeTuple groupTypeTuple = postInsertBean.getGroupTypeTuple();

    String groupUuid = groupTypeTuple.getGroupUuid();

    //make sure this is the right type
    String includeExcludeTypeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.type.name");
    String requireGroupsTypeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.type.name");

    //there better be a session now!
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    try {
      
      GroupType includeExcludeType = useGrouperIncludeExclude ? GroupTypeFinder.find(includeExcludeTypeName, false) : null;
      boolean fireHook = (useGrouperIncludeExclude && isInsert)
        ? StringUtils.equals(groupTypeTuple.getTypeUuid(), 
            includeExcludeType == null ? null : includeExcludeType.getUuid()) : false;

      GroupType requireGroupsType = useRequireGroups ? GroupTypeFinder.find(requireGroupsTypeName, false) : null;
      fireHook = fireHook || (useRequireGroups 
        ? StringUtils.equals(groupTypeTuple.getTypeUuid(), 
            requireGroupsType == null ? null : requireGroupsType.getUuid()) : false);
        
      //see if a custom group type
      if (!fireHook) {
        
        //#grouperIncludeExclude.requireGroup.name.0 = requireActiveEmployee
        //#grouperIncludeExclude.requireGroup.attributeOrType.0 = type
        int i = 0;
        while (true) {
          String name = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroup.name." + i);
          if (StringUtils.isBlank(name)) {
            break;
          }
          String attributeOrType = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroup.attributeOrType." + i);
          if (StringUtils.equalsIgnoreCase("type", attributeOrType)) {
            GroupType type = GroupTypeFinder.find(name, false);
            if (type != null) {
              fireHook = fireHook ||StringUtils.equals(groupTypeTuple.getTypeUuid(), type.getUuid());
              if (fireHook) {
                break;
              }
            }
          }
          i++;
        }
        
      }
      if (!fireHook) {
        return;
      }
        
      //not sure why this would be null, could be a stale state problem if so
      Group typedGroup = postInsertBean.getGroupTypeTuple().retrieveGroup(true);

      manageIncludesExcludesAndGroups(grouperSession, typedGroup, includeExcludeTypeName + " changed on group: " + typedGroup.getName());
      
    } catch (Exception e) {
      throw new RuntimeException("Error doing include/exclude on group: " + groupUuid, e);
    }
  }

  /**
   * convert a system of record extension to an overall extension
   * @param extension is overall extension or system of record extension
   * @return the overall extension
   */
  public static String convertToOverall(String extension) {
    //strip system of record suffix (i.e. we are putting the type on the system of record group)
    if (extension.endsWith(systemOfRecordExtensionSuffix())) {
      extension = extension.substring(0, 
          extension.length() - systemOfRecordExtensionSuffix().length());
    }
    return extension;
    
  }
  
  /**
   * return a set of groups including the one passed in, related to this group.
   * if the groups arent found, dont worry
   * i.e. if include/exclude or requireGroups, find related groups
   * @param group
   * @return the set of groups
   */
  public static Set<Group> relatedGroups(Group group) {
    Set<Group> groups = new LinkedHashSet<Group>();
    
    groups.add(group);
    
    String name = group.getName();
    
    //baseName is name without suffix if applicable
    String baseName = name;
    
    boolean useGrouperIncludeExclude = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.use", false);
    
    if (useGrouperIncludeExclude) {
      
      if (name.endsWith(systemOfRecordExtensionSuffix())) {
        baseName = GrouperUtil.stripSuffix(name, systemOfRecordExtensionSuffix());
      } else if (name.endsWith(includeExtensionSuffix())) {
        baseName = GrouperUtil.stripSuffix(name, includeExtensionSuffix());
      } else if (name.endsWith(includesMinusExcludesExtensionSuffix())) {
        baseName = GrouperUtil.stripSuffix(name, includesMinusExcludesExtensionSuffix());
      } else if (name.endsWith(systemOfRecordAndIncludesExtensionSuffix())) {
        baseName = GrouperUtil.stripSuffix(name, systemOfRecordAndIncludesExtensionSuffix());
      }
    }
    boolean useRequireGroups = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    //strip off something like _requireGroups15
    if (useRequireGroups) {
      Matcher matcher = requireGroupsPattern.matcher(name);
      if (matcher.matches()) {
        baseName = matcher.group(1);
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("name: " + name + ", baseName: " + baseName);
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession().internal_getRootSession();
    
    Group groupFind = null;
    
    //now lets find all the groups
    if (!StringUtils.equals(name, baseName)) {
      groupFind = GroupFinder.findByName(grouperSession, baseName,false);
      if (groupFind != null) {
        groups.add(groupFind);
      }
    }

    if (useGrouperIncludeExclude) {
      
      //system of record
      groupFind = GroupFinder.findByName(grouperSession, baseName + systemOfRecordExtensionSuffix(),false);
      if (groupFind != null) {
        groups.add(groupFind);
      }
    
      //include
      groupFind = GroupFinder.findByName(grouperSession, baseName + includeExtensionSuffix(),false);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Searching for group: " + (baseName + includeExtensionSuffix()) + ", found: " + (groupFind != null));
      }
      
      if (groupFind != null) {
        groups.add(groupFind);
      }
    
      //exclude
      groupFind = GroupFinder.findByName(grouperSession, baseName + excludeExtensionSuffix(),false);
      if (groupFind != null) {
        groups.add(groupFind);
      }

      //include and sor
      groupFind = GroupFinder.findByName(grouperSession, baseName + includesMinusExcludesExtensionSuffix(),false);
      if (groupFind != null) {
        groups.add(groupFind);
      }
      
    }
    
    if (useRequireGroups) {
      
      //any require groups
      for (int i=1;i<200;i++) {
        groupFind = GroupFinder.findByName(grouperSession, baseName + requireGroupsExtensionSuffix(1),false);
        if (groupFind == null) {
          break;
        }
        groups.add(groupFind);
      }
    }
    
    return groups;
  }

  /**
   * regex pattern for require groups
   */
  private static final Pattern requireGroupsPattern = Pattern.compile("(.*)" 
      + StringUtils.replace(GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.extension.suffix"), "${i}", "") 
      + "\\d+");

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
  }
  
  /**
   * @param grouperSession
   * @param typedGroup
   * @param summaryForLog some string that will be logged to debug...
   */
  public static void manageIncludesExcludesAndGroups(GrouperSession grouperSession, Group typedGroup, String summaryForLog) {
    
    //make sure this is the right type
    String includeExcludeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.type.name");
    String groupTypeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.type.name");

    //make these sorted so that it is more easily testable
    Set<Group> andGroups = new TreeSet<Group>();
    
    boolean includeExclude = false;
    GroupType includesExcludesType = null;
    GroupType requireGroupsType = null;
    try {
      
      includesExcludesType = GroupTypeFinder.find(includeExcludeName, true);
      
      includeExclude = typedGroup.hasType(includesExcludesType, false);
      
      //if other groups are there, then this is include/exclude.  we dont remove when the checkbox is
      //unchecked
      String overallExtension = typedGroup.getExtension();
      
      overallExtension = convertToOverall(overallExtension);

      String stemName = GrouperUtil.parentStemNameFromName(typedGroup.getName());
      String overallName = stemName + ":" + overallExtension;

      includeExclude = includeExclude || GroupFinder.findByName(grouperSession, 
          overallName + includeExtensionSuffix(), false) != null;
      includeExclude = includeExclude || GroupFinder.findByName(grouperSession, 
          overallName + excludeExtensionSuffix(), false) != null;
      
      requireGroupsType = GroupTypeFinder.find(groupTypeName, true);
  
      boolean hasRequireGroupsType = typedGroup.hasType(requireGroupsType, false);
      
      String andGroupsAttributeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.attributeName");
      boolean hasAndGroupsAttributeName = StringUtils.isNotBlank(andGroupsAttributeName);
      
      if (hasRequireGroupsType && hasAndGroupsAttributeName) {
        String groupNames = typedGroup.getAttributeValue(andGroupsAttributeName, false, false);
        if (!StringUtils.isBlank(groupNames)) {
          String[] andGroupNames = GrouperUtil.splitTrim(groupNames, ",");
          for (String andGroupName: andGroupNames) {
            andGroups.add(GroupFinder.findByName(grouperSession, andGroupName, true));
          }
        }
      }
      
      if (hasRequireGroupsType) {
        Set<AttributeDefName> attrs = GrouperDAOFactory.getFactory().getAttributeDefName().findByAttributeDef(requireGroupsType.internal_getAttributeDefForAttributes().getId());
        
        for (AttributeDefName attr : GrouperUtil.nonNull(attrs)) {
          
          //see if this is not a custom require group
          if (hasAndGroupsAttributeName && StringUtils.equals(andGroupsAttributeName, attr.getLegacyAttributeName(true))) {
            continue;
          }
          
          String valueString = typedGroup.getAttributeValue(attr.getLegacyAttributeName(true), false, false);
    
          boolean valueBoolean = GrouperUtil.booleanValue(valueString, false);
          
          if (valueBoolean) {
            String groupName = groupNameFromAndGroupAttributeName(attr.getLegacyAttributeName(true));
            andGroups.add(GroupFinder.findByName(grouperSession, groupName, true));
          }
          
        }
      }
      //now try custom types
      //#grouperIncludeExclude.requireGroup.name.0 = requireActiveEmployee
      //#grouperIncludeExclude.requireGroup.attributeOrType.0 = type
      //#grouperIncludeExclude.requireGroup.group.0 = school:community:activeEmployee
      int i=0;
      while (true) {
        
        String typeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroup.name." + i);
        if (StringUtils.isBlank(typeName)) {
          break;
        }
        String attributeOrType = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroup.attributeOrType." + i);
        //just check types
        if (StringUtils.equalsIgnoreCase("type", attributeOrType)) {
          String groupName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroup.group." + i);
          
          GroupType type = GroupTypeFinder.find(typeName, true);
          //if the hooked group has this type, then good to go
          if (typedGroup.getTypesDb().contains(type)) {
            Group group = GroupFinder.findByName(grouperSession, groupName, true);
            andGroups.add(group);
          }
        }
        i++;
      }
      
    } catch (SchemaException se) {
      throw new RuntimeException(se);
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    manageIncludesExcludesAndGroups(typedGroup, includeExclude, andGroups, summaryForLog);
  }

  /**
   * substitute and return systemOfRecordAndIncludes exclude description
   * @param overallGroupExtension 
   * @param overallGroupDisplayExtension 
   * @return description
   */
  public static String excludeDescription(String overallGroupExtension, String overallGroupDisplayExtension) {
    String description = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.exclude.description");
    description = StringUtils.replace(description, "${extension}", overallGroupExtension);
    description = StringUtils.replace(description, "${displayExtension}", overallGroupDisplayExtension);
    return description;
  }

  /**
   * substitute and return exclude name suffix
   * @return suffix
   */
  public static String excludeDisplayExtensionSuffix() {
    String nameSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.exclude.displayExtension.suffix");
    return StringUtils.replace(nameSuffix, "${space}", " ");
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GroupTypeTupleIncludeExcludeHook.class);

  /**
   * substitute and return exclude id suffix
   * @return suffix
   */
  public static String excludeExtensionSuffix() {
    String idSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.exclude.extension.suffix");
    return StringUtils.replace(idSuffix, "${space}", " ");
  }

  /**
   * change a typed group into include and exclude group lists and andGroups also
   * @param typedGroup 
   * @param isIncludeExclude 
   * @param andGroups 
   * @param calledFromForLog summary of where this is coming from for debug log
   */
  public static void manageIncludesExcludesAndGroups(Group typedGroup, boolean isIncludeExclude, 
      Set<Group> andGroups, String calledFromForLog) {

    String groupUuid = null;
    
    if (LOG.isDebugEnabled()) {
      StringBuilder summary = new StringBuilder();
      summary.append("manageIncludeExclude called for group: " + typedGroup.getName() 
          + ", includeExclude? " + isIncludeExclude + " and " + GrouperUtil.length(andGroups) + " andGroups: ");
      Iterator<Group> iterator = GrouperUtil.nonNull(andGroups).iterator();
      boolean first = true;
      while (iterator.hasNext()) {
        if (!first) {
          summary.append(", ");
        }
        first = false;
        summary.append(iterator.next().getExtension());
      }
      summary.append(".  ").append(calledFromForLog);
      LOG.debug(summary.toString());
     }
    
    //there better be a session now!
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    try {
      
      groupUuid = typedGroup.getUuid();
      
      String overallExtension = typedGroup.getExtension();
      
      String overallDisplayExtension = typedGroup.getDisplayExtension();

      //strip system of record suffix (i.e. we are putting the type on the system of record group)
      if (overallExtension.endsWith(systemOfRecordExtensionSuffix())) {
        overallExtension = overallExtension.substring(0, 
            overallExtension.length() - systemOfRecordExtensionSuffix().length());

        //strip system of record suffixes
        if (overallDisplayExtension.endsWith(systemOfRecordExtensionSuffix())) {
          overallDisplayExtension = overallDisplayExtension.substring(0, 
              overallDisplayExtension.length() - systemOfRecordExtensionSuffix().length());
        }
        if (overallDisplayExtension.endsWith(systemOfRecordDisplayExtensionSuffix())) {
          overallDisplayExtension = overallDisplayExtension.substring(0, 
              overallDisplayExtension.length() - systemOfRecordDisplayExtensionSuffix().length());
        }
        
      }
      
      String stemName = GrouperUtil.parentStemNameFromName(typedGroup.getName());
      String overallName = stemName + ":" + overallExtension;

      //create the overall group if not exist already
      Group overallGroup = null;
      
      if (StringUtils.equals(typedGroup.getName(), overallName)) {
        overallGroup = typedGroup;
      } else {
        overallGroup = GroupFinder.findByName(grouperSession, overallName, false);
      }
      
      String overallDescription = overallDescription(overallExtension, overallDisplayExtension);
      if (overallGroup == null) {
        LOG.debug("Adding overall group: " + overallName);
        overallGroup = Group.saveGroup(grouperSession, null, null, overallName, overallDisplayExtension, 
            overallDescription, null, false);
      } else if (StringUtils.isBlank(overallGroup.getDescription())) {
        overallGroup.setDescription(overallDescription);
        overallGroup.store();
      }

      //create system of record if not exist
      String systemOfRecordName = overallName + systemOfRecordExtensionSuffix();

      Group systemOfRecordGroup = GroupFinder.findByName(grouperSession, systemOfRecordName, false);
      if (systemOfRecordGroup == null ) {
        
        String systemOfRecordDescription = systemOfRecordDescription(overallExtension, overallDisplayExtension);
        String systemOfRecordDisplayExtension = overallDisplayExtension + systemOfRecordDisplayExtensionSuffix();
        LOG.debug("Adding system of record group: " + systemOfRecordName);
        systemOfRecordGroup = Group.saveGroup(grouperSession, null, null, systemOfRecordName, systemOfRecordDisplayExtension, 
            systemOfRecordDescription, null, true);
        
      }
      
      //create includes group if not exist
      String includesName = overallName + includeExtensionSuffix();

      Group includesGroup = GroupFinder.findByName(grouperSession, includesName, false);
      if (isIncludeExclude && includesGroup == null ) {
        
        String includesDescription = includeDescription(overallExtension, overallDisplayExtension);
        String includesDisplayExtension = overallDisplayExtension + includeDisplayExtensionSuffix();
        LOG.debug("Adding includes group: " + includesName);
        includesGroup = Group.saveGroup(grouperSession, null, null, includesName, includesDisplayExtension, 
            includesDescription, null, true);
        
      }
      
      //create excludes group if not exist
      String excludesName = overallName + excludeExtensionSuffix();

      Group excludesGroup = GroupFinder.findByName(grouperSession, excludesName, false);
      if (isIncludeExclude && excludesGroup == null ) {
        
        String excludesDescription = excludeDescription(overallExtension, overallDisplayExtension);
        String excludesDisplayExtension = overallDisplayExtension + excludeDisplayExtensionSuffix();
        LOG.debug("Adding excludes group: " + excludesName);
        excludesGroup = Group.saveGroup(grouperSession, null, null, excludesName, excludesDisplayExtension, 
            excludesDescription, null, true);
        
      }
      
      //create excludes group if not exist
      String systemOfRecordAndIncludesName = overallName + systemOfRecordAndIncludesExtensionSuffix();

      Group systemOfRecordAndIncludesGroup = GroupFinder.findByName(grouperSession, systemOfRecordAndIncludesName, false);
      if (isIncludeExclude && systemOfRecordAndIncludesGroup == null ) {
        
        String systemOfRecordAndIncludesDescription = systemOfRecordAndIncludesDescription(overallExtension, overallDisplayExtension);
        String systemOfRecordAndIncludesDisplayExtension = overallDisplayExtension + systemOfRecordAndIncludesDisplayExtensionSuffix();
        LOG.debug("Adding system of record group: " + systemOfRecordAndIncludesName);
        systemOfRecordAndIncludesGroup = Group.saveGroup(grouperSession, null, null, systemOfRecordAndIncludesName, systemOfRecordAndIncludesDisplayExtension, 
            systemOfRecordAndIncludesDescription, null, true);
        
      }

      //dont need to do this if not include/exclude
      if (isIncludeExclude) {
        //lets make sure systemOfRecord and includes are members
        systemOfRecordAndIncludesGroup.addMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true), false);
        systemOfRecordAndIncludesGroup.addMember(SubjectFinder.findById(includesGroup.getUuid(), true), false);
      }
      
      //at this point, the main group must have no members
      if (overallGroup.getComposite(false) == null) {
        
        //lets get all the existing members and add to the system of record group
        //lets do this if the overall group has members, and if the system of record group
        //doesnt have any members yet
        Set<Member> existingOverallMembers = overallGroup.getImmediateMembers();
        
        if (existingOverallMembers.size() > 0) {
          
          //note: lists arent covered here...
          for (Member member : existingOverallMembers) {
            LOG.debug("Removing member from overall, and adding to system of record: " 
                + member.getSubjectSourceIdDb() + " - " + member.getSubjectIdDb());
            //dont add system of record to itself
            if (!StringUtils.equals(member.getSubjectIdDb(), systemOfRecordGroup.getUuid())) {
              systemOfRecordGroup.addMember(member.getSubject(), false);
            }
            overallGroup.deleteMember(member, false);
          }
        }

      }
      int andGroupsLength = GrouperUtil.length(andGroups);

      //################################################
      //lets remove unneeded groups
      int i=andGroupsLength;
      int highestIndexToDelete = -1;
      while(true) {
        
        String requireGroupsSuffixExtension = requireGroupsExtensionSuffix(i);
        String requireGroupsName = overallName + requireGroupsSuffixExtension;
        Group requireGroupsSuffix = GroupFinder.findByName(grouperSession, requireGroupsName, false);
        if (requireGroupsSuffix != null) {
          highestIndexToDelete = i;
        }
        if (requireGroupsSuffix == null && i > 10) {
          break;
        }
        i++;
      }

      String includesMinusExcludesName = overallName + includesMinusExcludesExtensionSuffix();
      Group includesMinusExcludesGroup = GroupFinder.findByName(grouperSession, includesMinusExcludesName, false);

      //#############################################
      //make final structure
      if (andGroupsLength == 0) {
            
        if (isIncludeExclude) {
          //now the overall group needs to be a composite complement
          overallGroup.assignCompositeMember(CompositeType.COMPLEMENT, systemOfRecordAndIncludesGroup, excludesGroup);
        } else {
          //make sure not a composite group from before
          if (overallGroup.hasComposite()) {
            overallGroup.deleteCompositeMember();
          }
          overallGroup.addMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true), false);
          LOG.debug("Adding system of record group (" + systemOfRecordName + ") to the overall group: "  
              + overallGroup.getName());

        }
        
      } else {

        //######################################
        //includesMinusExcludes is composite complement: systemOfRecordAndIncludesGroup minus excludesGroup
        includesMinusExcludesGroup = GroupFinder.findByName(grouperSession, includesMinusExcludesName, false);
        //if no groups we dont need this one
        if (isIncludeExclude && includesMinusExcludesGroup == null ) {

          String includesMinusExcludesGroupDescription = systemOfRecordAndIncludesDescription(overallExtension, overallDisplayExtension);
          String includesMinusExcludesGroupDisplayExtension = overallDisplayExtension + includesMinusExcludesDisplayExtensionSuffix();
          includesMinusExcludesGroup = Group.saveGroup(grouperSession, null, null, includesMinusExcludesName, 
              includesMinusExcludesGroupDisplayExtension, 
              includesMinusExcludesGroupDescription, null, true);
        }
        if (isIncludeExclude) {
          includesMinusExcludesGroup.assignCompositeMember(CompositeType.COMPLEMENT, systemOfRecordAndIncludesGroup, excludesGroup);
        }

        //for one andGroup, structure will be
        //includesMinusExcludes is composite complement: systemOfRecordAndIncludesGroup minus excludesGroup
        //overallGroup is composite complement: includesMinusExcludes minus aStem:activeEmployee
        Group[] andGroupsArray = GrouperUtil.toArray(andGroups, Group.class);
        if (andGroups.size() == 1) {
          if (isIncludeExclude) {
            overallGroup.assignCompositeMember(CompositeType.INTERSECTION, includesMinusExcludesGroup, andGroupsArray[0]);
          } else {
            overallGroup.assignCompositeMember(CompositeType.INTERSECTION, systemOfRecordGroup, andGroupsArray[0]);
          }
        } else {
        
          Group previousGroup = null;
          
          //for more than one andGroup, structure will be
          //includeExcludeMinusAnd2 is composite complement: includesMinusExcludes minus aStem:activeEmployee
          //includeExcludeMinusAnd1 is composite complement: includeExcludeMinusAnd2 minus aStem:anotherGroup
          //overallGroup is composite complement: includeExcludeMinusAnd1 minus aStem:yetAnotherGroup
          int arrayIndex = 0;
          for (i=andGroupsLength-1;i>=1;i--) {
            String requireGroupsName = overallName + requireGroupsExtensionSuffix(i);
            Group requireGroup = GroupFinder.findByName(grouperSession, requireGroupsName, false);
            //if no groups we dont need this one
            if (requireGroup == null ) {
  
              String requireGroupsDescription = requireGroupsDescription(i, overallExtension, overallDisplayExtension);
              String requireGroupsDisplayExtension = overallDisplayExtension + requireGroupsDisplayExtensionSuffix(i);
              requireGroup = Group.saveGroup(grouperSession, null, null, requireGroupsName, requireGroupsDisplayExtension, 
                  requireGroupsDescription, null, true);
              LOG.debug("Adding requireGroups group: " + requireGroup.getExtension());
            }
            Group leftGroup = i == andGroupsLength-1 
              ? (isIncludeExclude ? includesMinusExcludesGroup : systemOfRecordGroup) : previousGroup;
            requireGroup.assignCompositeMember(CompositeType.INTERSECTION, leftGroup, andGroupsArray[arrayIndex]);
            previousGroup = requireGroup;
            arrayIndex++;
          }

          //cant assign if has one
          if (overallGroup.hasComposite()) {
            overallGroup.deleteCompositeMember();
          }
          
          overallGroup.assignCompositeMember(CompositeType.INTERSECTION, previousGroup, andGroupsArray[andGroupsLength-1]);
            
        }
        
      }
      
      for (i=andGroupsLength;i<=highestIndexToDelete;i++) {
        String requireGroupsSuffixExtension = requireGroupsExtensionSuffix(i);
        String requireGroupsName = overallName + requireGroupsSuffixExtension;
        Group includeExcludeMinusAndGroup = GroupFinder.findByName(grouperSession, requireGroupsName, false);
        if (includeExcludeMinusAndGroup != null) {
          LOG.debug("Deleting unneeded group: " + includeExcludeMinusAndGroup.getName());
          includeExcludeMinusAndGroup.delete();
        }
      }

      //if no groups we dont need this one
      if (andGroupsLength == 0 || !isIncludeExclude) {
        if (includesMinusExcludesGroup != null ) {
          LOG.debug("Deleting unneeded group: " + includesMinusExcludesGroup.getName());
          includesMinusExcludesGroup.delete();
        }
      }
      if (!isIncludeExclude && systemOfRecordAndIncludesGroup != null ) {
          LOG.debug("Deleting unneeded group: " + systemOfRecordAndIncludesGroup.getName());
          systemOfRecordAndIncludesGroup.delete();
      }

    } catch (Exception e) {
      throw new RuntimeException("Error doing include/exclude on group: " + groupUuid, e);
    }
  }
  
  /**
   * substitute and return system of record id suffix
   * @return suffix
   */
  public static String systemOfRecordExtensionSuffix() {
    String idSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.extension.suffix");
    return StringUtils.replace(idSuffix, "${space}", " ");
  }

  /**
   * see if a group name has an include/exclude or requireGroup suffix
   * @param groupName
   * @return true if include/exclude
   */
  public static boolean nameIsIncludeExcludeRequireGroup(String groupName) {
    boolean useGrouperIncludeExclude = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.use", false);

    //check include exclude
    if (useGrouperIncludeExclude) {
      
      if (groupName.endsWith(excludeExtensionSuffix())
          || groupName.endsWith(includeExtensionSuffix())
          || groupName.endsWith(includesMinusExcludesExtensionSuffix())
          || groupName.endsWith(systemOfRecordAndIncludesExtensionSuffix())
          || groupName.endsWith(systemOfRecordExtensionSuffix())) {
        return true;
      }
      
    }
    //check 10 require groups
    boolean useRequireGroups = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    if (useRequireGroups) {
      for (int i=1;i<=10;i++) {
        if (groupName.endsWith(requireGroupsExtensionSuffix(i))) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * substitute and return include id suffix
   * @return suffix
   */
  public static String includeExtensionSuffix() {
    String idSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.include.extension.suffix");
    return StringUtils.replace(idSuffix, "${space}", " ");
  }
  
  /**
   * substitute and return includesMinusExcludes id suffix
   * @return suffix
   */
  public static String includesMinusExcludesExtensionSuffix() {
    String idSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.includesMinusExcludes.extension.suffix");
    return StringUtils.replace(idSuffix, "${space}", " ");
  }

  /**
   * substitute and return systemOfRecordAndIncludes id suffix
   * @return suffix
   */
  public static String systemOfRecordAndIncludesExtensionSuffix() {
    String idSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecordAndIncludes.extension.suffix");
    return StringUtils.replace(idSuffix, "${space}", " ");
  }
  
  /**
   * substitute and return overall systemOfRecord suffix
   * @return suffix
   */
  public static String systemOfRecordDisplayExtensionSuffix() {
    String nameSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.displayExtension.suffix");
    return StringUtils.replace(nameSuffix, "${space}", " ");
  }

  /**
   * substitute and return include name suffix
   * @return suffix
   */
  public static String includeDisplayExtensionSuffix() {
    String nameSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.include.displayExtension.suffix");
    return StringUtils.replace(nameSuffix, "${space}", " ");
  }

  /**
   * substitute and return includesMinusExcludes name suffix
   * @return suffix
   */
  public static String includesMinusExcludesDisplayExtensionSuffix() {
    String nameSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.includesMinusExcludes.displayExtension.suffix");
    return StringUtils.replace(nameSuffix, "${space}", " ");
  }

  /**
   * extension of group for includeExcludeMinusAndGroup, index is 1 based
   * @param index
   * @return the extension
   */
  public static String requireGroupsExtensionSuffix(int index) {
    String nameSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.extension.suffix");
    if (!nameSuffix.contains("${i}")) {
      throw new RuntimeException("grouperIncludeExclude.requireGroups.extension.suffix in grouper.properties must contain ${i}");
    }
    nameSuffix = nameSuffix.replace("${i}", Integer.toString(index));
    nameSuffix = StringUtils.replace(nameSuffix, "${space}", " ");
    return nameSuffix;
  }
  
  /**
   * 
   * @param grouperSession 
   * @param groupName if the overall or system of record group
   * @param reason if you want a reason passed back
   * @param saveIncludesExcludesIfMembers true if not delete only the includes group and excludes group
   * if they have members.  false, delete anyway
   * @return the number of groups removed
   */
  public static int deleteGroupsIfNotUsed(GrouperSession grouperSession, 
      String groupName, StringBuilder reason, boolean saveIncludesExcludesIfMembers) {
    if (reason == null) {
      reason = new StringBuilder();
    }
    int count = 0;
    try {
      String overallName = convertToOverall(groupName);
      Group group = GroupFinder.findByName(grouperSession, overallName, false);
      if (group != null) {
        
        Member member = MemberFinder.internal_findBySubject(SubjectFinder.findByIdAndSource(group.getUuid(),
            SubjectFinder.internal_getGSA().getId(), true), null, false);
        
        if (member != null) {
          //get any membership
          Set<Membership> memberships = GrouperDAOFactory.getFactory()
            .getMembership().findAllImmediateByMember(member.getUuid(), false);
          if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
              "loader.sqlTable.likeString.removeGroupIfMemberOfAnotherGroup", false) && memberships.size() > 0) {
            String message = "Not deleting group: " + overallName + " since used in " 
              + memberships.size() + " immediate memberships";
            LOG.debug(message);
            reason.append(message);
            return 0;
          }
          Set<Composite> composites = CompositeFinder.findAsFactor(group);
          if (composites.size() > 0) {
            String message = "Not deleting group: " + overallName + " since used in " 
              + composites.size() + " composites";
            LOG.debug(message);
            reason.append(message);
            return 0;
          }
        } 
        
      }
      
      //we can delete the overall group
      group.delete();
      count++;
      
      //now lets pick apart the composites and such
      for (int i=1;i<10;i++) {
        String requireGroupsName = overallName + requireGroupsExtensionSuffix(i);
        group = GroupFinder.findByName(grouperSession, requireGroupsName, false);
        if (group != null) {
          String logString = "deleting group: " + requireGroupsName;
          GrouperUtil.append(reason, ", ", logString);
          LOG.debug(logString);
          group.delete();
          count++;
        }
      }
      
      {
        //now if the includesMinusExcludes
        String includesMinuxExcludesName = overallName + includesMinusExcludesExtensionSuffix();
        group = GroupFinder.findByName(grouperSession, 
            includesMinuxExcludesName, false);
  
        if (group != null) {
          String logString = "deleting group: " + includesMinuxExcludesName;
          GrouperUtil.append(reason, ", ", logString);
          LOG.debug(logString);
          group.delete();
          count++;
        }
      }
      
      {
        //now if the includesMinusExcludes
        String sorAndIncludesName = overallName + systemOfRecordAndIncludesExtensionSuffix();
        group = GroupFinder.findByName(grouperSession, 
            sorAndIncludesName, false);
  
        if (group != null) {
          String logString = "deleting group: " + sorAndIncludesName;
          GrouperUtil.append(reason, ", ", logString);
          LOG.debug(logString);
          group.delete();
          count++;
        }
      }
      
      {
        //now excludes if no members
        String excludesName = overallName + excludeExtensionSuffix();
        group = GroupFinder.findByName(grouperSession, 
            excludesName, false);
  
        if (group != null) {
          Set<Member> members = group.getMembers();
          if (members.size() > 0 && saveIncludesExcludesIfMembers) {
            String logString = "not deleting group: " + excludesName + " since has " + members.size() + " members";
            GrouperUtil.append(reason, ", ", logString);
            LOG.debug(logString);
          } else {
            String logString = "deleting group: " + excludesName;
            GrouperUtil.append(reason, ", ", logString);
            LOG.debug(logString);
            group.delete();
            count++;
          }
        }
      }
      
      {
        //now includes if no members
        String includesName = overallName + includeExtensionSuffix();
        group = GroupFinder.findByName(grouperSession, 
            includesName, false);
  
        if (group != null) {
          Set<Member> members = group.getMembers();
          if (members.size() > 0 && saveIncludesExcludesIfMembers) {
            String logString = "not deleting group: " + includesName + " since has " + members.size() + " members";
            GrouperUtil.append(reason, ", ", logString);
            LOG.debug(logString);
          } else {
            String logString = "deleting group: " + includesName;
            GrouperUtil.append(reason, ", ", logString);
            LOG.debug(logString);
            group.delete();
            count++;
          }
        }
      }
      
      {
        //finally system of record
        String systemOfRecordName = overallName + systemOfRecordExtensionSuffix();
        group = GroupFinder.findByName(grouperSession, 
            systemOfRecordName, false);
  
        if (group != null) {
          String logString = "deleting group: " + systemOfRecordName;
          GrouperUtil.append(reason, ", ", logString);
          LOG.debug(logString);
          group.delete();
          count++;
        }
      }      
      GrouperUtil.append(reason, ", ", "deleted " + count + " groups");
      
      return count;
    } catch (Exception e) {
      throw new RuntimeException("Problem deleting groups for name: " + groupName 
          + ", though did delete " + count + " groups", e);
    }
  }
  
  /**
   * description of group for includeExcludeMinusAndGroup, index is 1 based
   * @param index
   * @param overallGroupExtension 
   * @param overallGroupDisplayExtension 
   * @return the extension
   */
  public static String requireGroupsDescription(int index, String overallGroupExtension, String overallGroupDisplayExtension) {
    String description = StringUtils.defaultString(GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.description"));
    description = description.replace("${i}", Integer.toString(index));
    description = StringUtils.replace(description, "${extension}", overallGroupExtension);
    description = StringUtils.replace(description, "${displayExtension}", overallGroupDisplayExtension);
    return description;
  }
  
  /**
   * display extension of group for includeExcludeMinusAndGroup, index is 1 based
   * @param index
   * @return the extension
   */
  public static String requireGroupsDisplayExtensionSuffix(int index) {
    String nameSuffix = StringUtils.defaultString(GrouperConfig.retrieveConfig().propertyValueString(
        "grouperIncludeExclude.requireGroups.displayExtension.suffix"));
    nameSuffix = nameSuffix.replace("${i}", Integer.toString(index));
    nameSuffix = StringUtils.replace(nameSuffix, "${space}", " ");
    return nameSuffix;
  }
  
  /**
   * substitute and return systemOfRecordAndIncludes name suffix
   * @return suffix
   */
  public static String systemOfRecordAndIncludesDisplayExtensionSuffix() {
    String nameSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecordAndIncludes.displayExtension.suffix");
    return StringUtils.replace(nameSuffix, "${space}", " ");
  }

  /**
   * substitute and return systemOfRecordAndIncludes overall description
   * @param overallGroupExtension 
   * @param overallGroupDisplayExtension 
   * @return description
   */
  public static String overallDescription(String overallGroupExtension, String overallGroupDisplayExtension) {
    String description = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.overall.description");
    description = StringUtils.replace(description, "${extension}", overallGroupExtension);
    description = StringUtils.replace(description, "${displayExtension}", overallGroupDisplayExtension);
    return description;
  }

  /**
   * substitute and return systemOfRecordAndIncludes systemOfRecordAndIncludes description
   * @param overallGroupExtension 
   * @param overallGroupDisplayExtension 
   * @return description
   */
  public static String systemOfRecordAndIncludesDescription(String overallGroupExtension, String overallGroupDisplayExtension) {
    String description = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecordAndIncludes.description");
    description = StringUtils.replace(description, "${extension}", overallGroupExtension);
    description = StringUtils.replace(description, "${displayExtension}", overallGroupDisplayExtension);
    return description;
  }

  /**
   * substitute and return systemOfRecordAndIncludes systemOfRecord description
   * @param overallGroupExtension 
   * @param overallGroupDisplayExtension 
   * @return description
   */
  public static String systemOfRecordDescription(String overallGroupExtension, String overallGroupDisplayExtension) {
    String description = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.description");
    description = StringUtils.replace(description, "${extension}", overallGroupExtension);
    description = StringUtils.replace(description, "${displayExtension}", overallGroupDisplayExtension);
    return description;
  }

  /**
   * substitute and return systemOfRecordAndIncludes include description
   * @param overallGroupExtension 
   * @param overallGroupDisplayExtension 
   * @return description
   */
  public static String includeDescription(String overallGroupExtension, String overallGroupDisplayExtension) {
    String description = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.include.description");
    description = StringUtils.replace(description, "${extension}", overallGroupExtension);
    description = StringUtils.replace(description, "${displayExtension}", overallGroupDisplayExtension);
    return description;
  }
  
  /**
   * substitute and return includesMinusExcludes description
   * @param overallGroupExtension 
   * @param overallGroupDisplayExtension 
   * @return description
   */
  public static String includesMinusExcludesDescription(String overallGroupExtension, String overallGroupDisplayExtension) {
    String description = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.includesMinusExcludes.description");
    description = StringUtils.replace(description, "${extension}", overallGroupExtension);
    description = StringUtils.replace(description, "${displayExtension}", overallGroupDisplayExtension);
    return description;
  }

}
