/*--
$Id: DecisionImpl.java,v 1.1 2005-08-25 20:31:35 acohen Exp $
$Date: 2005-08-25 20:31:35 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
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
