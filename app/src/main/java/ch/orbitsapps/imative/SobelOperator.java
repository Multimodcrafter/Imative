package ch.orbitsapps.imative;

import androidx.core.graphics.ColorUtils;

public class SobelOperator extends SeparableFilter {
    private final double [][] sobm = new double[][] {{1,0,-1},{1,2,1}};
    private final double [][] scharm = new double[][] {{1,0,-1},{3,10,3}};
    private final boolean scharr;
    private Mode m;

    public enum Mode {Horizontal, Vertical, Absolute, Angle, Both}

    SobelOperator(Mode m, boolean scharr) {
        this.m = m;
        double[] vm = scharr ? scharm[1] : sobm[1];
        double[] hm = scharr ? scharm[0] : sobm[0];
        if(m == Mode.Vertical) {
            Init(vm, hm, false, true);
        } else if(m == Mode.Horizontal) {
            Init(hm, vm, false, true);
        }
        this.scharr = scharr;
    }

    @Override
    public ImageData[] applyTo(ImageData data) throws Exception{
        ImageData [] result = new ImageData [1];
        ImageData v;
        ImageData h;

        GaussFilter filter = new GaussFilter(3);
        data = filter.applyTo(data)[0];

        TimeKeeper.RecordStep("apply gauss");

        if(m == Mode.Horizontal || m == Mode.Vertical) {
            result = super.applyTo(data);
            return result;
        }

        Init(scharr ? scharm[1] : sobm[1],scharr ? scharm[0] : sobm[0], false, true);
        v = data.clone();
        v = super.applyTo(v)[0];
        Init(scharr ? scharm[0] : sobm[0],scharr ? scharm[1] : sobm[1], false, true);
        h = super.applyTo(data)[0];

        switch (m) {
            case Both:
                Combine(v,h,false);
                result = new ImageData[2];
                result[0] = v;
                result[1] = h;
                break;
            case Angle:
                Combine(v,h,true);
                result[0] = v;
                break;
            case Absolute:
                Combine(v,h,false);
                result[0] = h;
                break;
        }
        return result;
    }

    private void Combine(ImageData v, ImageData h, boolean drawableAngles) {
        for(int i = 0; i < v.width; ++i) {
            for(int j = 0; j < v.height; ++j) {
                int vv = v.getGray(i,j) - 128;
                int hv = h.getGray(i,j) - 128;

                int av = Math.abs(vv) + Math.abs(hv);
                h.setGray(i,j,av);

                double gv = Math.round((Math.atan2(vv,hv) + Math.PI) / (2*Math.PI) * 8) * 45;
                int col = drawableAngles ? ColorUtils.HSLToColor(new float[]{(float)gv,1,(float)av/255}) : (int)gv;
                v.setColor(i,j,col);
            }
        }
    }
}
