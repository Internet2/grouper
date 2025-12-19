/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;


/**
 *
 */
public class HttpServletRequestCopier extends HttpServletRequestWrapper {

  /**
   * capture headers
   */
  private StringBuilder headers = new StringBuilder();
  
  private boolean headersInitialized = false;
  
  /**
   * headers
   * @return headers
   */
  public synchronized String getHeaders() {
    
    if (!this.headersInitialized) {
      this.headersInitialized = true;
      //get all headers
      Enumeration<String> headerNames = this.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        
        Enumeration<String> headerValues = this.getHeaders(headerName);
        while (headerValues.hasMoreElements()) {
          String originalHeaderValue = headerValues.nextElement();
          
          String headerValue = sanitizeHeader(headerName, originalHeaderValue);
          headerName = sanitizeHeaderName(headerName, originalHeaderValue);
          
          this.headers.append(headerName).append(": ").append(headerValue).append("\n");
        }
      }
    }
    
    return this.headers.toString();
  }

  /**
   * sanitize header value for logging
   * 
   * @param headerName
   * @param headerValue
   * @return
   */
  private static String sanitizeHeader(String headerName, String headerValue) {
    
//    // normalize the new lines
//    headerValue = StringUtils.replaceEach(
//        headerValue,
//        new String[] {"\r", "\n"},
//        new String[] {"\\r", "\\n"}
//    );
    
    
    // if basic auth, log username only
    if (Strings.CI.equalsAny(headerName, "authorization")) {
      
      // see if basic auth and log the username only
      if (Strings.CI.startsWith(headerValue, "basic ")) {
        String base64Part = StringUtils.substringAfter(headerValue, " ");
        try {
          String decoded = new String(java.util.Base64.getDecoder().decode(base64Part), StandardCharsets.UTF_8);
          if (!decoded.contains(":")) {
            return "*****";
          } 
          String username = StringUtils.substringBefore(decoded, ":");
          return "Basic " + username + ":*****";
        } catch (Exception e) {
          // problem decoding
          return "Problem parsing basic auth Base64!!! *****";
        }
      }
      //skip these values
      return "*****";
      
    // add more if there are other headers to hide
    } else if (Strings.CI.equalsAny(headerName, "cookie")) {
      //skip these values
      return "*****";
    }
    return headerValue;
  }
  
  /**
   * sanitize header name for logging
   * 
   * @param headerName
   * @param headerValue
   * @return
   */
  private static String sanitizeHeaderName(String headerName, String headerValue) {
    
//    // normalize the new lines
//    headerValue = StringUtils.replaceEach(
//        headerValue,
//        new String[] {"\r", "\n"},
//        new String[] {"\\r", "\\n"}
//    );
    
    
    // if basic auth, log username only
    if (Strings.CI.equalsAny(headerName, "authorization")) {
      
      // see if basic auth and log the username only
      if (Strings.CI.startsWith(headerValue, "basic ")) {
        String base64Part = StringUtils.substringAfter(headerValue, " ");
        try {
          String decoded = new String(java.util.Base64.getDecoder().decode(base64Part), StandardCharsets.UTF_8);
          if (!decoded.contains(":")) {
            return headerName;
          } 
          return "authorization (base64 decoded username)";
        } catch (Exception e) {
          // problem decoding
          return headerName;
        }
      }
    }
    return headerName;
  }

  /**
   * output stream
   */
  private ServletInputStream inputStream;

  /**
   * reader
   */
  private BufferedReader reader;

  /**
   * copy inputstream
   */
  private ServletInputStreamCopier copier;

  /**
   * copy inputstream
   */
  private ServletReaderCopier readerCopier;


  /**
   * @param request
   */
  public HttpServletRequestCopier(HttpServletRequest request) {
    super(request);
    
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getReader()
   */
  @Override
  public BufferedReader getReader() throws IOException {
    if (this.inputStream != null) {
      throw new IllegalStateException(
          "getInputStream() has already been called on this response.");
    }

    if (this.reader == null) {
      this.readerCopier = new ServletReaderCopier(getRequest().getReader());
      this.reader = new BufferedReader(this.readerCopier);
    }

    return this.reader;
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getInputStream()
   */
  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (this.reader != null) {
      throw new IllegalStateException(
          "getReader() has already been called on this response.");
    }
    if (this.inputStream == null) {
      this.inputStream = getRequest().getInputStream();
      this.copier = new ServletInputStreamCopier(this.inputStream);
    }
    return this.copier;
  }

  /**
   * finish reading request if someone else didnt do it already
   */
  public void finishReading() {
    try {
      if (this.inputStream == null) {
        Reader theReader = this.getReader();
        while (theReader.read() != -1) {
          //nothing
        }
      } else {
        InputStream theInputStream = this.getInputStream();
        while (theInputStream.read() != -1) {
          //nothing
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
  /**
   * get copy
   * @return copy
   */
  public byte[] getCopy() {
    if (this.copier != null) {
      return this.copier.getCopy();
    } 
    if (this.readerCopier != null) {
      return this.readerCopier.getCopy();
    } 
    return new byte[0];
  }

}
