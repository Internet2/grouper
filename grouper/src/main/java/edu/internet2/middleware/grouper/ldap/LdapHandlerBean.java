/**
 * Copyright 2014 Internet2
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
 * $Id: HibernateHandlerBean.java,v 1.1 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ldap;


/**
 * bean with reference to the ldap object
 * @param <T> 
 */
public class LdapHandlerBean<T> {
    
  /** ldap object */
  private T ldap;

  /**
   * ldap object
   * @return ldap object
   */
  public T getLdap() {
    return this.ldap;
  }

  /**
   * ldap object
   * @param ldap1
   */
  public void setLdap(T ldap1) {
    this.ldap = ldap1;
  }
}
