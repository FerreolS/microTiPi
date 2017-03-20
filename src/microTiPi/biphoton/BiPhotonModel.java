package microTiPi.biphoton;

import microTiPi.epifluorescence.WideFieldModel;
import microTiPi.microscopy.MicroscopeModel;
import mitiv.array.Array3D;
import mitiv.base.Shape;
import mitiv.linalg.shaped.DoubleShapedVector;
import mitiv.linalg.shaped.ShapedVector;
import mitiv.linalg.shaped.ShapedVectorSpace;

/**
 * Compute the 3D point spread function of a two photon microscope
 *
 * @author ferreol
 *
 */
public class BiPhotonModel extends MicroscopeModel {

    WideFieldModel pupil;
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
    public BiPhotonModel(Shape psfShape, double NA, double lambda, double ni, double dxy, double dz, int Nx, int Ny, int Nz,
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
        pupil = new WideFieldModel(psfShape, nPhase, nModulus, NA, lambda, ni, dxy, dz, radial, single);
    }
    @Override
    public
    void computePSF() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#getPSF()
     */
    @Override
    public Array3D getPSF() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#setParam(mitiv.linalg.shaped.DoubleShapedVector)
     */
    @Override
    public void setParam(DoubleShapedVector param) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#apply_Jacobian(mitiv.linalg.shaped.ShapedVector, mitiv.linalg.shaped.ShapedVectorSpace)
     */
    @Override
    public DoubleShapedVector apply_Jacobian(ShapedVector grad, ShapedVectorSpace xspace) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see microTiPi.microscopy.MicroscopeModel#freePSF()
     */
    @Override
    public void freePSF() {
        // TODO Auto-generated method stub

    }
}

