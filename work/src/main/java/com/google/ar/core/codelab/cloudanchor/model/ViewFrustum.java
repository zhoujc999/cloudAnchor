package com.google.ar.core.codelab.cloudanchor.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

public class ViewFrustum {
    private List<Vector3f> myPoints = null;
    public ViewFrustum() {
        myPoints = new ArrayList<>(8);
        for (int i = 0; i < 8; i ++) {
            myPoints.add(new Vector3f());
        }
    }

    public ViewFrustum(List<Vector3f> points) {
        if (points.size() != 8) {
            // TODO: THROW ERROR
        }
        else {
            myPoints = points;
        }
    }

    public Vector3f getTopLeftNear() {
        return myPoints.get(0);
    }
    public Vector3f getTopRightNear() { return myPoints.get(1); }
    public Vector3f getBottomLeftNear() {
        return myPoints.get(2);
    }
    public Vector3f getBottomRightNear() {
        return myPoints.get(3);
    }
    public Vector3f getTopLeftFar() {
        return myPoints.get(4);
    }
    public Vector3f getTopRightFar() {
        return myPoints.get(5);
    }
    public Vector3f getBottomLeftFar() {
        return myPoints.get(6);
    }
    public Vector3f getBottomRightFar() {
        return myPoints.get(7);
    }

    public synchronized void setTopLeftNear(Vector3f point) {
        myPoints.set(0, point);
    }
    public synchronized void setTopRightNear(Vector3f point) {
        myPoints.set(1, point);
    }
    public synchronized void setBottomLeftNear(Vector3f point) {
        myPoints.set(2, point);
    }
    public synchronized void setBottomRightNear(Vector3f point) {
        myPoints.set(3, point);
    }
    public synchronized void setTopLeftFar(Vector3f point) {
        myPoints.set(4, point);
    }
    public synchronized void setTopRightFar(Vector3f point) {
        myPoints.set(5, point);
    }
    public synchronized void setBottomLeftFar(Vector3f point) {
        myPoints.set(6, point);
    }
    public synchronized void setBottomRightFar(Vector3f point) {
        myPoints.set(7, point);
    }



    public List<Vector3f> getPoints() {
        return myPoints;
    }

    @NonNull
    @Override
    public synchronized String toString() {
        String topLeftNear = "TopLeftNear: " + getTopLeftNear().toString();
        String topRightNear = "TopRightNear: " + getTopRightNear().toString();
        String bottomLeftNear = "BottomLeftNear: " + getBottomLeftNear().toString();
        String bottomRightNear = "BottomRightNear: " + getBottomRightNear().toString();

        String topLeftFar = "TopLeftFar: " + getTopLeftFar().toString();
        String topRightFar = "TopRightFar: " + getTopRightFar().toString();
        String bottomLeftFar = "BottomLeftFar: " + getBottomLeftFar().toString();
        String bottomRightFar = "BottomRightFar: " + getBottomRightFar().toString();

        return topLeftNear + "\n" + topRightNear + "\n" + bottomLeftNear + "\n" + bottomRightNear + "\n"
                + topLeftFar + "\n" + topRightFar + "\n" + bottomLeftFar + "\n" + bottomRightFar;
    }
}
