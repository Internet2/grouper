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
import  edu.internet2.middleware.subject.*;

/**
 * Test wheel group use cases.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_uc_WheelGroup.java,v 1.9 2007-12-05 11:25:10 isgwb Exp $
 * @since   1.2.1
 */
public class Test_uc_WheelGroup extends GrouperTest {


  private Group   dev, wheel;
  private R       r;
  private Stem    etc;
  private Subject subjA;


  public void setUp() {
    super.setUp();
    try {
      r     = R.getContext("grouper");
      etc   = r.root.addChildStem("etc", "etc");
      wheel = etc.addChildGroup("wheel", "wheel");
      dev   = r.getGroup("i2mi:grouper", "grouper-dev");
      subjA = r.getSubject("a");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  /** 
   * @since   1.2.1
   */
  public void test_presenceOfWheelGroupDoesNotAutomaticallyGrantPrivs() 
    throws  SessionException
  {
    GrouperSession s = GrouperSession.start(this.subjA);
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    assertFalse( s.getMember().canAdmin(dev) );
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
  }


  /** 
   * @since   1.2.1
   */
  public void test_canAdminWhenMemberOfWheel()
    throws  GrantPrivilegeException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            MemberAddException,
            SchemaException,
            SessionException
  {
    // make this.subjA a member of wheel
    GroupFinder.findByUuid( 
      GrouperSession.start( SubjectFinder.findRootSubject() ), wheel.getUuid()
    ).addMember( this.subjA );

    // start session and turn on wheel
    GrouperSession s = GrouperSession.start(this.subjA);
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    // now should be able to grant admin 
    assertTrue( s.getMember().canAdmin(dev) );
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
  }

  /** 
   * @since   1.2.1
   */
  public void test_allCanAdminWhenMemberOfWheel()
    throws  GrantPrivilegeException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            MemberAddException,
            SchemaException,
            SessionException
  {
    // make ALL a member of wheel
    GroupFinder.findByUuid( 
      GrouperSession.start( SubjectFinder.findRootSubject() ), wheel.getUuid()
    ).addMember( SubjectFinder.findAllSubject() );

    // start session and turn on wheel
    GrouperSession s = GrouperSession.start( SubjectFinder.findAllSubject() );
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    // now should be able to grant admin 
    assertTrue( s.getMember().canAdmin(dev) );
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
  }

  /**
   * @since   1.2.1
   */
   // 2007/12/03: Gary Brown
   //Since I added caching for wheel group members it can take up to 2 minutes before 
   //privileges will work
  public void test_addingMemberToWheelGroupShouldElevatePrivilegesWithinSession() 
    throws  GroupNotFoundException,
            InsufficientPrivilegeException,
            MemberAddException,
            MemberNotFoundException,
            SessionException
  {
    GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );

    Member  mA  = MemberFinder.findBySubject(s, subjA);
    Group   g   = GroupFinder.findByUuid( s, wheel.getUuid() );
    //WheelAccessResolver caches wheel group membership
    //so doing a pre-check breaks the subsequent ADMIN check
    assertFalse( "does not have ADMIN", g.hasAdmin(subjA) );
    assertFalse( "cannot ADMIN", mA.canAdmin(g) );
    try {
    	Thread.currentThread().sleep(3000);
    }catch(InterruptedException e){}
    g.addMember(subjA);
    assertTrue( "now has ADMIN", g.hasAdmin(subjA) );
    assertTrue( "now can ADMIN", mA.canAdmin(g) );

    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
  } 
  

} 

