package edu.internet2.middleware.grouper.app.provisioning.targetDao;


public class TargetDaoRetrieveAllDataRequest {

  private boolean includeNativeEntity;
  
  public boolean isIncludeNativeEntity() {
    return includeNativeEntity;
  }
  
  public void setIncludeNativeEntity(boolean includeNativeEntity) {
    this.includeNativeEntity = includeNativeEntity;
  }

}
