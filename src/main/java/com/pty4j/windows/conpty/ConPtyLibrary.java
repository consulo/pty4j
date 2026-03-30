package com.pty4j.windows.conpty;

import com.pty4j.util.PtyUtil;
import com.pty4j.windows.conpty.WinEx.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.W32APIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("FunctionName")
interface ConPtyLibrary extends Library {
  HRESULT CreatePseudoConsole(COORDByValue size,
                              WinNT.HANDLE hInput,
                              WinNT.HANDLE hOutput,
                              DWORD dwFlags,
                              HPCONByReference phPC);

  void ClosePseudoConsole(HPCON hPC);

  HRESULT ResizePseudoConsole(HPCON hPC, COORDByValue size);

  static ConPtyLibrary getInstance() {
    return Holder.INSTANCE;
  }

  @SuppressWarnings("SpellCheckingInspection")
  class Holder {
    private static final String CONPTY = "conpty.dll";
    private static final String KERNEL32 = "kernel32";
    private static final String DISABLE_BUNDLED_CONPTY_PROP_NAME = "com.pty4j.windows.disable.bundled.conpty";

    private static final Logger LOG = LoggerFactory.getLogger(ConPtyLibrary.class);

    static final ConPtyLibrary INSTANCE = createInstance();

    private static ConPtyLibrary createInstance() {
      if (Boolean.parseBoolean(System.getProperty(DISABLE_BUNDLED_CONPTY_PROP_NAME))) {
        LOG.warn("Loading bundled " + CONPTY + " is disabled by '" + DISABLE_BUNDLED_CONPTY_PROP_NAME + "'");
        return Native.load(KERNEL32, ConPtyLibrary.class, W32APIOptions.DEFAULT_OPTIONS);
      }
      try {
        String bundledConptyDll = PtyUtil.resolveNativeFile(CONPTY).getAbsolutePath();
        return Native.load(bundledConptyDll, ConPtyLibrary.class, W32APIOptions.DEFAULT_OPTIONS);
      }
      catch (Throwable e) {
        LOG.warn("Failed to load bundled " + CONPTY + ", fallback to " + KERNEL32, e);
        return Native.load(KERNEL32, ConPtyLibrary.class, W32APIOptions.DEFAULT_OPTIONS);
      }
    }
  }
}
