/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.animation;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;

/**
 *
 * @author Nehon
 */
public class SoundTrack extends AbstractCinematicEvent {

    protected AudioNode audioNode;
    protected AudioRenderer audioRenderer;

    public SoundTrack(AudioNode node,AudioRenderer renderer) {
        audioNode=node;
        audioRenderer=renderer;
    }

    public SoundTrack() {
    }

    @Override
    public void playEvent() {
        audioRenderer.playSource(audioNode);
    }

    @Override
    public void stopEvent() {
        audioRenderer.stopSource(audioNode);
    }

    @Override
    public void pauseEvent() {
        audioRenderer.pauseSource(audioNode);
    }

    public void setAudioNode(AudioNode audioNode) {
        this.audioNode = audioNode;
    }

    public void setAudioRenderer(AudioRenderer audioRenderer) {
        this.audioRenderer = audioRenderer;
    }



}
