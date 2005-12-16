/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.registry;

import  java.io.*;
import  java.sql.*;
import  java.util.*;

/**
 * Reset the Groups Registry to a default state.
 * <p />
 * @author  blair christensen.
 * @version $Id: Reset.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class Reset {

  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Resets Groups Registry to pristine state.
   * <p>
   * Removes all stems, groups, memberships, privileges, members and 
   * subjects that have been aded to the Groups Registry.  The 
   * <i>GrouperSystem</i> subject and schema information are left
   * untouched.
   * </p>
   * <p>
   * Subjects are not reliably removed at this point, at least when
   * using HSQLDB.
   * </p>
   * <pre class="eg">
   * // Reset the Groups Registry to an almost pristine state.
   * % java com.devclue.grouper.registry.Reset
   * </pre>
   */
  public static void main(String[] args) {
    GroupsRegistry gr = new GroupsRegistry();
    gr.reset();
  } // public static void main(args)

}

