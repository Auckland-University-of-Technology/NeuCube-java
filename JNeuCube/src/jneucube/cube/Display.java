/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.cube;

/**
 *
 * @author em9403
 */
public class Display {
    private double reservoirNeuronSize = 1.0;
    private double inputNeuronSize = 3.0;
    private double outputNeuronSize = 3.0;
    private double firedNeuronSize = 2.0;
    private double selectedNeuronSize = 2.0;
    private double connectionWidth = 0.05;//0.005;
    private double connectionItensity = 1.0;

    private boolean viewRealWeightConnection = false;

    private double neuronsInitPosX = 0;    // Initial position in the x axix. Just for visualization of the network
    private double neuronsInitPosY = 0;    // Initial position in the y axix. Just for visualization of the network
    private double neuronsInitPosZ = 0;    // Initial position in the z axix. Just for visualization of the network
    private double neuronDistance = 20;    // Distance between two neurons. Just for visualization of the network
    
    private double minWeight=-0.005;
    private double maxWeight=0.005;
    

    public void initializePositions(int numNeuronsX, int numNeuronsY, int numNeuronsZ) {
        neuronsInitPosX = (numNeuronsX % 2 == 1) ? -(numNeuronsX / 2) * neuronDistance : -((numNeuronsX / 2) * neuronDistance) + (neuronDistance / 2);
        neuronsInitPosY = (numNeuronsY % 2 == 1) ? -(numNeuronsY / 2) * neuronDistance : -((numNeuronsY / 2) * neuronDistance) + (neuronDistance / 2);
        neuronsInitPosZ = (numNeuronsZ % 2 == 1) ? -(numNeuronsZ / 2) * neuronDistance : -((numNeuronsZ / 2) * neuronDistance) + (neuronDistance / 2);
    }

    public void setNeuronsInitPos(double x, double y, double z) {
        this.neuronsInitPosX = x;
        this.neuronsInitPosY = y;
        this.neuronsInitPosZ = z;
    }

    /**
     * @return the neuronsInitPosX
     */
    public double getNeuronsInitPosX() {
        return neuronsInitPosX;
    }

    /**
     * @param neuronsInitPosX the neuronsInitPosX to set
     */
    public void setNeuronsInitPosX(double neuronsInitPosX) {
        this.neuronsInitPosX = neuronsInitPosX;
    }

    /**
     * @return the neuronsInitPosY
     */
    public double getNeuronsInitPosY() {
        return neuronsInitPosY;
    }

    /**
     * @param neuronsInitPosY the neuronsInitPosY to set
     */
    public void setNeuronsInitPosY(double neuronsInitPosY) {
        this.neuronsInitPosY = neuronsInitPosY;
    }

    /**
     * @return the neuronsInitPosZ
     */
    public double getNeuronsInitPosZ() {
        return neuronsInitPosZ;
    }

    /**
     * @param neuronsInitPosZ the neuronsInitPosZ to set
     */
    public void setNeuronsInitPosZ(double neuronsInitPosZ) {
        this.neuronsInitPosZ = neuronsInitPosZ;
    }

    /**
     * @return the neuronDistance
     */
    public double getNeuronDistance() {
        return neuronDistance;
    }

    /**
     * @param neuronDistance the neuronDistance to set
     */
    public void setNeuronDistance(double neuronDistance) {
        this.neuronDistance = neuronDistance;
    }

    /**
     * @return the reservoirNeuronSize
     */
    public double getReservoirNeuronSize() {
        return reservoirNeuronSize;
    }

    /**
     * @param reservoirNeuronSize the reservoirNeuronSize to set
     */
    public void setReservoirNeuronSize(double reservoirNeuronSize) {
        this.reservoirNeuronSize = reservoirNeuronSize;
    }

    /**
     * @return the inputNeuronSize
     */
    public double getInputNeuronSize() {
        return inputNeuronSize;
    }

    /**
     * @param inputNeuronSize the inputNeuronSize to set
     */
    public void setInputNeuronSize(double inputNeuronSize) {
        this.inputNeuronSize = inputNeuronSize;
    }

    /**
     * @return the viewRealWeightConnection
     */
    public boolean isViewRealWeightConnection() {
        return viewRealWeightConnection;
    }

    /**
     * @param viewRealWeightConnection
     */
    public void setViewRealWeightConnection(boolean viewRealWeightConnection) {
        this.viewRealWeightConnection = viewRealWeightConnection;
    }

    /**
     * @return the outputNeuronSize
     */
    public double getOutputNeuronSize() {
        return outputNeuronSize;
    }

    /**
     * @param outputNeuronSize the outputNeuronSize to set
     */
    public void setOutputNeuronSize(double outputNeuronSize) {
        this.outputNeuronSize = outputNeuronSize;
    }

    /**
     * @return the firedNeuronSize
     */
    public double getFiredNeuronSize() {
        return firedNeuronSize;
    }

    /**
     * @param firedNeuronSize the firedNeuronSize to set
     */
    public void setFiredNeuronSize(double firedNeuronSize) {
        this.firedNeuronSize = firedNeuronSize;
    }

    /**
     * @return the selectedNeuronSize
     */
    public double getSelectedNeuronSize() {
        return selectedNeuronSize;
    }

    /**
     * @param selectedNeuronSize the selectedNeuronSize to set
     */
    public void setSelectedNeuronSize(double selectedNeuronSize) {
        this.selectedNeuronSize = selectedNeuronSize;
    }

    /**
     * @return the connectionWidth
     */
    public double getConnectionWidth() {
        return connectionWidth;
    }

    /**
     * @param connectionWidth the connectionWidth to set
     */
    public void setConnectionWidth(double connectionWidth) {
        this.connectionWidth = connectionWidth;
    }

    /**
     * @return the connectionItensity
     */
    public double getConnectionItensity() {
        return connectionItensity;
    }

    /**
     * @param connectionItensity the connectionItensity to set
     */
    public void setConnectionItensity(double connectionItensity) {
        this.connectionItensity = connectionItensity;
    }

    /**
     * @return the minWeight
     */
    public double getMinWeight() {
        return minWeight;
    }

    /**
     * @param minWeight the minWeight to set
     */
    public void setMinWeight(double minWeight) {
        this.minWeight = minWeight;
    }

    /**
     * @return the maxWeight
     */
    public double getMaxWeight() {
        return maxWeight;
    }

    /**
     * @param maxWeight the maxWeight to set
     */
    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

}
