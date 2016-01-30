package defa.logic;

import defa.gui.AddingPersonWindow;
import defa.gui.MainWindow;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import net.sf.libai.common.Function;
import net.sf.libai.common.Matrix;
import net.sf.libai.nn.NeuralNetwork;
import net.sf.libai.nn.supervised.MLP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TrainingNN implements Runnable{
    public TrainingNN(AddingPersonWindow window){
        images = new ArrayList<File>();
        apw = window;
        if (apw.hiddenNeurons != 0)
            hiddenNeurons = apw.hiddenNeurons;
        if (apw.maxIterations != 0)
            maxIterations = apw.maxIterations;
    }
    
    public TrainingNN(MainWindow mainWindow){
        images = new ArrayList<File>();
        mw = mainWindow;
        if (Integer.parseInt(mw.getSpinnerNeurons().getValue().toString()) != 0)
            hiddenNeurons = Integer.parseInt(mw.getSpinnerNeurons().getValue().toString());
        if (Integer.parseInt(mw.getSpinnerIterations().getValue().toString()) != 0)
            maxIterations = Integer.parseInt(mw.getSpinnerIterations().getValue().toString());
    }

    public TrainingNN(String fileName){
        mlp = MLP.open(fileName);
    }
    
    @Override
    public void run(){
        File foldersList[] = new File("facebase").listFiles();
        for (File dir : foldersList)
            if (dir.list().length == 10)
                images.addAll(Arrays.asList(dir.listFiles()));
        outputNeurons = images.size() / 10;
        initialVectors = new ArrayList<int[]>(outputNeurons * 10);
        mlp = new MLP(new int[]{inputNeurons, hiddenNeurons, outputNeurons}, new Function[]{NeuralNetwork.identity, NeuralNetwork.sigmoid, NeuralNetwork.sigmoid});
        init();

//        fileOut("vectors.txt", true);
//        fileOut("matrix.txt", false);

        if (mw == null && apw != null)
            mlp.setProgressBar(apw.getProgressBar());
        else if (apw == null && mw != null){
            mw.getProgressBar().setVisible(true);
            mlp.setProgressBar(mw.getProgressBar());
        }
        mlp.train(patterns, answers, 0.2, maxIterations);
//        System.out.println(mlp.error(patterns, answers));
        mlp.save("mlp.nn");
        JOptionPane.showMessageDialog(null, "Done!");
        if (mw != null) {
            mw.getProgressBar().setVisible(false);
            mw.getProgressBar().setValue(0);
        }
    }
    
    private void init(){
        patterns = new Matrix[outputNeurons * 10];       
        try {
            int counter = 0;
            for (File image : images){
                int[] tempPixels = new int[FaceDetection.FACE_WIDTH * FaceDetection.FACE_HEIGHT];
                BufferedImage tempBufImg = new BufferedImage(FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                Graphics graphics = tempBufImg.getGraphics();
                graphics.drawImage(ImageIO.read(image), 0, 0, null);
                graphics.dispose();
                tempBufImg.getRaster().getPixels(0, 0, FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, tempPixels);
                initialVectors.add(tempPixels);
                
                patterns[counter] = new Matrix(inputNeurons, 1, discreteFourierTransform(tempPixels));
                
                counter++;
            }
        } catch (IOException ioEx){
            ioEx.printStackTrace();
        }

        double desireOutputs[][] = new double[outputNeurons][outputNeurons];
        for (int i = 0; i < outputNeurons; i++)
            for (int j = 0; j < outputNeurons; j++)
                if (i == j) desireOutputs[i][j] = 1;
        answers = new Matrix[outputNeurons * 10];
        for (int i = 0; i < answers.length; i++)
            answers[i] = new Matrix(outputNeurons, 1, desireOutputs[i / 10]);
    }
    
    public double[] discreteFourierTransform(int input[]){
        DoubleFFT_1D fft1 = new DoubleFFT_1D(input.length);
        DoubleFFT_1D fft2 = new DoubleFFT_1D(inputNeurons);
        double tempInput[] = new double[input.length * 2];
        for (int i = 0; i < tempInput.length; i++)
            if (i % 2 == 0) tempInput[i] = input[i / 2];
        fft1.complexForward(tempInput);
        double tempResult[] = new double[inputNeurons * 2];
        System.arraycopy(tempInput, 0, tempResult, 0, inputNeurons * 2);
        fft2.complexInverse(tempResult, true);
        double result[] = new double[inputNeurons];
        for (int i = 0; i < tempResult.length; i += 2)
            result[i / 2] = Math.sqrt(Math.pow(tempResult[i], 2) + Math.pow(tempResult[i + 1], 2));
        double min = result[0];
        double max = result[0];
        for (int i = 1; i < result.length; i++){
            if (result[i] < min) min = result[i];
            if (result[i] > max) max = result[i];
        }
        for (int i = 0; i < result.length; i++)
            result[i] = (result[i] - min) / max;
        return result;
    }

    private void fileOut(String name, boolean isInit){
        try {
            FileWriter fileWriter = new FileWriter(name);
            if (isInit){
                for (int i = 0; i < images.size(); i++)
                    fileWriter.write(images.get(i).getName() + "\r\n" + Arrays.toString(initialVectors.get(i)) + "\r\n\r\n");
            } else {
                for (int i = 0; i < patterns.length; i++){
                    for (int j = 0; j < patterns[i].getRows(); j++)
                        fileWriter.write(String.valueOf(patterns[i].position(j, 0)) + "\t");
                    fileWriter.write("\r\n");
                    for (int j = 0; j < answers[i].getRows(); j++)
                        fileWriter.write(String.valueOf(answers[i].position(j, 0)) + "\t");
                    fileWriter.write("\r\n\r\n");
                }
            }
            fileWriter.close();
        } catch (IOException ioEx){
            ioEx.printStackTrace();
        }
    }

    public MLP getMLP(){
        return mlp;
    }

    private ArrayList<File> images;
    private ArrayList<int[]> initialVectors;
    private Matrix patterns[];
    private Matrix answers[];
    
    private static final int inputNeurons = 200;
    private static int hiddenNeurons = 100;
    private int outputNeurons;
    private int maxIterations = 100;

    private MLP mlp;
    private MainWindow mw = null;
    private AddingPersonWindow apw = null;
}
