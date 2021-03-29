package edu.internet2.middleware.grouper.misc;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.OtherJobLogUpdater;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class SyncAllPitTablesDaemon extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SyncAllPitTablesDaemon.class);

  @Override
  public OtherJobOutput run(final OtherJobInput theOtherJobInput) {
    
    final SyncPITTables syncPITTables = new SyncPITTables();
    syncPITTables.createReport(true);
    syncPITTables.captureOutput(true);
    syncPITTables.showResults(true);

    OtherJobLogUpdater otherJobLogUpdater = new OtherJobLogUpdater() {
      
      @Override
      public void changeLoaderLogJavaObjectWithoutStoringToDb() {
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = theOtherJobInput.getHib3GrouperLoaderLog();
        String logMessage = syncPITTables.getFullOutput();
        hib3GrouperLoaderLog.setJobMessage(logMessage);
      }
    };

    RuntimeException runtimeException = null;
    try {
      SyncAllPitTablesDaemon.this.otherJobLogUpdaterRegister(otherJobLogUpdater);
      syncPITTables.syncAllPITTables();
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      SyncAllPitTablesDaemon.this.otherJobLogUpdaterDeregister(otherJobLogUpdater);
    }
    
        
    theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage(syncPITTables.getFullOutput() + (runtimeException == null ? "" : ("\n" + GrouperUtil.getFullStackTrace(runtimeException))));
    theOtherJobInput.getHib3GrouperLoaderLog().store();

    if (LOG.isDebugEnabled()) {
      LOG.debug(syncPITTables.getFullOutput());
    }
    
    if (runtimeException != null) {
      throw runtimeException;
    }
    
    return null;
  }

}
