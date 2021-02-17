package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.CompositeSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * 
 * @author mchyzer
 *
 */
public class SyncCompositeToGrouperLogic {

  /**
   * no composites to sync message
   */
  public static final String NO_COMPOSITES_TO_SYNC = "There are no composites to sync";

  /**
   * composite sync false message
   */
  public static final String COMPOSITE_SYNC_FALSE = "Compsites are skipped since the compositeSync behavior is false";
  
  private SyncToGrouper syncToGrouper = null;

  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }

  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }


  public SyncCompositeToGrouperLogic() {
    super();
  }


  public SyncCompositeToGrouperLogic(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }

  /**
   * set of composites
   */
  private Set<MultiKey> grouperCompositeOwnerLeftRightTypeToComposite = new HashSet<MultiKey>();

  /**
   * set of composites
   * @return multikeys
   */
  public Set<MultiKey> getGrouperCompositeOwnerLeftRightTypeToComposite() {
    return grouperCompositeOwnerLeftRightTypeToComposite;
  }


  /**
   * 
   */
  public void syncLogic() {
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isSqlLoad()) {
      this.getSyncToGrouper().getSyncToGrouperFromSql().loadCompositeDataFromSql();
    }
    
    this.retrieveCompositesFromGrouper();

    this.compareComposites();

    this.changeGrouper();

  }


  private void changeGrouper() {
    
    if (!this.syncToGrouper.isReadWrite()) {
      return;
    }

    for (MultiKey composite : GrouperUtil.nonNull(this.compositeDeletes)) {

      try {
        CompositeSave compositeSave = new CompositeSave().assignOwnerName((String)composite.getKey(0));
        compositeSave.assignLeftFactorName((String)composite.getKey(1));
        compositeSave.assignRightFactorName((String)composite.getKey(2));
        compositeSave.assignType((String)composite.getKey(3));
        compositeSave.assignDeleteComposite(true);
        compositeSave.save();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success deleting composite '" + composite.getKey(0) + "'");
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error deleting composite '" + composite.getKey(0) + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    for (SyncCompositeToGrouperBean syncCompositeToGrouperBean : GrouperUtil.nonNull(this.compositeInserts)) {
      
      try {
        CompositeSave compositeSave = new CompositeSave().assignOwnerName(syncCompositeToGrouperBean.getOwnerName());
        compositeSave.assignLeftFactorName(syncCompositeToGrouperBean.getLeftFactorName());
        compositeSave.assignRightFactorName(syncCompositeToGrouperBean.getRightFactorName());
        compositeSave.assignType(syncCompositeToGrouperBean.getType());
        if (this.syncToGrouper.getSyncToGrouperBehavior().isCompositeSyncFieldIdOnInsert() && !StringUtils.isBlank(syncCompositeToGrouperBean.getId())) {
          compositeSave.assignId(syncCompositeToGrouperBean.getId());
        }
        compositeSave.save();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success inserting composite '" + syncCompositeToGrouperBean.getOwnerName());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting composite '" + syncCompositeToGrouperBean.getOwnerName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    for (SyncCompositeToGrouperBean syncCompositeToGrouperBean : GrouperUtil.nonNull(this.compositeUpdates)) {

      try {
        CompositeSave compositeSave = new CompositeSave().assignOwnerName(syncCompositeToGrouperBean.getOwnerName());
        compositeSave.assignLeftFactorName(syncCompositeToGrouperBean.getLeftFactorName());
        compositeSave.assignRightFactorName(syncCompositeToGrouperBean.getRightFactorName());
        compositeSave.assignType(syncCompositeToGrouperBean.getType());
        compositeSave.save();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success updating composite '" + syncCompositeToGrouperBean.getOwnerName());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error updating composite '" + syncCompositeToGrouperBean.getOwnerName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
  }

  /**
   * 
   */
  private List<SyncCompositeToGrouperBean> compositeInserts = new ArrayList<SyncCompositeToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncCompositeToGrouperBean> getCompositeInserts() {
    return compositeInserts;
  }

  /**
   * 
   */
  private List<SyncCompositeToGrouperBean> compositeUpdates = new ArrayList<SyncCompositeToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncCompositeToGrouperBean> getCompositeUpdates() {
    return compositeUpdates;
  }

  private void compareComposites() {
    
    if (!this.syncToGrouper.getSyncToGrouperBehavior().isCompositeSync()) {
      this.syncToGrouper.getSyncToGrouperReport().addOutputLine(COMPOSITE_SYNC_FALSE);
      return;
    }
      
    Map<MultiKey, SyncCompositeToGrouperBean> compositeMultiKeysToSyncBeansIncoming = new HashMap<MultiKey, SyncCompositeToGrouperBean>();

    for (SyncCompositeToGrouperBean syncCompositeToGrouperBean : this.syncToGrouper.getSyncCompositeToGrouperBeans()) {
      compositeMultiKeysToSyncBeansIncoming.put(new MultiKey(syncCompositeToGrouperBean.getOwnerName(), 
          syncCompositeToGrouperBean.getLeftFactorName(), syncCompositeToGrouperBean.getRightFactorName(),
          syncCompositeToGrouperBean.getType()), syncCompositeToGrouperBean);
    }

    // owner name to multiKey new
    Map<String, MultiKey> ownerNameToMultiKeyIncoming = new HashMap<String, MultiKey>();
    
    for (MultiKey composite : compositeMultiKeysToSyncBeansIncoming.keySet()) {
      ownerNameToMultiKeyIncoming.put((String)composite.getKey(0), composite);
    }


    // owner name to multiKey new
    Map<String, MultiKey> ownerNameToMultiKeyInGrouper = new HashMap<String, MultiKey>();

    for (MultiKey composite : grouperCompositeOwnerLeftRightTypeToComposite) {
      ownerNameToMultiKeyInGrouper.put((String)composite.getKey(0), composite);
    }
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isCompositeInsert()) {
      
      Set<String> ownerNamesToInsert = new HashSet<String>();
      ownerNamesToInsert.addAll(ownerNameToMultiKeyIncoming.keySet());
      
      ownerNamesToInsert.removeAll(ownerNameToMultiKeyInGrouper.keySet());
      for (String ownerNameToInsert : ownerNamesToInsert) {
        MultiKey multiKey = ownerNameToMultiKeyIncoming.get(ownerNameToInsert);
        this.compositeInserts.add(compositeMultiKeysToSyncBeansIncoming.get(multiKey));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isCompositeDeleteExtra()) {
      Set<String> compositeOwnerNamesToDelete = new TreeSet<String>();
      
      compositeOwnerNamesToDelete.addAll(ownerNameToMultiKeyInGrouper.keySet());
      
      compositeOwnerNamesToDelete.removeAll(ownerNameToMultiKeyIncoming.keySet());
      for (String ownerNameToDelete : compositeOwnerNamesToDelete) {
        this.compositeDeletes.add(ownerNameToMultiKeyInGrouper.get(ownerNameToDelete));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isCompositeUpdate()) {
      Set<String> compositeOwnerNamesToUpdate = new TreeSet<String>();
      
      compositeOwnerNamesToUpdate.addAll(ownerNameToMultiKeyInGrouper.keySet());
      
      compositeOwnerNamesToUpdate.retainAll(ownerNameToMultiKeyIncoming.keySet());
      
      for (String ownerName : compositeOwnerNamesToUpdate) {
        
        MultiKey compositeInGrouper = ownerNameToMultiKeyInGrouper.get(ownerName);
        MultiKey compositeIncoming = ownerNameToMultiKeyIncoming.get(ownerName);
        if (!compositeInGrouper.equals(compositeIncoming)) {
          SyncCompositeToGrouperBean syncCompositeToGrouperBean = compositeMultiKeysToSyncBeansIncoming.get(compositeIncoming);
          compositeUpdates.add(syncCompositeToGrouperBean);
        }
      }
    }    
  }

  private void retrieveCompositesFromGrouper() {
    
    StringBuilder theCompositeSql = new StringBuilder(
        "SELECT group_owner.name AS owner_name, group_left_factor.name AS left_factor_name, group_right_factor.name AS right_factor_name, gc.type "
        + "FROM grouper_composites gc, grouper_groups group_owner, grouper_groups group_left_factor, "
            + "grouper_groups group_right_factor "
        + "WHERE gc.owner = group_owner.id AND gc.left_factor = group_left_factor.id AND gc.right_factor = group_right_factor.id");

    if (this.syncToGrouper.getSyncToGrouperBehavior().isCompositeSyncFromStems()) {
      
      // get all the parent stems
      Set<Stem> topLevelStems = this.syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemsToSync();

      GrouperUtil.assertion(GrouperUtil.length(topLevelStems) < 400, "Cannot have more than 400 top level stems to sync");

      if (GrouperUtil.length(topLevelStems) > 0) {
        theCompositeSql.append(" and ( ");
        boolean addedOne = false;
        GcDbAccess gcDbAccess = new GcDbAccess();
        List<Object> bindVars = new ArrayList<Object>();
        
        for (Stem topLevelStem : topLevelStems) {
          if (addedOne) {
            theCompositeSql.append(" or ");
          }
          addedOne = true;
          // children
          theCompositeSql.append("group_owner.name like ?");
          bindVars.add(topLevelStem.getName() + ":%");
        }
  
        theCompositeSql.append(" ) ");
        gcDbAccess.sql(theCompositeSql.toString()).bindVars(bindVars);
  
        List<Object[]> compositeRows = gcDbAccess.selectList(Object[].class);
        
        for (Object[] compositeRow : compositeRows) {
          MultiKey compositeMultiKey = new MultiKey(compositeRow);
          this.grouperCompositeOwnerLeftRightTypeToComposite.add(compositeMultiKey);
        }
      }      
    } else {

      List<SyncCompositeToGrouperBean> syncCompositeToGrouperBeans = this.syncToGrouper.getSyncCompositeToGrouperBeans();

      GrouperUtil.assertion(GrouperUtil.length(syncCompositeToGrouperBeans) < 800, "Cannot have more than 800 composites to sync");

      if (GrouperUtil.length(syncCompositeToGrouperBeans) > 0) {
        
        theCompositeSql.append(" and ( ");
  
        boolean addedOne = false;
        GcDbAccess gcDbAccess = new GcDbAccess();
        List<Object> bindVars = new ArrayList<Object>();
  
        for (SyncCompositeToGrouperBean syncCompositeToGrouperBean : GrouperUtil.nonNull(syncCompositeToGrouperBeans)) {
          if (addedOne) {
            theCompositeSql.append(" or ");
          }
          addedOne = true;
          // the exact name
          theCompositeSql.append("group_owner.name = ?");
          bindVars.add(syncCompositeToGrouperBean.getOwnerName());
        }
        theCompositeSql.append(" ) ");
        gcDbAccess.sql(theCompositeSql.toString()).bindVars(bindVars);
  
        List<Object[]> compositeRows = gcDbAccess.selectList(Object[].class);
        
        for (Object[] compositeRow : compositeRows) {
          MultiKey compositeMultiKey = new MultiKey(compositeRow);
          this.grouperCompositeOwnerLeftRightTypeToComposite.add(compositeMultiKey);
        }
      }      
    }
    
  }


  /**
   * composite deletes
   */
  private List<MultiKey> compositeDeletes = new ArrayList<MultiKey>();

  /**
   * composite deletes
   * @return
   */
  public List<MultiKey> getCompositeDeletes() {
    return compositeDeletes;
  }

}
