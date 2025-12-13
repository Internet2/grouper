package edu.internet2.middleware.grouper.app.adobe;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;

/**
 * 
 * @author mchyzer
 *
 */
public class AdobeProvisionerTestConfigInput {

  /**
   * null will use params
   */
  private String provisioningStrategy = null;
  
  /**
   * null will use params
   * scimGithubOrgs is the use case from michael gettes
   * @return
   */
  public String getProvisioningStrategy() {
    return provisioningStrategy;
  }

  /**
   * null will use params
   * scimGithubOrgs is the use case from michael gettes
   * @param provisioningStrategy
   * @return this for chaining
   */
  public AdobeProvisionerTestConfigInput assignProvisioningStrategy(String provisioningStrategy) {
    this.provisioningStrategy = provisioningStrategy;
    return this;
  }

  /**
   * extra config by suffix and value
   */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  /**
   * extra config by suffix and value
   * @param suffix
   * @param value
   * @return this for chaining
   */
  public AdobeProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  
  /**
   * extra config by suffix and value
   * @return map
   */
  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

  /**
   * change log consumer config id
   */
  private String changelogConsumerConfigId;
  
  
  /**
   * change log consumer config id
   * @return config id
   */
  public String getChangelogConsumerConfigId() {
    return this.changelogConsumerConfigId;
  }

  /**
   * change log consumer config id
   * @param changelogConsumerConfigId1
   * @return this for chaining
   */
  public AdobeProvisionerTestConfigInput assignChangelogConsumerConfigId(String changelogConsumerConfigId1) {
    this.changelogConsumerConfigId = changelogConsumerConfigId1;
    return this;
  }

  /**
   * no default
   */
  private String configId = null;

  /**
   * no default
   * @param string
   * @return this for chaining
   */
  public AdobeProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * no default
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }
  
  /**
   * e.g. ${subject.getAttributeValue('email')}
   */
  private String subjectLinkCache0;

  
  public String getSubjectLinkCache0() {
    return subjectLinkCache0;
  }
  

  public AdobeProvisionerTestConfigInput assignSubjectLinkCache0(String subjectLinkCache0) {
    this.subjectLinkCache0 = subjectLinkCache0;
    return this;
  }

  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   */
  private String groupDeleteType; 
  

  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   */
  public String getGroupDeleteType() {
    return groupDeleteType;
  }

  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   * @param groupDeleteType
   * @return this for chaining
   */
  public AdobeProvisionerTestConfigInput assignGroupDeleteType(String groupDeleteType) {
    this.groupDeleteType = groupDeleteType;
    return this;
  }

  /**
   * entityDeleteType e.g. deleteEntitiesIfNotExistInGrouper or deleteEntitiesIfGrouperDeleted or deleteEntitiesIfGrouperCreated or null (default)
   */
  private String entityDeleteType; 
  

  /**
   * entityDeleteType e.g. deleteEntitiesIfNotExistInGrouper or deleteEntitiesIfGrouperDeleted or deleteEntitiesIfGrouperCreated or null (default)
   */
  public String getEntityDeleteType() {
    return entityDeleteType;
  }

  /**
   * entityDeleteType e.g. deleteEntitiesIfNotExistInGrouper or deleteEntitiesIfGrouperDeleted or deleteEntitiesIfGrouperCreated or null (default)
   * @param entityDeleteType
   * @return this for chaining
   */
  public AdobeProvisionerTestConfigInput assignEntityDeleteType(String entityDeleteType) {
    this.entityDeleteType = entityDeleteType;
    return this;
  }
  
  /**
   * membershipDeleteType e.g. deleteMembershipsIfNotExistInGrouper or deleteMembershipsIfGrouperDeleted or deleteMembershipsIfGrouperCreated or null (default)
   */
  private String membershipDeleteType; 
  

  /**
   * membershipDeleteType e.g. deleteMembershipsIfNotExistInGrouper or deleteMembershipsIfGrouperDeleted or deleteMembershipsIfGrouperCreated or null (default)
   */
  public String getMembershipDeleteType() {
    return membershipDeleteType;
  }

  /**
   * membershipDeleteType e.g. deleteMembershipsIfNotExistInGrouper or deleteMembershipsIfGrouperDeleted or deleteMembershipsIfGrouperCreated or null (default)
   * @param membershipDeleteType
   * @return this for chaining
   */
  public AdobeProvisionerTestConfigInput assignMembershipDeleteType(String membershipDeleteType) {
    this.membershipDeleteType = membershipDeleteType;
    return this;
  }
  
  /**
   * 
   */
  private Group groupOfUsersToProvision;
  /**
   * 0, or 2 (default)
   */
  private int groupAttributeCount = 2;

  public Group getGroupOfUsersToProvision() {
    return groupOfUsersToProvision;
  }


  public AdobeProvisionerTestConfigInput assignGroupOfUsersToProvision(Group groupOfUsersToProvision) {
    this.groupOfUsersToProvision = groupOfUsersToProvision;
    return this;
  }


  /**
   * 0, or 2 (default)
   */
  public AdobeProvisionerTestConfigInput assignGroupAttributeCount(int groupAttributeCount) {
    this.groupAttributeCount = groupAttributeCount;
    return this;
  }


  /**
   * 0, or 2 (default)
   */
  public int getGroupAttributeCount() {
    return groupAttributeCount;
  }
  
  /**
   * if select all entities
   */
  private boolean selectAllEntities;

  /**
   * if select all entities
   * @return if select all entities
   */
  public boolean isSelectAllEntities() {
    return this.selectAllEntities;
  }

  /**
   * if select all entities
   * @param selectAllEntities
   */
  public AdobeProvisionerTestConfigInput assignSelectAllEntities(boolean selectAllEntities) {
    this.selectAllEntities = selectAllEntities;
    return this;
  }
  
  /**
   * displayName (default) or emailValue
   */
  private String entityAttribute4name = "displayName";

  /**
   * displayName (default) or emailValue
   * @return
   */
  public String getEntityAttribute4name() {
    return entityAttribute4name;
  }

  /**
   * displayName (default) or emailValue
   * @param entityAttribute5name1
   */
  public AdobeProvisionerTestConfigInput assignEntityAttribute4name(String entityAttribute5name1) {
    this.entityAttribute4name = entityAttribute5name1;
    return this;
  }

}
