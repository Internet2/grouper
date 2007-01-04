/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;

/**
 * Utility Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: U.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @since   0.0.1
 */
class U {

  // PRIVATE CLASS CONSTANTS //
  private static final String Q_CLOSE = "' ";
  private static final String Q_OPEN  = "'";


  // PROTECTED CLASS METHODS //

  // @since 0.0.1
  protected static String q(String txt) {
    return Q_OPEN + txt + Q_CLOSE;
  } // protected static String q(txt)

} // class U

