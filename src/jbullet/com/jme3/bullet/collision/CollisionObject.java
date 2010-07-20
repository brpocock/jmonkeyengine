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
package com.jme3.bullet.collision;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape.ChildCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for collision objects (PhysicsNode, PhysicsGhostNode)
 * @author normenhansen
 */
public abstract class CollisionObject extends Node {

    protected Spatial debugShape;
    protected CollisionShape collisionShape;
    public static final int COLLISION_GROUP_NONE = 0x00000000;
    public static final int COLLISION_GROUP_01 = 0x00000001;
    public static final int COLLISION_GROUP_02 = 0x00000002;
    public static final int COLLISION_GROUP_03 = 0x00000004;
    public static final int COLLISION_GROUP_04 = 0x00000008;
    public static final int COLLISION_GROUP_05 = 0x00000010;
    public static final int COLLISION_GROUP_06 = 0x00000020;
    public static final int COLLISION_GROUP_07 = 0x00000040;
    public static final int COLLISION_GROUP_08 = 0x00000080;
    public static final int COLLISION_GROUP_09 = 0x00000100;
    public static final int COLLISION_GROUP_10 = 0x00000200;
    public static final int COLLISION_GROUP_11 = 0x00000400;
    public static final int COLLISION_GROUP_12 = 0x00000800;
    public static final int COLLISION_GROUP_13 = 0x00001000;
    public static final int COLLISION_GROUP_14 = 0x00002000;
    public static final int COLLISION_GROUP_15 = 0x00004000;
    public static final int COLLISION_GROUP_16 = 0x00008000;
    protected int collisionGroup = 0x00000001;
    protected int collisionGroupsMask = 0x00000001;

    @Override
    public void updateGeometricState() {
        //important! nothing happening here, not calling Node.updateGeometric!
    }

    public void updatePhysicsState() {
    }

    public void setCollisionShape(CollisionShape collisionShape) {
        this.collisionShape = collisionShape;
        if (debugShape != null) {
            detachDebugShape();
        }
    }

    /**
     * @return the collisionGroup
     */
    public int getCollisionGroup() {
        return collisionGroup;
    }

    /**
     * Sets the collision group number for this physics object. <br>
     * The groups are integer bit masks and some pre-made variables are available in CollisionObject.
     * All physics objects are by default in COLLISION_GROUP_01.
     * @param collisionGroup the collisionGroup to set
     */
    public void setCollisionGroup(int collisionGroup) {
        this.collisionGroup = collisionGroup;
    }

    /**
     * Add a group that this object will collide with.
     * @param collisionGroup
     */
    public void addCollideWithGroup(int collisionGroup) {
        this.collisionGroupsMask = this.collisionGroupsMask | collisionGroup;
    }

    /**
     * Remove a group from the list this object collides with.
     * @param collisionGroup
     */
    public void removeCollideWithGroup(int collisionGroup) {
        this.collisionGroupsMask = this.collisionGroupsMask & ~collisionGroup;
    }

    /**
     * Directly set the bitmask for collision groups that this object collides with.
     * @param collisionGroup
     */
    public void setCollideWithGroups(int collisionGroup) {
        this.collisionGroupsMask = collisionGroup;
    }

    /**
     * Gets the bitmask of collision groups that this object collides with.
     * @return
     */
    public int getCollideWithGroups() {
        return collisionGroupsMask;
    }

    /**
     * Should only be called from updateGeometricState().
     * In most cases should not be subclassed.
     */
    protected void updateWorldTransforms() {
        super.updateWorldTransforms();
        //hack to avoid scaling of PhysicsNodes thru parent..
        if (parent != null) {
            worldTransform.setScale(localTransform.getScale());
        }
    }

    /**
     * WARNING: Physics objects cannot be scaled, scale the collision shape and/or attached geometry!<br>
     * The CollisionShape might be used by another physics object, so scaling is disabled.
     */
    @Override
    public void setLocalScale(Vector3f localScale) {
        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "PhysicsNodes cannot be scaled");
    }

    /**
     * WARNING: Physics objects cannot be scaled, scale the collision shape and/or attached geometry!<br>
     * The CollisionShape might be used by another physics object, so scaling is disabled.
     */
    @Override
    public void setLocalScale(float localScale) {
        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "PhysicsNodes cannot be scaled");
    }

    /**
     * WARNING: Physics objects cannot be scaled, scale the collision shape and/or attached geometry!<br>
     * The CollisionShape might be used by another physics object, so scaling is disabled.
     */
    @Override
    public void setLocalScale(float x, float y, float z) {
        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "PhysicsNodes cannot be scaled");
    }

    /**
     * Attaches a visual debug shape of the current collision shape to this physics object
     * @param manager AssetManager to load the default wireframe material for the debug shape
     */
    public void attachDebugShape(AssetManager manager) {
        Material mat = new Material(manager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Blue);
        attachDebugShape(mat);
    }

    /**
     * Attaches a visual debug shape of the current collision shape to this physics object
     * @param material Material to use for the debug shape
     */
    public Spatial attachDebugShape(Material material) {
        if (debugShape != null) {
            detachDebugShape();
        }

        Spatial spatial = getDebugShape();
        if (spatial == null) {
            return null;
        }
        if (spatial instanceof Node) {
            List<Spatial> children = ((Node) spatial).getChildren();
            for (Iterator<Spatial> it1 = children.iterator(); it1.hasNext();) {
                Spatial spatial1 = it1.next();
                Geometry geom = ((Geometry) spatial1);
                geom.setMaterial(material);
            }
        } else {
            Geometry geom = ((Geometry) spatial);
            geom.setMaterial(material);

        }
        this.attachChild(spatial);
        this.debugShape = spatial;
        return spatial;
    }

    /**
     * Detaches the debug shape
     */
    public void detachDebugShape() {
        this.detachChild(debugShape);
        debugShape = null;
    }

    protected Spatial getDebugShape() {
        if (collisionShape == null) {
            return null;
        }
        Spatial debugShape;
        if (collisionShape instanceof CompoundCollisionShape) {
            CompoundCollisionShape shape = (CompoundCollisionShape) collisionShape;
            List<ChildCollisionShape> children = shape.getChildren();
            Node node = new Node("DebugShapeNode");
            for (Iterator<ChildCollisionShape> it = children.iterator(); it.hasNext();) {
                ChildCollisionShape childCollisionShape = it.next();
                CollisionShape collisionShape = childCollisionShape.shape;
                Geometry geometry = createDebugShape(collisionShape);
                geometry.setLocalTranslation(childCollisionShape.location);
                geometry.setLocalRotation(childCollisionShape.rotation);
                node.attachChild(geometry);
            }
            debugShape = node;
        } else {
            debugShape = createDebugShape(collisionShape);
        }
        if (debugShape == null) {
            return null;
        }
        debugShape.updateGeometricState();
        return debugShape;
    }

    protected Geometry createDebugShape(CollisionShape shape) {
        Geometry geom = new Geometry();
        if (shape instanceof BoxCollisionShape) {
            geom.setName("BoxDebugShape");
            BoxCollisionShape boxCollisionShape = (BoxCollisionShape) shape;
            final Vector3f halfExtents = boxCollisionShape.getHalfExtents();
            Vector3f scale = boxCollisionShape.getScale();
            Box box = new Box(halfExtents.negate(), halfExtents);
            geom.setMesh(box);
            geom.setLocalScale(scale);
        } else if (shape instanceof SphereCollisionShape) {
            geom.setName("SphereDebugShape");
            SphereCollisionShape sphereCollisionShape = (SphereCollisionShape) shape;
            float radius = sphereCollisionShape.getRadius();
            Sphere sphere = new Sphere(16, 16, radius);
            Vector3f scale = sphereCollisionShape.getScale();
            geom.setMesh(sphere);
            geom.setLocalScale(scale);
        } else if (shape instanceof MeshCollisionShape) {
            geom.setName("MeshDebugShape");
            MeshCollisionShape meshCollisionShape = (MeshCollisionShape) shape;
            Mesh mesh = meshCollisionShape.createJmeMesh();
            Vector3f scale = meshCollisionShape.getScale();
            geom.setMesh(mesh);
            geom.setLocalScale(scale);
        } else if (shape instanceof GImpactCollisionShape) {
            geom.setName("GImpactDebugShape");
            GImpactCollisionShape meshCollisionShape = (GImpactCollisionShape) shape;
            Mesh mesh = meshCollisionShape.createJmeMesh();
            Vector3f scale = meshCollisionShape.getScale();
            geom.setMesh(mesh);
            geom.setLocalScale(scale);
        } else if (shape instanceof CylinderCollisionShape) {
            geom.setName("CylinderDebugShape");
            CylinderCollisionShape cylinderCollisionShape = (CylinderCollisionShape) shape;
            Vector3f scale = cylinderCollisionShape.getScale();
            Vector3f halfExtents = cylinderCollisionShape.getHalfExtents();
            int axis = cylinderCollisionShape.getAxis();
            Mesh cylinder = null;
            //TODO: bestter debug shape for cylinder
            switch (axis) {
                case 0:
                    cylinder = new Box(Vector3f.ZERO, halfExtents.x, halfExtents.y, halfExtents.z);
                    break;
                case 1:
                    cylinder = new Box(Vector3f.ZERO, halfExtents.x, halfExtents.y, halfExtents.z);
                    break;
                case 2:
                    cylinder = new Cylinder(16, 16, halfExtents.x, halfExtents.z * 2);
                    break;

            }
            geom.setMesh(cylinder);
            geom.setLocalScale(scale);

        } else if (shape instanceof CapsuleCollisionShape) {
            geom.setName("CapsuleDebugShape");
            CapsuleCollisionShape capsuleCollisionShape = (CapsuleCollisionShape) shape;
            Vector3f scale = capsuleCollisionShape.getScale();
            int axis = capsuleCollisionShape.getAxis();
            float height = capsuleCollisionShape.getHeight();
            float radius = capsuleCollisionShape.getRadius();
            Mesh cylinder = null;
            //TODO: better debug shape for capsule
            switch (axis) {
                case 0:
                    cylinder = new Box(Vector3f.ZERO, (height / 2.0f) + radius, radius, radius);
                    break;
                case 1:
                    cylinder = new Box(Vector3f.ZERO, radius, (height / 2.0f) + radius, radius);
                    break;
                case 2:
                    cylinder = new Cylinder(16, 16, radius, height + (radius*2));
                    break;
            }

            geom.setMesh(cylinder);
            geom.setLocalScale(scale);
        }
        return geom;
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);
        capsule.write(collisionGroup, "collisionGroup", 0x00000001);
        capsule.write(collisionGroupsMask, "collisionGroupsMask", 0x00000001);

    }

    @Override
    public void read(JmeImporter e) throws IOException {
        super.read(e);
        InputCapsule capsule = e.getCapsule(this);
        collisionGroup = capsule.readInt("collisionGroup", 0x00000001);
        collisionGroupsMask = capsule.readInt("collisionGroupsMask", 0x00000001);
    }
}
