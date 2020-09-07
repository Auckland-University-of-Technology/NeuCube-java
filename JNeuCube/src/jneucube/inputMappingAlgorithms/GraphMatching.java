/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.inputMappingAlgorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import jneucube.distances.Distance;
import jneucube.util.Matrix;
import jneucube.util.Util;
import static jneucube.log.Log.LOGGER;

/**
 * The {@code GraphMatching} class implements the algorithm for generating the
 * random input neurons. For this process, the algorithm needs the coordinates
 * of the neurons in the reservoir (cuboid) and all the data set encoded into
 * spike trains. The spike train are the result of merging all data sets into
 * one m-by-n matrix, where m is the sum of the number of time points of all
 * samples and n the number of features (all the samples must contains the same
 * number of features). The idea behind this algorithm is that the order of the
 * input coordinates and the order of the input features must match according to
 * their distances among the coordinates and the features respectively. The
 * objective is to create a graph with the shortest trajectory among all the
 * nodes without repetition, for both the input coordinates and the spike
 * trains. Then, the first element of the trajectory from the input coordinates
 * must be the first element of the trajectory of the features of the spike
 * train. The steps of this algorithms are the following. First, select randomly
 * the input neurons the surface of the cuboid. Second, convolve the spike
 * trains with a Gaussian function kernel for generating a normal probability
 * density function of the spike trains. Every, input coordinate is part of a
 * graph and every spike train feature is an element of another graph. Third,
 * calculate the shortest route among the nodes of each graph (the Euclidian
 * distance is considered to calculate the shortest distance between two
 * vertices of the input coordinates graph, and the Correlation distance for the
 * shortest distance between two vertices of the spike train features). Fourth,
 * match the input coordinates to the features.
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class GraphMatching extends InputMapping {

    private Matrix spikeTrain;
    private Matrix neuronsCoordinates;

    /**
     * This function creates the input coordinates
     *
     * @return
     */
    @Override
    public Matrix createCoordinates() {
        LOGGER.info("- Creating input coordinates using " + this.toString());
        long processTime=System.nanoTime();
        if (this.neuronsCoordinates == null) {
            throw new NullPointerException("Reservoir coordinates can not be founds.");
        }
        if (this.spikeTrain == null) {
            throw new NullPointerException("The spike train data can not be found.");
        }

        try {
            Matrix randInputs = this.getRandomInputs(this.spikeTrain.getCols(), neuronsCoordinates);
            Matrix inputs = new Matrix(randInputs.getRows(), randInputs.getCols());
            double[] points = {-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
            double[] gaussianKernel = Util.normpdf(points, 0, 1.5);

            Matrix spikePDF = this.getPDF(this.spikeTrain, gaussianKernel);
            //pdf.show();
            Matrix realIndexes = new Matrix(randInputs.getRows(), 1);
            for (int i = 0; i < randInputs.getRows(); i++) {
                realIndexes.set(i, 0, i);
            }

            ArrayList<double[]> bestRouteInputs = this.getBestRoute(randInputs, Distance.EUCLIDIAN_DISTANCE);
            ArrayList<double[]> bestRouteSpikes = this.getBestRoute(spikePDF.transpose(), Distance.CORRELATION_DISTANCE);

            int index = -1;
            int indexInput;
            for (int i = 0; i < bestRouteSpikes.size(); i++) {
                for (int j = 0; j < bestRouteSpikes.size(); j++) {
                    if ((int) bestRouteSpikes.get(j)[0] == i) {
                        index = j;
                        break;
                    }
                }
                indexInput = (int) bestRouteInputs.get(index)[0];
                inputs.setRow(i, randInputs.getVecRow(indexInput));
            }
            LOGGER.info("- Complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
            return inputs;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(GraphMatching.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Gets random locations from the surfaces of the network.
     *
     * @param numInputs The number of input neurons.
     * @param neuronsLocation The coordinates of the neurons in the network.
     * @return a numInputs-by-3 Matrix containing the Z, Y and Z coordinates of
     * the inputs.
     */
    public Matrix getRandomInputs(int numInputs, Matrix neuronsLocation) {
        Matrix inputLocations = new Matrix(numInputs, neuronsLocation.getCols());
        Matrix min = neuronsLocation.min(2);
        Matrix max = neuronsLocation.max(2);
        int minX = (int) min.get(0, 0);
        int minY = (int) min.get(0, 1);
        int minZ = (int) min.get(0, 2);

        int maxX = (int) max.get(0, 0);
        int maxY = (int) max.get(0, 1);
        int maxZ = (int) max.get(0, 2);

        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < neuronsLocation.getRows(); i++) {
            if (neuronsLocation.get(i, 0) == minX || neuronsLocation.get(i, 0) == maxX || neuronsLocation.get(i, 1) == minY || neuronsLocation.get(i, 1) == maxY || neuronsLocation.get(i, 2) == minZ || neuronsLocation.get(i, 2) == maxZ) {
                indexes.add(i);
            }
        }
        Collections.shuffle(indexes);
        for (int i = 0; i < numInputs; i++) {
            inputLocations.setRow(i, neuronsLocation.getVecRow(indexes.get(i)));
        }
        return inputLocations;
    }

    /**
     * Calculates the best route of a graph which nodes are located in the n
     * dimensional space.
     *
     * @param data an m-by-n matrix where m represents the nodes and n the
     * dimension of the node
     * @param distance the type of distance
     * @return an array list that contains the index of the node (row of the
     * data) and the distance to the n+1 node
     */
    public ArrayList<double[]> getBestRoute(Matrix data, Distance distance) {
        ArrayList<double[]> bestRoute = new ArrayList<>();
        double d = Double.POSITIVE_INFINITY;
        Matrix realIndexes = new Matrix(data.getRows(), 1);
        for (int i = 0; i < data.getRows(); i++) {
            realIndexes.set(i, 0, i);
        }
        for (int i = 0; i < data.getRows(); i++) {
            ArrayList<double[]> route = new ArrayList<>();
            double tempD = this.calculateRoute(i, data, realIndexes, distance, route);
            if (tempD < d) {
                d = tempD;
                bestRoute = route;
            }
        }
//        LOGGER.debug("Best route distance " + d);
//        for (double[] node : bestRoute) {
//            LOGGER.debug("node " + node[0] + " distance " + node[1]);
//        }
        return bestRoute;
    }

    /**
     * This function recursively creates a route where every node n+1 is the
     * closest one to the node n.
     *
     * @param index the index of the element where a distance
     * @param matrix the m-by-n matrix that contains the remaining nodes for
     * calculating the next n+1 node
     * @param realIndexes a m-by-1 matrix that contains the original row indexes
     * of the original matrix
     * @param distance the distance to uses
     * @param route an empty array list that will be filled during the recursion
     * @return
     */
    //public double calculateRoute(double index, Matrix matrix, Matrix realIndexes, Distance distance, double[][] route, int routeNodeId) {
    public double calculateRoute(double index, Matrix matrix, Matrix realIndexes, Distance distance, ArrayList<double[]> route) {
        double realIndex = realIndexes.get((int) index, 0);
        double d = 0.0;
        if (matrix.getRows() > 1) {
            Matrix rowNode = matrix.getRow((int) index);
            //System.out.println("Node "+ realIndex);
            //System.out.println("Real indexes before");
            //realIndexes.show();
            realIndexes = realIndexes.removeRow((int) index);
            //System.out.println("Real indexes after");
            //realIndexes.show();
            //System.out.println("Matrix before");
            //matrix.show();
            Matrix newMatrix = matrix.removeRow((int) index);   // Removes the row of the current element for calculating the distances to the remaining nodes
            //System.out.println("Matrix after");
            //newMatrix.show();
            Matrix[] m = Util.knnsearch(rowNode, newMatrix, 1, distance); // returns an 1-by-1 matrix with the index of the nearest neighbor and an 1-by-1 matrix with the distance
            //System.out.println("Index "+m[0].get(0, 0)+ " distance "+m[1].get(0, 0));            
            route.add(new double[]{realIndex, m[1].get(0, 0)}); // Adds a new node and the distance to the next node

            d = m[1].get(0, 0); // gets the distance to the next node
            d += this.calculateRoute(m[0].get(0, 0), newMatrix, realIndexes, distance, route);
        } else {
            route.add(new double[]{realIndex, 0.0});
            //System.out.println("Node "+ realIndex);   
            d = 0.0;
        }
        return d;
    }

    /**
     * Gets the probability density function of the spike trains. The spike
     * train is split into positive and negative spike trains, then every spike
     * train is convolved with a Gaussian kernel. The result is the sum of the
     * positive and negative convolved spike trains.
     *
     * @param spikeTrain
     * @param kernel
     * @return
     * @throws IOException
     */
    public Matrix getPDF(Matrix spikeTrain, double[] kernel) throws IOException {
        Matrix positiveSpikes = spikeTrain.operation('>', 0.0);
        Matrix negativeSpikes = spikeTrain.operation('<', 0.0);
        //LOGGER.debug("Positive spikes "+positiveSpikes.sum());
        //LOGGER.debug("Negative spikes "+negativeSpikes.sum());
        Matrix pdfPos = new Matrix(positiveSpikes.getRows(), positiveSpikes.getCols());
        Matrix pdfNeg = new Matrix(negativeSpikes.getRows(), negativeSpikes.getCols());

        for (int col = 0; col < positiveSpikes.getCols(); col++) { // for each column
            pdfPos.setCol(col, this.convolve(positiveSpikes.getVecCol(col), kernel));
        }
        //pdfPos.show();
        for (int col = 0; col < negativeSpikes.getCols(); col++) { // for each column
            pdfNeg.setCol(col, this.convolve(negativeSpikes.getVecCol(col), kernel));
        }
        //pdfNeg.show();
        Matrix pdf = pdfPos.operation('-', pdfNeg);
        return pdf;
    }

    public double[] convolve(double[] signal, double[] kernel) {
        double[] conv = new double[signal.length];
        int midPoint = (kernel.length / 2);
        for (int i = midPoint; i < signal.length - midPoint; i++) {
            if (signal[i] == 1) {
                for (int k = 0; k < kernel.length; k++) {
                    conv[(i - 5) + k] = conv[(i - 5) + k] + kernel[k];
                }
            }
        }
        return conv;
    }

    /**
     *
     *
     * @param inputs
     * @param pdf
     * @param k Positive integer specifying the number of nearest neighbors
     */
    public void getInputMapping(Matrix inputs, Matrix pdf, int k) {
        // Choosing the clossest inputs
        Matrix[] m = Util.knnsearch(inputs, inputs, k, Distance.EUCLIDIAN_DISTANCE);
        System.out.println("");
        m[0].show();
        System.out.println("");
        m[1].show();
        Matrix indexes = m[0].get(0, m[0].getRows(), 1, m[0].getCols());    // takes all the elements from the second column to the end
        Matrix distances = m[1].get(0, m[1].getRows(), 1, m[1].getCols());  // takes all the elements from the second column to the end

        Matrix Wn = this.NN2W(indexes, distances);
        //Wn.show();        
        Wn = Wn.operation('+', Wn.transpose()).operation('/', 2.0);
        Wn.show();

        // Choosing the clossest signals according to the spike probabilities with Gaussian distribution
        m = Util.knnsearch(pdf.transpose(), pdf.transpose(), k, Distance.CORRELATION_DISTANCE); // Get k nearest neighbors probabilites woith gaussian distribution
        indexes = m[0].get(0, m[0].getRows(), 1, m[0].getCols());    // takes all the elements from the second column to the end
        distances = m[1].get(0, m[1].getRows(), 1, m[1].getCols());  // takes all the elements from the second column to the end
        //indexes.show();
        //distances.show();        
        Matrix Wf = this.NN2W(indexes, distances);
        //Wf.show();        
        Wf = Wf.operation('+', Wf.transpose()).operation('/', 2.0);
        Wf.show();
        System.out.println("");

        int n1 = Wn.getRows();
        int n2 = Wf.getRows();
        ///////////// Calculate node affinity matrix
        Matrix Ln = Wn.operation('>', 0.0);
        Matrix Lf = Wf.operation('>', 0.0);
        Matrix VnIndexes = new Matrix(Ln.triu(1).findIdx("!=", 0.0));
        Matrix VfIndexes = new Matrix(Lf.triu(1).findIdx("!=", 0.0));
        int m1 = VnIndexes.getRows();
        int m2 = VfIndexes.getRows();

        Matrix Kp = this.getNodeAffinityMatrix(Ln, Lf);
        /// Calculate edge affinity matrix
        Matrix Kq = this.getEdgeAffinityMatrix(Wn, VnIndexes, Wf, VfIndexes);

///////////////////    
/////////////////////////
        Matrix Ct = new Matrix(n1, n2, 0.0);

        Matrix G1 = new Matrix(n1, m1, 0.0);
        for (int i = 0; i < VnIndexes.getRows(); i++) {
            G1.set((int) VnIndexes.get(i, 0), i, 1);
            G1.set((int) VnIndexes.get(i, 1), i, 1);
        }
        System.out.println("");
        G1.show();

        Matrix G2 = new Matrix(n2, m2, 0.0);
        for (int i = 0; i < VfIndexes.getRows(); i++) {
            G2.set((int) VfIndexes.get(i, 0), i, 1);
            G2.set((int) VfIndexes.get(i, 1), i, 1);
        }
        System.out.println("");
        G2.show();

        Matrix H1 = Matrix.horzcat(G1, new Matrix().eye(n1));
        Matrix H2 = Matrix.horzcat(G2, new Matrix().eye(n2));

        System.out.println("");
        H1.show();
        System.out.println("");
        H2.show();
        System.out.println("");

    }

    /**
     *
     * @param indexes the indexes of the k nearest neighbors
     * @param distances the distances of the k nearest neighbors
     * @return
     */
    public Matrix NN2W(Matrix indexes, Matrix distances) {
        Matrix w = new Matrix(indexes.getRows(), indexes.getRows(), 0.0);
        double minDis = distances.min();
        distances = distances.operation('-', minDis);
        double maxDis = distances.max();
        distances = distances.operation('/', maxDis);
        Matrix ones = new Matrix(distances.getRows(), distances.getCols(), 1.0);
        distances = ones.operation('-', distances);
        distances.show();
        for (int i = 0; i < indexes.getRows(); i++) {
            for (int j = 0; j < indexes.getCols(); j++) {
                w.set(i, (int) indexes.get(i, j), distances.get(i, j));
            }
        }
        return w;
    }

    /**
     * This functions calculates the node affinity matrix
     *
     * @param Ln m-by-m binary (1,0) matrix
     * @param Lf m-by-m binary (1,0) matrix
     * @return The affinity m-by-n matrix
     */
    public Matrix getNodeAffinityMatrix(Matrix Ln, Matrix Lf) {
        int n1 = Ln.getRows();
        int n2 = Lf.getRows();
        Matrix Kp = new Matrix(n1, n2);   // Node affinity matrix
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                Kp.set(i, j, Math.abs(Ln.getCol(i).sum() - Lf.getCol(j).sum()));
            }
        }
        Matrix kpMax = new Matrix(Kp.getRows(), Kp.getCols(), Kp.max());
        Kp = kpMax.operation('-', Kp);
        System.out.println("");
        Kp.show();
        System.out.println("");
        return Kp;
    }

    /**
     * Calculates the edge affinity matrix.
     *
     * @param Wn
     * @param VnIndexes
     * @param Wf
     * @param VfIndexes
     * @return
     */
    public Matrix getEdgeAffinityMatrix(Matrix Wn, Matrix VnIndexes, Matrix Wf, Matrix VfIndexes) {
        int m1 = VnIndexes.getRows();
        int m2 = VfIndexes.getRows();

        Matrix Kq = new Matrix(m1, m2, 0.0);
        Wn = Wn.norm();
        System.out.println("");
        Wn.show();
        Wf = Wf.norm();
        System.out.println("");
        Wf.show();
        double val;
        for (int i = 0; i < m1; i++) {
            for (int j = 0; j < m2; j++) {
                val = Math.abs(Wn.get((int) VnIndexes.get(i, 0), (int) VnIndexes.get(i, 1)) - Wf.get((int) VfIndexes.get(j, 0), (int) VfIndexes.get(j, 1)));
                Kq.set(i, j, val);
            }
        }

        Kq = new Matrix(Kq.getRows(), Kq.getCols(), Kq.max()).operation('-', Kq);
        System.out.println("");
        Kq.show();
        return Kq;
    }

    @Override
    public String toString() {
        return "Graph matching";
    }

    /**
     * @return the spikeTrain
     */
    public Matrix getSpikeTrain() {
        return spikeTrain;
    }

    /**
     * @param spikeTrain the spikeTrain to set
     */
    public void setSpikeTrain(Matrix spikeTrain) {
        this.spikeTrain = spikeTrain;
    }

    /**
     * @return the neuronsCoordinates
     */
    public Matrix getNeuronsCoordinates() {
        return neuronsCoordinates;
    }

    /**
     * @param neuronsCoordinates the neuronsCoordinates to set
     */
    public void setNeuronsCoordinates(Matrix neuronsCoordinates) {
        this.neuronsCoordinates = neuronsCoordinates;
    }

    public static void main(String args[]) {
        GraphMatching graphMatching = new GraphMatching();
        try {
            graphMatching.setNeuronsCoordinates(new Matrix("C:\\DataSets\\mapping\\cube_8_8_8.csv", ","));
            graphMatching.setSpikeTrain(new Matrix("C:\\DataSets\\mapping\\spikeTrains.csv", ","));
            //Matrix neuronsLocation = new Matrix("C:\\DataSets\\mapping\\cube_8_8_8.csv", ",");
            //Matrix spikeTrain = new Matrix("C:\\DataSets\\mapping\\spikeTrains.csv", ",");
            Matrix inputs = graphMatching.createCoordinates();
            inputs.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
