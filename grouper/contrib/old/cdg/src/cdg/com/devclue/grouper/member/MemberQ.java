/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.member;

import  com.devclue.grouper.session.*;
import  com.devclue.grouper.subject.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;

/**
 * Query for group memberships within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberQ.java,v 1.1 2006-06-23 17:30:10 blair Exp $
 */
public class MemberQ {

  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new MemberQ object.
   * <pre class="eg">
   * MemberQ mq = new MemberQ();
   * </pre>
   */
  public MemberQ() {
    // Nothing
  } // public MemberQ()


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Query for group memberships for the specified subject id and type.
   * <p>
   * Groups are printed to STDOUT if found.  The output format is:
   * </p>
   * <pre class="eg">
   * group id,group name,group display name 
   * </pre>
   * <p>Exits with 0 if memberships found, 1 otherwise.</p>
   * <pre class="eg">
   * // Query for groups where subject <i>john</i> is a member.
   * % java com.devclue.grouper.member.MemberQ john
   *
   * // Query for groups where <i>person</i> subject <i>john</i> is a member.
   * % java com.devclue.grouper.member.MemberQ john person
   *
   * // Query for groups where <i>group</i> subject <i>org:example</i> is a member.
   * % java com.devclue.grouper.member.MemberQ org:example group
   * </pre>
   */
  public static void main(String[] args) {
    int       ev    = 0;
    MemberQ   mq    = new MemberQ();
    if (args.length == 1 || args.length == 2) {
      String  id    = args[0];
      String  type  = 
        args.length == 2 ? args[1] : "person";
      try {
        Set groups = mq.getGroups(id, type);
        if (groups.size() > 0) {
          Iterator iter = groups.iterator();
          while (iter.hasNext()) {
            Group g = (Group) iter.next(); 
            System.out.println(
              g.getUuid() + "," + g.getName() + "," + g.getDisplayName()
            );
          } // while
        } 
        else {
          System.err.println("No group memberships found: " + id);
          ev = 1; 
        } // if (groups.size() > 0)
      } 
      catch (SubjectNotFoundException e) {
        System.err.println("Subject " + id + " not found");
        ev = 1;
      } // try
    } 
    else {
      System.err.println("Invalid number of arguments: " + args.length);
      ev = 1;
    } // if (args.length == 2)
    System.exit(ev);
  } // public static void main(args)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Query for group memberships.
   * <pre class="eg">
   * // Return groups where <i>person</i> subject <i>john</i> is a
   * // member.
   * MemberQ  mq      = new MemberQ();
   * Set      groups  = mq.getGroups("john", "person");
   * </pre>
   * @param   id    Return groups where this subject id is a member.
   * @param   type  Type of subject.
   * @return  Set of groups where subject is a member.
   */
  public Set getGroups(String id, String type)
    throws SubjectNotFoundException
  {
    try {
      // First get the Subject
      SubjectQ  sq    = new SubjectQ();
      Subject   subj  = sq.getSubject(id, type);

      // Then get the Member
      Member m = MemberFinder.findBySubject(
        SessionFactory.getSession(), subj
      );      

      // Then get the memberhips and extract the groups
      Set       groups  = new HashSet();
      Iterator  iter    = m.getMemberships().iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        groups.add( ms.getGroup() );
      }
    
      return groups;
    }
    catch (SubjectNotFoundException eSNF) {
      throw eSNF;
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  } // public Set getGroups(String id)

}

