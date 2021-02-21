package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class SyncStemToGrouperLogic {

  /**
   * no folders to sync message
   */
  public static final String NO_FOLDERS_TO_SYNC = "There are no folders to sync";

  /**
   * stem sync false message
   */
  public static final String STEM_SYNC_FALSE = "Folders are skipped since the stemSync behavior is false";
  
  private SyncToGrouper syncToGrouper = null;

  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }

  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }


  public SyncStemToGrouperLogic() {
    super();
  }


  public SyncStemToGrouperLogic(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }

  /**
   * top level stems to retrieve from database (and substems), as specified by the called.
   * Note: you should probably use the one from SyncToGrouper
   */
  private Set<String> topLevelStemNamesToSync = new TreeSet<String>();
  
  /**
   * top level stems to retrieve from database (and substems), as specified by the called
   * Note: you should probably use the one from SyncToGrouper
   * @return top level stems to sync
   */
  public Set<String> getTopLevelStemNamesToSync() {
    return topLevelStemNamesToSync;
  }

  /**
   * if these are the stems to sync: a:b:c, a:b, a:d, a:b:d, then the top level are: a:b, a:d
   */
  private void calculateTopLevelStemsToSync() {

    Set<String> stemsToSync = new HashSet<String>();
    for (SyncStemToGrouperBean syncStemToGrouperBean : GrouperUtil.nonNull(this.syncToGrouper.getSyncStemToGrouperBeans())) {
      stemsToSync.add(syncStemToGrouperBean.getName());
    }
    
    this.topLevelStemNamesToSync = GrouperUtil.stemCalculateTopLevelStems(stemsToSync);

  }

  /**
   * map of stem name to stem
   */
  private Map<String, Stem> grouperStemNameToStem = new TreeMap<String, Stem>();

  /**
   * map of stem name to stem
   * @return the map of stem name to stem
   */
  public Map<String, Stem> getGrouperStemNameToStem() {
    return this.grouperStemNameToStem;
  }

  /**
   * map of stem uuid to stem
   * @return
   */
  public Map<String, Stem> getGrouperStemUuidToStem() {
    return this.grouperStemUuidToStem;
  }

  /**
   * map of stem uuid to stem
   */
  private Map<String, Stem> grouperStemUuidToStem = new TreeMap<String, Stem>();
  
  /**
   * 
   */
  public void syncLogic() {
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isSqlLoad()) {
      this.getSyncToGrouper().getSyncToGrouperFromSql().loadStemDataFromSql();
    }
    
    this.calculateTopLevelStemsToSync();
    
    this.retrieveStemsFromGrouper();

    this.compareStems();

    this.changeGrouper();

  }


  private void changeGrouper() {
    
    if (!this.syncToGrouper.isReadWrite()) {
      return;
    }

    for (Stem stem : GrouperUtil.nonNull(this.stemDeletes)) {
      
      // get this again to reduce race conditions
      Stem stemInGrouper = StemFinder.findByName(GrouperSession.staticGrouperSession(), stem.getName(), false);
      if (stemInGrouper == null) {
        continue;
      }

      try {
        stemInGrouper.obliterate(false, false);
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success deleting folder '" + stem.getName() + "'");
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error deleting folder '" + stem.getName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    for (SyncStemToGrouperBean syncStemToGrouperBean : GrouperUtil.nonNull(this.stemInserts)) {
      
      try {
        StemSave stemSave = new StemSave(GrouperSession.staticGrouperSession()).assignName(syncStemToGrouperBean.getName()).assignCreateParentStemsIfNotExist(true);
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdOnInsert() && !StringUtils.isBlank(syncStemToGrouperBean.getId())) {
          stemSave.assignUuid(syncStemToGrouperBean.getId());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDescription()) {
          stemSave.assignDescription(syncStemToGrouperBean.getDescription());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdIndexOnInsert()
            && syncStemToGrouperBean.getIdIndex() != null) {
          stemSave.assignIdIndex(syncStemToGrouperBean.getIdIndex());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldAlternateName()
            && !StringUtils.isBlank(syncStemToGrouperBean.getAlternateName())) {
          stemSave.assignAlternateName(syncStemToGrouperBean.getAlternateName());
        }
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDisplayName()
            && !StringUtils.isBlank(syncStemToGrouperBean.getDisplayName())) {
          stemSave.assignDisplayName(syncStemToGrouperBean.getDisplayName());
        }
        
        stemSave.save();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success inserting folder '" + syncStemToGrouperBean.getName());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error inserting folder '" + syncStemToGrouperBean.getName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    for (SyncStemToGrouperBean syncStemToGrouperBean : GrouperUtil.nonNull(this.stemUpdates)) {
      
      // get this again to reduce race conditions
      Stem stemInGrouper = StemFinder.findByName(GrouperSession.staticGrouperSession(), syncStemToGrouperBean.getName(), false);
      if (stemInGrouper == null) {
        continue;
      }
      
      try {
        StemSave stemSave = new StemSave(GrouperSession.staticGrouperSession()).assignName(syncStemToGrouperBean.getName());
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDescription()) {
          stemSave.assignDescription(syncStemToGrouperBean.getDescription());
        } else {
          stemSave.assignDescription(stemInGrouper.getDescription());
        }
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDisplayName() && !StringUtils.isBlank(syncStemToGrouperBean.getDisplayName())) {
          stemSave.assignDisplayName(syncStemToGrouperBean.getDisplayName());
        } else {
          stemSave.assignDisplayName(stemInGrouper.getDisplayName());
        }

        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldAlternateName()) {
          stemSave.assignAlternateName(syncStemToGrouperBean.getAlternateName());
        } else {
          stemSave.assignAlternateName(stemInGrouper.getAlternateName());
        }

        stemSave.save();
        this.syncToGrouper.getSyncToGrouperReport().addChangeOverall();
        this.syncToGrouper.getSyncToGrouperReport().addOutputLine("Success updating folder '" + syncStemToGrouperBean.getName());
      } catch (Exception e) {
        this.syncToGrouper.getSyncToGrouperReport().addErrorLine("Error updating folder '" + syncStemToGrouperBean.getName() + "', " + GrouperUtil.getFullStackTrace(e));
      }
      
    }
    
  }

  /**
   * 
   */
  private List<SyncStemToGrouperBean> stemInserts = new ArrayList<SyncStemToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncStemToGrouperBean> getStemInserts() {
    return stemInserts;
  }

  /**
   * 
   */
  private List<SyncStemToGrouperBean> stemUpdates = new ArrayList<SyncStemToGrouperBean>();
  
  /**
   * 
   * @return
   */
  public List<SyncStemToGrouperBean> getStemUpdates() {
    return stemUpdates;
  }

  private void compareStems() {
    
    if (!this.syncToGrouper.getSyncToGrouperBehavior().isStemSync()) {
      this.syncToGrouper.getSyncToGrouperReport().addOutputLine(STEM_SYNC_FALSE);
      return;
    }
      
    Map<String, SyncStemToGrouperBean> stemNamesToSyncBeans = new TreeMap<String, SyncStemToGrouperBean>();
    
    for (SyncStemToGrouperBean syncStemToGrouperBean : GrouperUtil.nonNull(this.syncToGrouper.getSyncStemToGrouperBeans())) {
      stemNamesToSyncBeans.put(syncStemToGrouperBean.getName(), syncStemToGrouperBean);
    }

    if (GrouperUtil.length(this.syncToGrouper.getSyncStemToGrouperBeans()) == 0) {
      this.syncToGrouper.getSyncToGrouperReport().addOutputLine(NO_FOLDERS_TO_SYNC);
    }
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemInsert()) {
      Set<String> stemNamesToInsert = new TreeSet<String>();
      
      stemNamesToInsert.addAll(stemNamesToSyncBeans.keySet());
      
      stemNamesToInsert.removeAll(this.grouperStemNameToStem.keySet());
      
      for (String stemName : stemNamesToInsert) {
        this.stemInserts.add(stemNamesToSyncBeans.get(stemName));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemDeleteExtra()) {
      Set<String> stemNamesToDelete = new TreeSet<String>();
      
      stemNamesToDelete.addAll(this.grouperStemNameToStem.keySet());
      
      stemNamesToDelete.removeAll(stemNamesToSyncBeans.keySet());
      
      for (String stemName : stemNamesToDelete) {
        this.stemDeletes.add(this.grouperStemNameToStem.get(stemName));
      }
    }    

    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemUpdate()) {
      Set<String> stemNamesToUpdate = new TreeSet<String>();
      
      stemNamesToUpdate.addAll(stemNamesToSyncBeans.keySet());
      
      stemNamesToUpdate.retainAll(this.grouperStemNameToStem.keySet());
      
      for (String stemName : stemNamesToUpdate) {
        
        Stem stemInGrouper = this.grouperStemNameToStem.get(stemName);
        SyncStemToGrouperBean stemToUpdate = stemNamesToSyncBeans.get(stemName);
        
        boolean needsUpdate = false;
        
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDescription()
            && !StringUtils.equals(StringUtils.trimToNull(stemInGrouper.getDescription()), StringUtils.trimToNull(stemToUpdate.getDescription()))) {
          needsUpdate = true;
        }
        // only check the extension since if an ancestor display name changes it is not our purview
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDisplayName()
            && !StringUtils.isBlank(stemToUpdate.getDisplayName())
            && !StringUtils.equals(StringUtils.trimToNull(GrouperUtil.extensionFromName(stemInGrouper.getDisplayName())), 
                StringUtils.trimToNull(GrouperUtil.extensionFromName(stemToUpdate.getDisplayName())))) {
          needsUpdate = true;
        }
        // only check the extension since if an ancestor display name changes it is not our purview
        if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldAlternateName()
            && !StringUtils.equals(StringUtils.trimToNull(stemInGrouper.getAlternateName()), 
                StringUtils.trimToNull(stemToUpdate.getAlternateName()))) {
          needsUpdate = true;
        }
        if (needsUpdate) {
          this.stemUpdates.add(stemNamesToSyncBeans.get(stemName));
        }
      }
    }
    
  }

  private void retrieveStemsFromGrouper() {
    
    Set<Stem> topLevelStems = null;
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFromStems()) {
      
      // get all the parent stems
      topLevelStems = this.syncToGrouper.getTopLevelStemsFlattenedFromSqlOrInput();

      if (GrouperUtil.length(topLevelStems) == 0) {
        return;
      }
      
    } else {

      List<SyncStemToGrouperBean> syncStemToGrouperBeans = this.syncToGrouper.getSyncStemToGrouperBeans();
      Set<String> stemNames = new TreeSet<String>();
      for (SyncStemToGrouperBean syncStemToGrouperBean : GrouperUtil.nonNull(syncStemToGrouperBeans)) {
        stemNames.add(syncStemToGrouperBean.getName());
      }
      stemNames = GrouperUtil.stemCalculateTopLevelStems(stemNames);
      if (GrouperUtil.length(stemNames) > 0) {
        topLevelStems = GrouperDAOFactory.getFactory().getStem().findByNames(stemNames, false);
      }
      
    }
    
    Set<Stem> stems = new TreeSet<Stem>();

    for (Stem topLevelStem : GrouperUtil.nonNull(topLevelStems)) {
      stems.add(topLevelStem);
      Set<Stem> theStems = new StemFinder().assignParentStemId(topLevelStem.getId())
          .assignStemScope(Scope.SUB).findStems();
      stems.addAll(GrouperUtil.nonNull(theStems));
    }

    for (Stem stem : GrouperUtil.nonNull(stems)) {
      this.grouperStemNameToStem.put(stem.getName(), stem);
      this.grouperStemUuidToStem.put(stem.getUuid(), stem);
    }
    
  }


  /**
   * stem deletes
   */
  private List<Stem> stemDeletes = new ArrayList<Stem>();

  /**
   * stem deletes
   * @return
   */
  public List<Stem> getStemDeletes() {
    return stemDeletes;
  }

}
