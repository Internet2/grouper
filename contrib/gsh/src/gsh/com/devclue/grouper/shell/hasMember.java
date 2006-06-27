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
 * Is the subject a member of this group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: hasMember.java,v 1.2 2006-06-27 23:09:45 blair Exp $
 * @since   0.0.1
 */
public class hasMember {

  // PUBLIC CLASS METHODS //

  /**
   * Is the subject a member of this group.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   group       Check membership in this {@link Group}.
   * @param   subjId      Check membership for this {@link Subject}.
   * @return  True if a {@link Member}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String group, String subjId
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Group           g     = GroupFinder.findByName(s, group);
      Subject         subj  = SubjectFinder.findById(subjId);
      return g.hasMember(subj);
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (SubjectNotFoundException eSNF)       {
      GrouperShell.error(i, eSNF); 
    }
    catch (SubjectNotUniqueException eSNU)      {
      GrouperShell.error(i, eSNU); 
    }
    return false;
  } // public static boolean invoke(i, stack, group, subjId)

} // public class hasMember

