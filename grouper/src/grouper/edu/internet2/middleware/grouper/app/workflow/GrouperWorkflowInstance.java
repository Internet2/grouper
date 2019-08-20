package edu.internet2.middleware.grouper.app.workflow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowInstance {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowInstance.class);
  
  /**
   * attriute assign id of this instance
   */
  private String attributeAssignId;
  
  /**
   * state of the workflow
   */
  private String workflowInstanceState;
  
  /**
   * last updated millis since 1970
   */
  private Long workflowInstanceLastUpdatedMillisSince1970;
  
  /**
   * config marker assignment id
   */
  private String workflowInstanceConfigMarkerAssignmentId;
  
  /**
   * millis since 1970 when this instance was initiated
   */
  private Long workflowInstanceInitiatedMillisSince1970;
  
  /**
   * instance uuid
   */
  private String workflowInstanceUuid;
  
  /**
   * file info json string
   */
  private String workflowInstanceFileInfoString;
  
  /**
   * file info object
   */
  private GrouperWorkflowInstanceFilesInfo grouperWorkflowInstanceFilesInfo;
  
  /**
   * log entries json string
   */
  private String workflowInstanceLogEntriesString;
  
  /**
   * log entries object
   */
  private GrouperWorkflowInstanceLogEntries grouperWorkflowInstanceLogEntries;
  
  /**
   * instance encryption key
   */
  private String workflowInstanceEncryptionKey;
  
  /**
   * instance last emailed date
   */
  private String workflowInstanceLastEmailedDate;
  
  /**
   * instance last emailed state
   */
  private String workflowInstanceLastEmailedState;
    
  /**
   * error if any
   */
  private String workflowInstanceError;
  
  /**
   * string value of 0 index param
   */
  private String workflowInstanceParamValue0String;
  
  /**
   * param value object for 0 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue0;
  
  /**
   * string value of 1 index param
   */
  private String workflowInstanceParamValue1String;
  
  /**
   * param value object for 1 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue1;
  
  /**
   * string value of 2 index param
   */
  private String workflowInstanceParamValue2String;
  
  /**
   * param value object for 2 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue2;
  
  /**
   * string value of 3 index param
   */
  private String workflowInstanceParamValue3String;
  
  /**
   * param value object for 3 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue3;
  
  /**
   * string value of 4 index param
   */
  private String workflowInstanceParamValue4String;
  
  /**
   * param value object for 4 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue4;
  
  /**
   * string value of 5 index param
   */
  private String workflowInstanceParamValue5String;
  
  /**
   * param value object for 5 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue5;
  
  /**
   * string value of 6 index param
   */
  private String workflowInstanceParamValue6String;
  
  /**
   * param value object for 6 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue6;
  
  /**
   * string value of 7 index param
   */
  private String workflowInstanceParamValue7String;
  
  /**
   * param value object for 7 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue7;
  
  /**
   * string value of 8 index param
   */
  private String workflowInstanceParamValue8String;
  
  /**
   * param value object for 8 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue8;
  
  /**
   * string value of 9 index param
   */
  private String workflowInstanceParamValue9String;
  
  /**
   * param value object for 9 index
   */
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue9;
  
  /**
   * workflow config this instance is child of
   */
  private GrouperWorkflowConfig grouperWorkflowConfig;
  
  /**
   * group on which this instance is hanging off
   */
  private GrouperObject ownerGrouperObject;
  
  /**
   * initiator subject
   */
  private Subject initiatorSubject;

  /**
   * workflow config this instance is child of
   * @return
   */
  public GrouperWorkflowConfig getGrouperWorkflowConfig() {
    return grouperWorkflowConfig;
  }

  /**
   * workflow config this instance is child of
   * @param grouperWorkflowConfig
   */
  public void setGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig) {
    this.grouperWorkflowConfig = grouperWorkflowConfig;
  }

  /**
   * attriute assign id of this instance
   * @return
   */
  public String getAttributeAssignId() {
    return attributeAssignId;
  }

  /**
   * attriute assign id of this instance
   * @param attributeAssignId
   */
  public void setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
  }

  /**
   * state of the workflow
   * @return
   */
  public String getWorkflowInstanceState() {
    return workflowInstanceState;
  }

  /**
   * state of the workflow
   * @param workflowInstanceState
   */
  public void setWorkflowInstanceState(String workflowInstanceState) {
    this.workflowInstanceState = workflowInstanceState;
  }

  /**
   * last updated millis since 1970
   * @return
   */
  public Long getWorkflowInstanceLastUpdatedMillisSince1970() {
    return workflowInstanceLastUpdatedMillisSince1970;
  }

  /**
   * last updated millis since 1970
   * @param workflowInstanceLastUpdatedMillisSince1970
   */
  public void setWorkflowInstanceLastUpdatedMillisSince1970(
      Long workflowInstanceLastUpdatedMillisSince1970) {
    this.workflowInstanceLastUpdatedMillisSince1970 = workflowInstanceLastUpdatedMillisSince1970;
  }
  
  /**
   * last updated date in string format
   * @return
   */
  public String getWorkflowInstanceLastUpdatedDate() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    String date = dateFormat.format(new Date(workflowInstanceLastUpdatedMillisSince1970));
    return date;
  }

  /**
   * config marker assignment id
   * @return
   */
  public String getWorkflowInstanceConfigMarkerAssignmentId() {
    return workflowInstanceConfigMarkerAssignmentId;
  }

  /**
   * config marker assignment id
   * @param workflowInstanceConfigMarkerAssignmentId
   */
  public void setWorkflowInstanceConfigMarkerAssignmentId(
      String workflowInstanceConfigMarkerAssignmentId) {
    this.workflowInstanceConfigMarkerAssignmentId = workflowInstanceConfigMarkerAssignmentId;
  }

  
  /**
   * millis since 1970 when this instance was initiated
   * @return
   */
  public Long getWorkflowInstanceInitiatedMillisSince1970() {
    return workflowInstanceInitiatedMillisSince1970;
  }

  /**
   * millis since 1970 when this instance was initiated
   * @param workflowInstanceInitiatedMillisSince1970
   */
  public void setWorkflowInstanceInitiatedMillisSince1970(
      Long workflowInstanceInitiatedMillisSince1970) {
    this.workflowInstanceInitiatedMillisSince1970 = workflowInstanceInitiatedMillisSince1970;
  }

  /**
   * instance uuid
   * @return
   */
  public String getWorkflowInstanceUuid() {
    return workflowInstanceUuid;
  }
  
  /**
   * instance uuid
   * @param workflowInstanceUuid
   */
  public void setWorkflowInstanceUuid(String workflowInstanceUuid) {
    this.workflowInstanceUuid = workflowInstanceUuid;
  }

  /**
   * file info json string
   * @return
   */
  public String getWorkflowInstanceFileInfoString() {
    return workflowInstanceFileInfoString;
  }

  /**
   * file info json string
   * @param workflowInstanceFileInfoString
   */
  public void setWorkflowInstanceFileInfoString(String workflowInstanceFileInfoString) {
    this.workflowInstanceFileInfoString = workflowInstanceFileInfoString;
  }

  /**
   * file info object
   * @return
   */
  public GrouperWorkflowInstanceFilesInfo getGrouperWorkflowInstanceFilesInfo() {
    return grouperWorkflowInstanceFilesInfo;
  }
  
  /**
   * file info object
   * @param grouperWorkflowInstanceFilesInfo
   */
  public void setGrouperWorkflowInstanceFilesInfo(GrouperWorkflowInstanceFilesInfo grouperWorkflowInstanceFilesInfo) {
    this.grouperWorkflowInstanceFilesInfo = grouperWorkflowInstanceFilesInfo;
  }
  
  /**
   * log entries json string
   * @return
   */
  public String getWorkflowInstanceLogEntriesString() {
    return workflowInstanceLogEntriesString;
  }

  /**
   * log entries json string
   * @param workflowInstanceLogEntriesString
   */
  public void setWorkflowInstanceLogEntriesString(String workflowInstanceLogEntriesString) {
    this.workflowInstanceLogEntriesString = workflowInstanceLogEntriesString;
  }

  /**
   * log entries object
   * @return
   */
  public GrouperWorkflowInstanceLogEntries getGrouperWorkflowInstanceLogEntries() {
    return grouperWorkflowInstanceLogEntries;
  }

  /**
   * log entries object
   * @param grouperWorkflowInstanceLogEntries
   */
  public void setGrouperWorkflowInstanceLogEntries(GrouperWorkflowInstanceLogEntries grouperWorkflowInstanceLogEntries) {
    this.grouperWorkflowInstanceLogEntries = grouperWorkflowInstanceLogEntries;
  }

  /**
   * instance encryption key
   * @param workflowInstanceEncryptionKey
   */
  public void setWorkflowInstanceEncryptionKey(String workflowInstanceEncryptionKey) {
    this.workflowInstanceEncryptionKey = workflowInstanceEncryptionKey;
  }

  /**
   * instance encryption key
   * @return
   */
  public String getWorkflowInstanceEncryptionKey() {
    return workflowInstanceEncryptionKey;
  }


  /**
   * instance last emailed date
   * @return
   */
  public String getWorkflowInstanceLastEmailedDate() {
    return workflowInstanceLastEmailedDate;
  }

  /**
   * instance last emailed date
   * @param workflowInstanceLastEmailedDate
   */
  public void setWorkflowInstanceLastEmailedDate(String workflowInstanceLastEmailedDate) {
    this.workflowInstanceLastEmailedDate = workflowInstanceLastEmailedDate;
  }

  /**
   * instance last emailed state
   * @return
   */
  public String getWorkflowInstanceLastEmailedState() {
    return workflowInstanceLastEmailedState;
  }

  
  /**
   * instance last emailed state
   * @param workflowInstanceLastEmailedState
   */
  public void setWorkflowInstanceLastEmailedState(String workflowInstanceLastEmailedState) {
    this.workflowInstanceLastEmailedState = workflowInstanceLastEmailedState;
  }
  
  /**
   * error if any
   * @return
   */
  public String getWorkflowInstanceError() {
    return workflowInstanceError;
  }

  /**
   * error if any
   * @param workflowInstanceError
   */
  public void setWorkflowInstanceError(String workflowInstanceError) {
    this.workflowInstanceError = workflowInstanceError;
  }

  /**
   * string value of 0 index param
   * @return
   */
  public String getWorkflowInstanceParamValue0String() {
    return workflowInstanceParamValue0String;
  }

  /**
   * string value of 0 index param
   * @param workflowInstanceParamValue0String
   */
  public void setWorkflowInstanceParamValue0String(
      String workflowInstanceParamValue0String) {
    this.workflowInstanceParamValue0String = workflowInstanceParamValue0String;
  }

  
  /**
   * param value object for 0 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue0() {
    return grouperWorkflowInstanceParamValue0;
  }

  /**
   * param value object for 0 index
   * @param grouperWorkflowInstanceParamValue0
   */
  public void setGrouperWorkflowInstanceParamValue0(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue0) {
    this.grouperWorkflowInstanceParamValue0 = grouperWorkflowInstanceParamValue0;
  }


  /**
   * string value of 1 index param
   * @return
   */
  public String getWorkflowInstanceParamValue1String() {
    return workflowInstanceParamValue1String;
  }

  /**
   * string value of 1 index param
   * @param workflowInstanceParamValue1String
   */
  public void setWorkflowInstanceParamValue1String(
      String workflowInstanceParamValue1String) {
    this.workflowInstanceParamValue1String = workflowInstanceParamValue1String;
  }

  /**
   * param value object for 1 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue1() {
    return grouperWorkflowInstanceParamValue1;
  }

  /**
   * param value object for 1 index
   * @param grouperWorkflowInstanceParamValue1
   */
  public void setGrouperWorkflowInstanceParamValue1(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue1) {
    this.grouperWorkflowInstanceParamValue1 = grouperWorkflowInstanceParamValue1;
  }

  /**
   * string value of 2 index param
   * @return
   */
  public String getWorkflowInstanceParamValue2String() {
    return workflowInstanceParamValue2String;
  }

  /**
   * string value of 2 index param
   * @param workflowInstanceParamValue2String
   */
  public void setWorkflowInstanceParamValue2String(
      String workflowInstanceParamValue2String) {
    this.workflowInstanceParamValue2String = workflowInstanceParamValue2String;
  }

  /**
   * param value object for 2 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue2() {
    return grouperWorkflowInstanceParamValue2;
  }

  /**
   * param value object for 2 index
   * @param grouperWorkflowInstanceParamValue2
   */
  public void setGrouperWorkflowInstanceParamValue2(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue2) {
    this.grouperWorkflowInstanceParamValue2 = grouperWorkflowInstanceParamValue2;
  }

  /**
   * string value of 3 index param
   * @return
   */
  public String getWorkflowInstanceParamValue3String() {
    return workflowInstanceParamValue3String;
  }

  /**
   * string value of 3 index param
   * @param workflowInstanceParamValue3String
   */
  public void setWorkflowInstanceParamValue3String(
      String workflowInstanceParamValue3String) {
    this.workflowInstanceParamValue3String = workflowInstanceParamValue3String;
  }

  /**
   * param value object for 3 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue3() {
    return grouperWorkflowInstanceParamValue3;
  }

  /**
   * param value object for 3 index
   * @param grouperWorkflowInstanceParamValue3
   */
  public void setGrouperWorkflowInstanceParamValue3(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue3) {
    this.grouperWorkflowInstanceParamValue3 = grouperWorkflowInstanceParamValue3;
  }

  /**
   * string value of 4 index param
   * @return
   */
  public String getWorkflowInstanceParamValue4String() {
    return workflowInstanceParamValue4String;
  }

  /**
   * string value of 4 index param
   * @param workflowInstanceParamValue4String
   */
  public void setWorkflowInstanceParamValue4String(
      String workflowInstanceParamValue4String) {
    this.workflowInstanceParamValue4String = workflowInstanceParamValue4String;
  }

  /**
   * param value object for 4 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue4() {
    return grouperWorkflowInstanceParamValue4;
  }

  /**
   * param value object for 4 index
   * @param grouperWorkflowInstanceParamValue4
   */
  public void setGrouperWorkflowInstanceParamValue4(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue4) {
    this.grouperWorkflowInstanceParamValue4 = grouperWorkflowInstanceParamValue4;
  }

  /**
   * string value of 5 index param
   * @return
   */
  public String getWorkflowInstanceParamValue5String() {
    return workflowInstanceParamValue5String;
  }

  /**
   * string value of 5 index param
   * @param workflowInstanceParamValue5String
   */
  public void setWorkflowInstanceParamValue5String(
      String workflowInstanceParamValue5String) {
    this.workflowInstanceParamValue5String = workflowInstanceParamValue5String;
  }

  /**
   * param value object for 5 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue5() {
    return grouperWorkflowInstanceParamValue5;
  }

  /**
   * param value object for 5 index
   * @param grouperWorkflowInstanceParamValue5
   */
  public void setGrouperWorkflowInstanceParamValue5(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue5) {
    this.grouperWorkflowInstanceParamValue5 = grouperWorkflowInstanceParamValue5;
  }

  /**
   * string value of 6 index param
   * @return
   */
  public String getWorkflowInstanceParamValue6String() {
    return workflowInstanceParamValue6String;
  }
  
  /**
   * string value of 6 index param
   * @param workflowInstanceParamValue6String
   */
  public void setWorkflowInstanceParamValue6String(String workflowInstanceParamValue6String) {
    this.workflowInstanceParamValue6String = workflowInstanceParamValue6String;
  }

  /**
   * param value object for 6 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue6() {
    return grouperWorkflowInstanceParamValue6;
  }

  /**
   * param value object for 6 index
   * @param grouperWorkflowInstanceParamValue6
   */
  public void setGrouperWorkflowInstanceParamValue6(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue6) {
    this.grouperWorkflowInstanceParamValue6 = grouperWorkflowInstanceParamValue6;
  }

  /**
   * string value of 7 index param
   * @return
   */
  public String getWorkflowInstanceParamValue7String() {
    return workflowInstanceParamValue7String;
  }

  /**
   * string value of 7 index param
   * @param workflowInstanceParamValue7String
   */
  public void setWorkflowInstanceParamValue7String(String workflowInstanceParamValue7String) {
    this.workflowInstanceParamValue7String = workflowInstanceParamValue7String;
  }

  /**
   * param value object for 7 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue7() {
    return grouperWorkflowInstanceParamValue7;
  }

  /**
   * param value object for 7 index
   * @param grouperWorkflowInstanceParamValue7
   */
  public void setGrouperWorkflowInstanceParamValue7(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue7) {
    this.grouperWorkflowInstanceParamValue7 = grouperWorkflowInstanceParamValue7;
  }

  /**
   * string value of 8 index param
   * @return
   */
  public String getWorkflowInstanceParamValue8String() {
    return workflowInstanceParamValue8String;
  }

  /**
   * string value of 8 index param
   * @param workflowInstanceParamValue8String
   */
  public void setWorkflowInstanceParamValue8String(String workflowInstanceParamValue8String) {
    this.workflowInstanceParamValue8String = workflowInstanceParamValue8String;
  }

  /**
   * string value of 9 index param
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue8() {
    return grouperWorkflowInstanceParamValue8;
  }

  /**
   * string value of 9 index param
   * @param grouperWorkflowInstanceParamValue8
   */
  public void setGrouperWorkflowInstanceParamValue8(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue8) {
    this.grouperWorkflowInstanceParamValue8 = grouperWorkflowInstanceParamValue8;
  }

  /**
   * string value of 9 index param
   * @return
   */
  public String getWorkflowInstanceParamValue9String() {
    return workflowInstanceParamValue9String;
  }

  /**
   * string value of 9 index param
   * @param workflowInstanceParamValue9String
   */
  public void setWorkflowInstanceParamValue9String(String workflowInstanceParamValue9String) {
    this.workflowInstanceParamValue9String = workflowInstanceParamValue9String;
  }

  /**
   * param value object for 9 index
   * @return
   */
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue9() {
    return grouperWorkflowInstanceParamValue9;
  }

  /**
   * param value object for 9 index
   * @param grouperWorkflowInstanceParamValue9
   */
  public void setGrouperWorkflowInstanceParamValue9(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue9) {
    this.grouperWorkflowInstanceParamValue9 = grouperWorkflowInstanceParamValue9;
  }

  /**
   * group on which this instance is hanging off
   * @return
   */
  public GrouperObject getOwnerGrouperObject() {
    return ownerGrouperObject;
  }

  /**
   * group on which this instance is hanging off
   * @param ownerGrouperObject
   */
  public void setOwnerGrouperObject(GrouperObject ownerGrouperObject) {
    this.ownerGrouperObject = ownerGrouperObject;
  }

  /**
   * initiator subject
   * @return
   */
  public Subject getInitiatorSubject() {
    return initiatorSubject;
  }

  /**
   * initiator subject
   * @param initiatorSubject
   */
  public void setInitiatorSubject(Subject initiatorSubject) {
    this.initiatorSubject = initiatorSubject;
  }

  /**
   * build instance file info object from json string
   * @param jsonString
   * @return
   */
  public static GrouperWorkflowInstanceFilesInfo buildInstanceFileInfoFromJsonString(String jsonString){
    
    try {      
      GrouperWorkflowInstanceFilesInfo configParams = GrouperWorkflowSettings.objectMapper.readValue(jsonString, GrouperWorkflowInstanceFilesInfo.class);
      return configParams;
    } catch (Exception e) {
      LOG.error("could not convert: "+jsonString+" to GrouperWorkflowInstanceFilesInfo object");
      throw new RuntimeException("could not convert json string to GrouperWorkflowInstanceFilesInfo object", e);
    }
    
  }
  
  /**
   * build instance log entries from json string
   * @param jsonString
   * @return
   */
  public static GrouperWorkflowInstanceLogEntries buildInstanceLogEntriesFromJsonString(String jsonString) {
    try {      
      GrouperWorkflowInstanceLogEntries logEntries = GrouperWorkflowSettings.objectMapper.readValue(jsonString, GrouperWorkflowInstanceLogEntries.class);
      return logEntries;
    } catch (Exception e) {
      LOG.error("could not convert: "+jsonString+" to GrouperWorkflowInstanceLogEntries object");
      throw new RuntimeException("could not convert json string to GrouperWorkflowInstanceLogEntries object", e);
    }
  }
  
  /**
   * build param value object from json string
   * @param jsonString
   * @return
   */
  public static GrouperWorkflowInstanceParamValue buildParamValueFromJsonString(String jsonString) {
    try {      
      GrouperWorkflowInstanceParamValue paramValue = GrouperWorkflowSettings.objectMapper.readValue(jsonString, GrouperWorkflowInstanceParamValue.class);
      return paramValue;
    } catch (Exception e) {
      LOG.error("could not convert: "+jsonString+" to GrouperWorkflowInstanceParamValue object");
      throw new RuntimeException("could not convert json string to GrouperWorkflowInstanceParamValue object", e);
    }
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(attributeAssignId)
        .append(workflowInstanceUuid)
        .toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    
    if (this == other) {
      return true;
    }
    if (!(other instanceof GrouperWorkflowInstance)) {
      return false;
    }
    return new EqualsBuilder()
      .append( attributeAssignId, ( (GrouperWorkflowInstance) other ).getAttributeAssignId() )
      .append( workflowInstanceUuid, ( (GrouperWorkflowInstance) other ).getWorkflowInstanceUuid())
      .isEquals();
    
  }
    
}
