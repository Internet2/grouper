/*--
$Id: ChoiceSetAdapter.java,v 1.1 2005-01-12 23:49:24 mnguyen Exp $
$Date: 2005-01-12 23:49:24 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.choice;

import edu.internet2.middleware.signet.AdapterUnavailableException;

public interface ChoiceSetAdapter {

	public ChoiceSet getChoiceSet(String id)
		throws ChoiceSetNotFoundException;
	
	public void init()
		throws AdapterUnavailableException;
	
}
