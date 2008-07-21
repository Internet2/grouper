/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Delete a {@link GroupType} from a {@link Group}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: groupDelType.java,v 1.2 2008-07-21 04:44:17 mchyzer Exp $
 * @since   0.1.0
 */
public class groupDelType {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a {@link GroupType} from a {@link Group}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link Group}.
   * @param   type        Name of {@link GroupType}.
   * @return  True if {@link GroupType} deleted from {@link Group}.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name, String type
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      Group     g = GroupFinder.findByName(GrouperShell.getSession(i), name);
      GroupType t = GroupTypeFinder.find(type);
      g.deleteType(t);
      return true;
    }
    catch (GroupModifyException eGM)    {
      GrouperShell.error(i, eGM);
    }
    catch (GroupNotFoundException eGNF) {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)          {
      GrouperShell.error(i, eS);
    }
    return false;
  } // public static boolean invoke(i, stack, name, type)

} // public class groupDelType

