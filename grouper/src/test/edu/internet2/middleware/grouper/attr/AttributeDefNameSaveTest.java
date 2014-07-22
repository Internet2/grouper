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
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;

/**
 * @author mchyzer
 *
 */
public class AttributeDefNameSaveTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameSaveTest("testAttributeDefNameSave"));
  }
  
  /**
   * 
   */
  public AttributeDefNameSaveTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefNameSaveTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  /** root stem */
  private Stem root;
  /** top stem */
  private Stem top;
  
  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }

  /**
   * 
   */
  public void testAttributeDefNameSave() {
    
    AttributeDef attributeDef = new AttributeDefSave(this.grouperSession).assignName("top:bDef").save();
    
    //cant create without privs
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      new AttributeDefNameSave(this.grouperSession, attributeDef).assignName("top:b").save();
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    //grant privs
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    this.top.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(this.grouperSession, attributeDef).assignName("top:b");
    AttributeDefName attributeDefName = attributeDefNameSave.save();
    assertEquals("top:b", attributeDefName.getName());
    assertNull(attributeDefName.getDescription());
    assertEquals("top display name:b", attributeDefName.getDisplayName());
    assertEquals(SaveResultType.INSERT, attributeDefNameSave.getSaveResultType());
    
    //update
    attributeDefNameSave = new AttributeDefNameSave(this.grouperSession, attributeDef)
      .assignName("top:b").assignDescription("whatever").assignDisplayExtension("theB");
    attributeDefName = attributeDefNameSave.save();
    assertEquals(SaveResultType.UPDATE, attributeDefNameSave.getSaveResultType());
    assertEquals("whatever", attributeDefName.getDescription());
    assertEquals("top display name:theB", attributeDefName.getDisplayName());
    
    //no change
    attributeDefNameSave = new AttributeDefNameSave(this.grouperSession, attributeDef)
      .assignName("top:b").assignDescription("whatever").assignDisplayExtension("theB");
    attributeDefName = attributeDefNameSave.save();
    assertEquals(SaveResultType.NO_CHANGE, attributeDefNameSave.getSaveResultType());
    assertEquals("whatever", attributeDefName.getDescription());
    assertEquals("top display name:theB", attributeDefName.getDisplayName());
  
    
  }
  
}
