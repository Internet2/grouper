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
 * Delete a stem.
 * <p/>
 * @author  blair christensen.
 * @version $Id: delStem.java,v 1.4 2006-06-21 22:33:54 blair Exp $
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
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            ns  = StemFinder.findByName(s, name);
      ns.delete();
      return true;
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new GrouperShellException(eIP);
    }
    catch (StemDeleteException eNSD)            {
      throw new GrouperShellException(eNSD);
    }
    catch (StemNotFoundException eNSNF)         {
      throw new GrouperShellException(eNSNF);
    }
  } // public static boolean invoke(i, stack, name)

} // public class delStem

