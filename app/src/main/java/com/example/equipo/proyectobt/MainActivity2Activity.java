package com.example.equipo.proyectobt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity2Activity extends ActionBarActivity {

    private ConexionBt mConexionBt;
    private Handler handler;
    private BluetoothAdapter mBluetoothAdapter;
    private SingBroadcastReceiver mReceiver;

    private ProgressDialog progressDialog;

    public static final int MESSAGE_READ = 9999;
    public static final int MESSAGE_WRITE = 1111;
    public static final int MESSAGE_DEVICE_NAME = 2222;

    public static final int ENVIO_LETRA = 3333;
    public static final int ENVIO__REC_LETRA = 4444;
    public static final int ADIVINA = 5555;
    public static final int BATALLA_NAVAL = 6666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler =  new Handler(){
            public void handleMessage(Message msg) {
                // process incoming messages here http://stackoverflow.com/questions/14601730/how-handler-classes-work-in-android
                if(msg.what == MESSAGE_DEVICE_NAME) {
                    String datas= msg.getData().getString("nombre de dispositivo");
                    progressDialog.dismiss();
                    if (datas!= null) {
                        progressDialog.setMessage("El dispositivo se ha conectado con exito con " + datas +".\nEsperando a que elija juego.");
                        progressDialog.show();

                    }
                    if (datas== null) {
                        progressDialog.setMessage("El dispositivo se ha conectado con exito con 'Dispsituvo sin nombre'.\nEsperando a que elija juego.");
                        progressDialog.show();

                    }

                }
                if(msg.what == MESSAGE_READ){
                    byte[] readBuf = (byte[]) msg.obj;
                    int a = byteArrayToInt(readBuf);
                    if(a==ENVIO_LETRA){
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2Activity.this);
                        builder.setTitle("Envio de letra");
                        builder.setMessage("Se eligio envio de letra");
                        builder.setPositiveButton("OK",null);
                        builder.create();
                        builder.show();

                    }
                    if(a==ENVIO__REC_LETRA){
                        progressDialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(),EnvioLetra.class);
                        startActivity(intent);

                    }
                    if(a==ADIVINA){
                        progressDialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(),AdivinaNumero.class);
                        startActivity(intent);

                    }
                    if(a==BATALLA_NAVAL){
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2Activity.this);
                        builder.setTitle("Batalla Naval");
                        builder.setMessage("Se Batalla Naval");
                        builder.setPositiveButton("OK",null);
                        builder.create();
                        builder.show();

                    }
                }
            }
        };
        mConexionBt = new ConexionBt(handler);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Buscando servidor");
        progressDialog.show();
        buscarDispositivos();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
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


    public void buscarDispositivos(){

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
        mReceiver = new SingBroadcastReceiver();
        IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, ifilter);
    }




    private class SingBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //may need to chain this to a recognizing function
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                // Get the BluetoothDevice object from the Intent

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mConexionBt.connect(device);

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
