/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.authentication;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>The subjects an OAuth read-write consent is limited to (the grouper_readwrite_subjects
 * claim), as canonical keys: <code>sourceId||subjectId</code>.</p>
 *
 * <p>A subject id is only unique within its source, and identifiers (email, netId, eppn) can be
 * shared across sources, for example a person source and the self-registering external users
 * source.  So the scope names a subject by source and id, decided once when the user consents,
 * and an MCP tool compares the key of the subject it is really about to act on, resolved the same
 * way the operation resolves it.  A string the caller typed is never compared on its own.</p>
 *
 * <p>An entry with no <code>||</code> (a bare subject id or identifier, as tokens issued before
 * this class existed carry) matches nothing, so an old restricted token fails closed until the
 * user consents again, instead of matching that id in every source.</p>
 *
 * <p>Used by the OAuth consent screen (UiV2OAuth) to build the keys and by GrouperMcpAuthUser to
 * check them.  Lives in core so the UI and WS webapps share one copy.</p>
 */
public class GrouperOAuthSubjectScope {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperOAuthSubjectScope.class);

  /** separates the source id from the subject id in a key, the same as the subject picker */
  public static final String SEPARATOR = "||";

  /**
   * no instances
   */
  private GrouperOAuthSubjectScope() {
  }

  /**
   * the scope key of a subject
   * @param sourceId source id of the subject
   * @param subjectId subject id
   * @return sourceId||subjectId, or null if either part is blank
   */
  public static String subjectKey(String sourceId, String subjectId) {
    if (StringUtils.isBlank(sourceId) || StringUtils.isBlank(subjectId)) {
      return null;
    }
    return sourceId + SEPARATOR + subjectId;
  }

  /**
   * the scope key of a subject
   * @param subject the subject
   * @return sourceId||subjectId, or null if the subject is null
   */
  public static String subjectKey(Subject subject) {
    if (subject == null) {
      return null;
    }
    return subjectKey(subject.getSourceId(), subject.getId());
  }

  /**
   * whether a value is a canonical key (has a source half and a subject half)
   * @param value a scope entry
   * @return true if it is sourceId||subjectId
   */
  public static boolean isSubjectKey(String value) {
    if (StringUtils.isBlank(value)) {
      return false;
    }
    // the first separator, not the last: a source id has no || in it, but a subject id might
    int index = value.indexOf(SEPARATOR);
    return index > 0 && index + SEPARATOR.length() < value.length();
  }

  /**
   * turn what the consent screen carried for one subject into a key.  A picked value from the
   * shared subject picker is sourceId||subjectId and is confirmed by looking the subject up in
   * that source.  A value with no source (typed in) is looked up in all sources by id or
   * identifier, and must match exactly one subject.
   * @param value what the form carried
   * @return the key, or null if it does not resolve to exactly one subject (the caller refuses
   * the consent, rather than dropping the entry, so the scope is never wider than what was shown)
   */
  public static String canonicalizeConsentValue(String value) {

    value = StringUtils.trimToNull(value);
    if (value == null) {
      return null;
    }

    try {
      Subject subject = null;
      if (isSubjectKey(value)) {
        String sourceId = StringUtils.substringBefore(value, SEPARATOR);
        String subjectId = StringUtils.substringAfter(value, SEPARATOR);
        subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
      } else {
        // no source: only accepted if it is unambiguous.  SubjectNotUniqueException is caught below
        subject = SubjectFinder.findByIdOrIdentifier(value, false);
      }
      return subjectKey(subject);
    } catch (Exception e) {
      // not unique, source unavailable, etc: refuse rather than guess which subject was meant
      LOG.debug("Could not resolve consent subject: " + value, e);
      return null;
    }
  }

  /**
   * whether a subject is one of the scope keys
   * @param scopeKeys the grouper_readwrite_subjects claim
   * @param sourceId source id of the subject being acted on
   * @param subjectId subject id of the subject being acted on
   * @return true only if sourceId||subjectId is in the list.  entries which are not keys never match
   */
  public static boolean isSubjectInScope(Collection<String> scopeKeys, String sourceId, String subjectId) {
    String key = subjectKey(sourceId, subjectId);
    if (key == null || scopeKeys == null) {
      return false;
    }
    for (String scopeKey : scopeKeys) {
      if (isSubjectKey(scopeKey) && StringUtils.equals(scopeKey, key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * whether a subject is one of the scope keys
   * @param scopeKeys the grouper_readwrite_subjects claim
   * @param subject the subject being acted on, null means no
   * @return true only if the subject's key is in the list
   */
  public static boolean isSubjectInScope(Collection<String> scopeKeys, Subject subject) {
    if (subject == null) {
      return false;
    }
    return isSubjectInScope(scopeKeys, subject.getSourceId(), subject.getId());
  }
}
