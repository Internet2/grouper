/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.stem;
import  com.devclue.grouper.session.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;

/**
 * Add stems to the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemAdd.java,v 1.1 2006-06-23 17:30:12 blair Exp $
 */
public class StemAdd {

  // CONSTRUCTORS //

  /**
   * Create a new StemAdd object.
   * <pre class="eg">
   * StemAdd nsa = new StemAdd();
   * </pre>
   */
  public StemAdd() {
    // Nothing
  } // public StemAdd()


  // MAIN //
  /**
   * Add stem or root stem depending upon command line arguments.
   * <p>Stem is printed to STDOUT if created.</p>
   * <p>Exits with 0 if stem created, 1 otherwise.</p>
   * <pre class="eg">
   * // Create a root stem with the extension <i>com</i>.
   * % java com.devclue.grouper.stem.StemAdd com
   * 
   * // Create a stem with extension <i>example</i> within stem 
   * // <i>com</i>.
   * % java com.devclue.grouper.stem.StemAdd com example
   * </pre>
   */
  public static void main(String[] args) {
    int         ev  = 1;
    StemAdd     nsa = new StemAdd();
    Stem ns  = null;
    try {
      if      (args.length == 1) {
        ns = nsa.addRootStem(args[0]);
      }
      else if (args.length == 2) {
        ns = nsa.addStem(args[0], args[1]);
      } 
      else {
        System.err.println("Invalid number of arguments: " + args.length);
      }
    }
    catch (RuntimeException e) {
      System.err.println("Error creating stem: " + e.getMessage());
    }
    if (ns != null) {
      ev = 0;
      System.out.println(
        ns.getUuid() + "," + ns.getName() + "," + ns.getDisplayName()
      );
    }
    System.exit(ev);
  } // public static void main(args)


  // PUBLIC INSTANCE METHODS //

  /**
   * Add a root stem.
   * <p />
   * <pre class="eg">
   * // Create a root stem with the extension <i>com</i>.
   * StemAdd nsa = new StemAdd();
   * try {
   *   Stem ns = nsa.addRootStem("com");
   * } 
   * catch (RuntimeException e) {
   *   // Stem not created
   * }
   * </pre>
   * @param   extension Create root stem with this extension.
   * @return  Created stem.  
   */
  public Stem addRootStem(String extension) {
    try {
      GrouperSession  s     = SessionFactory.getSession();
      Stem            root  = StemFinder.findRootStem(s); 
      return root.addChildStem(extension, extension);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  } // public Stem addRootStem(extension)

  /**
   * Add a stem.
   * <pre class="eg">
   * // Create a stem with the extension <i>example</i> within the stem
   * // <i>com</i>.
   * StemAdd nsa = new StemAdd();
   * try {
   *   Stem ns = nsa.addStem("com", "example");
   * } 
   * catch (RuntimeException e) {
   *   // Stem not created
   * }
   * </pre>
   * @param   stem      Create stem within this stem.
   * @param   extension Create stem with this extension.
   * @return  Created stem.  
   */
  public Stem addStem(String stem, String extension) {
    try {
      GrouperSession  s       = SessionFactory.getSession();
      Stem            parent  = StemFinder.findByName(s, stem);
      return parent.addChildStem(extension, extension);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  } // public Stem addStem(stem, extension)

}

