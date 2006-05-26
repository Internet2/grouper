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
import  java.util.*;
import  net.sf.hibernate.*;

/**
 * Add a Subject to the I2MI JDBC Subject source.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectAdd.java,v 1.2 2006-05-26 17:15:13 blair Exp $
 */
public class SubjectAdd {

  // PRIVATE INSTANCE VARIABLES //
  SubjectQ  sq;


  // CONSTRUCTORS //
  /**
   * Create a new SubjectAdd object.
   * <pre class="eg">
   * SubjectAdd sa = new SubjectAdd();
   * </pre>
   */
  public SubjectAdd() {
    this.sq = new SubjectQ();
  } // public SubjectAdd()


  // PUBLIC CLASS METHODS //

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


  // PUBLIC INSTANCE METHODS //

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
    try {
      String  id    = (String) ms.getId();
      String  type  = ms.getType().getName();
      String  name  = ms.getName();
      HibernateSubject.add(id, type, name);
    }
    catch (HibernateException eH) {
      throw new RuntimeException(eH);
    }
  } // public void addSubject(ms)

}

