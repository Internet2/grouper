/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

/**
 * which front door a tool call came through.
 *
 * <p>nothing about running a tool depends on this: the same tool, the same privileges, the same
 * scope check either way.  it exists so that somebody reading the log afterwards can tell a call
 * the user made themselves in the Grouper UI from one an MCP client made on their behalf, which
 * are different enough that "who did this and why" has a different answer.</p>
 */
public enum GrouperToolEntryPath {

  /** an MCP client the user connected, over OAuth or web service authentication */
  mcp,

  /** the AI agent in the Grouper UI, acting for the user whose session it is */
  ui;

}
