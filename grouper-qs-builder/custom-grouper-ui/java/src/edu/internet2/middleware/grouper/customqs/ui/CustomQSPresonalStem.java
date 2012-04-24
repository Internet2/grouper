/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

package edu.internet2.middleware.grouper.customqs.ui;

import edu.internet2.middleware.grouper.ui.PersonalStem;
import edu.internet2.middleware.subject.Subject;

/**
 * Example implementation of PersonalStemInterface
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CustomQSPresonalStem.java,v 1.1 2005-12-14 15:24:11 isgwb Exp $
 */

public class CustomQSPresonalStem implements PersonalStem {

	/**
	 * 
	 */
	public CustomQSPresonalStem() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.PersonalStem#getPersonalStemRoot(edu.internet2.middleware.subject.Subject)
	 */
	public String getPersonalStemRoot(Subject subject) throws Exception {
		// TODO Auto-generated method stub
		return "qsuob:personal";
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.PersonalStem#getPersonalStemId(edu.internet2.middleware.subject.Subject)
	 */
	public String getPersonalStemId(Subject subject) throws Exception {
		return subject.getId();
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.PersonalStem#getPersonalStemDisplayName(edu.internet2.middleware.subject.Subject)
	 */
	public String getPersonalStemDisplayName(Subject subject) throws Exception {
		return subject.getName();
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.PersonalStem#getPersonalStemDescription(edu.internet2.middleware.subject.Subject)
	 */
	public String getPersonalStemDescription(Subject subject) throws Exception {
		return "Personal Stem for: " + subject.getName();
	}

}
