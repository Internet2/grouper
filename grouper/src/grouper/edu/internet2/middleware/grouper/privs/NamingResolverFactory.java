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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.privs;
import edu.internet2.middleware.grouper.exception.GrouperException;
import  edu.internet2.middleware.grouper.GrouperSession;
import  edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import  edu.internet2.middleware.grouper.internal.util.Realize;


/** 
 * Factory for returning a <code>NamingResolver</code>.
 * @author  blair christensen.
 * @version $Id: NamingResolverFactory.java,v 1.6 2009-08-11 20:18:08 mchyzer Exp $
 * @since   1.2.1
 */
public class NamingResolverFactory {
  // TODO 20070820 DRY w/ access resolution


  private static NamingResolver   resolver;
  private static ParameterHelper  param     = new ParameterHelper();;



  /**
   * @return  <code>NamingResolver</code> instance.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static NamingResolver getInstance(GrouperSession session)
    throws  IllegalArgumentException
  {
    return getInstance(
             session, (NamingAdapter) Realize.instantiate( GrouperConfig.getProperty( ApiConfig.NAMING_PRIVILEGE_INTERFACE ) )
           )
           ;
  }
    
  /**
   * Returns chain of naming resolvers.
   * <p>Order of execution:</p>
   * <ol>
   *  <li>{@link ValidatingNamingResolver}</li>
   *  <li>{@link WheelNamingResolver}</li>
   *  <li>{@link CachingNamingResolver}</li>
   *  <li>{@link GrouperSystemNamingResolver}</li>
   *  <li>{@link GrouperAllNamingResolver}</li>
   *  <li>{@link NamingWrapper}</li>
   * </ol>
   * @return  <code>NamingResolver</code> instance.
   * @throws  GrouperException if unable to get instance.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static NamingResolver getInstance(GrouperSession session, NamingAdapter naming) 
    throws  GrouperException,
            IllegalArgumentException
  {
    param.notNullGrouperSession(session).notNullNamingAdapter(naming);
    return new ValidatingNamingResolver(
      new WheelNamingResolver( 
        new CachingNamingResolver(
          new GrouperSystemNamingResolver(
            new GrouperAllNamingResolver(
              new NamingWrapper(session, naming)
            )
          )
        )
      )
    );
  }

  /**
   * @return  Singleton <code>NamingResolver</code>.
   * @throws  GrouperException if unable to get resolver.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static NamingResolver getResolver(GrouperSession session) 
    throws  GrouperException,
            IllegalArgumentException
  {
    if (resolver == null) {
      resolver = getInstance(session);
    }
    return resolver;
  }

}

