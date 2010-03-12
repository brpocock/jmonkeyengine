package jme3test.asset;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

/**
 * Load an image and display it from the internet using the UrlLocator.
 * @author Kirill Vainer
 */
public class TestUrlLoading extends SimpleApplication {

    public static void main(String[] args){
        TestUrlLoading app = new TestUrlLoading();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // create a simple plane/quad
        Quad quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, true);

        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.updateModelBound();

        manager.registerLocator("http://www.jmonkeyengine.com/images/",
                                "com.jme3.asset.plugins.UrlLocator",
                                "*");
        TextureKey key = new TextureKey("jmeheader.png", false);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);

        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", tex);
        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect * 1.5f, 1.5f, 1));
        quad.center();

        rootNode.attachChild(quad);
    }

}
