/*--
$Id: Subsystem.java,v 1.3 2005-01-21 20:30:47 acohen Exp $
$Date: 2005-01-21 20:30:47 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.tree.Tree;


/**
* Subsystem describes a business entity within the
* enterprise, such as the financial department.
* <p>
* For our September release, the following Subsystem-related entities
* will be implemented:
* <ul>
* 		<li>Subsystem</li>
* 		<li>Category</li>
* 		<li>Function</li>
* 		<li>Permission</li>
* 		<li>Tree</li>
* </ul>
*
* The following Subsystem-related entities will appear in subsequent
* releases:
* <ul>
* 		<li>Condition</li>
* 		<li>ProxyType</li>
* 		<li>Limit</li>
* 		<li>LimitChoice</li>
* 		<li>Prerequisite</li>
* </ul>
* 
*/

public interface Subsystem
extends HelpText, Entity, Name, Comparable
{
  /**
   * Gets the Categories currently associated with this Subsystem.
   * 
   * @return Returns the categories currently associated with this
   * 		Subsystem.
   */
  public Set getCategories();
  
  /**
   * Gets the Functions currently associated with this Subsystem.
   * 
   * @return Returns the functions currently associated with this
   * 		Subsystem.
   */
  public Set getFunctions();
  
  /**
   * Sets the Functions that should be associated with this Subsystem.
   * 
   * @param categories the functions that should be associated with
   * 		this Subsystem.
   */
  public void setFunctionsArray(Function[] categories);

  /**
   * Gets a single Function associated with this Subsystem by its ID.
   * @param functionId
   * @return the specified Function
   */
  public Function getFunction(String functionId)
  throws ObjectNotFoundException;

  /**
   * Gets the Tree currently associated with this Subsystem.
   * 
   * @return the Tree currently associated with this Subsystem.
   */
  public Tree getTree();
  
  /**
   * Set the Tree which should be associated with this Subsystem.
   * @param tree
   */
  public void setTree(Tree tree);
  
  /**
   * Add a Category to the set of Categories that are associated with this
   * Subsystem.
   * 
   * @param category
   */
  public void add(Category category);
  
  /**
   * Get the ChoiceSets currently associated with this Subsystem.
   * @param choiceSetId
   * 
   * @return a Map of the ChoiceSets, indexed by choiceSetId.
   */
  public Map getChoiceSets();
  
  /**
   * Gets a single ChoiceSet associated with this Subsystem by its ID.
   * @param choiceSetId
   * @return the specified ChoiceSet
   * 
   * @throws ObjectNotFoundException
   */
  public ChoiceSet getChoiceSet(String id)
  throws ObjectNotFoundException;
}