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

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Folder visibility for the query and lookup paths that resolve folders with the unsecured
 * finders and hand them to the caller: GrouperQuery.getStems (the stem query filters behind WS
 * findStems and GSH getStems) and the WS folder lookups (WsStemLookup, the folder of a WS query
 * filter).</p>
 *
 * <p>When security.folders.are.viewable.by.all is false (the default) a folder is only visible to
 * a subject with a naming privilege on it or something visible inside it (StemViewAuthorization).
 * This class adds the cheap cases on top: the root and wheel subjects see every folder, and when
 * the config says folders are viewable by all there is nothing to filter.</p>
 *
 * <p>No session means no caller to check, so nothing is visible (fail closed).</p>
 *
 * <p>Only uses APIs present since 2.6 so the same class can be shipped to every release.</p>
 */
public class StemViewQueryAuthorization {

  /**
   * no instances
   */
  private StemViewQueryAuthorization() {
  }

  /**
   * see if the caller of a session may see a folder
   * @param grouperSession session of the caller, null means the static session
   * @param stem the folder, null means no
   * @return true if visible
   */
  public static boolean canView(GrouperSession grouperSession, Stem stem) {
    if (stem == null) {
      return false;
    }
    Subject subject = callerSubject(grouperSession);
    if (subject == null) {
      return false;
    }
    if (allFoldersVisible(subject)) {
      return true;
    }
    return StemViewAuthorization.canView(subject, stem);
  }

  /**
   * keep only the folders the caller of a session may see, in the order given
   * @param grouperSession session of the caller, null means the static session
   * @param stems folders, may be null
   * @return the visible folders, never null
   */
  public static Set<Stem> filterViewable(GrouperSession grouperSession, Set<Stem> stems) {
    if (stems == null || stems.size() == 0) {
      return new LinkedHashSet<Stem>();
    }
    Subject subject = callerSubject(grouperSession);
    if (subject == null) {
      return new LinkedHashSet<Stem>();
    }
    if (allFoldersVisible(subject)) {
      return stems;
    }
    return StemViewAuthorization.filterViewable(subject, stems);
  }

  /**
   * @param grouperSession session or null
   * @return the subject of the session or of the static session, or null
   */
  private static Subject callerSubject(GrouperSession grouperSession) {
    if (grouperSession == null) {
      grouperSession = GrouperSession.staticGrouperSession(false);
    }
    return grouperSession == null ? null : grouperSession.getSubject();
  }

  /**
   * @param subject the caller
   * @return true if the caller sees every folder without a check
   */
  private static boolean allFoldersVisible(Subject subject) {
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    return GrouperConfig.retrieveConfig().propertyValueBoolean("security.folders.are.viewable.by.all", false);
  }
}
