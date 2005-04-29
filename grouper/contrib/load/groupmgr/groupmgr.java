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
 * Program to add and delete groups and memberships using the 
 * {@link Grouper} API.
 * <p />
 * See <i>README</i> for more information.
 * 
 * @author  blair christensen.
 * @version $Id: groupmgr.java,v 1.11 2005-04-29 17:52:33 blair Exp $ 
 */
class groupmgr {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String NAME = "groupmgr";


  /*
   * PRIVATE CLASS VARIABLES
   */
  private static boolean        actUponG;
  private static boolean        actUponM;
  private static boolean        actUponNS;
  private static CommandLine    cmd;
  private static String         extn;
  private static GrouperMember  mem;
  private static String         member;
  private static boolean        memberIsGroup = false;
  private static Options        options;
  private static String         path;
  private static GrouperSession s;
  private static String         stem;
  private static Subject        subj;
  private static String         subjectID;
  private static boolean        toAdd;
  private static boolean        toDel;
  private static boolean        verbose = false;


  /*
   * PUBLIC CLASS METHODS
   */
  public static void main(String[] args) {
    _opts(args);      // Parse and handle command line options
    _grouperStart();  // Initialize Grouper and start session
    boolean rv = _dispatch();   // Take action
    _grouperStop();   // And we're done.  Tidy up.
    if (rv == true) {
      System.exit(0);
    } else {
      System.exit(1);
    }
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /* (!javadoc)
   * Determine what it is that has been asked of us
   */
  private static boolean _dispatch() {
    boolean rv = false;
    if (toAdd == true) {
      if        (actUponG  == true)  {
        rv = _groupAdd();
      } else if (actUponM  == true) {
        rv = _memberAdd();
      } else if (actUponNS == true) {
        rv = _stemAdd();
      } else {
        System.err.println("No additions specified!");
        _usage();
      }
    } else if (toDel == true) {
      if        (actUponG  == true) {
        rv = _groupDel(); 
      } else if (actUponM  == true) {
        rv = _memberDel();
      } else if (actUponNS == true) {
        System.err.println("Namespace deletions not supported!");
        _usage();
      } else {
        System.err.println("No deletions specified!");
        _usage();
      }
    } else {
      System.err.println("No actions specified!"); 
      _usage();
    }
    return rv;
  }

  /* (!javadoc)
   * Add a group to the registry.
   */
  private static boolean _groupAdd() {
    boolean rv = false;
    if ( (stem != null) && (extn != null) ) {
      stem = _translateRoot(stem);
      try {
        GrouperGroup g = GrouperGroup.create(
                           s, stem, extn, Grouper.DEF_GROUP_TYPE
                         );
        _verbose("Added group `" + g.name() + "'");
        rv = true;
      } catch (RuntimeException e) {
        System.err.println(
          "Failed to add group `" + 
          GrouperGroup.groupName(stem, extn) + "': " + e
        );
      }
    }
    return rv;
  }

  /* (!javadoc)
   * Delete a group from the registry.
   */
  private static boolean _groupDel() {
    boolean rv = false;
    if ( (stem != null) && (extn != null) ) {
      stem = _translateRoot(stem);
      GrouperGroup g = GrouperGroup.load(
                         s, stem, extn, Grouper.DEF_GROUP_TYPE
                       );
      if (g != null) {
        try {
          GrouperGroup.delete(s, g);
          _verbose("Deleted group `" + g.name() + "'");
          rv = true;
        } catch (RuntimeException e) {
          System.err.println("FUCK! " + e);
        }
      }
    }  
    if (rv != true) {
      System.err.println(
        "Failed to delete group `" + 
        GrouperGroup.groupName(stem, extn) + "'"
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
  private static boolean _memberAdd() {
    boolean rv = false;
    if ( (stem != null) && (extn != null) && (mem != null) ) {
      stem = _translateRoot(stem);
      GrouperMember m     = null;
      String        sid   = null;
      String        stid  = null;
      if (memberIsGroup) {
        GrouperGroup mg = GrouperGroup.loadByName(s, member);
        if (mg != null) {
          sid   = mg.id();
          stid  = "group";
        }
      } else {
        sid   = member;
        stid  = Grouper.DEF_SUBJ_TYPE;
      }
      // Load the member
      m = GrouperMember.load(s, sid, stid);
      if (m != null) {
        // Load the group
        GrouperGroup g = GrouperGroup.load(s, stem, extn);
        if (g != null) {
          try {
            g.listAddVal(m);
            rv = true;
            _verbose("Added `" + member + "' to `" + g.name() + "'");
          } catch (RuntimeException e) {
            System.err.println(e);
          }
        }
      }
    }
    if (rv != true) {
      System.err.println(
        "Failed to add `" + member + "' to `" + 
        GrouperGroup.groupName(stem, extn) + "'"
      );
    }
    return rv;
  }

  /* (!javadoc)
   * Delete a member from the registry.
   */
  private static boolean _memberDel() {
    boolean rv = false;
    if ( (stem != null) && (extn != null) && (mem != null) ) {
      stem = _translateRoot(stem);
      GrouperMember m     = null;
      String        sid   = null;
      String        stid  = null;
      if (memberIsGroup) {
        GrouperGroup mg = GrouperGroup.loadByName(s, member);
        if (mg != null) {
          sid   = mg.id();
          stid  = "group";
        }
      } else {
        sid   = member;
        stid  = Grouper.DEF_SUBJ_TYPE;
      }
      // Load the member
      m = GrouperMember.load(s, sid, stid);
      if (m != null) {
        // Load the group
        GrouperGroup g = GrouperGroup.load(s, stem, extn);
        if (g != null) {
          try {
            g.listDelVal(m);
            rv = true;
            _verbose("Deleted `" + member + "' from `" + g.name() + "'");
          } catch (RuntimeException e) {
            System.err.println(e);
          }
        }
      }
    }
    if (rv != true) {
      System.err.println(
        "Failed to delete `" + member + "' from `" + 
        GrouperGroup.groupName(stem, extn) + "'"
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
    options.addOption("a", false, "Add Mode");
    options.addOption("d", false, "Delete Mode");
    options.addOption(
                      OptionBuilder.withArgName("extension")
                        .withDescription("Specify extension to act upon")
                        .hasArg()
                        .create("e")
                     );
    options.addOption("G", false, "Treat argument to -m as a group name");
    options.addOption("g", false, "Act upon a group");
    options.addOption("h", false, "Print usage information");
    options.addOption(
                      OptionBuilder.withArgName("member")
                        .withDescription("Specify member to act upon")
                        .hasArg()
                        .create("m")
                     );
    options.addOption("n", false, "Act upon a namespace");
    options.addOption(
                      OptionBuilder.withArgName("subject")
                        .withDescription("Specify subject to act as")
                        .hasArg()
                        .create("S")
                     );
    options.addOption(
                      OptionBuilder.withArgName("stem")
                        .withDescription("Specify stem to act upon")
                        .hasArg()
                        .create("s")
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
    if (cmd.hasOption("a")) {
      toAdd   = true;
      _verbose("Enabling add mode");
    } 
    if (cmd.hasOption("d")) {
      toDel   = true;
      _verbose("Enabling delete mode");
    } 
    if (cmd.hasOption("e")) {
      extn = cmd.getOptionValue("e");
      _verbose("Using extension '" + extn + "'");
    } 
    if (cmd.hasOption("G")) {
      memberIsGroup = true;
      _verbose("Will treat -m value as a group");
    } 
    if (cmd.hasOption("g")) {
      actUponG = true;
      _verbose("Will act upon a group");
    } 
    if (cmd.hasOption("m")) {
      actUponM  = true;
      member    = cmd.getOptionValue("m");
      _verbose("Will act upon a member `" + member + "'");
    } 
    if (cmd.hasOption("n")) {
      actUponNS = true;
      _verbose("Will act upon a namespace");
    } 
    if (cmd.hasOption("S")) {
      subjectID = cmd.getOptionValue("S");
      _verbose("Using subjectID '" + subjectID + "'");
    } 
    if (cmd.hasOption("s")) {
      stem = cmd.getOptionValue("s");
      _verbose("Using stem '" + stem + "'");
    }
  }

  /* (!javadoc)
   * Add a group to the registry.
   */
  private static boolean _stemAdd() {
    boolean rv = false;
    if ( (stem != null) && (extn != null) ) {
      stem = _translateRoot(stem);
      try {
        GrouperStem ns = GrouperStem.create(s, stem, extn);
        _verbose("Added stem `" + ns + "'");
        rv = true;
      } catch (RuntimeException e) {
        System.err.println(
          "Failed to add stem `" + 
          GrouperGroup.groupName(stem, extn) + "': " + e
        );
      }
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
   * Interpolate, if necessary, the namespace root
   */
  private static String _translateRoot(String stem) {
    // TODO Bah.  I have to interpolate.
    if (stem.equals("Grouper.NS_ROOT")) {
      stem = Grouper.NS_ROOT;
    }
    return stem;
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

