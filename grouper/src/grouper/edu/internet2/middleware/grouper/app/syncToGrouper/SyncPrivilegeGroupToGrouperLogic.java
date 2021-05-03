package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.PrivilegeGroupSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * 
 * @author mchyzer
 *
 */
public class SyncPrivilegeGroupToGrouperLogic {

  /**
   * no priv group to sync message
   */
  public static final String NO_PRIVILEGE_GROUPS_TO_SYNC = "There are no group privileges to sync";

  /**
   * priv group sync false message
   */
  public static final String PRIVILEGE_GROUP_SYNC_FALSE = "Group privileges are skipped since the groupPrivilegeSync behavior is false";
  
  private SyncToGrouper syncToGrouper = null;

  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }

  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }


  public SyncPrivilegeGroupToGrouperLogic() {
    super();
  }


  public SyncPrivilegeGroupToGrouperLogic(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }

  /**
   * set of privileges (either groupName, sourceId, subjectId (non groups), field name, or groupName, sourceId, subjectIdentifier (groups), field name)
   */
  private Map<MultiKey, SyncPrivilegeGroupToGrouperBean> grouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembership = new HashMap<MultiKey, SyncPrivilegeGroupToGrouperBean>();

  /**
   * set of privileges
   * @return multikeys
   */
  public Map<MultiKey, SyncPrivilegeGroupToGrouperBean> getGrouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembershipembership() {
    return grouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembership;
  }


  /**
   * 
   */
  public void syncLogic() {
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isSqlLoad()) {
      this.getSyncToGrouper().getSyncToGrouperFromSql().loadPrivilegeGroupDataFromSql();
    }
    
    this.retrievePrivilegeGroupsFromGrouper();

    this.comparePrivilegeGroups();

    this.changeGrouper();

    // reclaim some memory
    this.getSyncToGrouper().getSyncToGrouperReport().addTotalCount(GrouperUtil.length(this.getSyncToGrouper().getSyncPrivilegeGroupToGrouperBeans()));
    this.getSyncToGrouper().getSyncToGrouperReport().addTotalCount(GrouperUtil.length(this.getGrouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembershipembership()));
    if (SyncToGrouper.reclaimMemory) {
      this.getSyncToGrouper().setSyncPrivilegeGroupToGrouperBeans(null);
      this.grouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembership = null;
    }
  }


  private void changeGrouper() {

    this.getSyncToGrouper().getSyncToGrouperReport().setState("changeGrouperPrivilegeGroup");
    
    if (!this.syncToGrouper.isReadWrite()) {
      return;
    }

    for (SyncPrivilegeGroupToGrouperBean syncPrivilegeGroupToGrouperBean : GrouperUtil.nonNull(this.privilegeGroupDeletes)) {
      String privilegeGroupLabel = syncPrivilegeGroupToGrouperBean.convertToLabel();
      try {
        
        GrouperUtil.assertion(!StringUtils.isBlank(syncPrivilegeGroupToGrouperBean.getImmediateMembershipId()), "Privilege id is required");
        
        PrivilegeGroupSave privilegeGroupSave = syncPrivilegeGroupToGrouperBean.convertToPrivilegeGroupSave().assignSaveMode(SaveMode.DELETE);
        privilegeGroupSave.save();
        
        if (privilegeGroupSave.getSaveResultType().isChanged()) {
          this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
          this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success deleting " + privilegeGroupLabel);
        }
        
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error deleting " + privilegeGroupLabel + ", " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
    for (SyncPrivilegeGroupToGrouperBean syncPrivilegeGroupToGrouperBean : GrouperUtil.nonNull(this.privilegeGroupInserts)) {
      
      String privilegeGroupLabel = syncPrivilegeGroupToGrouperBean.convertToLabel();
      try {
        PrivilegeGroupSave privilegeGroupSave = syncPrivilegeGroupToGrouperBean.convertToPrivilegeGroupSave();
        privilegeGroupSave.save();
        if (privilegeGroupSave.getSaveResultType().isChanged()) {
          this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
          this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success inserting " + privilegeGroupLabel);
          
        }
      } catch (SubjectNotFoundException snfe) {
        this.syncToGrouper.getSyncToGrouperReport().addSubjectNotFound();
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting " + privilegeGroupLabel + ", " + snfe.getMessage());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting " + privilegeGroupLabel + ", " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
    // theres no such thing as privilege updates
  }

  /**
   * 
   */
  private List<SyncPrivilegeGroupToGrouperBean> privilegeGroupInserts = new ArrayList<SyncPrivilegeGroupToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncPrivilegeGroupToGrouperBean> getPrivilegeGroupInserts() {
    return privilegeGroupInserts;
  }

  private void comparePrivilegeGroups() {

    this.getSyncToGrouper().getSyncToGrouperReport().setState("comparePrivilegeGroups");
    
    if (!this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupSync()) {
      this.syncToGrouper.getSyncToGrouperReport().addOutputLine(PRIVILEGE_GROUP_SYNC_FALSE);
      return;
    }
      
    Map<MultiKey, SyncPrivilegeGroupToGrouperBean> privilegeGroupMultiKeysToSyncBeansIncoming = new HashMap<MultiKey, SyncPrivilegeGroupToGrouperBean>();

    for (SyncPrivilegeGroupToGrouperBean syncPrivilegeGroupToGrouperBean : this.syncToGrouper.getSyncPrivilegeGroupToGrouperBeans()) {
      privilegeGroupMultiKeysToSyncBeansIncoming.put(syncPrivilegeGroupToGrouperBean.convertToMultikey(), syncPrivilegeGroupToGrouperBean);
    }

    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupInsert()) {
      
      Set<MultiKey> membershipsToInsert = new HashSet<MultiKey>();
      membershipsToInsert.addAll(privilegeGroupMultiKeysToSyncBeansIncoming.keySet());
      
      membershipsToInsert.removeAll(grouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.keySet());
      for (MultiKey multiKeyToInsert : membershipsToInsert) {
        this.privilegeGroupInserts.add(privilegeGroupMultiKeysToSyncBeansIncoming.get(multiKeyToInsert));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupDeleteExtra()) {
      Set<MultiKey> membershipsToDelete = new HashSet<MultiKey>();
      
      membershipsToDelete.addAll(grouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.keySet());
      
      membershipsToDelete.removeAll(privilegeGroupMultiKeysToSyncBeansIncoming.keySet());
      for (MultiKey multiKeyToDelete : membershipsToDelete) {
        this.privilegeGroupDeletes.add(grouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.get(multiKeyToDelete));
      }
    }    

    // theres no such thing as privilege updates right now
  }

  private void retrievePrivilegeGroupsFromGrouper() {

    this.getSyncToGrouper().getSyncToGrouperReport().setState("retrievePrivilegeGroupsFromGrouper");
    
    StringBuilder thePrivilegeGroupSqlBase = new StringBuilder(
        // we dont need immediate membership id
        "SELECT gmav.immediate_membership_id as immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, (select gg2.name from grouper_groups gg2 where gm.subject_source='g:gsa' and gg2.id = gm.subject_id) as subject_identifier, gf.name field_name");
    
    thePrivilegeGroupSqlBase.append(" FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf "
        + "WHERE gmav.mship_type = 'immediate'");
    
    thePrivilegeGroupSqlBase.append(" AND gmav.immediate_mship_enabled = 'T'");

    thePrivilegeGroupSqlBase.append(" AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'access'");
          
    List<Object[]> membershipRows = new ArrayList<Object[]>();
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupSyncFromStems()) {

      GrouperUtil.assertion(GrouperUtil.length(this.syncToGrouper.getTopLevelStemNamesFlattenedFromSqlOrInput()) > 0, 
          "If syncing grouper and folders then the top level folders are required or : for all");
      
      if (GrouperUtil.length(this.getSyncToGrouper().getTopLevelStemsFlattenedFromSqlOrInput()) == 0) {
        return;
      }

      GcDbAccess gcDbAccess = new GcDbAccess();
      if (!this.getSyncToGrouper().isTopLevelStemsHaveRoot()) {

        // get all the parent stems
        Set<Stem> topLevelStems = this.syncToGrouper.getTopLevelStemsFlattenedFromSqlOrInput();
  
        GrouperUtil.assertion(GrouperUtil.length(topLevelStems) < 400, "Cannot have more than 400 top level stems to sync");

        thePrivilegeGroupSqlBase.append(" and ( ");
        boolean addedOne = false;
        List<Object> bindVars = new ArrayList<Object>();

        for (Stem topLevelStem : topLevelStems) {
          if (addedOne) {
            thePrivilegeGroupSqlBase.append(" or ");
          }
          addedOne = true;
          // children
          thePrivilegeGroupSqlBase.append("gg.name like ?");
          bindVars.add(topLevelStem.getName() + ":%");
        }

        thePrivilegeGroupSqlBase.append(" ) ");
        gcDbAccess.bindVars(bindVars);
      }

      gcDbAccess.sql(thePrivilegeGroupSqlBase.toString());
  
      membershipRows.addAll(gcDbAccess.selectList(Object[].class));
        
    } else {

      List<SyncPrivilegeGroupToGrouperBean> syncPrivilegeGroupToGrouperBeans = this.syncToGrouper.getSyncPrivilegeGroupToGrouperBeans();

      if (GrouperUtil.length(syncPrivilegeGroupToGrouperBeans) == 0) {
        return;
      }
        
      Set<String> groupNameSet = new TreeSet<String>();
      for (SyncPrivilegeGroupToGrouperBean syncPrivilegeGroupToGrouperBean : syncPrivilegeGroupToGrouperBeans) {
        GrouperUtil.assertion(!StringUtils.isBlank(syncPrivilegeGroupToGrouperBean.getGroupName()), "Group name cannot be null!");
        groupNameSet.add(syncPrivilegeGroupToGrouperBean.getGroupName());
      }

      List<String> groupNameList = new ArrayList<String>(groupNameSet);
      
      int batchSize = 800;

      int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupNameList, batchSize);

      for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
        
        List<String> batchGroupNames = GrouperUtil.batchList(groupNameList, batchSize, batchIndex);

        StringBuilder theMembershipSql = new StringBuilder(thePrivilegeGroupSqlBase);
        theMembershipSql.append(" and ( ");
  
        boolean addedOne = false;
        GcDbAccess gcDbAccess = new GcDbAccess();
        List<Object> bindVars = new ArrayList<Object>();
  
        for (String batchGroupName : GrouperUtil.nonNull(batchGroupNames)) {
          if (addedOne) {
            theMembershipSql.append(" or ");
          }
          addedOne = true;
          // the exact name
          theMembershipSql.append("gg.name = ?");
          bindVars.add(batchGroupName);
        }
        theMembershipSql.append(" ) ");
        gcDbAccess.sql(theMembershipSql.toString()).bindVars(bindVars);
  
        membershipRows.addAll(gcDbAccess.selectList(Object[].class));
        
      }
        
    }
    
    for (Object[] membershipRow : membershipRows) {
      
      SyncPrivilegeGroupToGrouperBean syncPrivilegeGroupToGrouperBean = new SyncPrivilegeGroupToGrouperBean();
      syncPrivilegeGroupToGrouperBean.assignImmediateMembershipId((String)membershipRow[0]);
      syncPrivilegeGroupToGrouperBean.assignGroupName((String)membershipRow[1]);
      syncPrivilegeGroupToGrouperBean.assignSubjectSourceId((String)membershipRow[2]);
      syncPrivilegeGroupToGrouperBean.assignSubjectId((String)membershipRow[3]);
      syncPrivilegeGroupToGrouperBean.assignSubjectIdentifier((String)membershipRow[4]);
      syncPrivilegeGroupToGrouperBean.assignFieldName((String)membershipRow[5]);
      MultiKey groupNameSubjectSourceIdSubjectIdOrIdentifier = syncPrivilegeGroupToGrouperBean.convertToMultikey();
      
      this.grouperGroupNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.put(
          groupNameSubjectSourceIdSubjectIdOrIdentifier, syncPrivilegeGroupToGrouperBean);
      
    }

  }


  /**
   * privilege group deletes
   */
  private List<SyncPrivilegeGroupToGrouperBean> privilegeGroupDeletes = new ArrayList<SyncPrivilegeGroupToGrouperBean>();


  /**
   * privilege group deletes
   * @return
   */
  public List<SyncPrivilegeGroupToGrouperBean> getPrivilegeGroupDeletes() {
    return privilegeGroupDeletes;
  }

}
