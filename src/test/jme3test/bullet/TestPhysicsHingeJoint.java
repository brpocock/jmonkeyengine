package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.joints.PhysicsHingeJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class TestPhysicsHingeJoint extends SimpleBulletApplication implements BindingListener {
    private PhysicsHingeJoint joint;

    public static void main(String[] args) {
        TestPhysicsHingeJoint app = new TestPhysicsHingeJoint();
        app.start();
    }

    private void setupKeys() {
        inputManager.registerKeyBinding("Lefts", KeyInput.KEY_H);
        inputManager.registerKeyBinding("Rights", KeyInput.KEY_K);
        inputManager.registerKeyBinding("Space", KeyInput.KEY_SPACE);
        //used with method onBinding in BindingListener interface
        //in order to add function to keys
        inputManager.addBindingListener(this);
    }

    public void onBinding(String binding, float value) {
        if(binding.equals("Lefts")){
            joint.enableMotor(true, 1, .1f);
        }
        else if(binding.equals("Rights")){
            joint.enableMotor(true, -1, .1f);
        }
        else if(binding.equals("Space")){
            joint.enableMotor(false, 0, 0);
        }
    }

    public void onPreUpdate(float tpf) {
    }

    public void onPostUpdate(float tpf) {
    }

    @Override
    public void simpleInitApp() {
        setupKeys();
        setupJoint();
    }

    public void setupJoint() {

        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Yellow);

        PhysicsNode holderNode=new PhysicsNode(new BoxCollisionShape(new Vector3f( .1f, .1f, .1f)),0);
        holderNode.setLocalTranslation(new Vector3f(0f,0,0f));
        holderNode.attachDebugShape(mat);
        holderNode.updateGeometricState();
        rootNode.attachChild(holderNode);
        getPhysicsSpace().add(holderNode);

        PhysicsNode hammerNode=new PhysicsNode(new BoxCollisionShape(new Vector3f( .3f, .3f, .3f)),1);
        hammerNode.setLocalTranslation(new Vector3f(0f,-1,0f));
        hammerNode.attachDebugShape(assetManager);
        hammerNode.updateGeometricState();
        rootNode.attachChild(hammerNode);
        getPhysicsSpace().add(hammerNode);

        joint=new PhysicsHingeJoint(holderNode, hammerNode, Vector3f.ZERO, new Vector3f(0f,-1,0f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
        getPhysicsSpace().add(joint);
    }

    @Override
    public void simpleUpdate(float tpf) {
        
    }


}