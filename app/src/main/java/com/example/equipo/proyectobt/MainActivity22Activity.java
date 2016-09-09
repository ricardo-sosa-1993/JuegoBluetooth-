package com.example.equipo.proyectobt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity22Activity extends ActionBarActivity {

    public static final int MESSAGE_READ = 9999;
    public static final int MESSAGE_WRITE = 1111;
    public static final int MESSAGE_DEVICE_NAME = 2222;

    public static final int ENVIO_LETRA = 3333;
    public static final int ENVIO__REC_LETRA = 4444;
    public static final int ADIVINA = 5555;
    public static final int BATALLA_NAVAL = 6666;

    //private BluetoothAdapter mBluetoothAdapter;
    private Handler handler;
    private ConexionBt mConexionBt;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
       // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button button = (Button) findViewById(R.id.envio_letra);
        Button button2 = (Button) findViewById(R.id.envio_rec_letra);
        Button button3 = (Button) findViewById(R.id.adivina);
        Button button4 = (Button) findViewById(R.id.batalla_naval);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConexionBt.write(intToByteArray(ENVIO_LETRA));
                //Intent intent = new Intent(getApplicationContext(),EnvioLetra.class);
                //startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConexionBt.write(intToByteArray(ENVIO__REC_LETRA));
                Intent intent = new Intent(getApplicationContext(),EnvioLetra.class);
                startActivity(intent);

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConexionBt.write(intToByteArray(ADIVINA));
                Intent intent = new Intent(getApplicationContext(),AdivinaNumero.class);
                startActivity(intent);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConexionBt.write(intToByteArray(BATALLA_NAVAL));
                //Intent intent = new Intent(getApplicationContext(),EnvioLetra.class);
                //startActivity(intent);
            }
        });



        handler =  new Handler(){
            public void handleMessage(Message msg) {
                // process incoming messages here http://stackoverflow.com/questions/14601730/how-handler-classes-work-in-android
                if(msg.what == MESSAGE_DEVICE_NAME) {
                    String datas= msg.getData().getString("nombre de dispositivo");
                    progressDialog.dismiss();
                    if (datas!= null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity22Activity.this);
                        builder.setTitle("Conectado");
                        builder.setMessage("El dispositivo se ha conectado con exito con " + datas);
                        builder.setPositiveButton("OK",null);
                        builder.create();
                        builder.show();

                    }
                    if (datas== null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity22Activity.this);
                        builder.setTitle("Conectado");
                        builder.setMessage("El dispositivo se ha conectado con exito con 'Dispositivo sin nombre'");
                        builder.setPositiveButton("OK",null);
                        builder.create();
                        builder.show();

                    }

                }
            }
        };

        mConexionBt = new ConexionBt(handler);
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(discoverableIntent, 42);




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity22, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if(requestCode == 42){

            if (resultCode == RESULT_CANCELED){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Error");
                builder.setMessage("No se pudo iniciar partida");
                builder.setPositiveButton("OK", null);
                builder.create();
                builder.show();
                //finish();

            }else{
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Buscando cliente");
                progressDialog.show();
                mConexionBt.start();
            }
        }
    }

    private byte[] intToByteArray(int a)
    {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    private  int byteArrayToInt(byte[] b)
    {
        return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mConexionBt != null) mConexionBt.stop();

    }
}
