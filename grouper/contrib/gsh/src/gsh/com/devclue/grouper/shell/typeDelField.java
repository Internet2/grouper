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
 * Delete a {@link Field} from a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeDelField.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 * @since   0.0.2
 */
public class typeDelField {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a {@link Field} from a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   type        Add to this {@link GroupType}.
   * @param   name        Name of {@link Field} to delete.
   * @return  True if {@link Field} was deleted.
   * @throws  GrouperShellException
   * @since   0.0.2
   */
  public static boolean invoke(Interpreter i, CallStack stack, String type, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      GroupType       t = GroupTypeFinder.find(type);
      t.deleteField(s, name);
      return true;
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)                  {
      GrouperShell.error(i, eS);
    }
    return false;
  } // public static Field invoke(i, stack, type, name) 

} // public class typeDelField

