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
 * $Id: csv2subject.java,v 1.1 2004-07-03 03:10:56 blair Exp $ 
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
