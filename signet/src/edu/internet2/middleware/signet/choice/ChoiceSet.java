/*--
$Id: ChoiceSet.java,v 1.3 2005-01-24 18:54:27 acohen Exp $
$Date: 2005-01-24 18:54:27 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.choice;

import java.util.Set;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Subsystem;

/**
 * Represents a collection of possible Choice values.
 */
public interface ChoiceSet
{
	/**
	 * Returns ID of this ChoiceSet.
	 */
	public String getId();
	
	/**
	 * Indicates if this ChoiceSet is restricted to the returned
	 * Subsystem. A null value indicates this ChoiceSet
	 * is usable by any Subsystem.
	 */
	public Subsystem getSubsystem();

	/**
	 * Returns the ChoiceSet adapter.
	 */
	public ChoiceSetAdapter getChoiceSetAdapter();

	/**
	 * Returns all of the Choices contained by this ChoiceSet.
	 */
	public Set getChoices();
	
	/**
	 * Retrieves the Choice in a ChoiceSet with the specified value.
	 * @param value
	 * @return
	 * 
	 * @throws ChoiceNotFoundException
	 */
	public Choice getChoiceByValue(String value)
	throws ChoiceNotFoundException;

  /**
   * Adds a Choice to a ChoiceSet.
   * 
   * @param choiceValue the internal value of this Choice.
   * @param choiceLabel the displayable value of this Choice.
   * @param choiceDisplayOrder the order in which this Choice should
   * 		appear within its ChoiceSet.
   * @param choiceRank used to determine the relative magnitude
   * 		of Choices within a ChoiceSet.
   * @return the new Choice.
   * 
   * @throws OperationNotSupportedException if this ChoiceSet
   * 	is read-only.
   */
  public Choice addChoice
  	(String choiceValue,
  	 String choice_label,
  	 int 		choice_display_order,
  	 int 		choice_rank)
  throws OperationNotSupportedException;

}
