/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.stem;

import  bsh.*;
import  com.devclue.grouper.session.*;
import  edu.internet2.middleware.grouper.*;
import  java.util.*;

/**
 * Query for stems within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemQ.java,v 1.2 2006-05-25 15:20:19 blair Exp $
 */
public class StemQ {

  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new StemQ object.
   * <pre class="eg">
   * StemQ sq = new StemQ();
   * </pre>
   */
  public StemQ() {
    // Nothing
  } // public StemQ()


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Query each stem named <b>like</b> the command line arguments.
   * <p>Stems printed to STDOUT if found.</p>
   * <p>Exits with 0 if all stems found, 1 otherwise.</p>
   * <pre class="eg">
   * // Query for stems named like <i>com</i>
   * % java com.devclue.grouper.stem.StemQ com
   *
   * // Query for stems named like <i>com:example</i> and
   * // <i>org:example</i>
   * % java com.devclue.grouper.stem.StemQ com:example org:example
   * </pre>
   */
  public static void main(String[] args) {
    int       ev      = 0;
    StemQ     nsq     = new StemQ();
    Iterator  iter    = Arrays.asList(args).iterator(); 
    while (iter.hasNext()) {
      String name = (String) iter.next();
      Set stems = nsq.getStems(name);
      if (stems.size() > 0) {
        Iterator stemIter = stems.iterator();
        while (stemIter.hasNext()) {
          Stem ns = (Stem) stemIter.next(); 
          System.out.println(
            ns.getUuid() + "," + ns.getName() + "," + ns.getDisplayName() 
          );
        }
      }
      else {
        System.err.println("Stem not found: " + name);
        ev = 1; 
      }
    }
    System.exit(ev);
  } // public static void main(args)

  public static void invoke(Interpreter env, CallStack stack, String name) {
    System.err.println("QUERY FOR STEM WITH ARGUMENT (" + name + ")");
  } // public static void invoke(env, stack, name)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Perform fuzzy query for stems by <i>name</i> and <i>displayName</i>.
   * <pre class="eg">
   * // Query for stems named like <i>com:example</i>.
   * StemQ  sq    = new StemQ();
   * Set    stems = sq.getStems("com:example");
   * </pre> 
   * @param   name  Name to query on.
   * @return  Set of found stems.
   */
  public Set getStems(String name) {
    try {
      GrouperSession  s     = SessionFactory.getSession();
      Stem            root  = StemFinder.findRootStem(s);
      GrouperQuery    gq    = GrouperQuery.createQuery(
        s, 
        new StemNameFilter(name, root)
      );
      return gq.getStems();
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  } // public Set getStems(name)

}

