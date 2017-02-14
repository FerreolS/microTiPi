/**
 *
 */
package lightSheet;

import org.apache.commons.math3.analysis.function.Gaussian;

import epifluorescence.WideFieldModel;
import microscopy.MicroscopeModel;
import mitiv.array.Array1D;
import mitiv.array.Array3D;
import mitiv.array.Double1D;
import mitiv.array.Float1D;
import mitiv.base.Shape;
import mitiv.linalg.shaped.DoubleShapedVector;
import mitiv.linalg.shaped.ShapedVector;
import mitiv.old.MathUtils;  // FIXME remove this ugly file
import utils.EnclosedUnivariateFunction;


/**
 * @author ferreol  <ferreol.soulez@epfl.ch>
 *
 */
public class LightSheetModel extends MicroscopeModel {

    protected double beamWidth; // Beam waist of the light sheet
    protected WideFieldModel wffm;
    protected Shape beamShape;
    protected Array1D beam;
    //   protected Gaussian beamFunc;
    protected EnclosedUnivariateFunction beamFunc;


    public LightSheetModel(Shape psfShape, double NA, double lambda, double ni, double beamWidth, double dxy, double dz, boolean radial,
            boolean single) {
        super(psfShape,  dxy, dz,  single);
        wffm= new WideFieldModel(psfShape, NA, lambda, ni, dxy, dz, radial, single);
        this.beamWidth = beamWidth;
        beamShape = new Shape(Nz);
        //  beamFunc = new Gaussian(0, beamWidth);
        beamFunc = new EnclosedUnivariateFunction(new Gaussian(0, beamWidth),dz);
    }



    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#computePSF()
     */
    @Override
    public void computePSF() {
        wffm.computeDefocus();
        if (single){
            beam = Float1D.create(beamShape);
        }else{
            beam = Double1D.create(beamShape);
            beam.assign(Double1D.wrap(MathUtils.fftIndgen(Nz),beamShape));
            ((Double1D) beam).map(beamFunc);
        }
    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#getPSF()
     */
    @Override
    public Array3D getPSF() {
        Array3D wffmPSF = wffm.getPSF();
        return wffmPSF;
    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#apply_J_modulus(mitiv.linalg.shaped.ShapedVector)
     */
    @Override
    protected DoubleShapedVector apply_J_modulus(ShapedVector grad) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#apply_J_defocus(mitiv.linalg.shaped.ShapedVector)
     */
    @Override
    protected DoubleShapedVector apply_J_defocus(ShapedVector grad) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#apply_J_phi(mitiv.linalg.shaped.ShapedVector)
     */
    @Override
    protected DoubleShapedVector apply_J_phi(ShapedVector grad) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#setDefocus(mitiv.linalg.shaped.DoubleShapedVector)
     */
    @Override
    protected void setDefocus(DoubleShapedVector defoc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#setModulus(mitiv.linalg.shaped.DoubleShapedVector)
     */
    @Override
    protected void setModulus(DoubleShapedVector modulus) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#setPhase(mitiv.linalg.shaped.DoubleShapedVector)
     */
    @Override
    protected void setPhase(DoubleShapedVector phase) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see mitiv.microscopy.MicroscopeModel#freePSF()
     */
    @Override
    public void freePSF() {
        // TODO Auto-generated method stub

    }

}
