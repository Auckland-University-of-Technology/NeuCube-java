/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.util;

import com.thoughtworks.xstream.XStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import jneucube.distances.Distance;
import org.apache.logging.log4j.core.util.Integers;
import java.nio.file.Files;
import java.nio.file.*;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public final class Util {

    public static Random randomNumbers = new Random();

    public static int[] getRandomPermutation(int length) {
        int[] array = new int[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        return getRandomPermutation(array);
    }

    public static int[] getRandomPermutation(int[] array) {
        for (int i = 0; i < array.length; i++) {
            int ran = i + randomNumbers.nextInt(array.length - i);
            // perform swap
            int temp = array[i];
            array[i] = array[ran];
            array[ran] = temp;
        }
        return array;
    }

    public static double getParsenWndPdf(Matrix spikeTrain, double sigma) {
        double pdf = 0.0;
        double x = Math.round(Math.sqrt(-2 * Math.pow(sigma, 2.0) * Math.log(0.0001 * Math.sqrt(2 * Math.PI) * Math.pow(sigma, 2.0))));
        Matrix temp = null;
        Matrix positiveSpikes = temp.operation('>', 0.0);
        Matrix negativeSpikes = temp.operation('<', 0.0);

        return pdf;
    }

    public static double getRandom(double min, double max) {
        return min + (max - min) * randomNumbers.nextDouble();
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value between the
     * specified min value (inclusive) and the specified max value (exclusive)
     * [Min, Max)
     *
     * @param min inclusive min value
     * @param max exclusive max value
     * @return
     */
    public static int getRandomInt(int min, int max) {
        //Random r = new Random();        
        return min + (randomNumbers.nextInt(max - min));
    }

    public static double roundUp(double num, int multipleOf) {
        return Math.floor((num + multipleOf / 2) / multipleOf) * multipleOf;
    }

    public static double roundDown(double num, int multipleOf) {
        return Math.floor((num - multipleOf / 2) / multipleOf) * multipleOf;
    }

    public static double getEuclidianDistance(double[] vec1, double[] vec2) {
        double val = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            val += Math.pow(vec1[i] - vec2[i], 2.0);
        }
        val = Math.sqrt(val);
        return val;
    }

    public static String decimalFormat(double n, int decimals) {
        String sign = "";
        if (n < 0) {
            n = n * -1.0;
            sign = "-";
        }

        DecimalFormat format = new DecimalFormat("#,##0.000");
        //DecimalFormat format = new DecimalFormat(" #,###,###,##0.000;-#,###,###,##0.000");
        format.setRoundingMode(RoundingMode.FLOOR);
        //format.setRoundingMode(RoundingMode.HALF_UP);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(decimals);
        return sign + format.format(n);
    }

    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String str, double min, double max) {
        try {
            double val = Double.parseDouble(str);
            if (val < min || val > max) {
                return false;
            }
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isInteger(String str, int min, int max) {
        try {
            int val = Integer.parseInt(str);
            if (val < min || val > max) {
                return false;
            }
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static void insertionSort(double list[][], int sortByIdx) {
        double temp[];
        for (int i = 1; i < list.length; i++) {
            temp = list[i];
            int j;
            for (j = i - 1; j >= 0 && list[j][sortByIdx] > temp[sortByIdx]; j--) {
                list[j + 1] = list[j];
            }
            list[j + 1] = temp;
        }
    }

    public static void quickSort(double A[][], int left, int right, int sortByIdx) {
        double pivote[] = A[left]; // tomamos primer elemento como pivote
        int i = left; // i realiza la búsqueda de izquierda a derecha
        int j = right; // j realiza la búsqueda de derecha a izquierda
        double aux[];

        while (i < j) {            // mientras no se crucen las búsquedas
            while (A[i][sortByIdx] <= pivote[sortByIdx] && i < j) {
                i++;        // busca elemento mayor que pivote
            }
            while (A[j][sortByIdx] > pivote[sortByIdx]) {
                j--;         // busca elemento menor que pivote
            }
            if (i < j) {                      // si no se han cruzado                      
                aux = A[i];                  // los intercambia
                A[i] = A[j];
                A[j] = aux;
            }
        }
        A[left] = A[j]; // se coloca el pivote en su lugar de forma que tendremos
        A[j] = pivote; // los menores a su izquierda y los mayores a su derecha
        if (left < j - 1) {
            quickSort(A, left, j - 1, sortByIdx); // ordenamos subarray izquierdo
        }
        if (j + 1 < right) {
            quickSort(A, j + 1, right, sortByIdx); // ordenamos subarray derecho
        }
    }

    /**
     * Sorts an array of files in ascendent order according to the name
     *
     * @param files
     */
    public static void sortFilesByNumber(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i;
                try {
                    int s = name.indexOf("sam") + 3;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch (Exception e) {
                    i = 0; // if filename does not match the format then default to 0
                }
                return i;
            }
        });
    }

    /**
     * Sorts an array of files in ascendent order according to the name
     *
     * @param files
     */
    public static void sortFiles(File[] files) {
        List<File> filenames = Arrays.asList(files);

        //adaptor for comparing files
        Collections.sort(filenames, new Comparator<File>() {
            private final Comparator<String> NATURAL_SORT = new WindowsSorter.WindowsExplorerComparator();

            @Override
            public int compare(File o1, File o2) {
                return NATURAL_SORT.compare(o1.getName(), o2.getName());
            }
        });
    }

    public static boolean saveObjectToXML(Object object, String file) {
        boolean status = false;
        XStream xstream = new XStream();
        BufferedWriter writer = null;
        long startTime;
        try {
            //_log.info("------- Writing file " + file + " -------");
//            System.out.println("------- Writing file " + file + " -------");
            startTime = System.nanoTime();
//            System.out.println("Object to xml");            
            //String xml = xstream.toXML(object);
//            System.out.println("Buffer");
            writer = new BufferedWriter(new FileWriter(file));
            xstream.toXML(object, writer);
//            System.out.println("Writting buffer");
            //writer.write(xml);
//            System.out.println("Finish");
            status = true;
            writer.close();
            //_log.info("------- Complete (time " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds) -------");
//            System.out.println("------- Complete (time " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds) -------");
        } catch (IOException ex) {
            //_log.error("******* Error while saving NeuCubeFX project *******");
//            System.out.println("******* Error while saving NeuCubeFX project *******");
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                //_log.error("******* Error while saving NeuCubeFX project *******");
                //Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
//                System.out.println("******* Error while saving NeuCubeFX project *******");
            }
        }
        return status;
    }

    public static Object loadObjectFromXML(String fileName) {
        XStream xstream = new XStream();
        File file = new File(fileName);
        return xstream.fromXML(file);
    }

    public static void saveStringToFile(StringBuffer bf, String fileName) {
        //public void export(String fileName, String separator) {
        File f = new File(fileName);
        FileWriter fw;
        try {
            fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(bf.toString());
            bw.close();
        } catch (IOException ex) {
            System.out.println("Error while writing the file: " + ex);
        }
    }

    /**
     * Computes the probability density function (pdf) at value X using the
     * normal distribution with mean mu and standard deviation sigma.
     *
     * @param x scalar input
     * @param mu mean
     * @param sigma sigma
     * @return the normal probability density function
     */
    public static double normpdf(double x, double mu, double sigma) {
        return (1 / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp((-Math.pow(x - mu, 2.0)) / (2 * Math.pow(sigma, 2.0)));
    }

    /**
     * Computes the probability density function (pdf) at value X using the
     * normal distribution with mean mu and standard deviation sigma.
     *
     * @param x vector of inputs
     * @param mu mean
     * @param sigma sigma
     * @return the normal probability density function
     */
    public static double[] normpdf(double[] x, double mu, double sigma) {
        double[] vec = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            vec[i] = normpdf(x[i], mu, sigma); // (1 / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp((-Math.pow(x[i] - mu, 2.0)) / (2 * Math.pow(sigma, 2.0)));
        }
        return vec;
    }

    /**
     * Convolution of two vectors of polynomial coefficients, convolving them is
     * equivalent to multiplying the two polynomials.
     *
     * @param u first input vector of size m
     * @param v second input vector of size n
     * @return an m+n-1 size vector that represents the convolution of vectors u
     * and v
     */
    public static double[] conv(double[] u, double[] v) {
        double[] vec = new double[u.length + v.length - 1];
        for (int i = 0; i < vec.length; i++) {
            vec[i] = 0;
        }
        for (int m = 0; m < u.length; m++) {
            for (int n = 0; n < v.length; n++) {
                vec[m + n] += (u[m] * v[n]);
            }
        }
        return vec;
    }

    /**
     * Finds the k nearest neighbor in X for each point in Y.
     *
     * @param x An mx-by-n numeric matrix. Rows of X correspond to observations
     * and columns correspond to variables.
     * @param y An my-by-n numeric matrix of query points. Rows of Y correspond
     * to observations and columns correspond to variables.
     * @param k Positive integer specifying the number of nearest neighbors in X
     * for each point in Y
     * @param distance The distance metric
     * @return An 2 array of Matrix objects. The first and the second elements
     * of the mx-by-k matrix indicates the indexes and the distances
     * respectively.
     */
    public static Matrix[] knnsearch(Matrix x, Matrix y, int k, Distance distance) {
        Matrix indexes = new Matrix(x.getRows(), k);
        Matrix distances = new Matrix(x.getRows(), k);
        Matrix[] m = {indexes, distances};
        //double [][]temp=new double [x.getRows()][y.getRows()]; // The first and second elements indicate the index and the distance respectively.        
        double[][] temp = new double[y.getRows()][2]; // The first and second elements indicate the index and the distance respectively.
        Matrix tt = new Matrix(temp);
        double d;
        for (int ix = 0; ix < x.getRows(); ix++) {
            for (int iy = 0; iy < y.getRows(); iy++) {
                d = distance.getDistance(x.getVecRow(ix), y.getVecRow(iy));
                //temp[ix][iy]=d;
                temp[iy][0] = iy;
                temp[iy][1] = d;
            }
            Util.quickSort(temp, 0, temp.length - 1, 1);  // sort by distance            
            for (int ik = 0; ik < k; ik++) {
                indexes.set(ix, ik, temp[ik][0]);
                distances.set(ix, ik, temp[ik][1]);
            }
        }
        m[0] = indexes;
        m[1] = distances;
        return m;
    }

    /**
     * Returns the correlation coefficient between two vectors.
     *
     * @param x
     * @param y
     * @return
     */
    public static double correlation(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays have different length.");
        }

        if (x.length < 3) {
            throw new IllegalArgumentException("Array length has to be at least 3.");
        }

        double Sxy = covariance(x, y);
        double Sxx = variance(x);
        double Syy = variance(y);

        if (Sxx == 0 || Syy == 0) {
            return Double.NaN;
        }

        return Sxy / java.lang.Math.sqrt(Sxx * Syy);
    }

    /**
     * Returns the covariance between two vectors.
     *
     * @param x
     * @param y
     * @return
     */
    public static double covariance(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays have different length.");
        }

        if (x.length < 3) {
            throw new IllegalArgumentException("Array length has to be at least 3.");
        }

        double mx = mean(x);
        double my = mean(y);

        double Sxy = 0.0;
        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - mx;
            double dy = y[i] - my;
            Sxy += dx * dy;
        }
        return Sxy / (x.length - 1);
    }

    /**
     * Returns the variance of an array.
     *
     * @param x
     * @return
     */
    public static double variance(double[] x) {
        if (x.length < 2) {
            throw new IllegalArgumentException("Array length is less than 2.");
        }
        double sum = 0.0;
        double sumsq = 0.0;
        for (double xi : x) {
            sum += xi;
            sumsq += xi * xi;
        }
        int n = x.length - 1;
        return sumsq / n - (sum / x.length) * (sum / n);
    }

    public static double std(double[] x) {
        return Math.sqrt(variance(x));
    }

    public static double mean(double[] x) {
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return sum / x.length;
    }
    
    public static double mean(int[] x) {
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return sum / x.length;
    }

    /**
     * Gets the index of the element with the minimum value.
     *
     * @param x the vector of values
     * @return the index
     */
    public static double[] getMin(double[] x) {
        double idx[] = new double[]{-1.0, Double.POSITIVE_INFINITY};
        for (int i = 0; i < x.length; i++) {
            if (idx[1] > x[i]) {
                idx[0] = i;
                idx[1] = x[i];
            }
        }
        return idx;
    }

    /**
     * Gets the index of the element with the minimum value.
     *
     * @param x the vector of values
     * @return the index
     */
    public static double min(double[] x) {
        double idx = Double.POSITIVE_INFINITY;
        for (int i = 0; i < x.length; i++) {
            if (idx > x[i]) {
                idx = x[i];
            }
        }
        return idx;
    }

    /**
     * Gets the index of the element with the minimum value.
     *
     * @param x the vector of values
     * @return the index
     */
    public static int min(int[] x) {
        int idx = Integer.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (idx > x[i]) {
                idx = x[i];
            }
        }
        return idx;
    }

    /**
     * Gets the index of the element with the maximum value.
     *
     * @param x the vector of values
     * @return the index
     */
    public static double[] getMax(double[] x) {
        double idx[] = new double[]{-1.0, Double.NEGATIVE_INFINITY};
        for (int i = 0; i < x.length; i++) {
            if (idx[1] < x[i]) {
                idx[0] = i;
                idx[1] = x[i];
            }
        }
        return idx;
    }

    public static double max(double[] x) {
        double idx = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < x.length; i++) {
            if (idx < x[i]) {
                idx = x[i];
            }
        }
        return idx;
    }

    public static int max(int[] x) {
        int idx = Integer.MIN_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (idx < x[i]) {
                idx = x[i];
            }
        }
        return idx;
    }

    /**
     * This function calculates the difference between two vectors x-y.
     *
     * @param x
     * @param y
     * @return a vector with the difference x-y
     */
    public static double[] getVecMinus(double[] x, double[] y) {
        if (y.length != x.length) {
            throw new IllegalArgumentException("Vectors should be the same length.");
        }
        double[] diff = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            diff[i] = x[i] - y[i];
        }
        return diff;
    }

    /**
     * This function calculates the minus between a vectors x and a scalar y.
     *
     * @param x
     * @param y
     * @return a vector with the difference x-y
     */
    public static double[] getVecMinus(double[] x, double y) {
        double[] diff = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            diff[i] = x[i] - y;
        }
        return diff;
    }

    /**
     * This function calculates the plus operation between two vectors x+y.
     *
     * @param x
     * @param y
     * @return a vector z=x+y
     */
    public static double[] getVecPlus(double[] x, double[] y) {
        if (y.length != x.length) {
            throw new IllegalArgumentException("Vectors should be the same length.");
        }
        double[] plus = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            plus[i] = x[i] + y[i];
        }
        return plus;
    }

    /**
     * This function calculates the plus operation between a vector x and a
     * scalar y.
     *
     * @param x
     * @param y
     * @return a vector z=x+y
     */
    public static double[] getVecPlus(double[] x, double y) {
        double[] plus = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            plus[i] = x[i] + y;
        }
        return plus;
    }

    /**
     * This function calculates the multiplication (not dot product) between two
     * vectors x*y.
     *
     * @param x
     * @param y
     * @return a vector z=x+y
     */
    public static double[] getVecMult(double[] x, double[] y) {
        if (y.length != x.length) {
            throw new IllegalArgumentException("Vectors should be the same length.");
        }
        double[] plus = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            plus[i] = x[i] * y[i];
        }
        return plus;
    }

    /**
     * This function calculates the multiplication operation between a vector x
     * and a scalar y.
     *
     * @param x
     * @param y
     * @return a vector z=x+y
     */
    public static double[] getVecMult(double[] x, double y) {
        double[] plus = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            plus[i] = x[i] * y;
        }
        return plus;
    }

    /**
     * This function get the difference between the elements of a vector.
     * y[n-1]= x[1]-x[0], x[2]-x[1]... x[n]-x[n-1].
     *
     * @param x
     * @return
     */
    public static double[] getVecDiff(double[] x) {
        double[] y = new double[x.length - 1];
        for (int i = 1; i < x.length; i++) {
            y[i - 1] = x[i] - x[i - 1];
        }
        return y;
    }

    /**
     * This function returns the absolute value of all elements of a vector.
     *
     * @param x
     * @return
     */
    public static double[] getVecAbs(double[] x) {
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = Math.abs(x[i]);
        }
        return y;
    }

    /**
     * This function returns a vector which elements represent boolean values
     * false(zero) true (one) given a conditional operator (<,>,<=,>=,=,!=) and
     * a value.
     *
     * @param x
     * @param operator
     * @param value
     * @param trueValue value if condition is true
     * @param falseValue value if condition is false
     * @return
     */
    public static double[] getVecBoolan(double[] x, String operator, double value, double trueValue, double falseValue) {
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            switch (operator) {
                case ">": {
                    y[i] = (x[i] > value) ? trueValue : falseValue;
                }
                break;
                case ">=": {
                    y[i] = (x[i] >= value) ? trueValue : falseValue;
                }
                break;
                case "<": {
                    y[i] = (x[i] < value) ? trueValue : falseValue;
                }
                break;
                case "<=": {
                    y[i] = (x[i] <= value) ? trueValue : falseValue;
                }
                break;
                case "=": {
                    y[i] = (x[i] == value) ? trueValue : falseValue;
                }
                break;
                case "!=": {
                    y[i] = (x[i] != value) ? trueValue : falseValue;
                }
                break;
                default: {
                    throw new IllegalArgumentException("Not a valid operator.");
                }
            }
        }
        return y;
    }

    public static double getRMSE(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays have different length.");
        }
        double rmse = 0;
        for (int i = 0; i < x.length; i++) {
            rmse += (x[i] - y[i]) * (x[i] - y[i]);
        }
        rmse = rmse / x.length;
        rmse = Math.sqrt(rmse);
        return rmse;
    }

    public static double[] StringToDouble(String[] string) {
        double[] values = new double[string.length];
        for (int i = 0; i < string.length; i++) {
            values[i] = Double.parseDouble(string[i]);
        }
        return values;
    }

    public static int[] StringToInteger(String[] string) {
        int[] values = new int[string.length];
        for (int i = 0; i < string.length; i++) {
            values[i] = Integer.parseInt(string[i]);
        }
        return values;
    }

    public static void printHorzArray(double[] vector) {
        for (int i = 0; i < vector.length; i++) {
            System.out.print(vector[i] + " ");
        }
        System.out.println("");
    }
    
    public static String vec2str(double [] vector, String separator){
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]).append(separator);
        }
        return sb.toString();
    }
    
    public static void printHorzArray(int[] vector) {
        for (int i = 0; i < vector.length; i++) {
            System.out.print(vector[i] + " ");
        }
        System.out.println("");
    }

    public static String getHorzArray(double[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]).append(" ");
        }
        return sb.toString();
    }

    public static void printVertArray(double[] vector) {
        for (int i = 0; i < vector.length; i++) {
            System.out.println(vector[i]);
        }
        System.out.println("");
    }
    
    public static void printVertArray(int[] vector) {
        for (int i = 0; i < vector.length; i++) {
            System.out.println(vector[i]);
        }
        System.out.println("");
    }

    public static String getVerArray(double[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]).append(System.lineSeparator());
        }
        return sb.toString();
    }

    public static double[] catArray(double[]... arrays) {
        int size = 0;
        for (double[] array : arrays) {
            size += array.length;
        }
        double[] newArray = new double[size];
        int k = 0;
        for (double[] array : arrays) {
            for (int j = 0; j < array.length; j++) {
                newArray[k] = array[j];
                k++;
            }
        }
        return newArray;
    }

    public static boolean isInRange(double number, double lowVal, double highVal) {
        if (lowVal > highVal) {
            throw new IllegalArgumentException("The low value is greater than the high value.");
        }
        return (lowVal <= number && number <= highVal);
    }

    /**
     * Returns the numerical range of a vector, i.e. the distance between the
     * maximum and minimum value.
     *
     * @param vec the vector
     * @return the range between the max and min values
     */
    public static double range(double[] vec) {
        double minVal = Double.POSITIVE_INFINITY;
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] < minVal) {
                minVal = vec[i];
            }
            if (vec[i] > maxVal) {
                maxVal = vec[i];
            }
        }
        return maxVal - minVal;
    }

    /**
     * The {@code numberExpression} is a simple number expression parser. This
     * supports cron-like expressions, like <code>1,3-6,100-200,666,</code>.
     *
     * @param pattern
     * @return
     */
    public static ArrayList<Integer> numberExpression(String pattern) {
        ArrayList<Integer> list = new ArrayList<>();
        String[] parts = pattern.split(",");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("-")) {
                String[] ranges = parts[i].split("-");
                for (int x = Integer.parseInt(ranges[0]); x <= Integer.parseInt(ranges[1]); x++) {
                    list.add(x);
                }
            } else {
                list.add(Integer.parseInt(parts[i]));
            }
        }
        return list;
    }

    public static double sum(double[] vector) {
        double sum = 0.0;
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i];
        }
        return sum;
    }

    public static void renameFile(String oldName, String newName) throws IOException {
        // File (or directory) with old name
        File file = new File(oldName);

        // File (or directory) with new name
        File file2 = new File(newName);

        if (file2.exists()) {
            throw new java.io.IOException("file exists");
        }

        // Rename file (or directory)
        boolean success = file.renameTo(file2);

        if (!success) {
            // File was not successfully renamed
        }
    }

    public static void moveFile(String source, String target) throws IOException {
        Path temp = Files.move(Paths.get(source), Paths.get(target));

        if (temp != null) {
            System.out.println("File renamed and moved successfully");
        } else {
            System.out.println("Failed to move the file");
        }
    }
}
