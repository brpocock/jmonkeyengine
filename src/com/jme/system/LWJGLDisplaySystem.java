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
package com.jme.system;

import java.awt.Toolkit;

import org.lwjgl.Display;
import org.lwjgl.DisplayMode;
import org.lwjgl.opengl.Window;

import com.jme.renderer.LWJGLRenderer;
import com.jme.renderer.Renderer;

/**
 * <code>LWJGLDisplaySystem</code> defines an implementation of 
 * <code>DisplaySystem</code> that uses the LWJGL API for window creation
 * and rendering via OpenGL. <code>LWJGLRenderer</code> is also created
 * that gives a way of displaying data to the created window.
 * 
 * @author Mark Powell
 * @version $Id: LWJGLDisplaySystem.java,v 1.5 2003-10-28 19:59:05 mojomonkey Exp $
 */
public class LWJGLDisplaySystem extends DisplaySystem {

    private int bpp;
    private int frq;
    private String title = "";
    private boolean fs;
    private boolean created;
    private LWJGLRenderer renderer;
    
    /**
     * Constructor instantiates a new <code>LWJGLDisplaySystem</code> object. 
     * During instantiation confirmation is made to determine if the 
     * LWJGL API is installed properly. If not, a JmeException is thrown.
     *
     */
    public LWJGLDisplaySystem() {
        try {
            System.loadLibrary("lwjgl");
        } catch (UnsatisfiedLinkError e) {
            throw new JmeException("LWJGL library not set.");
        }
        
    }

    /**
     * <code>createWindow</code> will create a LWJGL display context. This 
     * window will be a purely native context as defined by the LWJGL API.
     * 
     * @see com.jme.system.DisplaySystem#createWindow(int, int, int, int, boolean)
     */
    public void createWindow(int w, int h, int bpp, int frq, boolean fs) {
        //confirm that the parameters are valid.
        if (w <= 0 || h <= 0) {
            throw new JmeException("Invalid resolution values: " + w + " " + h);
        } else if ((bpp != 32) && (bpp != 16) && (bpp != 24)) {
            throw new JmeException("Invalid pixel depth: " + bpp);
        }

        //set the window attributes
        this.width = w;
        this.height = h;
        this.bpp = bpp;
        this.frq = frq;
        this.fs = fs;

        initDisplay();
        renderer = new LWJGLRenderer(width,height);
    
        created = true;
    }

    /**
     * <code>getRenderer</code> returns the created rendering class for 
     * LWJGL (<code>LWJGLRenderer</code>). This will give the needed access to
     * display data to the window.
     * @see com.jme.system.DisplaySystem#getRenderer()
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * <code>isCreated</code> returns true if the current display is created,
     * false otherwise.
     * @see com.jme.system.DisplaySystem#isCreated()
     * @return true if display is created.
     */
    public boolean isCreated() {
        return created;
    }
    
    /**
     * <code>isClosing</code> returns any close requests. True if any exist,
     * false otherwise.
     * @see com.jme.system.DisplaySystem#isClosing()
     * @return true if a close request is active.
     */
    public boolean isClosing() {
        return Window.isCloseRequested();
    }
    
    /**
     * <code>reset</code> prepares the window for closing or restarting.
     * @see com.jme.system.DisplaySystem#reset()
     */
    public void reset() {
        Display.resetDisplayMode();
    }

    /**
     * <code>getValidDisplayMode</code> returns a <code>DisplayMode</code> object
     * that has the requested width, height and color depth. If there is no
     * mode that supports a requested resolution, null is returned.
     * 
     * @param width the width of the desired mode.
     * @param height the height of the desired mode.
     * @param bpp the color depth of the desired mode.
     * @param freq the frequency of the monitor.
     * @return <code>DisplayMode</code> object that supports the requested
     *      resolutions. Null is returned if no valid modes are found.
     */
    private DisplayMode getValidDisplayMode(
        int width,
        int height,
        int bpp,
        int freq) {
        //get all the modes, and find one that matches our width, height, bpp.
        DisplayMode[] modes = Display.getAvailableDisplayModes();
        //Make sure that we find the mode that uses our current monitor freq.

        for (int i = 0; i < modes.length; i++) {
            if (modes[i].width == width
                && modes[i].height == height
                && modes[i].bpp == bpp
                && modes[i].freq == freq) {

                return modes[i];
            }
        }

        //none found
        return null;
    }

    /**
     * <code>initDisplay</code> creates the LWJGL window with the desired
     * specifications. 
     *
     */
    private void initDisplay() {
        try {
            //create the Display. 
            DisplayMode mode = getValidDisplayMode(width, height, bpp, frq);
            if (null == mode) {
                throw new JmeException("Bad display mode");
            }

            if (fs) {
                Display.setDisplayMode(mode);
                Window.create(title, bpp, 0, 8, 0);
            } else {
                int x, y;
                x =
                    (Toolkit.getDefaultToolkit().getScreenSize().width - width)
                        / 2;
                y =
                    (Toolkit.getDefaultToolkit().getScreenSize().height
                        - height)
                        / 2;
                Window.create(title, x, y, width, height, bpp, 0, 8, 0);
            }

        } catch (Exception e) {
            System.exit(1);
        }
    }
}
