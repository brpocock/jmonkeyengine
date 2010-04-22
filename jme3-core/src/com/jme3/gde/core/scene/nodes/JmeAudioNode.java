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
package com.jme3.gde.core.scene.nodes;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioNode.Status;
import com.jme3.audio.Filter;
import com.jme3.gde.core.scene.nodes.properties.JmeProperty;
import com.jme3.math.Vector3f;
import java.awt.Image;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author normenhansen
 */
public class JmeAudioNode extends JmeNode {

    private static Image smallImage =
            ImageUtilities.loadImage("com/jme3/gde/core/scene/nodes/icons/audionode.gif");
    private AudioNode node;

    public JmeAudioNode(AudioNode spatial, JmeSpatialChildFactory children) {
        super(spatial, children);
        getLookupContents().add(spatial);
        this.node = spatial;
        setName(spatial.getName());
    }

    @Override
    public Image getIcon(int type) {
        return smallImage;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return smallImage;
    }

    @Override
    protected Sheet createSheet() {
        //TODO: multithreading..
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("AudioNode");
        set.setName(AudioNode.class.getName());
        AudioNode obj = node;//getLookup().lookup(Spatial.class);
        if (obj == null) {
            return sheet;
        }
//        obj.set

        set.put(makeProperty(obj, int.class, "getChannel", "setChannel", "channel"));
        set.put(makeProperty(obj, Vector3f.class, "getDirection", "setDirection", "direction"));
        set.put(makeProperty(obj, boolean.class, "isDirectional", "setDirectional", "directional"));
        set.put(makeProperty(obj, float.class, "getInnerAngle", "setInnerAngle", "inner angle"));
        set.put(makeProperty(obj, float.class, "getOuterAngle", "setOuterAngle", "outer angle"));
        set.put(makeProperty(obj, Filter.class, "getDryFilter", "setDryFilter", "dry filter"));
        set.put(makeProperty(obj, boolean.class, "isLooping", "setLooping", "looping"));
        set.put(makeProperty(obj, float.class, "getMaxDistance", "setMaxDistance", "max distance"));

        set.put(makeProperty(obj, float.class, "getPitch", "setPitch", "audio pitch"));
        set.put(makeProperty(obj, boolean.class, "isPositional", "setPositional", "positional"));

        set.put(makeProperty(obj, boolean.class, "isReverbEnabled", "setReverbEnabled", "reverb"));
        set.put(makeProperty(obj, Filter.class, "getReverbFilter", "setReverbFilter", "reverb filter"));
        set.put(makeProperty(obj, float.class, "getRefDistance", "setRefDistance", "ref distance"));
        set.put(makeProperty(obj, float.class, "getTimeOffset", "setTimeOffset", "time offset"));

        set.put(makeProperty(obj, Status.class, "getStatus", "setStatus", "status"));

        set.put(makeProperty(obj, float.class, "getVolume", "setVolume", "volume"));
        set.put(makeProperty(obj, Vector3f.class, "getVelocity", "setVelocity", "velocity"));
        sheet.put(set);
        return sheet;

    }

    private Property makeProperty(AudioNode obj, Class returntype, String method, String name) {
        Property prop = null;
        try {
            prop = new JmeProperty(obj, returntype, method, null);
            prop.setName(name);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }

    private Property makeProperty(AudioNode obj, Class returntype, String method, String setter, String name) {
        Property prop = null;
        try {
            prop = new JmeProperty(obj, returntype, method, setter);
            prop.setName(name);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }
}
