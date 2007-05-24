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
import  java.util.Date;

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_MemberOf_deleteComposite.java,v 1.2 2007-05-24 19:34:59 blair Exp $
 * @since   1.2.0
 */
public class Test_I_API_MemberOf_deleteComposite extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Composite       c;
  private Group           gA, gB, gC;
  private Stem            parent;
  private GrouperSession  s;



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
      // TODO 20070523 this *screams* for an easier way
      c       = new Composite();
      c.setDTO(
        new CompositeDTO()
          .setCreateTime( new Date().getTime() )
          .setCreatorUuid( s.getMember().getUuid() )
          .setFactorOwnerUuid( gA.getUuid() )
          .setLeftFactorUuid( gB.getUuid() )
          .setRightFactorUuid( gB.getUuid() )
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
   * Generate correct membership delta when deleting a simple empty UNION from an isolated group.
   * @since   1.2.0
   */
  public void test_deleteComposite_addEmptyUnionToIsolatedGroup() {
    try {
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
    }
    catch (Exception eShouldNotHappen) {
      errorInitializingTest(eShouldNotHappen);
    }

    MemberOf mof = new DefaultMemberOf(); // TODO 20070523 should use a factory or equiv
    mof.deleteComposite(s, gA, c);

    assertEquals( "mof deletes",        1, mof.getDeletes().size() );
    assertEquals( "mof saves",          0, mof.getSaves().size() );
    assertEquals( "mof modifiedGroups", 1, mof.getModifiedGroups().size() );
    assertEquals( "mof modifiedStems",  0, mof.getModifiedStems().size() );
  }

} 

