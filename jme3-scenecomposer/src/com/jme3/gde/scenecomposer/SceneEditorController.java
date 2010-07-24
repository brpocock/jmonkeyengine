/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.EmitterSphereShape;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.AssetLinkNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import com.jme3.util.TangentBinormalGenerator;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class SceneEditorController {

    private JmeSpatial jmeRootNode;
    private JmeSpatial selectedSpat;
    private FileObject currentFileObject;

    public SceneEditorController(JmeSpatial jmeRootNode, FileObject currentFileObject) {
        this.jmeRootNode = jmeRootNode;
        this.currentFileObject = currentFileObject;
    }

    public JmeSpatial getJmeRootNode() {
        return jmeRootNode;
    }

    public JmeSpatial getSelectedSpat() {
        return selectedSpat;
    }

    public void setSelectedSpat(JmeSpatial selectedSpat) {
        this.selectedSpat = selectedSpat;
    }

    public FileObject getCurrentFileObject() {
        return currentFileObject;
    }

    public void addSpatial(final String name) {
        addSpatial(name, new Vector3f(0, 0, 0));
    }

    public void addSpatial(final String name, final Vector3f point) {
        if (selectedSpat == null) {
            return;
        }
        try {
            selectedSpat.fireSave(true);
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doAddSpatial(node, name, point);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doAddSpatial(Spatial selected, String name, Vector3f point) {
        if (selected instanceof Node) {
            if ("Node".equals(name)) {
                ((Node) selected).attachChild(new Node("Node"));
            } else if ("Particle Emitter".equals(name)) {
                ParticleEmitter emit = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 200);
                emit.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
                emit.setGravity(0);
                emit.setLowLife(5);
                emit.setHighLife(10);
                emit.setStartVel(new Vector3f(0, 0, 0));
                emit.setImagesX(15);
                Material mat = new Material(SceneApplication.getApplication().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
                //                    mat.setTexture("m_Texture", SceneApplication.getApplication().getAssetManager().loadTexture("Effects/Smoke/Smoke.png"));
                emit.setMaterial(mat);
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    emit.setLocalTranslation(localVec);
                }
                ((Node) selected).attachChild(emit);
                refreshSelected();
            } else if ("Audio Node".equals(name)) {
                AudioNode node = new AudioNode();
                node.setName("Audio Node");
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    node.setLocalTranslation(localVec);
                }
                ((Node) selected).attachChild(node);
                refreshSelected();
            } else if ("Picture".equals(name)) {
                Picture pic = new Picture("Picture");
                Material mat = new Material(SceneApplication.getApplication().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
                pic.setMaterial(mat);
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    pic.setLocalTranslation(localVec);
                }
                ((Node) selected).attachChild(pic);
                refreshSelected();
            } else if ("Point Light".equals(name)) {
                PointLight light = new PointLight();
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    light.setPosition(localVec);
                }
                light.setColor(ColorRGBA.White);
                ((Node) selected).addLight(light);
                refreshSelected();
            } else if ("Directional Light".equals(name)) {
                DirectionalLight dl = new DirectionalLight();
                dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
                dl.setColor(ColorRGBA.Green);
                ((Node) selected).addLight(dl);
                refreshSelected();
            } else if ("Node".equals(name)) {
                Node node = new Node("Node");
                ((Node) selected).attachChild(node);
                refreshSelected();
            }
        } else if (selected instanceof Geometry) {
            if ("Point Light".equals(name)) {
                PointLight light = new PointLight();
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    light.setPosition(localVec);
                }
                light.setColor(ColorRGBA.White);
                selected.addLight(light);
                refreshSelected();
            } else if ("Directional Light".equals(name)) {
                DirectionalLight dl = new DirectionalLight();
                dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
                dl.setColor(ColorRGBA.Green);
                selected.addLight(dl);
                refreshSelected();
            }
        }
    }

    public void moveSelectedSpatial(final Vector3f point) {
        if (selectedSpat == null) {
            return;
        }
        try {
//            selectedSpat.fireSave(true);
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doMoveSpatial(node, point);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doMoveSpatial(Spatial selected, Vector3f translation) {
        Vector3f localTranslation = selected.getLocalTranslation();
        Node parent = selected.getParent();
        if (parent != null) {
            localTranslation.set(translation).subtractLocal(parent.getWorldTranslation());
            localTranslation.divideLocal(parent.getWorldScale());
            //TODO: reuse quaternion..
            new Quaternion().set(parent.getWorldRotation()).inverseLocal().multLocal(localTranslation);
        } else {
            localTranslation.set(translation);
        }
        selected.setLocalTranslation(localTranslation);
    }

    public void createTangentsForSelectedSpatial() {
        if (selectedSpat == null) {
            return;
        }
        try {
//            selectedSpat.fireSave(true);
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doCreateTangents(node);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doCreateTangents(Spatial selected) {
        if (selected instanceof Geometry) {
            Geometry geom = (Geometry) selected;
            Mesh mesh = geom.getMesh();
            if (mesh != null) {
                TangentBinormalGenerator.generate(mesh);
            }
        }
    }

    public void createPhysicsMeshForSelectedSpatial() {
        if (selectedSpat == null) {
            return;
        }
        if (selectedSpat != jmeRootNode) {
            try {
//                selectedSpat.fireSave(true);
                final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
                if (node != null) {
                    SceneApplication.getApplication().enqueue(new Callable() {

                        public Object call() throws Exception {
                            doCreatePhysicsMesh(node);
                            return null;

                        }
                    }).get();
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void doCreatePhysicsMesh(Spatial selected) {
        Node parent = selected.getParent();
        if (selected instanceof PhysicsCollisionObject) {
            PhysicsCollisionObject collObj = (PhysicsCollisionObject) selected;
            collObj.removeFromParent();
            collObj.setCollisionShape(CollisionShapeFactory.createMeshShape(selected));
            if (parent != null) {
                parent.attachChild(selected);
            }
            return;
        }
        selected.removeFromParent();
        PhysicsNode node = new PhysicsNode(selected, CollisionShapeFactory.createMeshShape(selected), 0);
        node.setName(selected.getName() + "-PhysicsNode");
        if (parent != null) {
            parent.attachChild(node);
        }
        refreshSelectedParent();
    }

    public void addModel(final AssetManager manager, final String assetName) {
        if (selectedSpat == null) {
            return;
        }
        final Node selected = selectedSpat.getLookup().lookup(Node.class);
        if (selected != null) {
            SceneApplication.getApplication().enqueue(new Callable<Object>() {

                public Object call() throws Exception {
                    doAddModel(manager, assetName, selected);
                    return null;
                }
            });
        }
    }

    public void doAddModel(AssetManager manager, String assetName, Node selected) {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Adding Model..");
        progressHandle.start();
        try {
            if (selected instanceof AssetLinkNode) {
                AssetLinkNode linkNode = (AssetLinkNode) selected;
                linkNode.attachLinkedChild(manager, new AssetKey<Spatial>(assetName));
            } else {
                ((DesktopAssetManager) manager).clearCache();
                AssetKey<Spatial> key = new AssetKey<Spatial>(assetName);
                AssetLinkNode linkNode = new AssetLinkNode(key);
                linkNode.attachLinkedChildren(manager);
                selected.attachChild(linkNode);
            }
            refreshSelected();
        } catch (Exception ex) {
            Confirmation msg = new NotifyDescriptor.Confirmation(
                    "Error importing " + assetName + "\n" + ex.toString(),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
        }
        progressHandle.finish();

    }

    public void saveScene() {
        final Node node = jmeRootNode.getLookup().lookup(Node.class);
        final FileObject file = currentFileObject;
        SceneApplication.getApplication().enqueue(new Callable() {

            public Object call() throws Exception {
                doSaveScene(node, file);
                return null;
            }
        });
    }

    public void doSaveScene(Node node, FileObject currentFileObject) {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Saving File..");
        progressHandle.start();
        BinaryExporter exp = BinaryExporter.getInstance();
        try {
            exp.save(node, FileUtil.toFile(currentFileObject));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        progressHandle.finish();
        StatusDisplayer.getDefault().setStatusText(currentFileObject.getNameExt() + " saved.");
        //try make NetBeans update the tree.. :/

    }

    private void refreshSelected(final JmeSpatial spat) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (spat != null) {
                    spat.refresh(false);
                }
            }
        });

    }

    private void refreshSelected() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getSelectedSpat() != null) {
                    getSelectedSpat().refresh(false);
                }
            }
        });

    }

    private void refreshSelectedParent() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getSelectedSpat() != null) {
                    ((JmeSpatial) getSelectedSpat().getParentNode()).refresh(false);
                }
            }
        });

    }

    private void refreshRoot() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getJmeRootNode() != null) {
                    getJmeRootNode().refresh(true);
                }
            }
        });

    }

    public void cleanup() {
        final Node node = jmeRootNode.getLookup().lookup(Node.class);
        SceneApplication.getApplication().enqueue(new Callable() {

            public Object call() throws Exception {
                doCleanup(node);
                return null;
            }
        });
    }

    public void doCleanup(Node node) {
        node.removeFromParent();
    }
}
