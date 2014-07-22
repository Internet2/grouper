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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.member.TestMember;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportMemberTest extends GrouperTest {

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

    TestRunner.run(XmlExportMemberTest.class);
    //TestRunner.run(new XmlExportMemberTest("testConvertToString"));

  }
  
  /**
   * 
   * @param name
   */
  public XmlExportMemberTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportMember xmlExportMember = new XmlExportMember();
    
    xmlExportMember.setContextId("contextId");
    xmlExportMember.setHibernateVersionNumber(3L);
    xmlExportMember.setSourceId("sourceId");
    xmlExportMember.setSubjectId("subjectId");
    xmlExportMember.setSubjectType("subjectType");
    xmlExportMember.setUuid("uuid");
    
    String xml = xmlExportMember.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportMember = XmlExportMember.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportMember.getContextId());
    assertEquals(3L, xmlExportMember.getHibernateVersionNumber());
    assertEquals("subjectId", xmlExportMember.getSubjectId());
    assertEquals("sourceId", xmlExportMember.getSourceId());
    assertEquals("subjectType", xmlExportMember.getSubjectType());
    assertEquals("uuid", xmlExportMember.getUuid());
    
  }
  
  /**
   * 
   */
  public void testConvertToMember() {
    Member member = TestMember.exampleMember();
    
    XmlExportMember xmlExportMember = member.xmlToExportMember(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    member = xmlExportMember.toMember();
    
    assertEquals("contextId", member.getContextId());
    assertEquals(new Long(3L), member.getHibernateVersionNumber());
    assertEquals("subjectId", member.getSubjectId());
    assertEquals("subjectSourceId", member.getSubjectSourceId());
    assertEquals("subjectTypeId", member.getSubjectTypeId());
    assertEquals("uuid", member.getUuid());
    
  }
}
