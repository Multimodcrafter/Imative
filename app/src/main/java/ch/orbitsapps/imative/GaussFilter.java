package ch.orbitsapps.imative;

public class GaussFilter extends SeparableFilter {
    GaussFilter(int size) {
        if(size % 2 == 0) ++size;
        double total = 0;
        double [] m = new double[size];
        double w = size/3;
        double euler = 1 / (2 * Math.PI * w * w);
        for(int i = -size/2; i <= size/2; ++i) {
            double dist = (i*i + size*size/4.0) / (2 * w*w);
            m[i + size/2] = euler * Math.exp(-dist);
            total += m[i + size/2];
        }
        for(int i = 0; i < m.length; ++i) {
            m[i] /= total;
        }
        Init(m,m,true, false);
    }
}
