package com.g3d.scene.plugins.ogre;

import com.g3d.animation.Bone;
import com.g3d.animation.BoneAnimation;
import com.g3d.animation.BoneTrack;
import com.g3d.animation.Skeleton;
import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetLoader;
import com.g3d.asset.AssetManager;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.util.xml.SAXUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class SkeletonLoader extends DefaultHandler implements AssetLoader {

    private static final Logger logger = Logger.getLogger(SceneLoader.class.getName());

    private AssetManager assetManager;
    private Queue<String> elementStack = new LinkedList<String>();

    private Map<Integer, Bone> indexToBone = new HashMap<Integer, Bone>();
    private Map<String, Bone> nameToBone = new HashMap<String, Bone>();

    private BoneTrack track;
    private List<BoneTrack> tracks = new ArrayList<BoneTrack>();

    private BoneAnimation animation;
    private List<BoneAnimation> animations;

    private Bone bone;
    private Skeleton skeleton;

    private List<Float> times = new ArrayList<Float>();
    private List<Vector3f> translations = new ArrayList<Vector3f>();
    private List<Quaternion> rotations = new ArrayList<Quaternion>();

    private float time = -1;
    private Vector3f position;
    private Quaternion rotation;
    private Vector3f scale;

    private float angle;
    private Vector3f axis;

    public static void main(String[] args) throws IOException{
        File f = new File("E:\\jME3\\src\\models\\ninja.skeletonxml");
        for (int i = 0; i < 100; i++){
            FileInputStream fis = new FileInputStream(f);
            new SkeletonLoader().load(fis);
            fis.close();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException{
        if (qName.equals("position") || qName.equals("translate")){
            position = SAXUtil.parseVector3(attribs);
        }else if (qName.equals("rotation") || qName.equals("rotate")){
            angle = SAXUtil.parseFloat(attribs.getValue("angle"));
        }else if (qName.equals("axis")){
            assert elementStack.peek().equals("rotation")
                || elementStack.peek().equals("rotate");
            axis = SAXUtil.parseVector3(attribs);
        }else if (qName.equals("scale")){
            scale = SAXUtil.parseVector3(attribs);
        }else if (qName.equals("keyframe")){
            assert elementStack.peek().equals("keyframes");
            time = SAXUtil.parseFloat(attribs.getValue("time"));
        }else if (qName.equals("keyframes")){
            assert elementStack.peek().equals("track"); 
        }else if (qName.equals("track")){
            assert elementStack.peek().equals("tracks");
            String boneName = SAXUtil.parseString(attribs.getValue("bone"));
            Bone bone = nameToBone.get(boneName);
            int index = skeleton.getBoneIndex(bone);
            track = new BoneTrack(index);
        }else if (qName.equals("boneparent")){
            assert elementStack.peek().equals("bonehierarchy");
            String boneName = attribs.getValue("bone");
            String parentName = attribs.getValue("parent");
            Bone bone = nameToBone.get(boneName);
            Bone parent = nameToBone.get(parentName);
            parent.addChild(bone);
        }else if (qName.equals("bone")){
            assert elementStack.peek().equals("bones");

            // insert bone into indexed map
            bone = new Bone(attribs.getValue("name"));
            int id = SAXUtil.parseInt(attribs.getValue("id"));
            indexToBone.put(id, bone);
            nameToBone.put(bone.getName(), bone);
        }else if (qName.equals("tracks")){
            assert elementStack.peek().equals("animation");
            tracks.clear();
        }else if (qName.equals("animation")){
            assert elementStack.peek().equals("animations");
            String name = SAXUtil.parseString(attribs.getValue("name"));
            float length = SAXUtil.parseFloat(attribs.getValue("length"));
            animation = new BoneAnimation(name, length);
        }else if (qName.equals("bonehierarchy")){
            assert elementStack.peek().equals("skeleton");
        }else if (qName.equals("animations")){
            assert elementStack.peek().equals("skeleton");
            animations = new ArrayList<BoneAnimation>();
        }else if (qName.equals("bones")){
            assert elementStack.peek().equals("skeleton");
        }else if (qName.equals("skeleton")){
            assert elementStack.size() == 0;
        }
        elementStack.add(qName);
    }

    public void endElement(String uri, String name, String qName) {
        if (qName.equals("translate") || qName.equals("position")){
        }else if (qName.equals("axis")){
        }else if (qName.equals("rotate") || qName.equals("rotation")){
            rotation = new Quaternion();
            axis.normalizeLocal();
            rotation.fromAngleNormalAxis(angle, axis);
            angle = 0;
            axis = null;
        }else if (qName.equals("bone")){
            bone.setBindTransforms(position, rotation, scale);
            bone = null;
            position = null;
            rotation = null;
            scale = null;
        }else if (qName.equals("bonehierarchy")){
            Bone[] bones = new Bone[indexToBone.size()];
            // find bones without a parent and attach them to the skeleton
            // also assign the bones to the bonelist
            for (Map.Entry<Integer, Bone> entry: indexToBone.entrySet()){
                Bone bone = entry.getValue();
                bones[entry.getKey()] = bone;
            }
            indexToBone.clear();
            skeleton = new Skeleton(bones);
        }else if (qName.equals("animation")){
            animations.add(animation);
            animation = null;
        }else if (qName.equals("track")){
            tracks.add(track);
            track = null;
        }else if (qName.equals("tracks")){
            BoneTrack[] trackList = tracks.toArray(new BoneTrack[tracks.size()]);
            animation.setTracks(trackList);
            tracks.clear();
        }else if (qName.equals("keyframe")){
            assert time >= 0;
            assert position != null;
            assert rotation != null;

            times.add(time);
            translations.add(position);
            rotations.add(rotation);

            time = -1;
            position = null;
            rotation = null;
            scale = null;
        }else if (qName.equals("keyframes")){
            float[] timesArray = new float[times.size()];
            for (int i = 0; i < timesArray.length; i++)
                timesArray[i] = times.get(i);

            Vector3f[] transArray = translations.toArray(new Vector3f[translations.size()]);
            Quaternion[] rotArray = rotations.toArray(new Quaternion[rotations.size()]);
            track.setKeyframes(timesArray, transArray, rotArray);

            times.clear();
            translations.clear();
            rotations.clear();
        }else if (qName.equals("skeleton")){
            nameToBone.clear();
        }
        assert elementStack.peek().equals(qName);
        elementStack.remove();
    }

    /**
     * Reset the SkeletonLoader in case an error occured while parsing XML.
     * This allows future use of the loader even after an error.
     */
    private void fullReset(){
        elementStack.clear();
        indexToBone.clear();
        nameToBone.clear();
        track = null;
        tracks.clear();
        animation = null;
        animations.clear();
        bone = null;
        skeleton = null;
        times.clear();
        rotations.clear();
        translations.clear();
        time = -1;
        position = null;
        rotation = null;
        scale = null;
        angle = 0;
        axis = null;
    }

    public Object load(InputStream in) throws IOException{
        try{
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
            InputStreamReader r = new InputStreamReader(in);
            xr.parse(new InputSource(r));
            AnimData data = new AnimData(skeleton, animations);
            skeleton = null;
            animations = null;
            return data;
        } catch (SAXException ex){
            IOException ioEx = new IOException("Error while parsing Ogre3D dotScene");
            ioEx.initCause(ex);
            fullReset();
            throw ioEx;
        }
    }

    public Object load(AssetInfo info) throws IOException {
        assetManager = info.getManager();
        InputStream in = info.openStream();
        Object obj = load(in);
        in.close();
        return obj;
    }

}
