/*
 * @author mchyzer
 * $Id: LoaderHooks.java,v 1.1.2.1 2009-04-28 19:37:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLoaderBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * group related actions
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
