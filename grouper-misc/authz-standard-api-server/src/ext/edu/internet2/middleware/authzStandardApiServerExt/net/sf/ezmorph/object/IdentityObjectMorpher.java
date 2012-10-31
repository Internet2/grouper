/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.authzStandardApiServerExt.net.sf.ezmorph.object;

import edu.internet2.middleware.authzStandardApiServerExt.net.sf.ezmorph.object.IdentityObjectMorpher;
import edu.internet2.middleware.authzStandardApiServerExt.net.sf.ezmorph.ObjectMorpher;

/**
 * Morpher that performs no conversion.<br>
 * This morpher is a singleton.
 * 
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public final class IdentityObjectMorpher implements ObjectMorpher
{
   private static final IdentityObjectMorpher INSTANCE = new IdentityObjectMorpher();

   /**
    * Returns the singleton instance
    */
   public static IdentityObjectMorpher getInstance()
   {
      return INSTANCE;
   }

   private IdentityObjectMorpher()
   {
   }

   public boolean equals( Object obj )
   {
      return INSTANCE == obj;
   }

   public int hashCode()
   {
      return 42 + getClass().hashCode();
   }

   public Object morph( Object value )
   {
      return value;
   }

   public Class morphsTo()
   {
      return Object.class;
   }

   public boolean supports( Class clazz )
   {
      return true;
   }
}