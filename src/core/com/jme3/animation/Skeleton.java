/*
 * Copyright (c) 2003-2009 jMonkeyEngine
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

package com.jme3.animation;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Matrix4f;
import com.jme3.util.TempVars;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * A skeleton is a hierarchy of bones.
 * Skeleton updates the world transforms to reflect the current local
 * animated matrixes.
 */
public final class Skeleton implements Savable {

    private Bone[] rootBones;
    private Bone[] boneList;

    /**
     * Contains the skinning matrices, multiplying it by a vertex effected by a bone
     * will cause it to go to the animated position.
     */
    private transient Matrix4f[] skinningMatrixes;

    /**
     * Creates a skeleton from a bone list. The root bone is found automatically.
     * @param boneList
     */
    public Skeleton(Bone[] boneList){
        this.boneList = boneList;

        List<Bone> rootBoneList = new ArrayList<Bone>();
        for (int i = boneList.length - 1; i >= 0; i--){
            Bone b = boneList[i];
            if (b.getParent() == null)
                rootBoneList.add(b);
        }
        rootBones = rootBoneList.toArray(new Bone[rootBoneList.size()]);

        createSkinningMatrices();

        for (int i = rootBones.length - 1; i >= 0; i--){
            Bone rootBone = rootBones[i];
            rootBone.update();
            rootBone.setBindingPose();
        }
    }

    /**
     * Copy constructor.
     * Most of the skeleton data is deeply-copied except the bone bind and inverseBind transforms.
     * @param source
     */
    public Skeleton(Skeleton source){
        Bone[] sourceList = source.boneList;
        boneList = new Bone[sourceList.length];
        for (int i = 0; i < sourceList.length; i++)
            boneList[i] = new Bone(sourceList[i]);

        rootBones = new Bone[source.rootBones.length];
        for (int i = 0; i < rootBones.length; i++){
            rootBones[i] = recreateBoneStructure(source.rootBones[i]);
        }
        createSkinningMatrices();

        for (int i = rootBones.length - 1; i >= 0; i--)
            rootBones[i].update();
    }

    /**
     * Used for binary loading as a Savable; the object must be constructed,
     * then the parameters usually present in the constructor for this class are
     * restored from the file the object was saved to.
     */
    public Skeleton() {
    }

    private void createSkinningMatrices(){
        skinningMatrixes = new Matrix4f[boneList.length];
        for (int i = 0; i < skinningMatrixes.length; i++)
            skinningMatrixes[i] = new Matrix4f();
    }

    private Bone recreateBoneStructure(Bone sourceRoot){
        Bone targetRoot = getBone(sourceRoot.getName());
        List<Bone> children = sourceRoot.getChildren();
        for (int i = 0; i < children.size(); i++){
            Bone sourceChild = children.get(i);
            // find my version of the child
            Bone targetChild = getBone(sourceChild.getName());
            targetRoot.addChild(targetChild);
            recreateBoneStructure(sourceChild);
        }

        return targetRoot;
    }

    /**
     * Updates world transforms for all bones in this skeleton.
     * Typically called after setting local animation transforms.
     */
    public void updateWorldVectors(){
        for (int i = rootBones.length - 1; i >= 0; i--)
            rootBones[i].update();
    }

    /**
     * Saves the current skeleton state as it's binding pose.
     */
    public void setBindingPose(){
        for (int i = rootBones.length - 1; i >= 0; i--)
            rootBones[i].setBindingPose();
    }

    /**
     * Reset the skeleton to bind pose.
     */
    public final void reset(){
        for (int i = rootBones.length-1; i >= 0; i--)
            rootBones[i].reset();
    }

    public final void resetAndUpdate(){
        for (int i = rootBones.length-1; i >= 0; i--){
            Bone rootBone = rootBones[i];
            rootBone.reset();
            rootBone.update();
        }
    }

    public Bone[] getRoots(){
        return rootBones;
    }

    public Bone getBone(int index){
        return boneList[index];
    }

    public Bone getBone(String name){
        for (int i = 0; i < boneList.length; i++)
            if (boneList[i].getName().equals(name))
                return boneList[i];

        return null;
    }

    public int getBoneIndex(Bone bone){
        for (int i = 0; i < boneList.length; i++)
            if (boneList[i] == bone)
                return i;

        return -1;
    }

    public Matrix4f[] computeSkinningMatrices(){
        TempVars vars = TempVars.get();
        assert vars.lock();
        for (int i = 0; i < boneList.length; i++){
            boneList[i].getOffsetTransform(skinningMatrixes[i], vars.quat1, vars.vect1);
        }
        assert vars.unlock();
        return skinningMatrixes;
    }

    public int getBoneCount() {
        return boneList.length;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Skeleton - "+boneList.length+" bones, "+rootBones.length+" roots\n");
        for (Bone rootBone : rootBones){
            sb.append(rootBone.toString());
        }
        return sb.toString();
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule input = im.getCapsule(this);

        Savable[] boneRootsAsSav = input.readSavableArray("rootBones", null);
        rootBones = new Bone[boneRootsAsSav.length];
        System.arraycopy(boneRootsAsSav, 0, rootBones, 0, boneRootsAsSav.length);
        
        Savable[] boneListAsSavable = input.readSavableArray("boneList", null);
        boneList = new Bone[boneListAsSavable.length];
        System.arraycopy(boneListAsSavable, 0, boneList, 0, boneListAsSavable.length);
        
        createSkinningMatrices();

        for (Bone rootBone : rootBones){
            rootBone.update();
            rootBone.setBindingPose();
        }
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule output = ex.getCapsule(this);
        output.write(rootBones, "rootBones", null);
        output.write(boneList, "boneList", null);
    }
}
