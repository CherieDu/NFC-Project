package it.ismb.nfc.sneppusher;

import java.nio.charset.Charset;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.Time;

public class SnepPusher extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback{
	
	NfcAdapter mNfcAdapter;
	TextView mInfoText;
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case 1:
	                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
	                break;
	        }
        }
	};

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snep_pusher);
  
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            mInfoText = (TextView) findViewById(R.id.textView3);
            mInfoText.setText("NFC is not available on this device.");
        }
        
        // Register callback to set NDEF message
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_snep_pusher, menu);
        return true;
    }

	@Override
	public void onNdefPushComplete(NfcEvent arg0) {		
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(1).sendToTarget();
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {		
		Time time = new Time();
        time.setToNow();
        String text = ("Beam me up!\n\n" + "Beam Time: " + time.format("%H:%M:%S"));
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMimeRecord(
                        "application/com.example.android.beam", text.getBytes())});
        return msg;	
	}

	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
	}
    
	void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        mInfoText.setText(new String(msg.getRecords()[0].getPayload()));
	}

	
}
