package website.amoeba.forestbewithme;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static website.amoeba.forestbewithme.GenerateMap.MAP_SIZE;
import static website.amoeba.forestbewithme.Mutant.E;
import static website.amoeba.forestbewithme.Mutant.validate;
import static website.amoeba.forestbewithme.PixelMap.Pixel.CITY_RUIN;
import static website.amoeba.forestbewithme.PixelMap.Pixel.POLLUTED_AREA;
import static website.amoeba.forestbewithme.PixelMap.Pixel.SOIL;
import static website.amoeba.forestbewithme.PixelMap.Pixel.TREE;
import static website.amoeba.forestbewithme.PixelMap.Pixel.WATER;

public class GameMain extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    int x = 0, y = 0;
    static int MUTANT_NUM=100;
    static int ORI_SEED_NUM=10;
    int seedsNumber;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, mLastLocation;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100;
    String mapFileName;
    LocationRequest mLocationRequest;
    String locationFileName;
    String mutantsFileName;
    ImageAdapter imageAdapter;
    PixelMap mPixelmap = new PixelMap(MAP_SIZE);
    Mutant[] mutants= new Mutant[MUTANT_NUM];


    @Override
    public void onLocationChanged(Location location) {
        double deltaX=0, deltaY=0;
        mLastLocation = mCurrentLocation;
        mCurrentLocation = location;
//        Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
        if (mLastLocation == null)
            mLastLocation = location;

        //Calculate the distance
        deltaY = mCurrentLocation.getLatitude() - mLastLocation.getLatitude();
        deltaX = mCurrentLocation.getLongitude() - mLastLocation.getLongitude();
        deltaY = deltaY * 111700 / 10;
        double l = (mCurrentLocation.getLatitude() + mLastLocation.getLatitude()) / 2;
        l = l / 360 * 2 * Math.PI;
        deltaX = 111700 * deltaX * Math.cos(l) / 10;
        deltaY = -deltaY;
        if (deltaX > 3) deltaX = 3;
        else if (deltaX < -3) deltaX = -3;
        if (deltaY > 3) deltaY = 3;
        else if (deltaY < -3) deltaY = -3;

        //Refresh PC
        for (int i = 0; i < Math.abs(deltaX) ; i++) {
            if (deltaX > 0&& x < MAP_SIZE - 1 && mPixelmap.getPixels()[x + 1][y].getType() != WATER )
                x += 1;
            else if (deltaX < 0&& x > 0 && mPixelmap.getPixels()[x - 1][y].getType() != WATER )
                x -= 1;
        }
        for (int i=0;i < Math.abs(deltaY);i++){
            if (deltaY > 0 &&y < MAP_SIZE - 1&& mPixelmap.getPixels()[x][y + 1].getType() != WATER) y += 1;
            else if (deltaY < 0&&y>0 && mPixelmap.getPixels()[x][y - 1].getType() != WATER) y -= 1;
        }


        //Move Mutants
        for(int i=0;i<MUTANT_NUM;i++){
            int mx=mutants[i].getX();
            int my=mutants[i].getY();
/*            int t1=mPixelmap.getPixels()[mx+1][my].getType();
            int t2=mPixelmap.getPixels()[mx-1][my].getType();
            int t3=mPixelmap.getPixels()[mx][my+1].getType();
            int t4=mPixelmap.getPixels()[mx][my-1].getType();*/

            if(mutants[i].getX()>x&&validate(mPixelmap.getPixels()[mx-1][my].getType()))mutants[i].move(Mutant.W);
            else if(mutants[i].getX()<x&&validate(mPixelmap.getPixels()[mx+1][my].getType()))  mutants[i].move(Mutant.E);
            if(mutants[i].getY()>y&&validate(mPixelmap.getPixels()[mx][my-1].getType()))mutants[i].move(Mutant.N);
            else if(mutants[i].getY()<y&&validate(mPixelmap.getPixels()[mx][my+1].getType())) mutants[i].move(Mutant.S);
            if(mutants[i].getX()==x&&mutants[i].getY()==y){
                Toast.makeText(this,"You Died",Toast.LENGTH_LONG).show();
                finish();
            }
        }

        imageAdapter.notifyDataSetChanged();
    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onStop() {
        try {
            FileOutputStream locationFile = openFileOutput(locationFileName, Context.MODE_PRIVATE);
            locationFile.write(x);
            locationFile.write(y);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int t) {
        ;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently
        ;
        // ...
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_main);
        Intent intent = getIntent();
        mapFileName = intent.getStringExtra("MAP");
        GridView map = (GridView) findViewById(R.id.map);
        imageAdapter = new ImageAdapter(this);

        //Plant
        Button plantButton =(Button)findViewById(R.id.plant);
        plantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //种子数量无法判断，待修改
                if(true){
                    if (mPixelmap.mPixels[x][y].getType()==SOIL) {
                        mPixelmap.mPixels[x][y].setType(TREE);
                        seedsNumber--;
                        imageAdapter.notifyDataSetChanged();
                    }
                    else
                        Toast.makeText(GameMain.this.getBaseContext(),"You have to plant on a soil",Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(GameMain.this.getBaseContext(),"No Seeds!",Toast.LENGTH_LONG).show();
            }
        });


        //Load map & current location
        locationFileName = mapFileName + ".u";
        File file = getFileStreamPath(locationFileName);
        if (file.exists()) {
            try {
                FileInputStream locationFile = openFileInput(locationFileName);
                x = locationFile.read();
                y = locationFile.read();
                seedsNumber = locationFile.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileOutputStream locationFile = openFileOutput(locationFileName, Context.MODE_PRIVATE);
                Random rNum = new Random();
                x = rNum.nextInt(MAP_SIZE);
                y = rNum.nextInt(MAP_SIZE);
                while (mPixelmap.getPixels()[x][y].getType() == WATER) {
                    x = rNum.nextInt(MAP_SIZE);
                    y = rNum.nextInt(MAP_SIZE);
                }
                seedsNumber=ORI_SEED_NUM;
                locationFile.write(x);
                locationFile.write(y);
                locationFile.write(seedsNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream mapFile = openFileInput(mapFileName);
            for (int i = 0; i < MAP_SIZE; i++) {
                for (int j = 0, h = 0, t = 0; j < MAP_SIZE; j++) {
                    h = mapFile.read();
                    t = mapFile.read();
                    mPixelmap.getPixels()[i][j].setAtr(h, t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set Location Request Parameters
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Spawn the Mutants
        mutantsFileName=mapFileName+".m";
        file=getFileStreamPath(mutantsFileName);
        if (file.exists()) {
            try {
                FileInputStream mutantsFile = openFileInput(mutantsFileName);
                for (int i = 0; i < MUTANT_NUM; i++) {
                    int x = mutantsFile.read();
                    int y = mutantsFile.read();
                    mutants[i]=new Mutant(x,y);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileOutputStream mutantsFile = openFileOutput(mutantsFileName, Context.MODE_PRIVATE);
                for (int i = 0; i < MUTANT_NUM; i++) {
                    mutants[i] = new Mutant();
                    while (mPixelmap.getPixels()[mutants[i].getX()][mutants[i].getY()].getType() == WATER) {
                        mutants[i]=new Mutant();
                    }
                    mutantsFile.write(mutants[i].getX());
                    mutantsFile.write(mutants[i].getY());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        map.setAdapter(imageAdapter);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return 35;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            int currentX, currentY;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            //Spawn Map
            currentX = x - 2 + position % 5;
            currentY = y - 3 + position / 5;
            if (currentX >= MAP_SIZE || currentY >= MAP_SIZE || currentX < 0 || currentY < 0)
                imageView.setImageResource(R.drawable.wall);
            else {
                int h = mPixelmap.getPixels()[currentX][currentY].getHeight();
                int t = mPixelmap.getPixels()[currentX][currentY].getType();
                // Choose resources according to type
                switch (t) {
                    case SOIL:
                        imageView.setImageResource(R.drawable.soil);
                        break;
                    case POLLUTED_AREA:
                        imageView.setImageResource(R.drawable.polluted);
                        break;
                    case WATER:
                        imageView.setImageResource(R.drawable.water2);
                        break;
                    case CITY_RUIN:
                        imageView.setImageResource(R.drawable.city1);
                        break;
                    case TREE:
                        imageView.setImageResource(R.drawable.tree);
                        break;
                }
            }
            //put mutants to map
            int s=0;
            for (int i=0;i<MUTANT_NUM;i++) {
                if (currentX == mutants[i].getX() && currentY == mutants[i].getY())
                    s = 1;
            }
            if (s==0) imageView.setForeground(null);
                else  imageView.setForeground(getDrawable(android.R.drawable.ic_menu_info_details));
            //put PC to map
            if (position == getCount() / 2) {
                imageView.setForeground(getDrawable(android.R.drawable.ic_menu_mylocation));
            }
            return imageView;
        }
    }
}

