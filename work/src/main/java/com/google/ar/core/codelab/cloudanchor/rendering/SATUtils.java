package com.google.ar.core.codelab.cloudanchor.rendering;

import com.google.ar.core.codelab.cloudanchor.model.ViewFrustum;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

public class SATUtils {
    public static float epsilon = 0.000001f;
    public static double[] onesArray = new double[]{1, 1, 1, 1};
    public static RealVector onesVector = new ArrayRealVector(onesArray);


    public static boolean detectCollision(ViewFrustum a, ViewFrustum b) {
        List<Vector3f> normals = new ArrayList<>();


        generateNormals(a, normals);
        generateNormals(b, normals);

        for (Vector3f normal : normals) {
            if (!intersectsWhenProjected(a.getPoints(), b.getPoints(), normal)) {
                return false;
            }
        }
        return true;
    }


    public static boolean detectCollision(ViewFrustum a, List<Vector3f> verticesB, List<Vector3f> normalsB) {
        List<Vector3f> normals = new ArrayList<>();


        generateNormals(a, normals);
        normals.addAll(normalsB);

        for (Vector3f normal : normals) {
            if (!intersectsWhenProjected(a.getPoints(), verticesB, normal)) {
                return false;
            }
        }
        return true;
    }



    public static void generateNormals(ViewFrustum viewFrustum, List<Vector3f> result) {

        Vector3f topLeftNearPoint = viewFrustum.getTopLeftNear();
        Vector3f topLeftFarPoint = viewFrustum.getTopLeftFar();
        Vector3f topRightNearPoint = viewFrustum.getTopRightNear();
        Vector3f topRightFarPoint = viewFrustum.getTopRightFar();
        Vector3f bottomLeftNearPoint = viewFrustum.getBottomLeftNear();
        Vector3f bottomLeftFarPoint = viewFrustum.getBottomLeftFar();
        Vector3f bottomRightNearPoint = viewFrustum.getBottomRightNear();
        Vector3f bottomRightFarPoint = viewFrustum.getBottomRightFar();


        generateNormal(topLeftNearPoint, topRightNearPoint, bottomRightNearPoint, bottomLeftNearPoint, result);
        generateNormal(topLeftFarPoint, topLeftNearPoint, bottomLeftNearPoint, bottomLeftFarPoint, result);
        generateNormal(topLeftFarPoint, topRightFarPoint, topRightNearPoint, topLeftNearPoint, result);
        generateNormal(topRightNearPoint, topRightFarPoint, bottomRightFarPoint, bottomRightNearPoint, result);
        generateNormal(bottomLeftNearPoint, bottomRightNearPoint, bottomRightFarPoint, bottomLeftFarPoint, result);
        generateNormal(topLeftFarPoint, topRightFarPoint, bottomRightFarPoint, bottomLeftFarPoint, result);

    }

    /*
     * Clockwise a, b, c, d
     */
    private static void generateNormal(Vector3f a, Vector3f b, Vector3f c, Vector3f d, List<Vector3f> result) {
        RealMatrix A = new BlockRealMatrix(4, 3);


        A.setEntry(0,0, a.x);
        A.setEntry(0,1, a.y);
        A.setEntry(0,2, a.z);
        A.setEntry(1,0, b.x);
        A.setEntry(1,1, b.y);
        A.setEntry(1,2, b.z);
        A.setEntry(2,0, c.x);
        A.setEntry(2,1, c.y);
        A.setEntry(2,2, c.z);
        A.setEntry(3,0, d.x);
        A.setEntry(3,1, d.y);
        A.setEntry(3,2, d.z);


        SingularValueDecomposition svd = new SingularValueDecomposition(A);

        DecompositionSolver ds = svd.getSolver();

        if (!ds.isNonSingular()) {
            // TODO: THROW ERROR
//            Log.e("SATUtils","Singular Matrix");
        }

        RealVector normal = ds.solve(onesVector);

        Vector3f normalVec = new Vector3f();
        normalVec.x = (float) normal.getEntry(0);
        normalVec.y = (float) normal.getEntry(1);
        normalVec.z = (float) normal.getEntry(2);

        result.add(normalVec);
    }


    private static boolean intersectsWhenProjected(List<Vector3f> verticesA, List<Vector3f> verticesB, Vector3f axis) {

        // Handles the cross product = {0,0,0} case
        Vector3f vecZero = new Vector3f();
        if (axis.epsilonEquals(vecZero, epsilon)) {
            return true;
        }

        float aMin = Float.MAX_VALUE;
        float aMax = Float.MIN_VALUE;
        float bMin = Float.MAX_VALUE;
        float bMax = Float.MIN_VALUE;

        // Define two intervals, a and b. Calculate their min and max values
        for (int i = 0; i < 8; i++) {
            Vector3f tempA = new Vector3f(verticesA.get(i));
            float distanceA = tempA.dot(axis);
            aMin = (distanceA < aMin) ? distanceA : aMin;
            aMax = (distanceA > aMax) ? distanceA : aMax;

            Vector3f tempB = new Vector3f(verticesB.get(i));
            float distanceB = tempB.dot(axis);
            bMin = (distanceB < bMin ) ? distanceB : bMin;
            bMax = (distanceB > bMax ) ? distanceB : bMax;
        }

        // One-dimensional intersection test between a and b
        float longSpan = Math.max(aMax, bMax) - Math.min(aMin, bMin);
        float sumSpan = aMax - aMin + bMax - bMin;
        return longSpan < sumSpan; // Change this to <= if you want the case were they are touching but not overlapping, to count as an intersection
    }


}
