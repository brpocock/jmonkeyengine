/*
 * @(#)$Id$
 *
 * Copyright (c) 2009, Blaine Simpson and the jMonkeyEngine Dev Team.
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


import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

import com.jme.util.export.xml.XMLImporter;
import com.jme.bounding.BoundingBox;
import com.jme.scene.Spatial;
import com.jme.app.SimpleGame;

/**
 * @author Blaine Simpson (blaine dot simpson at admc dot com)
 * @see #main(String[])
 */
public class XmlWorld extends SimpleGame {
    private URL[] modelUrls;
    protected XMLImporter xmlImporter = XMLImporter.getInstance();

    /**
     * Instantiate a jME game world, loading the specified jME XML models
     * into the scene.
     *
     * @param args
     *     <CODE><PRE>
     *     Syntax:  java... XmlWorld [-r] file:model1-jme.xml...
     *     </PRE><CODE>
     *     where "-r" means to display the settings widget.
     *
     */
    static public void main(String[] args) throws MalformedURLException {
        int counter = -1;
        List<URL> urls = new ArrayList<URL>();
        XmlWorld xmlWorld = new XmlWorld();
        for (String urlString : args) {
            if (urlString.equals("-r")) {
                xmlWorld.setConfigShowMode(ConfigShowMode.AlwaysShow);
            } else {
                urls.add(new URL(urlString));
            }
        }
        xmlWorld.setModelUrls(urls.toArray(new URL[0]));
        xmlWorld.start();
    }

    public void setModelUrls(URL[] modelUrls) {
        this.modelUrls = modelUrls;
    }

    protected void simpleInitGame() {
        if (modelUrls == null)
            throw new IllegalStateException(XmlWorld.class.getName()
                    + " not initialized properly");
        for (URL url : modelUrls) loadModel(url);
    }

    protected void loadModel(URL modelUrl) {
        // May also be called during update() loop to add to scene.
        Spatial loadedSpatial = null;
        try {
            loadedSpatial = (Spatial) xmlImporter.load(modelUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to load URL: " + modelUrl, e);
        }
        loadedSpatial.setModelBound(new BoundingBox());
        /*
         * This setModelBound is not suitable for general usage.
         * If the model file contains specific model bounds for nested
         * Geometries, this will replace them all with new BoundingBoxes.
         * Since the Blender => jME Exporter doesn't store bounding volumes
         * yet, that's ok, but there's also no opportunity to choose a
         * BoundingSphere or OBB instead of a plain box.
         */
        rootNode.attachChild(loadedSpatial);
        loadedSpatial.updateModelBound();
        // The default update loop will update world bounding volumes.
        rootNode.updateRenderState();
    }
}
