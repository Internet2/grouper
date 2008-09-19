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
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import  edu.internet2.middleware.subject.*;

/**
 * Get {@link Group} attribute value.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getGroupAttr.java,v 1.2 2008-09-19 06:28:17 mchyzer Exp $
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
      if (attr.equals("createSubject"))    {
        return g.getCreateSubject();
      }
      else if (attr.equals("createTime"))       {
        return g.getCreateTime();
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
    throw new GrouperShellException(GshErrorMessages.GROUP_ATTR_INVALID + attr);
  } // public static boolean invoke(i, stack, name, attr)

} // public class getGroupAttr

