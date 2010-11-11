/*
 * @author mchyzer
 * $Id: LoaderHooks.java,v 1.2 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksExternalSubjectBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * external subject related actions
 */
public abstract class ExternalSubjectHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: postEditExternalSubject */
  public static final String METHOD_POST_EDIT_EXTERNAL_SUBJECT = "postEditExternalSubject";


  //*****  END GENERATED WITH GenerateMethodConstants.java *****//  
  /**
   * called right after an edit of external subject (same transaction)
   * @param hooksContext
   * @param editBean
   */
  public void postEditExternalSubject(HooksContext hooksContext, HooksExternalSubjectBean editBean) {
    
  }
  
}
