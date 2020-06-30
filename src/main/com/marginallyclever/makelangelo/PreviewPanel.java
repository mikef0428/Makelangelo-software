package com.marginallyclever.makelangelo;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLPipelineFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.marginallyclever.makelangelo.log.Log;
import com.marginallyclever.makelangelo.preferences.GFXPreferences;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

// Custom drawing panel written as an inner class to access the instance variables.
public class PreviewPanel extends JPanel implements GLEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final float CAMERA_ZFAR = 1000.0f;
	private static final float CAMERA_ZNEAR = 10.0f;

	// Use debug pipeline?
	private static final boolean DEBUG_GL_ON = false;
	private static final boolean TRACE_GL_ON = false;

	// motion control
	// private boolean mouseIn=false;
	private int buttonPressed = MouseEvent.NOBUTTON;
	private int mouseOldX, mouseOldY;

	// scale + position
	private double cameraOffsetX = 0.0d;
	private double cameraOffsetY = 0.0d;
	private double cameraZoom = 1.0d;
	private int windowWidth = 0;
	private int windowHeight = 0;

	private GLJPanel glPanel;
	private GLU glu;

	private JPanel optionsPanel;
	private JCheckBox showPenUp;
	private JSlider showProgress;
	
	protected MakelangeloRobot robot;
	

	public PreviewPanel() {
		super();

		try { 
			glPanel = new GLJPanel();
		} catch(GLException e) {
			Log.error("I failed the very first call to OpenGL.  Are your native libraries missing?");
			System.exit(1);
		}
		
		showPenUp = new JCheckBox("Show pen up moves");
		showProgress = new JSlider(0,10000,10000);
		optionsPanel = new JPanel(new BorderLayout());
		optionsPanel.add(showPenUp,BorderLayout.LINE_START);
		optionsPanel.add(showProgress,BorderLayout.CENTER);
		
		this.setLayout(new BorderLayout());
		this.add(glPanel,BorderLayout.CENTER);
		this.add(optionsPanel,BorderLayout.PAGE_END);
		
		showPenUp.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				GFXPreferences.setShowPenUp(showPenUp.isSelected());
				glPanel.repaint();
			}
		});
		showProgress.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				glPanel.repaint();
			}
		});

		glPanel.addGLEventListener(this);
		glPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int dx = x - mouseOldX;
				int dy = y - mouseOldY;
				if (buttonPressed == MouseEvent.BUTTON1) moveCamera(-dx, -dy);
				//if (buttonPressed == MouseEvent.BUTTON3) zoomCamera(dy);
				mouseOldX = x;
				mouseOldY = y;
			}
		});
		glPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				buttonPressed = e.getButton();
				mouseOldX = e.getX();
				mouseOldY = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				buttonPressed = MouseEvent.NOBUTTON;
			}
		});
		glPanel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches < 0) {
					zoomIn();
				} else {
					zoomOut();
				}
			}
		});
	}

	void setRobot(MakelangeloRobot r) {
		robot = r;
	}

	/**
	 * set up the correct projection so the image appears in the right location
	 * and aspect ratio.
	 */
	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		// gl2.setSwapInterval(1);

		windowWidth = width;
		windowHeight = height;
		// window_aspect_ratio = window_width / window_height;

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		// gl2.glOrtho(-windowWidth / 2.0d, windowWidth / 2.0d, -windowHeight /
		// 2.0d, windowHeight / 2.0d, 0.01d, 100.0d);

		glu.gluPerspective(
				90,
				(float) windowWidth / (float) windowHeight,
				CAMERA_ZNEAR,
				CAMERA_ZFAR);
	}

	/**
	 * turn on debug pipeline(s) if needed.
	 */
	@Override
	public void init(GLAutoDrawable glautodrawable) {
		GL gl = glautodrawable.getGL();

		if (DEBUG_GL_ON) {
			try {
				// Debug ..
				gl = gl.getContext().setGL(GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (TRACE_GL_ON) {
			try {
				// Trace ..
				gl = gl.getContext().setGL(
						GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err }));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		glu = GLU.createGLU(gl);
	}

	@Override
	public void dispose(GLAutoDrawable glautodrawable) {}

	// refresh the image in the view
	@Override
	public void display(GLAutoDrawable glautodrawable) {
		// long now_time = System.currentTimeMillis();
		// float dt = (now_time - last_time)*0.001f;
		// last_time = now_time;
		// Log.message(dt);

		// draw the world
		GL2 gl2 = glautodrawable.getGL().getGL2();
		render(gl2);
	}

	// scale the picture of the robot to fake a zoom.
	public void zoomToFit() {
		if (robot != null) {
			double widthOfPaper = robot.getSettings().getPaperWidth();
			double heightOfPaper = robot.getSettings().getPaperHeight();
			double drawPanelWidthZoom = widthOfPaper;
			double drawPanelHeightZoom = heightOfPaper;

			if (windowWidth < windowHeight) {
				cameraZoom = (drawPanelWidthZoom > drawPanelHeightZoom ? drawPanelWidthZoom : drawPanelHeightZoom);
			} else {
				cameraZoom = (drawPanelWidthZoom < drawPanelHeightZoom ? drawPanelWidthZoom : drawPanelHeightZoom);
			}
		}
		cameraOffsetX = 0;
		cameraOffsetY = 0;
		
		repaint();
	}

	/**
	 * Reposition the camera
	 * @param dx change horizontally
	 * @param dy change vertically
	 */
	private void moveCamera(int dx, int dy) {
		cameraOffsetX += (float) dx * cameraZoom / windowWidth;
		cameraOffsetY += (float) dy * cameraZoom / windowHeight;
		repaint();
	}

	public void repaint() {
		super.repaint();
		if(glPanel!=null) glPanel.repaint();
	}
	
	// scale the picture of the robot to fake a zoom.
	public void zoomIn() {
		cameraZoom *= 3.5d / 4.0d;
		if(cameraZoom<CAMERA_ZNEAR) cameraZoom=CAMERA_ZNEAR;
		
		repaint();
	}

	// scale the picture of the robot to fake a zoom.
	public void zoomOut() {
		cameraZoom *= 4.0d / 3.5d;
		if(cameraZoom>CAMERA_ZFAR) cameraZoom=CAMERA_ZFAR;
		
		repaint();
	}

	public double getZoom() {
		return cameraZoom;
	}
	
	/**
	 * set up the correct modelview so the robot appears where it should.
	 * @param gl2
	 */
	private void paintCamera(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glTranslated(-cameraOffsetX, cameraOffsetY,-cameraZoom);
	}

	/**
	 * Clear the panel
	 * @param gl2
	 */
	private void paintBackground(GL2 gl2) {
		// Clear The Screen And The Depth Buffer
		gl2.glClearColor(212.0f / 255.0f, 233.0f / 255.0f, 255.0f / 255.0f, 0.0f);

		// Special handling for the case where the GLJPanel is translucent
		// and wants to be composited with other Java 2D content
		if (GLProfile.isAWTAvailable()
				&& !glPanel.isOpaque()
				&& glPanel.shouldPreserveColorBufferIfTranslucent()) {
			gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		} else {
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		}
	}

	public void render(GL2 gl2) {
		if(GFXPreferences.getAntialias()) {
			gl2.glEnable(GL2.GL_LINE_SMOOTH);
			gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
		} else {
			gl2.glDisable(GL2.GL_LINE_SMOOTH);
			gl2.glDisable(GL2.GL_POLYGON_SMOOTH);
			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_FASTEST);
		}
		gl2.glEnable(GL2.GL_BLEND);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);  
		
		paintBackground(gl2);
		paintCamera(gl2);

		gl2.glPushMatrix();

		if (robot != null) {
			gl2.glLineWidth((float)cameraZoom);
			double max = showProgress.getMaximum();
			double min = showProgress.getMinimum();
			double end = (showProgress.getValue()-min)/(max-min);
			robot.render(gl2,0,end);
		}

		gl2.glPopMatrix();
	}

}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
