package ch.orbitsapps.imative;

public class ParentedPoint {
    ParentedPoint parent;
    int rank,label;
    boolean taken = false;
    ParentedPoint(int label) {
        rank = 0;
        parent = this;
        this.label = label;
    }

    static void Union(ParentedPoint p1, ParentedPoint p2) {
        ParentedPoint root1 = Find(p1);
        ParentedPoint root2 = Find(p2);
        if(root1 == root2) return;
        if(root1.rank < root2.rank) {
            ParentedPoint temp = root1;
            root1 = root2;
            root2 = temp;
        }
        root2.parent = root1;
        if(root1.rank == root2.rank) ++root1.rank;
    }

    static ParentedPoint Find(ParentedPoint p) {
        if (p.parent != p) {
            if(p.taken) p.parent.taken = true;
            p.parent = Find(p.parent);
        }
        return p.parent;
    }
}
