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
public class JmePaletteShinySphere implements ActiveEditorDrop {

    public JmePaletteShinySphere() {
    }

    private String createBody() {

        String body = "/** Illuminated bumpy rock with shiny effect. \n *  Uses Texture from jme3-test-data library! Needs light source! */\nSphere rock = new Sphere(32,32, 2f);\nGeometry rock_shiny = new Geometry(\"Shiny rock\", rock);\nrock.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres\nTangentBinormalGenerator.generate(rock);   // for lighting effect\nMaterial mat_shiny = new Material( assetManager, \"Common/MatDefs/Light/Lighting.j3md\");\nmat_shiny.setTexture(\"DiffuseMap\", assetManager.loadTexture(\"Textures/Terrain/Pond/Pond.png\"));\nmat_shiny.setTexture(\"NormalMap\",  assetManager.loadTexture(\"Textures/Terrain/Pond/Pond_normal.png\"));\n//mat_shiny.setTexture(\"GlowMap\", assetManager.loadTexture(\"Textures/glowmap.png\")); // requires flow filter!\nmat_shiny.setBoolean(\"UseMaterialColors\",true);  // needed for shininess\nmat_shiny.setColor(\"Specular\", ColorRGBA.White); // needed for shininess\nmat_shiny.setColor(\"Diffuse\",  ColorRGBA.White); // needed for shininess\nmat_shiny.setFloat(\"Shininess\", 5f); // shininess from 1-128\nrock_shiny.setMaterial(mat_shiny);\nrootNode.attachChild(rock_shiny);";
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
