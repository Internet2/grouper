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
package edu.internet2.middleware.grouper.hibernate;




/**
 * @version $Id: HibernateMisc.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 * @author mchyzer
 */
public class HibernateMisc extends HibernateDelegate {

  /**
   * @param theHibernateSession
   */
  public HibernateMisc(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }

  /**
   * Flush the underlying hibernate session (sync the object model with the DB).
   * This doesnt commit or anything, it just sends the bySql across
   */
  public void flush() {
    
    HibernateSession.assertNotGrouperReadonly();
    
    this.getHibernateSession().getSession().flush();
  }

}
