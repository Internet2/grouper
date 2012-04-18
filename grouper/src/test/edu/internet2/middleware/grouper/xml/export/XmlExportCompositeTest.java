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
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.TestComposite;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportCompositeTest extends GrouperTest {

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

    TestRunner.run(XmlExportCompositeTest.class);
    //TestRunner.run(new XmlExportGroupTypeTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportCompositeTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportComposite xmlExportComposite = new XmlExportComposite();
    
    xmlExportComposite.setContextId("contextId");
    xmlExportComposite.setCreateTime("createTime");
    xmlExportComposite.setCreatorId("creatorId");
    xmlExportComposite.setHibernateVersionNumber(3L);
    xmlExportComposite.setLeftFactor("leftFactor");
    xmlExportComposite.setOwner("owner");
    xmlExportComposite.setRightFactor("rightFactor");
    xmlExportComposite.setType("type");
    xmlExportComposite.setUuid("uuid");
    
    String xml = xmlExportComposite.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportComposite = XmlExportComposite.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportComposite.getContextId());
    assertEquals("createTime", xmlExportComposite.getCreateTime());
    assertEquals("creatorId", xmlExportComposite.getCreatorId());
    assertEquals(3L, xmlExportComposite.getHibernateVersionNumber());
    assertEquals("leftFactor", xmlExportComposite.getLeftFactor());
    assertEquals("owner", xmlExportComposite.getOwner());
    assertEquals("rightFactor", xmlExportComposite.getRightFactor());
    assertEquals("type", xmlExportComposite.getType());
    assertEquals("uuid", xmlExportComposite.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToComposite() {
    Composite composite = TestComposite.exampleComposite();
    
    XmlExportComposite xmlExportComposite = composite.xmlToExportComposite(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    composite = xmlExportComposite.toComposite();
    
    assertEquals("contextId", composite.getContextId());
    assertEquals(3L, composite.getCreateTime());
    assertEquals("creatorId", composite.getCreatorUuid());
    assertEquals(new Long(3L), composite.getHibernateVersionNumber());
    assertEquals("leftFactor", composite.getLeftFactorUuid());
    assertEquals("owner", composite.getFactorOwnerUuid());
    assertEquals("rightFactor", composite.getRightFactorUuid());
    assertEquals("type", composite.getTypeDb());
    assertEquals("uuid", composite.getUuid());
    
  }
}
