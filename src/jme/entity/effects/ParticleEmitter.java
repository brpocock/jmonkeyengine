/*
 * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this 
 * list of conditions and the following disclaimer. 
 * 
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution. 
 * 
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the 
 * names of its contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package jme.entity.effects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.vector.Vector3f;

import jme.texture.TextureManager;

/**
 * <code>ParticleEmitter</code> controls and maintains a number of 
 * <code>Particle</code>s. The emitter handles creation of a particle 
 * setting the particles attributes to those defined by the user
 * creating the emitter. An emitter updates the particle during
 * a call to <code>update</code> setting it's new position, size,
 * color and transparency.
 * 
 * Size and color have a start and end value, where these values
 * are interpolated through the life of the particle.
 * 
 * Another important note is that the gravity value is what handles
 * the updating of the velocity of individual particles. When a 
 * particle is first created, it's velocity is in a random direction.
 * It is not until the gravity takes hold that it will travel in
 * the desired direction. This adds realism to the particle emitter.
 * 
 * @author Mark Powell
 * @version 1
 */
public class ParticleEmitter {

	//handles the particle objects.
	private int numParticles;
	private Particle particles[];

	//what does a particle look like?
	private int texId;

	//attributes for positional updates.
	private Vector3f gravity;
	private Vector3f position;
	private float speed;
	private float friction;

	//attribute that denotes the life of the particle.
	private float fade;

	//attribute for the particles appearance.
	private Vector3f startSize;
	private Vector3f endSize;
	private Vector3f startColor;
	private Vector3f endColor;
	
	private boolean isLooping = false;
	private boolean isFirst = true;
	
	//data for billboarding, note that the vectors are
	//an instance variable simply because they are used
	//for each render, and it is cheaper to change the
	//objects attributes than create a new one.
	private float[] matrix;
	private FloatBuffer buf;
	private Vector3f right;
	private Vector3f up;
	private Vector3f billboard;

	/**
	 * Constructor instantiates a new <code>ParticleEmitter</code> 
	 * object, intializing the particle array for the number of
	 * particles this emitter is responsible for.
	 * @param numParticles the number of particles that will be
	 * 		on screen at any one time.
	 */
	public ParticleEmitter(int numParticles) {

		this.numParticles = numParticles;
		particles = new Particle[numParticles];

		startSize = new Vector3f();
		endSize = new Vector3f();
		startColor = new Vector3f();
		endColor = new Vector3f();
		gravity = new Vector3f();
		position = new Vector3f();
		speed = 1.0f;
		matrix = new float[16];
		buf =
			ByteBuffer
				.allocateDirect(16 * 4)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		right = new Vector3f();
		up = new Vector3f();
		billboard = new Vector3f();

		for (int i = 0; i < numParticles; i++) {
			particles[i] = new Particle();
			particles[i].life = -1.0f;
		}
	}

	/**
	 * <code>update</code> updates each particles position, velocity, life,
	 * color and size. If a particles life has reached zero, it is considered 
	 * dead and will be replaced by a new particle.
	 * 
	 * @param timeFraction the time factor between frames.
	 */
	public void update(float timeFraction) {
		float time = timeFraction * speed;

		for (int i = 0; i < numParticles; i++) {
			//the particle is dead, so create a new one.
			if (particles[i].life <= 0.0f && (isLooping || isFirst)) {
				createParticle(i);
				continue;
			}

			//update position by velocity
			particles[i].position.x += particles[i].velocity.x
				/ (friction * 1000)
				* time;
			particles[i].position.y += particles[i].velocity.y
				/ (friction * 1000)
				* time;
			particles[i].position.z += particles[i].velocity.z
				/ (friction * 1000)
				* time;
			//update velocity by gravity
			particles[i].velocity.x += gravity.x * time;
			particles[i].velocity.y += gravity.y * time;
			particles[i].velocity.z += gravity.z * time;

			//update life by fade.
			particles[i].life -= particles[i].fade * time;
			
			//interpolate color
			particles[i].color.x =
				(startColor.x * (particles[i].life))
					+ (endColor.x * (1 - particles[i].life));
			particles[i].color.y =
				(startColor.y * (particles[i].life))
					+ (endColor.y * (1 - particles[i].life));
			particles[i].color.z =
				(startColor.z * (particles[i].life))
					+ (endColor.z * (1 - particles[i].life));

			//interpolate size
			particles[i].size.x =
				(startSize.x * (particles[i].life))
					+ (endSize.x * (1 - particles[i].life));
			particles[i].size.y =
				(startSize.y * (particles[i].life))
					+ (endSize.y * (1 - particles[i].life));
			particles[i].size.z =
				(startSize.z * (particles[i].life))
					+ (endSize.z * (1 - particles[i].life));

		}
		isFirst = false;	
	}

	/**
	 * <code>render</code> renders a quad for each particle using
	 * a billboarding effect so that the particle is always facing
	 * the viewport. This gives us a very good three dimensional 
	 * effect using two dimensional objects. 
	 */
	public void render() {
		GL.glPushMatrix();
		//set gl state.
		GL.glEnable(GL.GL_TEXTURE_2D);
		GL.glEnable(GL.GL_BLEND);
		GL.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
		TextureManager.getTextureManager().bind(texId);

		//get the view matrix for billboarding.
        GL.glGetFloat(GL.GL_MODELVIEW_MATRIX, buf);
		buf.rewind();
		buf.get(matrix);

		//set up the right and up vectors
		right.x = matrix[0];
		right.y = matrix[4];
		right.z = matrix[8];

		up.x = matrix[1];
		up.y = matrix[5];
		up.z = matrix[9];

		//render each particle
		for (int i = 0; i < numParticles; i++) {
			GL.glColor4f(
				particles[i].color.x,
				particles[i].color.y,
				particles[i].color.z,
				particles[i].life);
			GL.glBegin(GL.GL_TRIANGLE_STRIP);
			GL.glTexCoord2f(1, 1);

			//top right of quad
			billboard.x =
				(right.x + up.x) * particles[i].size.x
					+ particles[i].position.x;
			billboard.y =
				(right.y + up.y) * particles[i].size.y
					+ particles[i].position.y;
			billboard.z =
				(right.z + up.z) * particles[i].size.z
					+ particles[i].position.z;
			GL.glVertex3f(billboard.x, billboard.y, billboard.z);
			GL.glTexCoord2f(0, 1);

			//top left of quad
			billboard.x =
				(up.x - right.x) * particles[i].size.x
					+ particles[i].position.x;
			billboard.y =
				(up.y - right.y) * particles[i].size.y
					+ particles[i].position.y;
			billboard.z =
				(up.z - right.z) * particles[i].size.z
					+ particles[i].position.z;
			
			GL.glVertex3f(billboard.x, billboard.y, billboard.z);
			GL.glTexCoord2f(1, 0);

			//bottom right of quad
			billboard.x =
				(right.x - up.x) * particles[i].size.x + particles[i].position.x;
			billboard.y =
				(right.y - up.y) * particles[i].size.y + particles[i].position.y;
			billboard.z =
				(right.z - up.z) * particles[i].size.z + particles[i].position.z;
			
			GL.glVertex3f(billboard.x, billboard.y, billboard.z);
			GL.glTexCoord2f(0, 0);

			//bottom left of quad
			billboard.x =
				(right.x + up.x) *
					- particles[i].size.x
					+ particles[i].position.x;
			billboard.y =
				(right.y + up.y) *
					- particles[i].size.y
					+ particles[i].position.y;
			billboard.z =
				(right.z + up.z) *
					- particles[i].size.z
					+ particles[i].position.z;
			
			GL.glVertex3f(billboard.x, billboard.y, billboard.z);
			GL.glEnd();

		}

		//revert open gl state
		GL.glDisable(GL.GL_TEXTURE_2D);
        GL.glDisable(GL.GL_BLEND);
		GL.glPopMatrix();
	}

	/**
	 * <code>setFriction</code> sets the value that the particles
	 * slow down during their travels. A typical value will be anywhere
	 * between 1 and 10. A negative value will cause the particle to 
	 * travel in the opposite direction.
	 * @param friction the new friction value.
	 */
	public void setFriction(float friction) {
		this.friction = friction;
	}

	/**
	 * <code>setEndColor</code> sets the final color of the particle 
	 * just before it dies. This color is interpolated with the
	 * start color.
	 * @param endColor the final color of the particle.
	 */
	public void setEndColor(Vector3f endColor) {
		this.endColor = endColor;
	}

	/**
	 * <code>setEndSize</code> sets the final size of the particle
	 * just before it dies. This size is interpolated with the
	 * start size.
	 * @param endSize the final size of the particle.
	 */
	public void setEndSize(Vector3f endSize) {
		this.endSize = endSize;
	}

	/**
	 * <code>setGravity</code> sets the value of the forces that will 
	 * be acting upon the particles. This value is what determines 
	 * the direction of the particles velocity.
	 * @param gravity the forces that will be acting on the particle.
	 */
	public void setGravity(Vector3f gravity) {
		this.gravity = gravity;
	}

	/**
	 * <code>setStartColor</code> sets the starting color of the
	 * particle. This color is interpolated with the final
	 * color.
	 * @param startColor the starting color of the particles.
	 */
	public void setStartColor(Vector3f startColor) {
		this.startColor = startColor;
	}

	/**
	 * <code>setStartSize</code> sets the starting size of the
	 * particle. This size is interpolated with the final size.
	 * @param startSize the starting size of the particles.
	 */
	public void setStartSize(Vector3f startSize) {
		this.startSize = startSize;
	}

	/**
	 * <code>setFade</code> sets the fade value of the particles. The 
	 * final fade value is determined by multiplying the fade value by
	 * two and then multiplying by a random number between 0 and 1. Therefore,
	 * the fade value determines the minimum and fade * 2 is the maximum. 
	 * @param fade the fade value of the particles.
	 */
	public void setFade(float fade) {
		this.fade = fade;
	}

	/**
	 * <code>setPosition</code> sets the position of the emitter. This
	 * will determine the initial position of each particle as they are
	 * generated.
	 * @param position
	 */
	public void setPosition(Vector3f position) {
		this.position = position;
	}

	/**
	 * <code>setSpeed</code> sets the speed at which the particle engine
	 * updates. By default the speed is 1.
	 * @param speed the speed of the updates.
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	/**
	 * <code>setTexture</code> assigns the texture to be 
	 * associated with the particles.
	 * @param filename the image file of the texture.
	 */
	public void setTexture(String filename) {
		texId = TextureManager.getTextureManager().loadTexture(
				filename,
				GL.GL_LINEAR_MIPMAP_LINEAR,
				GL.GL_LINEAR,
				true);
	}
	
	/**
	 * <code>loopAnimation</code> determines whether to continue animating
	 * the particles after the first batch. That is, a fire would continue
	 * to burn, but there would only be a single explosion.
	 * @param value true loop, false only create a single batch.
	 */
	public void loopAnimation(boolean value) {
		isLooping = value;
	}
	
	private void createParticle(int i) {
		particles[i].life = 1.0f;
		particles[i].fade = ((float) (fade * Math.random() + fade));
		particles[i].position.x = position.x;
		particles[i].position.y = position.y;
		particles[i].position.z = position.z;

		particles[i].velocity.x =
			(((float) (Math.random() * 32767) % 50) - 26.0f) * 10.0f;
		particles[i].velocity.y =
			(((float) (Math.random() * 32767) % 50) - 26.0f) * 10.0f;
		particles[i].velocity.z =
			(((float) (Math.random() * 32767) % 50) - 26.0f) * 10.0f;

		particles[i].color.x = startColor.x;
		particles[i].color.y = startColor.y;
		particles[i].color.z = startColor.z;

		particles[i].size.x = startSize.x;
		particles[i].size.y = startSize.y;
		particles[i].size.z = startSize.z;
	}

}
