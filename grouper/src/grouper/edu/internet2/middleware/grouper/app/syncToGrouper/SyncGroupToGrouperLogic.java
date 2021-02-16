package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class SyncGroupToGrouperLogic {

  /**
   * no groups to sync message
   */
  public static final String NO_GROUPS_TO_SYNC = "There are no groups to sync";

  /**
   * group sync false message
   */
  public static final String GROUP_SYNC_FALSE = "Groups are skipped since the groupSync behavior is false";
  
  private SyncToGrouper syncToGrouper = null;

  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }

  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }


  public SyncGroupToGrouperLogic() {
    super();
  }


  public SyncGroupToGrouperLogic(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }
  
  /**
   * map of stem name to stem
   */
  private Map<String, Group> grouperGroupNameToGroup = new TreeMap<String, Group>();

  /**
   * map of stem name to stem
   * @return the map of stem name to stem
   */
  public Map<String, Group> getGrouperGroupNameToGroup() {
    return this.grouperGroupNameToGroup;
  }

  /**
   * map of stem uuid to stem
   * @return
   */
  public Map<String, Group> getGrouperGroupUuidToGroup() {
    return this.grouperGroupUuidToGroup;
  }

  /**
   * map of stem uuid to stem
   */
  private Map<String, Group> grouperGroupUuidToGroup = new TreeMap<String, Group>();
  
  /**
   * 
   */
  public void syncLogic() {
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isSqlLoad()) {
      this.getSyncToGrouper().getSyncToGrouperFromSql().loadGroupDataFromSql();
    }
    
    this.retrieveGroupsFromGrouper();

    this.compareGroups();

    this.changeGrouper();

  }


  private void changeGrouper() {
    
    if (!this.syncToGrouper.isReadWrite()) {
      return;
    }

    for (Group group : GrouperUtil.nonNull(this.groupDeletes)) {
      
      // get this again to reduce race conditions
      Group groupInGrouper = GroupFinder.findByName(GrouperSession.staticGrouperSession(), group.getName(), false);
      if (groupInGrouper == null) {
        continue;
      }

      try {
        groupInGrouper.delete();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success deleting group '" + group.getName() + "'");
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error deleting folder '" + group.getName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    for (SyncGroupToGrouperBean syncGroupToGrouperBean : GrouperUtil.nonNull(this.groupInserts)) {
      
      try {
        GroupSave groupSave = new GroupSave(GrouperSession.staticGrouperSession()).assignName(syncGroupToGrouperBean.getName()).assignCreateParentStemsIfNotExist(true);

        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldAlternateName()
            && !StringUtils.isBlank(syncGroupToGrouperBean.getAlternateName())) {
          groupSave.assignAlternateName(syncGroupToGrouperBean.getAlternateName());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDescription()) {
          groupSave.assignDescription(syncGroupToGrouperBean.getDescription());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisabledTimestamp()) {
          groupSave.assignDisabledTime(syncGroupToGrouperBean.getDisabledTimestamp());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisplayName()
            && !StringUtils.isBlank(syncGroupToGrouperBean.getDisplayName())) {
          groupSave.assignDisplayName(syncGroupToGrouperBean.getDisplayName());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldEnabledTimestamp()) {
          groupSave.assignEnabledTime(syncGroupToGrouperBean.getEnabledTimestamp());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdOnInsert() && !StringUtils.isBlank(syncGroupToGrouperBean.getId())) {
          groupSave.assignUuid(syncGroupToGrouperBean.getId());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdIndexOnInsert()
            && syncGroupToGrouperBean.getIdIndex() != null) {
          groupSave.assignIdIndex(syncGroupToGrouperBean.getIdIndex());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldTypeOfGroup()) {
          groupSave.assignTypeOfGroup(TypeOfGroup.valueOfIgnoreCase(syncGroupToGrouperBean.getTypeOfGroup(), false));
        }
        
        groupSave.save();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success inserting group '" + syncGroupToGrouperBean.getName());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting group '" + syncGroupToGrouperBean.getName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    for (SyncGroupToGrouperBean syncGroupToGrouperBean : GrouperUtil.nonNull(this.groupUpdates)) {
      
      // get this again to reduce race conditions
      Group groupInGrouper = GroupFinder.findByName(GrouperSession.staticGrouperSession(), syncGroupToGrouperBean.getName(), false);
      if (groupInGrouper == null) {
        continue;
      }
      
      try {
        GroupSave groupSave = new GroupSave(GrouperSession.staticGrouperSession()).assignName(syncGroupToGrouperBean.getName());
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldAlternateName()) {
          groupSave.assignAlternateName(syncGroupToGrouperBean.getAlternateName());
        } else {
          groupSave.assignAlternateName(groupInGrouper.getAlternateName());
        }

        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDescription()) {
          groupSave.assignDescription(syncGroupToGrouperBean.getDescription());
        } else {
          groupSave.assignDescription(groupInGrouper.getDescription());
        }
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisabledTimestamp()) {
          groupSave.assignDisabledTimestamp(GrouperUtil.toTimestamp(syncGroupToGrouperBean.getDisabledTimestamp()));
        } else {
          groupSave.assignDisabledTimestamp(groupInGrouper.getDisabledTime());
        }

        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisplayName() && !StringUtils.isBlank(syncGroupToGrouperBean.getDisplayName())) {
          groupSave.assignDisplayName(syncGroupToGrouperBean.getDisplayName());
        } else {
          groupSave.assignDisplayName(groupInGrouper.getDisplayName());
        }
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldEnabledTimestamp()) {
          groupSave.assignEnabledTimestamp(GrouperUtil.toTimestamp(syncGroupToGrouperBean.getEnabledTimestamp()));
        } else {
          groupSave.assignEnabledTimestamp(groupInGrouper.getEnabledTime());
        }

        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldTypeOfGroup()) {
          groupSave.assignTypeOfGroup(TypeOfGroup.valueOfIgnoreCase(syncGroupToGrouperBean.getTypeOfGroup(), false));
        } else {
          groupSave.assignTypeOfGroup(groupInGrouper.getTypeOfGroup());
        }
        groupSave.save();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success updating folder '" + syncGroupToGrouperBean.getName());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error updating folder '" + syncGroupToGrouperBean.getName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
  }

  /**
   * 
   */
  private List<SyncGroupToGrouperBean> groupInserts = new ArrayList<SyncGroupToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncGroupToGrouperBean> getGroupInserts() {
    return groupInserts;
  }

  /**
   * 
   * @param stemInserts
   */
  public void setGroupInserts(List<SyncGroupToGrouperBean> stemInserts) {
    this.groupInserts = stemInserts;
  }

  /**
   * 
   */
  private List<SyncGroupToGrouperBean> groupUpdates = new ArrayList<SyncGroupToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncGroupToGrouperBean> getGroupUpdates() {
    return groupUpdates;
  }

  /**
   * 
   * @param groupUpdates
   */
  public void setGroupUpdates(List<SyncGroupToGrouperBean> groupUpdates) {
    this.groupUpdates = groupUpdates;
  }

  private void compareGroups() {
    
    if (!this.syncToGrouper.getSyncToGrouperBehavior().isGroupSync()) {
      this.syncToGrouper.getSyncToGrouperReport().addOutputLine(GROUP_SYNC_FALSE);
      return;
    }
      
    Map<String, SyncGroupToGrouperBean> groupNamesToSyncBeans = new TreeMap<String, SyncGroupToGrouperBean>();
    
    for (SyncGroupToGrouperBean syncGroupToGrouperBean : this.syncToGrouper.getSyncGroupToGrouperBeans()) {
      groupNamesToSyncBeans.put(syncGroupToGrouperBean.getName(), syncGroupToGrouperBean);
    }

    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupInsert()) {
      Set<String> groupNamesToInsert = new TreeSet<String>();
      
      groupNamesToInsert.addAll(groupNamesToSyncBeans.keySet());
      
      groupNamesToInsert.removeAll(this.grouperGroupNameToGroup.keySet());
      
      for (String groupName : groupNamesToInsert) {
        this.groupInserts.add(groupNamesToSyncBeans.get(groupName));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupDeleteExtra()) {
      Set<String> groupNamesToDelete = new TreeSet<String>();
      
      groupNamesToDelete.addAll(this.grouperGroupNameToGroup.keySet());
      
      groupNamesToDelete.removeAll(groupNamesToSyncBeans.keySet());
      
      for (String groupName : groupNamesToDelete) {
        this.groupDeletes.add(this.grouperGroupNameToGroup.get(groupName));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupUpdate()) {
      Set<String> groupNamesToUpdate = new TreeSet<String>();
      
      groupNamesToUpdate.addAll(groupNamesToSyncBeans.keySet());
      
      groupNamesToUpdate.retainAll(this.grouperGroupNameToGroup.keySet());
      
      for (String groupName : groupNamesToUpdate) {
        
        Group groupInGrouper = this.grouperGroupNameToGroup.get(groupName);
        SyncGroupToGrouperBean groupToUpdate = groupNamesToSyncBeans.get(groupName);
        
        boolean needsUpdate = false;
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldAlternateName()
            && !StringUtils.equals(StringUtils.trimToNull(groupInGrouper.getAlternateName()), 
                StringUtils.trimToNull(groupToUpdate.getAlternateName()))) {
          needsUpdate = true;
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDescription()
            && !StringUtils.equals(StringUtils.trimToNull(groupInGrouper.getDescription()), StringUtils.trimToNull(groupToUpdate.getDescription()))) {
          needsUpdate = true;
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisabledTimestamp()
            && !GrouperUtil.equals(groupInGrouper.getDisabledTimeDb(), groupToUpdate.getDisabledTimestamp())) {
          needsUpdate = true;
        }
        // only check the extension since if an ancestor display name changes it is not our purview
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisplayName()
            && !StringUtils.isBlank(groupToUpdate.getDisplayName())
            && !StringUtils.equals(StringUtils.trimToNull(GrouperUtil.extensionFromName(groupInGrouper.getDisplayName())), 
                StringUtils.trimToNull(GrouperUtil.extensionFromName(groupToUpdate.getDisplayName())))) {
          needsUpdate = true;
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldEnabledTimestamp()
            && !GrouperUtil.equals(groupInGrouper.getEnabledTimeDb(), groupToUpdate.getEnabledTimestamp())) {
          needsUpdate = true;
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldTypeOfGroup()
            && !GrouperUtil.equals(groupInGrouper.getTypeOfGroupDb(), StringUtils.trimToNull(groupToUpdate.getTypeOfGroup()))) {
          needsUpdate = true;
        }
        if (needsUpdate) {
          this.groupUpdates.add(groupNamesToSyncBeans.get(groupName));
        }
      }
    }    
  }

  private void retrieveGroupsFromGrouper() {

    Set<Group> groups = null;
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFromStems()) {
      
      groups = new TreeSet<Group>();
      
      // get all the parent stems
      Set<Stem> topLevelStems = this.syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemsToSync();

      if (GrouperUtil.length(topLevelStems) == 0) {
        return;
      }

      for (Stem topLevelStem : topLevelStems) {
        Set<Group> theGroups = new GroupFinder().assignParentStemId(topLevelStem.getId())
            .assignStemScope(Scope.SUB).findGroups();
        groups.addAll(GrouperUtil.nonNull(theGroups));
      }
      
    } else {
      List<SyncGroupToGrouperBean> syncGroupToGrouperBeans = this.syncToGrouper.getSyncGroupToGrouperBeans();
      Set<String> groupNames = new TreeSet<String>();
      for (SyncGroupToGrouperBean syncGroupToGrouperBean : GrouperUtil.nonNull(syncGroupToGrouperBeans)) {
        groupNames.add(syncGroupToGrouperBean.getName());
      }
      if (GrouperUtil.length(groupNames) > 0) {
        groups = GrouperDAOFactory.getFactory().getGroup().findByNamesSecure(groupNames, null);
      }
      
    }
    
    for (Group group : GrouperUtil.nonNull(groups)) {
      this.grouperGroupNameToGroup.put(group.getName(), group);
      this.grouperGroupUuidToGroup.put(group.getUuid(), group);
    }

  }

  /**
   * group deletes
   */
  private List<Group> groupDeletes = new ArrayList<Group>();

  /**
   * group deletes
   * @return
   */
  public List<Group> getGroupDeletes() {
    return groupDeletes;
  }

  /**
   * group deletes
   * @param groupDeletes
   */
  public void setGroupDeletes(List<Group> groupDeletes) {
    this.groupDeletes = groupDeletes;
  }

}
