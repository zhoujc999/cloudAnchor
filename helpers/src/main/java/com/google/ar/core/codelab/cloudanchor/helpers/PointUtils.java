package com.google.ar.core.codelab.cloudanchor.helpers;
import android.opengl.Matrix;

import com.google.ar.core.Pose;

//import javax.vecmath.Vector2f;
//import javax.vecmath.Vector3f;

public class PointUtils {
    public static float[] GetWorldCoords(float[] touchPoint, float screenWidth,
                                         float screenHeight, float[] projectionMatrix, float[] viewMatrix) {

        float[] origin = projectRay(touchPoint.clone(), screenWidth, screenHeight, projectionMatrix,
                viewMatrix);
//        touchRay.direction.scale(AppSettings.getStrokeDrawDistance());
//        touchRay.origin.add(touchRay.direction);
        return origin.clone();
    }

    private static float[] projectRay(float[] touchPoint, float screenWidth, float screenHeight,
                                  float[] projectionMatrix, float[] viewMatrix) {
        float[] viewProjMtx = new float[16];
        Matrix.multiplyMM(viewProjMtx, 0, projectionMatrix, 0, viewMatrix, 0);
        float[] viewportSize = new float[2];
        viewportSize[0] = screenWidth;
        viewportSize[1] = screenHeight;
        return screenPointToRay(touchPoint, viewportSize, viewProjMtx);
    }

    private static float[] screenPointToRay(float[] point, float[] viewportSize, float[] viewProjMtx) {
        point[1] = viewportSize[1] - point[1];
        float x = point[0] * 2.0F / viewportSize[0] - 1.0F;
        float y = point[1] * 2.0F / viewportSize[1] - 1.0F;
        float[] farScreenPoint = new float[]{x, y, 1.0F, 1.0F};
        float[] nearScreenPoint = new float[]{x, y, -1.0F, 1.0F};
        float[] nearPlanePoint = new float[4];
        float[] farPlanePoint = new float[4];
        float[] invertedProjectionMatrix = new float[16];
        Matrix.setIdentityM(invertedProjectionMatrix, 0);
        Matrix.invertM(invertedProjectionMatrix, 0, viewProjMtx, 0);
        Matrix.multiplyMV(nearPlanePoint, 0, invertedProjectionMatrix, 0, nearScreenPoint, 0);
        Matrix.multiplyMV(farPlanePoint, 0, invertedProjectionMatrix, 0, farScreenPoint, 0);
        float[] direction = new float[3];
        direction[0] = farPlanePoint[0] / farPlanePoint[3];
        direction[1] = farPlanePoint[1] / farPlanePoint[3];
        direction[2] = farPlanePoint[2] / farPlanePoint[3];
//        Vector3f direction = new Vector3f(farPlanePoint[0] / farPlanePoint[3],
//                farPlanePoint[1] / farPlanePoint[3], farPlanePoint[2] / farPlanePoint[3]);
        float[] origin = new float[3];
        origin[0] = nearPlanePoint[0] / nearPlanePoint[3];
        origin[1] = nearPlanePoint[1] / nearPlanePoint[3];
        origin[2] = nearPlanePoint[2] / nearPlanePoint[3];
//        Vector3f origin = new Vector3f(new Vector3f(nearPlanePoint[0] / nearPlanePoint[3],
//                nearPlanePoint[1] / nearPlanePoint[3], nearPlanePoint[2] / nearPlanePoint[3]));
//        direction.sub(origin);
//        direction.normalize();
        return origin.clone();
    }


    /**
     * Transform a vector3f TO anchor coordinates FROM world coordinates
     */
    public static float[] TransformPointToPose(float[] point, Pose anchorPose) {
        // Recenter to anchor
        float[] position = new float[3];
        position[0] = point[0];
        position[1] = point[1];
        position[2] = point[2];

        position = anchorPose.inverse().transformPoint(position);
        return position.clone();
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