/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  java.io.*;
import  java.util.*;

/**
 * {@link GrouperShell} Command Reader.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CommandReader.java,v 1.2 2006-08-22 19:48:22 blair Exp $
 * @since   0.0.1
 */
class CommandReader {

  // PRIVATE INSTANCE VARIABLES //
  private Interpreter     i       = null;
  private BufferedReader  in      = null;
  private String          prompt  = null;


  // CONSTRUCTORS //

  // @throws  GrouperShellException
  // @since   0.0.1
  protected CommandReader(String[] args) 
    throws  GrouperShellException
  {
    if (args.length > 0) {
      String file = args[0];
      if ("-".equals( file )) {
        this.in = new BufferedReader( new InputStreamReader(System.in) );
      }
      else {
        try {
          this.in = new BufferedReader( new FileReader(file) );
        }
        catch (FileNotFoundException eFNF) {
          throw new GrouperShellException(eFNF);
        }
      }
    }
    else {
      this.in     = new BufferedReader( new InputStreamReader(System.in) );
      this.prompt = GrouperShell.NAME + "-" + GrouperShell.VERSION + " ";
    }
    this.i = new Interpreter(this.in, System.out, System.err, false);
  } // protected CommandReader(args)


  // PROTECTED INSTANCE METHODS //

   // @throws  GrouperShellException
   // @since   0.0.1
  protected Interpreter getInterpreter() 
    throws  GrouperShellException
  {
    if (this.i == null) {
      throw new GrouperShellException(E.I_NULL);
    }
    return this.i;
  } // protected Interpreter getInterpreter()

   // @throws  GrouperShellException
   // @since   0.0.1
  protected String next() 
    throws  GrouperShellException
  {
    if (this.prompt != null) {
      this.i.print(this.prompt + this._getCnt() + "% ");
    }
    try {
      String cmd = this.in.readLine();
      return cmd;
    }
    catch (IOException eIO) {
      throw new GrouperShellException(eIO);
    }
  } // protected String next()


  // PRIVATE INSTANCE METHODS //

  // @since   0.0.1
  private int _getCnt() {
    int cnt = 0;
    try {
      List history = GrouperShell.getHistory(this.i);
      cnt = history.size();
    }
    catch (bsh.EvalError eBEE) {
      this.i.error(eBEE.getMessage());
    }
    return cnt;
  } // protected int _getCnt()

} // class CommandReader

