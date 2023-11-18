package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;

public class GrouperProvisioningError {
  
  private Timestamp errorTimestamp;
  
  private String objectType;
  
  private String groupName;
  
  private String subjectId;
  
  private String subjectIdentifier;
  
  private boolean fatal;
  
  private String errorCode;
  
  private String errorDescription;

  
  public Timestamp getErrorTimestamp() {
    return errorTimestamp;
  }

  
  public void setErrorTimestamp(Timestamp errorTimestamp) {
    this.errorTimestamp = errorTimestamp;
  }

  
  public String getObjectType() {
    return objectType;
  }

  
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  
  public String getGroupName() {
    return groupName;
  }

  
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  
  public String getSubjectId() {
    return subjectId;
  }

  
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  
  public String getSubjectIdentifier() {
    return subjectIdentifier;
  }

  
  public void setSubjectIdentifier(String subjectIdentifier) {
    this.subjectIdentifier = subjectIdentifier;
  }

  
  public boolean isFatal() {
    return fatal;
  }

  
  public void setFatal(boolean fatal) {
    this.fatal = fatal;
  }

  
  public String getErrorCode() {
    return errorCode;
  }

  
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  
  public String getErrorDescription() {
    return errorDescription;
  }

  
  public void setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
  }
  
}
