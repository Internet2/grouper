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
 *  memberId,presentationID
 *
 * You will need to modify the jdbc* variables to reflect your local
 * configuration.
 *
 * Usage:
 * % javac LoadGrouperMembersTableFromCSV.java
 * % java LoadGrouperMembersTableFromCSV /path/to/csv/file
 *
 * $Id: csv2subject.java,v 1.2 2004-11-29 19:05:17 blair Exp $ 
 */

import  java.io.BufferedReader;
import  java.io.File;
import  java.io.FileReader;
import  java.io.IOException;
import  java.lang.reflect.*;
import  java.sql.*;
import  java.util.StringTokenizer;

class LoadGrouperMembersTableFromCSV {
  /* 
   * Update these variables to match your local configuration.
   */ 
  public static String     jdbcDriver    = "com.mysql.jdbc.Driver";
  public static String     jdbcUrl       = "jdbc:mysql://localhost:3306/grouper";
  public static String     jdbcUsername  = "grouper";
  public static String     jdbcPassword  = "gr0up3r";

  public static Connection con           = null;

  public static void main(String[] args) {
    if (args.length == 1) {
      File f = new File(args[0]);
      if (!f.exists()) {
        System.err.println("File '" + args[0] + "' does not exist.");
        System.exit(65);
      }

      _createConnection();

      try { 
        BufferedReader  br    = new BufferedReader(new FileReader(args[0])); 
        String          line  = null; 
        while ((line=br.readLine()) != null){ 
          StringTokenizer st        = new StringTokenizer(line, ",");
          // Blindly assume that if we have two tokens, they are two
          // *good* tokens
          if (st.countTokens() == 2) {
            String memberID       = st.nextToken();
            String presentationID = st.nextToken();
            _insertMember(memberID, presentationID);
          } else {
            System.err.println("Skipping.  Invalid format: '" + line + "'");
          }
        } 
        br.close(); 
      } catch (IOException e) { 
        System.err.println(e); 
        System.exit(1); 
      }
    } else {
      System.err.println("USAGE: LoadGrouperMembersTableFromCSV /path/to/csv/file");
      System.exit(64);
    }
  }

  // Initialize the JDBC connection
  private static void _createConnection() {
    try {
      Class.forName( jdbcDriver ).newInstance();
      con = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    } catch(Exception e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  // Inserts the memberID, presentationID pair into the
  // 'grouper_members' table 
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

}
