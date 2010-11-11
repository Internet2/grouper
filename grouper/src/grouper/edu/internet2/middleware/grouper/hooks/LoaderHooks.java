/*
 * @author mchyzer
 * $Id: LoaderHooks.java,v 1.2 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLoaderBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * loader related actions
 */
public abstract class LoaderHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: loaderPreRun */
  public static final String METHOD_LOADER_PRE_RUN = "loaderPreRun";

  /** constant for method name for: loaderPreRun */
  public static final String METHOD_LOADER_POST_RUN = "loaderPostRun";


  //*****  END GENERATED WITH GenerateMethodConstants.java *****//  
  /**
   * called right before a loader run
   * @param hooksContext
   * @param preRunBean
   */
  public void loaderPreRun(HooksContext hooksContext, HooksLoaderBean preRunBean) {
    
  }
  
  /**
   * called right after a loader run
   * @param hooksContext
   * @param preRunBean
   */
  public void loaderPostRun(HooksContext hooksContext, HooksLoaderBean preRunBean) {
    
  }
  
  
}
