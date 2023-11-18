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
 * $Id: Hib3SessionInterceptor.java,v 1.2 2008-07-28 20:12:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * hibernate should be able to tell if an assigned key and version
 * are insert or update, but it cant, so tell it
 */
public class Hib3SessionInterceptor extends EmptyInterceptor implements Serializable {

  /**
   * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
   */
  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
      Object[] previousState, String[] propertyNames, Type[] types) {
    if (entity instanceof Hib3GrouperVersioned) {
      //find the current version
      long newVersion = (Long)HibUtils.propertyValue(currentState, propertyNames, 
          GrouperAPI.FIELD_HIBERNATE_VERSION_NUMBER) + 1;
      //increment by one, granted this should always by 0
      HibUtils.assignProperty(currentState, propertyNames, 
          GrouperAPI.FIELD_HIBERNATE_VERSION_NUMBER, newVersion);
      //assign back to object since might not flush in time
      GrouperUtil.assignField(entity, GrouperAPI.FIELD_HIBERNATE_VERSION_NUMBER, newVersion);
      //edited it
      return true;
    }
    //not edited
    return false;
  }

  /**
   * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
   */
  @Override
  public boolean onSave(Object entity, Serializable id, Object[] state,
      String[] propertyNames, Type[] types) {
    if (entity instanceof Hib3GrouperVersioned) {
      //find the current version
      long newVersion = (Long)HibUtils.propertyValue(state, propertyNames, 
          GrouperAPI.FIELD_HIBERNATE_VERSION_NUMBER) + 1;
      //increment by one, granted this should always by 0
      HibUtils.assignProperty(state, propertyNames, 
          GrouperAPI.FIELD_HIBERNATE_VERSION_NUMBER, newVersion);
      //assign back to object since might not flush in time
      GrouperUtil.assignField(entity, GrouperAPI.FIELD_HIBERNATE_VERSION_NUMBER, newVersion);
      //edited it
      return true;
    }
    //not edited
    return false;
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see org.hibernate.EmptyInterceptor#isTransient(java.lang.Object)
   */
  @Override
  public Boolean isTransient(Object entity) {
    if (entity instanceof Hib3GrouperVersioned) {
      return ((GrouperAPI)entity).getHibernateVersionNumber() < 0;
    }
    return super.isTransient(entity);
  }

}
