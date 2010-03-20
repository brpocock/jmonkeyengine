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
package com.jme3.bullet.collision.shapes;

import com.bulletphysics.collision.shapes.BoxShape;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.bullet.util.Converter;
import java.util.List;

/**
 * Basic box collision shape
 * @author normenhansen
 */
public class BoxCollisionShape extends CollisionShape{

    /**
     * creates a collision box from the given halfExtents
     * @param halfExtents the halfExtents of the CollisionBox
     */
    public BoxCollisionShape(Vector3f halfExtents) {
        BoxShape sphere=new BoxShape(Converter.convert(halfExtents));
        cShape=sphere;
    }

    /**
     * creates a box in the physics space that represents this Node and all
     * children. The extents are computed from the world bound of this Node.
     */
    private void createCollisionBox(Node node) {
        List<Spatial> children=node.getChildren();
        if(children.size()==0){
            throw (new UnsupportedOperationException("PhysicsNode has no children, cannot compute collision box"));
        }
        if(!(node.getWorldBound() instanceof BoundingBox)){
            node.setModelBound(new BoundingBox());
        }
        node.updateModelBound();
        node.updateGeometricState();
        BoundingBox volume=(BoundingBox)node.getWorldBound();
        createCollisionBox(volume);
    }

    private void createCollisionBox(BoundingBox volume) {
        javax.vecmath.Vector3f halfExtents=new javax.vecmath.Vector3f(
                volume.getXExtent() - volume.getCenter().x,
                volume.getYExtent() - volume.getCenter().y,
                volume.getZExtent() - volume.getCenter().z);
        BoxShape sphere=new BoxShape(halfExtents);
        cShape=sphere;
    }

}
