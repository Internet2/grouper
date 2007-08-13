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
 * Test {@link Stem}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_Stem.java,v 1.3 2007-08-13 17:54:28 blair Exp $
 * @since   @HEAD@
 */
public class Test_api_Stem extends GrouperTest {


  private Group           child_group, top_group;
  private GrouperSession  s;
  private Stem            child, root, top;



  public void setUp() {
    super.setUp();
    try {
      this.s            = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.root         = StemFinder.findRootStem(this.s);
      this.top          = this.root.addChildStem("top", "top");
      this.top_group    = this.top.addChildGroup("top group", "top group");
      this.child        = this.top.addChildStem("child", "child");
      this.child_group  = this.child.addChildGroup("child group", "child group");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  public void test_getChildGroups_fromRoot() {
    assertEquals( 0, this.root.getChildGroups().size() );
  }

  public void test_getChildGroups_fromTop() {
    assertEquals( 1, this.top.getChildGroups().size() );
  }

  public void test_getChildGroups_fromChild() {
    assertEquals( 1, this.child.getChildGroups().size() );
  }

  public void test_getChildGroups_Scope_nullScope() {
    try {
      this.root.getChildGroups(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getChildGroups_Scope_fromRootScopeONE() {
    assertEquals( 0, this.root.getChildGroups(Stem.Scope.ONE).size() );
  }

  public void test_getChildGroups_Scope_fromRootScopeSUB() {
    assertEquals( 2, this.root.getChildGroups(Stem.Scope.SUB).size() );
  }

  public void test_getChildGroups_Scope_fromTopScopeONE() {
    assertEquals( 1, this.top.getChildGroups(Stem.Scope.ONE).size() );
  }

  public void test_getChildGroups_Scope_fromTopScopeSUB() {
    assertEquals( 2, this.top.getChildGroups(Stem.Scope.SUB).size() );
  }

  public void test_getChildGroups_Scope_fromChildScopeONE() {
    assertEquals( 1, this.child.getChildGroups(Stem.Scope.ONE).size() );
  }

  public void test_getChildGroups_Scope_fromChildScopeSUB() {
    assertEquals( 1, this.child.getChildGroups(Stem.Scope.SUB).size() );
  }



  public void test_getChildStems_fromRoot() {
    assertEquals( 1, this.root.getChildStems().size() );
  }

  public void test_getChildStems_fromTop() {
    assertEquals( 1, this.top.getChildStems().size() );
  }

  public void test_getChildStems_fromChild() {
    assertEquals( 0, this.child.getChildStems().size() );
  }



  public void test_getChildStems_Scope_nullScope() {
    try {
      this.root.getChildStems(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getChildStems_Scope_fromRootScopeONE() {
    assertEquals( 1, this.root.getChildStems(Stem.Scope.ONE).size() );
  }

  public void test_getChildStems_Scope_fromRootScopeSUB() {
    assertEquals( 2, this.root.getChildStems(Stem.Scope.SUB).size() );
  }

  public void test_getChildStems_Scope_fromTopScopeONE() {
    assertEquals( 1, this.top.getChildStems(Stem.Scope.ONE).size() );
  }

  public void test_getChildStems_Scope_fromTopScopeSUB() {
    assertEquals( 1, this.top.getChildStems(Stem.Scope.SUB).size() );
  }

  public void test_getChildStems_Scope_fromChildScopeONE() {
    assertEquals( 0, this.child.getChildStems(Stem.Scope.ONE).size() );
  }

  public void test_getChildStems_Scope_fromChildScopeSUB() {
    assertEquals( 0, this.child.getChildStems(Stem.Scope.SUB).size() );
  }



  public void test_isChildGroup_nullChild() {
    try {
      this.root.isChildGroup(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    } 
  }

  public void test_isChildGroup_rootAsPotentialParent() {
    assertTrue( this.root.isChildGroup( this.child_group ) );
  }

  public void test_isChildGroup_immediateChild() {
    assertTrue( this.child.isChildGroup( this.child_group ) );
  }

  public void test_isChildGroup_notChild() {
    assertFalse( this.child.isChildGroup( this.top_group ) );
  }


  public void test_isChildStem_nullChild() {
    try {
      this.root.isChildStem(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_isChildStem_rootAsPotentialParent() {
    assertTrue( this.root.isChildStem( this.child ) );
  }

  public void test_isChildStem_rootAsChild() {
    assertFalse( this.child.isChildStem( this.root ) );
  }

  public void test_isChildStem_selfAsChild() {
    assertFalse( this.child.isChildStem( this.child ) );
  }

  public void test_isChildStem_isChild() {
    assertTrue( this.top.isChildStem( this.child ) );
  }

  public void test_isChildStem_notChild() 
    throws  InsufficientPrivilegeException,
            StemAddException
  {
    Stem otherTop = this.root.addChildStem("other top", "other top");
    assertFalse( otherTop.isChildStem( this.child ) );
  }



  public void test_isRootStem_root() {
    assertTrue( this.root.isRootStem() );
  }

  public void test_isRootStem_notRootStem() {
    assertFalse( this.top.isRootStem() );
  }

}

