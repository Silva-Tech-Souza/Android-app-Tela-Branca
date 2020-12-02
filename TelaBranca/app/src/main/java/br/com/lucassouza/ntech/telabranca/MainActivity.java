package br.com.lucassouza.ntech.telabranca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public boolean criado = false;
    private TelaBrancaView t_banca = null;
    public int controle_cor = 0, controle_pincel = 0;
    FloatingActionButton btn_limpartela, btn_voltar,  btn_salvar, btn_cores, btn_pincel;
    FloatingActionMenu floatingActionMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

try{
    View descorView = getWindow().getDecorView();
    descorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    );
}catch (Exception e){

}

        floatingActionMenu = findViewById(R.id.menu2);
        btn_cores = findViewById(R.id.btn_cores);
        btn_limpartela = findViewById(R.id.btn_limpartela);
        btn_voltar = findViewById(R.id.btn_desfazer);
        btn_salvar =  findViewById(R.id.btn_salvar);
        btn_pincel = findViewById(R.id.btn_pincel);
        this.t_banca = this.findViewById(R.id.tela_branca);
        this.t_banca.setMode(TelaBrancaView.Mode.DRAW);
        this.t_banca.setDrawer(TelaBrancaView.Drawer.PEN);
        this.t_banca.setPaintStyle(Paint.Style.STROKE);
        this.t_banca.setOpacity(128);
        this.t_banca.setBlur(15);
        this.t_banca.setBackgroundColor(Color.WHITE);

        btn_pincel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checarPermissao();
                if (controle_pincel == 0){
                    Toast.makeText(MainActivity.this, "Pincel grosso", Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeWidth = 6f;

                }else if (controle_pincel == 1){
                    Toast.makeText(MainActivity.this, "Pincel fino", Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeWidth = 3f;

                }

                if (controle_pincel <1){
                    controle_pincel++;
                }else{
                    controle_pincel=0;
                }

            }
        });
        btn_cores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checarPermissao();
                if (controle_cor == 0){
                    Toast.makeText(MainActivity.this, R.string.cor1, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.MAGENTA;
                    t_banca.paintFillColor= Color.MAGENTA;
                    floatingActionMenu.setMenuButtonColorNormal(Color.MAGENTA);
                }if (controle_cor == 1){
                    Toast.makeText(MainActivity.this, R.string.cor2, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.BLUE;
                    t_banca.paintFillColor= Color.BLUE;
                    floatingActionMenu.setMenuButtonColorNormal(Color.BLUE);
                }else if (controle_cor == 2){
                    Toast.makeText(MainActivity.this, R.string.cor3, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.GREEN;
                    t_banca.paintFillColor= Color.GREEN;
                    floatingActionMenu.setMenuButtonColorNormal(Color.GREEN);
                }else if (controle_cor == 3){
                    Toast.makeText(MainActivity.this, R.string.cor4, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.RED;
                    t_banca.paintFillColor= Color.RED;
                    floatingActionMenu.setMenuButtonColorNormal(Color.RED);
                }else if (controle_cor == 4){
                    Toast.makeText(MainActivity.this, R.string.cor5, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.rgb(255,215,0);
                    t_banca.paintFillColor= Color.rgb(255,215,0);
                    floatingActionMenu.setMenuButtonColorNormal(Color.rgb(255,215,0));
                }else if (controle_cor == 5){
                    Toast.makeText(MainActivity.this, R.string.cor6, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.CYAN;
                    t_banca.paintFillColor= Color.CYAN;
                    floatingActionMenu.setMenuButtonColorNormal(Color.CYAN);
                }else if (controle_cor == 6){
                    Toast.makeText(MainActivity.this, R.string.cor7, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.rgb(92,51,23);
                    t_banca.paintFillColor= Color.rgb(92,51,23);
                    floatingActionMenu.setMenuButtonColorNormal(Color.rgb(92,51,23));
                }else if (controle_cor ==7){
                    Toast.makeText(MainActivity.this, R.string.cor8, Toast.LENGTH_SHORT).show();
                    t_banca.paintStrokeColor = Color.BLACK;
                    t_banca.paintFillColor= Color.BLACK;
                    floatingActionMenu.setMenuButtonColorNormal(Color.BLACK);
                }
                if (controle_cor <7){
                    controle_cor++;
                }else{
                    controle_cor=0;
                }

            }
        });

        btn_limpartela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                t_banca.clear();
                try {
                  
                    checarPermissao();
                }catch (Exception e){

                }

                return;
            }
        });
        btn_voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t_banca.undo();
            }
        });
        btn_salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(criado == true){
                    saveImage(t_banca.getBitmap());
                    Toast.makeText(getBaseContext(), "Imagem Salva com Sucesso no seu Dispositivo", Toast.LENGTH_LONG).show();
                    try {
                       
                    }catch (Exception e){

                    }
                    return;
                }else{
                    Toast.makeText(MainActivity.this, "Primeiro permita o acesso ao armazenamento", Toast.LENGTH_SHORT).show();
                    checarPermissao();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        t_banca.undo();
    }

    public void checarPermissao(){

        int permissaoCheck = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissaoCheck != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            checarPermissao();
        }else{
            criado =true;
        }
    }
    private void saveImage(Bitmap pictureBitmap) {

        Intent shere = new Intent(Intent.ACTION_SEND);
        shere.setType("*/*");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pictureBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(),pictureBitmap,"TelaBranca", null);
        Uri imageUri = Uri.parse(path);
        shere.putExtra(Intent.EXTRA_STREAM, imageUri);


    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        try{
            View descorView = getWindow().getDecorView();
            descorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }catch (Exception e){

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            View descorView = getWindow().getDecorView();
            descorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }catch (Exception e){

        }

    }
}