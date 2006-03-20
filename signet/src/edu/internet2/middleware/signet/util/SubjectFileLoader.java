/*
SubjectFileLoader.java
Created on Feb 22, 2005

Copyright 2006 Internet2, Stanford University

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

package edu.internet2.middleware.signet.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.*;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

public class SubjectFileLoader
{
  private static SessionFactory sessionFactory;
  private        Session        session;
  private        Connection     conn;

  private String[] deletionStatements
    = new String[]
        {
          "delete from SubjectAttribute",
          "delete from Subject",
          "delete from SubjectType"
        };
  
  private String insertSubjectSQL
    = "insert into Subject"
      + "(subjectTypeID,"
      + " subjectID,"
      + " name,"
      + " description,"
      + " displayID,"
      + " modifyDatetime)"
      + "values (?, ?, ?, ?, ?, ?)";

  private String insertAttrSQL
    = "insert into SubjectAttribute"
      + "(subjectTypeID,"
      + " subjectID,"
      + " name,"
      + " instance,"
      + " value,"
      + " searchValue,"
      + " modifyDatetime)"
      + "values (?, ?, ?, ?, ?, ?, ?)";

  
  static
  /* runs at class load time */
  {
    Configuration cfg = new Configuration();

    try
    {
      // Read the "hibernate.cfg.xml" file.
      cfg.configure();
      sessionFactory = cfg.buildSessionFactory();
    }
    catch (HibernateException he)
    {
      throw new RuntimeException(he);
    }
  }
    
  /**
   * Opens a connection to the database for subsequent use in loading
   * and deleting Subjects.
   *
   */
  public SubjectFileLoader()
  {
    try
    {
      this.session = sessionFactory.openSession();
      this.conn = session.connection();
    }
    catch (HibernateException he)
    {
      throw new RuntimeException(he);
    }
  }
  
  
  /**
   * Creates a new SubjectAttribute, and stores that value in the database.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param Subject
   * @param name
   * @param instance
   * @param value
   * @param searchValue
   * @throws SQLException
   */
  public void newAttribute
    (Subject subject,
     String name,
     int    instance,
     String value,
     String searchValue)
  throws
    SQLException
  {
    PreparedStatement pStmt = null;
    try {
      pStmt = this.conn.prepareStatement(insertAttrSQL);
      pStmt.setString(1, subject.getType().getName());
      pStmt.setString(2, subject.getId());
      pStmt.setString(3, name);
      pStmt.setInt(4, instance);
      pStmt.setString(5, value);
      pStmt.setString(6, searchValue);
      pStmt.setDate(7, new Date(new java.util.Date().getTime()));
      pStmt.executeUpdate();
    }
    finally {
      if (pStmt != null) {
        pStmt.close();
      }
    }
   
    ((SubjImpl)subject).addAttribute(name, value);
  }

  
  /**
   * Deletes all Subject data and associated attributes.
   * This method updates the database, but does not commit any transaction.
   * 
   * @throws SQLException
   */
  public void deleteAll()
  throws SQLException
  {
    try
    {
      //conn.setAutoCommit(true);
      for (int i = 0; i < this.deletionStatements.length; i++)
      {
        execute(conn, this.deletionStatements[i], "deleted");
      }
    }
    catch (SQLException ex)
    {
      conn.rollback();
      System.out.println("SQL error occurred: " + ex.getMessage());
    }
  }
  
  /**
   * Commits the current database transaction in use by the SubjectFileLoader.
   * 
   * @throws SQLException
   */
  public void commit() throws SQLException
  {
    this.conn.commit();
  }
  
  
  private void execute(Connection conn, String sql, String verb)
  throws SQLException
  {
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql);
      int rows = ps.executeUpdate();
      System.out.println("- " + sql + ": " + rows + " rows affected");
    }
    finally {
      if (ps != null) {
        ps.close();
      }
    }
  }
  
  /**
   * Creates a new Subject.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param subjectType
   * @param subjectId
   * @param subjectName
   * @param subjectDescription
   * @param subjectDisplayId
   * @return
   * @throws SQLException
   */
  public Subject newSubject
    (SubjectType subjectType,
     String subjectId,
     String subjectName,
     String subjectDescription,
     String subjectDisplayId)
  throws
    SQLException
  {
    PreparedStatement pStmt = null;
    try {
      pStmt = this.conn.prepareStatement(insertSubjectSQL);
      pStmt.setString(1, subjectType.getName());
      pStmt.setString(2, subjectId);
      pStmt.setString(3, subjectName);
      pStmt.setString(4, subjectDescription);
      pStmt.setString(5, subjectDisplayId);
      pStmt.setDate(6, new Date(new java.util.Date().getTime()));
      pStmt.executeUpdate();
    }
    finally {
      if (pStmt != null) {
        pStmt.close();
      }
    }
    
    Subject subject
      = new SubjImpl
          (subjectType,
           subjectId,
           subjectName,
           subjectDescription,
           subjectDisplayId);

    return subject;
  }
  
  public class SubjImpl
  implements Subject
  {
    private SubjectType type;
    private String      id;
    private String      name;
    private String      description;
    private String      displayId;
    private Map         attributes;
    
    SubjImpl
      (SubjectType type,
       String       id,
       String       name,
       String       description,
       String       displayId)
    {
      this.type = type;
      this.id = id;
      this.name = name;
      this.description = description;
      this.displayId = displayId;
      this.attributes = new HashMap();
    }
    
    public String getDescription()
    {
      return this.description;
    }
    
    public String getDisplayId()
    {
      return this.displayId;
    }
    
    public String getId()
    {
      return this.id;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public SubjectType getType()
    {
      return this.type;
    }

    public void addAttribute(String name, String value)
    {
      Set attr = (Set)(this.attributes.get(name));
      
      if (attr == null)
      {
        attr = new HashSet();
        this.attributes.put(name, attr);
      }
      
      attr.add(value);
    }

    public Map getAttributes() {
    	return attributes;
    }
    
    public Set getAttributeValues(String name)
    {
      Set attr = (Set)(this.attributes.get(name));
      
      if (attr == null)
      {
        return new HashSet();
      }

      return (attr);
    }
    
	public String getAttributeValue(String name) {
		Set values = getAttributeValues(name);
		return ((String[])values.toArray(new String[0]))[0];
	}
	
	public Source getSource() {
		return new JDBCSourceAdapter("local", "local");
	}
	
  }

  public static void main(String[] args) 
    throws SQLException
    {

    SubjectFileLoader loader = new SubjectFileLoader();

    try {
      if (args.length < 1) {
          System.err.println("Usage: SubjectFileLoader <inputfile>");
          return;
      }
         
      String inputFileName = args[0];
      BufferedReader in = new BufferedReader(new FileReader(inputFileName));
      loader.processFile(loader, in);

      in.close();
      loader.commit();

    } catch (IOException e) {
       e.printStackTrace();
    }
  }

private void processFile(SubjectFileLoader loader, BufferedReader in)
  throws IOException, SQLException {
    try {

    String lineData = "";
    String lineData2 = "";
    String lineData3 = "";
    String keyword = "";
    String value = "";
    String subjectSourceID = "";
    String subjectID = "";
    String subjectName = "";
    int    lineNumber = 0;

   /**
    * Find the initial "source" line
    * -- should be the first non-command, non-blank line in the input file
    */

    while ((lineData = in.readLine()) != null) {
      lineNumber++;
      // System.out.println(lineNumber + ": " + lineData);

      if (lineData.startsWith("/"))
      {
         // skip
      }         
      else if (lineData.equals(""))
      {
         //skip
      } 
       
      else {
         System.out.println(lineNumber + ": " + lineData);
         StringTokenizer st = new StringTokenizer(lineData);
   
         if (st.hasMoreTokens()) {
            keyword = st.nextToken();
            if (!keyword.equals("source"))
            {
               throw new IOException
               ("Error in line " + lineNumber + ": Initial keyword must be 'source'");
            }
         }
   
         if (st.hasMoreTokens()) {
            subjectSourceID = st.nextToken();
         }
   
         if (st.hasMoreTokens()) {
            value = st.nextToken();
            throw new IOException
            ("Error in line " + lineNumber + ": Extraneous data: " + value);
         }
         
         if (!subjectSourceID.equals("")) {
            break;
         }
      }
    }

    removeSubjects();
    loader.commit();

    Subject subject = null;
    String  currAttributeName = "";
    String  prevAttributeName = "";
    String  attributeName = "";
    int     attributeInstance = 0;

   /**
    * --- Start processing the individual subject entries
    */
    
    while ((lineData = in.readLine()) != null) {
      lineNumber++;
      System.out.println(lineNumber + ": " + lineData);

      if (lineData.startsWith("/"))
      {
         // skip
      }         
      else if (lineData.equals(""))
      {
         //skip
      } 
       
      else {

         if (lineData.startsWith("+")) {
            
            // Get the subject header line
            lineData = lineData.substring(1);
            
            subject = loader.processAddSubject(loader, lineData);
   
            currAttributeName = "";
            prevAttributeName = "";
            attributeInstance = 1;
   
         } else {
   
            currAttributeName = loader.processSubjectAttribute(loader, subject, lineData, prevAttributeName, attributeInstance);
            if (currAttributeName.equals(prevAttributeName) ) {
               attributeInstance++;
            } else {
               prevAttributeName = currAttributeName;
               attributeInstance = 2;
            }
         }
       }
    }
    } catch (Exception e) {
        System.err.println("Exception caught: " + e.getMessage());
    }
  }

  private static Subject processAddSubject(SubjectFileLoader loader, String lineData)
    throws IOException, SQLException
    {

    String subjectID = "";
    String subjectName = "";
    String inputSubjectType = "";
    String subjectNormalizedName = "";
    String attributeName = "";

    StringTokenizer st = new StringTokenizer(lineData);

    if (st.hasMoreTokens()) {
      inputSubjectType = st.nextToken();
    } else {
       throw new IOException ("No Subject Type found");
    }

    lineData = lineData.substring(inputSubjectType.length()+1);

    SubjectType subjectType = SubjectTypeEnum.valueOf(inputSubjectType);

    if (st.hasMoreTokens()) {
      subjectID = st.nextToken();
    } else {
       throw new IOException ("No Subject ID found");
    }

    if (!st.hasMoreTokens()) {
       throw new IOException ("No Subject Name found");
    }
     
    subjectName = lineData.substring(subjectID.length()+1);
    subjectName = subjectName.trim();
    subjectNormalizedName = loader.normalizeString(subjectName);

    // System.out.println("--- SubjectID: " + subjectID + ", SubjectName: " + subjectName);

    Subject subject = loader.newSubject
       (subjectType, subjectID, subjectName, "n/a", "n/a");

    loader.newAttribute
      (subject, "name", 1, subjectName, subjectNormalizedName);

    return subject;
  }

  private static String processSubjectAttribute(SubjectFileLoader loader, Subject subject, String lineData, String prevAttributeName, int attributeInstance)
     throws IOException, SQLException {

     String attributeName;
     String attributeValue;
     String attributeSearchValue;

     StringTokenizer st = new StringTokenizer(lineData);

     if (st.hasMoreTokens()) {
       attributeName = st.nextToken();
     } else {
        throw new IOException ("No Attribute ID found");
     }

     if (!st.hasMoreTokens()) {
        throw new IOException ("No Attribute Value found");
     }
     
     attributeValue = lineData.substring(attributeName.length());
     attributeValue = attributeValue.trim();
     attributeSearchValue = attributeValue.toLowerCase();

     if (!attributeName.equals(prevAttributeName)) {
        attributeInstance = 1;
     }

     // System.out.println("--- Attribute: " + attributeName + ", instance: " + attributeInstance + ", Value: " + attributeValue);

     loader.newAttribute
       (subject, attributeName, attributeInstance, attributeValue, attributeSearchValue);
       
     return attributeName;
  }
   
  private static boolean readYesOrNo(String prompt) {
      while (true) {
          String response = promptedReadLine(prompt);
          if (response.length() > 0) {
              switch (Character.toLowerCase(response.charAt(0))) {
              case 'y':
                  return true;
              case 'n':
                  return false;
              default:
                  System.out.println("Please enter Y or N. ");
              }
          }
      }
  }
  
  private static String promptedReadLine(String prompt) {
      try {
          System.out.print(prompt);
          return reader.readLine();
      } catch (java.io.IOException e) {
          return null;
      }
  }
    
  private static BufferedReader reader;
  
  static {
      reader = new BufferedReader(new InputStreamReader(System.in));
  }


  private void removeSubjects() {
      if (! readYesOrNo(
          "\nYou are about to delete and replace all subjects in the Signet Subject table."
          + "\nDo you wish"
          + " to continue (Y/N)? ")) {
      System.exit(0);
      }

      try {
          deleteAll();
      }
      catch (SQLException sqle) {
         System.out.println("-Error: unable to delete subjects for source person");
         System.out.println(sqle.getMessage());
         System.exit(1);
      }
   }

    private static String normalizeString(String value) {
    /*
    * Normalize a value for searching.  All non-alpha-numeric are converted
    * to a space except for apostrophes, which are elided.
    */
       if (value == null) {
            return null;
        }
        //to lowercase
        char[] work = value.trim().toLowerCase().toCharArray();
        StringBuffer buf = new StringBuffer();

        boolean lastCharacterIsSpace = false;
        for (int i = 0; i < work.length; ++i) {
            if (Character.isLetterOrDigit(work[i])) {
                buf.append(work[i]);
                lastCharacterIsSpace = false;
            } else if (work[i] == '\'') {
                continue; // elide apostrophes
            } else if (!lastCharacterIsSpace) {
                //change any non-alpha, non-numeric to a space.
                buf.append(' ');
                lastCharacterIsSpace = true;
            }
        }
        //trim the leading/trailing whitespace
        return buf.toString().trim();
    }

}
