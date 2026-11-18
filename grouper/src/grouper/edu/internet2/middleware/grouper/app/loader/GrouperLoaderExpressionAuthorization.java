package edu.internet2.middleware.grouper.app.loader;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Who may save loader expressions.
 *
 * The SQL loader query and group query, and the LDAP loader filter and expressions (subject,
 * group name, group display name, group description, attribute filter), may contain ${...}
 * expressions.  The loader daemon evaluates them as GrouperSystem with static classes allowed,
 * so whoever writes one can run any code as root.  Delegated loader editors
 * (uiV2.loader.edit.if.in.group) are not trusted to do that, so:
 *
 * - a user who is not wheel or root may not save ${ in those fields;
 * - may not change (save or change the type of) a loader whose fields already contain ${ (a
 *   sysadmin wrote it, the delegate must not take it over); removing such a loader IS allowed,
 *   since it only stops it running (user decision 2026-11-11);
 * - may not import a SQL or LDAP loader with ${ in any attribute value.
 *
 * Wheel and root may do all of this as before.  Existing loaders keep running unchanged; nothing
 * here is checked at daemon time.  Plain queries and filters (no ${) are not affected.
 *
 * Kept in its own small class so the same file works in every release (2.6 on, Java 8 syntax, no
 * commons-lang import): UiV2GrouperLoader and GrouperLoaderImportExport each only add one call.
 * </pre>
 */
public class GrouperLoaderExpressionAuthorization {

  /** start of a loader expression (GrouperUtil.substituteExpressionLanguage only evaluates ${...}) */
  public static final String EXPRESSION_START = "${";

  /** reason key of the veto; there is no externalized text, so the UI shows the reason */
  public static final String VETO_REASON_KEY = "veto.grouperLoader.expression.wheelOrRootOnly";

  /** message for a refused save */
  public static final String MESSAGE_SAVE = "Only a Grouper system administrator can save a loader SQL query, group query, "
      + "or LDAP filter or expression that contains ${ (the loader runs it as the system).  Remove the ${...} or ask a system administrator.";

  /** message for a refused change of an existing loader */
  public static final String MESSAGE_EXISTING = "This loader has a SQL query, group query, or LDAP filter or expression that contains ${, "
      + "so only a Grouper system administrator can change it.";

  /** message for a refused import */
  public static final String MESSAGE_IMPORT = "Only a Grouper system administrator can import a SQL or LDAP loader with ${ in an attribute value "
      + "(the loader runs it as the system).";

  /**
   * no instances
   */
  private GrouperLoaderExpressionAuthorization() {
  }

  /**
   * see if any value contains an expression
   * @param values may be null or have nulls
   * @return true if any value contains ${
   */
  public static boolean containsExpression(String... values) {
    if (values == null) {
      return false;
    }
    for (String value : values) {
      if (value != null && value.contains(EXPRESSION_START)) {
        return true;
      }
    }
    return false;
  }

  /**
   * see if a subject may save loader expressions: wheel or root, checked as root so it does not
   * depend on the current session
   * @param subject
   * @return true if allowed
   */
  public static boolean allowedToEditExpressions(final Subject subject) {
    if (subject == null) {
      return false;
    }
    if (PrivilegeHelper.isSystemSubject(subject)) {
      return true;
    }
    return (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return PrivilegeHelper.isWheelOrRoot(subject);
      }
    });
  }

  /**
   * the stored values of the expression fields of a group's SQL and LDAP loader (nulls if not set),
   * read as root so a user who cannot read the loader attributes does not see them as blank
   * @param group
   * @return the values
   */
  public static String[] existingExpressionFieldValues(final Group group) {
    if (group == null) {
      return new String[0];
    }
    return (String[]) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        String[] result = new String[8];

        // SQL loader: legacy attributes, read through the attribute delegate on the grouperLoader type
        // assignment.  Not group.getAttributeOrNull(): that caches the group's type assignments on
        // this Group object, and when the group is not a loader yet the cache is empty, so the save
        // that follows (assign the type, then setAttribute on the same object) fails with "doesn't
        // have attribute" and nobody can create a new SQL loader
        result[0] = legacyLoaderAttributeValue(group, GrouperLoader.GROUPER_LOADER_QUERY);
        result[1] = legacyLoaderAttributeValue(group, GrouperLoader.GROUPER_LOADER_GROUP_QUERY);

        // LDAP loader: values on the LDAP loader marker assignment
        AttributeDefName ldapMarker = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);
        AttributeAssign ldapAssign = ldapMarker == null ? null
            : group.getAttributeDelegate().retrieveAssignment(null, ldapMarker, false, false);
        if (ldapAssign != null) {
          result[2] = ldapAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapFilterName());
          result[3] = ldapAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName());
          result[4] = ldapAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName());
          result[5] = ldapAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionName());
          result[6] = ldapAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName());
          result[7] = ldapAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapAttributeFilterExpressionName());
        }
        return result;
      }
    });
  }

  /**
   * value of a legacy grouperLoader attribute on the group, without touching the Group object's
   * cached type assignments (see existingExpressionFieldValues).  Call as root.
   * @param group
   * @param attributeName e.g. grouperLoaderQuery
   * @return the value or null if the group is not a SQL loader or the value is not set
   */
  private static String legacyLoaderAttributeValue(Group group, String attributeName) {
    edu.internet2.middleware.grouper.GroupType grouperLoaderType = edu.internet2.middleware.grouper.GroupTypeFinder.find("grouperLoader", false);
    AttributeDefName legacyAttribute = edu.internet2.middleware.grouper.misc.GrouperDAOFactory.getFactory()
        .getAttributeDefName().findLegacyAttributeByName(attributeName, false);
    if (grouperLoaderType == null || grouperLoaderType.getAttributeDefName() == null || legacyAttribute == null) {
      return null;
    }
    AttributeAssign typeAssign = group.getAttributeDelegate().retrieveAssignment(null, grouperLoaderType.getAttributeDefName(), false, false);
    if (typeAssign == null) {
      return null;
    }
    return typeAssign.getAttributeValueDelegate().retrieveValueString(legacyAttribute.getName());
  }

  /**
   * check a save on the loader screen
   * @param subject logged in user
   * @param group the group whose loader is saved
   * @param saveIsLoader true if the user saves a loader, false if the user removes it
   * @param editLoaderType SQL, LDAP, JEXL_SCRIPT, RECENT_MEMBERSHIPS, ... (the type being saved)
   * @param newSqlValues submitted SQL query and group query (checked when the type is SQL)
   * @param newLdapValues submitted LDAP filter and expressions (checked when the type is LDAP)
   * @return null if allowed, else the reason
   */
  public static String checkUiSave(Subject subject, Group group, boolean saveIsLoader, String editLoaderType,
      String[] newSqlValues, String[] newLdapValues) {

    // removing a loader only stops it running, so anyone allowed to edit the loader may remove it
    if (!saveIsLoader) {
      return null;
    }

    boolean newHasExpression = saveIsLoader
        && (("SQL".equals(editLoaderType) && containsExpression(newSqlValues))
            || ("LDAP".equals(editLoaderType) && containsExpression(newLdapValues)));

    // most saves have no expression at all: only look up wheel and the stored values when needed
    if (!newHasExpression && !containsExpression(existingExpressionFieldValues(group))) {
      return null;
    }
    if (allowedToEditExpressions(subject)) {
      return null;
    }
    return newHasExpression ? MESSAGE_SAVE : MESSAGE_EXISTING;
  }

  /**
   * check a save on the loader screen and throw a HookVeto (the UI shows its reason) if refused
   * @param subject logged in user
   * @param group the group whose loader is saved
   * @param saveIsLoader true if the user saves a loader, false if the user removes it
   * @param editLoaderType the type being saved
   * @param newSqlValues submitted SQL query and group query
   * @param newLdapValues submitted LDAP filter and expressions
   */
  public static void assertUiSaveAllowed(Subject subject, Group group, boolean saveIsLoader, String editLoaderType,
      String[] newSqlValues, String[] newLdapValues) {
    String error = checkUiSave(subject, group, saveIsLoader, editLoaderType, newSqlValues, newLdapValues);
    if (error != null) {
      throw new HookVeto(VETO_REASON_KEY, error);
    }
  }

  /**
   * check a loader import (GrouperLoaderImportExport.importLoaderConfig) by the subject of the
   * current grouper session (the UI imports in a session of the logged in user).  For SQL and LDAP
   * imports every attribute value is checked, not only the expression fields, so a differently
   * spelled attribute name cannot slip an expression past the check.
   * @param loaderType from the json
   * @param jsonNode parsed import
   * @return null if allowed, else the reason
   */
  public static String checkImport(String loaderType, JsonNode jsonNode) {
    if (!"SQL".equals(loaderType) && !"LDAP".equals(loaderType)) {
      return null;
    }
    JsonNode attributes = jsonNode == null ? null : jsonNode.get("attributes");
    boolean hasExpression = false;
    if (attributes != null && attributes.isArray()) {
      for (int index = 0; index < attributes.size(); index++) {
        JsonNode attribute = attributes.get(index);
        if (attribute != null && attribute.has("attributeValue")
            && containsExpression(GrouperUtil.jsonJacksonGetString(attribute, "attributeValue"))) {
          hasExpression = true;
          break;
        }
      }
    }
    if (!hasExpression) {
      return null;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    Subject subject = grouperSession == null ? null : grouperSession.getSubject();
    return allowedToEditExpressions(subject) ? null : MESSAGE_IMPORT;
  }

}
