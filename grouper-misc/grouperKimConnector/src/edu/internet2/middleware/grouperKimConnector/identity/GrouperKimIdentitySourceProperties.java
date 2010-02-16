/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import org.apache.commons.lang.StringUtils;

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
   * get the source properties for an app name (current app name)
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
        if (StringUtils.isBlank(currentSourceId)) {
          break;
        }
        if (StringUtils.equals(sourceId, currentSourceId)) {
          //we found it
          String nameAttribute = GrouperClientUtils.propertiesValue("kuali.identity.source.nameAttribute." + i, true);
          grouperKimIdentitySourceProperties.setNameAttribute(nameAttribute);
          break;
        }
      }
      grouperKimIdentitySourcePropertiesCache.put(sourceId, grouperKimIdentitySourceProperties);
    }
    return grouperKimIdentitySourceProperties;
  }
  
  /**
   * source id
   */
  private String sourceId;
  
  
  /**
   * name attribute from a subject (attribute for subjects in this source which is the name of the subject)
   */
  private String nameAttribute;


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
