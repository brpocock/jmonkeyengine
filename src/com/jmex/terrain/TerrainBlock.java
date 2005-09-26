/*
 * Copyright (c) 2003-2005 jMonkeyEngine
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

package com.jmex.terrain;

import java.nio.FloatBuffer;

import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.VBOInfo;
import com.jme.scene.lod.AreaClodMesh;
import com.jme.util.geom.BufferUtils;

/**
 * <code>TerrainBlock</code> defines the lowest level of the terrain system.
 * <code>TerrainBlock</code> is the actual part of the terrain system that
 * renders to the screen. The terrain is built from a heightmap defined by a one
 * dimenensional int array. The step scale is used to define the amount of units
 * each block line will extend. Clod can be used to allow for level of detail
 * control.
 *
 * By directly creating a <code>TerrainBlock</code> yourself, you can generate
 * a brute force terrain. This is many times sufficient for small terrains on
 * modern hardware. If terrain is to be large, it is recommended that you make
 * use of the <code>TerrainPage</code> class.
 *
 * @author Mark Powell
 * @version $Id: TerrainBlock.java,v 1.6 2005-09-26 22:51:46 renanse Exp $
 */
public class TerrainBlock extends AreaClodMesh {

    private static final long serialVersionUID = 1L;

	//size of the block, totalSize is the total size of the heightmap if this
    //block is just a small section of it.
    private int size;

    private int totalSize;

    //x/z step
    private Vector3f stepScale;

    //use lod or not
    private boolean useClod;

    //center of the block in relation to (0,0,0)
    private Vector2f offset;

    //amount the block has been shifted.
    private int offsetAmount;

    // heightmap values used to create this block
    private int[] heightMap;
    private int[] oldHeightMap;

    /**
     * Empty Constructor to be used internally only.
     */ 
    public TerrainBlock() {}

    /**
     * For internal use only.  Creates a new Terrainblock with the given name by simply calling
     * super(name)
     * @param name The name.
     * @see com.jme.scene.lod.AreaClodMesh#AreaClodMesh(java.lang.String)
     */
    public TerrainBlock(String name){
        super(name);
    }

    /**
     * Constructor instantiates a new <code>TerrainBlock</code> object. The
     * parameters and heightmap data are then processed to generate a
     * <code>TriMesh</code> object for renderering.
     *
     * @param name
     *            the name of the terrain block.
     * @param size
     *            the size of the heightmap.
     * @param stepScale
     *            the scale for the axes.
     * @param heightMap
     *            the height data.
     * @param origin
     *            the origin offset of the block.
     * @param clod
     *            true will use level of detail, false will not.
     */
    public TerrainBlock(String name, int size, Vector3f stepScale,
            int[] heightMap, Vector3f origin, boolean clod) {
        this(name, size, stepScale, heightMap, origin, clod, size,
                new Vector2f(), 0);
    }

    /**
     * Constructor instantiates a new <code>TerrainBlock</code> object. The
     * parameters and heightmap data are then processed to generate a
     * <code>TriMesh</code> object for renderering.
     *
     * @param name
     *            the name of the terrain block.
     * @param size
     *            the size of the block.
     * @param stepScale
     *            the scale for the axes.
     * @param heightMap
     *            the height data.
     * @param origin
     *            the origin offset of the block.
     * @param clod
     *            true will use level of detail, false will not.
     * @param totalSize
     *            the total size of the terrain. (Higher if the block is part of
     *            a <code>TerrainPage</code> tree.
     * @param offset
     *            the offset for texture coordinates.
     * @param offsetAmount
     *            the total offset amount.  Used for texture coordinates.
     */
    protected TerrainBlock(String name, int size, Vector3f stepScale,
            int[] heightMap, Vector3f origin, boolean clod, int totalSize,
            Vector2f offset, int offsetAmount) {
        super(name);
        this.useClod = clod;
        this.size = size;
        this.stepScale = stepScale;
        this.totalSize = totalSize;
        this.offsetAmount = offsetAmount;
        this.offset = offset;
        this.heightMap = heightMap;

        setLocalTranslation(origin);

        buildVertices();
        buildTextureCoordinates();
        buildNormals();
        buildColors();

        VBOInfo vbo = new VBOInfo(true);
        vboInfo = vbo;

        if (useClod) {
            this.create(null);
            this.setTrisPerPixel(0.02f);
        }
    }
    
    public int getType() {
    	return (Spatial.GEOMETRY | Spatial.TRIMESH | Spatial.TERRAIN_BLOCK);
    }

  /**
     * <code>chooseTargetRecord</code> determines which level of detail to
     * use. If CLOD is not used, the index 0 is always returned.
     *
     * @param r
     *            the renderer to use for determining the LOD record.
     * @return the index of the record to use.
     */
    public int chooseTargetRecord(Renderer r) {
        if (useClod) {
            return super.chooseTargetRecord(r);
        } else {
            return 0;
        }
    }

    /**
     *
     * <code>setDetailTexture</code> sets the detail texture unit's repeat
     * value.
     *
     * @param unit
     *            int
     * @param repeat
     *            int
     */
    public void setDetailTexture(int unit, int repeat) {
        FloatBuffer buf = texBuf[unit];
        if (buf == null || buf.capacity() != texBuf[0].capacity()) {
            buf = BufferUtils.createFloatBuffer(texBuf[0].capacity());
            texBuf[unit] = buf;
        }
        buf.clear();
        texBuf[0].rewind();
        for (int i = 0, len = buf.capacity(); i < len; i++) {
            buf.put(repeat * texBuf[0].get());
        }
    }

    /**
     *
     * <code>getHeight</code> returns the height of an arbitrary point on the
     * terrain. If the point is between height point values, the height is
     * linearly interpolated. This provides smooth height calculations. If the
     * point provided is not within the bounds of the height map, the NaN float
     * value is returned (Float.NaN).
     *
     * @param position
     *            the vector representing the height location to check.
     * @return the height at the provided location.
     */
    public float getHeight(Vector2f position) {
        return getHeight(position.x, position.y);
    }

    /**
     *
     * <code>getHeight</code> returns the height of an arbitrary point on the
     * terrain. If the point is between height point values, the height is
     * linearly interpolated. This provides smooth height calculations. If the
     * point provided is not within the bounds of the height map, the NaN float
     * value is returned (Float.NaN).
     *
     * @param position
     *            the vector representing the height location to check. Only the
     *            x and z values are used.
     * @return the height at the provided location.
     */
    public float getHeight(Vector3f position) {
        return getHeight(position.x, position.z);
    }

    /**
     *
     * <code>getHeight</code> returns the height of an arbitrary point on the
     * terrain. If the point is between height point values, the height is
     * linearly interpolated. This provides smooth height calculations. If the
     * point provided is not within the bounds of the height map, the NaN float
     * value is returned (Float.NaN).
     *
     * @param x
     *            the x coordinate to check.
     * @param z
     *            the z coordinate to check.
     * @return the height at the provided location.
     */
    public float getHeight(float x, float z) {
        x /= stepScale.x;
        z /= stepScale.z;
        float col = FastMath.floor(x);
        float row = FastMath.floor(z);

        if (col < 0 || row < 0 || col >= size - 1 || row >= size - 1) { return Float.NaN; }
        float intOnX = x - col, intOnZ = z - row;

        float topLeft, topRight, bottomLeft, bottomRight;

        int focalSpot = (int) (col + row * size);

        // find the heightmap point closest to this position (but will always
        // be to the left ( < x) and above (< z) of the spot.
        topLeft = heightMap[focalSpot] * stepScale.y;

        // now find the next point to the right of topLeft's position...
        topRight = heightMap[focalSpot + 1] * stepScale.y;

        // now find the next point below topLeft's position...
        bottomLeft = heightMap[focalSpot + size] * stepScale.y;

        // now find the next point below and to the right of topLeft's
        // position...
        bottomRight = heightMap[focalSpot + size + 1] * stepScale.y;

        // Use linear interpolation to find the height.
        return FastMath.LERP(intOnZ, FastMath.LERP(intOnX, topLeft, topRight),
                FastMath.LERP(intOnX, bottomLeft, bottomRight));
    }

    /**
     * <code>buildVertices</code> sets up the vertex and index arrays of the
     * TriMesh.
     */
    private void buildVertices() {
        vertQuantity = heightMap.length;
        vertBuf = BufferUtils.createVector3Buffer(vertBuf, vertQuantity);
        Vector3f point = new Vector3f();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                point.set(x * stepScale.x,
                        heightMap[x + (y * size)] * stepScale.y, y
                                * stepScale.z);
                BufferUtils.setInBuffer(point, vertBuf, (x + (y * size)));
            }
        }

        //set up the indices
        triangleQuantity = ((size - 1) * (size - 1)) * 2;
        indexBuffer = BufferUtils.createIntBuffer(triangleQuantity * 3);

        //go through entire array up to the second to last column.
        for (int i = 0; i < (size * (size - 1)); i++) {
            //we want to skip the top row.
            if (i % ((size * (i / size + 1)) - 1) == 0 && i != 0) {
                continue;
            }
            //set the top left corner.
            indexBuffer.put(i);
            //set the bottom right corner.
            indexBuffer.put((1 + size) + i);
            //set the top right corner.
            indexBuffer.put(1 + i);
            //set the top left corner
            indexBuffer.put(i);
            //set the bottom left corner
            indexBuffer.put(size + i);
            //set the bottom right corner
            indexBuffer.put((1 + size) + i);

        }
    }

    /**
     * <code>buildTextureCoordinates</code> calculates the texture coordinates
     * of the terrain.
     *
     */
    private void buildTextureCoordinates() {
        offset.x += (int) (offsetAmount * stepScale.x);
        offset.y += (int) (offsetAmount * stepScale.z);

        texBuf[0] = BufferUtils.createVector2Buffer(texBuf[0],vertQuantity);
        texBuf[0].clear();

        vertBuf.rewind();
        for (int i = 0; i < vertQuantity; i++) {
            texBuf[0].put((vertBuf.get() + offset.x) / (stepScale.x * (totalSize - 1)));
            vertBuf.get(); // ignore vert y coord.
            texBuf[0].put((vertBuf.get() + offset.y) / (stepScale.z * (totalSize - 1)));
        }
    }

    /**
     *
     * <code>buildNormals</code> calculates the normals of each vertex that
     * makes up the block of terrain.
     *
     *
     */
    private void buildNormals() {
        normBuf = BufferUtils.createVector3Buffer(normBuf, vertQuantity);
        Vector3f oppositePoint = new Vector3f();
        Vector3f adjacentPoint = new Vector3f();
        Vector3f rootPoint = new Vector3f();
        Vector3f tempNorm = new Vector3f();
        int adj = 0, opp = 0, normalIndex = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                BufferUtils.populateFromBuffer(rootPoint, vertBuf, normalIndex);
                if (row == size - 1) {
                    if (col == size - 1) { // last row, last col
                        // up cross left
                        adj = normalIndex-size;
                        opp = normalIndex-1;
                    } else { // last row, except for last col
                        // right cross up
                        adj = normalIndex+1;
                        opp = normalIndex-size;
                    }
                } else {
                    if (col == size - 1) { // last column except for last row
                        // left cross down
                        adj = normalIndex-1;
                        opp = normalIndex+size;
                    } else { // most cases
                        // down cross right
                        adj = normalIndex+size;
                        opp = normalIndex+1;
                    }
                }
                BufferUtils.populateFromBuffer(adjacentPoint, vertBuf, adj);
                BufferUtils.populateFromBuffer(oppositePoint, vertBuf, opp);
                tempNorm.set(adjacentPoint)
	                .subtractLocal(rootPoint)
	                .crossLocal(oppositePoint.subtractLocal(rootPoint))
	                .normalizeLocal();
                BufferUtils.setInBuffer(tempNorm, normBuf, normalIndex);
                normalIndex++;
            }
        }
    }

    /**
     * Sets the colors for each vertex to the color white.
     */
    private void buildColors()
    {
	    setDefaultColor(ColorRGBA.white);
    }

    /**
     * Returns the height map this terrain block is using.
     * @return This terrain block's height map.
     */
    public int[] getHeightMap() {
        return heightMap;
    }

    /**
     * Returns the offset amount this terrain block uses for textures.
     * @return The current offset amount.
     */
    public int getOffsetAmount() {
        return offsetAmount;
    }

    /**
     * Returns the step scale that stretches the height map.
     * @return The current step scale.
     */
    public Vector3f getStepScale() {
        return stepScale;
    }

    /**
     * Returns the total size of the terrain.
     * @return The terrain's total size.
     */
    public int getTotalSize() {
        return totalSize;
    }

    /**
     * Returns the size of this terrain block.
     * @return The current block size.
     */
    public int getSize() {
        return size;
    }

    /**
     * If true, the terrain is created as a ClodMesh.  This is only usefull as a call after the
     *  default constructor.
     * @param useClod
     */
    public void setUseClod(boolean useClod) {
      this.useClod = useClod;
    }

    /**
     * Returns the current offset amount.  This is used when building texture coordinates.
     * @return The current offset amount.
     */
    public Vector2f getOffset() {
        return offset;
    }

    /**
     * Sets the value for the current offset amount to use when building texture coordinates.
     * Note that this does <b>NOT</b> rebuild the terrain at all.  This is mostly used for
     * outside constructors of terrain blocks.
     * @param offset The new texture offset.
     */
    public void setOffset(Vector2f offset) {
        this.offset = offset;
    }

    /**
     * Returns true if this TerrainBlock was created as a clod.
     * @return True if this terrain block is a clod.  False otherwise.
     */
    public boolean isUseClod() {
      return useClod;
    }

    /**
     * Sets the size of this terrain block.  Note that this does <b>NOT</b> rebuild the terrain
     * at all.  This is mostly used for outside constructors of terrain blocks.
     * @param size The new size.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Sets the total size of the terrain .  Note that this does <b>NOT</b> rebuild the terrain
     * at all.  This is mostly used for outside constructors of terrain blocks.
     * @param totalSize The new total size.
     */
    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * Sets the step scale of this terrain block's height map.  Note that this does <b>NOT</b> rebuild
     * the terrain at all.  This is mostly used for outside constructors of terrain blocks.
     * @param stepScale The new step scale.
     */
    public void setStepScale(Vector3f stepScale) {
        this.stepScale = stepScale;
    }

    /**
     * Sets the offset of this terrain texture map.  Note that this does <b>NOT</b> rebuild
     * the terrain at all.  This is mostly used for outside constructors of terrain blocks.
     * @param offsetAmount The new texture offset.
     */
    public void setOffsetAmount(int offsetAmount) {
        this.offsetAmount = offsetAmount;
    }

    /**
     * Sets the terrain's height map.  Note that this does <b>NOT</b> rebuild
     * the terrain at all.  This is mostly used for outside constructors of terrain blocks.
     * @param heightMap The new height map.
     */
    public void setHeightMap(int[] heightMap) {
        this.heightMap = heightMap;
    }

    /**
     * Updates the block's vertices and normals from the current height map values.
     */
    public void updateFromHeightMap() {
        if (!hasChanged()) return;
        
        Vector3f point = new Vector3f();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                point.set(x * stepScale.x,
                        heightMap[x + (y * size)] * stepScale.y, y
                                * stepScale.z);
                BufferUtils.setInBuffer(point, vertBuf, (x + (y * size)));
            }
        }
        buildNormals();
        if (vboInfo != null) {
            vboInfo.setVBOVertexID(-1);
            vboInfo.setVBONormalID(-1);
        }
    }
    
    /**
     * <code>setHeightMapValue</code> sets the value of this block's height
     * map at the given coords
     * 
     * @param x
     * @param y
     * @param newVal
     */
    public void setHeightMapValue(int x, int y, int newVal) {
        heightMap[x + (y * size)] = newVal;
    }
    
    /**
     * <code>setHeightMapValue</code> adds to the value of this block's height
     * map at the given coords
     * 
     * @param x
     * @param y
     * @param toAdd
     */
    public void addHeightMapValue(int x, int y, int toAdd) {
        heightMap[x + (y * size)] += toAdd;
    }
    
    /**
     * <code>setHeightMapValue</code> multiplies the value of this block's height
     * map at the given coords by the value given.
     * 
     * @param x
     * @param y
     * @param toMult
     */
    public void multHeightMapValue(int x, int y, int toMult) {
        heightMap[x + (y * size)] *= toMult;
    }
    
    protected boolean hasChanged() {
        boolean update = false;
        if (oldHeightMap == null) {
            oldHeightMap = new int[heightMap.length];
            update = true;
        }
        
        for (int x = 0; x < oldHeightMap.length; x++)
            if (oldHeightMap[x] != heightMap[x] || update) {
                update = true;
                oldHeightMap[x] = heightMap[x];
            }
        
        return update;
    }
}