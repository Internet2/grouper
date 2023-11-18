/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;
import java.util.Date;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 *
 */
public class TestgrouperSyncSubjectFrom extends GrouperAPI implements Hib3GrouperVersioned {

  /**
   * 
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    this.assignChangeFlag();
    this.addChangeLog();

  }

  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    this.addChangeLog();
  }

  /**
   * 
   */
  private void assignChangeFlag() {
    this.changeFlag = (this.netId + ", " + this.personId + ", " + this.someDate + ", " + this.someFloat + ", " + this.someInt + ", " + this.someTimestamp + ", " + this.theGroup).hashCode();
  }

  /**
   * 
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.assignChangeFlag();
    this.addChangeLog();
  }

  /**
   * add a change log for any change
   */
  private void addChangeLog() {
    TestgrouperSyncChangeLog testgrouperSyncChangeLog = new TestgrouperSyncChangeLog();
    testgrouperSyncChangeLog.setUuid(GrouperUuid.getUuid());
    testgrouperSyncChangeLog.setPersonId(this.personId);
    testgrouperSyncChangeLog.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    HibernateSession.byObjectStatic().save(testgrouperSyncChangeLog);
  }
  

  /**
   * grouping val
   */
  private String theGroup;

  /**
   * grouping val
   * @return
   */
  public String getTheGroup() {
    return this.theGroup;
  }

  /**
   * grouping val
   * @param theGroup1
   */
  public void setTheGroup(String theGroup1) {
    this.theGroup = theGroup1;
  }

  /**
   * checksum col
   */
  private Integer changeFlag;
  
  
  /**
   * checksum col
   * @return change flag
   */
  public Integer getChangeFlag() {
    return this.changeFlag;
  }

  /**
   * checksum col
   * @param changeFlag1
   */
  public void setChangeFlag(Integer changeFlag1) {
    this.changeFlag = changeFlag1;
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param personId
   * @param netId
   * @param someInt
   * @param someDate
   * @param someFloat
   * @param someTimestamp
   */
  public TestgrouperSyncSubjectFrom(Integer personId, String netId, Integer someInt,
      Date someDate, Double someFloat, Timestamp someTimestamp, Integer changeFlag, String theGroup) {
    super();
    this.personId = personId;
    this.netId = netId;
    this.someInt = someInt;
    this.someDate = someDate;
    this.someFloat = someFloat;
    this.someTimestamp = someTimestamp;
    this.changeFlag = changeFlag;
    this.theGroup = theGroup;
  }

  /**
   * 
   */
  public TestgrouperSyncSubjectFrom() {
  }

  /**
   * person id: varchar 8
   */
  private Integer personId;

  
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
   * varchar 30
   */
  private String netId;


  
  /**
   * @return the netId
   */
  public String getNetId() {
    return this.netId;
  }


  
  /**
   * @param netId the netId to set
   */
  public void setNetId(String netId) {
    this.netId = netId;
  }

  /**
   * integer 10
   */
  private Integer someInt;


  
  /**
   * @return the someInt
   */
  public Integer getSomeInt() {
    return this.someInt;
  }


  
  /**
   * @param someInt the someInt to set
   */
  public void setSomeInt(Integer someInt) {
    this.someInt = someInt;
  }
  
  /**
   * 
   */
  private Date someDate;


  
  /**
   * @return the someDate
   */
  public Date getSomeDate() {
    return this.someDate;
  }


  
  /**
   * @param someDate the someDate to set
   */
  public void setSomeDate(Date someDate) {
    this.someDate = someDate;
  }
  
  /**
   * 
   */
  private Double someFloat;


  
  /**
   * @return the someFloat
   */
  public Double getSomeFloat() {
    return this.someFloat;
  }


  
  /**
   * @param someFloat the someFloat to set
   */
  public void setSomeFloat(Double someFloat) {
    this.someFloat = someFloat;
  }

  /**
   * some timestamp
   */
  private Timestamp someTimestamp;


  
  /**
   * @return the someTimestamp
   */
  public Timestamp getSomeTimestamp() {
    return this.someTimestamp;
  }


  
  /**
   * @param someTimestamp the someTimestamp to set
   */
  public void setSomeTimestamp(Timestamp someTimestamp) {
    this.someTimestamp = someTimestamp;
  }


  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return new TestgrouperSyncSubjectFrom(this.personId, this.netId, this.someInt, this.someDate, this.someFloat, this.someTimestamp, this.changeFlag, this.theGroup);
  }

}
