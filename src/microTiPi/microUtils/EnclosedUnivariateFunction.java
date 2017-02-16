package microTiPi.microUtils;

import org.apache.commons.math3.analysis.UnivariateFunction;

import mitiv.base.mapping.DoubleFunction;
import mitiv.base.mapping.FloatFunction;

public class EnclosedUnivariateFunction implements DoubleFunction, FloatFunction {

    UnivariateFunction func;
    double scale;

    public EnclosedUnivariateFunction(UnivariateFunction func, double scale){
        this.func = func;
        this.scale = scale;
    }

    public EnclosedUnivariateFunction(UnivariateFunction func){
        this( func,1);
    }

    @Override
    public float apply(float arg) {
        return (float) func.value(scale * arg);
    }

    @Override
    public double apply(double arg) {
        return func.value(scale * arg);
    }

}
