/**
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.TestStem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportStemTest extends GrouperTest {

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

    TestRunner.run(XmlExportStemTest.class);
    //TestRunner.run(new XmlExportStemTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportStemTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportStem xmlExportStem = new XmlExportStem();
    
    xmlExportStem.setAlternateName("alternateName");
    xmlExportStem.setContextId("contextId");
    xmlExportStem.setCreateTime("createTime");
    xmlExportStem.setCreatorId("creatorId");
    xmlExportStem.setDescription("description");
    xmlExportStem.setDisplayExtension("displayExtension");
    xmlExportStem.setDisplayName("displayName");
    xmlExportStem.setExtension("extension");
    xmlExportStem.setHibernateVersionNumber(3L);
    xmlExportStem.setIdIndex(12345L);
    xmlExportStem.setLastMembershipChange(4L);
    xmlExportStem.setModifierId("modifierId");
    xmlExportStem.setModifierTime("modifierTime");
    xmlExportStem.setName("name");
    xmlExportStem.setParentStem("parentStem");
    xmlExportStem.setUuid("uuid");
    
    String xml = xmlExportStem.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportStem = XmlExportStem.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("alternateName", xmlExportStem.getAlternateName());
    assertEquals("contextId", xmlExportStem.getContextId());
    assertEquals("createTime", xmlExportStem.getCreateTime());
    assertEquals("creatorId", xmlExportStem.getCreatorId());
    assertEquals("description", xmlExportStem.getDescription());
    assertEquals("displayExtension", xmlExportStem.getDisplayExtension());
    assertEquals("displayName", xmlExportStem.getDisplayName());
    assertEquals("extension", xmlExportStem.getExtension());
    assertEquals(3L, xmlExportStem.getHibernateVersionNumber());
    assertEquals(new Long(4L), xmlExportStem.getLastMembershipChange());
    assertEquals("modifierId", xmlExportStem.getModifierId());
    assertEquals("modifierTime", xmlExportStem.getModifierTime());
    assertEquals("name", xmlExportStem.getName());
    assertEquals("parentStem", xmlExportStem.getParentStem());
    assertEquals("uuid", xmlExportStem.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToStem() {
    Stem stem = TestStem.exampleStem();
    
    XmlExportStem xmlExportStem = stem.xmlToExportStem(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    stem = xmlExportStem.toStem();
    
    assertEquals("alternateName", stem.getAlternateNameDb());
    assertEquals("contextId", stem.getContextId());
    assertEquals(5L, stem.getCreateTimeLong());
    assertEquals("creatorId", stem.getCreatorUuid());
    assertEquals("description", stem.getDescription());
    assertEquals("displayExtension", stem.getDisplayExtension());
    assertEquals("displayName", stem.getDisplayName());
    assertEquals("extension", stem.getExtension());
    assertEquals(new Long(3L), stem.getHibernateVersionNumber());
    assertEquals(new Long(12345L), stem.getIdIndex());
    assertEquals(new Long(4L), stem.getLastMembershipChangeDb());
    assertEquals("modifierId", stem.getModifierUuid());
    assertEquals(6L, stem.getModifyTimeLong());
    assertEquals("name", stem.getName());
    assertEquals("parentUuid", stem.getParentUuid());
    assertEquals("uuid", stem.getUuid());
    
  }
}
