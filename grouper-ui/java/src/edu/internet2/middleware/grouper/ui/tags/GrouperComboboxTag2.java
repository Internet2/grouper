/**
 * @author mchyzer
 * $Id: TfCombobox.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;

/**
 * <pre>
 * Note:
 * The idBase + "Id" is the DOM id of the control
 * The idBase + "Name" is what is submitted to the server (selected id)
 * The idBase + "ErrorId" is the span for where validation errors can go
 * </pre>
 */
public class GrouperComboboxTag2 extends SimpleTagSupport {

  /**
   * id and class of elements, and name of combobox. Make this unique in page.
   * e.g. personPicker. The id of the tag will be personPickerId, name will be
   * personPickerName.
   */
  private String idBase;

  /**
   * style, could include the width of the textfield
   */
  private String style;

  /**
   * class to use when drawing the control.
   */
  private String classCss;

  /**
   * wrap the control in a table so the error span can be placed to the right.
   * defaults to true.
   */
  private Boolean useTable = true;

  /**
   * search delay in ms (nullable). If null/negative, JS will default it.
   */
  private Integer searchDelay;

  /**
   * the operation to call when filtering, relative to this page url to call.
   * NOTE: should end with "&name=" (or "?name=") so the query can be appended.
   */
  private String filterOperation;

  /**
   * the default value (will be submitted) which should appear in the combo box when drawn.
   * This value is an ID; the label will be looked up via ajax.
   */
  private String value;

  /**
   * send more form element names to the filter operation, comma separated
   */
  private String additionalFormElementNames;

  public void setIdBase(String idBase1) {
    this.idBase = idBase1;
  }

  public void setStyle(String style1) {
    this.style = style1;
  }

  public void setClassCss(String classCss1) {
    this.classCss = classCss1;
  }

  /**
   * @param useTable1 whether to wrap in a table
   */
  public void setUseTable(Boolean useTable1) {
    this.useTable = useTable1;
  }

  public void setSearchDelay(Integer searchDelay1) {
    this.searchDelay = searchDelay1;
  }

  public void setFilterOperation(String filterOperation1) {
    this.filterOperation = filterOperation1;
  }

  public void setValue(String value1) {
    this.value = value1;
  }

  public void setAdditionalFormElementNames(String additionalFormElementNames1) {
    this.additionalFormElementNames = additionalFormElementNames1;
  }

  /**
   * @return whether to wrap in a table (defaults to true)
   */
  public boolean useTableProcessed() {
    if (this.useTable == null) {
      return true;
    }
    return this.useTable.booleanValue();
  }

  /**
   * @return javascript literal for searchDelay (number or null). If null/negative, JS will default it.
   */
  public String searchDelayJs() {
    if (this.searchDelay == null || this.searchDelay < 0) {
      return "null";
    }
    return String.valueOf(this.searchDelay);
  }

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {

    StringBuilder result = new StringBuilder();

    boolean useTableLocal = this.useTableProcessed();

    // Put a table around it so the error message can be to the right
    if (useTableLocal) {
      result.append("<table style=\"padding: 0; border-spacing: 0\" ><tr><td>");
    }

    // Use a text input for Tom Select
    result.append("<input type=\"text\" id=\"" + this.idBase + "Id\" name=\"" + this.idBase + "Name\"");

    if (!StringUtils.isBlank(this.classCss)) {
      result.append(" class=\"" + this.classCss + "\"");
    }

    if (!StringUtils.isBlank(this.style)) {
      result.append(" style=\"" + this.style + "\"");
    }

    result.append(" autocomplete=\"off\" />");

    // Hidden field to capture what was typed (if anything) when not selected
    result.append("<input id=\"" + this.idBase + "IdDisplay\" name=\"" + this.idBase
        + "NameDisplay\" type=\"hidden\" value=\"\" />");

    // Init script (Tom Select) after DOM is ready
    result.append("<script>");
    result.append("  $(document).ready(function(){");

    // disableEnterKeyOnCombobox defaults to false. When Enter is not disabled, we enable useEnterForLookup so Enter triggers exact lookup (no '*').
    // When Enter is not disabled, we enable useEnterForLookup so Enter triggers exact lookup (no '*').
    boolean disableEnter = GrouperUiConfig.retrieveConfig()
        .propertyValueBoolean("grouperUi.disableEnterKeyOnCombobox", false);
    boolean useEnterForLookup = !disableEnter;

    // Escape single quotes since filterOperation is embedded in a single-quoted JS string
    String filterOperationEscaped = StringUtils.replace(StringUtils.defaultString(this.filterOperation), "'", "\\'");
    String valueEscaped = StringUtils.replace(StringUtils.defaultString(this.value), "'", "\\'");
    // NOTE: no newline immediately before ");" (requested)
    result.append("    grouperRegisterCombobox(" +
        "'#" + this.idBase + "Id', " +
        "'" + filterOperationEscaped + "', " +
        (StringUtils.isBlank(this.additionalFormElementNames) ? "null" : "'" + this.additionalFormElementNames + "'") + ", " +
        (StringUtils.isBlank(this.value) ? "null" : "'" + valueEscaped + "'") + ", " +
        "{searchDelay: " + this.searchDelayJs() + ", useEnterForLookup: " + (useEnterForLookup ? "true" : "false") + "}" +
        ");");

    result.append("  });");
    result.append("</script>");

    if (useTableLocal) {
      result.append("</td><td><span id=\"" + this.idBase + "ErrorId\"></span></td></tr></table>");
    } else {
      // When not using a table, keep the error span right after the control
      result.append("&nbsp;<span id=\"" + this.idBase + "ErrorId\"></span>");
    }

    this.getJspContext().getOut().print(result.toString());
  }
}
