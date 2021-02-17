package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class SyncToGrouper {

  public SyncToGrouper() {
    
    this.syncCompositeToGrouperLogic = new SyncCompositeToGrouperLogic(this);
    this.syncGroupToGrouperLogic = new SyncGroupToGrouperLogic(this);
    this.syncStemToGrouperLogic = new SyncStemToGrouperLogic(this);
    this.syncToGrouperReport = new SyncToGrouperReport(this);
    this.syncToGrouperFromSql = new SyncToGrouperFromSql(this);
    
  }

  /**
   * holds queries and settings for sql load
   */
  private SyncToGrouperFromSql syncToGrouperFromSql = null;
  
  /**
   * holds queries and settings for sql load
   * @return bean
   */
  public SyncToGrouperFromSql getSyncToGrouperFromSql() {
    return syncToGrouperFromSql;
  }

  /**
   * if this was a success
   * @return
   */
  public boolean isSuccess() {
    return this.syncToGrouperReport.getErrorLines().size() == 0;
  }
  

  /**
   * stems to sync to grouper
   */
  private List<SyncStemToGrouperBean> syncStemToGrouperBeans = null;


  /**
   * stems to sync to grouper
   * @return
   */
  public List<SyncStemToGrouperBean> getSyncStemToGrouperBeans() {
    return syncStemToGrouperBeans;
  }

  /**
   * stems to sync to grouper
   * @param syncStemToGrouperBeans
   */
  public void setSyncStemToGrouperBeans(
      List<SyncStemToGrouperBean> syncStemToGrouperBeans) {
    this.syncStemToGrouperBeans = syncStemToGrouperBeans;
  }

  /**
   * 
   */
  private SyncToGrouperBehavior syncToGrouperBehavior = new SyncToGrouperBehavior();

  /**
   * 
   * @return
   */
  public SyncToGrouperBehavior getSyncToGrouperBehavior() {
    return syncToGrouperBehavior;
  }

  /**
   * 
   * @param syncToGrouperBehavior
   */
  public void setSyncToGrouperBehavior(SyncToGrouperBehavior syncToGrouperBehavior) {
    this.syncToGrouperBehavior = syncToGrouperBehavior;
  }
  
  /**
   * 
   */
  private SyncStemToGrouperLogic syncStemToGrouperLogic = null;
  
  /**
   * 
   * @return
   */
  public SyncStemToGrouperLogic getSyncStemToGrouperLogic() {
    return syncStemToGrouperLogic;
  }

  /**
   * report of what changed or what will change
   */
  private SyncToGrouperReport syncToGrouperReport = null;

  /**
   * report of what changed or what will change
   * @return the report
   */
  public SyncToGrouperReport getSyncToGrouperReport() {
    return syncToGrouperReport;
  }
  
  /**
   * readWrite mode default false
   */
  private boolean readWrite = false;
  
  /**
   * readWrite mode default false
   * @return
   */
  public boolean isReadWrite() {
    return readWrite;
  }

  /**
   * readWrite mode default false
   * @param readonly
   */
  public void setReadWrite(boolean readonly) {
    this.readWrite = readonly;
  }

  /**
   * dont run twice with same objects
   */
  private boolean hasRunSync = false;
  
  /**
   * groups to sync to grouper
   */
  private List<SyncGroupToGrouperBean> syncGroupToGrouperBeans = null;
  
  /**
   * groups to sync to grouper
   * @return
   */
  public List<SyncGroupToGrouperBean> getSyncGroupToGrouperBeans() {
    return syncGroupToGrouperBeans;
  }

  /**
   * groups to sync to grouper
   * @param syncGroupToGrouperBeans
   */
  public void setSyncGroupToGrouperBeans(
      List<SyncGroupToGrouperBean> syncGroupToGrouperBeans) {
    this.syncGroupToGrouperBeans = syncGroupToGrouperBeans;
  }

  /**
   * group logic for sync
   * @return
   */
  public SyncGroupToGrouperLogic getSyncGroupToGrouperLogic() {
    return syncGroupToGrouperLogic;
  }

  /**
   * group logic for sync
   */
  private SyncGroupToGrouperLogic syncGroupToGrouperLogic = null;

  /**
   * composites to sync to grouper
   */
  private List<SyncCompositeToGrouperBean> syncCompositeToGrouperBeans = null;

  /**
   * composites to sync to grouper
   * @return
   */
  public List<SyncCompositeToGrouperBean> getSyncCompositeToGrouperBeans() {
    return syncCompositeToGrouperBeans;
  }

  /**
   * composites to sync to grouper
   * @param syncCompositeToGrouperBeans
   */
  public void setSyncCompositeToGrouperBeans(
      List<SyncCompositeToGrouperBean> syncCompositeToGrouperBeans) {
    this.syncCompositeToGrouperBeans = syncCompositeToGrouperBeans;
  }

  /**
   * composite logic
   * @return
   */
  public SyncCompositeToGrouperLogic getSyncCompositeToGrouperLogic() {
    return syncCompositeToGrouperLogic;
  }

  /**
   * 
   */
  private SyncCompositeToGrouperLogic syncCompositeToGrouperLogic = null;
  
  /**
   * generate a report about the sync
   * @return the report
   */
  public SyncToGrouperReport syncLogic() {

    GrouperUtil.assertion(!this.hasRunSync, "This sync has run with this object, please make a new object and run again!");
    this.hasRunSync = true;

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        SyncToGrouper.this.syncStemToGrouperLogic.syncLogic();
        SyncToGrouper.this.syncGroupToGrouperLogic.syncLogic();
        SyncToGrouper.this.syncCompositeToGrouperLogic.syncLogic();

        return null;
      }
    });
    return this.syncToGrouperReport;
  }
  
}
