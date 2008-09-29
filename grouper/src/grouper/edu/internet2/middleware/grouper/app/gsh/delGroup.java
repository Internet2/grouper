/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;

/**
 * Delete a group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: delGroup.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class delGroup {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a  stem.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Group} to delete.
   * @return  True if {@link Group} deleted.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      Group           g = GroupFinder.findByName(s, name);
      g.delete();
      return true;
    }
    catch (GroupDeleteException eGD)            {
      GrouperShell.error(i, eGD);
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    return false;
  } // public static boolean invoke(i, stack, name)

} // public class delGroup

