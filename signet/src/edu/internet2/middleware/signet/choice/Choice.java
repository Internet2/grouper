/*--
$Id: Choice.java,v 1.2 2005-01-21 20:30:47 acohen Exp $
$Date: 2005-01-21 20:30:47 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.choice;

public interface Choice
{
	/*
	 * Returns the enclosing ChoiceSet of this Choice.
	 */
	public ChoiceSet getChoiceSet() throws ChoiceSetNotFoundException;
	
	/*
	 * Returns the value associated with this Choice.
	 */
	public String getValue();
	
	/*
	 * Returns a user-friendly representation of the value associated
	 * with this Choice.
	 */
	public String getDisplayValue();
	
	/*
	 * Returns an int that is the relative display order of this
	 * Choice among other Choices within a ChoiceSet.
	 */
	public int getDisplayOrder();

	/*
	 * Returns an int that is used for comparing this Choice 
	 * among other Choices within a ChoiceSet.
	 */
	public int getRank();

}
