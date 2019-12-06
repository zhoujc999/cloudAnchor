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

package com.google.ar.core.codelab;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.ar.core.codelab.cloudanchor.model.CloudViewFrustum;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Helper class for Firebase storage of cloud anchor IDs. */
public class FirebaseManager {

  /** Listener for a new Cloud Anchor ID from the Firebase Database. */
  public interface CloudAnchorIdListener {
    void onCloudAnchorIdAvailable(String cloudAnchorId);
  }

  /** Listener for a partner view frustum from the Firebase Database. */
  public interface PartnerCloudViewFrustumListener {
    void onCloudViewFrustumChanged(CloudViewFrustum cloudViewFrustum);
  }

  /** Listener for a new short code from the Firebase Database. */
  public interface ShortCodeListener {
    void onShortCodeAvailable(Integer shortCode);
  }

  private static final String TAG = FirebaseManager.class.getName();
  private static final String KEY_ROOT_DIR = "shared_anchor_codelab_root";
  private static final String KEY_NEXT_SHORT_CODE = "next_short_code";
  private static final String KEY_PREFIX = "anchor_";
  private static final String ANCHOR_ID = "anchor_id";
  private static final String PARTICIPANTS = "participants";
  private static final String REQUEST_UPDATE = "request_update";
  private static final int INITIAL_SHORT_CODE = 142;
  private final DatabaseReference rootRef;
  private DatabaseReference mAnchorRef;
  private DatabaseReference mParticipantsRef;
  private final FirebaseAuth mFirebaseAuth;
  private FirebaseUser currentUser;
  private Consumer userUidConsumer;
  private Supplier<CloudViewFrustum> mCloudViewFrustumSupplier;
  private String mUserUid;
  private String partnerUid;

  private PartnerCloudViewFrustumListener partnerCloudViewFrustumListener;

  /** Constructor that initializes the Firebase connection. */
  public FirebaseManager(Context context) {
    FirebaseApp firebaseApp = FirebaseApp.initializeApp(context);
    mFirebaseAuth = FirebaseAuth.getInstance();
    rootRef = FirebaseDatabase.getInstance(firebaseApp).getReference().child(KEY_ROOT_DIR);
    DatabaseReference.goOnline();
  }

  /** Gets a new short code that can be used to store the anchor ID. */
  public void nextShortCode(ShortCodeListener listener) {
    // Run a transaction on the node containing the next short code available. This increments the
    // value in the database and retrieves it in one atomic all-or-nothing operation.
    rootRef
        .child(KEY_NEXT_SHORT_CODE)
        .runTransaction(
            new Transaction.Handler() {
              @Override
              public Transaction.Result doTransaction(MutableData currentData) {
                Integer shortCode = currentData.getValue(Integer.class);
                if (shortCode == null) {
                  // Set the initial short code if one did not exist before.
                  shortCode = INITIAL_SHORT_CODE - 1;
                }
                currentData.setValue(shortCode + 1);
                return Transaction.success(currentData);
              }

              @Override
              public void onComplete(
                  DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (!committed) {
                  Log.e(TAG, "Firebase Error", error.toException());
                  listener.onShortCodeAvailable(null);
                } else {
                  listener.onShortCodeAvailable(currentData.getValue(Integer.class));
                }
              }
            });
  }


  /** Stores the cloud anchor ID in the configured Firebase Database. */
  public void storeUsingShortCode(int shortCode, String cloudAnchorId) {
    if (mAnchorRef == null) {
      getAnchorRef(shortCode);
    }
    mAnchorRef.child(ANCHOR_ID).setValue(cloudAnchorId);
  }

  /** Stores the boolean requestUpdate in the configured Firebase Database. */
  public void storeRequestUpdate() {
    if (mAnchorRef == null) {
      return;
    }
  }

  public void requestPartnerToUpdate() {
    if (mAnchorRef == null || partnerUid == null) {
      return;
    }
    mAnchorRef.child(REQUEST_UPDATE).setValue(mUserUid);
  }

  public void setRequestUpdateListener() {
    if (mAnchorRef == null) {
      return;
    }
    mAnchorRef.child(REQUEST_UPDATE).addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists()) {
              return;
            }
            if (partnerUid.equals(snapshot.getValue(String.class))) {
              updateViewFrustum(mCloudViewFrustumSupplier.get());
              mAnchorRef.child(REQUEST_UPDATE).setValue(null);
            }
          }

          public void onCancelled(@NonNull DatabaseError error) {
            Log.e(
                TAG,
                "The Firebase operation for requestPartnerToUpdate was cancelled.",
                error.toException());
          }
        }
    );
  }

  public void updateViewFrustum(CloudViewFrustum cloudViewFrustum) {
    if (mAnchorRef == null) {
      return;
    }
    Map<String, Object> update = new HashMap<>();
    update.put(mUserUid, cloudViewFrustum);
    mAnchorRef.updateChildren(update);
  }

    public void storeUid(int shortCode) {
      if (mParticipantsRef == null) {
        getParticipantsRef(shortCode);
      }
      mParticipantsRef.child(mUserUid).setValue(true);
    }


  public void setPartnerCloudViewFrustumListener(PartnerCloudViewFrustumListener listener) {
    partnerCloudViewFrustumListener = listener;
  }

  public void initializeFrustumListener(PartnerCloudViewFrustumListener listener) {
    if (mAnchorRef == null || partnerUid == null) {
      return;
    }
    mAnchorRef.child(partnerUid)
        .addValueEventListener(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
              if (!snapshot.exists()) {
                return;
              }
              CloudViewFrustum cloudViewFrustum = snapshot.getValue(CloudViewFrustum.class);
              listener.onCloudViewFrustumChanged(cloudViewFrustum);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                Log.e(
                    TAG,
                    "The Firebase operation for getCloudViewFrustum was cancelled.",
                    error.toException());
              }
            }
        );
  }


  public void setPartnerListener() {
    if (mParticipantsRef == null) {
      return;
    }
    mParticipantsRef
        .addChildEventListener(
            new ChildEventListener() {
              @Override
              public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (!mUserUid.equals(snapshot.getKey())) {
                  partnerUid = snapshot.getKey();
                  initializeFrustumListener(partnerCloudViewFrustumListener);
                }
              }
              @Override
              public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
              }
              @Override
              public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
              }
              @Override
              public void onChildRemoved(@NonNull DataSnapshot snapshot) {
              }
              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                Log.e(
                    TAG,
                    "The Firebase operation for getPartner was cancelled.",
                    error.toException());
              }
            });
  }
  /**
   * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
   * was not stored for this short code.
   */

  public void getCloudAnchorId(int shortCode, CloudAnchorIdListener listener) {
    if (mAnchorRef == null) {
      getAnchorRef(shortCode);
    }
    mAnchorRef.child(ANCHOR_ID)
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Listener invoked when the data is successfully read from Firebase.
                listener.onCloudAnchorIdAvailable(String.valueOf(snapshot.getValue()));
              }

              @Override
              public void onCancelled(DatabaseError error) {
                Log.e(
                    TAG,
                    "The Firebase operation for getCloudAnchorId was cancelled.",
                    error.toException());
                listener.onCloudAnchorIdAvailable(null);
              }
            });
  }

  public void login(Consumer c) {
      userUidConsumer = c;
      FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
      if (currentUser != null) {
          Log.e(TAG, "onCreate: user uid " + currentUser.getUid());
          mUserUid = currentUser.getUid();
        userUidConsumer.accept(mUserUid);
      } else {
          loginAnonymously(userUidConsumer);
      }
  }

   public void loginAnonymously(Consumer c) {
        mFirebaseAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            // Sign in success, update UI with the signed-in user's information
            currentUser = mFirebaseAuth.getCurrentUser();
            mUserUid = currentUser.getUid();
            userUidConsumer.accept(mUserUid);

        } else {
            // If sign in fails, display a message to the user.
            Log.w(TAG, "signInAnonymously:failure", task.getException());
        }

    });
   }

   private void getAnchorRef(int shortCode) {
     mAnchorRef = rootRef.child(KEY_PREFIX + shortCode).getRef();
   }


  private void getParticipantsRef(int shortCode) {
    if (mAnchorRef == null) {
      getAnchorRef(shortCode);
    }
    mParticipantsRef = mAnchorRef.child(PARTICIPANTS).getRef();
  }

  public synchronized void getCloudViewFrustumSupplier(Supplier<CloudViewFrustum> cloudViewFrustumSupplier) {
    mCloudViewFrustumSupplier = cloudViewFrustumSupplier;
  }
}