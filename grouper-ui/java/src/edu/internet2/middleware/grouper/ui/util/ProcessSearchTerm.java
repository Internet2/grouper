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
package edu.internet2.middleware.grouper.ui.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.SearchTermProcessor;
import edu.internet2.middleware.subject.Source;

/**
 * Utility class to provide access to a local implementation of searchTermProcessor interface.
 * Any exceptions that are generated are displayed to the user in the form of an error
 * message.
 * 
 * @author Catherine Jewell.
 * @version $Id: ProcessSearchTerm.java,v 1.3 2009-08-12 04:52:14 mchyzer Exp $
 */

public class ProcessSearchTerm {

	public String processSearchTerm(Source source, String searchTerm, HttpServletRequest request){
		
		Exception generalException = null;
		String sourceId = source.getId();
		
		//Get the ResourceBundle which contains key / values
  		//we will check in our template finding algorithm
  		ResourceBundle mediaBundle = GrouperUiFilter.retrieveSessionMediaResourceBundle();
  		
  		try{
  			String keyValue = null;
  			
  			try {
  				keyValue=(String) mediaBundle.getString("subject.search.term.process." + sourceId);
  			}catch(MissingResourceException mre) {}
  			
  			if (keyValue != null){
  				
  				Class t = Class.forName(keyValue);
  				
  				SearchTermProcessor processor = (SearchTermProcessor)t.newInstance();
  				String locallyProcessedSearchTerm = processor.processSearchTerm(source, searchTerm, request);
  				return locallyProcessedSearchTerm;
  				
  			}
  			
  			return searchTerm;
  			
  		} catch(MissingResourceException e) {
  			
  			generalException = new Exception(e.getMessage());
  			
  		} catch(ClassNotFoundException cnfe) {
  			
  			generalException = new Exception(cnfe.getMessage());
  			
  		} catch (InstantiationException ie) {
  			
  			generalException = new Exception(ie.getMessage());
  			
  		} catch (IllegalAccessException iae) {
  			
  			generalException = new Exception(iae.getMessage());
  		}
		
  		if (generalException != null) {
  			request.setAttribute("message", new Message(
  					"subject.message.error.process-search-term", new String[] {source.getName(), generalException.getMessage()}, true));
					
  		}
  		
		return searchTerm;
		
	}
}
