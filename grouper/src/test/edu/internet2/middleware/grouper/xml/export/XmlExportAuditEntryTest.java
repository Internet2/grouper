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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditEntryTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAuditEntryTest extends GrouperTest {

  /** grouperSession */
  private GrouperSession grouperSession;

  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    this.grouperSession = GrouperSession.startRootSession();
  }

 
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    super.tearDown();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    TestRunner.run(XmlExportAuditEntryTest.class);
    //TestRunner.run(new XmlExportGroupTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAuditEntryTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAuditEntry xmlExportAuditEntry = new XmlExportAuditEntry();
    
    xmlExportAuditEntry.setActAsMemberId("actAsMemberId");
    xmlExportAuditEntry.setAuditTypeId("auditTypeId");
    xmlExportAuditEntry.setContextId("contextId");
    xmlExportAuditEntry.setCreatedOn("createdOn");
    xmlExportAuditEntry.setDescription("description");
    xmlExportAuditEntry.setDurationMicroseconds(3L);
    xmlExportAuditEntry.setEnvName("envName");
    xmlExportAuditEntry.setGrouperEngine("grouperEngine");
    xmlExportAuditEntry.setGrouperVersion("grouperVersion");
    xmlExportAuditEntry.setHibernateVersionNumber(4L);
    xmlExportAuditEntry.setId("id");
    xmlExportAuditEntry.setInt01(11L);
    xmlExportAuditEntry.setInt02(12L);
    xmlExportAuditEntry.setInt03(13L);
    xmlExportAuditEntry.setInt04(14L);
    xmlExportAuditEntry.setInt05(15L);
    xmlExportAuditEntry.setLastUpdated("lastUpdated");
    xmlExportAuditEntry.setLoggedInMemberId("loggedInMemberId");
    xmlExportAuditEntry.setQueryCount(5);
    xmlExportAuditEntry.setServerHost("serverHost");
    xmlExportAuditEntry.setServerUserName("serverUserName");
    xmlExportAuditEntry.setString01("string01");
    xmlExportAuditEntry.setString02("string02");
    xmlExportAuditEntry.setString03("string03");
    xmlExportAuditEntry.setString04("string04");
    xmlExportAuditEntry.setString05("string05");
    xmlExportAuditEntry.setString06("string06");
    xmlExportAuditEntry.setString07("string07");
    xmlExportAuditEntry.setString08("string08");
    xmlExportAuditEntry.setUserIpAddress("userIpAddress");

    
    String xml = xmlExportAuditEntry.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAuditEntry = XmlExportAuditEntry.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);

    assertEquals("actAsMemberId", xmlExportAuditEntry.getActAsMemberId());
    assertEquals("auditTypeId", xmlExportAuditEntry.getAuditTypeId());
    assertEquals("contextId", xmlExportAuditEntry.getContextId());
    assertEquals("createdOn", xmlExportAuditEntry.getCreatedOn());
    assertEquals("description", xmlExportAuditEntry.getDescription());
    assertEquals(3L, xmlExportAuditEntry.getDurationMicroseconds());
    assertEquals("envName", xmlExportAuditEntry.getEnvName());
    assertEquals("grouperEngine", xmlExportAuditEntry.getGrouperEngine());
    assertEquals("grouperVersion", xmlExportAuditEntry.getGrouperVersion());
    assertEquals(4L, xmlExportAuditEntry.getHibernateVersionNumber());
    assertEquals("id", xmlExportAuditEntry.getId());
    assertEquals(new Long(11), xmlExportAuditEntry.getInt01());
    assertEquals(new Long(12), xmlExportAuditEntry.getInt02());
    assertEquals(new Long(13), xmlExportAuditEntry.getInt03());
    assertEquals(new Long(14), xmlExportAuditEntry.getInt04());
    assertEquals(new Long(15), xmlExportAuditEntry.getInt05());
    assertEquals("lastUpdated", xmlExportAuditEntry.getLastUpdated());
    assertEquals("loggedInMemberId", xmlExportAuditEntry.getLoggedInMemberId());
    assertEquals(5, xmlExportAuditEntry.getQueryCount());
    assertEquals("serverHost", xmlExportAuditEntry.getServerHost());
    assertEquals("serverUserName", xmlExportAuditEntry.getServerUserName());
    assertEquals("string01", xmlExportAuditEntry.getString01());
    assertEquals("string02", xmlExportAuditEntry.getString02());
    assertEquals("string03", xmlExportAuditEntry.getString03());
    assertEquals("string04", xmlExportAuditEntry.getString04());
    assertEquals("string05", xmlExportAuditEntry.getString05());
    assertEquals("string06", xmlExportAuditEntry.getString06());
    assertEquals("string07", xmlExportAuditEntry.getString07());
    assertEquals("string08", xmlExportAuditEntry.getString08());
    assertEquals("userIpAddress", xmlExportAuditEntry.getUserIpAddress());

  }
  
  /**
   * 
   */
  public void testConvertToAuditEntry() {
    AuditEntry auditEntry = AuditEntryTest.exampleAuditEntry();
    
    XmlExportAuditEntry xmlExportAuditEntry = auditEntry.xmlToExportAuditEntry(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    auditEntry = xmlExportAuditEntry.toAuditEntry();
    
    assertEquals("actAsMemberId", auditEntry.getActAsMemberId());
    assertEquals("auditTypeId", auditEntry.getAuditTypeId());
    assertEquals("contextId", auditEntry.getContextId());
    assertEquals(new Long(3L), auditEntry.getCreatedOnDb());
    assertEquals("description", auditEntry.getDescription());
    assertEquals(4L, auditEntry.getDurationMicroseconds());
    assertEquals("envName", auditEntry.getEnvName());
    assertEquals("grouperEngine", auditEntry.getGrouperEngine());
    assertEquals("grouperVersion", auditEntry.getGrouperVersion());
    assertEquals(new Long(5L), auditEntry.getHibernateVersionNumber());
    assertEquals("id", auditEntry.getId());
    assertEquals(new Long(11L), auditEntry.getInt01());
    assertEquals(new Long(12L), auditEntry.getInt02());
    assertEquals(new Long(13L), auditEntry.getInt03());
    assertEquals(new Long(14L), auditEntry.getInt04());
    assertEquals(new Long(15L), auditEntry.getInt05());
    assertEquals(new Long(6L), auditEntry.getLastUpdatedDb());
    assertEquals("loggedInMemberId", auditEntry.getLoggedInMemberId());
    assertEquals(7, auditEntry.getQueryCount());
    assertEquals("serverHost", auditEntry.getServerHost());
    assertEquals("serverUserName", auditEntry.getServerUserName());
    assertEquals("string01", auditEntry.getString01());
    assertEquals("string02", auditEntry.getString02());
    assertEquals("string03", auditEntry.getString03());
    assertEquals("string04", auditEntry.getString04());
    assertEquals("string05", auditEntry.getString05());
    assertEquals("string06", auditEntry.getString06());
    assertEquals("string07", auditEntry.getString07());
    assertEquals("string08", auditEntry.getString08());
    assertEquals("userIpAddress", auditEntry.getUserIpAddress());

    
  }
}
