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

package edu.internet2.middleware.grouper.membership;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * Test {@link MembershipFinder}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: TestMembershipFinder.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
 */
public class TestMembershipFinder extends GrouperTest {


  public void setUp() {
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
  }



  public void test_findMembers_nullGroup() {
    try {
      MembershipFinder.findMembers(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findMembers_nullField() {
    try {
      MembershipFinder.findMembers( new Group(), null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findMembers_findZeroMembers() 
    throws  GroupAddException,  
            InsufficientPrivilegeException,
            SessionException,
            StemAddException
  {
    Group g = StemFinder.findRootStem( GrouperSession.start( SubjectFinder.findRootSubject() ) )
                .addChildStem("top", "top")
                .addChildGroup("child", "child")
                ;
    assertEquals( 0, MembershipFinder.findMembers( g, Group.getDefaultList() ).size() );
  }

  public void test_findMembers_findOneMember() 
    throws  GroupAddException,  
            InsufficientPrivilegeException,
            MemberAddException,
            SessionException,
            StemAddException
  {
    GrouperSession  s = GrouperSession.start( SubjectFinder.findRootSubject() );
    Group           g = StemFinder.findRootStem(s)
                          .addChildStem("top", "top")
                          .addChildGroup("child", "child")
                          ;
    g.addMember( SubjectFinder.findAllSubject() );
    assertEquals( 1, MembershipFinder.findMembers( g, Group.getDefaultList() ).size() );
  }

}

