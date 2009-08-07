/*
 * @author mchyzer
 * $Id: GuiUtils.java,v 1.3 2009-08-07 07:36:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.GenericServletResponseWrapper;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
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
    
    Properties propertiesSettings = propertiesGrouperUiSettings(); 
    String maxSubjectSortSizeString = GrouperUtil.propertiesValue(propertiesSettings, "grouperUi.max.subject.sort.size");
    int maxSubjectSortSize = GrouperUtil.intValue(maxSubjectSortSizeString, 200);
    
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
    
    Properties propertiesSettings = propertiesGrouperUiSettings(); 
    String maxSubjectSortSizeString = GrouperUtil.propertiesValue(propertiesSettings, "grouperUi.max.members.sort.size");
    int maxSubjectSortSize = GrouperUtil.intValue(maxSubjectSortSizeString, 200);
    
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
    String label = subject.getDescription();
    if (StringUtils.isBlank(label)) {
      
      if ("g:gsa".equals(subject.getSource().getId())) {
        label = subject.getName();
      } else {
      
        label = subject.getSource().getId() + " - " + subject.getId() + " - " + subject.getName();
      }
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
        imageUrl = "../public/assets/" + imageUrl;
      }

      result.append(" img_src=\"" + escapeHtml(imageUrl, true) + "\"");
    }
    result.append(">").append(escapeHtml(label, true, false)).append("</option>\n");
  }
  
  /**
   * Print some text to the screen
   * @param string 
   * @param contentType e.g. "text/html", "text/xml"
   * @param includeXmlTag 
   * @param includeHtmlTag 
   * 
   */
  public static void printToScreen(String string, String contentType, boolean includeXmlTag, boolean includeHtmlTag) {
  
    HttpServletResponse response = GrouperUiJ2ee.retrieveHttpServletResponse(); 
    
    //say it is HTML, if not too late
    if (!response.isCommitted()) {
      response.setContentType(contentType);
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
      Properties propertiesSettings = propertiesGrouperUiSettings();
      
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
      imageName = "../public/assets/" + imageName;
    }
    return imageName;
  }
  
  /** map from source to subject screen EL */
  private static Map<String, String> subjectToScreenEl = null;
  
  /**
   * get a label from a subject based on grouperUiSettings.properties
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
      Properties propertiesSettings = propertiesGrouperUiSettings();
      
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

  /**
   * get the text properties, might be cached
   * @return the properties
   */
  public static Properties propertiesUiTextGui() {
    Properties propertiesSettings = propertiesGrouperUiSettings();
    boolean cache = GrouperUtil.propertiesValueBoolean(propertiesSettings, 
      "grouperUi.cache.uiText", true);
  
    Properties properties = GrouperUtil.propertiesFromResourceName(
        "grouperUiText.properties", cache , true);
    return properties;
  }

  /**
   * properties object for grouper ui settings
   * @return properties
   */
  public static Properties propertiesGrouperUiSettings() {
    return GrouperUtil.propertiesFromResourceName(
      "grouperUiSettings.properties");
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
   * @param jspName e.g. whatever.jsp, or /assetsJsp/something.jsp
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

    return responseWrapper.resultString();
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

}
