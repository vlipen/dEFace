package defa.logic;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RecognizedFace extends JPanel implements Runnable{
    public RecognizedFace(){
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
                if (isFirstTime){
                    Thread.sleep(FaceDetection.FRAME_RATE * 30 + 100);
                    isFirstTime = false;
                } else Thread.sleep(FaceDetection.FRAME_RATE * 30);
            } catch (InterruptedException iEx){
                iEx.printStackTrace();
            }

            if (recognizing.getFace() != null)
                face = recognizing.getFace();

            repaint();
        }
        isFirstTime = true;
    }

    public Thread getThread() {
        return t;
    }

    public void setThread(FaceRecognizing faceRecognizing) {
        recognizing = faceRecognizing;
        t = new Thread(this);
    }

    public void clearThread(){
        t = null;
    }

    private Thread t = null;
    private boolean isFirstTime = true;
    private Image face = new BufferedImage(FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

    private FaceRecognizing recognizing;
}
