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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Placeholder for the "other factor" of a composite in the membership trace and privilege
 * trace screens (UiV2Membership), when the logged in user cannot VIEW that factor group.</p>
 *
 * <p>Why: the trace explains a composite step as "intersect with &lt;other factor&gt;".  The
 * other factor is not part of the membership path the user is allowed to see, so READ on the
 * composite owner does not authorize its UUID, name, path or description.  The trace text
 * templates (membershipTraceGroupCompositeOf*) render guiGroupFactor.shortLink, which
 * GuiGroupRestrictedReference (GRP-7579) does not override, so this subclass also renders the
 * "(restricted group)" text for shortLink and link.  The wrapped group is null, so no other
 * getter can leak it either.</p>
 */
public class GuiGroupRestrictedFactor extends GuiGroupRestrictedReference {

  /** serial */
  private static final long serialVersionUID = 1L;

  /**
   * no group: the factor is never stored in this object
   */
  public GuiGroupRestrictedFactor() {
    super();
  }

  /**
   * wrap a composite factor for the trace screens
   * @param factor other factor of the composite, can be null
   * @param subject logged in subject (subject of the current grouper session)
   * @return the gui group if the subject can VIEW the factor, else the placeholder (fails closed)
   */
  public static GuiGroup guiGroupIfViewable(Group factor, Subject subject) {
    if (factor == null || subject == null) {
      return new GuiGroupRestrictedFactor();
    }
    boolean canView = false;
    try {
      canView = factor.canHavePrivilege(subject, AccessPrivilege.VIEW.getName(), false);
    } catch (RuntimeException re) {
      // cannot tell: do not show the factor
      canView = false;
    }
    return canView ? new GuiGroup(factor) : new GuiGroupRestrictedFactor();
  }

  /**
   * @see GuiGroup#getShortLink()
   */
  @Override
  public String getShortLink() {
    return this.getShortLinkWithIcon();
  }

  /**
   * @see GuiGroup#getLink()
   */
  @Override
  public String getLink() {
    return this.getLinkWithIcon();
  }
}
