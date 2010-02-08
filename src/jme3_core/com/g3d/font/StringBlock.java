package com.g3d.font;

import com.g3d.font.BitmapFont.Align;
import com.g3d.math.ColorRGBA;

/**
 * @author dhdd
 *
 *         Defines a String that is to be drawn in one block that can be constrained by a {@link Rectangle}. Also holds
 *         formatting information for the StringBlock
 */
public class StringBlock {

    private String text;
    private Rectangle textBox;
    private BitmapFont.Align alignment;
    private float size;
    private ColorRGBA color;
    private boolean kerning;

    /**
     *
     * @param text the text that the StringBlock will hold
     * @param textBox the rectangle that constrains the text
     * @param alignment the initial alignment of the text
     * @param size the size in pixels (vertical size of a single line)
     * @param color the initial color of the text
     * @param kerning
     */
    public StringBlock(String text, Rectangle textBox, BitmapFont.Align alignment, float size, ColorRGBA color,
            boolean kerning) {
        this.text = text;
        this.textBox = textBox;
        this.alignment = alignment;
        this.size = size;
        this.color = color;
        this.kerning = kerning;
    }

    public StringBlock(){
        this.text = "";
        this.textBox = null;
        this.alignment = Align.Left;
        this.size = 100;
        this.color = ColorRGBA.White;
        this.kerning = true;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Rectangle getTextBox() {
        return textBox;
    }

    public void setTextBox(Rectangle textBox) {
        this.textBox = textBox;
    }

    public BitmapFont.Align getAlignment() {
        return alignment;
    }

    public void setAlignment(BitmapFont.Align alignment) {
        this.alignment = alignment;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
    }

    public boolean isKerning() {
        return kerning;
    }

    public void setKerning(boolean kerning) {
        this.kerning = kerning;
    }
}