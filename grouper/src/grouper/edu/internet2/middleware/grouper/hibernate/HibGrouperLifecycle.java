/**
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
 */
/*
 * @author mchyzer
 * $Id: HibGrouperLifecycle.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;


/**
 * callbacks for hib grouper lifecycle events
 */
public interface HibGrouperLifecycle {

  /**
   * after an update occurs
   * @param hibernateSession 
   */
  public void onPostUpdate(HibernateSession hibernateSession);
  
  /**
   * after a save (insert) occurs
   * @param hibernateSession 
   */
  public void onPostSave(HibernateSession hibernateSession);
  
  /**
   * before an update occurs
   * @param hibernateSession 
   */
  public void onPreUpdate(HibernateSession hibernateSession);
  
  /**
   * before a save (insert) occurs
   * @param hibernateSession 
   */
  public void onPreSave(HibernateSession hibernateSession);

  /**
   * after a delete occurs
   * @param hibernateSession 
   */
  public void onPostDelete(HibernateSession hibernateSession);

  /**
   * before a delete (insert) occurs
   * @param hibernateSession 
   */
  public void onPreDelete(HibernateSession hibernateSession);
  
}
