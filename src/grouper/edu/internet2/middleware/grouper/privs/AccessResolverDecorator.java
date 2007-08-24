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
 * Decorator for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessResolverDecorator.java,v 1.1 2007-08-24 14:18:16 blair Exp $
 * @since   @HEAD@
 */
public abstract class AccessResolverDecorator implements AccessResolver {

 
  private AccessResolver  decorated;
  private ParameterHelper param;


 
  /**
   * @param   resolver  <i>AccessResolver</i> to decorate.
   * @throws  IllegalArgumentException if <i>resolver</i> is null.
   * @since   @HEAD@
   */
  public AccessResolverDecorator(AccessResolver resolver) 
    throws  IllegalArgumentException
  {
    this.param      = new ParameterHelper();
    this.param.notNullAccessResolver(resolver);
    this.decorated  = resolver;
  }


  /**
   * @return  Decorated <i>AccessResolver</i>.
   * @throws  IllegalStateException if no decorated <i>AccessResolver</i>.
   * @since   @HEAD@
   */
  public AccessResolver getDecoratedResolver() 
    throws  IllegalStateException
  {
    if (this.decorated == null) { // TODO 20070816 StateHelper
      throw new IllegalStateException("null decorated AccessResolver");
    }
    return this.decorated;
  }

}

