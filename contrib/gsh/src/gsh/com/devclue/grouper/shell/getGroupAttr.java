/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;

/**
 * Get {@link Group} attribute value.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getGroupAttr.java,v 1.4 2007-01-04 17:17:45 blair Exp $
 * @since   0.0.1
 */
public class getGroupAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Get {@link Group} attribute value.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Group} to retrieve attribute on.
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
      GrouperSession  s = GrouperShell.getSession(i);
      Group           g = GroupFinder.findByName(s, name);
      if      ("createSource".equals( attr ))     {
        return g.getCreateSource();
      }
      else if (attr.equals("createSubject"))    {
        return g.getCreateSubject();
      }
      else if (attr.equals("createTime"))       {
        return g.getCreateTime();
      }
      else if (attr.equals("modifySource"))     {
        return g.getModifySource();
      }
      else if (attr.equals("modifySubject"))    {
        return g.getModifySubject();
      }
      else if (attr.equals("modifyTime"))       {
        return g.getModifyTime();
      }
      else {
        return g.getAttribute(attr);
      }
    }
    catch (AttributeNotFoundException eANF)     {
      GrouperShell.error(i, eANF);  
    }
    catch (GroupNotFoundException eNSNF)        {
      GrouperShell.error(i, eNSNF);
    }
    catch (SubjectNotFoundException eSNF)       {
      GrouperShell.error(i, eSNF);
    }
    throw new GrouperShellException(E.GROUP_ATTR_INVALID + attr);
  } // public static boolean invoke(i, stack, name, attr)

} // public class getGroupAttr

