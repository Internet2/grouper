/*
 * Copyright (C) 2004-2005 The University Of Chicago
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
 * @version $Id: grouperq.java,v 1.19 2005-05-20 15:46:36 blair Exp $
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
  private static GrouperGroup   grpQueryOn;
  private static GrouperMember  mem;
  private static GrouperMember  memQueryOn;
  private static GrouperSession s;
  private static Options        options;
  private static String         queryGroupName;
  private static String         querySubjectID;
  private static Subject        subj;  
  private static String         subjectID;
  private static boolean        verbose     = false;
  

  /*
   * PUBLIC CLASS METHODS
   */
  public static void main(String[] args) {
    _opts(args);      // Parse and handle command line options
    _grouperStart();  // Initialize Grouper and start session
    _grouperQuery();  // Perform a naive query and output results
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
  private static void _grouperQuery() {
    if ( (queryGroupName != null) && (querySubjectID != null) ) {
      System.err.println("ERROR: Cannot use both -g and -m!");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(NAME, options);
      System.exit(64);
    }
    if (field == null) {
      _verbose("Using default group field (" + Grouper.DEF_LIST_TYPE + ")");
      field = Grouper.DEF_LIST_TYPE;
    }
    _verbose("Looking up field '" + field + "'");
    _queryOnGroup();
    _queryOnMember();
    if      ( (queryGroupName != null) && (grpQueryOn != null) ) {
      _reportMembers( grpQueryOn.listVals(field) );
    } else if ( ( querySubjectID != null) && (memQueryOn != null) ) {
      _reportGroups( memQueryOn.listVals(field) );
    } else {
      System.err.println("ERROR: Unable to determine what to query on");
    }
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
    mem = GrouperMember.load(s, subj);
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
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("field");
    OptionBuilder.withDescription("Specify group field to query on");
    options.addOption( OptionBuilder.create("f") );
    options.addOption("h", false, "Print usage information");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("group");
    OptionBuilder.withDescription(
    	  "Specify group to query on [Cannot be used with '-m']"
    	);
    options.addOption( OptionBuilder.create("g") );
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("member");
    OptionBuilder.withDescription(
    	  "Specify member to query on [Cannot be used with '-g']"
    );
    options.addOption( OptionBuilder.create("m") );
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("subject");
    OptionBuilder.withDescription("specify subject to query as");
    options.addOption( OptionBuilder.create("S") );
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
    if (cmd.hasOption("g")) {
      queryGroupName = cmd.getOptionValue("g");
      _verbose("Got group '" + queryGroupName + "'");
    }
    if (cmd.hasOption("m")) {
      querySubjectID = cmd.getOptionValue("m");
      _verbose("Got member '" + querySubjectID + "'");
    }
    if (cmd.hasOption("S")) {
      subjectID = cmd.getOptionValue("S");
      _verbose("Got subject '" + subjectID + "'");
    }
  }

  /* (!javadoc)
   * Determine what, if any, group to query on
   */
  private static void _queryOnGroup() {
    if (queryGroupName != null) {
      grpQueryOn = GrouperGroup.loadByName(s, queryGroupName);
    }
  }

  /* (!javadoc)
   * Determine what member to query on
   */
  private static void _queryOnMember() {
    // FIXME I should really have a command-line type option
    // First try looking up the member as DEF_SUBJ_TYPE
    if (querySubjectID != null) {
      memQueryOn = GrouperMember.load(
                     s, querySubjectID, Grouper.DEF_SUBJ_TYPE
                   );
      // If that doesn't resolve, attempt to look up the member as a
      // group
      if (memQueryOn == null) {
        GrouperGroup g = GrouperGroup.loadByName(s, querySubjectID);
        if (g != null) {
          memQueryOn = g.toMember();
          if (memQueryOn != null) {
            _verbose("Retrieved member '" + querySubjectID + "' as type 'group'");
          }
        }
      } else {
        _verbose("Retrieved member '" + querySubjectID + "' as type '" + 
                 Grouper.DEF_SUBJ_TYPE + "'");
      }
    } else {
      memQueryOn = GrouperMember.load(
                     s, subjectID, Grouper.DEF_SUBJ_TYPE
                   );
    }
  }

  /* (!javadoc)
   * Report on groups within search results
   */
  private static void _reportGroups(List vals) {
    _verbose("Results returned by query: " + vals.size());
    if (memQueryOn != null) {
      GrouperMember m = memQueryOn; // Too damn much typing otherwise
      String sid = m.subjectID();
      // If member is a group, attempt to fetch name
      if (m.typeID().equals("group")) {
        Group mAsG = m.toGroup();
        if (mAsG != null) {
          sid = mAsG.name();
        }
      }
      System.out.println("subjectID: " + sid);
      System.out.println("subjectTypeID: " + m.typeID());
      System.out.println("memberID: " + m.memberID());
      Iterator iter = vals.iterator();
      while (iter.hasNext()) {
        GrouperList gl  = (GrouperList) iter.next();
        Group       g   = gl.group();
        Group       v   = gl.via();
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
   * Report on members within search results
   */
  private static void _reportMembers(List vals) {
    _verbose("Results returned by query: " + vals.size());
    if (grpQueryOn != null) {
      GrouperGroup g = grpQueryOn; // Too damn much typing otherwise
      System.out.println("name: " + g.name());
      System.out.println("type: " + g.type());
      System.out.println("stem: " + g.attribute("stem").value());
      System.out.println("extn: " + g.attribute("extension").value());
      System.out.println("groupID: " + g.id());
      Subject createSubj  = g.createSubject();
      Date   createDate   = g.createTime();
      if ( (createSubj != null) && (createDate != null) ) {
        System.out.println("createSubjectID: " + createSubj.getId());
        System.out.println(
          "createSubjectTypeID: " + 
          createSubj.getSubjectType().getId()
        );
        System.out.println(
          "createTime: " + Long.toString(createDate.getTime())
        );
        System.out.println("createTimePretty: " + createDate);
      };
      Subject modifySubj  = g.modifySubject();
      Date   modifyDate   = g.modifyTime();
      if ( (modifySubj != null) && (modifyDate != null) ) {
        System.out.println("modifySubjectID: " + modifySubj.getId());
        System.out.println(
          "modifySubjectTypeID: " + 
          modifySubj.getSubjectType().getId()
        );
        System.out.println(
          "modifyTime: " + Long.toString(modifyDate.getTime())
        );
        System.out.println("modifyTimePretty: " + modifyDate);
      };
      Iterator iter = vals.iterator();
      while (iter.hasNext()) {
        GrouperList   gl  = (GrouperList) iter.next();
        Group         v   = gl.via();
        GrouperMember m   = gl.member();
        String mem      = null;
        if (m.typeID().equals("group")) {
          Group gAsM = m.toGroup();
          if (gAsM != null) {
            mem = gAsM.name() + " (" + gAsM.type() + ")";
          } 
        }
        // If not set, take the safe approach
        if (mem == null) {
          mem = m.subjectID() + " (" + m.typeID() + ")";
        }
        if (v == null) {
          System.out.println("immediateMember: " + mem);
        } else {
          System.out.println(
            "effectiveMember: " + mem + " via " + v.name() + 
            " (" + v.type() + ")"
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
    subj = SubjectFactory.getSubject(subjectID, Grouper.DEF_SUBJ_TYPE);
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
