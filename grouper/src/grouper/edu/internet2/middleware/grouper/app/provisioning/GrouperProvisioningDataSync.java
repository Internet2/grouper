package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * grouper sync objects retrieved from database on first retrieve
 * @author mchyzer
 *
 */
public class GrouperProvisioningDataSync {

  public GrouperProvisioningDataSync() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  private List<GcGrouperSyncGroup> gcGrouperSyncGroups = null;
  
  private List<GcGrouperSyncMember> gcGrouperSyncMembers = null;
  
  private List<GcGrouperSyncMembership> gcGrouperSyncMemberships = null;

  
  public List<GcGrouperSyncGroup> getGcGrouperSyncGroups() {
    return gcGrouperSyncGroups;
  }

  
  public void setGcGrouperSyncGroups(List<GcGrouperSyncGroup> gcGrouperSyncGroups) {
    this.gcGrouperSyncGroups = gcGrouperSyncGroups;
  }

  
  public List<GcGrouperSyncMember> getGcGrouperSyncMembers() {
    return gcGrouperSyncMembers;
  }

  
  public void setGcGrouperSyncMembers(List<GcGrouperSyncMember> gcGrouperSyncMembers) {
    this.gcGrouperSyncMembers = gcGrouperSyncMembers;
  }

  
  public List<GcGrouperSyncMembership> getGcGrouperSyncMemberships() {
    return gcGrouperSyncMemberships;
  }
  
  public void setGcGrouperSyncMemberships(
      List<GcGrouperSyncMembership> gcGrouperSyncMemberships) {
    this.gcGrouperSyncMemberships = gcGrouperSyncMemberships;
  }

}
