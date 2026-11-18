package edu.internet2.middleware.grouper.app.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Builds the audit line that is appended (as HTML) to a workflow instance form each time
 * someone initiates, approves or disapproves it.  The stored form is later printed raw on the
 * view instance screen, so every dynamic value (subject source name, subject id, subject name,
 * action, state, timestamp) is HTML escaped here.  Subject names and ids come from subject
 * sources (often from upstream systems or self-editable data), so they must never be markup.
 *
 * The template itself is the externalized text "workflowFormAuditLine" (admin controlled) and
 * stays markup.
 *
 * Both placeholder spellings are replaced: ##name## (5.8.0 and later) and $$name$$ (5.7.1 and
 * older, and still the fr_FR text in 7.x).  The template is scanned once, so a value that
 * itself looks like a placeholder is not expanded again.
 *
 * In its own class so one version ships to every release and can be unit tested without a
 * database.
 * </pre>
 */
public class GrouperWorkflowAuditLine {

  /**
   * no instances
   */
  private GrouperWorkflowAuditLine() {
  }

  /**
   * build the audit line html fragment (wrapped in a div) to append to the form
   * @param template externalized text template with placeholders
   * @param subject subject who acted
   * @param state state of the instance when the subject acted
   * @param action button / action name
   * @param date when
   * @return the html fragment, values escaped
   */
  public static String auditLineHtml(String template, Subject subject, String state, String action, Date date) {

    // escape every value once, up front
    Map<String, String> values = new HashMap<String, String>();
    values.put("subjectSource", escape(subject == null || subject.getSource() == null ? null : subject.getSource().getName()));
    values.put("subjectId", escape(subject == null ? null : subject.getId()));
    values.put("subjectName", escape(subject == null ? null : subject.getName()));
    values.put("buttonText", escape(action));
    values.put("state", escape(state));
    values.put("timestamp", escape(date == null ? null : new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)));

    StringBuilder result = new StringBuilder();
    result.append("<div>");
    result.append(replacePlaceholders(template == null ? "" : template, values));
    result.append("</div>");
    return result.toString();
  }

  /**
   * single pass replace of ##name## and $$name$$ placeholders with known names.  Unknown
   * placeholders are left as is
   * @param template
   * @param values name to already escaped value
   * @return the replaced string
   */
  static String replacePlaceholders(String template, Map<String, String> values) {
    StringBuilder result = new StringBuilder();
    int i = 0;
    while (i < template.length()) {
      String delimiter = null;
      if (template.startsWith("##", i)) {
        delimiter = "##";
      } else if (template.startsWith("$$", i)) {
        delimiter = "$$";
      }
      if (delimiter != null) {
        int end = template.indexOf(delimiter, i + 2);
        if (end > i + 2) {
          String name = template.substring(i + 2, end);
          if (values.containsKey(name)) {
            result.append(values.get(name));
            i = end + 2;
            continue;
          }
        }
      }
      result.append(template.charAt(i));
      i++;
    }
    return result.toString();
  }

  /**
   * html escape a value, null becomes empty
   * @param value
   * @return the escaped value
   */
  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    return GrouperUtil.escapeHtml(value, true);
  }
}
