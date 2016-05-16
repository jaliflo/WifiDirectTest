package com.example.franciscojavier.wifidirecttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.Toast;

/**
 * Created by FRANCISCOJAVIER on 16/05/2016.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    public WifiP2pManager.PeerListListener peerListListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity){
        super();
        mManager = manager;
        mChannel = channel;
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(context, "WiFi is enabled", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "WiFi is disabled", Toast.LENGTH_SHORT).show();
            }
        }

        if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(mManager != null){
                mManager.requestPeers(mChannel, peerListListener);
            }
        }
    }
}
