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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * properties about a source, cached
 */
public class GrouperKimIdentitySourceProperties {

  /**
   * cache of grouper source configs
   */
  private static ExpirableCache<String, GrouperKimIdentitySourceProperties> grouperKimIdentitySourcePropertiesCache 
    = new ExpirableCache<String, GrouperKimIdentitySourceProperties>(5);

  /**
   * get the source properties for source name (current source name)
   * @param sourceId
   * @return properties for source and app name
   */
  public static GrouperKimIdentitySourceProperties grouperKimIdentitySourceProperties(String sourceId) {
    
    GrouperKimIdentitySourceProperties grouperKimIdentitySourceProperties = 
      grouperKimIdentitySourcePropertiesCache.get(sourceId);
    if (grouperKimIdentitySourceProperties == null) {
      grouperKimIdentitySourceProperties = new GrouperKimIdentitySourceProperties();
      grouperKimIdentitySourceProperties.setSourceId(sourceId);
      
      //loop through and find this config
      
      for (int i=0;i<100;i++) {
        
        String currentSourceId = GrouperClientUtils.propertiesValue("kuali.identity.source.id." + i, false);
        if (GrouperClientUtils.isBlank(currentSourceId)) {
          break;
        }
        if (GrouperClientUtils.equals(sourceId, currentSourceId)) {
          //we found it
          {
            String nameAttribute = GrouperClientUtils.propertiesValue("kuali.identity.source.nameAttribute." + i, false);
            grouperKimIdentitySourceProperties.setNameAttribute(nameAttribute);
          }
          
          {
            String identifierAttribute = GrouperClientUtils.propertiesValue("kuali.identity.source.identifierAttribute." + i, false);
            grouperKimIdentitySourceProperties.setIdentifierAttribute(identifierAttribute);
          }
          
          {
            String firstNameAttribute = GrouperClientUtils.propertiesValue("kuali.identity.source.firstNameAttribute." + i, false);
            grouperKimIdentitySourceProperties.setFirstNameAttribute(firstNameAttribute);
          }
          
          {
            String lastNameAttribute = GrouperClientUtils.propertiesValue("kuali.identity.source.lastNameAttribute." + i, false);
            grouperKimIdentitySourceProperties.setLastNameAttribute(lastNameAttribute);
          }
          
          {
            String middleNameAttribute = GrouperClientUtils.propertiesValue("kuali.identity.source.middleNameAttribute." + i, false);
            grouperKimIdentitySourceProperties.setMiddleNameAttribute(middleNameAttribute);
          }
          
          {
            String emailAttribute = GrouperClientUtils.propertiesValue("kuali.identity.source.emailAttribute." + i, false);
            grouperKimIdentitySourceProperties.setEmailAttribute(emailAttribute);
          }
          
          {
            String entityTypeCode = GrouperClientUtils.propertiesValue("kuali.identity.source.entityTypeCode." + i, false);
            grouperKimIdentitySourceProperties.setEntityTypeCode(entityTypeCode);
          }
          
          
          break;
        }
        
      }
      grouperKimIdentitySourcePropertiesCache.put(sourceId, grouperKimIdentitySourceProperties);
    }
    return grouperKimIdentitySourceProperties;
  }
  
  /**
   * 
   * @return first name attribute
   */
  public String getFirstNameAttribute() {
    return this.firstNameAttribute;
  }

  /**
   * 
   * @param firstNameAttribute1
   */
  public void setFirstNameAttribute(String firstNameAttribute1) {
    this.firstNameAttribute = firstNameAttribute1;
  }

  /**
   * 
   * @return last name attribute
   */
  public String getLastNameAttribute() {
    return this.lastNameAttribute;
  }

  
  /**
   * 
   * @param lastNameAttribute1
   */
  public void setLastNameAttribute(String lastNameAttribute1) {
    this.lastNameAttribute = lastNameAttribute1;
  }

  /**
   * 
   * @return attribute
   */
  public String getMiddleNameAttribute() {
    return this.middleNameAttribute;
  }

  /**
   * 
   * @param middleNameAttribute1
   */
  public void setMiddleNameAttribute(String middleNameAttribute1) {
    this.middleNameAttribute = middleNameAttribute1;
  }

  /**
   * source id
   */
  private String sourceId;
  
  /**
   * identifier attribute
   */
  private String identifierAttribute;
  
  /**
   * identifier attribute
   * @return identifier attribute
   */
  public String getIdentifierAttribute() {
    return this.identifierAttribute;
  }

  /**
   * identifier attribute
   * @param identifierAttribute1
   */
  public void setIdentifierAttribute(String identifierAttribute1) {
    this.identifierAttribute = identifierAttribute1;
  }

  /**
   * name attribute from a subject (attribute for subjects in this source which is the name of the subject)
   */
  private String nameAttribute;

  /**
   * email attribute from a subject
   */
  private String emailAttribute;
  
  /**
   * email attribute from a subject
   * @return the emailAttribute
   */
  public String getEmailAttribute() {
    return this.emailAttribute;
  }
  
  /**
   * email attribute from a subject
   * @param emailAttribute1 the emailAttribute to set
   */
  public void setEmailAttribute(String emailAttribute1) {
    this.emailAttribute = emailAttribute1;
  }

  /**
   * first name attribute from a subject (attribute for subjects in this source which is the first name of the subject)
   */
  private String firstNameAttribute;
  
  /**
   * last name attribute from a subject (attribute for subjects in this source which is the last name of the subject)
   */
  private String lastNameAttribute;
  
  /**
   * middle name attribute from a subject (attribute for subjects in this source which is the middle name of the subject)
   */
  private String middleNameAttribute;
  
  /**
   * entity type code is the type of entity, e.g. PERSON
   */
  private String entityTypeCode;
  
  /**
   * entity type code is the type of entity, e.g. PERSON
   * @return the entityTypeCode
   */
  public String getEntityTypeCode() {
    return this.entityTypeCode;
  }
  
  /**
   * entity type code is the type of entity, e.g. PERSON
   * @param entityTypeCode1 the entityTypeCode to set
   */
  public void setEntityTypeCode(String entityTypeCode1) {
    this.entityTypeCode = entityTypeCode1;
  }

  /**
   * source id
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * source id
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * name attribute from a subject (attribute for subjects in this source which is the name of the subject)
   * @return name attribute
   */
  public String getNameAttribute() {
    return this.nameAttribute;
  }

  /**
   * name attribute from a subject (attribute for subjects in this source which is the name of the subject)
   * @param nameAttribute1
   */
  public void setNameAttribute(String nameAttribute1) {
    this.nameAttribute = nameAttribute1;
  }
  
}
