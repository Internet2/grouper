/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.messaging;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.grouperMessagingAWS.GrouperMessagingSqsSystem;
import edu.internet2.middleware.grouperMessagingActiveMQ.GrouperMessagingActiveMQSystem;
import edu.internet2.middleware.grouperMessagingRabbitmq.GrouperMessagingRabbitmqSystem;
import edu.internet2.middleware.subject.Subject;

/**
 * External messaging systems (RabbitMQ, SQS, ActiveMQ) use Grouper's broker credentials, so a
 * subject who is not wheel or root must be a member of the group in grouper.properties
 * grouper.messaging.system.&lt;name&gt;.allowedGroupName to send, receive or acknowledge, and
 * only wheel or root may autocreate queues or topics.  Threads with no session (daemons) are not
 * checked.  The check runs before any broker connection, so no broker is needed: a refused call
 * throws InsufficientPrivilegeException, anything else would be a connection error.
 * Before the fix this class does not compile (GrouperMessagingExternalAuthorization is new), and
 * the systems try to connect for any caller.
 */
public class GrouperMessagingExternalAuthorizationTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMessagingExternalAuthorizationTest("testNonWheelRefusedWhenNotConfigured"));
  }

  /**
   *
   */
  public GrouperMessagingExternalAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperMessagingExternalAuthorizationTest(String name) {
    super(name);
  }

  /** messaging system name used in the tests (not configured, the check runs before the config is read) */
  private static final String SYSTEM_NAME = "testExternalMessaging";

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** group whose members may use SYSTEM_NAME (only configured in some tests) */
  private Group allowedGroup;

  /**
   * make the allowed group as root, SUBJ0 is a member
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(this.rootSession);
    Stem top = root.addChildStem("top", "top");
    this.allowedGroup = top.addChildGroup("messagingAllowed", "messagingAllowed");
    this.allowedGroup.addMember(SubjectTestHelper.SUBJ0, false);
  }

  /**
   * remove config and stop sessions
   */
  @Override
  public void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
        GrouperMessagingExternalAuthorization.allowedGroupNameConfigKey(SYSTEM_NAME));
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // refused through each external system
  // ---------------------------------------------------------------------------------------

  /**
   * nothing configured: a non-wheel subject is refused by send, receive and acknowledge on all
   * three external systems (fail closed)
   */
  public void testNonWheelRefusedWhenNotConfigured() {
    assertAllSystemsRefused(SubjectTestHelper.SUBJ0);
  }

  /**
   * group configured: a non-member is refused on all three systems
   */
  public void testNonMemberRefusedWhenConfigured() {
    configureAllowedGroup(this.allowedGroup.getName());
    assertAllSystemsRefused(SubjectTestHelper.SUBJ1);
  }

  /**
   * group name configured but the group does not exist: refused, even for SUBJ0
   */
  public void testMissingGroupRefused() {
    configureAllowedGroup("top:doesNotExist");
    assertAllSystemsRefused(SubjectTestHelper.SUBJ0);
  }

  /**
   * a blank messaging system name resolves to the default system, it does not skip the check
   */
  public void testBlankSystemNameStillChecked() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      try {
        new GrouperMessagingSqsSystem().send(new GrouperMessageSendParam());
        fail("blank system name should be checked against the default system");
      } catch (InsufficientPrivilegeException ipe) {
        // good
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ---------------------------------------------------------------------------------------
  // allowed
  // ---------------------------------------------------------------------------------------

  /**
   * a member of the configured group passes the check (no autocreate)
   */
  public void testMemberAllowed() {
    configureAllowedGroup(this.allowedGroup.getName());
    assertAllowed(SubjectTestHelper.SUBJ0, false);
  }

  /**
   * a member still may not autocreate queues or topics, on any system
   */
  public void testAutocreateRefusedForMember() {
    configureAllowedGroup(this.allowedGroup.getName());
    assertRefused(SubjectTestHelper.SUBJ0, true);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      try {
        new GrouperMessagingRabbitmqSystem().send(new GrouperMessageSendParam()
            .assignGrouperMessageSystemName(SYSTEM_NAME).assignAutocreateObjects(true));
        fail("autocreate should be wheel only");
      } catch (InsufficientPrivilegeException ipe) {
        // good
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * root and wheel pass with nothing configured, including autocreate
   */
  public void testRootAndWheelAllowed() {
    assertAllowed(SubjectTestHelper.SUBJR, true);
    assertAllowed(SubjectTestHelper.SUBJ1, false, true);

    configureWheelWithSubj1();
    assertAllowed(SubjectTestHelper.SUBJ1, true);
  }

  /**
   * a thread with no grouper session (daemon code) is not checked
   */
  public void testNoSessionNotChecked() throws Exception {
    final Throwable[] thrown = new Throwable[1];
    // the static session is a plain ThreadLocal, so a new thread starts with none
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          assertNull(GrouperSession.staticGrouperSession(false));
          GrouperMessagingExternalAuthorization.assertAllowed(
              new GrouperMessageSystemParam().assignMesssageSystemName(SYSTEM_NAME), "send");
        } catch (Throwable t) {
          thrown[0] = t;
        }
      }
    });
    thread.start();
    thread.join();
    assertNull(String.valueOf(thrown[0]), thrown[0]);
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * @param groupName to put in grouper.properties for SYSTEM_NAME
   */
  private static void configureAllowedGroup(String groupName) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMessagingExternalAuthorization.allowedGroupNameConfigKey(SYSTEM_NAME), groupName);
  }

  /**
   * as this subject, send, receive and acknowledge on each external system must throw
   * InsufficientPrivilegeException (not a connection error)
   * @param subject
   */
  private static void assertAllSystemsRefused(Subject subject) {
    GrouperMessagingSystem[] systems = new GrouperMessagingSystem[] {
        new GrouperMessagingRabbitmqSystem(), new GrouperMessagingSqsSystem(), new GrouperMessagingActiveMQSystem()};
    GrouperSession session = GrouperSession.start(subject);
    try {
      for (GrouperMessagingSystem system : systems) {
        String label = system.getClass().getSimpleName();
        try {
          system.send(new GrouperMessageSendParam().assignGrouperMessageSystemName(SYSTEM_NAME)
              .assignQueueOrTopicName("testQueue").addMessageBody("hello"));
          fail(label + " send should be refused");
        } catch (InsufficientPrivilegeException ipe) {
          // good
        }
        try {
          system.receive(new GrouperMessageReceiveParam().assignGrouperMessageSystemName(SYSTEM_NAME)
              .assignQueueName("testQueue"));
          fail(label + " receive should be refused");
        } catch (InsufficientPrivilegeException ipe) {
          // good
        }
        try {
          system.acknowledge(new GrouperMessageAcknowledgeParam().assignGrouperMessageSystemName(SYSTEM_NAME)
              .assignQueueName("testQueue").assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed));
          fail(label + " acknowledge should be refused");
        } catch (InsufficientPrivilegeException ipe) {
          // good
        }
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * @param subject
   * @param autocreate
   */
  private static void assertAllowed(Subject subject, boolean autocreate) {
    assertAllowed(subject, autocreate, false);
  }

  /**
   * call the check directly as this subject (a real system call would go on to connect)
   * @param subject
   * @param autocreate
   * @param expectRefused true to expect InsufficientPrivilegeException instead
   */
  private static void assertAllowed(Subject subject, boolean autocreate, boolean expectRefused) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      GrouperMessagingExternalAuthorization.assertAllowed(new GrouperMessageSystemParam()
          .assignMesssageSystemName(SYSTEM_NAME).assignAutocreateObjects(autocreate), "send");
      if (expectRefused) {
        fail("expected refused: " + subject.getId());
      }
    } catch (InsufficientPrivilegeException ipe) {
      if (!expectRefused) {
        throw ipe;
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * @param subject
   * @param autocreate
   */
  private static void assertRefused(Subject subject, boolean autocreate) {
    assertAllowed(subject, autocreate, true);
  }

  /**
   * make an etc:testWheel group with SUBJ1 in it and turn wheel on
   */
  private static void configureWheelWithSubj1() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Stem root = StemFinder.findRootStem(session);
      Stem etc = StemFinder.findByName(session, "etc", false);
      if (etc == null) {
        etc = root.addChildStem("etc", "etc");
      }
      Group wheel = etc.addChildGroup("testWheel", "testWheel");
      wheel.addMember(SubjectTestHelper.SUBJ1, false);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}
