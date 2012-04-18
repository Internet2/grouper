/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: XmlAuditEntry.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.Dom4JReader;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;



/**
 * represents a user audit record.  This is one unit of work that could 
 * contain multiple operations.
 */
@SuppressWarnings("serial")
public class XmlExportAuditEntry {
  
  /**
   * construct
   */
  public XmlExportAuditEntry() {
    
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
  private String createdOn;

  /**
   * when this record was last updated 
   */
  private String lastUpdated;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAuditEntry.class);

  /**
   * 
   */
  private static final String AUDIT_ENTRIES_XPATH = "/grouperExport/auditEntries";

  /**
   * 
   */
  private static final String XML_EXPORT_AUDIT_ENTRY_XPATH = "/grouperExport/auditEntries/XmlExportAuditEntry";
  
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
   * @param grouperEngine1
   */
  public void setGrouperEngine(String grouperEngine1) {
    this.grouperEngine = grouperEngine1;
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
   * when last updated
   * @return timestamp
   */
  public String getLastUpdated() {
    return this.lastUpdated;
  }

  /**
   * when created
   * @return timestamp
   */
  public String getCreatedOn() {
    return this.createdOn;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(String createdOn1) {
    this.createdOn = createdOn1;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(String lastUpdated1) {
    this.lastUpdated = lastUpdated1;
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
   * get db count
   * @return db count
   */
  public static long dbCount() {
    long result = HibernateSession.byHqlStatic().createQuery("select count(*) from AuditEntry").uniqueResult(Long.class);
    return result;
  }

  /**
   * Username of the OS user running the API.  This might identify who ran a GSH call
   * @param serverUserName1
   */
  public void setServerUserName(String serverUserName1) {
    this.serverUserName = serverUserName1;
  }

  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportAuditEntries(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the audit entries
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all audit entries in order
        Query query = session.createQuery(
            "select theAuditEntry from AuditEntry as theAuditEntry order by theAuditEntry.lastUpdatedDb");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <auditEntries>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AuditEntry auditEntry = (AuditEntry)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");

                      XmlExportUtils.toStringAuditType(null, writer, auditEntry.getAuditTypeId(), false);

                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }

              
              XmlExportAuditEntry xmlExportAuditEntry = auditEntry.xmlToExportAuditEntry(grouperVersion);
              writer.write("    ");
              xmlExportAuditEntry.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }

          //end the audit entries element 
          writer.write("  </auditEntries>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming audit entries", ioe);
        }
        return null;
      }
    });
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export audit entry
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAuditEntry fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAuditEntry xmlExportAuditEntry = (XmlExportAuditEntry)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAuditEntry;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAuditEntry fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAuditEntry xmlExportAuditEntry = (XmlExportAuditEntry)xStream.fromXML(xml);
  
    return xmlExportAuditEntry;
  }

  /**
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( AUDIT_ENTRIES_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
            }
        }
    );
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_AUDIT_ENTRY_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
                // do nothing here...    
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
  
                xmlImportMain.incrementTotalImportFileCount();
            }
        }
    );
  
  }

  /**
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlSecondPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( AUDIT_ENTRIES_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
            }
        }
    );
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_AUDIT_ENTRY_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
                // do nothing here...    
            }
            public void onEnd(ElementPath path) {
  
              Element row = null;
              try {
                // process a ROW element
                row = path.getCurrent();
  
                // prune the tree
                row.detach();
  
                XmlExportAuditEntry xmlExportAuditEntryFromFile = (XmlExportAuditEntry)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                AuditEntry auditEntry = xmlExportAuditEntryFromFile.toAuditEntry();
                
                XmlExportUtils.syncImportable(auditEntry, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing audit entry: " + XmlExportUtils.toString(row), re);
                throw re;
              }
            }
        }
    );
  
  }

  /**
   * convert to audit entry
   * @return the audit entry
   */
  public AuditEntry toAuditEntry() {
    AuditEntry auditEntry = new AuditEntry();
    auditEntry.setActAsMemberId(this.actAsMemberId);
    auditEntry.setAuditTypeId(this.auditTypeId);
    auditEntry.setContextId(this.contextId);
    auditEntry.setCreatedOnDb(GrouperUtil.dateLongValue(this.createdOn));
    auditEntry.setDescription(this.description);
    auditEntry.setDurationMicroseconds(this.durationMicroseconds);
    auditEntry.setEnvName(this.envName);
    auditEntry.setGrouperEngine(this.grouperEngine);
    auditEntry.setGrouperVersion(this.grouperVersion);
    auditEntry.setHibernateVersionNumber(this.hibernateVersionNumber);
    auditEntry.setId(this.id);
    auditEntry.setInt01(this.int01);
    auditEntry.setInt02(this.int02);
    auditEntry.setInt03(this.int03);
    auditEntry.setInt04(this.int04);
    auditEntry.setInt05(this.int05);
    auditEntry.setLastUpdatedDb(GrouperUtil.dateLongValue(this.lastUpdated));
    auditEntry.setLoggedInMemberId(this.loggedInMemberId);
    auditEntry.setQueryCount(this.queryCount);
    auditEntry.setServerHost(this.serverHost);
    auditEntry.setServerUserName(this.serverUserName);
    auditEntry.setString01(this.string01);
    auditEntry.setString02(this.string02);
    auditEntry.setString03(this.string03);
    auditEntry.setString04(this.string04);
    auditEntry.setString05(this.string05);
    auditEntry.setString06(this.string06);
    auditEntry.setString07(this.string07);
    auditEntry.setString08(this.string08);
    auditEntry.setUserIpAddress(this.userIpAddress);    
    
    return auditEntry;
  }

  /**
   * @param exportVersion
   * @return the xml string
   */
  public String toXml(GrouperVersion exportVersion) {
    StringWriter stringWriter = new StringWriter();
    this.toXml(exportVersion, stringWriter);
    return stringWriter.toString();
  }

  /**
   * @param exportVersion 
   * @param writer
   */
  public void toXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer) {
    XStream xStream = XmlExportUtils.xstream();
  
    CompactWriter compactWriter = new CompactWriter(writer);
    
    xStream.marshal(this, compactWriter);
  
  }

  /**
   * hibernateVersionNumber
   * @return hibernateVersionNumber
   */
  public long getHibernateVersionNumber() {
    return this.hibernateVersionNumber;
  }

  /**
   * hibernateVersionNumber
   * @param hibernateVersionNumber1
   */
  public void setHibernateVersionNumber(long hibernateVersionNumber1) {
    this.hibernateVersionNumber = hibernateVersionNumber1;
  }
  
}
