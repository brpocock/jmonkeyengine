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
package com.jme.scene.state;

import org.lwjgl.opengl.GL;

/**
 * <code>LWJGLZBufferState</code> subclasses ZBufferState to use the 
 * LWJGL API to access OpenGL.
 * @author Mark Powell
 * @version $Id: LWJGLZBufferState.java,v 1.1.1.1 2003-10-29 10:56:40 Anakan Exp $
 */
public class LWJGLZBufferState extends ZBufferState {
    //the open gl depth tests
    private int[] glBufferCompare =
        {
            GL.GL_NEVER,
            GL.GL_LESS,
            GL.GL_EQUAL,
            GL.GL_LEQUAL,
            GL.GL_GREATER,
            GL.GL_NOTEQUAL,
            GL.GL_GEQUAL,
            GL.GL_ALWAYS };
            
    /**
     * <code>set</code> turns on the specified depth test specified by the
     * state.
     * @see com.jme.scene.state.RenderState#set()
     */
    public void set() {
        if (isEnabled()) {
            GL.glEnable(GL.GL_DEPTH_TEST);
            GL.glDepthFunc(glBufferCompare[function]);
        } else {
            GL.glDisable(GL.GL_DEPTH_TEST);
            GL.glDepthFunc(GL.GL_ALWAYS);
        }

        if (writable) {
            GL.glDepthMask(true);
        } else {
            GL.glDepthMask(false);
        }

    }

    /**
     * <code>unset</code> resets the depth test to disabled.
     * @see com.jme.scene.state.RenderState#unset()
     */
    public void unset() {
        GL.glDisable(GL.GL_DEPTH_TEST);
        GL.glDepthFunc(GL.GL_ALWAYS);
    }

}
