package ch.orbitsapps.imative;
import android.util.Pair;
import java.util.ArrayList;

public class CannyFilter implements IFilter {

    @Override
    public ImageData[] applyTo(ImageData data) throws Exception{
        SobelOperator k = new SobelOperator(SobelOperator.Mode.Both, true);
        ImageData[] sobel = k.applyTo(data);
        ImageData gradientValue = sobel[1];
        ImageData gradientAngle = sobel[0];
        TimeKeeper.RecordStep("apply sobel");
        TimeKeeper.RecordStep("clone result");
        //thinning
        for(int i = 0; i < gradientAngle.width; ++i) {
            for(int j = 0; j < gradientAngle.height; ++j) {
                double ddx = Math.cos(Math.toRadians(gradientAngle.getColor(i,j) + 90));
                double ddy = Math.sin(Math.toRadians(gradientAngle.getColor(i,j) + 90));
                int dx = (int)Math.round(ddx);
                int dy = (int)Math.round(ddy) * -1;

                gradientAngle.setGray(i,j,gradientValue.getGray(i,j));

                if(!(i + dx < 0 || i + dx >= gradientAngle.width || j + dy < 0 || j + dy >= gradientAngle.height)
                        && gradientValue.getGray(i + dx, j + dy) > gradientValue.getGray(i,j))
                    gradientAngle.setGray(i,j,0);

                if(!(i - dx < 0 || i - dx >= gradientAngle.width || j - dy < 0 || j - dy >= gradientAngle.height)
                        && gradientValue.getGray(i - dx, j - dy) > gradientValue.getGray(i,j))
                    gradientAngle.setGray(i,j,0);
            }
        }

        TimeKeeper.RecordStep("thinn edges");
        final int uth = 10;
        final int lth = 5;

        //hysterisis
        for(int i = 0; i < data.width; ++i) {
            for(int j = 0; j < data.height; ++j) {
                if(gradientAngle.getGray(i,j) > uth) {
                    gradientAngle.setGray(i,j,255);
                } else if(gradientAngle.getGray(i,j) < lth) {
                    gradientAngle.setGray(i,j, 0);
                } else {
                    gradientAngle.setGray(i,j,128);
                }
            }
        }

        TwoPass(gradientAngle,gradientValue);

        TimeKeeper.RecordStep("apply hysterisis");
        return new ImageData[] {gradientValue};

    }

    private void TwoPass(ImageData dataIn, ImageData dataOut) {
        ArrayList<ParentedPoint> Linked = new ArrayList<>();
        int nextLabel = 0;

        for(int j = 0; j < dataIn.height; ++j) {
            for (int i = 0; i < dataIn.width; ++i) {
                if (dataIn.getGray(i, j) == 128) {
                    ArrayList<Pair<Integer, Integer>> neighbours = new ArrayList<>();
                    boolean foundHigh = false;
                    int[] dx = {1, 0, -1, -1};
                    int[] dy = {-1, -1, -1, 0};
                    for (int x = 0; x < dx.length; ++x) {
                        if (!(i + dx[x] < 0 || i + dx[x] >= dataIn.width || j + dy[x] < 0 || j + dy[x] >= dataIn.height)) {
                            if (dataIn.getGray(i + dx[x], j + dy[x]) == 128) {
                                neighbours.add(new Pair<>(i + dx[x], j + dy[x]));
                            } else if(dataIn.getGray(i + dx[x], j + dy[x]) == 255) foundHigh = true;
                        }
                    }

                    if (neighbours.isEmpty()) {
                        Linked.add(new ParentedPoint(nextLabel));
                        if(foundHigh) Linked.get(nextLabel).taken = true;
                        dataOut.setColor(i, j, nextLabel);
                        ++nextLabel;
                    } else {
                        int minL = nextLabel + 1;
                        ArrayList<Integer> nL = new ArrayList<>();
                        for (Pair<Integer, Integer> p : neighbours) {
                            nL.add(dataOut.getColor(p.first, p.second));
                            minL = Math.min(dataOut.getColor(p.first, p.second), minL);
                        }
                        dataOut.setColor(i, j, minL);
                        for (int l : nL) {
                            for (int m : nL) {
                                if(foundHigh) {
                                    Linked.get(l).taken = true;
                                    Linked.get(m).taken = true;
                                }
                                if (l != m) ParentedPoint.Union(Linked.get(l), Linked.get(m));
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0; i < dataIn.width; ++i) {
            for(int j = 0; j < dataIn.height; ++j) {
                if(dataIn.getGray(i,j) == 128) {
                    ParentedPoint root = ParentedPoint.Find(Linked.get(dataOut.getColor(i,j)));
                    if(root.taken) dataOut.setGray(i,j,255);
                    else dataOut.setGray(i,j,0);
                } else if(dataIn.getGray(i,j) == 255) dataOut.setGray(i,j,255);
                else dataOut.setGray(i,j,0);
            }
        }
    }
}
