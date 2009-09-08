/*
 * @author mchyzer
 * $Id: GuiUtils.java,v 1.15 2009-09-08 18:53:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
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
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GenericServletResponseWrapper;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.tags.TagUtils;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * utils for the gui
 */
public class GuiUtils {

  /**
   * get request params (e.g. for logging), in one string, abbreviated
   * @return request params
   */
  @SuppressWarnings("unchecked")
  public static String requestParams() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
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
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
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
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjects, 100);
    
    Set<Member> result = new LinkedHashSet<Member>();
    
    for (int i=0;i<numberOfBatches;i++) {
      List<Subject> subjectBatch = GrouperUtil.batchList(subjects, 100, i);
      
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
          "from Member gm, Membership gms " +
          "where gm.uuid = gms.memberUuid " +
          "and gms.fieldId = :fieldId " +
          "and gms.ownerUuid = :ownerId " +
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
        args[i] = GuiUtils.escapeHtml((String)args[i], true, false);
      }
    }
    
    MessageFormat formatter = new MessageFormat("");
    //note, is this correct?
    HttpServletRequest request = GrouperUiJ2ee.retrieveHttpServletRequest();
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
    
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();

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
   * @return the set of subject, or empty set (never null)
   */
  @SuppressWarnings("unchecked")
  public static Set<Subject> subjectsSortedPaged(Set<Subject> subjects, QueryPaging queryPaging) {
    
    subjects = GrouperUtil.nonNull(subjects);
    
    //if we are getting size, set it
    if (queryPaging.isDoTotalCount()) {
      queryPaging.setTotalRecordCount(subjects.size());
      queryPaging.calculateIndexes();
    }

    if (subjects.size() == 0) {
      return subjects;
    }
    
    int maxSubjectSortSize = TagUtils.mediaResourceInt("comparator.sort.limit", 200);
    
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
    }
    
    //get the page
    
    int numberOfPages = GrouperUtil.batchNumberOfBatches(subjects.size(), queryPaging.getPageSize());
    
    //dont let a dynamic query let the page number go off the screen
    int pageNumber = numberOfPages >= queryPaging.getPageNumber() ? queryPaging.getPageNumber() : numberOfPages;
    
    List<Subject> subjectsList = GrouperUtil.batchList(subjects, queryPaging.getPageSize(), pageNumber-1);
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
    
    List<Member> membersList = GrouperUtil.batchList(members, queryPaging.getPageSize(), pageNumber-1);
    //convert yet again back to set
    members = new LinkedHashSet<Member>(membersList);
    
    return members;
  }

  /**
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public static String substituteExpressionLanguage(String stringToParse, Map<String, Object> variableMap) {
    if (GrouperUtil.isBlank(stringToParse)) {
      return stringToParse;
    }
    try {
      JexlContext jc = JexlHelper.createContext();

      int index = 0;
      
      for (String key: variableMap.keySet()) {
        jc.getVars().put(key, variableMap.get(key));
      }
      
      //allow utility methods
      jc.getVars().put("guiUtils", new GuiUtils());
      
      // matching ${ exp }   (non-greedy)
      Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
      Matcher matcher = pattern.matcher(stringToParse);
      
      StringBuilder result = new StringBuilder();

      //loop through and find each script
      while(matcher.find()) {
        result.append(stringToParse.substring(index, matcher.start()));
        
        //here is the script inside the curlies
        String script = matcher.group(1);
        
        Expression e = ExpressionFactory.createExpression(script);

        //this is the result of the evaluation
        Object o = e.evaluate(jc);
  
        if (o == null) {
          LOG.warn("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
              + GrouperUtil.toStringForLog(variableMap.keySet()));
        }
        
        result.append(o);
        
        index = matcher.end();
      }
      
      result.append(stringToParse.substring(index, stringToParse.length()));
      return result.toString();
      
    } catch (Exception e) {
      throw new RuntimeException("Error substituting string: '" + stringToParse + "'", e);
    }
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
    String label = null;
    if ("g:gsa".equals(subject.getSource().getId())) {
      
      label = subject.getAttributeValue(GrouperConfig.ATTR_DISPLAY_NAME);
      if (!StringUtils.isBlank(label)) {
        return label;
      }
      
    }

    label = subject.getDescription();
    if (StringUtils.isBlank(label)) {
      
      label = subject.getSource().getId() + " - " + subject.getId() + " - " + subject.getName();
    }
    return label;
  }
  
  /** pattern of a subject: sourceId||||subjectId  (slashes escape the pipes) */
  private static Pattern subjectPattern = Pattern.compile("^(.*)\\|\\|\\|\\|(.*)$");
  
  /**
   * find a subject based on search string.  must be sourceId||||subjectId 
   * or a subjectId or subjectIdentifier which is unique
   * @param searchString
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   * @throws SourceUnavailableException 
   */
  public static Subject findSubject(String searchString) 
      throws SubjectNotFoundException, SubjectNotUniqueException, SourceUnavailableException {
    if (searchString == null) {
      throw new SubjectNotFoundException("Cant find null string");
    }
    Matcher matcher = subjectPattern.matcher(searchString);

    //if it matches sourceId||||subjectId then we know exactly which subject
    if (matcher.matches()) {
      String sourceId = matcher.group(1);
      String subjectId = matcher.group(2);
      Source source = SubjectFinder.getSource(sourceId);
      return source.getSubject(subjectId);
    }
    
    //if not, then try to get by subjectId or identifier
    return SubjectFinder.findByIdOrIdentifier(searchString, true);
  }
  
  /**
   * dhtmlx option end of xml
   */
  public static final String DHTMLX_OPTIONS_END = "</complete>";
  
  /**
   * dhtmlx option start of xml 
   */
  public static final String DHTMLX_OPTIONS_START = "<?xml version=\"1.0\" ?>\n<complete>\n";

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
        imageUrl = "../public/assets/images/" + imageUrl;
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

    HttpServletResponse response = GrouperUiJ2ee.retrieveHttpServletResponse(); 

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
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GuiUtils.class);
  /**
   * web service format string
   */
  private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  /**
   * get a cookie value by name, null if not there
   * @param prefix
   */
  public static void removeCookiesByPrefix(String prefix) {
    HttpServletResponse httpServletResponse = GrouperUiJ2ee.retrieveHttpServletResponse();
    
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
    
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
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

  /** subject image map */
  private static Map<String, String> subjectImageMap = null;
  
  /**
   * get the image name from subject source
   * @param sourceId
   * @return the relative path to image path
   */
  public static String imageFromSubjectSource(String sourceId) {
    if (subjectImageMap == null) {
      Map<String, String> theSubjectImageMap = new HashMap<String, String>();
      Properties propertiesSettings = GrouperUtil
        .propertiesFromResourceName("resources/grouper/media.properties");
      
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
      imageName = "../public/assets/images/" + imageName;
    }
    return imageName;
  }
  
  /** map from source to subject screen EL */
  private static Map<String, String> subjectToScreenEl = null;
  
  /**
   * get a label from a subject based on media.properties
   * @param subject
   * @return the relative path to image path
   */
  public static String convertSubjectToLabelConfigured(Subject subject) {
    
    //see if it is already computed
    if (subject instanceof SubjectSortWrapper) {
      return ((SubjectSortWrapper)subject).getScreenLabel();
    }

    if (subjectToScreenEl == null) {
      Map<String, String> theSubjectToScreenEl = new HashMap<String, String>();
      Properties propertiesSettings = GrouperUtil
        .propertiesFromResourceName("resources/grouper/media.properties");
      
      int index = 0;
      while (true) {
      
        String sourceName = GrouperUtil.propertiesValue(propertiesSettings, 
            "grouperUi.subjectImg.sourceId." + index);
        String screenEl = GrouperUtil.propertiesValue(propertiesSettings, 
            "grouperUi.subjectImg.screenEl." + index);
        
        if (StringUtils.isBlank(screenEl)) {
          break;
        }
        
        theSubjectToScreenEl.put(sourceName, screenEl);
        
        index++;
      }
      subjectToScreenEl = theSubjectToScreenEl;
    }
    String screenEl = subjectToScreenEl.get(subject.getSource().getId());
    if (StringUtils.isBlank(screenEl)) {
      return convertSubjectToLabel(subject);
    }
    //run the screen EL
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subject", subject);
    return substituteExpressionLanguage(screenEl, variableMap);
  }

  /** class file dir cached */
  private static File classFileDir = null;
  
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
  private static void listFilesByExtensionRecursiveHelper(File dir, String extension,
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
    
    HttpServlet servlet = GrouperUiJ2ee.retrieveHttpServlet();
    ServletContext servletContext = servlet.getServletContext();
    
    //get this from context not request, since could be in daemon
    RequestDispatcher dispatcher = servletContext.getRequestDispatcher(jspName);

    HttpServletRequest request = GrouperUiJ2ee.retrieveHttpServletRequest();
    HttpServletResponse response = GrouperUiJ2ee.retrieveHttpServletResponse();
    
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

  /** array for converting HTML to string */
  private static final String[] HTML_REPLACE = new String[]{"&amp;","&lt;","&gt;","&#39;","&quot;"};

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

  /** array for converting HTML to string */
  private static final String[] HTML_REPLACE_NO_SINGLE = new String[]{"&amp;","&lt;","&gt;","&quot;"};
  /** array for converting HTML to string */
  private static final String[] HTML_SEARCH = new String[]{"&","<",">","'","\""};
  /** array for converting HTML to string */
  private static final String[] HTML_SEARCH_NO_SINGLE = new String[]{"&","<",">","\""};

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
