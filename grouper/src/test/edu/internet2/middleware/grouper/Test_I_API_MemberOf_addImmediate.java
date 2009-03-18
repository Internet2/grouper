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
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.MemberOf;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_MemberOf_addImmediate.java,v 1.7 2009-03-18 18:51:58 shilen Exp $
 * @since   1.2.0
 */
public class Test_I_API_MemberOf_addImmediate extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Group           gA, gB, gC, gD;
  private Member          mX;
  private Stem            parent;
  private GrouperSession  s;
  private Subject         subjX, subjY;



  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {
      // TODO 20070523 this *really* cries out for an object mother     
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      gA      = parent.addChildGroup("child group a", "child group a");
      gB      = parent.addChildGroup("child group b", "child group b");
      gC      = parent.addChildGroup("child group c", "child group c");
      gD      = parent.addChildGroup("child group d", "child group d");
      subjX   = SubjectFinder.findById( RegistrySubject.add(s, "subjX", "person", "subjX").getId(), true );
      subjY   = SubjectFinder.findById( RegistrySubject.add(s, "subjY", "person", "subjY").getId(), true );
      mX      = MemberFinder.findBySubject(s, subjX, true);
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

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
   * Generate correct membership delta when adding a single immediate member to an isolated group.
   * @since   1.2.0
   */
  public void test_addImmediate_addSubjectToIsolatedGroup() {
    MemberOf mof = new DefaultMemberOf(); // TODO 20070523 should use a factory or equiv
    mof.addImmediate( s, gA, Group.getDefaultList(), mX );

    assertEquals( "mof deletes",        0, mof.getDeletes().size() );
    assertEquals( "mof saves",          1, mof.getSaves().size() );
    assertEquals( "mof modifiedGroups", 0, mof.getModifiedGroups().size() );
    assertEquals( "mof modifiedStems",  0, mof.getModifiedStems().size() );
  }

  /**  
   * <pre>
   * 1. Add subjX to gB.
   * 2. Add subjY to gC.
   * 3. Add UNION(gB, gC) to gA.
   * 4. Add gA to gD.
   * </pre>
   * @since   1.2.0
   */
  public void test_addImmediate_addGroupWithSimpleUnionAsMemberToIsolatedGroup() {
    try {
      gB.addMember(subjX);
      gC.addMember(subjY);
      gA.addCompositeMember( CompositeType.UNION, gB, gC );
    }
    catch (Exception eShouldNotHappen) {
      errorInitializingTest(eShouldNotHappen);
    }

    MemberOf mof = new DefaultMemberOf(); // TODO 20070524 should use a factory or equiv
    mof.addImmediate( s, gD, Group.getDefaultList(), gA.toMember() );

    assertEquals( "mof deletes",        0, mof.getDeletes().size() );
    // immediate membership in gD (1), effectives in gD from composite gA (2)
    assertEquals( "mof saves",          3, mof.getSaves().size() );
    // membership owner (1)
    assertEquals( "mof modifiedGroups", 0, mof.getModifiedGroups().size() );
    assertEquals( "mof modifiedStems",  0, mof.getModifiedStems().size() );
  }

} 

