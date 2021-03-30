package edu.internet2.middleware.grouper.misc;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.OtherJobLogUpdater;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class SyncAllSetTablesDaemon extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SyncAllSetTablesDaemon.class);

  private void runStemSetSync(final OtherJobInput theOtherJobInput) {

    final SyncStemSets syncStemSets = new SyncStemSets();
    
    syncStemSets.captureOutput(true);
    syncStemSets.logDetails(true);
    syncStemSets.saveUpdates(true);
    syncStemSets.showResults(true);
    
    OtherJobLogUpdater otherJobLogUpdater = new OtherJobLogUpdater() {
      
      @Override
      public void changeLoaderLogJavaObjectWithoutStoringToDb() {
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = theOtherJobInput.getHib3GrouperLoaderLog();
        String logMessage = syncStemSets.getOutput();
        hib3GrouperLoaderLog.setJobMessage(currentOutput + "\n" + logMessage);
      }
    };

    RuntimeException runtimeException = null;
    try {
      SyncAllSetTablesDaemon.this.otherJobLogUpdaterRegister(otherJobLogUpdater);
      long updates = syncStemSets.fullSync();
      theOtherJobInput.getHib3GrouperLoaderLog().setUpdateCount(theOtherJobInput.getHib3GrouperLoaderLog().getUpdateCount() + (int)updates);
      theOtherJobInput.getHib3GrouperLoaderLog().setTotalCount(theOtherJobInput.getHib3GrouperLoaderLog().getTotalCount() + (int)syncStemSets.getProcessedCount());

    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      SyncAllSetTablesDaemon.this.otherJobLogUpdaterDeregister(otherJobLogUpdater);
    }
    
    this.currentOutput = currentOutput + "\n" + syncStemSets.getOutput() + (runtimeException == null ? "" : ("\n" + GrouperUtil.getFullStackTrace(runtimeException)));
    theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage(currentOutput);
    theOtherJobInput.getHib3GrouperLoaderLog().store();

    if (LOG.isDebugEnabled()) {
      LOG.debug(syncStemSets.getOutput());
    }
    
    if (runtimeException != null) {
      throw runtimeException;
    }

  }
  
  private void runGroupSetSync(final OtherJobInput theOtherJobInput) {

    final AddMissingGroupSets addMissingGroupSets = new AddMissingGroupSets();
    
    addMissingGroupSets.captureOutput(true);
    addMissingGroupSets.logDetails(true);
    addMissingGroupSets.saveUpdates(true);
    addMissingGroupSets.showResults(true);
    
    OtherJobLogUpdater otherJobLogUpdater = new OtherJobLogUpdater() {
      
      @Override
      public void changeLoaderLogJavaObjectWithoutStoringToDb() {
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = theOtherJobInput.getHib3GrouperLoaderLog();
        String logMessage = addMissingGroupSets.getOutput();
        hib3GrouperLoaderLog.setJobMessage(currentOutput + "\n" + logMessage);
      }
    };

    RuntimeException runtimeException = null;
    try {
      SyncAllSetTablesDaemon.this.otherJobLogUpdaterRegister(otherJobLogUpdater);
      addMissingGroupSets.addAllMissingGroupSets();
      theOtherJobInput.getHib3GrouperLoaderLog().setUpdateCount(theOtherJobInput.getHib3GrouperLoaderLog().getUpdateCount() + (int)addMissingGroupSets.getUpdateCount());
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      SyncAllSetTablesDaemon.this.otherJobLogUpdaterDeregister(otherJobLogUpdater);
    }
    this.currentOutput = currentOutput + "\n" + addMissingGroupSets.getOutput() + (runtimeException == null ? "" : ("\n" + GrouperUtil.getFullStackTrace(runtimeException)));
    theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage(currentOutput);
    theOtherJobInput.getHib3GrouperLoaderLog().store();

    if (LOG.isDebugEnabled()) {
      LOG.debug(addMissingGroupSets.getOutput());
    }
    
    if (runtimeException != null) {
      throw runtimeException;
    }

  }
  
  private String currentOutput = "";
  
  @Override
  public OtherJobOutput run(final OtherJobInput theOtherJobInput) {
    
    theOtherJobInput.getHib3GrouperLoaderLog().setUpdateCount(0);
    theOtherJobInput.getHib3GrouperLoaderLog().setTotalCount(0);

    this.runStemSetSync(theOtherJobInput);
    this.runGroupSetSync(theOtherJobInput);
    
    return null;
  }

}
