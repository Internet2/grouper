/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * lets the existing tool classes satisfy {@link GrouperTool} without being rewritten.
 *
 * <p>the tool classes are plain classes with a static toolDefinition() and a static execute().
 * rather than converting all thirty of them at once, each is registered as an anonymous subclass
 * of this which calls through.  a tool moves onto the interface properly when it needs something
 * this cannot give it, which in practice means when it needs a confirmation summary or a row
 * limit.  the interface does not change either way, so callers never notice which form a tool is
 * in.</p>
 */
public abstract class GrouperToolLegacyAdapter implements GrouperTool {

  /** the tool name */
  private String name;

  /** what this tool needs to be allowed to do */
  private GrouperToolCategory category;

  /** whether to offer it in a tool list */
  private boolean advertised;

  /**
   * @param theName the tool name
   * @param theCategory what this tool needs to be allowed to do
   */
  public GrouperToolLegacyAdapter(String theName, GrouperToolCategory theCategory) {
    this(theName, theCategory, true);
  }

  /**
   * @param theName the tool name
   * @param theCategory what this tool needs to be allowed to do
   * @param theAdvertised whether to offer it in a tool list
   */
  public GrouperToolLegacyAdapter(String theName, GrouperToolCategory theCategory,
      boolean theAdvertised) {
    this.name = theName;
    this.category = theCategory;
    this.advertised = theAdvertised;
  }

  /**
   * @see GrouperTool#name()
   */
  public String name() {
    return this.name;
  }

  /**
   * a tool whose category depends on its arguments overrides this
   * @see GrouperTool#category(JsonNode)
   */
  public GrouperToolCategory category(JsonNode arguments) {
    return this.category;
  }

  /**
   * @see GrouperTool#advertised()
   */
  public boolean advertised() {
    return this.advertised;
  }

}
