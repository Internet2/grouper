/*
 * @author mchyzer $Id: StemScope.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.query;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem.Scope;

/**
 * scope of groups under a stem.  either all or just one level (immediate)
 */
public enum StemScope {

  /** just direct immediate chlidren */
  ONE_LEVEL {

    /**
     * convert this to a real Grouper scope
     * @return the scope
     */
    @Override
    public Scope convertToScope() {
      return Scope.ONE;
    }
  },

  /** all children in the subtree below the stem */
  ALL_IN_SUBTREE {

    /**
     * convert this to a real Grouper scope
     * @return the scope
     */
    @Override
    public Scope convertToScope() {
      return Scope.SUB;
    }
  };

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static StemScope valueOfIgnoreCase(String string) {
    if (StringUtils.isBlank(string)) {
      return null;
    }
    for (StemScope stemScope : StemScope.values()) {
      if (StringUtils.equalsIgnoreCase(string, stemScope.name())) {
        return stemScope;
      }
    }
    StringBuilder error = new StringBuilder("Cant find sctemScope from string: '")
        .append(string);
    error.append("', expecting one of: ");
    for (StemScope stemScope : StemScope.values()) {
      error.append(stemScope.name()).append(", ");
    }
    throw new RuntimeException(error.toString());
  }

  /**
   * convert this to a real Grouper scope
   * @return the scope
   */
  public abstract Scope convertToScope();
}
