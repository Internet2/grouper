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
 * @version $Id: GrouperShell.java,v 1.3 2006-05-30 19:40:51 blair Exp $
 */
public class GrouperShell {

  // PRIVATE INSTANCE VARIABLES //
  private int               cnt = 0;
  private PrintStream       err;
  private Interpreter       i;
  private BufferedReader    in;
  private PrintStream       out;


  // MAIN //

  /**
   * @since 1.0
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
    this.in   = new BufferedReader( new InputStreamReader(System.in) );
    this.out  = System.out;
    this.err  = System.err;
    this.i = new Interpreter(this.in, this.out, this.err, true);
    this._importCommands();
  } // private GrouperShell()


  // PRIVATE INSTANCE METHODS //
  //  @since  1.0
  private void _importCommand(String name) 
    throws  bsh.EvalError
  {
    String cmd = "importCommands(\"com.devclue.grouper." + name + "\")";
    this.i.eval(cmd);
  } // private void _importCommand(name)

  //  @since  1.0
  private void _importCommands() 
    throws  bsh.EvalError
  {
    this._importCommand(  "group"   );
    this._importCommand(  "member"  );
    this._importCommand(  "shell"   );
    this._importCommand(  "stem"    );
    this._importCommand(  "subject" );
  } // private void _importCommands()

  //  @since  1.0
  private String _prompt() 
    throws  bsh.EvalError,
            IOException
  {
    Object prompt = this.i.eval("getBshPrompt()");
    if (prompt == null) {
      prompt = "%";
    }
    this.out.print(this.cnt++ + prompt.toString());
    return this.in.readLine();
  } // private String _prompt()

  //  @since  1.0
  private void _run() 
    throws  bsh.EvalError,
            IOException
  {
    String cmd = new String();
    while ( (cmd = this._prompt()) != null ) {
      if ( cmd.equals("exit") || cmd.equals("quit") ) {
        break;
      }
      try {
        Object obj = this.i.eval(cmd);
        if (obj != null) {
          this.out.println(obj.toString());
        }
      }
      catch (bsh.EvalError eBEE) {
        this.err.println("ERROR: " + eBEE.getMessage());
      }
    }
  } // private void _run()

}

