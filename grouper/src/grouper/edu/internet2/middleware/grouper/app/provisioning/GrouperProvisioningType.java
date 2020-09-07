/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * type of table sync
 */
public enum GrouperProvisioningType {
  
  /**
   * full sync everything
   */
  fullProvisionFull {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }
    
    @Override
    public boolean isFullMetadataSync() {
      return false;
    }


    @Override
    public boolean isIncrementalSync() {
      return false;
    }

    @Override
    protected void retrieveData(GrouperProvisioner grouperProvisioner) {
      grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveAllData();
    }

    @Override
    public Map<String, GcGrouperSyncGroup> retrieveSyncGroups(GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveAllSyncGroups();
    }

    @Override
    public Map<String, GcGrouperSyncMember> retrieveSyncMembers(GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveAllSyncMembers();
    }

    @Override
    public Map<MultiKey, GcGrouperSyncMembership> retrieveSyncMemberships(GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveAllSyncMemberships();
    }

    @Override
    public List<ProvisioningGroup> retrieveGrouperGroups(
        GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveGroups(true, null);
    }

    @Override
    public List<ProvisioningEntity> retrieveGrouperMembers(
        GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveMembers(true, null);
    }

    @Override
    public List<ProvisioningMembership> retrieveGrouperMemberships(
        GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveMemberships(true, null);
    }
    
  },
  incrementalProvisionChangeLog {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
    
    @Override
    public boolean isFullMetadataSync() {
      return false;
    }


    @Override
    public boolean isIncrementalSync() {
      return true;
    }

    @Override
    protected void retrieveData(GrouperProvisioner grouperProvisioner) {
      grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIncrementalData();
    }

    @Override
    public Map<String, GcGrouperSyncGroup> retrieveSyncGroups(GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveIncrementalSyncGroups();
    }

    @Override
    public Map<String, GcGrouperSyncMember> retrieveSyncMembers(GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveIncrementalSyncMembers();
    }

    @Override
    public Map<MultiKey, GcGrouperSyncMembership> retrieveSyncMemberships(GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveIncrementalSyncMemberships();
    }
    
    @Override
    public List<ProvisioningGroup> retrieveGrouperGroups(
        GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveGroups(false, grouperProvisioner.retrieveGrouperDao().incrementalGroupUuids());
    }

    @Override
    public List<ProvisioningEntity> retrieveGrouperMembers(
        GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveMembers(false, grouperProvisioner.retrieveGrouperDao().incrementalMemberUuids());
    }

    @Override
    public List<ProvisioningMembership> retrieveGrouperMemberships(
        GrouperProvisioner grouperProvisioner) {
      return grouperProvisioner.retrieveGrouperDao().retrieveMemberships(false, grouperProvisioner.retrieveGrouperDao().incrementalGroupUuidsMemberUuids());
    }

  };

  /**
   * see if full metadata sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullMetadataSync();

  /**
   * see if full sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullSync();

  /**
   * see if incremental sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isIncrementalSync();

 
  public abstract Map<String, GcGrouperSyncGroup> retrieveSyncGroups(GrouperProvisioner grouperProvisioner);

  public abstract Map<String, GcGrouperSyncMember> retrieveSyncMembers(GrouperProvisioner grouperProvisioner);

  public abstract Map<MultiKey, GcGrouperSyncMembership> retrieveSyncMemberships(GrouperProvisioner grouperProvisioner);

  public abstract List<ProvisioningGroup> retrieveGrouperGroups(GrouperProvisioner grouperProvisioner);

  public abstract List<ProvisioningEntity> retrieveGrouperMembers(GrouperProvisioner grouperProvisioner);

  public abstract List<ProvisioningMembership> retrieveGrouperMemberships(GrouperProvisioner grouperProvisioner);


  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningType.class, string, exceptionOnNull);
  }

  /**
   * 
   * @param grouperProvisioner
   */
  protected abstract void retrieveData(GrouperProvisioner grouperProvisioner);
}
