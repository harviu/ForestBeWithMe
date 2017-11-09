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

public class GenerateMap extends AppCompatActivity implements MapNameInputDialog.InputDialogListener {
    MapNameInputDialog nameDialog = new MapNameInputDialog();
    ArrayAdapter<String> mAdapter;
    static int MAP_SIZE = 100;

    @Override
    public void onNegative(MapNameInputDialog dialog) {
        dialog.getDialog().cancel();
    }

    @Override
    public void onPositive(MapNameInputDialog dialog) {
        PixelMap mPixelMap = new PixelMap(MAP_SIZE);
        FileOutputStream outputStream;
        String fileName = nameDialog.getMapName();
        try {
            outputStream = openFileOutput(fileName + ".map", Context.MODE_PRIVATE);
            for (int i = 0, h = 0, t = 0; i < MAP_SIZE; i++) {
                for (int j = 0; j < MAP_SIZE; j++) {
                    h = mPixelMap.getPixels()[i][j].getHeight();
                    t = mPixelMap.getPixels()[i][j].getType();
                    outputStream.write(h);
                    outputStream.write(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter.add(fileName+".map");
        startGame(fileName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_map);
        final ListView mapList = (ListView) findViewById(R.id.mapList);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ArrayList<String> mapFilesArray = new ArrayList<>();
        setSupportActionBar(toolbar);
        String[] allFileString = getApplicationContext().fileList();
        for (int i = 0; i < allFileString.length; i++) {
            if (allFileString[i].endsWith(".map")) {
                mapFilesArray.add(allFileString[i]);
            }
        }
        mAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.map_file_line, mapFilesArray);
        mapList.setAdapter(mAdapter);
        mapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) mapList.getItemAtPosition(position);
                startGame(s);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameDialog.show(getFragmentManager(), "nameinput");
            }
        });
    }

    public void startGame(String file) {
        Intent intent = new Intent(this, GameMain.class);
        intent.putExtra("MAP", file);
        startActivity(intent);
    }
}

