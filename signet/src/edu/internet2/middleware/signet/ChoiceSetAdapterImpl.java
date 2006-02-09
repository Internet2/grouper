/*--
$Id: ChoiceSetAdapterImpl.java,v 1.4 2006-02-09 10:18:50 lmcrae Exp $
$Date: 2006-02-09 10:18:50 $

Copyright 2006 Internet2, Stanford University

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

import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.choice.ChoiceSetNotFound;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

class ChoiceSetAdapterImpl implements ChoiceSetAdapter
{
  Signet signet;
  
  /**
   * Every implementation of ChoiceSetAdapter is required to have
   * a public parameterless constructor.
   */
  public ChoiceSetAdapterImpl()
  {
    super();
    // TODO Auto-generated constructor stub
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSetAdapter#getChoiceSet(java.lang.String)
   */
  public ChoiceSet getChoiceSet(String id)
    throws ChoiceSetNotFound
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSetAdapter#init()
   */
  public void init() throws AdapterUnavailableException
  {
    // TODO Auto-generated method stub
  }

  /**
   * @param signet
   */
  public void setSignet(Signet signet)
  {
    this.signet = signet;
  }

}
