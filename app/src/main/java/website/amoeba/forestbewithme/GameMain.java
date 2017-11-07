package website.amoeba.forestbewithme;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import java.util.Random;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static website.amoeba.forestbewithme.GenerateMap.PixelMap.Pixel.CITY_RUIN;
import static website.amoeba.forestbewithme.GenerateMap.PixelMap.Pixel.POLLUTED_AREA;
import static website.amoeba.forestbewithme.GenerateMap.PixelMap.Pixel.SOIL;
import static website.amoeba.forestbewithme.GenerateMap.PixelMap.Pixel.WATER;


public class GameMain extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, mLastLocation;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100;
    String mapFileName;
    LocationRequest mLocationRequest;
    ImageAdapter imageAdapter;

    @Override
    public void onLocationChanged(Location location) {
        double deltaX,deltaY;
        mLastLocation=mCurrentLocation;
        mCurrentLocation = location;
        if (mLastLocation==null)
            mLastLocation=location;
        deltaY=mCurrentLocation.getLatitude()-mLastLocation.getLatitude();
        deltaX=mCurrentLocation.getLongitude()-mLastLocation.getLongitude();
        deltaY=deltaY*111700;
        double l=(mCurrentLocation.getLatitude()+mLastLocation.getLatitude())/2;
        l=l/360*2*Math.PI;
        deltaX=111700*deltaX*Math.cos(l);
        try {
            FileInputStream lin=openFileInput(mapFileName+".u");
            int x=lin.read()+(int)deltaX/10;
            int y=lin.read()+(int)deltaY/10;
            FileOutputStream lou=openFileOutput(mapFileName+".u",Context.MODE_PRIVATE);
            lou.write(x);
            lou.write(y);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageAdapter.notifyDataSetChanged();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int t){
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
        int x=0,y=0;
        setContentView(R.layout.activity_game_main);
        Intent intent = getIntent();
        mapFileName=intent.getStringExtra("MAP");
        GridView map = (GridView)findViewById(R.id.map);
        imageAdapter=new ImageAdapter(this);
        map.setAdapter(imageAdapter);
        mLocationRequest=LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

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
            int x=0,y=0;
            ImageView imageView;
            int offset;
            int currentX,currentY;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            String locationFileName=mapFileName+".u";
            File file=getFileStreamPath(locationFileName);
            if (file.exists()){
                try {
                    FileInputStream locationFile = openFileInput(locationFileName);
                    x=locationFile.read();
                    y=locationFile.read();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                try {
                    FileOutputStream locationFile = openFileOutput(locationFileName, Context.MODE_PRIVATE);
                    Random rNum = new Random();
                    x=rNum.nextInt(100);
                    y=rNum.nextInt(100);
                    locationFile.write(x);
                    locationFile.write(y);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            try {
                FileInputStream mapFile = openFileInput(mapFileName);
                int h;
                int t;
                currentX = x-2 + position % 5;
                currentY = y-3 + position / 5;
                if (currentX >= 100 || currentY >= 100||currentX<0||currentY<0)
                    imageView.setImageResource(R.drawable.wall);
                else {
                    offset = 2 * (currentY * 100 + currentX);
                    mapFile.skip(offset);
                    h = mapFile.read();
                    t = mapFile.read();
                    // Realize t first
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
                            Random rNum = new Random();
                            if (rNum.nextInt(2) == 0) imageView.setImageResource(R.drawable.city1);
                            else imageView.setImageResource(R.drawable.city2);
                            break;
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            return imageView;
        }
    }
    public void refreshPC(){
        ;
    }
}

