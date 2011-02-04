/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.bullet.control;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author normenhansen
 */
public class RigidBodyControl extends PhysicsRigidBody implements PhysicsControl {

    protected Spatial spatial;
    protected boolean enabled = true;
    protected boolean added = false;
    protected PhysicsSpace space = null;
    private Matrix3f temp_matrix = new Matrix3f();
    protected boolean kinematicSpatial = true;

    public RigidBodyControl() {
    }

    /**
     * When using this constructor, the CollisionShape for the RigidBody is generated
     * automatically when the Control is added to a Spatial.
     * @param mass When not 0, a HullCollisionShape is generated, otherwise a MeshCollisionShape is used. For geometries with box or sphere meshes the proper box or sphere collision shape is used.
     */
    public RigidBodyControl(float mass) {
        this.mass = mass;
    }

    /**
     * Creates a new PhysicsNode with the supplied collision shape
     * @param child
     * @param shape
     */
    public RigidBodyControl(CollisionShape shape) {
        super(shape);
    }

    public RigidBodyControl(CollisionShape shape, float mass) {
        super(shape, mass);
    }

    public Control cloneForSpatial(Spatial spatial) {
        RigidBodyControl control = new RigidBodyControl(collisionShape, mass);
        control.setAngularFactor(getAngularFactor());
        control.setAngularSleepingThreshold(getAngularSleepingThreshold());
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setDamping(getLinearDamping(), getAngularDamping());
        control.setFriction(getFriction());
        control.setGravity(getGravity());
        control.setKinematic(isKinematic());
        control.setKinematicSpatial(isKinematicSpatial());
        control.setLinearSleepingThreshold(getLinearSleepingThreshold());
        control.setPhysicsLocation(getPhysicsLocation(null));
        control.setPhysicsRotation(getPhysicsRotation(null));
        control.setRestitution(getRestitution());

        if (mass > 0) {
            control.setAngularVelocity(getAngularVelocity());
            control.setLinearVelocity(getLinearVelocity());
        }

        control.setSpatial(spatial);
        return control;
    }

    public void setSpatial(Spatial spatial) {
        if (getUserObject() == null || getUserObject() == this.spatial) {
            setUserObject(spatial);
        }
        this.spatial = spatial;
        if (spatial == null) {
            if (getUserObject() == spatial) {
                setUserObject(null);
            }
            spatial = null;
            collisionShape = null;
            return;
        }
        if (collisionShape == null) {
            createCollisionShape();
            rebuildRigidBody();
        }
        setPhysicsLocation(spatial.getWorldTranslation());
        setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix());
    }

    protected void createCollisionShape() {
        if (spatial == null) {
            return;
        }
        if (spatial instanceof Geometry) {
            Geometry geom = (Geometry) spatial;
            Mesh mesh = geom.getMesh();
            if (mesh instanceof Sphere) {
                collisionShape = new SphereCollisionShape(((Sphere) mesh).getRadius());
                return;
            } else if (mesh instanceof Box) {
                collisionShape = new BoxCollisionShape(new Vector3f(((Box) mesh).getXExtent(), ((Box) mesh).getYExtent(), ((Box) mesh).getZExtent()));
                return;
            }
        }
        if (mass > 0) {
            Node parent = spatial.getParent();
            if (parent != null) {
                spatial.removeFromParent();
            }
            collisionShape = CollisionShapeFactory.createDynamicMeshShape(spatial);
            if (parent != null) {
                parent.attachChild(spatial);
            }
        } else {
            Node parent = spatial.getParent();
            if (parent != null) {
                spatial.removeFromParent();
            }
            collisionShape = CollisionShapeFactory.createMeshShape(spatial);
            if (parent != null) {
                parent.attachChild(spatial);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (space != null) {
            if (enabled && !added) {
                if (spatial != null) {
                    setPhysicsLocation(spatial.getWorldTranslation());
                    setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix(temp_matrix));
                }
                space.addCollisionObject(this);
                added = true;
            } else if (!enabled && added) {
                space.removeCollisionObject(this);
                added = false;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setPhysicsLocation(Vector3f location) {
        super.setPhysicsLocation(location);
        if (kinematicSpatial) {
            Logger.getLogger(RigidBodyControl.class.getName()).log(Level.WARNING, "When using setPhysicsLocation in kinematic mode call rigidBodyControl.setKinematicSpatial(false)!");
        }
    }

    @Override
    public void setPhysicsRotation(Matrix3f rotation) {
        super.setPhysicsRotation(rotation);
        if (kinematicSpatial) {
            Logger.getLogger(RigidBodyControl.class.getName()).log(Level.WARNING, "When using setPhysicsRotation in kinematic mode call rigidBodyControl.setKinematicSpatial(false)!");
        }
    }

    @Override
    public void setKinematic(boolean kinematic) {
        super.setKinematic(kinematic);
    }

    /**
     * Checks if this control is in kinematic spatial mode.
     * @return true if the spatial location is applied to this kinematic rigidbody
     */
    public boolean isKinematicSpatial() {
        return kinematicSpatial;
    }

    /**
     * Sets this control to kinematic spatial mode so that the spatials transform will
     * be applied to the rigidbody in kinematic mode, defaults to true.
     * @param kinematicSpatial
     */
    public void setKinematicSpatial(boolean kinematicSpatial) {
        this.kinematicSpatial = kinematicSpatial;
    }

    public void update(float tpf) {
        if (enabled && spatial != null) {
            if (isKinematic() && kinematicSpatial) {
                super.setPhysicsLocation(spatial.getWorldTranslation());
                super.setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix(temp_matrix));
            } else {
                getMotionState().applyTransform(spatial);
            }
        }
    }

    public void render(RenderManager rm, ViewPort vp) {
        if (debugShape != null && enabled) {
            debugShape.setLocalTranslation(motionState.getWorldLocation());
            debugShape.setLocalRotation(motionState.getWorldRotation());
            debugShape.updateLogicalState(0);
            debugShape.updateGeometricState();
            rm.renderScene(debugShape, vp);
        }
    }

    public void setPhysicsSpace(PhysicsSpace space) {
        if (space == null) {
            if (this.space != null) {
                this.space.removeCollisionObject(this);
                added = false;
            }
        } else {
            space.addCollisionObject(this);
            added = true;
        }
        this.space = space;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(enabled, "enabled", true);
        oc.write(kinematicSpatial, "kinematicSpatial", true);
        oc.write(spatial, "spatial", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        enabled = ic.readBoolean("enabled", true);
        kinematicSpatial = ic.readBoolean("kinematicSpatial", true);
        spatial = (Spatial) ic.readSavable("spatial", null);
    }
}
