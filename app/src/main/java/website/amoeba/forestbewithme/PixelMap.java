package website.amoeba.forestbewithme;

import java.util.Random;

/**
 * Created by aide0 on 11/8/2017.
 */

class PixelMap{
    Pixel[][] mPixels;
    public PixelMap(int size){
        generateMap(size);
    }
    public PixelMap(int size, boolean r){
        mPixels = new Pixel[size][size];
    }
    private void generateMap(int size){
        mPixels= new Pixel[size][size];
        int[][] heightArray,typeArray ;
        heightArray = new int[size][size];
        typeArray= new int[size][size];
        Random rNum = new Random();
        for(int i=0,h=0,t=0;i<size;i++) {
            for (int j = 0; j < size; j++) {
                h = rNum.nextInt(100);
                if (h<10)
                    heightArray[i][j]=0;
                else if (h<20)
                    heightArray[i][j]=1;
                else
                    heightArray[i][j]=2;
                t = rNum.nextInt(1000);
                if ((i!=0&&j!=0)&&(typeArray[i-1][j]==Pixel.WATER || typeArray[i][j-1]==Pixel.WATER)) {
                    if (t < 500)
                        typeArray[i][j] = Pixel.WATER;
                    else if (t < 900)
                        typeArray[i][j] = Pixel.POLLUTED_AREA;
                    else if (t < 950)
                        typeArray[i][j] = Pixel.CITY_RUIN;
                    else
                        typeArray[i][j] = Pixel.SOIL;
                }
                else{
                    if (t < 100)
                        typeArray[i][j] = Pixel.WATER;
                    else if (t < 800)
                        typeArray[i][j] = Pixel.POLLUTED_AREA;
                    else if (t < 900)
                        typeArray[i][j] = Pixel.CITY_RUIN;
                    else
                        typeArray[i][j] = Pixel.SOIL;
                }
                mPixels[i][j]=new Pixel(heightArray[i][j], typeArray[i][j]);
            }
        }
    }

    public Pixel[][] getPixels() {
        return mPixels;
    }

    class Pixel{
        final static int POLLUTED_AREA=3;
        final static int CITY_RUIN=4;
        final static int SOIL=5;
        final static int WATER=6;
        final static int TREE=7;
        private int height;
        private int type;
        public Pixel(int h,int t){
            height=h;
            type=t;
        }
        public Pixel(){
            height=0;
            type=0;
        }
        void setAtr(int h,int t){
            height=h;
            type=t;
        }

        public int getHeight() {
            return height;
        }

        public int getType() {
            return type;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
