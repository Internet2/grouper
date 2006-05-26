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
 * @version $Id: GrouperShell.java,v 1.2 2006-05-26 17:53:21 blair Exp $
 */
public class GrouperShell {

  // PRIVATE INSTANCE VARIABLES //
  Interpreter i;


  // MAIN //

  /**
   * @since 0.3
   */
  public static void main(String args[]) {
    try {
      GrouperShell shell = new GrouperShell();
      shell._run();
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args)


  // CONSTRUCTORS //
  private GrouperShell() 
    throws  bsh.EvalError
  {
    this.i = new Interpreter();
    this._importCommands();
  } // private GrouperShell()


  // PRIVATE INSTANCE METHODS //
  //  @since  0.3
  private void _importCommand(String name) 
    throws  bsh.EvalError
  {
    String cmd = "importCommands(\"com.devclue.grouper." + name + "\")";
    this.i.eval(cmd);
  } // private void _importCommand(name)

  //  @since  0.3
  private void _importCommands() 
    throws  bsh.EvalError
  {
    this._importCommand(  "group"   );
    this._importCommand(  "member"  );
    this._importCommand(  "stem"    );
    this._importCommand(  "subject" );
  } // private void _importCommands()

  //  @since  0.3
  private void _run() 
    throws  IOException
  {
    String          cmd = new String();
    BufferedReader  in  = new BufferedReader(new InputStreamReader(System.in));
    while ( (cmd = in.readLine() ) != null ) {
      System.err.println("GOT: <<" + cmd + ">>");
      if ( cmd.equals("exit") || cmd.equals("quit") ) {
        break;
      }
      try {
        Object obj = i.eval(cmd);
        System.err.println("OBJ: <<" + obj + ">>");
      }
      catch (bsh.EvalError eBEE) {
        System.err.println("ERROR: " + eBEE.getMessage());
      }
    }
  } // private void _run()

}

