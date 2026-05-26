package edu.internet2.middleware.grouper.app.gsh.template;

/**
 * One diagnostic produced by the Java compiler when compiling a GSH template
 * source — an error, warning, or note. Carries the line and column where the
 * problem was detected, plus a human-readable message. The line and column
 * come straight from javac (1-based), with -1 used when the compiler could
 * not pin the diagnostic to a specific position.
 *
 * See GshTemplateJavaCompiler for the producing API.
 *
 * GRP-7006
 */
public class GshTemplateCompileDiagnostic {

  /**
   * severity of the diagnostic: ERROR, WARNING, MANDATORY_WARNING, NOTE, or OTHER
   * (mirrors the javax.tools.Diagnostic.Kind enum values as strings)
   */
  private String severity;

  /**
   * 1-based line number in the source where the diagnostic occurred, or -1 if
   * the compiler did not associate the diagnostic with a specific line
   */
  private long lineNumber = -1;

  /**
   * 1-based column number in the source where the diagnostic occurred, or -1
   * if the compiler did not associate the diagnostic with a specific column
   */
  private long columnNumber = -1;

  /**
   * human-readable diagnostic message as produced by javac, in US locale
   */
  private String message;

  /**
   * name of the source object the diagnostic refers to (typically the class
   * name as a path-like string), or null if the compiler did not attribute
   * the diagnostic to a particular source
   */
  private String sourceClassName;

  /**
   * two-line "source context" snippet showing the offending source line
   * followed by a line of spaces with a single '^' pointing at the column,
   * mirroring javac's text output. Populated when line, column, and source
   * are all available; null otherwise. UI can render this in a fixed-width
   * font directly under the message for easy diagnosis. Example:
   * <pre>
   *     return 42
   *              ^
   * </pre>
   */
  private String sourceContext;

  /**
   * severity of the diagnostic
   * @return severity (ERROR, WARNING, MANDATORY_WARNING, NOTE, or OTHER)
   */
  public String getSeverity() {
    return severity;
  }

  /**
   * severity of the diagnostic
   * @param severity
   */
  public void setSeverity(String severity) {
    this.severity = severity;
  }

  /**
   * 1-based line number; -1 if not associated with a line
   * @return line number
   */
  public long getLineNumber() {
    return lineNumber;
  }

  /**
   * 1-based line number; -1 if not associated with a line
   * @param lineNumber
   */
  public void setLineNumber(long lineNumber) {
    this.lineNumber = lineNumber;
  }

  /**
   * 1-based column number; -1 if not associated with a column
   * @return column number
   */
  public long getColumnNumber() {
    return columnNumber;
  }

  /**
   * 1-based column number; -1 if not associated with a column
   * @param columnNumber
   */
  public void setColumnNumber(long columnNumber) {
    this.columnNumber = columnNumber;
  }

  /**
   * human-readable diagnostic message
   * @return message
   */
  public String getMessage() {
    return message;
  }

  /**
   * human-readable diagnostic message
   * @param message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * name of the source object the diagnostic refers to, or null
   * @return source class name
   */
  public String getSourceClassName() {
    return sourceClassName;
  }

  /**
   * name of the source object the diagnostic refers to, or null
   * @param sourceClassName
   */
  public void setSourceClassName(String sourceClassName) {
    this.sourceClassName = sourceClassName;
  }

  /**
   * source-context snippet (offending line + caret), or null
   * @return source context
   */
  public String getSourceContext() {
    return sourceContext;
  }

  /**
   * source-context snippet (offending line + caret), or null
   * @param sourceContext
   */
  public void setSourceContext(String sourceContext) {
    this.sourceContext = sourceContext;
  }

  /**
   * true if this diagnostic represents an error (not a warning, note, etc.)
   * @return is error
   */
  public boolean isError() {
    return "ERROR".equals(this.severity);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(severity == null ? "?" : severity);
    if (this.lineNumber != -1) {
      builder.append(" at line ").append(this.lineNumber);
      if (this.columnNumber != -1) {
        builder.append(", column ").append(this.columnNumber);
      }
    }
    if (this.message != null) {
      builder.append(": ").append(this.message);
    }
    if (this.sourceContext != null) {
      builder.append("\n").append(this.sourceContext);
    }
    return builder.toString();
  }

}
