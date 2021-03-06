//a version that only receive message.
package de.grundid.hcedemo;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import de.grundid.hcedemo.IsoDepTransceiver.OnMessageReceived;
 


public class MainActivity extends Activity implements OnMessageReceived, ReaderCallback {

private NfcAdapter nfcAdapter;
private ListView listView;
private IsoDepAdapter isoDepAdapter;

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
Intent intent = new Intent(this, MyHostApduService.class);
Log.d("service","nfc runs here");
startService(intent);

nfcAdapter = NfcAdapter.getDefaultAdapter(this);
setContentView(R.layout.activity_main);
listView = (ListView)findViewById(R.id.listView);
isoDepAdapter = new IsoDepAdapter(getLayoutInflater());
listView.setAdapter(isoDepAdapter);

//Tag myTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
////get NDEF tag details
//Ndef ndefTag = Ndef.get(myTag);
//int size = ndefTag.getMaxSize(); // tag size
//boolean writable = ndefTag.isWritable(); // is tag writable?
//String type = ndefTag.getType(); // tag type
//
////get NDEF message details
//NdefMessage ndefMesg = ndefTag.getCachedNdefMessage();
//NdefRecord[] ndefRecords = ndefMesg.getRecords();
//int len = ndefRecords.length;
//String[] recTypes = new String[len]; // will contain the NDEF record
////types
//for (int i = 0; i < len; i++) {
//recTypes[i] = new String(ndefRecords[i].getType());
//}


}

@Override
public void onResume() {
super.onResume();
nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
null);
}

@Override
public void onPause() {
super.onPause();
nfcAdapter.disableReaderMode(this);
}

@Override
public void onTagDiscovered(Tag tag) {
IsoDep isoDep = IsoDep.get(tag);
IsoDepTransceiver transceiver = new IsoDepTransceiver(isoDep, this);
Thread thread = new Thread(transceiver);
thread.start();
}

@Override
public void onMessage(final byte[] message) {
runOnUiThread(new Runnable() {

@Override
public void run() {
isoDepAdapter.addMessage(new String(message));
}
});
}

@Override
public void onError(Exception exception) {
onMessage(exception.getMessage().getBytes());
}
}
