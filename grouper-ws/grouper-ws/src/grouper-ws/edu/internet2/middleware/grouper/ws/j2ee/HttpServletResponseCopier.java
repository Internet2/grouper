package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * response wrapper to capture stuff for logging
 */
public class HttpServletResponseCopier extends HttpServletResponseWrapper {

  /**
   * capture headers
   */
  private StringBuilder headers = new StringBuilder("(note, not all headers captured, and not in this order)\n");
  
  /**
   * headers
   * @return headers
   */
  public String getHeaders() {
    return this.headers.toString();
  }
  
  /**
   * @see javax.servlet.http.HttpServletResponseWrapper#addIntHeader(java.lang.String, int)
   */
  @Override
  public void addIntHeader(String name, int value) {
    this.headers.append(name).append(": ").append(value).append("\n");
    super.addIntHeader(name, value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
   */
  @Override
  public void setHeader(String name, String value) {
    this.headers.append(name).append(": ").append(value).append("\n");
    super.setHeader(name, value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponseWrapper#setIntHeader(java.lang.String, int)
   */
  @Override
  public void setIntHeader(String name, int value) {
    this.headers.append(name).append(": ").append(value).append("\n");
    super.setIntHeader(name, value);
  }

  /**
   * @see javax.servlet.ServletResponseWrapper#setContentType(java.lang.String)
   */
  @Override
  public void setContentType(String type) {
    this.headers.append("Content-Type").append(": ").append(type).append("\n");
    super.setContentType(type);
  }

  /**
   * output stream
   */
  private ServletOutputStream outputStream;

  /**
   * @see javax.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
   */
  @Override
  public void addHeader(String name, String value) {
    this.headers.append(name).append(": ").append(value).append("\n");
    super.addHeader(name, value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int, java.lang.String)
   */
  @Override
  public void setStatus(int sc, String sm) {
    this.headers.append("HTTP/1.1 ").append(sc).append(" ").append(sm).append("\n");
    super.setStatus(sc, sm);
  }

  /**
   * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
   */
  @Override
  public void setStatus(int sc) {
    this.headers.append("HTTP/1.1 ").append(sc).append("\n");
    super.setStatus(sc);
  }

  /**
   * writer
   */
  private PrintWriter writer;

  /**
   * copy outputstream
   */
  private ServletOutputStreamCopier copier;

  /**
   * construct
   * @param response
   * @throws IOException
   */
  public HttpServletResponseCopier(HttpServletResponse response) throws IOException {
    super(response);
  }

  /**
   * 
   * @see javax.servlet.ServletResponseWrapper#getOutputStream()
   */
  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (this.writer != null) {
      throw new IllegalStateException(
          "getWriter() has already been called on this response.");
    }

    if (this.outputStream == null) {
      this.outputStream = getResponse().getOutputStream();
      this.copier = new ServletOutputStreamCopier(this.outputStream);
    }

    return this.copier;
  }

  /**
   * 
   * @see javax.servlet.ServletResponseWrapper#getWriter()
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    if (this.outputStream != null) {
      throw new IllegalStateException(
          "getOutputStream() has already been called on this response.");
    }

    if (this.writer == null) {
      this.copier = new ServletOutputStreamCopier(getResponse().getOutputStream());
      this.writer = new PrintWriter(new OutputStreamWriter(this.copier, getResponse()
          .getCharacterEncoding()), true);
    }

    return this.writer;
  }

  /**
   * @Override
   * @see javax.servlet.ServletResponseWrapper#flushBuffer()
   */
  @Override
  public void flushBuffer() throws IOException {
    if (this.writer != null) {
      this.writer.flush();
    } else if (this.outputStream != null) {
      this.copier.flush();
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
    return new byte[0];
  }

}
