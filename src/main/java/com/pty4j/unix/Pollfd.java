package com.pty4j.unix;

class Pollfd {
  final int fd;
  final short events;
  short revents = 0;

  Pollfd(int fd, short events) {
    this.fd = fd;
    this.events = events;
  }

  short getRevents() {
    return revents;
  }
}
