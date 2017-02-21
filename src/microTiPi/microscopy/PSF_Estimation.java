/*
 * This file is part of TiPi (a Toolkit for Inverse Problems and Imaging)
 * developed by the MitiV project.
 *
 * Copyright (c) 2014 the MiTiV project, http://mitiv.univ-lyon1.fr/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package microTiPi.microscopy;

import mitiv.array.Array3D;
import mitiv.array.ShapedArray;
import mitiv.base.Shape;
import mitiv.deconv.WeightedConvolutionCost;
import mitiv.linalg.shaped.DoubleShapedVector;
import mitiv.linalg.shaped.DoubleShapedVectorSpace;
import mitiv.linalg.shaped.FloatShapedVectorSpace;
import mitiv.linalg.shaped.ShapedVector;
import mitiv.linalg.shaped.ShapedVectorSpace;
import mitiv.old.MathUtils;
import mitiv.optim.BoundProjector;
import mitiv.optim.LineSearch;
import mitiv.optim.MoreThuenteLineSearch;
import mitiv.optim.OptimTask;
import mitiv.optim.ReverseCommunicationOptimizer;
import mitiv.optim.VMLMB;

public class PSF_Estimation  {

    private double gatol = 0.0;
    private double grtol = 1e-3;
    private int limitedMemorySize = 5;
    private double lowerBound = Double.NEGATIVE_INFINITY;
    private double upperBound = Double.POSITIVE_INFINITY;
    private boolean debug = false;
    private int maxiter = 20;
    private int maxeval = 20;
    private ShapedArray data = null;
    private ShapedArray obj = null;
    //  private DoubleArray result = null;
    private ShapedArray psf = null;
    private double fcost = 0.0;
    private ShapedVector gcost = null;
    private MicroscopeModel pupil = null;
    private ReverseCommunicationOptimizer minimizer = null;
    //   private ReconstructionViewer viewer = null;
    private  ShapedArray weights = null;
    private boolean single;

    public static final int DEFOCUS = 1;
    public static final int ALPHA = 2;
    public static final int BETA = 3;

    private boolean run = true;

    public void enablePositivity(Boolean positivity) {
        setLowerBound(positivity ? 0.0 : Double.NEGATIVE_INFINITY);
    }

    public PSF_Estimation(MicroscopeModel pupil) {
        if (pupil!=null){
            this.pupil = pupil;
            single = pupil.single;
        }else{
            fatal("pupil not specified");
        }
    }
    private static void fatal(String reason) {
        throw new IllegalArgumentException(reason);
    }


    public void fitPSF(  int flag) {
        // FIXME set a best X
        DoubleShapedVector x = null;
        double best_cost = Double.POSITIVE_INFINITY;
        // Check input data and get dimensions.
        if (data == null) {
            fatal("Input data not specified.");
        }

        if(flag == DEFOCUS)
        {
            x = pupil.defocus_coefs;
        }
        else if (flag == ALPHA)
        {
            x = pupil.phase_coefs;
        }
        else if(flag == BETA)
        {
            x = pupil.modulus_coefs;
            System.out.println("pupil.modulus_coefs: " + pupil.modulus_coefs.getNumber());

        }else{
            fatal("Wrong flag type");
        }



        Shape dataShape = data.getShape();
        int rank = data.getRank();
        ShapedVectorSpace dataSpace, objSpace;

        DoubleShapedVector best_x = x.clone();
        // Check the PSF.
        if (obj == null) {
            fatal("Object not specified.");
        }
        if (obj.getRank() != rank) {
            fatal("Obj must have same rank as data.");
        }

        if(single){
            dataSpace = new FloatShapedVectorSpace(dataShape);
            objSpace = new FloatShapedVectorSpace(dataShape);
        }else{
            dataSpace = new DoubleShapedVectorSpace(dataShape);
            objSpace = new DoubleShapedVectorSpace(dataShape);
        }

        // Initialize a vector space and populate it with workspace vectors.

        DoubleShapedVectorSpace variableSpace = x.getSpace();
        int[] off ={0,0, 0};
        // Build convolution operator.
        WeightedConvolutionCost fdata = WeightedConvolutionCost.build(objSpace, dataSpace);
        fdata.setPSF(obj,off);
        fdata.setData(data);
        fdata.setWeights(weights);

        if (debug) {
            System.out.println("Vector space initialization complete.");
        }

        // Build the cost functions
        //   fcost = 0.0;
        gcost = objSpace.create();
        fcost = fdata.computeCostAndGradient(1.0, objSpace.create(pupil.getPSF() ), gcost, true);
        best_cost = fcost;
        best_x = x.clone();

        if (debug) {
            System.out.println("Cost function initialization complete.");
        }

        // Initialize the non linear conjugate gradient
        LineSearch lineSearch = null;
        VMLMB vmlmb = null;
        //BLMVM vmlmb = null;
        BoundProjector projector = null;
        int bounded = 0;
        limitedMemorySize = 0;

        if (lowerBound != Double.NEGATIVE_INFINITY) {
            bounded |= 1;
        }
        if (upperBound != Double.POSITIVE_INFINITY) {
            bounded |= 2;
        }


        if (debug) {
            System.out.println("bounded");
            System.out.println(bounded);
        }

        /* No bounds have been specified. */
        lineSearch = new MoreThuenteLineSearch(0.05, 0.1, 1E-17);

        int m = (limitedMemorySize > 1 ? limitedMemorySize : 5);
        vmlmb = new VMLMB(variableSpace, projector, m, lineSearch);
        vmlmb.setAbsoluteTolerance(gatol);
        vmlmb.setRelativeTolerance(grtol);
        minimizer = vmlmb;
        //

        if (debug) {
            System.out.println("Optimization method initialization complete.");
        }

        DoubleShapedVector gX = variableSpace.create();
        // Launch the non linear conjugate gradient
        OptimTask task = minimizer.start();
        while (run) {
            if (task == OptimTask.COMPUTE_FG) {
                pupil.setParam(x);

                pupil.computePSF();


                fcost = fdata.computeCostAndGradient(1.0, objSpace.create(pupil.getPSF()), gcost, true);

                if(fcost<best_cost){
                    best_cost = fcost;
                    best_x = x.clone();
                    if(debug){
                        System.out.println("Cost: " + best_cost);
                    }
                }
                gX =  pupil.apply_Jacobian(gcost,x.getSpace());

            } else if (task == OptimTask.NEW_X || task == OptimTask.FINAL_X) {
                /*   if (viewer != null) {
                    viewer.display(this);
                }*/
                boolean stop = (task == OptimTask.FINAL_X);
                if (! stop && maxiter >= 0 && minimizer.getIterations() >= maxiter) {
                    if (debug){
                        System.out.format("Warning: too many iterations (%d).\n", maxiter);
                    }
                    stop = true;
                }
                if (stop) {
                    break;
                }
            } else {
                if (debug){
                    System.out.println("TiPi: PSF_Estimation, "+task+" : "+minimizer.getReason());
                }
                break;
            }
            if (debug) {
                System.out.println("Evaluations");
                System.out.println(minimizer.getEvaluations());
                System.out.println("Iterations");
                System.out.println(minimizer.getIterations());
            }

            if (minimizer.getEvaluations() >= maxeval) {
                System.err.format("Warning: too many evaluation (%d).\n", maxeval);
                break;
            }
            task = minimizer.iterate(x, fcost, gX);

        }
        //  pupil.setParam(best_x);

        if(flag == DEFOCUS)
        {
            if (debug) {
                System.out.println("--------------");
                System.out.println("defocus");
                MathUtils.printArray(best_x.getData());
            }
            pupil.setDefocus(best_x);
        }
        else if (flag == ALPHA)
        {
            if (debug) {
                System.out.println("--------------");
                System.out.println("alpha");
                MathUtils.printArray(best_x.getData());
            }
            pupil.setPhase(best_x);
        }
        else if(flag == BETA)
        {
            if (true) {
                System.out.println("--------------");
                System.out.println("beta");
                MathUtils.printArray(best_x.getData());
            }
            pupil.setModulus(best_x );
        }
    }

    /* Below are all methods required for a ReconstructionJob. */

    public void setDebugMode(boolean value) {
        debug = value;
    }
    public void setMaximumIterations(int value) {
        maxiter = value;
        maxeval = 2* value; // 2 or 3 times more evaluations than iterations seems reasonable
    }
    public void setLimitedMemorySize(int value) {
        limitedMemorySize = value;
    }
    public void setAbsoluteTolerance(double value) {
        gatol = value;
    }
    public void setRelativeTolerance(double value) {
        grtol = value;
    }
    //  @Override
    public double getRelativeTolerance() {
        return grtol;
    }
    //the same effect with preMade value is enablePositivity()
    public void setLowerBound(double value) {
        lowerBound = value;
    }
    public void setUpperBound(double value) {
        upperBound = value;
    }
    public void stop(){
        run = false;
    }
    public void start(){
        run = true;
    }
    public void setWeight(ShapedArray shapedArray){
        this.weights = shapedArray;
    }
    /* public ReconstructionViewer getViewer() {
        return viewer;
    }
    public void setViewer(ReconstructionViewer rv) {
        viewer = rv;
    }*/
    public void setPupil(MicroscopeModel pupil) {
        this.pupil = pupil;
    }
    public MicroscopeModel getPupil() {
        return pupil;
    }
    public ShapedArray getData() {
        return data;
    }

    public void setData(ShapedArray shapedArray) {
        this.data = shapedArray;
    }

    public ShapedArray getPsf() {
        return psf;
    }
    public void setPsf(Array3D psf) {
        this.psf = psf;
    }


    // @Override
    public int getIterations() {
        return (minimizer == null ? 0 : minimizer.getIterations());
    }

    //   @Override
    public int getEvaluations() {
        return (minimizer == null ? 0 : minimizer.getEvaluations());
    }

    //  @Override
    public double getCost() {
        return fcost;
    }

    //  @Override
    public double getGradientNorm2() {
        return (gcost == null ? 0.0 : gcost.norm2());
    }

    //   @Override
    public double getGradientNorm1() {
        return (gcost == null ? 0.0 : gcost.norm1());
    }

    //   @Override
    public double getGradientNormInf() {
        return (gcost == null ? 0.0 : gcost.normInf());
    }
    public void setObj(ShapedArray objArray) {
        // TODO Auto-generated method stub
        this.obj = objArray;

    }
}