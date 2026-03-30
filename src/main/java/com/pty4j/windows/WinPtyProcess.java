package com.pty4j.windows;

import com.pty4j.PtyProcessOptions;

import java.io.IOException;

/**
 * @deprecated Use com.pty4j.windows.winpty.WinPtyProcess instead
 */
@Deprecated
public class WinPtyProcess extends com.pty4j.windows.winpty.WinPtyProcess {
  public WinPtyProcess(PtyProcessOptions options, boolean consoleMode) throws IOException {
    super(options, consoleMode);
  }
}
