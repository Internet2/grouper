/*--
$Id: Entity.java,v 1.8 2006-02-09 10:19:35 lmcrae Exp $
$Date: 2006-02-09 10:19:35 $

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

import java.util.Date;

import edu.internet2.middleware.signet.Status;

/**
* This interface specifies some common methods that every 
* Signet entity must provide.
* 
*/
interface Entity
{  
  /**
   * Saves the current state of this Signet entity, and any Signet entities
   * that it refers to.
   *
   */
  public void save();

  /**
   * Gets the <code>Status</code> of this entity. {@link Assignment}s and
   * {@link Proxy}s may have the status <code>ACTIVE</code>,
   * <code>INACTIVE</code>, or <code>PENDING</code>, but all other entities may
   * only have the status <code>ACTIVE</code> or <code>INACTIVE</code>.
   * 
   * @return Returns the status of this entity.
   */
  public Status getStatus();
  
  /**
   * Gets the date and time this entity was first created.
   * 
   * @return Returns the date and time this entity was first created.
   */
  public Date getCreateDatetime();
}
