/*
 * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.jme.scene;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;
import com.jme.math.Quaternion;

/**
 * <code>BillboardNode</code> defines a node that always orients towards the camera. However, it
 * does not tilt up/down as the camera rises. This keep geometry from appearing to fall over if the camera
 * rises or lowers. <code>BillboardNode</code> is useful to contain a single quad that has a image
 * applied to it for lowest detail models. This quad, with the texture, will appear to be a full
 * model at great distances, and save on rendering and memory. It is important to note that the
 * billboards orientation will always be up (0,1,0). This means that a standard camera with up (0,1,0)
 * is the only camera setting compatible with <code>BillboardNode</code>.
 *
 * @author Mark Powell
 * @version $Id: BillboardNode.java,v 1.4 2004-03-31 03:07:12 renanse Exp $
 */
public class BillboardNode extends Node {
  private float lastTime;
  private Matrix3f orient;
  private Vector3f diff;
  private Vector3f loc;
  private int type;

//  public static final int WORLD_ORIENTED = 0;
  public static final int SCREEN_ALIGNED = 1;
  public static final int AXIAL = 2;

  /**
   * Constructor instantiates a new <code>BillboardNode</code>. The name of the node is supplied
   * during construction.
   * @param name the name of the node.
   */
  public BillboardNode(String name) {
    super(name);
    orient = new Matrix3f();
    loc = new Vector3f();
    diff = new Vector3f();
    type = AXIAL;
  }


  /**
   *  <code>updateWorldData</code> defers the updating of the billboards orientation until
   * rendering. This keeps the billboard from being needlessly oriented if the player can
   * not actually see it.
   * @param time the time between frames.
   * @see com.jme.scene.Spatial#updateWorldData(float)
   */
  public void updateWorldData(float time) {
    lastTime = time;
    updateWorldBound();
  }

  /**
   *  <code>draw</code> updates the billboards orientation then renders the billboard's
   * children.
   * @param r the renderer used to draw.
   * @see com.jme.scene.Spatial#draw(com.jme.renderer.Renderer)
   */
  public void draw(Renderer r) {
    Camera cam = r.getCamera();
    rotateBillboard(cam);

    super.draw(r);
  }

  public void rotateBillboard(Camera cam) {
    //get the scale, translation and rotation of the node in world space
    if (parent != null) {
      worldScale = parent.getWorldScale() * localScale;
      parent.getWorldRotation().mult(localRotation, worldRotation);
      worldTranslation = parent.getWorldRotation().mult(localTranslation,
          worldTranslation)
          .multLocal(parent.getWorldScale())
          .addLocal(parent.getWorldTranslation());
    } else {
      worldScale = localScale;
      worldRotation = localRotation;
      worldTranslation = localTranslation;
    }

    switch (type) {
      case AXIAL:
        rotateAxial(cam);
        break;
      case SCREEN_ALIGNED:
        rotateScreenAligned(cam);
        break;
//      case WORLD_ORIENTED:
//        rotateWorldAligned(cam);
//        break;
    }

    for (int i = 0, cSize = children.size(); i < cSize; i++) {
      Spatial child = (Spatial) children.get(i);
      if (child != null) {
        child.updateGeometricState(lastTime, false);
      }
    }
  }
//
//  /**
//   * rotateWorldAligned
//   *
//   * @param camera Camera
//   */
//  private void rotateWorldAligned(Camera camera) {
//
//  }


  /**
   * rotateScreenAligned
   *
   * @param camera Camera
   */
  private void rotateScreenAligned(Camera camera) {
    diff = camera.getDirection().negate();
    orient.fromAxes(camera.getLeft().negate(), camera.getUp(), camera.getDirection().negate());
    worldRotation.fromRotationMatrix(orient);
  }

  /**
   * rotateAxial
   *
   * @param camera Camera
   */
  private void rotateAxial(Camera camera) {

    // Compute the additional rotation required for the billboard to face
    // the camera.  To do this, the camera must be inverse-transformed into
    // the model space of the billboard.
    diff = camera.getLocation().subtract(worldTranslation);
    float invWorldScale = 1.0f / worldScale;
    worldRotation.mult(diff, loc).multLocal(invWorldScale);

    // squared length of the camera projection in the xz-plane
    float lengthSquared = loc.x * loc.x + loc.z * loc.z;
    if (lengthSquared < FastMath.FLT_EPSILON) {
      // camera on the billboard axis, rotation not defined
      return;
    }

    // unitize the projection
    float invLength = FastMath.invSqrt(lengthSquared);
    loc.x *= invLength;
    loc.y = 0.0f;
    loc.z *= invLength;

    // compute the local orientation matrix for the billboard
    orient.m00 = loc.z;
    orient.m01 = 0;
    orient.m02 = loc.x;
    orient.m10 = 0;
    orient.m11 = 1;
    orient.m12 = 0;
    orient.m20 = -loc.x;
    orient.m21 = 0;
    orient.m22 = loc.z;

    // The billboard must be oriented to face the camera before it is
    // transformed into the world.
    worldRotation.apply(orient);

  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}
