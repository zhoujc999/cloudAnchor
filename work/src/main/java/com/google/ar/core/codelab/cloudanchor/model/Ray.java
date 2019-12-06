package com.google.ar.core.codelab.cloudanchor.model;

import javax.vecmath.Vector3f;

/*
 * Created by kaitlynanderson on 1/29/18.
 * pulled from previous implementation of Ray from ar core math library (removed in preview 2)
 */

public class Ray {

    public final Vector3f origin;

    public final Vector3f direction;

    public Ray(Vector3f origin, Vector3f direction) {
        this.origin = origin;
        this.direction = direction;
    }
}