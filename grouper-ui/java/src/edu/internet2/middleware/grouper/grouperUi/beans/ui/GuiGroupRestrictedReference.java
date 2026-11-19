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

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Placeholder for a group that a screen references (for example the email recipient group
 * or authorized group of an attestation configuration) when the logged in user cannot VIEW that
 * group.</p>
 *
 * <p>Why: the attestation screens resolve these related groups in a root session.  Rendering
 * the real GuiGroup link would put the group's UUID, name, folder path and description in the
 * response for a user who is only allowed to READ the reviewed group.  This subclass renders a
 * fixed "restricted group" text instead, so the JSPs that call linkWithIcon,
 * shortLinkWithIcon or shortLinkWithIconAndPath do not need to change.  The wrapped group is
 * null, so nothing else about it can leak through other getters either.</p>
 *
 * <p>The text comes from the externalized text key guiGroupRestrictedReference, falling back to
 * a built-in English value when the key is not present (patched older releases ship no text
 * change).  Only uses GuiGroup's public no-arg constructor and its link getters, which exist in
 * every Grouper 5.x, 6.x and 7.x release.</p>
 */
public class GuiGroupRestrictedReference extends GuiGroup {

  /** serial */
  private static final long serialVersionUID = 1L;

  /** text if the externalized text key is not there */
  private static final String DEFAULT_TEXT = "(restricted group)";

  /**
   * no group: the referenced group is never stored in this object
   */
  public GuiGroupRestrictedReference() {
    super();
  }

  /**
   * @return the escaped placeholder text
   */
  private static String restrictedText() {
    String text = null;
    try {
      text = TextContainer.textOrNull("guiGroupRestrictedReference");
    } catch (RuntimeException re) {
      // no text container (e.g. not in a UI request): use the default
      text = null;
    }
    if (GrouperUtil.isBlank(text)) {
      text = DEFAULT_TEXT;
    }
    return "<span class=\"grouperRestrictedReference\">" + GrouperUtil.xmlEscape(text) + "</span>";
  }

  /**
   * @see GuiGroup#getLinkWithIcon()
   */
  @Override
  public String getLinkWithIcon() {
    return restrictedText();
  }

  /**
   * @see GuiGroup#getShortLinkWithIcon()
   */
  @Override
  public String getShortLinkWithIcon() {
    return restrictedText();
  }

  /**
   * @see GuiGroup#getShortLinkWithIconAndPath()
   */
  @Override
  public String getShortLinkWithIconAndPath() {
    return restrictedText();
  }
}
