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

import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * Interface which DefaultComparatorImpl uses to obtain appropriate comparison String
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: GrouperComparatorHelper.java,v 1.2 2007-04-17 08:40:07 isgwb Exp $
 */

public interface GrouperComparatorHelper extends Serializable {
	
	/**
	 * Determine the correct String for sorting
	 * @param obj
	 * @param config
	 * @param context
	 * @return a String, based on context, used to sort this object
	 */
	public String getComparisonString(Object obj,ResourceBundle config,String context);

}
