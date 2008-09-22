/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;

/**
 * Grouper Management Shell.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperShell.java,v 1.4 2008-09-22 15:06:40 mchyzer Exp $
 * @since   0.0.1
 */
public class GrouperShell {

  
  /** if we should exist on failure */ 
  static boolean exitOnFailure = false;
  
  // PROTECTED CLASS CONSTANTS //
  protected static final String NAME    = "gsh";


  // PRIVATE CLASS CONSTANTS //
  private static final String GSH_DEBUG   = "GSH_DEBUG";
  private static final String GSH_DEVEL   = "GSH_DEVEL";
  private static final String GSH_HISTORY = "_GSH_HISTORY";
  private static final String GSH_OURS    = "_GSH_OURS";
  private static final String GSH_SESSION = "_GSH_GROUPER_SESSION";
  private static final String GSH_TIMER   = "GSH_TIMER";


  // PRIVATE INSTANCE VARIABLES //
  private Interpreter   interpreter = null;
  private CommandReader r = null;


  // MAIN //

  /**
   * Run {@link GrouperShell}.
   * <pre class="eg">
   * // Launch GrouperShell in interactive mode
   * % gsh.sh
   *
   * // Run GrouperShell script
   * % gsh.sh script.gsh
   * 
   * // Read commands from STDIN
   * % gsh.sh - 
   * </pre>
   * @param args 
   * @since 0.0.1
   */
  public static void main(String args[]) {
    GrouperStartup.startup();
    //turn on logging
    Log bshLogger = LogFactory.getLog("bsh");
    if (bshLogger.isTraceEnabled()) {
      Interpreter.TRACE = true;
    }
    if (bshLogger.isDebugEnabled()) {
      Interpreter.DEBUG = true;
    }
    exitOnFailure = true;
    try {
      grouperShellHelper(args);
    }
    catch (GrouperShellException eGS) {
      System.err.println(eGS.getMessage());
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args)

  /**
   * helper method to kick off GSH without exiting
   * @param args
   * @throws GrouperShellException
   */
  static void grouperShellHelper(String args[]) throws GrouperShellException {
    
    System.out.println("Type help() for instructions");
    
    GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GSH);
    
    new GrouperShell( new ShellCommandReader(args) ).run();
  }
  

  // CONSTRUCTORS //

  // @since   0.1.1
  protected GrouperShell(CommandReader r) 
    throws  GrouperShellException
  {
    this.interpreter  = r.getInterpreter();
    this.r  = r;
  } // protected GrouperShell()


  // PROTECTED CLASS METHODS //

  // @since   0.0.1
  protected static void error(Interpreter i, Exception e) {
    error(i, e, e.getMessage());
  } // protected static void error(i, e)

  // @since   0.0.1
  protected static void error(Interpreter interpreter, Exception e, String msg) {
    interpreter.error(msg);
    if (isDebug(interpreter)) {
      e.printStackTrace();
    }
    if (ShellHelper.closeOpenTransactions(interpreter, e)) {
      ShellHelper.exitDueToOpenTransaction(interpreter);
    }
  } // protected static void error(i, e, msg)

  // @since   0.0.1
  protected static Object get(Interpreter i, String key) 
    throws  bsh.EvalError
  {
    return i.get(key);
  } // protected static Object set(i, key)

  // @since   0.0.1
  protected static List getHistory(Interpreter i) 
    throws  bsh.EvalError
  {
    List history = (ArrayList) GrouperShell.get(i, GSH_HISTORY);
    if (history == null) {
      history = new ArrayList();
    }
    return history;
  } // protected static List getHistory(i)

  // @since   0.0.1
  protected static GrouperSession getSession(Interpreter i) 
    throws  GrouperShellException
  {
    try {
      GrouperSession s = (GrouperSession) GrouperShell.get(i, GSH_SESSION);
      if (s == null) {
        s = GrouperSession.staticGrouperSession(false);
        
        if (s == null) {
          s = GrouperSession.start(
            SubjectFinder.findById(
              "GrouperSystem", "application", InternalSourceAdapter.ID
            )
          );
        }
        GrouperShell.set(i, GSH_SESSION, s);
      }
      return s;
    }
    catch (Exception e) {
      if (i != null) {
        i.error(e.getMessage());
      }
      throw new GrouperShellException(e);
    }
  } // protected static GrouperSession getSession(i)

  // @since   0.0.1
  protected static boolean isDebug(Interpreter i) {
    return _isTrue(i, GSH_DEBUG);
  } // protected static boolean isDebug(i)

  // @return  True if last command run was a GrouperShell command.
  // @since   0.0.1
  protected static boolean isOurCommand(Interpreter i) {
    return _isTrue(i, GSH_OURS);
  } // protected static boolean isOurCommand()

  // @return  True if commands should be timed.
  // @since   0.0.1
  protected static boolean isTimed(Interpreter i) {
    return _isTrue(i, GSH_TIMER);
  } // protected static boolean isTimed()

  // @since   0.0.1
  protected static void set(Interpreter i, String key, Object obj) 
    throws  bsh.EvalError
  {
    i.set(key, obj);  
  } // protected static void set(i, key, obj)

  // @since   0.0.1
  protected static boolean isDevel(Interpreter i) {
    return _isTrue(i, GSH_DEVEL);
  } // protected static boolean isDevel(i)

  // @since   0.0.1
  protected static void setHistory(Interpreter i, int cnt, String cmd) 
    throws  bsh.EvalError
  {
    List history = GrouperShell.getHistory(i);
    history.add(cnt, cmd);
    GrouperShell.set(i, GSH_HISTORY, history);
  } // protected static void setHistory(i, cnt, cmd)

  // @since   0.0.1
  public static void setOurCommand(Interpreter i, boolean b) {
    try {
      GrouperShell.set(i, GSH_OURS, Boolean.valueOf(b));
    }
    catch (bsh.EvalError eBEE) {
      i.error(eBEE.getMessage());
    }
  } // protected static void setOurCommand(i, b)


  // PROTECTED INSTANCE METHODS //

  // @since   0.1.1
  protected void run() 
    throws  GrouperShellException
  {
    String cmd = new String();
    try {
      this.interpreter.eval(  "importCommands(\"edu.internet2.middleware.grouper\")");
      this.interpreter.eval(  "importCommands(\"edu.internet2.middleware.grouper.app.gsh\")");
      this.interpreter.eval(  "importCommands(\"edu.internet2.middleware.grouper.app.misc\")");
      this.interpreter.eval(  "importCommands(\"edu.internet2.middleware.grouper.privs\")");
      //this.i.eval(  "importCommands(\"edu.internet2.middleware.grouper.registry\")");
      this.interpreter.eval(  "importCommands(\"edu.internet2.middleware.subject\")");
      this.interpreter.eval(  "importCommands(\"edu.internet2.middleware.subject.provider\")");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.app.gsh.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.app.misc.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.privs.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.misc.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.hibernate.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.subject.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.subject.provider.*;");
      
    }
    catch (bsh.EvalError eBBB) {
      throw new GrouperShellException(GshErrorMessages.I_IMPORT + eBBB.getMessage(), eBBB);
    }
    while ( ( cmd = r.getNext() ) != null) {
      if ( this._isComment(cmd) ) {
        continue;
      }
      if ( this._isTimeToExit(cmd) ) {
        int txSize = HibernateSession._internal_staticSessions().size();
        boolean hasTransactions = txSize>0;
        if (hasTransactions) {
          String error = "Exiting in the middle of " + txSize + " open transactions, they will be rolled back and closed";
          this.interpreter.println(error);
          LOG.error(error);
          HibernateSession._internal_closeAllHibernateSessions(new RuntimeException());
        }
        this._stopSession();
        if (hasTransactions) {
          ShellHelper.exitDueToOpenTransaction(this.interpreter);
        }
        break;
      }
      // Now try to eval the command
      cmd = ShellHelper.eval(interpreter, cmd);
    }
  } 

  /** logger */
  private static final Log LOG = LogFactory.getLog(ShellHelper.class);

  /**
   * 
   * @param i
   * @param var
   * @return true if istrue
   */
  private static boolean _isTrue(Interpreter i, String var) {
    boolean rv = false;
    try {
      Object  obj = GrouperShell.get(i, var);
    if (
                (obj != null)
            &&  (obj instanceof Boolean)
            &&  (Boolean.TRUE.equals( obj ))
         )
      {
        rv = true;
      }
    }
    catch (bsh.EvalError eBEE) {
      i.error(eBEE.getMessage());
    }
    return rv;
  } // private static boolean _isTrue(i, var)


  // PRIVATE INSTANCE METHODS //

  // I'm not sure if this is the best place for this but...
  // @since   1.1.0
  private boolean _isComment(String cmd) {
    if ( cmd.startsWith("#") || cmd.startsWith("//") ) {
      return true;
    }
    return false;
  } // private boolean _isComment(cmd)

  // I'm not sure if this is the best place for this but...
  // @since   1.1.0
  private boolean _isTimeToExit(String cmd) {
    if ( cmd.equals("exit") || cmd.equals("quit") ) {
      return true;
    }
    return false;
  } // private boolean _isTimeToExit(cmd)

  // @since   0.0.1
  private void _stopSession() 
    throws  GrouperShellException
  {
    try {
      // `GrouperShell.getSession()` will start the session if it doesn't exist.
      // That's just slow.  And wrong.
      GrouperSession s = (GrouperSession) GrouperShell.get(this.interpreter, GSH_SESSION);
      if (s != null) {
        s.stop();
        this.interpreter.unset(GSH_SESSION);
      }
    }
    catch (Exception e) {
      if (interpreter != null) {
        this.interpreter.error(e.getMessage());
      }
      throw new GrouperShellException(e);
    }
  } // private void _stopSession()

} // public class GrouperShell

