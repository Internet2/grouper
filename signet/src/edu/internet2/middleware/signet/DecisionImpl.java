/*--
$Id: DecisionImpl.java,v 1.2 2006-02-09 10:19:30 lmcrae Exp $
$Date: 2006-02-09 10:19:30 $

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


class DecisionImpl implements Decision
{
  private boolean answer;
  private Reason  reason;
  private Limit   limit;
  
  public DecisionImpl
    (boolean  answer,
     Reason   reason,
     Limit    limit)
  {
    this.answer = answer;
    this.reason = reason;
    this.limit = limit;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Decision#getAnswer()
   */
  public boolean getAnswer()
  {
    return this.answer;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Decision#getReason()
   */
  public Reason getReason()
  {
    return this.reason;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Decision#getLimit()
   */
  public Limit getLimit()
  {
    return this.limit;
  }

}
