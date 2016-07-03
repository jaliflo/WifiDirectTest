package com.example.franciscojavier.wifidirecttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, WifiP2pManager.ConnectionInfoListener{

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private static boolean server_running = false;
    private WifiP2pDevice device;

    private List peers = new ArrayList();

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersList) {
            peers.clear();
            peers.addAll(peersList.getDeviceList());
            ListView listView = (ListView) findViewById(R.id.listView);
            WiFiPeerListAdapter adapter = (WiFiPeerListAdapter)listView.getAdapter();
            adapter.notifyDataSetChanged();
            if(peers.size()==0){
                Toast.makeText(MainActivity.this, "No se encuentran dispositivos", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new WiFiPeerListAdapter(this, android.R.layout.simple_list_item_1, peers));
        listView.setOnItemClickListener(this);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        Button enviarB = (Button) findViewById(R.id.enviarB);
        enviarB.setVisibility(View.GONE);

        EditText mensaje = (EditText) findViewById(R.id.mensaje);
        mensaje.setVisibility(View.GONE);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    @Override
    protected void onResume(){
        super.onResume();
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, peerListListener);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void isWifiEnabled(boolean isEnabled){
        if(isEnabled){
            Toast.makeText(MainActivity.this, "Wi-Fi is enabled", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Wi-Fi is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void buscarPeer(View view) {
       mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Buscando...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "Fallo de busqueda. WiFi desconectado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connect(int pos){
        final WifiP2pDevice device = (WifiP2pDevice) peers.get(pos);

        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Conectando con "+device.deviceAddress, Toast.LENGTH_SHORT).show();
                Button enviarB = (Button) findViewById(R.id.enviarB);
                EditText mensaje = (EditText) findViewById(R.id.mensaje);
                enviarB.setVisibility(View.VISIBLE);
                mensaje.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Conexion fallida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        connect(position);
        device = (WifiP2pDevice) peers.get(position);
    }

    public void enviarMensaje(View view){

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        ListView listView = (ListView) findViewById(R.id.listView);
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        if(!server_running){
            new ServerAsyncTask(this, listView);
            server_running = true;
        }
    }

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice>{
        private List<WifiP2pDevice> items;

        public WiFiPeerListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects){
            super(context, textViewResourceId, objects);
            items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            if(v==null){
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            WifiP2pDevice device = items.get(position);
            if(device != null){
                TextView top = (TextView) v.findViewById(android.R.id.text1);
                if(top != null){
                    top.setText(device.deviceName);
                }
            }

            return v;
        }
    }

    public static class ServerAsyncTask extends AsyncTask<Void, Void, String>{

        private Context context;
        private ListView listView;

        public ServerAsyncTask(Context context, View chatLog){
            this.context = context;
            listView = (ListView) chatLog;
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                InputStream inputStream = client.getInputStream();
                String mensaje = "";
                byte buff[] = new byte[1024];

                try{
                    while ((inputStream.read(buff))!= -1){
                        mensaje = mensaje + new String(buff, StandardCharsets.UTF_8);
                    }
                }catch (IOException e){
                    return null;
                }

                return mensaje;
            }catch (IOException e){

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result){
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
            adapter.add(result);
        }
    }
}
