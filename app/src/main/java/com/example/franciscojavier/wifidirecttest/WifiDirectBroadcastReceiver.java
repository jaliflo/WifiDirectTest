package com.example.franciscojavier.wifidirecttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FRANCISCOJAVIER on 16/05/2016.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener infoListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity, WifiP2pManager.PeerListListener peerListListener, WifiP2pManager.ConnectionInfoListener infoListener){
        super();
        mManager = manager;
        mChannel = channel;
        mActivity = activity;
        this.peerListListener = peerListListener;
        this.infoListener = infoListener;
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                mActivity.isWifiEnabled(true);
            }else{
                mActivity.isWifiEnabled(false);
            }
        }

        if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(mManager != null){
                mManager.requestPeers(mChannel, peerListListener);
            }
        }

        if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()){
                mManager.requestConnectionInfo(mChannel, infoListener);
            }
        }
    }
}
