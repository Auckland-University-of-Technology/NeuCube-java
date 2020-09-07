/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.cube;

import com.thoughtworks.xstream.XStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import jneucube.classifiers.Classifier;
import jneucube.classifiers.KNN;
import jneucube.connectionAlgorithms.ConnectionAlgorithm;
import jneucube.connectionAlgorithms.SmallWorld;
import jneucube.data.SpatioTemporalData;
import jneucube.tasks.Tasks;
import jneucube.encodingAlgorithms.AERalgorithm;
import jneucube.encodingAlgorithms.EncodingAlgorithm;
import jneucube.network.NetworkController;
import jneucube.crossValidation.CrossValidation;
import jneucube.crossValidation.Kfold;
import jneucube.network.Network;
import jneucube.network.reservoirBuilders.ReservoirBuilder;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.trainingAlgorithms.supervised.deSNNs;
import jneucube.trainingAlgorithms.unsupervised.STDP;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;
import jneucube.util.Util;

/**
 *
 * @author kedguest
 */
public class NeuCube {

    public static final int MODULE_3D_DISPLAY = 0;
    public static final int MODULE_DATA = 1;
    public static final int MODULE_INITIALIZATION = 2;
    public static final int MODULE_UNSUPERVISED_LEARNING = 3;
    public static final int MODULE_SUPERVISED_LEARNING = 4;
    public static final int STEP_NEW = 0;
    public static final int STEP_INITIALIZED = 1;
    public static final int STEP_READY_UNSUPERVISED = 2;
    public static final int STEP_TRAINED_UNSUPERVISED = 3;
    public static final int STEP_READY_SUPERVISED = 4;
    public static final int STEP_TRAINED_SUPERVISED = 5;

    private int step = STEP_NEW;
    private int problemType = Tasks.CLASSIFICATION;

    private String name = "Project";
    private Network network = new Network();
    private SpatioTemporalData SSTD = new SpatioTemporalData();
    private ConnectionAlgorithm connectionAlgorithm = new SmallWorld();
    private EncodingAlgorithm encodingAlgorithm = new AERalgorithm();
    private LearningAlgorithm unsupervisedLearningAlgorithm = new STDP();
    private LearningAlgorithm supervisedLearningAlgorithm = new deSNNs();
    private CrossValidation crossValidation = new Kfold();
    private Classifier classifier = new KNN();
    private ReservoirBuilder reservoirBuilder;
    private Display display = new Display();

    public static NeuCube loadNeuCubeFile(String fileName) {
        XStream xstream = new XStream();
        File file = new File(fileName);
        NeuCube neucube = (NeuCube) xstream.fromXML(file);
        return neucube;
    }

    /**
     *
     * @param strReservoirCoordinates
     */
    public void createNetwork(String strReservoirCoordinates) {
        try {
            this.getDisplay().setNeuronsInitPos(0, 0, 0);
            Matrix reservoirCoordinates = new Matrix(strReservoirCoordinates, ",");
            Matrix inputCoordinates = this.getRandomInputs();
            this.createNetwork(reservoirCoordinates, inputCoordinates);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * This function creates the networkController using the coordinates loaded
     * from a file.
     *
     * @param strReservoirCoordinates
     * @param strInputCoordinates
     */
    public void createNetwork(String strReservoirCoordinates, String strInputCoordinates) {
        try {
            this.getDisplay().setNeuronsInitPos(0, 0, 0);
            Matrix reservoirCoordinates = new Matrix(strReservoirCoordinates, ",");
            Matrix inputCoordinates = new Matrix(strInputCoordinates, ",");
            this.createNetwork(reservoirCoordinates, inputCoordinates);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Creates a NeuCube reservoir given X,Y, and Z number of neurons with
     * random inputs
     *
     * @param numNeuronsX
     * @param numNeuronsY
     * @param numNeuronsZ
     */
    public void createNetwork(int numNeuronsX, int numNeuronsY, int numNeuronsZ) {
        Matrix reservoirCoordinates = this.createCoordinates(numNeuronsX, numNeuronsY, numNeuronsZ);
        Matrix inputCoordinates = this.getRandomInputs();
        this.createNetwork(reservoirCoordinates, inputCoordinates);
    }

    /**
     * Creates a NeuCube reservoir given X,Y, and Z number of neurons. The input
     * neurons are read from a file that contains their X,Y,Z coordinates
     *
     * @param numNeuronsX
     * @param numNeuronsY
     * @param numNeuronsZ
     * @param strInputCoordinates
     */
    public void createNetwork(int numNeuronsX, int numNeuronsY, int numNeuronsZ, String strInputCoordinates) {
        try {
            Matrix reservoirCoordinates = this.createCoordinates(numNeuronsX, numNeuronsY, numNeuronsZ);
            Matrix inputCoordinates = new Matrix(strInputCoordinates, ",");
            this.createNetwork(reservoirCoordinates, inputCoordinates);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Creates the NeuCube reservoir given a matrix of reservoir coordinates and
     * a matrix of input neuron coordinates
     *
     * @param reservoirCoordinates
     * @param inputCoordinates
     */
    public void createNetwork(Matrix reservoirCoordinates, Matrix inputCoordinates) {
        NetworkController controller = new NetworkController(this.network);
        controller.createNetwork(reservoirCoordinates, inputCoordinates);
    }

    /**
     * The function creates a matrix with the X,Y,Z position of the neurons
     * given X,Y,Z number of neurons
     *
     * @param numNeuronsX
     * @param numNeuronsY
     * @param numNeuronsZ
     * @return
     */
    public Matrix createCoordinates(int numNeuronsX, int numNeuronsY, int numNeuronsZ) {
        int numNeurons = numNeuronsX * numNeuronsY * numNeuronsZ;
        this.network.setNumNeuronsX(numNeuronsX);
        this.network.setNumNeuronsY(numNeuronsY);
        this.network.setNumNeuronsZ(numNeuronsZ);

        double initX = (numNeuronsX % 2 == 1) ? -(numNeuronsX / 2) : -((numNeuronsX / 2.0) - (0.5));
        double initY = (numNeuronsY % 2 == 1) ? -(numNeuronsY / 2) : -((numNeuronsY / 2.0) - (0.5));
        double initZ = (numNeuronsZ % 2 == 1) ? -(numNeuronsZ / 2) : -((numNeuronsZ / 2.0) - (0.5));

        this.getDisplay().setNeuronsInitPosX(initX);
        this.getDisplay().setNeuronsInitPosY(initY);
        this.getDisplay().setNeuronsInitPosZ(initZ);
        int pos;
        Matrix coordinateMatrix = new Matrix(numNeurons, 3, 0.0);
        for (int z = 0; z < numNeuronsZ; z++) {
            for (int y = 0; y < numNeuronsY; y++) {
                for (int x = 0; x < numNeuronsX; x++) {
                    pos = (z * numNeuronsY * numNeuronsX) + (y * numNeuronsX) + x;
                    coordinateMatrix.setRow(pos, new double[]{x, y, z});
                }
            }
        }
        return coordinateMatrix;
    }

    private Matrix getRandomInputs() {
        Matrix inputs = new Matrix();
        LOGGER.error("Mehtod under development");
        return inputs;
    }

    /**
     * Pseudo on-line learning
     *
     */
    public void runUnsupervisedLearningAlgorithmPseudoOnLine() {
        SSTD.getDataSamples().stream().forEach((dataSample) -> {
            this.SSTD.getTrainingData().add(dataSample);
        });
        this.unsupervisedLearningAlgorithm.setTrainingRounds(1);
        this.unsupervisedLearningAlgorithm.train(this.network, SSTD.getTrainingData());

    }

    public void runSupervisedLearningAlgorithm() {
        this.supervisedLearningAlgorithm.train(this.network, SSTD.getTrainingData());
        this.supervisedLearningAlgorithm.validate(this.network, SSTD);
    }

    /**
     * Saves the NeuCube modules into an XML file.
     *
     * @param file
     * @return
     */
    public boolean save(String file) {
        LOGGER.info("Saving NeuCube file " + file);
        boolean status = false;
        XStream xstream = new XStream();
        long startTime = System.nanoTime();
        OutputStream outputStream = null;
        Writer writer = null;
        try {
            outputStream = new FileOutputStream(file);
            writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            xstream.toXML(this, writer);
        } catch (FileNotFoundException exp) {
            LOGGER.error(exp);
            return false;
        } finally {
            close(writer);
            close(outputStream);
        }
        LOGGER.info("Complete (time " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds) ");
        return status;
    }

    public boolean save2(String file) {
        LOGGER.info("Saving NeuCube file " + file);
        boolean status = false;        
        long startTime = System.nanoTime();
        Util.saveObjectToXML(this, file);        
        LOGGER.info("Complete (time " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds) ");
        return status;
    }

    public static void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            //log the exception
        }
    }

    public byte[] getXmlBytes() {
        XStream xstream = new XStream();
        String xml = xstream.toXML(this);
        return xml.getBytes();
    }

    /**
     * @return the SSTD
     */
    public SpatioTemporalData getSSTD() {
        return SSTD;
    }

    /**
     * @param SSTD the SSTD to set
     */
    public void setSSTD(SpatioTemporalData SSTD) {
        this.SSTD = SSTD;
    }

    /**
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * @param network the network to set
     */
    public void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * @return the connectionAlgorithm
     */
    public ConnectionAlgorithm getConnectionAlgorithm() {
        return connectionAlgorithm;
    }

    /**
     * @param connectionAlgorithm the connectionAlgorithm to set
     */
    public void setConnectionAlgorithm(ConnectionAlgorithm connectionAlgorithm) {
        this.connectionAlgorithm = connectionAlgorithm;
    }

    /**
     * @return the encodingAlgorithm
     */
    public EncodingAlgorithm getEncodingAlgorithm() {
        return encodingAlgorithm;
    }

    /**
     * @param encodingAlgorithm the encodingAlgorithm to set
     */
    public void setEncodingAlgorithm(EncodingAlgorithm encodingAlgorithm) {
        this.encodingAlgorithm = encodingAlgorithm;
    }

    /**
     * @return the unsupervisedLearningAlgorithm
     */
    public LearningAlgorithm getUnsupervisedLearningAlgorithm() {
        return unsupervisedLearningAlgorithm;
    }

    /**
     * @param unsupervisedLearningAlgorithm the unsupervisedLearningAlgorithm to
     * set
     */
    public void setUnsupervisedLearningAlgorithm(LearningAlgorithm unsupervisedLearningAlgorithm) {
        this.unsupervisedLearningAlgorithm = unsupervisedLearningAlgorithm;
    }

    /**
     * @return the supervisedLearningAlgorithm
     */
    public LearningAlgorithm getSupervisedLearningAlgorithm() {
        return supervisedLearningAlgorithm;
    }

    /**
     * @param supervisedLearningAlgorithm the supervisedLearningAlgorithm to set
     */
    public void setSupervisedLearningAlgorithm(LearningAlgorithm supervisedLearningAlgorithm) {
        this.supervisedLearningAlgorithm = supervisedLearningAlgorithm;
    }

    /**
     * @return the step
     */
    public int getStep() {
        return step;
    }

    /**
     * @param step the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the problemType
     */
    public int getProblemType() {
        return problemType;
    }

    /**
     * @param problemType the problemType to set
     */
    public void setProblemType(int problemType) {
        this.problemType = problemType;
    }

    /**
     * @return the display
     */
    public Display getDisplay() {
        return display;
    }

    /**
     * @param display the display to set
     */
    public void setDisplay(Display display) {
        this.display = display;
    }

    /**
     * @return the crossValidation
     */
    public CrossValidation getCrossValidation() {
        return crossValidation;
    }

    /**
     * @param crossValidation the crossValidation to set
     */
    public void setCrossValidation(CrossValidation crossValidation) {
        this.crossValidation = crossValidation;
    }

    /**
     * @return the classifier
     */
    public Classifier getClassifier() {
        return classifier;
    }

    /**
     * @param classifier the classifier to set
     */
    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    /**
     * @return the reservoirBuilder
     */
    public ReservoirBuilder getReservoirBuilder() {
        return reservoirBuilder;
    }

    /**
     * @param reservoirBuilder the reservoirBuilder to set
     */
    public void setReservoirBuilder(ReservoirBuilder reservoirBuilder) {
        this.reservoirBuilder = reservoirBuilder;
    }

}
