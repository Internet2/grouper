/*--
 $Id: LimitShape.java,v 1.1 2005-02-01 19:48:20 acohen Exp $
 $Date: 2005-02-01 19:48:20 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

/**
 * This is a typesafe enumeration that identifies the various shapes
 * a Signet Limit may have.
 *  
 */
public class LimitShape
	extends TypeSafeEnumeration
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   *          the external name of the LimitShape value.
   * @param description
   *          the human readable description of the LimitShape value,
   * 					by which it is presented in the user interface.
   */
  private LimitShape(String name, String description)
  {
    super(name, description);
  }

  /**
   * The instance that represents a ChoiceSet.
   */
  public static final LimitShape CHOICE_SET
  	= new LimitShape("choice_set", "A set of discrete choices");

  /**
   * The instance that represents a Tree.
   */
  public static final LimitShape TREE
  	= new LimitShape
  			("tree",
  			 "A hierarchical tree, with the possibility of multiple roots and multiple parents for each node.");
}