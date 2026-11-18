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
package edu.internet2.middleware.grouper.ui;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.authentication.GrouperOidcConfig;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * <pre>
 * Resolve the user id that a UI OIDC login produced (the value of the connector's subject id
 * claim) to a subject, only the way that OIDC connector is configured:
 *
 *  - in the connector's subject sources (grouper.oidcExternalSystem.(id).subjectSourceIds as the
 *    config screen saves it, or the legacy subjectSourceId, both read by GrouperOidcConfig), else
 *    in grouper.ui.authentication.sourceIds, else in all sources
 *  - by the connector's subjectIdType (subjectId, subjectIdentifier or subjectIdOrIdentifier)
 *  - never in the internal source (g:isa, e.g. GrouperSystem): a value from an identity provider
 *    must not log in as the root or another built in subject
 *
 * In strict mode (the user id came from this connector's OIDC login) nothing falls back to a
 * lookup across all sources: if the subject is not found, the login fails.  Lenient mode is for a
 * container (or header, or session) identity on a release where the UI also looked such an
 * identity up with the connector's settings: it returns null when not found so the caller's
 * existing lookup runs as before.
 *
 * The session marker methods are for releases where the OIDC connector is UI wide (no route
 * specific connectors): there the filter remembers in the session which connector produced the
 * user id, so the lookup knows the id came from OIDC.
 *
 * Same file for every release (it only uses APIs from 2.6 on), so it is identical in every patch set.
 * </pre>
 */
public class GrouperUiOidcSubjectResolver {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiOidcSubjectResolver.class);

  /** session attribute: the OIDC connector config id of the UI OIDC login of this session */
  public static final String SESSION_OIDC_CONFIG_ID = "grouperUiOidcLoginConfigId";

  /** session attribute: the user id that the UI OIDC login of this session produced */
  public static final String SESSION_OIDC_USER_ID = "grouperUiOidcLoginUserId";

  /**
   * remember in the session that this user id came from this OIDC connector's login
   * @param httpSession
   * @param oidcConfigId
   * @param userId
   */
  public static void assignOidcLoginToSession(HttpSession httpSession, String oidcConfigId, String userId) {
    if (httpSession == null) {
      return;
    }
    httpSession.setAttribute(SESSION_OIDC_CONFIG_ID, oidcConfigId);
    httpSession.setAttribute(SESSION_OIDC_USER_ID, userId);
  }

  /**
   * the OIDC connector config id if this user id came from the UI OIDC login of this session
   * @param httpSession
   * @param userId
   * @return the config id or null if the user id did not come from an OIDC login
   */
  public static String oidcConfigIdForSessionUser(HttpSession httpSession, String userId) {
    if (httpSession == null || GrouperUtil.isBlank(userId)) {
      return null;
    }
    String oidcConfigId = (String)httpSession.getAttribute(SESSION_OIDC_CONFIG_ID);
    String oidcUserId = (String)httpSession.getAttribute(SESSION_OIDC_USER_ID);
    if (GrouperUtil.isBlank(oidcConfigId) || !userId.equals(oidcUserId)) {
      return null;
    }
    return oidcConfigId;
  }

  /**
   * resolve an OIDC user id the way the connector is configured
   * @param oidcConfigId the external system config id of the OIDC connector
   * @param userId the value of the subject id claim (or the user id from the session)
   * @param uiSourceIds grouper.ui.authentication.sourceIds (comma separated), used when the
   * connector has no source
   * @param strict true if the user id came from this connector's OIDC login: never null, throws
   * SubjectNotFoundException if not found or if the subject is in the internal source.  false for
   * a container identity: returns null if not found
   * @return the subject
   */
  public static Subject resolveSubject(String oidcConfigId, String userId, String uiSourceIds, boolean strict) {

    if (GrouperUtil.isBlank(userId)) {
      if (strict) {
        throw new SubjectNotFoundException("OIDC login has no user id, connector: '" + oidcConfigId + "'");
      }
      return null;
    }

    GrouperOidcConfig grouperOidcConfig = GrouperOidcConfig.retrieveFromConfigOrCache(oidcConfigId);
    String subjectIdType = grouperOidcConfig.getSubjectIdType();

    // lenient keeps the old behavior for an unexpected id type: not found, so the caller's lookup runs
    if (!strict && !"subjectId".equals(subjectIdType) && !"subjectIdentifier".equals(subjectIdType)
        && !"subjectIdOrIdentifier".equals(subjectIdType)) {
      return null;
    }

    // connector sources (plural as saved by the config screen, or the legacy singular), else the UI ones
    String sourceIdsString = grouperOidcConfig.getSubjectSourceId();
    if (GrouperUtil.isBlank(sourceIdsString)) {
      sourceIdsString = uiSourceIds;
    }
    String[] sourceIds = GrouperUtil.isBlank(sourceIdsString) ? new String[0] : GrouperUtil.splitTrim(sourceIdsString, ",");

    Subject subject = null;
    if (sourceIds.length == 0) {
      // no source configured anywhere: all sources, but still by the connector's id type
      subject = findBySubjectIdType(subjectIdType, userId, null, oidcConfigId);
    } else {
      for (String sourceId : sourceIds) {
        if (GrouperUtil.isBlank(sourceId)) {
          continue;
        }
        Subject tempSubject = findBySubjectIdType(subjectIdType, userId, sourceId, oidcConfigId);
        if (tempSubject != null) {
          if (subject != null && !subjectEquals(subject, tempSubject)) {
            throw new SubjectNotUniqueException("OIDC login user id matches subjects in more than one source of connector '"
                + oidcConfigId + "': '" + userId + "'");
          }
          subject = tempSubject;
        }
      }
    }

    if (subject == null) {
      if (strict) {
        LOG.warn("OIDC login user id not found in the sources of connector '" + oidcConfigId + "' (subjectIdType: "
            + subjectIdType + ", sources: " + (sourceIds.length == 0 ? "all" : sourceIdsString) + "): '" + userId + "'");
        throw new SubjectNotFoundException("Cannot find subject for OIDC login, connector: '" + oidcConfigId + "', user id: '" + userId + "'");
      }
      return null;
    }

    // a value from an identity provider never logs in as GrouperSystem or another internal subject
    if (strict && InternalSourceAdapter.ID.equals(subject.getSourceId())) {
      LOG.error("Refusing OIDC login that resolves to an internal subject, connector: '" + oidcConfigId
          + "', user id: '" + userId + "', subject: " + subject.getSourceId() + " - " + subject.getId());
      throw new SubjectNotFoundException("OIDC login cannot resolve to an internal subject: '" + userId + "'");
    }

    return subject;
  }

  /**
   * find a subject by the connector's subject id type, in one source or all (sourceId null)
   * @param subjectIdType
   * @param userId
   * @param sourceId
   * @param oidcConfigId for the error message
   * @return the subject or null
   */
  private static Subject findBySubjectIdType(String subjectIdType, String userId, String sourceId, String oidcConfigId) {
    if ("subjectId".equals(subjectIdType)) {
      return sourceId == null ? SubjectFinder.findById(userId, false) : SubjectFinder.findByIdAndSource(userId, sourceId, false);
    }
    if ("subjectIdentifier".equals(subjectIdType)) {
      return sourceId == null ? SubjectFinder.findByIdentifier(userId, false) : SubjectFinder.findByIdentifierAndSource(userId, sourceId, false);
    }
    if ("subjectIdOrIdentifier".equals(subjectIdType)) {
      return sourceId == null ? SubjectFinder.findByIdOrIdentifier(userId, false) : SubjectFinder.findByIdOrIdentifierAndSource(userId, sourceId, false);
    }
    // subjectIdType is required and has three values, anything else is a config error: fail closed
    throw new RuntimeException("Invalid subjectIdType '" + subjectIdType + "' for OIDC connector '" + oidcConfigId
        + "', expecting subjectId, subjectIdentifier or subjectIdOrIdentifier");
  }

  /**
   * same subject (source and id)
   * @param one
   * @param two
   * @return true if the same
   */
  private static boolean subjectEquals(Subject one, Subject two) {
    return one.getSourceId().equals(two.getSourceId()) && one.getId().equals(two.getId());
  }

}
