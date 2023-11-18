package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.MembershipSave;
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
public class SyncMembershipToGrouperLogic {

  /**
   * no membership to sync message
   */
  public static final String NO_MEMBERSHIPS_TO_SYNC = "There are no memberships to sync";

  /**
   * membership sync false message
   */
  public static final String MEMBERSHIP_SYNC_FALSE = "Memberships are skipped since the membershipSync behavior is false";
  
  private SyncToGrouper syncToGrouper = null;

  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }

  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }


  public SyncMembershipToGrouperLogic() {
    super();
  }


  public SyncMembershipToGrouperLogic(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }

  /**
   * set of memberships (either groupName, sourceId, subjectId (non groups), or groupName, sourceId, subjectIdentifier (groups))
   */
  private Map<MultiKey, SyncMembershipToGrouperBean> grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership = new HashMap<MultiKey, SyncMembershipToGrouperBean>();

  /**
   * set of memberships
   * @return multikeys
   */
  public Map<MultiKey, SyncMembershipToGrouperBean> getGrouperGroupNameSourceIdSubjectIdOrIdentifierToMembership() {
    return grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership;
  }


  /**
   * 
   */
  public void syncLogic() {
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isSqlLoad()) {
      this.getSyncToGrouper().getSyncToGrouperFromSql().loadMembershipDataFromSql();
    }
    
    this.retrieveMembershipsFromGrouper();

    this.compareMemberships();

    this.changeGrouper();

    // reclaim some memory
    this.getSyncToGrouper().getSyncToGrouperReport().addTotalCount(GrouperUtil.length(this.getSyncToGrouper().getSyncMembershipToGrouperBeans()));
    this.getSyncToGrouper().getSyncToGrouperReport().addTotalCount(GrouperUtil.length(this.getGrouperGroupNameSourceIdSubjectIdOrIdentifierToMembership()));
    if (SyncToGrouper.reclaimMemory) {
      this.getSyncToGrouper().setSyncMembershipToGrouperBeans(null);
      this.grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership = null;
    }
  }


  private void changeGrouper() {

    this.getSyncToGrouper().getSyncToGrouperReport().setState("changeGrouperMemberships");
    
    if (!this.syncToGrouper.isReadWrite()) {
      return;
    }

    for (SyncMembershipToGrouperBean syncMembershipToGrouperBean : GrouperUtil.nonNull(this.membershipDeletes)) {
      String membershipLabel = syncMembershipToGrouperBean.convertToLabel();
      try {
        
        GrouperUtil.assertion(!StringUtils.isBlank(syncMembershipToGrouperBean.getImmediateMembershipId()), "Membership id is required");
        
        MembershipSave membershipSave = syncMembershipToGrouperBean.convertToMembershipSave().assignSaveMode(SaveMode.DELETE);
        membershipSave.save();
        
        if (membershipSave.getSaveResultType().isChanged()) {
          this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
          this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success deleting " + membershipLabel);
        }
        
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error deleting " + membershipLabel + ", " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
    for (SyncMembershipToGrouperBean syncMembershipToGrouperBean : GrouperUtil.nonNull(this.membershipInserts)) {
      
      String membershipLabel = syncMembershipToGrouperBean.convertToLabel();
      try {
        MembershipSave membershipSave = syncMembershipToGrouperBean.convertToMembershipSave();
        membershipSave.save();
        if (membershipSave.getSaveResultType().isChanged()) {
          this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
          this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success inserting " + membershipLabel);
          
        }
      } catch (SubjectNotFoundException snfe) {
        this.syncToGrouper.getSyncToGrouperReport().addSubjectNotFound();
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting " + membershipLabel + ", " + snfe.getMessage());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting " + membershipLabel + ", " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    for (SyncMembershipToGrouperBean syncMembershipToGrouperBean : GrouperUtil.nonNull(this.membershipUpdates)) {

      String membershipLabel = syncMembershipToGrouperBean.convertToLabel();
      try {
        MembershipSave membershipSave = syncMembershipToGrouperBean.convertToMembershipSave();
        membershipSave.save();
        if (membershipSave.getSaveResultType().isChanged()) {
          this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
          this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success updating " + membershipLabel);
        }
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error updating " + membershipLabel + ", " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
  }

  /**
   * 
   */
  private List<SyncMembershipToGrouperBean> membershipInserts = new ArrayList<SyncMembershipToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncMembershipToGrouperBean> getMembershipInserts() {
    return membershipInserts;
  }

  /**
   * 
   */
  private List<SyncMembershipToGrouperBean> membershipUpdates = new ArrayList<SyncMembershipToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncMembershipToGrouperBean> getMembershipUpdates() {
    return membershipUpdates;
  }

  private void compareMemberships() {

    this.getSyncToGrouper().getSyncToGrouperReport().setState("compareMemberships");
    
    if (!this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSync()) {
      this.syncToGrouper.getSyncToGrouperReport().addOutputLine(MEMBERSHIP_SYNC_FALSE);
      return;
    }
      
    Map<MultiKey, SyncMembershipToGrouperBean> membershipMultiKeysToSyncBeansIncoming = new HashMap<MultiKey, SyncMembershipToGrouperBean>();

    for (SyncMembershipToGrouperBean syncMembershipToGrouperBean : this.syncToGrouper.getSyncMembershipToGrouperBeans()) {
      membershipMultiKeysToSyncBeansIncoming.put(syncMembershipToGrouperBean.convertToMultikey(), syncMembershipToGrouperBean);
    }

    if (this.syncToGrouper.getSyncToGrouperBehavior().isMembershipInsert()) {
      
      Set<MultiKey> membershipsToInsert = new HashSet<MultiKey>();
      membershipsToInsert.addAll(membershipMultiKeysToSyncBeansIncoming.keySet());
      
      membershipsToInsert.removeAll(grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership.keySet());
      for (MultiKey multiKeyToInsert : membershipsToInsert) {
        this.membershipInserts.add(membershipMultiKeysToSyncBeansIncoming.get(multiKeyToInsert));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isMembershipDeleteExtra()) {
      Set<MultiKey> membershipsToDelete = new HashSet<MultiKey>();
      
      membershipsToDelete.addAll(grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership.keySet());
      
      membershipsToDelete.removeAll(membershipMultiKeysToSyncBeansIncoming.keySet());
      for (MultiKey multiKeyToDelete : membershipsToDelete) {
        this.membershipDeletes.add(grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership.get(multiKeyToDelete));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isMembershipUpdate()) {
      Set<MultiKey> membershipsToUpdate = new HashSet<MultiKey>();
      
      membershipsToUpdate.addAll(membershipMultiKeysToSyncBeansIncoming.keySet());
      
      membershipsToUpdate.retainAll(grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership.keySet());
      
      for (MultiKey membershipMultiKey : membershipsToUpdate) {
        
        SyncMembershipToGrouperBean membershipInGrouper = grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership.get(membershipMultiKey);
        SyncMembershipToGrouperBean membershipIncoming = membershipMultiKeysToSyncBeansIncoming.get(membershipMultiKey);
        if (!membershipInGrouper.equals(membershipIncoming)) {
          membershipUpdates.add(membershipIncoming);
        }
      }
    }    
  }

  private void retrieveMembershipsFromGrouper() {

    this.getSyncToGrouper().getSyncToGrouperReport().setState("retrieveMembershipsFromGrouper");
    
    StringBuilder theMembershipSqlBase = new StringBuilder(
        // we dont need immediate membership id
        "SELECT gmav.immediate_membership_id as immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, (select gg2.name from grouper_groups gg2 where gm.subject_source='g:gsa' and gg2.id = gm.subject_id) as subject_identifier");
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled()) {
      theMembershipSqlBase.append(", gmav.immediate_mship_disabled_time, gmav.immediate_mship_enabled_time");
    }

    theMembershipSqlBase.append(" FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf "
        + "WHERE gmav.mship_type = 'immediate'");
    
    if (!this.getSyncToGrouper().getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled()) {
      theMembershipSqlBase.append(" AND gmav.immediate_mship_enabled = 'T'");
    }
    theMembershipSqlBase.append(" AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.name = 'members'");
          
    List<Object[]> membershipRows = new ArrayList<Object[]>();
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSyncFromStems()) {

      GrouperUtil.assertion(GrouperUtil.length(this.syncToGrouper.getTopLevelStemNamesFlattenedFromSqlOrInput()) > 0, 
          "If syncing grouper and folders then the top level folders are required or : for all");
      
      Set<String> topLevelStemSet = this.getSyncToGrouper().getTopLevelStemNamesFlattenedFromSqlOrInput();

      if (GrouperUtil.length(topLevelStemSet) == 0) {
        return;
      }

      GcDbAccess gcDbAccess = new GcDbAccess();
      if (!this.getSyncToGrouper().isTopLevelStemsHaveRoot()) {

        // get all the parent stems
        Set<Stem> topLevelStems = this.syncToGrouper.getTopLevelStemsFlattenedFromSqlOrInput();
  
        GrouperUtil.assertion(GrouperUtil.length(topLevelStems) < 400, "Cannot have more than 400 top level stems to sync");

        if (GrouperUtil.length(this.syncToGrouper.getTopLevelStemsFlattenedFromSqlOrInput()) == 0) {
          return;
        }
        
        theMembershipSqlBase.append(" and ( ");
        boolean addedOne = false;
        List<Object> bindVars = new ArrayList<Object>();

        for (Stem topLevelStem : topLevelStems) {
          if (addedOne) {
            theMembershipSqlBase.append(" or ");
          }
          addedOne = true;
          // children
          theMembershipSqlBase.append("gg.name like ?");
          bindVars.add(topLevelStem.getName() + ":%");
        }

        theMembershipSqlBase.append(" ) ");
        gcDbAccess.bindVars(bindVars);
      }

      gcDbAccess.sql(theMembershipSqlBase.toString());
  
      membershipRows.addAll(gcDbAccess.selectList(Object[].class));
        
    } else {

      List<SyncMembershipToGrouperBean> syncMembershipToGrouperBeans = this.syncToGrouper.getSyncMembershipToGrouperBeans();

      if (GrouperUtil.length(syncMembershipToGrouperBeans) == 0) {
        return;
      }
        
      Set<String> groupNameSet = new TreeSet<String>();
      for (SyncMembershipToGrouperBean syncMembershipToGrouperBean : syncMembershipToGrouperBeans) {
        GrouperUtil.assertion(!StringUtils.isBlank(syncMembershipToGrouperBean.getGroupName()), "Group name cannot be null!");
        groupNameSet.add(syncMembershipToGrouperBean.getGroupName());
      }

      List<String> groupNameList = new ArrayList<String>(groupNameSet);
      
      int batchSize = 800;

      int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupNameList, batchSize);

      for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
        
        List<String> batchGroupNames = GrouperUtil.batchList(groupNameList, batchSize, batchIndex);

        StringBuilder theMembershipSql = new StringBuilder(theMembershipSqlBase);
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
      
      SyncMembershipToGrouperBean syncMembershipToGrouperBean = new SyncMembershipToGrouperBean();
      syncMembershipToGrouperBean.assignImmediateMembershipId((String)membershipRow[0]);
      syncMembershipToGrouperBean.assignGroupName((String)membershipRow[1]);
      syncMembershipToGrouperBean.assignSubjectSourceId((String)membershipRow[2]);
      syncMembershipToGrouperBean.assignSubjectId((String)membershipRow[3]);
      syncMembershipToGrouperBean.assignSubjectIdentifier((String)membershipRow[4]);
      if (this.getSyncToGrouper().getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled()) {
        syncMembershipToGrouperBean.assignImmediateMshipDisabledTime(GrouperUtil.longObjectValue(membershipRow[5], true));
        syncMembershipToGrouperBean.assignImmediateMshipEnabledTime(GrouperUtil.longObjectValue(membershipRow[6], true));
      }
      MultiKey groupNameSubjectSourceIdSubjectIdOrIdentifier = syncMembershipToGrouperBean.convertToMultikey();
      
      this.grouperGroupNameSourceIdSubjectIdOrIdentifierToMembership.put(
          groupNameSubjectSourceIdSubjectIdOrIdentifier, syncMembershipToGrouperBean);
      
    }

  }


  /**
   * membership deletes
   */
  private List<SyncMembershipToGrouperBean> membershipDeletes = new ArrayList<SyncMembershipToGrouperBean>();

  /**
   * membership deletes
   * @return
   */
  public List<SyncMembershipToGrouperBean> getMembershipDeletes() {
    return membershipDeletes;
  }

}
