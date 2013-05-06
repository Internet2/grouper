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
/**
 * 
 */
package edu.internet2.middleware.grouper.ui.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.j2ee.GenericServletResponseWrapper;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;




/**
 * utility methods for grouper
 * 
 * @author mchyzer
 * 
 */
public class GrouperUiUtils {

  /**
   * compute a url of a resource
   * @param resourceName
   * @param canBeNull if cant be null, throw runtime
   * @return the URL
   */
  public static URL computeUrl(String resourceName, boolean canBeNull) {
    //get the url of the navigation file
    //TODO move this to grouperutil
    ClassLoader cl = classLoader();

    URL url = null;

    try {
      url = cl.getResource(resourceName);
    } catch (NullPointerException npe) {
      String error = "computeUrl() Could not find resource file: " + resourceName;
      throw new RuntimeException(error, npe);
    }

    if (!canBeNull && url == null) {
      throw new RuntimeException("Cant find resource: " + resourceName);
    }

    return url;
  }


  /**
   * fast class loader
   * @return the class loader
   */
  public static ClassLoader classLoader() {
    return GrouperUiUtils.class.getClassLoader();
  }
  
  /**
   * Field lastId.
   */
  private static char[] lastId = convertLongToStringSmall(new Date().getTime())
      .toCharArray();

  /** cache the properties read from resource */
  private static Map<String, Properties> resourcePropertiesCache = new HashMap<String, Properties>();
  
  /**
   * read properties from a resource, dont modify the properties returned since they are cached
   * @param resourceName
   * @return the properties
   */
  public synchronized static Properties propertiesFromResourceName(String resourceName) {
    Properties properties = resourcePropertiesCache.get(resourceName);
    if (properties == null) {

      properties = new Properties();
      //TODO move this to grouperutil
      URL url = computeUrl(resourceName, true);
      InputStream inputStream = null;
      try {
        inputStream = url.openStream();
        properties.load(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Problem with resource: '" + resourceName + "'");
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }

    }
    return properties;
  }
  
  /**
   * get a unique string identifier based on the current time,
   * this is not globally unique, just unique for as long as this
   * server is running...
   * 
   * @return String
   */
  public static String uniqueId() {
    //this needs to be threadsafe since we are using a static field
    synchronized (GrouperUiUtils.class) {
      lastId = incrementStringInt(lastId);
    }

    return String.valueOf(lastId);
  }

  /**
   * this method takes a long (less than 62) and converts it to a 1 character
   * string (a-z, A-Z, 0-9)
   * 
   * @param theLong
   *          is the long (less than 62) to convert to a 1 character string
   * 
   * @return a one character string
   */
  public static String convertLongToChar(long theLong) {
    if ((theLong < 0) || (theLong >= 62)) {
      throw new RuntimeException("StringUtils.convertLongToChar() "
          + " invalid input (not >=0 && <62: " + theLong);
    } else if (theLong < 26) {
      return "" + (char) ('a' + theLong);
    } else if (theLong < 52) {
      return "" + (char) ('A' + (theLong - 26));
    } else {
      return "" + (char) ('0' + (theLong - 52));
    }
  }

  /**
   * this method takes a long (less than 36) and converts it to a 1 character
   * string (A-Z, 0-9)
   * 
   * @param theLong
   *          is the long (less than 36) to convert to a 1 character string
   * 
   * @return a one character string
   */
  public static String convertLongToCharSmall(long theLong) {
    if ((theLong < 0) || (theLong >= 36)) {
      throw new RuntimeException("StringUtils.convertLongToCharSmall() "
          + " invalid input (not >=0 && <36: " + theLong);
    } else if (theLong < 26) {
      return "" + (char) ('A' + theLong);
    } else {
      return "" + (char) ('0' + (theLong - 26));
    }
  }

  /**
   * convert a long to a string by converting it to base 62 (26 lower, 26 upper,
   * 10 digits)
   * 
   * @param theLong
   *          is the long to convert
   * 
   * @return the String conversion of this
   */
  public static String convertLongToString(long theLong) {
    long quotient = theLong / 62;
    long remainder = theLong % 62;
  
    if (quotient == 0) {
      return convertLongToChar(remainder);
    }
    StringBuffer result = new StringBuffer();
    result.append(convertLongToString(quotient));
    result.append(convertLongToChar(remainder));
  
    return result.toString();
  }

  /**
   * convert a long to a string by converting it to base 36 (26 upper, 10
   * digits)
   * 
   * @param theLong
   *          is the long to convert
   * 
   * @return the String conversion of this
   */
  public static String convertLongToStringSmall(long theLong) {
    long quotient = theLong / 36;
    long remainder = theLong % 36;
  
    if (quotient == 0) {
      return convertLongToCharSmall(remainder);
    }
    StringBuffer result = new StringBuffer();
    result.append(convertLongToStringSmall(quotient));
    result.append(convertLongToCharSmall(remainder));
  
    return result.toString();
  }

  /**
   * increment a character (A-Z then 0-9)
   * 
   * @param theChar
   * 
   * @return the value
   */
  public static char incrementChar(char theChar) {
    if (theChar == 'Z') {
      return '0';
    }
  
    if (theChar == '9') {
      return 'A';
    }
  
    return ++theChar;
  }

  /**
   * Increment a string with A-Z and 0-9 (no lower case so case insensitive apps
   * like windows IE will still work)
   * 
   * @param string
   * 
   * @return the value
   */
  public static char[] incrementStringInt(char[] string) {
    if (string == null) {
      return string;
    }
  
    //loop through the string backwards
    int i = 0;
  
    for (i = string.length - 1; i >= 0; i--) {
      char inc = string[i];
      inc = incrementChar(inc);
      string[i] = inc;
  
      if (inc != 'A') {
        break;
      }
    }
  
    //if we are at 0, then it means we hit AAAAAAA (or more)
    if (i < 0) {
      return ("A" + new String(string)).toCharArray();
    }
  
    return string;
  }

  /**
   * get a cookie based on name or null if not there
   * @param cookieName
   * @param cookies (from httprequest)
   * @return the cookie
   */
  public static Cookie retrieveCookie(String cookieName, Cookie[] cookies) {

    cookies = cookies == null ? new Cookie[0] : cookies;
    for (Cookie cookie : cookies) {
      if (StringUtils.equals(cookie.getName(), cookieName)) {
        return cookie;
      }
    }
    return null;
  }
  
  /**
   * get a cookie value (null if not there)
   * @param cookieName
   * @param cookies (from httprequest)
   * @return the cookie value
   */
  public static String cookieValue(String cookieName, Cookie[] cookies) {
    Cookie cookie = retrieveCookie(cookieName, cookies);
    return cookie == null ? null : cookie.getName();
  }
  
  /**
   * kill a cookie if it is there
   * @param cookieName
   * @param cookies (from httprequest)
   * @param httpServletResponse is response for adding cookies
   */
  public static void killCookie(String cookieName, Cookie[] cookies, HttpServletResponse httpServletResponse) {
    Cookie cookie = retrieveCookie(cookieName, cookies);
    if (cookie != null) {
      cookie.setMaxAge(0);
      cookie.setValue(null);
      httpServletResponse.addCookie(cookie);
    }
  }

  /** pattern of a subject: sourceId||||subjectId  (slashes escape the pipes) */
  public static Pattern subjectPattern = Pattern.compile("^(.*)\\|\\|\\|\\|(.*)$");

  /**
   * dhtmlx option end of xml
   */
  public static final String DHTMLX_OPTIONS_END = "</complete>";

  /**
   * dhtmlx option start of xml 
   */
  public static final String DHTMLX_OPTIONS_START = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<complete>\n"; 
//utf-8, iso-8859-1

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperUiUtils.class);

  /**
   * web service format string
   */
  private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  /** subject image map */
  private static Map<String, String> subjectImageMap = null;

  /** map from source to subject screen EL */
  private static Map<String, String> subjectToScreenEl = null;

  /** map from source to subject screen EL the long version */
  private static Map<String, String> subjectToScreenElLong = null;

  /** class file dir cached */
  public static File classFileDir = null;

  /** array for converting HTML to string */
  public static final String[] HTML_REPLACE = new String[]{"&amp;","&lt;","&gt;","&#39;","&quot;"};

  /** array for converting HTML to string */
  public static final String[] HTML_REPLACE_NO_SINGLE = new String[]{"&amp;","&lt;","&gt;","&quot;"};

  /** array for converting HTML to string */
  private static final String[] HTML_SEARCH = new String[]{"&","<",">","'","\""};

  /** array for converting HTML to string */
  public static final String[] HTML_SEARCH_NO_SINGLE = new String[]{"&","<",">","\""};

  /** array for converting javascript to string */
  private static final String[] JAVASCRIPT_REPLACE = new String[]{"&amp;","&lt;","&gt;","\\'","&quot;"};

  /** array for converting javascript to string */
  private static final String[] JAVASCRIPT_SEARCH = new String[]{"&","<",">","'","\""};

  /**
   * get request params (e.g. for logging), in one string, abbreviated
   * @return request params
   */
  @SuppressWarnings("unchecked")
  public static String requestParams() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    StringBuilder requestParams = new StringBuilder();
    Map parameterMap = GrouperUtil.nonNull(httpServletRequest.getParameterMap());
    for (Object nameObject : parameterMap.keySet()) {
      String name = (String)nameObject;
      Object object = parameterMap.get(name);
      requestParams.append(name).append(" : ");
      if (object == null) {
        requestParams.append("null");
      } else if (object instanceof String[]) {
        String[] values = (String[])object;
        for (int i=0;i<50;i++) {
          if (i >= values.length) {
            break;
          }
          requestParams.append(StringUtils.abbreviate(values[i], 50)).append(", ");
        }
        if (values.length > 50) {
          requestParams.append("[more than 50 params...]");
        }
      }
      requestParams.append("; ");
    }
    return requestParams.toString();
  }


  /**
   * append an error to the request, will be logged and maybe emailed to admins
   * @param error
   */
  public static void appendErrorToRequest(String error) {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String existingError = (String)httpServletRequest.getAttribute("error");
    String newError = error;
    if (!StringUtils.isBlank(existingError)) {
      newError = existingError + "\n\n" + error;
    }
    httpServletRequest.setAttribute("error", newError);
  
  }


  /**
     * find subjects which are members of a group, and return those members.  Do this in few queries
     * since we might run out of bind variables
     * 
     * NOTE, I DONT THINK IMMEDIATE ONLY AS FALSE WILL WORK, WILL ONLY WORK WITH IMMEDIATE ONLY
     * 
     * @param grouperSession
     * @param group
     * @param subjects
     * @param immediateOnly true for only immediate, false for immediate and effective
     * @return the members or none if not allowed
     */
    public static Set<Member> convertSubjectsToMembers(GrouperSession grouperSession, 
        Group group, Set<Subject> subjects, boolean immediateOnly) {
  
      if (!PrivilegeHelper.canViewMembers(grouperSession, group, Group.getDefaultList())) {
        return new LinkedHashSet<Member>();
      }
      
      //lets do this in batches
      List<Subject> subjectsList = GrouperUtil.listFromCollection(subjects);
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectsList, 100);
      
      Set<Member> result = new LinkedHashSet<Member>();
      
      for (int i=0;i<numberOfBatches;i++) {
        List<Subject> subjectBatch = GrouperUtil.batchList(subjectsList, 100, i);
        
        //      select distinct gm.* 
        //      from grouper_members gm, grouper_memberships gms
        //      where gm.id = gms.member_id
        //      and gms.field_id = 'abc' and gms.owner_id = '123'
        //      and gm.subject_id in ('123','234')
        //      and mship_type = 'immediate'
        
        //lets turn the subjects into subjectIds
        Set<String> subjectIds = new LinkedHashSet<String>();
        for (Subject currentSubject : subjectBatch) {
          subjectIds.add(currentSubject.getId());
        }
        
        if (subjectIds.size() == 0) {
          continue;
        }
        
        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
        StringBuilder query = new StringBuilder("select gm " +
            "from Member gm, ImmediateMembershipEntry gms " +
            "where gm.uuid = gms.memberUuid " +
            "and gms.fieldId = :fieldId " +
            "and gms.ownerGroupId = :ownerId " +
            (immediateOnly ? "and gms.type = 'immediate' " : "") + 
            "and gm.subjectIdDb in (");
        
        //add all the uuids
        byHqlStatic.setString("fieldId", Group.getDefaultList().getUuid());
        byHqlStatic.setString("ownerId", group.getUuid());
        byHqlStatic.setCollectionInClause(query, subjectIds);
        query.append(")");
        List<Member> currentListPerhapsWithExtra = byHqlStatic.createQuery(query.toString())
          .list(Member.class);
        //could have two subjects with different sources with same subject id... weed those out
        Set<Member> currentMembers = removeOverlappingSubjects(currentListPerhapsWithExtra, subjectBatch);
        result.addAll(currentMembers);
        
      }
      return result;
    }


  /**
   * remove duplicates
   * @param members
   */
  public static void memberRemoveDuplicates(List<Member> members) {
    if (members == null) {
      return;
    }
    
    Iterator<Member> iterator = members.iterator();
    //keep track of ones we have seen
    Set<MultiKey> uniqueSubjects = new LinkedHashSet<MultiKey>();
    
    //loop through all items
    while(iterator.hasNext()) {
      Member member = iterator.next();
      
      //get the key
      MultiKey subjectKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId());
      
      //see if already seen
      if (uniqueSubjects.contains(subjectKey)) {
        //remove if so
        iterator.remove();
      } else {
        uniqueSubjects.add(subjectKey);
      }
    }
  }


  /**
   * remove duplicates
   * @param subjects
   */
  public static void subjectRemoveDuplicates(List<Subject> subjects) {
    if (subjects == null) {
      return;
    }
    
    Iterator<Subject> iterator = subjects.iterator();
    //keep track of ones we have seen
    Set<MultiKey> uniqueSubjects = new LinkedHashSet<MultiKey>();
    
    //loop through all items
    while(iterator.hasNext()) {
      Subject subject = iterator.next();
      
      //get the key
      MultiKey subjectKey = new MultiKey(subject.getId(), subject.getSource().getId());
      
      //see if already seen
      if (uniqueSubjects.contains(subjectKey)) {
        //remove if so
        iterator.remove();
      } else {
        uniqueSubjects.add(subjectKey);
      }
    }
  }


  /**
   * remove overlapping subjects from two lists.  i.e. if first is existing, and
   * second is new, then if we are replacing all members of the group, then the first would
   * end up being the ones to remove, and the second is the one to add.
   * this will also remove dupes
   * @param first
   * @param second
   * @return the overlap, never null
   */
  public static Set<Member> removeOverlappingSubjects(List<Member> first, List<Subject> second) {
    
    Set<Member> overlaps = new LinkedHashSet<Member>();
    
    //lets add them to hashes (multikeys)
    //multikey is the sourceId and subjectId
    //we use a listordered set so it is a set and a list all in one, unfortunately no generics
    ListOrderedSet firstHashes = new ListOrderedSet();
    ListOrderedSet secondHashes = new ListOrderedSet();
    
    memberRemoveDuplicates(first);
    subjectRemoveDuplicates(second);
    
    //if null no overlap
    if (GrouperUtil.length(first) == 0 || GrouperUtil.length(second) == 0) {
      return overlaps;
    }
    
    //put these both in hashes, keep track of hashes
    for (Member member : first) {
      firstHashes.add(new MultiKey(member.getSubjectSourceId(), member.getSubjectId()));
      
    }
    for (Subject subject : second) {
      secondHashes.add(new MultiKey(subject.getSource().getId(), subject.getId()));
    }
    
    //now lets go through, and remove if it is (or was) in the other
    {
      int i=0;
      Iterator<Member> firstIterator = first.iterator();
      while (firstIterator.hasNext()) {
        Member next = firstIterator.next();
        MultiKey hash = (MultiKey)firstHashes.get(i);
        if (secondHashes.contains(hash)) {
          firstIterator.remove();
          overlaps.add(next);
          
          //lets add the subject to the Member if not already there, so we dont have to look it up again
          Subject memberSubject = (Subject)GrouperUtil.fieldValue(next, "subj");
          if (memberSubject == null ) {
            int subjectIndex = secondHashes.indexOf(hash);
            Subject realSubject = second.get(subjectIndex);
            //a little sanity here
            if (!StringUtils.equals(realSubject.getId(), next.getSubjectId()) 
                || !StringUtils.equals(realSubject.getSource().getId(), next.getSubjectSourceId())) {
              throw new RuntimeException("These should be equal!!!");
            }
            GrouperUtil.assignField(next, "subj", realSubject);
          }
        }
        i++;
      }
    }
      
    {
      int i=0;
      Iterator<Subject> secondIterator = second.iterator();
      while (secondIterator.hasNext()) {
        secondIterator.next();
        if (firstHashes.contains(secondHashes.get(i))) {
          secondIterator.remove();
        }
        i++;
      }
    }
    return overlaps;
  }


  /**
   * 
   * @param members
   * @return the subjects
   */
  public static Set<Subject> convertMembersToSubject(Set<Member> members) {
    if (members == null) {
      return null;
    }
    Set<Subject> subjects = new LinkedHashSet<Subject>();
    for (Member member : members) {
      try {
        subjects.add(member.getSubject());
      } catch (SubjectNotFoundException snfe) {
        throw new RuntimeException("Subject not found: " + member.getSubjectSourceId() + ", " + member.getSubjectId());
      }
    }
    return subjects;
    
  }


  /**
   * 
   * @param a
   * @param b
   * @param ignoreCase if case shoul dbe ignored
   * @return 0, 1,-1
   */
  public static int compare(String a, String b, boolean ignoreCase) {
    if (a==b) {
      return 0;
    }
    if (a == null) {
      return -1;
    }
    if (b == null) {
      return 1;
    }
    if (ignoreCase) {
      return a.toLowerCase().compareTo(b.toLowerCase());
    }
    return a.compareTo(b);
  }


  /**
   * keep a-z, A-Z, 0-9, underscore, dash
   * @param string
   * @return the string (or empty if nothing left
   */
  public static String stripNonFilenameChars(String string) {
    if (string == null) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    
    //strip non filename chars
    for (int i=0;i<string.length();i++) {
      char theChar = string.charAt(i);
      if ((theChar >= 'a' && theChar <= 'z') || (theChar >= 'A' && theChar <= 'Z')
          || (theChar >= '0' && theChar <= '9') || theChar == '_' || theChar == '-') {
        result.append(theChar);
      }
    }
    return result.toString();
  }


  /**
   * lookup something in nav.properties (localized), substitute args
   * @param key
   * @param blankIfNotFound true if null or blank if not found, else it will return ???key???
   * @param escapeHtmlArgs if html should be escaped from args
   * @param args
   * @return the message
   */
  public static String message(String key, boolean blankIfNotFound, boolean escapeHtmlArgs, Object... args) {
    String message = message(key, true);
    
    //handle blank
    if (StringUtils.isBlank(message)) {
      return message(key, blankIfNotFound);
    }
    
    if (GrouperUtil.length(args) == 0) {
      return message;
    }
  
    for (int i=0;i<args.length;i++) {
      if (args[i] instanceof String) {
        args[i] = GrouperUiUtils.escapeHtml((String)args[i], true, false);
      }
    }
    
    MessageFormat formatter = new MessageFormat("");
    //note, is this correct?
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    if (request != null) {
      formatter.setLocale(request.getLocale());
    }
    
    formatter.applyPattern(message);
    message = formatter.format(args);
    return message;
    
  }


  /**
   * lookup something in nav.properties (localized)
   * @param key
   * @return the message
   */
  public static String message(String key) {
    return message(key, false);
  }


  /**
   * lookup something in nav.properties (localized)
   * @param key
   * @param blankIfNotFound true if null or blank if not found, else it will return ???key???
   * @return the message
   */
  public static String message(String key, boolean blankIfNotFound) {
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
  
    if (blankIfNotFound) {
      MapBundleWrapper mapBundleWrapper = (MapBundleWrapper)httpServletRequest.getSession().getAttribute("navNullMap");
      return (String)mapBundleWrapper.get(key);
      
    }
        
    LocalizationContext localizationContext = (LocalizationContext)httpServletRequest
        .getSession().getAttribute("nav");
    ResourceBundle nav = localizationContext.getResourceBundle();
    return nav.getString(key);
  }


  /**
   * Convenience method to retrieve nav ResourceBundle
   * @param session 
   * @return the bundle
   */
  public static ResourceBundle getNavResourcesStatic(HttpSession session) {
    LocalizationContext localizationContext = (LocalizationContext)session.getAttribute("nav");
    ResourceBundle nav = localizationContext.getResourceBundle();
    return nav;
  }


  /**
   * 
   * @param subjects to sort and page
   * @param queryPaging
   * @param searchTerm 
   * @return the set of subject, or empty set (never null)
   */
  @SuppressWarnings("unchecked")
  public static Set<Subject> subjectsSortedPaged(Set<Subject> subjects, QueryPaging queryPaging, String searchTerm) {
    
    subjects = GrouperUtil.nonNull(subjects);
    
    //if we are getting size, set it
    if (queryPaging.isDoTotalCount()) {
      queryPaging.setTotalRecordCount(subjects.size());
      queryPaging.calculateIndexes();
    }
  
    if (subjects.size() == 0) {
      return subjects;
    }
    
    int maxSubjectSortSize = TagUtils.mediaResourceInt("comparator.sort.limit", 400);
    
    //see if we should sort
    if (subjects.size() < maxSubjectSortSize) {
      
      //lets convert to a wrapper which has a sort field
      List<SubjectSortWrapper> subjectsSorted = new ArrayList<SubjectSortWrapper>();
      for (Subject subject : subjects) {
        subjectsSorted.add(new SubjectSortWrapper(subject));
      }
      
      //sort it
      Collections.sort(subjectsSorted);
      
      //convert back to set
      subjects = new LinkedHashSet<Subject>(subjectsSorted);
      
      //lets bring more important things to the top
      subjects = SubjectHelper.sortSetForSearch(subjects, searchTerm);
      
    }
    
    //get the page
    
    int numberOfPages = GrouperUtil.batchNumberOfBatches(subjects.size(), queryPaging.getPageSize());
    
    //dont let a dynamic query let the page number go off the screen
    int pageNumber = numberOfPages >= queryPaging.getPageNumber() ? queryPaging.getPageNumber() : numberOfPages;
    
    List<Subject> subjectsList = GrouperUtil.batchList(GrouperUtil.listFromCollection(subjects), 
        queryPaging.getPageSize(), pageNumber-1);
    
    //convert yet again back to set
    subjects = new LinkedHashSet<Subject>(subjectsList);
    
    return subjects;
  }


  /**
   * 
   * @param members to sort and page
   * @param queryPaging
   * @return the set of subject, or empty set (never null)
   */
  @SuppressWarnings("unchecked")
  public static Set<Member> membersSortedPaged(Set<Member> members, QueryPaging queryPaging) {
    
    members = GrouperUtil.nonNull(members);
    
    //if we are getting size, set it
    if (queryPaging.isDoTotalCount()) {
      queryPaging.setTotalRecordCount(members.size());
      queryPaging.calculateIndexes();
    }
  
    if (members.size() == 0) {
      return members;
    }
    
    int maxSubjectSortSize = TagUtils.mediaResourceInt("comparator.sort.limit", 200);
    
    //see if we should sort
    if (members.size() < maxSubjectSortSize) {
  
      List<MemberSortWrapper> membersSorted = new ArrayList<MemberSortWrapper>();
  
      //lets convert to a wrapper which has a sort field
      for (Member member : members) {
        membersSorted.add(new MemberSortWrapper(member));
      }
      
      //sort it
      Collections.sort(membersSorted);
      
      //convert back to set
      members = new LinkedHashSet<Member>();
      for (MemberSortWrapper memberSortWrapper : membersSorted) {
        members.add(memberSortWrapper.getWrappedMember());
      }
    }
    
    //get the page
    
    int numberOfPages = GrouperUtil.batchNumberOfBatches(members.size(), queryPaging.getPageSize());
    
    //dont let a dynamic query let the page number go off the screen
    int pageNumber = numberOfPages >= queryPaging.getPageNumber() ? queryPaging.getPageNumber() : numberOfPages;
    
    List<Member> membersList = GrouperUtil.batchList(new ArrayList<Member>(members), queryPaging.getPageSize(), pageNumber-1);
    //convert yet again back to set
    members = new LinkedHashSet<Member>(membersList);
    
    return members;
  }



  /**
   * convert a subject to string for screen
   * @param subject
   * @return the string
   */
  public static String convertSubjectToValue(Subject subject) {
    String value = subject.getSource().getId() + "||||" + subject.getId();
    return value;
  }


  /**
   * convert a subject to string for screen
   * @param subject
   * @return the string
   */
  public static String convertSubjectToLabel(Subject subject) {
    return convertSubjectToLabelConfigured(subject, false);
  }

  /**
   * convert a subject to string for screen e.g. for tooltip
   * @param subject
   * @return the string
   */
  public static String convertSubjectToLabelLong(Subject subject) {
    return convertSubjectToLabelConfigured(subject, true);
  }


  /**
   * find a subject based on search string.  must be sourceId||||subjectId 
   * or a subjectId or subjectIdentifier which is unique
   * @param searchString
   * @param exceptionIfNotFound
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   * @throws SourceUnavailableException 
   */
  public static Subject findSubject(String searchString, boolean exceptionIfNotFound) 
      throws SubjectNotFoundException, SubjectNotUniqueException, SourceUnavailableException {
    if (searchString == null) {
      throw new SubjectNotFoundException("Cant find null string");
    }
    Matcher matcher = subjectPattern.matcher(searchString);
  
    //if it matches sourceId||||subjectId then we know exactly which subject
    if (matcher.matches()) {
      String sourceId = matcher.group(1);
      String subjectId = matcher.group(2);
      return SubjectFinder.findByIdAndSource(subjectId, sourceId, exceptionIfNotFound);
    }
    
    //if not, then try to get by subjectId or identifier
    return SubjectFinder.findByIdOrIdentifier(searchString, exceptionIfNotFound);
  }


  /**
   * make one dhtmlx option
   * @param result to append to
   * @param value
   * @param label
   * @param imageUrl
   */
  public static void dhtmlxOptionAppend(StringBuilder result, String value, String label, String imageUrl) {
    
    //<option value="1">one</option>
    result.append("   <option value=\"").append(escapeHtml(StringUtils.defaultString(value), true, false))
      .append("\"");
    
    //only append image if there is one
    if (!StringUtils.isBlank(imageUrl)) {
      if (!imageUrl.contains("/")) {
        imageUrl = "../../grouperExternal/public/assets/images/" + imageUrl;
      }
  
      result.append(" img_src=\"" + escapeHtml(imageUrl, true) + "\"");
    }
    result.append(">").append(escapeHtml(label, true, false)).append("</option>\n");
  }


  /**
   * Print some text to the screen
   * @param string 
   * @param httpContentType e.g. "text/html", "text/xml"
   * @param includeXmlTag 
   * @param includeHtmlTag 
   * 
   */
  public static void printToScreen(String string, HttpContentType httpContentType, 
      boolean includeXmlTag, boolean includeHtmlTag) {
  
    HttpServletResponse response = GrouperUiFilter.retrieveHttpServletResponse(); 
  
    //say it is HTML, if not too late
    if (httpContentType != null && !response.isCommitted()) {
      response.setContentType(httpContentType.getContentType());
    }
  
    //just write some stuff
    PrintWriter out = null;
  
    try {
      out = response.getWriter();
    } catch (Exception e) {
      throw new RuntimeException("Cant get response.getWriter: ", e);
    }
  
    if (includeXmlTag) {
      out.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
        + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
              + "Transitional//EN\" " +
                  "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    }
  
    //see if we should add <html> etc
    if (includeHtmlTag) {
      out.println("<html><head></head><body>");
      out.println(string);
      out.println("</body></html>");
    } else {
      out.println(string);
    }
  
    out.close();
  
  }


  /**
   * escape single quotes for javascript
   * @param input
   * @return the escaped string
   */
  public static String escapeSingleQuotes(String input) {
    return StringUtils.replace(input, "'", "\\'");
  }


  /**
   * get a cookie value by name, null if not there
   * @param prefix
   */
  public static void removeCookiesByPrefix(String prefix) {
    HttpServletResponse httpServletResponse = GrouperUiFilter.retrieveHttpServletResponse();
    
    List<Cookie> cookies = findCookiesByPrefix(prefix);
    for (Cookie cookie : cookies) {
      cookie.setMaxAge(0);
      //note: this is needed for websec cookies... is it for all cookies?
      cookie.setPath("/");
      httpServletResponse.addCookie(cookie);
      
      if (httpServletResponse.isCommitted()) {
        LOG.error("Trying to kill cookie: " + cookie.getName() + ", but the response is committed!", new RuntimeException("stack"));
      }
      
    }
  }


  /**
   * find a cookie or empty list if cant find
   * @param name
   * @return the cookies or empty list if not found
   */
  public static List<Cookie> findCookiesByPrefix(String name) {
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    StringBuilder allCookies = null;
    boolean isDebug = LOG.isDebugEnabled();
    if (isDebug) {
      allCookies = new StringBuilder("Looking for cookie with prefix: '" + name + "'");
    }
  
    List<Cookie> cookieList = new ArrayList<Cookie>();
    Cookie[] cookies = httpServletRequest.getCookies();
    //go through all cookies and find the cookie by name
    int cookiesLength = GrouperUtil.length(cookies);
    for (int i=0;i<cookiesLength;i++) {
      if (StringUtils.indexOf(cookies[i].getName(), name) == 0) {
        cookieList.add(cookies[i]);
        if (isDebug) {
          allCookies.append(", Found cookie: " + cookies[i].getName());
        }
      } else {
        if (isDebug) {
          allCookies.append(", Didnt find cookie: " + cookies[i].getName());
        }
      }
      
    }
    if (isDebug) {
      LOG.debug(allCookies.toString());
    }
    return cookieList;
  }


  /**
   * get the image name from subject source
   * @param sourceId
   * @return the relative path to image path
   */
  public static String imageFromSubjectSource(String sourceId) {
    if (subjectImageMap == null) {
      Map<String, String> theSubjectImageMap = new HashMap<String, String>();
      Properties propertiesSettings = GrouperUiConfig.retrieveConfig().properties();
      
      int index = 0;
      while (true) {
        
        String sourceName = GrouperUtil.propertiesValue(propertiesSettings, 
            "grouperUi.subjectImg.sourceId." + index);
        String imageName = GrouperUtil.propertiesValue(propertiesSettings, 
            "grouperUi.subjectImg.image." + index);
        
        if (StringUtils.isBlank(imageName)) {
          break;
        }
        
        theSubjectImageMap.put(sourceName, imageName);
        
        index++;
      }
      subjectImageMap = theSubjectImageMap;
    }
    String imageName = subjectImageMap.get(sourceId);
    if (!StringUtils.isBlank(imageName)) {
      imageName = "../../grouperExternal/public/assets/images/" + imageName;
    }
    return imageName;
  }

  /**
   * get a label from a subject based on media.properties
   * @param subject
   * @return the relative path to image path
   */
  public static String convertSubjectToLabelConfigured(Subject subject) {
    return convertSubjectToLabelConfigured(subject, true);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject subject = SubjectFinder.findByIdentifierAndSource("edu:someGroup", "g:gsa", true);

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subject", subject);
    variableMap.put("grouperUiUtils", new GrouperUiUtils());
    String result = GrouperUtil.substituteExpressionLanguage("${subject.getAttributeValue('displayName')}", variableMap);
    System.out.println(result);
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * get a label from a subject based on media.properties
   * @param subject
   * @param tryLong if should see if there is a long version first
   * @return the relative path to image path
   */
  public static String convertSubjectToLabelConfigured(Subject subject, boolean tryLong) {
    if (subject == null) {
      return "";
    }
    //see if it is already computed
    if (subject instanceof SubjectSortWrapper) {
      return ((SubjectSortWrapper)subject).getScreenLabelLong();
    }
  
    if (subjectToScreenEl == null) {
      {
        Map<String, String> theSubjectToScreenEl = new HashMap<String, String>();
        Properties propertiesSettings = GrouperUiConfig.retrieveConfig().properties();
        
        int index = 0;
        while (true) {
        
          String sourceName = GrouperUtil.propertiesValue(propertiesSettings, 
              "grouperUi.subjectImg.sourceId." + index);
          String screenEl = GrouperUtil.propertiesValue(propertiesSettings, 
              "grouperUi.subjectImg.screenEl." + index);
          
          if (StringUtils.isBlank(sourceName)) {
            break;
          }
          if (!StringUtils.isBlank(screenEl)) {
            theSubjectToScreenEl.put(sourceName, screenEl);
          }
          
          index++;
        }
        subjectToScreenEl = theSubjectToScreenEl;
      }
      {
        Map<String, String> theSubjectToScreenElLong = new HashMap<String, String>();

        Properties propertiesSettings = GrouperUiConfig.retrieveConfig().properties();
        
        int index = 0;
        while (true) {
        
          String sourceName = GrouperUtil.propertiesValue(propertiesSettings, 
              "grouperUi.subjectImgLong.sourceId." + index);
          String screenElLong = GrouperUtil.propertiesValue(propertiesSettings, 
              "grouperUi.subjectImgLong.screenEl." + index);
          
          if (StringUtils.isBlank(sourceName)) {
            break;
          }
          
          if (!StringUtils.isBlank(screenElLong)) {
            theSubjectToScreenElLong.put(sourceName, screenElLong);
          }
          
          index++;
        }
        subjectToScreenElLong = theSubjectToScreenElLong;
      }
    }
    String screenEl = null;
    if (tryLong) {
      screenEl = subjectToScreenElLong.get(subject.getSource().getId());
    }
    if (StringUtils.isBlank(screenEl)) {
      screenEl = subjectToScreenEl.get(subject.getSource().getId());
    }
    if (StringUtils.isBlank(screenEl)) {

      String label = subject.getDescription();
      if (StringUtils.isBlank(label)) {

        label = subject.getSourceId() + " - " + subject.getId() + " - " + subject.getName();
      }

      return label;

    }
    //run the screen EL
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subject", subject);
    variableMap.put("grouperUiUtils", new GrouperUiUtils());
    String result = GrouperUtil.substituteExpressionLanguage(screenEl, variableMap, false, true, true);
    return result;
  }


  /**
   * get the class file dir
   * @return the class file dir
   */
  public static File classFileDir() {
    if (classFileDir == null) {
      classFileDir = GrouperUtil.fileFromResourceName("grouperUiText.properties").getParentFile();
    }
    return classFileDir;
  }


  /** 
   * list files with a certain extension.  Note, there cannot be more than 10000
   * files or exception will be throws
   * @param dir
   * @param extension if this is the empty string it should list all
   * @return the array of files
   */
  public static List<File> listFilesByExtensionRecursive(File dir, String extension) {
    List<File> theList = new ArrayList<File>();
    listFilesByExtensionRecursiveHelper(dir, extension, theList);
    return theList;
  }


  /** 
   * list files with a certain extension 
   * @param dir
   * @param extension if this is the empty string it should list all
   * @param theList is the current list to append to
   */
  public static void listFilesByExtensionRecursiveHelper(File dir, String extension,
      List<File> theList) {
    //see if its a directory
    if (!dir.exists()) {
      throw new RuntimeException("The directory: " + dir + " does not exist");
    }
    if (!dir.isDirectory()) {
      throw new RuntimeException("The directory: " + dir + " is not a directory");
    }
  
    //get the files into a list
    File[] allFiles = listFilesByExtension(dir, extension);
  
    //loop through the array
    for (int i = 0; i < allFiles.length; i++) {
      if (StringUtils.contains(allFiles[i].getName(), "..")) {
        continue; //dont go to the parent directory
      }
  
      if (allFiles[i].isFile()) {
  
        //make sure not too big
        if (theList.size() > 10000) {
          throw new RuntimeException("File list too large: " + dir.getAbsolutePath()
              + ", " + extension);
        }
  
        //add to list
        theList.add(allFiles[i]);
      } else {
        //ignore, we will do all dirs in good time
      }
    }
  
    //do all the subdirs
    File[] allSubdirs = listSubdirs(dir);
    int allSubdirsLength = allSubdirs == null ? 0 : allSubdirs.length;
    for (int i = 0; i < allSubdirsLength; i++) {
      listFilesByExtensionRecursiveHelper(allSubdirs[i], extension, theList);
    }
  
  }


  /**
   * get the subdirs of a dir (not ..)
   * @param dir
   * @return the dirs
   */
  public static File[] listSubdirs(File dir) {
    //see if its a directory
    if (!dir.exists()) {
      throw new RuntimeException("The directory: " + dir + " does not exist");
    }
    if (!dir.isDirectory()) {
      throw new RuntimeException("The directory: " + dir + " is not a directory");
    }
  
    File[] subdirs = dir.listFiles(new FileFilter() {
  
      public boolean accept(File pathname) {
        if (StringUtils.contains(pathname.getName(), "..")) {
          return false; //dont go to the parent directory
        }
        //allow dirs
        if (pathname.isDirectory()) {
          return true;
        }
        //must not be a dir
        return false;
      }
  
    });
  
    return subdirs;
  }


  /** 
   * list files with a certain extension 
   * @param dir
   * @param extension if this is the empty string it should list all
   * @return the array of files
   */
  public static File[] listFilesByExtension(File dir, String extension) {
    final String finalExtension = extension;
  
    FilenameFilter fileFilter = new FilenameFilter() {
  
      /*
       * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
       */
      public boolean accept(File theDir, String name) {
        if ((name != null) && name.endsWith(finalExtension)) {
          //doubt we would ever look for .., but in case
          if (StringUtils.contains(finalExtension, "..")) {
            return true;
          }
          //if the file is .., then its not what we are looking for
          if (StringUtils.contains(name, "..")) {
            return false;
          }
          return true;
        }
  
        return false;
      }
    };
  
    return dir.listFiles(fileFilter);
  }


  /**
   * convert a boolean to a T or F
   * 
   * @param theBoolean
   * @return the one char booloean string
   */
  public static String booleanToStringOneChar(Boolean theBoolean) {
    if (theBoolean == null) {
      return null;
    }
    return theBoolean ? "T" : "F";
  }


  /**
   * convert a date to a string using the standard web service pattern
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * 
   * @param date
   * @return the string, or null if the date is null
   */
  public static String dateToString(Date date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
    return simpleDateFormat.format(date);
  }


  /**
   * convert a jsp to string.  This doesnt work from unit tests, but will work from web requests or daemons
   * @param jspName e.g. whatever.jsp, or /somePath/something.jsp
   * @return the eval version of the jsp
   */
  public static String convertJspToString(String jspName) {
    
    //default is in the jsp dir
    if (!jspName.contains("/")) {
      jspName = "/jsp/" + jspName;
    }
    
    HttpServlet servlet = GrouperUiFilter.retrieveHttpServlet();
    ServletContext servletContext = servlet.getServletContext();
    
    //get this from context not request, since could be in daemon
    RequestDispatcher dispatcher = servletContext.getRequestDispatcher(jspName);
  
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    HttpServletResponse response = GrouperUiFilter.retrieveHttpServletResponse();
    
    //RequestDispatcher dispatcher = request.getRequestDispatcher(jspName);
    //wrap the response so that the output goes to a string
    GenericServletResponseWrapper responseWrapper = new GenericServletResponseWrapper(response);
      
    try {
      dispatcher.include(request, responseWrapper);
    } catch (Exception e) {
      throw new RuntimeException("Problem converting JSP to string: " + jspName, e);
    }
  
    String result = responseWrapper.resultString();
    
    //see if we are logging
    if (TagUtils.mediaResourceBoolean("grouperUi.logHtml", false)) {
      String logDir = TagUtils.mediaResourceString("grouperUi.logHtmlDir");
      
      if (StringUtils.isBlank(logDir)) {
        throw new RuntimeException("Cant log html to file with dir to put files in: grouperUi.logHtmlDir");
      }
      
      File htmlFile = null;
      
      String jspNameforLog = GrouperUtil.suffixAfterChar(jspName, '/');
      jspNameforLog = GrouperUtil.suffixAfterChar(jspNameforLog, '\\');
      
      logDir = GrouperUtil.stripEnd(logDir, "/");
      logDir = GrouperUtil.stripEnd(logDir, "\\");
      Date date = new Date();
      String logName = logDir  + File.separator + "htmlLog_" 
        + new SimpleDateFormat("yyyy_MM").format(date)
        + File.separator + "day_" 
        + new SimpleDateFormat("dd" + File.separator + "HH_mm_ss_SSS").format(date)
        + "_" + ((int)(1000 * Math.random())) + "_" + jspNameforLog + ".html";
        
      htmlFile = new File(logName);
      
      //make parents
      GrouperUtil.mkdirs(htmlFile.getParentFile());
      
      GrouperUtil.saveStringIntoFile(htmlFile, result);
      
    }
  
    return result;
    
  }


  /**
   * Convert an XML string to HTML to display on the screen
   * 
   * @param input
   *          is the XML to convert
   * @param isEscape true to escape chars, false to unescape
   * 
   * @return the HTML converted string
   */
  public static String escapeHtml(String input, boolean isEscape) {
    return escapeHtml(input, isEscape, true);
  }


  /**
   * Convert an XML string to HTML to display on the screen
   * 
   * @param input
   *          is the XML to convert
   * @param isEscape true to escape chars, false to unescape
   * @param escapeSingleQuotes true to escape single quotes too
   * 
   * @return the HTML converted string
   */
  public static String escapeHtml(String input, boolean isEscape, boolean escapeSingleQuotes) {
    if (escapeSingleQuotes) {
      if (isEscape) {
        return GrouperUtil.replace(input, HTML_SEARCH, HTML_REPLACE);
      }
      return GrouperUtil.replace(input, HTML_REPLACE, HTML_SEARCH);
    }
    if (isEscape) {
      return GrouperUtil.replace(input, HTML_SEARCH_NO_SINGLE, HTML_REPLACE_NO_SINGLE);
    }
    return GrouperUtil.replace(input, HTML_REPLACE_NO_SINGLE, HTML_SEARCH_NO_SINGLE);
    
  }

  /**
   * Escapes XML ( ampersand, lessthan, greater than, double quote), and single quote with slash
   * 
   * @param input
   *          is the XML to convert
   * @param isEscape true to escape chars, false to unescape
   * 
   * @return the Javascript converted string
   */
  public static String escapeJavascript(String input, boolean isEscape) {
    if (isEscape) {
      return GrouperUtil.replace(input, JAVASCRIPT_SEARCH, JAVASCRIPT_REPLACE);
    }
    return GrouperUtil.replace(input, JAVASCRIPT_REPLACE, JAVASCRIPT_SEARCH);
    
  }


  /**
   * clone a collection, shallow, do not clone all objects inside
   * @param <T> 
   * @param object
   * @return the cloned collection
   */
  @SuppressWarnings("unchecked")
  public static <T> T cloneShallow(T object) {
    if (object == null) {
      return null;
    }
    if (object instanceof ArrayList) {
      return (T)((ArrayList)object).clone();
    }
    if (object instanceof LinkedList) {
      return (T)((LinkedList)object).clone();
    }
    if (object instanceof HashSet) {
      return (T)((HashSet)object).clone();
    }
    if (object instanceof LinkedHashSet) {
      return (T)((LinkedHashSet)object).clone();
    }
    if (object instanceof HashMap) {
      return (T)((HashMap)object).clone();
    }
    if (object instanceof LinkedHashMap) {
      return (T)((LinkedHashMap)object).clone();
    }
    if (object instanceof TreeMap) {
      return (T)((TreeMap)object).clone();
    }
    //cant clone
    throw new RuntimeException("Unsupported object type: " + GrouperUtil.className(object));
  }
  
}
