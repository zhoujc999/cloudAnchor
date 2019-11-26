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

package com.google.ar.core.codelab.cloudanchor.helpers;

import android.util.DisplayMetrics;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.Anchor.CloudAnchorState;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

  private Session arSession = null;
  private Frame mFrame = null;
  private Anchor mAnchor = null;
  private Supplier<Frame> mFrameSupplier = null;

  private float[] projmtx = new float[16];
  private float[] viewmtx = new float[16];
  private float[] mZeroMatrix = new float[16];
  DisplayMetrics displayMetrics = new DisplayMetrics();
  private float mScreenHeight = displayMetrics.heightPixels;
  private float mScreenWidth = displayMetrics.widthPixels;

  /**
   * This method hosts an anchor. The {@code listener} will be invoked when the results are
   * available.
   */
  public synchronized void hostCloudAnchor(
      Session session, Anchor anchor, CloudAnchorListener listener) {
    Pose pose = mFrame.getCamera().getPose();
    Anchor cameraAnchor = session.createAnchor(pose);
    Anchor newAnchor = session.hostCloudAnchor(cameraAnchor);
//    Anchor newAnchor = session.hostCloudAnchor(anchor);
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

    mFrame.getCamera().getProjectionMatrix(projmtx, 0, 0.001f,100.0f);
    mFrame.getCamera().getViewMatrix(viewmtx, 0);

    float[] position = new float[3];

//    mFrame.getCamera().getPose().getTranslation(position, 0);
    List<float[]> action = new ArrayList<float[]>();

    if (mAnchor != null) {
      float[] offset = PointUtils.TransformPointToPose(position, mAnchor.getPose());
      Log.e("TAGGG", Arrays.toString(offset));
    }
//    float[] tapPoint = new float[2];
//    tapPoint[0] = 0;
//    tapPoint[1] = 0;
//    float[] newPoint = PointUtils
//            .GetWorldCoords(tapPoint, screenWidth, screenHeight, projmtx, viewmtx);

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
}
