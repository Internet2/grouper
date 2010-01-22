/*
 * @author mchyzer $Id: StemScope.java,v 1.3 2008-03-29 10:50:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.query;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

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
    return GrouperServiceUtils.enumValueOfIgnoreCase(StemScope.class, string, false);
  }

  /**
   * convert this to a real Grouper scope
   * @return the scope
   */
  public abstract Scope convertToScope();
}
