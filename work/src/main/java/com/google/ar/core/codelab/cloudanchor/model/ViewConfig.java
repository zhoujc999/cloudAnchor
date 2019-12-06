package com.google.ar.core.codelab.cloudanchor.model;

public class ViewConfig {
  private float top;
  private float bottom;
  private float left;
  private float right;

  public ViewConfig(float top, float bottom, float left, float right) {
    this.top = top;
    this.bottom = bottom;
    this.left = left;
    this.right = right;
  }

  public float getTop() {
    return top;
  }

  public float getBottom() {
    return bottom;
  }

  public float getLeft() {
    return left;
  }

  public float getRight() {
    return right;
  }
}
