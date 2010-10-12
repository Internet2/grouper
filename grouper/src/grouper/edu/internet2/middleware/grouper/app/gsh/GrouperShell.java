/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportGsh;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportGsh;

/**
 * Grouper Management Shell.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperShell.java,v 1.18 2009-11-02 03:50:51 mchyzer Exp $
 * @since   0.0.1
 */
public class GrouperShell {

  
  /** if we should exist on failure */ 
  static boolean exitOnFailure = false;
  
  private static Map<String, String> mainLookups = new HashMap<String, String>();
  //TODO config file?
  static {
	  mainLookups.put("-xmlimportold", 
			  "edu.internet2.middleware.grouper.xml.XmlImporter");
	  
	  mainLookups.put("-xmlexportold", 
			  "edu.internet2.middleware.grouper.xml.XmlExporter");
	  
    mainLookups.put("-xmlimport", 
      XmlImportGsh.class.getName());

    mainLookups.put("-xmlexport", 
      XmlExportGsh.class.getName());

	  mainLookups.put("-test",      
			  "edu.internet2.middleware.grouper.AllTests");
	  
	  mainLookups.put("-loader",    
			  "edu.internet2.middleware.grouper.app.loader.GrouperLoader");
	  
	  mainLookups.put("-usdu",      
			  "edu.internet2.middleware.grouper.app.usdu.USDU");
	  
	  mainLookups.put("-registry",  
			  "edu.internet2.middleware.grouper.registry.RegistryInitializeSchema");
	  
	  mainLookups.put("-findbadmemberships",  
			  "edu.internet2.middleware.grouper.misc.FindBadMemberships");

    mainLookups.put("-ldappc",  
      "edu.internet2.middleware.ldappc.Ldappc");
    
    mainLookups.put("-ldappcng",
        "edu.internet2.middleware.ldappc.spml.PSPCLI");

  }
  
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

  /** if running from GSH */
  public static boolean runFromGsh = false;
  
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

    //set this and leave it...
    @SuppressWarnings("unused")
    GrouperContext grouperContext = GrouperContext.createNewDefaultContext(
        GrouperEngineBuiltin.GSH, false, true);
    
    boolean wasSpecialCase = handleSpecialCase(args);
    if(wasSpecialCase) {
    	return;
    }
	  runFromGsh = true;
	
    GrouperStartup.runFromMain = true;
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
      grouperShellHelper(args, null);
    }
    catch (GrouperShellException eGS) {
      eGS.printStackTrace();
      LOG.error("GSH is exiting: " + eGS.getMessage(), eGS);
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args)
  
  
 /**
 * Avoid GSH initialisation for special cases 
 * @param args - command line arguments
 * @return whether this was handled as a special case
 */
private static boolean handleSpecialCase(String[] args) {
	  if(args == null || args.length==0) {
		  return false;
	  }
	  if("-h".equalsIgnoreCase(args[0])||"-help".equalsIgnoreCase(args[0])) {
		  System.out.println(_getUsage());
		  return true;
	  }
	  
	  if("-nocheck".equalsIgnoreCase(args[0])) {
		  GrouperStartup.ignoreCheckConfig = true;
		  return false;
	  }
	  String mainClass = mainLookups.get(args[0].toLowerCase());
	  if(mainClass==null) {
		  return false;
	  }
	  String[] mainArgs = new String[args.length -1];
	  for(int i=1;i<args.length;i++) {
		  mainArgs[i-1] = args[i];
	  }
	  
	  try {
		  Class claz = Class.forName(mainClass);
		  
		  Method method = claz.getMethod("main", String[].class);
		  method.invoke(null, (Object)mainArgs);
	  }catch(Exception e) {
	    if (ExceptionUtils.getFullStackTrace(e).contains("PSPCLI")) {
	      String error = "Make sure you have run 'ant dist' in ldappcng, and 'ant ldappcng' in grouper to copy the libs over";
        LOG.fatal(error);
        System.err.println(error);
	    }
		  if(e instanceof RuntimeException) {
			  throw (RuntimeException)e;
		  }
		  throw new RuntimeException(e);
	  }
	  
	  return true;
  } //private static boolean handleSpecialCase(args)
 
  /**
   * helper method to kick off GSH without exiting
   * @param args
   * @param inputStreamParam if passing in an inputStream
   * @throws GrouperShellException
   */
  static void grouperShellHelper(String args[], InputStream inputStreamParam) throws GrouperShellException {
    
    System.out.println("Type help() for instructions");
    
    GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GSH);
    
    new GrouperShell( new ShellCommandReader(args, inputStreamParam )).run();
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
      if (s != null && s.getSubjectDb() == null) {
        s = null;
        GrouperShell.set(i, GSH_SESSION, s);
      }
      if (s == null) {
        s = GrouperSession.staticGrouperSession(false);
        
        if (s == null) {
          s = GrouperSession.start(
            SubjectFinder.findRootSubject()
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
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.attr.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.attr.assign.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.attr.finder.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.attr.value.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.audit.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.externalSubjects.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.group.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.app.loader.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.xml.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.registry.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.app.usdu.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.app.gsh.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.app.misc.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.privs.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.rules.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.misc.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.hibernate.*;");
      this.interpreter.eval(  "import edu.internet2.middleware.grouper.util.*;");
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
  private static final Log LOG = GrouperUtil.getLog(ShellHelper.class);

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
  
  private static String _getUsage() {
	    return  "Usage:"                                                                   + GrouperConfig.NL
	            + "args: -h,               Prints this message"                            + GrouperConfig.NL
	            + "args: <filename>,       Execute commands in specified file"             + GrouperConfig.NL
	            + "no args:                Enters an interactive shell"                    + GrouperConfig.NL
	            + "args: -nocheck,         Skips startup check and enters an "          + GrouperConfig.NL
	            + "                        interactive shell"                              + GrouperConfig.NL
	            + "args: -runarg <command> Run command (use \\\\n to separate commands)"   + GrouperConfig.NL
	            + "args: -main <class> [args...]                                    "      + GrouperConfig.NL
	            + "   class,               Full class name (must have main method)"        + GrouperConfig.NL
	            + "   args,                args as required by main method of class"       + GrouperConfig.NL
	            + "args: -initEnv [<configDir>]"                                           + GrouperConfig.NL
	            + "       On Windows sets GROUPER_HOME and adds GROUPER_HOME/bin to path"  + GrouperConfig.NL
	            + "       For *nix 'source gsh.sh' for the same result"                    + GrouperConfig.NL
	            + "       configDir optionally adds an alternative conf directory than"    + GrouperConfig.NL 
	            + "       GROUPER_HOME/conf to the classpath"                              + GrouperConfig.NL
	            + "args: (-xmlimport | -xmlexport | -loader | -test | -registry | -usdu |"   + GrouperConfig.NL
	            + "       -findbadmemberships | -ldappc) "
	            + "                        Enter option to get additional usage for that " + GrouperConfig.NL
	            + "                        option "                                        + GrouperConfig.NL
	            
	            + "  -xmlimport,           Invokes XmlImporter*"                           + GrouperConfig.NL
	            + "                        *XML format has changed in v1.6. To import"     + GrouperConfig.NL
	            + "                        the original XML format use -xmlimportold"      + GrouperConfig.NL
	            + "  -xmlexport,           Invokes XmlExporter"                            + GrouperConfig.NL
	            + "  -loader,              Invokes GrouperLoader"                          + GrouperConfig.NL
	            + "  -registry,            Manipulate the Grouper schema and install"      + GrouperConfig.NL
	            + "                        bootstrap data"                                 + GrouperConfig.NL
	            + "  -test,                Run JUnit tests"                                + GrouperConfig.NL
	             
	            + "  -usdu,                Invoke USDU - Unresolvable Subject Deletion "   + GrouperConfig.NL
	            + "                        Utility"                                        + GrouperConfig.NL
	           
	            + "  -findbadmemberships,  Check for membership data inconsistencies    "  + GrouperConfig.NL
                + "  -ldappc,              Run the grouper ldap provisioning connector to send data to ldap    "  + GrouperConfig.NL
	            ;
	  } // private static String _getUsage()

} // public class GrouperShell

