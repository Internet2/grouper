package edu.internet2.middleware.grouper.app.scim2Provisioning;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Decides whether an existing SCIM user found after a 409 "uniqueness" conflict (on create or on
 * rename) may be treated as the same identity as the Grouper entity being provisioned.
 * <p>
 * Security: a conflicting account is only accepted when it carries the exact externalId Grouper
 * expects.  A match on a mutable attribute such as userName (or email) is never enough, because an
 * ordinary user of the target could pre-claim a userName that a Grouper subject is about to get,
 * and Grouper would then link that subject (and its memberships and roles) to the attacker's
 * account, and could delete or disable the subject's real account.  When the anchor cannot be
 * proven the caller throws its normal "no existing user matched" error, so the entity becomes an
 * object error that an administrator reconciles by hand.
 * </p>
 * <p>
 * Kept in its own class (only Jackson and the JDK) so the same file works in every release that
 * has the conflict recovery (6.3.0+ and 7.3.0+).
 * </p>
 */
public class GrouperScim2ConflictAnchor {

  /**
   * SCIM attribute used as the immutable identity anchor for conflict recovery
   */
  public static final String ANCHOR_FIELD = "externalId";

  /**
   * no instances
   */
  private GrouperScim2ConflictAnchor() {
  }

  /**
   * Whether the conflict recovery may look up the existing account by userName.  Always false: if an
   * externalId was expected and nothing has it, a userName hit is not the same identity; if no
   * externalId is configured there is no anchor to prove identity with at all.  Kept as a method so
   * a future, explicitly documented opt-in has one place to go.
   * @param configId external system config id (unused for now)
   * @param expectedExternalId the externalId Grouper is provisioning, may be blank
   * @return false
   */
  public static boolean allowUserNameFallback(String configId, String expectedExternalId) {
    return false;
  }

  /**
   * Whether the resource found by the conflict lookup really carries the expected externalId.  SCIM
   * "eq" filters can be case-insensitive or loosely implemented by a target, and a target can return
   * extra resources, so the value is compared exactly here instead of trusting the filter.
   * @param node SCIM user resource returned by the target, may be null
   * @param expectedExternalId the externalId Grouper is provisioning
   * @return true only if both are present and equal (case sensitive)
   */
  public static boolean nodeMatchesAnchor(JsonNode node, String expectedExternalId) {
    if (node == null || expectedExternalId == null || expectedExternalId.trim().length() == 0) {
      return false;
    }
    JsonNode externalIdNode = node.get(ANCHOR_FIELD);
    if (externalIdNode == null || externalIdNode.isNull() || !externalIdNode.isValueNode()) {
      return false;
    }
    return expectedExternalId.equals(externalIdNode.asText());
  }

  /**
   * Whether the resource found by the conflict lookup is the very account the rename was sent to.
   * That happens when the externalId belongs to the linked account itself and the 409 came from a
   * DIFFERENT account holding the new userName.  It is not a re-link: re-linking an account onto
   * itself would make drainEntityRelinks delete or disable the subject's real account.
   * @param node SCIM user resource returned by the target, may be null
   * @param oldTargetId id of the account the PATCH was sent to
   * @return true if the node's id equals oldTargetId
   */
  public static boolean isSameAccount(JsonNode node, String oldTargetId) {
    if (node == null || oldTargetId == null) {
      return false;
    }
    JsonNode idNode = node.get("id");
    return idNode != null && idNode.isValueNode() && oldTargetId.equals(idNode.asText());
  }

  /**
   * Return the node if it carries the expected anchor, else null (treated as "not found").
   * @param node SCIM user resource returned by the target, may be null
   * @param expectedExternalId the externalId Grouper is provisioning
   * @return node or null
   */
  public static JsonNode anchoredOrNull(JsonNode node, String expectedExternalId) {
    return nodeMatchesAnchor(node, expectedExternalId) ? node : null;
  }

}
