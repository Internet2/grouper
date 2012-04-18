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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.TestGroupType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportGroupTypeTest extends GrouperTest {

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

    TestRunner.run(XmlExportGroupTypeTest.class);
    //TestRunner.run(new XmlExportGroupTypeTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportGroupTypeTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportGroupType xmlExportGroupType = new XmlExportGroupType();
    
    xmlExportGroupType.setAssignable("T");
    xmlExportGroupType.setContextId("contextId");
    xmlExportGroupType.setCreateTime("createTime");
    xmlExportGroupType.setCreatorId("creatorId");
    xmlExportGroupType.setHibernateVersionNumber(3L);
    xmlExportGroupType.setInternal("T");
    xmlExportGroupType.setName("name");
    xmlExportGroupType.setUuid("uuid");
    
    String xml = xmlExportGroupType.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportGroupType = XmlExportGroupType.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("T", xmlExportGroupType.getAssignable());
    assertEquals("contextId", xmlExportGroupType.getContextId());
    assertEquals("createTime", xmlExportGroupType.getCreateTime());
    assertEquals("creatorId", xmlExportGroupType.getCreatorId());
    assertEquals(3L, xmlExportGroupType.getHibernateVersionNumber());
    assertEquals("T", xmlExportGroupType.getInternal());
    assertEquals("name", xmlExportGroupType.getName());
    assertEquals("uuid", xmlExportGroupType.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToGroupType() {
    GroupType groupType = TestGroupType.exampleGroupType();
    
    XmlExportGroupType xmlExportGroupType = groupType.xmlToExportGroupType(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    groupType = xmlExportGroupType.toGroupType();
    
    assertEquals(true, groupType.getIsAssignable());
    assertEquals("contextId", groupType.getContextId());
    assertEquals(3L, groupType.getCreateTime());
    assertEquals("creatorId", groupType.getCreatorUuid());
    assertEquals(new Long(3L), groupType.getHibernateVersionNumber());
    assertEquals(true, groupType.getIsInternal());
    assertEquals("name", groupType.getName());
    assertEquals("uuid", groupType.getUuid());
    
  }
}
