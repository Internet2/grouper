/*--
  $Id: SubjectType.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject;

/**
A SubjectType may be a person, organization, or group.

*/

public interface SubjectType
{
	public String getId();

	public String getName();

	public SubjectTypeAdapter getAdapter();
}
