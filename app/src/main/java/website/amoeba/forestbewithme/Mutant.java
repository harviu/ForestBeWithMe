package website.amoeba.forestbewithme;

import java.util.Map;
import java.util.Random;

import static website.amoeba.forestbewithme.GameMain.MUTANT_NUM;
import static website.amoeba.forestbewithme.GenerateMap.MAP_SIZE;

/**
 * Created by aide0 on 11/6/2017.
 */

public class Mutant {
    public static int W=0;
    public static int E=1;
    public static int N=2;
    public static int S=3;
    private int hp;
    private int speed;
    private int x;
    private int y;
    public int getX(){return x;}
    public int getY(){return y;}
    public Mutant (){
        Random rNum = new Random();
        x=rNum.nextInt(MUTANT_NUM);
        y=rNum.nextInt(MUTANT_NUM);
    }
    public Mutant(int m,int n){
        x=m;y=n;
    }
    public void move(int direction){
        if(direction==W) x--;
        if(direction==E) x++;
        if(direction==N) y--;
        if(direction==S) y++;
    }
    public static boolean validate(int t){
        if(t==PixelMap.Pixel.TREE || t==PixelMap.Pixel.WATER){
            return false;
        }
        return true;
    }
}
