/*
 * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
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

package jme.geometry.primitive;

import java.util.logging.Level;

import jme.exception.MonkeyRuntimeException;
import jme.math.Vector;
import jme.utility.LoggingSystem;

import org.lwjgl.opengl.GLU;

/**
 * <code>Cylinder</code> defines a cylindrical geometry. The cylinder is
 * defined by a base radius, top radius, height, slices and stacks. The two
 * radii define the size of the ends of the cylinder. The top radius can be
 * zero, creating a cone. The height defines the length of the cylinder, while
 * the slices and stacks determine the number of vertices that make up the
 * cylinder. The higher the slices and stacks, the better the quality of the
 * image, but framerate will suffer.
 * 
 * @author Mark Powell
 * @version $Id: Cylinder.java,v 1.1.1.1 2003-10-29 10:58:07 Anakan Exp $
 */
public class Cylinder extends Quadric {
    
    //The attributes that define the cylinder
    private double baseRadius;
    private double topRadius;
    private double height;
    private int slices;
    private int stacks;
    
    //GLU object for quadrics
    private GLU glu;
    
    /**
     * Constructor defines the attributes of the cylinder and prepares it
     * to be rendered via a call to <code>initialize</code>
     * 
     * @param baseRadius size of the base.
     * @param topRadius size of the top
     * @param height length of the cylinder.
     * @param slices The number of subdivisions around the z-axis.
     * @param stacks The number of subdivisions along the z-axis.
     * 
     * @throws MonkeyRuntimeException if any parameter other than top radius
     *      is zero or less.
     */
    public Cylinder(double baseRadius, double topRadius, double height,
            int slices, int stacks) {
        
        if(baseRadius <=0 || height <= 0 || slices <= 0 || stacks <= 0
                || topRadius < 0) {
            throw new MonkeyRuntimeException("No parameter may be less than" +
                    " zero, and only toRadius may be zero");
        }
            
        this.baseRadius = baseRadius;
        this.topRadius = topRadius;
        this.height = height;
        this.slices = slices;
        this.stacks = stacks;
        
        
        super.initialize();
        
        LoggingSystem.getLoggingSystem().getLogger().log(Level.INFO,
                "Cylinder created.");
    }
    
    /**
     * <code>render</code> handles rendering the cylinder to the view context.
     */
    public void render() {
        super.preRender();
//        GLU.gluCylinder(quadricPointer, baseRadius, topRadius, height, slices,
//                stacks);
        super.clean();
    }
    
    /**
     * <code>setBaseRadius</code> sets the base size of the cylinder.
     * 
     * @param baseRadius the new base size of the cylinder.
     * 
     * @throws MonkeyRuntimeException if the baseRadius is less than or equal
     *      to zero.
     */
    public void setBaseRadius(double baseRadius) {
        if(baseRadius <= 0) {
            throw new MonkeyRuntimeException("baseRadius must be greater " + 
                    "than zero");
        }
        
        this.baseRadius = baseRadius;
    }

    /**
     * <code>setTopRadius</code> sets the top size of the cylinder. If it is
     * set to zero, the cylinder is essentially a cone.
     * 
     * @param topRadius the size of the top of the cylinder.
     * 
     * @throws MonkeyRuntimeException if the top radius is less than zero.
     */
    public void setTopRadius(double topRadius) {
        if(topRadius < 0) {
            throw new MonkeyRuntimeException("topRadius must be greater " +
                    "than or equal to zero");
        }
        this.topRadius = topRadius;
    }
    
    /**
     * <code>setHeight</code> sets the length of the cylinder.
     * 
     * @param height the new height of the cylinder.
     * 
     * @throws MonkeyRuntimeException if the top radius is less than or
     *      equal to zero.
     */
    public void setHeight(double height) {
        if(height <= 0) {
            throw new MonkeyRuntimeException("Height must be greater than " + 
                    "zero");
        }
        this.height = height;
    }
    
    /**
     * <code>setSlices</code> sets the number of slices for this cylinder.
     * 
     * @param slices the number of slices for the cylinder.
     * @throws MonkeyRuntimeException if slices is less than or equal to zero.
     */
    public void setSlices(int slices) {
        if(slices <= 0) {
            throw new MonkeyRuntimeException("slices must be greater " +
                    "than zero");
        }
        this.slices = slices;
    }
    
    /**
     * <code>setStacks</code> sets the number of stacks for this cylinder.
     * 
     * @param stacks the number of stacks for the cylinder.
     * 
     * @throws MonkeyRuntimeException if stacks is less than or equal to zero.
     */
    public void setStacks(int stacks) {
        if(stacks <= 0) {
            throw new MonkeyRuntimeException("Stacks must be greater " +
                    "than zero");
        }
        this.stacks = stacks;
    }
    
    public Vector[] getPoints() {
        return null;
    }
}
