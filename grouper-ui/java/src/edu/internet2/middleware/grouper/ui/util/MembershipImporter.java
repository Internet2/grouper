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

import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;

import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Interface that reads data and tries to create memberships.  
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MembershipImporter.java,v 1.5 2009-08-12 04:52:14 mchyzer Exp $
 */

public interface MembershipImporter {
	/**
	 * Parses data to resolve to a Subject and attempts to add the Subject to the
	 * Group membership. If a Subject cannot be resolved and error count is incremented - 
	 * and returned, and an error message is written to output. This method would normally 
	 * be called indirectly from MembershipImportManager, which reads configuration information from 
	 * an XML file. Implementation specific configuration is encoded as XML attributes
	 * @param group
	 * @param input
	 * @param output
	 * @param config
	 * @param nav
	 * @return count of errors encountered during load
	 * @throws IOException
	 */
	public int load(Group group,Reader input,PrintWriter output,Element config,Field field) throws IOException,SchemaException;
}
