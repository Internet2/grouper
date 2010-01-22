/*
 * @author mchyzer $Id: GroupQueryScope.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.query;

/**
 * scope of a group query (which attribute to look in or all)
 */
public enum GroupQueryScope {

  /** all of these attributes */
  ALL,

  /** search group name: a:b:c */
  NAME,

  /** search extension (right part of name) */
  EXTENSION,

  /** friendly version of name */
  DISPLAY_NAME,

  /** friendly version of extension */
  DISPLAY_EXTENSION;

}
