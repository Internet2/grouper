package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import java.util.Map;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class EsbEventContainer {

  /**
   * 
   */
  private GcGrouperSyncGroup gcGrouperSyncGroup;
  
  public GcGrouperSyncGroup getGcGrouperSyncGroup() {
    return gcGrouperSyncGroup;
  }
  
  public void setGcGrouperSyncGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    this.gcGrouperSyncGroup = gcGrouperSyncGroup;
  }

  private GcGrouperSyncMember gcGrouperSyncMember;
  
  public GcGrouperSyncMember getGcGrouperSyncMember() {
    return gcGrouperSyncMember;
  }

  
  public void setGcGrouperSyncMember(GcGrouperSyncMember gcGrouperSyncMember) {
    this.gcGrouperSyncMember = gcGrouperSyncMember;
  }

  private GcGrouperSyncMembership gcGrouperSyncMembership;
  
  public GcGrouperSyncMembership getGcGrouperSyncMembership() {
    return gcGrouperSyncMembership;
  }
  
  public void setGcGrouperSyncMembership(GcGrouperSyncMembership gcGrouperSyncMembership) {
    this.gcGrouperSyncMembership = gcGrouperSyncMembership;
  }

  private String routingKey;
  
  public String getRoutingKey() {
    return routingKey;
  }

  
  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }

  private Long sequenceNumber;

  public Long getSequenceNumber() {
    return sequenceNumber;
  }
  
  public void setSequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }
  
  private EsbEvent esbEvent;
  public EsbEvent getEsbEvent() {
    return this.esbEvent;
  }
  public void setEsbEvent(EsbEvent theEsbEvent) {
    this.esbEvent = theEsbEvent;
  }
  private Map<String, Object> debugMapForEvent;
  
  public Map<String, Object> getDebugMapForEvent() {
    return debugMapForEvent;
  }
  
  public void setDebugMapForEvent(Map<String, Object> debugMapForEvent) {
    this.debugMapForEvent = debugMapForEvent;
  }
  
  private EsbEventType esbEventType;
  
  public EsbEventType getEsbEventType() {
    return esbEventType;
  }
  
  public void setEsbEventType(EsbEventType esbEventType) {
    this.esbEventType = esbEventType;
  }
  
  
  
}