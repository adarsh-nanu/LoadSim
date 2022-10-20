package channel;

import org.apache.log4j.Logger;
import adssim.util.*;
import org.apache.log4j.PropertyConfigurator;
import oracle.jdbc.driver.*;
import java.sql.*;

class CCCPOS implements Channels, Runnable
{
	static Logger logger = Logger.getLogger( adssim.channel.CCCPOS.class );
	DataFormatterUtil dfu;
	ManageBitmap manageBM;
	byte PrimaryBitmap[];
	byte SecondaryBitmap[];
	int ResponseDataLength;
	utilities o_utilities;

	public void run()
	{
		logger.debug( "receiving from inside thread" );
		o_utilities.receive();
		parseResponse();
	}

	public void parseResponse()
	{
		byte[] MSGTYPE = new byte[4];
		o_utilities.getISOFields( MSGTYPE, "MSGTYPE" );
		byte[] PRIMARYBITMAP = new byte[8];
		byte[] SECONDARYBITMAP = new byte[8];
		o_utilities.getISOFields( PRIMARYBITMAP, "PRIMARYBITMAP" );
		manageBM = new ManageBitmap();
		if( manageBM.checkIsBitOn( PRIMARYBITMAP, 1 ) )
		{
			logger.info( "Secondary bitmap available" );
			o_utilities.getISOFields( SECONDARYBITMAP, "SECONDARYBITMAP" );
		}

		byte[] PanLength = new byte[2];
		byte[] Pan = new byte[0];
		byte[] Pcode = new byte[6];
		byte[] Amount = new byte[12];
		byte[] Trace = new byte[6];
		byte[] CaptureDate = new byte[4];
		byte[] AcqIDLength = new byte[2];
		byte[] AcqID = new byte[0];
		byte[] FwdIDLength = new byte[2];
		byte[] FwdID = new byte[0];
		byte[] RetRefNum = new byte[12];
		byte[] Authnum = new byte[6];
		byte[] RespCode = new byte[2];
		byte[] TerminalID = new byte[8];
		byte[] AcqCurrencyCode = new byte[3];
		byte[] NetRefDataLength = new byte[2];
		byte[] NetRefData = new byte[0];
		for( int i=2; i<PRIMARYBITMAP.length*8; i++ )
		{
			if( manageBM.checkIsBitOn( PRIMARYBITMAP, i ) ) switch(i){
			case 2:
				o_utilities.getISOFields( PanLength, "Pan Length" );
				Pan = new byte[ Integer.parseInt( new String( dfu.byteToChar(PanLength) ) )];
				o_utilities.getISOFields( Pan, "Pan" );
				logger.info( "Pan " + new String( dfu.byteToChar( Pan ) ) );
				break;
			case 3:
				o_utilities.getISOFields( Pcode, "Pcode" );
				logger.info( "Pcode " + new String( dfu.byteToChar( Pcode ) ) );
				break;
			case 4:
				o_utilities.getISOFields( Amount, "Amount" );
				logger.info( "Amount " + new String( dfu.byteToChar( Amount ) ) );
				break;
			case 11:
				o_utilities.getISOFields( Trace, "Trace" );	
				logger.info( "Trace " + new String( dfu.byteToChar( Trace ) ) );
				break;
			case 15:
				o_utilities.getISOFields( CaptureDate, "CaptureDate" );	
				logger.info( "CaptureDate " + new String( dfu.byteToChar( CaptureDate ) ) );
				break;
			case 32:
				o_utilities.getISOFields( AcqIDLength, "AcqIDLength" );
				AcqID = new byte[ Integer.parseInt( new String( dfu.byteToChar( AcqIDLength ) ) )];
				o_utilities.getISOFields( AcqID, "AcqID" );
				logger.info( "AcqID " + new String( dfu.byteToChar( AcqID ) ) );
				break;
			case 33:
				o_utilities.getISOFields( FwdIDLength, "FwdIDLength" );
				FwdID = new byte[ Integer.parseInt( new String( dfu.byteToChar(FwdIDLength) ) )];
				o_utilities.getISOFields( FwdID, "FwdID" );
				logger.info( "FwdID " + new String( dfu.byteToChar( FwdID ) ) );
				break;
			case 37:
				o_utilities.getISOFields( RetRefNum, "RetRefNum" );	
				logger.info( "RetRefNum " + new String( dfu.byteToChar( RetRefNum ) ) );
				break;
			case 38:
				o_utilities.getISOFields( Authnum, "Authnum" );	
				logger.info( "Authnum " + new String( dfu.byteToChar( Authnum ) ) );
				break;
			case 39:
				o_utilities.getISOFields( RespCode, "RespCode" );	
				logger.info( "RespCode " + new String( dfu.byteToChar( RespCode ) ) );
				break;
			case 41:
				o_utilities.getISOFields( TerminalID, "TerminalID" );	
				logger.info( "TerminalID " + new String( dfu.byteToChar( TerminalID ) ) );
				break;
			case 49:
				o_utilities.getISOFields( AcqCurrencyCode, "AcqCurrencyCode" );	
				logger.info( "AcqCurrencyCode " + new String( dfu.byteToChar( AcqCurrencyCode ) ) );
				break;
			case 63:
				o_utilities.getISOFields( NetRefDataLength, "NetRefDataLength" );
				NetRefData = new byte[ Integer.parseInt( new String( dfu.byteToChar( NetRefDataLength ) ) )];
				o_utilities.getISOFields( NetRefData, "NetRefData" );
				logger.info( "NetRefData " + new String( dfu.byteToChar( NetRefData ) ) );
				break;
			}
		}
	}

	public void getDCCData( String AcqCurrency, String Amount  )
	{
		if( o_utilities.isDCC() )
		{
			double exchg_rate = o_utilities.getExchangeRate( AcqCurrency );
			int decimal = o_utilities.getCurrencyDecimals( AcqCurrency );

			logger.info( "Amount " + Amount );
			logger.info( "AcqCurrency " + AcqCurrency );
			logger.info( "Ex Rate " + exchg_rate );

			int DE62LenPos = o_utilities.getMessageSize();
			o_utilities.appendToRecord( dfu.charToByte( "000".toCharArray() ) );
			int DCCDataLenPos = o_utilities.appendToRecord( dfu.charToByte( "F0".toCharArray() ) );
			int DCCBitMapPos = o_utilities.appendToRecord( dfu.charToByte( "0000".toCharArray() ) );

			byte[] de62bmap = new byte[2];
			o_utilities.appendToRecord(dfu.charToByte( "0000".toCharArray() )); //bitmap

			o_utilities.appendToRecord( dfu.charToByte( "1".toCharArray() ) );
			manageBM.setBitOn( de62bmap, 2 );

			String QARAmount = o_utilities.ConvertAmtNoDecimal( Amount, exchg_rate, decimal );
			logger.debug( "QARAmount " + QARAmount );
			String DCCData = "634" + "2" +  dfu.getPadString( '0', 12 ).substring( QARAmount.length() ).concat( QARAmount );
			o_utilities.appendToRecord( dfu.charToByte( DCCData.toCharArray() ) );
			manageBM.setBitOn( de62bmap, 3 );

			String DCCAmount = o_utilities.fmtAmtNoDecimal( Amount, decimal );
			DCCData = AcqCurrency + decimal + dfu.getPadString( '0', 12 ).substring( String.valueOf(DCCAmount).length() ).concat( String.valueOf( DCCAmount ) );
			o_utilities.appendToRecord( dfu.charToByte( DCCData.toCharArray() ) );
			manageBM.setBitOn( de62bmap, 4 );

			String ExchangerateND = o_utilities.fmtNoDecimal( String.valueOf( exchg_rate ) );
			int ExchangerateDec = o_utilities.getDecimalization( String.valueOf( exchg_rate ) );
			DCCData = ExchangerateDec + dfu.getPadString( '0', 12 ).substring( String.valueOf(ExchangerateND).length() ).concat( String.valueOf( ExchangerateND ) );
			o_utilities.appendToRecord( dfu.charToByte( DCCData.toCharArray() ) );
			manageBM.setBitOn( de62bmap, 5 );

			DCCData = o_utilities.getCurrentDateTime("yyyyMMddHHmmss");
			o_utilities.appendToRecord( dfu.charToByte( DCCData.toCharArray() ) );
			manageBM.setBitOn( de62bmap, 7 );
			o_utilities.appendToRecord( dfu.charToByte( DCCData.toCharArray() ) );
			manageBM.setBitOn( de62bmap, 8 );

			int DE62Len = o_utilities.getMessageSize()-DE62LenPos-3;
			int DCCLen = o_utilities.getMessageSize()-DE62LenPos-8;

			byte[] FullDCCLengthPosData = dfu.charToByte( dfu.getPadString( '0', 3 ).substring( String.valueOf( DE62Len ).length() ).concat( String.valueOf( DE62Len ) ).toCharArray() );
			o_utilities.appendToRecord( FullDCCLengthPosData, DE62LenPos );

			byte[] de62bmapAscii = dfu.BCDToHex( de62bmap );
			o_utilities.appendToRecord( de62bmapAscii, DCCBitMapPos );

			byte[] DCCLengthData = dfu.BCDToHex( dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( dfu.intToHex( DCCLen ), 4, '0' ) ), 4 ) );
			o_utilities.appendToRecord( DCCLengthData, DCCDataLenPos );
		}
		else
		{
			o_utilities.appendToRecord( dfu.charToByte( "011F0000540000".toCharArray() ) );
		}
	}

	public CCCPOS()  throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		dfu = new DataFormatterUtil();
		manageBM = new ManageBitmap();
		ResponseDataLength = 0;
		PrimaryBitmap = new byte[8];
		SecondaryBitmap = new byte[8];
		o_utilities = new utilities();
		o_utilities.connectDB("ccc.cfg");
		o_utilities.ConnectSwitch("ccc.cfg");
		o_utilities.initCard( "ccc.cfg" );
		o_utilities.initTerminal( "ccc.cfg" );
		logger.info( "OK" );
	}

	public void prepareMsg()
	{
		logger.info( "inside prepareMsg" );
		o_utilities.initMessageBuffer(512);
		o_utilities.setPurchase();
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0000" ), 4 ) );
		int bitmap_pos = o_utilities.appendToRecord( dfu.charToByte( "0100".toCharArray() ), "Message Type" );
		byte[] bitmap = new byte[8];
		o_utilities.appendToRecord(bitmap, "Bitmap" );
		o_utilities.appendToRecord( dfu.charToByte( String.valueOf( o_utilities.getPAN().length() ).toCharArray() ), "PAN Length" );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getPAN().toCharArray() ), "PAN" );
		manageBM.setBitOn( bitmap, 2 );
		o_utilities.appendToRecord( dfu.charToByte( "000000".toCharArray() ), "PCODE" );
		manageBM.setBitOn( bitmap, 3 );
		o_utilities.appendToRecord( dfu.charToByte( dfu.getPadString( '0', 12 ).substring( o_utilities.getAmountAuthorized().length() ).concat( o_utilities.getAmountAuthorized() ).toCharArray() ), "AMOUNT" );
		manageBM.setBitOn( bitmap, 4 );
		String Trace = o_utilities.getTrace();
		o_utilities.appendToRecord( dfu.charToByte( dfu.leftPad( Trace, 6, '0' ).toCharArray() ), "TRACE" );
		manageBM.setBitOn( bitmap, 11 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getCurrentDateTime( "HHmmss" ).toCharArray() ), "Local Time" );
		manageBM.setBitOn( bitmap, 12 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getCurrentDateTime( "MMdd" ).toCharArray() ), "Local Date" );
		manageBM.setBitOn( bitmap, 13 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getExpiryDate().toCharArray() ), "Expiry Date" );
		manageBM.setBitOn( bitmap, 14 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getMCC().toCharArray() ), "MCC" );
		manageBM.setBitOn( bitmap, 18 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getPosEntryMode().toCharArray() ), "PEM" );
		manageBM.setBitOn( bitmap, 22 );
		o_utilities.appendToRecord( dfu.charToByte( "00".toCharArray() ), "POS Condition Code" );
		manageBM.setBitOn( bitmap, 25 );
		o_utilities.appendToRecord( dfu.charToByte( "1100000000001".toCharArray() ), "Acquirer ID" );
		manageBM.setBitOn( bitmap, 32 );
		o_utilities.appendToRecord( dfu.charToByte( "1100000000000".toCharArray() ), "Forwarder ID" );
		manageBM.setBitOn( bitmap, 33 );
		if( o_utilities.getTrackII().length() > 1 && o_utilities.getPosEntryMode().equals( "900" ) )
		{
			o_utilities.appendToRecord( dfu.charToByte( ( String.valueOf( o_utilities.getTrackII().length() ).concat( o_utilities.getTrackII() ) ).toCharArray() ), "Track II" );
			manageBM.setBitOn( bitmap, 35 );
		}
		o_utilities.appendToRecord( dfu.charToByte( ("CCC" + dfu.leftPad( Trace, 9, '0' ) ).toCharArray() ), "Refnum" );
		manageBM.setBitOn( bitmap, 37 );

		o_utilities.appendToRecord( dfu.charToByte( dfu.rightPad( o_utilities.getTID(), 8, ' ').toCharArray() ), "TID" );
		manageBM.setBitOn( bitmap, 41 );
		o_utilities.appendToRecord( dfu.charToByte( dfu.rightPad(o_utilities.getMID(), 15, ' ').toCharArray() ), "MID" );
		manageBM.setBitOn( bitmap, 42 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getAcceptorName().toCharArray() ), "Acceptorname" );
		manageBM.setBitOn( bitmap, 43 );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getTransactionCurrencyCode().toCharArray() ), "Currency" );
		manageBM.setBitOn( bitmap, 49 );
		o_utilities.appendToRecord( dfu.charToByte( "026".toCharArray() ), "POS Additional Data Length" );

		if( o_utilities.isAttended() )
			o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		else
			o_utilities.appendToRecord( dfu.charToByte( "1".toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		if( o_utilities.isOnPremises() )
			o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		else
			o_utilities.appendToRecord( dfu.charToByte( "1".toCharArray() ) );
		if( o_utilities.isCustomerPresent() )
			o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		else
			o_utilities.appendToRecord( dfu.charToByte( "1".toCharArray() ) );
		if( o_utilities.isCardPresent() )
			o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		else
			o_utilities.appendToRecord( dfu.charToByte( "1".toCharArray() ) );
		if( o_utilities.isCardCaptureCapablity() )
			o_utilities.appendToRecord( dfu.charToByte( "1".toCharArray() ) );
		else
			o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "4".toCharArray() ) );
		if( o_utilities.isSuspect() )
			o_utilities.appendToRecord( dfu.charToByte( "1".toCharArray() ) );
		else
			o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "0".toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getCardDataInputCapability().toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "21".toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getTerminalCountry().toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "0000000000".toCharArray() ) );
		manageBM.setBitOn( bitmap, 61 );

		getDCCData( o_utilities.getTransactionCurrencyCode() , o_utilities.getAmountAuthorized() );
		manageBM.setBitOn( bitmap, 62 );

		byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.intToHex( o_utilities.getMessageSize() - 2) ), 4 );
		o_utilities.appendToRecord(hexaLength, 0 );

		o_utilities.appendToRecord( bitmap, bitmap_pos );

		logger.info( "outgoing" );
	}

	public void process( int counter )
	{
		try
		{
			logger.info( "-----Start-----" );
			String SQL = "select TRACK2, PIN, PAYMENTSCHEME, ISSUERAPPLICATIONDATA, DEDICATEDFILENAME, CVN from ADSSIMCARDS where STATUS = 'A' and THREADNAME = '" + this.getClass().getName() + "'";
			Connection connection = o_utilities.getDataBaseConnection();
			Statement statement = connection.createStatement();
			for( int i=0; i<counter; i++ )
			{
				logger.debug( SQL );
				ResultSet CardsResultSet = statement.executeQuery( SQL );
				while( CardsResultSet.next() )
				{
					o_utilities.setTrackII( CardsResultSet.getString( "TRACK2" ) );
					o_utilities.setPIN( CardsResultSet.getString( "PIN" ) );
					prepareMsg();
					o_utilities.send();
					if( o_utilities.isWaitForResponse().equals("Y") == true )
					{
						o_utilities.receive();
						parseResponse();
					}
					else
					{
						try { Thread.sleep( Integer.parseInt( o_utilities.getNap() ) ); }
						catch( Exception E ){ logger.error( "Thread Nap Error : ", E ); }
					}
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
			CCCPOS obj = new CCCPOS();
			if( args.length > 0 )
				counter = Integer.parseInt( args[0] );
			else
				counter = 1;
			logger.info( "Run " + counter + " times" );
			obj.process( counter );
		}
		catch( Exception E )
		{
			logger.info( "Exception in main" );
			E.printStackTrace();
		}
	}
}
