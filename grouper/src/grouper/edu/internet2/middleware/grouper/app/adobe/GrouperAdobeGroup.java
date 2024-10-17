package edu.internet2.middleware.grouper.app.adobe;

import java.sql.Types;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperAdobeGroup {

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAdobeGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_adobe_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.BIGINT, "12", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "2000", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "type", Types.VARCHAR, "100", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "product_name", Types.VARCHAR, "2000", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_count", Types.BIGINT, "12", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "license_quota", Types.BIGINT, "12", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_adobe_group_name_idx", true, "name");
    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);
    targetGroup.setId(String.valueOf(this.id));
    targetGroup.setName(this.name);
    targetGroup.assignAttributeValue("type", this.type);
    targetGroup.assignAttributeValue("productName", this.productName);
    targetGroup.assignAttributeValue("memberCount", this.memberCount);
    targetGroup.assignAttributeValue("licenseQuota", this.licenseQuota);
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @return
   */
  public static GrouperAdobeGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperAdobeGroup grouperAdobeGroup = new GrouperAdobeGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperAdobeGroup.setId(Long.valueOf(targetGroup.getId()));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperAdobeGroup.setName(targetGroup.getName());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("type")) { 
      grouperAdobeGroup.setType(targetGroup.retrieveAttributeValueString("type"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("productName")) { 
      grouperAdobeGroup.setProductName(targetGroup.retrieveAttributeValueString("productName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("memberCount")) { 
      grouperAdobeGroup.setMemberCount(targetGroup.retrieveAttributeValueLong("memberCount"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("licenseQuota")) { 
      grouperAdobeGroup.setLicenseQuota(targetGroup.retrieveAttributeValueLong("licenseQuota"));
    }
    
    return grouperAdobeGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  private Long id;
  private String name;
  private String type;
  private String productName;
  private Long memberCount;
  private Long licenseQuota;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  
  public Long getId() {
    return id;
  }

  
  public void setId(Long id) {
    this.id = id;
  }

  
  public String getType() {
    return type;
  }

  
  public void setType(String type) {
    this.type = type;
  }

  
  public String getProductName() {
    return productName;
  }

  
  public void setProductName(String productName) {
    this.productName = productName;
  }

  
  public Long getMemberCount() {
    return memberCount;
  }

  
  public void setMemberCount(Long memberCount) {
    this.memberCount = memberCount;
  }

  
  public Long getLicenseQuota() {
    return licenseQuota;
  }

  
  public void setLicenseQuota(Long licenseQuota) {
    this.licenseQuota = licenseQuota;
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperAdobeGroup fromJson(JsonNode groupNode) {
    
    GrouperAdobeGroup grouperAdobeGroup = new GrouperAdobeGroup();
    grouperAdobeGroup.type = GrouperUtil.jsonJacksonGetString(groupNode, "type");
    grouperAdobeGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "groupName");
    grouperAdobeGroup.productName = GrouperUtil.jsonJacksonGetString(groupNode, "productName");
    
    grouperAdobeGroup.id = GrouperUtil.jsonJacksonGetLong(groupNode, "groupId");
    grouperAdobeGroup.memberCount = GrouperUtil.jsonJacksonGetLong(groupNode, "memberCount");
    
    String licenseQuota = GrouperUtil.jsonJacksonGetString(groupNode, "licenseQuota");
    if (StringUtils.isBlank(licenseQuota)) {
      grouperAdobeGroup.licenseQuota = null;
    } else if (StringUtils.equals(licenseQuota, "UNLIMITED")) {
      grouperAdobeGroup.licenseQuota = -1L;
    } else {
      grouperAdobeGroup.licenseQuota = Long.valueOf(licenseQuota);
    }
    
    return grouperAdobeGroup;
  }

}
