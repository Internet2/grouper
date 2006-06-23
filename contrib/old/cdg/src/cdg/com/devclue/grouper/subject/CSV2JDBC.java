/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.subject;

import  com.devclue.grouper.registry.*;
import  com.Ostermiller.util.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;

/**
 * Populate I2MI's JDBC subject source from an input CSV file.
 * <p />
 * @author  blair christensen.
 * @version $Id: CSV2JDBC.java,v 1.1 2006-06-23 17:30:12 blair Exp $
 */
public class CSV2JDBC {

  // PRIVATE INSTANCE VARIABLES //
  private SubjectAdd sa;


  // CONSTRUCTORS //

  /**
   * Create a new CSV2JDBC object.
   * <pre class="eg">
   * CSV2JDBC c2j = new CSV2JDBC();
   * </pre>
   */
  public CSV2JDBC() {
    this.sa = new SubjectAdd();
  } // public CSV2JDBC()


  // PUBLIC CLASS METHODS //

  /**
   * Read CSV on STDIN and add subjects to Groups Registry.
   * <p>
   * The CSV parser understands the following semantics:
   * </p>
   * <pre class="eg">
   * # This is a comment
   * subject id,subject type
   * subject id,subject type,name
   * FIXME The line below no longer works
   * subject id,subject type,name attribute,loginID attribute
   * </pre>
   * <p>
   * Internally, <i>CSV2JDBC</i> parses the CSV entries into a
   * collection of {@link MockSubject} objects that are added to the
   * JDBC Source via {@link SubjectAdd}.
   * </p>
   * <pre>
   * // Read Subject CSV on STDIN and add subjects to the JDBC Source.
   * % java com.devclue.grouper.subject.CSV2JDBC
   * </pre>
   */
  public static void main(String[] args) {
    int       ev  = 1;
    CSV2JDBC  c2j = new CSV2JDBC();
    try {
      List subjects = c2j.parseCSVFile( 
        new BufferedReader(
          new InputStreamReader(System.in)
        )
      );
      c2j.addSubjects(subjects);
      ev = 0;
    }
    catch (Exception e) {
      System.err.println("Error adding subjects: " + e.getMessage());
    }
    System.exit(ev);
  } // public static void main(args)


  // PUBLIC INSTANCE METHODS //

  /**
   * Add a list of {@link MockSubject} subjects.
   * <pre class="eg">
   * // Read Subject CSV on STDIN and add found entries to JDBC Source.
   * CSV2JDBC c2j       = new CSV2JDBC();
   * List     subjects  = c2j.parseCSVFile( 
   *   new BufferedReader(
   *     new InputStreamReader(System.in)
   *   )
   * );
   * c2j.addSubjects(subjects);
   * </pre>
   * @param subjects  A list of mock subjects to add.
   */
  public void addSubjects(List subjects) 
    throws  RuntimeException
  {
    GroupsRegistry  gr    = new GroupsRegistry();
    Connection      conn  = gr.getConnection();
    Iterator iter = subjects.iterator();
    while (iter.hasNext()) {
      this.sa.addSubject( (MockSubject) iter.next() );
    }
  } // public void addSubjects(subjects)

  /**
   * Parse a CSV file into a list of {@link MockSubject} objects.
   * <pre class="eg">
   * // Read Subject CSV on STDIN and return a list of mock subjects.
   * CSV2JDBC c2j       = new CSV2JDBC();
   * List     subjects  = c2j.parseCSVFile( 
   *   new BufferedReader(
   *     new InputStreamReader(System.in)
   *   )
   * );
   * </pre>
   * @param   r   CSV reader stream.
   * @return  List of {@link MockSubject} objects.
`  */
  public List parseCSVFile(Reader r)
    throws  RuntimeException
  { 
    List      subjects  = new ArrayList();
    CSVParser p         = new CSVParser(r, new String(), new String(), "#");
    String line[];
    try {
      while ( (line = p.getLine()) != null ) {
        subjects.add( this._parseList( Arrays.asList(line) ) );
      }
    } 
    catch (Exception e) {
      throw new RuntimeException(
        "Error parsing CSV: " + e.getMessage()
      );
    }
    return subjects;
  } // public List parseCSVFile(r)


  // PRIVATE INSTANCE METHODS //

  // Parse "description" attribute
  private MockSubject _parseAttrDescription(MockSubject ms) {
    // Attempt to split, reverse and otherwise munge the name
    String[] tokens = ms.getName().split("\\s+", 2);
    String val, sval;
    if (tokens.length > 1) {
      val   = tokens[1] + ", " + tokens[0];
      sval  = tokens[1].toLowerCase() + " " + tokens[0].toLowerCase();
    } 
    else {
      val   = ms.getName();
      sval  = ms.getName().toLowerCase();
    }
    ms.setAttributeSearchValue("description", val, sval);
    return ms;
  } // private MockSubject _parseAttrDescription(ms)

  // Parse "loginid" attribute
  private MockSubject _parseAttrLoginid(MockSubject ms, List l) {
    String val = ms.getId();
    if (l.size() >= 4) {
      val = (String) l.get(3);
    } 
    ms.setAttributeValue("loginid", val);
    return ms;
  } // private MockSubject _parseLoginid(ms, l)

  // Parse a CSV entry line and return a MockSubject
  private MockSubject _parseList(List l) 
    throws  IllegalArgumentException
  {
    if (l.size() < 2) {
      throw new IllegalArgumentException(
        "ERROR: Insufficient elements (" + l.size() +")"
      );
    } 
    else if (l.size() > 4) {
      throw new IllegalArgumentException(
        "ERROR: Too many elements (" + l.size() +")"
      );
    }
    return this._parseListToMockSubject(l);
  } // private MockSubject _parseList(l)

  // Create a MockSubject from a line's list elements
  private MockSubject _parseListToMockSubject(List l) {
    MockSubject ms = new MockSubject();
    ms.setIdValue(                    (String) l.get(0)       );
    ms.setType(                       (String) l.get(1)       );
    ms.setNameValue(                  this._parseName(ms, l)  );
    ms.setDescriptionValue(           ms.getName()            );
    ms = this._parseAttrDescription(  ms                      );
    ms = this._parseAttrLoginid(      ms, l                   );
    return ms;
  } // private MockSubject _parseListToMockSubject(l)

  // Parse a "name" value
  private String _parseName(MockSubject ms, List l) {
    if (l.size() >= 3) {
      return (String) l.get(2);
    } 
    return ms.getId();
  } // private String _parseName(ms, l)

}

