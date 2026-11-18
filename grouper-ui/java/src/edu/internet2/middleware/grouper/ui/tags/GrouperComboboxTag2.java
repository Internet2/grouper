/**
 * @author mchyzer
 * $Id: TfCombobox.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang3.StringUtils;

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

  /**
   * optional display label for the initial value. When provided the combobox seeds this label
   * so the value renders even if it cannot be resolved via ajax. Defaults to the raw value.
   */
  private String valueLabel;

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

  public void setValueLabel(String valueLabel1) {
    this.valueLabel = valueLabel1;
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
    this.getJspContext().getOut().print(this.renderHtml());
  }

  /**
   * Build the HTML and script for this combobox.  Values that come from the page (value, label,
   * filter operation, additional form element names) are encoded for the context they land in
   * (JavaScript string in a script element, or HTML attribute), since they can hold request or
   * subject data.  Normal values decode back to exactly what they were.
   * @return the html
   */
  public String renderHtml() {

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

    // These are embedded in single-quoted JS strings inside a script element.  Escaping only the
    // single quote is not enough: a closing script tag in the value would end the script element
    // and the rest would be parsed as HTML, so encode for a JS string in a script element.
    String filterOperationEscaped = escapeJavascriptStringInScript(this.filterOperation);
    String valueEscaped = escapeJavascriptStringInScript(this.value);
    String additionalFormElementNamesEscaped = escapeJavascriptStringInScript(this.additionalFormElementNames);
    // Only seed a display label when explicitly provided. Callers with an id-only value (e.g. reports)
    // omit valueLabel and rely on the async lookup to resolve a friendly label.
    boolean hasValueLabel = !StringUtils.isBlank(this.value) && !StringUtils.isBlank(this.valueLabel);
    String valueLabelEscaped = escapeJavascriptStringInScript(this.valueLabel);
    // NOTE: no newline immediately before ");" (requested)
    result.append("    grouperRegisterCombobox(" +
        "'#" + this.idBase + "Id', " +
        "'" + filterOperationEscaped + "', " +
        (StringUtils.isBlank(this.additionalFormElementNames) ? "null" : "'" + additionalFormElementNamesEscaped + "'") + ", " +
        (StringUtils.isBlank(this.value) ? "null" : "'" + valueEscaped + "'") + ", " +
        "{searchDelay: " + this.searchDelayJs() + ", useEnterForLookup: " + (useEnterForLookup ? "true" : "false") +
        (hasValueLabel ? ", valueLabel: '" + valueLabelEscaped + "'" : "") + "}" +
        ");");

    result.append("  });");
    result.append("</script>");

    if (useTableLocal) {
      result.append("</td><td><span id=\"" + this.idBase + "ErrorId\"></span></td></tr></table>");
    } else {
      // When not using a table, keep the error span right after the control
      result.append("&nbsp;<span id=\"" + this.idBase + "ErrorId\"></span>");
    }

    return result.toString();
  }

  /**
   * Encode a string for use INSIDE a JavaScript string literal (single or double quoted) that is
   * itself inside an HTML script element.  Escapes backslash, both quotes, CR, LF, tab, other
   * control characters and the U+2028 / U+2029 line separators, and also encodes the characters
   * that matter to the HTML parser (less than, greater than, ampersand, slash), so a value such as
   * a closing script tag cannot end the script element.  JavaScript decodes every escape back to
   * the original character, so the value the browser sees (and later submits) is unchanged.
   * @param input raw value, may be null
   * @return the encoded value (empty string for null), without surrounding quotes
   */
  public static String escapeJavascriptStringInScript(String input) {
    if (input == null) {
      return "";
    }
    StringBuilder result = new StringBuilder(input.length() + 16);
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      switch (c) {
        case '\\':
          result.append("\\\\");
          break;
        case '\'':
          result.append("\\'");
          break;
        case '"':
          result.append("\\\"");
          break;
        case '\n':
          result.append("\\n");
          break;
        case '\r':
          result.append("\\r");
          break;
        case '\t':
          result.append("\\t");
          break;
        // HTML-significant characters: the HTML parser ends a script element at a closing
        // script tag even inside a JavaScript string, so never emit a literal less than sign
        case '<':
          result.append("\\u003c");
          break;
        case '>':
          result.append("\\u003e");
          break;
        case '&':
          result.append("\\u0026");
          break;
        case '/':
          result.append("\\/");
          break;
        // JavaScript line terminators that are not newlines in Java
        case '\u2028':
          result.append("\\u2028");
          break;
        case '\u2029':
          result.append("\\u2029");
          break;
        default:
          if (c < 0x20 || c == 0x7f) {
            // other control characters
            String hex = Integer.toHexString(c);
            result.append("\\u");
            for (int j = hex.length(); j < 4; j++) {
              result.append('0');
            }
            result.append(hex);
          } else {
            result.append(c);
          }
      }
    }
    return result.toString();
  }

  /**
   * Encode a string for use inside a double or single quoted HTML attribute value.  The browser
   * decodes the entities, so the attribute value the page sees is the original string.
   * @param input raw value, may be null
   * @return the encoded value (empty string for null)
   */
  public static String escapeHtmlAttribute(String input) {
    if (input == null) {
      return "";
    }
    StringBuilder result = new StringBuilder(input.length() + 16);
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      switch (c) {
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&#39;");
          break;
        default:
          result.append(c);
      }
    }
    return result.toString();
  }
}
