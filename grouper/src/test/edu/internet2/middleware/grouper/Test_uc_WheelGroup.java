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
import org.apache.commons.lang.StringUtils;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import  edu.internet2.middleware.subject.*;

/**
 * Test wheel group use cases.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_uc_WheelGroup.java,v 1.13 2008-07-21 04:43:57 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_uc_WheelGroup extends GrouperTest {

  /**
   * 
   */
  public Test_uc_WheelGroup() {
    super();
    
  }

  /**
   * @param name
   */
  public Test_uc_WheelGroup(String name) {
    super(name);
    
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    TestRunner.run(new Test_uc_WheelGroup("test_addingMemberToWheelGroupShouldElevatePrivilegesWithinSession"));
    //TestRunner.run(Test_uc_WheelGroup.class);
  }


  private Group   dev, wheel;
  private R       r;
  private Subject subjA;


  public void setUp() {
    super.setUp();
    try {
      r     = R.getContext("grouper");
      
      String wheelGroupName = GrouperConfig.getProperty("groups.wheel.group");
      
      if (StringUtils.isBlank(wheelGroupName)) {
        throw new RuntimeException("grouper.properties must have an extry for " +
        		"groups.wheel.group, e.g. etc:sysadmingroup");
      }
      GrouperSession grouperSession = r.startRootSession();
      String extension = GrouperUtil.extensionFromName(wheelGroupName);
      wheel = Group.saveGroup(grouperSession, wheelGroupName, null, wheelGroupName, extension, "description for " + extension, SaveMode.INSERT_OR_UPDATE, true);
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
    Subject rootSubject = SubjectFinder.findRootSubject();
    GrouperSession grouperSession = GrouperSession.start(rootSubject);
    String wheelUuid = wheel.getUuid();
    Group wheelGroup = GroupFinder.findByUuid(grouperSession,wheelUuid);
    
    //System.out.println("##############  Before adding member  ##############");
    try {
      wheelGroup.addMember( this.subjA );
    } finally {
      //System.out.println("##############  After adding member  ##############");
    }
    
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
    GrouperSession s2 = GrouperSession.staticGrouperSession();
    Member  mA  = MemberFinder.findBySubject(s, subjA);
    GrouperSession s3 = GrouperSession.staticGrouperSession();
    Group   g   = GroupFinder.findByUuid( s, wheel.getUuid() );
    GrouperSession s4 = GrouperSession.staticGrouperSession();
    //WheelAccessResolver caches wheel group membership
    //so doing a pre-check breaks the subsequent ADMIN check
    assertFalse( "does not have ADMIN", g.hasAdmin(subjA) );
    GrouperSession s5 = GrouperSession.staticGrouperSession();
    assertFalse( "cannot ADMIN", mA.canAdmin(g) );
    GrouperSession s6 = GrouperSession.staticGrouperSession();
    try {
    	Thread.currentThread().sleep(3000);
    }catch(InterruptedException e){}
    g.addMember(subjA);
    GrouperSession s7 = GrouperSession.staticGrouperSession();
    assertTrue( "now has ADMIN", g.hasAdmin(subjA) );
    GrouperSession s8 = GrouperSession.staticGrouperSession();
    assertTrue( "now can ADMIN", mA.canAdmin(g) );
    GrouperSession s9 = GrouperSession.staticGrouperSession();

    s.setConfig( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
  } 
  

} 

