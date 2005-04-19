/*
SubsystemXmlDestroyer.java
Created on Feb 22, 2005

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectTypeAdapter;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

public class SubjectManager
{
  private static SessionFactory sessionFactory;
  private Session               session;

  private String[] deletionStatements
    = new String[]
        {
          "delete from SubjectAttribute",
          "delete from Subject"
        };
  
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
   *
   *
   */
  public SubjectManager()
  {
    try
    {
      this.session = sessionFactory.openSession();
    }
    catch (HibernateException he)
    {
      throw new RuntimeException(he);
    }
  }
  
  public void newAttribute
    (String subjectTypeId,
     String subjectId,
     String name,
     int    instance,
     String value,
     String searchValue) throws HibernateException, SQLException
  {
    String insertSQL
      = "insert into SubjectAttribute"
        + "(subjectTypeID,"
        + " subjectID,"
        + " name,"
        + " instance,"
        + " value,"
        + " searchValue,"
        + " modifyDatetime)"
        + "values (?, ?, ?, ?, ?, ?, ?)";
  
    PreparedStatement pStmt
      = this.session.connection().prepareStatement(insertSQL);
  
    pStmt.setString(1, subjectTypeId);
    pStmt.setString(2, subjectId);
    pStmt.setString(3, name);
    pStmt.setInt(4, instance);
    pStmt.setString(5, value);
    pStmt.setString(6, searchValue);
    pStmt.setDate(7, new Date(new java.util.Date().getTime()));
    pStmt.executeUpdate();
  }
  
  public void newAttribute
    (Subject   subject,
     String    name,
     int       instance,
     String    value,
     String    searchValue)
  throws
    HibernateException,
    SQLException
  {
    newAttribute
      (subject.getSubjectType().getId(),
       subject.getId(),
       name,
       instance,
       value,
       searchValue);
  }

  /*
   * Deletes all Subject data and associated attributes.
   *
   */
  public void deleteAll()
  throws HibernateException, SQLException
  {
    Connection conn = this.session.connection();
      
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
  
  public void commit() throws HibernateException, SQLException
  {
    this.session.connection().commit();
  }
  
  private void execute(Connection conn, String sql, String verb)
  throws SQLException
  {
    PreparedStatement ps = conn.prepareStatement(sql);
    int rows = ps.executeUpdate();
    System.out.println("Number of rows " + verb + ": " + rows);
  }
  
  // HERE BEGINS CODE MOVED FROM SIGNET.JAVA

//  /**
//   * Creates a new Subject.
//   * 
//   * @param id
//   * @param name
//   * @return
//   * @throws OperationNotSupportedException
//   * @throws ObjectNotFoundException
//   */
//  public Subject newSubject(String id, String name)
//      throws ObjectNotFoundException
//  {
//    return this.newSubject(id, name, null, null);
//  }
//
//  /**
//   * Creates a new Subject.
//   * 
//   * @param id
//   * @param name
//   * @param description
//   * @param displayId
//   * @return
//   * @throws ObjectNotFoundException
//   */
//  public Subject newSubject(String id, String name, String description,
//      String displayId) throws ObjectNotFoundException
//  {
//    Subject newSubject;
//
//    try
//    {
//      newSubject = this.newSubject(Signet.DEFAULT_SUBJECT_TYPE_ID, id, name,
//          description, displayId);
//    }
//    catch (OperationNotSupportedException onse)
//    {
//      throw new SignetRuntimeException(
//          "An attempt to create a new native Signet subject has failed,"
//              + " because the Signet subject-adapter has reported that its"
//              + " collection cannot be modified. Although other subject-adapters"
//              + " may prevent subject-creation, this one should always allow it.",
//          onse);
//    }
//
//    return newSubject;
//  }

//  /**
//   * Creates a new Subject.
//   * 
//   * @param subjectTypeId
//   * @param subjectId
//   * @param subjectName
//   * @param subjectDescription
//   * @param subjectDisplayId
//   * @return
//   * @throws ObjectNotFoundException
//   * @throws OperationNotSupportedException
//   */
//  Subject newSubject(String subjectTypeId, String subjectId,
//      String subjectName, String subjectDescription, String subjectDisplayId)
//      throws ObjectNotFoundException, OperationNotSupportedException
//  {
//    SubjectType subjectType = this.getSubjectType(subjectTypeId);
//    Subject subject = newSubject(subjectType, subjectId, subjectName,
//        subjectDescription, subjectDisplayId);
//
//    return subject;
//  }

  /**
   * Creates a new Subject.
   * 
   * @param subjectType
   * @param subjectId
   * @param subjectName
   * @param subjectDescription
   * @param subjectDisplayId
   * @return
   * @throws OperationNotSupportedException
   */
  public Subject newSubject
    (SubjectType subjectType,
     String subjectId,
     String subjectName,
     String subjectDescription,
     String subjectDisplayId)
  throws OperationNotSupportedException
  {
    SubjectTypeAdapter adapter = subjectType.getAdapter();
    Subject subject
      = adapter.newSubject
          (subjectType,
           subjectId,
           subjectName,
           subjectDescription,
           subjectDisplayId);

    return subject;
  }
}
