/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Sample loader for populating the 'grouper_members' table from a CSV
 * file of the format: 
 *  subjectID,subjectTypeID
 *
 * You will need to modify the jdbc* variables to reflect your local
 * configuration.
 *
 * Ideally this would all go through the I2MI Subject interface but
 * until that stabilizes, lets cheat.
 *
 * Usage:
 * % javac subject2csv.java
 * % java subject2csv /path/to/csv/file
 *
 * $Id: csv2subject.java,v 1.6 2004-12-03 16:05:47 blair Exp $ 
 */

import  java.io.*;
import  java.lang.reflect.*;
import  java.sql.*;
import  java.util.*;

class csv2subject {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String cf = "csv2subject.properties";


  /*
   * PRIVATE CLAS VARIABLES
   */
  private static Properties conf    = new Properties();
  private static Connection conn    = null;
  private static Map        newSubs = new HashMap();
  private static boolean    verbose = true;


  public static void main(String[] args) {
    _cfRead();
    _jdbcConnect();

    if (args.length == 1) {
      _csvRead(args[0]);
    } else {
      System.err.println("USAGE: subject2csv /path/to/csv/file");
      System.exit(64);
    }

    _jdbcDisconnect();
  }

/*
  private static void _insertMember(String memberID, String presentationID) {
    Statement stmt = null;
    try { 
      stmt = con.createStatement();
      String insertMember = "INSERT INTO grouper_members " +
                             "(memberID, presentationID) " +
                             "VALUES (" +
                             "'" + memberID + "', " +
                             "'" + presentationID + "'" +
                             ")";
      try { 
        stmt.executeUpdate(insertMember);
      } catch (Exception e) {
        System.err.println("Unable to insert session: " + insertMember);
        System.exit(1);
      }
    } catch (Exception e) {
      System.err.println("Unable to create statement");
      System.exit(1);
    }
  }
*/

  /* (!javadoc)
   * Read configuration file.
   * <p />
   * @return Boolean true if succesful.
   */
  private static void _cfRead() {
    try {
      FileInputStream in = new FileInputStream(cf);
      try { 
        conf.load(in);
      } catch (IOException ie) { 
        System.err.println("Unable to read '" + cf + "'");
        System.exit(1);
      }
    } catch (FileNotFoundException fe) {
      System.err.println("Could not find '" + cf + "'");
      System.exit(1);
    }
    _verbose("driver    " + conf.get("jdbc.driver"));
    _verbose("url       " + conf.get("jdbc.url"));
    _verbose("username  " + conf.get("jdbc.username"));
    _verbose("password  " + conf.get("jdbc.password"));
  }

  /* (!javadoc)
   * Read in CSV file
   * <p />
   * @param path Path to input CSV file
   */
  private static void _csvRead(String path) {
    try { 
      BufferedReader  br    = new BufferedReader(new FileReader(path));
      String          line  = null; 
      while ((line=br.readLine()) != null){ 
        StringTokenizer st = new StringTokenizer(line, ",");
        // FIXME Blindly assume that if we have two tokens, they are two
        //       *good* tokens
        if (st.countTokens() == 2) {
          String subjID     = st.nextToken();
          String subjTypeID = st.nextToken();
          _verbose(
            "Found sid=`" + subjID + "', subjTypeID=`" + subjTypeID + "'"
          );
          newSubs.put(subjID, subjTypeID);
        } else {
          System.err.println("Skipping.  Invalid format: '" + line + "'");
        }
      } 
      br.close(); 
    } catch (IOException e) { 
      System.err.println("Error processing '" + path + "': " + e);
      // Kill whatever might have been added to the hashmap and then
      // carry on so that our connection is closed
      newSubs = new HashMap();
    }
  }

  /* (!javadoc)
   * Initialize JDBC connection.
   */
  private static void _jdbcConnect() {
    try {
      Class.forName( (String) conf.get("jdbc.driver") ).newInstance();
      try {
        conn = DriverManager.getConnection(
                 (String) conf.get("jdbc.url"),
                 (String) conf.get("jdbc.username"),
                 (String) conf.get("jdbc.password")
               );
        _verbose("Connected to " + conf.get("jdbc.url"));
      } catch (SQLException se) {
        System.err.println("Unable to connect: " + se);
        System.exit(1);
      }
    } catch(ClassNotFoundException ce) {
      System.err.println(
        "Unable to find class '" + conf.get("jdbc.driver") + "'"
      );
      System.exit(1);
    } catch(InstantiationException ie) {
      System.err.println(
        "Unable to instantiate class '" + conf.get("jdbc.driver") + "'"
      );
      System.exit(1);
    } catch(IllegalAccessException iae) {
      System.err.println(
        "Unable to access class '" + conf.get("jdbc.driver") + "'"
      );
      System.exit(1);
    }
  }

  /* (!javadoc)
   * Close JDBC connection.
   */
  private static void _jdbcDisconnect() {
    if (conn != null) { 
      try {
        conn.commit();
        _verbose("JDBC commit performed");
        try {
          conn.close();
          _verbose("JDBC connection closed");
        } catch (SQLException ce) {
          System.err.println("Unable to close JDBC connection: " + ce);
          System.exit(1);
        }
      } catch (SQLException come) {
        System.err.println("Unable to perform JDBC commit: " + come);
        System.exit(1);
      }
    }
  }

  // Inserts the memberID, presentationID pair into the
  // 'grouper_members' table 
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
