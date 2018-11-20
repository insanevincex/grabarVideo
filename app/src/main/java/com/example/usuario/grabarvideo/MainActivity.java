package com.example.usuario.grabarvideo;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button grabar;
    VideoView video;
    static final int TENGO_FICHERO = 1;
    static final int TENGO_PERMISOS = 2;
    MediaController control;
    String rutaVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        grabar=findViewById(R.id.grabar);
        video=findViewById(R.id.videoView);
        control=new MediaController(this);
        control.setAnchorView(video);

        grabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pedirPermisos();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void pedirPermisos() throws IOException {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //¿por qué pido permiso?
            } else {
                //Pido el permiso
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},TENGO_PERMISOS);
                // El resultado de la petición se recupera en onRequestPermissionsResult
            }
        } else {//Tengo el permiso
            grabarVideo();
        }
    }

    private void grabarVideo() throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT ,5);
        intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION,false);

        File fichero=crearFichero();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fichero));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TENGO_FICHERO);
        }else{
            Toast.makeText(this, getResources().getString(R.string.faltacamara), Toast.LENGTH_SHORT).show();
        }
    }

    private File crearFichero() throws IOException {
        String fechaYHora = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreFichero = "Ejemplo_"+fechaYHora;
        File carpeta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File vid = File.createTempFile(nombreFichero, ".mp4", carpeta);
        rutaVideo = vid.getAbsolutePath();
        Toast.makeText(this, getResources().getString(R.string.ruta)+rutaVideo, Toast.LENGTH_SHORT).show();
        return vid;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case TENGO_PERMISOS:{
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    try{
                        this.grabarVideo();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                } else {

                    //No tengo permisos: Le digo que no se puede hacer nada
                    Toast.makeText(this, getResources().getString(R.string.faltanpermisos), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == TENGO_FICHERO) && (resultCode == RESULT_OK)){
            Uri ruta=Uri.parse(rutaVideo);
            video.setMediaController(control);
            video.setVideoURI(ruta);
            video.requestFocus();
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
                @Override
                public void onPrepared(MediaPlayer mp) {
                    control.show();
                }
            });
        }
    }
}
