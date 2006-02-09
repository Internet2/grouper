/*--
$Id: HTMLLimitRenderer.java,v 1.4 2006-02-09 10:21:17 lmcrae Exp $
$Date: 2006-02-09 10:21:17 $

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

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
interface HTMLLimitRenderer
{
  /**
   * @param string
   * @param set
   * @return
   */
  String render(String limitId, ChoiceSet choiceSet);
}
