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
package edu.internet2.middleware.grouper.ws;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

/**
 * Rules for the web service X-Grouper-actAs headers (ws.grouper.act.as.group), which replace
 * the authenticated caller with another subject before the rest of the web service runs.
 *
 * <p>Two rules are enforced when the headers actually change who the request runs as:</p>
 * <ul>
 * <li>Admission: the authenticated caller (not only the header act-as subject) must be in
 * ws.client.user.group.name if that is configured.  The existing check in the body act-as code
 * still runs against the header act-as subject, so both must be in the group.</li>
 * <li>No chaining: a request that used the headers cannot also send a request body
 * actAsSubjectLookup for a different subject.  Otherwise the authenticated caller would inherit
 * the header subject's own ws.act.as.group rights and reach subjects the header rule denies.</li>
 * </ul>
 *
 * <p>The web service filter (GrouperServiceJ2ee) works out who is logged in, assigns the
 * authenticated caller here when the headers replaced it, and clears it at the end of the
 * request.  The body act-as code asks {@link #isChainedActAs(Subject, Subject)}.  This class only
 * uses APIs that exist in every Grouper 5.x, 6.x and 7.x release so the same file can ship in
 * every patch, and it does not use commons-lang (5.x uses lang, 6.x and later lang3).</p>
 */
public class GrouperWsHeaderActAsGuard {

  /**
   * the authenticated caller when X-Grouper-actAs headers replaced it for this request, else
   * nothing.  set and cleared by the web service filter
   */
  private static ThreadLocal<Subject> authenticatedSubjectThreadLocal = new ThreadLocal<Subject>();

  /** cache of whether an authenticated caller is in ws.client.user.group.name */
  private static GrouperCache<MultiKey, Boolean> clientUserAdmittedCache = null;

  /**
   * remember the authenticated caller, since X-Grouper-actAs headers replaced it for this request
   * @param authenticatedSubject the subject the container or authentication class logged in
   */
  public static void assignAuthenticatedSubject(Subject authenticatedSubject) {
    if (authenticatedSubject == null) {
      authenticatedSubjectThreadLocal.remove();
    } else {
      authenticatedSubjectThreadLocal.set(authenticatedSubject);
    }
  }

  /**
   * @return the authenticated caller if X-Grouper-actAs headers replaced it for this request,
   * else null
   */
  public static Subject retrieveAuthenticatedSubject() {
    return authenticatedSubjectThreadLocal.get();
  }

  /**
   * forget the authenticated caller (no header act-as for this request, or the request ended)
   */
  public static void clear() {
    authenticatedSubjectThreadLocal.remove();
  }

  /**
   * @param subject1 subject
   * @param subject2 subject
   * @return true if both are the same subject (id and source)
   */
  public static boolean sameSubject(Subject subject1, Subject subject2) {
    if (subject1 == null || subject2 == null) {
      return subject1 == subject2;
    }
    return GrouperUtil.equals(subject1.getId(), subject2.getId())
        && GrouperUtil.equals(subject1.getSourceId(), subject2.getSourceId());
  }

  /**
   * see if a request body actAsSubjectLookup would chain off the X-Grouper-actAs headers
   * @param loggedInSubject who the request runs as so far (the header act-as subject if the
   * headers were used)
   * @param actAsSubject the subject the request body wants to act as
   * @return true if the headers replaced the authenticated caller for this request and the body
   * act-as is a different subject, i.e. the request must be refused
   */
  public static boolean isChainedActAs(Subject loggedInSubject, Subject actAsSubject) {
    Subject authenticatedSubject = retrieveAuthenticatedSubject();
    if (authenticatedSubject == null || actAsSubject == null) {
      return false;
    }
    // acting as the header subject itself in the body is a no-op, allow it
    return !sameSubject(loggedInSubject, actAsSubject);
  }

  /**
   * @param loggedInSubject header act-as subject
   * @param actAsSubject body act-as subject
   * @return the error message for a chained act-as
   */
  public static String chainedActAsMessage(Subject loggedInSubject, Subject actAsSubject) {
    return "A request that uses the X-Grouper-actAs headers (ws.grouper.act.as.group) cannot also "
        + "send an actAsSubjectLookup for a different subject.  Authenticated: '"
        + GrouperUtil.subjectToString(retrieveAuthenticatedSubject()) + "', header actAs: '"
        + GrouperUtil.subjectToString(loggedInSubject) + "', body actAs: '"
        + GrouperUtil.subjectToString(actAsSubject) + "'";
  }

  /**
   * get the cache, and init if not initted.  same lifetime setting as the existing check of the
   * logged in subject (ws.client.user.group.cache.minutes)
   * @return the cache
   */
  private static GrouperCache<MultiKey, Boolean> clientUserAdmittedCache() {
    if (clientUserAdmittedCache == null) {
      int cacheMinutes = GrouperWsConfigInApi.retrieveConfig().propertyValueInt(
          "ws.client.user.group.cache.minutes", 2);
      synchronized(GrouperWsHeaderActAsGuard.class) {
        if (clientUserAdmittedCache == null) {
          clientUserAdmittedCache = new GrouperCache<MultiKey, Boolean>(
              GrouperWsHeaderActAsGuard.class.getName() + "clientUserAdmittedCache",
              10000, false, 60*60*24, cacheMinutes*60, false);
        }
      }
    }
    return clientUserAdmittedCache;
  }

  /**
   * see if the authenticated caller may use the web service at all (ws.client.user.group.name).
   * called before the X-Grouper-actAs header subject is used for anything
   * @param authenticatedSubject the subject the container or authentication class logged in
   * @return true if no group is configured or the subject is a member.  false if the group is
   * configured and the subject is not a member, or the group does not exist (fail closed: the
   * existing check would create it empty, so nobody would be admitted anyway)
   */
  public static boolean isClientUserAdmitted(final Subject authenticatedSubject) {

    final String userGroupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString(
        "ws.client.user.group.name");

    if (userGroupName == null || userGroupName.trim().length() == 0) {
      return true;
    }

    if (authenticatedSubject == null) {
      return false;
    }

    MultiKey cacheKey = new MultiKey(userGroupName.trim(), authenticatedSubject.getId(),
        authenticatedSubject.getSourceId());

    Boolean admitted = clientUserAdmittedCache().get(cacheKey);
    if (admitted != null) {
      return admitted;
    }

    // look it up as root, same as the existing check of the logged in subject
    GrouperSession rootSession = GrouperSession.startRootSession(false);
    try {
      admitted = (Boolean)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = GroupFinder.findByName(grouperSession, userGroupName.trim(), false);
          return group != null && group.hasMember(authenticatedSubject);
        }
      });
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    clientUserAdmittedCache().put(cacheKey, admitted);
    return admitted;
  }

}
