package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.PrivilegeStemSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * 
 * @author mchyzer
 *
 */
public class SyncPrivilegeStemToGrouperLogic {

  /**
   * no priv stem to sync message
   */
  public static final String NO_PRIVILEGE_STEMS_TO_SYNC = "There are no stem privileges to sync";

  /**
   * priv stem sync false message
   */
  public static final String PRIVILEGE_STEM_SYNC_FALSE = "Stem privileges are skipped since the stemPrivilegeSync behavior is false";
  
  private SyncToGrouper syncToGrouper = null;

  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }

  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }


  public SyncPrivilegeStemToGrouperLogic() {
    super();
  }


  public SyncPrivilegeStemToGrouperLogic(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }

  /**
   * set of privileges (either groupName, sourceId, subjectId (non groups), field name, or stemName, sourceId, subjectIdentifier (stems), field name)
   */
  private Map<MultiKey, SyncPrivilegeStemToGrouperBean> grouperStemNameSourceIdSubjectIdOrIdentifierFieldNameToMembership = new HashMap<MultiKey, SyncPrivilegeStemToGrouperBean>();

  /**
   * set of privileges
   * @return multikeys
   */
  public Map<MultiKey, SyncPrivilegeStemToGrouperBean> getGrouperStemNameSourceIdSubjectIdOrIdentifierFieldNameToMembership() {
    return grouperStemNameSourceIdSubjectIdOrIdentifierFieldNameToMembership;
  }


  /**
   * 
   */
  public void syncLogic() {
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isSqlLoad()) {
      this.getSyncToGrouper().getSyncToGrouperFromSql().loadPrivilegeStemDataFromSql();
    }
    
    this.retrievePrivilegeStemsFromGrouper();

    this.comparePrivilegeStems();

    this.changeGrouper();

  }


  private void changeGrouper() {
    
    if (!this.syncToGrouper.isReadWrite()) {
      return;
    }

    for (SyncPrivilegeStemToGrouperBean syncPrivilegeStemToGrouperBean : GrouperUtil.nonNull(this.privilegeStemDeletes)) {
      String privilegeStemLabel = syncPrivilegeStemToGrouperBean.convertToLabel();
      try {
        
        GrouperUtil.assertion(!StringUtils.isBlank(syncPrivilegeStemToGrouperBean.getImmediateMembershipId()), "Privilege id is required");
        
        PrivilegeStemSave privilegeStemSave = syncPrivilegeStemToGrouperBean.convertToPrivilegeStemSave().assignSaveMode(SaveMode.DELETE);
        privilegeStemSave.save();
        
        if (privilegeStemSave.getSaveResultType().isChanged()) {
          this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
          this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success deleting " + privilegeStemLabel);
        }
        
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error deleting " + privilegeStemLabel + ", " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
    for (SyncPrivilegeStemToGrouperBean syncPrivilegeStemToGrouperBean : GrouperUtil.nonNull(this.privilegeStemInserts)) {
      
      String privilegeStemLabel = syncPrivilegeStemToGrouperBean.convertToLabel();
      try {
        PrivilegeStemSave privilegeStemSave = syncPrivilegeStemToGrouperBean.convertToPrivilegeStemSave();
        privilegeStemSave.save();
        if (privilegeStemSave.getSaveResultType().isChanged()) {
          this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
          this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success inserting " + privilegeStemLabel);
          
        }
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting " + privilegeStemLabel + ", " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
    // theres no such thing as privilege updates
  }

  /**
   * 
   */
  private List<SyncPrivilegeStemToGrouperBean> privilegeStemInserts = new ArrayList<SyncPrivilegeStemToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncPrivilegeStemToGrouperBean> getPrivilegeStemInserts() {
    return privilegeStemInserts;
  }

  private void comparePrivilegeStems() {
    
    if (!this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemSync()) {
      this.syncToGrouper.getSyncToGrouperReport().addOutputLine(PRIVILEGE_STEM_SYNC_FALSE);
      return;
    }
      
    Map<MultiKey, SyncPrivilegeStemToGrouperBean> privilegeStemMultiKeysToSyncBeansIncoming = new HashMap<MultiKey, SyncPrivilegeStemToGrouperBean>();

    for (SyncPrivilegeStemToGrouperBean syncPrivilegeStemToGrouperBean : this.syncToGrouper.getSyncPrivilegeStemToGrouperBeans()) {
      privilegeStemMultiKeysToSyncBeansIncoming.put(syncPrivilegeStemToGrouperBean.convertToMultikey(), syncPrivilegeStemToGrouperBean);
    }

    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemInsert()) {
      
      Set<MultiKey> membershipsToInsert = new HashSet<MultiKey>();
      membershipsToInsert.addAll(privilegeStemMultiKeysToSyncBeansIncoming.keySet());
      
      membershipsToInsert.removeAll(grouperStemNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.keySet());
      for (MultiKey multiKeyToInsert : membershipsToInsert) {
        this.privilegeStemInserts.add(privilegeStemMultiKeysToSyncBeansIncoming.get(multiKeyToInsert));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemDeleteExtra()) {
      Set<MultiKey> membershipsToDelete = new HashSet<MultiKey>();
      
      membershipsToDelete.addAll(grouperStemNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.keySet());
      
      membershipsToDelete.removeAll(privilegeStemMultiKeysToSyncBeansIncoming.keySet());
      for (MultiKey multiKeyToDelete : membershipsToDelete) {
        this.privilegeStemDeletes.add(grouperStemNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.get(multiKeyToDelete));
      }
    }    

    // theres no such thing as privilege updates right now
  }

  private void retrievePrivilegeStemsFromGrouper() {
    
    String schema = "";
    if (!StringUtils.isBlank(this.syncToGrouper.getSyncToGrouperFromSql().getDatabaseSyncFromAnotherGrouperSchema())) {
      schema = StringUtils.trim(this.syncToGrouper.getSyncToGrouperFromSql().getDatabaseSyncFromAnotherGrouperSchema());
      if (!this.syncToGrouper.getSyncToGrouperFromSql().getDatabaseSyncFromAnotherGrouperSchema().contains(".")) {
        schema += ".";
      }
    }
    
    StringBuilder thePrivilegeStemSqlBase = new StringBuilder(
        // we dont need immediate membership id
        "SELECT gmav.immediate_membership_id as immediate_membership_id, gs.name AS stem_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gf.name field_name");
    
    thePrivilegeStemSqlBase.append(" FROM " + schema + "grouper_memberships_all_v gmav, " + schema + "grouper_members gm, " + schema + "grouper_stems gs, " + schema + "grouper_fields gf "
        + "WHERE gmav.mship_type = 'immediate'");
    
    thePrivilegeStemSqlBase.append(" AND gmav.immediate_mship_enabled = 'T'");

    thePrivilegeStemSqlBase.append(" AND gmav.owner_stem_id = gs.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'naming'");
          
    List<Object[]> membershipRows = new ArrayList<Object[]>();
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemSyncFromStems()) {

      GrouperUtil.assertion(GrouperUtil.length(this.syncToGrouper.getTopLevelStemNamesFlattenedFromSqlOrInput()) > 0, 
          "If syncing grouper and privilege stems then the top level folders are required or : for all");
      
      if (GrouperUtil.length(this.getSyncToGrouper().getTopLevelStemsFlattenedFromSqlOrInput()) == 0) {
        return;
      }

      GcDbAccess gcDbAccess = new GcDbAccess();
      if (!this.getSyncToGrouper().isTopLevelStemsHaveRoot()) {

        // get all the parent stems
        Set<Stem> topLevelStems = this.syncToGrouper.getTopLevelStemsFlattenedFromSqlOrInput();
  
        GrouperUtil.assertion(GrouperUtil.length(topLevelStems) < 400, "Cannot have more than 400 top level stems to sync");

        thePrivilegeStemSqlBase.append(" and ( ");
        boolean addedOne = false;
        List<Object> bindVars = new ArrayList<Object>();

        for (Stem topLevelStem : topLevelStems) {
          if (addedOne) {
            thePrivilegeStemSqlBase.append(" or ");
          }
          addedOne = true;
          // children
          thePrivilegeStemSqlBase.append("gs.name = ? or ");
          bindVars.add(topLevelStem.getName());
          thePrivilegeStemSqlBase.append("gs.name like ?");
          bindVars.add(topLevelStem.getName() + ":%");
        }

        thePrivilegeStemSqlBase.append(" ) ");
        gcDbAccess.bindVars(bindVars);
      }

      gcDbAccess.sql(thePrivilegeStemSqlBase.toString());
  
      membershipRows.addAll(gcDbAccess.selectList(Object[].class));
        
    } else {

      List<SyncPrivilegeStemToGrouperBean> syncPrivilegeStemToGrouperBeans = this.syncToGrouper.getSyncPrivilegeStemToGrouperBeans();

      if (GrouperUtil.length(syncPrivilegeStemToGrouperBeans) == 0) {
        return;
      }
        
      Set<String> stemNameSet = new TreeSet<String>();
      for (SyncPrivilegeStemToGrouperBean syncPrivilegeStemToGrouperBean : syncPrivilegeStemToGrouperBeans) {
        GrouperUtil.assertion(!StringUtils.isBlank(syncPrivilegeStemToGrouperBean.getStemName()), "Stem name cannot be null!");
        stemNameSet.add(syncPrivilegeStemToGrouperBean.getStemName());
      }

      List<String> stemNameList = new ArrayList<String>(stemNameSet);
      
      int batchSize = 800;

      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemNameList, batchSize);

      for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
        
        List<String> batchStemNames = GrouperUtil.batchList(stemNameList, batchSize, batchIndex);

        StringBuilder theMembershipSql = new StringBuilder(thePrivilegeStemSqlBase);
        theMembershipSql.append(" and ( ");
  
        boolean addedOne = false;
        GcDbAccess gcDbAccess = new GcDbAccess();
        List<Object> bindVars = new ArrayList<Object>();
  
        for (String batchStemName : GrouperUtil.nonNull(batchStemNames)) {
          if (addedOne) {
            theMembershipSql.append(" or ");
          }
          addedOne = true;
          // the exact name
          theMembershipSql.append("gs.name = ?");
          bindVars.add(batchStemName);
        }
        theMembershipSql.append(" ) ");
        gcDbAccess.sql(theMembershipSql.toString()).bindVars(bindVars);
  
        membershipRows.addAll(gcDbAccess.selectList(Object[].class));
        
      }
        
    }
    
    for (Object[] membershipRow : membershipRows) {
      
      SyncPrivilegeStemToGrouperBean syncPrivilegeStemToGrouperBean = new SyncPrivilegeStemToGrouperBean();
      syncPrivilegeStemToGrouperBean.assignImmediateMembershipId((String)membershipRow[0]);
      syncPrivilegeStemToGrouperBean.assignStemName((String)membershipRow[1]);
      syncPrivilegeStemToGrouperBean.assignSubjectSourceId((String)membershipRow[2]);
      syncPrivilegeStemToGrouperBean.assignSubjectId((String)membershipRow[3]);
      syncPrivilegeStemToGrouperBean.assignSubjectIdentifier((String)membershipRow[4]);
      syncPrivilegeStemToGrouperBean.assignFieldName((String)membershipRow[5]);
      MultiKey groupNameSubjectSourceIdSubjectIdOrIdentifier = syncPrivilegeStemToGrouperBean.convertToMultikey();
      
      this.grouperStemNameSourceIdSubjectIdOrIdentifierFieldNameToMembership.put(
          groupNameSubjectSourceIdSubjectIdOrIdentifier, syncPrivilegeStemToGrouperBean);
      
    }

  }


  /**
   * privilege group deletes
   */
  private List<SyncPrivilegeStemToGrouperBean> privilegeStemDeletes = new ArrayList<SyncPrivilegeStemToGrouperBean>();


  /**
   * privilege group deletes
   * @return
   */
  public List<SyncPrivilegeStemToGrouperBean> getPrivilegeStemDeletes() {
    return privilegeStemDeletes;
  }

}
