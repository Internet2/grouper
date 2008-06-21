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

package edu.internet2.middleware.grouper;

/**
 * Test <code>Group.deleteAttribute()</code>.
 * @author  blair christensen.
 * @version $Id: Test_I_API_Group_deleteAttribute.java,v 1.3 2008-06-21 04:16:12 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_I_API_Group_deleteAttribute extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Group           gA;
  private Stem            parent;
  private GrouperSession  s;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {
      // TODO 20070531 this *really* cries out for an object mother     
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      gA      = parent.addChildGroup("child group a", "child group a");
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  public void tearDown() {
    try {
      s.stop();
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
    super.tearDown();
  }


  // TESTS //

  /**
   * Delete the <i>description</i> attribute.
   * @since   1.2.0
   */
  public void test_deleteAttribute_description_ANFwhenNoDesc() 
    throws  GroupModifyException,
            InsufficientPrivilegeException
  {
    String attr = "description";
    try {
      gA.deleteAttribute(attr);
      fail("failed to throw AttributeNotFoundException");
    }
    catch (AttributeNotFoundException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  /**
   * Delete the <i>description</i> attribute.
   * @since   1.2.0
   */
  public void test_deleteAttribute_description_whenSet()
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException
  {
    // SETUP //
    String attr = "description";
    gA.setDescription(attr);
    gA.store();

    // TEST //
    gA.deleteAttribute(attr);
    assertTrue("deleted attribute without any exceptions", true);
  }

  /**
   * Verify <i>description</i> is empty string after being deleted.
   * @since   1.2.0
   */
  public void test_deleteAttribute_description_emptyStringAfterDeletion()
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException
  {
    // SETUP //
    String attr = "description";
    gA.setDescription(attr);
    gA.store();

    // TEST //
    gA.deleteAttribute(attr);
    assertEquals( "description is empty string after deletion", GrouperConfig.EMPTY_STRING, gA.getDescription() );
  }

} 

