package website.amoeba.forestbewithme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class GenerateMap extends AppCompatActivity implements MapNameInputDialog.InputDialogListener{
    MapNameInputDialog nameDialog = new MapNameInputDialog();
    @Override
    public void onNegative(MapNameInputDialog dialog){
        dialog.getDialog().cancel();
    }

    @Override
    public void onPositive(MapNameInputDialog dialog){
        PixelMap mPixelMap=new PixelMap(MAP_SIZE);
        FileOutputStream outputStream;
        String fileName = nameDialog.getMapName();
        try{
            outputStream = openFileOutput(fileName+".map", Context.MODE_PRIVATE);
            for (int i=0,h=0,t=0;i<MAP_SIZE;i++){
                for (int j=0;j<MAP_SIZE;j++){
                    h=mPixelMap.getPixels()[i][j].getHeight();
                    t=mPixelMap.getPixels()[i][j].getType();
                    outputStream.write(h);
                    outputStream.write(t);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    final static int MAP_SIZE=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_map);
        final ListView mapList = (ListView)findViewById(R.id.mapList);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ArrayList<String> mapFilesArray = new ArrayList<>();
        ArrayAdapter<String> mAdapter;
        setSupportActionBar(toolbar);
        String[] allFileString= getApplicationContext().fileList();
        for(int i=0;i<allFileString.length;i++){
            if (allFileString[i].endsWith(".map")) {
                mapFilesArray.add(allFileString[i]);
            }
        }
        mAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.map_file_line,mapFilesArray);
        mapList.setAdapter(mAdapter);
        mapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s= (String)mapList.getItemAtPosition(position);
                startGame(s);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameDialog.show(getFragmentManager(),"nameinput");
            }
        });
    }

    public void startGame(String file){
        Intent intent=new Intent(this,GameMain.class);
        intent.putExtra("MAP",file);
        startActivity(intent);
    }

    class PixelMap{
        Pixel[][] mPixels;
        public PixelMap(int size){
            generateMap(size);
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
            private int height;
            private int type;
            public Pixel(int h,int t){
                height=h;
                type=t;
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
        }
    }
}
