package de.grundid.hcedemo;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import java.util.Arrays;

public class MyHostApduService extends HostApduService {
	private int messageCounter = 0;

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		String str = new String(apdu);
		Log.i("commandTag", "Received APDU: " + str);
		if (selectAidApdu(apdu)) {
			Log.i("HCEDEMO", "Application selected");
			return getWelcomeMessage();
		}
		else {
			Log.i("HCEDEMO", "Received: " + new String(apdu));
			return getNextMessage();
		}
	}

	private byte[] getWelcomeMessage() {
		return "Hello Desktop!".getBytes();
	}

	private byte[] getNextMessage() {
		return ("Message from Android Device: " + messageCounter++).getBytes();
	}

	private boolean selectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4;
	}

	@Override
	public void onDeactivated(int reason) {
		Log.i("HCEDEMO", "Deactivated: " + reason);
	}
}