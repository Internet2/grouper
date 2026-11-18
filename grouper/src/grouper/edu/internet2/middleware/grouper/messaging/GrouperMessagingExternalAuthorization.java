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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Authorization for the external messaging systems (RabbitMQ, AWS SQS, ActiveMQ).</p>
 *
 * <p>Those systems talk to the broker with the credentials configured for Grouper and never look
 * at who is calling, so any caller (for example any user admitted to WS sendMessage,
 * receiveMessage or acknowledge) would get Grouper's broker access.  Built-in messaging is not
 * affected: GrouperBuiltinMessagingSystem checks the caller's permissions per queue or topic.</p>
 *
 * <p>Rule, checked at the start of send, receive and acknowledge in each external system,
 * before any broker connection:</p>
 * <ul>
 *   <li>No static Grouper session on the thread: allowed.  This is internal code (daemons,
 *   listeners, change log consumers) with no user on whose behalf it runs.</li>
 *   <li>The session subject is root or wheel: allowed, including autocreate of queues/topics.</li>
 *   <li>Anyone else (WS caller, UI user, GSH as a non-wheel subject, a job running as a
 *   non-root subject): must be a member of the group in
 *   grouper.properties grouper.messaging.system.&lt;systemName&gt;.allowedGroupName.  Not
 *   configured, or the group is not found, means refused (fail closed).  Autocreate of queues or
 *   topics is refused even for members.</li>
 * </ul>
 *
 * <p>Uses only APIs that exist in every Grouper release from 5.0.0 on, so the same file ships in
 * every patch set.</p>
 */
public class GrouperMessagingExternalAuthorization {

  /** operation name for send */
  public static final String OPERATION_SEND = "send";

  /** operation name for receive */
  public static final String OPERATION_RECEIVE = "receive";

  /** operation name for acknowledge */
  public static final String OPERATION_ACKNOWLEDGE = "acknowledge";

  /**
   * config key (grouper.properties) for the group whose members may use a messaging system
   * @param messagingSystemName
   * @return the key
   */
  public static String allowedGroupNameConfigKey(String messagingSystemName) {
    return "grouper.messaging.system." + messagingSystemName + ".allowedGroupName";
  }

  /**
   * make sure the subject of the current static grouper session may use this external messaging
   * system for this operation, else throw.  Call before connecting to the broker.
   * @param grouperMessageSystemParam from the send, receive or acknowledge param (may be null,
   * which means the default messaging system)
   * @param operation one of the OPERATION_ constants, only used in the error message
   * @throws InsufficientPrivilegeException if not allowed
   */
  public static void assertAllowed(GrouperMessageSystemParam grouperMessageSystemParam, String operation) {

    // no session on this thread: internal daemon code, not a user request (WS, UI and GSH
    // always start a session for the user before doing anything)
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    if (grouperSession == null) {
      return;
    }
    final Subject subject = grouperSession.getSubject();
    if (subject == null) {
      return;
    }

    // root and wheel may do anything, including autocreate of queues and topics
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return;
    }

    // blank name means the default messaging system, resolve it the same way the engine does
    // so a caller cannot get around the check by leaving the name out
    String messagingSystemName = GrouperMessagingEngine.retrieveGrouperMessageSystemName(grouperMessageSystemParam);

    String subjectString = GrouperUtil.subjectToString(subject);

    // creating queues, topics or exchanges on the broker is an admin action, never for non-wheel callers
    if (grouperMessageSystemParam != null && grouperMessageSystemParam.isAutocreateObjects()) {
      throw new InsufficientPrivilegeException("Subject " + subjectString
          + " cannot autocreate queues or topics on messaging system '" + messagingSystemName
          + "' (" + operation + "), only wheel or root can");
    }

    // fail closed: no group configured means only wheel or root may use this system
    final String configKey = allowedGroupNameConfigKey(messagingSystemName);
    final String allowedGroupName = GrouperConfig.retrieveConfig().propertyValueString(configKey);
    if (GrouperUtil.isBlank(allowedGroupName)) {
      throw new InsufficientPrivilegeException("Subject " + subjectString
          + " is not allowed to " + operation + " on messaging system '" + messagingSystemName
          + "': only wheel or root, or members of the group in grouper.properties " + configKey
          + " (not configured)");
    }

    // look up the group and the membership as root, since the caller may not be able to see the group
    boolean isMember = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) {
        Group allowedGroup = GroupFinder.findByName(rootSession, allowedGroupName.trim(), false);
        if (allowedGroup == null) {
          // misconfigured: refuse rather than open the system to everyone
          return false;
        }
        return allowedGroup.hasMember(subject);
      }
    });

    if (!isMember) {
      throw new InsufficientPrivilegeException("Subject " + subjectString
          + " is not allowed to " + operation + " on messaging system '" + messagingSystemName
          + "': not a member of the group in grouper.properties " + configKey);
    }
  }

}
