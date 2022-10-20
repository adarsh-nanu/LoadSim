package util.crypto;

import util.*;
import org.apache.log4j.Logger;	

public class CVN17 implements Cryptogram
{
	byte[] Record;
	int Used;
	String Pan;
	String PanSeq;
	String AmountAuthorized;
	String UnpredictableNumber;
	String ApplicationTransactionCounter;
	String Mdk;
	String CardVerificationResults;
	String PaymentScheme;
	static Logger logger = Logger.getLogger( util.crypto.CVN17.class);
	
	public CVN17( String Mdk, String Pan, String PanSeq, String AmountAuthorized, String UnpredictableNumber, String CardVerificationResults, String ApplicationTransactionCounter )
	{
		Record = new byte[256];
		Used = 0;
		this.Mdk = Mdk;
		logger.debug( "Mdk " + this.Mdk );
		this.Pan = Pan;
		logger.debug( "Pan " + this.Pan );
		this.PanSeq = PanSeq;
		logger.debug( "PanSeq " + this.PanSeq );
		this.AmountAuthorized = AmountAuthorized;
		logger.debug( "AmountAuthorized " + this.AmountAuthorized );
		this.UnpredictableNumber = UnpredictableNumber;
		logger.debug( "UnpredictableNumber " + this.UnpredictableNumber );
		this.ApplicationTransactionCounter = ApplicationTransactionCounter;
		logger.debug( "ApplicationTransactionCounter " + this.ApplicationTransactionCounter );
		this.CardVerificationResults = CardVerificationResults;
		logger.debug( "CardVerificationResults " + this.CardVerificationResults );
		logger.debug( "OK" );
	}

	public void resetTxnData()
	{
		Record = new byte[256];
		Used = 0;
	}
	
	public byte[] getMAC()
	{
		DataFormatterUtil dfu = new DataFormatterUtil();
		String PanAndSeq = Pan.concat( PanSeq );
		String Data = PanAndSeq.substring( PanAndSeq.length() - 16 );

		byte[] MDK = dfu.hexToBCD( dfu.asciiToHex( Mdk ), Mdk.length() );
		byte[] DATA = dfu.hexToBCD( dfu.asciiToHex( Data ), Data.length() );
		TDes tdesobj = new TDes( MDK, DATA );
		byte[] Zl = tdesobj.encrypt();

		//tracing.log( 1,"---Zl---" );
		for( int i=0;i<Zl.length; i++ )
		{
			//tracing.log( String.format( "%02X ", Zl[i] ) );
		}

		byte[] XZr = new byte[8];
		for( int i=0; i<8; i++ )
			XZr[i] = ( byte ) ( 0xFF ^ DATA[i] );

		tdesobj = new TDes( MDK, XZr );
		byte[] Zr = tdesobj.encrypt();


		//tracing.log( 1,"---Zr---" );
		for( int i=0;i<Zr.length; i++ )
		{
			//tracing.log( String.format( "%02X ", Zr[i] ) );
		}


		byte[] Ck = new byte[16];
		System.arraycopy( Zl, 0, Ck, 0, Zl.length );
		System.arraycopy( Zr, 0, Ck, 8, Zr.length );

		byte[] ATC = new byte[2];
		ATC = dfu.hexToBCD( dfu.asciiToHex( dfu.getPadString( '0', 4 ).substring( ApplicationTransactionCounter.length() ).concat( ApplicationTransactionCounter ) ), 4 );
		//tracing.log( 1, "ATC " + String.format( "%02X ", ATC[0] ) + String.format( "%02X ", ATC[1] ) );

		AmountAuthorized = dfu.getPadString( '0', 12 ).substring( AmountAuthorized.length() ).concat( AmountAuthorized );
		UnpredictableNumber = dfu.getPadString( '0', 8 ).substring( UnpredictableNumber.length() ).concat( UnpredictableNumber );
		//int CVRLength = dfu.hexToInt( IssuerApplicationData.substring(6 ).substring(0, 2) );
		//CardVerificationResults is aready formattedi
		

		logger.debug( "AmountAuthorized " + AmountAuthorized );
		logger.debug( "UnpredictableNumber " + UnpredictableNumber );
		logger.debug( "ApplicationTransactionCounter " + ApplicationTransactionCounter );
		logger.debug( "CVR " + CardVerificationResults );

		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( AmountAuthorized ), AmountAuthorized.length() ) );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( UnpredictableNumber ), UnpredictableNumber.length() ) );
		appendToRecord( ATC );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( CardVerificationResults ), CardVerificationResults.length() ) );
		if( Used % 8 != 0 )
			appendToRecord( new byte[ 8 - ( Used % 8 ) ] );

		byte[] SKl = new byte[8];
		byte[] SKr = new byte[8];
		System.arraycopy( Zl, 0, SKl, 0, Zl.length );
		System.arraycopy( Zr, 0, SKr, 0, Zr.length );
		
		byte[] Hi = new byte[8];
		byte[] Xie = new byte[8];
		for( int i=0; i<Used/8; i++ )
		{
			byte[] Xi = new byte[8];
			System.arraycopy( Record, i*8, Xi, 0, 8 );
			if( i != 0 )
				for( int j=0; j<8; j++ )
					Xi[j] ^= Xie[j];
			tdesobj = new TDes( SKl, Xi );
			Xie = tdesobj.encrypt();

		}
		//tracing.log( 1, "----------ISO 9797-1 Algo 3 " );	

		tdesobj = new TDes( SKr, Xie );
		Hi = tdesobj.decrypt();
/*
		tracing.log( 1, "---before Final---" );
		for( int j=0;j<Hi.length; j++ )
			tracing.log( String.format( "%02X ", Hi[j] ) );
*/
		tdesobj = new TDes( SKl, Hi );
		Hi = tdesobj.encrypt();

/*
		tracing.log( 1,"---Final---" );
		for( int j=0;j<Hi.length; j++ )
			tracing.log( String.format( "%02X ", Hi[j] ) );
*/

		return Hi;

	}

	
    public void appendToRecord( byte[] dataToAppend )
    {
        System.arraycopy( dataToAppend, 0,  Record, Used, dataToAppend.length );
        Used += dataToAppend.length;
    }

    public void appendToRecord( byte dataToAppend )
    {
        Record[Used] = dataToAppend;
        Used++;
    }
}
