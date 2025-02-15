package com.marginallyclever.makelangelo.turtle;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.log.Log;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Dan Royer
 *
 */
public class Turtle implements Cloneable {
	public ArrayList<TurtleMove> history;

	private ReentrantLock lock = new ReentrantLock();

	// current state
	private double px, py;
	private double nx, ny;  // normal of angle. aka sin() and cos() of angle.
	private double angle;
	private boolean isUp;
	private ColorRGB color;
	private double diameter=1;

	public Turtle() {
		super();
		reset(new ColorRGB(0,0,0));
	}
	
	public Turtle(Turtle t) {
		this();
		this.px = t.px;
		this.py = t.py;
		this.nx = t.nx;
		this.ny = t.ny;
		this.angle = t.angle;
		this.isUp = t.isUp;
		this.color.set(t.color);
		this.diameter = t.diameter; 

		for( TurtleMove m : t.history ) {
			this.history.add(new TurtleMove(m));
		}
	}
	
	public Turtle(ColorRGB firstColor) {
		super();
		reset(firstColor);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Turtle t = (Turtle)super.clone();
		for( TurtleMove m : history ) {
			t.history.add(new TurtleMove(m));
		}
		return t;
	}
	
	/**
	 * Return this Turtle to mint condition.  Erases history and resets all parameters.  Called by constructor.
	 */
	private void reset(ColorRGB c) {
		px = 0;
		py = 0;
		setAngle(0);
		penUp();
		history = new ArrayList<TurtleMove>();
		// default turtle color is black.
		setColor(c);
	}
	
	// multithreading lock safety
	public boolean isLocked() {
		return lock.isLocked();
	}
	
	// multithreading lock safety
	public void lock() {
		lock.lock();
	}
	
	// multithreading lock safety
	public void unlock() {
		if(lock.isLocked()) {  // prevents "illegal state exception - not locked"
			lock.unlock();
		}
	}

	public void setColor(ColorRGB c) {
		if(color!=null) {
			if(color.red==c.red && color.green==c.green && color.blue==c.blue) return;
		}
		color = new ColorRGB(c);
		history.add( new TurtleMove(color.toInt(),diameter,TurtleMove.TOOL_CHANGE) );
	}
	
	public ColorRGB getColor() {
		return color;
	}
	
	public void setDiameter(double d) {
		if(diameter==d) return;
		diameter=d;
		history.add( new TurtleMove(color.toInt(),diameter,TurtleMove.TOOL_CHANGE) );
	}
	
	public double getDiameter() {
		return diameter;
	}
	
	/**
	 * Absolute position change, make sure pen is up before move and put pen down after move.
	 * @param x  
	 * @param y 
	 */
	public void jumpTo(double x,double y) {
		penUp();
		moveTo(x,y);
		penDown();
	}
	
	/**
	 * Absolute position change, do not adjust pen status
	 * @param x  
	 * @param y 
	 */
	public void moveTo(double x,double y) {
		px=x;
		py=y;
		history.add( new TurtleMove(x, y, isUp ? TurtleMove.TRAVEL : TurtleMove.DRAW) );
	}
		
	/**
	 * Absolute position
	 * @param arg0 x axis
	 */
	public void setX(double arg0) {
		moveTo(arg0,py);
	}
	
	/**
	 * Absolute position
	 * @param arg0 y axis
	 */
	public void setY(double arg0) {
		moveTo(px,arg0);
	}
	
	public double getX() {
		return px;
	}
	
	public double getY() {
		return py;
	}
	
	public void penUp() {
		isUp=true;
	}
	
	public void penDown() {
		isUp=false;
	}
	
	/**
	 * @return true if pen is up
	 */
	public boolean isUp() {
		return isUp;
	}

	/**
	 * Relative turn
	 * @param degrees
	 */
	public void turn(double degrees) {
		setAngle(angle+degrees);
	}

	// Get absolute angle degrees
	public double getAngle() {
		return angle;
	}
	
	/**
	 * Set absolute angle
	 * @param degrees degrees
	 */
	public void setAngle(double degrees) {
		angle=degrees;
		double radians=Math.toRadians(angle);
		nx = Math.cos(radians);
		ny = Math.sin(radians);
	}

	/**
	 * Relative move forward/back
	 * @param stepSize
	 */
	public void forward(double stepSize) {
		moveTo(
			px + nx * stepSize,
			py + ny * stepSize
		);
	}

	/**
	 * Calculate the limits of drawing lines in this turtle history
	 * @param top maximum limits
	 * @param bottom minimum limits
	 */
	public Rectangle2D.Double getBounds() {
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		getBounds(top,bottom);
		
		Rectangle2D.Double r = new Rectangle.Double();
		r.x=bottom.x;
		r.y=bottom.y;
		r.width=top.x-bottom.x;
		r.height=top.y-bottom.y;
		
		return r;
	}
	
	private void getBounds(Point2D top,Point2D bottom) {
		bottom.x=Float.MAX_VALUE;
		bottom.y=Float.MAX_VALUE;
		top.x=-Float.MAX_VALUE;
		top.y=-Float.MAX_VALUE;
		TurtleMove old=null;
		
		int hits=0;
		
		for( TurtleMove m : history ) {
			if(m.type == TurtleMove.DRAW) {
				hits++;
				if(top.x<m.x) top.x=m.x;
				if(top.y<m.y) top.y=m.y;
				if(bottom.x>m.x) bottom.x=m.x;
				if(bottom.y>m.y) bottom.y=m.y;
				if(old != null) {
					if(top.x<old.x) top.x=old.x;
					if(top.y<old.y) top.y=old.y;
					if(bottom.x>old.x) bottom.x=old.x;
					if(bottom.y>old.y) bottom.y=old.y;
				}
			}
			old=m;
		}
		
		if(hits==0) {
			bottom.set(0,0);
			top.set(0,0);
		}
	}

	/**
	 * Scale all draw and move segments by parameters
	 * @param sx
	 * @param sy
	 */
	public void scale(double sx, double sy) {
		for( TurtleMove m : history ) {
			switch(m.type) {
			case TurtleMove.DRAW:
			case TurtleMove.TRAVEL:
				m.x*=sx;
				m.y*=sy;
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Translate all draw and move segments by parameters
	 * @param dx relative move x
	 * @param dy relative move y
	 */
	public void translate(double dx, double dy) {
		for( TurtleMove m : history ) {
			switch(m.type) {
			case TurtleMove.DRAW:
			case TurtleMove.TRAVEL:
				m.x+=dx;
				m.y+=dy;
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Log smallest bounding rectangle for Turtle path.
	 */
	public void showExtent() {
		int i;
		double xmin=0,xmax=0,ymin=0,ymax=0;
		int first=1;
		for(i=0;i<history.size();i++) {
			TurtleMove mov=history.get(i);
			if (mov.type == TurtleMove.DRAW) {
				if(first == 1 || mov.x < xmin) xmin=mov.x;
				if(first == 1 || mov.y < ymin) ymin=mov.y;
				if(first == 1 || mov.x > xmax) xmax=mov.x;
				if(first == 1 || mov.y > ymax) ymax=mov.y;
				first=0;
			}
		}
		Log.message("extent is ("+xmin+"/"+ymin+" "+xmax+"/"+ymax+" ");
	}

	// return a list of all the pen-down lines while remembering their color.
	public ArrayList<LineSegment2D> getAsLineSegments() {
		ArrayList<LineSegment2D> lines = new ArrayList<LineSegment2D>();
		TurtleMove previousMovement=null;
		ColorRGB color = new ColorRGB(0,0,0);

		Log.message("  Found "+history.size()+" instructions.");
		
		for( TurtleMove m : history ) {
			switch(m.type) {
			case TurtleMove.DRAW:
				if(previousMovement!=null) {
					LineSegment2D line = new LineSegment2D(
							new Point2D(previousMovement.x,previousMovement.y),
							new Point2D(m.x,m.y),
							color);
					if(line.lengthSquared()>0) {
						lines.add(line);
					}
				}
				previousMovement = m;
				break;
			case TurtleMove.TRAVEL:
				previousMovement = m;
				break;
			case TurtleMove.TOOL_CHANGE:
				color = m.getColor();
				break;
			}
		}

		return lines;
	}

	public void addLineSegments(ArrayList<LineSegment2D> orderedLines, double minimumJumpSize) {
		if(orderedLines.isEmpty()) return;
		
		LineSegment2D first = orderedLines.get(0); 
		jumpTo(first.a.x,first.a.y);
		moveTo(first.b.x,first.b.y);
		
		double minJumpSquared = minimumJumpSize*minimumJumpSize;
		
		for( LineSegment2D line : orderedLines ) {
			// change color if needed
			if(line.c!=getColor()) {
				setColor(line.c);
			}
			
			if(distanceSquared(line.a) > minJumpSquared) {
				// The previous line ends too far from the start point of this line,
				// need to make a travel with the pen up to the start point of this line.
				jumpTo(line.a.x,line.a.y);
			}
			// Make a pen down move to the end of this line
			moveTo(line.b.x,line.b.y);
		}
	}

	private double distanceSquared(Point2D b) {
		double dx = px-b.x;
		double dy = py-b.y;
		return dx*dx + dy*dy; 
	}
	
	public ArrayList<Turtle> splitByToolChange() {
		ArrayList<Turtle> list = new ArrayList<Turtle>();
		Turtle t = new Turtle();
		list.add(t);
		
		for( TurtleMove m : history) {
			if(m.type==TurtleMove.TOOL_CHANGE) {
				t = new Turtle();
				t.history.clear();
				list.add(t);
			}
			t.history.add(m);
		}
		Log.message("Turtle.splitByToolChange() into "+list.size()+" sections.");

		ArrayList<Turtle> notEmptyList = new ArrayList<Turtle>();
		for( Turtle t2 : list ) {
			if(t2.getHasAnyDrawingMoves()) {
				notEmptyList.add(t2);
			}
		}
		Log.message("Turtle.splitByToolChange() "+notEmptyList.size()+" not-empty sections.");
		
		return notEmptyList;
	}
	
	public boolean getHasAnyDrawingMoves() {
		for( TurtleMove m : history) {
			if(m.type==TurtleMove.DRAW) return true;
		}
		return false;
	}

	public void add(Turtle t) {
		this.history.addAll(t.history);
	}

	public ColorRGB getFirstColor() {
		for( TurtleMove m : history) {
			if(m.type==TurtleMove.TOOL_CHANGE)
				return m.getColor();
		}
		
		return new ColorRGB(0,0,0);
	}
}
