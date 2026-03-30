package com.pty4j.windows;

/**
 * @deprecated Use com.pty4j.windows.winpty.WinPtyException instead
 */
@Deprecated
public class WinPtyException extends Exception {
  private WinPtyException(String message) {
    super(message);
  }
}
