package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * objects and metadata
 * @author mchyzer
 *
 */
public class LdapSyncObjectContainer {

  /**
   * reference back to sync data
   */
  private LdapSyncData ldapSyncData;

  
  
  /**
   * reference back to sync data
   * @return the sync data
   */
  public LdapSyncData getLdapSyncData() {
    return this.ldapSyncData;
  }

  /**
   * reference back to sync data
   * @param ldapSyncData1
   */
  public void setLdapSyncData(LdapSyncData ldapSyncData1) {
    this.ldapSyncData = ldapSyncData1;
  }

  /**
   * convert attribute name to zero index of array
   */
  private Map<String, Integer> lookupAttributeToIndexZeroIndexed = new HashMap<String, Integer>();

  /**
   * 
   * @return
   */
  public Integer lookupAttributeToIndexZeroIndexed(String attributeName, boolean exceptionIfNotFound) {
    Integer columnIndex = lookupAttributeToIndexZeroIndexed.get(attributeName);
    if (columnIndex == null) {
      
      for (int i=0;i<this.attributeMetadatas.size();i++) {
        if (GrouperClientUtils.equalsIgnoreCase(attributeName, this.attributeMetadatas.get(i).getAttributeName())) {
          columnIndex = i;
          break;
        }
      }
      if (columnIndex == null) {
        if (!exceptionIfNotFound) {
          return null;
        }
        throw new RuntimeException("Cant find attribute: " + attributeName + ", in list: " + GrouperClientUtils.toStringForLog(this.attributeMetadatas));
      }
      lookupAttributeToIndexZeroIndexed.put(attributeName, columnIndex);
    }
    return columnIndex;

  }
  
  /**
   * list of users or groups
   */
  private List<LdapSyncObject> ldapSyncObjects;

  /**
   * list of users or groups
   * @return list
   */
  public List<LdapSyncObject> getLdapSyncObjects() {
    return this.ldapSyncObjects;
  }

  /**
   * list of users or groups
   * @param ldapSyncObjects1
   */
  public void setLdapSyncObjects(List<LdapSyncObject> ldapSyncObjects1) {
    this.ldapSyncObjects = ldapSyncObjects1;
  }
  
  /**
   * list of attribute metadata matches the order of the attributes in the ldap sync object
   */
  private List<LdapSyncAttributeMetadata> attributeMetadatas;

  /**
   * list of attribute metadata matches the order of the attributes in the ldap sync object
   * @return metadata
   */
  public List<LdapSyncAttributeMetadata> getAttributeMetadatas() {
    return this.attributeMetadatas;
  }

  /**
   * list of attribute metadata matches the order of the attributes in the ldap sync object
   * @param attributeMetadata1
   */
  public void setAttributeMetadata(List<LdapSyncAttributeMetadata> attributeMetadata1) {
    this.attributeMetadatas = attributeMetadata1;
  }
  
}
