/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  java.util.*;

/**
 * Shell Helper Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: ShellHelper.java,v 1.4 2006-06-22 15:03:09 blair Exp $
 * @since   0.0.1
 */
class ShellHelper {

  // PROTECTED CLASS METHODS //

  // @return  True if suceeds.
  // @throws  GrouperShellException
  // @since 0.0.1
  protected static boolean eval(Interpreter i, int idx) 
    throws  GrouperShellException
  {
    try {
      List    history = GrouperShell.getHistory(i);
      // An ugly way of getting the last command
      if (idx == -2) {  
        idx = history.size() - 2;
      }
      String cmd = (String) history.get(idx);
      i.eval(cmd);
      return true;
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
    return false;
  } // protected static boolean eval(i, idx)

  // @return  True if suceeds.
  // @throws  GrouperShellException
  // @since   0.0.1
  protected static boolean history(Interpreter i, int cnt) 
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
      return true;
    }
    catch (ArrayIndexOutOfBoundsException eAIOOB) {
      GrouperShell.error(i, eAIOOB, E.OUTOFBOUNDS);
    }
    catch (bsh.EvalError eBEE) {
      GrouperShell.error(i, eBEE);
    }
    return false;
  } // protected static boolean history(i, cnt)

} // class ShellHelper

