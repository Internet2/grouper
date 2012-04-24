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
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.TestGroupTypeTuple;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportGroupTypeTupleTest extends GrouperTest {

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

    TestRunner.run(XmlExportGroupTypeTupleTest.class);
    //TestRunner.run(new XmlExportGroupTypeTupleTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportGroupTypeTupleTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportGroupTypeTuple xmlExportGroupTypeTuple = new XmlExportGroupTypeTuple();
    
    xmlExportGroupTypeTuple.setContextId("contextId");
    xmlExportGroupTypeTuple.setGroupId("groupId");
    xmlExportGroupTypeTuple.setHibernateVersionNumber(3L);
    xmlExportGroupTypeTuple.setTypeId("typeId");
    xmlExportGroupTypeTuple.setUuid("uuid");
    
    String xml = xmlExportGroupTypeTuple.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportGroupTypeTuple = XmlExportGroupTypeTuple.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportGroupTypeTuple.getContextId());
    assertEquals("groupId", xmlExportGroupTypeTuple.getGroupId());
    assertEquals(3L, xmlExportGroupTypeTuple.getHibernateVersionNumber());
    assertEquals("typeId", xmlExportGroupTypeTuple.getTypeId());
    assertEquals("uuid", xmlExportGroupTypeTuple.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToGroupTypeTuple() {
    GroupTypeTuple groupTypeTuple = TestGroupTypeTuple.exampleGroupTypeTuple();
    
    XmlExportGroupTypeTuple xmlExportGroupTypeTuple = groupTypeTuple.xmlToExportGroup(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    groupTypeTuple = xmlExportGroupTypeTuple.toGroupTypeTuple();
    
    assertEquals("contextId", groupTypeTuple.getContextId());
    assertEquals("groupId", groupTypeTuple.getGroupUuid());
    assertEquals(new Long(3L), groupTypeTuple.getHibernateVersionNumber());
    assertEquals("typeId", groupTypeTuple.getTypeUuid());
    assertEquals("uuid", groupTypeTuple.getId());
    
  }
}
