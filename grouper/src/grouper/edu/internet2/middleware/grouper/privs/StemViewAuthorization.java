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
package edu.internet2.middleware.grouper.privs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Folder visibility rule for read paths that resolve a folder by id or name and then show it
 * (UI folder tree, favorites, recently used, audit lines).</p>
 *
 * <p>The static finders (StemFinder.findByUuid, findByName, the stem DAO) are lookups, not
 * authorization checks: they return any existing folder whatever the session.  A subject may see
 * a folder if it has a naming privilege on it (stemView is implied by stemAdmin, create,
 * stemAttrRead and stemAttrUpdate), or if it can see something inside it.  The second part comes
 * from the secure StemFinder with a subject, which reads the stem view privilege table and honors
 * security.folders.are.viewable.by.all and wheel / view-only root.  This is the same rule as
 * PrivilegeHelper.canViewStemOrObjectInside (GRP-7387), kept in its own class so it can be
 * shipped to releases that do not have that method (it only uses APIs present since 5.0.0).</p>
 *
 * <p>The root folder is always visible: it is the top of every tree and has no secret name.</p>
 */
public class StemViewAuthorization {

  /**
   * no instances
   */
  private StemViewAuthorization() {
  }

  /**
   * see if a subject may see a folder: a naming privilege on it, or something inside it it can see
   * @param subject the subject (e.g. the logged in user), null means no
   * @param stem the folder, null means no
   * @return true if the subject may see the folder
   */
  public static boolean canView(final Subject subject, final Stem stem) {

    if (subject == null || stem == null) {
      return false;
    }

    if (stem.isRootStem()) {
      return true;
    }

    // run as root: the subject is checked explicitly, and the caller's session may not be allowed to
    // read privileges on a folder it cannot see
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        // cheap check first: a privilege on the folder itself
        if (stem.canHavePrivilege(subject, NamingPrivilege.STEM_VIEW.getName(), false)) {
          return true;
        }

        // otherwise something inside it, from the stem view privilege table
        return new StemFinder().addStemId(stem.getId()).assignSubject(subject).findStem() != null;
      }
    });
  }

  /**
   * keep only the folders a subject may see (see canView), in the order given.  One privilege check
   * per folder, then one batched secure StemFinder query for the folders that did not pass it.
   * @param subject the subject, null means none are visible
   * @param stems folders, may be null or contain nulls
   * @return the visible folders in the original order, never null
   */
  public static Set<Stem> filterViewable(final Subject subject, final Collection<Stem> stems) {

    final Set<Stem> result = new LinkedHashSet<Stem>();

    if (subject == null || stems == null || stems.size() == 0) {
      return result;
    }

    // ids that pass, filled as root
    final Set<String> allowedStemIds = new HashSet<String>();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        List<String> notYetAllowedStemIds = new ArrayList<String>();

        // cheap check first: root, or a privilege on the folder itself
        for (Stem stem : stems) {
          if (stem == null) {
            continue;
          }
          if (stem.isRootStem() || stem.canHavePrivilege(subject, NamingPrivilege.STEM_VIEW.getName(), false)) {
            allowedStemIds.add(stem.getId());
          } else {
            notYetAllowedStemIds.add(stem.getId());
          }
        }

        // otherwise something inside it, one query for the rest (the finder batches the ids)
        if (notYetAllowedStemIds.size() > 0) {
          Set<Stem> insideStems = new StemFinder().assignStemIds(notYetAllowedStemIds)
              .assignSubject(subject).findStems();
          if (insideStems != null) {
            for (Stem insideStem : insideStems) {
              allowedStemIds.add(insideStem.getId());
            }
          }
        }
        return null;
      }
    });

    // keep the caller's order
    for (Stem stem : stems) {
      if (stem != null && allowedStemIds.contains(stem.getId())) {
        result.add(stem);
      }
    }
    return result;
  }
}
