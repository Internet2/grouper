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
/*
 * @author mchyzer
 * $Id: GrouperRequestWrapper.java,v 1.5 2009-10-16 12:16:32 isgwb Exp $
 */
package edu.internet2.middleware.grouper.j2ee;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * wrap request so we can customize
 */
public class GrouperRequestWrapper extends HttpServletRequestWrapper {

  /** keep a reference to this */
  HttpServletRequest wrapped = null;
  
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
    return (FileItem)this.parameterMap.get(name);
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
  private java.util.Map<String,Object> parameterMap;

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperUiRestServlet.class);

  /**
   * @param request
   */
  public GrouperRequestWrapper(HttpServletRequest request) {
    super(request);
    this.wrapped = request;
  }

  /**
   * init
   */
  public void init() {
    this.requestURL = super.getRequestURL();
    this.multipart = ServletFileUpload.isMultipartContent(this);

    SessionContainer sessionContainer = null;
    try {
      sessionContainer = SessionContainer.retrieveFromSession();
      if (!sessionContainer.isInitted()) {
        SessionInitialiser.init(this);
      }
    } finally {
      if (sessionContainer != null) {
        sessionContainer.setInitted(true);
      }
    }    
    if (this.multipart) {
      this.initMultipartMap();
    }
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
    
    if (session != null) {
    if (this.grouperSessionWrapper == null 
        || this.grouperSessionWrapper.getHttpSession() != session) {
      this.grouperSessionWrapper = new GrouperSessionWrapper(session);
      }
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
    if (this.parameterMap != null) {
      return;
    }
  
    ServletFileUpload servletFileUpload = servletFileUpload();
    
    List<FileItem> fileItemList = null;
    try {
      fileItemList = servletFileUpload.parseRequest(this);
      
      //this is here for debugging
      boolean testError = false;
      if (testError) {
        throw new FileUploadException("Stream ended unexpectedly");
      }
      
    } catch (FileUploadException fue) {
      
      throw new RuntimeException("Error uploading files or params: " + fue.getMessage(), fue);
    }
  
  
    this.parameterMap = new HashMap<String,Object>();
  
    //now, loop through array and process the items
    for (FileItem fileItem : fileItemList) {
            
      //this means not an upload file
      if (fileItem.isFormField()) {
  
        //see if there is something there already
        Object existing = this.parameterMap.get(fileItem.getFieldName());
        if (existing != null) {
  
          //see if we have a vector
          if (existing instanceof List) {
            ((List) existing).add(fileItem.getString());
  
            //else we need a new vector and add the existing item
          } else {
            throw new RuntimeException("Why is this not a list??? " + existing.getClass());
          }
  
          //else we can just insert it since there is nothing there
        } else {
          List<String> itemList = new ArrayList<String>();
          itemList.add(fileItem.getString());
          //put this vector in the pace of the existing value
          this.parameterMap.put(fileItem.getFieldName(), itemList);
        }
  
      } else {
        //just store as file item 
        this.parameterMap.put(fileItem.getFieldName(), fileItem);
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

    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    
    if (fileItemFactory == null) {
      
      // the location for saving data that is larger than getSizeThreshold()
      String tempDir = TagUtils.mediaResourceString("file.upload.temp.dir");

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
    if (staticServletFileUpload == null) {
      staticServletFileUpload = new ServletFileUpload(fileItemFactory);

      int maxBytes = 
          TagUtils.mediaResourceInt("file.upload.max.bytes", 10000000);
      
      //10 megs
      staticServletFileUpload.setSizeMax(maxBytes);
    }
    return staticServletFileUpload;
  }
  

  /** Create a new file upload handler */
  private static ServletFileUpload staticServletFileUpload = null;

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

  /**
   * Get the parameter names from the fast request
   * 
   * @return get parameter names passed in
   */
  @Override
  public Enumeration getParameterNames() {
  
    if (!this.multipart) {
      return super.getParameterNames();
    }
    return new SetToEnumeration(this.parameterMap.keySet());
  }

  /**
   * Return an array of strings for an input parameter
   * 
   * @param name
   * @return parameter values based on name
   */
  @Override
  @SuppressWarnings("unchecked")
  public String[] getParameterValues(String name) {
    if (!this.multipart) {
      return super.getParameterValues(name);
    }
    Object objectSubmitted = this.parameterMap.get(name);
    //if not found, then return null
    if (objectSubmitted == null) {
      return null;
    }
    //if a vector, return the array
    if (objectSubmitted instanceof List) {
      List<String> objectSubmittedList = (List) objectSubmitted;
      return (String[]) GrouperUtil.toArray(objectSubmittedList);
    }
  
    //now see if we are dealing with a file
    if (objectSubmitted instanceof File) {
      objectSubmitted = ((File) objectSubmitted).getPath();
    }
  
    //not expecting
    //throw new RuntimeException("Not expecting type: " + name + ", " + objectSubmitted.getClass());
    return super.getParameterValues(name);
  }

  /**
   * param boolean for EL
   * @return the boolean value of param
   */
  @SuppressWarnings("unchecked")
  public Map getParameter() {
    
    if (this.parameterMap == null) {
      this.parameterMap = new MapWrapper() {
        /**
         * @see MapWrapper#get(java.lang.Object)
         */
        @Override
        public Object get(Object key) {
          return GrouperRequestWrapper.this.getParameter((String)key);
        }
      };
    }
    return this.parameterMap;
  }

  /**
   * Use this instead of request.getParameter as it will handle file uploads.
   * 
   * If the parameter is in fast a file, this method will return the filepath.
   * However, please do not call this method for files, please use
   * getParameterFile(name).
   * 
   * @param name
   * @return get a certain param
   */
  @Override
  public String getParameter(String name) {
  
    if (!this.multipart) {
      String param = this.wrapped.getParameter(name);
      if (param != null && StringUtils.equals("GET", this.getMethod()) && TagUtils.mediaResourceBoolean("convertInputToUtf8", true)) {
        try {
          byte[] bytes = param.getBytes("ISO-8859-1");
          param = new String(bytes, "UTF-8");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      return param;
    }
  
    Object objectSubmitted = this.parameterMap.get(name);
    //if not found, then return null
    if (objectSubmitted == null) {
      return null;
    }
    //if a vector, use the first one
    if (objectSubmitted instanceof List) {
      if (((List)objectSubmitted).size() > 1) {
        throw new RuntimeException("This is a multi-list, should be single: " + name + ", " + ((List)objectSubmitted).size());
      }
      return (String)((List) objectSubmitted).get(0);
    }
    if (objectSubmitted instanceof FileItem) {
    	return null; 
    }
    throw new RuntimeException("Not expecting type: " + (objectSubmitted == null ? null : objectSubmitted.getClass()));
    
  }

  /**
   * Get a boolean from the input.  must be true or false or not existent.
   * For expression language, just use getParameter as string
   * 
   * @param name
   * @return TRUE or FALSE or null
   */
  public Boolean getParameterBoolean(String name) {
    String param = getParameter(name);
    //this handles null
    return GrouperUtil.booleanObjectValue(param);
  }

  /**
   * Get a boolean from the input.  mus tbe true or false or not existent.
   * 
   * @param name
   * @param theDefault is what to return if param not there (usually false)
   * @return TRUE or FALSE or null
   */
  public boolean getParameterBoolean(String name, boolean theDefault) {
    Boolean param = getParameterBoolean(name);
    if (param == null) {
      return theDefault;
    }
    return param.booleanValue();
  }
  
  
  
}
