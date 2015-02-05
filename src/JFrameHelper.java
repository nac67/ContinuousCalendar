import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class JFrameHelper extends JFrame {
	
	private BufferedImage canvas;
	
	public JFrameHelper (int width, int height) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setResizable(false);
		
		getContentPane().setPreferredSize(new Dimension(width, height));
		
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = canvas.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width,height);
		g.dispose();
		getContentPane().add(new JLabel(new ImageIcon(canvas)));
		
		
		pack();
		setVisible(true);
	}
	
	public void pasteImage(BufferedImage image, int x, int y) {
		Graphics g = canvas.getGraphics();
		g.drawImage(image, x, y, null);
		g.dispose();
	}
	
	public static BufferedImage loadImage (String path) {
		try {
			BufferedImage img = ImageIO.read(new File(path));
			return img;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
