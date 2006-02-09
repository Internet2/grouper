/*--
$Id: Limit.java,v 1.14 2006-02-09 10:21:36 lmcrae Exp $
$Date: 2006-02-09 10:21:36 $

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

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
* Limit organizes a group of {@link Choice}s. Each Assignment definition
* includes some number of Limits.
* 
*/

public interface Limit
extends Comparable
{
  /**
   * Get the ID of this Limit.
   * 
   * @return the limit ID.
   */
  public String getId();
  
  /**
   * Get the Subsystem associated with this Limit.
   * 
   * @return the associated Subsystem.
   * @throws ObjectNotFoundException
   */
  public Subsystem getSubsystem();
  
  /**
   * Get the ChoiceSet associated with this Limit.
   * 
   * @return the ChoiceSet associated with this Limit.
   */
  public ChoiceSet getChoiceSet();
  
  /**
   * Get the name of this Limit.
   * 
   * @return the limit name.
   */
  public String getName();
  
  /**
   * Get the help-text associated with this Limit.
   * 
   * @return the help text for this limit.
   */
  public String getHelpText();
  
  /**
   * Get the renderer associated with this Limit.
   * 
   * @return the renderer for this Limit, which is the name of a JSP file
   * that implements a Struts Tile for display of this Limit.
   */
  public String getRenderer();
  
  /**
   * displayOrder indicates the relative order in which Limits should be
   * displayed in a GUI.
   * 
   * @return the displayOrder of this Limit - lower numbers are displayed
   * before higher numbers.
   */
  public int getDisplayOrder();
  
  /**
   * A Limit's DataType can be used to fine-tune its display to a user.
   * @return the DataType of this Limit.
   */
  public DataType getDataType();
  
  /**
   * selectionType indicates whether this Limit is single-valued or
   * multi-valued.
   * @return the SelectionType of this Limit.
   */
  public SelectionType getSelectionType();

  /**
   * Persists the current state of this Limit.
   */
  public void save();
}
