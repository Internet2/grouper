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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;

/**
 * Delete a stem.
 * <p/>
 * @author  blair christensen.
 * @version $Id: delStem.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class delStem {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a stem.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Stem} to delete.
   * @return  True if {@link Stem} was deleted.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            ns  = StemFinder.findByName(s, name);
      ns.delete();
      return true;
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemDeleteException eNSD)            {
      GrouperShell.error(i, eNSD);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    return false;
  } // public static boolean invoke(i, stack, name)

} // public class delStem

