package microTiPi.biphoton;

import microTiPi.epifluorescence.WideFieldModel;
import microTiPi.microscopy.MicroscopeModel;
import mitiv.array.Array3D;
import mitiv.array.Double3D;
import mitiv.array.DoubleArray;
import mitiv.array.Float3D;
import mitiv.array.FloatArray;
import mitiv.array.impl.FlatDouble3D;
import mitiv.array.impl.FlatFloat3D;
import mitiv.base.Shape;
import mitiv.base.mapping.DoubleFunction;
import mitiv.base.mapping.FloatFunction;
import mitiv.linalg.shaped.DoubleShapedVector;
import mitiv.linalg.shaped.DoubleShapedVectorSpace;
import mitiv.linalg.shaped.FloatShapedVector;
import mitiv.linalg.shaped.ShapedVector;
import mitiv.linalg.shaped.ShapedVectorSpace;

/**
 * Compute the 3D point spread function of a two photons microscope
 *
 * @author ferreol soulez
 *
 */
public class BiPhotonModel extends MicroscopeModel {

    WideFieldModel wfPSF;


    protected int nModulus;
    protected int nDefocus;
    protected int nPhase;

    double scale =1.;

    /**
     * index of defocus parameter space
     */
    public static final int DEFOCUS = 0;
    /**
     * index of phase parameter space
     */
    public static final int PHASE = 1;
    /**
     * index of modulus parameter space
     */
    public static final int MODULUS = 2;

    /**
     * @param psfShape  shape of the PSF
     * @param NA        numerical aperture
     * @param lambda    wavelength in m
     * @param ni        refractive index of the immersion medium
     * @param dxy       lateral pixel size
     * @param dz        axial sampling step size
     * @param single    single precision flag
     * @param radial    radially symmetric PSF flag
     */
    public BiPhotonModel(Shape psfShape, double NA, double lambda, double ni, double dxy, double dz,
            boolean radial,  boolean single) {
        this( psfShape,0, 1, NA,  lambda,  ni,  dxy,  dz,  radial,  single) ;
    }

    /**
     * @param psfShape  shape of the PSF
     * @param nPhase    number of phase coefficients
     * @param nModulus  number of modulus coefficients
     * @param NA        numerical aperture
     * @param lambda    wavelength in m
     * @param ni        refractive index of the immersion medium
     * @param dxy       lateral pixel size
     * @param dz        axial sampling step size
     * @param single    single precision flag
     * @param radial    radially symmetric PSF flag
     */
    public BiPhotonModel(Shape psfShape,int nPhase, int nModulus,
            double NA, double lambda, double ni, double dxy, double dz, boolean radial, boolean single) {
        super(psfShape, dxy, dz, single);
        wfPSF = new WideFieldModel(psfShape, nPhase, nModulus, NA, lambda, ni, dxy, dz, radial, single);

        parameterSpace = new DoubleShapedVectorSpace[3];
        parameterCoefs = new DoubleShapedVector[3];

        parameterSpace[DEFOCUS] =  wfPSF.getDefocusCoefs().getOwner();
        parameterCoefs[DEFOCUS] = wfPSF.getDefocusCoefs();
        setNi(ni);
        setNPhase(nPhase);
        if(nModulus<1){
            nModulus = 1;
        }
        setNModulus(nModulus);
        computePSF();
        scale = 1./ ((DoubleArray) psf).sum();
        if(single){
            ((FloatArray) psf).scale( (float) scale);
        }else{
            ((DoubleArray) psf).scale( scale);
        }
    }

    @Override
    public
    void computePSF() {
        if (PState>0)
            return;
        wfPSF.computePSF();
        if(single){
            psf = wfPSF.getPSF().copy();
            psf.flatten();
            ((FlatFloat3D) psf).map(new FloatFunction(){
                @Override
                public float apply(float arg) {
                    return arg*arg;
                }
            });
            ((Float3D) psf).scale((float) scale);
            //    ((Float3D) psf).scale((float) (1./((Float3D) psf).sum()));
        }else{
            psf = wfPSF.getPSF().copy();
            psf.flatten();
            ((FlatDouble3D) psf).map(new DoubleFunction(){
                @Override
                public double apply(double arg) {
                    return arg*arg;
                }
            });
            ((Double3D) psf).scale(scale);
            //   ((Double3D) psf).scale(1./((Double3D) psf).sum());
        }
        PState = 1;
    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#getPSF()
     */
    @Override
    public Array3D getPSF() {
        if (PState<1){
            computePSF();
        }
        return psf;
    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#setParam(mitiv.linalg.shaped.DoubleShapedVector)
     */
    @Override
    public void setParam(DoubleShapedVector param) {
        wfPSF.setParam(param);
        PState = 0;
    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#apply_Jacobian(mitiv.linalg.shaped.ShapedVector, mitiv.linalg.shaped.ShapedVectorSpace)
     */
    @Override
    public DoubleShapedVector apply_Jacobian(ShapedVector grad, ShapedVectorSpace xspace) {
        if(single){
            Float3D psf2 = (Float3D) wfPSF.getPSF().copy();
            psf2.scale((float) (2.*scale));
            ((FloatShapedVector) grad).multiply(grad.getSpace().create(psf2));
        }
        else{
            Double3D psf2 = (Double3D) wfPSF.getPSF().copy();
            psf2.scale(2.*scale);
            ((DoubleShapedVector) grad).multiply(grad.getSpace().create(psf2));
        }
        return wfPSF.apply_Jacobian(grad, xspace);
    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#freePSF()
     */
    @Override
    public void freePSF() {
        PState =0;
        psf = null;
    }

    /**
     * @param axis
     */
    public void setPupilAxis(double[] axis) {
        wfPSF.setPupilAxis(axis);
        if (parameterSpace[DEFOCUS]==null){
            parameterSpace[DEFOCUS] =  wfPSF.getDefocusCoefs().getOwner();
        }
        parameterCoefs[DEFOCUS] =  wfPSF.getDefocusCoefs();
    }

    /**
     * @param value
     */
    public void setNi(Double value) {
        wfPSF.setNi(value);
        if (parameterSpace[DEFOCUS]==null){
            parameterSpace[DEFOCUS] =  wfPSF.getDefocusCoefs().getOwner();
        }
        parameterCoefs[DEFOCUS] = wfPSF.getDefocusCoefs();
    }

    /**
     * @param modulus
     */
    public void setModulus(double[] modulus) {
        wfPSF.setModulus(modulus);
        if (parameterSpace[MODULUS]==null){
            parameterSpace[MODULUS] =  wfPSF.getModulusCoefs().getOwner();
        }
        parameterCoefs[MODULUS] = wfPSF.getModulusCoefs();
    }

    /**
     * @param phase
     */
    public void setPhase(double[] phase) {
        wfPSF.setPhase(phase);
        if (parameterSpace[PHASE]==null){
            parameterSpace[PHASE] =  wfPSF.getPhaseCoefs().getOwner();
        }
        parameterCoefs[PHASE] = wfPSF.getPhaseCoefs();
    }

    /**
     * @return
     */
    public double[] getPupilShift() {
        return wfPSF.getPupilShift();
    }

    /**
     * @return
     */
    public DoubleShapedVector getPhaseCoefs() {
        return wfPSF.getPhaseCoefs();
    }

    /**
     * @return
     */
    public DoubleShapedVector getModulusCoefs() {
        return wfPSF.getModulusCoefs();
    }

    /**
     * @return
     */
    public double[] getDefocus() {
        return wfPSF.getDefocus();
    }

    /**
     * @return
     */
    public int getNPhase() {
        return wfPSF.getNPhase();
    }

    /**
     * @param nbAlpha
     */
    public void setNPhase(int nbAlpha) {
        wfPSF.setNPhase(nbAlpha);
        nPhase = nbAlpha;
        if(nbAlpha==0){
            parameterSpace[PHASE] = null;
            parameterCoefs[PHASE] = null;
        }else{
            parameterSpace[PHASE] =  wfPSF.getPhaseCoefs().getOwner();
            parameterCoefs[PHASE] = wfPSF.getPhaseCoefs();
        }
    }
    /**
     * @return
     */
    public int getNModulus() {
        return wfPSF.getNModulus();
    }

    /**
     * @param nb
     */
    public void setNModulus(int nb) {
        wfPSF.setNModulus(nb);
        nModulus = nb;
        parameterSpace[MODULUS] =  wfPSF.getModulusCoefs().getOwner();
        parameterCoefs[MODULUS] = wfPSF.getModulusCoefs();
    }

    /**
     * @return
     */
    public double[] getRho() {
        return wfPSF.getRho();
    }

    /**
     * @return
     */
    public double[] getPhi() {
        return wfPSF.getPhi();
    }

    /**
     * @return
     */
    public Double getNi() {
        return wfPSF.getNi();
    }

    public double getScale(){
        return scale;
    }
    public void setScale(double scl){
        scale = scl;
    }
    public void updateScale(double updt){
        scale *= updt;
    }
}

