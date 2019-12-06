package com.google.ar.core.codelab.cloudanchor.rendering;

import android.opengl.Matrix;

import com.google.ar.core.Pose;
import com.google.ar.core.codelab.cloudanchor.model.Ray;
import com.google.ar.core.codelab.cloudanchor.model.ViewConfig;
import com.google.ar.core.codelab.cloudanchor.model.ViewFrustum;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class PointUtils {
    public static synchronized void setFrustumWorldCoords(ViewFrustum viewFrustum, ViewConfig viewConfig, float screenWidth,
                                                          float screenHeight, float[] projectionMatrix, float[] viewMatrix,
                                                          float nearClip, float farClip) {

        Vector2f topLeft = new Vector2f(viewConfig.getLeft(), viewConfig.getTop());
        Vector2f topRight = new Vector2f(viewConfig.getRight(), viewConfig.getTop());
        Vector2f bottomLeft = new Vector2f(viewConfig.getLeft(), viewConfig.getBottom());
        Vector2f bottomRight = new Vector2f(viewConfig.getRight(), viewConfig.getBottom());

        Ray topLeftRay = projectRay(new Vector2f(topLeft), screenWidth, screenHeight, projectionMatrix,
                viewMatrix);

        Vector3f topLeftNearPoint = new Vector3f(topLeftRay.origin);
        Vector3f topLeftNearDirection = new Vector3f(topLeftRay.direction);
        topLeftNearDirection.scale(nearClip);
        topLeftNearPoint.add(topLeftNearDirection);
        Vector3f topLeftFarPoint = new Vector3f(topLeftRay.origin);
        Vector3f topLeftFarDirection = new Vector3f(topLeftRay.direction);
        topLeftFarDirection.scale(farClip);
        topLeftFarPoint.add(topLeftFarDirection);
        viewFrustum.setTopLeftNear(topLeftNearPoint);
        viewFrustum.setTopLeftFar(topLeftFarPoint);

        Ray topRightRay = projectRay(new Vector2f(topRight), screenWidth, screenHeight, projectionMatrix,
                viewMatrix);

        Vector3f topRightNearPoint = new Vector3f(topRightRay.origin);
        Vector3f topRightNearDirection = new Vector3f(topRightRay.direction);
        topRightNearDirection.scale(nearClip);
        topRightNearPoint.add(topRightNearDirection);
        Vector3f topRightFarPoint = new Vector3f(topRightRay.origin);
        Vector3f topRightFarDirection = new Vector3f(topRightRay.direction);
        topRightFarDirection.scale(farClip);
        topRightFarPoint.add(topRightFarDirection);
        viewFrustum.setTopRightNear(topRightNearPoint);
        viewFrustum.setTopRightFar(topRightFarPoint);

        Ray bottomLeftRay = projectRay(new Vector2f(bottomLeft), screenWidth, screenHeight, projectionMatrix,
                viewMatrix);

        Vector3f bottomLeftNearPoint = new Vector3f(bottomLeftRay.origin);
        Vector3f bottomLeftNearDirection = new Vector3f(bottomLeftRay.direction);
        bottomLeftNearDirection.scale(nearClip);
        bottomLeftNearPoint.add(bottomLeftNearDirection);
        Vector3f bottomLeftFarPoint = new Vector3f(bottomLeftRay.origin);
        Vector3f bottomLeftFarDirection = new Vector3f(bottomLeftRay.direction);
        bottomLeftFarDirection.scale(farClip);
        bottomLeftFarPoint.add(bottomLeftFarDirection);
        viewFrustum.setBottomLeftNear(bottomLeftNearPoint);
        viewFrustum.setBottomLeftFar(bottomLeftFarPoint);

        Ray bottomRightRay = projectRay(new Vector2f(bottomRight), screenWidth, screenHeight, projectionMatrix,
                viewMatrix);

        Vector3f bottomRightNearPoint = new Vector3f(bottomRightRay.origin);
        Vector3f bottomRightNearDirection = new Vector3f(bottomRightRay.direction);
        bottomRightNearDirection.scale(nearClip);
        bottomRightNearPoint.add(bottomRightNearDirection);
        Vector3f bottomRightFarPoint = new Vector3f(bottomRightRay.origin);
        Vector3f bottomRightFarDirection = new Vector3f(bottomRightRay.direction);
        bottomRightFarDirection.scale(farClip);
        bottomRightFarPoint.add(bottomRightFarDirection);
        viewFrustum.setBottomRightNear(bottomRightNearPoint);
        viewFrustum.setBottomRightFar(bottomRightFarPoint);
    }

    private static synchronized Ray projectRay(Vector2f touchPoint, float screenWidth, float screenHeight,
                                  float[] projectionMatrix, float[] viewMatrix) {
        float[] viewProjMtx = new float[16];
        Matrix.multiplyMM(viewProjMtx, 0, projectionMatrix, 0, viewMatrix, 0);
        return screenPointToRay(touchPoint, new Vector2f(screenWidth, screenHeight),
                viewProjMtx);
    }

    private static synchronized Ray screenPointToRay(Vector2f point, Vector2f viewportSize, float[] viewProjMtx) {
        point.y = viewportSize.y - point.y;
        float x = point.x * 2.0F / viewportSize.x - 1.0F;
        float y = point.y * 2.0F / viewportSize.y - 1.0F;
        float[] farScreenPoint = new float[]{x, y, 1.0F, 1.0F};
        float[] nearScreenPoint = new float[]{x, y, -1.0F, 1.0F};
        float[] nearPlanePoint = new float[4];
        float[] farPlanePoint = new float[4];
        float[] invertedProjectionMatrix = new float[16];
        Matrix.setIdentityM(invertedProjectionMatrix, 0);
        Matrix.invertM(invertedProjectionMatrix, 0, viewProjMtx, 0);
        Matrix.multiplyMV(nearPlanePoint, 0, invertedProjectionMatrix, 0, nearScreenPoint, 0);
        Matrix.multiplyMV(farPlanePoint, 0, invertedProjectionMatrix, 0, farScreenPoint, 0);
        Vector3f direction = new Vector3f(farPlanePoint[0] / farPlanePoint[3],
                farPlanePoint[1] / farPlanePoint[3], farPlanePoint[2] / farPlanePoint[3]);
        Vector3f origin = new Vector3f(new Vector3f(nearPlanePoint[0] / nearPlanePoint[3],
                nearPlanePoint[1] / nearPlanePoint[3], nearPlanePoint[2] / nearPlanePoint[3]));
        direction.sub(origin);
        direction.normalize();
        return new Ray(origin, direction);
    }


    public static synchronized void transformFrustumCoords(ViewFrustum viewFrustum, Pose anchorPose) {
        Vector3f topLeftNearPoint = viewFrustum.getTopLeftNear();
        Vector3f topLeftFarPoint = viewFrustum.getTopLeftFar();
        Vector3f topRightNearPoint = viewFrustum.getTopRightNear();
        Vector3f topRightFarPoint = viewFrustum.getTopRightFar();
        Vector3f bottomLeftNearPoint = viewFrustum.getBottomLeftNear();
        Vector3f bottomLeftFarPoint = viewFrustum.getBottomLeftFar();
        Vector3f bottomRightNearPoint = viewFrustum.getBottomRightNear();
        Vector3f bottomRightFarPoint = viewFrustum.getBottomRightFar();

        viewFrustum.setTopLeftNear(TransformPointToPose(topLeftNearPoint, anchorPose));
        viewFrustum.setTopLeftFar(TransformPointToPose(topLeftFarPoint, anchorPose));
        viewFrustum.setTopRightNear(TransformPointToPose(topRightNearPoint, anchorPose));
        viewFrustum.setTopRightFar(TransformPointToPose(topRightFarPoint, anchorPose));
        viewFrustum.setBottomLeftNear(TransformPointToPose(bottomLeftNearPoint, anchorPose));
        viewFrustum.setBottomLeftFar(TransformPointToPose(bottomLeftFarPoint, anchorPose));
        viewFrustum.setBottomRightNear(TransformPointToPose(bottomRightNearPoint, anchorPose));
        viewFrustum.setBottomRightFar(TransformPointToPose(bottomRightFarPoint, anchorPose));
    }


    /**
     * Transform a vector3f TO anchor coordinates FROM world coordinates
     */
    public static synchronized Vector3f TransformPointToPose(Vector3f point, Pose anchorPose) {
        // Recenter to anchor
        float[] position = new float[3];
        position[0] = point.x;
        position[1] = point.y;
        position[2] = point.z;

        position = anchorPose.inverse().transformPoint(position);
        return new Vector3f(position[0], position[1], position[2]);
    }



//    public static boolean distanceCheck(Vector3f newPoint, Vector3f lastPoint) {
//        Vector3f temp = new Vector3f();
//        temp.sub(newPoint, lastPoint);
//        return temp.lengthSquared() > AppSettings.getMinDistance();
//    }



//    /**
//     * Transform a vector3f FROM anchor coordinates TO world coordinates
//     */
//    public static Vector3f TransformPointFromPose(Vector3f point, Pose anchorPose) {
//        Log.e("HELLO",anchorPose.toString());
//        float[] position = new float[3];
//        position[0] = point.x;
//        position[1] = point.y;
//        position[2] = point.z;
//
//        position = anchorPose.transformPoint(position);
//        return new Vector3f(position[0], position[1], position[2]);
//    }


}