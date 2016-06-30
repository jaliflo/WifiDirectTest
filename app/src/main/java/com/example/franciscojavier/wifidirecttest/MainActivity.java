package com.example.franciscojavier.wifidirecttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

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

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);


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
                Toast.makeText(MainActivity.this, "Fallo de busqueda", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connect(){
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Exito en la conexion", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Conexion fallida", Toast.LENGTH_SHORT).show();
            }
        });
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
}
