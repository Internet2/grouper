package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults.WsAssignAttributesResultsCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * <pre>
 * results for assigning attributes call.
 * 
 * result code:
 * code of the result for this attribute assignment overall
 * SUCCESS: means everything ok
 * INSUFFICIENT_PRIVILEGES: problem with some input where privileges are not sufficient
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsAssignAttributesLiteResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsAssignAttributesLiteResults.class);

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   */
  private WsAttributeDef[] wsAttributeDefs;
  
  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @return attribute defs
   */
  public WsAttributeDef[] getWsAttributeDefs() {
    return this.wsAttributeDefs;
  }

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
    this.wsAttributeDefs = wsAttributeDefs1;
  }

  /**
   * attribute def name referenced in the assignments or inputs (and able to read)
   */
  private WsAttributeDefName wsAttributeDefName;
  
  /**
   * attribute def name referenced in the assignments or inputs (and able to read)
   * @return attribute def name
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @param wsAttributeDefName1
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * the assignment results being queried
   */
  private WsAssignAttributeResult wsAttributeAssignResult;
  
  /**
   * the assignment results being queried
   * @return the assignments being queried
   */
  public WsAssignAttributeResult getWsAttributeAssignResult() {
    return this.wsAttributeAssignResult;
  }

  /**
   * the assignment results being queried
   * @param wsAttributeAssignResult1
   */
  public void setWsAttributeAssignResult(WsAssignAttributeResult wsAttributeAssignResult1) {
    this.wsAttributeAssignResult = wsAttributeAssignResult1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * 
   */
  public WsAssignAttributesLiteResults() {
    //default
  }
  
  /**
   * 
   * @param wsAssignAttributesResults
   */
  public WsAssignAttributesLiteResults(WsAssignAttributesResults wsAssignAttributesResults) {
    this.responseMetadata = wsAssignAttributesResults.getResponseMetadata();
    this.resultMetadata = wsAssignAttributesResults.getResultMetadata();
    this.subjectAttributeNames = wsAssignAttributesResults.getSubjectAttributeNames();
    this.wsAttributeAssignResult = GrouperUtil.arrayPopOne(wsAssignAttributesResults.getWsAttributeAssignResults());
    this.wsAttributeDefName = GrouperUtil.arrayPopOne(wsAssignAttributesResults.getWsAttributeDefNames());
    this.wsAttributeDefs = wsAssignAttributesResults.getWsAttributeDefs();
    this.wsGroup = GrouperUtil.arrayPopOne(wsAssignAttributesResults.getWsGroups());
    this.wsMembership = GrouperUtil.arrayPopOne(wsAssignAttributesResults.getWsMemberships());
    this.wsStem = GrouperUtil.arrayPopOne(wsAssignAttributesResults.getWsStems());
    this.wsSubject = GrouperUtil.arrayPopOne(wsAssignAttributesResults.getWsSubjects());
  }
  
  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * group that is in the result
   */
  private WsGroup wsGroup;

  /**
   * stems that are in the results
   */
  private WsStem wsStem;

  /**
   * stem that is in the results
   * @return stem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * stems that are in the results
   * @param wsStem1
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }

  /**
   * results for each assignment sent in
   */
  private WsMembership wsMembership;

  /**
   * subjects that are in the result
   */
  private WsSubject wsSubject;

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsMembership getWsMembership() {
    return this.wsMembership;
  }

  /**
   * subject that is in the results
   * @return the subject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * results for each assignment sent in
   * @param result1 the results to set
   */
  public void setWsMembership(WsMembership result1) {
    this.wsMembership = result1;
  }

  /**
   * subject that is in the results
   * @param wsSubject1
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetAttributeAssignmentsResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAssignAttributesResultsCode wsGetAttributeAssignmentsResultsCodeOverride, String theError,
      Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsAssignAttributesResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsAssignAttributesResultsCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);
  
    }
  }

  /**
   * assign the code from the enum
   * @param getAttributeAssignmentsResultCode
   */
  public void assignResultCode(WsAssignAttributesResultsCode getAttributeAssignmentsResultCode) {
    this.getResultMetadata().assignResultCode(getAttributeAssignmentsResultCode);
  }
}
