package com.example.myinteriorapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;

public class ARFragment extends ArFragment {
    private Uri modelUri;
    private ArSceneView arSceneView;

    public void setModelUri(Uri modelUri) {
        this.modelUri = modelUri;
    }

    public ARFragment() {
        // 기본 생성자
    }
    @Override
    protected Config getSessionConfiguration(Session session) {
        // Create a config object
        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setFocusMode(Config.FocusMode.AUTO);
        session.configure(config);
        this.getArSceneView().setupSession(session);

        return config;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!(context instanceof Activity)) {
            throw new UnsupportedOperationException("ARFragment should be attached to an Activity.");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ArSceneView arSceneView = getArSceneView();
        if (arSceneView != null) {
            Scene scene = arSceneView.getScene();
            Camera camera = scene.getCamera();
            placeObject(scene, camera);
        }
        return view;
    }

    private void placeObject(Scene scene, Camera camera) {
        if (modelUri == null) {
            return;
        }

        ModelRenderable.builder()
                .setSource(getContext(), modelUri)
                .build()
                .thenAccept(modelRenderable -> {
                    // Handle model renderable
                    if (scene != null) {
                        // Perform hit test against the AR scene view
                        float screenX = 0.5f;  // 화면의 가로 중앙 좌표
                        float screenY = 0.5f;  // 화면의 세로 중앙 좌표
                        float screenWidth = arSceneView.getWidth();
                        float screenHeight = arSceneView.getHeight();

                        PointF screenPoint = new PointF(screenX * screenWidth, screenY * screenHeight);
                        List<HitResult> hitResultList = arSceneView.getArFrame().hitTest(screenPoint.x, screenPoint.y);

                        for (HitResult hitResult : hitResultList) {
                            Trackable trackable = hitResult.getTrackable();
                            if (trackable instanceof Plane && ((Plane) trackable).getTrackingState() == TrackingState.TRACKING) {
                                Plane plane = (Plane) trackable;
                                if (plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                                    // Create an anchor based on the hit point
                                    Anchor anchor = hitResult.createAnchor();
                                    AnchorNode anchorNode = new AnchorNode(anchor);
                                    anchorNode.setParent(scene);

                                    // Create a transformable node and add it to the anchor node
                                    TransformableNode transformableNode = new TransformableNode(getTransformationSystem());
                                    transformableNode.setParent(anchorNode);
                                    transformableNode.setRenderable(modelRenderable);
                                    transformableNode.select();

                                    // Only handle the first valid hit result
                                    break;
                                }
                            }
                        }
                    }


                })
                .exceptionally(throwable -> {
                    // Handle error in loading the model renderable
                    return null;
                });
    }

}

