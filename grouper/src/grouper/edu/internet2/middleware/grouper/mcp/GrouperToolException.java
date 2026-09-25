/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

/**
 * something went wrong which is not a result the caller can act on.
 *
 * <p>a tool which ran and decided it could not do what was asked reports that as an ordinary
 * result, so that a model can read it and try something else.  this is for the other kind: there
 * is no such tool, or the server itself broke.  a model cannot correct its way past either, so
 * they travel as an exception and each front door renders it the way its protocol expects -- the
 * MCP servlet as a JSON-RPC error, the UI as a message on the screen.</p>
 */
public class GrouperToolException extends RuntimeException {

  /** serial version uid */
  private static final long serialVersionUID = 1L;

  /**
   * what kind of failure this is, so a front door can map it without parsing the message
   */
  public static enum Kind {

    /** no tool by that name, or the institution has turned it off */
    notFound,

    /** the server broke while running the tool */
    internalError;
  }

  /** what kind of failure this is */
  private Kind kind;

  /**
   * @param theKind what kind of failure this is
   * @param message what happened
   */
  public GrouperToolException(Kind theKind, String message) {
    super(message);
    this.kind = theKind;
  }

  /**
   * @param theKind what kind of failure this is
   * @param message what happened
   * @param cause the underlying failure
   */
  public GrouperToolException(Kind theKind, String message, Throwable cause) {
    super(message, cause);
    this.kind = theKind;
  }

  /**
   * @return what kind of failure this is
   */
  public Kind getKind() {
    return this.kind;
  }

}
