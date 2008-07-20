/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InternalSourceAdapter;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Grouper Management Shell.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperShell.java,v 1.2 2008-07-20 21:18:54 mchyzer Exp $
 * @since   0.0.1
 */
public class GrouperShell {

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

    Properties grouperHibernateProperties = GrouperUtil.propertiesFromResourceName("grouper.hibernate.properties");
    File propertiesFile = GrouperUtil.fileFromResourceName("grouper.hibernate.properties");
    String url = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.url"));
    String user = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.username"));
    String propertiesFileLocation = propertiesFile == null ? " [cant find grouper.hibernate.properties]" :
      propertiesFile.getAbsolutePath();
    System.out.println("Connecting to: " + user + "@" + url + "\n    based on " + propertiesFileLocation);
    
    GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GSH);
    
    try {
      new GrouperShell( new ShellCommandReader(args) ).run();
    }
    catch (GrouperShellException eGS) {
      System.err.println(eGS.getMessage());
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args)


  // CONSTRUCTORS //

  // @since   0.1.1
  protected GrouperShell(CommandReader r) 
    throws  GrouperShellException
  {
    this.i  = r.getInterpreter();
    this.r  = r;
  } // protected GrouperShell()


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
  protected static void setOurCommand(Interpreter i, boolean b) {
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
    while ( ( cmd = r.getNext() ) != null) {
      if ( this._isComment(cmd) ) {
        continue;
      }
      if ( this._isTimeToExit(cmd) ) {
        this._stopSession();
        break;
      }
      // Now try to eval the command
      cmd = ShellHelper.eval(i, cmd);
    }
  } // protected void run()


  // PRIVATE CLASS METHODS //

  // @since   0.0.1
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
      GrouperSession s = (GrouperSession) GrouperShell.get(this.i, GSH_SESSION);
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

