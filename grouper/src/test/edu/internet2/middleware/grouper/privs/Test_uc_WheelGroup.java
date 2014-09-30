/**
 * Copyright 2014 Internet2
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
 */
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

package edu.internet2.middleware.grouper.privs;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test wheel group use cases.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_uc_WheelGroup.java,v 1.3 2009-08-11 20:18:09 mchyzer Exp $
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
    //TestRunner.run(new Test_uc_WheelGroup("test_addingMemberToWheelGroupShouldElevatePrivilegesWithinSession"));
    TestRunner.run(Test_uc_WheelGroup.class);
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
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
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
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    assertFalse( s.getMember().canAdmin(dev) );
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
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
    Group wheelGroup = GroupFinder.findByUuid(grouperSession,wheelUuid, true);
    
    try {
      wheelGroup.addMember( this.subjA );
    } finally {
    }
    
    // start session and turn on wheel
    GrouperSession s = GrouperSession.start(this.subjA);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    // now should be able to grant admin 
    assertTrue( s.getMember().canAdmin(dev) );
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
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
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put( GrouperConfig.PROP_USE_WHEEL_GROUP, "true" );
    Member  mA  = MemberFinder.findBySubject(s, subjA, true);
    Group   g   = GroupFinder.findByUuid( s, wheel.getUuid(), true );
    //WheelAccessResolver caches wheel group membership
    //so doing a pre-check breaks the subsequent ADMIN check
    assertFalse( "does not have ADMIN", g.hasAdmin(subjA) );
    assertFalse( "cannot ADMIN", mA.canAdmin(g) );
    s.getAccessResolver().flushCache();
    g.addMember(subjA);
    assertTrue( "now has ADMIN", g.hasAdmin(subjA) );
    assertTrue( "now can ADMIN", mA.canAdmin(g) );

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put( GrouperConfig.PROP_USE_WHEEL_GROUP, "false" );
  } 
  
  /**
   * If a subject is a member of the wheel group and we're in self mode, the 
   * subject should not have wheel access.
   * @throws GrantPrivilegeException
   * @throws GroupNotFoundException
   * @throws InsufficientPrivilegeException
   * @throws MemberAddException
   * @throws SchemaException
   * @throws SessionException
   */
  public void test_preventAdminWhenMemberOfWheelAndSelfMode()
      throws GrantPrivilegeException, GroupNotFoundException,
      InsufficientPrivilegeException, MemberAddException, SchemaException,
      SessionException {
    // make this.subjA a member of wheel
    Subject rootSubject = SubjectFinder.findRootSubject();
    GrouperSession grouperSession = GrouperSession.start(rootSubject);
    String wheelUuid = wheel.getUuid();
    Group wheelGroup = GroupFinder.findByUuid(grouperSession, wheelUuid, true);

    wheelGroup.addMember(this.subjA);

    // start session and turn on wheel
    GrouperSession s = GrouperSession.start(this.subjA);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "true");
    s.setConsiderIfWheelMember(false);
    // verify no admin privilege
    assertFalse(s.getMember().canAdmin(dev));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "false");
  }
  
  /**
   * If a subject is a member of the wheel group and we're in self mode, the 
   * subject should not have wheel access.
   * @throws GrantPrivilegeException
   * @throws GroupNotFoundException
   * @throws InsufficientPrivilegeException
   * @throws MemberAddException
   * @throws SchemaException
   * @throws SessionException
   */
  public void test_preventStemWhenMemberOfWheelAndSelfMode()
      throws GrantPrivilegeException, GroupNotFoundException,
      InsufficientPrivilegeException, MemberAddException, SchemaException,
      SessionException {
    // make this.subjA a member of wheel
    Subject rootSubject = SubjectFinder.findRootSubject();
    GrouperSession grouperSession = GrouperSession.start(rootSubject);
    String wheelUuid = wheel.getUuid();
    Group wheelGroup = GroupFinder.findByUuid(grouperSession, wheelUuid, true);

    wheelGroup.addMember(this.subjA);

    // start session and turn on wheel
    GrouperSession s = GrouperSession.start(this.subjA);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "true");
    s.setConsiderIfWheelMember(false);
    // verify no stem privilege
    assertFalse(s.getMember().canStem(dev.getParentStem()));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "false");
  }
  
  /**
   * If a subject is a member of the wheel group and we're in self mode, the 
   * subject should still have access if the subject is a member of the access list.
   * @throws GrantPrivilegeException
   * @throws GroupNotFoundException
   * @throws InsufficientPrivilegeException
   * @throws MemberAddException
   * @throws SchemaException
   * @throws SessionException
   */
  public void test_canAdminWhenMemberOfWheelAndSelfMode()
      throws GrantPrivilegeException, GroupNotFoundException,
      InsufficientPrivilegeException, MemberAddException, SchemaException,
      SessionException {
    // make this.subjA a member of wheel
    Subject rootSubject = SubjectFinder.findRootSubject();
    GrouperSession grouperSession = GrouperSession.start(rootSubject);
    String wheelUuid = wheel.getUuid();
    Group wheelGroup = GroupFinder.findByUuid(grouperSession, wheelUuid, true);

    wheelGroup.addMember(this.subjA);
    dev.grantPriv(this.subjA, AccessPrivilege.ADMIN);

    // start session and turn on wheel
    GrouperSession s = GrouperSession.start(this.subjA);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "true");
    s.setConsiderIfWheelMember(false);
    // verify admin privilege
    assertTrue(s.getMember().canAdmin(dev));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "false");
  }
  
  /**
   * If a subject is a member of the wheel group and we're in self mode, the 
   * subject should still have access if the subject is a member of the access list.
   * @throws GrantPrivilegeException
   * @throws GroupNotFoundException
   * @throws InsufficientPrivilegeException
   * @throws MemberAddException
   * @throws SchemaException
   * @throws SessionException
   */
  public void test_canStemWhenMemberOfWheelAndSelfMode()
      throws GrantPrivilegeException, GroupNotFoundException,
      InsufficientPrivilegeException, MemberAddException, SchemaException,
      SessionException {
    // make this.subjA a member of wheel
    Subject rootSubject = SubjectFinder.findRootSubject();
    GrouperSession grouperSession = GrouperSession.start(rootSubject);
    String wheelUuid = wheel.getUuid();
    Group wheelGroup = GroupFinder.findByUuid(grouperSession, wheelUuid, true);

    wheelGroup.addMember(this.subjA);
    dev.getParentStem().grantPriv(this.subjA, NamingPrivilege.STEM);

    // start session and turn on wheel
    GrouperSession s = GrouperSession.start(this.subjA);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "true");
    s.setConsiderIfWheelMember(false);
    // verify stem privilege
    assertTrue(s.getMember().canStem(dev.getParentStem()));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperConfig.PROP_USE_WHEEL_GROUP, "false");
  }
  
  

} 

