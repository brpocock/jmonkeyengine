package com.jme3.scene.plugins.blender.textures;

import java.nio.ByteBuffer;

import com.jme3.scene.plugins.blender.DataRepository;
import com.jme3.scene.plugins.blender.file.Structure;
import com.jme3.scene.plugins.blender.textures.TextureHelper.ColorBand;
import com.jme3.scene.plugins.blender.textures.TextureHelper.TexResult;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;

/**
 * This class generates the 'distorted noise' texture.
 * @author Marcin Roguski (Kaelthas)
 */
public class TextureGeneratorDistnoise extends TextureGenerator {

	/**
	 * Constructor stores the given noise generator.
	 * @param noiseGenerator the noise generator
	 */
	public TextureGeneratorDistnoise(NoiseGenerator noiseGenerator) {
		super(noiseGenerator);
	}

	@Override
	protected Texture generate(Structure tex, int width, int height, DataRepository dataRepository) {
		float noisesize = ((Number) tex.getFieldValue("noisesize")).floatValue();
		float nabla = ((Number) tex.getFieldValue("nabla")).floatValue();
		float distAmount = ((Number) tex.getFieldValue("dist_amount")).floatValue();
		int noisebasis = ((Number) tex.getFieldValue("noisebasis")).intValue();
		int noisebasis2 = ((Number) tex.getFieldValue("noisebasis2")).intValue();
		float contrast = ((Number) tex.getFieldValue("contrast")).floatValue();
		float brightness = ((Number) tex.getFieldValue("bright")).floatValue();

		TexResult texres = new TexResult();
		float[] texvec = new float[] { 0, 0, 0 };
		float wDelta = 1.0f / width, hDelta = 1.0f / height;
		int halfW = width, halfH = height;
		width <<= 1;
		height <<= 1;
		ColorBand colorBand = this.readColorband(tex, dataRepository);
		Format format = colorBand != null ? Format.RGB8 : Format.Luminance8;
		int bytesPerPixel = colorBand != null ? 3 : 1;

		ByteBuffer data = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
		for (int i = -halfW; i < halfW; ++i) {
			texvec[0] = wDelta * i / noisesize;
			for (int j = -halfH; j < halfH; ++j) {
				texvec[1] = hDelta * j / noisesize;

				texres.tin = noiseGenerator.mgVLNoise(texvec[0], texvec[1], texvec[2], distAmount, noisebasis, noisebasis2);
				if (colorBand != null) {
					noiseGenerator.doColorband(colorBand, texres, dataRepository);
					if (texres.nor != null) {
						float offs = nabla / noisesize; // also scaling of texvec
						/* calculate bumpnormal */
						texres.nor[0] = noiseGenerator.mgVLNoise(texvec[0] + offs, texvec[1], texvec[2], distAmount, noisebasis, noisebasis2);
						texres.nor[1] = noiseGenerator.mgVLNoise(texvec[0], texvec[1] + offs, texvec[2], distAmount, noisebasis, noisebasis2);
						texres.nor[2] = noiseGenerator.mgVLNoise(texvec[0], texvec[1], texvec[2] + offs, distAmount, noisebasis, noisebasis2);
						noiseGenerator.texNormalDerivate(colorBand, texres, dataRepository);
					}

					noiseGenerator.brightnesAndContrastRGB(tex, texres);
					data.put((byte) (texres.tr * 255.0f));
					data.put((byte) (texres.tg * 255.0f));
					data.put((byte) (texres.tb * 255.0f));
				} else {
					noiseGenerator.brightnesAndContrast(texres, contrast, brightness);
					data.put((byte) (texres.tin * 255.0f));
				}
			}
		}
		return new Texture2D(new Image(format, width, height, data));
	}
}
