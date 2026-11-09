/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * what the user is shown before a write runs: the summary, and for a tool which shows a script, the
 * same summary as lines, each one either the server's words or a script, so the approval screen can
 * show a script in a code block.
 *
 * <p>The summary is the lines joined with new lines, so the two always say the same thing.  A script
 * line is escaped like any value in a summary, all on one line, with its line breaks shown as spaces
 * the way Grouper runs it.  The summary and the lines are kept with the waiting approval, in the
 * conversation; this object is used once and dropped.</p>
 */
public class GrouperToolConfirmation {

  /**
   * one line of what the user is shown
   */
  public static class Line implements Serializable {

    /** kept in the conversation, which is kept in the HTTP session */
    private static final long serialVersionUID = 1L;

    /** the text, already escaped for hidden characters, not for HTML */
    private String text;

    /** true if it is a script, shown in a code block */
    private boolean code;

    /**
     * @param theText the text, already escaped for hidden characters
     * @param theCode true if it is a script
     */
    public Line(String theText, boolean theCode) {
      this.text = theText;
      this.code = theCode;
    }

    /**
     * @return the text, escaped for hidden characters but not for HTML
     */
    public String getText() {
      return this.text;
    }

    /**
     * @return true if it is a script, shown in a code block
     */
    public boolean isCode() {
      return this.code;
    }
  }

  /** the summary, which is the record of what was approved */
  private String summary;

  /** the summary as lines, or empty if it is shown as it is */
  private List<Line> lines = new ArrayList<Line>();

  /**
   * a summary shown as it is
   * @param theSummary the summary
   */
  public GrouperToolConfirmation(String theSummary) {
    this.summary = theSummary;
  }

  /**
   * a summary made of lines, which is them joined with new lines
   * @param theLines the lines
   */
  public GrouperToolConfirmation(List<Line> theLines) {
    this.lines.addAll(theLines);
    this.summary = joinLines(theLines);
  }

  /**
   * @param theLines lines
   * @return their text joined with new lines, a script between backticks, the way a summary shows it.
   * no line has a new line of its own, so the only new lines are the ones between the lines
   */
  public static String joinLines(List<Line> theLines) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < theLines.size(); i++) {
      if (i > 0) {
        result.append('\n');
      }
      Line line = theLines.get(i);
      if (line.isCode()) {
        result.append('`').append(line.getText()).append('`');
      } else {
        result.append(line.getText());
      }
    }
    return result.toString();
  }

  /**
   * @return the summary
   */
  public String getSummary() {
    return this.summary;
  }

  /**
   * @return the summary as lines, empty if it is shown as it is
   */
  public List<Line> getLines() {
    return this.lines;
  }
}
