package com.example.mayank.pracrice;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.*;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener {
    private static final int MY_PERMISSION_REQUEST = 1;
    FloatingActionButton play, next, prev, search;
    Button songlbl;
    Uri songUri, caluri;
    String filename, currenttitle;
    Cursor cursor, nextcursor, prevcursor, calcursor, playcursor,resumecursor;
    ArrayList<String> arrayList;
    ListView listView;
    ArrayAdapter<String> adapter;
    int music_column_index, nextmusicIndex, prevmusicIndex, playmusicIndex,resumeindex;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    int length = 0, count = 0, flag = 0, post = 0;
    public static final String TEXT = "text";
    public static final String SHARED_PREFS = "sharedprefs";
    public static final String check = "false";
    public static final String POSITION = "position";
    public static final String LENGTH="length";
    public boolean checker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songlbl = (Button) findViewById(R.id.snglbl);
        songlbl.setSelected(true);
        play = (FloatingActionButton) findViewById(R.id.playpause);
        play.setOnClickListener(this);
        next = (FloatingActionButton) findViewById(R.id.next);
        next.setOnClickListener(this);
        prev = (FloatingActionButton) findViewById(R.id.prev);
        prev.setOnClickListener(this);
        search = (FloatingActionButton) findViewById(R.id.search);
        search.setOnClickListener(this);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        checker = sharedPreferences.getBoolean(check, false);
        if (checker) {
            currenttitle = sharedPreferences.getString(TEXT, "");
            play.setImageResource(R.drawable.play);
            songlbl.setText(currenttitle);
            post = sharedPreferences.getInt(POSITION, 0);
            //flag=sharedPreferences.getInt(FLAG,1);
            length=sharedPreferences.getInt(LENGTH,0);
           resume();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doStuff();
        } else {
            requeststoragepermission();
        }
    }

    public void requeststoragepermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this).setTitle("Permission needed").setMessage("This permission is needed to fetch the audio files.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                doStuff();
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void calmusicitem() {
        ContentResolver contentResolver1 = getContentResolver();
        caluri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        calcursor = contentResolver1.query(caluri, null, null, null, null);

        do {
            count++;
        } while (calcursor.moveToNext());
    }

    public void doStuff() {
        listView = (ListView) findViewById(R.id.songlist);
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        getMusic();
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //open music player to play desired song
                System.gc();
                music_column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                cursor.moveToPosition(position);
                post = position;
                int songtitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                currenttitle = cursor.getString(songtitle);
                filename = cursor.getString(music_column_index);
                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.reset();
                    }
                    if (flag == 1) {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(filename);
                    mMediaPlayer.prepare();
                    songlbl.setText(currenttitle);
                    savetitle();
                    play.setImageResource(R.drawable.pause);
                    flag = 1;
                    mMediaPlayer.start();
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mMediaPlayer.reset();
                            nextsong();
                        }
                    });
                } catch (Exception e) {
                }
            }
        });

    }

    public void resume() {
        ContentResolver contentResolver = getContentResolver();
        songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        resumecursor = contentResolver.query(songUri, null, null, null, MediaStore.Audio.Media.TITLE);
        resumeindex = resumecursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        resumecursor.moveToPosition(post);
        filename = resumecursor.getString(resumeindex);
    try{
        mMediaPlayer.setDataSource(filename);
        mMediaPlayer.prepare();
        flag=1;
    }
    catch (Exception e){

    }

}
    public void savetitle() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(TEXT, currenttitle);
        edit.putBoolean(check, true);
        edit.putInt(POSITION, post);
        edit.commit();
    }

    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        cursor = contentResolver.query(songUri, null, null, null, MediaStore.Audio.Media.TITLE);
        if (cursor != null && cursor.moveToFirst()) {
            int songtitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songdata = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songartist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            do {
                currenttitle = cursor.getString(songtitle);
                String currentartist = cursor.getString(songartist);
                arrayList.add(currenttitle + "\n" + currentartist);
            } while (cursor.moveToNext());
        }
        calmusicitem();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playpause:
                pauseplay();
                break;
            case R.id.next:
                nextsong();
                break;
            case R.id.prev:
                prevsong();
                break;
            case R.id.search:
                search();
                break;
        }

    }

    public void search() {

    }

    public void prevsong() {
        if (post <= 0) {
            Toast.makeText(MainActivity.this, "This is the last song,can't go back", Toast.LENGTH_SHORT).show();
        } else {
            ContentResolver contentResolver = getContentResolver();
            prevcursor = contentResolver.query(songUri, null, null, null, MediaStore.Audio.Media.TITLE);
            prevmusicIndex = prevcursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            prevcursor.moveToPosition(--post);
            String filename = prevcursor.getString(prevmusicIndex);
            int songtitle = prevcursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            currenttitle = prevcursor.getString(songtitle);
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(filename);
                mMediaPlayer.prepare();
                songlbl.setText(currenttitle);
                play.setImageResource(R.drawable.pause);
                SharedPreferences sharedPreferences=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putInt(POSITION,post);
                savetitle();
                mMediaPlayer.start();
            } catch (Exception e) {
            }
        }


    }

    public void nextsong() {
        if (post >= count - 2) {
            Toast.makeText(MainActivity.this, "Can't go next", Toast.LENGTH_SHORT).show();
        } else {
            ContentResolver contentResolver = getContentResolver();
            nextcursor = contentResolver.query(songUri, null, null, null, MediaStore.Audio.Media.TITLE);
            nextmusicIndex = nextcursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            nextcursor.moveToPosition(++post);
            String filename = nextcursor.getString(nextmusicIndex);
            int songtitle = nextcursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            currenttitle = nextcursor.getString(songtitle);
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(filename);
                mMediaPlayer.prepare();
                songlbl.setText(currenttitle);
                play.setImageResource(R.drawable.pause);
                SharedPreferences sharedPreferences=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putInt(POSITION,post);
                savetitle();
                mMediaPlayer.start();
            } catch (Exception e) {
            }
        }


    }

    public void pauseplay() {
        if (mMediaPlayer.isPlaying()) {
            listView = (ListView) findViewById(R.id.songlist);
            mMediaPlayer.pause();
            length = mMediaPlayer.getCurrentPosition();
            play.setImageResource(R.drawable.play);
            flag = 1;
        } else {
            mMediaPlayer.seekTo(length);
            play.setImageResource(R.drawable.pause);
            mMediaPlayer.start();

        }
        if (flag == 0) {
            ContentResolver contentResolver = getContentResolver();
            playcursor = contentResolver.query(songUri, null, null, null, MediaStore.Audio.Media.TITLE);
            playmusicIndex = playcursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            playcursor.moveToPosition(0);
            post = 0;
            String filename = playcursor.getString(playmusicIndex);
            int songtitle = playcursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            currenttitle = playcursor.getString(songtitle);
            try {
                mMediaPlayer.setDataSource(filename);
                mMediaPlayer.prepare();
                songlbl.setText(currenttitle);
                play.setImageResource(R.drawable.pause);
                flag = 1;
                mMediaPlayer.start();
            } catch (Exception e) {
            }
        }

    }
}
