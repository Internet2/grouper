/*--
$Id: Choice.java,v 1.1 2005-01-12 23:49:24 mnguyen Exp $
$Date: 2005-01-12 23:49:24 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.choice;

public interface Choice {

	/*
	 * Returns the ID of this Choice.
	 */
	public String getId();
	
	/*
	 * Returns a system value associated with this Choice.
	 */
	public String getValue();
	
	/*
	 * Returns a user-friendly value associated with this Choice.
	 */
	public String getLabel();
	
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
