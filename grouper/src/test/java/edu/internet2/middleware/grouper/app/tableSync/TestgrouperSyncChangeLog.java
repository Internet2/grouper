/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;

/**
 *
 */
public class TestgrouperSyncChangeLog extends GrouperAPI implements Hib3GrouperVersioned {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param personId
   * @param uuid
   * @param lastUpdated
   */
  public TestgrouperSyncChangeLog(String uuid, Integer personId, Timestamp lastUpdated) {
    super();
    this.personId = personId;
    this.uuid = uuid;
    this.lastUpdated = lastUpdated;
  }

  /**
   * 
   */
  public TestgrouperSyncChangeLog() {
  }

  /**
   * person id: varchar 8
   */
  private Integer personId;

  /**
   * uuid of this record
   */
  private String uuid;

  /**
   * uuid of this record
   * @return uuid 
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of this record
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * last updated
   */
  private Timestamp lastUpdated;  

  /**
   * last updated
   * @return last updated
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdated;
  }

  /**
   * last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  /**
   * @return the personId
   */
  public Integer getPersonId() {
    return this.personId;
  }

  
  /**
   * @param personId the personId to set
   */
  public void setPersonId(Integer personId) {
    this.personId = personId;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return new TestgrouperSyncChangeLog(this.uuid, this.personId, this.lastUpdated);
  }

}
