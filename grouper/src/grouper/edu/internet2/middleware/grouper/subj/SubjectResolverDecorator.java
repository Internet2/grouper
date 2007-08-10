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

package edu.internet2.middleware.grouper.subj;


/**
 * Decorator for {@link SubjectResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectResolverDecorator.java,v 1.2 2007-08-10 13:19:14 blair Exp $
 * @since   @HEAD@
 */
public abstract class SubjectResolverDecorator implements SubjectResolver {

 
  private SubjectResolver decorated;

 
  /**
   * @param   resolver  <i>SubjectResolver</i> to decorate.
   * @throws  IllegalArgumentException if <i>resolver</i> is null.
   * @since   @HEAD@
   */
  public SubjectResolverDecorator(SubjectResolver resolver) 
    throws  IllegalArgumentException
  {
    if (resolver == null) { // TODO 20070807 ParameterHelper
      throw new IllegalArgumentException("null SubjectResolver");
    }
    this.decorated = resolver;
  }


  /**
   * @return  Decorated <i>SubjectResolver</i>.
   * @throws  IllegalStateException if no decorated <i>SubjectResolver</i>.
   * @since   @HEAD@
   */
  public SubjectResolver getDecoratedResolver() 
    throws  IllegalStateException
  {
    if (this.decorated == null) {
      throw new IllegalStateException("null decorated SubjectResolver");
    }
    return this.decorated;
  }

}

