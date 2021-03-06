package sc.plugin2016.gui.renderer.primitives;

import processing.core.PApplet;
import processing.core.PImage;
import sc.plugin2016.gui.renderer.FrameRenderer;

public class Background extends PrimitiveBase {
	
	FrameRenderer parent;
	PImage rawImage;
	PImage img;

	public Background(FrameRenderer parent) {
		super(parent);
		this.parent = parent;
		rawImage = parent.loadImage(GuiConstants.BACKGROUND_IMAGE);
	}
	
	public void draw(){
	  resize(parent.displayWidth, parent.displayHeight);
		parent.background(GuiConstants.colorBackGround);
		parent.image(img, 0, 0);
	}
	
	public synchronized void resize(int width, int height){
		try {
			img = (PImage) rawImage.clone();
		} catch (CloneNotSupportedException e) {
			img = new PImage();
		}
		img.resize(width, height);
	}

}
