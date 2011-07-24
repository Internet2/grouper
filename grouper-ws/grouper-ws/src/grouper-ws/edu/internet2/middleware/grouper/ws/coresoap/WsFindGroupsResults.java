package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * returned from the group find query
 * 
 * @author mchyzer
 * 
 */
public class WsFindGroupsResults implements WsResponseBean, ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsFindGroupsResults.class);

  /**
   * result code of a request
   */
  public static enum WsFindGroupsResultsCode implements WsResultCode {

    /** found the subject (lite http status code 200) (success: F) */
    SUCCESS(200),

    /** found the subject (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** cant find the stem in a stem search (lite http status code 404) (success: F) */
    STEM_NOT_FOUND(404);

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
    private WsFindGroupsResultsCode(int statusCode) {
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
   * assign the code from the enum
   * 
   * @param wsFindGroupsResultsCode
   */
  public void assignResultCode(WsFindGroupsResultsCode wsFindGroupsResultsCode) {
    this.getResultMetadata().assignResultCode(wsFindGroupsResultsCode);
  }

  /**
   * put a group in the results
   * @param includeDetail true if the detail for each group should be included
   * @param group
   */
  public void assignGroupResult(Group group, boolean includeDetail) {
    this.assignGroupResult(GrouperUtil.toSet(group), includeDetail);
  }

  /**
   * put a group in the results
   * @param includeDetail true if the detail for each group should be included
   * @param groupSet
   */
  public void assignGroupResult(Set<Group> groupSet, boolean includeDetail) {
    this.setGroupResults(WsGroup.convertGroups(groupSet, includeDetail));
  }

  /**
   * has 0 to many groups that match the query
   */
  private WsGroup[] groupResults;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * has 0 to many groups that match the query by example
   * 
   * @return the groupResults
   */
  public WsGroup[] getGroupResults() {
    return this.groupResults;
  }

  /**
   * basic results to the query
   * @param groupResults1 the groupResults to set
   */
  public void setGroupResults(WsGroup[] groupResults1) {
    this.groupResults = groupResults1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsFindGroupsResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsFindGroupsResultsCode wsFindGroupsResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsFindGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindGroupsResultsCodeOverride, WsFindGroupsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsFindGroupsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsFindGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindGroupsResultsCodeOverride, WsFindGroupsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsFindGroupsResultsCodeOverride);

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
