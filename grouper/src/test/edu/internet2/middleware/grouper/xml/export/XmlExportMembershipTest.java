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
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportMembershipTest extends GrouperTest {

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

    TestRunner.run(XmlExportMembershipTest.class);
    //TestRunner.run(new XmlExportMembershipTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportMembershipTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportMembership xmlExportMembership = new XmlExportMembership();
    
    xmlExportMembership.setContextId("contextId");
    xmlExportMembership.setCreateTime("createTime");
    xmlExportMembership.setCreatorId("creatorId");
    xmlExportMembership.setDisableTimestamp("disableTimestamp");
    xmlExportMembership.setEnabled("enabled");
    xmlExportMembership.setEnabledTimestamp("enabledTimestamp");
    xmlExportMembership.setFieldId("fieldId");
    xmlExportMembership.setHibernateVersionNumber(3L);
    xmlExportMembership.setMemberId("memberId");
    xmlExportMembership.setOwnerAttrDefId("ownerAttrDefId");
    xmlExportMembership.setOwnerGroupId("ownerGroupId");
    xmlExportMembership.setOwnerStemId("ownerStemId");
    xmlExportMembership.setType("type");
    xmlExportMembership.setViaCompositeId("viaCompositeId");
    xmlExportMembership.setUuid("uuid");
    
    String xml = xmlExportMembership.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportMembership = XmlExportMembership.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportMembership.getContextId());
    assertEquals("createTime", xmlExportMembership.getCreateTime());
    assertEquals("creatorId", xmlExportMembership.getCreatorId());
    assertEquals("disableTimestamp", xmlExportMembership.getDisableTimestamp());
    assertEquals("enabled", xmlExportMembership.getEnabled());
    assertEquals("enabledTimestamp", xmlExportMembership.getEnabledTimestamp());
    assertEquals("fieldId", xmlExportMembership.getFieldId());
    assertEquals(3L, xmlExportMembership.getHibernateVersionNumber());
    assertEquals("memberId", xmlExportMembership.getMemberId());
    assertEquals("ownerAttrDefId", xmlExportMembership.getOwnerAttrDefId());
    assertEquals("ownerGroupId", xmlExportMembership.getOwnerGroupId());
    assertEquals("ownerStemId", xmlExportMembership.getOwnerStemId());
    assertEquals("type", xmlExportMembership.getType());
    assertEquals("uuid", xmlExportMembership.getUuid());
    assertEquals("viaCompositeId", xmlExportMembership.getViaCompositeId());

  }
  
  /**
   * 
   */
  public void testConvertToMembership() {
    Membership membership = new Membership();
    membership.setContextId("contextId");
    membership.setCreateTimeLong(5L);
    membership.setCreatorUuid("creatorId");
    membership.setDisabledTimeDb(4L);
    membership.setEnabledDb("T");
    membership.setEnabledTimeDb(6L);
    membership.setFieldId("fieldId");
    membership.setHibernateVersionNumber(3L);
    membership.setMemberUuid("memberId");
    membership.setOwnerAttrDefId("ownerAttrDefId");
    membership.setOwnerGroupId("ownerGroupId");
    membership.setOwnerStemId("ownerStemId");
    membership.setType("type");
    membership.setImmediateMembershipId("uuid");
    membership.setViaCompositeId("viaCompositeId");
    
    XmlExportMembership xmlExportMembership = membership.xmlToExportMembership(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    membership = xmlExportMembership.toMembership();
    
    assertEquals("contextId", membership.getContextId());
    assertEquals(5L, membership.getCreateTimeLong());
    assertEquals("creatorId", membership.getCreatorUuid());
    assertEquals(new Long (4L), membership.getDisabledTimeDb());
    assertEquals("T", membership.getEnabledDb());
    assertEquals(new Long(6L), membership.getEnabledTimeDb());
    assertEquals("fieldId", membership.getFieldId());
    assertEquals(new Long(3L), membership.getHibernateVersionNumber());
    assertEquals("memberId", membership.getMemberUuid());
    assertEquals("ownerAttrDefId", membership.getOwnerAttrDefId());
    assertEquals("ownerGroupId", membership.getOwnerGroupId());
    assertEquals("ownerStemId", membership.getOwnerStemId());
    assertEquals("type", membership.getType());
    assertEquals("uuid", membership.getImmediateMembershipId());
    assertEquals("viaCompositeId", membership.getViaCompositeId());
    
  }
}
