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
import edu.internet2.middleware.grouper.GrouperAccessAdapter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.grouper.internal.util.Realize;


/** 
 * Factory for returning a <code>AccessResolver</code>.
 * @author  blair christensen.
 * @version $Id: AccessResolverFactory.java,v 1.6 2009-08-11 20:18:08 mchyzer Exp $
 * @since   1.2.1
 */
public class AccessResolverFactory {


  private static AccessResolver   resolver;
  private static ParameterHelper  param     = new ParameterHelper();;



  /**
   * @return  <code>AccessResolver</code> instance.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static AccessResolver getInstance(GrouperSession session)
    throws  IllegalArgumentException
  {
    return getInstance(
             session, (AccessAdapter) Realize.instantiate( GrouperAccessAdapter.class.getName() )
           )
           ;
  }
    
  /**
   * Returns chain of access resolvers.
   * <p>Order of execution:</p>
   * <ol>
   *  <li>{@link ValidatingAccessResolver}</li>
   *  <li>{@link WheelAccessResolver}</li>
   *  <li>{@link CachingAccessResolver}</li>
   *  <li>{@link GrouperSystemAccessResolver}</li>
   *  <li>{@link GrouperAllAccessResolver}</li>
   *  <li>{@link AccessWrapper}</li>
   * </ol>
   * @return  <code>AccessResolver</code> instance.
   * @throws  GrouperException if unable to get instance.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static AccessResolver getInstance(GrouperSession session, AccessAdapter access) 
    throws  GrouperException,
            IllegalArgumentException
  {
    param.notNullGrouperSession(session).notNullAccessAdapter(access);
    return new ValidatingAccessResolver(
      new WheelAccessResolver(
        new CachingAccessResolver(
          new GrouperSystemAccessResolver(
            new GrouperAllAccessResolver(
              new AccessWrapper(session, access)
            )
          )
        )
      )
    );
  }

  /**
   * @return  Singleton <code>AccessResolver</code>.
   * @throws  GrouperException if unable to get resolver.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static AccessResolver getResolver(GrouperSession session) 
    throws  GrouperException,
            IllegalArgumentException
  {
    if (resolver == null) {
      resolver = getInstance(session);
    }
    return resolver;
  }

}

