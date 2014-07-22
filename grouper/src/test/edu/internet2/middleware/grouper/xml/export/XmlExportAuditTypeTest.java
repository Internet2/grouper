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
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.AuditTypeTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAuditTypeTest extends GrouperTest {

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

    TestRunner.run(XmlExportAuditTypeTest.class);
    //TestRunner.run(new XmlExportAuditTypeTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAuditTypeTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAuditType xmlExportAuditType = new XmlExportAuditType();
    
    xmlExportAuditType.setActionName("actionName");
    xmlExportAuditType.setAuditCategory("auditCategory");
    xmlExportAuditType.setContextId("contextId");
    xmlExportAuditType.setCreatedOn("createdOn");
    xmlExportAuditType.setHibernateVersionNumber(3L);
    xmlExportAuditType.setId("id");
    xmlExportAuditType.setLabelInt01("labelInt01");
    xmlExportAuditType.setLabelInt02("labelInt02");
    xmlExportAuditType.setLabelInt03("labelInt03");
    xmlExportAuditType.setLabelInt04("labelInt04");
    xmlExportAuditType.setLabelInt05("labelInt05");
    xmlExportAuditType.setLabelString01("labelString01");
    xmlExportAuditType.setLabelString02("labelString02");
    xmlExportAuditType.setLabelString03("labelString03");
    xmlExportAuditType.setLabelString04("labelString04");
    xmlExportAuditType.setLabelString05("labelString05");
    xmlExportAuditType.setLabelString06("labelString06");
    xmlExportAuditType.setLabelString07("labelString07");
    xmlExportAuditType.setLabelString08("labelString08");
    xmlExportAuditType.setLastUpdated("lastUpdated");
    String xml = xmlExportAuditType.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAuditType = XmlExportAuditType.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("actionName", xmlExportAuditType.getActionName());
    assertEquals("auditCategory", xmlExportAuditType.getAuditCategory());
    assertEquals("contextId", xmlExportAuditType.getContextId());
    assertEquals("createdOn", xmlExportAuditType.getCreatedOn());
    assertEquals(3L, xmlExportAuditType.getHibernateVersionNumber());
    assertEquals("id", xmlExportAuditType.getId());
    assertEquals("labelInt01", xmlExportAuditType.getLabelInt01());
    assertEquals("labelInt02", xmlExportAuditType.getLabelInt02());
    assertEquals("labelInt03", xmlExportAuditType.getLabelInt03());
    assertEquals("labelInt04", xmlExportAuditType.getLabelInt04());
    assertEquals("labelInt05", xmlExportAuditType.getLabelInt05());
    assertEquals("labelString01", xmlExportAuditType.getLabelString01());
    assertEquals("labelString02", xmlExportAuditType.getLabelString02());
    assertEquals("labelString03", xmlExportAuditType.getLabelString03());
    assertEquals("labelString04", xmlExportAuditType.getLabelString04());
    assertEquals("labelString05", xmlExportAuditType.getLabelString05());
    assertEquals("labelString06", xmlExportAuditType.getLabelString06());
    assertEquals("labelString07", xmlExportAuditType.getLabelString07());
    assertEquals("labelString08", xmlExportAuditType.getLabelString08());
    assertEquals("lastUpdated", xmlExportAuditType.getLastUpdated());

  }
  
  /**
   * 
   */
  public void testConvertToAuditType() {
    AuditType auditType = AuditTypeTest.exampleAuditType();
    
    XmlExportAuditType xmlExportAuditType = auditType.xmlToExportAuditType(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    auditType = xmlExportAuditType.toAuditType();
    
    assertEquals("actionName", auditType.getActionName());
    assertEquals("auditCategory", auditType.getAuditCategory());
    assertEquals("contextId", auditType.getContextId());
    assertEquals(new Long(3L), auditType.getCreatedOnDb());
    assertEquals(new Long(4L), auditType.getHibernateVersionNumber());
    assertEquals("id", auditType.getId());
    assertEquals("labelInt01", auditType.getLabelInt01());
    assertEquals("labelInt02", auditType.getLabelInt02());
    assertEquals("labelInt03", auditType.getLabelInt03());
    assertEquals("labelInt04", auditType.getLabelInt04());
    assertEquals("labelInt05", auditType.getLabelInt05());
    assertEquals("labelString01", auditType.getLabelString01());
    assertEquals("labelString02", auditType.getLabelString02());
    assertEquals("labelString03", auditType.getLabelString03());
    assertEquals("labelString04", auditType.getLabelString04());
    assertEquals("labelString05", auditType.getLabelString05());
    assertEquals("labelString06", auditType.getLabelString06());
    assertEquals("labelString07", auditType.getLabelString07());
    assertEquals("labelString08", auditType.getLabelString08());
    assertEquals(new Long(5L), auditType.getLastUpdatedDb());
    
  }
}
