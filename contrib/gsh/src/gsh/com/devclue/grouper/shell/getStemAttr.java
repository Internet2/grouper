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
 * Get {@link Stem} attribute value.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getStemAttr.java,v 1.1 2006-06-27 19:28:29 blair Exp $
 * @since   0.0.1
 */
public class getStemAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Get {@link Stem} attribute value.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Stem} to retrieve attribute on.
   * @param   attr  Name of attribute to retrieve.
   * @return  Value of attribute.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Object invoke(Interpreter i, CallStack stack, String name, String attr) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            ns  = StemFinder.findByName(s, name);
      if      (attr.equals("createSource"))     {
        return ns.getCreateSource();
      }
      else if (attr.equals("createSubject"))    {
        return ns.getCreateSubject();
      }
      else if (attr.equals("createTime"))       {
        return ns.getCreateTime();
      }
      else if (attr.equals("description"))      {
        return ns.getDescription();
      }
      else if (attr.equals("displayExtension")) {
        return ns.getDisplayExtension();
      }
      else if (attr.equals("displayName"))      {
        return ns.getDisplayName();     
      }
      else if (attr.equals("extension"))        {
        return ns.getExtension();
      }
      else if (attr.equals("modifySource"))     {
        return ns.getModifySource();
      }
      else if (attr.equals("modifySubject"))    {
        return ns.getModifySubject();
      }
      else if (attr.equals("modifyTime"))       {
        return ns.getModifyTime();
      }
      else if (attr.equals("name"))             {
        return ns.getName();
      }
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    catch (SubjectNotFoundException eSNF)       {
      GrouperShell.error(i, eSNF);
    }
    throw new GrouperShellException(E.STEM_ATTR_INVALID + attr);
  } // public static boolean invoke(i, stack, name, attr)

} // public class getStemAttr

