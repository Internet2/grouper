package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import edu.internet2.middleware.grouper.abac.GrouperJexlScriptAnalysis;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Privacy-realm access checks for entity data rows and data fields, in one place so every
 * consumer applies the same rule:
 *
 *   - a data row is protected by its row privacy realm (grouperDataRow.&lt;id&gt;.rowPrivacyRealm)
 *     AND each field in the row by its own field privacy realm
 *     (grouperDataField.&lt;id&gt;.fieldPrivacyRealm)
 *   - "view" access only allows seeing that the field or row exists (metadata); reading assigned
 *     values needs "read" or "update"; using a row or field in a saved scripted group needs "update"
 *   - a missing row, field or realm config fails closed: blank access, except that sysadmins keep
 *     the access the GrouperDataEngine gives them when a realm is not configured
 *
 * Used by:
 *   - GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml (scripted group save and Analyze screen):
 *     the row named in entity.hasRow(...) and the fields named in attributeCompare(...)
 *   - UiV2Subject.viewDataFieldAssignments (subject entity data screen): which row assignments
 *     may be shown
 *
 * This class only uses GrouperDataEngine / config APIs that exist in every release with privacy
 * realms (5.7.0 and later), so the same source can be dropped into each release.  Note that
 * GrouperDataEngine.calculateHighestLevelAccess is static before 5.19.0 and an instance method
 * after, so the source is the same everywhere but the class must be compiled against the release
 * it runs on.
 * </pre>
 */
public class GrouperDataPrivacyRealmAccess {

  /** access level that allows editing the policy (saving a scripted group that uses the data) */
  public static final String ACCESS_UPDATE = "update";

  /** access level that allows reading assigned values */
  public static final String ACCESS_READ = "read";

  /** access level that only allows seeing metadata (that the field or row exists) */
  public static final String ACCESS_VIEW = "view";

  /**
   * highest access the subject has to a privacy realm.
   * @param grouperDataEngine engine with fields and rows loaded
   * @param privacyRealmConfigId realm config id (from a field or row config)
   * @param subject logged in subject (null is handled the way the engine handles it)
   * @return "update", "read", "view" or "" (never null)
   */
  public static String accessForPrivacyRealm(GrouperDataEngine grouperDataEngine, String privacyRealmConfigId, Subject subject) {
    if (grouperDataEngine == null) {
      return "";
    }
    GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = privacyRealmConfigId == null ? null
        : grouperDataEngine.getPrivacyRealmConfigByConfigId().get(privacyRealmConfigId);

    if (grouperPrivacyRealmConfig == null) {
      // realm not configured: fail closed for normal users.  Sysadmins get the same access the
      // 6.3+ GrouperDataEngine gives them for a missing realm (older engines would throw a
      // NullPointerException here, so do not pass null to the engine)
      if (subject == null) {
        return "";
      }
      if (PrivilegeHelper.isWheelOrRoot(subject)) {
        return ACCESS_UPDATE;
      }
      if (PrivilegeHelper.isWheelOrRootOrReadonlyRoot(subject)) {
        return ACCESS_READ;
      }
      if (PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject)) {
        return ACCESS_VIEW;
      }
      return "";
    }

    // called through the instance so this compiles where the method is static (5.x before 5.19)
    // and where it is an instance method (5.19 and later).  A null subject is passed through as is,
    // so callers get exactly what the existing data field check gets (e.g. "read" on a public realm)
    String highestLevelAccess = grouperDataEngine.calculateHighestLevelAccess(grouperPrivacyRealmConfig, subject);
    return highestLevelAccess == null ? "" : highestLevelAccess;
  }

  /**
   * access to a data row's privacy realm, by row alias (case insensitive)
   * @param grouperDataEngine engine with fields and rows loaded
   * @param rowAlias alias of the row, e.g. from entity.hasRow('alias', ...)
   * @param subject logged in subject
   * @return the access, "" if the row is not configured
   */
  public static String rowAccess(GrouperDataEngine grouperDataEngine, String rowAlias, Subject subject) {
    if (grouperDataEngine == null || rowAlias == null) {
      return "";
    }
    GrouperDataRowConfig grouperDataRowConfig = grouperDataEngine.getRowConfigByAlias().get(rowAlias.toLowerCase());
    return rowAccess(grouperDataEngine, grouperDataRowConfig, subject);
  }

  /**
   * access to a data row's privacy realm, by row config id
   * @param grouperDataEngine engine with fields and rows loaded
   * @param rowConfigId config id of the row (grouperDataRow.&lt;configId&gt;)
   * @param subject logged in subject
   * @return the access, "" if the row is not configured
   */
  public static String rowAccessByConfigId(GrouperDataEngine grouperDataEngine, String rowConfigId, Subject subject) {
    if (grouperDataEngine == null || rowConfigId == null) {
      return "";
    }
    GrouperDataRowConfig grouperDataRowConfig = grouperDataEngine.getRowConfigByConfigId().get(rowConfigId);
    return rowAccess(grouperDataEngine, grouperDataRowConfig, subject);
  }

  /**
   * @param grouperDataEngine
   * @param grouperDataRowConfig
   * @param subject
   * @return the access, "" if the row config is null
   */
  private static String rowAccess(GrouperDataEngine grouperDataEngine, GrouperDataRowConfig grouperDataRowConfig, Subject subject) {
    if (grouperDataRowConfig == null) {
      return "";
    }
    // despite the name, this holds the realm config id (grouperDataRow.<id>.rowPrivacyRealm)
    return accessForPrivacyRealm(grouperDataEngine, grouperDataRowConfig.getPrivacyRealmName(), subject);
  }

  /**
   * access to a data field's privacy realm, by field alias (case insensitive)
   * @param grouperDataEngine engine with fields and rows loaded
   * @param fieldAlias alias of the field
   * @param subject logged in subject
   * @return the access, "" if the field is not configured
   */
  public static String fieldAccess(GrouperDataEngine grouperDataEngine, String fieldAlias, Subject subject) {
    if (grouperDataEngine == null || fieldAlias == null) {
      return "";
    }
    GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByAlias().get(fieldAlias.toLowerCase());
    return fieldAccess(grouperDataEngine, grouperDataFieldConfig, subject);
  }

  /**
   * access to a data field's privacy realm, by field config id
   * @param grouperDataEngine engine with fields and rows loaded
   * @param fieldConfigId config id of the field (grouperDataField.&lt;configId&gt;)
   * @param subject logged in subject
   * @return the access, "" if the field is not configured
   */
  public static String fieldAccessByConfigId(GrouperDataEngine grouperDataEngine, String fieldConfigId, Subject subject) {
    if (grouperDataEngine == null || fieldConfigId == null) {
      return "";
    }
    GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(fieldConfigId);
    return fieldAccess(grouperDataEngine, grouperDataFieldConfig, subject);
  }

  /**
   * @param grouperDataEngine
   * @param grouperDataFieldConfig
   * @param subject
   * @return the access, "" if the field config is null
   */
  private static String fieldAccess(GrouperDataEngine grouperDataEngine, GrouperDataFieldConfig grouperDataFieldConfig, Subject subject) {
    if (grouperDataFieldConfig == null) {
      return "";
    }
    return accessForPrivacyRealm(grouperDataEngine, grouperDataFieldConfig.getGrouperPrivacyRealmConfigId(), subject);
  }

  /**
   * @param access from one of the access methods
   * @return true if the access allows reading assigned values ("read" or "update"); "view" does not
   */
  public static boolean canReadValues(String access) {
    return ACCESS_READ.equals(access) || ACCESS_UPDATE.equals(access);
  }

  /**
   * @param access from one of the access methods
   * @return true if the access allows editing policy that uses the data ("update")
   */
  public static boolean canUpdate(String access) {
    return ACCESS_UPDATE.equals(access);
  }

  // ---------------------------------------------------------------------------------------------
  // subject data screen: which row assignments may be shown
  // ---------------------------------------------------------------------------------------------

  /**
   * Keep only the data row assignments whose values the subject may read: "read" or "update" on
   * the row's privacy realm AND on the privacy realm of the assignment's field.  Anything with a
   * missing row, field or realm config is dropped (fail closed).  Filter before building any
   * response model, so no value, row config id or field config id of an unreadable row is shown.
   *
   * Generic (with getters passed in) so this class does not depend on GrouperDataRowAssignView,
   * which does not exist before 5.17.0; the scripted group check below needs this class there too.
   *
   * @param <T> assignment type, e.g. GrouperDataRowAssignView
   * @param grouperDataEngine engine with fields and rows loaded
   * @param assignments assignments for one subject (null gives an empty list)
   * @param rowConfigIdGetter gets the row config id, e.g. GrouperDataRowAssignView::getDataRowConfigId
   * @param fieldConfigIdGetter gets the field config id, e.g. GrouperDataRowAssignView::getDataFieldConfigId
   * @param loggedInSubject who is looking
   * @return a new list with the readable assignments, in the same order
   */
  public static <T> List<T> filterReadableRowAssignments(GrouperDataEngine grouperDataEngine, List<T> assignments,
      Function<T, String> rowConfigIdGetter, Function<T, String> fieldConfigIdGetter, Subject loggedInSubject) {

    List<T> result = new ArrayList<T>();
    if (assignments == null || assignments.size() == 0) {
      return result;
    }

    // one realm calculation per row config and per field config, not per assignment
    Map<String, Boolean> rowConfigIdToCanRead = new HashMap<String, Boolean>();
    Map<String, Boolean> fieldConfigIdToCanRead = new HashMap<String, Boolean>();

    for (T assignment : assignments) {
      if (assignment == null) {
        continue;
      }
      String rowConfigId = rowConfigIdGetter.apply(assignment);
      String fieldConfigId = fieldConfigIdGetter.apply(assignment);
      if (rowConfigId == null || fieldConfigId == null) {
        continue;
      }

      Boolean canReadRow = rowConfigIdToCanRead.get(rowConfigId);
      if (canReadRow == null) {
        canReadRow = canReadValues(rowAccessByConfigId(grouperDataEngine, rowConfigId, loggedInSubject));
        rowConfigIdToCanRead.put(rowConfigId, canReadRow);
      }
      if (!canReadRow) {
        continue;
      }

      Boolean canReadField = fieldConfigIdToCanRead.get(fieldConfigId);
      if (canReadField == null) {
        canReadField = canReadValues(fieldAccessByConfigId(grouperDataEngine, fieldConfigId, loggedInSubject));
        fieldConfigIdToCanRead.put(fieldConfigId, canReadField);
      }
      if (!canReadField) {
        continue;
      }

      result.add(assignment);
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------------
  // scripted group (ABAC) analysis: rows and fields named in the script
  // ---------------------------------------------------------------------------------------------

  /**
   * Check the row named in entity.hasRow('rowAlias', ...) for the scripted group analysis, with the
   * same rules the analysis applies to a data field: no access, "view", or a row that does not
   * exist is an error; when saving (not read only) anything but "update" is an error; "read" when
   * read only is a warning.  A missing row gets the same message as a row the user may not use, so
   * the message does not say which rows exist.
   * @param grouperDataEngine engine with fields and rows loaded
   * @param rowAlias row alias from the script
   * @param loggedInSubject user editing or analyzing the script
   * @param readOnly true if only analyzing, false if about to save
   * @param grouperJexlScriptAnalysis error or warning is set here
   * @return true if the analysis can continue, false if an error was set (caller returns the analysis)
   */
  public static boolean checkRowForJexlAnalysis(GrouperDataEngine grouperDataEngine, String rowAlias,
      Subject loggedInSubject, boolean readOnly, GrouperJexlScriptAnalysis grouperJexlScriptAnalysis) {

    // the analysis binds the row's internal id from the provider index, so the row must be there too
    boolean rowExists = grouperDataEngine != null && rowAlias != null
        && grouperDataEngine.getGrouperDataProviderIndex() != null
        && grouperDataEngine.getGrouperDataProviderIndex().getRowWrapperByLowerAlias().get(rowAlias.toLowerCase()) != null;

    String access = rowExists ? rowAccess(grouperDataEngine, rowAlias, loggedInSubject) : "";
    return applyJexlAnalysisAccess(rowAlias, access, readOnly, grouperJexlScriptAnalysis);
  }

  /**
   * Check a field named in the script (e.g. inside attributeCompare(...)), same rules as
   * checkRowForJexlAnalysis.
   * @param grouperDataEngine engine with fields and rows loaded
   * @param fieldAlias field alias from the script
   * @param loggedInSubject user editing or analyzing the script
   * @param readOnly true if only analyzing, false if about to save
   * @param grouperJexlScriptAnalysis error or warning is set here
   * @return true if the analysis can continue, false if an error was set (caller returns the analysis)
   */
  public static boolean checkFieldForJexlAnalysis(GrouperDataEngine grouperDataEngine, String fieldAlias,
      Subject loggedInSubject, boolean readOnly, GrouperJexlScriptAnalysis grouperJexlScriptAnalysis) {

    // the analysis binds the field's internal id from the provider index, so the field must be there too
    boolean fieldExists = grouperDataEngine != null && fieldAlias != null
        && grouperDataEngine.getGrouperDataProviderIndex() != null
        && grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(fieldAlias.toLowerCase()) != null;

    String access = fieldExists ? fieldAccess(grouperDataEngine, fieldAlias, loggedInSubject) : "";
    return applyJexlAnalysisAccess(fieldAlias, access, readOnly, grouperJexlScriptAnalysis);
  }

  /**
   * apply the scripted group analysis rule to an access level
   * @param alias row or field alias, for the message
   * @param access access level
   * @param readOnly true if only analyzing, false if about to save
   * @param grouperJexlScriptAnalysis error or warning is set here
   * @return true if the analysis can continue
   */
  private static boolean applyJexlAnalysisAccess(String alias, String access, boolean readOnly,
      GrouperJexlScriptAnalysis grouperJexlScriptAnalysis) {

    // the alias comes from the user's script and the message is shown in the UI, so escape it
    String aliasForMessage = " '" + GrouperUtil.xmlEscape(alias == null ? "" : alias) + "'";

    if (!canReadValues(access)) {
      // no access, "view" only, or not found
      grouperJexlScriptAnalysis.setErrorMessage(
          GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptAnalysisUserNotAllowedToViewAttribute") + aliasForMessage);
      return false;
    }

    if (!readOnly && !canUpdate(access)) {
      // saving a scripted group needs update on every row and field it names
      grouperJexlScriptAnalysis.setErrorMessage(
          GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptAnalysisUserNotAllowedToEditPolicy") + aliasForMessage);
      return false;
    }

    if (!canUpdate(access)) {
      // read only analysis with read access: allowed, but warn that the policy could not be saved
      grouperJexlScriptAnalysis.setWarningMessage(
          GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptAnalysisUserNotAllowedToEditPolicy") + aliasForMessage);
    }
    return true;
  }

}
