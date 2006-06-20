/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.Interpreter;
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
 *   <li><b>quit</b> - Terminate shell.</li>
 * </ul> 
 * <h3>Variables</h3>
 * <ul>
 *  <li><b>GSH_DEBUG</b> - If set to true, stack traces will be printed
 *    upon failure.</li>
 * </ul>
 * @author  blair christensen.
 * @version $Id: GrouperShell.java,v 1.7 2006-06-20 19:53:17 blair Exp $
 * @since   1.0
 */
public class GrouperShell {

  // PROTECTED CLASS CONSTANTS //
  protected static final String GSH_DEBUG   = "GSH_DEBUG";
  protected static final String GSH_HISTORY = "_GSH_HISTORY";
  protected static final String GSH_SESSION = "_GSH_GROUPER_SESSION";


  // PRIVATE CLASS CONSTANTS //
  private static final String   NAME    = "gsh";
  private static final String   VERSION = "0.0.1";


  // PRIVATE INSTANCE VARIABLES //
  private int             cnt   = 0;
  private PrintStream     err;
  private Interpreter     i;
  private BufferedReader  in;
  private PrintStream     out;


  // MAIN //

  /**
   * @since 1.0
   */
    
  public static void main(String args[]) {
    try {
      GrouperShell shell = new GrouperShell();
      shell._run();
    
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args)


  // CONSTRUCTORS //
  private GrouperShell() 
    throws  bsh.EvalError
  {
    this.in   = new BufferedReader( new InputStreamReader(System.in) );
    this.out  = System.out;
    this.err  = System.err;
    this.i    = new Interpreter(this.in, this.out, this.err, true);
    this._importCommands();
  } // private GrouperShell()


  // PROTECTED CLASS METHODS //

  // @since   1.0
  protected static void error(Interpreter i, Exception e) {
    error(i, e, e.getMessage());
  } // protected static void error(i, e)

  // @since   1.0
  protected static void error(Interpreter i, Exception e, String msg) {
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
  } // protected static void error(i, e, msg)

  // @throws  bsh.EvalError
  // @since   1.0
  protected static Object get(Interpreter i, String key) 
    throws  bsh.EvalError
  {
    return i.get(key);
  } // protected static Object set(i, key)

  // @throws  bsh.EvalError
  // @since   1.0
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

  // @throws  GrouperRuntimeException
  // @since   1.0
  protected static GrouperSession getSession(Interpreter i) 
    throws  GrouperRuntimeException
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
      throw new GrouperRuntimeException(e);
    }
  } // protected static GrouperSession getSession(i)

  // @throws  bsh.EvalError
  // @since   1.0
  protected static void set(Interpreter i, String key, Object obj) 
    throws  bsh.EvalError
  {
    i.set(key, obj);  
  } // protected static void set(i, key, obj)

  // @since   1.0
  protected static void setHistory(Interpreter i, int cnt, String cmd) 
    throws  bsh.EvalError
  {
    List history = GrouperShell.getHistory(i);
    history.add(cnt, cmd);
    GrouperShell.set(i, GSH_HISTORY, history);
  } // protected static void setHistory(i, cnt, cmd)


  // PRIVATE INSTANCE METHODS //

  // @throws  bsh.EvalError
  // @since   1.0
  private void _importCommand(String name) 
    throws  bsh.EvalError
  {
    String cmd = "importCommands(\"com.devclue.grouper." + name + "\")";
    this.i.eval(cmd);
  } // private void _importCommand(name)

  //  @since  1.0
  private void _importCommands() 
    throws  bsh.EvalError
  {
    this._importCommand(  "shell"   );
  } // private void _importCommands()

  //  @since  1.0
  private String _prompt() 
    throws  bsh.EvalError,
            IOException
  {
    Object prompt = this.i.eval("getBshPrompt()");
    if (prompt == null) {
      prompt = "%";
    }
    //this.out.print(NAME + "-" + VERSION + " " + this.cnt++ + prompt.toString());
    //return this.in.readLine();
    this.out.print(NAME + "-" + VERSION + " " + this.cnt + prompt.toString());
    String cmd = this.in.readLine();
    GrouperShell.setHistory(this.i, cnt, cmd);
    cnt++;
    return cmd;
  } // private String _prompt()

  //  @since  1.0
  private void _run() 
    throws  bsh.EvalError,
            IOException
  {
    String cmd = new String();
    while ( (cmd = this._prompt()) != null ) {
      if ( cmd.equals("exit") || cmd.equals("quit") ) {
        this._stopSession();
        break;
      }
      try {
        Object obj = this.i.eval(cmd);
        if (obj != null) {
          this.out.println(obj.toString());
        }
      }
      catch (bsh.EvalError eBEE) {
        this.i.error(eBEE.getMessage());
      }
    }
  } // private void _run()

  // @throws  GrouperRuntimeException
  // @since   1.0
  private void _stopSession() 
    throws  GrouperRuntimeException
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
      throw new GrouperRuntimeException(e);
    }
  } // private void _stopSession()


} // public class GrouperShell

