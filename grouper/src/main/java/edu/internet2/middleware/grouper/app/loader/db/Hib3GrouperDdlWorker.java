package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class Hib3GrouperDdlWorker {

  private String id;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  private String grouper;
  
  private String workerUuid;
  
  private Timestamp heartbeat;
  
  private Timestamp lastUpdated;

  
  public String getGrouper() {
    return grouper;
  }

  
  public void setGrouper(String id) {
    this.grouper = id;
  }

  
  public String getWorkerUuid() {
    return workerUuid;
  }

  
  public void setWorkerUuid(String workerUuid) {
    this.workerUuid = workerUuid;
  }

  
  public Timestamp getHeartbeat() {
    return heartbeat;
  }

  
  public void setHeartbeat(Timestamp heartbeat) {
    this.heartbeat = heartbeat;
  }

  
  public Timestamp getLastUpdated() {
    return lastUpdated;
  }

  
  public void setLastUpdated(Timestamp lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
  
}
