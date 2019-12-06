/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.codelab.cloudanchor;

import com.google.ar.core.Anchor;
import com.google.ar.core.Anchor.CloudAnchorState;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.codelab.cloudanchor.model.CloudViewFrustum;
import com.google.ar.core.codelab.cloudanchor.model.ViewFrustum;
import com.google.ar.core.codelab.cloudanchor.rendering.PointUtils;
import com.google.ar.core.codelab.cloudanchor.rendering.SATUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.vecmath.Vector3f;


/**
 * A helper class to handle all the Cloud Anchors logic, and add a callback-like mechanism on top of
 * the existing ARCore API.
 */
public class CloudAnchorManager {
  /** Listener for the results of a host or resolve operation. */
  public interface CloudAnchorListener {

    /** This method is invoked when the results of a Cloud Anchor operation are available. */
    void onCloudTaskComplete(Anchor anchor);
  }
  private final HashMap<Anchor, CloudAnchorListener> pendingAnchors = new HashMap<>();


  public static float NEARCLIP = 0.01f;
  public static float FARCLIP = 10.0f;


  private Session arSession;
  private Frame mFrame;
  private Anchor mAnchor;
  private Supplier<Frame> mFrameSupplier;

  private ViewFrustum mViewFrustum = new ViewFrustum();
  private CloudViewFrustum mCloudViewFrustum = new CloudViewFrustum();

  private float[] projmtx = new float[16];
  private float[] viewmtx = new float[16];

  private float mScreenHeight = 0;
  private float mScreenWidth = 0;

  /**
   * This method hosts an anchor. The {@code listener} will be invoked when the results are
   * available.
   */
  public synchronized void hostCloudAnchor(
      Session session, Anchor anchor, CloudAnchorListener listener) {
//    Pose pose = mFrame.getCamera().getPose();
//    Anchor cameraAnchor = session.createAnchor(pose);
//    Anchor newAnchor = session.hostCloudAnchor(cameraAnchor);
    Anchor newAnchor = session.hostCloudAnchor(anchor);
    pendingAnchors.put(newAnchor, listener);
  }

  /**
   * This method resolves an anchor. The {@code listener} will be invoked when the results are
   * available.
   */
  public synchronized void resolveCloudAnchor(
      Session session, String anchorId, CloudAnchorListener listener) {
    Anchor newAnchor = session.resolveCloudAnchor(anchorId);
    pendingAnchors.put(newAnchor, listener);
  }

  /** Should be called after a {@link Session#update()} call. */
  public synchronized void onUpdate() {
    mFrame = mFrameSupplier.get();

    mFrame.getCamera().getProjectionMatrix(projmtx, 0, NEARCLIP,FARCLIP);
    mFrame.getCamera().getViewMatrix(viewmtx, 0);


//    mFrame.getCamera().getPose().getTranslation(position, 0);

    PointUtils.setFrustumWorldCoords(mViewFrustum, mScreenWidth, mScreenHeight, projmtx, viewmtx, NEARCLIP, FARCLIP);
    if (mAnchor != null) {
      PointUtils.transformFrustumCoords(mViewFrustum, mAnchor.getPose());
      List<Vector3f> normals = new ArrayList<>();
      SATUtils.generateNormals(mViewFrustum, normals);
      mCloudViewFrustum.setVertices(mViewFrustum.getPoints());
      mCloudViewFrustum.setNormals(normals);

//      Log.e("TAGGGG", cvf.toString());
//      Log.e("TAGGG", mViewFrustum.toString());
    }

    Iterator<Map.Entry<Anchor, CloudAnchorListener>> iter = pendingAnchors.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Anchor, CloudAnchorListener> entry = iter.next();
      Anchor anchor = entry.getKey();
      if (isReturnableState(anchor.getCloudAnchorState())) {
        mAnchor = anchor;
        CloudAnchorListener listener = entry.getValue();
        listener.onCloudTaskComplete(anchor);
        iter.remove();
      }
    }
  }

  /** Used to clear any currently registered listeners, so they wont be called again. */
  public synchronized void clearListeners() {
    pendingAnchors.clear();
  }

  private static boolean isReturnableState(CloudAnchorState cloudState) {
    switch (cloudState) {
      case NONE:
      case TASK_IN_PROGRESS:
        return false;
      default:
        return true;
    }
  }

  public synchronized void getSession(Session session) {
    arSession = session;
  }
  public synchronized void getFrameSupplier(Supplier<Frame> frameSupplier) {
    mFrameSupplier = frameSupplier;
  }
  public synchronized CloudViewFrustum getCloudViewFrustum() {
    return mCloudViewFrustum;
  }

  public synchronized boolean detectCollision(CloudViewFrustum partnerCloudViewFrustum) {
    if (mViewFrustum == null || partnerCloudViewFrustum == null) {
      return false;
    }

    return SATUtils.detectCollision(mViewFrustum, partnerCloudViewFrustum.getVertices(), partnerCloudViewFrustum.getNormals());
  }

  public void setScreenHeight(float screenHeight) {
    mScreenHeight = screenHeight;
  }
  public void setScreenWidth(float screenWidth) {
    mScreenWidth = screenWidth;
  }
}
