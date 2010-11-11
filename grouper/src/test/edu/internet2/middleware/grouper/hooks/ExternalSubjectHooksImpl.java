/*
 * @author mchyzer
 * $Id: LifecycleHooksImpl.java,v 1.1 2008-07-10 00:46:53 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksExternalSubjectBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;



/**
 * test implementation of external subject hook
 */
public class ExternalSubjectHooksImpl extends ExternalSubjectHooks {

  /**
   * last email of the external subject
   */
  public static String lastIdentifier;
  
  /**
   * test hook
   */
  @Override
  public void postEditExternalSubject(HooksContext hooksContext,
      HooksExternalSubjectBean editBean) {
    
    lastIdentifier = editBean.getExternalSubject().getIdentifier();
    
    if (StringUtils.equals(lastIdentifier, "vetome@school.edu")) {
      throw new HookVeto("hook.veto.external.subject.cant.be.vetome", "name cannot be vetome");
    }
    
  }



}
