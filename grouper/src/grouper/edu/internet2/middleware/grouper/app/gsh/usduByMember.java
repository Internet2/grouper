/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.usdu.USDU;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * usdu by a specific member
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: usduByMember.java,v 1.1 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class usduByMember {

  /**
   * usdu by member
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param member 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, Member member) {
    return invoke(interpreter, stack, member, 0);
  }

  /**
   * usdu by source
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param member 
   * @param options 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, Member member, int options) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(interpreter);
      String memberId = member.getUuid();
      boolean delete = GrouperUtil.hasOption(options, usdu.DELETE);
      USDU.resolveMember(s, memberId, delete);
      return "usdu completed successfully";
    } catch (Exception e) {
      GrouperShell.error(interpreter, e);
    }
    return "";    
  } // public static boolean invoke(i, stack, name)

} // public class resetRegistry

