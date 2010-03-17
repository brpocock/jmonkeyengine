package com.jme3.system.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;
import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.android.AndroidInput;
import com.jme3.renderer.android.OGLESRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OGLESContext implements JmeContext, GLSurfaceView.Renderer {

    private static final Logger logger = Logger.getLogger(OGLESContext.class.getName());

    protected AtomicBoolean created = new AtomicBoolean(false);
    protected AppSettings settings = new AppSettings(true);
    protected OGLESRenderer renderer;
    protected Timer timer;
    protected SystemListener listener;

    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected boolean wasActive = false;
    protected int frameRate = 0;
    protected boolean autoFlush = true;

    protected AndroidInput view;

    public OGLESContext(){
    }

    public Type getType() {
        return Type.Display;
    }

    public GLSurfaceView createView(Activity activity){
        view = new AndroidInput(activity);
        //RGB565, Depth16
        view.setEGLConfigChooser(5, 6, 5, 0, 16, 0);
        view.setFocusableInTouchMode(true);
        view.setFocusable(true);
        view.getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
//        view.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
//                         | GLSurfaceView.DEBUG_LOG_GL_CALLS);
   		view.setRenderer(this);
        return view;

    }

    protected void applySettings(AppSettings setting){
    }

    protected void initInThread(GL10 gl){
        logger.info("Display created.");
        logger.fine("Running on thread: "+Thread.currentThread().getName());

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable thrown) {
                listener.handleError("Uncaught exception thrown in "+thread.toString(), thrown);
            }
        });

        created.set(true);

        timer = new AndroidTimer();
        renderer = new OGLESRenderer(gl);
        renderer.initialize();
        listener.initialize();
    }

    /**
     * De-initialize in the OpenGL thread.
     */
    protected void deinitInThread(){
        listener.destroy();
        renderer.cleanup();
        // do android specific cleaning here

        logger.info("Display destroyed.");
        created.set(false);
        renderer = null;
        timer = null;
    }

    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
    }

    public void setSystemListener(SystemListener listener){
        this.listener = listener;
    }

    public AppSettings getSettings() {
        return settings;
    }

    public com.jme3.renderer.Renderer getRenderer() {
        return renderer;
    }

    public MouseInput getMouseInput() {
        return view;
    }

    public KeyInput getKeyInput() {
        return view;
    }

    public JoyInput getJoyInput() {
        return null;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTitle(String title) {
    }

    public boolean isCreated(){
        return created.get();
    }

    public void setAutoFlushFrames(boolean enabled){
        this.autoFlush = enabled;
    }

    // renderer:initialize
    public void onSurfaceCreated(GL10 gl, EGLConfig cfg) {
        logger.info("Using Android");
        initInThread(gl);
    }

    // SystemListener:reshape
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        settings.setResolution(width, height);
        listener.reshape(width, height);
    }

    // SystemListener:update
    public void onDrawFrame(GL10 gl) {
        if (needClose.get()){
            deinitInThread(); // ???
            return;
        }

//        if (wasActive != Display.isActive()){
//            if (!wasActive){
//                listener.gainFocus();
//                wasActive = true;
//            }else{
//                listener.loseFocus();
//                wasActive = false;
//            }
//        }

		if (!created.get())
            throw new IllegalStateException();

        listener.update();

        // swap buffers
        
        if (frameRate > 0){
//            Display.sync(frameRate);
            // synchronzie to framerate
        }

        if (autoFlush)
            renderer.onFrame();
    }

    public void create() {
        if (created.get()){
            logger.warning("create() called when display is already created!");
            return;
        }
    }

    public void restart() {
    }

    public void destroy() {
        needClose.set(true);
    }

}
