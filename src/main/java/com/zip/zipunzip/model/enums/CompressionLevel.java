package com.zip.zipunzip.model.enums;

public enum CompressionLevel {

  FASTEST(1),
  FAST(3),
  NORMAL(5),
  MAXIMUM(7),
  ULTRA(9);

  private int level;

  CompressionLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }
}
