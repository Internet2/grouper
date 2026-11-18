package edu.internet2.middleware.grouper.authentication;

import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.group.TypeOfGroup;

/**
 * <pre>
 * Checks, on every self-service WS JWT request, that the holder of the credential is still an
 * active local entity.  GrouperPublicPrivateKeyJwt caches the credential row and the subject for
 * a minute, and a disabled local entity still resolves as a subject, so without this check a
 * disabled (or not yet enabled) local entity could keep authenticating with its private key.
 *
 * Nothing here is cached: the member and the backing entity group are read from the database
 * with the second level cache off, so disabling the entity (or reaching its disabled date)
 * denies the very next request, without waiting for the enabled/disabled daemon or any cache.
 *
 * This class only uses APIs that exist in every Grouper 5.x, 6.x and 7.x release, so the same
 * file ships in every patch set.
 * </pre>
 */
public class GrouperLocalEntityJwtCheck {

  /** source id of local entities (EntitySourceAdapter) */
  private static final String ENTITY_SOURCE_ID = "grouperEntities";

  /** entity type the UI stores on a local entity WS JWT key */
  private static final String ENTITY_TYPE_LOCAL_ENTITY = "localEntity";

  /**
   * see if the credential belongs to an existing, enabled local entity with the member id from
   * the bearer token.  Any failure returns false (fail closed); the caller treats that as a
   * generic authentication failure.
   * @param grouperPassword credential row found for the member id
   * @param memberId member id decoded from the bearer token
   * @param debugMap reason is put here (key localEntityCheck), can be null
   * @return true if the credential holder is an active local entity
   */
  public static boolean isCredentialHolderActive(final GrouperPassword grouperPassword,
      final String memberId, final Map<String, Object> debugMap) {

    String reason = null;
    try {
      reason = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return inactiveReason(grouperSession, grouperPassword, memberId, System.currentTimeMillis());
        }
      });
    } catch (RuntimeException re) {
      // fail closed: if the state cannot be read, do not authenticate
      reason = "error: " + re.getClass().getSimpleName();
    }

    if (debugMap != null) {
      debugMap.put("localEntityCheck", reason == null ? "active" : reason);
    }
    return reason == null;
  }

  /**
   * @param grouperSession root session
   * @param grouperPassword credential
   * @param memberId member id from the token
   * @param nowMillis current time
   * @return null if active, else a short reason (for the debug log only)
   */
  private static String inactiveReason(GrouperSession grouperSession, GrouperPassword grouperPassword,
      String memberId, long nowMillis) {

    // ---- the credential itself must be a WS RS_2048 key for a local entity, bound to this member
    if (grouperPassword == null || memberId == null) {
      return "noCredential";
    }
    // compare the db string: setApplicationDb() (what hibernate calls on load) does not set the
    // enum, so getApplication() is null for every row read from the database
    if (!GrouperPassword.Application.WS.name().equals(grouperPassword.getApplicationDb())) {
      return "applicationNotWs";
    }
    if (grouperPassword.getEncryptionType() != GrouperPassword.EncryptionType.RS_2048) {
      return "encryptionTypeNotRs2048";
    }
    if (!ENTITY_TYPE_LOCAL_ENTITY.equals(grouperPassword.getEntityType())) {
      return "entityTypeNotLocalEntity";
    }
    if (!memberId.equals(grouperPassword.getUsername()) || !memberId.equals(grouperPassword.getMemberId())) {
      return "credentialMemberMismatch";
    }

    // ---- the member must be a local entity (not a person, and not a group from g:gsa)
    Member member = MemberFinder.findByUuid(grouperSession, memberId, false);
    if (member == null) {
      return "memberNotFound";
    }
    if (!ENTITY_SOURCE_ID.equals(member.getSubjectSourceId())) {
      return "memberNotLocalEntity";
    }

    // ---- read the backing group straight from the DAO with the second level cache off
    // (GroupFinder keeps root and flash caches that could hold the pre-disable copy)
    Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(member.getSubjectId(), false,
        new QueryOptions().secondLevelCache(false));
    if (group == null) {
      return "entityNotFound";
    }
    if (group.getTypeOfGroup() != TypeOfGroup.entity) {
      return "groupNotEntity";
    }
    if (!group.isEnabled()) {
      return "entityDisabled";
    }

    // the enabled flag is flipped by a daemon when an enabled or disabled date arrives, so also
    // check the dates themselves: a date that has taken effect denies now
    if (group.getEnabledTimeDb() != null && group.getEnabledTimeDb() > nowMillis) {
      return "entityNotYetEnabled";
    }
    if (group.getDisabledTimeDb() != null && group.getDisabledTimeDb() <= nowMillis) {
      return "entityDisabledByDate";
    }
    return null;
  }

}
