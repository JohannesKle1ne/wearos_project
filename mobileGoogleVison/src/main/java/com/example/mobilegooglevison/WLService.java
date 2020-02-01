package com.example.mobilegooglevison;

import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class WLService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Toast.makeText(this, "Received message", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPeerConnected(Node peer) {
        Toast.makeText(this, "Peer connected", Toast.LENGTH_LONG).show();
    }

}
