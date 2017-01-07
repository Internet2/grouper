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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

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
                    Matcher paramValueMatcher = paramValueConfigPattern.matcher(configName);
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
                //  # internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
                //  # subjectApi.source.<configName>.internalAttributes = someName, anotherName
                String internalAttributes = propertyValueString("subjectApi.source." + sourceConfigId + ".internalAttributes");
                if (!StringUtils.isEmpty(internalAttributes)) {
                  for (String internalAttribute : SubjectUtils.splitTrim(internalAttributes, ",")) {
                    source.addInternalAttribute(internalAttribute);
                  }
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
                      Matcher paramValueMatcher = searchParamValueConfigPattern.matcher(configName);
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
                  
                  source.loadSearch(search);
                }
              }
            }
          }
          
          this.sources = theSources;
        }
      }
    }
    return this.sources;
  }

}
