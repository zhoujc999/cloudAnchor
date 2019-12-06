package com.google.ar.core.codelab.cloudanchor.model;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

public class CloudViewFrustum {
  @PropertyName("vertices")
  private List<Vector3f> vertices = new ArrayList<>();

  @PropertyName("normals")
  private List<Vector3f> normals = new ArrayList<>();

//  public CloudViewFrustum(List<Vector3f> vertices, List<Vector3f> normals) {
//    this.vertices = vertices;
//    this.normals = normals;
//  }

  public List<Vector3f> getVertices() {
    return vertices;
  }
  public List<Vector3f> getNormals() {
    return normals;
  }

  public void setVertices(List<Vector3f> vertices) {
    this.vertices = vertices;
  }

  public void setNormals(List<Vector3f> normals) {
    this.normals = normals;
  }

  @Override
  public String toString() {
    return "CloudViewFrustum{" +
        "vertices=" + vertices +
        ", normals=" + normals +
        '}';
  }
}
