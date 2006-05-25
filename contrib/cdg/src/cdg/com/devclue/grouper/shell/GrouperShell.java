/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;

import  bsh.Interpreter;
import  java.io.*;
import  java.util.*;

/**
 * Grouper Management Shell.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperShell.java,v 1.1 2006-05-25 15:20:19 blair Exp $
 */
public class GrouperShell {

  // Main //
  public static void main(String args[]) {
    try {
      Interpreter     i   = new Interpreter();
      i.eval("importCommands(\"com.devclue.grouper.stem\")");
      String          cmd = new String();
      BufferedReader  in  = new BufferedReader(new InputStreamReader(System.in));
      while ( (cmd = in.readLine() ) != null ) {
        System.err.println("GOT: <<" + cmd + ">>");
        Object obj = i.eval(cmd);
        System.err.println("OBJ: <<" + obj + ">>");
      }
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
    }
    System.exit(0);
  } // public static void main(args)

}

