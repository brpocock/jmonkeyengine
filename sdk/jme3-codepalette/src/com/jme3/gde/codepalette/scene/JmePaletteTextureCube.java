/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.gde.codepalette.scene;
import com.jme3.gde.codepalette.JmePaletteUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author normenhansen, zathras
 */
public class JmePaletteTextureCube implements ActiveEditorDrop {

    public JmePaletteTextureCube() {
    }

    private String createBody() {

        String body = "/** An unshaded textured cube. \n *  Uses texture from jme3-test-data library! */ \nBox boxshape1 = new Box(Vector3f.ZERO, 1f,1f,1f); \nGeometry cube_tex = new Geometry(\"A Textured Box\", boxshape1); \nMaterial mat_tex = new Material(assetManager, \"Common/MatDefs/Misc/Unshaded.j3md\"); \nTexture tex = assetManager.loadTexture(\"Interface/Logo/Monkey.jpg\"); \nmat_tex.setTexture(\"ColorMap\", tex); \ncube_tex.setMaterial(mat_tex); \nrootNode.attachChild(cube_tex); ";
        return body;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        String body = createBody();
        try {
            JmePaletteUtilities.insert(body, targetComponent);
        } catch (BadLocationException ble) {
            return false;
        }
        return true;
    }

}
