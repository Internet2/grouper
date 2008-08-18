/*
 * @author mchyzer $Id: Encrypt.java,v 1.1 2008-08-18 06:15:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util.rijndael;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.misc.GrouperStartup;

/**
 * from sh or bat file to encrypt a string
 */
public class Encrypt {

  /**
   * if an arg is dontMask, then it wont try the fancy masking stuff
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("Wait a minute during initialization...");
    GrouperStartup.startup();
    String passwordString = null;
    String prompt = "Enter the string to encrypt: ";

    //allow this in case the other way messes up for some reason
    if (args.length == 1 && StringUtils.equals("dontMask", args[0]) ) {
      System.out.print(prompt);
      //  open up standard input 
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 

      //  read the username from the command-line; need to use try/catch with the 
      //  readLine() method 
      try { 
         passwordString = br.readLine(); 
      } catch (IOException ioe) { 
         System.out.println("IO error trying to read your name! " + ExceptionUtils.getFullStackTrace(ioe));
         System.exit(1); 
      } 

    } else {
      char password[] = null;
      try {
        password = password(System.in, prompt);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      passwordString = String.valueOf(password);
    }
    if (StringUtils.isBlank(passwordString)) {
      System.out.println("No input entered");
    } else {
      System.out.println("The encrypted string is: " + Morph.encrypt(passwordString));
    }

  }

  /**
   * @param in stream to be used (e.g. System.in)
   *@param prompt The prompt to display to the user.
   *@return The password as entered by the user.
   * @throws IOException 
   */

  public static final char[] password(InputStream in, String prompt) throws IOException {
    MaskingThread maskingthread = new MaskingThread(prompt);
    Thread thread = new Thread(maskingthread);
    thread.start();

    char[] lineBuffer;
    char[] buf;

    buf = lineBuffer = new char[128];

    int room = buf.length;
    int offset = 0;
    int c;

    loop: while (true) {
      switch (c = in.read()) {
        case -1:
        case '\n':
          break loop;

        case '\r':
          int c2 = in.read();
          if ((c2 != '\n') && (c2 != -1)) {
            if (!(in instanceof PushbackInputStream)) {
              in = new PushbackInputStream(in);
            }
            ((PushbackInputStream) in).unread(c2);
          } else {
            break loop;
          }

        default:
          if (--room < 0) {
            buf = new char[offset + 128];
            room = buf.length - offset - 1;
            System.arraycopy(lineBuffer, 0, buf, 0, offset);
            Arrays.fill(lineBuffer, ' ');
            lineBuffer = buf;
          }
          buf[offset++] = (char) c;
          break;
      }
    }
    maskingthread.stopMasking();
    if (offset == 0) {
      return null;
    }
    char[] ret = new char[offset];
    System.arraycopy(buf, 0, ret, 0, offset);
    Arrays.fill(buf, ' ');
    return ret;
  }

  /**
   * thread to mask input
   */
  static class MaskingThread extends Thread {

    /** stop */
    private volatile boolean stop;

    /** echo char */
    private char echochar = '*';

    /**
     *@param prompt The prompt displayed to the user
     */
    public MaskingThread(String prompt) {
      System.out.print(prompt);
    }

    /**
     * Begin masking until asked to stop.
     */
    public void run() {

      int priority = Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

      try {
        stop = true;
        while (stop) {
          System.out.print("\010" + echochar);
          try {
            // attempt masking at this rate
            Thread.sleep(1);
          } catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      } finally { // restore the original priority
        Thread.currentThread().setPriority(priority);
      }
    }

    /**
     * Instruct the thread to stop masking.
     */
    public void stopMasking() {
      this.stop = false;
    }
  }
}
