/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Delete a group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: delGroup.java,v 1.1 2006-06-23 17:30:09 blair Exp $
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

