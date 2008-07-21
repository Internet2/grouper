/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.Interpreter;
import  java.io.BufferedReader;
import  java.io.FileNotFoundException;
import  java.io.FileReader;
import  java.io.InputStreamReader;
import  java.io.IOException;
import  java.util.List;

/**
 * {@link GrouperShell} Shell Command Reader.
 * <p/>
 * @author  blair christensen.
 * @since   0.1.1
 */
class ShellCommandReader implements CommandReader {

  // PRIVATE INSTANCE VARIABLES //
  private Interpreter     i       = null;
  private BufferedReader  in      = null;
  private String          prompt  = null;


  // CONSTRUCTORS //

  // @since   0.0.1
  protected ShellCommandReader(String[] args) 
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
      this.prompt = GrouperShell.NAME + "-" + GrouperShellVersion.VERSION + " ";
    }
    this.i = new Interpreter(this.in, System.out, System.err, false);
  } // protected ShellCommandReader(args)


  // PUBLIC INSTANCE  METHODS //

  /**
   * @since   0.1.1
   */
  public Interpreter getInterpreter() 
    throws  GrouperShellException
  {
    if (this.i == null) {
      throw new GrouperShellException(GshErrorMessages.I_NULL);
    }
    return this.i;
  } // public Interpreter getInterpreter()

  /**
   * @since   0.1.1
   */
  public String getNext() 
    throws  GrouperShellException
  {
    if (this.prompt != null) {
      this.i.print( this.prompt + this._getCnt() + "% " );
    }
    try {
      return this.in.readLine();
    }
    catch (IOException eIO) {
      throw new GrouperShellException(eIO);
    }
  } // public String getNext()


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

} // class ShellCommandReader implements CommandReader

