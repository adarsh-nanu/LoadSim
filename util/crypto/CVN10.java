package util.crypto;

import util.*;
import org.apache.log4j.Logger;

public class CVN10 implements Cryptogram
{
	byte[] Record;
	int Used;
	String Pan;
	String PanSeq;
	String AmountAuthorized;
	String AmountOther;
	String TerminalCountry;
	String TerminalVerificationResults;
	String TransactionCurrencyCode;
	String TransactionDate;
	String TransactionType;
	String UnpredictableNumber;
	String ApplicationInterchangeProfile;
	String ApplicationTransactionCounter;
	String Mdk;
	String CardVerificationResults;
	static Logger logger = Logger.getLogger( util.crypto.CVN10.class);
	utilities o_utilities;
	
	public CVN10( String Mdk, String Pan, String PanSeq, String AmountAuthorized, String AmountOther, String TerminalCountry, String TerminalVerificationResults, String TransactionCurrencyCode, String TransactionDate, String TransactionType, String UnpredictableNumber, String ApplicationInterchangeProfile, String CardVerificationResults, String ApplicationTransactionCounter )
	{
		o_utilities = new utilities();
		o_utilities.initMessageBuffer( 512 );
		Record = new byte[256];
		Used = 0;
		this.Mdk = Mdk;
		logger.debug( "Mdk " + this.Mdk );
		this.Pan = Pan;
		logger.debug( "Pan " + this.Pan );
		this.PanSeq = PanSeq;
		logger.debug( "PanSeq " + this.PanSeq );
		this.AmountAuthorized = AmountAuthorized;
		this.AmountOther = AmountOther;
		this.TerminalCountry = TerminalCountry;
		this.TerminalVerificationResults = TerminalVerificationResults;
		this.TransactionCurrencyCode = TransactionCurrencyCode;
		this.TransactionDate = TransactionDate;
		this.TransactionType = TransactionType;
		this.UnpredictableNumber = UnpredictableNumber;
		this.ApplicationInterchangeProfile = ApplicationInterchangeProfile;
		this.CardVerificationResults = CardVerificationResults;
		this.ApplicationTransactionCounter = ApplicationTransactionCounter;
		logger.debug( "OK" );
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

		byte[] XZr = new byte[8];
		for( int i=0; i<8; i++ )
			XZr[i] = ( byte ) ( 0xFF ^ DATA[i] );

		tdesobj = new TDes( MDK, XZr );
		byte[] Zr = tdesobj.encrypt();

		byte[] Ck = new byte[16];
		System.arraycopy( Zl, 0, Ck, 0, Zl.length );
		System.arraycopy( Zr, 0, Ck, 8, Zr.length );

		o_utilities.printhexdump( Ck, "Ck" );

		byte[] ATC = new byte[2];
		ATC = dfu.hexToBCD( dfu.asciiToHex( dfu.getPadString( '0', 4 ).substring( ApplicationTransactionCounter.length() ).concat( ApplicationTransactionCounter ) ), 4 );
		//tracing.log( 1, "ATC " + String.format( "%02X ", ATC[0] ) + String.format( "%02X ", ATC[1] ) );

		AmountAuthorized = dfu.getPadString( '0', 12 ).substring( AmountAuthorized.length() ).concat( AmountAuthorized );
		AmountOther = dfu.getPadString( '0', 12 ).substring( AmountOther.length() ).concat( AmountOther );
		TerminalCountry = dfu.getPadString( '0', 4 ).substring( TerminalCountry.length() ).concat( TerminalCountry );
		TerminalVerificationResults = dfu.getPadString( '0', 10 ).substring( TerminalVerificationResults.length() ).concat( TerminalVerificationResults );
		TransactionCurrencyCode = dfu.getPadString( '0', 4 ).substring( TransactionCurrencyCode.length() ).concat( TransactionCurrencyCode );
		TransactionDate = dfu.getPadString( '0', 6 ).substring( TransactionDate.length() ).concat( TransactionDate );
		TransactionType = dfu.getPadString( '0', 2 ).substring( TransactionType.length() ).concat( TransactionType );
		UnpredictableNumber = dfu.getPadString( '0', 8 ).substring( UnpredictableNumber.length() ).concat( UnpredictableNumber );
		ApplicationInterchangeProfile = dfu.getPadString( '0', 4 ).substring( ApplicationInterchangeProfile.length() ).concat( ApplicationInterchangeProfile );
		
		logger.debug( "AmountAuthorized " + AmountAuthorized );
	        logger.debug( "AmountOther " + AmountOther );
		logger.debug( "TerminalCountry " + TerminalCountry );
		logger.debug( "TerminalVerificationResults " + TerminalVerificationResults );
		logger.debug( "TransactionCurrencyCode " + TransactionCurrencyCode );
		logger.debug( "TransactionDate " + TransactionDate );
		logger.debug( "TransactionType " + TransactionType );
		logger.debug( "UnpredictableNumber " + UnpredictableNumber );
		logger.debug( "ApplicationInterchangeProfile " + ApplicationInterchangeProfile );
		logger.debug( "ApplicationTransactionCounter " + ApplicationTransactionCounter );
		logger.debug( "CVR " + CardVerificationResults );

		int CDOLLength = ( AmountAuthorized.length() + AmountOther.length() + TerminalCountry.length() + TerminalVerificationResults.length() + TransactionCurrencyCode.length() + TransactionDate.length() + TransactionType.length() + UnpredictableNumber.length() + ApplicationInterchangeProfile.length() + ApplicationTransactionCounter.length() + CardVerificationResults.length() ) / 2;
		CDOLLength += ( 8 - CDOLLength%8 );
		logger.debug( "Size a " + CDOLLength );
		Record = new byte[CDOLLength];
		o_utilities.initMessageBuffer( CDOLLength );

		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( AmountAuthorized ), AmountAuthorized.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( AmountOther ), AmountOther.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( TerminalCountry ), TerminalCountry.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( TerminalVerificationResults ), TerminalVerificationResults.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( TransactionCurrencyCode ), TransactionCurrencyCode.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( TransactionDate ), TransactionDate.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( TransactionType ), TransactionType.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( UnpredictableNumber ), UnpredictableNumber.length() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( ApplicationInterchangeProfile ), ApplicationInterchangeProfile.length() ) );
		o_utilities.appendToRecord( ATC );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( CardVerificationResults ), CardVerificationResults.length() ) );
		//if( o_utilities.getMessageSize() % 8 != 0 )
		//	o_utilities.appendToRecord( new byte[ 8 - ( o_utilities.getMessageSize() % 8 ) ] );

		//Used = o_utilities.getMessageSize();
		//logger.debug( "Size " + Used );
		//byte[] Record = new byte[ BufferSize ];
		//System.arraycopy( o_utilities.getCurrentBuffer(), 0, Record, 0, Used );
		//Record = new byte[CDOLLength];
		Record = o_utilities.getCurrentBuffer();
		Used = Record.length;
		o_utilities.printhexdump( Record, "Record" );

		byte[] SKl = new byte[8];
		byte[] SKr = new byte[8];
		System.arraycopy( Zl, 0, SKl, 0, Zl.length );
		System.arraycopy( Zr, 0, SKr, 0, Zr.length );

		byte[] Hi = new byte[8];
		byte[] Xie = new byte[8];
		for( int i=0; i < Used/8; i++ )
		{
			byte[] Xi = new byte[8];
			System.arraycopy( Record, i*8, Xi, 0, 8 );
			if( i != 0 )
				for( int j=0; j<8; j++ )
					Xi[j] ^= Xie[j];
			tdesobj = new TDes( SKl, Xi );
			Xie = tdesobj.encrypt();

		}

		tdesobj = new TDes( SKr, Xie );
		Hi = tdesobj.decrypt();

		tdesobj = new TDes( SKl, Hi );
		Hi = tdesobj.encrypt();

		return Hi;
	}
}
