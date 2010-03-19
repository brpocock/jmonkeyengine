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
package com.jme3.bullet.nodes.infos;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.Converter;

/**
 * stores all info about a bullet physics object in a thread-safe manner to
 * allow access from the jme scenegraph and the bullet physicsspace<br>
 * @author normenhansen
 */
public class PhysicsNodeState implements MotionState{
    //stores the bullet transform
    private Transform motionStateTrans=new Transform();
    
    //stores jme transform info
    private Vector3f worldLocation=new Vector3f();
    private Matrix3f worldRotation=new Matrix3f();
    private Quaternion worldRotationQuat=new Quaternion();
    //keep track of transform changes
    private boolean physicsLocationDirty=false;
    private boolean jmeLocationDirty=true;

    //temp variable for conversion
    private Quaternion tmp_inverseWorldRotation=new Quaternion();

    private PhysicsNode pNode;

    public PhysicsNodeState(PhysicsNode pNode) {
        this.pNode = pNode;
    }

    public synchronized Transform getWorldTransform(Transform t) {
        t.set(motionStateTrans);
        return t;
    }

    public synchronized void setWorldTransform(Transform worldTrans) {
        if(jmeLocationDirty) return;
        motionStateTrans.set(worldTrans);
        Converter.convert(worldTrans.origin, worldLocation);
        Converter.convert(worldTrans.basis, worldRotation);
        worldRotationQuat.fromRotationMatrix(worldRotation);
        physicsLocationDirty=true;
    }

    public synchronized void applyTransform(RigidBody rBody) {
        if(!jmeLocationDirty) return;
        assert(rBody!=null);
        rBody.setWorldTransform(motionStateTrans);
        jmeLocationDirty=false;
    }

    public synchronized void applyTransform(Spatial spatial){
        if(!physicsLocationDirty) return;
        if(spatial.getParent()!=null){
            Spatial parent=spatial.getParent();
            Vector3f location=spatial.getLocalTranslation();
            Quaternion rotation=spatial.getLocalRotation();
            location.set(worldLocation);
            rotation.set(worldRotationQuat);
            location.subtractLocal(parent.getWorldTranslation());
            location.divideLocal( parent.getWorldScale() );
            tmp_inverseWorldRotation.set( parent.getWorldRotation()).inverseLocal().multLocal( location );
            tmp_inverseWorldRotation.set( parent.getWorldRotation()).inverseLocal().mult( worldRotationQuat, rotation );
        }
        else{
            Vector3f location=spatial.getLocalTranslation();
            Quaternion rotation=spatial.getLocalRotation();
            location.set(worldLocation);
            rotation.set(worldRotationQuat);
        }
        spatial.setLocalTranslation(spatial.getLocalTranslation());
        spatial.setLocalRotation(spatial.getLocalRotation());
        physicsLocationDirty=false;
        return;
    }

    public synchronized void setWorldTransform(Vector3f location, Quaternion rotation){
        worldLocation.set(location);
        worldRotationQuat.set(rotation);
        worldRotation.set(rotation.toRotationMatrix());
        Converter.convert(worldLocation,motionStateTrans.origin);
        Converter.convert(worldRotation,motionStateTrans.basis);
        jmeLocationDirty=true;
    }

}
