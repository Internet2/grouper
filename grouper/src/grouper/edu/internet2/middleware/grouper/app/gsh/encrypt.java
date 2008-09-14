/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.morphString.Encrypt;

/**
 * Encrypt a password
 * <p/>
 * @author  blair christensen.
 * @version $Id: encrypt.java,v 1.1 2008-09-14 04:20:28 mchyzer Exp $
 * @since   0.0.1
 */
public class encrypt {

  // PUBLIC CLASS METHODS //

  /**
   * Encrypt a password
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    Encrypt.encryptInput(false);
    return "";
  }
  /**
   * Encrypt a password
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param dontMask if dontMask, then dont mask the input from user
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter i, CallStack stack, String dontMask) {
    GrouperShell.setOurCommand(i, true);
    boolean dontMaskBoolean = "dontMask".equals(dontMask);
    if (!StringUtils.isBlank(dontMask) && !dontMaskBoolean) {
      System.out.println("If the argument is 'dontMask', then when typing in the password you will see the chars");
    }
    Encrypt.encryptInput(dontMaskBoolean);
    //lets remove the last history entry
    try {
      List history = GrouperShell.getHistory(i);
      history.remove(history.size()-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return "";
  }

} // public class resetRegistry

