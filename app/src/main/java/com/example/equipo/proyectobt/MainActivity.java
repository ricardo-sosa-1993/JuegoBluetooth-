package com.example.equipo.proyectobt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage("El dispositivo no soporta Bluetooth");
            builder.setPositiveButton("OK",null);
            builder.create();
            builder.show();
        }

        if (mBluetoothAdapter != null){
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 100);
            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Button button = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity2Activity.class); //unirse a partida
                startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity22Activity.class); //crear partida
                startActivity(intent);
            }
        });



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
        // Check which request we're responding to
        if (requestCode == 100) {

            if (resultCode == RESULT_CANCELED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Error");
                builder.setMessage("No se puede usar aplicacion sin Bluetooth");
                builder.setPositiveButton("OK", null);
                builder.create();
                builder.show();
                finish();

            }
        }
    }
}
