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
 * @author mchyzer $Id: TcpCaptureServer.java,v 1.3 2008-10-21 18:12:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.net.telnet.TelnetClient;

/**
 * A TCP capture server listening on the port, forwards requests to another
 * port, and relays info back, while keeping track of the request and response.
 */
public class TcpCaptureServer {

  /** if print debug info */
  private boolean debug = false;

  /** request string */
  private StringBuilder request = new StringBuilder();

  /** response string */
  private StringBuilder response = new StringBuilder();

  /** if found error, here it is */
  private String error = null;

  /**
   * start the server
   * @param portListen
   * @param portConnect
   * @param executeInThread true to spawn a thread
   * @return thread if started one
   */
  public Thread startServer(final int portListen, final int portConnect,
      boolean executeInThread) {
    if (executeInThread) {
      if (TcpCaptureServer.this.debug) {

        TelnetClient telnetClient = new TelnetClient();
        try {
          telnetClient.connect("localhost", portListen);
          telnetClient.disconnect();
          System.err.println("Connected successfully to port: " + portListen);
        } catch (Exception e) {
          System.err.println("Couldn't connect successfully to port: " + portListen +", " + e.getMessage());
        }
        
        System.err.println("Socket thread called from: " + ExceptionUtils.getFullStackTrace(new RuntimeException()));
      }
      
      Thread thread = new Thread(new Runnable() {

        public void run() {
          TcpCaptureServer.this.startServer(portListen, portConnect);
        }
      });
      thread.start();

      return thread;
    } 

    this.startServer(portListen, portConnect);
    
    return null;
  }

  /**
   * start the server
   * @param portListen
   * @param portConnect
   */
  public void startServer(int portListen, int portConnect) {
    ServerSocket sock = null;

    try {
      // establish the socket
      sock = new ServerSocket(portListen);

      /**
       * listen for new connection requests.
       * when a request arrives, service it
       * and resume listening for more requests.
       */
      int id = 0;

      if (TcpCaptureServer.this.debug) {
        System.err.println("Starting server on port: " + portListen);
      }
      
      //for now just do this once
      //      while (true) {
      // now listen for connections
      sock.setSoTimeout(30000);
      Socket client = sock.accept();

      if (TcpCaptureServer.this.debug) {
        System.err.println("Servicing connection on port: " + portListen);
      }
      
      // service the connection 
      ServiceConnection(client, id++, portConnect);

      //      }
    } catch (IOException ioe) {

      System.err.println("Problem with port: " + portListen + ", " + ioe);
      this.error = ExceptionUtils.getFullStackTrace(ioe);
    
    } finally {
      if (sock != null) {
        try {
          
          if (TcpCaptureServer.this.debug) {
            System.err.println("Closing port: " + portListen);
          }
          sock.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   * service a socket connection to server
   * @param client
   * @param socketId
   * @param portToConnect
   * @throws IOException
   */
  public void ServiceConnection(final Socket client, final int socketId, int portToConnect)
      throws IOException {
    Reader clientToProxy = null;
    Writer proxyToClient = null;
    Reader serverToProxy = null;
    Writer proxyToServer = null;

    //    BufferedReader fromUser = null;
    Socket proxyToServerSocket = null;

    try {
      proxyToServerSocket = new Socket("localhost", portToConnect);
      //Thread.sleep(1000);
      // set up the necessary communication channels
      serverToProxy = new BufferedReader(new InputStreamReader(proxyToServerSocket
          .getInputStream()));
      //fromUser = new BufferedReader(new InputStreamReader(System.in));
      proxyToServer = new PrintWriter(proxyToServerSocket.getOutputStream(), true);

      /**
       * get the input and output streams associated with the socket.
       */
      clientToProxy = new BufferedReader(new InputStreamReader(client.getInputStream()));
      proxyToClient = new OutputStreamWriter(client.getOutputStream());

      final Reader CLIENT_TO_PROXY = clientToProxy;
      final Writer PROXY_TO_SERVER = proxyToServer;
      final Reader SERVER_TO_PROXY = serverToProxy;
      final Writer PROXY_TO_CLIENT = proxyToClient;

      Thread clientToServer = new Thread(new Runnable() {

        public void run() {
          try {
            //give it some time
            Thread.sleep(1000);

            if (TcpCaptureServer.this.debug) {
              System.out.println(new Date() + "" + socketId
                  + ": Starting client to server loop: ");
            }

            char[] buf = new char[1024];

            while (true) {
              String line = null;

              if (client.isClosed() || !client.isConnected() || !CLIENT_TO_PROXY.ready()) {
                break;
              }

              int chars = CLIENT_TO_PROXY.read(buf);

              if ((chars == -1) && !CLIENT_TO_PROXY.ready()) {
                break;
              }

              line = new String(buf, 0, chars);
              TcpCaptureServer.this.request.append(line);

              if (TcpCaptureServer.this.debug) {
                System.out.println(new Date() + "" + socketId + ": From client: " + line);
              }

              //send to server
              PROXY_TO_SERVER.write(line);
              PROXY_TO_SERVER.flush();
            }
          } catch (Exception e) {
            TcpCaptureServer.this.error = ExceptionUtils.getFullStackTrace(e);
            e.printStackTrace();
          }

          if (TcpCaptureServer.this.debug) {
            System.out.println(new Date() + "" + socketId + ": clientToServer done");
          }
        }
      });

      clientToServer.start();

      Thread serverToClient = new Thread(new Runnable() {

        public void run() {
          try {
            //give it a second
            Thread.sleep(1000);

            if (TcpCaptureServer.this.debug) {
              System.out.println(new Date() + "" + socketId
                  + ": Starting server to client loop: ");
            }

            char[] buf = new char[1024];

            while (true) {
              String line = null;
              int chars = SERVER_TO_PROXY.read(buf);

              if ((chars == -1) && !SERVER_TO_PROXY.ready()) {
                //Thread.sleep(100);
                //continue;
                break;
              }

              line = new String(buf, 0, chars);
              TcpCaptureServer.this.response.append(line);

              if (TcpCaptureServer.this.debug) {
                System.out.println(new Date() + "" + socketId + ": From server: " + line);
              }

              //send to server
              PROXY_TO_CLIENT.write(line);
              PROXY_TO_CLIENT.flush();
            }
          } catch (Exception e) {
            TcpCaptureServer.this.error = ExceptionUtils.getFullStackTrace(e);
            e.printStackTrace();
          }

          if (TcpCaptureServer.this.debug) {
            System.out.println(new Date() + "" + socketId + ": serverToClient done");
          }
        }
      });

      serverToClient.start();

      serverToClient.join();
      clientToServer.join();
      proxyToClient.flush();
    } catch (Exception e) {
      this.error = ExceptionUtils.getFullStackTrace(e);
      e.printStackTrace();
    } finally {
      if (serverToProxy != null) {
        serverToProxy.close();
      }

      //      if (fromUser != null)
      //        fromUser.close();
      if (proxyToServer != null) {
        proxyToServer.close();
      }

      if (proxyToServerSocket != null) {
        proxyToServerSocket.close();
      }

      try {
        if (clientToProxy != null) {
          clientToProxy.close();
        }

        if (proxyToClient != null) {
          proxyToClient.close();
        }

        if (client != null) {
          client.close();
        }
      } catch (IOException ioee) {
        System.err.println(ioee);
        ioee.printStackTrace();
      }
    } // end try
  } // end ServiceConnection

  /**
   * get the response string
   * @return repsonse string
   */
  public String getRequest() {
    if (this.error != null) {
      throw new RuntimeException("Found error: " + this.error
          + ", current progress was: " + this.request);
    }
    return this.request.toString();
  }

  /**
   * get the repsonse string
   * @return the response string
   */
  public String getResponse() {
    if (this.error != null) {
      throw new RuntimeException("Found error: " + this.error
          + ", current progress was: " + this.response);
    }
    return this.response.toString();
  }
}
