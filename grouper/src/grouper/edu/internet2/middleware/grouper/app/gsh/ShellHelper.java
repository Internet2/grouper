/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import bsh.Interpreter;
import bsh.TargetError;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Shell Helper Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: ShellHelper.java,v 1.4 2009-11-02 03:50:51 mchyzer Exp $
 * @since   0.0.1
 */
class ShellHelper {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ShellHelper.class);

  /**
   * 
   * @param interpreter
   * @param cmd
   * @return a string
   * @throws GrouperShellException
   */
  protected static String eval(Interpreter interpreter, String cmd) 
    throws  GrouperShellException {
    StopWatch sw = new StopWatch();
    sw.start();
    boolean hasTransactionProblem = false;
    try {
      GrouperShell.setOurCommand(interpreter, false); // Default to false
      Object obj = null;
      try {
        obj = interpreter.eval(cmd);
      } catch (Throwable t) {
        hasTransactionProblem = closeOpenTransactions(interpreter, t);
        if (!(t instanceof bsh.EvalError)) {
          //these werent getting logged
          LOG.error(t.getMessage(), t);
        }
        if (t instanceof bsh.EvalError) {
          throw (bsh.EvalError)t;
        }
        if (t instanceof RuntimeException) {
          throw (RuntimeException)t;
        }
        throw new RuntimeException(t);
      }
      // If we aren't in devel mode, just print out the results
      if (!GrouperShell.isDevel(interpreter)) {
        p.pp(interpreter, obj);
      }
    } catch (bsh.EvalError eBEE) {
    	//2008-04-25: Gary Brown
    	//Invocation errors were being swallowed giving no indication of the
    	//actual problem. Now GSH prints more detailed error + causes and
    	//logs full stacktrace
    	StringBuffer err = new StringBuffer(eBEE.getMessage());
    	if(eBEE instanceof TargetError) {
    		Throwable t=((TargetError)eBEE).getTarget();
    		if(t!=null) {
    			LOG.error(eBEE.getMessage(), t);
    			err.append("\n// See error log for full stacktrace");
    		}
	    	while(t!=null) {
	    		err.append("\n// caused by: " + t.getClass().getName() + ":\n// " + t.getMessage());
	    		t=t.getCause();
	    	}
    	}
    	//note, full stack isnt useful here, as the cause isnt there...
      interpreter.error(GshErrorMessages.BSH_EVAL + err.toString());
            
    }
    // Now update the command history
    try {
      // Unless it involves references to `last`
      if ( !_isLastCommand(cmd) ) {
        List history = GrouperShell.getHistory(interpreter);
        GrouperShell.setHistory(interpreter, history.size(), cmd);
      }
    }
    catch (bsh.EvalError eBEE) {
      interpreter.error(GshErrorMessages.GSH_SETHISTORY + eBEE.getMessage());
    }
    sw.stop();
    // If commands are timed and this was not a `last` command output
    // how long it took to evaluate.
    if ( (GrouperShell.isTimed(interpreter)) && (!_isLastCommand(cmd)) ) {
      interpreter.println( "time: " + sw.getTime() + "ms command=" + GshUtil.q(cmd) );
    }

    //if we were in the middle of a transaction, then end it all
    if (hasTransactionProblem) {
      exitDueToOpenTransaction(interpreter);
    }
  
    return cmd;
  } // protected static String  eval(i, cmd)

  /**
   * @param interpreter
   */
  public static void exitDueToOpenTransaction(Interpreter interpreter) {
    //safest thing to do is kill java... im afraid if running a batch script that it will
    //continue and starting not using a transaction...
    String error = "Due to error inside of a transaction in GSH, java is shutting down";
    interpreter.println(error);
    LOG.error(error);
    System.exit(1);
  }

  /**
   * @param interpreter
   * @param t
   * @return true if has transaction problems
   */
  public static boolean closeOpenTransactions(Interpreter interpreter, Throwable t) {
    int numberOfTransactionsOpen;
    boolean hasTransactionProblem = false;
    numberOfTransactionsOpen = HibernateSession._internal_staticSessions().size();
    if (numberOfTransactionsOpen > 0) {
      hasTransactionProblem = true;
      HibernateSession._internal_closeAllHibernateSessions(t);
      String error = "Due to error, rolled back and closed " 
        + HibernateSession._internal_staticSessions().size() + " of " + numberOfTransactionsOpen 
        + " transactions";
      interpreter.println(error);
    }
    return hasTransactionProblem;
  }

  /**
   * 
   * @param i
   * @param idx
   * @throws GrouperShellException
   */
  protected static void last(Interpreter i, int idx) 
    throws  GrouperShellException {
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
      GrouperShell.error(i, eAIOOB, GshErrorMessages.OUTOFBOUNDS);
    }
    catch (bsh.EvalError eBEE)                    {
      GrouperShell.error(i, eBEE);
    }
    catch (IndexOutOfBoundsException eIOOB) {
      GrouperShell.error(i, eIOOB, GshErrorMessages.OUTOFBOUNDS);
    }
  } // protected static void last(i, idx)

  /**
   * 
   * @param i
   * @param cnt
   * @throws GrouperShellException
   */
  protected static void history(Interpreter i, int cnt) 
    throws  GrouperShellException {
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
      GrouperShell.error(i, eAIOOB, GshErrorMessages.OUTOFBOUNDS);
    }
    catch (bsh.EvalError eBEE) {
      GrouperShell.error(i, eBEE);
    }
  } // protected static void history(i, cnt)

  /**
   * 
   * @param cmd
   * @return true if last command
   */
  private static boolean _isLastCommand(String cmd) {
    if (cmd.startsWith("last(")) {
      return true;
    }
    return false;
  } // private static boolean _isLastCommand(cmd)

} // class ShellHelper

