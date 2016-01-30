package defa.logic;

import hypermedia.video.OpenCV;

import java.awt.*;
import java.awt.image.*;
import java.util.Vector;

public class FaceDetection extends Panel implements Runnable {
    public FaceDetection() {
        super();
        setThread();
    }

    @Override
    public void paint(Graphics g){
        g.drawImage(img, 0, 0, null);
        g.setColor( Color.RED );
        for( Rectangle rect : squares )
            g.drawRect( rect.x, rect.y, rect.width, rect.height );
    }

    @Override
    public void run() {
        OpenCV cv = new OpenCV();
        cv.capture(FRAME_WIDTH, FRAME_HEIGHT);
        cv.cascade(OpenCV.CASCADE_FRONTALFACE_ALT);
        Vector<Image> faces = new Vector<Image>();
        while( t != null ) {
            try {
                Thread.sleep(FRAME_RATE);
            } catch (InterruptedException iEx){
                iEx.printStackTrace();
            }

            cv.read();
            cv.flip(OpenCV.FLIP_HORIZONTAL);

            MemoryImageSource mis = new MemoryImageSource( cv.width, cv.height, cv.pixels(), 0, cv.width );
            img = createImage( mis );

            squares = cv.detect(1.2f, 2, OpenCV.HAAR_DO_CANNY_PRUNING, 20, 20);

            for (Rectangle face : squares)
                faces.add(createImage(new FilteredImageSource(img.getSource(), new CropImageFilter(face.x, face.y, face.width, face.height))));
            if (!faces.isEmpty()){
                bufferedImage = new BufferedImage(FACE_WIDTH, FACE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D graphics = bufferedImage.createGraphics();
                graphics.drawImage(faces.get(0), 0, 0, FACE_WIDTH, FACE_HEIGHT, null);
                graphics.dispose();                
                faces.removeAllElements();
            }

            repaint();
        }
        cv.dispose();
    }

    public Thread getThread() {
        return t;
    }

    public void setThread() {
        t = new Thread(this);
    }
    
    public void clearThread(){
        t = null;
    }

    public Image getImage() {
        return bufferedImage;
    }

    private Image img = null;
    public static final int FACE_WIDTH = 50;
    public static final int FACE_HEIGHT = 50;
    private BufferedImage bufferedImage = null;

    private Thread t = null;
    private static final int FRAME_WIDTH = 320;
    private static final int FRAME_HEIGHT = 240;
    public static final int FRAME_RATE  = 1000 / 15;
    private Rectangle[] squares = new Rectangle[0];
}