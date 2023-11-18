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

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.subject.Source;

/**
 * Sites can add custom form elements to the subject search screen.
 * This interface allows the UI to pre-process a searchTerm plus Source specific
 * request parameters to give an appropriate query string for the Source.
 *  
 * @author Catherine Jewell.
 * @version $Id: SearchTermProcessor.java
 */
public interface SearchTermProcessor {

	/**
	 * formats SearchTerm according to searchTerm parameters passed on the
	 * request
	 * 
	 * @param source - the Source Adapter that is currently in use.
	 * @param searchTerm - the string entered into the search box.
	 * @param request - carries other request parameters that may be used to process the searchTerm. 
	 * 
	 * @return String - the processed searchTerm
	 */
	public String processSearchTerm(Source source, String searchTerm, HttpServletRequest request);
}
