/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  java.util.*;
import  org.apache.commons.lang.time.*;

/**
 * Shell Helper Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: ShellHelper.java,v 1.1 2006-06-23 17:30:09 blair Exp $
 * @since   0.0.1
 */
class ShellHelper {

  // PROTECTED CLASS METHODS //

  // @return  Evaluated command.
  // @throws  GrouperShellException
  // @since   0.0.1
  protected static String eval(Interpreter i, String cmd) 
    throws  GrouperShellException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    try {
      GrouperShell.setOurCommand(i, false); // Default to false
      Object obj = i.eval(cmd);
      // If we aren't in devel mode, just print out the results
      if (!GrouperShell.isDevel(i)) {
        p.pp(i, obj);
      }
    }
    catch (bsh.EvalError eBEE) {
      // FIXME I need to figure out what to do here
      if (GrouperShell.isDebug(i)) {
        i.error(E.BSH_EVAL + eBEE.getMessage());
      }
    }
    // Now update the command history
    try {
      // Unless it involves references to `last`
      // TODO Add methods for this
      if (!cmd.startsWith("last(")) {
        List history = GrouperShell.getHistory(i);
        GrouperShell.setHistory(i, history.size(), cmd);
      }
    }
    catch (bsh.EvalError eBEE) {
      i.error(E.GSH_SETHISTORY + eBEE.getMessage());
    }
    sw.stop();
    // If command are timed and this was not a `last` command output
    // how long it took to evaluate.
    // TODO Add methods for this
    // TODO Should we only time internal methods?  No, probably not.
    if ( (GrouperShell.isTimed(i)) && (!cmd.startsWith("last(")) ) {
      i.println( "time: " + sw.getTime() + "ms command=" + U.q(cmd) );
    }
    return cmd;
  } // protected static String  eval(i, cmd)

  // @throws  GrouperShellException
  // @since 0.0.1
  protected static void last(Interpreter i, int idx) 
    throws  GrouperShellException
  {
    try {
      List    history = GrouperShell.getHistory(i);
      // An ugly way of getting the last command
      if (idx == last.LAST) {  
        idx = history.size() + last.LAST;
      }
      String cmd = (String) history.get(idx);
      eval(i, cmd);
    }
    catch (ArrayIndexOutOfBoundsException eAIOOB) {
      GrouperShell.error(i, eAIOOB, E.OUTOFBOUNDS);
    }
    catch (bsh.EvalError eBEE)                    {
      GrouperShell.error(i, eBEE);
    }
    catch (IndexOutOfBoundsException eIOOB) {
      GrouperShell.error(i, eIOOB, E.OUTOFBOUNDS);
    }
  } // protected static void last(i, idx)

  // @throws  GrouperShellException
  // @since   0.0.1
  protected static void history(Interpreter i, int cnt) 
    throws  GrouperShellException
  {
    try {
      List      history = GrouperShell.getHistory(i);  
      Object[]  cmds    = history.toArray();
      int       offset  = 0;
      // We don't want everything
      if (cnt > 0) {
        offset = cmds.length - cnt;
      }
      for (int idx=offset; idx<cmds.length; idx++) {
        i.println("[" + idx + "] "  + cmds[idx]);
      }
    }
    catch (ArrayIndexOutOfBoundsException eAIOOB) {
      GrouperShell.error(i, eAIOOB, E.OUTOFBOUNDS);
    }
    catch (bsh.EvalError eBEE) {
      GrouperShell.error(i, eBEE);
    }
  } // protected static void history(i, cnt)

} // class ShellHelper

