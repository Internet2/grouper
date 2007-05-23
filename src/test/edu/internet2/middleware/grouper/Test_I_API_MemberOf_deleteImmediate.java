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
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_MemberOf_deleteImmediate.java,v 1.1 2007-05-23 18:20:55 blair Exp $
 * @since   1.2.0
 */
public class Test_I_API_MemberOf_deleteImmediate extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Group           gA;
  private Member          mX;
  private MemberDTO       _mX;
  private Stem            parent;
  private GrouperSession  s;
  private Subject         subjX;



  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {
      // TODO 20070523 this *really* cries out for an object mother     
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      gA      = parent.addChildGroup("child group a", "child group a");
      subjX   = SubjectFinder.findById( RegistrySubject.add(s, "subjX", "person", "subjX").getId() );
      mX      = MemberFinder.findBySubject(s, subjX);
      _mX     = (MemberDTO) mX.getDTO();
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
   * Generate correct membership delta when deleting a single immediate member from an isolated group.
   * @since   1.2.0
   */
  public void test_deleteImmediate_addSubjectToIsolatedGroup() {
    MembershipDTO _ms = null;
    try {
      gA.addMember(subjX);
      _ms = GrouperDAOFactory.getFactory().getMembership().findByOwnerAndMemberAndFieldAndType(
        gA.getUuid(), mX.getUuid(), Group.getDefaultList(), Membership.IMMEDIATE
      );
    }
    catch (Exception eShouldNotHappen) {
      errorInitializingTest(eShouldNotHappen);
    }

    MemberOf mof = new DefaultMemberOf(); // TODO 20070523 should use a factory or equiv
    mof.deleteImmediate(s, gA, _ms, _mX);

    assertEquals( "mof deletes",        1, mof.getDeletes().size() );
    assertEquals( "mof saves",          0, mof.getSaves().size() );
    assertEquals( "mof modifiedGroups", 1, mof.getModifiedGroups().size() );
    assertEquals( "mof modifiedStems",  0, mof.getModifiedStems().size() );
  }

} 

