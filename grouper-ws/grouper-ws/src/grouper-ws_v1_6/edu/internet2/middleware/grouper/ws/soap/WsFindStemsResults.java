package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * returned from the stem find query, if none found, return none
 * 
 * @author mchyzer
 * 
 */
public class WsFindStemsResults {

  /**
   * result code of a request
   */
  public static enum WsFindStemsResultsCode implements WsResultCode {

    /** found the stem (or not) (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** found the subject (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** if the parent was not found in a search by parent (lite http status code 404) (success: F) */
    PARENT_STEM_NOT_FOUND(404);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsFindStemsResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsFindStemsResults.class);

  /**
   * assign the code from the enum
   * @param wsFindStemsResultsCode the code
   * 
   */
  public void assignResultCode(WsFindStemsResultsCode wsFindStemsResultsCode) {
    this.getResultMetadata().assignResultCode(wsFindStemsResultsCode);
  }

  /**
   * put a stem in the results
   * 
   * @param stem
   */
  public void assignStemResult(Stem stem) {

    WsStem wsStem = new WsStem(stem);

    this.setStemResults(new WsStem[] { wsStem });
  }

  /**
   * put a stem in the results
   * 
   * @param stemSet
   */
  public void assignStemResult(Set<Stem> stemSet) {
    if (stemSet == null) {
      this.setStemResults(null);
      return;
    }
    int stemSetSize = stemSet.size();
    WsStem[] wsStemResults = new WsStem[stemSetSize];
    int index = 0;
    for (Stem stem : stemSet) {

      WsStem wsStem = new WsStem(stem);

      wsStemResults[index] = wsStem;
      index++;
    }
    this.setStemResults(wsStemResults);
  }

  /**
   * has 0 to many stems that match the query by example
   */
  private WsStem[] stemResults;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * has 0 to many stems that match the query by example
   * 
   * @return the stemResults
   */
  public WsStem[] getStemResults() {
    return this.stemResults;
  }

  /**
   * has 0 to many stems that match the query by example
   * 
   * @param stemResults1
   *            the stemResults to set
   */
  public void setStemResults(WsStem[] stemResults1) {
    this.stemResults = stemResults1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsFindStemsResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsFindStemsResultsCode wsFindStemsResultsCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsFindStemsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindStemsResultsCodeOverride, WsFindStemsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsFindStemsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsFindStemsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindStemsResultsCodeOverride, WsFindStemsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsFindStemsResultsCodeOverride);

    }
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
