package com.example.josepablo.ejemplocamara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button bFoto, bGallery;
    private TextView tvUrl;
    private ImageView ivFoto;

    //TODO 02
    private static final int SELECT_PHOTO = 100;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    //TODO 03
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CAMERA = 200;
    private static final String FILE_PROVIDER = "com.example.josepablo.ejemplocamara.fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bFoto = (Button) findViewById(R.id.bFoto);
        bGallery = (Button) findViewById(R.id.bGaleria);
        tvUrl = (TextView) findViewById(R.id.tvUrl);
        ivFoto = (ImageView) findViewById(R.id.ivFoto);

        //TODO 04
        bGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarPermisosStorage();
            }
        });

        //TODO 10
        bFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarPermisosCamara();
            }
        });
    }
    //TODO 05
    public void validarPermisosStorage(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                //Mostrar mensaje de error
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }
        } else{
            iniciarIntentSeleccionarFotos();
        }
    }

    //TODO 11
    public void validarPermisosCamara(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                //Mostrar mensaje de error
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        } else{
            iniciarIntentTomarFoto();
        }
    }

    //TODO 06
    private void iniciarIntentSeleccionarFotos(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    //TODO 12
    private void iniciarIntentTomarFoto(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Validamos que hay una actividad de camara
        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ioe){
                Toast.makeText(this, ioe.getMessage().toString(), Toast.LENGTH_LONG).show();
            }

            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, FILE_PROVIDER, photoFile);
                // Llamamos el intent
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        }
    }

    //TODO 13
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreImagen = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                nombreImagen, /* prefijo */
                ".jpg",       /* sufijo */
                storageDir    /* directorio */
        );

        // Url local de la imagen
        String urlName = "file://" + image.getAbsolutePath();
        tvUrl.setText(urlName);
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permiso Concedido
                    iniciarIntentSeleccionarFotos();
                } else{
                    Toast.makeText(this, "Permission to read Gallery denied", Toast.LENGTH_SHORT).show();
                }
                return;
            //TODO 14
            case REQUEST_CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    iniciarIntentTomarFoto();
                } else {
                    Toast.makeText(this, "Permission to use camera denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        //TODO 08
        switch(requestCode){
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri imagenSeleccionada = imageReturnedIntent.getData();
                    try{
                        InputStream imagenStream = getContentResolver().openInputStream(imagenSeleccionada);
                        Bitmap imagen = BitmapFactory.decodeStream(imagenStream);
                        ivFoto.setImageBitmap(imagen);

                        tvUrl.setText(imagenSeleccionada.toString());
                    } catch (FileNotFoundException fnfe){
                        Toast.makeText(this, fnfe.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }

                }
                return;
            //TODO 15
            case REQUEST_CAMERA:
                if(resultCode == RESULT_OK){
                    Picasso.with(this).load(tvUrl.getText().toString()).into(ivFoto);
                }
                return;
        }
    }
}
