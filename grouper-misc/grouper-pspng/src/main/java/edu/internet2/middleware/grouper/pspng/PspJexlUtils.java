package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.util.GrouperUtilElSafe;


/**
 * This class is included in the variable namespace created when JEXL 
 * expressions are evaluated. This class's purpose it to help simplify
 * some jexl expressions. 
 * 
 *  -- containedWithin(item, collections...) will search multiple
 *  java arrays or collections for 'item.' This class will transparently
 *  handle null collections.
 *  
 * @author bert
 *
 */
public class PspJexlUtils extends GrouperUtilElSafe {
  
  /**
   * This is a null-safe and flexible method for seeing if an item is a member
   * of one or more arrays or collections.
   * 
   * @param item
   * @param arraysOrCollections varargs of Java arrays or collections
   * 
   * @return True iff item is a member of (or equal to) one of the arrays or collections.
   */
  public static boolean containedWithin(Object item, Object... arraysOrCollections) {
    for (Object arrayOrCollection : arraysOrCollections ) {
      if ( arrayOrCollection == null )
        continue;
      
      if ( arrayOrCollection instanceof Collection ) {
        if ( ((Collection) arrayOrCollection).contains(item) )
          return true;
      } else if ( arrayOrCollection instanceof Object[] ) {
        if ( Arrays.asList((Object[]) arrayOrCollection).contains(item) )
          return true;
      } else {
        if ( arrayOrCollection.equals(item) )
          return true;
      }
    }
    
    return false;
  }
  
  public static String bushyDn(String groupName, String rdnAttributeName, String ouAttributeName) {
    StringBuilder result = new StringBuilder();
    
    List<String> namePieces=Arrays.asList(groupName.split(":"));
    Collections.reverse(namePieces);
    
    /// Work through the pieces backwards. The first is rdn=X and the others are ou=X
    for (int i=0; i<namePieces.size(); i++) {
      if ( result.length() != 0 )
        result.append(',');
      
      String piece = namePieces.get(i);
      if (i==0)
        result.append(rdnAttributeName);
      else
        result.append(ouAttributeName);
      
      result.append('=');
      result.append(piece);
    }
    return result.toString();
  }

}
