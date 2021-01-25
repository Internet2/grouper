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
package edu.internet2.middleware.subject.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.subectSource.SubjectSourceConfiguration;
import edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.Search;

/**
 * hierarchical config class for subject.properties
 * @author mchyzer
 *
 */
public class SubjectConfig extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private SubjectConfig() {
    
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static SubjectConfig retrieveConfig() {
    return retrieveConfig(SubjectConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "subject.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "subject.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "subject.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "subject.config.secondsBetweenUpdateChecks";
  }

  /**
   * exclude these sourceIds from the subject cache
   */
  private Set<String> subjectCacheExcludeSourceIds = null;
  
  /**
   * @return the cache
   */
  public Set<String> subjectCacheExcludeSourceIds() {
    if (subjectCacheExcludeSourceIds == null) {
      subjectCacheExcludeSourceIds = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(this.propertyValueString("subject.cache.excludeSourceIds"), ","));
    }
    return subjectCacheExcludeSourceIds;
  }
  
  /**
   * cache the sources by source config id (which might not be source id)
   */
  private Map<String, Source> sources;

  /**
   * pattern for source
   * subjectApi.source.<configName>.id
   */
  private static Pattern sourceIdConfigPattern = Pattern.compile("^subjectApi\\.source\\.([^.]+)\\.id$");
  
  /**
   * pattern for param value
   * subjectApi.source.<configName>.param.throwErrorOnFindAllFailure.value
   */
  private static Pattern paramValueConfigPattern = Pattern.compile("^subjectApi\\.source\\.[^.]+\\.param\\.([^.]+)\\.value$");
  
  /**
   * pattern for param value
   * subjectApi.source.<configName>.search.<searchType>.param.<paramName>.value = something
   */
  private static Pattern searchParamValueConfigPattern = Pattern.compile("^subjectApi\\.source\\.[^.]+\\.search\\.[^.]+\\.param\\.([^.]+)\\.value$");
  
  public Source reloadSourceConfigs(String sourceId) {
    if (this.sources == null) {
      retrieveSourceConfigs();
      for (Source source : this.sources.values()) {
        if (sourceId.equals(source.getId())) {
          return source;
        }
      }
    } else {
      for (String configName : this.propertyNames()) {
        Matcher matcher = sourceIdConfigPattern.matcher(configName);
        if (matcher.matches()) {
          String sourceConfigId = matcher.group(1);
          String thisSourceId = propertyValueStringRequired("subjectApi.source." + sourceConfigId + ".id");
          if (sourceId.equals(thisSourceId)) {
            Source source = loadSourceConfigs(sourceConfigId);
            this.sources.put(sourceConfigId, source);
            return source;
          }
        }
      }
    }
    
    return null;
  }
  
  private Source loadSourceConfigs(String sourceConfigId) {

    //  # the adapter class implements the interface: edu.internet2.middleware.subject.Source
    //  # generally the adapter class should extend: edu.internet2.middleware.subject.provider.BaseSourceAdapter
    //  # edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2  :  if doing JDBC this should be used if possible.  All subject data in one table/view.
    //  # edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter   :  oldest JDBC source.  Put freeform queries in here
    //  # edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter   :  used for LDAP
    //  # subjectApi.source.<configName>.adapterClass = 

    BaseSourceAdapter source = null;
    
    {
    
      String adapterClassName = propertyValueString("subjectApi.source." + sourceConfigId + ".adapterClass");
      
      Class<?> adapterClassClass = SubjectUtils.forName(adapterClassName);
      source = (BaseSourceAdapter)SubjectUtils.newInstance(adapterClassClass);
    }              

    source.setConfigId(sourceConfigId);
    
    {
      //  # generally the <configName> is the same as the source id.  Generally this should not have special characters
      //  # subjectApi.source.<configName>.id = sourceId
      String sourceId = propertyValueStringRequired("subjectApi.source." + sourceConfigId + ".id");
      source.setId(sourceId);
    }

    {
      //  # this is a friendly name for the source
      //  # subjectApi.source.<configName>.name = sourceName
      String sourceName = propertyValueStringRequired("subjectApi.source." + sourceConfigId + ".name");
      source.setName(sourceName);
    }

    {
      //  # type is not used all that much 
      //  # subjectApi.source.<configName>.types = person, application
      String sourceTypes = propertyValueString("subjectApi.source." + sourceConfigId + ".types");
      if (!StringUtils.isEmpty(sourceTypes)) {
        for (String sourceType : SubjectUtils.splitTrim(sourceTypes, ",")) {
          source.addSubjectType(sourceType);
        }
      }
    }

    {
      //params (note, name is optional and generally not there)
      //  # subjectApi.source.<configName>.param.throwErrorOnFindAllFailure.value = true
      for (String paramValueKey : this.propertyNames()) {
        
        if (paramValueKey.startsWith("subjectApi.source." + sourceConfigId + ".param") 
            && paramValueKey.endsWith(".value") ) {
          String paramValue = propertyValueString(paramValueKey);
          Matcher paramValueMatcher = paramValueConfigPattern.matcher(paramValueKey);
          paramValueMatcher.matches();
          String paramConfigId = paramValueMatcher.group(1);
          String paramName = propertyValueString("subjectApi.source." + sourceConfigId + ".param." + paramConfigId + ".name");
          if (StringUtils.isBlank(paramName)) {
            paramName = paramConfigId;
          }
          source.addInitParam(paramName, paramValue);
        }
      }
    }
    
    {
      List<SubjectSourceConfiguration> subjectSourceConfigurations = SubjectSourceConfiguration.retrieveAllSubjectSourceConfigurations();
      SubjectSourceConfiguration subjectSourceConfiguration = null;
      for (SubjectSourceConfiguration subjectSourceConfig: subjectSourceConfigurations) {
        
        if (StringUtils.equals(subjectSourceConfig.getConfigId(), sourceConfigId)) {
          subjectSourceConfiguration = subjectSourceConfig;
          break;
        }
      }
      
      if (subjectSourceConfiguration != null) {
        Map<String, GrouperConfigurationModuleAttribute> attributes = subjectSourceConfiguration.retrieveAttributes();
        GrouperConfigurationModuleAttribute sqlAttribute = attributes.get("param.jdbcConfigId.value");
        if (sqlAttribute != null) {
          
//          source.addInitParam("jdbcConnectionProvider", GrouperJdbcConnectionProvider.class.getName());
          
          GrouperConfigurationModuleAttribute subjectIdAttribute = attributes.get("param.SubjectID_AttributeType.value");
          String subjectIdAttributeName = subjectIdAttribute.getValueOrExpressionEvaluation();
          
          GrouperConfigurationModuleAttribute subjectNameAttribute = attributes.get("param.Name_AttributeType.value");
          String subjectNameAttributeName = subjectNameAttribute.getValueOrExpressionEvaluation();
          
          GrouperConfigurationModuleAttribute subjectDescriptionAttribute = attributes.get("param.Description_AttributeType.value");
          String subjectDescriptionAttributeName = subjectDescriptionAttribute.getValueOrExpressionEvaluation();
          
          GrouperConfigurationModuleAttribute subjectEmailAttribute = attributes.get("param.emailAttributeName.value");
          String subjectEmailAttributeName = null;
          if (subjectEmailAttribute != null) {            
            subjectEmailAttributeName = subjectEmailAttribute.getValueOrExpressionEvaluation();
          }
          
          GrouperConfigurationModuleAttribute subjectNetIdAttribute = attributes.get("param.netId.value");
          String subjectNetIdAttributeName = null;
          if (subjectNetIdAttribute != null) {            
            subjectNetIdAttributeName = subjectNetIdAttribute.getValueOrExpressionEvaluation();
          }
          
          
          String numberOfAttributes = propertyValueString("subjectApi.source." + sourceConfigId + ".numberOfAttributes");
                
          if (StringUtils.isNotBlank(numberOfAttributes)) {
            
            int numberOfAttrs = Integer.parseInt(numberOfAttributes);
            for (int i=0; i<numberOfAttrs; i++) {
              
              String subjectAttributeNme = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".name");
              if (StringUtils.equals(subjectIdAttributeName, subjectAttributeNme)) {
                String sourceAttribute = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".sourceAttribute");
                source.addInitParam("subjectIdCol", sourceAttribute);
              }
              
              if (StringUtils.equals(subjectNameAttributeName, subjectAttributeNme)) {
                String sourceAttribute = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".sourceAttribute");
                source.addInitParam("nameCol", sourceAttribute);
                source.removeInitParam("Name_AttributeType");
              }
              
              if (StringUtils.equals(subjectDescriptionAttributeName, subjectAttributeNme)) {
                String sourceAttribute = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".sourceAttribute");
                source.addInitParam("descriptionCol", sourceAttribute);
                source.removeInitParam("Description_AttributeType");
              }
              
              if (StringUtils.isNotBlank(subjectEmailAttributeName) && StringUtils.equals(subjectEmailAttributeName, subjectAttributeNme)) {
                String sourceAttribute = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".sourceAttribute");
                source.addInitParam("emailAttributeName", sourceAttribute);
              }
              
              if (StringUtils.isNotBlank(subjectNetIdAttributeName) && StringUtils.equals(subjectNetIdAttributeName, subjectAttributeNme)) {
                String sourceAttribute = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".sourceAttribute");
                source.addInitParam("netId", sourceAttribute);
              }
              
            }
          }
          
        }
        
      }
      
    }
    
    {
      //  # internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
      //  # subjectApi.source.<configName>.internalAttributes = someName, anotherName
      String internalAttributes = propertyValueString("subjectApi.source." + sourceConfigId + ".internalAttributes");
      if (!StringUtils.isEmpty(internalAttributes)) {
        for (String internalAttribute : SubjectUtils.splitTrim(internalAttributes, ",")) {
          source.addInternalAttribute(internalAttribute);
        }
      }
      
      String numberOfAttributes = propertyValueString("subjectApi.source." + sourceConfigId + ".numberOfAttributes");
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        for (int i=0; i<numberOfAttrs; i++) {
          
          boolean isInternal = propertyValueBoolean("subjectApi.source." + sourceConfigId + ".attribute."+i+".internal", false);
          if (isInternal) {
            String name = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".name");
            if (StringUtils.isNotBlank(name)) {
              source.addInternalAttribute(name);
            }
          }
        
        }
        
      }
      
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        Set<String> subjectIdentifiers = new TreeSet<String>();
        for (int i=0; i<numberOfAttrs; i++) {
          
          boolean isTranslation = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + sourceConfigId + ".attribute."+i+".isTranslation", false);
          if (!isTranslation) {
            String sourceAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".sourceAttribute");
            String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".name");
            boolean isSubjectIdentifier = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + sourceConfigId + ".attribute."+i+".subjectIdentifier", false);
            
            if (isSubjectIdentifier) {
              subjectIdentifiers.add(subjectAttributeName);
              source.addInitParam("subjectIdentifierCol"+(subjectIdentifiers.size()-1), sourceAttributeName);
              source.addInitParam("subjectIdentifierAttribute"+(subjectIdentifiers.size()-1), subjectAttributeName);
            }
          }
        
        }
        
        source.addInitParam("identifierAttributes", GrouperUtil.join(subjectIdentifiers.iterator(), ","));
        
      }
      
      
      
      
    }

    {
      //  # attributes from ldap object to become subject attributes.  comma separated
      //  # subjectApi.source.<configName>.attributes = cn, sn, uid, department, exampleEduRegId
      String attributes = propertyValueString("subjectApi.source." + sourceConfigId + ".attributes");
      if (!StringUtils.isEmpty(attributes)) {
        for (String attribute : SubjectUtils.splitTrim(attributes, ",")) {
          source.addAttribute(attribute);
        }
      }
      
      String extraAttributesFromSource = propertyValueString("subjectApi.source." + sourceConfigId + ".extraAttributesFromSource");
      if (!StringUtils.isEmpty(extraAttributesFromSource)) {
        for (String extraAttribute : SubjectUtils.splitTrim(extraAttributesFromSource, ",")) {
          source.addAttribute(extraAttribute);
        }
      }
      
      String numberOfAttributes = propertyValueString("subjectApi.source." + sourceConfigId + ".numberOfAttributes");
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        for (int i=0; i<numberOfAttrs; i++) {
          
          boolean isTranslation = propertyValueBoolean("subjectApi.source." + sourceConfigId + ".attribute."+i+".isTranslation", false);
          if (!isTranslation) {
            String sourceAttributeName = propertyValueString("subjectApi.source." + sourceConfigId + ".attribute."+i+".sourceAttribute");
            if (StringUtils.isNotBlank(sourceAttributeName)) {
              source.addAttribute(sourceAttributeName);
            }
          }
        
        }
        
      }
      
    }

    //  digester.addObjectCreate("sources/source/search",
    //      "edu.internet2.middleware.subject.provider.Search");
    //  digester.addCallMethod("sources/source/search/searchType", "setSearchType", 0);
    //  digester.addCallMethod("sources/source/search/param", "addParam", 2);
    //  digester.addCallParam("sources/source/search/param/param-name", 0);
    //  digester.addCallParam("sources/source/search/param/param-value", 1);
    //  digester.addSetNext("sources/source/search", "loadSearch");

    //  # searchTypes are: 
    //  #   searchSubject: find a subject by ID.  ID is generally an opaque and permanent identifier, e.g. 12345678.  Each subject has one and only on ID.  Returns one result when searching for one ID.
    //  #   searchSubjectByIdentifier: find a subject by identifier.  Identifier is anything that uniquely identifies the user, e.g. jsmith or jsmith@institution.edu.  
    //  #        Subjects can have multiple identifiers.  Note: it is nice to have if identifiers are unique even across sources.  Returns one result when searching for one identifier.
    //  #   search: find subjects by free form search.  Returns multiple results.
    //  # subjectApi.source.<configName>.search.<searchType>.param.<paramName>.value = something
    {
      //params (note, name is optional and generally not there)
      //  # subjectApi.source.<configName>.param.throwErrorOnFindAllFailure.value = true
      for (String searchType : new String[] {"searchSubject", "searchSubjectByIdentifier", "search"}) {
        
        Search search = new Search();
        search.setSearchType(searchType);
        
        for (String paramValueKey : this.propertyNames()) {
          
          //all search params has a value
          if (paramValueKey.startsWith("subjectApi.source." + sourceConfigId + ".search." + searchType + ".param.") 
              && paramValueKey.endsWith(".value") ) {
            String paramValue = propertyValueString(paramValueKey);
            Matcher paramValueMatcher = searchParamValueConfigPattern.matcher(paramValueKey);
            paramValueMatcher.matches();
            String paramConfigId = paramValueMatcher.group(1);
            String paramName = propertyValueString("subjectApi.source." + sourceConfigId + ".search." + searchType + ".param." + paramConfigId + ".name");
            
            //if name is not specified used the config id (most arent specified)
            if (StringUtils.isBlank(paramName)) {
              paramName = paramConfigId;
            }
            search.addParam(paramName, paramValue);
          }
        }
        
        if (StringUtils.isBlank(search.getParam("base"))) {
          String searchSubjectBase = propertyValueString("subjectApi.source." + sourceConfigId + ".search.searchSubject.param.base.value");
          if (StringUtils.isNotBlank(searchSubjectBase)) {
            search.addParam("base", searchSubjectBase);
          }
        }
        
        if (StringUtils.isBlank(search.getParam("scope"))) {
          String searchSubjectScope = propertyValueString("subjectApi.source." + sourceConfigId + ".search.searchSubject.param.scope.value");
          if (StringUtils.isNotBlank(searchSubjectScope)) {
            search.addParam("scope", searchSubjectScope);
          } else {
            search.addParam("scope", "SUBTREE_SCOPE");
          }
        }
        
        source.loadSearch(search);
      }
    }
    
    return source;
  }
  
  /**
   * process configs for sources and return the map 
   * @return the configs
   */
  public Map<String, Source> retrieveSourceConfigs() {
    if (this.sources == null) {
      synchronized (SubjectConfig.class) {
        if (this.sources == null) {
          Map<String, Source> theSources = new HashMap<String, Source>();
          
          for (String configName : this.propertyNames()) {
            
            //  # generally the <configName> is the same as the source id.  Generally this should not have special characters
            //  # subjectApi.source.<configName>.id = sourceId

            Matcher matcher = sourceIdConfigPattern.matcher(configName);
            if (matcher.matches()) {
              String sourceConfigId = matcher.group(1);
              Source source = loadSourceConfigs(sourceConfigId);
              theSources.put(sourceConfigId, source);
            }
          }
          
          this.sources = theSources;
        }
      }
    }
    return this.sources;
  }

}
