package fr.protogen.engine.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;


public class PDFEngine {
	
	public void convertToPNG(String pdfPath, String pngPath){
		try {
	        String sourceDir = pdfPath;
	        String destinationDir = "C:/PDFCopy/";

	        File pdfFile = new File(sourceDir);
	        RandomAccessFile raf = new RandomAccessFile(pdfFile, "r");
	        FileChannel channel = raf.getChannel();
	        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
	        PDFFile pdf = new PDFFile(buf);

	        int pageNumber = 1;
	        for (int i = 0; i < pdf.getNumPages(); i++) {

	            PDFPage page = pdf.getPage(i);

	            String fileName = pdfFile.getName().replace(".pdf", "");

	            // image dimensions 
	            int width = 1200;
	            int height = 1550;

	            // create the image
	            Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
	            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	            // width & height, // clip rect, // null for the ImageObserver, // fill background with white, // block until drawing is done
	            Image image = page.getImage(width, height, rect, null, true, true );
	            Graphics2D bufImageGraphics = bufferedImage.createGraphics();
	            bufImageGraphics.drawImage(image, 0, 0, null);
	            ImageIO.write(bufferedImage, "png", new File( pngPath ));
	            pageNumber++;
	        }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
}
