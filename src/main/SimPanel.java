package main;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vectors.Matrix2D;
import vectors.Matrix3D;
import vectors.Point2D;
import vectors.Point3D;

public class SimPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int scale = 1;
	int screenheight;
	int screenwidth;
	int screenarea;
	int threadnum = 0;
	boolean started = false;
	BufferedImage frame;
	boolean painted;
	int timemilis = 0;
	Point2D Mouse = Point2D.Origin();
	Point2D MouseMovement = Point2D.Origin();
	boolean[] isheld = new boolean[66536];
	boolean[] ispressed = new boolean[66536];
	boolean[] pressqueued = new boolean[66536];
	double angle = 0;
	double movementspeed = 0.01;
	boolean lockedMouse = false;
	boolean detectmovement = true;
	JFrame panel;
	BufferedImage[] images;
	Player player = new Player(Point2D.Origin(), Point2D.Origin());
	Matrix2D revtransform = Matrix2D.rotation(-angle);
	Point2D playerbuf = player.pos;

	public SimPanel(int width, int height, JFrame container) {
		loadImages();
		panel = container;
		screenwidth = width;
		screenheight = height;
		screenarea = screenwidth * screenheight;
		setPreferredSize(new Dimension(screenwidth * scale, screenheight * scale));
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
		panel.addKeyListener(this);
		frame = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_ARGB);
		for (; threadnum < 3; threadnum++) {
			new Thread(this).start();
			while (!started) {
			}
			started = false;
		}
	}

	private void loadImages() {
		images = new BufferedImage[2];
		URL imagedir = getClass().getClassLoader().getResource("assets/images/");
		try {
			images[0] = ImageIO.read(new URL(imagedir.toString().concat("floor.png")));
			images[1] = ImageIO.read(new URL(imagedir.toString().concat("car.png")));
		} catch (IOException e) {
		}
	}

	protected void contentUpdate() {
		try {
			if (images[0].getRGB((int) player.pos.x, (int) player.pos.y) == 0xff00ff00) {
				player.vel = player.vel.scale(0.9);
			}
		} catch (Exception e) {
		}
		player.update(0.0625);
		boundUpdate();
		player.vel = player.vel.scale(0.999);
		player.vel = player.vel.scale(ispressed[KeyEvent.VK_SHIFT] ? 0.995 : 1);
		inputUpdate();
		MouseMovement = Point2D.Origin();
		Arrays.fill(pressqueued, false);
	}

	private void boundUpdate() {
		if (player.pos.x < 0 || player.pos.x > images[0].getWidth()) {
			player.vel.x *= -1;
		}
		if (player.pos.y < 0 || player.pos.y > images[0].getHeight()) {
			player.vel.y *= -1;
		}
		player.update(0.0625);
	}

	private void inputUpdate() {
		if (ispressed[KeyEvent.VK_W]) {
			player.vel = Point2D.add(player.vel,
					new Point2D(Math.sin(angle) * movementspeed, Math.cos(angle) * movementspeed));

		}
		if (pressqueued[KeyEvent.VK_E]) {
			lockedMouse = !lockedMouse;
			if (lockedMouse) {
				Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
						new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "Blank Cursor");
				panel.getContentPane().setCursor(blankCursor);
			}
			if (!lockedMouse) {
				panel.getContentPane().setCursor(Cursor.getDefaultCursor());
			}

		}
	}

	protected void graphicsUpdate() {
		playerbuf.x = player.pos.x;
		playerbuf.y = player.pos.y;
		revtransform = Matrix2D.rotation(-angle);
		Matrix2D view = Matrix2D.rotation(angle);
		Point2D camera = Point2D.add(Point2D.add(playerbuf, new Point2D(Math.sin(angle) * -1, Math.cos(angle) * -1)),
				player.vel.scale(8));
		Matrix3D camerarot = Matrix3D.rotx(Math.PI * 0.125);
		for (int i = 0; i < screenarea; i++) {
			double x = (i % screenwidth) / (double) screenwidth;
			double y = (i / screenwidth) / (double) screenheight;
			Point3D ray = camerarot.transform(new Point3D((x - 0.5) * screenwidth / screenheight, 0.5 - y, 0.5));
			Point2D relativecoords = new Point2D(ray.x / ray.z * 256, ray.y / ray.z * 256);
			Point2D floorcoords = Point2D.add(view.transform(relativecoords), camera);
			frame.setRGB(i % screenwidth, i / screenwidth, floortex(floorcoords));

		}
		// frame.getGraphics().drawImage(images[1], (screenwidth>>1)-50,
		// (screenheight>>1)-50,100,100, null);
	}

	protected int floortex(Point2D coords) {
		Point2D coordsnew = revtransform.transform(Point2D.add(coords, playerbuf.scale(-1)));
		// coordsnew = coordsnew.scale(0.5);
		coordsnew.x += images[1].getWidth() * 0.5;
		if ((int) coordsnew.x < 0 != (int) coordsnew.x < images[1].getWidth()
				&& (int) coordsnew.y < 0 != (int) coordsnew.y < images[1].getHeight()) {
			if (images[1].getRGB((int) coordsnew.x, (int) coordsnew.y) != 0xffffff) {
				return images[1].getRGB((int) coordsnew.x, (int) coordsnew.y) | 0xff000000;
			}
		}
		if ((int) coords.x < 0 != (int) coords.x < images[0].getWidth()
				&& (int) coords.y < 0 != (int) coords.y < images[0].getHeight()) {
			return images[0].getRGB((int) coords.x, (int) coords.y);
		} else {
			return -1;
		}
	}

	protected void contentInit() {

	}

	protected void graphicsInit() {

	}

	public void run() {
		if (threadnum == 0) {
			started = true;
			contentInit();
			for (;;) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				contentUpdate();
			}
		}
		if (threadnum == 1) {
			started = true;
			graphicsInit();
			for (;;) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}

				graphicsUpdate();
				painted = false;
				repaint();
				while (!painted) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		if (threadnum == 2) {
			started = true;
			graphicsInit();
			for (;;) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				timemilis++;
			}
		}
	}

	public void paint(Graphics g) {
		g.drawImage(frame, 0, 0, screenwidth * scale, screenheight * scale, null);
		painted = true;
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		isheld[e.getButton()] = true;

	}

	public void mouseReleased(MouseEvent e) {
		isheld[e.getButton()] = false;

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);

	}

	public void mouseMoved(MouseEvent e) {
		if (lockedMouse) {
			angle += (e.getXOnScreen() - ((screenwidth >> 1) + panel.getX())) * 0.0025;
			try {
				new Robot().mouseMove((screenwidth >> 1) + panel.getX(), (screenheight >> 1) + panel.getY());
			} catch (AWTException e1) {
			}
		}

	}

	public void keyTyped(KeyEvent e) {

	}

	public void keyPressed(KeyEvent e) {
		ispressed[e.getKeyCode()] = true;
		pressqueued[e.getKeyCode()] = true;

	}

	public void keyReleased(KeyEvent e) {
		ispressed[e.getKeyCode()] = false;

	}

}
