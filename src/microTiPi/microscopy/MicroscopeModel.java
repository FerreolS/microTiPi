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
import mitiv.base.Shape;
import mitiv.linalg.shaped.DoubleShapedVector;
import mitiv.linalg.shaped.DoubleShapedVectorSpace;
import mitiv.linalg.shaped.ShapedVector;
import mitiv.linalg.shaped.ShapedVectorSpace;

/**
 * Abstract class for to model PSF of any fluorescence microscope
 *
 * @author Ferréol
 *
 */
public abstract class MicroscopeModel
{
    protected int PState=0;             // flag to prevent useless recomputation of the PSF
    protected final static boolean NORMALIZED = true;
    protected static final double DEUXPI = 2*Math.PI;
    protected double dxy;               // the lateral pixel size in meter
    protected double dz;                // the axial sampling step size in meter
    protected int Nx;                   // number of samples along lateral X-dimension
    protected int Ny;                   // number of samples along lateral Y-dimension
    protected int Nz;                   // number of samples along axial Z-dimension
    protected boolean single = false;   // computation in single precision

    protected Shape psfShape;           // Shape (size) of the PSF
    protected Array3D psf;              //3D point spread function

<<<<<<< HEAD
    protected DoubleShapedVectorSpace[] parameterSpace;
    protected DoubleShapedVector[] parameterCoefs;
=======
    protected DoubleShapedVectorSpace defocusSpace;
    protected DoubleShapedVectorSpace phaseSpace;
    protected DoubleShapedVectorSpace modulusSpace;
    protected DoubleShapedVector modulus_coefs;  // array of Zernike coefficients that describe the modulus
    protected DoubleShapedVector phase_coefs;  // array of Zernike coefficients that describe the phase
    protected DoubleShapedVector defocus_coefs;  // array of Zernike coefficients that describe the phase


    /**
     * Compute the PSF and fill the psf property
     */
    public  abstract  void computePSF();
    /**
     * Getter for the PSF
     * @return  psf
     */
    public  abstract  Array3D getPSF();

    /**
     * Apply the Jacobian of the modulus to the vector GRAD
     * @param grad
     * @return J(grad)
     */
    abstract public DoubleShapedVector apply_J_modulus(ShapedVector grad);
    /**
     * Apply the Jacobian of the defocus to the vector GRAD
     * @param grad
     * @return J(grad)
     */
    abstract public DoubleShapedVector apply_J_defocus(ShapedVector grad);
    /**
     * Apply the Jacobian of the phase to the vector GRAD
     * @param grad
     * @return J(grad)
     */
    abstract public DoubleShapedVector apply_J_phase(ShapedVector grad);

    /**
     * Setter for the defocus
     * @param defoc parameters
     */
    abstract public  void setDefocus(DoubleShapedVector defoc);
    /**
     * Setter for the modulus of the pupil
     * @param modulus parameters
     */
    abstract public  void setModulus(DoubleShapedVector modulus);
    /**
     * Setter for the phase of the pupil
     * @param phase parameters
     */
    abstract public  void setPhase(DoubleShapedVector phase);

    /**
     * free some memory
     */
    abstract public void freePSF();
>>>>>>> Add comments


    /** Initialize the  PSF model containing parameters
     *  @param psfShape shape of the PSF array
     *  @param dxy lateral pixel size
     *  @param dz axial sampling step size
<<<<<<< HEAD
     *  @param single single precision flag
=======
     * @param single enforce computation in float
>>>>>>> Add comments
     */
    public MicroscopeModel(Shape psfShape,
            double dxy, double dz,
            boolean single)
    {
        this.dxy = dxy;
        this.dz = dz;

        if (psfShape.rank() !=3){
            throw new IllegalArgumentException("PSF rank should be 3");
        }
        Nx = psfShape.dimension(0);
        Ny = psfShape.dimension(1);
        Nz = psfShape.dimension(2);
        this.psfShape = psfShape;
        this.single = single;
    }

<<<<<<< HEAD

    /**
     * Launch internal routines to compute PSF
     */
    abstract public void computePSF();

    /**
     * @return the PSF
     */
    abstract public Array3D getPSF();

    /**
     * Setter for PSF parameters. The parameter type is given by the parameter space of @param
     * @param param PSF parameters
     */
    abstract public void setParam(DoubleShapedVector param);

    /**
     * Apply the Jacobian to the gradient on the PSF to get the
     *  derivative with respect to the PSF parameters
     *
     * @param grad derivative with respect to the PSF pixels
     * @param xspace PSF parameter space
     * @return derivative with respect to the PSF parameters
     */
    abstract public DoubleShapedVector apply_Jacobian(ShapedVector grad, ShapedVectorSpace xspace);


    /**
     * Free some memory
     */
    abstract public void freePSF();

    /**
     * Setter for the single precision flag
=======
    /**
     * Apply Jacobian of parameters in xspace to the vector grad
     * @param grad input vector
     * @param xspace vector space of the parameter
     * @return J(x)
     */
    public DoubleShapedVector apply_Jacobian(ShapedVector grad, ShapedVectorSpace xspace){
        if(xspace ==  defocusSpace){
            return apply_J_defocus( grad);
        }else if(xspace ==  phaseSpace){
            return apply_J_phase( grad);
        }else if(xspace ==  modulusSpace){
            return apply_J_modulus( grad);
        }else{
            throw new IllegalArgumentException("DoubleShapedVector grad does not belong to any space");
        }
    }

    /**
     * Setter for the parameters param according to the vectorspace it belongs to
     * @param param
     */
    public  void setParam(DoubleShapedVector param) {
        if(param.getOwner() ==  defocusSpace){
            setDefocus(param);
        }else if(param.getOwner() ==  phaseSpace){
            setPhase(param);
        }else if(param.getOwner() ==  modulusSpace){
            setModulus(param);
        }else{
            throw new IllegalArgumentException("DoubleShapedVector param does not belong to any space");
        }
    }

    /**
     * Enforce single precision computation
>>>>>>> Add comments
     * @param single
     */
    public void setSingle(boolean single){
        this.single = single;
    }

    /**
     * @param best_x
     * @param flag
     */
    public void set(DoubleShapedVector best_x, int flag) {
        // TODO Auto-generated method stub

    }


}