package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

public class WsGshTemplateExecResult implements WsResponseBean, ResultMetadataHolder {
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGshTemplateExecResult.class);
  
  /**
   * result code of a request
   */
  public static enum WsGshTemplateExecResultCode implements WsResultCode {

    /** executed the gsh template successfully (http status code 200) (success: T) */
    SUCCESS(200),

    /** exception was thrown while running gsh template (http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid input (e.g. if everything blank) (http status code 400) (success: F) */
    INVALID(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGshTemplateExecResultCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

  }
  
  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();
  
  private boolean transaction;
  
  
  private WsGshValidationLine[] gshValidationLines;
  
  private WsGshOutputLine[] gshOutputLines;
  
  private String gshScriptOutput;
  
  
  public boolean isTransaction() {
    return transaction;
  }

  
  public void setTransaction(boolean transaction) {
    this.transaction = transaction;
  }
  
  
  public WsGshValidationLine[] getGshValidationLines() {
    return gshValidationLines;
  }


  
  public void setGshValidationLines(WsGshValidationLine[] gshValidationLines) {
    this.gshValidationLines = gshValidationLines;
  }


  
  public WsGshOutputLine[] getGshOutputLines() {
    return gshOutputLines;
  }


  
  public void setGshOutputLines(WsGshOutputLine[] gshOutputLines) {
    this.gshOutputLines = gshOutputLines;
  }

  
  public String getGshScriptOutput() {
    return gshScriptOutput;
  }


  
  public void setGshScriptOutput(String gshScriptOutput) {
    this.gshScriptOutput = gshScriptOutput;
  }


  @Override
  public WsResultMeta getResultMetadata() {
    return resultMetadata;
  }

  @Override
  public WsResponseMeta getResponseMetadata() {
    return responseMetadata;
  }
  
  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param clientVersion
   */
  public void assignResultCodeException(Exception e, String stack, GrouperVersion clientVersion) {
    this.assignResultCode(WsGshTemplateExecResultCode.EXCEPTION, clientVersion);
    this.getResultMetadata().appendResultMessage(stack);
  }

  
  /**
   * assign the code from the enum
   * @param groupSaveResultsCode
   * @param clientVersion 
   */
  public void assignResultCode(WsGshTemplateExecResultCode wsGshTemplateExecResultCode, GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(wsGshTemplateExecResultCode, clientVersion);
  }
  
}
