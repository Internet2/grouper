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
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;


/**
 * Decorator for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NamingResolverDecorator.java,v 1.2 2007-08-27 15:53:53 blair Exp $
 * @since   1.2.1
 */
public abstract class NamingResolverDecorator implements NamingResolver {
  // TODO 20070820 DRY w/ access resolution

 
  private NamingResolver  decorated;
  private ParameterHelper param;


 
  /**
   * @param   resolver  <i>NamingResolver</i> to decorate.
   * @throws  IllegalArgumentException if <i>resolver</i> is null.
   * @since   1.2.1
   */
  public NamingResolverDecorator(NamingResolver resolver) 
    throws  IllegalArgumentException
  {
    this.param      = new ParameterHelper();
    this.param.notNullNamingResolver(resolver);
    this.decorated  = resolver;
  }


  /**
   * @return  Decorated <i>NamingResolver</i>.
   * @throws  IllegalStateException if no decorated <i>NamingResolver</i>.
   * @since   1.2.1
   */
  public NamingResolver getDecoratedResolver() 
    throws  IllegalStateException
  {
    if (this.decorated == null) { // TODO 20070816 StateHelper
      throw new IllegalStateException("null decorated NamingResolver");
    }
    return this.decorated;
  }

}

