package jme3test.scene;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.FirstPersonCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.MeshLoader;
import com.jme3.scene.plugins.ogre.OgreMaterialList;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import com.jme3.scene.shape.Sphere;
import java.io.File;
import javax.swing.JOptionPane;

public class TestQ3 extends SimpleBulletApplication implements BindingListener{

    private Sphere sphereMesh = new Sphere(32, 32, 10f, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);
    private Spatial gameLevel;
    private PhysicsCharacterNode player;
    private Vector3f walkDirection=new Vector3f();

    public static void main(String[] args){
        File file=new File("quake3level.zip");
        if(!file.exists()){
            JOptionPane.showMessageDialog(null, "Error opening quake3level.zip\nPlease download at:\nhttp://jmonkeyengine.googlecode.com/svn/branches/jme3/quake3level.zip");
            return;
        }
        TestQ3 app = new TestQ3();
        app.start();
    }

    public void simpleInitApp() {
        inputManager.removeBindingListener(flyCam);
        setupKeys();
        MeshLoader.AUTO_INTERLEAVE = false;
        FirstPersonCamera fps = new FirstPersonCamera(cam, new Vector3f(0, -10, 0));
        fps.registerWithDispatcher(inputManager);
        fps.setMoveSpeed(100);

        this.cam.setFrustumFar(2000);

        // load sky
//        sphere.updateModelBound();
//        sphere.setQueueBucket(Bucket.Sky);
//        Material sky = new Material(manager, "sky.j3md");
//        Texture tex = manager.loadTexture("sky3.dds", false, true, true, 0);
//        sky.setTexture("m_Texture", tex);
//        sphere.setMaterial(sky);
//        rootNode.attachChild(sphere);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(-1, -1, -1).normalize());
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(1, -1, 1).normalize());
        rootNode.addLight(dl);
        // create the geometry and attach it
        assetManager.registerLocator("quake3level.zip", ZipLocator.class.getName());

        // create the geometry and attach it
        OgreMaterialList matList = (OgreMaterialList) assetManager.loadAsset("Scene.material");
        OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
        gameLevel = (Spatial) assetManager.loadAsset(key);
        gameLevel.updateGeometricState();

        CompoundCollisionShape levelShape=CollisionShapeFactory.createMeshCompoundShape((Node)gameLevel);

        //TODO: values are too high, level is too large,
        //      scaling not supported yet with CollisionShapeFactory.createMeshCompoundShape
        PhysicsNode levelNode=new PhysicsNode(gameLevel, levelShape,0);
        player=new PhysicsCharacterNode(new SphereCollisionShape(60), 0.1f);
        player.setGravity(100);

        player.setLocalTranslation(new Vector3f(600,100,-600));
        player.updateGeometricState();

        rootNode.attachChild(levelNode);
        rootNode.attachChild(player);
        rootNode.updateGeometricState();

        getPhysicsSpace().add(levelNode);
        getPhysicsSpace().add(player);

//        MotionAllowedListener motAllow = new SphereMotionAllowedListener(rootNode, new Vector3f(100, 200, 100));
//        fps.setMotionAllowedListener(motAllow);
    }

    @Override
    public void simpleUpdate(float tpf){
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getLocalTranslation());
    }

    private void setupKeys() {
        inputManager.registerKeyBinding("Lefts", KeyInput.KEY_H);
        inputManager.registerKeyBinding("Rights", KeyInput.KEY_K);
        inputManager.registerKeyBinding("Ups", KeyInput.KEY_U);
        inputManager.registerKeyBinding("Downs", KeyInput.KEY_J);
        inputManager.registerKeyBinding("Space", KeyInput.KEY_SPACE);
        //used with method onBinding in BindingListener interface
        //in order to add function to keys
        inputManager.addBindingListener(this);
    }

    public void onBinding(String binding, float value) {
        if(binding.equals("Lefts")){
            Quaternion quat=new Quaternion(0, 1, 0, FastMath.QUARTER_PI);
            walkDirection.addLocal(quat.mult(cam.getDirection().mult(2)));
        }
        else if(binding.equals("Rights")){
            Quaternion quat=new Quaternion(0, 1, 0, -FastMath.QUARTER_PI);
            walkDirection.addLocal(quat.mult(cam.getDirection().mult(2)));
        }
        else if(binding.equals("Ups")){
            walkDirection.addLocal(cam.getDirection().mult(2));
        }
        else if(binding.equals("Downs")){
            walkDirection.addLocal(cam.getDirection().mult(-2));
        }
        else if(binding.equals("Space")){
            player.jump();
        }
    }

    public void onPreUpdate(float tpf) {
        walkDirection.set(0,0,0);
    }

    public void onPostUpdate(float tpf) {
    }
}
