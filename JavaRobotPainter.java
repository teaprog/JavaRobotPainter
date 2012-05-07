package javaRobotPainter;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * When this class is instantiated a bot is created, that draws a specified picture in Microsoft Paint.
 * This program has only been tested on Windows Vista with MS Paint version 6.0.
 * <br /><br />
 * NOTE: The delay between each operation and the time to start MS Paint may have to be adjusted.
 * The shortcut key to maximize the window, in {@code initMsPaint()}, may also have to be changed.
 * It works with the Norwegian version of Vista.
 * 
 * @author teaprog
 * @version 1.0, 30/04/12
 */
public class JavaRobotPainter {
	/** The size of the screen. */
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	/** Where to start painting on the x-axis. */
	private static final int START_POSITION_X = 61;
	/** Where to start painting on the y-axis. */
	private static final int START_POSITION_Y = 94;
	
	/** The size of the rectangles being drawn. */
	private int rectangleSize = 5;
	
	/** The colors this bot has at its disposal. */
	private ArrayList<Color> availableColors = new ArrayList<Color>();
	/** The position for each color. */
	private HashMap<Color, Point> colorPosition = new HashMap<Color, Point>();
	/** The colors available. */
	private enum ColorNr {COLOR_ONE, COLOR_TWO};
	/** The current color selected. */
	private ColorNr currentColor = ColorNr.COLOR_ONE;
	/** The selected colors. (By default color one is black and color two is white) */
	private Color[] selectedColors = new Color[]{new Color(0, 0, 0), new Color(255, 255, 255)};
	
	/** The image that is going to be drawn in MS Paint. */
	private BufferedImage image;
	
	/** The process of MS Paint. */
	private Process msPaint;
	
	/** The robot that is going to draw the image. */
	private Robot robot;
	
	/** The delay between each bot operation. This may have to be adjusted for different computers. */
	private int robotDelay = 1;
	
	/** The time that is required to start MS Paint. This may have to be adjusted for different computers. */
	private long timeRequiredToStartMsPaint = 1000;
	
	/**
	 * Executes MS Paint and starts drawing.
	 * 
	 * @throws AWTException
	 * @throws IOException 
	 */
	public JavaRobotPainter() throws AWTException, IOException  {
		image = ImageIO.read(new File("img/image.jpg"));
		robot = new Robot();
		robot.setAutoDelay(robotDelay);
		
		initMsPaint();
		startPainting();
	}
	
	/**
	 * Executes MS Paint, sets the attributes of the picture, initializes the available colors 
	 * and chooses a filled rectangle as drawing utility.
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private void initMsPaint() throws IOException {
		msPaint = Runtime.getRuntime().exec("mspaint.exe");
		
		// wait until mspaint.exe starts
		long time = System.currentTimeMillis();
		while(System.currentTimeMillis() - time < timeRequiredToStartMsPaint);

		// maximize the window
		// NOTE: the shortcut may vary, depending on the language your windows version has
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_SPACE);
		robot.keyPress(KeyEvent.VK_M);
		robot.keyRelease(KeyEvent.VK_M);
		robot.keyRelease(KeyEvent.VK_SPACE);
		robot.keyRelease(KeyEvent.VK_ALT);
		
		setAttributes(image.getWidth(), image.getHeight());
		initColors();
		chooseFilledRectangle();
	}
	
	/**
	 * Sets the canvas size in MS Paint.
	 * 
	 * @param width - The width of the image to be drawn.
	 * @param height - The height of the image to be drawn.
	 */
	private void setAttributes(int width, int height) {
		// if the width or height is too large then exit program
		if (width + START_POSITION_X > screenSize.width || height + START_POSITION_Y > screenSize.height) {
			System.out.println("The width or height is too large!");
			msPaint.destroy();
			System.exit(0);
		}
		
		// open attributes window
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_E);
		robot.keyRelease(KeyEvent.VK_E);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		
		// set width and height
		setCliboard(new Integer(width).toString());
		pasteDataFromClipboard();
		robot.keyPress(KeyEvent.VK_TAB);
		robot.keyRelease(KeyEvent.VK_TAB);
		setCliboard(new Integer(height).toString());
		pasteDataFromClipboard();
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
	}
	
	/**
	 * Initializes the available colors. That is the 28 colors in the color panel above the canvas.
	 */
	private void initColors() {
		int interval = 16; // The distance the mouse is going to be moved from each color.
		int startX = 95; // The x position to the first color in the panel.
		int startY = 55; // The y position to the first color in the panel.
		
		// Finds the colors and adds them to avaibleColors, and puts the color position in colorPosition.
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 2; j++) {
				Color color = robot.getPixelColor(startX + i*interval, startY + j*interval);
				availableColors.add(color);
				colorPosition.put(color, new Point(startX + i*interval, startY + j*interval));
			}
		}
	}
	
	/**
	 * Sets a filled rectangle as drawing tool.
	 */
	private void chooseFilledRectangle() {
		// choose rectangle
		robot.mouseMove(15, 210);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
		// choose filled
		robot.mouseMove(15, 300);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	/**
	 * Presses the keys "Ctrl" and "V" to paste the content from the clipboard.
	 */
	private void pasteDataFromClipboard() {
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
	}
	
	/**
	 * Sets the cliboard to the specified string.
	 * 
	 * @param string - The string to be copied into clipboard.
	 */
	public void setCliboard(String string) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(string), null);
	}
	
	/**
	 * Starts painting the image in MS Paint.
	 * If the user moves the mouse then the program terminates.
	 */
	private void startPainting() {
		Point point = new Point(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		for (int i = START_POSITION_Y; i-START_POSITION_Y < image.getHeight(); i += rectangleSize) {
			for (int j = START_POSITION_X; j-START_POSITION_X < image.getWidth(); j += rectangleSize) {
				// if user moves the mouse then the program terminates
				if (point.x != MouseInfo.getPointerInfo().getLocation().x || point.y != MouseInfo.getPointerInfo().getLocation().y)
					System.exit(0);
				
				setColor(new Color(image.getRGB(j-START_POSITION_X, i-START_POSITION_Y)));
					
				int mouseButton = (currentColor == ColorNr.COLOR_ONE) ? InputEvent.BUTTON1_MASK : InputEvent.BUTTON3_MASK;
				robot.mouseMove(j, i);
				robot.mousePress(mouseButton);	
				robot.mouseMove(j + rectangleSize, i + rectangleSize);
				robot.mouseRelease(mouseButton);
				
				// set the position of the last point where the mouse was
				point.setLocation(j + rectangleSize, i + rectangleSize);
			}
		}
	}
	
	/**
	 * Changes the color if necessary, i.e. if the color is not in {@code selectedColors[0]} or {@code selectedColors[1]}.
	 * 
	 * @param pixelColor - The pixel color, from the image, where the program has to find an approximated color in {@code availableColors}.
	 */
	private void setColor(Color pixelColor) {
		Color mostAccurateColor = findMostAccurateColorAvailable(pixelColor);
		
		if (selectedColors[0].equals(mostAccurateColor)) {
			currentColor = ColorNr.COLOR_ONE;
		}
		else if (selectedColors[1].equals(mostAccurateColor)) {
			currentColor = ColorNr.COLOR_TWO;
		}
		else { // If the color is not in selectedColors, then select new color from the panel.
			Point p = colorPosition.get(mostAccurateColor);
			
			int mouseButton;
			if (currentColor == ColorNr.COLOR_ONE) {
				mouseButton = InputEvent.BUTTON3_MASK;
				currentColor = ColorNr.COLOR_TWO;
				selectedColors[1] = mostAccurateColor;
			}
			else {
				mouseButton = InputEvent.BUTTON1_MASK;
				currentColor = ColorNr.COLOR_ONE;
				selectedColors[0] = mostAccurateColor;
			}
			
			robot.mouseMove(p.x, p.y);
			robot.mousePress(mouseButton);
			robot.mouseRelease(mouseButton);
		}
	}
	
	/**
	 * Finds the most accurate color that is available in {@code availableColors}.
	 * 
	 * @param pixelColor - The pixel color in the image.
	 * @return the most accurate color from {@code availableColors}
	 */
	private Color findMostAccurateColorAvailable(Color pixelColor) {
		double minError = Double.MAX_VALUE;
		int bestColorIndex = 0;
		for (int i = 0; i < availableColors.size(); i++) {
			int r1 = availableColors.get(i).getRed();
			int g1 = availableColors.get(i).getGreen();
			int b1 = availableColors.get(i).getBlue();
			
			int r2 = pixelColor.getRed();
			int g2 = pixelColor.getGreen();
			int b2 = pixelColor.getBlue();
			
			// 2-norm distance
			double error = Math.sqrt((r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2));
			
			if (error < minError) {
				minError = error;
				bestColorIndex = i;
			}
		}
		
		return availableColors.get(bestColorIndex);
	}
	
	/**
	 * The main method.
	 * 
	 * @param args
	 * @throws AWTException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws AWTException, IOException {
		new JavaRobotPainter();
	}
}
