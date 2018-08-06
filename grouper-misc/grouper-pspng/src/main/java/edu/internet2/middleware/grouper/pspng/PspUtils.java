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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITGroup;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.ldaptive.LdapEntry;


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
   * @param items
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

  // PIT Groups do not have any attributes
  public static Map<String, Object> getGroupAttributes(PITGroup pitGroup) {
    return Collections.EMPTY_MAP;
  }


  /**
   * This method returns the first RDN of an LDAP DN. If there weren't commas for other reasons, this
   * would mean that it returns the part of the DN before the first comma. However, there can be commas
   * for different reasons, so this is more complicated
   *
   * Here are some examples:
   *   Easy:
   *   cn=xyz,ou=department,dc=example,dc=edu would return cn=xyz
   *   JEXL, no comma:
   *   cn=${group.name},ou=department,dc=example,dc=edu would return cn=${group.name}
   *
   *   JEXL, commas in method parameter lists:
   *   cn=${group.name.substring(1,3)},ou=department,dc=example,dc=edu would return cn=${group.name.substring(1,3)}
   *
   *   Nested JEXL:
   *   cn=${Hash.md5(${group.name})},ou=department,dc=example,dc=edu would return cn=${Hash.md5(${group.name}}
   *
   *  There are other ways commas need to be ignored (escaped, etc):
   *    cn=abc\,123,ou=department,dc=example,dc=edu would return cn=abc\,123
   *
   * @param dnAttributeValue
   * @return
   */
  public static String getFirstRdnString(final String dnAttributeValue) {
    // We are going to replace JEXL and comma-escaping strings with jibberish, equal-length strings
    // until there aren't any more of them. Then, the first comma will actually be the separator
    // between the first and second RDN components
    //
    // This approach was chosen because java doesn't have support for recursive regexes and this is
    // simpler than a real parser

    String s = dnAttributeValue;

    // Removed some escaped patterns: \\ and \,
    s = replaceMatchesWithUnderscores(s, Pattern.compile("\\\\"));
    s = replaceMatchesWithUnderscores(s, Pattern.compile("\\,"));

    // Match ${function()} but not nested ${function(${value})} (fwiw, the inner ${value} would be matched)
    s = replaceMatchesWithUnderscores(s, Pattern.compile("\\$\\{[^{}]*\\}"));

    int indexOfFirstComma = s.indexOf(',');
    if ( indexOfFirstComma < 0 ) {
      return dnAttributeValue;
    }
    else {
      // This is why we've kept s to be the same length of dnAttributeValue:
      //   we can use the index of the first comma in s to find the first DN-relevant comma in
      //   the original dnAttributeValue
      return dnAttributeValue.substring(0, indexOfFirstComma);
    }
  }

  /**
   * This is used by getFirstRdnString() in order to iteratively remove parts of a string
   * that match a pattern. However, this method does not just delete the matched characters,
   * it instead replaces them with an equal number of underscores, KEEPING THE INPUT AND
   * RESULT STRINGS THE SAME LENGTH
   *
   * @param s
   * @param jexlPattern
   * @return
   */
  private static String replaceMatchesWithUnderscores(String s, Pattern jexlPattern) {
    Matcher m = jexlPattern.matcher(s);
    while (m.find()) {
      String matched = m.group();

      // Replace the occurrence with an equal-length string of underscores
      String newString = s.replace(matched, StringUtils.repeat("_", matched.length()) );

      s=newString;
      m=jexlPattern.matcher(s);
    }

    return s;
  }
}
