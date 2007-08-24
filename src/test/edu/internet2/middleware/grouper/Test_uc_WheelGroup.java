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
 * @version $Id: Test_uc_WheelGroup.java,v 1.5 2007-08-24 14:18:16 blair Exp $
 * @since   @HEAD@
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


  public void test_presenceOfWheelGroupDoesNotAutomaticallyGrantPrivs() 
    throws  SessionException
  {
    GrouperSession s = GrouperSession.start(this.subjA);
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    assertFalse( s.getMember().canAdmin(dev) );
    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
  }


  /** 
   * @since   @HEAD@
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
   * @since   @HEAD@
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

/* FIXME 20070816 temporarily disabled.  see GRP-24 for more details.
  public void test_fromNotAMemberOfTheWheelGroupToAMemberOfTheWheelGroup() 
    throws  InsufficientPrivilegeException,
            InterruptedException,
            MemberAddException,
            MemberNotFoundException,
            SessionException
  {
    GrouperSession  s   = GrouperSession.start( SubjectFinder.findRootSubject() );
    Member          mA  = MemberFinder.findBySubject(s, subjA);

    // Before wheel 
    assertFalse( "does not have CREATE", etc.hasCreate(subjA) );
    assertFalse( "cannot CREATE", mA.canCreate(etc) );

    // Enable wheel
    wheel.addMember(subjA);
    GrouperConfig.internal_setProperty( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    GrouperConfig.internal_setProperty( GrouperConfig.PROP_WHEEL_GROUP, "etc:wheel");

    // TODO 20070815  attempted temporary workaround for GRP-24.  unfortunately i don't
    //                think this actually helps at all.
    Thread.sleep(2);
      
    // After wheel
    assertTrue( "now has CREATE", etc.hasCreate(subjA) );
    assertTrue( "now can CREATE", mA.canCreate(etc) );

    GrouperConfig.internal_setProperty( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );  // FIXME 20070806
  } 
*/

} 

