/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
package edu.internet2.middleware.tierApiAuthzServer.util;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.ExpressionLanguageMissingVariableException;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiFolderInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupsMemberInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderLookup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupLookup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.tierApiAuthzServer.j2ee.TaasFilterJ2ee;
import edu.internet2.middleware.tierApiAuthzServer.version.TaasWsVersion;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;


/**
 * utility methods for the authz standard api server
 * 
 * @author mchyzer
 *
 */
public class StandardApiServerUtils extends StandardApiServerCommonUtils {

  /**
   * <pre>
   * split trim but dont deal with things in single or double quotes
   * e.g. if input is:     "someField:complicate.whatever"."someField:complicate.another"[2]
   * and you splitOn dot
   * then you should get two strings back:  "someField:complicate.whatever" and "someField:complicate.another"[2]
   * @param input
   * @param splitOn
   * @return the strings that are splittrimmed
   */
  public static String[] splitTrimQuoted(String input, String splitOn) {
    if (input == null) {
      return null;
    }
    
    // this is just too confusing, dont allow
    if (splitOn.contains("\"") || splitOn.contains("'")) {
      throw new RuntimeException("splitOn cannot contain single or double quotes: '" + splitOn + "'");
    }
    
    //if nothing to split on, dont worry about it
    if (!input.contains(splitOn)) {
      return new String[]{input.trim()};
    }
    //dont mess with it if no quotes
    if (!input.contains("\"") && !input.contains("'")) {
      return splitTrim(input, splitOn);
    }
    
    List<Integer> indices = indexOfsQuoted(input, splitOn);
    
    if (indices.size() == 0) {
      return new String[]{input.trim()};
    }
    
    List<String> resultList = new ArrayList<String>();
    
    int currentStart = 0;
    
    for (int i=0;i<indices.size();i++){
      int index = indices.get(i);
      
      //dont add an empty string at the beginning
      if (currentStart == 0 && index == 0) {
        currentStart += splitOn.length();
        continue;
      }
      
      //add the string
      String substring = input.substring(currentStart, index);
      resultList.add(substring.trim());
      
      currentStart += substring.length() + splitOn.length();
      
      //if we end with an empty string, ignore it
      if (currentStart >= input.length()-1) {
        break;
      }
    }

    //lets add the last string
    if (currentStart < input.length()) {
      String substring = input.substring(currentStart, input.length());
      resultList.add(substring.trim());
      
    }
    
    return resultList.toArray(new String[0]);
  }

  /**
   * <pre>
   * split trim but dont deal with things in single or double quotes or things in brackets
   * e.g. if input is:     "someField:complicate.whatever"."someField:complicate.another"[@attr='val']
   * and you splitOn dot
   * then you should get two strings back:  "someField:complicate.whatever" and "someField:complicate.another"[2]
   * @param input
   * @param splitOn
   * @return the strings that are splittrimmed
   */
  public static String[] splitTrimQuotedBracketed(String input, String splitOn) {
    if (input == null) {
      return null;
    }
    
    // this is just too confusing, dont allow
    if (splitOn.contains("\"") || splitOn.contains("'") || splitOn.contains("[") || splitOn.contains("]")) {
      throw new RuntimeException("splitOn cannot contain single or double quotes or brackets: '" + splitOn + "'");
    }
    
    //if nothing to split on, dont worry about it
    if (!input.contains(splitOn)) {
      return new String[]{input.trim()};
    }

    //dont mess with it if no quotes
    if (!input.contains("\"") && !input.contains("'") && !input.contains("[")) {
      return splitTrim(input, splitOn);
    }
    
    List<Integer> indices = indexOfsQuotedBracketed(input, splitOn);
    
    if (indices.size() == 0) {
      return new String[]{input.trim()};
    }
    
    List<String> resultList = new ArrayList<String>();
    
    int currentStart = 0;
    
    for (int i=0;i<indices.size();i++){
      int index = indices.get(i);
      
      //dont add an empty string at the beginning
      if (currentStart == 0 && index == 0) {
        currentStart += splitOn.length();
        continue;
      }
      
      //add the string
      String substring = input.substring(currentStart, index);
      resultList.add(substring.trim());
      
      currentStart += substring.length() + splitOn.length();
      
      //if we end with an empty string, ignore it
      if (currentStart >= input.length()-1) {
        break;
      }
    }

    //lets add the last string
    if (currentStart < input.length()) {
      String substring = input.substring(currentStart, input.length());
      resultList.add(substring.trim());
      
    }
    
    return resultList.toArray(new String[0]);
  }

  /**
   * see if a string contains a substring, but ignore things that are quoted (single or double)
   * @param input
   * @param substring
   * @return true if contains and false if not
   */
  public static boolean containsQuoted(String input, String substring) {
    List<Integer> indices = indexOfsQuoted(input, substring);
    return length(indices) > 0;
  }

  /**
   * find the first index of a substring but ignore quoted strings
   * @param input
   * @param substring
   * @return the first index or -1 if not there
   */
  public static int indexOfQuoted(String input, String substring) {
    List<Integer> indices = indexOfsQuoted(input, substring);
    if (length(indices) == 0) {
      return -1;
    }
    return indices.get(0);
  }
  
  /**
   * see if a string contains a substring, but ignore things that are quoted (single or double), or bracketed
   * @param input
   * @param substring
   * @return true if contains and false if not
   */
  public static boolean containsQuotedBracketed(String input, String substring) {
    List<Integer> indices = indexOfsQuotedBracketed(input, substring);
    return length(indices) > 0;
  }

  /**
   * find the first index of a substring but ignore quoted and bracketed strings
   * @param input
   * @param substring
   * @return the first index or -1 if not there
   */
  public static int indexOfQuotedBracketed(String input, String substring) {
    List<Integer> indices = indexOfsQuotedBracketed(input, substring);
    if (length(indices) == 0) {
      return -1;
    }
    return indices.get(0);
  }
  
  /**
   * find the last index of a substring but ignore quoted strings
   * @param input
   * @param substring
   * @return the last index or -1 if not there
   */
  public static int lastIndexOfQuoted(String input, String substring) {
    List<Integer> indices = indexOfsQuoted(input, substring);
    if (length(indices) == 0) {
      return -1;
    }
    return indices.get(indices.size()-1);
  }
  
  /**
   * find the last index of a substring but ignore quoted strings and bracketed strings
   * @param input
   * @param substring
   * @return the last index or -1 if not there
   */
  public static int lastIndexOfQuotedBracketed(String input, String substring) {
    List<Integer> indices = indexOfsQuotedBracketed(input, substring);
    if (length(indices) == 0) {
      return -1;
    }
    return indices.get(indices.size()-1);
  }
  
  /**
   * <pre>
   * get the indices where the substring occurs (dont worry about overlaps), and ignore quoted strings
   * if the input is ab..cd..ef
   * and the substring is ..
   * then return 2,6
   * if the input is ab..c"e..\" '.."d..ef
   * and the substring is ..
   * then return 2,17
   * 
   * </pre>
   * @param input
   * @param substring
   * @return the list of indices
   */
  public static List<Integer> indexOfsQuoted(String input, String substring) {
    
    if (input == null) {
      return null;
    }

    //ok, we have quotes...  lets get the indices
    List<Integer> indices = new ArrayList<Integer>();
    
    int inputLength = input.length();
    
    boolean inSingleQuotes = false;
    boolean inDoubleQuotes = false;
    
    OUTER:
    for (int i=0;i<inputLength;i++) {
      char curChar = input.charAt(i);
      boolean isSingleQuote = curChar == '\'';
      boolean isDoubleQuote = curChar == '\"';
      boolean isSlash = curChar == '\\';
      
      //if its a single quote, and you are not in quotes, then you are now in single quotes
      if (isSingleQuote && !inSingleQuotes && !inDoubleQuotes) {
        inSingleQuotes = true;
        continue;
      }
      //if its a double quote, and you are not in quotes, then you are now in double quotes
      if (isDoubleQuote && !inSingleQuotes && !inDoubleQuotes) {
        inDoubleQuotes = true;
        continue;
      }
      //if its a single quote, and you are in double quotes, then ignore
      if (isSingleQuote && inDoubleQuotes) {
        continue;
      }
      //if its a double quote, and you are in single quotes, then ignore
      if (isDoubleQuote && inSingleQuotes) {
        continue;
      }
      //if its a single quote, and we are in single quotes, then we arent in single quotes anymore
      if (isSingleQuote && inSingleQuotes) {
        inSingleQuotes = false;
        continue;
      }
      //if its a double quote, and we are in double quotes, then we arent in double quotes anymore
      if (isDoubleQuote && inDoubleQuotes) {
        inDoubleQuotes = false;
        continue;
      }
      //if its a slash and we are in quotes, then ignore the next char
      if (isSlash && (inDoubleQuotes || inSingleQuotes)) {

        //not sure why this would happen
        if (i == inputLength-1) {
          break;
        }
        //we are processing the escaped char
        i++;
        continue;
      }
      //if we are in single quotes or double quotes, ignore checking for 
      if (inSingleQuotes || inDoubleQuotes) {
        continue;
      }
      //lets see if we found the string
      //lets see if there is even space
      //string is ab..cd..ef
      //length is 10
      //index is 6
      //splitOn is ..
      //remaining length of string is length-index
      int remainingLength = inputLength - i;
      //we are done
      if (remainingLength < substring.length()) {
        break;
      }
      //see if equals
      for (int splitOnIndex = 0; splitOnIndex < substring.length(); splitOnIndex++) {
        if (input.charAt(i+splitOnIndex) != substring.charAt(splitOnIndex)) {
          continue OUTER;
        }
      }
      //the string was found!
      //keep track that we found it
      indices.add(i);
      //move the pointer forward
      i += substring.length()-1;
    }
    
    //now we have the indices
    return indices;
  }
  
  /**
   * <pre>
   * get the indices where the substring occurs (dont worry about overlaps), and ignore quoted strings, ignore bracketed strings.
   * inside bracketed strings, ignore quoted string.
   * if the input is ab..c[rf..yh]d..ef
   * and the substring is ..
   * then return 2,14
   * if the input is ab..c"e..\" '.."d..ef
   * and the substring is ..
   * then return 2,17
   * if the input is ab..c"e..\" '.."d["re..][]..rf"]..ef
   * and the substring is ..
   * then return 2,32
   * 
   * </pre>
   * @param input
   * @param substring
   * @return the list of indices
   */
  public static List<Integer> indexOfsQuotedBracketed(String input, String substring) {
    
    if (input == null) {
      return null;
    }

    // this is just too confusing, dont allow
    if (substring.contains("\"") || substring.contains("'") || substring.contains("[") || substring.contains("]")) {
      throw new RuntimeException("splitOn cannot contain single or double quotes or brackets: '" + substring + "'");
    }

    //ok, we have quotes...  lets get the indices
    List<Integer> indices = new ArrayList<Integer>();
    
    int inputLength = input.length();
    
    boolean inBrackets = false;
    boolean inSingleQuotes = false;
    boolean inDoubleQuotes = false;
    
    OUTER:
    for (int i=0;i<inputLength;i++) {
      char curChar = input.charAt(i);
      boolean isSingleQuote = curChar == '\'';
      boolean isDoubleQuote = curChar == '\"';
      boolean isOpenBracket = curChar == '[';
      boolean isCloseBracket = curChar == ']';
      boolean isSlash = curChar == '\\';
      
      //if its a single quote, and you are not in quotes, then you are now in single quotes
      if (isSingleQuote && !inSingleQuotes && !inDoubleQuotes) {
        inSingleQuotes = true;
        continue;
      }
      //if its a double quote, and you are not in quotes, then you are now in double quotes
      if (isDoubleQuote && !inSingleQuotes && !inDoubleQuotes) {
        inDoubleQuotes = true;
        continue;
      }
      //if its an open bracket, and you are not in brackets, then you are now in brackets
      if (isOpenBracket && !inSingleQuotes && !inDoubleQuotes) {
        inBrackets = true;
        continue;
      }
      //if its a single quote, and you are in double quotes, then ignore
      if (isSingleQuote && inDoubleQuotes) {
        continue;
      }
      //if its a double quote, and you are in single quotes, then ignore
      if (isDoubleQuote && inSingleQuotes) {
        continue;
      }
      //brackets dont count in quotes
      if ((isOpenBracket || isCloseBracket) && (inSingleQuotes || inDoubleQuotes)) {
        continue;
      }
      //if its a single quote, and we are in single quotes, then we arent in single quotes anymore
      if (isSingleQuote && inSingleQuotes) {
        inSingleQuotes = false;
        continue;
      }
      //if its a double quote, and we are in double quotes, then we arent in double quotes anymore
      if (isDoubleQuote && inDoubleQuotes) {
        inDoubleQuotes = false;
        continue;
      }
      //if its a close bracket and in brackets, then we arent in brackets anymore
      if (isCloseBracket && inBrackets) {
        inBrackets = false;
        continue;
      }
      //if its a slash and we are in quotes, then ignore the next char
      if (isSlash && (inDoubleQuotes || inSingleQuotes)) {

        //not sure why this would happen
        if (i == inputLength-1) {
          break;
        }
        //we are processing the escaped char
        i++;
        continue;
      }
      //if we are in single quotes or double quotes or brackets, ignore checking for 
      if (inSingleQuotes || inDoubleQuotes || inBrackets) {
        continue;
      }
      //lets see if we found the string
      //lets see if there is even space
      //string is ab..cd..ef
      //length is 10
      //index is 6
      //splitOn is ..
      //remaining length of string is length-index
      int remainingLength = inputLength - i;
      //we are done
      if (remainingLength < substring.length()) {
        break;
      }
      //see if equals
      for (int splitOnIndex = 0; splitOnIndex < substring.length(); splitOnIndex++) {
        if (input.charAt(i+splitOnIndex) != substring.charAt(splitOnIndex)) {
          continue OUTER;
        }
      }
      //the string was found!
      //keep track that we found it
      indices.add(i);
      //move the pointer forward
      i += substring.length()-1;
    }
    
    //now we have the indices
    return indices;
  }
  
  /**
   * make sure two arrays are equal (of simple objects)
   * @param a
   * @param b
   */
  public static void assertEqualsArray(Object a, Object b) {
    if (a == b) {
      return;
    }
    if (a == null) {
      throw new RuntimeException("expected null, but didnt get null");
    }
    if (b == null) {
      throw new RuntimeException("expected not null, but got null");
    }
    int lengthA = Array.getLength(a);
    int lengthB = Array.getLength(b);
    if (lengthA != lengthB) {
      throw new RuntimeException("Expected array of length " + lengthA + ", but received array of length: " + lengthB);
    }
    //loop through and see if equal
    for (int i=0;i<lengthA;i++) {
      Object objectA = Array.get(a, i);
      Object objectB = Array.get(b, i);
      if (!equals(objectA, objectB)) {
        throw new RuntimeException("Index " + i + ", not equal, expected: " + objectA + ", but received: " + objectB);
      }
    }
    //all good
  }

  /**
   * make sure two lists are equal (of simple objects)
   * @param a
   * @param b
   */
  public static void assertEqualsList(List<?> a, List<?> b) {
    if (a == b) {
      return;
    }
    if (a == null) {
      throw new RuntimeException("expected null, but didnt get null");
    }
    if (b == null) {
      throw new RuntimeException("expected not null, but got null");
    }
    int lengthA = a.size();
    int lengthB = b.size();
    if (lengthA != lengthB) {
      throw new RuntimeException("Expected array of length " + lengthA + ", but received list of length: " + lengthB);
    }
    //loop through and see if equal
    for (int i=0;i<lengthA;i++) {
      Object objectA = a.get(i);
      Object objectB = b.get(i);
      if (!equals(objectA, objectB)) {
        throw new RuntimeException("Index " + i + ", not equal, expected: " + objectA + ", but received: " + objectB);
      }
    }
    //all good
  }

  /**
   * if this string is surrounded by double or single quotes, then take the quotes
   * off and unescape the quotes inside.  Note, the things that will be changed inside are
   * \\ will go to \, and \" will go to " or a double quoted string, or \' will go to '
   * for a single quoted string
   * @param input
   * @return the unquoted string
   */
  public static String unquoteString(String input) {
    
    if (input == null) {
      return null;
    }
    
    if ((input.startsWith("\"") && input.endsWith("\""))
        || (input.startsWith("'") && input.endsWith("'")) ){
    
      StringBuilder result = new StringBuilder();

      //single or double
      char quoteChar = input.charAt(0);

      for (int i=0; i<input.length(); i++) {

        //strip off the start and end
        if (i==0 || i==input.length()-1) {
          continue;
        }

        char curChar = input.charAt(i);
        
        if (curChar == '\\') {
          
          if (i == input.length()-2) {
            throw new RuntimeException("Why is there a slash right before the last quote???? '" + input + "'");
          }
          
          char nextChar = input.charAt(i+1);
          
          if (nextChar == '\\') {
            result.append('\\');
            i++;
            continue;
          }
          
          if (nextChar == quoteChar) {
            result.append(quoteChar);
            i++;
            continue;
          }
          
          throw new RuntimeException("Should only be escaping slash \\ or quote char: " + quoteChar + ", '" + nextChar + "'");
          
        }
        result.append(curChar);
        
      }
      
      return result.toString();
    }
    
    return input;
  }

  /**
   * convert a uri to folder lookup
   * @param folderUri
   */
  public static AsasApiFolderLookup folderConvertUriToLookup(String folderUri) {

    AsasApiFolderLookup asasApiFolderLookup = new AsasApiFolderLookup();

    if (folderUri.startsWith("name:")) {

      String name = folderUri.substring(5);
      asasApiFolderLookup.setName(name);

    } else if (folderUri.startsWith("id:")) {
  
      String id = folderUri.substring(3);
      asasApiFolderLookup.setId(id);
  
    } else if (folderUri.contains(":")) {
  
      String handleName = StandardApiServerUtils.prefixOrSuffix(folderUri, ":", true);
      String handleValue = StandardApiServerUtils.prefixOrSuffix(folderUri, ":", false);
      asasApiFolderLookup.setHandleName(handleName);
      asasApiFolderLookup.setHandleValue(handleValue);
      
    } else {
      throw new AsasRestInvalidRequest("folderUri needs to contain a colon, start " +
          "with name: id: or another uri prefix that is server specific: '" + folderUri + "'", "404", "ERROR_INVALID_PARAM");
    }
    return asasApiFolderLookup;
  }
  
  /**
   * 
   * @return current version for url
   */
  public static String version() {
    //TODO: this should be based on the request
    return TaasWsVersion.serverVersion().name();
  }

  /**
   * if the path is the root folder
   * @param objectName
   * @return true if the path is the root folder
   */
  public static boolean pathIsRootFolder(String objectName) {
    
    return equals(":", objectName);
    
  }

  /**
   * get the parent folder name from an object name
   * @param objectName
   * @return the parent folder name, or null if it is the root folder (":")
   */
  public static String pathParentFolderName(String objectName) {
    
    if (objectName == null) {
      throw new NullPointerException("Why is there a null objectName?");
    }
    
    //there is no parent of the top parent
    if (pathIsRootFolder(objectName)) {
      return null;
    }
    
    //lets get the list of folders here:
    List<String> extensions = convertPathToExtensionList(objectName);
    
    //if top level, then return colon
    if (extensions.size() == 1) {
      return ":";
    }
    
    //else, pop off the top and convert back
    extensions.remove(extensions.size()-1);
    
    return convertPathFromExtensionList(extensions);
  }
  
  /**
   * convert the authz system path to use the standard api server path char
   * @param path a path that needs to be converted
   * @param currentSeparatorChar the separator of the current path
   * @param newSeparatorChar the separator of the new path, typically this would be:
   * String configuredSeparatorChar = StandardApiServerConfig.retrieveConfig().configItemPathSeparatorChar();
   * @return the new path with the configured separator
   */
  @Deprecated
  public static String convertPathToUseSeparatorAndEscapeOld(String path, String currentSeparatorChar, String newSeparatorChar) {
    
    if (StandardApiServerUtils.isBlank(newSeparatorChar) || newSeparatorChar.length() > 1) {
      throw new RuntimeException("pathSeparatorChar must be one char: '" + newSeparatorChar + "'");
    }

    if (StandardApiServerUtils.isBlank(currentSeparatorChar) || currentSeparatorChar.length() > 1) {
      throw new RuntimeException("separatorChar must be one char: '" + currentSeparatorChar + "'");
    }

    //split the path
    String[] extensions = StandardApiServerUtils.split(path, currentSeparatorChar.charAt(0));

    //loop through
    StringBuilder result = new StringBuilder();

    String hexString = "%" + Integer.toHexString(currentSeparatorChar.charAt(0)).toLowerCase();

    for (String extension : extensions) {
      if (result.length() > 0) {
        result.append(newSeparatorChar);
      }

      //escape percents
      extension = StandardApiServerUtils.replace(extension, "%", "%25");

      //escape the separator
      extension = StandardApiServerUtils.replace(extension, newSeparatorChar, hexString);
      
      result.append(extension);
    }
    
    return result.toString();
    
  }
  
  /**
   * convert an authz standard path (colon separated)
   * @param authzStandardPath
   * @return the list of strings
   */
  public static List<String> convertPathToExtensionList(String authzStandardPath) {
    List<String> resultList = new ArrayList<String>();
    if (StringUtils.isBlank(authzStandardPath)) {
      return resultList;
    }
    
    StringBuilder currentExtension = new StringBuilder();
    
    //lets loop through, and look for slash or colon
    char[] theChars = authzStandardPath.toCharArray();
    for (int i=0;i<theChars.length;i++) {
      char curChar = theChars[i];

      //end of the extension
      if (curChar == ':') {
        resultList.add(currentExtension.toString());
        currentExtension = new StringBuilder();
        continue;
      }
      
      //escaped character
      if (curChar == '\\') {
        
        //if there is no next char, then there is an error
        if (i==theChars.length-1) {
          throw new RuntimeException("There is a slash at the end of the path, " +
          		"but there is no next char, this is an error: '" + authzStandardPath + "'" );
        }
        
        //we arent at the end of the string, so what is the next char
        i++;
        char nextChar = theChars[i];
        
        //if the next char is colon or slash, then it is an escaped colon or slash
        if (nextChar == ':' || nextChar == '\\') {
          curChar = nextChar;
        } else {
          throw new RuntimeException("There is a slash in the path, " +
              "but there the next char is not colon or slash: '" + nextChar 
              + "', this is an error: '" + authzStandardPath + "'" );
        }
        
        //dont continue, since we need to append that char below
      }

      //normal character
      currentExtension.append(curChar);
    }
    //add the last extension on
    resultList.add(currentExtension.toString());
    
    //this doenst work for complex escaped cases
    //String[] extensions = split(authzStandardPath, ':');
    //for (int i=0;i<extensions.length;i++) {
    //  extensions[i] = extensions[i].replace("\\:", ":");
    //  resultList.add(extensions[i].replace("\\\\", "\\"));
    //}
    return resultList;
  }
  
  /**
   * convert an authz standard path (colon separated)
   * @param extensionList
   * @return the list of strings
   */
  public static String convertPathFromExtensionList(List<String> extensionList) {
    StringBuilder result = new StringBuilder();
    if (length(extensionList) > 0) {
      for (String extension : extensionList) {
        if (extension == null) {
          throw new NullPointerException("Why is extension null????");
        }
         
        if (result.length() > 0) {
          result.append(":");
        }
        
        extension = extension.replace("\\", "\\\\");
        extension = extension.replace(":", "\\:");
        result.append(extension);
      }
    }
    return result.toString();
  }
  
  /**
   * convert a path passed in from client to use the separator that the authz server understands
   * note: the path cannot contain any of the new separator chars...
   * @param path a path that needs to be converted
   * @param newSeparatorChar the separator of the current path
   * @return the new path with the configured separator
   */
  @Deprecated
  public static String convertPathToUseSeparatorAndUnescapeOld(String path, String currentSeparatorChar, String newSeparatorChar) {
    
    if (StandardApiServerUtils.isBlank(currentSeparatorChar) || currentSeparatorChar.length() > 1) {
      throw new RuntimeException("separatorChar must be one char: '" + currentSeparatorChar + "'");
    }

    if (StandardApiServerUtils.isBlank(newSeparatorChar) || newSeparatorChar.length() > 1) {
      throw new RuntimeException("separatorChar must be one char: '" + newSeparatorChar + "'");
    }

    //split the path
    String[] extensions = StandardApiServerUtils.split(path, currentSeparatorChar.charAt(0));

    //loop through
    StringBuilder result = new StringBuilder();

    String hexString = "%" + Integer.toHexString(currentSeparatorChar.charAt(0)).toLowerCase();

    for (String extension : extensions) {
      if (result.length() > 0) {
        result.append(newSeparatorChar);
      }

      //unescape the separator
      extension = StandardApiServerUtils.replace(extension, hexString, currentSeparatorChar);

      //escape percents
      extension = StandardApiServerUtils.replace(extension, "%25", "%");
      
      if (extension.contains(newSeparatorChar)) {
        throw new RuntimeException("extensions cannot contain the server separator char: extension: '" 
            + extension + "', separator: '" + newSeparatorChar + "'");
      }
      
      result.append(extension);

    }
    
    return result.toString();
    
  }

  /**
   * see which group interface is configured and make an instance of it
   * @return an instance of the group interface
   */
  public static AsasApiGroupInterface interfaceGroupInstance() {
    
    String className = StandardApiServerConfig.retrieveConfig().propertyValueStringRequired("tierApiAuthzServer.interface.group");
    
    @SuppressWarnings("unchecked")
    Class<AsasApiGroupInterface> theClass = (Class<AsasApiGroupInterface>)forName(className);
    
    return newInstance(theClass);
  }

  /**
   * see which folder interface is configured and make an instance of it
   * @return an instance of the folder interface
   */
  public static AsasApiFolderInterface interfaceFolderInstance() {
    
    String className = StandardApiServerConfig.retrieveConfig().propertyValueStringRequired("tierApiAuthzServer.interface.folder");
    
    @SuppressWarnings("unchecked")
    Class<AsasApiFolderInterface> theClass = (Class<AsasApiFolderInterface>)forName(className);
    
    return newInstance(theClass);
  }
  
  /**
   * do a case-insensitive matching
   * @param theEnumClass class of the enum
   * @param <E> generic type
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws AsasRestInvalidRequest 
   * @throws RuntimeException if there is a problem
   */
  public static <E extends Enum<?>> E enumValueOfIgnoreCase(Class<E> theEnumClass, String string, 
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {

    try {
      return StandardApiServerCommonUtils.enumValueOfIgnoreCase(theEnumClass, string, exceptionOnNotFound);
    } catch (RuntimeException re) {
      if (!(re instanceof AsasRestInvalidRequest)) {
        throw new AsasRestInvalidRequest(re.getMessage(), re, 
            "400", "ERROR_INVALID_PATH");
      }
      throw re;
    }
  }

  /**
   * structure name of class
   * @param theClass
   * @return the structure name
   */
  public static String structureName(Class<?> theClass) {
    String registerByName = theClass.getSimpleName();
    if (registerByName.toLowerCase().startsWith("asas")) {
      registerByName = registerByName.substring(4);
    }
    registerByName = StandardApiServerUtils.lowerFirstLetter(registerByName);
    return registerByName;
  }
  
  /**
   * logger
   */
  private static Log LOG = StandardApiServerUtils.retrieveLog(StandardApiServerUtils.class);

  /**
   * return something like https://server.whatever.ext/appName/servlet
   * @return the url of the servlet with no slash
   */
  public static String servletUrl() {
    
    HttpServletRequest httpServletRequest = TaasFilterJ2ee.retrieveHttpServletRequest();
    
    if (httpServletRequest == null) {
      String servletUrl = StandardApiServerConfig.retrieveConfig().propertyValueStringRequired("tierApiAuthzServer.servletUrl");
      if (StandardApiServerUtils.isBlank(servletUrl)) {
        throw new RuntimeException("Cant find servlet URL!  you can set it in authzStandardapi.server.properties as tierApiAuthzServer.servletUrl");
      }
      return servletUrl;
    }
    
    String fullUrl = httpServletRequest.getRequestURL().toString();
    
    String servletPath = httpServletRequest.getServletPath();
    
    return fullUrlToServletUrl(fullUrl, servletPath, AsasRestContentType.retrieveContentType());
  }
  
  /**
   * 
   * @param fullUrl https://whatever/appName/servlet
   * @oaram servletPath /servlet
   * @return the servlet url
   */
  static String fullUrlToServletUrl(String fullUrl, String servletPath, AsasRestContentType wsRestContentType) {
    
    if (servletPath.endsWith("." + wsRestContentType.name())) {
      servletPath = servletPath.substring(0,servletPath.length()-(1+wsRestContentType.name().length()));
    }
    
    int fromIndex = 0;
    for (int i=0;i<4;i++) {
      fromIndex = fullUrl.indexOf('/', fromIndex+1);
    }
    
    int servletIndex = fullUrl.indexOf(servletPath, fromIndex);
    
    return fullUrl.substring(0, servletIndex + servletPath.length());

  }
  
  /**
   * generate a uuid
   * @return uuid
   */
  public static String uuid() {
    String uuid = UUID.randomUUID().toString();
    
    char[] result = new char[32];
    int resultIndex = 0;
    for (int i=0;i<uuid.length();i++) {
      char theChar = uuid.charAt(i);
      if (theChar != '-') {
        if (resultIndex >= result.length) {
          throw new RuntimeException("Why is resultIndex greater than result.length ???? " 
              + resultIndex + " , " + result.length + ", " + uuid);
        }
        result[resultIndex++] = theChar;
      }
    }
    return new String(result);

  }
  
  /**
   * configure jdk14 logs once
   */
  private static boolean configuredLogs = false;

  /** iso date string */
  private static final String YYYY_MM_DD_T_HH_MM_SS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  /**
   * convert the date to a string
   * 2012-10-04T03:10.123Z
   * @param date
   * @return the string
   */
  public static String convertToIso8601(Date date) {
    
    if (date == null) {
      return null;
    }
    
    DateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_SSS_Z);
    
    return dateFormat.format(date);
    
  }
  
  /**
   * convert the string to a date
   * 2012-10-04T03:10.123Z
   * @param date
   * @return the string
   */
  public static Date convertFromIso8601(String date) {
    
    if (date == null) {
      return null;
    }
    
    DateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_SSS_Z);
    
    try {
      return dateFormat.parse(date);
    } catch (ParseException parseException) {
      throw new RuntimeException(parseException);
    }
  }
  
  /**
   * @param theClass
   * @return the log
   */
  public static Log retrieveLog(Class<?> theClass) {

    Log theLog = LogFactory.getLog(theClass);
    
    //if this isnt here, dont configure yet
    if (isBlank(StandardApiServerConfig.retrieveConfig().propertyValueString("encrypt.disableExternalFileLookup"))
        || theClass.equals(StandardApiServerCommonUtils.class)) {
      return new StandardApiServerLog(theLog);
    }
    
    if (!configuredLogs) {
      String logLevel = StandardApiServerConfig.retrieveConfig().propertyValueString("grouperClient.logging.logLevel");
      String logFile = StandardApiServerConfig.retrieveConfig().propertyValueString("grouperClient.logging.logFile");
      String grouperClientLogLevel = StandardApiServerConfig.retrieveConfig().propertyValueString(
          "grouperClient.logging.grouperClientOnly.logLevel");
      
      boolean hasLogLevel = !isBlank(logLevel);
      boolean hasLogFile = !isBlank(logFile);
      boolean hasStandardApiClientLogLevel = !isBlank(grouperClientLogLevel);
      
      if (hasLogLevel || hasLogFile) {
        if (theLog instanceof Jdk14Logger) {
          Jdk14Logger jdkLogger = (Jdk14Logger) theLog;
          Logger logger = jdkLogger.getLogger();
          long timeToLive = 60;
          while (logger.getParent() != null && timeToLive-- > 0) {
            //this should be root logger
            logger = logger.getParent();
          }
  
          if (length(logger.getHandlers()) == 1) {
  
            //remove console appender if only one
            if (logger.getHandlers()[0].getClass() == ConsoleHandler.class) {
              logger.removeHandler(logger.getHandlers()[0]);
            }
          }
  
          if (length(logger.getHandlers()) == 0) {
            Handler handler = null;
            if (hasLogFile) {
              try {
                handler = new FileHandler(logFile, true);
              } catch (IOException ioe) {
                throw new RuntimeException(ioe);
              }
            } else {
              handler = new ConsoleHandler();
            }
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);

            logger.setUseParentHandlers(false);
          }
          
          if (hasLogLevel) {
            Level level = Level.parse(logLevel);
            
            logger.setLevel(level);

          }
        }
      }
      
      if (hasStandardApiClientLogLevel) {
        Level level = Level.parse(grouperClientLogLevel);
        Log grouperClientLog = LogFactory.getLog("edu.internet2.middleware.grouperClient");
        if (grouperClientLog instanceof Jdk14Logger) {
          Jdk14Logger jdkLogger = (Jdk14Logger) grouperClientLog;
          Logger logger = jdkLogger.getLogger();
          logger.setLevel(level);
        }
      }
      
      configuredLogs = true;
    }
    
    return new StandardApiServerLog(theLog);
    
  }
  
  /**
   * <pre>
   * this method will indent xml or json.
   * this is for logging or documentations purposes only and should
   * not be used for a production use (since it is not 100% tested
   * or compliant with all constructs like xml CDATA
   *
   * For xml, assumes elements either have text or sub elements, not both.
   * No cdata, nothing fancy.
   *
   * If the input is &lt;a&gt;&lt;b&gt;&lt;c&gt;hey&lt;/c&gt;&lt;d&gt;&lt;e&gt;there&lt;/e&gt;&lt;/d&gt;&lt;/b&gt;&lt;/a&gt;
   * It would output:
   * &lt;a&gt;
   *   &lt;b&gt;
   *     &lt;c&gt;hey&lt;/c&gt;
   *     &lt;d&gt;
   *       &lt;e&gt;there&lt;/e&gt;
   *     &lt;/d&gt;
   *   &lt;/b&gt;
   * &lt;/a&gt;
   *
   * For , if the input is: {"a":{"b\"b":{"c\\":"d"},"e":"f","g":["h":"i"]}}
   * It would output:
   * {
   *   "a":{
   *     "b\"b":{
   *       "c\\":"d"
   *     },
   *     "e":"f",
   *     "g":[
   *       "h":"i"
   *     ]
   *   }
   * }
   *
   *
   * <pre>
   * @param string
   * @param failIfTypeNotFound
   * @return the indented string, 2 spaces per line
   */
  public static String indent(String string, boolean failIfTypeNotFound) {
    if (string == null) {
      return null;
    }
    string = trim(string);
    if (string.startsWith("<")) {
      //this is xml
      return new XmlIndenter(string).result();
    }
    if (string.startsWith("{")) {
      return new JsonIndenter(string).result();
    }
    if (!failIfTypeNotFound) {
      //just return if cant indent
      return string;
    }
    throw new RuntimeException("Cant find type of string: " + string);
  }

  /**
   * convert an object to json.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertTo(Object object) {
    return jsonConvertTo(object, true);
  }

  /**
   * convert an object to json.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertTo(Object object, boolean includeObjectNameWrapper) {

    if (object == null) {
      throw new NullPointerException();
    }
//    Gson gson = new GsonBuilder().create();
//    String json = gson.toJson(object);

//    JSONObject jsonObject = net.sf.json.JSONObject.fromObject( object );
//    String json = jsonObject.toString();

    JsonConfig jsonConfig = new JsonConfig();
    jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
       public boolean apply( Object source, String name, Object value ) {
         //json-lib cannot handle maps where the key is not a string
         if( value != null && value instanceof Map ){
           @SuppressWarnings("rawtypes")
          Map map = (Map)value;
           if (map.size() > 0 && !(map.keySet().iterator().next() instanceof String)) {
             return true;
           }
         }
         return value == null;
       }
    });
    JSONObject jsonObject = JSONObject.fromObject( object, jsonConfig );
    String json = jsonObject.toString();

    if (!includeObjectNameWrapper) {
      return json;
    }
    return "{\"" + object.getClass().getSimpleName() + "\":" + json + "}";
  }
  /**
   * convert an object to json without wrapping it with the simple class name.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertToNoWrap(Object object) {
    //TODO call the other jsonConvertTo() method
      if (object == null) {
        throw new NullPointerException();
      }

      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
         public boolean apply( Object source, String name, Object value ) {
           //json-lib cannot handle maps where the key is not a string
           if( value != null && value instanceof Map ){
             @SuppressWarnings("rawtypes")
            Map map = (Map)value;
             if (map.size() > 0 && !(map.keySet().iterator().next() instanceof String)) {
               return true;
             }
           }
           return value == null;
         }
      });
      JSONObject jsonObject = JSONObject.fromObject( object, jsonConfig );
      String json = jsonObject.toString();

      return json;
    }

  /**
   * convert an object to json.  note this wraps the gson with the object simple name so it can be revived
   * @param object
   * @param writer
   */
  public static void jsonConvertTo(Object object, Writer writer) {
    String json = jsonConvertTo(object);
    try {
      writer.write(json);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * <pre>
   * detects the front of a json string, pops off the first field, and gives the body as the matcher
   * ^\s*\{\s*\"([^"]+)\"\s*:\s*\{(.*)}$
   * Example matching text:
   * {
   *  "XstreamPocGroup":{
   *    "somethingNotMarshaled":"whatever",
   *    "name":"myGroup",
   *    "someInt":5,
   *    "someBool":true,
   *    "members":[
   *      {
   *        "name":"John",
   *        "description":"John Smith - Employee"
   *      },
   *      {
   *        "name":"Mary",
   *        "description":"Mary Johnson - Student"
   *      }
   *    ]
   *  }
   * }
   *
   * ^\s*          front of string and optional space
   * \{\s*         open bracket and optional space
   * \"([^"]+)\"   quote, simple name of class, quote
   * \s*:\s*       optional space, colon, optional space
   * \{(.*)}$      open bracket, the class info, close bracket, end of string
   *
   *
   * </pre>
   */
  private static Pattern jsonPattern = Pattern.compile("^\\s*\\{\\s*\\\"([^\"]+)\\\"\\s*:\\s*(.*)}$", Pattern.DOTALL);

  /**
   * convert an object from json.  note this works well if there are no collections, just real types, arrays, etc.
   * @param conversionMap is the class simple name to class of objects which are allowed to be brought back.
   * Note: only the top level object needs to be registered
   * @param json
   * @return the object
   */
  public static Object jsonConvertFrom(Map<String, Class<?>> conversionMap, String json) {

    //gson does not put the type of the object in the json, but we need that.  so when we convert,
    //put the type in there.  So we need to extract the type out when unmarshaling
    Matcher matcher = jsonPattern.matcher(json);

    if (!matcher.matches()) {
      throw new RuntimeException("Cant match this json, should start with simple class name: " + json);
    }

    String simpleClassName = matcher.group(1);
    String jsonBody = matcher.group(2);

    Class<?> theClass = conversionMap.get(simpleClassName);
    if (theClass == null) {
      throw new RuntimeException("Not allowed to unmarshal json: " + simpleClassName + ", " + json);
    }
//    Gson gson = new GsonBuilder().create();
//    Object object = gson.fromJson(jsonBody, theClass);
    JSONObject jsonObject = JSONObject.fromObject( jsonBody );
    Object object = JSONObject.toBean( jsonObject, theClass );

    return object;
  }
  /**
   * convert an object from json.  note this works well if there are no collections, just real types, arrays, etc.
   * @param json is the json string, not wrapped with a simple class name
   * @param theClass is the class that the object should be coverted into.
   * Note: only the top level object needs to be registered
   * @return the object
   */
  public static Object jsonConvertFrom (String json, Class<?> theClass) {
      JSONObject jsonObject = JSONObject.fromObject( json );
      Object object = JSONObject.toBean( jsonObject, theClass );
      return object;

  }

  /** class object for this string */
  private static Map<String, Class<?>> jexlClass = new HashMap<String, Class<?>>();

  /** pattern to see if class or not */
  private static Pattern jexlClassPattern = Pattern.compile("^[a-zA-Z0-9_.]*\\.[A-Z][a-zA-Z0-9_]*$");

  /** true or false for if we know if this is a class or not */
  private static Map<String, Boolean> jexlKnowsIfClass = new HashMap<String, Boolean>();

  /**
   * 
   */
  private static class JexlCustomMapContext extends MapContext {
  
    /**
     * retrieve class if class
     * @param name
     * @return class
     */
    private static Object retrieveClass(String name) {
      if (isBlank(name)) {
        return null;
      }
      
      //see if fully qualified class
      
      Boolean knowsIfClass = jexlKnowsIfClass.get(name);
      
      //see if knows answer
      if (knowsIfClass != null) {
        //return class or null
        return jexlClass.get(name);
      }
      
      //see if valid class
      if (jexlClassPattern.matcher(name).matches()) {
        
        jexlKnowsIfClass.put(name, true);
        //try to load
        try {
          Class<?> theClass = Class.forName(name);
          jexlClass.put(name, theClass);
          return theClass;
        } catch (Exception e) {
          LOG.info("Cant load what looks like class: " + name, e);
          //this is ok I guess, dont rethrow, not sure it is a class
        }
      }
      return null;
      
    }
    
    /**
     * @see edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.MapContext#get(java.lang.String)
     */
    @Override
    public Object get(String name) {
      
      //see if registered      
      Object object = super.get(name);
      
      if (object != null) {
        return object;
      }
      return retrieveClass(name);
    }
  
    /**
     * @see edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.MapContext#has(java.lang.String)
     */
    @Override
    public boolean has(String name) {
      boolean superHas = super.has(name);
      if (superHas) {
        return true;
      }
      
      return retrieveClass(name) != null;
      
    }
    
  }

  /**
   * substitute an EL for objects.  Dont worry if something returns null
   * @param stringToParse
   * @param variableMap
   * @return the string
   */
  public static String substituteExpressionLanguage(String stringToParse, Map<String, Object> variableMap) {
    
    return substituteExpressionLanguage(stringToParse, variableMap, true, true, true, false);
    
  }
  

  /**
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @param allowStaticClasses if true allow static classes not registered with context
   * @param silent if silent mode, swallow exceptions (warn), and dont warn when variable not found
   * @param lenient false if undefined variables should throw an exception.  if lenient is true (default)
   * then undefined variables are null
   * @param logOnNull if null output of substitution should be logged
   * @return the string
   */
  public static String substituteExpressionLanguage(String stringToParse, 
      Map<String, Object> variableMap, boolean allowStaticClasses, boolean silent, boolean lenient, boolean logOnNull) {
    if (!jexlEnginesInitialized) {
      synchronized (StandardApiServerUtils.class) {
        if (!jexlEnginesInitialized) {
          
          int cacheSize = StandardApiServerConfig.retrieveConfig().propertyValueInt("jexl.cacheSize", 10000);
          for (JexlEngine jexlEngine : jexlEngines.values()) {
            jexlEngine.setCache(cacheSize);
          }
          
          jexlEnginesInitialized = true;
        }
      }
    }

    
    if (isBlank(stringToParse)) {
      return stringToParse;
    }
    String overallResult = null;
    Exception exception = null;
    try {
      JexlContext jc = allowStaticClasses ? new JexlCustomMapContext() : new MapContext();

      //start index of section
      int index = 0;

      for (String key: variableMap.keySet()) {
        jc.set(key, variableMap.get(key));
      }

      //allow utility methods
      jc.set("personWsServerUtils", new AsasElUtilsSafe());
      //if you add another one here, add it in the logs below

      // matching ${ exp }   (non-greedy)
      List<Integer> sectionIndexes = indexOfsQuoted(stringToParse, "${"); 

      StringBuilder result = new StringBuilder();

      int count = 0;
      
      //loop through and find each script
      for (int sectionIndex : nonNull(sectionIndexes)) {

        //get the stuff in between
        result.append(stringToParse.substring(index, sectionIndex));

        String scriptletSection = null;
        
        //if we are on the last one
        if (count == length(sectionIndexes)-1) {
          //add two since two chars in ${
          scriptletSection = stringToParse.substring(sectionIndex+2, stringToParse.length());
        } else {
          
          int nextSectionIndex = sectionIndexes.get(count+1);
          
          scriptletSection = stringToParse.substring(sectionIndex+2, nextSectionIndex);
        }
        
        //where is the end?
        int endCurlyIndex = indexOfQuoted(scriptletSection, "}");
        
        //here is the script inside the curlies
        String script = scriptletSection.substring(0, endCurlyIndex);

        //add one for the end index, add two for the ${ opening
        index = sectionIndex+endCurlyIndex+1+2;

        Expression e = jexlEngines.get(new MultiKey(silent, lenient)).createExpression(script);

        //this is the result of the evaluation
        Object o = null;

        try {
          o = e.evaluate(jc);
        } catch (JexlException je) {
          //exception-scrape to see if missing variable
          if (!lenient && StringUtils.trimToEmpty(je.getMessage()).contains("undefined variable")) {
            //clean up the message a little bit
            // e.g. org.personWebService.server.util.PersonWsServerUtils.substituteExpressionLanguage@8846![0,6]: 'amount < 50000 && amount2 < 23;' undefined variable amount
            String message = je.getMessage();
            //Pattern exceptionPattern = Pattern.compile("^" + PersonWsServerUtils.class.getName() + "\\.substituteExpressionLanguage.*?]: '(.*)");
            Pattern exceptionPattern = Pattern.compile("^.*undefined variable (.*)");
            Matcher exceptionMatcher = exceptionPattern.matcher(message);
            if (exceptionMatcher.matches()) {
              //message = "'" + exceptionMatcher.group(1);
              message = "variable '" + exceptionMatcher.group(1) + "' is not defined in script: '" + script + "'";
            }
            throw new ExpressionLanguageMissingVariableException(message, je);
          }
          throw je;
        }

        //we dont want "null" in the result I think...
        if (o == null && lenient) {
          o = "";
        }
        
        if (o == null) {
          if (logOnNull) {
            LOG.warn("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
                + toStringForLog(variableMap.keySet()));
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
                  + toStringForLog(variableMap.keySet()));
            }            
          }
        }

        if (o instanceof RuntimeException) {
          throw (RuntimeException)o;
        }

        result.append(o);
        count++;
      }

      result.append(stringToParse.substring(index, stringToParse.length()));
      overallResult = result.toString();
      return overallResult;

    } catch (Exception e) {
      exception = e;
      if (e instanceof ExpressionLanguageMissingVariableException) {
        throw (ExpressionLanguageMissingVariableException)e;
      }
      throw new RuntimeException("Error substituting string: '" + stringToParse + "'", e);
    } finally {
      if (LOG.isDebugEnabled()) {
        Set<String> keysSet = new LinkedHashSet<String>(nonNull(variableMap).keySet());
        keysSet.add("personWsServerUtils");
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Subsituting EL: '").append(stringToParse).append("', and with env vars: ");
        String[] keys = keysSet.toArray(new String[]{});
        for (int i=0;i<keys.length;i++) {
          logMessage.append(keys[i]);
          if (i != keys.length-1) {
            logMessage.append(", ");
          }
        }
        logMessage.append(" with result: '" + overallResult + "'");
        if (exception != null) {
          logMessage.append(", and exception: " + exception + ", " + ExceptionUtils.getFullStackTrace(exception));
        }
        LOG.debug(logMessage.toString());
      }
    }
  }

  /**
   * convert a query string to a map (for testing purposes only, in a real system the
   * HttpServletRequest would be used.  no dupes allowed
   * @param queryString
   * @return the map (null if no query string)
   */
  public static Map<String, String> convertQueryStringToMap(String queryString) {
    
    if (StandardApiServerUtils.isBlank(queryString)) {
      return null;
    }
    
    Map<String, String> paramMap = new HashMap<String, String>();
    
    String[] queryStringPairs = StandardApiServerUtils.splitTrim(queryString, "&");
    
    for (String queryStringPair : queryStringPairs) {
      
      String key = StandardApiServerUtils.prefixOrSuffix(queryStringPair, "=", true);
      String value = StandardApiServerUtils.prefixOrSuffix(queryStringPair, "=", false);
      //unescape the value
      value = StandardApiServerUtils.escapeUrlDecode(value);
      
      if (paramMap.containsKey(key)) {
        throw new RuntimeException("Query string contains two of the same key: " 
            + key + ", " + queryString);
        
      }
      paramMap.put(key, value);
    }
    return paramMap;
  }

  /**
   * pop first url string, retrieve, and remove, or null if not there
   * @param urlStrings
   * @return the string or null if not there
   */
  public static String popUrlString(List<String> urlStrings) {
    
    int urlStringsLength = length(urlStrings);
  
    if (urlStringsLength > 0) {
      String firstResource = urlStrings.get(0);
      //pop off
      urlStrings.remove(0);
      //return
      return firstResource;
    }
    return null;
  }

  /**
   * 
   * @param object
   * @param length
   * @return abbreviate an object tostring
   */
  public static String abbreviate(Object object, int length) {
    if (object == null) {
      return null;
    }
    return abbreviate(toStringSafe(object), length);
  }

  /**
   * <p>Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "Now is the time for..."</p>
   *
   * <p>Specifically:
   * <ul>
   *   <li>If <code>str</code> is less than <code>maxWidth</code> characters
   *       long, return it.</li>
   *   <li>Else abbreviate it to <code>(substring(str, 0, max-3) + "...")</code>.</li>
   *   <li>If <code>maxWidth</code> is less than <code>4</code>, throw an
   *       <code>IllegalArgumentException</code>.</li>
   *   <li>In no case will it return a String of length greater than
   *       <code>maxWidth</code>.</li>
   * </ul>
   * </p>
   *
   * <pre>
   * StringUtils.abbreviate(null, *)      = null
   * StringUtils.abbreviate("", 4)        = ""
   * StringUtils.abbreviate("abcdefg", 6) = "abc..."
   * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
   * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
   * StringUtils.abbreviate("abcdefg", 4) = "a..."
   * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param maxWidth  maximum length of result String, must be at least 4
   * @return abbreviated String, <code>null</code> if null String input
   * @throws IllegalArgumentException if the width is too small
   * @since 2.0
   */
  public static String abbreviate(String str, int maxWidth) {
    return abbreviate(str, 0, maxWidth);
  }

  /**
   * <p>Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "...is the time for..."</p>
   *
   * <p>Works like <code>abbreviate(String, int)</code>, but allows you to specify
   * a "left edge" offset.  Note that this left edge is not necessarily going to
   * be the leftmost character in the result, or the first character following the
   * ellipses, but it will appear somewhere in the result.
   *
   * <p>In no case will it return a String of length greater than
   * <code>maxWidth</code>.</p>
   *
   * <pre>
   * StringUtils.abbreviate(null, *, *)                = null
   * StringUtils.abbreviate("", 0, 4)                  = ""
   * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
   * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
   * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
   * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param offset  left edge of source String
   * @param maxWidth  maximum length of result String, must be at least 4
   * @return abbreviated String, <code>null</code> if null String input
   * @throws IllegalArgumentException if the width is too small
   * @since 2.0
   */
  public static String abbreviate(String str, int offset, int maxWidth) {
    if (str == null) {
      return null;
    }
    if (maxWidth < 4) {
      throw new IllegalArgumentException("Minimum abbreviation width is 4");
    }
    if (str.length() <= maxWidth) {
      return str;
    }
    if (offset > str.length()) {
      offset = str.length();
    }
    if ((str.length() - offset) < (maxWidth - 3)) {
      offset = str.length() - (maxWidth - 3);
    }
    if (offset <= 4) {
      return str.substring(0, maxWidth - 3) + "...";
    }
    if (maxWidth < 7) {
      throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
    }
    if ((offset + (maxWidth - 3)) < str.length()) {
      return "..." + abbreviate(str.substring(offset), maxWidth - 3);
    }
    return "..." + str.substring(str.length() - (maxWidth - 3));
  }

  /**
   * Return the zero element of the list, if it exists, null if the list is empty.
   * If there is more than one element in the list, an exception is thrown.
   * @param <T>
   * @param list is the container of objects to get the first of.
   * @return the first object, null, or exception.
   */
  public static <T> T listPopOne(List<T> list) {
    int size = length(list);
    if (size == 1) {
      return list.get(0);
    } else if (size == 0) {
      return null;
    }
    throw new RuntimeException("More than one object of type " + className(list.get(0))
        + " was returned when only one was expected. (size:" + size +")" );
  }

  /**
   * create one set of jexlEngine instances (one per type of setting) so we can cache expressions
   */
  private final static Map<MultiKey, JexlEngine> jexlEngines = new HashMap<MultiKey, JexlEngine>();

  /**
   * if the jexl engine instances are all initialized completely
   */
  private static boolean jexlEnginesInitialized = false;
  
  /**
   * initialize the instances
   */
  static {
    {
      Boolean silent = true;
      Boolean lenient = true;
      final JexlEngine jexlEngine = new JexlEngine();
      jexlEngine.setSilent(silent);
      jexlEngine.setLenient(lenient);
      jexlEngines.put(new MultiKey(silent, lenient), jexlEngine);
    }
    {
      Boolean silent = false;
      Boolean lenient = true;
      final JexlEngine jexlEngine = new JexlEngine();
      jexlEngine.setSilent(silent);
      jexlEngine.setLenient(lenient);
      jexlEngines.put(new MultiKey(silent, lenient), jexlEngine);
    }
    {
      Boolean silent = true;
      Boolean lenient = false;
      final JexlEngine jexlEngine = new JexlEngine();
      jexlEngine.setSilent(silent);
      jexlEngine.setLenient(lenient);
      jexlEngines.put(new MultiKey(silent, lenient), jexlEngine);
    }
    {
      Boolean silent = false;
      Boolean lenient = false;
      final JexlEngine jexlEngine = new JexlEngine();
      jexlEngine.setSilent(silent);
      jexlEngine.setLenient(lenient);
      jexlEngines.put(new MultiKey(silent, lenient), jexlEngine);
    }
  }

  /**
   * see which groupsMember interface is configured and make an instance of it
   * @return an instance of the groupsMember interface
   */
  public static AsasApiGroupsMemberInterface interfaceGroupsMemberInstance() {
    
    String className = StandardApiServerConfig.retrieveConfig().propertyValueStringRequired("tierApiAuthzServer.interface.groupsMember");
    
    @SuppressWarnings("unchecked")
    Class<AsasApiGroupsMemberInterface> theClass = (Class<AsasApiGroupsMemberInterface>)forName(className);
    
    return newInstance(theClass);
  }

  /**
   * convert a uri to group lookup
   * @param groupUri
   */
  public static AsasApiGroupLookup groupConvertUriToLookup(String groupUri) {
  
    AsasApiGroupLookup asasApiGroupLookup = new AsasApiGroupLookup();
  
    if (groupUri.startsWith("name:")) {
  
      String name = groupUri.substring(5);
      asasApiGroupLookup.setName(name);
  
    } else if (groupUri.startsWith("id:")) {
  
      String id = groupUri.substring(3);
      asasApiGroupLookup.setId(id);
  
    } else if (groupUri.contains(":")) {
  
      String handleName = StandardApiServerUtils.prefixOrSuffix(groupUri, ":", true);
      String handleValue = StandardApiServerUtils.prefixOrSuffix(groupUri, ":", false);
      asasApiGroupLookup.setHandleName(handleName);
      asasApiGroupLookup.setHandleValue(handleValue);
      
    } else {
      throw new AsasRestInvalidRequest("groupUri needs to contain a colon, start " +
          "with name: id: or another uri prefix that is server specific: '" + groupUri + "'", "404", "ERROR_INVALID_PATH");
    }
    return asasApiGroupLookup;
  }

  /**
   * convert a uri to folder lookup
   * @param entityUri
   */
  public static AsasApiEntityLookup entityConvertUriToLookup(String entityUri) {
  
    AsasApiEntityLookup asasApiEntityLookup = new AsasApiEntityLookup();
  
    if (entityUri.contains(":")) {
  
      String handleName = StandardApiServerUtils.prefixOrSuffix(entityUri, ":", true);
      String handleValue = StandardApiServerUtils.prefixOrSuffix(entityUri, ":", false);
      asasApiEntityLookup.setHandleName(handleName);
      asasApiEntityLookup.setHandleValue(handleValue);
      
    } else {
      throw new AsasRestInvalidRequest("entityUri needs to contain a colon: '" + entityUri + "'", 
          "404", "ERROR_INVALID_PATH");
    }
    return asasApiEntityLookup;
  }
  
  
}
