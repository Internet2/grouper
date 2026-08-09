/**
 * GRP-7187: convert an existing normal group into a composite in place, without churning the
 * change log or PIT for members whose effective membership does not change.
 */
package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.misc.CompositeType;

/**
 * Convenience wrapper for converting a normal group that already has immediate members into a
 * composite in place. The actual logic lives in {@link CompositeSave} (assignConvertMembersInPlace):
 * the overlap between the group's current members and the composite result is relabeled from a
 * direct (immediate) membership to a composite membership with no change log entry and no PIT
 * record, so only genuine joins and leaves reach the change log. This class just calls CompositeSave
 * with that behavior enabled, in CompositeSave's own transaction.
 */
public class CompositeInPlaceConverter {

  /**
   * Convert an existing normal group into a composite (owner = left [type] right), relabeling the
   * overlap in place so only genuine joins and leaves reach the change log.
   *
   * The composite membership rows for genuine joins are materialized asynchronously by the
   * compositeMemberships consumer (the daemon in a running system), so callers who need them
   * immediately -- like tests -- must run that consumer.
   *
   * @param owner the normal group to convert into a composite
   * @param type the composite type (union, intersection, or complement)
   * @param left the left factor group
   * @param right the right factor group
   * @return the created composite
   */
  public static Composite convert(Group owner, CompositeType type, Group left, Group right) {
    return new CompositeSave()
        .assignOwnerGroup(owner)
        .assignLeftFactorGroup(left)
        .assignRightFactorGroup(right)
        .assignCompositeType(type)
        .assignConvertMembersInPlace(true)
        .save();
  }

}
