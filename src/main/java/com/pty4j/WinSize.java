package com.pty4j;

public final class WinSize {
  private final int columns;
  private final int rows;

  public WinSize(int columns, int rows) {
    this.columns = columns;
    this.rows = rows;
  }

  @Deprecated
  public WinSize(int columns, int rows, int width, int height) {
    this(columns, rows);
  }

  public int getColumns() {
    return columns;
  }

  public int getRows() {
    return rows;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    WinSize winSize = (WinSize) other;
    return columns == winSize.columns && rows == winSize.rows;
  }

  @Override
  public int hashCode() {
    return 31 * columns + rows;
  }

  @Override
  public String toString() {
    return "columns=" + columns + ", rows=" + rows;
  }
}
