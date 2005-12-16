/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.group;

import  com.devclue.grouper.session.*;
import  edu.internet2.middleware.grouper.*;
import  java.util.*;

/**
 * Query for groups within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupQ.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class GroupQ {

  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new GroupQ object.
   * <pre class="eg">
   * GroupQ gq = new GroupQ();
   * </pre>
   */
  public GroupQ() {
    // Nothing
  } // public GroupQ()


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Query each group named <b>like</b> the command line arguments.
   * <p />
   * <p>Groups printed to STDOUT if found, STDERR otherwise.</p>
   * <p>Exits with 0 if all groups are found, 1 otherwise.</p>
   * <pre class="eg">
   * // Query for groups named like <i>com:example</i>
   * % java com.devclue.grouper.group.GroupQ com:example
   *
   * // Query for groups named like <i>com:example</i> and
   * // <i>org:example</i>
   * % java com.devclue.grouper.group.GroupQ com:example org:example
   * </pre>
   */
  public static void main(String[] args) {
    int       ev    = 0;
    GroupQ    gq    = new GroupQ();
    Iterator  iter  = Arrays.asList(args).iterator(); 
    while (iter.hasNext()) {
      String  name    = (String) iter.next();
      Set     groups  = gq.getGroups(name);
      if (groups.size() > 0) {
        Iterator groupIter = groups.iterator();
        while (groupIter.hasNext()) {
          Group g = (Group) groupIter.next(); 
          System.out.println(
            g.getUuid() + "," + g.getName() + "," + g.getDisplayName() 
          );
        }
      }
      else {
        System.err.println("Group not found: " + name);
        ev = 1; 
      }
    }
    System.exit(ev);
  } // public static void main(args)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Perform fuzzy query for groups by <i>name</i> and <i>displayName</i>.
   * <pre class="eg">
   * // Query for a groups named like <i>com:example</i>
   * GroupQ gq      = new GroupQ();
   * Set    groups  = gq.getGroups("com:example");
   * </pre>
   * @param   name  Name to query on.
   * @return  Set of found groups.
   */
  // DESIGN Should I make this exact?  Or maybe just add a getGroup()
  //        method?
  public Set getGroups(String name) {
    try {
      GrouperSession  s     = SessionFactory.getSession();
      Stem            root  = StemFinder.findRootStem(s);
      GrouperQuery    gq    = GrouperQuery.createQuery(
        s, 
        new GroupNameFilter(name, root)
      );
      return gq.getGroups();
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  } // public Set getGroups(name)

}

