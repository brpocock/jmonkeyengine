/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
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
package com.jme3.gde.terraineditor;

import com.jme3.bounding.BoundingBox;
import com.jme3.gde.core.assets.AssetDataObject;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.PreviewRequest;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.SceneListener;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.core.sceneexplorer.nodes.JmeNode;
import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.gde.core.sceneexplorer.nodes.NodeUtility;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.ProgressMonitor;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.jme3.gde.terraineditor//TerrainEditor//EN",
autostore = false)
public final class TerrainEditorTopComponent extends TopComponent implements SceneListener, LookupListener {

    private static TerrainEditorTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "com/jme3/gde/terraineditor/TerraMonkey.png";
    private static final String PREFERRED_ID = "TerrainEditorTopComponent";
    private final Result<JmeSpatial> result;
    TerrainCameraController camController;
    TerrainToolController toolController;
    TerrainEditorController editorController;
    private SceneRequest currentRequest;
    private javax.swing.JToggleButton currentSelectedButton;

    public enum TerrainEditButton {none, raiseTerrain, lowerTerrain, smoothTerrain, levelTerrain, paintTerrain, eraseTerrain};

    private HelpCtx ctx = new HelpCtx("com.jme3.gde.terraineditor.usage");

    public TerrainEditorTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TerrainEditorTopComponent.class, "CTL_TerrainEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(TerrainEditorTopComponent.class, "HINT_TerrainEditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        result = Utilities.actionsGlobalContext().lookupResult(JmeSpatial.class);
    }


    class EntropyCalcProgressMonitor implements ProgressMonitor {

        private ProgressHandle progressHandle;
        private float progress = 0;
        private float max = 0;
        private final Object lock = new Object();

        public void incrementProgress(float f) {
            progress += f;
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    synchronized(lock) {
                        progressHandle.progress((int)progress);
                        Logger.getLogger(TerrainEditorTopComponent.class.getName()).info("######         generated entropy " + progress);
                    }
                }
            });
        }

        public void setMonitorMax(float f) {
            max = f;
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    synchronized(lock){
                        if (progressHandle == null) {
                            progressHandle = ProgressHandleFactory.createHandle("Calculating terrain entropies...");
                            progressHandle.start((int) max);
                        }
                    }
                }
            });
        }

        public float getMonitorMax() {
            return max;
        }

        public void progressComplete() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressHandle.finish();
                }
            });
        }

    }

    private void setHintText(String text) {
        hintTextArea.setText(text);
    }

    private void setHintText(TerrainEditButton terrainEditButton) {
        if (TerrainEditButton.none.equals(terrainEditButton) )
            hintTextArea.setText("");
        else
            hintTextArea.setText("Switch between camera and tool controls by holding down SHIFT");
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        terrainModButtonGroup = new ToggleButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        createTerrainButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        raiseTerrainButton = new javax.swing.JToggleButton();
        lowerTerrainButton = new javax.swing.JToggleButton();
        smoothTerrainButton = new javax.swing.JToggleButton();
        roughTerrainButton = new javax.swing.JToggleButton();
        toolSettingsPanel = new javax.swing.JPanel();
        radiusLabel = new javax.swing.JLabel();
        radiusSlider = new javax.swing.JSlider();
        heightSlider = new javax.swing.JSlider();
        heightLabel = new javax.swing.JLabel();
        terrainOpsPanel = new javax.swing.JPanel();
        genEntropiesButton = new javax.swing.JButton();
        hintPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        hintTextArea = new javax.swing.JTextArea();

        jToolBar1.setRollover(true);

        createTerrainButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jme3/gde/terraineditor/icon_terrain-new.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(createTerrainButton, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.createTerrainButton.text")); // NOI18N
        createTerrainButton.setToolTipText(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.createTerrainButton.toolTipText")); // NOI18N
        createTerrainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createTerrainButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(createTerrainButton);
        jToolBar1.add(jSeparator1);

        terrainModButtonGroup.add(raiseTerrainButton);
        raiseTerrainButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jme3/gde/terraineditor/icon_terrain-up.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(raiseTerrainButton, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.raiseTerrainButton.text")); // NOI18N
        raiseTerrainButton.setToolTipText(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.raiseTerrainButton.toolTipText")); // NOI18N
        raiseTerrainButton.setActionCommand(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.raiseTerrainButton.actionCommand")); // NOI18N
        raiseTerrainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                raiseTerrainButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(raiseTerrainButton);

        terrainModButtonGroup.add(lowerTerrainButton);
        lowerTerrainButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jme3/gde/terraineditor/icon_terrain-down.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lowerTerrainButton, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.lowerTerrainButton.text")); // NOI18N
        lowerTerrainButton.setToolTipText(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.lowerTerrainButton.toolTipText")); // NOI18N
        lowerTerrainButton.setActionCommand(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.lowerTerrainButton.actionCommand")); // NOI18N
        lowerTerrainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowerTerrainButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(lowerTerrainButton);

        terrainModButtonGroup.add(smoothTerrainButton);
        smoothTerrainButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jme3/gde/terraineditor/icon_terrain-smooth.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(smoothTerrainButton, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.smoothTerrainButton.text")); // NOI18N
        smoothTerrainButton.setToolTipText(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.smoothTerrainButton.toolTipText")); // NOI18N
        smoothTerrainButton.setActionCommand(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.smoothTerrainButton.actionCommand")); // NOI18N
        smoothTerrainButton.setEnabled(false);
        jToolBar1.add(smoothTerrainButton);

        terrainModButtonGroup.add(roughTerrainButton);
        roughTerrainButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jme3/gde/terraineditor/icon_terrain-rough.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(roughTerrainButton, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.roughTerrainButton.text")); // NOI18N
        roughTerrainButton.setToolTipText(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.roughTerrainButton.toolTipText")); // NOI18N
        roughTerrainButton.setActionCommand(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.roughTerrainButton.actionCommand")); // NOI18N
        roughTerrainButton.setEnabled(false);
        jToolBar1.add(roughTerrainButton);

        toolSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.toolSettingsPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(radiusLabel, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.radiusLabel.text")); // NOI18N

        radiusSlider.setMajorTickSpacing(5);
        radiusSlider.setMaximum(20);
        radiusSlider.setMinorTickSpacing(1);
        radiusSlider.setPaintTicks(true);
        radiusSlider.setSnapToTicks(true);
        radiusSlider.setToolTipText(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.radiusSlider.toolTipText")); // NOI18N
        radiusSlider.setValue(5);
        radiusSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                radiusSliderPropertyChange(evt);
            }
        });

        heightSlider.setMajorTickSpacing(20);
        heightSlider.setMaximum(200);
        heightSlider.setPaintTicks(true);
        heightSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                heightSliderPropertyChange(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(heightLabel, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.heightLabel.text")); // NOI18N

        javax.swing.GroupLayout toolSettingsPanelLayout = new javax.swing.GroupLayout(toolSettingsPanel);
        toolSettingsPanel.setLayout(toolSettingsPanelLayout);
        toolSettingsPanelLayout.setHorizontalGroup(
            toolSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolSettingsPanelLayout.createSequentialGroup()
                .addGroup(toolSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(radiusLabel)
                    .addComponent(heightLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(toolSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(heightSlider, 0, 0, Short.MAX_VALUE)
                    .addComponent(radiusSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        toolSettingsPanelLayout.setVerticalGroup(
            toolSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolSettingsPanelLayout.createSequentialGroup()
                .addGroup(toolSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radiusSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radiusLabel))
                .addGap(21, 21, 21)
                .addGroup(toolSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(heightLabel)
                    .addComponent(heightSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        terrainOpsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.terrainOpsPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(genEntropiesButton, org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.genEntropiesButton.text")); // NOI18N
        genEntropiesButton.setToolTipText(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.genEntropiesButton.toolTipText")); // NOI18N
        genEntropiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genEntropiesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout terrainOpsPanelLayout = new javax.swing.GroupLayout(terrainOpsPanel);
        terrainOpsPanel.setLayout(terrainOpsPanelLayout);
        terrainOpsPanelLayout.setHorizontalGroup(
            terrainOpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(terrainOpsPanelLayout.createSequentialGroup()
                .addComponent(genEntropiesButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        terrainOpsPanelLayout.setVerticalGroup(
            terrainOpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(terrainOpsPanelLayout.createSequentialGroup()
                .addComponent(genEntropiesButton)
                .addContainerGap(55, Short.MAX_VALUE))
        );

        hintPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TerrainEditorTopComponent.class, "TerrainEditorTopComponent.hintPanel.border.title"))); // NOI18N

        hintTextArea.setColumns(20);
        hintTextArea.setEditable(false);
        hintTextArea.setLineWrap(true);
        hintTextArea.setRows(2);
        hintTextArea.setTabSize(4);
        hintTextArea.setWrapStyleWord(true);
        hintTextArea.setFocusable(false);
        hintTextArea.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(hintTextArea);

        javax.swing.GroupLayout hintPanelLayout = new javax.swing.GroupLayout(hintPanel);
        hintPanel.setLayout(hintPanelLayout);
        hintPanelLayout.setHorizontalGroup(
            hintPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(hintPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                .addContainerGap())
        );
        hintPanelLayout.setVerticalGroup(
            hintPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(hintPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(toolSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(terrainOpsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(toolSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(terrainOpsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hintPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createTerrainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createTerrainButtonActionPerformed
        addSpatial("Terrain", Vector3f.ZERO);
    }//GEN-LAST:event_createTerrainButtonActionPerformed

    private void raiseTerrainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_raiseTerrainButtonActionPerformed

        //toolController.setShowGrid(true);

        if (raiseTerrainButton.isSelected()) {
            toolController.setTerrainEditButtonState(TerrainEditButton.raiseTerrain);
            setHintText(TerrainEditButton.raiseTerrain);
        } else {
            toolController.setTerrainEditButtonState(TerrainEditButton.none);
            setHintText(TerrainEditButton.none);
        }
    }//GEN-LAST:event_raiseTerrainButtonActionPerformed

    private void lowerTerrainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowerTerrainButtonActionPerformed
        if (lowerTerrainButton.isSelected()) {
            toolController.setTerrainEditButtonState(TerrainEditButton.lowerTerrain);
            setHintText(TerrainEditButton.lowerTerrain);
        } else {
            toolController.setTerrainEditButtonState(TerrainEditButton.none);
            setHintText(TerrainEditButton.none);
        }
    }//GEN-LAST:event_lowerTerrainButtonActionPerformed

    private void radiusSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_radiusSliderPropertyChange
        if (toolController != null)
            toolController.setHeightToolRadius(radiusSlider.getValue());
    }//GEN-LAST:event_radiusSliderPropertyChange

    private void heightSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_heightSliderPropertyChange
        if (toolController != null)
            toolController.setHeightToolHeight(heightSlider.getValue()); // should always be values upto and over 100, because it will be divided by 100
    }//GEN-LAST:event_heightSliderPropertyChange

    private void genEntropiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genEntropiesButtonActionPerformed
        if (editorController != null) {
            setHintText("Run entropy generation when you are finished modifying the terrain's height. It is a slow process but required for some LOD operations.");
            EntropyCalcProgressMonitor monitor = new EntropyCalcProgressMonitor();
            editorController.generateEntropies(monitor);
        }
    }//GEN-LAST:event_genEntropiesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createTerrainButton;
    private javax.swing.JButton genEntropiesButton;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JSlider heightSlider;
    private javax.swing.JPanel hintPanel;
    private javax.swing.JTextArea hintTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton lowerTerrainButton;
    private javax.swing.JLabel radiusLabel;
    private javax.swing.JSlider radiusSlider;
    private javax.swing.JToggleButton raiseTerrainButton;
    private javax.swing.JToggleButton roughTerrainButton;
    private javax.swing.JToggleButton smoothTerrainButton;
    private javax.swing.ButtonGroup terrainModButtonGroup;
    private javax.swing.JPanel terrainOpsPanel;
    private javax.swing.JPanel toolSettingsPanel;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TerrainEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new TerrainEditorTopComponent();
        }
        return instance;
    }

    /*****************************************************
     * Move to external controller
     */
    //TODO move to controller

    private JmeSpatial selectedSpat;

    public void addSpatial(final String name, final Vector3f point) {
        if (selectedSpat == null) {
            
            Confirmation msg = new NotifyDescriptor.Confirmation(
                            "You must select a Node to add the terrain to in the Scene Explorer window",
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        
        final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
        if (node != null) {

            //setNeedsSave(true);

            if ("Terrain".equals(name)) {
                CreateTerrainWizardAction wiz = new CreateTerrainWizardAction(this);
                wiz.performAction();
            } else {

            }
        }
        
    }
    

    protected void generateTerrain(final WizardDescriptor wizardDescriptor) {
        final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
        try {
            SceneApplication.getApplication().enqueue(new Callable() {

                public Object call() throws Exception {
                    
                    int totalSize = (Integer) wizardDescriptor.getProperty("totalSize");
                    int patchSize = (Integer) wizardDescriptor.getProperty("patchSize");

                    float[] heightmapData = null;
                    AbstractHeightMap heightmap = (AbstractHeightMap) wizardDescriptor.getProperty("abstractHeightMap");
                    if (heightmap != null) {
                        heightmap.load(); // can take a while
                        heightmapData = heightmap.getHeightMap();
                    }

                    TerrainQuad terrain = new TerrainQuad("terrain", patchSize, totalSize, heightmapData);
                    com.jme3.material.Material mat = new com.jme3.material.Material(SceneApplication.getApplication().getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
                    mat.setColor("Color", ColorRGBA.Brown);
                    /*com.jme3.material.Material mat = new com.jme3.material.Material(SceneApplication.getApplication().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
                    mat.setFloat("Shininess", 1);
                    mat.setBoolean("LowQuality", true);
                    mat.setBoolean("UseMaterialColors", true);
                    mat.setColor("Diffuse", ColorRGBA.Brown);
                    mat.setColor("Specular", ColorRGBA.Brown);*/
                    terrain.setMaterial(mat);
                    terrain.setModelBound(new BoundingBox());
                    terrain.updateModelBound();
                    terrain.setLocalTranslation(0, 0, 0);
                    terrain.setLocalScale(1f, 1f, 1f);

                    //JmeTerrain terrainNode = new JmeTerrain(terrain, null);
                    ((Node) node).attachChild(terrain);

                    editorController.setNeedsSave(true);
                    
                    refreshSelected();
                    return null;
                }
            }).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void refreshSelected() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (selectedSpat != null) {
                    selectedSpat.refresh(false);
                }
            }
        });

    }


    /**
     * listener for node selection changes
     */
    public void resultChanged(LookupEvent ev) {
        if (currentRequest == null || !currentRequest.isDisplayed()) {
            return;
        }
        Collection<JmeSpatial> items = (Collection<JmeSpatial>) result.allInstances();
        for (JmeSpatial spatial : items) {
            selectSpatial(spatial);
            return;
        }
    }

    private void selectSpatial(JmeSpatial spatial) {
        if (spatial == null) {
            //setSelectedObjectText(null); TODO?
            //setSelectionData(null);  TODO?

                selectedSpat = spatial;
            
            setActivatedNodes(new org.openide.nodes.Node[]{});
            return;
        } else {
            
        }
        System.out.println("set selected spatial: "+spatial.getName());
        selectedSpat = spatial;
        
        //SceneApplication.getApplication().setSelectedNode(spatial);  TODO?
        //setActivatedNodes(new org.openide.nodes.Node[]{spatial});  TODO?
    }



    /*
     *
     *******************************************************************/

    /**
     * Obtain the TerrainEditorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TerrainEditorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(TerrainEditorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TerrainEditorTopComponent) {
            return (TerrainEditorTopComponent) win;
        }
        Logger.getLogger(TerrainEditorTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        if (currentRequest != null) {
            SceneApplication.getApplication().closeScene(currentRequest);
        }

    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
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
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void openScene(Spatial spat, AssetDataObject file, ProjectAssetManager manager) {
        cleanupControllers();
        SceneApplication.getApplication().addSceneListener(this);
        result.addLookupListener(this);
        //TODO: handle request change
        Node node;
        if (spat instanceof Node) {
            node = (Node) spat;
        } else {
            node = new Node();
            node.attachChild(spat);
        }
        JmeNode jmeNode = NodeUtility.createNode(node, file, false);
        SceneRequest request = new SceneRequest(this, jmeNode, manager);
        request.setDataObject(file);
        request.setHelpCtx(ctx);
        //file.setSaveCookie(saveCookie);
        if (editorController != null) {
            editorController.cleanup();
        }
        editorController = new TerrainEditorController(jmeNode, file);
        this.currentRequest = request;
        request.setWindowTitle("SceneComposer - " + manager.getRelativeAssetPath(file.getPrimaryFile().getPath()));
        request.setToolNode(new Node("SceneComposerToolNode"));
        SceneApplication.getApplication().requestScene(request);
    }

    public void sceneRequested(SceneRequest request) {
        if (request.equals(currentRequest)) {

            setLoadedScene(currentRequest.getJmeNode(), true);

            if (camController != null) {
                camController.disable();
            }
            if (toolController != null) {
                toolController.cleanup();
            }
            toolController = new TerrainToolController(currentRequest.getToolNode(), currentRequest.getManager().getManager(), request.getJmeNode());
            camController = new TerrainCameraController(SceneApplication.getApplication().getCamera());
            camController.setMaster(this);
            camController.enable();

            camController.setToolController(toolController);
            camController.setEditorController(editorController);
            toolController.setEditorController(editorController);
            editorController.setToolController(toolController);

            toolController.setHeightToolRadius(radiusSlider.getValue());
            toolController.setHeightToolHeight(heightSlider.getValue()); // should always be values upto and over 100, because it will be divided by 100
        }
    }

    private void setLoadedScene(final JmeNode jmeNode, final boolean active) {
        final TerrainEditorTopComponent inst = this;
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (jmeNode != null) {

                } else {

                }

                if (!active) {
                    result.removeLookupListener(inst);
                    close();
                } else {
                    open();
                    requestActive();
                }
            }
        });
    }

    private boolean checkSaved() {
        if (editorController != null && editorController.isNeedSave()) {
                Confirmation msg = new NotifyDescriptor.Confirmation("Your Scene is not saved, do you want to save?", 
                        NotifyDescriptor.YES_NO_CANCEL_OPTION, 
                        NotifyDescriptor.WARNING_MESSAGE);
                Object res = DialogDisplayer.getDefault().notify(msg);
                if (NotifyDescriptor.CANCEL_OPTION.equals(res)) {
                    return false;
                } else if (NotifyDescriptor.YES_OPTION.equals(res)) {
                    editorController.saveScene();
                    return true;
                } else if (NotifyDescriptor.NO_OPTION.equals(res)) {
                    return true;
                }
                return true;
        }
        return true;
    }

    public boolean sceneClose(SceneRequest request) {
        if (request.equals(currentRequest)) {
            if (checkSaved()) {
                SceneApplication.getApplication().removeSceneListener(this);
                setLoadedScene(null, false);
                currentRequest = null;
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        cleanupControllers();
                    }
                });
            }
        }
        return true;
    }

    public void previewRequested(PreviewRequest request) {
    }

    

    private void cleanupControllers() {
        if (camController != null) {
            camController.disable();
            camController = null;
        }
        if (toolController != null) {
            toolController.cleanup();
            toolController = null;
        }
        if (editorController != null) {
            editorController.cleanup();
            editorController = null;
        }
    }

    
}