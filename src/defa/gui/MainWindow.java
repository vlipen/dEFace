package defa.gui;

import defa.logic.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainWindow {
    public MainWindow(){
        spinnerNeurons.setValue(100);
        spinnerIterations.setValue(100);
        updateList();

        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                videoControl();
            }
        });
        listPersons.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
                        Runtime.getRuntime().exec("explorer " + new File(".").getAbsolutePath() + "\\facebase\\" + listPersons.getSelectedValue());
                    } catch (IOException ioEx) {
                        ioEx.printStackTrace();
                    }
                }
            }
        });
        buttonNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddingWindow();
            }
        });
        checkRecognizing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recognizing();
            }
        });
        panel.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "help");
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        buttonTrain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onTrain();
            }
        });
    }
    
    public void updateList(){
        DefaultListModel<String> list = new DefaultListModel<String>();
        File[] foldersList = new File("facebase").listFiles();
        for (File el : foldersList)
            if (el.isDirectory() && el.listFiles().length == 10)
                list.addElement(el.getName());
        listPersons.setModel(list);
    }
    
    private void videoControl(){        
        if (buttonStart.getText().equals("Start")){
            if (isFirstStart){
                unFace = new UnrecognizedFace();
                fd = new FaceDetection();
                unFace.setThread(fd);
                isFirstStart = false;
            } else {
                fd.setThread();
                unFace.setThread(fd);
            }
            panelStream.add(fd);
            panelUnrecognized.add(unFace);
            fd.getThread().start();
            unFace.getThread().start();
            buttonStart.setText("Stop");
            buttonNew.setEnabled(true);
            checkRecognizing.setEnabled(true);
        } else {
            if (checkRecognizing.isSelected())
                checkRecognizing.doClick();
            fd.clearThread();
            unFace.clearThread();
            panelStream.removeAll();
            panelUnrecognized.removeAll();
            buttonStart.setText("Start");
            buttonNew.setEnabled(false);
            checkRecognizing.setEnabled(false);
        }
    }
    
    private void openAddingWindow(){
        AddingPersonWindow personWindow = new AddingPersonWindow(this);
        personWindow.setResizable(false);
        personWindow.setContentPane(personWindow.contentPane);
        personWindow.setLocationByPlatform(true);
        personWindow.pack();
        personWindow.setVisible(true);
    }

    private void recognizing(){
        if (checkRecognizing.isSelected()){
            try {
                if (isFirstRecognizing){
                    new FileInputStream("mlp.nn");
                    recognizing = new FaceRecognizing(this);
                    face = new RecognizedFace();
                    face.setThread(recognizing);
                    isFirstRecognizing = false;
                } else {
                    recognizing.setThread();
                    face.setThread(recognizing);
                }
                buttonNew.setEnabled(false);
                buttonTrain.setEnabled(false);
                recognizing.getThread().start();
                panelRecognized.add(face);
                panel.updateUI();
                face.getThread().start();
            } catch (IOException ioEx){
                checkRecognizing.setSelected(false);
                JOptionPane.showMessageDialog(null, "Trained neural network wasn't founded!");
            }
        } else {
            buttonNew.setEnabled(true);
            buttonTrain.setEnabled(true);
            face.clearThread();
            recognizing.clearThread();
            panelRecognized.removeAll();
            panel.updateUI();
        }
    }

    private void onTrain(){
        TrainingNN trainingNN = new TrainingNN(this);
        Thread t = new Thread(trainingNN);
        t.start();
        isFirstRecognizing = true;
    }
    
    public UnrecognizedFace getUnFace() {
        return unFace;
    }

    public void setPanelUnrecognized(JPanel face){
        panelUnrecognized.removeAll();
        panelUnrecognized.add(face);
    }

    public void setTextRecognition(String text) {
        textRecognition.setText("");
        textRecognition.setText(text);
    }

    public JList<String> getListPersons() {
        return listPersons;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JSpinner getSpinnerNeurons() {
        return spinnerNeurons;
    }

    public JSpinner getSpinnerIterations() {
        return spinnerIterations;
    }

    private FaceDetection fd = null;
    private UnrecognizedFace unFace = null;
    private RecognizedFace face = null;
    private FaceRecognizing recognizing = null;
    private boolean isFirstStart = true;
    public boolean isFirstRecognizing = true;

    private JPanel panel;
    private JButton buttonStart;
    private JPanel panelStream;
    private JButton buttonNew;
    private JList<String> listPersons;
    private JTextArea textRecognition;
    private JPanel panelUnrecognized;
    private JPanel panelRecognized;
    private JCheckBox checkRecognizing;
    private JButton buttonTrain;
    private JProgressBar progressBar;
    private JSpinner spinnerNeurons;
    private JSpinner spinnerIterations;

    public static void main(String[] args){
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex){
            ex.printStackTrace();
        }
        Image mainIcon  = null;
        try {
            mainIcon = ImageIO.read(MainWindow.class.getResourceAsStream("/icons/icon.gif"));
        } catch (IOException ioEx){
            JOptionPane.showMessageDialog(null, "Icons not founded!");
        }
        MainWindow mw = new MainWindow();
        JFrame frame = new JFrame("dEFace");
        frame.setIconImage(mainIcon);
        frame.setResizable(false);
        frame.setContentPane(mw.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);

//        TrainingNN trainingNN = new TrainingNN();
    }
}
