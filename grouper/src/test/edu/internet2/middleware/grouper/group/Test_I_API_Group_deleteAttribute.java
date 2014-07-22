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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.group;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Test <code>Group.deleteAttribute()</code>.
 * @author  blair christensen.
 * @version $Id: Test_I_API_Group_deleteAttribute.java,v 1.4 2009-10-26 02:26:07 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_I_API_Group_deleteAttribute extends GrouperTest {

  /**
   * 
   */
  private static final String GROUP_TYPE1 = "groupType1";
  /**
   * 
   */
  private static final String ATTRIBUTE1 = "attribute1";

  /**
   * 
   */
  public Test_I_API_Group_deleteAttribute() {
    super();
  }

  /**
   * @param name
   */
  public Test_I_API_Group_deleteAttribute(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    TestRunner.run(new Test_I_API_Group_deleteAttribute("test_deleteAttribute_description_emptyStringAfterDeletion"));
    //TestRunner.run(Test_I_API_Group_deleteAttribute.class);
  }
  
  /** */
  private Group           gA;
  /** */
  private Stem            parent;
  /** */
  private GrouperSession  s;
  /** */
  private GroupType groupType;
  /** */
  @SuppressWarnings("unused")
  private AttributeDefName attr;

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  public void setUp() {
    super.setUp();
    try {
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      gA      = parent.addChildGroup("child group a", "child group a");
      
      //make sure a user can change a type
      this.groupType = GroupType.createType(s, GROUP_TYPE1, false);
      this.attr = this.groupType.addAttribute(s,ATTRIBUTE1, 
          false);
      gA.addType(this.groupType, false);
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  public void tearDown() {
    try {
      s.stop();
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
    super.tearDown();
  }


  // TESTS //

  /**
   * Delete the <i>description</i> attribute.
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   * @since   1.2.0
   */
  public void test_deleteAttribute_description_ANFwhenNoDesc() 
    throws  GroupModifyException,
            InsufficientPrivilegeException
  {
    try {
      gA.deleteAttribute(ATTRIBUTE1);
      fail("failed to throw AttributeNotFoundException");
    }
    catch (AttributeNotFoundException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  /**
   * Delete the <i>description</i> attribute.
   * @throws AttributeNotFoundException 
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   * @since   1.2.0
   */
  public void test_deleteAttribute_description_whenSet()
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException
  {
    // SETUP //
    gA.setAttribute(ATTRIBUTE1, "whatever");

    // TEST //
    gA.deleteAttribute(ATTRIBUTE1);
    assertTrue("deleted attribute without any exceptions", true);
  }

  /**
   * Verify <i>description</i> is empty string after being deleted.
   * @throws AttributeNotFoundException 
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   * @since   1.2.0
   */
  public void test_deleteAttribute_description_emptyStringAfterDeletion()
    throws  AttributeNotFoundException, SubjectNotFoundException,
    SubjectNotUniqueException,
            GroupModifyException,
            InsufficientPrivilegeException
  {
    // SETUP //
    gA.setAttribute(ATTRIBUTE1, "whatever");

    //get the subject, make sure the lazy attributes load correctly (not too early, not too late)
    GrouperSubject groupSubject = (GrouperSubject)SubjectFinder.findById(gA.getUuid(), true);
    assertFalse(groupSubject.isLoadedGroupAttributes());
    assertFalse(groupSubject.isLoadedModifyCreateSubjects());
    assertEquals(gA.getName(), groupSubject.getName());
    assertFalse(groupSubject.isLoadedGroupAttributes());
    assertFalse(groupSubject.isLoadedModifyCreateSubjects());
    assertEquals(gA.getName(), groupSubject.getAttributeValue("name"));
    assertTrue(groupSubject.isLoadedGroupAttributes());
    assertTrue(groupSubject.isLoadedModifyCreateSubjects());
    assertEquals(gA.getName(), GrouperUtil.nonNull(groupSubject.getAttributes()).get("name").iterator().next());
    assertTrue(groupSubject.isLoadedGroupAttributes());
    assertTrue(groupSubject.isLoadedModifyCreateSubjects());
    assertEquals("whatever", GrouperUtil.nonNull(groupSubject.getAttributes()).get(ATTRIBUTE1).iterator().next());
    assertTrue(groupSubject.isLoadedGroupAttributes());
    assertTrue(groupSubject.isLoadedModifyCreateSubjects());
    assertEquals("whatever", groupSubject.getAttributeValue(ATTRIBUTE1));
    assertTrue(groupSubject.isLoadedGroupAttributes());
    assertTrue(groupSubject.isLoadedModifyCreateSubjects());
    
    // TEST //
    gA.deleteAttribute(ATTRIBUTE1);
    assertEquals( "description is empty string after deletion", GrouperConfig.EMPTY_STRING, gA.getAttributeValue(ATTRIBUTE1, false, false) );
  }

} 

