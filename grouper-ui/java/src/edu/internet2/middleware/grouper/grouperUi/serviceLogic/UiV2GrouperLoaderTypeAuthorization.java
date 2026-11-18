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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <pre>
 * Decides whether the logged-in user may open, export, or save the loader configuration of
 * one group, based on the loader type(s) the group actually has and the type the request asks
 * for.  Deny by default: an unknown loader type is never allowed, and every path that removes a
 * loader marker needs the right for that marker's type.
 *
 * Why this class exists: UiV2GrouperLoader.editGrouperLoaderSave only checked rights inside
 * the branches for the four recognized types, then ran its cleanup as root, so a request with
 * an unknown type (or "no loader" on a group with no marker) reached the root cleanup with no
 * check at all.  The edit and export screens checked "can edit some loader type" instead of
 * this group's type, so an ABAC-only editor could open an SQL/LDAP config.
 *
 * This class is pure decision logic: no request, session or container access.  The caller
 * passes in the group's markers and the user's three rights (computed by GrouperLoaderContainer
 * in the caller's release), so the same file works in every release.
 * </pre>
 */
public class UiV2GrouperLoaderTypeAuthorization {

  /** SQL loader type name, as posted in grouperLoaderTypeName */
  public static final String SQL = "SQL";

  /** LDAP loader type name */
  public static final String LDAP = "LDAP";

  /** JEXL scripted group (ABAC) loader type name */
  public static final String JEXL_SCRIPT = "JEXL_SCRIPT";

  /** recent memberships loader type name */
  public static final String RECENT_MEMBERSHIPS = "RECENT_MEMBERSHIPS";

  /** checkSave result: the requested type is blank or not one of the four known types */
  public static final String REFUSE_TYPE_INVALID = "typeInvalid";

  /** checkSave result: the user lacks the right for the requested type or for an existing type being removed */
  public static final String REFUSE_NOT_ALLOWED = "notAllowed";

  /**
   * The three loader-editing rights of the logged-in user on this group, as the caller's
   * release computes them (GrouperLoaderContainer).
   */
  public static class Rights {

    /** may edit SQL and LDAP loaders */
    private final boolean canEditSqlLdap;

    /** may edit JEXL scripted group (ABAC) loaders */
    private final boolean canEditAbac;

    /** may edit recent memberships loaders */
    private final boolean canEditRecentMemberships;

    /**
     * @param canEditSqlLdap may edit SQL and LDAP loaders
     * @param canEditAbac may edit JEXL scripted group loaders
     * @param canEditRecentMemberships may edit recent memberships loaders
     */
    public Rights(boolean canEditSqlLdap, boolean canEditAbac, boolean canEditRecentMemberships) {
      this.canEditSqlLdap = canEditSqlLdap;
      this.canEditAbac = canEditAbac;
      this.canEditRecentMemberships = canEditRecentMemberships;
    }

    /**
     * @return may edit SQL and LDAP loaders
     */
    public boolean isCanEditSqlLdap() {
      return this.canEditSqlLdap;
    }

    /**
     * @return may edit JEXL scripted group loaders
     */
    public boolean isCanEditAbac() {
      return this.canEditAbac;
    }

    /**
     * @return may edit recent memberships loaders
     */
    public boolean isCanEditRecentMemberships() {
      return this.canEditRecentMemberships;
    }

    /**
     * @return true if the user has at least one loader-editing right
     */
    public boolean isAnyRight() {
      return this.canEditSqlLdap || this.canEditAbac || this.canEditRecentMemberships;
    }
  }

  /**
   * Exact match only: no trimming and no case folding, so a padded or mixed-case value cannot
   * slip past the type branches in the save code.
   * @param loaderType posted type name
   * @return true if one of the four known types
   */
  public static boolean isKnownLoaderType(String loaderType) {
    return SQL.equals(loaderType) || LDAP.equals(loaderType)
        || JEXL_SCRIPT.equals(loaderType) || RECENT_MEMBERSHIPS.equals(loaderType);
  }

  /**
   * The loader types a group has now.  A set, since a group can carry more than one marker.
   * @param sqlMarker group has the SQL loader marker
   * @param ldapMarker group has the LDAP loader marker
   * @param jexlScriptMarker group has the JEXL scripted group marker
   * @param recentMembershipsMarker group has the recent memberships marker
   * @return the types, unmodifiable, empty if the group has no loader
   */
  public static Set<String> existingLoaderTypes(boolean sqlMarker, boolean ldapMarker,
      boolean jexlScriptMarker, boolean recentMembershipsMarker) {
    Set<String> result = new LinkedHashSet<String>();
    if (sqlMarker) {
      result.add(SQL);
    }
    if (ldapMarker) {
      result.add(LDAP);
    }
    if (jexlScriptMarker) {
      result.add(JEXL_SCRIPT);
    }
    if (recentMembershipsMarker) {
      result.add(RECENT_MEMBERSHIPS);
    }
    return Collections.unmodifiableSet(result);
  }

  /**
   * @param loaderType type name
   * @param rights user's rights, null means none
   * @return true if the user may create, change or remove a loader of this type; false for an unknown type
   */
  public static boolean canEditType(String loaderType, Rights rights) {
    if (rights == null) {
      return false;
    }
    if (SQL.equals(loaderType) || LDAP.equals(loaderType)) {
      return rights.isCanEditSqlLdap();
    }
    if (JEXL_SCRIPT.equals(loaderType)) {
      return rights.isCanEditAbac();
    }
    if (RECENT_MEMBERSHIPS.equals(loaderType)) {
      return rights.isCanEditRecentMemberships();
    }
    // deny by default
    return false;
  }

  /**
   * Edit screen and export: a group with no loader can be opened by anyone with some loader
   * right (they are about to create one); a group with a loader needs the right for every
   * type it has, so an ABAC-only editor cannot read an SQL/LDAP config.
   * @param existingLoaderTypes from existingLoaderTypes()
   * @param rights user's rights
   * @return true if allowed
   */
  public static boolean canOpenOrExport(Set<String> existingLoaderTypes, Rights rights) {
    if (rights == null) {
      return false;
    }
    if (existingLoaderTypes == null || existingLoaderTypes.isEmpty()) {
      return rights.isAnyRight();
    }
    for (String existingLoaderType : existingLoaderTypes) {
      if (!canEditType(existingLoaderType, rights)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Save: decide before any change is made.
   * <ul>
   * <li>Loader on: the requested type must be known and editable, and every existing type
   * other than the requested one must be editable (the save removes it).</li>
   * <li>Loader off: every existing type must be editable (the save removes it).  A group with
   * no marker has nothing to remove; that is allowed, and the caller must then skip the root
   * metadata cleanup (see shouldRemoveLoaderMetadata).</li>
   * </ul>
   * @param existingLoaderTypes from existingLoaderTypes()
   * @param requestedIsLoader posted grouperLoaderHasLoaderName
   * @param requestedType posted grouperLoaderTypeName
   * @param rights user's rights
   * @return null if allowed, else REFUSE_TYPE_INVALID or REFUSE_NOT_ALLOWED
   */
  public static String checkSave(Set<String> existingLoaderTypes, boolean requestedIsLoader,
      String requestedType, Rights rights) {
    Set<String> existing = existingLoaderTypes == null ? Collections.<String>emptySet() : existingLoaderTypes;
    if (requestedIsLoader) {
      if (!isKnownLoaderType(requestedType)) {
        return REFUSE_TYPE_INVALID;
      }
      if (!canEditType(requestedType, rights)) {
        return REFUSE_NOT_ALLOWED;
      }
      for (String existingLoaderType : existing) {
        if (!existingLoaderType.equals(requestedType) && !canEditType(existingLoaderType, rights)) {
          return REFUSE_NOT_ALLOWED;
        }
      }
      return null;
    }
    for (String existingLoaderType : existing) {
      if (!canEditType(existingLoaderType, rights)) {
        return REFUSE_NOT_ALLOWED;
      }
    }
    return null;
  }

  /**
   * GRP-6706 cleans loader metadata from the group and the groups it managed when a loader
   * config is removed.  Only do that when the group actually had a loader marker; otherwise a
   * VIEW-only user could strip the metadata that ties a managed group to its group-list loader.
   * @param existingLoaderTypes from existingLoaderTypes(), computed before the markers were removed
   * @return true if the metadata cleanup should run
   */
  public static boolean shouldRemoveLoaderMetadata(Set<String> existingLoaderTypes) {
    return existingLoaderTypes != null && !existingLoaderTypes.isEmpty();
  }
}
