package defa.logic;

import defa.gui.MainWindow;
import net.sf.libai.common.Matrix;
import net.sf.libai.nn.supervised.MLP;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FaceRecognizing implements Runnable{
    public FaceRecognizing(MainWindow mainWindow){
        mw = mainWindow;
        trainingNN = new TrainingNN("mlp.nn");
        mlp = trainingNN.getMLP();
        setThread();
    }
    
    @Override
    public void run(){
        while (t != null){
            try {
                Thread.sleep(FaceDetection.FRAME_RATE * 30);
            } catch (InterruptedException iEx){
                iEx.printStackTrace();
            }

            int tempPixels[] = new int[FaceDetection.FACE_WIDTH * FaceDetection.FACE_HEIGHT];
            Image face = mw.getUnFace().getFace();
            BufferedImage bufImg = new BufferedImage(FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
            Graphics graphics = bufImg.getGraphics();
            graphics.drawImage(face, 0, 0, null);
            graphics.dispose();
            bufImg.getRaster().getPixels(0, 0, FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, tempPixels);
            Matrix pattern = new Matrix(200, 1, trainingNN.discreteFourierTransform(tempPixels));
            answer = mlp.simulate(pattern);
            String text = "";
            for (int i = 0; i < answer.getRows(); i++)
                text += mw.getListPersons().getModel().getElementAt(i) + ": " + String.format("%.5f", answer.position(i, 0)) + "\n";
            mw.setTextRecognition(text);
        }
        mw.setTextRecognition(null);
    }

    public Image getFace(){
        double max = answer.position(0, 0);
        int index = 0;
        for (int i = 1; i < answer.getRows(); i++)
            if (max < answer.position(i, 0)){
                max = answer.position(i, 0);
                index = i;
            }
        String folder = mw.getListPersons().getModel().getElementAt(index);
        Image face = null;
        try {
            if (max > 0.1)
                face = ImageIO.read(new File("facebase/" + folder + "/" + folder + "_01.png"));
            else
                face = ImageIO.read(FaceRecognizing.class.getResourceAsStream("/icons/unFace.jpg"));
        } catch (IOException ioEx){
            ioEx.printStackTrace();
        }
        return face;
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

    private Thread t = null;
    private Matrix answer;
    private TrainingNN trainingNN;
    private MLP mlp;
    private MainWindow mw;
}
