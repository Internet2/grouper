/*--
$Id: Limit.java,v 1.12 2005-07-21 07:41:00 acohen Exp $
$Date: 2005-07-21 07:41:00 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet;

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
