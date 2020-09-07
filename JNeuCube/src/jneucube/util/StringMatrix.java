/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class StringMatrix {

    private int rows;
    private int cols;
    private String data[][];

    public StringMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new String[this.rows][this.cols];
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                data[r][c] = "";
            }
        }
    }

    public StringMatrix(File fileName, String separator) {
        //Matrix matrix=new Matrix();
        String line;
        String strValues[];
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                strValues = line.split(separator);
                if (i == 0) {
                    initialize(1, strValues.length, "");
                    i++;
                } else {
                    insertEmptyRow();
                }
                this.data[this.rows - 1] = strValues;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void initialize(int rows, int cols, String string) {
        this.setRows(rows);
        this.setCols(cols);
        setData(new String[rows][cols]);
        for (int m = 0; m < rows; m++) {
            for (int n = 0; n < cols; n++) {
                getData()[m][n] = string;
            }
        }
    }

    private void insertEmptyRow() {
        String oldData[][] = new String[this.getRows() + 1][this.getCols()];
        for (int i = 0; i < this.getRows(); i++) {
            System.arraycopy(this.getData()[i], 0, oldData[i], 0, this.getCols());
        }
        this.setData(oldData);
        this.setRows(this.getRows() + 1);
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
     * @return the data
     */
    public String[][] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String[][] data) {
        this.data = data;
    }

    public String get(int row, int col) {
        return this.data[row][col];
    }

    public String[] getVecCol(int numCol) {
        String[] vec = new String[this.getRows()];
        for (int i = 0; i < this.getRows(); i++) {
            vec[i] = this.get(i, numCol);
        }
        return vec;
    }

    public String[] getVecRow(int numRow) {
        String[] vec = new String[this.getCols()];
        for (int i = 0; i < this.getCols(); i++) {
            vec[i] = this.get(numRow, i);
        }
        return vec;
    }

    public void setVecRow(int numRow, String[] data) {
        for (int i = 0; i < this.getCols(); i++) {
            this.set(numRow, i, data[i]);
        }
    }

    public void setVecCol(int numCol, String[] data) {
        for (int i = 0; i < data.length; i++) {
            this.set(i, numCol, data[i]);
        }
    }

    public void set(int row, int col, String data) {
        this.data[row][col] = data;
    }    
}
