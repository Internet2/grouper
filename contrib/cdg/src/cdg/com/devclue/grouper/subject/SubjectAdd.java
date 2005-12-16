/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.subject;

import  com.devclue.grouper.registry.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.sql.*;
import  java.util.*;

/**
 * Add a Subject to the I2MI JDBC Subject source.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectAdd.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class SubjectAdd {

  /*
   * PRIVATE INSTANCE VARIABLES
   */

  Connection        conn;
  PreparedStatement sa, su;
  SubjectQ          sq;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new SubjectAdd object.
   * <pre class="eg">
   * SubjectAdd sa = new SubjectAdd();
   * </pre>
   */
  public SubjectAdd() {
    this._getConnection();
    this._prepareStatements();
    this.sq = new SubjectQ();
  } // public SubjectAdd()


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Create subject specified as command line argument.
   * <p>Subject is printed to STDOUT if created.</p>
   * <p>Exits with 0 if subject created, 1 otherwise.</p>
   * <pre class="eg">
   * // Add <i>person</i> subject with id <i>john</i> to the JDBC
   * // source.
   * % java com.devclue.grouper.subject.SubjectAdd john
   * </pre>
   */
  public static void main(String[] args) {
    int           ev    = 1;
    SubjectAdd    sa    = new SubjectAdd();
    MockSubject   ms    = new MockSubject(
      args[0], args[0], new MockSourceAdapter()
    );
    try {
      if (args.length == 1) {
        sa.addSubject(ms);
        ev = 0;
      }
      else {
        System.err.println("Invalid number of arguments: " + args.length);
      }
    }
    catch (RuntimeException e) {
      System.err.println("Error creating subject: " + e.getMessage());
    }
    if (ev == 0) {
      System.out.println(
        ms.getId()      + "," + ms.getType().getName() + "," + ms.getName() 
      );
    }
    System.exit(ev);
  } // public static void main(args)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Add a JDBC Subject.
   * <pre class="eg">
   * // Add subject with id <i>id</i> and name <i>name</i> to the JDBC
   * // source.
   * SubjectAdd sa = new SubjectAdd();
   * try {
   *   sa.addSubject(
   *     new MockSubject(id, name, new MockSourceAdapter()
   *   );
   * }
   * catch (RuntimeException e) {
   *   // Error adding subject
   * }
   * </pre>
   * @param ms  Mock subject to add to the JDBC source.
   */
  public void addSubject(MockSubject ms) 
    throws  RuntimeException
  {
    this._setAutoCommitOff();

    // It just makes things easier
    String id = (String) ms.getId();

    // TODO Query for subject existence first.  Too bad that part is
    // not working properly and I'm not sure why at the moment.

    try {
      // Subject
      this.su.setString(1, id                              );
      this.su.setString(2, (String) ms.getType().getName() );
      this.su.setString(3, (String) ms.getName()           );
      this.su.executeUpdate();
      // SubjectAttribute
      Iterator iter = ms.getAttributes().keySet().iterator();
      while (iter.hasNext()) {
        String name = (String) iter.next();
        String val  = ms.getAttributeValue(name);
        String sval = ms.getAttributeSearchValue(name);
        this.sa.setString(1, id  );
        this.sa.setString(2, name);
        this.sa.setString(3, val );
        this.sa.setString(4, sval); 
        this.sa.executeUpdate();
      }
      conn.commit();
    }
    catch (SQLException e) {
      try {
        conn.rollback();
      }
      catch (SQLException e1) {
        throw new RuntimeException(
          "Unable to rollback transactions: " + e1.getMessage()
        );
      }
      throw new RuntimeException(
        "Unable to commit transactions: " + e.getMessage()
      );
    }

    this._setAutoCommitOn();
  } // public void addSubject(ms)


  /*
   * PRIVATE INSTANCE METHODS
   */

  // Get JDBC connection to the Groups Registry
  private void _getConnection() {
    GroupsRegistry gr = new GroupsRegistry();
    this.conn = gr.getConnection();
  }
  // private void _getConnection()

  // Prepare JDBC statements
  private void _prepareStatements() {
    try {
      this.su = this.conn.prepareStatement(
        "INSERT INTO Subject (subjectID, subjectTypeID, name) " 
        + "VALUES (?, ?, ?)"
      );
      this.sa = this.conn.prepareStatement(
        "INSERT INTO SubjectAttribute (subjectID, name, value, searchValue) " 
        + "VALUES (?, ?, ?, ?)"
      );
    }
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to prepare statement: " + e.getMessage()
      );
    }
  } // private void _prepareStatements()

  // Disable autocommit
  private void _setAutoCommitOff() {
    try {
      this.conn.setAutoCommit(false);
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to disable autocommit: " + e.getMessage()
      );
    }
  } // private void _setAutoCommitOff()

  // Enable autocommit
  private void _setAutoCommitOn() {
    try {
      this.conn.setAutoCommit(true);
    } 
    catch (SQLException e) {
      throw new RuntimeException(
        "Unable to enable autocommit: " + e.getMessage()
      );
    }
  } // private void _setAutoCommitOn()

}

