/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2020 Broad Institute, Aiden Lab, Rice University, Baylor College of Medicine
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package javastraw.matrices;


import javastraw.reader.block.ContactRecord;
import org.broad.igv.util.collections.FloatArrayList;
import org.broad.igv.util.collections.IntArrayList;

import java.util.Arrays;
import java.util.List;

public class SparseSymmetricMatrix implements BasicMatrix {

    private final int numValsEstimate;
    private final IntArrayList rows1;
    private final IntArrayList cols1;
    private final FloatArrayList values1;
    private IntArrayList rows2 = null;
    private IntArrayList cols2 = null;
    private FloatArrayList values2 = null;

    public SparseSymmetricMatrix(int numValsEstimate) {
        this.numValsEstimate = numValsEstimate;
        rows1 = new IntArrayList(numValsEstimate);
        cols1 = new IntArrayList(numValsEstimate);
        values1 = new FloatArrayList(numValsEstimate);
    }

    public void populateMatrix(List<ContactRecord> list, int[] offset) {
        for (ContactRecord cr : list) {
            int x = cr.getBinX();
            int y = cr.getBinY();
            float value = cr.getCounts();
            if (offset[x] != -1 && offset[y] != -1) {
                setEntry(offset[x], offset[y], value);
            }
        }
    }

    public double[] multiply(double[] vector) {

        double[] result = new double[vector.length];
        Arrays.fill(result, 0);

        multiplyOverRowsCols(vector, result, rows1, cols1, values1);
        if (rows2 != null) {
            multiplyOverRowsCols(vector, result, rows2, cols2, values2);
        }

        return result;
    }

    private void multiplyOverRowsCols(double[] vector, double[] result, IntArrayList rows1, IntArrayList cols1, FloatArrayList values1) {
        int[] rowArray1 = rows1.toArray();
        int[] colArray1 = cols1.toArray();
        float[] valueArray1 = values1.toArray();

        int n = rowArray1.length;
        for (int i = 0; i < n; i++) {
            int row = rowArray1[i];
            int col = colArray1[i];
            float value = valueArray1[i];
            result[row] += vector[col] * value;
            if (row != col) {
                result[col] += vector[row] * value;
            }
        }
    }


    @Override
    public float getEntry(int row, int col) {
        notImplementedDontCall();
        return 0;
    }

    /**
     * functions not implemented as they will reduce certain optimizations / add to runtime
     */
    private void notImplementedDontCall() {
        System.err.println("called unimplemented function - terminate");
        System.exit(9);
    }

    @Override
    public int getRowDimension() {
        notImplementedDontCall();
        return 0;
    }

    @Override
    public int getColumnDimension() {
        notImplementedDontCall();
        return 0;
    }

    @Override
    public float getLowerValue() {
        notImplementedDontCall();
        return 0;
    }

    @Override
    public float getUpperValue() {
        notImplementedDontCall();
        return 0;
    }

    @Override
    public void setEntry(int row, int col, float val) {
        if (!Float.isNaN(val)) {
            if (rows2 == null) {
                try {
                    rows1.add(row);
                    cols1.add(col);
                    values1.add(val);
                } catch (NegativeArraySizeException error) {
                    rows2 = new IntArrayList(numValsEstimate);
                    cols2 = new IntArrayList(numValsEstimate);
                    values2 = new FloatArrayList(numValsEstimate);
                    rows2.add(row);
                    cols2.add(col);
                    values2.add(val);
                }
            } else {
                rows2.add(row);
                cols2.add(col);
                values2.add(val);
            }
        }
    }
}