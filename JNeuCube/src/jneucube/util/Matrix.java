/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class Matrix {

    private int rows = 0;
    private int cols = 0;
    private double data[][];
    //Logger LOGGER = LoggerFactory.getLogger(Matrix.class);

    public Matrix() {

    }

    /**
     *
     * @param fileName The file name
     * @param separator The string containing separator characters
     * @throws java.io.FileNotFoundException
     */
    public Matrix(File fileName, String separator) throws FileNotFoundException, IOException {
        //Matrix matrix=new Matrix();
        String line;
        String strValues[];
        ArrayList<double[]> tempRows = new ArrayList<>();
        double[] row;
//        int numLine = 1;
        try (
                BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
//                LOGGER.debug("Line " + numLine);
                strValues = line.split(separator);
                row = new double[strValues.length];
                for (int j = 0; j < strValues.length; j++) {
//                    System.out.println(j + 1);
                    row[j] = Double.parseDouble(strValues[j]);
                }
                tempRows.add(row);
                this.cols = strValues.length;
//                numLine++;
            }
            this.rows = tempRows.size();
            this.data = tempRows.toArray(new double[tempRows.size()][]);
        }
        // catch (Exception e) {
        //    System.out.println(e);
        //}
    }

    /**
     *
     * @param fileName The file name
     * @param separator The string containing separator characters
     * @throws java.io.FileNotFoundException
     */
    public Matrix(String fileName, String separator) throws FileNotFoundException, IOException {
        //Matrix matrix=new Matrix();
        String line;
        String strValues[];
        ArrayList<double[]> tempRows = new ArrayList<>();
        double[] row;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                strValues = line.split(separator);
                row = new double[strValues.length];
                for (int j = 0; j < strValues.length; j++) {
                    row[j] = Double.parseDouble(strValues[j]);
                }
                tempRows.add(row);
                this.cols = strValues.length;
            }
            this.rows = tempRows.size();
            this.data = tempRows.toArray(new double[tempRows.size()][]);
            br.close();
        }
    }

    /**
     *
     * @param fileName The file name
     * @param separator The string containing separator characters
     * @param numRow A specific record
     */
    public Matrix(String fileName, String separator, long numRow) {
        //Matrix matrix=new Matrix();
        String line;
        String strValues[];
        int i = 0; // the first recrod
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                if (i == numRow) {
                    strValues = line.split(separator);
                    this.rows = 1;
                    this.cols = strValues.length;
                    this.data = new double[1][this.cols];
                    for (int j = 0; j < strValues.length; j++) {
                        this.data[0][j] = Double.parseDouble(strValues[j]);
                    }
                    break;
                }
                i++;
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Matrix(String fileName, String separator, int rows, int cols) {
        try {
            this.rows = rows;
            this.cols = cols;
            this.data = new double[rows][cols];

            File inFile = new File(fileName);
            Scanner in = new Scanner(inFile);

            int lineCount = 0;
            while (in.hasNextLine()) {
                String[] currentLine = in.nextLine().trim().split(",");
                for (int i = 0; i < this.cols; i++) {
                    this.data[lineCount][i] = Double.parseDouble(currentLine[i]);
                }
                lineCount++;
            }
            in.close();
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.toString());
        }
    }

    public void clear() {
        data = null;
        rows = 0;
        cols = 0;
    }

    public Matrix(int rows, int cols, double initial) {
        initialize(rows, cols, initial);
    }

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        data = new double[rows][cols];
    }

    public Matrix(int dim) {
        this.rows = dim;
        this.cols = dim;
        data = new double[rows][cols];
    }

    public Matrix(String expression) {
        String xyExpression[] = expression.split(",");
        String xExpression[] = xyExpression[0].split(":");
        String yExpression[] = xyExpression[1].split(":");
        double yIncrement = 0;
        double xIncrement = 0;
        double xValue = 0;
        double yValue = 0;
        boolean hFill = false;
        switch (xExpression.length) {
            case 1: {
                this.rows = (int) Double.parseDouble(xExpression[0]);
            }
            break;
            case 3: {
                xValue = Double.parseDouble(xExpression[0]);
                xIncrement = Double.parseDouble(xExpression[1]);
                this.rows = (int) ((Double.parseDouble(xExpression[2]) - xValue) / xIncrement) + 1;
            }
            break;
        }
        switch (yExpression.length) {
            case 1: {
                this.cols = (int) Double.parseDouble(yExpression[0]);
            }
            break;
            case 3: {
                hFill = true;
                yValue = Double.parseDouble(yExpression[0]);
                yIncrement = Double.parseDouble(yExpression[1]);
                this.cols = (int) ((Double.parseDouble(yExpression[2]) - yValue) / yIncrement) + 1;
            }
            break;
        }
        initialize(rows, cols, 0.0);

        for (int m = 0; m < rows; m++) {
            yValue = Double.parseDouble(yExpression[0]);
            for (int n = 0; n < cols; n++) {
                if (hFill == true) {
                    this.data[m][n] = yValue;
                    yValue += yIncrement;
                } else {
                    this.data[m][n] = xValue;
                }
            }
            xValue += xIncrement;
        }
    }

    public Matrix(double[][] data) {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = data;
    }

    public void initialize(int rows, int cols, double initial) {
        this.rows = rows;
        this.cols = cols;
        data = new double[rows][cols];
        for (int m = 0; m < rows; m++) {
            for (int n = 0; n < cols; n++) {
                data[m][n] = initial;
            }
        }
    }

    /**
     *
     * @param B Matrix B
     * @return The dot product of A and B
     */
    public Matrix dotProduct(Matrix B) {
        Matrix temp = null;
        if (this.getCols() == B.getRows()) {
            temp = new Matrix(this.getRows(), B.getCols(), 0.0);
            for (int i = 0; i < temp.rows; i++) {
                for (int j = 0; j < temp.cols; j++) {
                    for (int k = 0; k < this.cols; k++) {
                        temp.set(i, j, temp.get(i, j) + this.get(i, k) * B.get(k, j));
                    }
                }
            }
        } else if (B.rows == 1 && B.cols == 1) {
            temp = this.scalarProduct(B.get(0, 0));
        } else {
            System.out.println("Inner matrix dimensions must agree.");
        }
        return temp;
    }

    /**
     *
     * @param scalar
     * @return
     */
    public Matrix scalarProduct(double scalar) {
        Matrix temp = new Matrix(this.getRows(), this.getCols(), 0.0);
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                temp.set(i, j, this.get(i, j) * scalar);
            }
        }
        return temp;
    }

    public Matrix transpose() {
        Matrix temp = new Matrix(this.getCols(), this.getRows(), 0.0);
        for (int i = 0; i < temp.getRows(); i++) {
            for (int j = 0; j < temp.getCols(); j++) {
                temp.set(i, j, this.get(j, i));
            }
        }
        return temp;
    }

    public Matrix flipud() {
        Matrix temp = new Matrix(this.getCols(), this.getRows(), 0.0);
        for (int i = 0; i < temp.getRows(); i++) {
            for (int j = 0; j < temp.getCols(); j++) {
                temp.set((temp.getRows() - 1) - i, j, this.get(i, j));
            }
        }
        return temp;
    }

    /**
     * Gets the diagonal of the matrix
     *
     * @return and m-by-m matrix
     */
    public Matrix diagonal() {
        Matrix temp;
        if (this.getCols() == 1) {
            temp = new Matrix(this.getRows(), this.getRows(), 0.0);
            for (int i = 0; i < this.getRows(); i++) {
                temp.set(i, i, this.get(i, 0));
            }
        } else {
            temp = new Matrix(this.getRows(), 1, 0.0);
            for (int i = 0; i < this.getRows(); i++) {
                temp.set(i, 0, this.get(i, i));
            }
        }
        return temp;
    }

    /**
     * Sums all values in the matrix
     *
     * @return A double number
     */
    public double sum() {
        double val;
        Matrix temp = this.sum(1).sum(2);   // sums rows and get a matrix with one row, then sums the columns of the resultant row.
        val = temp.get(0, 0);
        return val;
    }

    /**
     * Sums the values of a given dimension. 1 rows, otherwise columns
     *
     * @param dimension
     * @return
     */
    public Matrix sum(int dimension) {
        if (dimension == 1) {
            return sumRows();
        } else {
            return sumCols();
        }
    }

    /**
     * Sums the values of all rows in a matrix
     *
     * @return A matrix with one row
     */
    private Matrix sumRows() {
        Matrix temp = new Matrix(1, this.getCols(), 0.0);
        for (int j = 0; j < this.getCols(); j++) {
            for (int i = 0; i < this.getRows(); i++) {
                temp.set(0, j, temp.get(0, j) + this.get(i, j));
            }
        }
        return temp;
    }

    /**
     * Sums the values of all columns in a matrix
     *
     * @return A matrix with one column
     */
    private Matrix sumCols() {
        Matrix temp = new Matrix(this.getRows(), 1, 0.0);
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                temp.set(i, 0, temp.get(i, 0) + this.get(i, j));
            }
        }
        return temp;
    }

    /**
     * Number of elements in a matrix
     *
     * @return
     */
    public int numel() {
        return this.rows * this.cols;
    }

    public void fill(int rowId, Matrix B) {
        for (int i = 0; i < B.getRows(); i++) {
            for (int j = 0; j < B.getCols(); j++) {
                this.set(rowId + i, j, B.get(i, j));
            }
        }
    }

    public boolean equals(Matrix B) {
        if (this.getRows() == B.getRows() && this.getCols() == B.getCols()) {
            for (int r = 0; r < this.getRows(); r++) {
                for (int c = 0; c < this.getCols(); c++) {
                    if (this.get(r, c) != B.get(r, c)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Multiplies all the elements in the matrix by a random number
     */
    public void multRandom() {
        Random rand = new Random();
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.set(i, j, this.get(i, j) * rand.nextDouble());
            }
        }
    }

    public Matrix operation(char operator, Matrix B) {
        Matrix A = new Matrix(this.getRows(), this.getCols(), 0.0);
        this.operation(A, operator, B);
        return A;
    }

    public void operation(Matrix A, char operator, Matrix B) {
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                switch (operator) {
                    case '+':
                        A.set(i, j, this.get(i, j) + B.get(i, j));
                        break;
                    case '-':
                        A.set(i, j, this.get(i, j) - B.get(i, j));
                        break;
                    case '*':
                        A.set(i, j, this.get(i, j) * B.get(i, j));
                        break;
                    case '>':
                        A.set(i, j, (this.get(i, j) > B.get(i, j)) ? 1.0 : 0.0);
                        break;
                    case '<':
                        A.set(i, j, (this.get(i, j) < B.get(i, j)) ? 1.0 : 0.0);
                        break;
                    case '!':
                        A.set(i, j, (this.get(i, j) != B.get(i, j)) ? 1.0 : 0.0);
                        break;
                    case '=':
                        A.set(i, j, (this.get(i, j) == B.get(i, j)) ? 1.0 : 0.0);
                        break;
                    case '|':
                        A.set(i, j, (this.get(i, j) != 0.0 || B.get(i, j) != 0.0) ? 1.0 : 0.0);
                        break;
                    case '/':
                        A.set(i, j, this.get(i, j) / B.get(i, j));
                        break;
                    case '\\':
                        A.set(i, j, (this.get(i, j) == 0.0) ? 0.0 : B.get(i, j) / this.get(i, j));
                        break;
                }
            }
        }
    }

    /**
     * Operations with scalar value
     *
     * @param operator
     * @param val
     * @return
     */
    public Matrix operation(char operator, double val) {
        Matrix A = new Matrix(this.getRows(), this.getCols(), 0.0);
        this.operation(A, operator, val);
        return A;
    }

    public void operation(Matrix A, char operator, double val) {
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                switch (operator) {
                    case '+':
                        A.set(i, j, this.get(i, j) + val);
                        break;
                    case '-':
                        A.set(i, j, this.get(i, j) - val);
                        break;
                    case '*':
                        A.set(i, j, this.get(i, j) * val);
                        break;
                    case '>':
                        A.set(i, j, (this.get(i, j) > val) ? 1.0 : 0.0);
                        break;
                    case '<':
                        A.set(i, j, (this.get(i, j) < val) ? 1.0 : 0.0);
                        break;
                    case '!':
                        A.set(i, j, (this.get(i, j) != val) ? 1.0 : 0.0);
                        break;
                    case '=':
                        A.set(i, j, (this.get(i, j) == val) ? 1.0 : 0.0);
                        break;
                    case '|':
                        A.set(i, j, (this.get(i, j) != 0.0 || val != 0.0) ? 1.0 : 0.0);
                        break;
                    case '/':
                        A.set(i, j, this.get(i, j) / val);
                        break;
                    case '\\':
                        A.set(i, j, (this.get(i, j) == 0.0) ? 0.0 : val / this.get(i, j));
                        break;
                }
            }
        }
    }

    public Matrix diff() {
        Matrix temp = new Matrix(this.rows - 1, this.cols, 0.0);
        for (int i = 1; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                temp.set(i - 1, j, this.data[i][j] - this.data[i - 1][j]);
            }
        }
        return temp;
    }

    public Matrix abs() {
        Matrix temp = new Matrix(this.rows, this.cols, 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                temp.set(i, j, Math.abs(this.data[i][j]));
            }
        }
        return temp;
    }

    /**
     * Merges an array list of matrices into one matrix.
     *
     * @param samples the matrices to be merged.
     * @return a matrix
     */
    public static Matrix merge(Matrix[] samples) {
        int sampleLength = samples[0].getRows();
        int numSamples;
        numSamples = samples.length;
        int numCols = samples[0].getCols();
        Matrix temp = new Matrix(sampleLength * numSamples, numCols, 0.0);
        for (int i = 0; i < numSamples; i++) {
            temp.fill(sampleLength * i, samples[i]);
        }
        return temp;
    }

    /**
     * Replicates n times one dimension of a matrix, rows or columns.
     *
     * @param times The times to replicate a dimension
     * @param dimension The dimension to be replicated. 1 rows, 2 columns
     * @return
     */
    public Matrix repmat(int times, int dimension) {
        Matrix temp = null;
        if (dimension == 1) {   // rows
            temp = new Matrix(this.rows * times, this.cols, 0.0);
            for (int t = 0; t < times; t++) {
                for (int i = 0; i < this.rows; i++) {
                    for (int j = 0; j < this.cols; j++) {
                        temp.set(this.rows * t + i, j, this.get(i, j));
                    }
                }
            }
        } else if (dimension == 2) {    // columns
            temp = new Matrix(this.rows, this.cols * times, 0.0);
            for (int t = 0; t < times; t++) {
                for (int i = 0; i < this.rows; i++) {
                    for (int j = 0; j < this.cols; j++) {
                        temp.set(i, this.cols * t + j, this.get(i, j));
                    }
                }
            }
        }

        return temp;
    }

    /**
     * Calculates the mean values of each column
     *
     * @return a 1 by m matrix with the mean of each m column
     */
    public Matrix mean() {
        Matrix temp = new Matrix(1, this.getCols(), 0.0);
        double val;
        for (int j = 0; j < this.getCols(); j++) {
            val = 0.0;
            for (int i = 0; i < this.getRows(); i++) {
                val += this.get(i, j);
            }
            val = val / this.getRows();
            temp.set(0, j, val);
        }
        return temp;
    }

    /**
     *
     * @return the mean value of all elements in the matrix
     */
    public double meanValue() {
        double val = 0.0;
        for (int j = 0; j < this.getCols(); j++) {
            for (int i = 0; i < this.getRows(); i++) {
                val += this.get(i, j);
            }
        }
        val = val / this.getRows() * this.getCols();
        return val;
    }

    /**
     *
     * @param dimension Dimension 1=rows, 2=cols
     * @param vector The row or column position
     * @return The minimum value of a row or column
     */
    public double min(int dimension, int vector) {
        double minVal = Double.POSITIVE_INFINITY;
        int iSize;
        if (dimension == 1) {
            iSize = this.getCols();
        } else {
            iSize = this.getRows();
        }
        for (int i = 0; i < iSize; i++) {
            if (dimension == 1) {
                if (minVal > this.get(vector, i)) {
                    minVal = this.get(vector, i);
                }
            } else if (minVal > this.get(i, vector)) {
                minVal = this.get(i, vector);
            }
        }
        return minVal;
    }

    /**
     *
     * @param dimension Dimension 1=rows, 2=cols
     * @return The minimum value of a row or column
     */
    public Matrix min(int dimension) {
        Matrix temp;
        if (dimension == 1) {   // Rows
            temp = new Matrix(this.getRows(), 1, 0.0);
            for (int i = 0; i < this.getRows(); i++) {
                temp.set(i, 0, min(dimension, i));
            }
        } else {    // Cols
            temp = new Matrix(1, this.getCols(), 0.0);
            for (int i = 0; i < this.getCols(); i++) {
                temp.set(0, i, min(dimension, i));
            }
        }
        return temp;
    }

    /**
     * Returns the smaller value of the matrix.
     *
     * @return the smaller value of the matrix
     */
    public double min() {
        Matrix temp = this.min(1);
        temp = temp.min(2);
        return temp.get(0, 0);
    }

    /**
     *
     * @param dimension Dimension 1=rows, 2=cols
     * @param vector The row or column position
     * @return The maximum value of a row or column
     */
    public double max(int dimension, int vector) {
        double maxVal = Double.NEGATIVE_INFINITY;
        int iSize;
        if (dimension == 1) {
            iSize = this.getCols();
        } else {
            iSize = this.getRows();
        }
        for (int i = 0; i < iSize; i++) {
            if (dimension == 1) {
                if (maxVal < this.get(vector, i)) {
                    maxVal = this.get(vector, i);
                }
            } else if (maxVal < this.get(i, vector)) {
                maxVal = this.get(i, vector);
            }
        }
        return maxVal;
    }

    /**
     *
     * @param dimension Dimension 1=rows, 2=cols
     * @return The maximum value of a row or column
     */
    public Matrix max(int dimension) {
        Matrix temp;
        if (dimension == 1) {   // Rows
            temp = new Matrix(this.getRows(), 1, 0.0);
            for (int i = 0; i < this.getRows(); i++) {
                temp.set(i, 0, max(dimension, i));
            }
        } else {    // Cols
            temp = new Matrix(1, this.getCols(), 0.0);
            for (int i = 0; i < this.getCols(); i++) {
                temp.set(0, i, max(dimension, i));
            }
        }
        return temp;
    }

    /**
     * Returns the greatest value of the matrix.
     *
     * @return the smaller value of the matrix
     */
    public double max() {
        Matrix temp = this.max(1);
        temp = temp.max(2);
        return temp.get(0, 0);
    }

    /**
     * Calculates the variance of each column. the value is normalized by the
     * number of observations -1. When the flag-1, it is normalized by the
     * number of observations.
     *
     * @return a 1 by m matrix with the variance of each m column normalized by
     * the number of observations-1
     */
    public Matrix variance() {
        return this.variance(0);
    }

    /**
     * Calculates the variance of each column. If the flag=0 the value is
     * normalized by the number of observations -1. When the flag-1, it is
     * normalized by the number of observations.
     *
     * @param flag
     * @return a 1 by m matrix with the variance of each m column
     */
    public Matrix variance(int flag) {
        Matrix temp = new Matrix(1, this.getCols(), 0.0);
        Matrix mean = this.mean();
        double val;
        for (int j = 0; j < this.getCols(); j++) {
            val = 0.0;
            for (int i = 0; i < this.getRows(); i++) {
                val += (mean.get(0, j) - this.get(i, j)) * (mean.get(0, j) - this.get(i, j));
            }
            if (flag == 0) {
                val = val / (this.getRows() - 1);
            } else {
                val = val / this.getRows();
            }
            temp.set(0, j, val);
        }
        return temp;
    }

    /**
     * Calculates the variance of all elements of the matrix.
     *
     * @return the variance normalized by the number of observations-1.
     */
    public double varianceValue() {
        return this.varianceValue(0);
    }

    /**
     * Calculates the variance of all elements of the matrix. If the flag=0 the
     * value is normalized by the number of observations -1. When the flag-1, it
     * is normalized by the number of observations.
     *
     * @param flag specifies the weight scheme.
     * @return the variance
     */
    public double varianceValue(int flag) {
        double mean = this.meanValue();
        int elements = (flag == 0) ? this.numel() - 1 : this.numel();
        double val = 0.0;
        for (int j = 0; j < this.getCols(); j++) {
            for (int i = 0; i < this.getRows(); i++) {
                val += ((this.get(i, j) - mean) * (this.get(i, j) - mean)) / elements;
            }
        }
        return val;
    }

    /**
     * Calculates the standard deviation of all elements of the matrix.
     *
     * @return the standard deviation normalized by the number of
     * observations-1.
     */
    public double stdValue() {
        return stdValue(0);
    }

    /**
     * Calculates the standard deviation of all elements of the matrix. If the
     * flag=0 the value is normalized by the number of observations -1. When the
     * flag-1, it is normalized by the number of observations.
     *
     * @param flag
     * @return
     */
    public double stdValue(int flag) {
        return Math.sqrt(this.varianceValue(flag));
    }

    public Matrix std() {
        Matrix temp = this.variance();
        for (int j = 0; j < this.getCols(); j++) {
            temp.set(0, j, Math.sqrt(temp.get(0, j)));
        }
        return temp;
    }

    /**
     *
     * @param flag 0 or 1
     * @return for flag = 0, returns the standard deviation using the square
     * root of an unbiased estimator of the variance of the population from
     * which X is drawn, as long as X consists of independent, identically
     * distributed samples. For flag = 1, std(X,1) returns the standard
     * deviation using (2) above, producing the second moment of the set of
     * values about their mean.
     */
    public Matrix std(int flag) {
        Matrix temp = this.variance(flag);
        for (int j = 0; j < this.getCols(); j++) {
            temp.set(0, j, Math.sqrt(temp.get(0, j)));
        }
        return temp;
    }

    /**
     * Horizontally concatenates matrices A1,...,AN. All matrices in the
     * argument list must have the same number of rows.
     *
     * @param A The list of matrices.
     * @return A m-by-n where n is the sum of the number of columns per matrix
     */
    public static Matrix horzcat(Matrix... A) {
        int numCols = 0;
        int numRows = A[0].getRows();
        for (Matrix A1 : A) {
            if (A1.getRows() != numRows) {
                throw new IllegalArgumentException("Matrices have different rows.");
            }
            numCols += A1.getCols();
        }
        Matrix m = new Matrix(numRows, numCols);
        int colCount = 0;
        for (Matrix A1 : A) {
            for (int c = 0; c < A1.getCols(); c++) {
                for (int r = 0; r < A1.getRows(); r++) {
                    m.set(r, colCount, A1.get(r, c));
                }
                colCount++;
            }
        }
        return m;
    }

    /**
     * This function vertically inserts a matrix B. The number of columns in
     * matrix B must coincide tho the current matrix.
     *
     * @param B the m-by-n matrix
     * @return and m-by-n where m is equal to n columns of this matrix plus m
     * columns of matrix B.
     */
    public Matrix vertInsert(Matrix B) {
//        Matrix temp = new Matrix(this.getRows() + B.getRows(), this.getCols(), 0.0);
//        for (int i = 0; i < temp.getRows(); i++) {
//            for (int j = 0; j < temp.getCols(); j++) {
//                if (i < this.getRows()) {
//                    temp.set(i, j, this.get(i, j));
//                } else {
//                    temp.set(i, j, B.get(i - this.getRows(), j));
//                }
//            }
//        }
//        return temp;
        return this.vertcat(this, B);

    }

    /**
     * Vertically concatenates matrices A1,...,AN. All matrices in the argument
     * list must have the same number of columns.
     *
     * @param A The list of matrices.
     * @return A m-by-n where m is the sum of the number of rows per matrix.
     */
    public static Matrix vertcat(Matrix... A) {
        int numCols = A[0].getCols();
        int numRows = 0;
        for (Matrix A1 : A) {
            if (A1.getCols() != numCols) {
                throw new IllegalArgumentException("Matrices have different columns.");
            }
            numRows += A1.getRows();
        }
        Matrix m = new Matrix(numRows, numCols);
        int rowCount = 0;
        for (Matrix A1 : A) {
            for (int r = 0; r < A1.getRows(); r++) {
                for (int c = 0; c < A1.getCols(); c++) {
                    m.set(rowCount, c, A1.get(r, c));
                }
                rowCount++;
            }
        }
        return m;
    }

    /**
     * Removes a row from the matrix.
     *
     * @param rowIndex the index of the row to be removed
     * @return a m-by-n matrix
     */
    public Matrix removeRow(int rowIndex) {
        Matrix newMatrix = null;
        int row = 0;
        if (this.getRows() > 1) {
            newMatrix = new Matrix(this.getRows() - 1, this.cols);
            for (int i = 0; i < this.getRows(); i++) {
                if (i != rowIndex) {
                    for (int j = 0; j < this.getCols(); j++) {
                        newMatrix.set(row, j, this.get(i, j));
                    }
                    row++;
                }
            }
        } else {
            throw new IllegalArgumentException("The matrix should have more than one row.");
        }
        return newMatrix;
    }

    /**
     * Removes a column from the matrix.
     *
     * @param colIndex the index of the column to be removed
     * @return a m-by-n matrix
     */
    public Matrix removeCol(int colIndex) {
        Matrix newMatrix = null;
        int col = 0;
        if (this.getRows() > 1) {
            newMatrix = new Matrix(this.getRows(), this.getCols() - 1);
            for (int j = 0; j < this.getCols(); j++) {
                if (j != colIndex) {
                    for (int i = 0; i < this.getRows(); i++) {
                        newMatrix.set(i, col, this.get(i, j));
                    }
                    col++;
                }
            }
        } else {
            throw new IllegalArgumentException("The matrix should have more than one row.");
        }
        return newMatrix;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * @return the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * @param cols the cols to set
     */
    public void setCols(int cols) {
        this.cols = cols;
    }

    /**
     * @param row
     * @param col
     * @return the element
     */
    public double get(int row, int col) {
        return this.data[row][col];
    }

    /**
     *
     * @param row the number of row
     * @param col the number of column
     * @param data the value
     */
    public void set(int row, int col, double data) {
        this.data[row][col] = data;
    }

    public void setIncrease(int row, int col, double value) {
        this.data[row][col] += value;
    }

    /**
     * @return the data
     */
    public double[][] getData() {
        return data;
    }

    public double[][] getData(int beginIndexRow, int endIndexRow, int beginIndexCol, int endIndexCol) {
        int numRows = endIndexRow - beginIndexRow;
        int numCols = endIndexCol - beginIndexCol;
        double[][] tempData = new double[numRows][numCols];
        for (int r = beginIndexRow; r < endIndexRow; r++) {
            for (int c = beginIndexCol; c < endIndexCol; c++) {
                System.arraycopy(this.data[r], 0, tempData[r - beginIndexRow], 0, numCols);
                //tempData[r-beginIndexRow][c-beginIndexCol] = this.data[r][c];
            }
        }
        return tempData;
    }

    public Matrix subMatrix(int beginIndexRow, int endIndexRow, int beginIndexCol, int endIndexCol) {
        return new Matrix(this.getData(beginIndexRow, endIndexRow, beginIndexCol, endIndexCol));
    }

    public Double[][] getDataDouble() {
        Double[][] tempData = new Double[this.rows][this.cols];
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                tempData[r][c] = data[r][c];
            }
        }
        return tempData;
    }

    public String[][] getStringData() {
        String[][] tempData = new String[this.rows][this.cols];
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                tempData[r][c] = String.valueOf(data[r][c]);
            }
        }
        return tempData;
    }

    /**
     * @param data the data to set
     */
    public void setData(double[][] data) {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = data;
    }

    public static Matrix getEuclidianDistance(Matrix A, Matrix B) {
        Matrix res = new Matrix(A.getRows(), B.getRows(), 0.0);
        double d;
        for (int i = 0; i < A.getRows(); i++) {
            for (int j = 0; j < B.getRows(); j++) {
                d = 0.0;
                for (int k = 0; k < A.getCols(); k++) {
                    d += Math.pow(A.get(i, k) - B.get(j, k), 2.0);
                }
                res.set(i, j, Math.sqrt(d));
            }
        }
        return res;
    }

    public void show() {
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                System.out.print(this.get(i, j) + " ");
            }
            System.out.println("");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                sb.append(this.get(i, j)).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void printMatlab() {
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                System.out.print(this.get(i, j) + " ");
            }
            System.out.println(";");
        }

    }

    /**
     * Returns a matrix that is a submatrix of this matrix. The submatrix begins
     * at the specified beginIndex row and extends to the row at index endIndex
     * - 1. Thus the length of the submatrix is endIndex-beginIndex.
     *
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public Matrix getRows(int beginIndex, int endIndex) {
        Matrix temp = new Matrix((endIndex - beginIndex), this.getCols(), 0.0);
        for (int i = 0; i < (endIndex - beginIndex); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                temp.set(i, j, this.get(i + beginIndex, j));
            }
        }
        return temp;
    }

    public Matrix getReverseRows(int beginIndex, int endIndex) {
        Matrix temp = new Matrix((beginIndex - endIndex), this.getCols(), 0.0);
        for (int i = 0; i < (beginIndex - endIndex); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                temp.set(i, j, this.get(beginIndex - i, j));
            }
        }
        return temp;
    }

    public Matrix getRow(int id) {
        Matrix temp = new Matrix(1, this.data[id].length, 0.0);        
        for (int j = 0; j < this.data[id].length; j++) {
            temp.set(0, j, this.get(id, j));
        }
        return temp;
    }

    public double[] getVecRow(int numRow) {
        //double[] vec = new double[this.getCols()];        
        double[] vec = new double[this.data[numRow].length];        
//        for (int i = 0; i < this.getCols(); i++) {
//            vec[i] = this.get(numRow, i);
//        }
        
        System.arraycopy(this.data[numRow], 0, vec, 0, this.data[numRow].length);
        
        return vec;
    }

    public void setRow(int numRow, double[] data) {
        if (data.length == this.cols) {
            for (int i = 0; i < this.getCols(); i++) {
                this.set(numRow, i, data[i]);
            }
        } else {
            throw new IllegalArgumentException("The array length has to be the same as the number of columns.");
        }
    }

    /**
     * Replaces the values of a specified row with a specific value. The size of
     * the vector must be the same as the number of rows of the matrix.
     *
     * @param numRow the number of column to be
     * @param value
     */
    public void setRow(int numRow, double value) {
        if (numRow < 0 || numRow >= this.rows) {
            throw new IllegalArgumentException("The column specified is not valid.");
        }
        for (int i = 0; i < this.getCols(); i++) {
            this.set(numRow, i, value);
        }
    }

    /**
     * Returns a matrix that is a submatrix of this matrix. The submatrix begins
     * at the specified beginIndex column and extends to the column at index
     * endIndex - 1. Thus the length of the submatrix is endIndex-beginIndex.
     *
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public Matrix getCols(int beginIndex, int endIndex) {
        Matrix temp = new Matrix(this.getRows(), (endIndex - beginIndex), 0.0);
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < (endIndex - beginIndex); j++) {
                temp.set(i, j, this.get(i, j + beginIndex));
            }
        }
        return temp;
    }

    public double[] getVecCol(int numCol) {
        double[] vec = new double[this.getRows()];
        for (int i = 0; i < this.getRows(); i++) {
            vec[i] = this.get(i, numCol);
        }
        return vec;
    }

    public Matrix getCol(int id) {
        Matrix temp = new Matrix(this.getRows(), 1, 0.0);
        for (int i = 0; i < this.getRows(); i++) {
            temp.set(i, 0, this.get(i, id));
        }
        return temp;
    }

    /**
     * Replaces the values of a specified column with a new vector. The size of
     * the vector must be the same as the number of rows of the matrix.
     *
     * @param numCol the number of column to be
     * @param vec
     */
    public void setCol(int numCol, double[] vec) {
        if (vec.length == this.rows) {
            for (int i = 0; i < this.getRows(); i++) {
                this.set(i, numCol, vec[i]);
            }
        } else {
            throw new IllegalArgumentException("The array length has to be the same as the number of rows.");
        }
    }

    /**
     * Replaces the values of a specified column with a specific value. The size
     * of the vector must be the same as the number of rows of the matrix.
     *
     * @param numCol the number of column to be
     * @param value
     */
    public void setCol(int numCol, double value) {
        if (numCol < 0 || numCol >= this.cols) {
            throw new IllegalArgumentException("The column specified is not valid.");
        }
        for (int i = 0; i < this.getRows(); i++) {
            this.set(i, numCol, value);
        }
    }

    public Matrix get(int beginIndexRow, int endIndexRow, int beginIndexCol, int endIndexCol) {
        Matrix temp = this.getRows(beginIndexRow, endIndexRow);
        temp = temp.getCols(beginIndexCol, endIndexCol);
        return temp;
    }

    /**
     * Return the x,y coordinates of the element that matches with the value.
     *
     * @param val
     * @return
     */
    public Matrix find(double val) {
        Matrix temp = new Matrix(0, 2, 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (this.get(i, j) == val) {
                    temp = temp.insertRow();
                    temp.set(temp.getRows() - 1, 0, i);
                    temp.set(temp.getRows() - 1, 1, j);
                }
            }
        }
        return temp;
    }

    /**
     * Get the indexes of the elements given a condition value.
     *
     * @param conditionOperator logical operator
     * @param value the condition value
     * @return the r,c coordinates of the elements that met the condition
     */
    public double[][] findIdx(String conditionOperator, double value) {
        ArrayList<double[]> list = new ArrayList<>();
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                switch (conditionOperator) {
                    case "<": {
                        if (this.get(r, c) < value) {
                            list.add(new double[]{r, c});
                        }
                    }
                    break;
                    case ">": {
                        if (this.get(r, c) > value) {
                            list.add(new double[]{r, c});
                        }
                    }
                    break;
                    case ">=": {
                        if (this.get(r, c) >= value) {
                            list.add(new double[]{r, c});
                        }
                    }
                    break;
                    case "<=": {
                        if (this.get(r, c) <= value) {
                            list.add(new double[]{r, c});
                        }
                    }
                    break;
                    case "==": {
                        if (this.get(r, c) == value) {
                            list.add(new double[]{r, c});
                        }
                    }
                    break;
                    case "!=": {
                        if (this.get(r, c) != value) {
                            list.add(new double[]{r, c});
                        }
                    }
                    break;
                }
            }
        }
        double[][] arr = new double[list.size()][2];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    /**
     * Get an array with the values that met the condition
     *
     * @param conditionOperator logical operator
     * @param value the condition value
     * @return the values that met the condition
     */
    public double[] find(String conditionOperator, double value) {
        ArrayList<Double> list = new ArrayList<>();
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                switch (conditionOperator) {
                    case "<": {
                        if (this.get(r, c) < value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case ">": {
                        if (this.get(r, c) > value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case ">=": {
                        if (this.get(r, c) >= value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case "<=": {
                        if (this.get(r, c) <= value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case "==": {
                        if (this.get(r, c) == value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case "!=": {
                        if (this.get(r, c) != value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                }
            }
        }
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    /**
     * Get an n by 1 matrix with the values that met a logic condition
     *
     * @param conditionOperator logical operator
     * @param value the condition value
     * @return a n by 1 matrix with the values that met the condition
     */
    public Matrix findM(String conditionOperator, double value) {
        ArrayList<Double> list = new ArrayList<>();
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                switch (conditionOperator) {
                    case "<": {
                        if (this.get(r, c) < value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case ">": {
                        if (this.get(r, c) > value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case ">=": {
                        if (this.get(r, c) >= value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case "<=": {
                        if (this.get(r, c) <= value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case "==": {
                        if (this.get(r, c) == value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                    case "!=": {
                        if (this.get(r, c) != value) {
                            list.add(this.get(r, c));
                        }
                    }
                    break;
                }
            }
        }
        Matrix temp = new Matrix(list.size(), 1);
        for (int i = 0; i < list.size(); i++) {
            temp.set(i, 0, list.get(i));
        }
        return temp;
    }

    /**
     * Replace all elements equals to oldVal into newVal
     *
     * @param oldVal
     * @param newVal
     * @return
     */
    public Matrix replace(double oldVal, double newVal) {
        Matrix temp = new Matrix(this.rows, this.cols, 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (this.get(i, j) == oldVal) {
                    temp.set(i, j, newVal);
                } else {
                    temp.set(i, j, this.get(i, j));
                }
            }
        }
        return temp;
    }

    /**
     * Replace all nonzeros values
     *
     * @param newVal
     * @return
     */
    public Matrix replace(double newVal) {
        Matrix temp = new Matrix(this.rows, this.cols, 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (this.get(i, j) != 0) {
                    temp.set(i, j, newVal);
                }
            }
        }
        return temp;
    }

    private Matrix redim(int row, int cols) {
        Matrix temp = new Matrix(row, cols, 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                temp.set(i, j, this.get(i, j));
            }
        }
        return temp;
    }

    public Matrix insertRow() {
        return this.redim(this.rows + 1, this.cols);
    }

    private void insertEmptyRow() {
        double oldData[][] = new double[this.rows + 1][this.cols];
        for (int i = 0; i < this.rows; i++) {
            System.arraycopy(this.getData()[i], 0, oldData[i], 0, this.cols);
        }
        this.data = oldData;
        this.rows++;
    }

    public Matrix insertRows(int index, int numRows) {
        Matrix temp = new Matrix(this.getRows() + numRows, this.getCols(), 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (i < index) {
                    temp.set(i, j, this.get(i, j));
                }
                if (i >= index) {
                    temp.set(i + numRows, j, this.get(i, j));
                }
            }
        }
        return temp;
    }

    /**
     * This function fills a column
     *
     * @param col the index of the column to fill the rows
     * @param rowValx
     */
    public void setRows(int col, double[] rowValx) {
        for (int i = 0; i < rowValx.length; i++) {
            this.set(i, col, rowValx[i]);
        }
    }

    public void setCols(int row, double[] colVals) {
        for (int i = 0; i < colVals.length; i++) {
            this.set(row, i, colVals[i]);
        }
    }

    public void insertCol() {
        Matrix redim = this.redim(this.rows, this.cols + 1);
        this.setData(redim.getData());
    }

    public Matrix insertCols(int index, int numCols) {
        Matrix temp = new Matrix(this.getRows(), this.getCols() + numCols, 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (j < index) {
                    temp.set(i, j, this.get(i, j));
                }
                if (j >= index) {
                    temp.set(i, j + numCols, this.get(i, j));
                }
            }
        }
        return temp;
    }

    public static Matrix sign(Matrix matrix) {
        Matrix temp = new Matrix(matrix.getRows(), matrix.getCols(), 0.0);
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int col = 0; col < matrix.getCols(); col++) {
                if (matrix.get(row, col) < 0) {
                    temp.set(row, col, -1.0);
                } else if (matrix.get(row, col) == 0) {
                    temp.set(row, col, 0.0);
                } else {
                    temp.set(row, col, 1.0);
                }
            }
        }
        return temp;
    }

    /**
     * Set all negative values to -1.0 and positive values to 1.0, no changes
     * for elements equal to zero.
     */
    public void sign() {
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.cols; col++) {
                if (this.get(row, col) < 0) {
                    this.set(row, col, -1.0);
                } else if (this.get(row, col) == 0) {
                    this.set(row, col, 0.0);
                } else {
                    this.set(row, col, 1.0);
                }
            }
        }
    }

    /**
     *
     * @param n Number of columns
     * @return An nxn matrix with double pseudorandom double values between 0.0
     * inclusive and 1.0 exclusive
     */
    public static Matrix rand(int n) {
        return rand(n, n);
    }

    /**
     *
     * @param m Number of rows
     * @param n Number of columns
     * @return A double pseudorandom double value between 0.0 inclusive and 1.0
     * exclusive
     */
    public static Matrix rand(int m, int n) {
        Random rand = new Random();
        Matrix temp = new Matrix(m, n, 0.0);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                temp.set(i, j, rand.nextDouble());
            }
        }
        return temp;
    }

    /**
     * Square matrix n x n dimensions with integer values randomly initialized
     * between a min and max values
     *
     * @param n
     * @param min
     * @param max
     * @return
     */
    public static Matrix randi(int n, int min, int max) {
        //Random rand = new Random();
        int randomNum;
        Matrix matrix = new Matrix(n, n, 0.0);
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                randomNum = Util.getRandomInt(min, max);
                matrix.set(row, col, randomNum);
            }
        }
        return matrix;
    }

    /**
     * Square matrix n x n dimensions with binary (0,1) values randomly
     * initialized.
     *
     * @param n dimensionality of the matrix
     * @return a n x n matrix
     */
    public static Matrix randBinary(int n) {
        return randi(n, 0, 2);
    }

    /**
     * Square matrix n x n dimensions with double values randomly initialized
     * between a min and max values
     *
     * @param n
     * @param min
     * @param max
     * @return
     */
    public static Matrix rand(int n, double min, double max) {
        double randomNum;
        Matrix matrix = new Matrix(n, n, 0.0);
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                randomNum = Util.getRandom(min, max);
                matrix.set(row, col, randomNum);
            }
        }
        return matrix;
    }

    /**
     * Normalize the values of the matrix between a and b values.
     *
     * @param a
     * @param b
     */
    public void norm(double a, double b) {
        double min = this.min();
        double max = this.max();
        double x = 0;
        for (int row = 0; row < this.getRows(); row++) {
            for (int col = 0; col < this.getCols(); col++) {
                x = a + ((this.get(row, col) - min) * (b - a)) / (max - min);
                this.set(row, col, x);
            }
        }
    }

    /**
     * Normalize the values of the matrix between a and b values.
     *
     * @return a m-by-n normalized matrix between
     */
    public Matrix norm() {
        double min = this.min();
        double max = this.max();
        return this.getNormalised(this, min, max);
    }

    /**
     * Normalize the values of the matrix between a and b values.
     *
     * @param matrix
     * @param a
     * @param b
     * @return
     */
    public Matrix getNormalised(Matrix matrix, double a, double b) {
        Matrix temp = new Matrix(matrix.getRows(), matrix.getCols());
        double min = matrix.min();
        double max = matrix.max();
        double x = 0;
        for (int row = 0; row < this.getRows(); row++) {
            for (int col = 0; col < this.getCols(); col++) {
                x = a + ((matrix.get(row, col) - min) * (b - a)) / (max - min);
                temp.set(row, col, x);
            }
        }
        return temp;
    }

    /**
     * Exports the matrix into file.
     *
     * @param fileName the file name
     * @param separator the separator character
     */
    public void export(String fileName, String separator) {
        this.export(fileName, separator, false);
    }

    /**
     * Exports the matrix into file.
     *
     * @param fileName the file name
     * @param separator the separator character
     * @param append
     */
    public void export(String fileName, String separator, boolean append) {
        LOGGER.info("Saving matrix into " + fileName);
        File f = new File(fileName);
        FileWriter fw;
        String newline = System.getProperty("line.separator");
        StringBuilder sb;
        try {
            fw = new FileWriter(f, append);            
            BufferedWriter bw = new BufferedWriter(fw);
            for (int r = 0; r < this.rows; r++) {
                sb = new StringBuilder();
                for (int c = 0; c < this.data[r].length; c++) {
                    sb.append(String.valueOf(this.data[r][c])).append(separator);
                }
                if (sb.lastIndexOf(",") >= 0) {
                    sb = sb.deleteCharAt(sb.lastIndexOf(","));
                }
                bw.write(sb.toString() + newline);
            }
            bw.close();
            LOGGER.info("Complete");
        } catch (IOException ex) {
            LOGGER.error(ex.toString());
        }
    }

    /**
     * Saves the string buffer into a text file
     *
     * @param FileName the file name
     * @param sb the string buffer
     */
    public void export(String FileName, StringBuffer sb) {
        Util.saveStringToFile(sb, FileName);
    }

    /**
     * Returns a 2 x steps Matrix, the first row contains the intervals and the
     * second row the number of values between them. The intervals begin with
     * the specified initValue (inclusive) and ends with the endValue
     * (inclusive).
     *
     * @param initValue Initial value (inclusive)
     * @param endValue Final value (inclusive)
     * @param steps number of steps between the range
     * @return
     */
    public Matrix getDistribution(double initValue, double endValue, int steps) {
        double stepSize = (endValue - initValue) / steps;
        Matrix res = new Matrix(2, steps + 1, 0.0);  // Returns X,Y values for 
        double cValue = initValue;
        for (int i = 0; i <= steps; i++) {
            res.set(0, i, cValue);
            cValue += stepSize;
        }
        double val;
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                val = this.get(r, c);
                if (val != 0) { ////// ATENTION /////////////////////
                    for (int mc = 1; mc < res.getCols() - 1; mc++) {
                        if (val >= res.get(0, mc - 1) && val < res.get(0, mc)) {
                            res.set(1, mc, res.get(1, mc) + 1);
                        }
                    }
                    if (val >= res.get(0, res.getCols() - 2) && val <= res.get(0, res.getCols() - 1)) {
                        res.set(1, res.getCols() - 1, res.get(1, res.getCols() - 1) + 1);
                    }
                }
            }
        }
        return res;
    }

    /**
     * Get the normal distribution of the values of a n by 1 matrix
     *
     * @return
     */
    public double[][] getNormalDistribution() {
        double mean = this.meanValue();
        double sigma = this.stdValue();

        double[][] values = new double[this.numel()][2];
        double value;

        int i = 0;
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                value = (1 / Math.sqrt(2 * Math.PI * Math.pow(sigma, 2.0))) * Math.exp(-(Math.pow(this.get(r, c) - mean, 2.0)) / (2 * Math.pow(sigma, 2.0)));
                values[i][0] = this.get(r, c);
                values[i][1] = value;
            }
        }
        return values;
    }

    /**
     * Get the standard normal distribution
     *
     * @return
     */
    public double[][] getZScore() {
        double mean = this.meanValue();
        double std = this.stdValue();
        return getZScore(mean, std);
    }

    /**
     * Get the standard normal distribution
     *
     * @param mean
     * @param std
     * @return
     */
    public double[][] getZScore(double mean, double std) {
        double[][] values = new double[this.numel()][2];
        double value;
        int i = 0;
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                value = (this.get(r, c) - mean) / std;
                values[i][0] = this.get(r, c);
                values[i][1] = value;
                i++;
            }
        }
        return values;
    }

    public Matrix triu(int k) {
        Matrix m = new Matrix(this.rows, this.cols, 0.0);
        for (int i = 0; i < this.rows; i++) {
            for (int j = k; j < this.cols; j++) {
                m.set(i, j, this.get(i, j));
            }
            k++;
        }
        return m;
    }

    /**
     * Creates an identity m-by-m matrix
     *
     * @param m the size of the matrix
     * @return the identity matrix
     */
    public Matrix eye(int m) {
        Matrix eye = new Matrix(m, m, 0.0);
        for (int i = 0; i < m; i++) {
            eye.set(i, i, 1.0);
        }
        return eye;
    }

    /**
     * Creates m-by-m matrix of ones
     *
     * @param m the size of the matrix
     * @return the identity matrix
     */
    public Matrix ones(int m) {
        return new Matrix(m, m, 1.0);
    }

    /**
     * Creates m-by-m matrix of zeros
     *
     * @param m the size of the matrix
     * @return the identity matrix
     */
    public Matrix zeros(int m) {
        return new Matrix(m, m, 0.0);
    }

    public static void main(String[] args) {
        double data1[][] = {{1, 2, 3}, {4, 5, 6}};
        double data2[][] = {{1, 4, 7}};
        double data3[][] = {{5, 3, 0}, {2, 3, 1}, {0, 2, 11}};
        double data4[][] = {{13, 18}, {12, 19}};
        double[][] data6 = {{-0.03, -0.03, 0.01, 0.02, 0.02, 0.06, -0.09, -0.06, -0.05, -0.11},
        {-0.13, 0.06, -0.08, -0.14, 0.13, -0.09, 0.03, 0.01, -0.05, 0.08},
        {-0.06, -0.03, -0.11, 0.12, 0.09, 0.01, 0.12, 0.01, 0.13, -0.09},
        {0.05, -0.11, -0.09, 0.09, 0.05, -0.11, -0.03, -0.12, 0.11, 0.07},
        {-0.02, -0.15, -0.07, 0.03, 0.09, -0.06, -0.06, -0.12, -0.12, 0.00},
        {0.04, -0.05, 0.03, -0.14, 0.02, 0.05, -0.08, -0.06, -0.01, 0.10},
        {0.03, 0.03, -0.08, -0.13, -0.01, -0.07, -0.09, 0.14, 0.00, -0.14},
        {0.05, 0.12, -0.01, 0.03, -0.10, 0.04, 0.13, 0.06, -0.05, 0.14},
        {0.10, 0.11, 0.05, -0.08, -0.09, -0.06, -0.08, 0.10, 0.11, 0.09},
        {0.07, -0.01, -0.12, 0.01, -0.05, 0.06, 0.08, 0.11, 0.03, -0.15}};

        Matrix m1 = new Matrix(data1);
        Matrix m2 = new Matrix(data2);
        Matrix m3 = new Matrix(data3);

        Matrix temp = m1.vertInsert(m3);
        temp.show();

        Matrix temp2 = new Matrix().vertcat(m1, m3);
        System.out.println("");
        temp2.show();

        Matrix randM = Matrix.randBinary(10);
        Matrix randM2 = Matrix.randBinary(10);
        System.out.println("");
        randM.show();
        System.out.println("");
        randM2.show();

    }
}
