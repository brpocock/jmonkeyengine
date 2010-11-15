/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.gui.multiview;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.OffScenePanel;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.gui.NiftyGuiDataObject;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.tools.resourceloader.FileSystemLocation;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.PanelView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author normenhansen
 */
public class NiftyPreviewPanel extends PanelView {

    private NiftyGuiDataObject niftyObject;
    private OffScenePanel offPanel;
    private Nifty nifty;
    private Document doc;
    private ToolBarDesignEditor comp;
    private String screen = "";

    public NiftyPreviewPanel(NiftyGuiDataObject niftyObject, ToolBarDesignEditor comp) {
        super();
        this.niftyObject = niftyObject;
        this.comp = comp;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        offPanel = new OffScenePanel(640, 480);
        add(offPanel);
        setRoot(Node.EMPTY);
        offPanel.startPreview();
        try {
            doc = XMLUtil.parse(new InputSource(niftyObject.getPrimaryFile().getInputStream()), false, false, null, null);
            screen = XmlHelper.findChildElement(doc.getDocumentElement(), "screen").getAttribute("id");
            if (screen == null) {
                screen = "";
            }
            comp.setContentView(this);
            comp.setRootContext(new NiftyFileNode(doc.getDocumentElement()));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            public Object call() throws Exception {
                preparePreview();
                return null;
            }
        });
    }

    private void preparePreview() {
        ViewPort guiViewPort = offPanel.getViewPort();
        ProjectAssetManager pm = niftyObject.getLookup().lookup(ProjectAssetManager.class);
        if (pm == null) {
            Logger.getLogger(NiftyPreviewPanel.class.getName()).log(Level.WARNING, "No Project AssetManager found!");
            return;
        }
        AssetManager assetManager = pm.getManager();
        AudioRenderer audioRenderer = SceneApplication.getApplication().getAudioRenderer();
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                SceneApplication.getApplication().getInputManager(),
                audioRenderer,
                guiViewPort);
        nifty = niftyDisplay.getNifty();
        de.lessvoid.nifty.tools.resourceloader.ResourceLoader.addResourceLocation(new FileSystemLocation(new File(pm.getAssetFolderName())));
//        nifty.fromXml(pm.getRelativeAssetPath(niftyObject.getPrimaryFile().getPath()), "start");

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);
    }

    public void updatePreView() {
        updatePreView(screen);
    }

    public void updatePreView(final String screen) {
        final ProjectAssetManager pm = niftyObject.getLookup().lookup(ProjectAssetManager.class);
        if (pm == null) {
            Logger.getLogger(NiftyPreviewPanel.class.getName()).log(Level.WARNING, "No Project AssetManager found!");
        }
        this.screen = screen;
        try {
            doc = XMLUtil.parse(new InputSource(niftyObject.getPrimaryFile().getInputStream()), false, false, null, null);
            comp.setRootContext(new NiftyFileNode(doc.getDocumentElement()));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            public Object call() throws Exception {
                nifty.fromXml(pm.getRelativeAssetPath(niftyObject.getPrimaryFile().getPath()), screen);
                return null;
            }
        });
    }

    public void initComponents() {
        super.initComponents();
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
    }

    @Override
    protected Error validateView() {
        return null;
    }

    @Override
    public void showSelection(Node[] nodes) {
        updatePreView(nodes[0].getName());
    }

    public void cleanup() {
        offPanel.stopPreview();
    }
}
