/*
 * Copyright (C) 2004 The University Of Chicago
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
 * Program to add and delete groups, memberships, and attributes using
 * the {@link Grouper} API.
 * <p />
 * See <i>README</i> for more information.
 * 
 * @author  blair christensen.
 * @version $Id: groupmgr.java,v 1.2 2004-12-08 03:53:59 blair Exp $ 
 */
class groupmgr {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String NAME = "groupmgr";


  /*
   * PRIVATE CLAS VARIABLES
   */
  private static CommandLine    cmd;
  private static GrouperMember  mem;
  private static Options        options;
  private static String         path;
  private static GrouperSession s;
  private static Subject        subj;
  private static String         subjectID;
  private static boolean        verbose = false;


  /*
   * PUBLIC CLASS METHODS
   */
  public static void main(String[] args) {
    _opts(args);            // Parse and handle command line options
    _grouperStart();        // Initialize Grouper and start session
    _grouperStop();         // And we're done.  Tidy up.
    System.exit(0);
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /* (!javadoc)
   * Add a group to the registry.
   */
  private static boolean _groupAdd(List tokens) {
    boolean rv = false;
    String stem = (String) tokens.get(0);
    String extn = (String) tokens.get(0);
    GrouperGroup g = GrouperGroup.create(
                       s, stem, extn, Grouper.DEF_GROUP_TYPE
                     );
    if (g != null) {
      _verbose("Added group: " + g);
      rv = true;
    } else {
      System.err.println(
        "Failed to add group=`" + stem + ", extn=`" + extn + "'"
      );
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
    mem = GrouperMember.load(subj);
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
    String sid  = null;
    String stid = Grouper.DEF_SUBJ_TYPE;
    if        (tokens.size() == 3)  {
      sid = (String) tokens.get(2);
    } else if (tokens.size() > 1)   {
      // Ye Olde Silent Ignore Trick
      String mS = (String) tokens.get(2);
      String mE = (String) tokens.get(3);
      GrouperGroup mAsG = GrouperGroup.load(s, mS, mE);
      if (mAsG != null) {
        sid   = mAsG.id();
        stid  = "group";
      } else {
        System.err.println("Unable to fetch member group!");
      }
    }
    // Load the subject
    Subject subj = GrouperSubject.load(sid, stid);
    if (subj != null) {
      // Load the group
      GrouperGroup g = GrouperGroup.load(s, stem, extn);
      if (g != null) {
        // Load the member
        GrouperMember m = GrouperMember.load(subj);
        if (m != null) {
          if (g.listAddVal(s, m)) {
            rv = true;
            System.err.println(
              "Added `" + sid + "' to `" + stem + "':`" + extn + "'"
            );
          }
        }
      }
    }
    if (rv != true) {
      System.err.println(
        "Failed to add `" + sid + "' to `" + stem + "':`" + extn + "'"
      );
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
    options.addOption("h", false, "Print usage information");
    options.addOption(
                      OptionBuilder.withArgName("subject")
                        .withDescription("Specify subject to act as")
                        .hasArg()
                        .create("S")
                     );
    options.addOption("v", false, "Be more verbose");
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
    if (cmd.hasOption("S")) {
      subjectID = cmd.getOptionValue("S");
      _verbose("Using subjectID '" + subjectID + "'");
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
    GrouperGroup g = GrouperGroup.create(s, stem, extn, Grouper.NS_TYPE);
    if (g != null) {
      _verbose("Added stem: " + g);
      rv = true;
    } else {
      System.err.println(
        "Failed to add stem=`" + stem + ", extn=`" + extn + "'"
      );
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
   * Conditionally print messages depending upon verbosity level.
   * <p />
   * @param   msg Message to print if running verbosely.
   */
  private static void _verbose(String msg) {
    if (verbose == true) {
      System.err.println(msg);
    }
  }

}

