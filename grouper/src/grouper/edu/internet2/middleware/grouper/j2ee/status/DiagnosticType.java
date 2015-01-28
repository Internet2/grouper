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
package edu.internet2.middleware.grouper.j2ee.status;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * type of diagnostics to run (trivial, deep, etc)
 * 
 * @author mchyzer
 */
public enum DiagnosticType {

  /**
   * just do a trivial memory only test
   */
  trivial {

    /**
     * @see DiagnosticType#appendDiagnostics(List)
     */
    @Override
    public void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks) {
      diagnosticsTasks.add(new DiagnosticMemoryTest());
    }
  },
  
  /**
   * just do the trivial plus the database check
   */
  db {

    /**
     * @see DiagnosticType#appendDiagnostics(List)
     */
    @Override
    public void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks) {
      trivial.appendDiagnostics(diagnosticsTasks);
      diagnosticsTasks.add(new DiagnosticDbTest());
    }
  },
  
  /**
   * do the DB test plus check the sources
   */
  sources {

    /**
     * @see DiagnosticType#appendDiagnostics(List)
     */
    @Override
    public void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks) {
      db.appendDiagnostics(diagnosticsTasks);

      Collection<Source> sources = SourceManager.getInstance().getSources();
      
      for (Source source : sources) {
        
        diagnosticsTasks.add(new DiagnosticSourceTest(source.getId()));
        
      }
      
    }
  },
  
  /**
   * do the sources test plus the jobs
   */
  all {

    /**
     * @see DiagnosticType#appendDiagnostics(List)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks) {
      sources.appendDiagnostics(diagnosticsTasks);
      
      diagnosticsTasks.add(new DiagnosticLoaderJobTest("CHANGE_LOG_changeLogTempToChangeLog", GrouperLoaderType.CHANGE_LOG));

      String emailTo = GrouperLoaderConfig.getPropertyString("daily.report.emailTo");
      String reportDirectory = GrouperLoaderConfig.getPropertyString("daily.report.saveInDirectory");
      
      if (!StringUtils.isBlank(emailTo) ||  !StringUtils.isBlank(reportDirectory)) {
        diagnosticsTasks.add(new DiagnosticLoaderJobTest("MAINTENANCE__grouperReport", GrouperLoaderType.MAINTENANCE));
      }
      diagnosticsTasks.add(new DiagnosticLoaderJobTest("MAINTENANCE_cleanLogs", GrouperLoaderType.MAINTENANCE));

      {
        //expand these out
        Map<String, String> consumerMap = GrouperLoaderConfig.retrieveConfig().propertiesMap( 
            GrouperCheckConfig.grouperLoaderConsumerPattern);
        
        for (String consumerKey : consumerMap.keySet()) {

          //get the name
          Matcher matcher = GrouperCheckConfig.grouperLoaderConsumerPattern.matcher(consumerKey);
          matcher.matches();
          String consumerName = matcher.group(1);

          //diagnosticsTasks.add(new DiagnosticLoaderJobTest("CHANGE_LOG_consumer_xmpp", GrouperLoaderType.CHANGE_LOG));
          diagnosticsTasks.add(new DiagnosticLoaderJobTest("CHANGE_LOG_consumer_" + consumerName, GrouperLoaderType.CHANGE_LOG));

        }
      }

      {
        GroupType groupType = GroupTypeFinder.find("grouperLoader", false);
        if (groupType != null) {
          Set<Group> groupSet = sourceCache.get(GrouperLoaderType.SQL_SIMPLE);
          
          if (groupSet == null) {

            groupSet = GroupFinder.findAllByType(GrouperSession.staticGrouperSession(), groupType);

            sourceCache.put(GrouperLoaderType.SQL_SIMPLE, groupSet);
            
          }
          
          for (Group group : groupSet) {
            
            String grouperLoaderType = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_TYPE, false, false);
            
            GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
    
            String jobName = grouperLoaderTypeEnum.name() + "__" + group.getName() + "__" + group.getUuid();
            
            //diagnosticsTasks.add(new DiagnosticLoaderJobTest("SQL_SIMPLE__penn:sas:query:faculty:adjunct_faculty__707d94fa3aaa4ef88051a144b74bac77", GrouperLoaderType.SQL_SIMPLE));
            //diagnosticsTasks.add(new DiagnosticLoaderJobTest("SQL_GROUP_LIST__penn:community:student:loader:studentPrimaryGroups__93750690c3474b41b349bbd196167d3e", GrouperLoaderType.SQL_GROUP_LIST));
            
            diagnosticsTasks.add(new DiagnosticLoaderJobTest(jobName, grouperLoaderTypeEnum));
            
          }
          
        }
        
      }
      
      {
        String attrRootStem = GrouperConfig.getProperty("grouper.attribute.rootStem");
        if (!StringUtils.isBlank(attrRootStem)) {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(
              GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoader", false);
          
          //see if attributeDef
          if (attributeDefName != null) {
            
            //lets get the attributeDefs which have this type
            Set<AttributeDef> attributeDefs = sourceCache.get(GrouperLoaderType.ATTR_SQL_SIMPLE);
            
            if (attributeDefs == null) {
    
              attributeDefs = GrouperDAOFactory.getFactory().getAttributeAssign()
                .findAttributeDefsByAttributeDefNameId(attributeDefName.getId());
    
              sourceCache.put(GrouperLoaderType.ATTR_SQL_SIMPLE, attributeDefs);
              
            }
    
            for (AttributeDef attributeDef : attributeDefs) {
              
               //lets get all attribute values
               String grouperLoaderType = attributeDef.getAttributeValueDelegate().retrieveValueString(GrouperCheckConfig.attributeLoaderStemName() + ":" + GrouperLoader.ATTRIBUTE_LOADER_TYPE);
                
               GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
        
               String jobName = grouperLoaderTypeEnum.name() + "__" + attributeDef.getName() + "__" + attributeDef.getUuid();
    
               //diagnosticsTasks.add(new DiagnosticLoaderJobTest("ATTR_SQL_SIMPLE__penn:community:employee:orgPermissions:orgs__092bd6259d814b5db665f2f0f4ca7dc6", GrouperLoaderType.ATTR_SQL_SIMPLE));
               diagnosticsTasks.add(new DiagnosticLoaderJobTest(jobName, grouperLoaderTypeEnum));
            }
          }
        }
      }
      
      {
        AttributeDefName ldapLoaderAttributeDefName = LoaderLdapUtils.grouperLoaderLdapAttributeDefName(false);
        if (ldapLoaderAttributeDefName != null) {
          
          //lets get the groups which have this type
          Set<DiagnosticLoaderJobTest> diagnosticLoaderJobTests = sourceCache.get(GrouperLoaderType.LDAP_SIMPLE);
          
          if (diagnosticLoaderJobTests == null) {
  
            diagnosticLoaderJobTests = new LinkedHashSet<DiagnosticLoaderJobTest>();
            Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet(ldapLoaderAttributeDefName.getId()), null, null, true, false);
  
            for (AttributeAssign attributeAssign : attributeAssigns) {
              
              Group group = attributeAssign.getOwnerGroup();

              //lets get all attribute values
              String grouperLoaderType = attributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapTypeName());
               
              GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
       
              String jobName = grouperLoaderTypeEnum.name() + "__" + group.getName() + "__" + group.getUuid();
              
              DiagnosticLoaderJobTest diagnosticLoaderJobTest = new DiagnosticLoaderJobTest(jobName, grouperLoaderTypeEnum);
              
              diagnosticLoaderJobTests.add(diagnosticLoaderJobTest);
              
            }
 
            //diagnosticsTasks.add(new DiagnosticLoaderJobTest("ATTR_SQL_SIMPLE__penn:community:employee:orgPermissions:orgs__092bd6259d814b5db665f2f0f4ca7dc6", GrouperLoaderType.ATTR_SQL_SIMPLE));
            sourceCache.put(GrouperLoaderType.LDAP_SIMPLE, diagnosticLoaderJobTests);
            
          }
  
          for (DiagnosticLoaderJobTest diagnosticLoaderJobTest : diagnosticLoaderJobTests) {
            
             diagnosticsTasks.add(diagnosticLoaderJobTest);
          }
        }
      }
      
      {
        //do min size groups
        Pattern groupNamePattern = Pattern.compile("^ws\\.diagnostic\\.checkGroupSize\\.(.+)\\.groupName$");
        
        Properties properties = GrouperConfig.retrieveConfig().properties();
        for (String key : (Set<String>)(Object)properties.keySet()) {
          Matcher groupNameMatcher = groupNamePattern.matcher(key);
          if (groupNameMatcher.matches()) {
            String configName = groupNameMatcher.group(1);
            int minSize = Integer.parseInt(GrouperConfig.retrieveConfig().propertyValueString(
                "ws.diagnostic.checkGroupSize." + configName + ".minSize"));
            String groupName = properties.getProperty(key);
            diagnosticsTasks.add(new DiagnosticMinGroupSize(groupName, minSize));
          }
        }
      }
      
    }
  };
  
  /**
   * cache the results of which groups or attributes are loadable
   */
  @SuppressWarnings("unchecked")
  private static GrouperCache<GrouperLoaderType, Set> sourceCache = new GrouperCache<GrouperLoaderType, Set>("loaderSets", 100, false, 1200, 1200, false);

  /**
   * append the diagnostics for this tasks
   * @param diagnosticsTasks
   */
  public abstract void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static DiagnosticType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(DiagnosticType.class, 
        string, exceptionOnNotFound);
  }

}
