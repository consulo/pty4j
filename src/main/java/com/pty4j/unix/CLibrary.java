package com.pty4j.unix;

import com.sun.jna.*;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.LibCAPI.ssize_t;

@SuppressWarnings("SpellCheckingInspection")
public class CLibrary {

  public static final int O_WRONLY = 0x00000001;
  public static final int O_RDWR = 0x00000002;
  public static final short POLLIN = 0x00000001;
  public static final int EINTR = 0x00000004;
  public static final int ENOTTY = 25; // Not a typewriter / "Inappropriate ioctl for device" (errno.h)

  public static final int EAGAIN;
  public static final int O_NOCTTY;

  static {
    EAGAIN = (Platform.isLinux() || Platform.isSolaris()) ? 0x0000000b : 0x00000023;

    if (Platform.isLinux()) {
      O_NOCTTY = 0x00000100;
    }
    else if (Platform.isFreeBSD()) {
      O_NOCTTY = 0x00008000;
    }
    else if (Platform.isSolaris()) {
      O_NOCTTY = 0x00000800;
    }
    else {
      O_NOCTTY = 0x00020000;
    }
  }

  private static final CLibraryNative libc = Native.load(Platform.C_LIBRARY_NAME, CLibraryNative.class);

  public static int open(String path, int flags) {
    return libc.open(path, flags);
  }

  public static int close(int fd) {
    return libc.close(fd);
  }

  public static int read(int fd, byte[] buf, int len) {
    return libc.read(fd, buf, new size_t(len)).intValue();
  }

  public static int write(int fd, byte[] buf, int len) {
    return libc.write(fd, buf, new size_t(len)).intValue();
  }

  public static int pipe(int[] fds) {
    return libc.pipe(fds);
  }

  // https://pubs.opengroup.org/onlinepubs/009696699/functions/errno.html
  public static int errno() {
    return Native.getLastError();
  }

  /**
   * Upon successful completion, poll() shall return a non-negative value.
   * A positive value indicates the total number of file descriptors that have been selected
   * (that is, file descriptors for which the revents member is non-zero).
   * A value of 0 indicates that the call timed out and no file descriptors have been selected.
   * Upon failure, poll() shall return -1 and set errno to indicate the error.
   */
  public static int poll(Pollfd[] fds, int timeout) {
    PollfdStructureByReference pollfdsReference = new PollfdStructureByReference();
    @SuppressWarnings("unchecked")
    PollfdStructure[] pollfdStructures = (PollfdStructure[]) pollfdsReference.toArray(fds.length);
    for (int i = 0; i < fds.length; i++) {
      pollfdStructures[i].fd = fds[i].fd;
      pollfdStructures[i].events = fds[i].events;
    }
    int ret = libc.poll(pollfdsReference, fds.length, timeout);
    for (int i = 0; i < fds.length; i++) {
      fds[i].revents = pollfdStructures[i].revents;
    }
    return ret;
  }

  public static int select(int nfds, FDSet readfds) {
    return libc.select(nfds, (fd_set) readfds, null, null, null);
  }

  private CLibrary() {
  }

  private interface CLibraryNative extends Library {
    // https://pubs.opengroup.org/onlinepubs/009695399/functions/open.html
    int open(String path, int flags);

    // https://pubs.opengroup.org/onlinepubs/009604499/functions/close.html
    int close(int fd);

    // https://pubs.opengroup.org/onlinepubs/009604599/functions/read.html
    ssize_t read(int fd, byte[] buf, size_t len);

    // https://pubs.opengroup.org/onlinepubs/009695399/functions/write.html
    ssize_t write(int fd, byte[] buf, size_t len);

    // https://pubs.opengroup.org/onlinepubs/009695399/functions/pipe.html
    int pipe(int[] fds);

    // https://pubs.opengroup.org/onlinepubs/009604599/functions/poll.html
    int poll(PollfdStructureByReference pollfds, int nfds, int timeout);

    // https://pubs.opengroup.org/7908799/xsh/select.html
    int select(int nfds, fd_set readfds, fd_set writefds, fd_set errorfds, Timeval timeout);
  }

  // https://pubs.opengroup.org/onlinepubs/009604599/basedefs/poll.h.html
  @Structure.FieldOrder({"fd", "events", "revents"})
  public static class PollfdStructure extends Structure {
    public int fd = 0;
    public short events = 0;
    public short revents = 0;
  }

  public static class PollfdStructureByReference extends PollfdStructure implements Structure.ByReference {
  }

  @SuppressWarnings({"unused", "ClassName"})
  @Structure.FieldOrder({"tv_sec", "tv_usec"})
  private static class Timeval extends Structure {
    public NativeLong tv_sec = new NativeLong(0);
    public NativeLong tv_usec = new NativeLong(0);
  }
}
