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
 * @version $Id: delGroup.java,v 1.3 2006-06-21 22:33:54 blair Exp $
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
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      Group           g = GroupFinder.findByName(s, name);
      g.delete();
      return true;
    }
    catch (GroupDeleteException eGD)            {
      throw new GrouperShellException(eGD);
    }
    catch (GroupNotFoundException eGNF)         {
      throw new GrouperShellException(eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new GrouperShellException(eIP);
    }
  } // public static boolean invoke(i, stack, name)

} // public class delGroup

