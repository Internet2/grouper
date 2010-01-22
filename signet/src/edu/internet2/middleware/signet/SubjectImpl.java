/*--
$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/SubjectImpl.java,v 1.6 2006-04-10 06:28:11 ddonn Exp $

Copyright (c) 2006 Internet2, Stanford University

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

public class SubjectImpl implements Subject
{
	// This is the metadata that describes the pre-defined Signet
	// application subject.
	private static final String		SIGNET_SUBJECT_ID = "signet";
	private static final String		SIGNET_NAME = "Signet";
	private static final String		SIGNET_DESC	= "the Signet system";


	public SubjectImpl()
	{
		super();
	}


	public String getId()
	{
		return SIGNET_SUBJECT_ID;
	}

	public String getSubjectTypeId()
	{
		return SubjectTypeEnum.APPLICATION.getName();
	}

	public SubjectType getType()
	{
		return SubjectTypeEnum.APPLICATION;
	}

	public String getName()
	{
		return (SIGNET_NAME);
	}

	public String getDescription()
	{
		return (SIGNET_DESC);
	}

	public String getAttributeValue(String name)
	{
		return null;
	}

	public Set getAttributeValues(String name)
	{
		return new HashSet();
	}

	public Map getAttributes()
	{
		return new HashMap();
	}

	public Source getSource()
	{
		return null;
	}
}
