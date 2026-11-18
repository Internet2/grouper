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
package edu.internet2.middleware.grouper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Optional authorization preflight for a recursive folder copy (StemCopy.save()).</p>
 *
 * <p>Why: Stem.internal_copy() only checks STEM_ADMIN on the top source folder, then copies
 * every descendant group with Group.internal_copy(checkSecurity=false) as root, so the
 * memberships, privileges and attributes of groups the caller cannot READ are copied too.
 * The copy itself does not grant the caller anything, but group-create rules on the
 * destination (privilege inheritance) can then give the caller READ or ADMIN on the copies,
 * which discloses data the caller could not read at the source.  That is documented and
 * tested behavior (GRP-3794), so this check is opt-in.</p>
 *
 * <p>When grouper.properties security.stem.copyRequireDescendantPrivileges is true and the
 * caller is not wheel or root, the whole source tree is checked before anything is created,
 * for each copy option that is on:</p>
 * <ul>
 *   <li>copy list members of groups: READ (or ADMIN) on every descendant group</li>
 *   <li>copy privileges of groups, or copy attributes: ADMIN on every descendant group</li>
 *   <li>copy privileges of folders: STEM_ADMIN on every descendant folder</li>
 *   <li>always: READ on every descendant composite group that has a factor outside the
 *   copied tree, since the copy is recreated with the same outside factors and so
 *   reproduces the composite's membership even with membership copying off</li>
 * </ul>
 * <p>Any failure throws InsufficientPrivilegeException, so the copy is all or nothing.  The
 * exception message gives only counts (the caller may not be able to VIEW the groups);
 * the names go to the WARN log for the administrator.  A structure-only copy (all data
 * options off, no outside composites) needs nothing beyond what Stem.internal_copy checks.</p>
 *
 * <p>Uses only APIs present in every 5.x, 6.x and 7.x release, so one version of this class
 * serves all of them.</p>
 */
public class StemCopyDescendantPreflight {

  /** grouper.properties key that turns the preflight on, default false */
  public static final String CONFIG_KEY = "security.stem.copyRequireDescendantPrivileges";

  /** at most this many names per failure kind in the log */
  private static final int MAX_NAMES = 5;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(StemCopyDescendantPreflight.class);

  /**
   * no instances
   */
  private StemCopyDescendantPreflight() {
  }

  /**
   * Check that the caller of a folder copy may read the data the copy options will duplicate.
   * Does nothing unless security.stem.copyRequireDescendantPrivileges is true.
   * @param stemToCopy source folder
   * @param privilegesOfStem copy folder privileges
   * @param privilegesOfGroup copy group privileges
   * @param listMembersOfGroup copy group memberships
   * @param attributes copy attributes
   * @throws InsufficientPrivilegeException if the caller lacks a needed privilege
   */
  public static void check(Stem stemToCopy, boolean privilegesOfStem, boolean privilegesOfGroup,
      boolean listMembersOfGroup, boolean attributes) throws InsufficientPrivilegeException {

    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(CONFIG_KEY, false)) {
      return;
    }

    // Stem.internal_copy rejects these itself with its own messages
    if (stemToCopy == null || stemToCopy.isRootStem()) {
      return;
    }

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Subject subject = grouperSession.getSubject();

    // wheel and root may copy anything, same as the rest of the copy code
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return;
    }

    // -------------------------------------------------------------------------------------
    // enumerate the tree the same way Stem.internal_copy does (raw DAO, no security), so the
    // check covers exactly the objects the copy will touch
    // -------------------------------------------------------------------------------------
    Set<Group> groups = GrouperDAOFactory.getFactory().getStem()
        .findAllChildGroups(stemToCopy, Stem.Scope.SUB);
    Set<String> groupIdsInTree = new HashSet<String>();
    for (Group group : groups) {
      groupIdsInTree.add(group.getUuid());
    }

    List<String> lackRead = new ArrayList<String>();
    List<String> lackAdmin = new ArrayList<String>();
    List<String> lackCompositeRead = new ArrayList<String>();
    List<String> lackStemAdmin = new ArrayList<String>();

    // -------------------------------------------------------------------------------------
    // groups: privilege checks run in the caller's session for the caller's subject
    // -------------------------------------------------------------------------------------
    for (Group group : groups) {

      boolean canAdmin = PrivilegeHelper.canAdmin(grouperSession, group, subject);

      if ((privilegesOfGroup || attributes) && !canAdmin) {
        lackAdmin.add(group.getName());
      }

      // READ is implied by ADMIN, skip the second query when possible
      boolean canRead = canAdmin || PrivilegeHelper.canRead(grouperSession, group, subject);

      if (listMembersOfGroup && !canRead) {
        lackRead.add(group.getName());
      }

      // a composite whose factors are not all in the tree keeps those outside factors in the
      // copy, so its membership is reproduced even when memberships are not copied
      if (!canRead) {
        Composite composite = GrouperDAOFactory.getFactory().getComposite().findAsOwner(group, false);
        if (composite != null && (!groupIdsInTree.contains(composite.getLeftFactorUuid())
            || !groupIdsInTree.contains(composite.getRightFactorUuid()))) {
          // already reported under lackRead when memberships are copied
          if (!listMembersOfGroup) {
            lackCompositeRead.add(group.getName());
          }
        }
      }
    }

    // -------------------------------------------------------------------------------------
    // folders: the top folder is checked by Stem.internal_copy, the descendants here
    // -------------------------------------------------------------------------------------
    if (privilegesOfStem) {
      // checkSecurity=false: the default lookup only returns folders the caller can VIEW, which
      // would skip exactly the descendant folders the caller has no privilege on
      Set<Stem> stems = GrouperDAOFactory.getFactory().getStem()
          .findAllChildStems(stemToCopy, Stem.Scope.SUB, null, false);
      for (Stem stem : stems) {
        if (!PrivilegeHelper.canStemAdmin(grouperSession, stem, subject)) {
          lackStemAdmin.add(stem.getName());
        }
      }
    }

    if (lackRead.isEmpty() && lackAdmin.isEmpty() && lackCompositeRead.isEmpty() && lackStemAdmin.isEmpty()) {
      return;
    }

    // -------------------------------------------------------------------------------------
    // deny: counts in the exception (shown to the caller), names only in the log
    // -------------------------------------------------------------------------------------
    StringBuilder message = new StringBuilder("Cannot copy stem " + stemToCopy.getName()
        + ": " + CONFIG_KEY + " is true and the caller lacks");
    StringBuilder logNames = new StringBuilder();
    appendFailure(message, logNames, lackRead, "READ on", "descendant groups (membership copy)");
    appendFailure(message, logNames, lackAdmin, "ADMIN on", "descendant groups (privilege or attribute copy)");
    appendFailure(message, logNames, lackCompositeRead, "READ on", "descendant composite groups with factors outside the folder");
    appendFailure(message, logNames, lackStemAdmin, "STEM_ADMIN on", "descendant folders (folder privilege copy)");

    LOG.warn(message + ", subject: " + GrouperUtil.subjectToString(subject) + logNames);

    throw new InsufficientPrivilegeException(message.toString());
  }

  /**
   * add one failure kind to the message and its first names to the log text
   * @param message exception message
   * @param logNames log suffix
   * @param names objects that failed
   * @param privilegeLabel e.g. "READ on"
   * @param objectLabel e.g. "descendant groups"
   */
  private static void appendFailure(StringBuilder message, StringBuilder logNames, List<String> names,
      String privilegeLabel, String objectLabel) {
    if (names.isEmpty()) {
      return;
    }
    message.append(" ").append(privilegeLabel).append(" ").append(names.size()).append(" ").append(objectLabel).append(";");
    logNames.append(", ").append(objectLabel).append(" e.g.: ")
        .append(names.subList(0, Math.min(MAX_NAMES, names.size())));
  }
}
