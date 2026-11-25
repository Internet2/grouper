package edu.internet2.middleware.grouper.app.config.check.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperObjectIdValidator;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * <p>SECURITY: the ids of groups, folders, attribute definitions and attribute definition names are
 * printed in UI pages, links and scripts as trusted values (they are not escaped there).  The save
 * operations only let a caller choose an id made of letters, digits, colon, underscore, dot and dash
 * (GrouperObjectIdValidator), and an XML import gives a new object a generated id if the id in the
 * file is outside that pattern.  An object created before those checks can still have another id.</p>
 *
 * <p>This check lists the objects whose id is outside the pattern: an ERROR when the id has a
 * character that means something in HTML, JavaScript or a URL (quotes, angle brackets, ampersand,
 * backslash, backtick, whitespace, control characters), else a WARNING.  It is a database check, so
 * the findings are not about a config property.  The ids themselves are not shown, only the object
 * names, since an id that fails might be markup.</p>
 */
public class ObjectIdNotAllowedConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * the object tables to check, and the object type in the message: {table, type}
   */
  public static final String[][] OBJECT_TABLES = new String[][] {
    {"grouper_groups", "group"},
    {"grouper_stems", "folder"},
    {"grouper_attribute_def", "attribute definition"},
    {"grouper_attribute_def_name", "attribute definition name"}
  };

  /**
   * most object names listed in one finding
   */
  public static final int MAX_NAMES_SHOWN = 10;

  /**
   * characters that mean something in HTML, JavaScript or URLs, or whitespace / control characters
   */
  private static final Pattern DANGEROUS_CHARACTERS = Pattern.compile("[<>\"'&\\\\`\\s\\p{Cntrl}]");

  /**
   * the allowed id pattern, as SQL regex (same as GrouperObjectIdValidator)
   */
  private static final String ALLOWED_ID_SQL_REGEX = "^[A-Za-z0-9:_.-]+$";

  @Override
  public String getName() {
    return "objectIdNotAllowed";
  }

  /**
   * where clause that picks ids outside the allowed pattern, in this database's regex syntax, or
   * null for a database without one here (then every id is checked in Java)
   * @return the where clause or null
   */
  protected String notAllowedIdWhereClause() {
    if (GrouperDdlUtils.isPostgres()) {
      return "id !~ '" + ALLOWED_ID_SQL_REGEX + "'";
    }
    if (GrouperDdlUtils.isOracle()) {
      return "not regexp_like(id, '" + ALLOWED_ID_SQL_REGEX + "')";
    }
    if (GrouperDdlUtils.isMysql()) {
      return "id not regexp '" + ALLOWED_ID_SQL_REGEX + "'";
    }
    return null;
  }

  /**
   * ids and names of the objects in a table whose id is not allowed.  Protected so a unit test can
   * stand in for the database.
   * @param tableName one of OBJECT_TABLES
   * @return rows of {id, name}
   */
  protected List<Object[]> retrieveIdsAndNamesNotAllowed(String tableName) {
    String whereClause = this.notAllowedIdWhereClause();
    String sql = "select id, name from " + tableName + (whereClause == null ? "" : (" where " + whereClause));
    List<Object[]> rows = new GcDbAccess().sql(sql).selectList(Object[].class);

    // the database regex is a filter; the Java validator is the rule (and the only check on other databases)
    List<Object[]> result = new ArrayList<Object[]>();
    for (Object[] row : GrouperUtil.nonNull(rows)) {
      String id = GrouperUtil.stringValue(row[0]);
      if (!GrouperObjectIdValidator.isValidNewId(id)) {
        result.add(row);
      }
    }
    return result;
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    for (String[] objectTable : OBJECT_TABLES) {

      List<Object[]> rows = this.retrieveIdsAndNamesNotAllowed(objectTable[0]);
      if (rows.size() == 0) {
        continue;
      }

      // ERROR if any of the ids could break out of an HTML attribute, script string or URL
      boolean dangerous = false;
      List<String> names = new ArrayList<String>();
      for (Object[] row : rows) {
        String id = GrouperUtil.stringValue(row[0]);
        if (id != null && DANGEROUS_CHARACTERS.matcher(id).find()) {
          dangerous = true;
        }
        if (names.size() < MAX_NAMES_SHOWN) {
          names.add(GrouperUtil.stringValue(row[1]));
        }
      }

      String objectType = objectTable[1];
      String namesText = GrouperUtil.join(names.iterator(), ", ")
          + (rows.size() > names.size() ? (", and " + (rows.size() - names.size()) + " more") : "");

      results.add(new ConfigurationCheckResult(
          dangerous ? ConfigurationCheckSeverity.ERROR : ConfigurationCheckSeverity.WARNING,
          this.getName(), null, null, null,
          rows.size() + " " + objectType + "(s) have an id with characters other than letters, digits, "
              + "colon, underscore, dot and dash"
              + (dangerous ? ", including characters that mean something in HTML, JavaScript or URLs" : "")
              + ".  Ids are printed in UI pages, links and scripts as trusted values.  Table "
              + objectTable[0] + ": " + namesText,
          "These were created before Grouper limited the ids a caller can choose (or imported from such "
              + "a Grouper).  Look in the audit log at who created them.  An id cannot be changed; if you "
              + "do not trust where an object came from, replace it with a new one (which gets a "
              + "generated id) and delete the old one."));
    }

    return results;
  }

}
