/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3test.animation;

import com.jme3.animation.Cinematic;
import com.jme3.animation.LoopMode;
import com.jme3.animation.MotionControl;
import com.jme3.animation.MotionPath;
import com.jme3.animation.MotionPathListener;
import com.jme3.animation.SoundTrack;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import org.lwjgl.opengl.APPLEAuxDepthStencil;

public class TestCinematic extends SimpleApplication {

    private Spatial teapot;
    private boolean active = true;
    private boolean playing = false;
    private MotionPath path;
    private MotionControl cameraMotionControl;
    private ChaseCamera chaser;
    private CameraNode camNode;

    public static void main(String[] args) {
        TestCinematic app = new TestCinematic();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        createScene();

        Cinematic cinematic = new Cinematic();
        stateManager.attach(cinematic);



        
        camNode = new CameraNode(cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);        
        camNode.setName("Motion cam");
        
        camNode.setLocalTranslation(new Vector3f(43.301273f, 25.0f, 0.0f));
        camNode.lookAt(teapot.getWorldTranslation(), Vector3f.UNIT_Y);

        path = new MotionPath();
        path.setCycle(true);
        path.addWayPoint(new Vector3f(20, 3, 0));
        path.addWayPoint(new Vector3f(0, 3, 20));
        path.addWayPoint(new Vector3f(-20, 3, 0));
        path.addWayPoint(new Vector3f(0, 3, -20));
        path.setCurveTension(0.83f);
       

        cameraMotionControl = new MotionControl(camNode, path);
        cameraMotionControl.setLoopMode(LoopMode.Loop);        
        cameraMotionControl.setLookAt(teapot.getWorldTranslation(), Vector3f.UNIT_Y);
        cameraMotionControl.setDirectionType(MotionControl.Direction.LookAt);

        rootNode.attachChild(camNode);

        cinematic.addCinematicEvent(1, cameraMotionControl);
        cinematic.addCinematicEvent(0, new SoundTrack(new AudioNode(assetManager, "Sound/Environment/Nature.ogg"), audioRenderer));
        cinematic.addCinematicEvent(3, new SoundTrack(new AudioNode(assetManager, "Sound/Effects/kick.wav"), audioRenderer));
        cinematic.addCinematicEvent(5.0f, new SoundTrack(new AudioNode(assetManager, "Sound/Effects/Beep.ogg"), audioRenderer));

        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        final BitmapText wayPointsText = new BitmapText(guiFont, false);
        wayPointsText.setSize(guiFont.getCharSet().getRenderedSize());

        guiNode.attachChild(wayPointsText);

        path.addListener(new MotionPathListener() {

            public void onWayPointReach(MotionControl control, int wayPointIndex) {
                if (path.getNbWayPoints() == wayPointIndex + 1) {
                    wayPointsText.setText(control.getSpatial().getName() + " Finish!!! ");
                } else {
                    wayPointsText.setText(control.getSpatial().getName() + " Reached way point " + wayPointIndex);
                }
                wayPointsText.setLocalTranslation((cam.getWidth() - wayPointsText.getLineWidth()) / 2, cam.getHeight(), 0);
            }
        });

        flyCam.setEnabled(false);
    
        cinematic.play();
//        chaser = new ChaseCamera(cam, teapot);
//        chaser.registerWithInput(inputManager);
//        chaser.setSmoothMotion(true);
//        chaser.setMaxDistance(50);
//        chaser.setDefaultDistance(50);


        //initInputs();

    }

    private void createScene() {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setFloat("m_Shininess", 1f);
        mat.setBoolean("m_UseMaterialColors", true);
        mat.setColor("m_Ambient", ColorRGBA.Black);
        mat.setColor("m_Diffuse", ColorRGBA.DarkGray);
        mat.setColor("m_Specular", ColorRGBA.White.mult(0.6f));
        Material matSoil = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matSoil.setBoolean("m_UseMaterialColors", true);
        matSoil.setColor("m_Ambient", ColorRGBA.Gray);
        matSoil.setColor("m_Diffuse", ColorRGBA.Gray);
        matSoil.setColor("m_Specular", ColorRGBA.Black);
        teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        teapot.setLocalScale(3);
        teapot.setMaterial(mat);



        rootNode.attachChild(teapot);
        Geometry soil = new Geometry("soil", new Box(new Vector3f(0, -1.0f, 0), 50, 1, 50));
        soil.setMaterial(matSoil);
        rootNode.attachChild(soil);
        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(0, -1, 0).normalizeLocal());
        light.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(light);
    }

    private void initInputs() {
        inputManager.addMapping("display_hidePath", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("SwitchPathInterpolation", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("tensionUp", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("tensionDown", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("play_stop", new KeyTrigger(KeyInput.KEY_SPACE));
        ActionListener acl = new ActionListener() {

            public void onAction(String name, boolean keyPressed, float tpf) {
                if (name.equals("display_hidePath") && keyPressed) {
                    if (active) {
                        active = false;
                        path.disableDebugShape();
                    } else {
                        active = true;
                        path.enableDebugShape(assetManager, rootNode);
                    }
                }
                if (name.equals("play_stop") && keyPressed) {
                    if (playing) {
                        playing = false;
                        cameraMotionControl.stop();
                        chaser.setEnabled(true);
                        camNode.getControl(0).setEnabled(false);
                    } else {
                        playing = true;
                        chaser.setEnabled(false);
                        camNode.getControl(0).setEnabled(true);
                        cameraMotionControl.play();
                    }
                }

                if (name.equals("SwitchPathInterpolation") && keyPressed) {
                    if (path.getPathInterpolation() == MotionPath.PathInterpolation.CatmullRom) {
                        path.setPathInterpolation(MotionPath.PathInterpolation.Linear);
                    } else {
                        path.setPathInterpolation(MotionPath.PathInterpolation.CatmullRom);
                    }
                }

                if (name.equals("tensionUp") && keyPressed) {
                    path.setCurveTension(path.getCurveTension() + 0.1f);
                    System.err.println("Tension : " + path.getCurveTension());
                }
                if (name.equals("tensionDown") && keyPressed) {
                    path.setCurveTension(path.getCurveTension() - 0.1f);
                    System.err.println("Tension : " + path.getCurveTension());
                }


            }
        };

        inputManager.addListener(acl, "display_hidePath", "play_stop", "SwitchPathInterpolation", "tensionUp", "tensionDown");

    }
}
