package ch.orbitsapps.imative;

import android.util.Pair;
import java.util.Stack;

public class CarreFilter implements IFilter {

    private int size;

    CarreFilter(int size) {
        this.size = size;
    }

    @Override
    public ImageData[] applyTo(ImageData data) throws Exception{
        CannyFilter kernel = new CannyFilter();
        ImageData cannyRes = data.clone();
        cannyRes = kernel.applyTo(cannyRes)[0];
        TimeKeeper.RecordStep("apply canny");
        int xsc = data.width / size;
        int ysc = data.height / size;

        for(int i = 0; i <= xsc; ++i) {
            for(int j = 0; j <= ysc; ++j) {
                boolean [][] visited = new boolean[size][size];
                for(int x = 0; x < size; ++x) {
                    for(int y = 0; y < size; ++y) {
                        if(!visited[x][y]) {
                            colorRegion(x,y,i,j,visited,cannyRes,data);
                        }
                    }
                }
            }
        }

        return new ImageData[] {data};
    }

    private void colorRegion(int x, int y, int i, int j, boolean [][] visited, ImageData canny, ImageData orig) {
        Stack<Pair<Integer,Integer>> stack = new Stack<>();
        Stack<Pair<Integer,Integer>> pToColor = new Stack<>();
        int count = 0;
        int totr = 0, totg = 0, totb = 0;
        stack.push(new Pair<>(x,y));
        while(!stack.empty()) {
            int curx = stack.peek().first;
            int cury = stack.pop().second;
            if(curx >= 0 && curx < size && cury >= 0 && cury < size &&
                    curx + i * size < orig.width && cury + j * size < orig.height &&
                    !visited[curx][cury]) {
                if(canny.getGray(curx + i * size, cury + j * size) == 0) {
                    stack.push(new Pair<>(curx + 1,cury));
                    stack.push(new Pair<>(curx - 1,cury));
                    stack.push(new Pair<>(curx,cury + 1));
                    stack.push(new Pair<>(curx,cury - 1));
                }
                visited[curx][cury] = true;
                ++count;
                totr += orig.getR(curx + i * size, cury + j * size);
                totg += orig.getG(curx + i * size, cury + j * size);
                totb += orig.getB(curx + i * size, cury + j * size);
                pToColor.push(new Pair<>(curx, cury));
            }
        }
        if(count > 0) {
            totr /= count;
            totg /= count;
            totb /= count;

            while (!pToColor.empty()) {
                int curx = pToColor.peek().first + i * size;
                int cury = pToColor.pop().second + j * size;
                orig.setR(curx, cury, totr);
                orig.setG(curx, cury, totg);
                orig.setB(curx, cury, totb);
            }
        }
    }
}
