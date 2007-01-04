/*
 * Copyright (C) 2006-2007 blair christensen.
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
 * @version $Id: ShellHelper.java,v 1.8 2007-01-04 17:17:45 blair Exp $
 * @since   0.0.1
 */
class ShellHelper {

  // PROTECTED CLASS METHODS //

  // @return  Evaluated command.
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
      i.error(E.BSH_EVAL + eBEE.getMessage());
    }
    // Now update the command history
    try {
      // Unless it involves references to `last`
      if ( !_isLastCommand(cmd) ) {
        List history = GrouperShell.getHistory(i);
        GrouperShell.setHistory(i, history.size(), cmd);
      }
    }
    catch (bsh.EvalError eBEE) {
      i.error(E.GSH_SETHISTORY + eBEE.getMessage());
    }
    sw.stop();
    // If commands are timed and this was not a `last` command output
    // how long it took to evaluate.
    if ( (GrouperShell.isTimed(i)) && (!_isLastCommand(cmd)) ) {
      i.println( "time: " + sw.getTime() + "ms command=" + U.q(cmd) );
    }
    return cmd;
  } // protected static String  eval(i, cmd)

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


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static boolean _isLastCommand(String cmd) {
    if (cmd.startsWith("last(")) {
      return true;
    }
    return false;
  } // private static boolean _isLastCommand(cmd)

} // class ShellHelper

