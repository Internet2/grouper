/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Is the subject a member of this group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: hasMember.java,v 1.5 2009-11-02 03:50:51 mchyzer Exp $
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
      Group           g     = GroupFinder.findByName(s, group, true);
      Subject         subj  = SubjectFinder.findByIdOrIdentifier(subjId, true);
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

  /**
   * Is the subject a member of this group.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   group       Check membership in this {@link Group}.
   * @param   subjId      Check membership for this {@link Subject}.
   * @param   field
   * @return  True if a {@link Member}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String group, String subjId, Field field
  )
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Group           g     = GroupFinder.findByName(s, group, true);
      Subject         subj  = SubjectFinder.findByIdOrIdentifier(subjId, true);
      return g.hasMember(subj, field);
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
    catch (SchemaException e)                   {
      GrouperShell.error(i, e);
    }
    return false;
  } // public static boolean invoke(i, stack, group, subjId, field)

} // public class hasMember

