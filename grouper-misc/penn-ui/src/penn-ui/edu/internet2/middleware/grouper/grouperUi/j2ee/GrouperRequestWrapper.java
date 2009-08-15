/*
 * @author mchyzer
 * $Id: GrouperRequestWrapper.java,v 1.4 2009-08-15 06:40:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.j2ee;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.tags.TagUtils;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 *
 */
public class GrouperRequestWrapper extends HttpServletRequestWrapper {

  /** wrapper around session */
  private GrouperSessionWrapper grouperSessionWrapper = null;
  
  /** if this is a multipart form which accesses parameters differently */
  boolean multipart;
  
  /**
   * @see javax.servlet.ServletRequestWrapper#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute(String name) {
    return super.getAttribute(name);
  }

  /**
   * get a param from file request as fileItem
   * @param name
   * @return the param
   */
  public FileItem getParameterFileItem(String name) {
    return (FileItem)this.multipartParamMap.get(name);
  }
  
  /**
   * <pre>
   * problem with tomcat and request wrappers, so stash this and
   * get back later:
   * http://brian.pontarelli.com/2008/01/16/httpservletrequestwrapper-tomcat-and-forwards/
   * </pre>
   */
  private StringBuffer requestURL = null;
  
  /** map of multipart mime data.  From String of the name submitted to either a FileItem or a List<String> of data */
  private java.util.Map<String,Object> multipartParamMap;

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperUiRestServlet.class);

  /**
   * @param request
   */
  public GrouperRequestWrapper(HttpServletRequest request) {
    super(request);
    this.requestURL = request.getRequestURL();
    this.multipart = ServletFileUpload.isMultipartContent(request);
    this.initMultipartMap();
  }

  /**
   * 
   * @see javax.servlet.http.HttpServletRequestWrapper#getSession()
   */
  @Override
  public HttpSession getSession() {
    HttpSession session = super.getSession();
    
    if (this.grouperSessionWrapper == null 
        || this.grouperSessionWrapper.getHttpSession() != session) {
      this.grouperSessionWrapper = new GrouperSessionWrapper(session);
    }
    
    return this.grouperSessionWrapper;
  }

  /**
   * 
   * @see javax.servlet.http.HttpServletRequestWrapper#getSession(boolean)
   */
  @Override
  public HttpSession getSession(boolean create) {
    HttpSession session = super.getSession(create);
    
    if (this.grouperSessionWrapper == null 
        || this.grouperSessionWrapper.getHttpSession() != session) {
      this.grouperSessionWrapper = new GrouperSessionWrapper(session);
    }
    
    return this.grouperSessionWrapper;
  }

  /**
   * <pre>
   * problem with tomcat and request wrappers, so stash this and
   * get back later:
   * http://brian.pontarelli.com/2008/01/16/httpservletrequestwrapper-tomcat-and-forwards/
   * </pre>
   * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
   */
  @Override
  public StringBuffer getRequestURL() {
    return this.requestURL;
  }

  /**
   * make sure the multipart mime map is initted (if so, dont do it again)
   */
  @SuppressWarnings("unchecked")
  private synchronized void initMultipartMap() {
    //if initted, dont worry about it
    if (this.multipartParamMap != null) {
      return;
    }
    FileItem[] fileItems = null;
    
  
    ServletFileUpload servletFileUpload = servletFileUpload();
    
    // maximum size before a FileUploadException will be thrown
    int maxSize = FastContext.fastContext().getParamIntSafe("fastMaxUploadFileSize", 15000000);
    
  
    List fileItemList = null;
    try {
      fileItemList = upload.parseRequest(this.transactionBean.getRequest());
      
      //this is here for debugging
      boolean testError = false;
      if (testError) {
        throw new FileUploadException("Stream ended unexpectedly");
      }
      
    } catch (FileUploadException fue) {
      
      //this is a problem with Safari where during keep alive it doesnt upload the file.
      //this should be fixed, but the error handling is here anyway
      String stack = ExceptionUtils.getFullStackTrace(fue);
      if (stack.contains("Stream ended unexpectedly") 
          && FastContext.fastContext().getParamBooleanSafe("fastSpecialErrorForFileUploadEndUnexpected", true)) {
        log.error("Error uploading files or params: Stream ended unexpectedly", fue);
        
        String appUrl = this.transactionBean.getFastSession().getStartPage();
        if (StringUtils.indexOf(appUrl, "fastButtonId") != -1) {
          appUrl = FastContext.fastContext().getAppUrl();
        }
        
        this.transactionBean.getFastController().showJspOrString(this.transactionBean, "fastUploadEndUnexpected", 
            "<html><body>Problem uploading file.  Try again, or try a different browser.  " +
            "E.g. if you are using the Safari browser, " +
            "perhaps try Firefox.<br/><br/><a href=\"" + appUrl + 
            "\">Try again</a><br />" + 
            FastContext.fastContext().serverNameFormatted() + "</body></html>",
          false, true);
      }
      
      throw new RuntimeException("Error uploading files or params: ", fue);
    }
  
    fileItems = (FileItem[])FastObjectUtils.toArray(fileItemList);
  
    this.multipartParamMap = new HashMap<String,Object>();
  
    //now, loop through array and process the items
    for (int i = 0; fileItems != null && i < fileItems.length; i++) {
  
      //this means not an upload file
      if (fileItems[i].isFormField()) {
  
        //see if there is something there already
        Object existing = this.multipartParamMap.get(fileItems[i].getFieldName());
        if (existing != null) {
  
          //see if we have a vector
          if (existing instanceof List) {
            ((List) existing).add(fileItems[i].getString());
  
            //else we need a new vector and add the existing item
          } else {
            List<String> itemList = new ArrayList<String>();
            itemList.add((String)existing);
            itemList.add(fileItems[i].getString());
            //put this vector in the pace of the existing value
            this.multipartParamMap.put(fileItems[i].getFieldName(), itemList);
          }
  
          //else we can just insert it since there is nothing there
        } else {
          this.multipartParamMap.put(fileItems[i].getFieldName(), fileItems[i]
              .getString());
        }
  
      } else {
  
        //this is a file, write (or copy) to the temp dir as unique id
        String fileName = fileItems[i].getName();
        File tempFile = null;
        
        //if filename, get it.  if not, then the file is null
        if (!StringUtils.isBlank(fileName)) {
          fileName = FastStringUtils.stringAfterLastSlash(fileName);
          
          //make sure valid name
          if (!FastContext.fastContext().getParamBooleanSafe("fastAllowIllegalFilenames")) {
            fileName = FastFileUtils.validFileName(fileName);
          }
          
          //could be blank
          if (StringUtils.isBlank(fileName)) {
            fileName = FastStringUtils.uniqueId();
          }
          //make a temp dir
          tempFile = FastFileUtils.tempFile("", "fastFileUpload");
          FastFileUtils.mkdirs(tempFile);
          //add the original filename
          tempFile = new File(tempFile + File.separator + fileName);
          
          try {
            fileItems[i].write(tempFile);
          } catch (Exception e) {
            String error = "Error uploading files or params: " + e.toString();
            throw new RuntimeException(error, e);
  
          }
        }
        //now store this file in the map
        this.multipartParamMap.put(fileItems[i].getFieldName(), tempFile);
      }
  
    }
  
  }

  /** keep 100k in memory, why not */
  private static FileItemFactory fileItemFactory = null;
  
  /**
   * 
   * @return the factory
   */
  private static synchronized ServletFileUpload servletFileUpload() {

    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    
    if (fileItemFactory == null) {
      
      // the location for saving data that is larger than getSizeThreshold()
      String tempDir = TagUtils.mediaResourceString(httpServletRequest, "file.upload.temp.dir");

      File tempDirFile = null;
      
      if (!StringUtils.isBlank(tempDir)) {
        
        tempDirFile = new File(tempDir);
        if (!tempDirFile.exists()) {
          tempDirFile.mkdirs();
        }
        LOG.warn("Created upload temp dir: " + GrouperUtil.fileCanonicalPath(tempDirFile));
        
      }
      
      fileItemFactory = new DiskFileItemFactory(100000, tempDirFile);
    }
    if (servletFileUpload == null) {
      servletFileUpload = new ServletFileUpload(fileItemFactory);
      String maxBytesString = 

      long maxBytes = 
          TagUtils.mediaResourceString(httpServletRequest, "file.upload.max.bytes"), 10000000);
      
      //10 megs
      servletFileUpload.setSizeMax(10000000);
      upload.setSizeMax(maxSize);
    }
    return servletFileUpload;
  }
  

  /** Create a new file upload handler */
  private static ServletFileUpload servletFileUpload = null;

  /**
   * find the request parameter names by prefix
   * @param prefix
   * @return the set, never null
   */
  @SuppressWarnings("unchecked")
  public Set<String> requestParameterNamesByPrefix(String prefix) {
    Set<String> result = new LinkedHashSet<String>();
    Enumeration<String> paramNames = this.getParameterNames();
    
    //cycle through all
    while(paramNames.hasMoreElements()) {
      String paramName = paramNames.nextElement();
      
      //see if starts with
      if (paramName.startsWith(prefix)) {
        result.add(paramName);
      }
    }
    
    
    return result;
  }

  
  
}
