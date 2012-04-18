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
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui;

import edu.internet2.middleware.subject.*;

/**
 * Pluggable interface thats allows site-specific rules for naming Personal
 * stems. To use this feature, the key <i>plugin.personalstem </i> in
 * resources/media.properties must be set to the name of a Class which
 * implements this interface
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: PersonalStem.java,v 1.4 2007-04-11 08:19:24 isgwb Exp $
 */

public interface PersonalStem {
	/**
	 * @param subject
	 *            owner of personal stem
	 * @return name of stem where personal stems are located
	 * @throws Exception
	 */
	public String getPersonalStemRoot(Subject subject) throws Exception;

	/**
	 * @param subject
	 *            owner of personal stem
	 * @return id - extension of personal stem
	 * @throws Exception
	 */
	public String getPersonalStemId(Subject subject) throws Exception;

	/**
	 * @param subject
	 *            owner of personal stem
	 * @return displayName - displayExtension of personal stem
	 * @throws Exception
	 */
	public String getPersonalStemDisplayName(Subject subject) throws Exception;

	/**
	 * @param subject
	 *            owner of personal stem
	 * @return description of personal stem
	 * @throws Exception
	 */
	public String getPersonalStemDescription(Subject subject) throws Exception;
}
