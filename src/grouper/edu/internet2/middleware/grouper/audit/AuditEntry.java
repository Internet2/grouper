/*
 * @author mchyzer
 * $Id: AuditEntry.java,v 1.4 2009-02-08 21:30:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * represents a user audit record.  This is one unit of work that could 
 * contain multiple operations.
 */
@SuppressWarnings("serial")
public class AuditEntry extends GrouperAPI implements Hib3GrouperVersioned {
  
  /**
   * construct
   */
  public AuditEntry() {
    
  }

  /**
   * save or update this object
   * @param copyContextData 
   */
  public void saveOrUpdate(boolean copyContextData) {
    if (copyContextData) {
      GrouperContext.assignAuditEntryFields(this);
    }
    GrouperDAOFactory.getFactory().getAuditEntry().saveOrUpdate(this);
  }
  
  /**
   * construct, assign an id
   * @param auditTypeIdentifier points to audit type
   * @param labelNamesAndValues alternate label name and value
   */
  public AuditEntry(AuditTypeIdentifier auditTypeIdentifier, 
      String... labelNamesAndValues) {
    
    this.id = GrouperUuid.getUuid();
    
    this.auditTypeId = auditTypeIdentifier.getId();
    
    int labelNamesAndValuesLength = GrouperUtil.length(labelNamesAndValues);
    
    if (labelNamesAndValuesLength % 2 != 0) {
      throw new RuntimeException("labelNamesAndValuesLength must be divisible by 2: " 
          + labelNamesAndValuesLength);
    }
    
    AuditType auditType = AuditTypeFinder.find(this.auditTypeId, true);
    
    for (int i=0;i<labelNamesAndValuesLength;i+=2) {
      String label = labelNamesAndValues[i];
      String value = labelNamesAndValues[i+1];

      assignStringValue(auditType, label, value);
    }
  }

  /**
   * @param auditType
   * @param label
   * @param value
   */
  public void assignStringValue(AuditType auditType, String label, String value) {
    if (StringUtils.equals(label, auditType.getLabelString01())) {
      this.string01 = value;
    } else if (StringUtils.equals(label, auditType.getLabelString02())) {
      this.string02 = value;
    } else if (StringUtils.equals(label, auditType.getLabelString03())) {
      this.string03 = value;
    } else if (StringUtils.equals(label, auditType.getLabelString04())) {
      this.string04 = value;
    } else if (StringUtils.equals(label, auditType.getLabelString05())) {
      this.string05 = value;
    } else if (StringUtils.equals(label, auditType.getLabelString06())) {
      this.string06 = value;
    } else if (StringUtils.equals(label, auditType.getLabelString07())) {
      this.string07 = value;
    } else if (StringUtils.equals(label, auditType.getLabelString08())) {
      this.string08 = value;
    } else {
      throw new RuntimeException("Cant find string label: " + label 
          + " in audit type: " + auditType.getAuditCategory() + " - " + auditType.getActionName());
    }
  }
  
  /** name of the grouper audit entry table in the db */
  public static final String TABLE_GROUPER_AUDIT_ENTRY = "grouper_audit_entry";

  /** primary key uuid of this record */
  private String id;
  
  /** foreign key to the type of audit entry this is */
  private String auditTypeId;

  /** env label from grouper.properties */
  private String envName;
  
  /** WS, UI, loader, GSH, etc */
  private String grouperEngine;
  
  /** version of the grouper API, e.g. 1.4.0 */
  private String grouperVersion;
  
  /**
   * member uuid of the user being acted as. 
   */
  private String actAsMemberId;

  /**
   * context id ties multiple db changes  
   */
  private String contextId;

  /**
   * member uuid of the user logged in to grouper ui or ws etc
   */
  private String loggedInMemberId;

  /**
   * host of the server that executed the transaction
   */
  private String serverHost;

  /**
   * ip address of user (from WS or UI etc)
   */
  private String userIpAddress;

  /**
   * Username of the OS user running the API.  This might identify who ran a GSH call
   */
  private String serverUserName;
  
  /**
   * number of microseconds that the duration of the context took
   */
  private long durationMicroseconds;
  
  /**
   * number of queries (count be db or otherwise)
   */
  private int queryCount;
  
  /**
   * description of what happened in paragraph form
   */
  private String description;
  
  /**
   * misc field 1
   */
  private String string01;
  
  /**
   * misc field 2
   */
  private String string02;
  
  /**
   * misc field 3
   */
  private String string03;
  
  /**
   * misc field 4
   */
  private String string04;
  
  /**
   * misc field 5
   */
  private String string05;
  
  /**
   * misc field 6
   */
  private String string06;
  
  /**
   * misc field 7
   */
  private String string07;
  
  /**
   * misc field 8
   */
  private String string08;

  /**
   * misc int field 1
   */
  private Long int01;
  
  /**
   * misc int field 2
   */
  private Long int02;
  
  /**
   * misc int field 3
   */
  private Long int03;
  
  /**
   * misc int field 4
   */
  private Long int04;
  
  /**
   * misc int field 5
   */
  private Long int05;

  /**
   * when this record was created 
   */
  private Timestamp createdOn;

  /**
   * when this record was last updated 
   */
  private Timestamp lastUpdated;
  
  /**
   * foreign key to the type of audit entry this is
   * @return the audit type id
   */
  public String getAuditTypeId() {
    return this.auditTypeId;
  }

  /**
   * foreign key to the type of audit entry this is
   * @param auditTypeId1
   */
  public void setAuditTypeId(String auditTypeId1) {
    this.auditTypeId = auditTypeId1;
  }

  /**
   * primary key uuid of this record
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * primary key uuid of this record
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * member uuid of the user being acted as
   * @return uuid
   */
  public String getActAsMemberId() {
    return this.actAsMemberId;
  }

  /**
   * context id ties multiple db changes
   * @return id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * member uuid of the user logged in to grouper ui or ws etc
   * @return uuid
   */
  public String getLoggedInMemberId() {
    return this.loggedInMemberId;
  }

  /**
   * host of the server that executed the transaction
   * @return host
   */
  public String getServerHost() {
    return this.serverHost;
  }

  /**
   * ip address of user (from WS or UI etc)
   * @return user ip address
   */
  public String getUserIpAddress() {
    return this.userIpAddress;
  }

  /**
   * member uuid of the user being acted as
   * @param actAsMemberUuid1
   */
  public void setActAsMemberId(String actAsMemberUuid1) {
    this.actAsMemberId = actAsMemberUuid1;
  }

  /**
   * context id ties multiple db changes
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * member uuid of the user logged in to grouper ui or ws etc
   * @param loggedInMemberUuid
   */
  public void setLoggedInMemberId(String loggedInMemberUuid) {
    this.loggedInMemberId = loggedInMemberUuid;
  }

  /**
   * host of the server that executed the transaction
   * @param serverHost1
   */
  public void setServerHost(String serverHost1) {
    this.serverHost = serverHost1;
  }

  /**
   * ip address of user (from WS or UI etc)
   * @param userIpAddress1
   */
  public void setUserIpAddress(String userIpAddress1) {
    this.userIpAddress = userIpAddress1;
  }

  /**
   * env label from grouper.properties
   * @return env label
   */
  public String getEnvName() {
    return this.envName;
  }

  /**
   * env label from grouper.properties
   * @param envLabel1
   */
  public void setEnvName(String envLabel1) {
    this.envName = envLabel1;
  }

  /**
   * WS, UI, loader, GSH, etc
   * @return grouper system
   */
  public String getGrouperEngine() {
    return this.grouperEngine;
  }

  /**
   * WS, UI, loader, GSH, etc
   * @param grouperSystem1
   */
  public void setGrouperEngine(String grouperSystem1) {
    this.grouperEngine = grouperSystem1;
  }

  /**
   * misc field 1
   * @return field
   */
  public String getString01() {
    return this.string01;
  }

  /**
   * misc field 1
   * @param string01a
   */
  public void setString01(String string01a) {
    this.string01 = string01a;
  }

  /**
   * misc field 2
   * @return field
   */
  public String getString02() {
    return this.string02;
  }

  /**
   * misc field 2
   * @param string02a
   */
  public void setString02(String string02a) {
    this.string02 = string02a;
  }

  /**
   * misc field 3
   * @return field
   */
  public String getString03() {
    return this.string03;
  }

  /**
   * misc field 3
   * @param string03a
   */
  public void setString03(String string03a) {
    this.string03 = string03a;
  }

  /**
   * misc field 4
   * @return field
   */
  public String getString04() {
    return this.string04;
  }

  /**
   * misc field 4
   * @param string04a
   */
  public void setString04(String string04a) {
    this.string04 = string04a;
  }

  /**
   * misc field 5
   * @return field
   */
  public String getString05() {
    return this.string05;
  }

  /**
   * misc field 5
   * @param string05a
   */
  public void setString05(String string05a) {
    this.string05 = string05a;
  }

  /**
   * misc field 6
   * @return field
   */
  public String getString06() {
    return this.string06;
  }

  /**
   * misc field 6
   * @param string06a
   */
  public void setString06(String string06a) {
    this.string06 = string06a;
  }

  /**
   * misc field 7
   * @return field
   */
  public String getString07() {
    return this.string07;
  }

  /**
   * misc field 7
   * @param string07a
   */
  public void setString07(String string07a) {
    this.string07 = string07a;
  }

  /**
   * misc field 8
   * @return field
   */
  public String getString08() {
    return this.string08;
  }

  /**
   * misc field 8
   * @param string08a
   */
  public void setString08(String string08a) {
    this.string08 = string08a;
  }

  /**
   * misc integer field 1
   * @return field
   */
  public Long getInt01() {
    return this.int01;
  }

  /**
   * misc integer field 1
   * @param int01a
   */
  public void setInt01(Long int01a) {
    this.int01 = int01a;
  }

  /**
   * misc integer field 2
   * @return field
   */
  public Long getInt02() {
    return this.int02;
  }

  /**
   * misc integer field 2
   * @param int02a
   */
  public void setInt02(Long int02a) {
    this.int02 = int02a;
  }

  /**
   * misc integer field 3
   * @return field
   */
  public Long getInt03() {
    return this.int03;
  }

  /**
   * misc integer field 3
   * @param int03a
   */
  public void setInt03(Long int03a) {
    this.int03 = int03a;
  }

  /**
   * misc integer field 4
   * @return field
   */
  public Long getInt04() {
    return this.int04;
  }

  /**
   * misc integer field 4
   * @param int04a
   */
  public void setInt04(Long int04a) {
    this.int04 = int04a;
  }

  /**
   * misc integer field 5
   * @return field
   */
  public Long getInt05() {
    return this.int05;
  }

  /**
   * misc integer field 5
   * @param int05a
   */
  public void setInt05(Long int05a) {
    this.int05 = int05a;
  }

  /**
   * description of what happened in paragraph form
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * description of what happened in paragraph form
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOn;
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdated;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOn = createdOn1;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  /**
   * make sure this object will fit in the DB
   */
  public void truncate() {
    this.actAsMemberId = GrouperUtil.truncateAscii(this.actAsMemberId, 128);
    this.auditTypeId = GrouperUtil.truncateAscii(this.auditTypeId, 128);
    this.contextId = GrouperUtil.truncateAscii(this.contextId, 128);
    this.description = GrouperUtil.truncateAscii(this.description, 4000);
    this.envName = GrouperUtil.truncateAscii(this.envName, 50);
    this.grouperEngine = GrouperUtil.truncateAscii(this.grouperEngine, 50);
    this.grouperVersion = GrouperUtil.truncateAscii(this.grouperVersion, 20);
    this.id = GrouperUtil.truncateAscii(this.id, 128);
    this.loggedInMemberId = GrouperUtil.truncateAscii(this.loggedInMemberId, 128);
    this.serverHost = GrouperUtil.truncateAscii(this.serverHost, 50);
    this.serverUserName = GrouperUtil.truncateAscii(this.serverUserName, 50);
    this.string01 = GrouperUtil.truncateAscii(this.string01, 4000);
    this.string02 = GrouperUtil.truncateAscii(this.string02, 4000);
    this.string03 = GrouperUtil.truncateAscii(this.string03, 4000);
    this.string04 = GrouperUtil.truncateAscii(this.string04, 4000);
    this.string05 = GrouperUtil.truncateAscii(this.string05, 4000);
    this.string06 = GrouperUtil.truncateAscii(this.string06, 4000);
    this.string07 = GrouperUtil.truncateAscii(this.string07, 4000);
    this.string08 = GrouperUtil.truncateAscii(this.string08, 4000);
    this.userIpAddress = GrouperUtil.truncateAscii(this.userIpAddress, 50);
  }

  /**
   * version of the grouper API, e.g. 1.4.0
   * @return version
   */
  public String getGrouperVersion() {
    return this.grouperVersion;
  }

  /**
   * version of the grouper API, e.g. 1.4.0
   * @param grouperVersion1
   */
  public void setGrouperVersion(String grouperVersion1) {
    this.grouperVersion = grouperVersion1;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    throw new RuntimeException("not implemented");
  }

  /**
   * number of nanos that the duration of the context took
   * @return duration nanos
   */
  public long getDurationMicroseconds() {
    return this.durationMicroseconds;
  }

  /**
   * number of nanos that the duration of the context took
   * @param durationMicroseconds1
   */
  public void setDurationMicroseconds(long durationMicroseconds1) {
    this.durationMicroseconds = durationMicroseconds1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    this.setLastUpdated(new Timestamp(System.currentTimeMillis()));
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.setLastUpdated(new Timestamp(System.currentTimeMillis()));
  }

  /**
   * number of queries (count be db or otherwise)
   * @return query count
   */
  public int getQueryCount() {
    return this.queryCount;
  }

  /**
   * number of queries (count be db or otherwise)
   * @param queryCount
   */
  public void setQueryCount(int queryCount) {
    this.queryCount = queryCount;
  }

  /**
   * Username of the OS user running the API.  This might identify who ran a GSH call
   * @return server user name
   */
  public String getServerUserName() {
    return this.serverUserName;
  }

  /**
   * Username of the OS user running the API.  This might identify who ran a GSH call
   * @param serverUserName1
   */
  public void setServerUserName(String serverUserName1) {
    this.serverUserName = serverUserName1;
  }

}
