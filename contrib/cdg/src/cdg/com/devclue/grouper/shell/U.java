/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  java.util.*;

/**
 * Utility Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: U.java,v 1.1 2006-06-20 18:26:43 blair Exp $
 * @since   1.0
 */
class U {

  // PRIVATE CLASS CONSTANTS //
  private static final String Q_CLOSE = "' ";
  private static final String Q_OPEN  = "'";


  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static String q(String txt) {
    return Q_OPEN + txt + Q_CLOSE;
  } // protected static String q(txt)

} // class U

