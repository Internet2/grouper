/*
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * You may use and distribute under the same terms as Grouper itself
 */


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  org.apache.commons.cli.*;


/**
 * This program demonstrates how to use the {@link Grouper} API to
 * search the {@link Grouper} group registry.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: grouperq.java,v 1.7 2004-12-08 01:11:19 blair Exp $
 */
class grouperq {

  /*
   * PRIVATE CLASS CONSTANTS
   */

  private static final String NAME = "grouperq";


  /*
   * PRIVATE CLASS VARIABLES
   */
 
  private static CommandLine    cmd;
  private static String         field;
  private static GrouperMember  mem;
  private static GrouperSession s;
  private static Options        options;
  private static Subject        subj;  
  private static String         subjectID;
  private static boolean        verbose     = false;
  

  /*
   * PUBLIC CLASS METHODS
   */
  public static void main(String[] args) {
    _opts(args);      // Parse and handle command line options
    _grouperStart();  // Initialize Grouper and start session
    List vals = _grouperQuery();  // Perform a naive query
    _report(vals);    // Now spew the results
    _grouperStop();   // And we're done.  Tidy up.
    System.exit(0);
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /* (!javadoc)
   *
   * Run query against the group registry.
   * <p />
   * @return  List of query results.
   */
  private static List _grouperQuery() {
    List vals = new ArrayList();
    if (field == null) {
      _verbose("Using default group field (" + Grouper.DEF_LIST_TYPE + ")");
      field = Grouper.DEF_LIST_TYPE;
    }
    _verbose("Looking up field '" + field + "'");
    vals = mem.listVals(s, field);
    return vals;
  } 

  /* (!javadoc)
   *
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
   *
   * Stop the {@link Grouper} session.
   */
  private static void _grouperStop() {
    s.stop();
  }

  /* (!javadoc)
   *
   * Handle command line options.
   * <p />
   * @param   String Array.
   */
  private static void _opts(String[] args) {
    _optsParse(args); // Parse CLI options
    _optsProcess();   // Handle CLI options
  }

  /* (!javadoc)
   *
   * Parse command line options.
   * <p />
   * @param   String array.
   */
  private static void _optsParse(String[] args) {
    options = new Options();
    options.addOption(
                      OptionBuilder.withArgName("field")
                                   .withDescription("Specify group field to query on")
                                   .hasArg()
                                   .create("f")
                     );
    options.addOption("h", false, "Print usage information");
    options.addOption(
                      OptionBuilder.withArgName("subject")
                                   .withDescription("Specify subject to query as")
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
    if (cmd.hasOption("f")) {
      field = cmd.getOptionValue("f");
      _verbose("Using field '" + field + "'");
    }
    if (cmd.hasOption("S")) {
      subjectID = cmd.getOptionValue("S");
      _verbose("Got subject '" + subjectID + "'");
    }
  }

  /* (!javadoc)
   *
   * Present the query results.
   * <p />
   * @param   vals  List of values to report on.
   */
  private static void _report(List vals) {
    _verbose("Results returned by query: " + vals.size());
    _reportGroups(vals);
  }

  /* (!javadoc)
   * Report on groups within search results
   */
  private static void _reportGroups(List vals) {
    if (vals.size() > 0) {
      System.out.println("subjectID: " + mem.subjectID());
      System.out.println("subjectTypeID: " + mem.typeID());
      System.out.println("memberID: " + mem.memberID());
      Iterator iter = vals.iterator();
      while (iter.hasNext()) {
        GrouperList gl  = (GrouperList) iter.next();
        GrouperGroup g  = gl.group();
        GrouperGroup v  = gl.via();
        if (v == null) {
          System.out.println(
            "immediateMemberOf: " + g.name() + " (" + g.type() + ")"
          );
        } else {
          System.out.println(
            "effectiveMemberOf: " + g.name() + " (" + g.type() + ") " +
            "via " + v.name() + " (" + v.type() + ")"
          );
        }
      }
      System.out.println();
    }
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
