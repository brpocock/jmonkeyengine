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
package com.jme3.bullet.joints;

import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.jme3.math.Vector3f;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.Converter;

/**
 * <i>From bullet manual:</i><br>
 * Hinge constraint, or revolute joint restricts two additional angular degrees of freedom,
 * so the body can only rotate around one axis, the hinge axis.
 * This can be useful to represent doors or wheels rotating around one axis.
 * The user can specify limits and motor for the hinge.
 * @author normenhansen
 */
public class PhysicsHingeJoint extends PhysicsJoint{
    protected Vector3f axisA;
    protected Vector3f axisB;

    /**
     * Creates a new HingeJoint
     * @param pivotA local translation of the joint connection point in node A
     * @param pivotB local translation of the joint connection point in node A
     */
    public PhysicsHingeJoint(PhysicsNode nodeA, PhysicsNode nodeB, Vector3f pivotA, Vector3f pivotB, Vector3f axisA, Vector3f axisB) {
        super(nodeA, nodeB, pivotA, pivotB);
        this.axisA=axisA;
        this.axisB=axisB;
        constraint=new HingeConstraint(nodeA.getRigidBody(), nodeB.getRigidBody(),
                Converter.convert(pivotA), Converter.convert(pivotB),
                Converter.convert(axisA), Converter.convert(axisB));
    }

    public void enableMotor(boolean enable, float targetVelocity, float maxMotorImpulse){
        ((HingeConstraint)constraint).enableAngularMotor(enable, targetVelocity, maxMotorImpulse);
    }

	public void setLimit(float low, float high) {
        ((HingeConstraint)constraint).setLimit(low,high);
    }

	public void setLimit(float low, float high, float _softness, float _biasFactor, float _relaxationFactor) {
        ((HingeConstraint)constraint).setLimit(low, high, _softness, _biasFactor, _relaxationFactor);
    }

}
