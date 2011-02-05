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

package com.jme3.terrain.geomipmap;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.SweepSphere;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.geomipmap.lodcalc.LodCalculator;
import com.jme3.terrain.geomipmap.lodcalc.LodCalculatorFactory;
import com.jme3.terrain.geomipmap.lodcalc.util.EntropyComputeUtil;
import com.jme3.util.BufferUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * A terrain patch is a leaf in the terrain quad tree. It has a mesh that can change levels of detail (LOD)
 * whenever the view point, or camera, changes. The actual terrain mesh is created by the LODGeomap class.
 * That uses a geo mip mapping algorithm to change the index buffer of the mesh.
 * The mesh is a triangle strip. In wireframe mode you might notice some strange lines, these are degenerate
 * triangles generated by the geoMipMap algorithm and can be ignored. The video card removes them at almost no cost.
 * 
 * Each patch needs to know its neighbour's LOD so it can seam its edges with them, in case the neighbour has a different
 * LOD. If this doesn't happen, you will see gaps.
 * 
 * The LOD value is most detailed at zero. It gets less detailed the higher the LOD value until you reach maxLod, which
 * is a mathematical limit on the number of times the 'size' of the patch can be divided by two. However there is a -1 to that
 * for now until I add in a custom index buffer calculation for that max level, the current algorithm does not go that far.
 * 
 * You can supply a LodThresholdCalculator for use in determining when the LOD should change. It's API will no doubt change 
 * in the near future. Right now it defaults to just changing LOD every two patch sizes. So if a patch has a size of 65, 
 * then the LOD changes every 130 units away.
 * 
 * @author Brent Owens
 */
public class TerrainPatch extends Geometry {

	protected LODGeomap geomap;
	protected int lod = -1; // this terrain patch's LOD
	private int maxLod = -1;
	protected int previousLod = -1;
	protected int lodLeft, lodTop, lodRight, lodBottom; // it's neighbour's LODs
	
	protected int size;

	protected int totalSize;

	protected short quadrant = 1;

	// x/z step
	protected Vector3f stepScale;

	// center of the block in relation to (0,0,0)
	protected Vector2f offset;

	// amount the block has been shifted.
	protected float offsetAmount;

    protected LodCalculator lodCalculator;
    protected LodCalculatorFactory lodCalculatorFactory;

    protected TerrainPatch leftNeighbour, topNeighbour, rightNeighbour, bottomNeighbour;
    protected boolean searchedForNeighboursAlready = false;


    protected float[] lodEntropy;

    public TerrainPatch() {
        super("TerrainPatch");
    }
    
	public TerrainPatch(String name) {
		super(name);
	}
	
	public TerrainPatch(String name, int size) {
		this(name, size, new Vector3f(1,1,1), null, new Vector3f(0,0,0));
	}
	
	/**
	 * Constructor instantiates a new <code>TerrainPatch</code> object. The
	 * parameters and heightmap data are then processed to generate a
	 * <code>TriMesh</code> object for rendering.
	 * 
	 * @param name
	 *			the name of the terrain block.
	 * @param size
	 *			the size of the heightmap.
	 * @param stepScale
	 *			the scale for the axes.
	 * @param heightMap
	 *			the height data.
	 * @param origin
	 *			the origin offset of the block.
	 */
	public TerrainPatch(String name, int size, Vector3f stepScale,
			float[] heightMap, Vector3f origin) {
		this(name, size, stepScale, heightMap, origin, size, new Vector2f(), 0);
	}

	/**
	 * Constructor instantiates a new <code>TerrainPatch</code> object. The
	 * parameters and heightmap data are then processed to generate a
	 * <code>TriMesh</code> object for renderering.
	 * 
	 * @param name
	 *			the name of the terrain block.
	 * @param size
	 *			the size of the block.
	 * @param stepScale
	 *			the scale for the axes.
	 * @param heightMap
	 *			the height data.
	 * @param origin
	 *			the origin offset of the block.
	 * @param totalSize
	 *			the total size of the terrain. (Higher if the block is part of
	 *			a <code>TerrainPage</code> tree.
	 * @param offset
	 *			the offset for texture coordinates.
	 * @param offsetAmount
	 *			the total offset amount. Used for texture coordinates.
	 */
	public TerrainPatch(String name, int size, Vector3f stepScale,
			float[] heightMap, Vector3f origin, int totalSize,
			Vector2f offset, float offsetAmount) {
		super(name);
		this.size = size;
		this.stepScale = stepScale;
		this.totalSize = totalSize;
		this.offsetAmount = offsetAmount;
		this.offset = offset;

		setLocalTranslation(origin);
		
		FloatBuffer heightBuffer = BufferUtils.createFloatBuffer(size*size);
		heightBuffer.put(heightMap);
		
		geomap = new LODGeomap(size, heightBuffer);
		Mesh m = geomap.createMesh(stepScale, new Vector2f(1,1), offset, offsetAmount, totalSize, false);
		setMesh(m);
		
	}

    /**
     * This calculation is slow, so don't use it often.
     */
    public void generateLodEntropies() {
        float[] entropies = new float[getMaxLod()+1];
        for (int i = 0; i <= getMaxLod(); i++){
            int curLod = (int) Math.pow(2, i);
            IntBuffer buf = geomap.writeIndexArrayLodDiff(null, curLod, false, false, false, false);
            entropies[i] = EntropyComputeUtil.computeLodEntropy(mesh, buf);
        }

        lodEntropy = entropies;
    }

    public float[] getLodEntropies(){
        if (lodEntropy == null){
            generateLodEntropies();
        }
        return lodEntropy;
    }

	public FloatBuffer getHeightmap() {
		return geomap.getHeightData();
	}
	
	/**
	 * The maximum lod supported by this terrain patch.
	 * If the patch size is 32 then the returned value would be log2(32)-2 = 3
	 * You can then use that value, 3, to see how many times you can divide 32 by 2 
	 * before the terrain gets too un-detailed (can't stitch it any further).
	 * @return
	 */
	public int getMaxLod() {
		if (maxLod < 0)
			maxLod = Math.max(1, (int) (FastMath.log(size-1)/FastMath.log(2)) -1); // -1 forces our minimum of 4 triangles wide
		
		return maxLod;
	}
	

    /**
     * Delegates to the lodCalculator that was passed in.
     * @param locations all possible camera locations
     * @param updates update objects that may or may not contain this terrain patch
     * @return true if the geometry needs re-indexing
     */
	protected boolean calculateLod(List<Vector3f> locations, HashMap<String,UpdatedTerrainPatch> updates) {
        return lodCalculator.calculateLod(locations, updates);
    }

	protected void reIndexGeometry(HashMap<String,UpdatedTerrainPatch> updated) {
		
		UpdatedTerrainPatch utp = updated.get(getName());
		
		if (utp != null && (utp.isReIndexNeeded() || utp.isFixEdges()) ) {
			int pow = (int) Math.pow(2, utp.getNewLod());
			boolean left = utp.getLeftLod() > utp.getNewLod();
			boolean top = utp.getTopLod() > utp.getNewLod();
			boolean right = utp.getRightLod() > utp.getNewLod();
			boolean bottom = utp.getBottomLod() > utp.getNewLod();
            
            IntBuffer ib = null;
            if (lodCalculator.usesVariableLod())
                ib = geomap.writeIndexArrayLodVariable(null, pow, (int) Math.pow(2, utp.getRightLod()), (int) Math.pow(2, utp.getTopLod()), (int) Math.pow(2, utp.getLeftLod()), (int) Math.pow(2, utp.getBottomLod()));
            else
                ib = geomap.writeIndexArrayLodDiff(null, pow, right, top, left, bottom);
			utp.setNewIndexBuffer(ib);
		}
		
	}

    public float getHeight(float x, float z) {
        if (x < 0 || z < 0 || x >= size || z >= size)
            return 0;
        int idx = (int) (z * size + x);
        return getMesh().getFloatBuffer(Type.Position).get(idx*3+1); // 3 floats per entry (x,y,z), the +1 is to get the Y
    }

    public void setHeight(float x, float z, float height) {
        if (x < 0 || z < 0 || x >= size || z >= size)
            return;
        int idx = (int) (z * size + x);
        geomap.getHeightData().put(idx, height);
        
        FloatBuffer newVertexBuffer = geomap.writeVertexArray(null, stepScale, false);
        getMesh().clearBuffer(Type.Position);
		getMesh().setBuffer(Type.Position, 3, newVertexBuffer);
        // normals are updated from the terrain controller on update()
    }

    public void adjustHeight(float x, float z, float delta) {
        if (x < 0 || z < 0 || x >= size || z >= size)
            return;
        int idx = (int) (z * size + x);
        float h = getMesh().getFloatBuffer(Type.Position).get(idx*3+1);
        
        geomap.getHeightData().put(idx, h+delta);

        FloatBuffer newVertexBuffer = geomap.writeVertexArray(null, stepScale, false);
        getMesh().clearBuffer(Type.Position);
		getMesh().setBuffer(Type.Position, 3, newVertexBuffer);
    }

    /**
     * recalculate all of this normal vectors in this terrain patch
     */
    protected void updateNormals() {
        FloatBuffer newNormalBuffer = geomap.writeNormalArray(null, stepScale);
        getMesh().clearBuffer(Type.Normal);
		getMesh().setBuffer(Type.Normal, 3, newNormalBuffer);
    }

    protected void fixNormalEdges(TerrainPatch right,
                                TerrainPatch bottom,
                                TerrainPatch top,
                                TerrainPatch left,
                                TerrainPatch bottomRight,
                                TerrainPatch topLeft)
    {

        Vector3f rootPoint = new Vector3f();
        Vector3f rightPoint = new Vector3f();
        Vector3f leftPoint = new Vector3f();
        Vector3f topPoint = new Vector3f();
        Vector3f bottomPoint = new Vector3f();
        Vector3f normal = new Vector3f();

        int s = this.getSize()-1;

        if (right != null) { // right side
            for (int i=0; i<s+1; i++) {
                rootPoint.set(s, this.getHeight(s,i), i);
                leftPoint.set(s-1, this.getHeight(s-1,i), i);
                rightPoint.set(s+1, right.getHeight(1,i), i);
                if (i == 0) { // top
                    if (top == null) {
                        bottomPoint.set(s, this.getHeight(s,i+1), i+1);
                        Vector3f n1 = getNormal(leftPoint, rootPoint, bottomPoint);
                        Vector3f n2 = getNormal(bottomPoint, rootPoint, rightPoint);
                        normal.set(n1.add(n2).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer rightNB = right.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), s);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)rightNB.getData(), 0);
                    } else {
                        topPoint.set(s, top.getHeight(s,s), i-1);
                        bottomPoint.set(s, this.getHeight(s,i+1), i+1);
                        Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                        Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                        Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                        Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                        normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer rightNB = right.getMesh().getBuffer(Type.Normal);
                        VertexBuffer topNB = top.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), s);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)rightNB.getData(), 0);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)topNB.getData(), (s+1)*(s+1)-1);
                    }
                } else if (i == s) { // bottom
                    if (bottom == null) {
                        topPoint.set(s, this.getHeight(s,i-1), i-1);
                        Vector3f n1 = getNormal(rightPoint, rootPoint, topPoint);
                        Vector3f n2 = getNormal(topPoint, rootPoint, leftPoint);
                        normal.set(n1.add(n2).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer rightNB = right.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*(s+1)-1);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)rightNB.getData(), (s+1)*s);
                    } else {
                        topPoint.set(s, this.getHeight(s,i-1), i-1);
                        bottomPoint.set(s, bottom.getHeight(s+1,1), i+1);
                        Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                        Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                        Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                        Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                        normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer rightNB = right.getMesh().getBuffer(Type.Normal);
                        VertexBuffer downNB = bottom.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*(s+1)-1);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)rightNB.getData(), (s+1)*s);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)downNB.getData(), s);
                    }
                } else { // all in the middle
                    topPoint.set(s, this.getHeight(s,i-1), i-1);
                    bottomPoint.set(s, this.getHeight(s,i+1), i+1);
                    Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                    Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                    Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                    Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                    normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                    VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                    VertexBuffer rightNB = right.getMesh().getBuffer(Type.Normal);
                    BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*(i+1)-1);
                    BufferUtils.setInBuffer(normal, (FloatBuffer)rightNB.getData(), (s+1)*(i));
                }
            }
        }

        if (bottom != null) {
            for (int i=0; i<s+1; i++) {
                rootPoint.set(i, this.getHeight(i,s), s);
                topPoint.set(i, this.getHeight(i,s-1), s-1);
                bottomPoint.set(i, bottom.getHeight(i,1), s+1);
                if (i == 0) { // left
                    if (left == null) {
                        rightPoint.set(i+1, this.getHeight(i+1,s), s);
                        Vector3f n1 = getNormal(rightPoint, rootPoint, topPoint);
                        Vector3f n2 = getNormal(bottomPoint, rootPoint, rightPoint);
                        normal.set(n1.add(n2).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer downNB = bottom.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*s);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)downNB.getData(), 0);
                    } else {
                        leftPoint.set(i-1, left.getHeight(s-1,s), s);
                        rightPoint.set(i+1, this.getHeight(i+1,s), s);
                        Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                        Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                        Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                        Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                        normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer leftNB = left.getMesh().getBuffer(Type.Normal);
                        VertexBuffer downNB = bottom.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*s);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)leftNB.getData(), (s+1)*(s+1)-1);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)downNB.getData(), 0);
                    }
                } else if (i == s) { // right
                    if (right == null) {
                        leftPoint.set(s-1, this.getHeight(s-1,s), s);
                        Vector3f n1 = getNormal(rightPoint, rootPoint, topPoint);
                        Vector3f n2 = getNormal(topPoint, rootPoint, leftPoint);
                        normal.set(n1.add(n2).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer downNB = left.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*(s+1)-1);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)downNB.getData(), s);
                    } else {
                        leftPoint.set(s-1, this.getHeight(s-1,s), s);
                        rightPoint.set(s+1, right.getHeight(1,s), s);
                        Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                        Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                        Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                        Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                        normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer rightNB = right.getMesh().getBuffer(Type.Normal);
                        VertexBuffer downNB = bottom.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*(s+1)-1);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)rightNB.getData(), (s+1)*s);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)downNB.getData(), s);

                        // lets also fix the one on the corner, below the right neighbour
                        if (bottomRight != null) {
                            VertexBuffer cornerNB = bottomRight.getMesh().getBuffer(Type.Normal);
                            BufferUtils.setInBuffer(normal, (FloatBuffer)cornerNB.getData(), 0);
                        }
                    }
                } else { // all in the middle
                    leftPoint.set(i-1, this.getHeight(i-1,s), s);
                    rightPoint.set(i+1, this.getHeight(i+1,s), s);
                    Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                    Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                    Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                    Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                    normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                    VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                    VertexBuffer downNB = bottom.getMesh().getBuffer(Type.Normal);
                    BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*(s)+i);
                    BufferUtils.setInBuffer(normal, (FloatBuffer)downNB.getData(), i);
                }
            }
        }

        if (left != null) { // left side
            for (int i=0; i<s+1; i++) {
                rootPoint.set(0, this.getHeight(0,i), i);
                leftPoint.set(-1, left.getHeight(s-1,i), i); //xxx
                rightPoint.set(1, this.getHeight(1,i), i); //xxx
                if (i == 0) { // top
                    if (top == null) {
                        bottomPoint.set(0, this.getHeight(0,i+1), i+1);
                        Vector3f n1 = getNormal(leftPoint, rootPoint, bottomPoint);
                        Vector3f n2 = getNormal(bottomPoint, rootPoint, rightPoint);
                        normal.set(n1.add(n2).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer leftNB = left.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), 0);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)leftNB.getData(), s+1);
                    } else {
                        topPoint.set(0, top.getHeight((s+1)*(s-1),s), i-1);
                        bottomPoint.set(0, this.getHeight(0,i+1), i+1);
                        Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                        Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                        Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                        Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                        normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                        VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                        VertexBuffer leftNB = left.getMesh().getBuffer(Type.Normal);
                        VertexBuffer topNB = top.getMesh().getBuffer(Type.Normal);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), 0);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)leftNB.getData(), s);
                        BufferUtils.setInBuffer(normal, (FloatBuffer)topNB.getData(), (s+1)*(s+1)-1);
                    }
                } else if (i == s) { // bottom
                    // ignore, handled by the other bottom calculation
                } else { // all in the middle
                    topPoint.set(0, this.getHeight(0,i-1), i-1);
                    bottomPoint.set(0, this.getHeight(0,i+1), i+1);
                    Vector3f n1 = getNormal(topPoint, rootPoint, leftPoint);
                    Vector3f n2 = getNormal(leftPoint, rootPoint, bottomPoint);
                    Vector3f n3 = getNormal(bottomPoint, rootPoint, rightPoint);
                    Vector3f n4 = getNormal(rightPoint, rootPoint, topPoint);
                    normal.set(n1.add(n2).add(n3).add(n4).normalizeLocal());
                    VertexBuffer tpNB = this.getMesh().getBuffer(Type.Normal);
                    VertexBuffer leftNB = left.getMesh().getBuffer(Type.Normal);
                    BufferUtils.setInBuffer(normal, (FloatBuffer)tpNB.getData(), (s+1)*(i));
                    BufferUtils.setInBuffer(normal, (FloatBuffer)leftNB.getData(), (s+1)*(i+1)-1);
                }
            }
        }
    }

    private Vector3f getNormal(Vector3f firstPoint, Vector3f rootPoint, Vector3f secondPoint) {
        Vector3f normal = new Vector3f();
        normal.set(firstPoint).subtractLocal(rootPoint)
                  .crossLocal(secondPoint.subtract(rootPoint)).normalizeLocal();
        return normal;
    }

    /**
     * Locks the mesh (sets it static) to improve performance.
     * But it it not editable then. Set unlock to make it editable.
     */
    public void lockMesh() {
        getMesh().setStatic();
    }

    /**
     * Unlocks the mesh (sets it dynamic) to make it editable.
     * It will be editable but performance will be reduced.
     * Call lockMesh to improve performance.
     */
    public void unlockMesh() {
        getMesh().setDynamic();
    }
	
	/**
	 * Returns the offset amount this terrain block uses for textures.
	 * 
	 * @return The current offset amount.
	 */
	public float getOffsetAmount() {
		return offsetAmount;
	}

	/**
	 * Returns the step scale that stretches the height map.
	 * 
	 * @return The current step scale.
	 */
	public Vector3f getStepScale() {
		return stepScale;
	}

	/**
	 * Returns the total size of the terrain.
	 * 
	 * @return The terrain's total size.
	 */
	public int getTotalSize() {
		return totalSize;
	}

	/**
	 * Returns the size of this terrain block.
	 * 
	 * @return The current block size.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns the current offset amount. This is used when building texture
	 * coordinates.
	 * 
	 * @return The current offset amount.
	 */
	public Vector2f getOffset() {
		return offset;
	}

	/**
	 * Sets the value for the current offset amount to use when building texture
	 * coordinates. Note that this does <b>NOT </b> rebuild the terrain at all.
	 * This is mostly used for outside constructors of terrain blocks.
	 * 
	 * @param offset
	 *			The new texture offset.
	 */
	public void setOffset(Vector2f offset) {
		this.offset = offset;
	}

	/**
	 * Sets the size of this terrain block. Note that this does <b>NOT </b>
	 * rebuild the terrain at all. This is mostly used for outside constructors
	 * of terrain blocks.
	 * 
	 * @param size
	 *			The new size.
	 */
	public void setSize(int size) {
		this.size = size;
		
		maxLod = -1; // reset it
	}

	/**
	 * Sets the total size of the terrain . Note that this does <b>NOT </b>
	 * rebuild the terrain at all. This is mostly used for outside constructors
	 * of terrain blocks.
	 * 
	 * @param totalSize
	 *			The new total size.
	 */
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	/**
	 * Sets the step scale of this terrain block's height map. Note that this
	 * does <b>NOT </b> rebuild the terrain at all. This is mostly used for
	 * outside constructors of terrain blocks.
	 * 
	 * @param stepScale
	 *			The new step scale.
	 */
	public void setStepScale(Vector3f stepScale) {
		this.stepScale = stepScale;
	}

	/**
	 * Sets the offset of this terrain texture map. Note that this does <b>NOT
	 * </b> rebuild the terrain at all. This is mostly used for outside
	 * constructors of terrain blocks.
	 * 
	 * @param offsetAmount
	 *			The new texture offset.
	 */
	public void setOffsetAmount(float offsetAmount) {
		this.offsetAmount = offsetAmount;
	}
	
	/**
	 * @return Returns the quadrant.
	 */
	public short getQuadrant() {
		return quadrant;
	}

	/**
	 * @param quadrant
	 *			The quadrant to set.
	 */
	public void setQuadrant(short quadrant) {
		this.quadrant = quadrant;
	}

	public int getLod() {
		return lod;
	}

	public void setLod(int lod) {
		this.lod = lod;
                if (this.lod <= 0)
                    throw new IllegalArgumentException();
	}

	public int getPreviousLod() {
		return previousLod;
	}

	public void setPreviousLod(int previousLod) {
		this.previousLod = previousLod;
	}

	protected int getLodLeft() {
		return lodLeft;
	}

	protected void setLodLeft(int lodLeft) {
		this.lodLeft = lodLeft;
	}

	protected int getLodTop() {
		return lodTop;
	}

	protected void setLodTop(int lodTop) {
		this.lodTop = lodTop;
	}

	protected int getLodRight() {
		return lodRight;
	}

	protected void setLodRight(int lodRight) {
		this.lodRight = lodRight;
	}

	protected int getLodBottom() {
		return lodBottom;
	}

	protected void setLodBottom(int lodBottom) {
		this.lodBottom = lodBottom;
	}

    public LodCalculator getLodCalculator() {
        return lodCalculator;
    }

    public void setLodCalculator(LodCalculator lodCalculator) {
        this.lodCalculator = lodCalculator;
    }

    public void setLodCalculator(LodCalculatorFactory lodCalculatorFactory) {
        this.lodCalculatorFactory = lodCalculatorFactory;
        setLodCalculator(lodCalculatorFactory.createCalculator(this));
    }

    @Override
    public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
        if (refreshFlags != 0)
            throw new IllegalStateException("Scene graph must be updated" +
                                            " before checking collision");

        if (getWorldBound().collideWith(other, new CollisionResults()) == 0)
            return 0;
        
        if (other instanceof SweepSphere)
             return collideWithSweepSphere((SweepSphere)other, results);
        else if(other instanceof Ray)
            return collideWithRay((Ray)other, results);
        else if (other instanceof BoundingVolume)
            return collideWithBoundingVolume((BoundingVolume)other, results);
        else {
            throw new UnsupportedCollisionException();
        }
    }

    private int collideWithSweepSphere(SweepSphere sweepSphere, CollisionResults results) {
        return 0; //TODO
    }

    private int collideWithRay(Ray ray, CollisionResults results) {
        Vector3f xyz = getWorldBound().getCenter();
        results.addCollision(new CollisionResult(xyz, ray.distanceSquared(getWorldTranslation())));
        System.out.println(xyz);
        return 1; //TODO
    }

    private int collideWithBoundingVolume(BoundingVolume boundingVolume, CollisionResults results) {
        return 0; //TODO
    }

    @Override
    public void write(JmeExporter ex) throws IOException {

        // we don't want to save the mesh. We just save the heightmap and rebuild it on load
        // this is much faster and makes way smaller save files
        Mesh tempMesh = getMesh();
        mesh = null;

        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(size, "size", 16);
        oc.write(totalSize, "totalSize", 16);
        oc.write(quadrant, "quadrant", (short)0);
        oc.write(stepScale, "stepScale", Vector3f.UNIT_XYZ);
        oc.write(offset, "offset", Vector3f.UNIT_XYZ);
        oc.write(offsetAmount, "offsetAmount", 0);
        oc.write(lodCalculator, "lodCalculator", null);
        oc.write(lodCalculatorFactory, "lodCalculatorFactory", null);
        oc.write(geomap.getHeightData(), "heightmap", null);
        oc.write(lodEntropy, "lodEntropy", null);

        setMesh(tempMesh); // add the mesh back
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        size = ic.readInt("size", 16);
        totalSize = ic.readInt("totalSize", 16);
        quadrant = ic.readShort("quadrant", (short)0);
        stepScale = (Vector3f) ic.readSavable("stepScale", Vector3f.UNIT_XYZ);
        offset = (Vector2f) ic.readSavable("offset", Vector3f.UNIT_XYZ);
        offsetAmount = ic.readFloat("offsetAmount", 0);
        lodCalculator = (LodCalculator) ic.readSavable("lodCalculator", new DistanceLodCalculator());
        lodCalculator.setTerrainPatch(this);
        lodCalculatorFactory = (LodCalculatorFactory) ic.readSavable("lodCalculatorFactory", null);
        lodEntropy = ic.readFloatArray("lodEntropy", null);
        FloatBuffer heightBuffer = ic.readFloatBuffer("heightmap", null);
        geomap = new LODGeomap(size, heightBuffer);
        Mesh m = geomap.createMesh(stepScale, Vector2f.UNIT_XY, offset, offsetAmount, totalSize, false);
        setMesh(m);
    }

    @Override
    public TerrainPatch clone() {
        TerrainPatch clone = new TerrainPatch();
        clone.name = name.toString();
        clone.size = size;
        clone.totalSize = totalSize;
        clone.quadrant = quadrant;
        clone.stepScale = stepScale.clone();
        clone.offset = offset.clone();
        clone.offsetAmount = offsetAmount;
        //clone.lodCalculator = lodCalculator.clone();
        //clone.lodCalculator.setTerrainPatch(clone);
        clone.setLodCalculator(lodCalculatorFactory.clone());
        clone.geomap = new LODGeomap(size, geomap.getHeightData());
        clone.setLocalTranslation(getLocalTranslation().clone());
        Mesh m = clone.geomap.createMesh(clone.stepScale, Vector2f.UNIT_XY, clone.offset, clone.offsetAmount, clone.totalSize, false);
        clone.setMesh(m);
        clone.setMaterial(material.clone());
        return clone;
    }

}