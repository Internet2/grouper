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
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_Group_addCompositeMember.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_I_API_Group_addCompositeMember extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Group           gA, gB, gC, gD;
  private Stem            parent;
  private GrouperSession  s;
  private Subject         subjX, subjY;



  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {   
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      gA      = parent.addChildGroup("child group a", "child group a");
      gB      = parent.addChildGroup("child group b", "child group b");
      gC      = parent.addChildGroup("child group c", "child group c");
      gD      = parent.addChildGroup("child group d", "child group d");
      subjX   = SubjectFinder.findById( RegistrySubject.add(s, "subjX", "person", "subjX").getId(), true );
      subjY   = SubjectFinder.findById( RegistrySubject.add(s, "subjY", "person", "subjY").getId(), true );
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
   * From <a href="https://bugs.internet2.edu/jira/browse/GRP-1">jira:grouper:#1</a>.
   * <pre>
   * 1. I created a group A and added two direct members Subject X and Subject Y.
   * 2. I created a group B and added one direct member Subject Y.
   * 3. I created a composite group C that is A\B (the complement of B within A). It has a
   *    single indirect member X.
   * 4. I created a group D and added a single direct member of group C. Group D also shows
   *    Subject X as an indirect member.
   * 5. I modified the composite group C so that C is now A U B (A union B). Group C now has
   *    two indirect members Subjects X and Y.
   * 6. I looked at the member list of Group D. Group C is still a direct member, but it is the
   *    only member of D. I had also expected to see Subjects X and Y as indirect members. 
   * </pre>
   * @since   1.2.0
   */
  public void test_addCompositeMember_propogateEffectiveChangesOnCompositeTypeChange() {
    try {
      // (1)
      gA.addMember(subjX);
      gA.addMember(subjY);
      // (2)
      gB.addMember(subjX);
      // (3)
      gC.addCompositeMember( CompositeType.COMPLEMENT, gA, gB );
      // (4)
      gD.addMember( gC.toSubject() );
      // (5)
      gC.deleteCompositeMember();
      gC.addCompositeMember( CompositeType.UNION, gA, gB );
    }
    catch (Exception eShouldNotHappen) {
      fail( "ERROR INITIALIZING TEST: " + ExceptionUtils.getFullStackTrace(eShouldNotHappen));
    }
    assertTrue( "gD has immediate gC",    gD.hasImmediateMember( gC.toSubject() ) );
    assertTrue( "gD has effective subjX", gD.hasEffectiveMember( subjX ) );
    assertTrue( "gD has effective subjY", gD.hasEffectiveMember( subjY ) );
  }

} 

