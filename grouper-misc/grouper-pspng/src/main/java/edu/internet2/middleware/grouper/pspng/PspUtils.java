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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITGroup;
import org.apache.log4j.MDC;


public class PspUtils {
  public static final String THREAD_ID_MDC = "pspng.threadid";

  private static final ThreadLocal<String> threadId = new ThreadLocal<>();
  private static final AtomicInteger threadCounter = new AtomicInteger(0);

  /**
   * Return a (unique) id for the current thread. This is both useful for logging because it is
   * short and differentiates between two threads that might have the same name.
   *
   * @return
   */
  public static String getThreadId() {
    if ( threadId.get() == null )
      threadId.set("t-" + threadCounter.incrementAndGet());

    return threadId.get();
  }

  /**
   * A method that does PSPNG's standard thread setup. Presently, this is assigning a short
   * threadId to the thread and putting that id into log4j's MDC. All new threads created by
   * PSPNG should call this.
   */
  public static void setupNewThread() {
    MDC.put(THREAD_ID_MDC, getThreadId());
  }

  /**
   * chops a list into sublists of length L
   * 
   * Adapted from: polygenelubricants at StackOverflow
   * http://stackoverflow.com/questions/2895342/java-how-can-i-split-an-arraylist-in-multiple-small-arraylists
   * @param list
   * @param L
   * @return
   */
  //
  static <T> List<List<T>> chopped(Collection<T> items, final int L) {
     List<T> list;
     
     // Convert items to a List if it isn't one already
     if ( items instanceof List )
       list = (List<T>) items;
     else
       list = new ArrayList<T>(items);
     
     List<List<T>> parts = new ArrayList<List<T>>();
     final int N = list.size();
     for (int i = 0; i < N; i += L) {
         parts.add(list.subList(i, Math.min(N, i + L)));
     }
     return parts;
  }
  
  public static Map<String, Object> getStemAttributes(Group group) {
    // In order for stems closer to the group to take precedence, 
    // we need the stem path in reverse order (from root to group's parent)
    // We do this by walking up the stem path from the group to the root
    // and save them and then reverse them. 
    List<Stem> groupStemPath = new ArrayList<Stem>();
    Stem stem = group.getParentStem();
    while ( stem != null ) {
      groupStemPath.add(stem);
      
      if ( stem.isRootStem() )
        stem = null;
      else
        stem = stem.getParentStem();
    }
    Collections.reverse(groupStemPath);
    
    
    // OverallAttributeValues: Stores all Stem attributes from root to parent stem
    // If an attribute appears in multiple parent stems: 
    //   Single-Valued: stem closest to the group wins (parent attributes take prec over grandparent)
    //   Multi-Valued: All the attribute values are merged into a list of values for the attribute
    Map<String, Object> stemPathAttributes = new HashMap<String, Object>();
    
    for ( Stem aStem : groupStemPath ) {
      Set<AttributeAssign> attributeAssigns = aStem.getAttributeDelegate().getAttributeAssigns();
      for ( AttributeAssign attributeAssign : attributeAssigns ) {
        AttributeDef attributeDef = attributeAssign.getAttributeDef();
        AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
        String attributeName = attributeDefName.getName();
        
        Set<AttributeAssignValue> attributeAssignValues = attributeAssign.getValueDelegate().getAttributeAssignValues();
        
        List<String> attributeValues = new ArrayList<String>();
        
        for ( AttributeAssignValue attributeAssignValue : attributeAssignValues ) {
          String value = attributeAssignValue.getValueFriendly();
          attributeValues.add(value);
        }
        
        // Skip the attribute if it doesn't actually have any values
        if ( attributeValues.size() == 0 )
          continue;
        
        if ( ! (attributeDef.isMultiValued() || attributeDef.isMultiAssignable()) ) 
          // Single-Valued: Put the first value into map, replacing whatever was there
          stemPathAttributes.put(attributeName, attributeValues.iterator().next());
        else {
          // Multi-valued: Put all the values into a collection
          Collection<Object> valueArray = (Collection<Object>) stemPathAttributes.get(attributeName);
          if ( valueArray == null ) 
            stemPathAttributes.put(attributeName, attributeValues);
          else
            valueArray.addAll(attributeValues);
        }
      }
    }
    return stemPathAttributes;
  }

  public static Map<String, Object> getGroupAttributes(Group group) {
    Map<String, Object> result = new HashMap<String, Object>();

    // Perhaps this should start with something like
    // an iteration over group.getAttributeDelegate().getAttributeAssigns()
    for ( AttributeAssign attributeAssign : group.getAttributeDelegate().getAttributeAssigns()) {
      AttributeDef attributeDef = attributeAssign.getAttributeDef();
      AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
      String attributeName = attributeDefName.getName();
      
      Set<AttributeAssignValue> attributeAssignValues = attributeAssign.getValueDelegate().getAttributeAssignValues();
      
      List<String> attributeValues = new ArrayList<String>();
      
      for ( AttributeAssignValue attributeAssignValue : attributeAssignValues ) {
        String value = attributeAssignValue.getValueFriendly();
        attributeValues.add(value);
      }
      
      // Skip the attribute if it doesn't actually have any values
      if ( attributeValues.size() == 0 )
        continue;
      
      if ( ! (attributeDef.isMultiValued() || attributeDef.isMultiAssignable()) ) 
        // Single-Valued: Put the first value into map, replacing whatever was there
        result.put(attributeName, attributeValues.iterator().next());
      else {
        // Multi-valued: Put all the values into a collection
        Collection<Object> valueArray = (Collection<Object>) result.get(attributeName);
        if ( valueArray == null ) 
          result.put(attributeName, attributeValues);
        else
          valueArray.addAll(attributeValues);
      }
    }
    return result;
  }

  public static Map<String, Object> getGroupAttributes(PITGroup pitGroup) {
    return Collections.EMPTY_MAP;
  }
}
