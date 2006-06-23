/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.group;

import  com.devclue.grouper.session.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;

/**
 * Add groups to the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupAdd.java,v 1.1 2006-06-23 17:30:10 blair Exp $
 */
public class GroupAdd {

  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new GroupAdd object.
   * <pre class="eg">
   * GroupAdd ga = new GroupAdd();
   * </pre>
   */
  public GroupAdd() {
    // Nothing
  } // public GroupAdd()


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Create group within the stem given as command line arguments.
   * <p>Group is printed to STDOUT if created.</p>
   * <p>Exits with 0 if group created, 1 otherwise.</p>
   * <pre class="eg">
   * // Create a group with extension <i>example</i> within stem
   * // <i>com</i>.
   * % java com.devclue.grouper.group.GroupAdd com example
   * </pre>
   */
  public static void main(String[] args) {
    int           ev  = 1;
    GroupAdd      ga  = new GroupAdd();
    Group  g   = null;
    try {
      if (args.length == 2) {
        g = ga.addGroup(args[0], args[1]); 
      }
      else {
        System.err.println("Invalid number of arguments: " + args.length);
      }
    }
    catch (RuntimeException e) {
      System.err.println("Error creating group: " + e.getMessage());
    }
    if (g !=null) {
      ev = 0;
      System.out.println(
        g.getUuid() + "," + g.getName() + "," + g.getDisplayName()
      );
    }
    System.exit(ev);
  } // public static void main(args)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Add a group.
   * <pre class="eg">
   * // Create a group with extension <i>example</i> within stem
   * // <i>com</i>.
   * GroupAdd ga = new GroupAdd();
   * try {
   *   Group g = ga.addGroup("com", "example");
   * }
   * catch (RuntimeException e) {
   *   // Group not created
   * }
   * </pre>
   * @param   stem      Create group within this stem.
   * @param   extension Create group with this extension.
   * @return  Created group.
   */
  public Group addGroup(String stem, String extension) {
    try {   
      GrouperSession  s       = SessionFactory.getSession();
      Stem            parent  = StemFinder.findByName(s, stem);
      return parent.addChildGroup(extension, extension);
    } 
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  } // public Group addGroup(stem, extension)

}

