package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * dao for jobs
 * @author mchyzer
 *
 */
public class GcGrouperSyncDao {


  public GcGrouperSyncDao() {
  }

  /**
   * 
   */
  private GcGrouperSync gcGrouperSync;
  
  /**
   * 
   * @return
   */
  public GcGrouperSync getGcGrouperSync() {
    return gcGrouperSync;
  }

  /**
   * 
   * @param gcGrouperSync
   */
  public void setGcGrouperSync(GcGrouperSync gcGrouperSync) {
    this.gcGrouperSync = gcGrouperSync;
  }

  /**
   * 
   * @param connectionName
   * @return true if changed
   */
  public boolean store() {
    this.gcGrouperSync.storePrepare();
    boolean changed = new GcDbAccess().connectionName(this.gcGrouperSync.getConnectionName()).storeToDatabase(this.gcGrouperSync);
    return changed;
  }

  /**
   * select grouper sync by id
   * @param theConnectionName
   * @param id
   * @return the sync
   */
  public static GcGrouperSync retrieveById(String id) {
    return retrieveById(null, id);
  }
  
  /**
   * select grouper sync by id
   * @param theConnectionName
   * @param id
   * @return the sync
   */
  public static GcGrouperSync retrieveById(String theConnectionName, String id) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSync gcGrouperSync = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync where id = ?").addBindVar(id).select(GcGrouperSync.class);
    if (gcGrouperSync != null) {
      gcGrouperSync.setConnectionName(theConnectionName);
    }
    return gcGrouperSync;
  }
  

  /**
   * 
   * @param batchSize or default to 900
   */
  public int storeAllObjects() {
    
    int changes = 0;
    
    changes += this.store() ? 1 : 0;
    changes += this.getGcGrouperSync().getGcGrouperSyncJobDao().internal_jobStoreAll();
    changes += this.getGcGrouperSync().getGcGrouperSyncGroupDao().internal_groupStoreAll();
    changes += this.getGcGrouperSync().getGcGrouperSyncMemberDao().internal_memberStoreAll();
    changes += this.getGcGrouperSync().getGcGrouperSyncMembershipDao().internal_membershipStoreAll();
    changes += this.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logStoreAll();
    
    return changes;
  }
  
  /**
   * 
   * @param connectionName
   */
  public void delete() {
    this.gcGrouperSync.storePrepare();
    new GcDbAccess().connectionName(this.gcGrouperSync.getConnectionName()).deleteFromDatabase(this.gcGrouperSync);
  }

  /**
   * select grouper sync by provisioner name
   * @param theConnectionName
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSync retrieveByProvisionerName(String provisionerName) {
    return retrieveByProvisionerName(null, provisionerName);
  }

  /**
   * select grouper sync by provisioner name
   * @param theConnectionName
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSync retrieveByProvisionerName(String theConnectionName, String provisionerName) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSync gcGrouperSync = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync where provisioner_name = ?")
         .addBindVar(provisionerName).select(GcGrouperSync.class);
    if (gcGrouperSync != null) {
      gcGrouperSync.setConnectionName(theConnectionName);
    }
    return gcGrouperSync;
  }

  /**
   * retrieve a sync provisioner or create
   * @param theConnectionName
   * @param syncEngine
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSync retrieveOrCreateByProvisionerName(String provisionerName) {
    return retrieveOrCreateByProvisionerName(null, provisionerName);
  }

  /**
   * retrieve a sync provisioner or create
   * @param theConnectionName
   * @param syncEngine
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSync retrieveOrCreateByProvisionerName(String theConnectionName, String provisionerName) {
    GcGrouperSync gcGrouperSync = retrieveByProvisionerName(theConnectionName, provisionerName);
    if (gcGrouperSync == null) {
      try {
        gcGrouperSync = new GcGrouperSync();
        gcGrouperSync.setConnectionName(theConnectionName);
        gcGrouperSync.setProvisionerName(provisionerName);
        gcGrouperSync.getGcGrouperSyncDao().store();
      } catch (RuntimeException re) {
        // maybe someone else just created it...
        GrouperClientUtils.sleep(2000);
        gcGrouperSync = retrieveByProvisionerName(theConnectionName, provisionerName);
        if (gcGrouperSync == null) {
          throw re;
        }
      }
    }
    return gcGrouperSync;
  }
  


}
