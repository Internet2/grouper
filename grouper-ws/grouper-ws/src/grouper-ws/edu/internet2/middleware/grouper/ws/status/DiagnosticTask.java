package edu.internet2.middleware.grouper.ws.status;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.GrouperWsConfig;



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
    
    boolean ignoreDiagnostics = GrouperWsConfig.getPropertyBoolean("ws.diagnostic.ignore." + this.retrieveName(), false);

    if (ignoreDiagnostics) {
      this.appendSuccessTextLine(this.retrieveNameFriendly() + " ignored in config");
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
