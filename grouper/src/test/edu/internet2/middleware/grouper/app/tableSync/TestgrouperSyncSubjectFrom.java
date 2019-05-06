/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;
import java.util.Date;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;

/**
 *
 */
public class TestgrouperSyncSubjectFrom extends GrouperAPI implements Hib3GrouperVersioned {

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
  public TestgrouperSyncSubjectFrom(String personId, String netId, Integer someInt,
      Date someDate, Double someFloat, Timestamp someTimestamp) {
    super();
    this.personId = personId;
    this.netId = netId;
    this.someInt = someInt;
    this.someDate = someDate;
    this.someFloat = someFloat;
    this.someTimestamp = someTimestamp;
  }

  /**
   * 
   */
  public TestgrouperSyncSubjectFrom() {
  }

  /**
   * person id: varchar 8
   */
  private String personId;

  
  /**
   * @return the personId
   */
  public String getPersonId() {
    return this.personId;
  }

  
  /**
   * @param personId the personId to set
   */
  public void setPersonId(String personId) {
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
    return new TestgrouperSyncSubjectFrom(this.personId, this.netId, this.someInt, this.someDate, this.someFloat, this.someTimestamp);
  }

}
