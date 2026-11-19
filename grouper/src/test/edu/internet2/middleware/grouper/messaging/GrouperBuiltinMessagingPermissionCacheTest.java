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

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageDefault;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.subject.Subject;

/**
 * Security regression test for builtinMessagingPermissionCacheAuthorization: the built-in
 * messaging permission cache stored "allowed" for every raw permission row of the messaging
 * role, so an explicit deny, a disabled / not yet enabled / expired assignment, or a limited
 * permission authorized send, receive and acknowledge.  After the fix the cache holds only
 * effective allows (same conflict processing as PermissionFinder.hasPermission(), enabled
 * assignments only, limits evaluated with an empty environment and failing closed).
 *
 * Fails before the fix: every deny / inactive / limited case returns true from the allowedTo*
 * helpers, and the denied send inserts a message row.
 */
public class GrouperBuiltinMessagingPermissionCacheTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperBuiltinMessagingPermissionCacheTest("testHelpersMatchEffectivePermission"));
  }

  /**
   *
   */
  public GrouperBuiltinMessagingPermissionCacheTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperBuiltinMessagingPermissionCacheTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- fixture

  /** queue: live allow */
  private static final String Q_ALLOW = "secQAllow";
  /** queue: explicit deny only */
  private static final String Q_DENY = "secQDeny";
  /** queue: implied by Q_ALL (allowed through inheritance) */
  private static final String Q_IMPLIED = "secQImplied";
  /** queue: implied by Q_ALL, plus a more specific direct deny */
  private static final String Q_SPECIFIC_DENY = "secQSpecificDeny";
  /** queue: broader queue whose allow is inherited by Q_IMPLIED and Q_SPECIFIC_DENY */
  private static final String Q_ALL = "secQAll";
  /** queue: allow whose assignment is not enabled yet */
  private static final String Q_FUTURE = "secQFuture";
  /** queue: allow whose assignment has expired */
  private static final String Q_EXPIRED = "secQExpired";
  /** queue: allow with a limit that needs request data */
  private static final String Q_LIMITED = "secQLimited";
  /** topic: explicit deny only */
  private static final String T_DENY = "secTDeny";
  /** topic: live allow */
  private static final String T_ALLOW = "secTAllow";

  /** the messaging role */
  private Role messagingRole;

  /**
   * build queues, topics and assignments as root, then clear the messaging cache
   */
  private void setupPermissions() {
    GrouperSession.startRootSession();

    for (String queue : new String[]{Q_ALLOW, Q_DENY, Q_IMPLIED, Q_SPECIFIC_DENY, Q_ALL, Q_FUTURE, Q_EXPIRED, Q_LIMITED}) {
      GrouperBuiltinMessagingSystem.createQueue(queue);
    }
    GrouperBuiltinMessagingSystem.createTopic(T_DENY);
    GrouperBuiltinMessagingSystem.createTopic(T_ALLOW);
    GrouperBuiltinMessagingSystem.topicAddSendToQueue(T_DENY, Q_ALLOW);
    GrouperBuiltinMessagingSystem.topicAddSendToQueue(T_ALLOW, Q_ALLOW);

    this.messagingRole = (Role)GroupFinder.findByName(GrouperSession.staticGrouperSession(),
        GrouperBuiltinMessagingSystem.grouperMessageNameOfRole(), true);
    Subject subj0 = SubjectTestHelper.SUBJ0;
    // SUBJ1 is an active member of the role but has no permission rows at all
    this.messagingRole.addMember(SubjectTestHelper.SUBJ1, false);

    // live allows (send + receive on Q_ALLOW, send on T_ALLOW)
    GrouperBuiltinMessagingSystem.allowSendToQueue(Q_ALLOW, subj0);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue(Q_ALLOW, subj0);
    GrouperBuiltinMessagingSystem.allowSendToTopic(T_ALLOW, subj0);

    // explicit deny-only rows
    assign(GrouperBuiltinMessagingSystem.actionSendToQueue, queue(Q_DENY), PermissionAllowed.DISALLOWED);
    assign(GrouperBuiltinMessagingSystem.actionReceive, queue(Q_DENY), PermissionAllowed.DISALLOWED);
    assign(GrouperBuiltinMessagingSystem.actionSendToTopic, topic(T_DENY), PermissionAllowed.DISALLOWED);

    // broader allow inherited by two queues, one of which also has a direct (more specific) deny
    queue(Q_ALL).getAttributeDefNameSetDelegate().addToAttributeDefNameSet(queue(Q_IMPLIED));
    queue(Q_ALL).getAttributeDefNameSetDelegate().addToAttributeDefNameSet(queue(Q_SPECIFIC_DENY));
    assign(GrouperBuiltinMessagingSystem.actionSendToQueue, queue(Q_ALL), PermissionAllowed.ALLOWED);
    assign(GrouperBuiltinMessagingSystem.actionSendToQueue, queue(Q_SPECIFIC_DENY), PermissionAllowed.DISALLOWED);

    // allow that is not enabled until next week
    for (String action : new String[]{GrouperBuiltinMessagingSystem.actionSendToQueue, GrouperBuiltinMessagingSystem.actionReceive}) {
      AttributeAssign future = assign(action, queue(Q_FUTURE), PermissionAllowed.ALLOWED);
      future.setEnabledTime(new Timestamp(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));
      future.saveOrUpdate();
    }

    // allow that expired yesterday
    for (String action : new String[]{GrouperBuiltinMessagingSystem.actionSendToQueue, GrouperBuiltinMessagingSystem.actionReceive}) {
      AttributeAssign expired = assign(action, queue(Q_EXPIRED), PermissionAllowed.ALLOWED);
      expired.setDisabledTime(new Timestamp(System.currentTimeMillis() - 24L * 60 * 60 * 1000));
      expired.saveOrUpdate();
    }

    // allow with a limit that needs an "amount" the messaging system never supplies
    AttributeAssign limited = assign(GrouperBuiltinMessagingSystem.actionSendToQueue, queue(Q_LIMITED), PermissionAllowed.ALLOWED);
    limited.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 50000");

    clearMessagingCache();
  }

  /**
   * @param action
   * @param resource
   * @param permissionAllowed
   * @return the assignment on the SUBJ0 role membership
   */
  private AttributeAssign assign(String action, AttributeDefName resource, PermissionAllowed permissionAllowed) {
    return this.messagingRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        action, resource, SubjectTestHelper.SUBJ0, permissionAllowed).getAttributeAssign();
  }

  /**
   * @param extension
   * @return the queue permission name
   */
  private static AttributeDefName queue(String extension) {
    return AttributeDefNameFinder.findByName(GrouperBuiltinMessagingSystem.queueStemName() + ":" + extension, true);
  }

  /**
   * @param extension
   * @return the topic permission name
   */
  private static AttributeDefName topic(String extension) {
    return AttributeDefNameFinder.findByName(GrouperBuiltinMessagingSystem.topicStemName() + ":" + extension, true);
  }

  /**
   * the cache would otherwise hide assignments made after the last rebuild
   */
  private static void clearMessagingCache() {
    try {
      Method clearCache = GrouperBuiltinMessagingSystem.class.getDeclaredMethod("clearCache");
      clearCache.setAccessible(true);
      clearCache.invoke(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * normal permission evaluator, for comparison
   * @param action
   * @param resource
   * @param subject
   * @return true if allowed
   */
  private boolean hasPermission(String action, AttributeDefName resource, Subject subject) {
    return new PermissionFinder().addRole(this.messagingRole).addSubject(subject)
        .addAction(action).addPermissionName(resource).hasPermission();
  }

  /**
   * @param queue
   * @return number of message rows in the queue
   */
  private static int messageCount(String queue) {
    return GrouperUtil.length(GrouperDAOFactory.getFactory().getMessage().findByQueue(queue, 1000));
  }

  // ---------------------------------------------------------------- tests

  /**
   * the three helpers return only effective allows, and agree with hasPermission() where
   * hasPermission() has the same inputs (it does not run limits without env vars)
   */
  public void testHelpersMatchEffectivePermission() {
    setupPermissions();
    Subject subj0 = SubjectTestHelper.SUBJ0;
    String send = GrouperBuiltinMessagingSystem.actionSendToQueue;

    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_ALLOW, subj0));
    assertTrue(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue(Q_ALLOW, subj0));
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToTopic(T_ALLOW, subj0));
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_ALL, subj0));
    assertTrue("inherited allow still works", GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_IMPLIED, subj0));

    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_DENY, subj0));
    assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue(Q_DENY, subj0));
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToTopic(T_DENY, subj0));
    assertFalse("specific deny beats broader allow", GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_SPECIFIC_DENY, subj0));
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_FUTURE, subj0));
    assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue(Q_FUTURE, subj0));
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_EXPIRED, subj0));
    assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue(Q_EXPIRED, subj0));
    assertFalse("limit with no environment fails closed", GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_LIMITED, subj0));

    // no permission row at all
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_ALLOW, SubjectTestHelper.SUBJ1));
    assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue(Q_ALLOW, SubjectTestHelper.SUBJ1));

    // root is still allowed by design
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_DENY, SubjectFinder.findRootSubject()));

    // same decision as the normal evaluator for every case it evaluates the same way
    for (String queue : new String[]{Q_ALLOW, Q_DENY, Q_IMPLIED, Q_SPECIFIC_DENY, Q_ALL, Q_FUTURE, Q_EXPIRED}) {
      assertEquals(queue, hasPermission(send, queue(queue), subj0),
          GrouperBuiltinMessagingSystem.allowedToSendToQueue(queue, subj0));
    }
    assertEquals(hasPermission(GrouperBuiltinMessagingSystem.actionSendToTopic, topic(T_DENY), subj0),
        GrouperBuiltinMessagingSystem.allowedToSendToTopic(T_DENY, subj0));

    // a rebuild of the cache gives the same answers (expiry recomputes, it does not grant)
    clearMessagingCache();
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_DENY, subj0));
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToQueue(Q_ALLOW, subj0));
  }

  /**
   * a denied send through the public API (non-wheel session) throws and writes no message row;
   * an allowed send still works
   */
  public void testDeniedSendWritesNothing() {
    setupPermissions();

    int denyBefore = messageCount(Q_DENY);
    int specificBefore = messageCount(Q_SPECIFIC_DENY);
    int allowBefore = messageCount(Q_ALLOW);

    GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      for (String queue : new String[]{Q_DENY, Q_SPECIFIC_DENY, Q_FUTURE, Q_EXPIRED, Q_LIMITED}) {
        try {
          GrouperMessagingEngine.send(new GrouperMessageSendParam().assignQueueType(GrouperMessageQueueType.queue)
              .assignQueueOrTopicName(queue).addMessageBody("denied"));
          fail("send to " + queue + " should be denied");
        } catch (RuntimeException re) {
          assertTrue(GrouperUtil.getFullStackTrace(re), GrouperUtil.getFullStackTrace(re).contains("not allowed to send"));
        }
      }
      try {
        GrouperMessagingEngine.send(new GrouperMessageSendParam().assignQueueType(GrouperMessageQueueType.topic)
            .assignQueueOrTopicName(T_DENY).addMessageBody("denied"));
        fail("send to topic should be denied");
      } catch (RuntimeException re) {
        assertTrue(GrouperUtil.getFullStackTrace(re), GrouperUtil.getFullStackTrace(re).contains("not allowed to send"));
      }

      // allowed send still works
      GrouperMessagingEngine.send(new GrouperMessageSendParam().assignQueueType(GrouperMessageQueueType.queue)
          .assignQueueOrTopicName(Q_ALLOW).addMessageBody("allowed"));
    } finally {
      GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
      GrouperSession.startRootSession();
    }

    assertEquals(denyBefore, messageCount(Q_DENY));
    assertEquals(specificBefore, messageCount(Q_SPECIFIC_DENY));
    // the denied topic send fans out to Q_ALLOW, so only the one allowed message is new
    assertEquals(allowBefore + 1, messageCount(Q_ALLOW));
  }

  /**
   * a denied receive or acknowledge (non-wheel session) does not claim, change or delete the
   * message
   */
  public void testDeniedReceiveAndAcknowledge() {
    setupPermissions();

    // root puts a message on the queues SUBJ0 is not effectively allowed to receive from
    for (String queue : new String[]{Q_DENY, Q_FUTURE, Q_EXPIRED}) {
      GrouperMessagingEngine.send(new GrouperMessageSendParam().assignQueueType(GrouperMessageQueueType.queue)
          .assignQueueOrTopicName(queue).addMessageBody("secret"));
    }
    Member rootMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(),
        SubjectFinder.findRootSubject(), true);
    Set<GrouperMessageHibernate> before = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(rootMember.getId());
    assertEquals(3, GrouperUtil.length(before));

    GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      for (GrouperMessageHibernate grouperMessageHibernate : before) {
        String queue = grouperMessageHibernate.getQueueName();
        try {
          GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName(queue));
          fail("receive from " + queue + " should be denied");
        } catch (RuntimeException re) {
          assertTrue(GrouperUtil.getFullStackTrace(re), GrouperUtil.getFullStackTrace(re).contains("not allowed to receive"));
        }
        GrouperMessageDefault grouperMessage = new GrouperMessageDefault();
        grouperMessage.setId(grouperMessageHibernate.getId());
        try {
          GrouperMessagingEngine.acknowledge(new GrouperMessageAcknowledgeParam()
              .assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed)
              .assignQueueName(queue).addGrouperMessage((GrouperMessage)grouperMessage));
          fail("acknowledge on " + queue + " should be denied");
        } catch (RuntimeException re) {
          assertTrue(GrouperUtil.getFullStackTrace(re), GrouperUtil.getFullStackTrace(re).contains("not allowed to receive"));
        }
      }
    } finally {
      GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
      GrouperSession.startRootSession();
    }

    // every message is still there in its original state
    for (GrouperMessageHibernate original : before) {
      GrouperMessageHibernate now = GrouperDAOFactory.getFactory().getMessage().findById(original.getId(), false);
      assertNotNull(original.getQueueName(), now);
      assertEquals(original.getQueueName(), original.getState(), now.getState());
    }
  }
}
