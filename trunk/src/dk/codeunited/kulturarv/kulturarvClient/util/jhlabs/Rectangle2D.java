package dk.codeunited.kulturarv.kulturarvClient.util.jhlabs;

import java.io.Serializable;

/**
 * @author Java SDK
 */
public abstract class Rectangle2D {

	/**
	 * The bitmask that indicates that a point lies to the left of this
	 * <code>Rectangle2D</code>.
	 * 
	 * @since 1.2
	 */
	public static final int OUT_LEFT = 1;

	/**
	 * The bitmask that indicates that a point lies above this
	 * <code>Rectangle2D</code>.
	 * 
	 * @since 1.2
	 */
	public static final int OUT_TOP = 2;

	/**
	 * The bitmask that indicates that a point lies to the right of this
	 * <code>Rectangle2D</code>.
	 * 
	 * @since 1.2
	 */
	public static final int OUT_RIGHT = 4;

	/**
	 * The bitmask that indicates that a point lies below this
	 * <code>Rectangle2D</code>.
	 * 
	 * @since 1.2
	 */
	public static final int OUT_BOTTOM = 8;

	/**
	 * Returns the X coordinate of the upper-left corner of the framing
	 * rectangle in <code>double</code> precision.
	 * 
	 * @return the X coordinate of the upper-left corner of the framing
	 *         rectangle.
	 * @since 1.2
	 */
	public abstract double getX();

	/**
	 * Returns the Y coordinate of the upper-left corner of the framing
	 * rectangle in <code>double</code> precision.
	 * 
	 * @return the Y coordinate of the upper-left corner of the framing
	 *         rectangle.
	 * @since 1.2
	 */
	public abstract double getY();

	/**
	 * Returns the width of the framing rectangle in <code>double</code>
	 * precision.
	 * 
	 * @return the width of the framing rectangle.
	 * @since 1.2
	 */
	public abstract double getWidth();

	/**
	 * Returns the height of the framing rectangle in <code>double</code>
	 * precision.
	 * 
	 * @return the height of the framing rectangle.
	 * @since 1.2
	 */
	public abstract double getHeight();

	/**
	 * Returns the smallest X coordinate of the framing rectangle of the
	 * <code>Shape</code> in <code>double</code> precision.
	 * 
	 * @return the smallest X coordinate of the framing rectangle of the
	 *         <code>Shape</code>.
	 * @since 1.2
	 */
	public double getMinX() {
		return getX();
	}

	/**
	 * Returns the smallest Y coordinate of the framing rectangle of the
	 * <code>Shape</code> in <code>double</code> precision.
	 * 
	 * @return the smallest Y coordinate of the framing rectangle of the
	 *         <code>Shape</code>.
	 * @since 1.2
	 */
	public double getMinY() {
		return getY();
	}

	/**
	 * Returns the largest X coordinate of the framing rectangle of the
	 * <code>Shape</code> in <code>double</code> precision.
	 * 
	 * @return the largest X coordinate of the framing rectangle of the
	 *         <code>Shape</code>.
	 * @since 1.2
	 */
	public double getMaxX() {
		return getX() + getWidth();
	}

	/**
	 * Returns the largest Y coordinate of the framing rectangle of the
	 * <code>Shape</code> in <code>double</code> precision.
	 * 
	 * @return the largest Y coordinate of the framing rectangle of the
	 *         <code>Shape</code>.
	 * @since 1.2
	 */
	public double getMaxY() {
		return getY() + getHeight();
	}

	/**
	 * Unions the pair of source <code>Rectangle2D</code> objects and puts the
	 * result into the specified destination <code>Rectangle2D</code> object.
	 * One of the source rectangles can also be the destination to avoid
	 * creating a third Rectangle2D object, but in this case the original points
	 * of this source rectangle will be overwritten by this method.
	 * 
	 * @param src1
	 *            the first of a pair of <code>Rectangle2D</code> objects to be
	 *            combined with each other
	 * @param src2
	 *            the second of a pair of <code>Rectangle2D</code> objects to be
	 *            combined with each other
	 * @param dest
	 *            the <code>Rectangle2D</code> that holds the results of the
	 *            union of <code>src1</code> and <code>src2</code>
	 * @since 1.2
	 */
	public static void union(Rectangle2D src1, Rectangle2D src2,
			Rectangle2D dest) {
		double x1 = Math.min(src1.getMinX(), src2.getMinX());
		double y1 = Math.min(src1.getMinY(), src2.getMinY());
		double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
		double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
		dest.setFrameFromDiagonal(x1, y1, x2, y2);
	}

	/**
	 * Sets the diagonal of the framing rectangle of this <code>Shape</code>
	 * based on the two specified coordinates. The framing rectangle is used by
	 * the subclasses of <code>RectangularShape</code> to define their geometry.
	 * 
	 * @param x1
	 *            the X coordinate of the start point of the specified diagonal
	 * @param y1
	 *            the Y coordinate of the start point of the specified diagonal
	 * @param x2
	 *            the X coordinate of the end point of the specified diagonal
	 * @param y2
	 *            the Y coordinate of the end point of the specified diagonal
	 * @since 1.2
	 */
	public void setFrameFromDiagonal(double x1, double y1, double x2, double y2) {
		if (x2 < x1) {
			double t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y2 < y1) {
			double t = y1;
			y1 = y2;
			y2 = t;
		}
		setFrame(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * Sets the location and size of the outer bounds of this
	 * <code>Rectangle2D</code> to the specified rectangular values.
	 * 
	 * @param x
	 *            the X coordinate of the upper-left corner of this
	 *            <code>Rectangle2D</code>
	 * @param y
	 *            the Y coordinate of the upper-left corner of this
	 *            <code>Rectangle2D</code>
	 * @param w
	 *            the width of this <code>Rectangle2D</code>
	 * @param h
	 *            the height of this <code>Rectangle2D</code>
	 * @since 1.2
	 */
	public void setFrame(double x, double y, double w, double h) {
		setRect(x, y, w, h);
	}

	/**
	 * Sets the location and size of this <code>Rectangle2D</code> to the
	 * specified <code>double</code> values.
	 * 
	 * @param x
	 *            the X coordinate of the upper-left corner of this
	 *            <code>Rectangle2D</code>
	 * @param y
	 *            the Y coordinate of the upper-left corner of this
	 *            <code>Rectangle2D</code>
	 * @param w
	 *            the width of this <code>Rectangle2D</code>
	 * @param h
	 *            the height of this <code>Rectangle2D</code>
	 * @since 1.2
	 */
	public abstract void setRect(double x, double y, double w, double h);

	/**
	 * Intersects the pair of specified source <code>Rectangle2D</code> objects
	 * and puts the result into the specified destination
	 * <code>Rectangle2D</code> object. One of the source rectangles can also be
	 * the destination to avoid creating a third Rectangle2D object, but in this
	 * case the original points of this source rectangle will be overwritten by
	 * this method.
	 * 
	 * @param src1
	 *            the first of a pair of <code>Rectangle2D</code> objects to be
	 *            intersected with each other
	 * @param src2
	 *            the second of a pair of <code>Rectangle2D</code> objects to be
	 *            intersected with each other
	 * @param dest
	 *            the <code>Rectangle2D</code> that holds the results of the
	 *            intersection of <code>src1</code> and <code>src2</code>
	 * @since 1.2
	 */
	public static void intersect(Rectangle2D src1, Rectangle2D src2,
			Rectangle2D dest) {
		double x1 = Math.max(src1.getMinX(), src2.getMinX());
		double y1 = Math.max(src1.getMinY(), src2.getMinY());
		double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
		double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
		dest.setFrame(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * Adds a point, specified by the double precision arguments
	 * <code>newx</code> and <code>newy</code>, to this <code>Rectangle2D</code>
	 * . The resulting <code>Rectangle2D</code> is the smallest
	 * <code>Rectangle2D</code> that contains both the original
	 * <code>Rectangle2D</code> and the specified point.
	 * <p>
	 * After adding a point, a call to <code>contains</code> with the added
	 * point as an argument does not necessarily return <code>true</code>. The
	 * <code>contains</code> method does not return <code>true</code> for points
	 * on the right or bottom edges of a rectangle. Therefore, if the added
	 * point falls on the left or bottom edge of the enlarged rectangle,
	 * <code>contains</code> returns <code>false</code> for that point.
	 * 
	 * @param newx
	 *            the X coordinate of the new point
	 * @param newy
	 *            the Y coordinate of the new point
	 * @since 1.2
	 */
	public void add(double newx, double newy) {
		double x1 = Math.min(getMinX(), newx);
		double x2 = Math.max(getMaxX(), newx);
		double y1 = Math.min(getMinY(), newy);
		double y2 = Math.max(getMaxY(), newy);
		setRect(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * The <code>Double</code> class defines a rectangle specified in double
	 * coordinates.
	 * 
	 * @since 1.2
	 */
	public static class Double extends Rectangle2D implements Serializable {
		/**
		 * The X coordinate of this <code>Rectangle2D</code>.
		 * 
		 * @since 1.2
		 * @serial
		 */
		public double x;

		/**
		 * The Y coordinate of this <code>Rectangle2D</code>.
		 * 
		 * @since 1.2
		 * @serial
		 */
		public double y;

		/**
		 * The width of this <code>Rectangle2D</code>.
		 * 
		 * @since 1.2
		 * @serial
		 */
		public double width;

		/**
		 * The height of this <code>Rectangle2D</code>.
		 * 
		 * @since 1.2
		 * @serial
		 */
		public double height;

		/**
		 * Constructs a new <code>Rectangle2D</code>, initialized to location
		 * (0,&nbsp;0) and size (0,&nbsp;0).
		 * 
		 * @since 1.2
		 */
		public Double() {
		}

		/**
		 * Constructs and initializes a <code>Rectangle2D</code> from the
		 * specified <code>double</code> coordinates.
		 * 
		 * @param x
		 *            the X coordinate of the upper-left corner of the newly
		 *            constructed <code>Rectangle2D</code>
		 * @param y
		 *            the Y coordinate of the upper-left corner of the newly
		 *            constructed <code>Rectangle2D</code>
		 * @param w
		 *            the width of the newly constructed
		 *            <code>Rectangle2D</code>
		 * @param h
		 *            the height of the newly constructed
		 *            <code>Rectangle2D</code>
		 * @since 1.2
		 */
		public Double(double x, double y, double w, double h) {
			setRect(x, y, w, h);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		@Override
		public double getX() {
			return x;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		@Override
		public double getY() {
			return y;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		@Override
		public double getWidth() {
			return width;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		@Override
		public double getHeight() {
			return height;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		public boolean isEmpty() {
			return (width <= 0.0) || (height <= 0.0);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		@Override
		public void setRect(double x, double y, double w, double h) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		public void setRect(Rectangle2D r) {
			this.x = r.getX();
			this.y = r.getY();
			this.width = r.getWidth();
			this.height = r.getHeight();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		public int outcode(double x, double y) {
			int out = 0;
			if (this.width <= 0) {
				out |= OUT_LEFT | OUT_RIGHT;
			} else if (x < this.x) {
				out |= OUT_LEFT;
			} else if (x > this.x + this.width) {
				out |= OUT_RIGHT;
			}
			if (this.height <= 0) {
				out |= OUT_TOP | OUT_BOTTOM;
			} else if (y < this.y) {
				out |= OUT_TOP;
			} else if (y > this.y + this.height) {
				out |= OUT_BOTTOM;
			}
			return out;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		public Rectangle2D getBounds2D() {
			return new Double(x, y, width, height);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		public Rectangle2D createIntersection(Rectangle2D r) {
			Rectangle2D dest = new Rectangle2D.Double();
			Rectangle2D.intersect(this, r, dest);
			return dest;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 1.2
		 */
		public Rectangle2D createUnion(Rectangle2D r) {
			Rectangle2D dest = new Rectangle2D.Double();
			Rectangle2D.union(this, r, dest);
			return dest;
		}

		/**
		 * Returns the <code>String</code> representation of this
		 * <code>Rectangle2D</code>.
		 * 
		 * @return a <code>String</code> representing this
		 *         <code>Rectangle2D</code>.
		 * @since 1.2
		 */
		@Override
		public String toString() {
			return getClass().getName() + "[x=" + x + ",y=" + y + ",w=" + width
					+ ",h=" + height + "]";
		}

		/*
		 * JDK 1.6 serialVersionUID
		 */
		private static final long serialVersionUID = 7771313791441850493L;
	}
}