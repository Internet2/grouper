/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/SignetAppSource.java,v 1.7 2007-08-07 23:26:19 ddonn Exp $

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

	@author ddonn
*/
package edu.internet2.middleware.signet.subjsrc;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/** A simplified SignetSource that contains the Signet Application Subject
 * (i.e. super subject).
 * @see SignetSources
 */
public class SignetAppSource extends SignetSource
{
	/** The Source name for the Signet Application subject source */
	public static final String	SIGNET_SOURCE_ID = "signetApplicationSource";
	/** The subject name for the Signet App Subject */
	public static final String	SIGNET_NAME = "Signet";
	/** The description value for the Signet App subject */
	public static final String	SIGNET_DESC	= "The " + SIGNET_NAME + " System";
	/** The subject ID value for the Signet App Subject */
	public static final String	SIGNET_SUBJECT_ID = "Super_" + SignetSubject.class.getSimpleName();

	/** the Signet Super Subject */
	protected SignetSubject		signetSubject;


	/**
	 * Constructor creates of a "super" Subject Source
	 */
	public SignetAppSource(SignetSources sources, String id)
	{
		super();
		setSources(sources);
		setId(id);
		setName(id);
		setStatus(SignetSource.STATUS_ACTIVE);
		setSubjectType(SubjectTypeEnum.APPLICATION.getName());
		signetSubject = null;

		initAttributes();
	}


	/**
	 * Initailize the hard-coded attributes for the Signet "super" subject
	 */
	protected void initAttributes()
	{
		// the only attribute (for now) is Description
		String descAttr = signetSources.getPersistedSource().getSignetDescription();
		addMappedAttribute(descAttr, descAttr);
	}


	////////////////////////////////////
	// overrides SignetSource
	////////////////////////////////////

	/**
	 * Overrides SignetSubject#getSubject(String). There is no corresponding
	 * Source in the SubjectAPI for Signet's own Source/Subject. Therefore, we
	 * create a Subject from scratch.
	 * @see edu.internet2.middleware.signet.subjsrc.SignetSource#getSubject(java.lang.String)
	 */
	public Subject getSubject(String subjectId)
	{
		SignetSubject retval = null;

		if (SignetAppSource.SIGNET_SUBJECT_ID.equals(subjectId))
		{
			if (null == signetSubject)
			{
				signetSubject = new SignetSubject();
				signetSubject.setSource(this);

				signetSubject.setId(SignetAppSource.SIGNET_SUBJECT_ID);
				signetSubject.setName(SignetAppSource.SIGNET_NAME);
				signetSubject.setType(SubjectTypeEnum.APPLICATION.getName());
				signetSubject.setDescription(SignetAppSource.SIGNET_DESC);
				signetSubject.setSynchDatetime(new Date(0L));
			}
			retval = signetSubject;
		}

		return (retval);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.subjsrc.SignetSource#getSubjectByIdentifier(java.lang.String)
	 */
	public Subject getSubjectByIdentifier(String id)
	{
		return (getSubject(id));
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.subjsrc.SignetSource#getSubjects()
	 */
	public Vector getSubjects()
	{
		Vector retval = new Vector();

		Subject subj = getSubject(SignetAppSource.SIGNET_SUBJECT_ID);
		retval.add(subj);

		return (retval);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.subjsrc.SignetSource#search(java.lang.String)
	 */
	public Set search(String searchValue)
	{
		Set retval = new HashSet();

		if (getSubject(SignetAppSource.SIGNET_SUBJECT_ID).getId().equals(searchValue))
			retval.add(signetSubject);

		return (retval);
	}

}
