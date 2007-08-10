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
 * @version $Id: Test_uc_WheelGroup.java,v 1.3 2007-08-10 13:19:14 blair Exp $
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
    GrouperConfig.internal_setProperty( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );   // FIXME 20070725
    GrouperConfig.internal_setProperty( GrouperConfig.PROP_WHEEL_GROUP, "etc:wheel" );  // FIXME 20070725

    assertFalse( GrouperSession.start(subjA).getMember().canAdmin(dev) );

    GrouperConfig.internal_setProperty( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );  // FIXME 20070725
  }

  public void test_fromNotAMemberOfTheWheelGroupToAMemberOfTheWheelGroup() 
    throws  InsufficientPrivilegeException,
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
      
    // After wheel
    assertTrue( "now has CREATE", etc.hasCreate(subjA) );
    assertTrue( "now can CREATE", mA.canCreate(etc) );

    GrouperConfig.internal_setProperty( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );  // FIXME 20070806
  } 

} 

