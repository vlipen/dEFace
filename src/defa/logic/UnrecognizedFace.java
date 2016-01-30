package defa.logic;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UnrecognizedFace extends JPanel implements Runnable{
    public UnrecognizedFace(){
        super();
    }
    
    @Override
    public void paint(Graphics g){
        g.drawImage(face, 0, 0, null);
    }

    @Override
    public void run() {
        while (t != null){
            try {
                Thread.sleep(FaceDetection.FRAME_RATE * 3);
            } catch (InterruptedException iEx){
                iEx.printStackTrace();
            }

            face = fd.getImage();

            repaint();
        }
    }

    public Thread getThread() {
        return t;
    }

    public void setThread(FaceDetection faceDetection) {
        fd = faceDetection;
        t = new Thread(this);
    }
    
    public void clearThread(){
        t = null;
    }

    public Image getFace() {
        return face;
    }

    private Thread t = null;
    private Image face = new BufferedImage(FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

    private FaceDetection fd;
}
