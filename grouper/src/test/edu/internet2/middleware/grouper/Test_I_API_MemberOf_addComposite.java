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
import  edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import  edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Date;

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_MemberOf_addComposite.java,v 1.4 2007-05-24 19:34:59 blair Exp $
 * @since   1.2.0
 */
public class Test_I_API_MemberOf_addComposite extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Composite       c;
  private Group           gA, gB, gC;
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
      subjX   = SubjectFinder.findById( RegistrySubject.add(s, "subjX", "person", "subjX").getId() );
      subjY   = SubjectFinder.findById( RegistrySubject.add(s, "subjY", "person", "subjY").getId() );
      // TODO 20070523 this *screams* for an easier way
      c       = new Composite();
      c.setDTO(
        new CompositeDTO()
          .setCreateTime( new Date().getTime() )
          .setCreatorUuid( s.getMember().getUuid() )
          .setFactorOwnerUuid( gA.getUuid() )
          .setLeftFactorUuid( gB.getUuid() )
          .setRightFactorUuid( gC.getUuid() )
          .setType( CompositeType.UNION.toString() )
          .setUuid( GrouperUuid.getUuid() )
      );
      c.setSession(s);
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
   * Generate correct membership delta when adding a simple empty UNION to an isolated group.
   * @since   1.2.0
   */
  public void test_addComposite_addEmptyUnionToIsolatedGroup() {
    MemberOf mof = new DefaultMemberOf(); // TODO 20070523 should use a factory or equiv
    mof.addComposite(s, gA, c);

    assertEquals( "mof deletes",        0, mof.getDeletes().size() );
    assertEquals( "mof saves",          1, mof.getSaves().size() );
    assertEquals( "mof modifiedGroups", 1, mof.getModifiedGroups().size() );
    assertEquals( "mof modifiedStems",  0, mof.getModifiedStems().size() );
  }

  /**  
   * <pre>
   * 1. Add subjX to gB.
   * 2. Add subjY to gC.
   * 3. Add UNION(gB, gC) to gA.
   * </pre>
   * @since   1.2.0
   */
  public void test_addComposite_addSimpleUnionToIsolatedGroup() {
    try {
      gB.addMember(subjX);
      gC.addMember(subjY);
    }
    catch (Exception eShouldNotHappen) {
      errorInitializingTest(eShouldNotHappen);
    }

    MemberOf mof = new DefaultMemberOf(); // TODO 20070523 should use a factory or equiv
    mof.addComposite(s, gA, c);

    assertEquals( "mof deletes",        0, mof.getDeletes().size() );
    // composite memberships (2) + composite (1)
    assertEquals( "mof saves",          3, mof.getSaves().size() );
    // composite owner (1)
    assertEquals( "mof modifiedGroups", 1, mof.getModifiedGroups().size() );
    assertEquals( "mof modifiedStems",  0, mof.getModifiedStems().size() );
  }

} 

