/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  java.util.*;

/**
 * Delete a group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: delGroup.java,v 1.1 2006-06-20 19:53:17 blair Exp $
 * @since   1.0
 */
public class delGroup {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a  stem.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Group} to delete.
   * @since   1.0
   */
  public static void invoke(Interpreter i, CallStack stack, String name) {
    GroupHelper.delGroup(i, name);
  } // public static void invoke(i, stack, parent, name)

} // public class delGroup

