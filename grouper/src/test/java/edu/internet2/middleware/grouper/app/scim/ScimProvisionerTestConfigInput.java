package edu.internet2.middleware.grouper.app.scim;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestConfigInput;

/**
 * 
 * @author mchyzer
 *
 */
public class ScimProvisionerTestConfigInput {

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
  public ScimProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
  public ScimProvisionerTestConfigInput assignChangelogConsumerConfigId(String changelogConsumerConfigId1) {
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
  public ScimProvisionerTestConfigInput assignConfigId(String string) {
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
   * if theres an accept header
   */
  private String acceptHeader;

  /**
   * if theres an accept header
   */
  public String getAcceptHeader() {
    return this.acceptHeader;
  }

  /**
   * if theres an accept header
   * @param acceptHeader
   */
  public ScimProvisionerTestConfigInput assignAcceptHeader(String acceptHeader) {
    this.acceptHeader = acceptHeader;
    return this;
  }
  
  /**
   * 
   */
  private String bearerTokenExternalSystemConfigId;

  /**
   * 
   * @return
   */
  public String getBearerTokenExternalSystemConfigId() {
    return this.bearerTokenExternalSystemConfigId;
  }

  /**
   * 
   * @param bearerTokenExternalSystemConfigId
   * @return this for chaining
   */
  public ScimProvisionerTestConfigInput assignBearerTokenExternalSystemConfigId(String bearerTokenExternalSystemConfigId) {
    this.bearerTokenExternalSystemConfigId = bearerTokenExternalSystemConfigId;
    return this;
  }
  
  /**
   * e.g. ${subject.getAttributeValue('email')}
   */
  private String subjectLinkCache0;

  
  public String getSubjectLinkCache0() {
    return subjectLinkCache0;
  }
  

  public ScimProvisionerTestConfigInput assignSubjectLinkCache0(String subjectLinkCache0) {
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
  public ScimProvisionerTestConfigInput assignGroupDeleteType(String groupDeleteType) {
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
  public ScimProvisionerTestConfigInput assignEntityDeleteType(String entityDeleteType) {
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
  public ScimProvisionerTestConfigInput assignMembershipDeleteType(String membershipDeleteType) {
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


  public ScimProvisionerTestConfigInput assignGroupOfUsersToProvision(Group groupOfUsersToProvision) {
    this.groupOfUsersToProvision = groupOfUsersToProvision;
    return this;
  }


  /**
   * 0, or 2 (default)
   */
  public ScimProvisionerTestConfigInput assignGroupAttributeCount(int groupAttributeCount) {
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
   * e.g. AWS or Github
   */
  private String scimType;

  /**
   * e.g. AWS or Github
   * @return
   */
  public String getScimType() {
    return scimType;
  }

  /**
   * e.g. AWS or Github
   * @param scimType
   */
  public ScimProvisionerTestConfigInput assignScimType(String scimType) {
    this.scimType = scimType;
    return this;
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
  public ScimProvisionerTestConfigInput assignSelectAllEntities(boolean selectAllEntities) {
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
  public ScimProvisionerTestConfigInput assignEntityAttribute4name(String entityAttribute5name1) {
    this.entityAttribute4name = entityAttribute5name1;
    return this;
  }

  
  public boolean isUseFirstLastName() {
    return useFirstLastName;
  }

  private boolean useFirstLastName = false;

  public ScimProvisionerTestConfigInput assignUseFirstLastName(boolean useFirstLastName) {
    this.useFirstLastName = useFirstLastName;
    return this;
  }

  
  public boolean isUseEmails() {
    return useEmails;
  }

  private boolean useEmails = false;

  public ScimProvisionerTestConfigInput assignUseEmails(boolean b) {
    this.useEmails  = b;
    return this;
  }
  
  private boolean useActiveOnUser = false;

  public boolean isUseActiveOnUser() {
    return useActiveOnUser;
  }
  
  public ScimProvisionerTestConfigInput assignUseActiveOnUser(boolean b) {
    this.useActiveOnUser  = b;
    return this;
  }
  
}
