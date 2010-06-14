/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.ogretools.convert;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author normenhansen
 */
public class OgreXMLConvertOptions {

    private String sourceFile = "";
    private String destFile = "";
    private int lodLevels = 0;
    private int lodValue = 250000;
    private int lodPercent = 20;
    private String lodStrategy = "Distance";
    private boolean generateTangents = true;
    private boolean generateEdgeLists = true;

    private boolean generate=false;

    public OgreXMLConvertOptions() {
    }

    public OgreXMLConvertOptions(String sourceFile) {
        this.sourceFile = sourceFile;
        this.destFile = sourceFile;
    }

    public OgreXMLConvertOptions(String sourceFile, String destFile) {
        this.sourceFile = sourceFile;
        this.destFile = destFile;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getBinaryFileName(){
        FileObject fobj=FileUtil.toFileObject(new File(sourceFile));
        return fobj.getParent().getPath()+File.separator+fobj.getName();
    }

    public String getDestFile() {
        return destFile;
    }

    public void setDestFile(String destFile) {
        this.destFile = destFile;
    }

    public int getLodLevels() {
        return lodLevels;
    }

    public void setLodLevels(int lodLevels) {
        this.lodLevels = lodLevels;
    }

    public int getLodValue() {
        return lodValue;
    }

    public void setLodValue(int lodValue) {
        this.lodValue = lodValue;
    }

    public int getLodPercent() {
        return lodPercent;
    }

    public void setLodPercent(int lodPercent) {
        this.lodPercent = lodPercent;
    }

    public String getLodStrategy() {
        return lodStrategy;
    }

    public void setLodStrategy(String lodStrategy) {
        this.lodStrategy = lodStrategy;
    }

    public boolean isGenerateTangents() {
        return generateTangents;
    }

    public void setGenerateTangents(boolean generateTangents) {
        this.generateTangents = generateTangents;
    }

    public boolean isGenerateEdgeLists() {
        return generateEdgeLists;
    }

    public void setGenerateEdgeLists(boolean generateEdgeLists) {
        this.generateEdgeLists = generateEdgeLists;
    }

    /**
     * @return the generate
     */
    public boolean isGenerate() {
        return generate;
    }

    /**
     * @param generate the generate to set
     */
    public void setGenerate(boolean generate) {
        this.generate = generate;
    }
}
