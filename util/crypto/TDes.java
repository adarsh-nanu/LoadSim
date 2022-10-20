package util.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import adssim.util.DataFormatterUtil;
import org.apache.log4j.Logger;
import util.*;

/**
 * Created by adarsh on 16/08/18.
*/

public class TDes 
{
	DataFormatterUtil dfu;
	Cipher cipher = null;
	byte[] keyBCD;
	byte[] eData;
	byte[] Data;
	String Algo;
	static Logger logger = Logger.getLogger( adssim.util.crypto.TDes.class);
	utilities o_utilities;

	public TDes( byte[] key, byte[] data)
	{
		o_utilities = new utilities();
		o_utilities.printhexdump( key, "Key" );
		o_utilities.printhexdump( data, "data" );
		keyBCD = new byte[ key.length ];
		System.arraycopy( key, 0, keyBCD, 0, key.length );
		Data = new byte[data.length];
		System.arraycopy( data, 0, Data, 0, data.length );

		if( keyBCD.length == 8 )
			Algo = "DES";
		else
			Algo = "DESede";
		logger.debug( Algo );
	}

	public TDes( String key, byte[] data )
	{
		dfu = new DataFormatterUtil();
		Data = new byte[ data.length ];
		System.arraycopy( data, 0, Data, 0, data.length );
		keyBCD = dfu.hexToBCD( dfu.asciiToHex( key ), key.length() );
		if( keyBCD.length == 8 )
			Algo = "DES";
		else
			Algo = "DESede";
		o_utilities = new utilities();
	}

	public byte[] encrypt()
	{
		try
		{
			cipher = Cipher.getInstance( Algo + "/ECB/NoPadding");
			logger.debug( Algo + "/ECB/NoPadding" );
		} 
		catch (Exception E) 
		{
			logger.debug( "Cipher.getInstance Exception : " + E);
		}
		Key key = new SecretKeySpec(keyBCD, Algo );
		try 
		{
			o_utilities.printhexdump( keyBCD, "Key" );
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} 
		catch (Exception E) 
		{
			logger.debug( "Cipher.init Exception : " + E);
		}

		try 
		{
			eData = cipher.doFinal(Data);
		} 
		catch (Exception E) 
		{
			logger.debug( "Cipher dofinal Exception : " + E);
		}
		return eData;
	}

	public byte[] decrypt()
	{
		try
		{
			cipher = Cipher.getInstance( Algo + "/ECB/NoPadding");
		} 
		catch (Exception E) 
		{
			logger.debug( "Cipher.getInstance Exception : " + E);
		}
		Key key = new SecretKeySpec(keyBCD, Algo );
		try 
		{
			cipher.init(Cipher.DECRYPT_MODE, key);
		} 
		catch (Exception E) 
		{
			logger.debug( "Cipher.init Exception : " + E);
		}

		try 
		{
			eData = cipher.doFinal(Data);
		} 
		catch (Exception E) 
		{
			logger.debug( "Cipher dofinal Exception : " + E);
		}
		return eData;
	}
}
