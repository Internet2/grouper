package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Represents one data row config (e.g. "person_role") as a table on the subject
 * "data field assignments" screen.  Each data row the subject has for this config
 * becomes one table row, and each row carries its own field values keyed by data
 * field config id.
 *
 * Row identity is preserved (rows are built up per data_row_assign_internal_id and
 * kept in a stable order) so that a field that is null on one row and populated on
 * another is rendered against the correct row.  The previous model flattened all
 * values of a field into a single position-indexed list, which shifted a value into
 * the wrong row whenever an earlier row had no value for that field.
 */
public class GuiSubjectDataRowConfig {

  /**
   * ordered list of data field config ids that appear on any of this subject's rows for
   * this data row config; these are the table column headers, in first-seen order.
   */
  private List<String> fieldConfigIds = new ArrayList<String>();

  /**
   * one entry per data row the subject has for this data row config; each map is
   * dataFieldConfigId -&gt; ui friendly value.  A field missing from the map means that
   * row has no value for that field (rendered as a blank/"-" cell).
   */
  private List<Map<String, String>> rows = new ArrayList<Map<String, String>>();


  public List<String> getFieldConfigIds() {
    return fieldConfigIds;
  }

  public void setFieldConfigIds(List<String> fieldConfigIds) {
    this.fieldConfigIds = fieldConfigIds;
  }

  public List<Map<String, String>> getRows() {
    return rows;
  }

  public void setRows(List<Map<String, String>> rows) {
    this.rows = rows;
  }

}
