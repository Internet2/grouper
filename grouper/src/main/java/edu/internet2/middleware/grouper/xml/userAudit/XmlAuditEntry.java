/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: XmlAuditEntry.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * represents a user audit record.  This is one unit of work that could 
 * contain multiple operations.
 */
@SuppressWarnings("serial")
public class XmlAuditEntry {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: actAsMemberId */
  public static final String FIELD_ACT_AS_MEMBER_ID = "actAsMemberId";

  /** constant for field name for: auditTypeId */
  public static final String FIELD_AUDIT_TYPE_ID = "auditTypeId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: durationMicroseconds */
  public static final String FIELD_DURATION_MICROSECONDS = "durationMicroseconds";

  /** constant for field name for: envName */
  public static final String FIELD_ENV_NAME = "envName";

  /** constant for field name for: grouperEngine */
  public static final String FIELD_GROUPER_ENGINE = "grouperEngine";

  /** constant for field name for: grouperVersion */
  public static final String FIELD_GROUPER_VERSION = "grouperVersion";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: int01 */
  public static final String FIELD_INT01 = "int01";

  /** constant for field name for: int02 */
  public static final String FIELD_INT02 = "int02";

  /** constant for field name for: int03 */
  public static final String FIELD_INT03 = "int03";

  /** constant for field name for: int04 */
  public static final String FIELD_INT04 = "int04";

  /** constant for field name for: int05 */
  public static final String FIELD_INT05 = "int05";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: loggedInMemberId */
  public static final String FIELD_LOGGED_IN_MEMBER_ID = "loggedInMemberId";

  /** constant for field name for: queryCount */
  public static final String FIELD_QUERY_COUNT = "queryCount";

  /** constant for field name for: serverHost */
  public static final String FIELD_SERVER_HOST = "serverHost";

  /** constant for field name for: serverUserName */
  public static final String FIELD_SERVER_USER_NAME = "serverUserName";

  /** constant for field name for: string01 */
  public static final String FIELD_STRING01 = "string01";

  /** constant for field name for: string02 */
  public static final String FIELD_STRING02 = "string02";

  /** constant for field name for: string03 */
  public static final String FIELD_STRING03 = "string03";

  /** constant for field name for: string04 */
  public static final String FIELD_STRING04 = "string04";

  /** constant for field name for: string05 */
  public static final String FIELD_STRING05 = "string05";

  /** constant for field name for: string06 */
  public static final String FIELD_STRING06 = "string06";

  /** constant for field name for: string07 */
  public static final String FIELD_STRING07 = "string07";

  /** constant for field name for: string08 */
  public static final String FIELD_STRING08 = "string08";

  /** constant for field name for: userIpAddress */
  public static final String FIELD_USER_IP_ADDRESS = "userIpAddress";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * construct based on xml audit type
   * @param auditEntry
   */
  public XmlAuditEntry(AuditEntry auditEntry) {
    
    //go through each field, and copy data over
    
    for (String fieldName : COPY_FIELDS) {
      Object auditEntryValue = GrouperUtil.fieldValue(auditEntry, fieldName);
      GrouperUtil.assignField(this, fieldName, auditEntryValue);
    }
    
  }
  
  /**
   * construct based on xml audit entry
   * @return the audit entry with all this data in there
   */
  public AuditEntry toAuditEntry() {
    
    AuditEntry auditEntry = new AuditEntry();
    
    //go through each field, and copy data over
    
    for (String fieldName : COPY_FIELDS) {
      Object auditTypeValue = GrouperUtil.fieldValue(this, fieldName);
      GrouperUtil.assignField(auditEntry, fieldName, auditTypeValue);
    }
    return auditEntry;
  }


  /**
   * fields which are included in copy method
   */
  private static final Set<String> COPY_FIELDS = GrouperUtil.toSet(
      FIELD_ACT_AS_MEMBER_ID, FIELD_AUDIT_TYPE_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_DESCRIPTION, FIELD_DURATION_MICROSECONDS, FIELD_ENV_NAME, FIELD_GROUPER_ENGINE, 
      FIELD_GROUPER_VERSION, FIELD_ID, FIELD_INT01, FIELD_INT02, 
      FIELD_INT03, FIELD_INT04, FIELD_INT05, FIELD_LAST_UPDATED_DB, 
      FIELD_LOGGED_IN_MEMBER_ID, FIELD_QUERY_COUNT, FIELD_SERVER_HOST, FIELD_SERVER_USER_NAME, 
      FIELD_STRING01, FIELD_STRING02, FIELD_STRING03, FIELD_STRING04, 
      FIELD_STRING05, FIELD_STRING06, FIELD_STRING07, FIELD_STRING08, 
      FIELD_USER_IP_ADDRESS);

  /**
   * construct
   */
  public XmlAuditEntry() {
    
  }

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
  private Long createdOnDb;

  /**
   * when this record was last updated 
   */
  private Long lastUpdatedDb;
  
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
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
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

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }

  
}
