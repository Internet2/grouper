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
 * $Id: GroupAttributeNameValidationHook.java,v 1.6 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * extensions in groups will be unique in a list of folders (or subfolders)
 * 
 * set that with grouper.properties:
 * 
 * groupUniqueExtensionInFolderHook.someConfigId.folderNames = a:b:c,d:e:f
 * groupUniqueExtensionInFolderHook.someConfigId2.folderNames = a:b:c,d:e:f
 * 
 * # set this to true in grouper.properties to make this case insensitive
 * groupUniqueExtensionInFolderHookCaseInsensitive = true
 * 
 * </pre>
 */
public class GroupUniqueExtensionInFoldersHook extends GroupHooks {
  
  private static Pattern configRegex = Pattern.compile("groupUniqueExtensionInFolderHook\\.([^.]+)\\.folderNames");
  
  /**
   * 
   * @return
   */
  public static boolean hasConfiguredFolders() {
    return configIdToSetOfFolderNames().size()>0;
  }
  
  /**
   * You cannot use this extension since it is in use with another related group (in this folder or another related folder):
   */
  public static final String VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER = "veto.group.unique.extension.in.folder.real";

  /**
   * only register once
   */
  private static boolean registered = false;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    Group group = preInsertBean.getGroup();
    verifyUniqueExtension(group);
  }

  private static ExpirableCache<Boolean, Map<String, Set<String>>> cacheConfigIdToSetOfFolderNames = new ExpirableCache<Boolean, Map<String,Set<String>>>(2);

  /**
   * never null.  key is config id _ true|false (for if case sensitive or not
   * @return
   */
  public static Map<String, Set<String>> configIdToSetOfFolderNames() {
    
    Map<String, Set<String>> result = cacheConfigIdToSetOfFolderNames.get(Boolean.TRUE);
    
    if (result == null) {
      synchronized (GroupUniqueExtensionInFoldersHook.class) {
        result = cacheConfigIdToSetOfFolderNames.get(Boolean.TRUE);
        
        if (result == null) {
          
          result = new HashMap<>();
          Map<String, String> propertiesMap = GrouperConfig.retrieveConfig().propertiesMap(configRegex);
          
          if (GrouperUtil.length(propertiesMap) > 0) {
            
            for (String key : propertiesMap.keySet()) {
              
              //  # comma separated folder names (id path).  Configure multiple with different config ids
              //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.folderNames$"}
              //  #groupUniqueExtensionInFolderHook.someConfigId.folderNames = a:b, b:c
              //
              //  # optional config for case sensitive extensions (default true)
              //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.caseSensitive$"}
              //  #groupUniqueExtensionInFolderHook.someConfigId.caseSensitive = false

              Matcher matcher = configRegex.matcher(key);
              matcher.matches();
              String configId = matcher.group(1);
              boolean caseSensitive = GrouperConfig.retrieveConfig().propertyValueBoolean("groupUniqueExtensionInFolderHook." + configId + ".caseSensitive", true);
              String folderNamesCommaSeparated = propertiesMap.get(key);
              Set<String> folderNamesRaw = GrouperUtil.splitTrimToSet(folderNamesCommaSeparated, ",");
              Set<String> folderNames = new HashSet<String>();
              
              for (String folderNameRaw : folderNamesRaw) {
                if (!folderNameRaw.endsWith(":")) {
                  folderNameRaw = folderNameRaw + ":";
                }
                folderNames.add(folderNameRaw + "%");
              }
              result.put(key + "_" + caseSensitive, folderNames);
            }
            
          }
          cacheConfigIdToSetOfFolderNames.put(Boolean.TRUE, result);
          
        }
        
      }
    }
    return result;

  }
  
  
  /**
   * 
   * @param group
   */
  public static void verifyUniqueExtension(Group group) {
    
    Map<String, Set<String>> configIdToSetOfFolderNames = configIdToSetOfFolderNames();
    
    String groupNameWithProblem = null;
    for (String configIdAndCaseSensitive : configIdToSetOfFolderNames.keySet()) {

      Set<String> folderNames = configIdToSetOfFolderNames.get(configIdAndCaseSensitive);
      
      boolean caseSensitive = configIdAndCaseSensitive.endsWith("_true");
      
      String sqlString = caseSensitive ? "select name from grouper_groups where extension = ? and id != ?" : "select name from grouper_groups where lower(extension) = ? and id != ?";

      String extension = caseSensitive ? group.getExtension() : group.getExtension().toLowerCase();

      GcDbAccess gcDbAccess = new GcDbAccess();
      gcDbAccess.addBindVar(extension).addBindVar(group.getId());
      
      //see if there is another group with the same extension
      StringBuilder sql = new StringBuilder(sqlString);

      if (!folderNames.contains(":%")) {
        sql.append(" and ( ");
        boolean first = true;
        boolean groupIsInFolder = false;
        for (String folderName : folderNames) {
          String folderNameWithoutPercent = folderName.substring(0,  folderName.length()-1);
          if (group.getName().startsWith(folderNameWithoutPercent)) {
            groupIsInFolder = true;
          }
          if (!first) {
            sql.append(" or ");
          }
          sql.append(" name like ? ");
          gcDbAccess.addBindVar(folderName);
          first = false;
        }
        if (!groupIsInFolder) {
          continue;
        }
        sql.append(" ) ");
      }

      gcDbAccess.sql(sql.toString());
      List<String> names = gcDbAccess.selectList(String.class);
      if (GrouperUtil.length(names) > 0) {
        groupNameWithProblem = names.get(0);
      }
    }
    if (StringUtils.isBlank(groupNameWithProblem)) {
      return;
    }
    
    String defaultErrorMessage = GrouperTextContainer.textOrNull("veto.group.unique.extension.in.folder") + groupNameWithProblem;
    
    throw new HookVeto(VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER, defaultErrorMessage);
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    Group group = preUpdateBean.getGroup();
    if (group.dbVersionDifferentFields().contains(Group.FIELD_EXTENSION) || group.dbVersionDifferentFields().contains(Group.FIELD_NAME)) {
      verifyUniqueExtension(group);
    }
  }

  /**
   * 
   */
  public static void clearHook() {
    registered = false;
    cacheConfigIdToSetOfFolderNames.clear();
  }

  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    //register this hook
    GrouperHooksUtils.addHookManual(GrouperHookType.GROUP.getPropertyFileKey(), 
        GroupUniqueExtensionInFoldersHook.class);
    
    registered = true;
  
  }
  
}
