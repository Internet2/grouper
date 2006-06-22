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
import  java.io.*;
import  java.lang.*;
import  java.util.*;

/**
 * Grouper Management Shell.
 * <p/>
 * <h3>Supported Commands</h3>
 * <ul>
 *   <li><b>addRootStem(extension, displayExtension)</b> - Add root
 *     stem with the specified <i>extension</i> and <i>displayExtension</i>.</li>
 *   <li><b>addGroup(parent, extension, displayExtension)</b> - Add
 *     group beneath <i>parent</i> stem with the specified <i>extension</i>
 *     and <i>displayExtension</i>.</li>
 *   <li><b>addStem(parent, extension, displayExtension)</b> - Add stem
 *     beneath <i>parent</i> stem with the specified <i>extension</i>
 *     and <i>displayExtension</i>.</li>
 *   <li><b>delGroup(name)</b> - Delete group with the specified
 *     <i>name</i>.</li>
 *   <li><b>delStem(name)</b> - Delete stem with the specified
 *     <i>name</i>.</li>
 *   <li><b>exit</b> - Terminate shell.</li>
 *   <li><b>getGroups(name)</b> - Find all groups with <i>name</i> in any
 *     naming attribute value.</li>
 *   <li><b>getStems(name)</b> - Find all stems with <i>name</i> in any
 *     naming attribute value.</li>
 *   <li><b>history()</b> - Print commands that have been run.</li>
 *   <li><b>history(n)</b> - Print the last <i>n</i> commands that
 *     have been run.</li>
 *   <li><b>last()</b> - Run the last command executed.</li>
 *   <li><b>last(n)</b> - Execute command number <i>n</i>.</li>
 *   <li><b>resetRegistry()</b> - Restore the Groups Registry to a
 *     default state.</li>
 *   <li><b>quit</b> - Terminate shell.</li>
 * </ul> 
 * <h3>Variables</h3>
 * <ul>
 *  <li><b>GSH_DEBUG</b> - If set to true, stack traces will be printed
 *    upon failure.</li>
 * </ul>
 * @author  blair christensen.
 * @version $Id: GrouperShell.java,v 1.10 2006-06-22 15:03:09 blair Exp $
 * @since   0.0.1
 */
public class GrouperShell {

  // PROTECTED CLASS CONSTANTS //
  protected static final String GSH_DEBUG   = "GSH_DEBUG";
  protected static final String GSH_HISTORY = "_GSH_HISTORY";
  protected static final String GSH_SESSION = "_GSH_GROUPER_SESSION";
  protected static final String NAME        = "gsh";
  protected static final String VERSION     = "0.0.1";


  // PRIVATE INSTANCE VARIABLES //
  private Interpreter   i = null;
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
   * @since 0.0.1
   */
  public static void main(String args[]) {
    try {
      GrouperShell shell = new GrouperShell( new CommandReader(args) );
      shell._run();
    }
    catch (GrouperShellException eGS) {
      System.err.println(eGS.getMessage());
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args)


  // CONSTRUCTORS //

  // @throws  GrouperShellException
  // @since   0.0.1
  private GrouperShell(CommandReader r) 
    throws  GrouperShellException
  {
    this.i  = r.getInterpreter();
    this.r  = r;
  } // private GrouperShell()


  // PROTECTED CLASS METHODS //

  // @throws  GrouperShellException
  // @since   0.0.1
  protected static void error(Interpreter i, Exception e) 
    throws  GrouperShellException
  {
    error(i, e, e.getMessage());
  } // protected static void error(i, e)

  // @throws  GrouperShellException
  // @since   0.0.1
  protected static void error(Interpreter i, Exception e, String msg) 
    throws  GrouperShellException
  {
    i.error(msg);
    try {
      Object obj = GrouperShell.get(i, GSH_DEBUG);
      if (
                (obj != null)
            &&  (obj instanceof Boolean)
            &&  (obj.equals(Boolean.TRUE))
         )
      {
        e.printStackTrace();
      }
    }
    catch (bsh.EvalError eBEE) {
      i.error(eBEE.getMessage());
    }
    throw new GrouperShellException(msg, e);
  } // protected static void error(i, e, msg)

  // @throws  bsh.EvalError
  // @since   0.0.1
  protected static Object get(Interpreter i, String key) 
    throws  bsh.EvalError
  {
    return i.get(key);
  } // protected static Object set(i, key)

  // @throws  bsh.EvalError
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

  // @throws  bsh.EvalError

  // @throws  GrouperShellException
  // @since   0.0.1
  protected static GrouperSession getSession(Interpreter i) 
    throws  GrouperShellException
  {
    try {
      GrouperSession s = (GrouperSession) GrouperShell.get(i, GSH_SESSION);
      if (s == null) {
        s = GrouperSession.start(
          SubjectFinder.findById(
            "GrouperSystem", "application", InternalSourceAdapter.ID
          )
        );
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

  // @throws  bsh.EvalError
  // @since   0.0.1
  protected static void set(Interpreter i, String key, Object obj) 
    throws  bsh.EvalError
  {
    i.set(key, obj);  
  } // protected static void set(i, key, obj)

  // @throws  bsh.EvalError
  // @since   0.0.1
  protected static void setHistory(Interpreter i, int cnt, String cmd) 
    throws  bsh.EvalError
  {
    List history = GrouperShell.getHistory(i);
    history.add(cnt, cmd);
    GrouperShell.set(i, GSH_HISTORY, history);
  } // protected static void setHistory(i, cnt, cmd)


  // PRIVATE INSTANCE METHODS //

  // @throws  GrouperShellException
  // @since   0.0.1
  private void _run() 
    throws  GrouperShellException
  {
    String cmd = new String();
    try {
      this.i.eval(  "importCommands(\"com.devclue.grouper.shell\")"                 );
      this.i.eval(  "importCommands(\"edu.internet2.middleware.grouper\")"          );
      this.i.eval(  "importCommands(\"edu.internet2.middleware.subject\")"          );
      this.i.eval(  "importCommands(\"edu.internet2.middleware.subject.provider\")" );
      this.i.eval(  "import edu.internet2.middleware.grouper.*;"                    );
      this.i.eval(  "import edu.internet2.middleware.subject.*;"                    );
      this.i.eval(  "import edu.internet2.middleware.subject.provider.*;"           );
    }
    catch (bsh.EvalError eBBB) {
      throw new GrouperShellException(E.I_IMPORT + eBBB.getMessage(), eBBB);
    }
    while ( (cmd = r.next()) != null) {
      // TODO Replace these with something cleaner
      if ( cmd.startsWith("#") || cmd.startsWith("//") ) {
        continue;
      }
      if ( cmd.equals("exit") || cmd.equals("quit") ) {
        this._stopSession();
        break;
      }
      // Update command history
      try {
        setHistory(this.i, this.r.getCnt(), cmd);
      }
      catch (bsh.EvalError eBEE) {
        this.i.error(E.GSH_SETHISTORY + eBEE.getMessage());
      }
      // Now try to eval the command
      try {
        i.eval(cmd);
      }
      catch (bsh.EvalError eBEE) {
        // TODO ???
        // this.i.error("EVAL ERROR.GET: " + eBEE.getErrorText());
        // this.i.error("EVAL ERROR.GM : " + eBEE.getMessage());
      }
    }
  } // private void _run(r)

  // @throws  GrouperShellException
  // @since   0.0.1
  private void _stopSession() 
    throws  GrouperShellException
  {
    try {
      GrouperSession s = (GrouperSession) GrouperShell.getSession(this.i);
      if (s != null) {
        s.stop();
        this.i.unset(GSH_SESSION);
      }
    }
    catch (Exception e) {
      if (i != null) {
        this.i.error(e.getMessage());
      }
      throw new GrouperShellException(e);
    }
  } // private void _stopSession()


} // public class GrouperShell

