import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;

public class ContinuousCalendar {
	
	private static String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
	private static String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", 
			                          "August", "September", "October", "November", "December"};
	
	private static Color MONTH_FILL1 = Color.WHITE;
	private static Color MONTH_FILL2 = new Color(230,230,230);
	
	private static int RECT_X_OFFSET = 100;
	private static int RECT_Y_OFFSET = 100;
	private static int RECT_WIDTH = 80;
	private static int RECT_HEIGHT = 60;
	

	/**
	 * Takes one user arg: which year
	 */
	public static void main(String[] args) throws IOException {
		// get year
		int year = Integer.parseInt(args[0]);
		
		// Find starting weekday
		// year is actual, month is 0-indexed, number date of month is 1-indexed
		// day of week is 1-indexed for some reason!
		GregorianCalendar calendar = new GregorianCalendar(year, Calendar.JANUARY, 1);
		int weekdayOfStart = calendar.get(Calendar.DAY_OF_WEEK)-1; //0=sunday, 1=monday ...
		
		// find month lengths and whether month starts on sunday
		int[] monthLengths = new int[12]; //0=jan, 1=feb ...
		boolean[] monthStartsOnSunday = new boolean[12];
		for (int month=0; month<12; month++) {
			GregorianCalendar cal = new GregorianCalendar(year, month, 1);
			int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			monthLengths[month] = daysInMonth;
			monthStartsOnSunday[month] = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
		}
		
		// Create Calendar
		BufferedImage canvas = createContinuousCalendar(year, weekdayOfStart,
				monthLengths, monthStartsOnSunday);
		
		// Create File
		ImageIO.write(canvas, "png", new File("Continuous"+year+".png"));
		
		// Display via JFrame
		JFrameHelper jf = new JFrameHelper(800, 600);
		jf.pasteImage(canvas, 0, 0);
		
		/*int y = 0;
		while (y > -4000) {
			a.pasteImage(canvas, 0, y);
			a.repaint();
			y -= 3;
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}

	/**
	 * Create calendar image
	 * @param year Year
	 * @param weekdayOfStart Which day of the week the year starts on 0=sunday, 1=monday, ...
	 * @param monthLengths Array of length of month in days
	 * @param monthStartsOnSunday Array of whether month starts on Sunday 
	 * @return The image of calendar
	 */
	private static BufferedImage createContinuousCalendar(int year,
			int weekdayOfStart, int[] monthLengths,
			boolean[] monthStartsOnSunday) {
		
		// Calculate dimensions
		int neededX = RECT_X_OFFSET + 7*RECT_WIDTH+100;
		int neededY = RECT_Y_OFFSET + 53*RECT_HEIGHT+50;
		
		//  Create image
		BufferedImage canvas = new BufferedImage(neededX, neededY, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = canvas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Fill background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, neededX, neededY);
		
		// Create title
		g.setColor(Color.BLACK);
		drawText(g,	"Continuous "+year, RECT_X_OFFSET + (int) (RECT_WIDTH*3.5), RECT_Y_OFFSET - 50, 32, true, false);
		
		// Write days of week
		for (int i=0; i<7; i++) {
			int xpos = RECT_X_OFFSET + (RECT_WIDTH*i) + (RECT_WIDTH/2);
			int ypos = RECT_Y_OFFSET - 10;
			drawText(g, DAYS[i], xpos, ypos, 16, true, false);
		}
		
		// Draw all rectangles
		int[] weekNumbers = drawRectangles(g, weekdayOfStart, monthLengths);
		
		// Write month names
		for (int i=0; i<12; i++) {
			int offset = (monthStartsOnSunday[i] ? 1 : 2);
			int xpos = -20 + RECT_X_OFFSET;
			int ypos = (int) ((weekNumbers[i]+offset) * RECT_HEIGHT + RECT_Y_OFFSET);
			drawTextSideways(g, MONTHS[i], xpos, ypos, 24, false, false);
		}
		
		g.dispose();
		return canvas;
	}

	/**
	 * Draws all of the day rectangles onto the calendar
	 * @param g Graphics2D object
	 * @param weekdayOfStart Which day of the week the year starts on 0=sunday, 1=monday, ...
	 * @param monthLengths Array of length of month in days
	 * @return Array[12] of which number week each month's 1st day falls on
	 */
	private static int[] drawRectangles(Graphics2D g, int weekdayOfStart, int[] monthLengths) {
		
		int dayOfWeek = weekdayOfStart; //0=sunday, 1=monday ...
		int weekNumber = 0;
		int[] weekNumberOfMonth = new int[12];
		for (int month=0; month<12; month++) { //month 0-indexed
			weekNumberOfMonth[month] = weekNumber;
			int monthLength = monthLengths[month]; //days in month
			for (int day=0; day<monthLength;day++) { //day of month 0-indexed
				String displayedDay = Integer.toString(day + 1); //what number you see on the calendar
				
				//System.out.println(weekNumber+" - "+(month+1)+"/"+displayedDay+"/"+2015+" - "+DAYS[dayOfWeek]);
				
				// At this point we have:
				//    dayOfYear - 0-indexed count of which day of the year it is
				//    dayOfWeek - 0-indexed starting on Sunday, which day of week it is
				//    month     - 0-indexed month of year
				//    monthLength - num days in this month
				//    day       - 0-indexed day of month
				//    displayedDay - 1-indexed day of month
				//    weekNumber - 0-indexed which number week is it?
				
				int xpos = RECT_X_OFFSET + dayOfWeek*RECT_WIDTH;
				int ypos = RECT_Y_OFFSET + weekNumber*RECT_HEIGHT;
				
				g.setColor(month%2 == 0 ? MONTH_FILL1 : MONTH_FILL2);
				g.fillRect(xpos, ypos, RECT_WIDTH, RECT_HEIGHT);
				g.setColor(Color.BLACK);
				g.drawRect(xpos, ypos, RECT_WIDTH, RECT_HEIGHT);
				
				drawText(g, displayedDay, xpos + 5, ypos + 5, 16, false, true);
				
				//end
				dayOfWeek = (dayOfWeek + 1) % 7;
				if (dayOfWeek == 0) weekNumber++;
			}
		}
		return weekNumberOfMonth;
	}
	
	/**
	 * Draw text!
	 * @param g Graphics2D object
	 * @param txt String to write
	 * @param x position in pixels
	 * @param y position in pixels
	 * @param fontSize size in pts
	 * @param centered if true, x will be the position of center, otherwise x will be pos of leftmost char
	 * @param topAligned if true, y will be the position of top, otherwise y will be pos of the bottom
	 */
	private static void drawText(Graphics2D g, String txt, int x, int y, int fontSize, boolean centered, boolean topAligned) {
		 Font myFont = new Font("Arial", Font.BOLD, fontSize);
		 
		 FontMetrics fm = g.getFontMetrics(myFont);
		 int width = fm.stringWidth(txt);
		 int xpos = centered ? x - width/2 : x;
		 int ypos = topAligned ? y + fm.getAscent()-2 : y;
		 
		 g.setFont(myFont);
		 g.drawString(txt, xpos, ypos); 
	}
	
	/**
	 * Draw text rotated 90 degrees CCW
	 * @param g Graphics 2D object
	 * @param txt String to write
	 * @param x position in pixels
	 * @param y position in pixels
	 * @param fontSize size in pts
	 * @param centered if true, y will be the position of center, otherwise y will be the pos of the topmost character
	 * @param topAligned if true, x will be the position of bottom (farthest right) of text, otherwise x will be pos of top (farthest left)
	 */
	private static void drawTextSideways(Graphics2D g, String txt, int x, int y, int fontSize, boolean centered, boolean topAligned) {
		Font myFont = new Font("Arial", Font.BOLD, fontSize);
		
		AffineTransform orig = g.getTransform();
		g.rotate(-Math.PI/2);
		if (centered) {
			drawText(g, txt, -y, x, fontSize, centered, topAligned);
		} else {
			FontMetrics fm = g.getFontMetrics(myFont);
			int height = fm.stringWidth(txt);
			
			drawText(g, txt, -(y+height), x, fontSize, false, topAligned);
		}
		g.setTransform(orig);
	}

}

