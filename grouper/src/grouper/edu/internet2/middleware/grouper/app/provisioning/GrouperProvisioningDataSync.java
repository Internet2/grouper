package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.Set;

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

  private Set<GcGrouperSyncGroup> gcGrouperSyncGroups = new HashSet<GcGrouperSyncGroup>();
  
  private Set<GcGrouperSyncMember> gcGrouperSyncMembers = new HashSet<GcGrouperSyncMember>();
  
  private Set<GcGrouperSyncMembership> gcGrouperSyncMemberships = new HashSet<GcGrouperSyncMembership>();

  
  public Set<GcGrouperSyncGroup> getGcGrouperSyncGroups() {
    return gcGrouperSyncGroups;
  }

    
  public Set<GcGrouperSyncMember> getGcGrouperSyncMembers() {
    return gcGrouperSyncMembers;
  }


  
  public Set<GcGrouperSyncMembership> getGcGrouperSyncMemberships() {
    return gcGrouperSyncMemberships;
  }
  
}
