/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.Interpreter;
import  java.util.LinkedList;

/**
 * {@link GrouperShell} Command Reader for simple evaluations.
 * <p/>
 * @author  blair christensen.
 * @since   0.1.1
 */
class SimpleCommandReader implements CommandReader {

  // PRIVATE INSTANCE VARIABLES //
  private Interpreter i     = null;
  private LinkedList  queue = new LinkedList();


  // CONSTRUCTORS //

  // @since   0.0.1
  protected SimpleCommandReader()
    throws  GrouperShellException
  {
    this.i = new Interpreter();
  } // protected SimpleCommandReader(args)


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
    if ( this.queue.size() > 0 ) {
      return (String) this.queue.removeFirst();
    }
    return null;
  } // public String getNext()


  // PROTECTED INSTANCE METHODS //

  // @since   0.1.1
  protected void add(String cmd) {
    this.queue.add(cmd);
  } // protected void add(cmd)

} // class SimpleCommandReader implements CommandReader

