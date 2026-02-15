package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;

/**
 * Config input for FreshRequester provisioner tests
 */
public class FreshRequesterProvisionerTestConfigInput {

  /**
   * null will use params
   */
  private String provisioningStrategy = null;

  /**
   * null will use params
   * @return
   */
  public String getProvisioningStrategy() {
    return provisioningStrategy;
  }

  /**
   * null will use params
   * @param provisioningStrategy
   * @return this for chaining
   */
  public FreshRequesterProvisionerTestConfigInput assignProvisioningStrategy(String provisioningStrategy) {
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
  public FreshRequesterProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
  public FreshRequesterProvisionerTestConfigInput assignChangelogConsumerConfigId(String changelogConsumerConfigId1) {
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
  public FreshRequesterProvisionerTestConfigInput assignConfigId(String string) {
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

  public FreshRequesterProvisionerTestConfigInput assignSubjectLinkCache0(String subjectLinkCache0) {
    this.subjectLinkCache0 = subjectLinkCache0;
    return this;
  }

  /**
   * groupDeleteType e.g. deleteGroupsIfNotExistInGrouper or deleteGroupsIfGrouperDeleted or deleteGroupsIfGrouperCreated or null (default)
   */
  private String groupDeleteType;

  /**
   * groupDeleteType
   */
  public String getGroupDeleteType() {
    return groupDeleteType;
  }

  /**
   * groupDeleteType
   * @param groupDeleteType
   * @return this for chaining
   */
  public FreshRequesterProvisionerTestConfigInput assignGroupDeleteType(String groupDeleteType) {
    this.groupDeleteType = groupDeleteType;
    return this;
  }

  /**
   * entityDeleteType
   */
  private String entityDeleteType;

  /**
   * entityDeleteType
   */
  public String getEntityDeleteType() {
    return entityDeleteType;
  }

  /**
   * entityDeleteType
   * @param entityDeleteType
   * @return this for chaining
   */
  public FreshRequesterProvisionerTestConfigInput assignEntityDeleteType(String entityDeleteType) {
    this.entityDeleteType = entityDeleteType;
    return this;
  }

  /**
   * membershipDeleteType
   */
  private String membershipDeleteType;

  /**
   * membershipDeleteType
   */
  public String getMembershipDeleteType() {
    return membershipDeleteType;
  }

  /**
   * membershipDeleteType
   * @param membershipDeleteType
   * @return this for chaining
   */
  public FreshRequesterProvisionerTestConfigInput assignMembershipDeleteType(String membershipDeleteType) {
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

  public FreshRequesterProvisionerTestConfigInput assignGroupOfUsersToProvision(Group groupOfUsersToProvision) {
    this.groupOfUsersToProvision = groupOfUsersToProvision;
    return this;
  }

  /**
   * 0, or 2 (default)
   */
  public FreshRequesterProvisionerTestConfigInput assignGroupAttributeCount(int groupAttributeCount) {
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
  public FreshRequesterProvisionerTestConfigInput assignSelectAllEntities(boolean selectAllEntities) {
    this.selectAllEntities = selectAllEntities;
    return this;
  }

}
