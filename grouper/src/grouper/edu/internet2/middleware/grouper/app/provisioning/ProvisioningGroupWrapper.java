package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningGroupWrapper extends ProvisioningUpdatableWrapper {

  private static Map<String, String> memberFieldToGrouperMembersColumn = GrouperUtil.toMap(
      "subjectId", "subject_id",
      "subjectIdentifier0", "subject_identifier0",
      "subjectIdentifier1", "subject_identifier1",
      "subjectIdentifier2", "subject_identifier2",
      "email", "email0"
      );

  /**
   * get a set of members from another group
   * @param groupName is the group name to check or null if this group
   * @param groupPrivilegeName admins, updaters, etc
   * @param memberField subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, email
   * @return set of values of subjects in the subject source of the provisioner
   */
  public Set<String> groupPrivilegeHolders(String groupName, String groupPrivilegeName, String memberField) {
    if (StringUtils.isBlank(groupName)) {
      if (this.getGrouperProvisioningGroup() != null) {
        groupName = this.getGrouperProvisioningGroup().getName();
      }
    }
    if (StringUtils.isBlank(groupName)) {
      return null;
    }
    return this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().groupPrivilegeHolders(groupName, groupPrivilegeName, memberField, this.getGroupId());
  }

  /**
   * get a set of members from a group
   * @param groupPrivilegeName admins, updaters, etc
   * @param memberField subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, email
   * @return set of values of subjects in the subject source of the provisioner
   */
  public Set<String> groupMembers(String groupName, String memberField) {
    return this.groupPrivilegeHolders(groupName, "members", memberField);
  }

  /**
   * get a set of members from this group
   * @param memberField subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, email
   * @return set of values of subjects in the subject source of the provisioner
   */
  public Set<String> thisGroupMembers(String memberField) {
    
    return this.groupPrivilegeHolders(null, "members", memberField);
  }

  /**
   * get a set of privilege holders from this group
   * @param groupPrivilegeName admins, updaters, etc
   * @param memberField subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, email
   * @return set of values of subjects in the subject source of the provisioner
   */
  public Set<String> thisGroupPrivilegeHolders(String groupPrivilegeName, String memberField) {

//    String column = memberFieldToGrouperMembersColumn.get(memberField);
//    if (StringUtils.isBlank(column)) {
//      throw new RuntimeException("Cant find memberField '" + memberField + "', should be one of: " + GrouperUtil.toStringForLog(memberFieldToGrouperMembersColumn.keySet()));
//    }
//
//    String sql = "select gm." + column + " from grouper_memberships_lw_v gmlv, grouper_members gm " +
//        " where gmlv.member_id = gm.id and gmlv.group_id = ? and gmlv.list_name = ? ";
//    
//    Set<String> subjectSources = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getSubjectSourcesToProvision());
//    GcDbAccess gcDbAccess = new GcDbAccess();
//    
//    gcDbAccess.addBindVar(this.getGroupId());
//    gcDbAccess.addBindVar(groupPrivilegeName);
//    
//    if (GrouperUtil.length(subjectSources) > 0) {
//      sql += "and gm.subject_source in (" + GrouperClientUtils.appendQuestions(subjectSources.size()) + ")";
//      for (String subjectSourceId : subjectSources) {
//        gcDbAccess.addBindVar(subjectSourceId);
//      }
//    }
//
//    return new HashSet<String>(gcDbAccess.sql(sql).selectList(String.class));
//    
    return this.groupPrivilegeHolders(null, groupPrivilegeName, memberField);
  }
  
  private ProvisioningStateGroup provisioningStateGroup = new ProvisioningStateGroup();


  public ProvisioningStateGroup getProvisioningStateGroup() {
    return provisioningStateGroup;
  }

  private boolean grouperTargetGroupFromCacheInitted = false;
  private ProvisioningGroup grouperTargetGroupFromCache;

  //TODO finish this for cached objects
  public ProvisioningGroup getGrouperTargetGroupFromCache() {
    if (grouperTargetGroupFromCacheInitted 
        || this.gcGrouperSyncGroup == null || this.getGrouperProvisioner() == null) {
      return grouperTargetGroupFromCache;
    }
    
    // see if there is an object cached
    for (GrouperProvisioningConfigurationAttributeDbCache cache :
      this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
      if (cache == null 
          || cache.getSource() != GrouperProvisioningConfigurationAttributeDbCacheSource.grouper 
          || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
        continue;
      }
      
    }
    return grouperTargetGroupFromCache;
  }

  private boolean targetProvisioningGroupFromCacheInitted = false;
  private ProvisioningGroup targetProvisioningGroupFromCache;

  
    
  public ProvisioningGroup getTargetProvisioningGroupFromCache() {
    return targetProvisioningGroupFromCache;
  }

  private String groupId;
  
  
  
  
  public String getGroupId() {
    return groupId;
  }




  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  private String syncGroupId;
  
  


  
  public String getSyncGroupId() {
    return syncGroupId;
  }




  
  public void setSyncGroupId(String syncGroupId) {
    this.syncGroupId = syncGroupId;
  }




  public ProvisioningGroupWrapper() {
    super();
    this.provisioningStateGroup.setProvisioningGroupWrapper(this);
  }

  /**
   * this is the representation of grouper side that grouper retrieves from its database 
   */
  private ProvisioningGroup grouperProvisioningGroup;

  /**
   * this is what is retrieved from the target and structured in the target representation
   */
  private ProvisioningGroup targetProvisioningGroup;
  
  /**
   * grouper side translated for target
   */
  private ProvisioningGroup grouperTargetGroup;

  /**
   * this comes from the commands class and is target specific bean
   */
  private Object targetNativeGroup;
  
  private GcGrouperSyncGroup gcGrouperSyncGroup;

  
  public ProvisioningGroup getGrouperProvisioningGroup() {
    return grouperProvisioningGroup;
  }

  private void calculateGroupId() {
    this.groupId = null;
    if (this.grouperProvisioningGroup != null) {
      this.groupId = this.grouperProvisioningGroup.getId();
    } else if (this.gcGrouperSyncGroup != null) {
      this.groupId = this.gcGrouperSyncGroup.getGroupId();
    }
  }
  
  public void setGrouperProvisioningGroup(ProvisioningGroup grouperProvisioningGroup) {
    
    if (this.grouperProvisioningGroup == grouperProvisioningGroup) {
      return;
    }
    
    ProvisioningGroup oldGrouperProvisioningGroup = this.grouperProvisioningGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldGrouperProvisioningGroup == null ? null : oldGrouperProvisioningGroup.getProvisioningGroupWrapper();

    this.grouperProvisioningGroup = grouperProvisioningGroup;
    
    if (this.grouperProvisioningGroup != null) {
      this.grouperProvisioningGroup.setProvisioningGroupWrapper(this);
    }

    if (oldGrouperProvisioningGroup != null) {
      oldGrouperProvisioningGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.grouperProvisioningGroup = null;
    }
    this.calculateGroupId();
  }

  
  public ProvisioningGroup getTargetProvisioningGroup() {
    return targetProvisioningGroup;
  }

  
  public void setTargetProvisioningGroup(ProvisioningGroup targetProvisioningGroup) {
    
    if (this.targetProvisioningGroup == targetProvisioningGroup) {
      return;
    }
    
    ProvisioningGroup oldTargetProvisioningGroup = this.targetProvisioningGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldTargetProvisioningGroup == null ? null : oldTargetProvisioningGroup.getProvisioningGroupWrapper();

    this.targetProvisioningGroup = targetProvisioningGroup;
    
    if (this.targetProvisioningGroup != null) {
      
      ProvisioningGroupWrapper newTargetGroupOldWrapper = this.targetProvisioningGroup.getProvisioningGroupWrapper();
      
      this.targetProvisioningGroup.setProvisioningGroupWrapper(this);
      
      if (newTargetGroupOldWrapper != null && newTargetGroupOldWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
        this.getProvisioningStateGroup().setSelectResultProcessed(true);
      }
      if (newTargetGroupOldWrapper != null && newTargetGroupOldWrapper.getProvisioningStateGroup().isSelectAllMembershipsResultProcessed()) {
        this.getProvisioningStateGroup().setSelectAllMembershipsResultProcessed(true);
      }
      
    }

    if (oldTargetProvisioningGroup != null && oldTargetProvisioningGroup != this.targetProvisioningGroup) {
      oldTargetProvisioningGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.targetProvisioningGroup = null;
    }

  }

  
  public ProvisioningGroup getGrouperTargetGroup() {
    return grouperTargetGroup;
  }

  
  public void setGrouperTargetGroup(ProvisioningGroup grouperTargetGroup) {
    
    if (this.grouperTargetGroup == grouperTargetGroup) {
      return;
    }
    
    ProvisioningGroup oldGrouperTargetGroup = this.grouperTargetGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldGrouperTargetGroup == null ? null : oldGrouperTargetGroup.getProvisioningGroupWrapper();

    this.grouperTargetGroup = grouperTargetGroup;
    
    if (this.grouperTargetGroup != null) {
      this.grouperTargetGroup.setProvisioningGroupWrapper(this);
    }

    if (oldGrouperTargetGroup != null) {
      oldGrouperTargetGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.grouperTargetGroup = null;
    }
  }

  
  public Object getTargetNativeGroup() {
    return targetNativeGroup;
  }

  
  public void setTargetNativeGroup(Object targetNativeGroup) {
    this.targetNativeGroup = targetNativeGroup;
  }

  
  public GcGrouperSyncGroup getGcGrouperSyncGroup() {
    return gcGrouperSyncGroup;
  }

  
  public void setGcGrouperSyncGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    this.gcGrouperSyncGroup = gcGrouperSyncGroup;
    if (this.gcGrouperSyncGroup != null) {
      this.syncGroupId = this.getGcGrouperSyncGroup().getId();
    }
    this.calculateGroupId();

  }
  
  public String toString() {
    return "GroupWrapper@" + Integer.toHexString(hashCode());
  }
  
  public String toStringForError() {
    
    if (this.grouperTargetGroup != null) {
      return "grouperTargetGroup: " + this.grouperTargetGroup;
    }

    if (this.grouperProvisioningGroup != null) {
      return "grouperProvisioningGroup: " + this.grouperProvisioningGroup;
    }

    if (this.targetProvisioningGroup != null) {
      return "targetProvisioningGroup: " + this.targetProvisioningGroup;
    }
    
    if (this.provisioningStateGroup != null) {
      return "provisioningStateGroup: " + this.provisioningStateGroup;
    }

    return this.toString();
  }

  public String toStringForErrorVerbose() {
    
    StringBuilder result = new StringBuilder();
    
    if (this.grouperTargetGroup != null) {
      result.append("grouperTargetGroup: " + this.grouperTargetGroup + ", ");
    }

    if (this.grouperProvisioningGroup != null) {
      result.append("grouperProvisioningGroup: " + this.grouperProvisioningGroup + ", ");
    }

    if (this.targetProvisioningGroup != null) {
      result.append("targetProvisioningGroup: " + this.targetProvisioningGroup + ", ");
    }
    
    if (this.provisioningStateGroup != null) {
      result.append("provisioningStateGroup: " + this.provisioningStateGroup + ", ");
    }

    return this.toString();
  }

  @Override
  public String objectTypeName() {
    return "group";
  }
  

}
