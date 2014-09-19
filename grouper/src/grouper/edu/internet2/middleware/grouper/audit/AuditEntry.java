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
 * $Id: AuditEntry.java,v 1.9 2009-05-26 06:50:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAuditEntry;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;
import edu.internet2.middleware.subject.Subject;


/**
 * represents a user audit record.  This is one unit of work that could 
 * contain multiple operations.
 */
@SuppressWarnings("serial")
public class AuditEntry extends GrouperAPI implements Hib3GrouperVersioned, XmlImportable<AuditEntry> {
  
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

  /** to string deep fields */
  private static final Set<String> TO_STRING_DEEP_FIELDS = GrouperUtil.toSet(
      FIELD_ACT_AS_MEMBER_ID, FIELD_AUDIT_TYPE_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_DESCRIPTION, FIELD_DURATION_MICROSECONDS, FIELD_ENV_NAME, FIELD_GROUPER_ENGINE, 
      FIELD_GROUPER_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_INT01, 
      FIELD_INT02, FIELD_INT03, FIELD_INT04, FIELD_INT05, 
      FIELD_LAST_UPDATED_DB, FIELD_LOGGED_IN_MEMBER_ID, FIELD_QUERY_COUNT, FIELD_SERVER_HOST, 
      FIELD_SERVER_USER_NAME, FIELD_STRING01, FIELD_STRING02, FIELD_STRING03, 
      FIELD_STRING04, FIELD_STRING05, FIELD_STRING06, FIELD_STRING07, 
      FIELD_STRING08, FIELD_USER_IP_ADDRESS);

  /**
   * see if one audit type is the same as another (not looking at last update, id, etc)
   * @param auditEntry
   * @return true if equals, false if not
   */
  public boolean equalsDeep(AuditEntry auditEntry) {
    
    return new EqualsBuilder().append(this.actAsMemberId, auditEntry.actAsMemberId)
      .append(this.auditTypeId, auditEntry.auditTypeId)
      .append(this.contextId, auditEntry.contextId)
      .append(this.createdOnDb, auditEntry.createdOnDb)
      .append(this.description, auditEntry.description)
      .append(this.durationMicroseconds, auditEntry.durationMicroseconds)
      .append(this.envName, auditEntry.envName)
      .append(this.grouperEngine, auditEntry.grouperEngine)
      .append(this.grouperVersion, auditEntry.grouperVersion)
      .append(this.id, auditEntry.id)
      .append(this.int01, auditEntry.int01)
      .append(this.int02, auditEntry.int02)
      .append(this.int03, auditEntry.int03)
      .append(this.int04, auditEntry.int04)
      .append(this.int05, auditEntry.int05)
      //not last updated
      .append(this.loggedInMemberId, auditEntry.loggedInMemberId)
      .append(this.queryCount, auditEntry.queryCount)
      .append(this.serverHost, auditEntry.serverHost)
      .append(this.serverUserName, auditEntry.serverUserName)
      .append(this.string01, auditEntry.string01)
      .append(this.string02, auditEntry.string02)
      .append(this.string03, auditEntry.string03)
      .append(this.string04, auditEntry.string04)
      .append(this.string05, auditEntry.string05)
      .append(this.string06, auditEntry.string06)
      .append(this.string07, auditEntry.string07)
      .append(this.string08, auditEntry.string08)
      .append(this.userIpAddress, auditEntry.userIpAddress)
      .isEquals();
      
  }

//  public Set<String> getRelatedGroupNames() {
//    
//  }
//  
//  public Set<String> getRelatedStemNames() {
//    
//  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "Audit entry: " + StringUtils.substring(this.description, 0, 30);
  }

  /**
   * get the audit type, it better be there
   * @return the audit type
   */
  public AuditType getAuditType() {
    return AuditTypeFinder.find(this.auditTypeId, true);
  }
  
  /**
   * 
   * @param extended if all fields should be printed
   * @return the report
   */
  public String toStringReport(boolean extended) {
    StringBuilder result = new StringBuilder();
    AuditType auditType = this.getAuditType();
    Timestamp lastUpdated = this.getLastUpdated();
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String lastUpdatedString = simpleDateFormat.format(lastUpdated);
    
    result.append(lastUpdatedString).append(" ").append(StringUtils.rightPad(auditType.getAuditCategory(), 12))
      .append(" - ").append(StringUtils.rightPad(auditType.getActionName(), 20))
      .append(" (").append(StringUtils.leftPad(Long.toString(this.getDurationMicroseconds()/1000), 6))
      .append("ms, ").append(StringUtils.leftPad(Integer.toString(this.getQueryCount()), 3)).append(" queries)\n");
    
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    
    if (!StringUtils.isBlank(this.loggedInMemberId)) {
      result.append("  ").append(StringUtils.rightPad("Logged in user:", 20));
      Member loggedInMember = MemberFinder.findByUuid(grouperSession, this.loggedInMemberId, false);
      String loggedInMemberString = subjectToString(loggedInMember);
      result.append(loggedInMemberString);
      
      if (!StringUtils.isBlank(this.userIpAddress)) {
        result.append(" (ip: ").append(this.userIpAddress).append(")");
      }
      
      if (!StringUtils.isBlank(this.actAsMemberId) && !StringUtils.equals(this.actAsMemberId, this.loggedInMemberId)) {
        result.append(" (actAs: ");
        Member actAsMember = MemberFinder.findByUuid(grouperSession, this.actAsMemberId, false);
        String actAsMemberString = subjectToString(actAsMember);
        result.append(actAsMemberString);
        result.append(")");
      }
      result.append("\n");
    }

    if (!StringUtils.isBlank(this.description)) {
      result.append("  ").append(StringUtils.rightPad("Description:", 20))
        .append(StringUtils.abbreviate(this.description, 200)).append("\n");
    }
    result.append("  ").append(StringUtils.rightPad("Server:", 20));
    result.append(this.grouperEngine).append(", host: ").append(this.serverHost);
    result.append(", user: ").append(this.serverUserName);
    result.append("\n");
    
    if (extended) {
      
      for (String label: auditType.labels()) {
        
        //see if there is data
        String fieldName = auditType.retrieveAuditEntryFieldForLabel(label);
        Object value = GrouperUtil.fieldValue(this, fieldName);
        String valueString = GrouperUtil.stringValue(value);
        if (!StringUtils.isBlank(valueString)) {
          
          result.append("  ").append(StringUtils.rightPad(StringUtils.capitalize(label) + ":", 20)).append(value).append("\n");
          
        }
      }
    }
    
    return result.toString();
  }
  
  /**
   * convert a subject to a string
   * @param member
   * @return the string
   */
  private static String subjectToString(Member member) {
    StringBuilder result = new StringBuilder();
    result.append(member.getSubjectSourceIdDb()).append(" - ").append(member.getSubjectIdDb())
      .append(" - ");
    try {
      Subject subject = member.getSubject();
      String more = subject.getAttributeValue("description");
      if (StringUtils.isBlank(more)) {
        more = subject.getDescription();
      }
      if (StringUtils.isBlank(more)) {
        more = subject.getAttributeValue("name");
      }
      if (StringUtils.isBlank(more)) {
        more = subject.getName();
      }
      
      if (!StringUtils.isBlank(more)) {
        result.append(more);
      }
      
    } catch (Exception e) {
      result.append(" problem with subject: " + e.getMessage());
    }
    return result.toString();
  }
  
  /**
   * construct
   */
  public AuditEntry() {
    this.id = GrouperUuid.getUuid();

  }

  /**
   * save or update this object
   * @param copyContextData 
   */
  public void saveOrUpdate(boolean copyContextData) {
    if (copyContextData) {
      GrouperContext.assignAuditEntryFields(this);
    }
    if (!GrouperLoader.isDryRun()) {
      GrouperDAOFactory.getFactory().getAuditEntry().saveOrUpdate(this);
    }
  }
  
  /**
   * construct, assign an id
   * @param auditTypeIdentifier points to audit type
   * @param labelNamesAndValues alternate label name and value
   */
  public AuditEntry(AuditTypeIdentifier auditTypeIdentifier, 
      String... labelNamesAndValues) {
    
    this.id = GrouperUuid.getUuid();
    
    AuditType auditType = AuditTypeFinder.find(auditTypeIdentifier.getAuditCategory(),
        auditTypeIdentifier.getActionName(), true);
    
    this.auditTypeId = auditType.getId();
    
    int labelNamesAndValuesLength = GrouperUtil.length(labelNamesAndValues);
    
    if (labelNamesAndValuesLength % 2 != 0) {
      throw new RuntimeException("labelNamesAndValuesLength must be divisible by 2: " 
          + labelNamesAndValuesLength);
    }
    
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
      throw new RuntimeException("Cant find string label: '" + label 
          + "' in audit type: " + auditType.getAuditCategory() + " - " + auditType.getActionName());
    }
  }
  
  /**
   * @param auditType
   * @param label
   * @param value
   */
  public void assignIntValue(AuditType auditType, String label, Long value) {
    if (StringUtils.equals(label, auditType.getLabelInt01())) {
      this.int01 = value;
    } else if (StringUtils.equals(label, auditType.getLabelInt02())) {
      this.int02 = value;
    } else if (StringUtils.equals(label, auditType.getLabelInt03())) {
      this.int03 = value;
    } else if (StringUtils.equals(label, auditType.getLabelInt04())) {
      this.int04 = value;
    } else if (StringUtils.equals(label, auditType.getLabelInt05())) {
      this.int05 = value;
      throw new RuntimeException("Cant find int label: '" + label 
          + "' in audit type: " + auditType.getAuditCategory() + " - " + auditType.getActionName());
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
    if (this.lastUpdatedDb == null) {
      this.lastUpdatedDb = System.currentTimeMillis();
    }
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
    if (this.actAsMemberId == null && this.loggedInMemberId != null) {
      this.actAsMemberId = this.loggedInMemberId;
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.setLastUpdatedDb(System.currentTimeMillis());
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

  /**
   * the string repre
   * @return string 
   */
  public String toStringDeep() {
    return GrouperUtil.toStringFields(this, TO_STRING_DEEP_FIELDS);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public XmlImportable<AuditEntry> xmlRetrieveByIdOrKey() {
    //in this case we are only going to find audits by id...
    return GrouperDAOFactory.getFactory().getAuditEntry().findById(this.id, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AuditEntry existingRecord) {
    existingRecord.setActAsMemberId(this.actAsMemberId);
    existingRecord.setAuditTypeId(this.auditTypeId);
    existingRecord.setContextId(this.contextId);
    existingRecord.setDescription(this.description);
    existingRecord.setDurationMicroseconds(this.durationMicroseconds);
    existingRecord.setEnvName(this.envName);
    existingRecord.setGrouperEngine(this.grouperEngine);
    existingRecord.setGrouperVersion(this.grouperVersion);
    existingRecord.setId(this.id);
    existingRecord.setInt01(this.int01);
    existingRecord.setInt02(this.int02);
    existingRecord.setInt03(this.int03);
    existingRecord.setInt04(this.int04);
    existingRecord.setInt05(this.int05);
    existingRecord.setLoggedInMemberId(this.loggedInMemberId);
    existingRecord.setQueryCount(this.queryCount);
    existingRecord.setServerHost(this.serverHost);
    existingRecord.setServerUserName(this.serverUserName);
    existingRecord.setString01(this.string01);
    existingRecord.setString02(this.string02);
    existingRecord.setString03(this.string03);
    existingRecord.setString04(this.string04);
    existingRecord.setString05(this.string05);
    existingRecord.setString06(this.string06);
    existingRecord.setString07(this.string07);
    existingRecord.setString08(this.string08);
    existingRecord.setUserIpAddress(this.userIpAddress);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AuditEntry other) {
    if (!StringUtils.equals(this.actAsMemberId, other.actAsMemberId)) {
      return true;
    }
    if (!StringUtils.equals(this.auditTypeId, other.auditTypeId)) {
      return true;
    }
    if (!StringUtils.equals(this.description, other.description)) {
      return true;
    }
    if (this.durationMicroseconds != other.durationMicroseconds) {
      return true;
    }
    if (!StringUtils.equals(this.envName, other.envName)) {
      return true;
    }
    if (!StringUtils.equals(this.grouperEngine, other.grouperEngine)) {
      return true;
    }
    if (!StringUtils.equals(this.grouperVersion, other.grouperVersion)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!GrouperUtil.equals(this.int01, other.int01)) {
      return true;
    }
    if (!GrouperUtil.equals(this.int02, other.int02)) {
      return true;
    }
    if (!GrouperUtil.equals(this.int03, other.int03)) {
      return true;
    }
    if (!GrouperUtil.equals(this.int04, other.int04)) {
      return true;
    }
    if (!GrouperUtil.equals(this.int05, other.int05)) {
      return true;
    }
    if (!StringUtils.equals(this.loggedInMemberId, other.loggedInMemberId)) {
      return true;
    }
    if (this.queryCount != other.queryCount) {
      return true;
    }
    if (!StringUtils.equals(this.serverHost, other.serverHost)) {
      return true;
    }
    if (!StringUtils.equals(this.serverUserName, other.serverUserName)) {
      return true;
    }
    if (!StringUtils.equals(this.string01, other.string01)) {
      return true;
    }
    if (!StringUtils.equals(this.string02, other.string02)) {
      return true;
    }
    if (!StringUtils.equals(this.string03, other.string03)) {
      return true;
    }
    if (!StringUtils.equals(this.string04, other.string04)) {
      return true;
    }
    if (!StringUtils.equals(this.string05, other.string05)) {
      return true;
    }
    if (!StringUtils.equals(this.string06, other.string06)) {
      return true;
    }
    if (!StringUtils.equals(this.string07, other.string07)) {
      return true;
    }
    if (!StringUtils.equals(this.string08, other.string08)) {
      return true;
    }
    if (!StringUtils.equals(this.userIpAddress, other.userIpAddress)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AuditEntry other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.createdOnDb, other.createdOnDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    if (!GrouperUtil.equals(this.lastUpdatedDb, other.lastUpdatedDb)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AuditEntry xmlSaveBusinessProperties(AuditEntry existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      existingRecord = new AuditEntry();
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.saveOrUpdate(false);
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAuditEntry().saveUpdateProperties(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAuditEntry xmlToExportAuditEntry(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportAuditEntry xmlExportAuditEntry = new XmlExportAuditEntry();
    xmlExportAuditEntry.setActAsMemberId(this.getActAsMemberId());
    xmlExportAuditEntry.setAuditTypeId(this.getAuditTypeId());
    xmlExportAuditEntry.setContextId(this.getContextId());
    xmlExportAuditEntry.setCreatedOn(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAuditEntry.setDescription(this.getDescription());
    xmlExportAuditEntry.setDurationMicroseconds(this.getDurationMicroseconds());
    xmlExportAuditEntry.setEnvName(this.getEnvName());
    xmlExportAuditEntry.setGrouperEngine(this.getGrouperEngine());
    xmlExportAuditEntry.setGrouperVersion(this.getGrouperVersion());
    xmlExportAuditEntry.setHibernateVersionNumber(GrouperUtil.longValue(this.getHibernateVersionNumber(), 0));
    xmlExportAuditEntry.setId(this.getId());
    xmlExportAuditEntry.setInt01(this.getInt01());
    xmlExportAuditEntry.setInt02(this.getInt02());
    xmlExportAuditEntry.setInt03(this.getInt03());
    xmlExportAuditEntry.setInt04(this.getInt04());
    xmlExportAuditEntry.setInt05(this.getInt05());
    xmlExportAuditEntry.setLastUpdated(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAuditEntry.setLoggedInMemberId(this.getLoggedInMemberId());
    xmlExportAuditEntry.setQueryCount(this.getQueryCount());
    xmlExportAuditEntry.setServerHost(this.getServerHost());
    xmlExportAuditEntry.setServerUserName(this.getServerUserName());
    xmlExportAuditEntry.setString01(this.getString01());
    xmlExportAuditEntry.setString02(this.getString02());
    xmlExportAuditEntry.setString03(this.getString03());
    xmlExportAuditEntry.setString04(this.getString04());
    xmlExportAuditEntry.setString05(this.getString05());
    xmlExportAuditEntry.setString06(this.getString06());
    xmlExportAuditEntry.setString07(this.getString07());
    xmlExportAuditEntry.setString08(this.getString08());
    xmlExportAuditEntry.setUserIpAddress(this.getUserIpAddress());
    return xmlExportAuditEntry;
  }

  /**
   * get a string value from name value pairs
   * @param label
   * 
   * @return value
   */
  public String retrieveStringValue(String label) {
    return this.retrieveStringValue(this.getAuditType(), label);
  }
  
  /**
   * get a string value from name value pairs
   * @param auditType
   * @param label
   * @return value
   */
  public String retrieveStringValue(AuditType auditType, String label) {
    if (StringUtils.equals(label, auditType.getLabelString01())) {
      return this.string01;
    } else if (StringUtils.equals(label, auditType.getLabelString02())) {
      return this.string02;
    } else if (StringUtils.equals(label, auditType.getLabelString03())) {
      return this.string03;
    } else if (StringUtils.equals(label, auditType.getLabelString04())) {
      return this.string04;
    } else if (StringUtils.equals(label, auditType.getLabelString05())) {
      return this.string05;
    } else if (StringUtils.equals(label, auditType.getLabelString06())) {
      return this.string06;
    } else if (StringUtils.equals(label, auditType.getLabelString07())) {
      return this.string07;
    } else if (StringUtils.equals(label, auditType.getLabelString08())) {
      return this.string08;
    } else {
      throw new RuntimeException("Cant find string label: '" + label 
          + "' in audit type: " + auditType.getAuditCategory() + " - " + auditType.getActionName());
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("AuditEntry: " + this.getId());
    
//    XmlExportUtils.toStringAuditType(null, stringWriter, this.getAuditTypeId(), false);
    
    return stringWriter.toString();
    
  }

}
