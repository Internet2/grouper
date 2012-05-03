package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;

/**
 * copy bytes to a buffer
 */
public class ServletReaderCopier extends Reader {

  /**
   * reader
   */
  private Reader reader;

  /** keep a copy
   * 
   */
  private ByteArrayOutputStream copy;

  /**
   * bridge from chars to bytes
   */
  private OutputStreamWriter outputStreamWriter;
  
  /**
   * constructor
   * @param reader1
   */
  public ServletReaderCopier(Reader reader1) {
    this.reader = reader1;
    this.copy = new ByteArrayOutputStream(1024);
    this.outputStreamWriter = new OutputStreamWriter(this.copy);
  }

  /**
   * get the copy
   * @return bytes
   */
  public byte[] getCopy() {
    try {
      this.outputStreamWriter.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return this.copy.toByteArray();
  }

  /**
   * @see java.io.Reader#close()
   */
  @Override
  public void close() throws IOException {
    this.reader.close();
  }

  /**
   * @see java.io.Reader#read(char[], int, int)
   */
  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    int length = this.reader.read(cbuf, off, len);
    if (length > 0) {
      this.outputStreamWriter.write(cbuf, off, length);
    }
    return length;
  }

}
