package ch.orbitsapps.imative;

import android.graphics.Bitmap;

public class ImageData {
    private int [] values;
    final int width;
    final int height;

    ImageData(int w, int h) {
        values = new int[w*h];
        width = w;
        height = h;
    }

    ImageData(Bitmap img) {
        values = new int[img.getWidth() * img.getHeight()];
        img.getPixels(values,0,img.getWidth(), 0,0,img.getWidth(),img.getHeight());
        width = img.getWidth();
        height = img.getHeight();
    }

    private ImageData(int [] vs, int w, int h) {
        values = vs;
        width = w;
        height = h;
    }

    void applyToImage(Bitmap img) {
        img.setPixels(values,0,width,0,0,width,height);
    }

    int getB(int x, int y) {
        return values[x + y * width] & 0x000000FF;
    }

    int getG(int x, int y) {
        return (values[x + y * width] & 0x0000FF00) >> 8;
    }

    int getR(int x, int y) {
        return (values[x + y * width] & 0x00FF0000) >> 16;
    }

    int getGray(int x, int y) {
        int r = getR(x,y);
        int g = getG(x,y);
        int b = getB(x,y);
        return (r + g + b) / 3;
    }

    int getColor(int x, int y) {
        return values[x + y*width];
    }

    void setColor(int x, int y, int c) {
        values[x+y*width] = c;
    }

    void setB(int x, int y, int v) {
        int offset = x + y * width;
        values[offset] = (values[offset] & 0xFFFFFF00) | (v & 0x000000FF);
    }

    void setG(int x, int y, int v) {
        int offset = x + y * width;
        values[offset] = (values[offset] & 0xFFFF00FF) | ((v << 8) & 0x0000FF00);
    }

    void setR(int x, int y, int v) {
        int offset = x + y * width;
        values[offset] = (values[offset] & 0xFF00FFFF) | ((v << 16) & 0x00FF0000);
    }

    void setA(int x, int y, int v) {
        int offset = x + y * width;
        values[offset] = (values[offset] & 0x00FFFFFF) | ((v << 24) & 0xFF000000);
    }

    void setGray(int x, int y, int v) {
        setR(x,y,v);
        setG(x,y,v);
        setB(x,y,v);
        setA(x,y,0xFF);
    }

    @Override
    public ImageData clone() {
        int [] copy = values.clone();
        return new ImageData(copy, width, height);
    }
}
