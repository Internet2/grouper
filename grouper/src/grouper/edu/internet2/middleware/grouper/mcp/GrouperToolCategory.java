/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

/**
 * what a tool needs to be allowed to do, and whether running it changes anything.
 *
 * <p>these line up with the consent scopes an OAuth client agrees to, and with the scope a UI
 * session is running under.  a tool declares its own category, so that "is this a write" has one
 * answer which lives next to the code that does the writing.  the switch this replaces defaulted
 * to readonly for any name it did not recognise, which meant a new write tool nobody remembered
 * to list would be treated as a read by the throttle and the tool log.</p>
 */
public enum GrouperToolCategory {

  /** ordinary reads of groups, folders, members, privileges, attributes */
  readonly(false, "Membership in the MCP readonly or readwrite group is required."),

  /** changes groups, folders, memberships, privileges or attributes */
  readwrite(true, "Membership in the MCP readwrite group is required."),

  /** reads the database directly */
  sql(false, "Membership in the MCP SQL readonly group is required."),

  /** reads configuration, daemons and external systems */
  admin_readonly(false, "Membership in the MCP admin readonly group is required."),

  /** runs daemon jobs */
  admin_readwrite(true, "Membership in the MCP admin readwrite group is required.");

  /** true if running a tool in this category changes something */
  private final boolean write;

  /**
   * what a refused caller is told, after the tool name.  worded exactly as the MCP servlet
   * worded it before this moved, so existing clients see the same text
   */
  private final String deniedDetail;

  /**
   * @param theWrite whether this category changes anything
   * @param theDeniedDetail what a refused caller is told
   */
  private GrouperToolCategory(boolean theWrite, String theDeniedDetail) {
    this.write = theWrite;
    this.deniedDetail = theDeniedDetail;
  }

  /**
   * @return what a refused caller is told, after the tool name
   */
  public String getDeniedDetail() {
    return this.deniedDetail;
  }

  /**
   * whether running a tool in this category changes something.  intended for the confirmation
   * gate in the UI, to decide what needs the user to approve it before it runs
   * @return true if this is a write
   */
  public boolean isWrite() {
    return this.write;
  }

  /**
   * the string form stored in the tool_category column of the tool log and used in the throttle
   * config keys (grouper.mcp.throttle.&lt;category&gt;.defaultCallsPerMinute).  this is the enum
   * constant name, so renaming a constant changes stored log values and breaks throttle config.
   * note the OAuth consent scopes are named differently (e.g. sqlReadonly, adminReadonly)
   * @return the category name as it appears in the log
   */
  public String getLegacyCategory() {
    return this.name();
  }

  /**
   * @param legacyCategory the string form, e.g. "admin_readonly"
   * @return the category, or null if there is no such category
   */
  public static GrouperToolCategory valueOfOrNull(String legacyCategory) {
    for (GrouperToolCategory grouperToolCategory : GrouperToolCategory.values()) {
      if (grouperToolCategory.name().equals(legacyCategory)) {
        return grouperToolCategory;
      }
    }
    return null;
  }

}
