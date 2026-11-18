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
package edu.internet2.middleware.grouper.misc;

import java.util.regex.Pattern;

/**
 * Validates an id (uuid) that a CALLER chose for a new group, folder, attribute definition or
 * attribute definition name (WS save operations, GroupSave, StemSave, AttributeDefSave,
 * AttributeDefNameSave, and the Stem API methods that take a uuid).
 * <p>
 * Grouper generates 32 hex character ids, but the save operations let a caller pick the id of a
 * new object.  Ids are used as trusted identifiers all over Grouper (HQL, URLs, HTML attributes,
 * JavaScript in the UI), so a caller-chosen id is limited to letters, digits, colon, underscore,
 * dot and dash, at most 40 characters (the width of the id columns).  That allows Grouper's own
 * ids, dashed (canonical) UUIDs, and the usual ids migrated from other systems, and refuses
 * markup, quotes, spaces, control characters and URL delimiters.
 * </p>
 * <p>
 * Only NEW objects are checked: existing objects keep their ids, and can still be found, updated
 * and deleted by them.  Internal paths that copy an existing id (XML import, copy, loader) do not
 * call this.
 * </p>
 */
public class GrouperObjectIdValidator {

  /** most characters in an id (the id columns are VARCHAR(40)) */
  public static final int MAX_LENGTH = 40;

  /** allowed characters: letters, digits, colon, underscore, dot, dash */
  private static final Pattern ALLOWED_ID = Pattern.compile("^[A-Za-z0-9:_.\\-]+$");

  /**
   * no instances
   */
  private GrouperObjectIdValidator() {
  }

  /**
   * see if a caller-chosen id is allowed for a new object.  Blank means "generate one" and is
   * allowed
   * @param id the id the caller chose, or blank
   * @return true if allowed
   */
  public static boolean isValidNewId(String id) {
    if (id == null || id.trim().length() == 0) {
      return true;
    }
    if (id.length() > MAX_LENGTH) {
      return false;
    }
    return ALLOWED_ID.matcher(id).matches();
  }

  /**
   * error message for an id that is not allowed.  Does not echo the id, since the reason it is
   * refused is that it might be markup or script, and this message can end up in a page or log
   * @param objectType e.g. group, folder, attributeDef, attributeDefName
   * @param id the refused id
   * @return the message
   */
  public static String invalidIdMessage(String objectType, String id) {
    return "The uuid for a new " + objectType + " may only contain letters, digits, colon, "
        + "underscore, dot and dash, at most " + MAX_LENGTH + " characters (the uuid passed in has "
        + (id == null ? 0 : id.length()) + " characters).  Leave it blank to have one generated.";
  }

  /**
   * throw if a caller-chosen id for a new object is not allowed
   * @param objectType e.g. group, folder, attributeDef, attributeDefName
   * @param id the id the caller chose, or blank
   * @throws IllegalArgumentException if not allowed
   */
  public static void assertValidNewId(String objectType, String id) {
    if (!isValidNewId(id)) {
      throw new IllegalArgumentException(invalidIdMessage(objectType, id));
    }
  }

}
