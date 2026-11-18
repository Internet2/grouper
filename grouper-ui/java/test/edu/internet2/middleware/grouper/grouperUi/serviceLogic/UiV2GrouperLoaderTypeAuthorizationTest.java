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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Arrays;
import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2GrouperLoaderTypeAuthorization.Rights;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * <pre>
 * Decision table for UiV2GrouperLoaderTypeAuthorization (security advisory M3, merged 43).  Plain
 * JUnit, no database: the class is pure logic, the handler wiring in UiV2GrouperLoader needs a
 * UI request and is covered by the manual reproduction steps in the security report.
 *
 * Before the fix this does not compile (the class does not exist); the behavior it guards is
 * an unknown loader type or "no loader" request reaching the root cleanup in
 * editGrouperLoaderSave, and an ABAC-only editor opening or exporting an SQL/LDAP config.
 * </pre>
 */
public class UiV2GrouperLoaderTypeAuthorizationTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GrouperLoaderTypeAuthorizationTest.class);
  }

  /** wheel-like: every right */
  private static final Rights ALL = new Rights(true, true, true);

  /** group admin allowed only JEXL scripted group loaders */
  private static final Rights ABAC_ONLY = new Rights(false, true, false);

  /** group admin allowed only recent memberships loaders */
  private static final Rights RECENT_ONLY = new Rights(false, false, true);

  /** VIEW-only user: no loader right */
  private static final Rights NONE = new Rights(false, false, false);

  /** group with no loader marker (for example a group managed by a group-list loader) */
  private static final Set<String> NO_MARKER = UiV2GrouperLoaderTypeAuthorization.existingLoaderTypes(false, false, false, false);

  /** SQL loader group */
  private static final Set<String> SQL_GROUP = UiV2GrouperLoaderTypeAuthorization.existingLoaderTypes(true, false, false, false);

  /** LDAP loader group */
  private static final Set<String> LDAP_GROUP = UiV2GrouperLoaderTypeAuthorization.existingLoaderTypes(false, true, false, false);

  /** JEXL scripted group */
  private static final Set<String> JEXL_GROUP = UiV2GrouperLoaderTypeAuthorization.existingLoaderTypes(false, false, true, false);

  /** recent memberships group */
  private static final Set<String> RECENT_GROUP = UiV2GrouperLoaderTypeAuthorization.existingLoaderTypes(false, false, false, true);

  /**
   * @param name
   */
  public UiV2GrouperLoaderTypeAuthorizationTest(String name) {
    super(name);
  }

  /**
   * trigger 1: an unknown, blank, mixed-case or padded type is refused, even with every right,
   * and no matter which loader the group has
   */
  public void testUnknownTypeRefused() {
    String[] badTypes = new String[] {"INVALID", "", " ", null, "sql", "Sql", " SQL", "SQL ", "jexl_script", "RECENT_MEMBERSHIPS\t"};
    Set<?>[] groups = new Set<?>[] {NO_MARKER, SQL_GROUP, LDAP_GROUP, JEXL_GROUP, RECENT_GROUP};
    for (String badType : badTypes) {
      assertFalse("[" + badType + "]", UiV2GrouperLoaderTypeAuthorization.isKnownLoaderType(badType));
      assertFalse("[" + badType + "]", UiV2GrouperLoaderTypeAuthorization.canEditType(badType, ALL));
      for (Set<?> group : groups) {
        @SuppressWarnings("unchecked")
        Set<String> existing = (Set<String>)group;
        assertEquals("[" + badType + "] " + existing, UiV2GrouperLoaderTypeAuthorization.REFUSE_TYPE_INVALID,
            UiV2GrouperLoaderTypeAuthorization.checkSave(existing, true, badType, ALL));
        assertEquals("[" + badType + "] " + existing, UiV2GrouperLoaderTypeAuthorization.REFUSE_TYPE_INVALID,
            UiV2GrouperLoaderTypeAuthorization.checkSave(existing, true, badType, NONE));
      }
    }
    for (String goodType : new String[] {"SQL", "LDAP", "JEXL_SCRIPT", "RECENT_MEMBERSHIPS"}) {
      assertTrue(goodType, UiV2GrouperLoaderTypeAuthorization.isKnownLoaderType(goodType));
    }
  }

  /**
   * a VIEW-only user can do nothing that changes a loader
   */
  public void testViewOnlyUserRefused() {
    for (String type : new String[] {"SQL", "LDAP", "JEXL_SCRIPT", "RECENT_MEMBERSHIPS"}) {
      assertEquals(type, UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
          UiV2GrouperLoaderTypeAuthorization.checkSave(NO_MARKER, true, type, NONE));
    }
    for (Set<String> existing : Arrays.asList(SQL_GROUP, LDAP_GROUP, JEXL_GROUP, RECENT_GROUP)) {
      // turning the loader off removes the marker
      assertEquals(existing.toString(), UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
          UiV2GrouperLoaderTypeAuthorization.checkSave(existing, false, null, NONE));
      assertFalse(existing.toString(), UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(existing, NONE));
    }
    assertFalse(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(NO_MARKER, NONE));
    assertFalse(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(SQL_GROUP, null));
    assertEquals(UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
        UiV2GrouperLoaderTypeAuthorization.checkSave(SQL_GROUP, true, "SQL", null));
  }

  /**
   * trigger 2: "no loader" on a group with no marker changes nothing, so it is allowed, but the
   * root metadata cleanup must not run
   */
  public void testNoMarkerTurnOffSkipsMetadataCleanup() {
    assertNull(UiV2GrouperLoaderTypeAuthorization.checkSave(NO_MARKER, false, null, NONE));
    assertFalse(UiV2GrouperLoaderTypeAuthorization.shouldRemoveLoaderMetadata(NO_MARKER));
    assertFalse(UiV2GrouperLoaderTypeAuthorization.shouldRemoveLoaderMetadata(null));
    // a real removal by an allowed editor still cleans the metadata (GRP-6706)
    assertNull(UiV2GrouperLoaderTypeAuthorization.checkSave(SQL_GROUP, false, null, ALL));
    assertTrue(UiV2GrouperLoaderTypeAuthorization.shouldRemoveLoaderMetadata(SQL_GROUP));
  }

  /**
   * advisory C2 and type switches: an ABAC-only editor on an SQL or LDAP group
   */
  public void testAbacEditorOnSqlLdapGroup() {
    for (Set<String> existing : Arrays.asList(SQL_GROUP, LDAP_GROUP)) {
      assertFalse("open/export " + existing, UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(existing, ABAC_ONLY));
      assertEquals("switch to JEXL " + existing, UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
          UiV2GrouperLoaderTypeAuthorization.checkSave(existing, true, "JEXL_SCRIPT", ABAC_ONLY));
      assertEquals("turn off " + existing, UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
          UiV2GrouperLoaderTypeAuthorization.checkSave(existing, false, null, ABAC_ONLY));
      assertEquals("keep SQL/LDAP " + existing, UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
          UiV2GrouperLoaderTypeAuthorization.checkSave(existing, true, "SQL", ABAC_ONLY));
    }
  }

  /**
   * an ABAC editor on a JEXL group: allowed for JEXL, refused for switching to another type it
   * has no right for, and refused on a recent memberships group
   */
  public void testAbacEditorOnJexlGroup() {
    assertTrue(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(JEXL_GROUP, ABAC_ONLY));
    assertNull(UiV2GrouperLoaderTypeAuthorization.checkSave(JEXL_GROUP, true, "JEXL_SCRIPT", ABAC_ONLY));
    assertNull(UiV2GrouperLoaderTypeAuthorization.checkSave(JEXL_GROUP, false, null, ABAC_ONLY));
    assertEquals(UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
        UiV2GrouperLoaderTypeAuthorization.checkSave(JEXL_GROUP, true, "SQL", ABAC_ONLY));
    assertEquals(UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
        UiV2GrouperLoaderTypeAuthorization.checkSave(JEXL_GROUP, true, "RECENT_MEMBERSHIPS", ABAC_ONLY));

    // removing the old type needs its own right: a recent-only editor cannot replace a JEXL loader
    assertEquals(UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
        UiV2GrouperLoaderTypeAuthorization.checkSave(JEXL_GROUP, true, "RECENT_MEMBERSHIPS", RECENT_ONLY));
    assertFalse(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(JEXL_GROUP, RECENT_ONLY));
    assertFalse(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(RECENT_GROUP, ABAC_ONLY));
    assertTrue(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(RECENT_GROUP, RECENT_ONLY));
  }

  /**
   * a group with no loader: anyone with some loader right may open the screen and create the
   * type they are allowed
   */
  public void testNoMarkerCreate() {
    assertTrue(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(NO_MARKER, ABAC_ONLY));
    assertTrue(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(NO_MARKER, RECENT_ONLY));
    assertNull(UiV2GrouperLoaderTypeAuthorization.checkSave(NO_MARKER, true, "JEXL_SCRIPT", ABAC_ONLY));
    assertEquals(UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
        UiV2GrouperLoaderTypeAuthorization.checkSave(NO_MARKER, true, "LDAP", ABAC_ONLY));
  }

  /**
   * every right: everything known is allowed, including a group carrying two markers
   */
  public void testAllRights() {
    Set<String> twoMarkers = UiV2GrouperLoaderTypeAuthorization.existingLoaderTypes(true, false, true, false);
    assertEquals(2, twoMarkers.size());
    for (String type : new String[] {"SQL", "LDAP", "JEXL_SCRIPT", "RECENT_MEMBERSHIPS"}) {
      assertNull(type, UiV2GrouperLoaderTypeAuthorization.checkSave(SQL_GROUP, true, type, ALL));
      assertNull(type, UiV2GrouperLoaderTypeAuthorization.checkSave(twoMarkers, true, type, ALL));
    }
    assertTrue(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(twoMarkers, ALL));
    // two markers and only one right: refused, since the save would remove the other one
    assertFalse(UiV2GrouperLoaderTypeAuthorization.canOpenOrExport(twoMarkers, ABAC_ONLY));
    assertEquals(UiV2GrouperLoaderTypeAuthorization.REFUSE_NOT_ALLOWED,
        UiV2GrouperLoaderTypeAuthorization.checkSave(twoMarkers, true, "JEXL_SCRIPT", ABAC_ONLY));
  }
}
