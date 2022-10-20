package channel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.io.HexDump;
import adssim.util.*;
import adssim.util.crypto.*;
import oracle.jdbc.driver.*;
import java.sql.*;

class NAPS
{
	static Logger logger = Logger.getLogger( adssim.channel.NAPS.class );
	utilities o_utilities;
	DataFormatterUtil dfu;
	ManageBitmap manageBM;
	byte MsgType[];
	byte PrimaryBitmap[];
	byte SecondaryBitmap[];
	int ResponseDataLength;

	public NAPS()
	{
		o_utilities = new utilities();
		PropertyConfigurator.configure("log4j.properties");
		logger.info("parent class print start");
		dfu = new DataFormatterUtil();
		manageBM = new ManageBitmap();
		ResponseDataLength = 0;
		MsgType = new byte[2];
		PrimaryBitmap = new byte[8];
		SecondaryBitmap = new byte[8];
		o_utilities.connectDB( "naps.cfg" );
		o_utilities.ConnectSwitch( "naps.cfg" );
		o_utilities.initISOFields( "naps.cfg" );
		o_utilities.initCard( "naps.cfg" );
		o_utilities.initTerminal( "naps.cfg" );
		o_utilities.initChip( "naps.cfg" );
		logger.info( "OK " + this.getClass().getName() );
	}

	public void parseResponse()
	{

		byte[] PCODE = new byte[6];
		byte[] AMOUNT = new byte[12];
		byte[] GMT = new byte[10];
		byte[] TRACE = new byte[6];
		byte[] LOCAL_TIME = new byte[6];
		byte[] LOCAL_DATE = new byte[4];
		byte[] EXP_DATE = new byte[4];
		byte[] SETTL_DATE = new byte[4];
		byte[] CAP_DATE = new byte[4];
		byte[] MCC = new byte[4];
		byte[] PEM = new byte[3];
		byte[] CARD_SEQ = new byte[3];
		byte[] acqidlen = new byte[2];
		byte[] ACQID= new byte[11];
		byte[] tracklen = new byte[2];
		byte[] TRACKII = new byte[37];
		byte[] RRN = new byte[12];
		byte[] AUTHNUM = new byte[6];
		byte[] RESPCODE = new byte[2];
		byte[] TERMID = new byte[16];
		byte[] MERID = new byte[15];
		byte[] de44len = new byte[2];
		byte[] DE55Len = new byte[3];
		byte[] DE44;
		byte[] de48len = new byte[3];
		byte[] DE48;
		byte[] CURRENCY = new byte[3];
		byte[] DE57Len = new byte[3];
		byte[] DE57;
		byte[] DE60;
		byte[] DE60Len = new byte[3];
		byte[] DE61;
		byte[] DE61Len = new byte[3];
		byte[] DE100;
		byte[] DE100Len = new byte[2];
		byte[] DE125;
		byte[] DE125Len = new byte[3];

		int secondaryBitmapLen = 0;
		byte[] Header = new byte[12];

		o_utilities.getISOFields( Header );
		logger.info( "header " + new String( dfu.byteToChar( Header ) ) );

		MsgType = new byte[4];
		o_utilities.getISOFields( MsgType );
		logger.info( "Msg type " + new String( dfu.byteToChar( MsgType ) ) );

		byte[] asciiBitMap = new byte[16];
		o_utilities.getISOFields( asciiBitMap );
		PrimaryBitmap = dfu.hexToBCD( asciiBitMap );
		o_utilities.printhexdump( PrimaryBitmap, "Primary Bitmap data" );
		int BitmapSerial = 1;

		if( manageBM.checkIsBitOn( PrimaryBitmap, 1 ) )
		{
			logger.info( "Secondary Bitmap available" );
			asciiBitMap = new byte[16];
			o_utilities.getISOFields( asciiBitMap );
			SecondaryBitmap = dfu.hexToBCD( asciiBitMap );
			o_utilities.printhexdump( SecondaryBitmap, "Secondary Bitmap data" );
			BitmapSerial++;
		}
		else
			logger.debug( "Secondary Bitmap unavailable" );

		ManageBitmap mb = new ManageBitmap();
		byte[] BitMap = new byte[8*BitmapSerial];
		System.arraycopy( PrimaryBitmap, 0, BitMap, 0, 8 );
		if( BitmapSerial >1 )
			System.arraycopy( SecondaryBitmap, 0, BitMap, 8, 8 );
		
		int pos = 0;
		for( int i=2; i<BitmapSerial*64; i++ )
		{
			if( mb.checkIsBitOn( BitMap, i ) )
				switch(i)
				{
					case 2:
						logger.debug( "Card available" );
						break;
					case 3:
						o_utilities.getISOFields( PCODE, "PCODE" );
						logger.info( "Pcode " + new String( dfu.byteToChar( PCODE ) ) );
						break;
					case 4:
						o_utilities.getISOFields( AMOUNT, "AMOUNT" );
						logger.info( "Amount " + new String( dfu.byteToChar( AMOUNT ) ) );
						break;
					case 7:
						o_utilities.getISOFields( GMT, "GMT" );
						logger.info( "GMT " +  new String( dfu.byteToChar( GMT ) ) );
						break;
					case 11:
						o_utilities.getISOFields( TRACE, "TRACE" );
						logger.info( "Trace " + new String( dfu.byteToChar( TRACE ) ) );
						break;
					case 12:
						o_utilities.getISOFields( LOCAL_TIME, "LOCAL_TIME" );
						logger.info( "Local time " + new String( dfu.byteToChar( LOCAL_TIME ) ) );
						break;
					case 13:
						o_utilities.getISOFields( LOCAL_DATE, "LOCAL_DATE" );
						logger.info("Local date " + new String( dfu.byteToChar( LOCAL_DATE ) ) );
						break;
					case 14:
						o_utilities.getISOFields( EXP_DATE, "EXP_DATE" );
						logger.info("Expiry Date " + new String( dfu.byteToChar( EXP_DATE ) ) );
						break;
					case 15:
						o_utilities.getISOFields( SETTL_DATE, "SETTL_DATE" );
						logger.info("Settlement Date " + new String( dfu.byteToChar( SETTL_DATE ) ) );
						break;
					case 17:
						o_utilities.getISOFields( CAP_DATE, "CAP_DATE" );
						logger.info("Cap Date " + new String( dfu.byteToChar( CAP_DATE ) ) );
						break;
					case 18:
						MCC = new byte[4];
						o_utilities.getISOFields( MCC, "MCC" );
						logger.info("Mcc " + new String( dfu.byteToChar( MCC ) ) );
						break;
					case 22:
						o_utilities.getISOFields( PEM, "PEM" );
						logger.info("Pem " + new String( dfu.byteToChar( PEM ) ) );
						break;
					case 23:
						o_utilities.getISOFields( CARD_SEQ, "CARD_SEQ" );
						logger.info( "Card Seq " + new String( dfu.byteToChar( CARD_SEQ ) ) );
						break;
					case 32:
						o_utilities.getISOFields( acqidlen, "acqidlen" );
						logger.info( "Length : " + Integer.parseInt( new String( dfu.byteToChar( acqidlen ) ) ) );
						ACQID = new byte[ Integer.parseInt( new String( dfu.byteToChar( acqidlen ) ) ) ];
						o_utilities.getISOFields( ACQID, "ACQID" );
						logger.info( "Acq Id " + new String( dfu.byteToChar( ACQID ) ) );
						break;
					case 35:
						o_utilities.getISOFields( tracklen, "tracklen" );
						logger.info( "Length : " + Integer.parseInt( new String( dfu.byteToChar( tracklen ) ) ) );
						TRACKII = new byte[ Integer.parseInt( new String( dfu.byteToChar( tracklen ) ) ) ];
						o_utilities.getISOFields( TRACKII, "TRACKII" );
						logger.info("Track II " +  new String( dfu.byteToChar( TRACKII ) ) );
						break;
					case 37:
						o_utilities.getISOFields( RRN, "RRN" );
						logger.info( "RRN " + new String( dfu.byteToChar( RRN ) ) );
						break;
					case 38:
						o_utilities.getISOFields( AUTHNUM, "AUTHNUM" );
						logger.info("AUTHNUM " +  new String( dfu.byteToChar( AUTHNUM ) ) );
						break;
					case 39:
						o_utilities.getISOFields( RESPCODE, "RESPCODE" );
						logger.info("Respcode " + new String( dfu.byteToChar( RESPCODE ) ) );
						break;
					case 41:
						o_utilities.getISOFields( TERMID, "TERMID" );
						logger.info("tERMID " + new String( dfu.byteToChar( TERMID ) ) );
						break;
					case 42:
						o_utilities.getISOFields( MERID, "MERID" );
						logger.info("MER ID " + new String( dfu.byteToChar( MERID ) ) );
						break;
					case 44:
						o_utilities.getISOFields( de44len, "de44len" );
						logger.info( "Length : " + Integer.parseInt( new String( dfu.byteToChar( de44len ) ) ) );
						DE44 = new byte[ Integer.parseInt( new String( dfu.byteToChar( de44len ) ) ) ];
						o_utilities.getISOFields( DE44, "DE44" );
						logger.info( "DE44 " + new String( dfu.byteToChar( DE44 ) ) );
						break;
					case 48:
						logger.info( "DE48" );
						o_utilities.getISOFields( de48len, "de48len" );
						logger.info( "Length : " + Integer.parseInt( new String( dfu.byteToChar( de48len ) ) ) );
						DE48 = new byte[ Integer.parseInt( new String( dfu.byteToChar( de48len ) ) ) ];
						o_utilities.getISOFields( DE48, "DE48" );
						logger.info("DE48 " + new String( dfu.byteToChar( DE48 ) ) );
						break;
					case 49:
						logger.info( "CURR" );
						o_utilities.getISOFields( CURRENCY, "CURRENCY" );
						logger.info( "CURR " + new String( dfu.byteToChar( CURRENCY ) ) );
						break;
					case 55:
						logger.info( "Chip Data" );
						o_utilities.getISOFields( DE55Len, "DE55Len" );
						logger.info( "DE55 Length " + Integer.parseInt( new String( dfu.byteToChar( DE55Len ) ) ) );
						byte[] DE55 = new byte[ Integer.parseInt( new String( dfu.byteToChar( DE55Len ) ) ) ];
						o_utilities.getISOFields( DE55, "DE55" );
						break;
					case 57:
						logger.info( "De57" );
						o_utilities.getISOFields( DE57Len, "DE57Len" );
						logger.info( "DE57 Length " + Integer.parseInt( new String( dfu.byteToChar( DE57Len ) ) ) );
						DE57 = new byte[ Integer.parseInt( new String( dfu.byteToChar( DE57Len ) ) ) ];
						o_utilities.getISOFields( DE57, "DE57" );
						break;
					case 60:
						logger.info( "De60" );
						o_utilities.getISOFields( DE60Len, "DE60Len" );
						logger.info( "Length : " + Integer.parseInt( new String( dfu.byteToChar( DE60Len ) ) ) );
						DE60 = new byte[ Integer.parseInt( new String( dfu.byteToChar( DE60Len ) ) ) ];
						o_utilities.getISOFields( DE60, "DE60" );
						break;
					case 61:
						logger.info( "De61" );
						o_utilities.getISOFields( DE61Len, "DE61Len" );
						logger.info( "DE61 Length " + Integer.parseInt( new String( dfu.byteToChar( DE61Len ) ) ) );
						DE61 = new byte[ Integer.parseInt( new String( dfu.byteToChar( DE61Len ) ) ) ];
						o_utilities.getISOFields( DE61, "DE61" );
						break;
					case 90:
						logger.info( "De90" );
						byte[] DE90 = new byte[ 42 ];
						o_utilities.getISOFields( DE90, "DE90" );
						break;
					case 100:
						logger.info( "De100" );
						o_utilities.getISOFields( DE100Len, "DE100Len" );
						logger.info( "DE100 Length " + Integer.parseInt( new String( dfu.byteToChar( DE100Len ) ) ) );
						DE100 = new byte[ Integer.parseInt( new String( dfu.byteToChar( DE100Len ) ) ) ];
						o_utilities.getISOFields( DE100, "DE100" );
						break;
					case 125:
						logger.info( "De125" );
						o_utilities.getISOFields( DE125Len, "DE125Len" );
						logger.info( "DE125 Length " + Integer.parseInt( new String( dfu.byteToChar( DE125Len ) ) ) );
						DE125 = new byte[ Integer.parseInt( new String( dfu.byteToChar( DE125Len ) ) ) ];
						o_utilities.getISOFields( DE125, "DE125" );
						break;
				}
		}

		boolean ReversalRequired = false;
		logger.info( "1 msgtype " + new String( dfu.byteToChar( MsgType ) ).equals("0210") );
		logger.info( "2 respcode " + new String( dfu.byteToChar( RESPCODE ) ).equals( "00" ) );
		logger.info( "3 pcode 00 " + new String( dfu.byteToChar( PCODE ) ).substring(0, 2).equals( "00" ) );
		logger.info( "4 pcode 01 " + new String( dfu.byteToChar( PCODE ) ).substring(0, 2).equals( "01" ) );
		logger.info( "5 ForceReversal " + o_utilities.isForceReversal() );
		logger.info( "6 RandomReversal " + o_utilities.isRandomReversal() );

		if( new String( dfu.byteToChar( MsgType ) ).equals("0210") & new String( dfu.byteToChar( RESPCODE ) ).equals( "00" ) & ( new String( dfu.byteToChar( PCODE ) ).substring(0, 2).equals( "00" ) | new String( dfu.byteToChar( PCODE ) ).substring(0, 2).equals( "01" ) ) )
		{
			if( o_utilities.isForceReversal() == true )
			{
				logger.info( "Force decision to send reversal" );
				ReversalRequired = true;
			}
			else if( o_utilities.isRandomReversal() == true )
			{
				logger.info( "Random decision to send reversal" );
				ReversalRequired = o_utilities.isReversalRequired();
			}
		}

		if( ReversalRequired == true )
		{
			o_utilities.initMessageBuffer();
			ReversalRequired = false;
			int msglength_pos = 0;
			o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0000" ), 4 ) );
			o_utilities.appendToRecord( dfu.charToByte( "ISO026000070".toCharArray() ) );
			int bitmap_pos = o_utilities.appendToRecord( dfu.charToByte( "0420".toCharArray() ) );
			byte[] bitmap = new byte[16];
			o_utilities.appendToRecord( dfu.charToByte( dfu.getPadString( '0', bitmap.length*2 ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 1 );
			
			o_utilities.appendToRecord( PCODE );
			manageBM.setBitOn( bitmap, 3 );
			
			o_utilities.appendToRecord( AMOUNT );
			manageBM.setBitOn( bitmap, 4 );
			
			o_utilities.appendToRecord( GMT );
			manageBM.setBitOn( bitmap, 7 );

			o_utilities.appendToRecord( TRACE );
			manageBM.setBitOn( bitmap, 11 );
			
			o_utilities.appendToRecord( LOCAL_TIME );
			manageBM.setBitOn( bitmap, 12 );
			
			o_utilities.appendToRecord( LOCAL_DATE );
			manageBM.setBitOn( bitmap, 13 );
			
			o_utilities.appendToRecord( EXP_DATE );
			manageBM.setBitOn( bitmap, 14 );
			
			o_utilities.appendToRecord( SETTL_DATE );
			manageBM.setBitOn( bitmap, 15 );

			o_utilities.appendToRecord( CAP_DATE );
			manageBM.setBitOn( bitmap, 17 );
			
			o_utilities.appendToRecord( PEM );
			manageBM.setBitOn( bitmap, 22 );
			
			o_utilities.appendToRecord( dfu.charToByte( String.valueOf( ACQID.length ).toCharArray() ) );
			o_utilities.appendToRecord( ACQID );
			manageBM.setBitOn( bitmap, 32 );
			
			o_utilities.appendToRecord( dfu.charToByte( String.valueOf( TRACKII.length ).toCharArray() ) );
			o_utilities.appendToRecord( TRACKII );
			manageBM.setBitOn( bitmap, 35 );
			
			o_utilities.appendToRecord( RRN );
			manageBM.setBitOn( bitmap, 37 );

			o_utilities.appendToRecord( AUTHNUM );
			manageBM.setBitOn( bitmap, 38 );

			o_utilities.appendToRecord( RESPCODE );
			manageBM.setBitOn( bitmap, 39 );
			
			o_utilities.appendToRecord( TERMID );
			manageBM.setBitOn( bitmap, 41 );
			
			if( o_utilities.isWithdrawal() == false )
			{
					o_utilities.appendToRecord( MERID );
					manageBM.setBitOn( bitmap, 42 );
			}
			
			o_utilities.appendToRecord( dfu.charToByte( o_utilities.getAcceptorName().toCharArray() ) );
			manageBM.setBitOn( bitmap, 43 );
			
			o_utilities.appendToRecord( dfu.charToByte( ( "027" + dfu.getPadString( '0', 27 ) ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 48 );
			
			o_utilities.appendToRecord( CURRENCY );
			manageBM.setBitOn( bitmap, 49 );
			
			o_utilities.appendToRecord( dfu.charToByte( o_utilities.getDE60().toCharArray() ) );
			manageBM.setBitOn( bitmap, 60 );
			
			o_utilities.appendToRecord( dfu.charToByte( o_utilities.getDE61().toCharArray() ) );
			manageBM.setBitOn( bitmap, 61 );

			o_utilities.appendToRecord( dfu.charToByte( "0200".toCharArray() ) );
			o_utilities.appendToRecord( RRN );
			o_utilities.appendToRecord( LOCAL_DATE );
			o_utilities.appendToRecord( LOCAL_TIME );
			o_utilities.appendToRecord( CAP_DATE );
			o_utilities.appendToRecord( dfu.charToByte( "0000000000".toCharArray() ) );
			manageBM.setBitOn( bitmap, 90 );
			
			o_utilities.appendToRecord( dfu.charToByte( "1163404000000".toCharArray() ) );
			manageBM.setBitOn( bitmap, 100 );
			
			o_utilities.appendToRecord( dfu.charToByte( o_utilities.getDE61().toCharArray() ) );
			manageBM.setBitOn( bitmap, 125 );
			
			byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.intToHex( o_utilities.getMessageSize() - 2) ), 4 );
			o_utilities.appendToRecord( hexaLength, 0 );

			byte[] BITMAP = dfu.BCDToHex( bitmap );
			o_utilities.appendToRecord( BITMAP, bitmap_pos );
			//tracing.hexDump( "Request", Record, Used );
			o_utilities.send();
			o_utilities.receive();
        	}
	}

	void prepareMsg()
	{
		logger.debug( "Start Preparing message" );
		o_utilities.updateTransactionData();
		o_utilities.initMessageBuffer();
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0000" ), 4 ) );
		o_utilities.appendToRecord( dfu.charToByte( "ISO026000070".toCharArray() ) );
		int bitmap_pos = o_utilities.appendToRecord( dfu.charToByte( o_utilities.getMsgType().toCharArray() ) );
		byte[] bitmap = new byte[16];
		o_utilities.appendToRecord( dfu.charToByte( dfu.getPadString( '0', bitmap.length*2 ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 1 );

		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getPcode().toCharArray() ) );
		manageBM.setBitOn( bitmap, 3 );

		logger.debug( "Pcode " + o_utilities.getPcode() );
		if( !o_utilities.getPcode().substring(0, 2).equals( "31" ) )
		{
			o_utilities.appendToRecord( dfu.charToByte( dfu.getPadString( '0', 12 ).substring( o_utilities.getAmountAuthorized().length() ).concat( o_utilities.getAmountAuthorized() ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 4 );
			logger.debug( "Amount " + o_utilities.getAmountAuthorized() );
		}

		DateFormatter gmtdate = new DateFormatter( "MMdd", "UTC" );
		o_utilities.appendToRecord( dfu.charToByte( gmtdate.getFormattedDate().toCharArray() ) );
		gmtdate = new DateFormatter( "HHmmss" );
		o_utilities.appendToRecord( dfu.charToByte( gmtdate.getFormattedDate().toCharArray() ) );
		manageBM.setBitOn( bitmap, 7 );
		String Trace = o_utilities.getTrace();
		o_utilities.appendToRecord( dfu.charToByte( dfu.leftPad( Trace, 6, '0' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 11 );

		DateFormatter date = new DateFormatter( "HHmmss" );
		o_utilities.appendToRecord( dfu.charToByte( date.getFormattedDate().toCharArray() ) );
		manageBM.setBitOn( bitmap, 12 );

		date = new DateFormatter( "MMdd" );
		o_utilities.appendToRecord( dfu.charToByte( date.getFormattedDate().toCharArray() ) );
		manageBM.setBitOn( bitmap, 13 );

		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getExpiryDate().toCharArray() ) );
		manageBM.setBitOn( bitmap, 14 );

		o_utilities.appendToRecord( dfu.charToByte( date.getFormattedDate().toCharArray() ) );
		manageBM.setBitOn( bitmap, 17 );

		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getMCC().toCharArray() ) );
		manageBM.setBitOn( bitmap, 18 );

		o_utilities.appendToRecord( dfu.charToByte( dfu.leftPad( o_utilities.getPosEntryMode(), 3, '0' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 22 );

		if( Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 5 || Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 7 )
		{
			o_utilities.appendToRecord( dfu.charToByte( dfu.leftPad( o_utilities.getApplicationPrimaryAccountNumberSequenceNumber(), 3, '0' ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 23 );
		}

		o_utilities.appendToRecord( dfu.charToByte( String.valueOf( o_utilities.getAcquirerId().length()).toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getAcquirerId().toCharArray() ) );
		manageBM.setBitOn( bitmap, 32 );
		if( o_utilities.getPosEntryMode().equals( "11" ) && !o_utilities.getAcquirerId().equals( "63493000000" ) || o_utilities.getAcquirerId().equals( "63493000000" ) && !o_utilities.getPosEntryMode().equals( "11" ) )
		{
			logger.error( "DE22=11 but DE32 is not 63493000000 or DE32 is 63493000000 but DE22 is not 11" );
			return;
		}

		//if( PosEntryMode.equals( "11" ) && TrackII.length() > 21 )
		//{
		//	logger.info( 1, "QPay Transaction cannot carry a complete track 2" );
		//	return;
		//}
		
		o_utilities.appendToRecord( dfu.charToByte( String.valueOf( o_utilities.getTrackII().length() ).toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getTrackII().toCharArray() ) );
		manageBM.setBitOn( bitmap, 35 );

		o_utilities.appendToRecord( dfu.charToByte( ( "TEST" + dfu.leftPad( Trace, 8, '0' )).toCharArray() ) );
		manageBM.setBitOn( bitmap, 37 );

		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getTID().toCharArray() ) );
		manageBM.setBitOn( bitmap, 41 );

		if( o_utilities.isWithdrawal() == false )
		{
			o_utilities.appendToRecord( dfu.charToByte( o_utilities.getMID().toCharArray() ) );
			manageBM.setBitOn( bitmap, 42 );
		}

		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getAcceptorName().toCharArray() ) );
		manageBM.setBitOn( bitmap, 43 );

		o_utilities.appendToRecord( dfu.charToByte( ( "027" + dfu.getPadString( '0', 27 ) ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 48 );

		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getDE49().toCharArray() ) );
		manageBM.setBitOn( bitmap, 49 );

		byte[] encPinBlock = o_utilities.getPINBlock();
		if( encPinBlock.length == 8 )
		{
			byte[] finalPB = dfu.BCDToHex( encPinBlock );
			o_utilities.appendToRecord( finalPB );
			manageBM.setBitOn( bitmap, 52 );
		}
		

		if( Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 5 || Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 7 )
		{
			String staticChipData = o_utilities.getstaticChipData();
			if( staticChipData.trim().isEmpty() )
			{
				String dynamicChipData = o_utilities.prepareEMVData();
				byte[] DE55Length =  dfu.charToByte( dfu.leftPad( String.valueOf( dynamicChipData.length()/2 ), 3, '0' ).toCharArray() );
				o_utilities.appendToRecord( DE55Length );
				o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dynamicChipData ), dynamicChipData.length() ) );
			}
			else
			{
				//byte[] DE55Length =  dfu.charToByte( dfu.leftPad( String.valueOf( staticChipData.length()/2 ), 3, '0' ).toCharArray() );
				//o_utilities.appendToRecord( DE55Length );
				//o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( istaticChipData ), staticChipData.length() ) );
			}
			manageBM.setBitOn( bitmap, 55 );
		}

		o_utilities.appendToRecord( dfu.charToByte( "003532".toCharArray() ) );
		manageBM.setBitOn( bitmap, 57 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getDE60().toCharArray() ) );
		manageBM.setBitOn( bitmap, 60 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getDE61().toCharArray() ) );
		manageBM.setBitOn( bitmap, 61 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getDE125().toCharArray() ) );
		manageBM.setBitOn( bitmap, 125 );

		byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.intToHex( o_utilities.getMessageSize() - 2) ), 4 );
		o_utilities.appendToRecord( hexaLength, 0 );

		byte[] BITMAP = dfu.BCDToHex( bitmap );
		o_utilities.appendToRecord( BITMAP, bitmap_pos );
		////tracing.hexDump( "Request", Record, Used );
	}

	public void process( int counter )
	{
		try
		{
			logger.info( "-----Start-----" );
			Connection connection = o_utilities.getDataBaseConnection();
			Statement statement = connection.createStatement();
			String SQL = "select TRACK2, PIN, PAYMENTSCHEME, ISSUERAPPLICATIONDATA, DEDICATEDFILENAME, CVN, PEM, ACCEPTORNAME, DE60, DE61, ACQUIRERID, MCC, TERMINALID, PANSEQ, DE125 from ADSSIMCARDS where STATUS = 'A' and THREADNAME = '" + this.getClass().getName() + "'";
			logger.debug( SQL );
			for( int i=0; i<counter; i++ )
			{
				ResultSet CardsResultSet = statement.executeQuery( SQL );
				while( CardsResultSet.next() )
				{
					o_utilities.setTrackII( CardsResultSet.getString( "TRACK2" ) );
					o_utilities.setPIN( CardsResultSet.getString( "PIN" ) );
					o_utilities.setPaymentScheme( CardsResultSet.getString( "PAYMENTSCHEME" ) );
					o_utilities.setIssuerApplicationData( CardsResultSet.getString( "ISSUERAPPLICATIONDATA" ) );
					o_utilities.setDedicatedFileName( CardsResultSet.getString( "DEDICATEDFILENAME" ) );
					o_utilities.setCVN( CardsResultSet.getString( "CVN" ) );

					o_utilities.setPosEntryMode( CardsResultSet.getString( "PEM" ) );
					o_utilities.setAcceptorName( CardsResultSet.getString( "ACCEPTORNAME" ) );
					o_utilities.setDE60( CardsResultSet.getString( "DE60" ) );
					o_utilities.setDE61( CardsResultSet.getString( "DE61" ) );
					o_utilities.setAcquirerId( CardsResultSet.getString( "ACQUIRERID" ) );
					o_utilities.setPosMCC( CardsResultSet.getString( "MCC" ) );
					o_utilities.setTID( CardsResultSet.getString( "TERMINALID" ) );
					o_utilities.setDE125( CardsResultSet.getString( "DE125" ) );
					o_utilities.setApplicationPrimaryAccountNumberSequenceNumber( CardsResultSet.getString( "PANSEQ" ) );

					prepareMsg();
					o_utilities.send();
					if( o_utilities.isWaitForResponse().equals("Y") == true )
					{
						o_utilities.receive();
						parseResponse();
					}
					try { Thread.sleep( Integer.parseInt( o_utilities.getNap() ) ); }
					catch( Exception E ){ logger.error( "Thread Nap Error : ", E ); }
				}
				CardsResultSet.close();
			}
			connection.close();
		}
		catch( Exception E ){ logger.error( "Error : ", E ); }
	}

	public static void main( String[] args )
	{
		try
		{
			int counter = 0;
			NAPS obj = new NAPS();
			if( args.length > 0 )
				counter = Integer.parseInt( args[0] );
			else
				counter = 1;
			logger.info( "Run " + counter + " time(s)" );
			obj.process( counter );
			logger.info( "-----End-----" );
		}
		catch( Exception E )
		{
			logger.error( "Loop Error : ", E );
		}
	}
}
