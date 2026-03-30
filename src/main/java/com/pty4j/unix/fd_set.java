package com.pty4j.unix;

import com.sun.jna.Structure;

@SuppressWarnings({"ClassName", "SpellCheckingInspection"})
@Structure.FieldOrder({"fd_array"})
class fd_set extends Structure implements FDSet {

  private static final int NFBBITS = 32;
  private static final int FS_COUNT = 1024;

  public int[] fd_array = new int[(FS_COUNT + NFBBITS - 1) / NFBBITS];

  @Override
  public void FD_SET(int fd) {
    fd_array[fd / NFBBITS] |= (1 << (fd % NFBBITS));
  }

  @Override
  public boolean FD_ISSET(int fd) {
    return (fd_array[fd / NFBBITS] & (1 << (fd % NFBBITS))) != 0;
  }
}
