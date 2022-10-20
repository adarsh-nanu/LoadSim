package channel;

import java.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.io.HexDump;
import adssim.util.*;
import adssim.util.crypto.*;

class JPOS
{
	static Logger logger = Logger.getLogger( adssim.channel.JPOS.class );
	utilities o_utilities;
	DataFormatterUtil dfu;
	ManageBitmap manageBM;
	byte PrimaryBitmap[];
	byte SecondaryBitmap[];
	int ResponseDataLength;
	boolean TxnAvailable;

	public void parseResponse()
	{
		byte[] PRIMARYBITMAP = new byte[8];
		byte[] SECONDARYBITMAP = new byte[8];
		byte[] TPDU = new byte[5];
		byte[] MSGTYPE = new byte[2];
		logger.debug( "inside parseResponse" );

		o_utilities.getISOFields( TPDU, "TPDU" );
		o_utilities.getISOFields( MSGTYPE, "MSGTYPE" );
		logger.info( "MSGTYPE " + new String( dfu.byteToChar( dfu.BCDToHex( MSGTYPE ) ) ) );
		PrimaryBitmap = new byte[8];
		o_utilities.getISOFields( PRIMARYBITMAP, "PRIMARYBITMAP" );
		manageBM = new ManageBitmap();
		if( manageBM.checkIsBitOn( PRIMARYBITMAP, 1 ) )
                {
                        logger.info( "Secondary Bitmap available" );
			o_utilities.getISOFields( SECONDARYBITMAP, "SECONDARYBITMAP" );
                }

		for( int i=2; i<PRIMARYBITMAP.length*8; i++ )
		{
			if( manageBM.checkIsBitOn( PRIMARYBITMAP, i ) )
				switch(i)
				{
					case 2:
						logger.info( "Card available" );
						break;
					case 3:
						byte[] PCODE = new byte[3];
						o_utilities.getISOFields( PCODE, "PCODE" );
						logger.info( "PCODE " + new String( dfu.byteToChar( dfu.BCDToHex( PCODE) ) ) );
						break;
					case 4:
						byte[] AMOUNT = new byte[6];
						o_utilities.getISOFields( AMOUNT, "AMOUNT" );
						logger.info( "AMOUNT " + new String( dfu.byteToChar( dfu.BCDToHex( AMOUNT ) ) ) );
						break;
					case 11:
						byte[] TRACE = new byte[3];
						o_utilities.getISOFields( TRACE, "TRACE" );
						logger.info( "TRACE " + new String( dfu.byteToChar( dfu.BCDToHex( TRACE ) ) ) );
						break;
					case 12:
						byte[] LOCAL_TIME = new byte[3];
						o_utilities.getISOFields( LOCAL_TIME, "LOCAL_TIME" );
						logger.info( "LOCAL_TIME " + new String( dfu.byteToChar( dfu.BCDToHex( LOCAL_TIME ) ) ) );
						break;
					case 13:
						byte[] LOCAL_DATE = new byte[2];
						o_utilities.getISOFields( LOCAL_DATE, "LOCAL_DATE" );
						logger.info( "LOCAL_DATE " + new String( dfu.byteToChar( dfu.BCDToHex( LOCAL_DATE ) ) ) );
						break;
					case 24:
						byte[] ACT_CODE = new byte[2];
						o_utilities.getISOFields( ACT_CODE, "ACT_CODE" );
						logger.info( "ACT_CODE " + new String( dfu.byteToChar( dfu.BCDToHex( ACT_CODE ) ) ) );
						break;
					case 37:
						byte[] RRN = new byte[12];
						o_utilities.getISOFields( RRN, "RRN" );
						logger.info( "RRN " + new String( dfu.byteToChar( RRN ) ) );
						break;
					case 38:
						byte[] AUTHNUM = new byte[6];
						o_utilities.getISOFields( AUTHNUM, "AUTHNUM" );
						logger.info( "AUTHNUM " + new String( dfu.byteToChar( AUTHNUM ) ) );
						break;
					case 39:
						byte[] RESPCODE = new byte[2];
						o_utilities.getISOFields( RESPCODE, "RESPCODE" );
						logger.info( "RESPCODE " + new String( dfu.byteToChar( RESPCODE ) ) );
						if( new String( dfu.byteToChar( RESPCODE ) ).equals( "00" ) )
							TxnAvailable = true;
						break;
					case 41:
						byte[] TERMID = new byte[8];
						o_utilities.getISOFields( TERMID, "TERMID" );
						logger.info( "TERMID " + new String( dfu.byteToChar( TERMID ) ) );
						break;
					case 55:
						byte[] DE55Len = new byte[2];
						o_utilities.getISOFields( DE55Len, "DE55Len" );
						byte[] DE55 = new byte[ Integer.parseInt( new String( dfu.BCDToChar( DE55Len ) ) ) ];
						o_utilities.getISOFields( DE55, "DE55" );
						break;
				}
		}
		o_utilities.initMessageBuffer();
		if( Integer.parseInt( o_utilities.getNap() ) > 0 )
		{
			logger.info( "Taking a nap " + o_utilities.getNap() + " seconds" );
			try { Thread.sleep( Integer.parseInt( o_utilities.getNap() ) ); }
			catch( Exception E ){}
		}
	}

	public JPOS()
	{
		o_utilities = new utilities();
		PropertyConfigurator.configure("log4j.properties");
		logger.info("parent class print start");
		dfu = new DataFormatterUtil();
		manageBM = new ManageBitmap();
		ResponseDataLength = 0;
		o_utilities.connectDB( "jpos.cfg" );
		o_utilities.ConnectSwitch( "jpos.cfg" );
		o_utilities.initISOFields( "jpos.cfg" );
		o_utilities.initCard( "jpos.cfg" );
		o_utilities.initTerminal( "jpos.cfg" );
		o_utilities.initChip( "jpos.cfg" );
		logger.info( "OK" );
	}

	void prepareMsg()
	{
		logger.info( "inside prepareMsg" );
		o_utilities.updateTransactionData();
		o_utilities.initMessageBuffer();
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0000" ), 4 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "600026137E" ), 0 ), "Header" );
		int bitmap_pos = o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( o_utilities.getMsgType() ), 0 ), "Message Type" );
		byte[] bitmap = new byte[8];
		o_utilities.appendToRecord( bitmap, "Bitmap" );
		if( o_utilities.getPosEntryMode().substring(0, 1).equals( "1" ))
		{
			o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( String.valueOf( o_utilities.getPAN().length() ) ), 0 ), "PAN" );
			o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( o_utilities.getPAN() ), 0 ), "PAN" );
			manageBM.setBitOn( bitmap, 2 );
		}
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( o_utilities.getPcode() ), 0 ), "Pcode" );
		manageBM.setBitOn( bitmap, 3 );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dfu.getPadString( '0', 12 ).substring( o_utilities.getAmountAuthorized().length() ).concat( o_utilities.getAmountAuthorized() ) ), 12 ), "Amount" );
		manageBM.setBitOn( bitmap, 4 );

		String Trace = o_utilities.getTrace();
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( Trace, 6, '0' ) ), 0 ), "Trace" );
		manageBM.setBitOn( bitmap, 11 );

		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dfu.getPadString( '0', 4 ).substring( o_utilities.getPosEntryMode().length() ).concat( o_utilities.getPosEntryMode() ) ), 4 ), "PEM" );
		manageBM.setBitOn( bitmap, 22 );

		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0026" ), 0 ), "Action Code" );
		manageBM.setBitOn( bitmap, 24 );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( o_utilities.getPosConditionCode() ), 0 ), "POS Condition Code" );
		manageBM.setBitOn( bitmap, 25 );
		String TrackII = o_utilities.getTrackII();
		int TrackIILength = o_utilities.getTrackII().length();
		if( TrackII.length() % 2 != 0 )
			TrackII = TrackII.concat( "F" );
		if( o_utilities.getPosEntryMode().substring(0,1 ).equals( "7") || o_utilities.getPosEntryMode().substring(0,1 ).equals( "5") || o_utilities.getPosEntryMode().substring(0,1 ).equals( "2") )
		{
			o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( String.valueOf( TrackIILength ) ) + dfu.asciiToHex( TrackII ), 0 ), "Track 2" );
			manageBM.setBitOn( bitmap, 35 );
		}
		o_utilities.appendToRecord( dfu.charToByte( dfu.rightPad( o_utilities.getTID(), 8, ' ' ).toCharArray() ), "Termid" );
		manageBM.setBitOn( bitmap, 41 );
		o_utilities.appendToRecord( dfu.charToByte( dfu.rightPad( o_utilities.getMID(), 15, ' ' ).toCharArray() ), "Merchant ID" );
		manageBM.setBitOn( bitmap, 42 );

		byte[] encPinBlock = o_utilities.getPINBlock();
		if( encPinBlock.length == 8 )
		{
			o_utilities.appendToRecord( encPinBlock, "PIN" );
			manageBM.setBitOn( bitmap, 52 );
			logger.info( "PIN present" );
		}

		if( Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 5 || Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 7 )
		{
			String dynamicChipData = o_utilities.prepareEMVData();
			dynamicChipData = dynamicChipData + "5F3401" + o_utilities.getApplicationPrimaryAccountNumberSequenceNumber();
			logger.debug( "Add 5F34 for terminal" );
			int EMVDataLength = dynamicChipData.length()/2;
			dynamicChipData = dfu.getPadString( '0', 4 ).substring( String.valueOf( EMVDataLength ).length() ).concat( String.valueOf( EMVDataLength ) ).concat( dynamicChipData );
			o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dynamicChipData ), 0 ), "DE55" );
			manageBM.setBitOn( bitmap, 55 );
		}

		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0006" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( dfu.leftPad( Trace, 6, '0' ).toCharArray() ), "DE62" );
		manageBM.setBitOn( bitmap, 62 );
		byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( dfu.intToHex( o_utilities.getMessageSize() - 2 ), 4, '0' ) ), 4 );
		o_utilities.appendToRecord( hexaLength, 0 );
		o_utilities.appendToRecord( bitmap, bitmap_pos );
		logger.debug( "outgoing" );
	}
	
	public void process( int counter )
	{
		logger.info( "-----Start-----" );
		for( int i=0; i<counter; i++ )
		{
			prepareMsg();
			o_utilities.send();
			o_utilities.receive();
			parseResponse();
		}
	}

	public static void main( String[] args )
	{
		try
		{
			int counter = 0;
			JPOS obj = new JPOS();
			if( args.length > 0 )
				counter = Integer.parseInt( args[0] );
			else
				counter = 1;
			logger.info( "Run " + counter + " times" );
			obj.process( counter );
			logger.info( "-----End-----" );
		}
		catch( Exception E )
		{
			logger.error( "Error : ", E );
		}
	}
}
