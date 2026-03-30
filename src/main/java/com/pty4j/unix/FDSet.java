package com.pty4j.unix;

@SuppressWarnings("SpellCheckingInspection")
interface FDSet {
  void FD_SET(int fd);
  boolean FD_ISSET(int fd);
}
