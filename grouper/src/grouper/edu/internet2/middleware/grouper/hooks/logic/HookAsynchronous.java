/*******************************************************************************
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: HookAsynchronous.java,v 1.7 2008-09-29 03:38:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextType;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * class to facilitate asynchronous hooks
 */
public class HookAsynchronous {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(HookAsynchronous.class);
  
  /**
   * @param hooksBean
   * @param hookId 
   * @return the log string
   */
  private static String hookLogString(String hookId, HooksBean hooksBean) {
    return "Hook bean: " + hooksBean.getClass().getSimpleName() + ", id: " + hookId;
  }

  /**
   * use this to make an asynchronous hook implementation
   * @param hooksContext
   * @param hooksBean
   * @param hookAsynchronousHandler 
   */
  public static void callbackAsynchronous(HooksContext hooksContext, final HooksBean hooksBean, 
      final HookAsynchronousHandler hookAsynchronousHandler) {
    
    final Map<String, Object> threadSafeAttributes = hooksContext._internal_threadSafeAttributes(); 
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    final Subject grouperSessionSubject = grouperSession != null ? grouperSession.getSubject() : null;

    final String hookId = hooksContext.getHookId();
    
    final HooksBean hooksBeanCloned = (HooksBean)hooksBean.clone();

    final GrouperContextType grouperContextType = GrouperContextTypeBuiltIn._internal_getThreadLocalGrouperContextType();
    
    Thread thread = new Thread(new Runnable() {

      //run in new thread
      public void run() {
        
        
        HooksContext threadHooksContext = null;
        
        String debugLogString = null;
        long start = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
          debugLogString = hookLogString(hookId, hooksBeanCloned);
          LOG.debug("START: (async) " + debugLogString);
        }
        

        try {
          
          if (grouperContextType != null) {
            //if there is a threadlocal, set that
            GrouperContextTypeBuiltIn.setDefaultContext(grouperContextType);
          
          }
          
          threadHooksContext = new HooksContext(true, threadSafeAttributes, grouperSessionSubject, hookId);
        
          hookAsynchronousHandler.callback(threadHooksContext, hooksBeanCloned);
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("END (async) (normal): " + debugLogString + " (" + (System.currentTimeMillis() - start) + "ms)");
          }

        } catch (HookVeto hv) {
          LOG.error("Cant veto an asynchronous hook! " + hv.getMessage(), hv);
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("END (async) (veto): " + debugLogString + " (" + (System.currentTimeMillis() - start) + "ms)" 
                + ", veto key: " + hv.getReasonKey() + ", veto message: " + StringUtils.abbreviate(hv.getReason(), 50) );
          }

        } catch (Exception e) {
          LOG.error("Problem in asynchronous hook! " + e.getMessage(), e);

          if (LOG.isDebugEnabled()) {
            LOG.debug("END (async) (exception): " + debugLogString + " (" + (System.currentTimeMillis() - start) + "ms)" + ", exception: " + e.getMessage(), e);
          }
        } finally {
          //stop session if started and still seems like the same one
          if (threadHooksContext != null && threadHooksContext._internal_isAsynchronousGrouperSessionStarted()) {
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            if (grouperSession != null) {
              Subject subject = grouperSession.getSubject();
              if (subject != null) {
                if (StringUtils.equals(subject.getId(), threadHooksContext._internal_getAsynchronousGrouperSessionSubject().getId())) {
                  try {
                    grouperSession.stop();
                  } catch (SessionException se) {
                    LOG.error("Cant stop session: " + subject.getId(), se);
                  }
                }
              }
            }
          }
        }
        
        
      }
      
    });
    
    thread.start();
  }
  
}
