/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.materials.multiview;

import com.jme3.asset.AssetKey;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.nodes.SaveNode;
import com.jme3.gde.core.scene.PreviewRequest;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.SceneListener;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.materials.MaterialProperties;
import com.jme3.gde.materials.MaterialProperty;
import com.jme3.gde.materials.multiview.widgets.MaterialPropertyWidget;
import com.jme3.gde.materials.multiview.widgets.MaterialWidgetListener;
import com.jme3.gde.materials.multiview.widgets.WidgetFactory;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableTopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.jme3.gde.materials.multiview//MaterialEditor//EN",
autostore = false)
public final class MaterialEditorTopComponent extends CloneableTopComponent implements SceneListener, MaterialWidgetListener {

    private static MaterialEditorTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "MaterialEditorTopComponent";
    private Lookup lookup;
    private final InstanceContent lookupContents = new InstanceContent();
    private SaveNode saveNode;
    private DataObject dataObject;
    private MaterialProperties properties;
    private String materialFileName;
    private ProjectAssetManager manager;
    private Sphere sphMesh;

    public MaterialEditorTopComponent() {
    }

    public MaterialEditorTopComponent(DataObject dataObject) {
        this.dataObject = dataObject;
        materialFileName = dataObject.getPrimaryFile().getPath();
        initWindow();
    }

    private void initWindow() {
        this.manager = dataObject.getLookup().lookup(ProjectAssetManager.class);
        properties = new MaterialProperties(dataObject.getPrimaryFile(), dataObject.getLookup().lookup(ProjectAssetManager.class));
        properties.read();
        initComponents();
        setName(NbBundle.getMessage(MaterialEditorTopComponent.class, "CTL_MaterialEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(MaterialEditorTopComponent.class, "HINT_MaterialEditorTopComponent"));
        saveNode = new SaveNode(new SaveCookieImpl());

        try {
            jTextArea1.setText(dataObject.getPrimaryFile().asText());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        jTextArea1.getDocument().addDocumentListener(new DocumentChangeListener());

        SceneApplication.getApplication().addSceneListener(this);

        updateProperties();

        setActivatedNodes(new Node[]{saveNode});

        sphMesh = new Sphere(32, 32, 2.5f);
        sphMesh.setTextureMode(Sphere.TextureMode.Projected);
        sphMesh.updateGeometry(32, 32, 2.5f, false, false);
        TangentBinormalGenerator.generate(sphMesh);
        showMaterial();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane2.setViewportView(jPanel3);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(MaterialEditorTopComponent.class, "MaterialEditorTopComponent.jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(MaterialEditorTopComponent.class, "MaterialEditorTopComponent.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMinimumSize(new java.awt.Dimension(200, 120));
        jToolBar1.setPreferredSize(new java.awt.Dimension(600, 120));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MaterialEditorTopComponent.class, "MaterialEditorTopComponent.jLabel2.text")); // NOI18N
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                reloadPreview(evt);
            }
        });
        jToolBar1.add(jLabel2);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 70, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 116, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MaterialEditorTopComponent.class, "MaterialEditorTopComponent.jLabel1.text")); // NOI18N
        jToolBar1.add(jLabel1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Common/MatDefs/Misc/SolidColor.j3md", "Common/MatDefs/Misc/VertexColor.j3md", "Common/MatDefs/Misc/SimpleTextured.j3md", "Common/MatDefs/Misc/ColoredTextured.j3md", "Common/MatDefs/Misc/Particle.j3md", "Common/MatDefs/Misc/Sky.j3md", "Common/MatDefs/Gui/Gui.j3md", "Common/MatDefs/Light/Lighting.j3md", "Common/MatDefs/Light/Reflection.j3md", "Common/MatDefs/Misc/ShowNormals.j3md", "Common/MatDefs/Hdr/LogLum.j3md", "Common/MatDefs/Hdr/ToneMap.j3md", "Common/MatDefs/Shadow/PreShadow.j3md", "Common/MatDefs/Shadow/PostShadow.j3md" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jComboBox1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if(properties!=null){
            properties.setMatDefName((String)jComboBox1.getSelectedItem());
            String string=properties.getUpdatedContent();
            jTextArea1.setText(string);
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void reloadPreview(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reloadPreview
        showMaterial();
    }//GEN-LAST:event_reloadPreview

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized MaterialEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new MaterialEditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the MaterialEditorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized MaterialEditorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(MaterialEditorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof MaterialEditorTopComponent) {
            return (MaterialEditorTopComponent) win;
        }
        Logger.getLogger(MaterialEditorTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;//ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty("MaterialFileName", materialFileName);
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        try {
            String version = p.getProperty("version");
            materialFileName = p.getProperty("MaterialFileName");
            // TODO read your settings according to their version
            dataObject = DataObject.find(FileUtil.toFileObject(new File(materialFileName)));
            initWindow();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private class DocumentChangeListener implements DocumentListener {

        String newline = "\n";

        public void insertUpdate(DocumentEvent e) {
            saveNode.fire(true);
        }

        public void removeUpdate(DocumentEvent e) {
            saveNode.fire(true);
        }

        public void changedUpdate(DocumentEvent e) {
            saveNode.fire(true);
        }

        public void updateLog(DocumentEvent e, String action) {
            saveNode.fire(true);
        }
    }

    private class SaveCookieImpl implements SaveCookie {

        public void save() throws IOException {
            FileObject file = dataObject.getPrimaryFile();
            String text = jTextArea1.getText();
            OutputStreamWriter out = new OutputStreamWriter(file.getOutputStream());
            out.write(text, 0, text.length());
            out.close();
            saveNode.fire(false);
            properties.read();
            updateProperties();
            showMaterial();
        }
    }

    public void setMatDefList(final String[] strings, String selected){
        MaterialProperties prop=properties;
        properties=null;
        jComboBox1.removeAllItems();
        jComboBox1.addItem("");

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            jComboBox1.addItem(string);
        }

        jComboBox1.addItem("Common/MatDefs/Misc/SolidColor.j3md");
        jComboBox1.addItem("Common/MatDefs/Misc/VertexColor.j3md");
        jComboBox1.addItem("Common/MatDefs/Misc/SimpleTextured.j3md");
        jComboBox1.addItem("Common/MatDefs/Misc/ColoredTextured.j3md");
        jComboBox1.addItem("Common/MatDefs/Misc/Particle.j3md");
        jComboBox1.addItem("Common/MatDefs/Misc/Sky.j3md");
        jComboBox1.addItem("Common/MatDefs/Gui/Gui.j3md");
        jComboBox1.addItem("Common/MatDefs/Light/Lighting.j3md");
        jComboBox1.addItem("Common/MatDefs/Light/Reflection.j3md");
        jComboBox1.addItem("Common/MatDefs/Misc/ShowNormals.j3md");
        jComboBox1.addItem("Common/MatDefs/Hdr/LogLum.j3md");
        jComboBox1.addItem("Common/MatDefs/Hdr/ToneMap.j3md");
        jComboBox1.addItem("Common/MatDefs/Shadow/PreShadow.j3md");
        jComboBox1.addItem("Common/MatDefs/Shadow/PostShadow.j3md");
        jComboBox1.setSelectedItem(selected);

        properties=prop;
    }

    private void updateProperties() {
        setMatDefList(manager.getMatDefs(),properties.getMatDefName());
        
        for (int i = 0; i < jPanel3.getComponents().length; i++) {
            Component component = jPanel3.getComponents()[i];
            if (component instanceof MaterialPropertyWidget) {
                ((MaterialPropertyWidget) component).registerChangeListener(null);
            }
        }
        
        jPanel3.removeAll();
        for (Iterator<Entry<String, MaterialProperty>> it = properties.getMap().entrySet().iterator(); it.hasNext();) {
            Entry<String, MaterialProperty> entry = it.next();
            MaterialPropertyWidget widget = WidgetFactory.getWidget(entry.getValue(), manager);
            widget.registerChangeListener(this);
            jPanel3.add(widget);
        }
        jScrollPane2.repaint();
        setDisplayName(properties.getName() + " - " + properties.getMaterialPath());
    }

    private void showMaterial() {
        try {

            AssetKey key = new AssetKey(manager.getRelativeAssetPath(materialFileName));
            Geometry geom = new Geometry("TestSphere", sphMesh);
            ((DesktopAssetManager) manager.getManager()).deleteFromCache(key);
            geom.setMaterial((Material) manager.getManager().loadAsset(key));
            if(geom.getMaterial()!=null){
                PreviewRequest request = new PreviewRequest(this, geom);
                SceneApplication.getApplication().createPreview(request);
            }
        } catch (Exception e) {
        }
    }

    public void sceneRequested(SceneRequest request) {
    }

    public void previewRequested(PreviewRequest request) {
        if (request.getRequester() == this) {
            final ImageIcon icon = new ImageIcon(request.getImage());
            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    jLabel2.setIcon(icon);
                }
            });
        }
    }

    public void propertyChanged(MaterialProperty property) {
        String string=properties.getUpdatedContent();
        jTextArea1.setText(string);
    }
}
