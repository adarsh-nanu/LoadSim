package util.crypto;

import util.*;
import org.apache.log4j.Logger;

public class ANSIPin{
	static Logger logger = Logger.getLogger( util.crypto.ANSIPin.class );
	public byte[] PINBlock = new byte[8];
	DataFormatterUtil dfu = null;
	byte[] Pan = new byte[0];
	byte[] Pin = new byte[0];

	public ANSIPin()
	{
		dfu = new DataFormatterUtil();
	}

	public byte[] format0(String pan, String pin) {
        logger.debug( "Pan [" + pan + "] PIN [ " + pin + " ]");
        char[] panLast12 = "0000000000000000".toCharArray();

        System.arraycopy(pan.substring(pan.length() - 13).toCharArray(), 0, panLast12, 4, 12);
        byte[] panLast12BCD = dfu.hexToBCD(dfu.asciiToHex(new String(panLast12)), 16);

        char[] PINLength = ("00".substring(0, 2 - String.valueOf(pin.length()).length()) + String.valueOf(pin.length())).toCharArray();

        char[] LLPin = new char[16];
        System.arraycopy(PINLength, 0, LLPin, 0, 2);
        System.arraycopy(pin.toCharArray(), 0, LLPin, 2, pin.length());
        for (int i = 2 + pin.length(); i < LLPin.length; i++)
            LLPin[i] = 15;

        byte[] LLPINFF = dfu.hexToBCD(dfu.asciiToHex(new String(LLPin)), 16);

        byte[] clearPINBlock = new byte[8];
        for (int i = 0; i < 8; i++) {
            clearPINBlock[i] = (byte) (panLast12BCD[i] ^ LLPINFF[i]);
        }
        return clearPINBlock;
    }
}
