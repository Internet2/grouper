package edu.internet2.middleware.grouper.changeLog;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;


public class ChangeLogProcessorMetadata {

  /** log for job */
  private Hib3GrouperLoaderLog hib3GrouperLoaderLog;

  /**
   * 
   * @return the loader log
   */
  public Hib3GrouperLoaderLog getHib3GrouperLoaderLog() {
    return hib3GrouperLoaderLog;
  }

  /**
   * 
   * @param hib3GrouperLoaderLog1
   */
  public void setHib3GrouperLoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog1) {
    this.hib3GrouperLoaderLog = hib3GrouperLoaderLog1;
  }
  
  
}
