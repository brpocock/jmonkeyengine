package com.g3d.light;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;
import java.io.IOException;

/**
 * A light coming from a certain direction in world space. E.g sun or moon light.
 */
public class DirectionalLight extends Light {

    protected Vector3f direction = new Vector3f(0f, -1f, 0f);

    @Override
    public void computeLastDistance(Spatial owner) {
        lastDistance = 0; // directional lights are always closest to their owner
    }

    /**
     * @return The direction of the light.
     */
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the light.
     * @param dir
     */
    public void setDirection(Vector3f dir){
        direction.set(dir);
    }

    @Override
    public Type getType() {
        return Type.Directional;
    }

    @Override
    public void write(G3DExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(direction, "direction", null);
    }

    @Override
    public void read(G3DImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        direction = (Vector3f) ic.readSavable("direction", null);
    }

}
