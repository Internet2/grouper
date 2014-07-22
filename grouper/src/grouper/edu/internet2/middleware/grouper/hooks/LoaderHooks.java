/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
