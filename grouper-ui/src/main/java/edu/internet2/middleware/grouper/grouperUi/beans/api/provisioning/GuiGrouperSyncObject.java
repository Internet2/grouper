package edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GuiGrouperSyncObject {
  
  private boolean hasDirectSettings;
  
  private GcGrouperSyncMember gcGrouperSyncMember;
  
  private GcGrouperSyncMembership gcGrouperSyncMembership;
  
  private String targetName;
  
  public boolean isHasDirectSettings() {
    return hasDirectSettings;
  }
  
  public void setHasDirectSettings(boolean hasDirectSettings) {
    this.hasDirectSettings = hasDirectSettings;
  }
  
  
  public GcGrouperSyncMember getGcGrouperSyncMember() {
    return gcGrouperSyncMember;
  }

  
  public void setGcGrouperSyncMember(GcGrouperSyncMember gcGrouperSyncMember) {
    this.gcGrouperSyncMember = gcGrouperSyncMember;
  }

  public GcGrouperSyncMembership getGcGrouperSyncMembership() {
    return gcGrouperSyncMembership;
  }
  
  public void setGcGrouperSyncMembership(GcGrouperSyncMembership gcGrouperSyncMembership) {
    this.gcGrouperSyncMembership = gcGrouperSyncMembership;
  }

  
  public String getTargetName() {
    return targetName;
  }

  
  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }
  
}
