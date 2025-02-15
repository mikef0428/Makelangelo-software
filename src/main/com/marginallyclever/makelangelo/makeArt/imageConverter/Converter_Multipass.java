package com.marginallyclever.makelangelo.makeArt.imageConverter;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.turtle.Turtle;


/**
 * 
 * @author Dan Royer
 */
public class Converter_Multipass extends ImageConverter {
	static private double angle=0;
	static private int passes=4;
	
	@Override
	public String getName() {
		return Translator.get("ConverterMultipassName");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("angle")) setAngle((double)evt.getNewValue());
		if(evt.getPropertyName().equals("levels")) setPasses((int)evt.getNewValue());		
	}
	
	public double getAngle() {
		return angle;
	}
	
	public void setAngle(double value) {
		angle = value;
	}
	
	public int getPasses() {
		return passes;
	}
	
	public void setPasses(int value) {
		if(passes<1) passes=1;
		passes=value;
	}
	
	/**
	 * create parallel lines across the image.  Raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void finish() {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);
		
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));

		double stepSize = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / (double)(passes+1);

		// from top to bottom of the margin area...
		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop();
		double xLeft   = myPaper.getMarginLeft();
		double xRight  = myPaper.getMarginRight();
		double height = yTop - yBottom;
		double width = xRight - xLeft;
		double maxLen = Math.sqrt(width*width+height*height);
		double [] error0 = new double[(int)Math.ceil(maxLen)];
		double [] error1 = new double[(int)Math.ceil(maxLen)];

		turtle = new Turtle();
		
		boolean useError=false;
		
		int i=0;
		for(double a = -maxLen;a<maxLen;a+=stepSize) {
			double px = dx * a;
			double py = dy * a;
			double x0 = px - dy * maxLen;
			double y0 = py + dx * maxLen;
			double x1 = px + dy * maxLen;
			double y1 = py - dx * maxLen;
		
			double l2 = level * (1 + (i % passes));
			if ((i % 2) == 0) {
				if(!useError) convertAlongLine(x0, y0, x1, y1, stepSize, l2, img);
				else convertAlongLineErrorTerms(x0,y0,x1,y1,stepSize,l2,error0,error1,img);
			} else {
				if(!useError) convertAlongLine(x1, y1, x0, y0, stepSize, l2, img);
				else convertAlongLineErrorTerms(x1,y1,x0,y0,stepSize,l2,error0,error1,img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			++i;
		}
	}
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
