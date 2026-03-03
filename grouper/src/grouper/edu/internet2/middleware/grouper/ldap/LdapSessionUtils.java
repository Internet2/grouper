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
package edu.internet2.middleware.grouper.ldap;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Use this class to establish ldap session</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
 * </pre>
 * </blockquote>
 * 
 * </p>
 */
public class LdapSessionUtils {

  public static void main(String[] args) {
    GrouperStartup.startup();
    List<String> uids = LdapSessionUtils.ldapSession().list(String.class, "personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aa*)", "uid");
    System.out.println(GrouperUtil.toStringForLog(uids));
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(LdapSessionUtils.class);

  private static boolean loggedErrorNotLdaptive = false;
  
  /**
   *
   * @return the external subject storable
   */
  public static LdapSession ldapSession() {
    String className = GrouperConfig.retrieveConfig().propertyValueString("ldap.implementation.className");
    
    if (!StringUtils.equals(className, LdaptiveSessionImpl.class.getName()) && !loggedErrorNotLdaptive) {
      LOG.error("ldap.implementation.className cannot be anything but " + LdaptiveSessionImpl.class.getName());
      loggedErrorNotLdaptive = true;
    }
    LdaptiveSessionImpl ldaptiveSessionImpl = new LdaptiveSessionImpl();
    LdapProvisioningCommandsLog commandsLog = logCurrentCommandsLog();
    if (commandsLog != null) {
      ldaptiveSessionImpl.assignDebug(true, commandsLog.getLog());
    }
    return ldaptiveSessionImpl;
  }

  private static ThreadLocal<LdapProvisioningCommandsLog> threadLocalLog = new InheritableThreadLocal<LdapProvisioningCommandsLog>();

  /**
   * start a static debug log
   * log start
   * @return false if already started, or true if just started
   */
  public static boolean logStart() {
    LdapProvisioningCommandsLog existing = threadLocalLog.get();
    if (existing != null) {
      if (existing.isDisabled()) {
        threadLocalLog.remove();
      } else {
        return false;
      }
    }
    threadLocalLog.set(new LdapProvisioningCommandsLog());
    return true;

  }

  /**
   * get the current commands log
   */
  public static LdapProvisioningCommandsLog logCurrentCommandsLog() {
    LdapProvisioningCommandsLog commandsLog = threadLocalLog.get();
    if (commandsLog != null && commandsLog.isDisabled()) {
      threadLocalLog.remove();
      return null;
    }
    return commandsLog;
  }

  /**
   * get the current log
   * log start
   */
  public static StringBuilder logCurrent() {
    LdapProvisioningCommandsLog commandsLog = logCurrentCommandsLog();
    return commandsLog == null ? null : commandsLog.getLog();
  }

  /**
   * stop a debug log in a finally block
   * @return the log message
   */
  public static String logEnd() {
    LdapProvisioningCommandsLog commandsLog = threadLocalLog.get();
    if (commandsLog != null) {
      commandsLog.setDisabled(true);
    }
    threadLocalLog.remove();
    StringBuilder log = commandsLog == null ? null : commandsLog.getLog();
    return log == null ? null : log.toString();
  }
  
}
