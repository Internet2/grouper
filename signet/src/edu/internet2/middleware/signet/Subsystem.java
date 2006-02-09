/*--
$Id: Subsystem.java,v 1.11 2006-02-09 10:25:35 lmcrae Exp $
$Date: 2006-02-09 10:25:35 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
*     <li>Subsystem</li>
*     <li>Category</li>
*     <li>Function</li>
*     <li>Permission</li>
*     <li>Tree</li>
* </ul>
*
* The following Subsystem-related entities will appear in subsequent
* releases:
* <ul>
*     <li>Condition</li>
*     <li>ProxyType</li>
*     <li>Limit</li>
*     <li>LimitChoice</li>
*     <li>Prerequisite</li>
* </ul>
* 
*/

public interface Subsystem
extends HelpText, NonGrantable, Name, Comparable
{
  /**
   * Gets the ID of this entity.
   * 
   * @return Returns a short mnemonic id which will appear in XML
   *    documents and other documents used by analysts.
   */
  public String getId();
  
  /**
   * Gets the Categories currently associated with this Subsystem.
   * 
   * @return Returns the categories currently associated with this
   *    Subsystem.
   */
  public Set getCategories();
  
  /**
   * Gets a single Category currently associated with this Subsystem.
   * 
   * @return Returns the specified Category.
   */
  public Category getCategory(String categoryId)
  throws ObjectNotFoundException;
  
  /**
   * Gets the Functions currently associated with this Subsystem.
   * 
   * @return Returns the functions currently associated with this
   *    Subsystem.
   */
  public Set getFunctions();

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
   * Add a Limit to the set of Limits that are associated with this
   * Subsystem.
   * 
   * @param limit
   */
  public void add(Limit limit);
  
  /**
   * Get the ChoiceSets currently associated with this Subsystem.
   * 
   * @return a Set containing all the ChoiceSets currently associated with
   * this Subsystem.
   */
  public Set getChoiceSets();
  
  /**
   * Gets a single ChoiceSet associated with this Subsystem by its ID.
   * @param id
   * @return the specified ChoiceSet
   * 
   * @throws ObjectNotFoundException
   */
  public ChoiceSet getChoiceSet(String id)
  throws ObjectNotFoundException;
  
  /**
   * Get the Limits currently associated with this Subsystem.
   * 
   * @return a Map of the Limits, indexed by limitId.
   */
  public Map getLimits();

  /**
   * Gets a single Limit associated with this Subsystem by its ID.
   * @param id
   * @return the specified Limit
   */
  public Limit getLimit(String id)
  throws ObjectNotFoundException;
  
  /**
   * Get the Permissions currently associated with this Subsystem.
   * 
   * @return a Map of the Permissions, indexed by permissionId.
   */
  public Map getPermissions();

  /**
   * Gets a single Permission associated with this Subsystem by its ID.
   * @param id
   * @return the specified Permission.
   */
  public Permission getPermission(String id)
  throws ObjectNotFoundException;
}
