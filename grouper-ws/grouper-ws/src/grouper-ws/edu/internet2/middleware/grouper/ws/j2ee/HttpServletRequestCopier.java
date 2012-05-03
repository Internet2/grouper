/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 *
 */
public class HttpServletRequestCopier extends HttpServletRequestWrapper {

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
