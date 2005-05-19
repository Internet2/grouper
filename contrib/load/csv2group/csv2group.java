/*
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 *
 * You may use and distribute under the same terms as Grouper itself
 */


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.util.*;
import  org.apache.commons.cli.*;


/**
 * Sample loader for adding groups and memberships to the registry
 * using the {@link Grouper} API.
 * <p />
 * See <i>README</i> for more information.
 * 
 * @author  blair christensen.
 * @version $Id: csv2group.java,v 1.24 2005-05-19 16:33:20 blair Exp $ 
 */
class csv2group {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String NAME = "csv2group";


  /*
   * PRIVATE CLASS VARIABLES
   */
  private static CommandLine    cmd;
  private static boolean        continueOnError = false;
  private static GrouperMember  mem;
  private static Options        options;
  private static String         path;
  private static boolean        quiet = false;
  private static GrouperSession s;
  private static Subject        subj;
  private static String         subjectID;
  private static boolean        verbose = false;
  private static int            exitCode = 0;


  /*
   * PUBLIC CLASS METHODS
   */
  public static void main(String[] args) {
    _opts(args);            // Parse and handle command line options
    _grouperStart();        // Initialize Grouper and start session
    _csvReadAndExecute();   // Read and parse input file
    _grouperStop();         // And we're done.  Tidy up.
    System.exit(exitCode);
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /* (!javadoc)
   * Read in CSV file and execute accordingly
   */
  private static void _csvReadAndExecute() {
    if (path != null) {
      try { 
        BufferedReader  br    = new BufferedReader(new FileReader(path));
        String          line  = null; 
        while ((line=br.readLine()) != null){ 
          if (line.startsWith("#")) {
            continue; // Ignore comments  
          }
          StringTokenizer st = new StringTokenizer(line, ",");
          /*
           * Trust the dispatch engine to DTRT.
           * Right now, if an action receives more tokens than it
           * wants, it silently ignores them.  I can't decide if I want
           * to change that or not.
           */
          if ( (st.countTokens() > 3) && (st.countTokens() < 7) ) {
            List tokens = new ArrayList();
            while (st.hasMoreTokens()) {
              tokens.add(st.nextToken());
            }
            _verbose("Received tokens " + tokens);
            if (!_dispatch(tokens)) {
              _verbose("Error dispatching '" + line + "'");
              if (continueOnError != true) {
                exitCode = 1;
                break;
              }
            }
          } else {
            System.err.println("Skipping, invalid format: '" + line + "'");
          }
        } 
        br.close(); 
      } catch (IOException e) { 
        System.err.println("Error processing '" + path + "': " + e);
      }
    } else {
      _usage();
    }
  }

  /* (!javadoc)
   * CSV Dispatch Handler
   */
  private static boolean _dispatch(List tokens) {
    boolean rv = false;
    String cat = (String) tokens.remove(0);
    if        (cat.equals("group"))   {
      rv = _dispatchGroup(tokens);
    } else if (cat.equals("member"))  {
      rv = _dispatchMember(tokens);
    } else if (cat.equals("stem"))    {
      rv = _dispatchStem(tokens);
    } else {
      System.err.println("ERROR: Invalid category: `" + cat + "'");
    }
    return rv;
  }

  /* (!javadoc)
   * Group Dispatch Handler
   */
  private static boolean _dispatchGroup(List tokens) {
    boolean rv = false;
    String act = (String) tokens.remove(0);
    if (act.equals("add")) {
      rv = _groupAdd(tokens);
    } else {
      System.err.println("ERROR: Invalid group action: `" + act + "'");
    }
    return rv;
  }

  /* (!javadoc)
   * Member Dispatch Handler
   */
  private static boolean _dispatchMember(List tokens) {
    boolean rv = false;
    String act = (String) tokens.remove(0);
    if (act.equals("add")) {
      rv = _memberAdd(tokens);
    } else {
      System.err.println("ERROR: Invalid member action: `" + act + "'");
    }
    return rv;
  }

  /* (!javadoc)
   * Stem Dispatch Handler
   */
  private static boolean _dispatchStem(List tokens) {
    boolean rv = false;
    String act = (String) tokens.remove(0);
    if (act.equals("add")) {
      rv = _stemAdd(tokens);
    } else {
      System.err.println("ERROR: Invalid stem action: `" + act + "'");
    }
    return rv;
  }

  /* (!javadoc)
   * Add a group to the registry.
   */
  private static boolean _groupAdd(List tokens) {
    boolean rv = false;
    String stem = (String) tokens.get(0);
    String extn = (String) tokens.get(1);
    try {
      GrouperGroup g = GrouperGroup.create(
                         s, stem, extn, Grouper.DEF_GROUP_TYPE
                       );
      _report("Added group: " + g.name());
      rv = true;
    } catch (RuntimeException e) {
      System.err.println("Failed to add group " + stem + ":" + extn + ": " + e);
    }
    return rv;
  }

  /* (!javadoc)
   * Initialize the {@link Grouper} environment and start a 
   * {@link GrouperSession}.
   */
  private static void _grouperStart() {
    _subject();
    s = GrouperSession.start(subj);
    _verbose(
             "Started session as "           + 
             subj.getId() + ":"              +
             subj.getSubjectType().getId()
            );
    mem = GrouperMember.load(s, subj);
    _verbose("Loaded member " + mem.subjectID() + ":" + mem.typeID());
  }

  /* (!javadoc)
   * Stop the {@link Grouper} session.
   */
  private static void _grouperStop() {
    s.stop();
  }

  /* (!javadoc)
   * Add a member to the registry.
   */
  private static boolean _memberAdd(List tokens) {
    boolean rv = false;
    String stem = (String) tokens.get(0);
    String extn = (String) tokens.get(1);
    String name = null; // For pretty printing
    String sid  = null;
    String stid = Grouper.DEF_SUBJ_TYPE;
    if        (tokens.size() == 3)  {
      // Adding a non-group as a member
      sid  = (String) tokens.get(2);
      name = sid;
    } else if (tokens.size() > 1)   {
      // Adding a group as a member

      // Ye Olde Silent Ignore Trick
      String mS = (String) tokens.get(2);
      String mE = (String) tokens.get(3);
      GrouperGroup mAsG = GrouperGroup.load(s, mS, mE);
      if (mAsG != null) {
        name  = mAsG.name();
        sid   = mAsG.id();
        stid  = "group";
      } else {
        System.err.println("Unable to fetch member group!");
      }
    }
    // Load the group
    GrouperGroup g = GrouperGroup.load(s, stem, extn);
    if (g != null) {
      // Load the member
      GrouperMember m = GrouperMember.load(s, sid, stid);
      if (m != null) {
        try {
          g.listAddVal(m);
          rv = true;
          _report("Added member: " + name + " to " + g.name());
        } catch (RuntimeException e) {
          System.err.println(e);
          rv = false;
        }
      }
    }
    if (rv != true) {
      _report("Failed to add member " + name + " to " + stem + ":" + extn);
    }
    return rv;
  }

  /* (!javadoc)
   * Handle command line options.
   */
  private static void _opts(String[] args) {
    _optsParse(args); // Parse CLI options
    _optsProcess();   // Handle CLI options
  }

  /* (!javadoc)
   * Parse command line options.
   * <p />
   * @param   String array.
   */
  private static void _optsParse(String[] args) {
    options = new Options();
    options.addOption("c", false, "Continue on operation error");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("file");
    OptionBuilder.withDescription("Specify input file [REQUIRED]");
    options.addOption( OptionBuilder.create("f") );
    options.addOption("h", false, "Print usage information");
    options.addOption("q", false, "Be more quiet");
    options.addOption("v", false, "Be more verbose [Overrides -q]");
    CommandLineParser parser = new PosixParser();
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Unable to parse command line options: " + e.getMessage());
      System.exit(1);
    }
  }

  /* (!javadoc)
   *
   * Process command line options.
   */
  private static void _optsProcess() {
    if (cmd == null) {
      System.err.println("Error parsing command line options!");
      System.exit(1);
    }
    // Handle help first
    if (cmd.hasOption("h")) {
      _usage();
      System.exit(0);
    }
    // And then verbose because it may affect output later in this
    // method
    if (cmd.hasOption("v")) {
      verbose = true;
      _verbose("Enabling verbose mode");
    }
    // And now everything else
    if (cmd.hasOption("c")) {
      continueOnError = true;
      _verbose("Enabling continue on operation error mode");
    }
    if (cmd.hasOption("f")) {
      path = cmd.getOptionValue("f");
      _verbose("Using input file '" + path + "'");
    }
    if (cmd.hasOption("q")) {
      quiet = true;
      _verbose("Enabling quiet mode");
    }
  }

  /* (!javadoc)
   *
   * Conditionally print default messages.
   * <p />
   * @param msg Message to print if not running in quiet mode.
   */
  private static void _report(String msg) {
    if (quiet == true) {
      _verbose(msg);
    } else {
      System.err.println(msg);
    }
  }

  /* (!javadoc)
   * Add a group to the registry.
   */
  private static boolean _stemAdd(List tokens) {
    boolean rv = false;
    String stem = (String) tokens.remove(0);
    String extn = (String) tokens.remove(0);
    // TODO Bah.  I have to do the interpolation for the config file.
    if (stem.equals("Grouper.NS_ROOT")) {
      stem = Grouper.NS_ROOT;
    }
    try {
      GrouperStem ns = GrouperStem.create(s, stem, extn);
      _report("Added stem: " + ns.name());
      rv = true;
    } catch (RuntimeException e) {
      _report("Failed to add stem " + stem + ":" + extn + ": " + e);
    }
    return rv;
  }

  /* (!javadoc)
   * Instantiate a subject via the {@link Subject} interface.
   */
  private static void _subject() {
    if (subjectID == null) {
      _verbose("Using default subjectID");
      subjectID = Grouper.config("member.system");
      if (subjectID == null) {
        System.err.println("Unable to retrieve default subjectID!");
        System.exit(1);
      }
    }
    _verbose("Using default subjectTypeID (" + Grouper.DEF_SUBJ_TYPE + ")");
    _verbose("Looking up subjectID '" + subjectID + "'");
    subj = GrouperSubject.load(subjectID, Grouper.DEF_SUBJ_TYPE);
  }

  /* (!javadoc)
   *
   * Print usage information.
   */
  private static void _usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(NAME, options);
  }

  /* (!javadoc)
   *
   * Conditionally print verbose messages.
   * <p />
   * @param msg Message to print if running verbosely.
   */
  private static void _verbose(String msg) {
    if (verbose == true) {
      System.err.println(msg);
    }
  }

}

