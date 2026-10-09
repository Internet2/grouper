/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

/**
 * how a tool keeps its result, and the work behind it, from growing without limit.  every tool
 * declares one (see {@link GrouperTool#resultBound()}), so a new tool cannot be added without
 * someone deciding how big its result can get.  one large result costs database work and memory
 * on the server, and for the AI agent in the UI it is also sent to the AI provider, and again on
 * later steps
 */
public enum GrouperToolResultBound {

  /**
   * returns a page at a time.  {@link GrouperTool#pageSizeWhenNotGiven} says how, and the executor
   * caps the page size (grouper.ai.agent.maxPageSize for the UI, grouper.mcp.maxPageSize for MCP)
   */
  paged,

  /** returns one object or the outcome of one change */
  oneObject,

  /** returns about what it was asked about, e.g. one entry for each subject passed in */
  boundedByInput,

  /** limits its own result, in rows or characters */
  cappedInTool,

  /** returns what admins have configured, e.g. recipes, templates or external systems */
  boundedByConfig,

  /** a subject search, limited by the result limits of the subject sources it searches */
  boundedBySubjectSources,

  /**
   * can return as much as the underlying data has.  a tool declares this only with a comment
   * saying why it cannot be bounded, and the list of such tools is checked by a unit test, so
   * adding one is a deliberate decision.  for the AI agent in the UI the result is still cut to
   * grouper.ai.agent.maxToolResultChars before the model sees it, but only after the server has
   * built all of it
   */
  notBounded;
}
