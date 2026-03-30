package com.pty4j;

final class Ascii {

  /**
   * End of Text: A communication control character used to terminate a sequence of characters
   * started with STX and transmitted as an entity.
   */
  static final byte ETX = 3;

  static final char ETX_CHAR = (char) ETX;

  /**
   * Bell ('\a'): A character for use when there is a need to call for human attention. It may
   * control alarm or attention devices.
   */
  static final byte BEL = 7;

  static final char BEL_CHAR = (char) BEL;

  /**
   * Backspace ('\b'): A format effector which controls the movement of the printing position one
   * printing space backward on the same printing line. (Applicable also to display devices.)
   */
  static final byte BS = 8;

  private Ascii() {
  }
}
