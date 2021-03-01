package edu.internet2.middleware.grouper.app.loader;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class OtherJobLogUpdater {

  public OtherJobLogUpdater() {
  }

  /**
   * update the Hib3GrouperLoaderLog object in java without saving to DB.
   * The caller will save this
   * @return
   */
  public abstract void changeLoaderLogJavaObjectWithoutStoringToDb();
  
}
