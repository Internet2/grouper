/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;


/**
 *
 */
public interface AtlassianCwdUser extends GcSqlAssignPrimaryKey {

  /**
   * id col
   * @return the id
   */
  public Long getId();

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode();

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj);

  /**
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1);

  /**
   * user_name col
   * @return the userName
   */
  public String getUserName();

  
  /**
   * user_name col
   * @param userName1 the userName to set
   */
  public void setUserName(String userName1);

  /**
   * active col
   * @return the active
   */
  public Boolean getActiveBoolean();
  
  /**
   * active col
   * @param active1 the active to set
   */
  public void setActiveBoolean(Boolean active1);

  /**
   * updated_date col
   * @return the updatedDate
   */
  public Timestamp getUpdatedDate();

  
  /**
   * updated_date col
   * @param updatedDate1 the updatedDate to set
   */
  public void setUpdatedDate(Timestamp updatedDate1);

  /**
   * directory_id col
   * @return the directoryId
   */
  public Long getDirectoryId();
  
  /**
   * directory_id col
   * @param directoryId1 the directoryId to set
   */
  public void setDirectoryId(Long directoryId1);

  /**
   * lower_user_name col
   * @return the lowerUserName
   */
  public String getLowerUserName();

  
  /**
   * lower_user_name col
   * @param lowerUserName1 the lowerUserName to set
   */
  public void setLowerUserName(String lowerUserName1);

  /**
   * created date col
   * @return the createdDate
   */
  public Timestamp getCreatedDate();

  
  /**
   * created date col
   * @param createdDate1 the createdDate to set
   */
  public void setCreatedDate(Timestamp createdDate1);

  /**
   * lower_display_name col
   * @return the lowerDisplayName
   */
  public String getLowerDisplayName();

  
  /**
   * lower_display_name col
   * @param lowerDisplayName1 the lowerDisplayName to set
   */
  public void setLowerDisplayName(String lowerDisplayName1);

  /**
   * lower_email_address col
   * @return the lowerEmailAddress
   */
  public String getLowerEmailAddress();

  
  /**
   * lower_email_address col
   * @param lowerEmailAddress1 the lowerEmailAddress to set
   */
  public void setLowerEmailAddress(String lowerEmailAddress1);
  
  /**
   * display_name col
   * @return the displayName
   */
  public String getDisplayName();

  
  /**
   * display_name col
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1);


  /**
   * email_address col
   * @return the emailAddress
   */
  public String getEmailAddress();
  
  /**
   * email_address col
   * @param emailAddress1 the emailAddress to set
   */
  public void setEmailAddress(String emailAddress1);


  /**
   * external_id col
   * @return the externalId
   */
  public String getExternalId();

  
  /**
   * external_id col
   * @param externalId1 the externalId to set
   */
  public void setExternalId(String externalId1);


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString();

  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject();
  
  /**
   * store this record insert or update
   */
  public void store();

  /**
   * delete this record
   */
  public void delete();

  
}
