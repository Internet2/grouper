/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;

/**
 * Delete a composite member.
 * <p/>
 * @author  blair christensen.
 * @version $Id: delComposite.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 * @since   0.0.1
 */
public class delComposite {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a composite member.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   group       Delete {@link Composite} from {@link Group} with this name.
   * @return  True if succeeds.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String group
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Group           g     = GroupFinder.findByName(s, group);
      g.deleteCompositeMember();
      return true;
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (MemberDeleteException eMD)           {
      GrouperShell.error(i, eMD);
    }
    return false;
  } // public static boolean invoke(i, stack, group)

} // public class delComposite

