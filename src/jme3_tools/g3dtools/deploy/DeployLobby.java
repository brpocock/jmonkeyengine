/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DeployLobby.java
 *
 * Created on Jan 8, 2010, 11:06:59 PM
 */

package g3dtools.deploy;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class DeployLobby extends javax.swing.JFrame {

    private TreeCellRenderer treeRender;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private DefaultTreeModel treeModel = new DefaultTreeModel(root);

    /** Creates new form DeployLobby */
    public DeployLobby() {
        initComponents();
        updateAppList();
        setLocationRelativeTo(null);
    }

    private void addAppConfig(File cfgFile){
        AppConfig cfg = new AppConfig();
        FileInputStream stream = null;
        try{
            stream = new FileInputStream(cfgFile);
            cfg.load(stream);
            treeModel.insertNodeInto(new DefaultMutableTreeNode(cfg), root, 0);
            treeModel.reload();
        }catch (IOException ex){
            ex.printStackTrace();
        } finally {
            try{
                if (stream != null)
                    stream.close();
            }catch (IOException ex){
            }
        }
    }

    private void addListing(File dir){
        for (File f : dir.listFiles()){
            if (f.getName().toLowerCase().endsWith(".appconfig")){
                addAppConfig(f);
            }
        }
    }

    public void updateAppList(){
        root.removeAllChildren();

        File userDir = new File(System.getProperty("user.dir"));
        File appsDir = new File(userDir, "apps/");
        if (appsDir.exists() && appsDir.isDirectory()){
            addListing(appsDir);
        }else{
            appsDir.mkdir();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnNewApp = new javax.swing.JButton();
        sclApps = new javax.swing.JScrollPane();
        treApps = new javax.swing.JTree();
        btnEditApp = new javax.swing.JButton();
        btnDeploy = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jME Deploy Utility");

        btnNewApp.setText("New App");
        btnNewApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewAppActionPerformed(evt);
            }
        });

        treApps.setModel(treeModel);
        treApps.setExpandsSelectedPaths(false);
        treApps.setLargeModel(true);
        treApps.setRootVisible(false);
        sclApps.setViewportView(treApps);

        btnEditApp.setText("Edit App");
        btnEditApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditAppActionPerformed(evt);
            }
        });

        btnDeploy.setText("Deploy App Now");
        btnDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeployActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, sclApps, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(btnDeploy)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 189, Short.MAX_VALUE)
                        .add(btnEditApp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnNewApp)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(sclApps, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(btnNewApp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(btnEditApp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(btnDeploy, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enableContainer(Container container, boolean enable){
        container.setEnabled(enable);
        for (int i = 0; i < container.getComponentCount(); i++){
            Component c = container.getComponent(i);
            if (c instanceof Container){
                enableContainer( (Container) c, enable);
            }else{
                c.setEnabled(enable);
            }
        }
    }

    private void btnNewAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewAppActionPerformed
        AppConfig cfg = new AppConfig();
        DeployAppInfo infoDialog = new DeployAppInfo(this, cfg);
        infoDialog.setVisible(true);
    }//GEN-LAST:event_btnNewAppActionPerformed

    private void btnEditAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditAppActionPerformed
        TreePath path = treApps.getSelectionPath();
        if (path == null)
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        AppConfig cfg = (AppConfig) node.getUserObject();
        if (cfg != null){
            DeployAppInfo infoDialog = new DeployAppInfo(this, cfg);
            infoDialog.setVisible(true);
        }
    }//GEN-LAST:event_btnEditAppActionPerformed

    private void btnDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeployActionPerformed
        TreePath path = treApps.getSelectionPath();
        if (path == null)
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        AppConfig cfg = (AppConfig) node.getUserObject();
        if (cfg != null){
            FileListProcessor.process(cfg);
        }
    }//GEN-LAST:event_btnDeployActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Throwable ex){
                    ex.printStackTrace();
                }

                new DeployLobby().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDeploy;
    private javax.swing.JButton btnEditApp;
    private javax.swing.JButton btnNewApp;
    private javax.swing.JScrollPane sclApps;
    private javax.swing.JTree treApps;
    // End of variables declaration//GEN-END:variables

}
