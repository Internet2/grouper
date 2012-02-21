/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;


/**
 * request bean in body of rest request
 */
public class WsRestAttributeDefNameSaveLiteRequest implements WsRequestBean {

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @return txType
   */
  public String getTxType() {
    return this.txType;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param txType1
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   */
  private String txType;
  
  
  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }
  
  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** if acting as another user */
  private String actAsSubjectId;
  /** if acting as another user */
  private String actAsSubjectIdentifier;
  /** if acting as another user */
  private String actAsSubjectSourceId;
  /** reserved for future use */
  private String paramName0;
  /** reserved for future use */
  private String paramName1;
  /** reserved for future use */
  private String paramValue0;
  /** reserved for future use */
  private String paramValue1;

  /**
   * the uuid of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupName)
   */
  private String attributeDefNameLookupUuid;
  
  /**
   * the uuid of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupName)
   * @return lookup uuid
   */
  public String getAttributeDefNameLookupUuid() {
    return this.attributeDefNameLookupUuid;
  }

  /**
   * the uuid of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupName)
   * @param attributeDefNameLookupUuid1
   */
  public void setAttributeDefNameLookupUuid(String attributeDefNameLookupUuid1) {
    this.attributeDefNameLookupUuid = attributeDefNameLookupUuid1;
  }

  /**
   * the name of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupUuid)
   * @return lookup name
   */
  public String getAttributeDefNameLookupName() {
    return this.attributeDefNameLookupName;
  }

  /**
   * the name of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupUuid)
   * @param attributeDefNameLookupName1
   */
  public void setAttributeDefNameLookupName(String attributeDefNameLookupName1) {
    this.attributeDefNameLookupName = attributeDefNameLookupName1;
  }

  /**
   * to lookup the attributeDef (mutually exclusive with attributeDefName)
   * @return lookup uuid
   */
  public String getAttributeDefLookupUuid() {
    return this.attributeDefLookupUuid;
  }

  /**
   * to lookup the attributeDef (mutually exclusive with attributeDefName)
   * @param attributeDefLookupUuid1
   */
  public void setAttributeDefLookupUuid(String attributeDefLookupUuid1) {
    this.attributeDefLookupUuid = attributeDefLookupUuid1;
  }

  /**
   * to lookup the attributeDef (mutually exclusive with attributeDefUuid)
   * @return lookup name
   */
  public String getAttributeDefLookupName() {
    return this.attributeDefLookupName;
  }

  /**
   * to lookup the attributeDef (mutually exclusive with attributeDefUuid)
   * @param attributeDefLookupName1
   */
  public void setAttributeDefLookupName(String attributeDefLookupName1) {
    this.attributeDefLookupName = attributeDefLookupName1;
  }

  /**
   * to lookup the attributeDefName (mutually exclusive with attributeDefNameName)
   * @return uuid
   */
  public String getAttributeDefNameUuid() {
    return this.attributeDefNameUuid;
  }

  /**
   * to lookup the attributeDefName (mutually exclusive with attributeDefNameName)
   * @param attributeDefNameUuid1
   */
  public void setAttributeDefNameUuid(String attributeDefNameUuid1) {
    this.attributeDefNameUuid = attributeDefNameUuid1;
  }

  /**
   * to lookup the attributeDefName (mutually exclusive with attributeDefNameUuid)
   * @return name
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * to lookup the attributeDefName (mutually exclusive with attributeDefNameUuid)
   * @param attributeDefNameName1
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * display name of the attributeDefName, empty will be ignored
   * @return display extension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * display name of the attributeDefName, empty will be ignored
   * @param displayExtension1
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * of the attributeDefName, empty will be ignored
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * of the attributeDefName, empty will be ignored
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return save mode
   */
  public String getSaveMode() {
    return this.saveMode;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }

  /**
   * T or F (default is F) if parent stems should be created if not exist
   * @return if create parent stems if not exist
   */
  public String getCreateParentStemsIfNotExist() {
    return this.createParentStemsIfNotExist;
  }

  /**
   * T or F (default is F) if parent stems should be created if not exist
   * @param createParentStemsIfNotExist1
   */
  public void setCreateParentStemsIfNotExist(String createParentStemsIfNotExist1) {
    this.createParentStemsIfNotExist = createParentStemsIfNotExist1;
  }
  
  /**
   * the name of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupUuid)
   */
  private String attributeDefNameLookupName;
  
  /**
   * to lookup the attributeDef (mutually exclusive with attributeDefName)
   */
  private String attributeDefLookupUuid;
  
  /**
   * to lookup the attributeDef (mutually exclusive with attributeDefUuid)
   */
  private String attributeDefLookupName;
  
  /**
   * to lookup the attributeDefName (mutually exclusive with attributeDefNameName)
   */
  private String attributeDefNameUuid;
  
  /**
   * to lookup the attributeDefName (mutually exclusive with attributeDefNameUuid)
   */
  private String attributeDefNameName;
  
  /**
   * display name of the attributeDefName, empty will be ignored
   */
  private String displayExtension;
  
  /**
   * of the attributeDefName, empty will be ignored
   */
  private String description;

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   */
  private String saveMode;
  
  /**
   * T or F (default is F) if parent stems should be created if not exist
   */
  private String createParentStemsIfNotExist;
  
  /**
   * if acting as another user
   * @return id
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * if acting as another user
   * @return subject identifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * if acting as another user
   * @return source id 
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * reserved for future use
   * @return param name 0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * reserved for future use
   * @return paramname1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * reserved for future use
   * @return param value 0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * reserved for future use
   * @return param value 1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectSourceId1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSourceId1) {
    this.actAsSubjectSourceId = actAsSubjectSourceId1;
  }

  /**
   * reserved for future use
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * reserved for future use
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * reserved for future use
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * reserved for future use
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }

  


}
