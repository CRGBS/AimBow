package net.famzangl.minecraft.aimbow;
public final class Pos2 {
    public final int x,y;
    public Pos2(int x,int y){this.x=x;this.y=y;}
    public double distanceTo(Pos2 other){double dx=(double)x-other.x,dy=(double)y-other.y;return Math.sqrt(dx*dx+dy*dy);}
    public int hashCode(){return 31*x+y;}
    public boolean equals(Object o){return o instanceof Pos2&&((Pos2)o).x==x&&((Pos2)o).y==y;}
    public String toString(){return "Pos2[x="+x+", y="+y+"]";}
}
