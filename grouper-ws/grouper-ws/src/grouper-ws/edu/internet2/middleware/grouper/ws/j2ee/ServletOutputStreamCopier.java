package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * copy bytes to a buffer
 */
public class ServletOutputStreamCopier extends ServletOutputStream {

  /**
   * output stream
   */
  private OutputStream outputStream;

  /** keep a copy
   * 
   */
  private ByteArrayOutputStream copy;

  /**
   * constructor
   * @param outputStream1
   */
  public ServletOutputStreamCopier(OutputStream outputStream1) {
    this.outputStream = outputStream1;
    this.copy = new ByteArrayOutputStream(1024);
  }

  /**
   * 
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    this.outputStream.write(b);
    this.copy.write(b);
  }

  /**
   * get the copy
   * @return bytes
   */
  public byte[] getCopy() {
    return this.copy.toByteArray();
  }

}
