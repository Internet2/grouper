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
 * Set {@link Stem} attribute value.
 * <p/>
 * @author  blair christensen.
 * @version $Id: setStemAttr.java,v 1.1 2006-06-27 19:28:29 blair Exp $
 * @since   0.0.1
 */
public class setStemAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Set {@link Stem} attribute value.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Stem} to retrieve attribute on.
   * @param   attr  Name of attribute to set.
   * @param   value New attribute value.
   * @return  True if attribute set to new value.1
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
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            ns  = StemFinder.findByName(s, name);
      if      (attr.equals("description"))      {
        ns.setDescription(val);
        return true;
      }
      else if (attr.equals("displayExtension")) {
        ns.setDisplayExtension(val);
        return true;
      }
      else {
        throw new GrouperShellException(E.STEM_ATTR_INVALID + attr);
      }
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemModifyException eNSM)            {
      GrouperShell.error(i, eNSM);  
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    return false;
  } // public static boolean invoke(i, stack, name, attr, val)

} // public class setStemAttr

