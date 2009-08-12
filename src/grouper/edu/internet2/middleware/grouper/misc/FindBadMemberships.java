/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.misc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Find bad memberships in the Grouper memberships table.
 *
 * This script is used to find bad memberships in Grouper.  It will go through all types of 
 * memberships including composites, access privileges and naming privileges.  If a bad
 * membership is found for a group or stem, a GSH script will be created to delete the immediate 
 * membership and recreate the membership.
 *
 * @since   1.3.1
 */
public class FindBadMemberships {

  /**
   * 
   */
  public static PrintStream out = System.out;
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(FindBadMemberships.class);

  /** GSH script to fix membership data */
  public static StringWriter gshScript = null;

  /** File name for GSH script */
  private static final String gshScriptFilename = "findbadmemberships.gsh";

  /** Whether to print memberships errors to standard out. */
  private static boolean printErrorsToSTOUT = false;
  /**
   * call this before finding bad memberships
   */
  public static void clearResults() {
    gshScript = null;
  }
  
  /**
   * @param args 
   * @since   1.3.1
   */
  public static void main(String[] args) {
    Options options = new Options();
    OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(new Option("all", false, 
      "Find bad composite memberships."));
    optionGroup.setRequired(true);
    options.addOptionGroup(optionGroup);

    if (args.length == 0) {
      printUsage(options);
      System.exit(0);
    }

    CommandLineParser parser = new GnuParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      printUsage(options);
      System.exit(1);
    }
    clearResults();
    // maybe this should go to a log file instead?
    printErrorsToSTOUT = true;
    
    try {
      if (line.hasOption("all")) {
        checkAll(out);
      } else {
        printUsage(options);
        System.exit(0);
      }

    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }

    out.println();
    out.println();
    if (gshScript == null) {
      out.println("No membership errors found.");
    } else {
      out.println("Membership errors have been found.  Do the following to resolve the errors:");
      writeFile(gshScript, gshScriptFilename);
      out.println(" - Review the GSH script before applying any changes to your database.");
      out.println(" - Execute the GSH Script " + gshScriptFilename);
      out.println(" - Re-run the bad membership finder utility to verify that bad memberships have been fixed.");
    }
    System.exit(0);
  }

  /**
   * @param printStream 
   * @throws SessionException
   */
  public static void checkAll(PrintStream printStream) throws SessionException {
    PrintStream oldPrintStream = out;
    out = printStream;
    try {  
      out.println();
      out.println("Checking Composite Memberships");
      out.println("Feature not implemented yet....");
    } finally {
      out = oldPrintStream;
    }
  }

  /**
   * Set whether to print errors to STDOUT.
   * @param v 
   */
  public static void printErrorsToSTOUT(boolean v) {
    printErrorsToSTOUT = v;
  }

  /**
   * Print usage.
   * @param options 
   */
  private static void printUsage(Options options) {

    out.println();
 
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(FindBadMemberships.class.getSimpleName(), options, true);
 
    out.println();
    out.print("This script will find bad composite memberships in your Grouper database.  ");
    out.print("It will not make any modifications to the Grouper database.  ");
    out.println("If bad memberships are found, this script will create a GSH script that will delete and re-add memberships.");
    out.println();
    out.println("To fix your memberships, complete these steps in the order listed:");
    out.println();
    out.println("1.  Review the GSH script before applying any changes to your database.");
    out.println("2.  Run the GSH script.");
    out.println("3.  Re-run the bad membership finder utility to verify that bad memberships have been fixed.");
    out.println();
  }

  /**
   * We have found an error with a group membership.  Write a GSH script to delete and re-add the immediate memberships.
   *
   * @param group where an error was found
   * @param composite if this is a composite group, otherwise null.
   * @param current memberships for the group
   * @throws GroupNotFoundException
   */
  private static void foundError(Group group, Composite composite, List<Membership> current) throws GroupNotFoundException {
    if (printErrorsToSTOUT) {
      out.println("FOUND BAD MEMBERSHIP: Bad membership in group with uuid=" + group.getUuid() + " and name=" + group.getName() + ".");
    }

    StringWriter deletes = new StringWriter();
    StringWriter adds = new StringWriter();

    if (composite != null) {
      String leftGroupName = composite.getLeftGroup().getName();
      String rightGroupName = composite.getRightGroup().getName();
      String compositeType = "";
      if (composite.getType().equals(CompositeType.UNION)) {
        compositeType = "CompositeType.UNION";
      } else if (composite.getType().equals(CompositeType.COMPLEMENT)) {
        compositeType = "CompositeType.COMPLEMENT";
      } else if (composite.getType().equals(CompositeType.INTERSECTION)) {
        compositeType = "CompositeType.INTERSECTION";
      }

      deletes.write("delComposite(\"" + group.getName() + "\")\n");
      adds.write("addComposite(\"" + group.getName() + "\", " + compositeType + ", \"" + leftGroupName + "\", \"" + rightGroupName + "\")\n");
    }

    logGshScript(deletes.toString());
    logGshScript(adds.toString());
  }
  
  /**
   * Keep the GSH script in memory until we're done with this utility.
   *
   * @param script to log
   */
  private static void logGshScript(String script) {
    if (gshScript == null) {
      gshScript = new StringWriter();
    }

    gshScript.write(script);
  }

  /**
   * Write GSH script to file.
   */
  public static void writeGshScriptToFile() {
    if (gshScript != null) {
      writeFile(gshScript, gshScriptFilename);
    }
  }

  /**
   * Write data to a file.
   *
   * @param data to write
   * @param filename to write data to.
   */
  private static void writeFile(StringWriter data, String filename) {
    FileWriter fw = null;
    try {
      out.println("Writing file: " + GrouperUtil.fileCanonicalPath(new File(filename)));
      fw = new FileWriter(filename, false);
      fw.write(data.toString());
    } catch (IOException e) {
      out.println("Exception while writing out to file " + filename + ": " + e.toString());
      out.println("Writing data out here instead: ");
      out.println(data.toString());
    } finally {
      try {
        if (fw != null) {
          fw.close();
        }
      } catch (IOException e) {
        // do nothing
      }
    }
  }
}

