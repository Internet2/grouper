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
 * <p>FIXME Move to `package.html`</p>
 * <h3>Supported Commands</h3>
 * <ul>
 *   <li><b>addComposite(group, type, left group, right group)</b> - Add
 *     composite membership.</li>
 *   <li><b>addGroup(parent, extension, displayExtension)</b> - Add
 *     group beneath <i>parent</i> stem with the specified <i>extension</i>
 *     and <i>displayExtension</i>.</li>
 *   <li><b>addMember(group, subject id)</b> - Add subject as a member
 *     to the group.</li>
 *   <li><b>addRootStem(extension, displayExtension)</b> - Add root
 *     stem with the specified <i>extension</i> and <i>displayExtension</i>.</li>
 *   <li><b>addStem(parent, extension, displayExtension)</b> - Add stem
 *     beneath <i>parent</i> stem with the specified <i>extension</i>
 *     and <i>displayExtension</i>.</li>
 *   <li><b>addSubject(id, type, name)</b> - Add a {@link HibernateSubject}
 *     to the Groups Registry.</li>
 *   <li><b>delComposite(group)</b> - Delete composite membership from the
 *     specified group.</li>
 *   <li><b>delGroup(name)</b> - Delete group with the specified
 *     <i>name</i>.</li>
 *   <li><b>delMember(group, subject id)</b> - Remove subject as a
 *     member of the group.</li>
 *   <li><b>delStem(name)</b> - Delete stem with the specified
 *     <i>name</i>.</li>
 *   <li><b>exit</b> - Terminate shell.</li>
 *   <li><b>findSubject(id)</b> - Find a subject.</li>
 *   <li><b>findSubject(id, type)</b> - Find a subject.</li>
 *   <li><b>findSubject(id, type, source)</b> - Find a subject.</li>
 *   <li><b>getGroupAttr(stem, attr)</b> - Get value of <i>group</i>'s
 *     <i>attr</i> attribute.</li>
 *   <li><b>getGroups(name)</b> - Find all groups with <i>name</i> in any
 *     naming attribute value.</li>
 *   <li><b>getMembers(group)</b> - Get members of the group.</li>
 *   <li><b>getSources()</b> - Find all Subject sources.</li>
 *   <li><b>getStemAttr(stem, attr)</b> - Get value of <i>stem</i>'s
 *     <i>attr</i> attribute.</li>
 *   <li><b>getStems(name)</b> - Find all stems with <i>name</i> in any
 *     naming attribute value.</li>
 *   <li><b>hasMember(group, subject id)</b> - Is subject a member of
 *   this group.</li>
 *   <li><b>history()</b> - Print commands that have been run.</li>
 *   <li><b>history(n)</b> - Print the last <i>n</i> commands that
 *     have been run.</li>
 *   <li><b>last()</b> - Run the last command executed.</li>
 *   <li><b>last(n)</b> - Execute command number <i>n</i>.</li>
 *   <li><b>p(command)</b> - Pretty print results.  This command is
 *     more useful when <i>GSH_DEVEL</i> is enabled.</li>
 *   <li><b>quit</b> - Terminate shell.</li>
 *   <li><b>resetRegistry()</b> - Restore the Groups Registry to a
 *     default state.</li>
 *   <li><b>setGroupAttr(stem, attr, value)</b> - Set value of <i>group</i>'s 
 *     <i>attr</i> attribute.</li>
 *   <li><b>setStemAttr(stem, attr, value)</b> - Set value of <i>stem</i>'s 
 *     <i>attr</i> attribute.</li>
 *   <li><b>version()</b> - Return version information.</li>
 * </ul> 
 * <h3>Variables</h3>
 * <ul>
 *  <li><b>GSH_DEBUG</b> - If set to <i>true</i>, stack traces will be printed
 *    upon failure.</li>
 *  <li><b>GSH_DEVEL</b> - If set to <i>true</i>, commands will return
 *    objects that can be manipulated rather than printing out summaries
 *    of the returned objects.</li>
 *  <li><b>GSH_TIMER</b> - If set to <i>true</i> the time taken to
 *    evaluate each command will be displayed.</li>
 * </ul>
 * @author  blair christensen.
 * @version $Id: GrouperShell.java,v 1.3 2006-06-27 19:28:29 blair Exp $
 * @since   0.0.1
 */
public class GrouperShell {

  // PROTECTED CLASS CONSTANTS //
  protected static final String NAME        = "gsh";
  protected static final String VERSION     = "0.0.1";


  // PRIVATE CLASS CONSTANTS //
  private static final String GSH_DEBUG   = "GSH_DEBUG";
  private static final String GSH_DEVEL   = "GSH_DEVEL";
  private static final String GSH_HISTORY = "_GSH_HISTORY";
  private static final String GSH_OURS    = "_GSH_OURS";
  private static final String GSH_SESSION = "_GSH_GROUPER_SESSION";
  private static final String GSH_TIMER   = "GSH_TIMER";


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

  // @since   0.0.1
  protected static void error(Interpreter i, Exception e) {
    error(i, e, e.getMessage());
  } // protected static void error(i, e)

  // @since   0.0.1
  protected static void error(Interpreter i, Exception e, String msg) {
    i.error(msg);
    if (isDebug(i)) {
      e.printStackTrace();
    }
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

  // @throws  bsh.EvalError
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

  // @throws  bsh.EvalError
  // @since   0.0.1
  protected static void setHistory(Interpreter i, int cnt, String cmd) 
    throws  bsh.EvalError
  {
    List history = GrouperShell.getHistory(i);
    history.add(cnt, cmd);
    GrouperShell.set(i, GSH_HISTORY, history);
  } // protected static void setHistory(i, cnt, cmd)

  // @since   0.0.1
  protected static void setOurCommand(Interpreter i, boolean b) {
    try {
      GrouperShell.set(i, GSH_OURS, b);
    }
    catch (bsh.EvalError eBEE) {
      i.error(eBEE.getMessage());
    }
  } // protected static void setOurCommand(i, b)


  // PRIVATE CLASS METHODS //

  // @since   0.0.1
  private static boolean _isTrue(Interpreter i, String var) {
    boolean rv = false;
    try {
      Object  obj = GrouperShell.get(i, var);
    if (
                (obj != null)
            &&  (obj instanceof Boolean)
            &&  (obj.equals(Boolean.TRUE))
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
      // Now try to eval the command
      cmd = ShellHelper.eval(i, cmd);
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

