/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.member;

import  com.devclue.grouper.group.*;
import  com.devclue.grouper.session.*;
import  com.devclue.grouper.subject.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;

/**
 * Add group memberships to the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberAdd.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class MemberAdd {

  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new MemberAdd object.
   * <pre class="eg">
   * MemberAdd ma = new MemberAdd();
   * </pre>
   */
  public MemberAdd() {
    // Nothing
  } // public MemberAdd()


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Add a subject to the named group.
   * <p>Prints member to STDOUT if subject was added to the group.</p>
   * <p>Exits with 0 if the subject was added, 1 otherwise.</p>
   * <pre class="eg">
   * // Add subject <i>john</i> to the group <i>com:example</i>.
   * % java com.devclue.grouper.member.MemberAdd com:example john
   * 
   * // Add <i>person</i> subject <i>john</i> to the group <i>com:example</i>.
   * % java com.devclue.grouper.member.MemberAdd com:example john person
   * 
   * // Add <i>group</i> subject <i>org:example</i> to the group <i>com:example</i>.
   * % java com.devclue.grouper.member.MemberAdd com:example org:example group
   * </pre>
   */
  public static void main(String[] args) {
    int           ev  = 1;
    Member m   = null;
    if (args.length >= 2 && args.length <= 3) {
      String type = args.length == 3 ? args[2] : "person";
      try {
        MemberAdd ma  = new MemberAdd();
                  m   = ma.addMember(args[0], args[1], type);
      }
      catch (SubjectNotFoundException e) {
        System.err.println(
          "Unable to add " + args[1] + "/" + type + " to " + args[0]
        );
      }
    }
    else {
      System.err.println("Invalid number of arguments: " + args.length);
    }
    if (m != null) {
      ev = 0;
      System.out.println(
        m.getUuid() + "," + m.getSubjectId() + "," + m.getSubjectTypeId()
      );
    }
    System.exit(ev);
  } // public static void main(args)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Add I2MI Subject to a group.
   * <pre class="eg">
   * // Make subject <i>subj</i> a member of group <i>g</i>.
   * MemberAdd ma = new MemberAdd();
   * try {
   *   Member m = ma.addMember(g, subj);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not added as member
   * }
   * </pre>
   * @param   g     Create membership in this group.
   * @param   subj  Make this subject a member.
   * @return  Created member.
   */
  public Member addMember(Group g, Subject subj) 
    throws SubjectNotFoundException
  {
    try {
      GrouperSession s = SessionFactory.getSession();
      g.addMember(subj);
      return MemberFinder.findBySubject(s, subj);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  } // public Member addMember(g, subj)


  /**
   * Add I2MI Subject to a group.
   * <pre class="eg">
   * // Make subject with <i>id</i> and <i>type</i> a member of the
   * // group named <i>group</i>.
   * MemberAdd ma = new MemberAdd();
   * try {
   *   Member m = ma.addMember(group, id, type);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not added as member
   * }
   * </pre>
   * @param   group Create membership in this group.
   * @param   id    Make this subject id a member.
   * @param   type  Subject is of this type.
   * @return  Created member.
   */
  public Member addMember(String group, String id, String type)
    throws SubjectNotFoundException
  {
    try {
      GrouperSession s = SessionFactory.getSession();

      // We need a subject
      SubjectQ  sq    = new SubjectQ();
      Subject   subj  = sq.getSubject(id, type);

      // And a group
      Group g = GroupFinder.findByName(s, group);

      return this.addMember(g, subj); 
    }
    catch (SubjectNotFoundException eSNF) {
      throw new SubjectNotFoundException(eSNF.getMessage());
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  } // public Member addMember(group, id, type)

}

