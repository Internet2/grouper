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
 * @version $Id: Test_uc_WheelGroup.java,v 1.1 2007-08-02 16:46:51 blair Exp $
 * @since   @HEAD@
 */
public class Test_uc_WheelGroup extends GrouperTest {


  private Group   dev, users, wheel;
  private R       r;
  private Subject subjA, subjB;


  public void setUp() {
    super.setUp();
    try {
      r     = R.getContext("grouper");
      wheel = r.root.addChildStem("etc", "etc").addChildGroup("wheel", "wheel");
      dev   = r.getGroup("i2mi:grouper", "grouper-dev");
      users = r.getGroup("i2mi:grouper", "grouper-users");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
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

    GrouperSession s = GrouperSession.start(subjA);
    assertFalse( GrouperSession.start(subjA).getMember().canAdmin(dev) );

    GrouperConfig.internal_setProperty( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );  // FIXME 20070725
  }

} 

