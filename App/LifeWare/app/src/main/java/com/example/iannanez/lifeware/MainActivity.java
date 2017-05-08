package com.example.iannanez.lifeware;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity
{
    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series2;
    LinkedList<DataPoint> data1;
    LinkedList<DataPoint> data2;
    ArrayList<DataPoint> heartData;
    int location;
    int location2;
    String dataToSend;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataToSend = "";

        TextView tx = (TextView)findViewById(R.id.app_name);
        final EditText emailAdd = (EditText)findViewById(R.id.emailAddress);

        Typeface milkshake_font = Typeface.createFromAsset(getAssets(),  "fonts/Milkshake.ttf");
        tx.setTypeface(milkshake_font);


//        Button findButton = (Button)findViewById(R.id.find);
        Button openButton = (Button)findViewById(R.id.open);
        Button sendEmailButton = (Button)findViewById(R.id.sendEmail);
//        Button sendButton = (Button)findViewById(R.id.send);
//        Button closeButton = (Button)findViewById(R.id.close);

        myLabel = (TextView)findViewById(R.id.label);

        //myTextbox = (EditText)findViewById(R.id.entry);


 /*       findButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    findBT();
                }
                catch (IOException ex) { }
            }
        });
        //Open Button
 */       openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });

        sendEmailButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAdd.getText().toString()});
                    email.putExtra(Intent.EXTRA_SUBJECT, "Your Heart Beat");
                    email.putExtra(Intent.EXTRA_TEXT, dataToSend);
                    email.setType("message/rfc822");
                    startActivity(Intent.createChooser(email, "Choose an Email client :"));
                }
                catch (Exception e) { }
            }
        });


  /*      //Send Button
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    sendData();
                }
                catch (IOException ex) { }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
*/
        graph = (GraphView) findViewById(R.id.graph);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1024);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(300);

        series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0)
        });

        series2 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1.2)
        });

        series.setColor(Color.WHITE);
        series.setThickness(20);
        series2.setColor(Color.WHITE);
        series2.setThickness(20);
        graph.addSeries(series);
        graph.addSeries(series2);




        Log.v("Pre Loop","Not Looping");
        final long temp= System.currentTimeMillis();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                data1 = new LinkedList<DataPoint>();
                data2 = new LinkedList<DataPoint>();
                int k = 0;
                double pl = 0.1;
                double inc = 1.0/20.0;
                while(k<20) {
                    try {
                        data2.add(new DataPoint(pl, 0));
                        k++;
                        pl += inc;
                        Log.v("After", Integer.toString(k));
                    } catch (Exception e) {
                        Log.v("Loop 1", "failed");
                    }

                }

                while(i<4000){
                    try {

                        int j = 0;

                        double place = 0.0;
                        double increment = 1.0/20.0;
                        //Log.v("Loop 1", "Looping");
                        while(j<20) {
                            try {
                                Thread.sleep(100);

                                data1.add(new DataPoint(place, Math.random()));
                                DataPoint[] dataFin = new DataPoint[data1.size()];
                                dataFin =  (DataPoint[]) data1.toArray(dataFin);

                                series.resetData(dataFin);

                                data2.removeFirst();
                                DataPoint[] dataFin2 = new DataPoint[data2.size()];
                                dataFin2 = (DataPoint[]) data2.toArray(dataFin2);

                                series2.resetData(dataFin2);

                                place += increment;
                                j++;

                            } catch (Exception e) {
                                //Log.v("Loop 2", "failed");
                            }
                        }
                        data2.clear();
                        int l = 0;
                        while(l<20) {
                            try {
                                data2.add(data1.get(l));
                                //Log.v("Loop 3", "here");
                                l++;
                            } catch (Exception e) {
                                Log.v("Loop 3", "failed");
                            }
                        }
                        data1.clear();

                    } catch (Exception e) {
                    }
                    i++;
                }
            }
        });

        //t1.start();



    }

    void findBT() throws IOException
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-06"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {

                final long temp= System.currentTimeMillis();
                heartData = new ArrayList<DataPoint>();
                final int numOfPoints = 300;
                double numOfPointsD = (double) numOfPoints;
                data1 = new LinkedList<DataPoint>();
                data2 = new LinkedList<DataPoint>();
                location = 0;
                location2 = 0;
                int k = 0;
                double pl = 0.0;
                double inc = 1.0/numOfPointsD;
                while(k<numOfPoints) {
                    try {
                        data2.add(new DataPoint(k, 0));
                        k++;
                        pl += inc;
                        //Log.v("After", Integer.toString(k));
                    } catch (Exception e) {
                        Log.v("Setup Data2 Loop", "failed");
                    }

                }
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {

                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {

                                            if(location <numOfPoints) {
                                                double currTime = (System.currentTimeMillis() - temp) / 1000.0;
                                                double currData = Double.parseDouble(data);
                                                DataPoint currPoint = new DataPoint(currTime, currData);

                                                //heartData.add(currPoint);

                                                data1.add(new DataPoint(location, currData));
                                                DataPoint[] dataFin = new DataPoint[data1.size()];
                                                dataFin = (DataPoint[]) data1.toArray(dataFin);

                                                series.resetData(dataFin);

                                                data2.removeFirst();
                                                DataPoint[] dataFin2 = new DataPoint[data2.size()];
                                                dataFin2 = (DataPoint[]) data2.toArray(dataFin2);

                                                series2.resetData(dataFin2);

                                                //Double.parseDouble(data);
                                                //series.appendData(new DataPoint((System.currentTimeMillis() - temp) / 1000.0, Double.parseDouble(data)), true, 300);
                                                myLabel.setText(data);

                                                dataToSend += currTime + ", " + currData + "\n";
                                                location++;
                                            }
                                            else{
                                                location = 0;
                                                 location2++;
                                                if(location2 % 5 ==0 ){
                                                    dataToSend = "";
                                                }
                                                data2.clear();
                                                int l = 0;
                                                for(DataPoint d : data1) {
                                                    try {
                                                        data2.add(d);
                                                        //Log.v("Loop 3", "here");
                                                    } catch (Exception e) {
                                                        Log.v("Loop 3", "failed");
                                                    }
                                                }
                                                data1.clear();

                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException
    {
        String msg = myTextbox.getText().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }
}