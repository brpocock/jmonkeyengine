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
package com.jme3.terrain.geomipmap.lodcalc;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.terrain.geomipmap.UpdatedTerrainPatch;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Calculates the LOD of the terrain based on its distance from the
 * cameras. Taking the minimum distance from all cameras.
 *
 * @author bowens
 */
public class DistanceLodCalculator implements LodCalculator {

    private TerrainPatch terrainPatch;
    private LodThreshold lodThresholdCalculator;

    public DistanceLodCalculator() {
    }

    public DistanceLodCalculator(LodThreshold lodThresholdCalculator) {
        this.lodThresholdCalculator = lodThresholdCalculator;
    }

    public DistanceLodCalculator(TerrainPatch terrainPatch, LodThreshold lodThresholdCalculator) {
        this.terrainPatch = terrainPatch;
        this.lodThresholdCalculator = lodThresholdCalculator;
    }

    public boolean calculateLod(List<Vector3f> locations, HashMap<String, UpdatedTerrainPatch> updates) {
        float distance = getCenterLocation().distance(locations.get(0));

        // go through each lod level to find the one we are in
        for (int i = 1; i <= terrainPatch.getMaxLod(); i++) {
            if (distance < lodThresholdCalculator.getLodDistanceThreshold() * (i + 1) || i == terrainPatch.getMaxLod()) {
                boolean reIndexNeeded = false;
                if (i != terrainPatch.getLod()) {
                    reIndexNeeded = true;
                    //System.out.println("lod change: "+lod+" > "+i+"    dist: "+distance);
                }
                int prevLOD = terrainPatch.getLod();
                //previousLod = lod;
                //lod = i;
                UpdatedTerrainPatch utp = updates.get(terrainPatch.getName());
                if (utp == null) {
                    utp = new UpdatedTerrainPatch(terrainPatch, i);//save in here, do not update actual variables
                    updates.put(utp.getName(), utp);
                }
                utp.setPreviousLod(prevLOD);
                utp.setReIndexNeeded(reIndexNeeded);

                return reIndexNeeded;
            }
        }

        return false;
        /*
        int newLOD = terrainPatch.getLod();
        int prevLOD = terrainPatch.getPreviousLod();

        if (newLOD != terrainPatch.getMaxLod())
        prevLOD = newLOD;

        // max lod (least detailed)
        newLOD = terrainPatch.getMaxLod();

        boolean reIndexNeeded = false;

        if (prevLOD != newLOD) {
        reIndexNeeded = true;
        System.out.println("lod change prev/new: "+prevLOD+"/"+newLOD);
        }
        
        UpdatedTerrainPatch utp = updates.get(terrainPatch.getName());
        if (utp == null) {
        utp = new UpdatedTerrainPatch(terrainPatch, newLOD);// save in here, do not update actual variables
        updates.put(utp.getName(), utp);
        }
        utp.setPreviousLod(prevLOD);
        utp.setReIndexNeeded(reIndexNeeded);

        return reIndexNeeded;
         */
    }

    public Vector3f getCenterLocation() {
        Vector3f loc = terrainPatch.getWorldTranslation().clone();
        loc.x += terrainPatch.getSize() / 2;
        loc.z += terrainPatch.getSize() / 2;
        return loc;
    }

    public void setTerrainPatch(TerrainPatch terrainPatch) {
        this.terrainPatch = terrainPatch;
    }

    protected LodThreshold getLodThreshold() {
        return lodThresholdCalculator;
    }

    protected void setLodThreshold(LodThreshold lodThresholdCalculator) {
        this.lodThresholdCalculator = lodThresholdCalculator;
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(lodThresholdCalculator, "lodThresholdCalculator", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        lodThresholdCalculator = (LodThreshold) ic.readSavable("lodThresholdCalculator", null);
    }

    @Override
    public LodCalculator clone() {
        DistanceLodCalculator clone = new DistanceLodCalculator();
        clone.lodThresholdCalculator = lodThresholdCalculator.clone();

        return clone;
    }

    @Override
    public String toString() {
        return "DistanceLodCalculator " + lodThresholdCalculator.toString();
    }
}
