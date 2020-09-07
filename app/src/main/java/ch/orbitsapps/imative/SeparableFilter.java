package ch.orbitsapps.imative;

public abstract class SeparableFilter implements IFilter {
    private double [] vm;
    private double [] hm;
    private boolean colorful;
    private boolean initok;
    private boolean relative;

    SeparableFilter(double [] vm, double[] hm, boolean colorful, boolean relative) {
        this.colorful = colorful;
        this.vm = vm;
        this.hm = hm;
        this.relative = relative;
        initok = true;
    }

    SeparableFilter() {
        initok = false;
    }

    protected void Init(double [] vm, double [] hm, boolean colorful, boolean relative) {
        this.vm = vm;
        this.hm = hm;
        this.colorful = colorful;
        this.relative = relative;
        initok = true;
    }

    public ImageData[] applyTo(ImageData data) throws Exception{
        if(!initok) throw new Exception("Filter not initialized");
        applyMatrix(data,true);
        applyMatrix(data,false);
        return new ImageData[] {data};
    }

    private void applyMatrix(ImageData dat, boolean horizontal) {
        double[] matrix = horizontal ? hm : vm;
        int [] cache;
        double size = 0;
        boolean negvalues = false;
        for (double value : matrix) {
            size += Math.abs(value);
            if (value < 0) negvalues = true;
        }
        for(int i = 0; i < (horizontal ? dat.height : dat.width); ++i) {
            cache = new int[matrix.length / 2];
            for(int j = 0; j < (horizontal? dat.width : dat.height); ++j) {
                double newr = 0;
                double newg = 0;
                double newb = 0;
                for(int x = -matrix.length / 2; x < matrix.length - matrix.length / 2 ; ++x) {
                    int pos = j + x;
                    int u = horizontal ? pos : i;
                    int v = horizontal ? i : pos;
                    if(u < 0 || v < 0 || u >= dat.width || v >= dat.height) continue;
                    if (x >= 0) {
                        if (colorful) {
                            newr += dat.getR(u, v) * matrix[x + matrix.length / 2];
                            newg += dat.getG(u, v) * matrix[x + matrix.length / 2];
                            newb += dat.getB(u, v) * matrix[x + matrix.length / 2];
                            if(x == 0) cache[j%cache.length] = dat.getColor(u,v);
                        } else {
                            newr += dat.getGray(u, v) * matrix[x + matrix.length / 2];
                            if(x == 0) cache[j%cache.length] = dat.getGray(u,v);
                        }
                    } else {
                        int val = cache[(j+x)%cache.length];
                        if (colorful) {
                            newr += ((val & 0x00FF0000) >> 16) * matrix[x + matrix.length / 2];
                            newg += ((val & 0x0000FF00) >> 8) * matrix[x + matrix.length / 2];
                            newb += (val & 0x000000FF) * matrix[x + matrix.length / 2];
                        } else {
                            newr += val * matrix[x + matrix.length / 2];
                        }
                    }
                }

                newr /= size;
                newg /= size;
                newb /= size;

                if(relative && negvalues) {
                    newr += 128;
                    newg += 128;
                    newb += 128;
                }
                int u = horizontal ? j : i;
                int v = horizontal ? i : j;
                if(colorful) {
                    dat.setR(u,v,(int)newr);
                    dat.setG(u,v,(int)newg);
                    dat.setB(u,v,(int)newb);
                } else {
                    dat.setGray(u,v,(int)newr);
                }
            }
        }
    }
}
