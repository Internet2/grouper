/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;

/**
 * Set {@link Group} attribute value.
 * <p/>
 * @author  blair christensen.
 * @version $Id: setGroupAttr.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.0.1
 */
public class setGroupAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Set {@link Group} attribute value.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Group} to retrieve attribute on.
   * @param   attr  Name of attribute to set.
   * @param   val   New attribute value.
   * @return  True if attribute set to new value.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name, String attr, String val
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      Group           g = GroupFinder.findByName(s, name);
      g.setAttribute(attr, val);
      g.store();
      return true;
    }
    catch (AttributeNotFoundException eANF)     {
      GrouperShell.error(i, eANF);  
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (GroupModifyException eNSM)           {
      GrouperShell.error(i, eNSM);  
    }
    catch (GroupNotFoundException eNSNF)        {
      GrouperShell.error(i, eNSNF);
    }
    return false;
  } // public static boolean invoke(i, stack, name, attr, val)

} // public class setGroupAttr

