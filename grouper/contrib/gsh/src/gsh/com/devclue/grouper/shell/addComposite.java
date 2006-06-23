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
 * Add a composite member.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addComposite.java,v 1.1 2006-06-23 19:48:43 blair Exp $
 * @since   0.0.1
 */
public class addComposite {

  // PUBLIC CLASS METHODS //

  /**
   * Add a composite member.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   group       Add {@link Composite} to {@link Group} with this name.
   * @param   type        {@link CompositeType}.
   * @param   left        Left factor {@link Group}.
   * @param   right       Right factor {@link Group}.
   * @param   subjId      Add {@link Subject} with this <i>subject id</i> as a member.
   * @return  True if succeeds.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String group, CompositeType type, String left, String right
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Group           g     = GroupFinder.findByName(s, group);
      Group           l     = GroupFinder.findByName(s, left);
      Group           r     = GroupFinder.findByName(s, right);
      g.addCompositeMember(type, l, r);
      return true;
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (MemberAddException eMA)              {
      GrouperShell.error(i, eMA);
    }
    return false;
  } // public static boolean invoke(i, stack, group, type, left, right)

} // public class addComposite

