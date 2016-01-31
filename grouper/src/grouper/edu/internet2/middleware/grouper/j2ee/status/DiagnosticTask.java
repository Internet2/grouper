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
package edu.internet2.middleware.grouper.j2ee.status;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * <pre>A task to be executed for diagnostics. Each task should append any text to return in the event
 * of a failure to the method appendFailureText(). Text to use in the event of a success
 * should be sent to the method appendSuccessText().</pre>
 * @author mchyzer
 * $Id: DiagnosticTask.java,v 1.2 2009/03/09 07:29:00 mchyzer Exp $
 */
public abstract class DiagnosticTask {

  /**
   * 
   */
  @Override
  public abstract boolean equals(Object obj);

  /**
   * 
   */
  @Override
  public abstract int hashCode();

  /**
   * Text to use in the event of a failure.
   */
  private StringBuilder failureText = new StringBuilder();
  
  /**
   * Text to use in the event of a success.
   */
  private StringBuilder successText = new StringBuilder();
  
  
  /**
   * The actual task for the sublass to populate.
   * @return true if success, false if a failure.
   */
  protected abstract boolean doTask();

  
  /**
   * Execute the diagnostic task.
   * @return true if ok, false if not
   */
  public boolean executeTask(){
    if (ignoringDiagnostic()){
      return true;
    }
    this.failureText = new StringBuilder();
    this.successText = new StringBuilder();
    return doTask();
  }

  /**
   * See if ignoring something based on messages key.
   * @return true if ignoring the diagnostic
   */
  protected boolean ignoringDiagnostic() {
    
    boolean ignoreDiagnostics = GrouperConfig.retrieveConfig().propertyValueBoolean("ws.diagnostic.ignore." + this.retrieveName(), false);

    if (ignoreDiagnostics) {
      this.appendSuccessTextLine(this.retrieveNameFriendly() + " ignored in config");
    }
    
    HttpServletRequest httpServletRequest = GrouperStatusServlet.retrieveRequest();
    
    if (!ignoreDiagnostics) {
      if (httpServletRequest != null) {
        String includeOnly = httpServletRequest.getParameter("includeOnly");
        if (!StringUtils.isBlank(includeOnly)) {
          Set<String> includes = GrouperUtil.splitTrimToSet(includeOnly, ",");
          if (!includes.contains(this.retrieveName())) {
            this.appendSuccessTextLine(this.retrieveNameFriendly() + " ignored in config since URL param contains includeOnly which doesn't have '" + this.retrieveName() + "'");
            ignoreDiagnostics = true;
          }
        }
      }
    }

    
    if (!ignoreDiagnostics) {
      if (httpServletRequest != null) {
        String exclude = httpServletRequest.getParameter("exclude");
        if (!StringUtils.isBlank(exclude)) {
          Set<String> excludes = GrouperUtil.splitTrimToSet(exclude, ",");
          if (excludes.contains(this.retrieveName())) {
            this.appendSuccessTextLine(this.retrieveNameFriendly() + " ignored in config since URL param contains exclude which has '" + this.retrieveName() + "'");
            ignoreDiagnostics = true;
          }
        }
      }
    }

    return ignoreDiagnostics;

  }


  /**
   * failure text if this was a failure
   * @return the failure text
   */
  public StringBuilder retrieveFailureText() {
    return this.failureText;
  }


  /**
   * success text if this was a success
   * @return success text
   */
  public StringBuilder retrieveSuccessText() {
    return this.successText;
  }
  
  /**
   * <pre>Append a line to the text to be used in the event of a success.
   * Each line added has the current time of the operation added to it.</pre>
   * @param text is the text to use.
   */
  public void appendSuccessTextLine(String text){
    if (StringUtils.isEmpty(text)){
      return;
    }
    this.retrieveSuccessText().append("SUCCESS ").append(this.retrieveName())
      .append(": ").append(text).append(GrouperStatusServlet.elapsedSuffix() + "\n");
  }
  
  /**
  * <pre>Append a line to the text to be used in the event of a failure.
  * Each line added has the current time of the operation added to it.</pre>
  * @param text is the text to use.
  */
  protected void appendFailureTextLine(String text){
    if (StringUtils.isEmpty(text)){
      return;
    }
    this.retrieveFailureText().append("FAILURE ").append(this.retrieveName())
      .append(": ").append(text).append(GrouperStatusServlet.elapsedSuffix() + "\n");
  }

  
  /**
   * should be alphaNumeric in camelcase (for config file)
   * @return name
   */
  public abstract String retrieveName();

  /**
   * friendly name to display on screen
   * @return the friendly name
   */
  public abstract String retrieveNameFriendly();
  
}
