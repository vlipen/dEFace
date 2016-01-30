package defa.gui;

import defa.logic.FaceDetection;
import defa.logic.TrainingNN;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddingPersonWindow extends JDialog {
    public AddingPersonWindow(MainWindow mainWindow) {
        mw = mainWindow;
        if (Integer.parseInt(mw.getSpinnerNeurons().getValue().toString()) != 0)
            hiddenNeurons = Integer.parseInt(mw.getSpinnerNeurons().getValue().toString());
        if (Integer.parseInt(mw.getSpinnerIterations().getValue().toString()) != 0)
            maxIterations = Integer.parseInt(mw.getSpinnerIterations().getValue().toString());
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonAdd);
        photo = mw.getUnFace();
        panelFace0.add(photo);
        Image addIcon  = null;
        try {
            addIcon = ImageIO.read(MainWindow.class.getResourceAsStream("/icons/Plus.png"));
        } catch (IOException ioEx){
            JOptionPane.showMessageDialog(null, "Icon not founded!");
        }
        buttonAdd.setIcon(new ImageIcon(addIcon));

        buttonTrain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onTrain();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAdd();
            }
        });
        nameField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                onChangeName();
            }
        });
    }

    private void onAdd(){
        switch (counter){
            case 0: panelFace1.add(photo);                    
                    break;
            case 1: panelFace2.add(photo);
                    break;
            case 2: panelFace3.add(photo);
                    break;
            case 3: panelFace4.add(photo);
                    break;
            case 4: panelFace5.add(photo);
                    break;
            case 5: panelFace6.add(photo);
                    break;
            case 6: panelFace7.add(photo);
                    break;
            case 7: panelFace8.add(photo);
                    break;
            case 8: panelFace9.add(photo);
                    break;
            case 9: mw.setPanelUnrecognized(photo);
        }
        photos.add(mw.getUnFace().getFace());
        counter++;
        if (counter == 10)
            buttonAdd.setEnabled(false);
        onChangeName();
    }
    
    private void onChangeName(){
        if (!nameField.getText().equals("") && counter == 10)
            buttonTrain.setEnabled(true);
        else buttonTrain.setEnabled(false);
    }
    
    private void onTrain() {
        boolean isWrongName = false;
        for (int i = 0; i < mw.getListPersons().getModel().getSize(); i++)
            if (mw.getListPersons().getModel().getElementAt(i).equals(nameField.getText())){
                isWrongName = true;
                break;
            }
        if (isWrongName)
            JOptionPane.showMessageDialog(null, "Change person's name.");
        else {
            buttonTrain.setEnabled(false);
            File dir = new File("facebase/" + nameField.getText());
            if (!dir.mkdir())
                JOptionPane.showMessageDialog(null, "Can't create a new folder.");
            for (int i = 0; i < 10; i++){
//                progressBar.setValue((i + 1) * 10);
                BufferedImage bufferedImage = new BufferedImage(FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D graphics = bufferedImage.createGraphics();
                graphics.drawImage(photos.get(i), 0, 0, FaceDetection.FACE_WIDTH, FaceDetection.FACE_HEIGHT, null);
                graphics.dispose();
                try {
                    String name = null;
                    if (i == 9) name = String.valueOf(i + 1);
                    else name = "0" + (i + 1);
                    ImageIO.write(bufferedImage, "png", new File(dir.getAbsolutePath() + "/" + dir.getName() + "_" + name + ".png"));
                } catch (IOException ioEx){
                    ioEx.printStackTrace();
                }
            }
//            progressBar.setValue(0);
            TrainingNN trainingNN = new TrainingNN(this);
            Thread t = new Thread(trainingNN);
            t.start();
            mw.isFirstRecognizing = true;
        }
    }

    private void onCancel() {
        mw.setPanelUnrecognized(photo);
        mw.updateList();
        photos.clear();
        dispose();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    private int counter = 0;
    private MainWindow mw;
    private JPanel photo;
    private ArrayList<Image> photos = new ArrayList<Image>(10);
    
    public int hiddenNeurons = 0;
    public int maxIterations = 0;

    public JPanel contentPane;
    private JButton buttonTrain;
    private JButton buttonCancel;
    private JProgressBar progressBar;
    private JButton buttonAdd;
    private JPanel panelFace0;
    private JPanel panelFace1;
    private JPanel panelFace2;
    private JPanel panelFace3;
    private JPanel panelFace4;
    private JPanel panelFace5;
    private JPanel panelFace6;
    private JPanel panelFace7;
    private JPanel panelFace8;
    private JPanel panelFace9;
    private JTextField nameField;
}
