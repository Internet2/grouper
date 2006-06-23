/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.subject;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;

/**
 * Query for subjects using Grouper's configured sources.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectQ.java,v 1.1 2006-06-23 17:30:12 blair Exp $
 */
public class SubjectQ {

  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new SubjectQ object.
   * <pre class="eg">
   * SubjectQ sq = new SubjectQ();
   * </pre>
   */
  public SubjectQ() {
    // Nothing 
  } // public SubjectQ()


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Query the subject given as a command line argument along with an
   * optional type.
   * <p>Subject is printed to STDOUT is found.<//p>
   * <p>Exits with 0 if subject found, 1 otherwise.</p>
   * <pre class="eg">
   * // Query for <i>person</i> subject <i>john</i>
   * % java com.devclue.grouper.subject.SubjectQ john
   * 
   * // Query for <i>person</i> subject <i>john</i>
   * % java com.devclue.grouper.subject.SubjectQ john person
   * 
   * // Query for <i>group</i> subject <i>com:example</i>
   * % java com.devclue.grouper.subject.SubjectQ com:example group
   * </pre>
   */
  public static void main(String[] args) {
    int       ev    = 1;
    SubjectQ  sq    = new SubjectQ();
    Subject   subj  = null;
    try {
      if (args.length == 1) {
        subj = sq.getSubject(args[0]);
      }
      else if (args.length == 2) {
        subj = sq.getSubject(args[0], args[1]);
      } 
      else {
        System.err.println("Invalid number of arguments: " + args.length);  
      }
    }
    catch (SubjectNotFoundException e) {
      System.err.println("Subject not found: " + args[0]);
    }
    if (subj != null) {
      ev = 0;
      System.out.println(
        subj.getId()      + "," + subj.getType().getName() + ","
        + subj.getName() 
      );
    } 
    System.exit(ev);
  } // public static void main(args)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Query a {@link Subject} by id or other well-known identifier.
   * <pre class="eg">
   * // Query for a <i>person</i> subject with an identity of
   * // <i>john</i>.
   * SubjectQ sq = new SubjectQ();
   * try {
   *   Subject subj = sq.getSubject("john");
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   id  Id or other well-known identifier to query on.
   * @return  Subject
   */
  public Subject getSubject(String id) 
    throws SubjectNotFoundException
  {
    try {
      return SubjectFinder.findById(id);
    }
    catch (SubjectNotFoundException e) {
      return SubjectFinder.findByIdentifier(id);
    }
  } // public Subject getSubject(id)

  /**
   * Query a {@link Subject} by id or other well-known identifier and
   * type.
   * <pre class="eg">
   * // Query for a <i>person</i> subject with an identity of
   * // <i>john</i>.
   * SubjectQ sq = new SubjectQ();
   * try {
   *   Subject subj = sq.getSubject("john", "person");
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *
   * // Query for a <i>group</i> subject with an identity of
   * // <i>com:example</i>.
   * try {
   *   Subject subj = sq.getSubject("com:example", "group");
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   id  Id or other well-known identifier to query on.
   * @return  Subject
   */
  public Subject getSubject(String id, String type) 
    throws SubjectNotFoundException
  {
    try {
      try {
        return SubjectFinder.findById(id, type);
      }
      catch (SubjectNotFoundException e) {
        return SubjectFinder.findByIdentifier(id, type);
      }
    }
    catch (Exception e) {
      throw new SubjectNotFoundException(e.getMessage());
    }
  } // public Subject getSubject(id, type)
}

