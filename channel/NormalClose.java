package channel;

import java.util.*;
import java.sql.*;
import oracle.jdbc.driver.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.io.HexDump;
import adssim.util.*;
import adssim.util.crypto.*;

class NormalClose
{
	DataFormatterUtil dfu;
	ManageBitmap manageBM;
	Connection connection;
	Statement statement;
	String SQL;
	public static ResultSet resultSet;
	String Nap;
	double DBC_NUM_DB;
	double DBC_AMT_DB;
	double DBC_NUM_CR;
	double DBC_AMT_CR;
	String RespCode;
	static Logger logger = Logger.getLogger( adssim.channel.JPOS.class );

	public void parseResponse() throws Exception
	{
		logger.debug( "inside parseResponse" );
		int secondaryBitmapLen = 0;
		System.arraycopy( Record, 0, TPDU, 0, 5 );
		Used = TPDU.length;
		tracing.hexDump( "TPDU", TPDU );
		System.arraycopy( Record, Used, MsgType, 0, 2 );
		Used = MsgType.length;
		tracing.hexDump( "MsgType", MsgType );
		tracing.log( 1, new String( dfu.byteToChar( dfu.BCDToHex( MsgType ) ) ) );
		System.arraycopy( Record, TPDU.length+MsgType.length, PrimaryBitmap, 0, 8 );
		BitManager bm = new BitManager();
		if( bm.checkIsBitOn( PrimaryBitmap[0], 1 ) )
		{
			tracing.log( 1, "Secondary Bitmap available" );
			System.arraycopy( Record, TPDU.length+MsgType.length, SecondaryBitmap, 0, 8 );
			secondaryBitmapLen = 8;
		}
		byte[] isoData = new byte[ ResponseDataLength-TPDU.length-MsgType.length-8-secondaryBitmapLen ];
		tracing.log( 1, Record.length + " " + (TPDU.length+MsgType.length+8+secondaryBitmapLen) + " " + isoData.length);
		System.arraycopy( Record, TPDU.length+MsgType.length+8+secondaryBitmapLen, isoData, 0, isoData.length );
		Used = isoData.length;
		tracing.hexDump( "iso data", isoData );
		ManageBitmap mb = new ManageBitmap();
		int pos = 0;
		for( int i=2; i<PrimaryBitmap.length *8; i++ )
		{
			if( mb.checkIsBitOn( PrimaryBitmap, i ) )
				switch(i)
				{
					case 2:
						tracing.log( 1, "Card available" );
						break;
					case 3:
						tracing.log( 1, "Pcode" );
						byte[] PCODE = new byte[3];
						System.arraycopy( isoData, pos, PCODE, 0, 3 );
						Used = 3;pos+=Used;
						tracing.hexDump( "PCODE", PCODE );
						tracing.log( 1, new String( dfu.byteToChar( dfu.BCDToHex( PCODE ) ) ) );
						break;
					case 4:
						tracing.log( 1, "Amount" );
						byte[] AMOUNT = new byte[6];
						System.arraycopy( isoData, pos, AMOUNT, 0, 6 );
						Used = 6;pos+=Used;
						tracing.hexDump( "AMOUNT", AMOUNT );
						tracing.log( 1, new String( dfu.byteToChar( dfu.BCDToHex( AMOUNT ) ) ) );
						break;
					case 11:
						tracing.log( 1, "Trace" );
						byte[] TRACE = new byte[3];
						System.arraycopy( isoData, pos, TRACE, 0, 3 );
						Used = 3;pos+=Used;
						tracing.hexDump( "TRACE", TRACE );
						tracing.log( 1, new String( dfu.byteToChar( dfu.BCDToHex( TRACE ) ) ) );
						break;
					case 12:
						tracing.log( 1, "Local time" );
						byte[] LOCAL_TIME = new byte[3];
						System.arraycopy( isoData, pos, LOCAL_TIME, 0, 3 );
						Used = 3;pos+=Used;
						tracing.hexDump( "LOCAL_TIME", LOCAL_TIME );
						tracing.log( 1, new String( dfu.byteToChar( dfu.BCDToHex( LOCAL_TIME ) ) ) );
						break;
					case 13:
						tracing.log( 1, "Local date" );
						byte[] LOCAL_DATE = new byte[2];
						System.arraycopy( isoData, pos, LOCAL_DATE, 0, 2 );
						Used = 2;pos+=Used;
						tracing.hexDump( "LOCAL_DATE", LOCAL_DATE );
						tracing.log( 1, new String( dfu.byteToChar( dfu.BCDToHex( LOCAL_DATE ) ) ) );
						break;
					case 24:
						tracing.log( 1, "Action Code" );
						byte[] ACT_CODE = new byte[2];
						System.arraycopy( isoData, pos, ACT_CODE, 0, 2 );
						Used = 2;pos+=Used;
						tracing.hexDump( "ACT_CODE", ACT_CODE );
						tracing.log( 1, new String( dfu.byteToChar( dfu.BCDToHex( ACT_CODE ) ) ) );
						break;
					case 37:
						tracing.log( 1, "RRN" );
						byte[] RRN = new byte[12];
						System.arraycopy( isoData, pos, RRN, 0, 12 );
						Used = 12;pos+=Used;
						tracing.hexDump( "RRN", RRN );
						tracing.log( 1, new String( dfu.byteToChar( RRN ) ) );
						break;
					case 38:
						tracing.log( 1, "authnum" );
						byte[] AUTHNUM = new byte[6];
						System.arraycopy( isoData, pos, AUTHNUM, 0, 6 );
						Used = 6;pos+=Used;
						tracing.hexDump( "AUTHNUM", AUTHNUM );
						tracing.log( 1, new String( dfu.byteToChar( AUTHNUM ) ) );
						break;
					case 39:
						tracing.log( 1, "Respcode" );
						byte[] RESPCODE = new byte[2];
						System.arraycopy( isoData, pos, RESPCODE, 0, 2 );
						Used = 2;pos+=Used;
						tracing.hexDump( "RESPCODE", RESPCODE );
						RespCode = new String( dfu.byteToChar( RESPCODE ) );
						tracing.log( 1, new String( dfu.byteToChar( RESPCODE ) ) );
						break;
					case 41:
						tracing.log( 1, "Termid" );
						byte[] TERMID = new byte[8];
						System.arraycopy( isoData, pos, TERMID, 0, 8 );
						Used = 8;pos+=Used;
						tracing.hexDump( "TERMID", TERMID );
						tracing.log( 1, new String( dfu.byteToChar( TERMID ) ) );
						break;
					case 55:
						tracing.log( 1, "Chip Data" );
						byte[] DE55Len = new byte[2];
						System.arraycopy( isoData, pos, DE55Len, 0, 2 );
						Used = 2;pos+=Used;
						tracing.hexDump( "DE55 Length", DE55Len );
						byte[] DE55 = new byte[ Integer.parseInt( new String( dfu.BCDToChar( DE55Len ) ) ) ];
						tracing.log( 1, String.valueOf( DE55.length ) );
						System.arraycopy( isoData, pos, DE55, 0, Integer.parseInt( new String( dfu.BCDToChar( DE55Len ) ) ) );
						tracing.hexDump( "DE55", DE55 );
						break;
				}
		}
		Used = 0;
		Record = new byte[4096];
        if( RespCode.equals( "95" ) )
        {
        	tracing.log( 1, module() + "Start uploading individual transactions" );
			UploadClose obj = new UploadClose();
        	obj.process320Msgs();
        	obj.prepare500Msg_2();
        	obj.send();
        	obj.receive();
        	obj.cleanup();
        }
        else if( RespCode.equals( "00" ) )
        {
        	tracing.log( 1, module() + "Batch close successfull" );
		}
	}

	public NormalClose()  throws Exception
	{
		dfu = new DataFormatterUtil();
		manageBM = new ManageBitmap();
		o_utilities = new utilities();
		PropertyConfigurator.configure("log4j.properties");
		dfu = new DataFormatterUtil();
		manageBM = new ManageBitmap();
		ResponseDataLength = 0;
		o_utilities.connectDB( "jpos.cfg" );
		o_utilities.ConnectSwitch( "jpos_switchdb.cfg" );
		o_utilities.initTerminal( "jpos.cfg" );
		logger.info( "OK" );
	}

	public void start() throws Exception
	{
		prepareMsg();
		send();
		receive();
		parseResponse();
	}

	void prepareMsg() throws Exception
	{
		o_utilities.initMessageBuffer();
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0000" ), 4 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "600026137E" ), 0 ), "Header" );
		int bitmap_pos = o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0500" ), 0 ) );
		byte[] bitmap = new byte[8];
		o_utilities.appendToRecord( bitmap );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "920000" ), 0 ) );
		manageBM.setBitOn( bitmap, 3 );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( getTrace(), 6, '0' ) ), 0 ) );
		manageBM.setBitOn( bitmap, 11 );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0026" ), 0 ) );
		manageBM.setBitOn( bitmap, 24 );
		o_utilities.appendToRecord( dfu.charToByte( dfu.rightPad( TID, 8, ' ' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 41 );
		o_utilities.appendToRecord( dfu.charToByte( dfu.rightPad( MID, 15, ' ' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 42 );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0006" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( "000001".toCharArray() ) );
		manageBM.setBitOn( bitmap, 60 );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0120" ), 0 ) );
			
		SQL = "select pos_batch_idx, batch_id, capture_type, close_flag, extract_flag, '000000000000000000000000000000'|| lpad( DBC_NUM_DB, 3, '0') || lpad( DBC_AMT_DB*100, 12, '0' )|| lpad( DBC_NUM_CR, 3, '0' ) || lpad( DBC_AMT_CR*100, 12, '0' ) || '000000000000000000000000000000000000000000000000000000000000' DE63 from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'H' and close_flag = 'O' and extract_flag = 'N' and batch_id = ( select max( batch_id ) from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'H' and close_flag = 'O' and extract_flag = 'N' )";
		logger.debug( SQL );
		ResultSet TraceResultSet = o_utilities.getstatement().executeQuery( SQL );
		while( TraceResultSet.next() )
		{
			o_utilities.appendToRecord( dfu.charToByte( TraceResultSet.getString( "DE63" ).toCharArray() ) );
			tracing.log( 1, TraceResultSet.getString( "DE63" ) );
			tracing.log( 1, "batch id" + TraceResultSet.getString( "batch_id" ) );
			tracing.log( 1, "capture type" + TraceResultSet.getString( "capture_type" ) );
			tracing.log( 1, "close flag" + TraceResultSet.getString( "close_flag" ) );
			tracing.log( 1, "extract flag" + TraceResultSet.getString( "extract_flag" ) );
			tracing.log( 1, "idx " + TraceResultSet.getString( "pos_batch_idx" ) );
		}
		TraceResultSet.close();
		manageBM.setBitOn( bitmap, 63 );

		o_utilities.appendToRecord( 
		System.arraycopy( bitmap, 0, Record, bitmap_pos, bitmap.length );
		byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( dfu.intToHex( Used - 2 ), 4, '0' ) ), 4 );
		System.arraycopy( hexaLength, 0, Record, msglength_pos, hexaLength.length );
		tracing.hexDump( "outgoing", Record, Used );
		tracing.log( 1, "outgoing" );
	}

	void send() throws Exception
	{
		DataOutputStream dOut = new DataOutputStream( clientSocket.getOutputStream() );
		dOut.write( Record, 0, Used );
	}

	void receive() throws Exception
	{
		DataInputStream dIn = new DataInputStream( clientSocket.getInputStream() );
		Record = new byte[2];
		dIn.read( Record, 0, 2 );
		Used = 2;
		tracing.log( 1, "incoming" );
		tracing.hexDump( "Length", Record, Used );
		ResponseDataLength = dfu.hexToInt( new String( dfu.BCDToHex( Record ) ) );
		tracing.log( 1, "Length " + ResponseDataLength );
		Record = new byte[ResponseDataLength];
		dIn.read( Record );
		Used = ResponseDataLength;
		tracing.hexDump( "Data ", Record, Used );
		parseResponse();
	}

	void appendToRecord( byte[] dataToAppend )
	{
		System.arraycopy( dataToAppend, 0,  Record, Used, dataToAppend.length );
		Used += dataToAppend.length;
	}

	void appendToRecord( byte dataToAppend )
	{
		Record[Used] = dataToAppend;
		Used++;
	}

	/*
	public static void main( String[] args )  throws Exception
	{
		int counter = 0;
		NormalClose obj = new NormalClose();
		if( args.length > 0 )
			counter = Integer.parseInt( args[0] );
		else
			counter = 1;
		System.out.println( "Run " + counter + " times" );
		System.out.println( "Start time " + obj.getCurrentDateTime() );
		try
		{
			for( int i=0; i<counter; i++ )
			{
				//if( i==0 ) return;		//testing
				obj.prepareMsg();
				obj.send();
				obj.receive();
			}
		}
		catch( Exception E )
		{
			System.out.println( "Error : " + E );
			E.printStackTrace();
			return;
		}
		System.out.println( "End time " + obj.getCurrentDateTime() );
	}
	*/

	public void getParam( String host ) throws Exception
	{
		FileReader cfgFile = new FileReader( "jpos.cfg" );
		BufferedReader cfgRead = new BufferedReader( cfgFile );
		String line ="";
		String cfgBuf ="";
		String Buf ="";
		XMLParser xp = new XMLParser();
	
		while (( line = cfgRead.readLine() ) != null )
		{
			cfgBuf += line;
		}

		cfgRead.close();
		Vector v = null;
		v = xp.getXMLTagValue(cfgBuf,host);
		if ( v.size() > 0 )
		{
			Buf = v.elementAt(0).toString();
			if ( host.equals("Config") )
			{
				v = xp.getXMLTagValue(Buf,"user");
					UserName = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"pass");
					Password = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"url");
					Url = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"HexDump");
					PrintHexDump = Boolean.valueOf( v.elementAt(0).toString() );
				v = xp.getXMLTagValue(Buf,"HexDumpLength");
					HexDumpLength = Integer.parseInt( v.elementAt(0).toString() );
				v = xp.getXMLTagValue(Buf,"LogFileName");
					LogFileName = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"tpk");
					TPK = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"PAN");
					PAN = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"PIN");
					PIN = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TrackII");
					TrackII = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TID");
					TID = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"MID");
					MID = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"IP");
					IP = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"Port");
					Port = Integer.parseInt( v.elementAt(0).toString() );
				v = xp.getXMLTagValue(Buf,"DE55");
					DE55 = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TerminalCountry");
					TerminalCountry = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TransactionCurrencyCode");
					TransactionCurrencyCode = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"ApplicationInterchangeProfile");
					ApplicationInterchangeProfile = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"ApplicationTransactionCounter");
					ApplicationTransactionCounter = v.elementAt(0).toString();
					atc = Integer.parseInt( ApplicationTransactionCounter );
				v = xp.getXMLTagValue(Buf,"UnpredictableNumber");
					UnpredictableNumber = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TerminalVerificationResults");
					TerminalVerificationResults = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"IssuerApplicationData");
					IssuerApplicationData = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"DedicatedFileName");
					DedicatedFileName = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"CryptogramInformationData");
					CryptogramInformationData = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TerminalCapabilities");
					TerminalCapabilities = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"AmountAuthorized");
					AmountAuthorized = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"AmountOther");
					AmountOther = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"ApplicationPrimaryAccountNumberSequenceNumber");
					ApplicationPrimaryAccountNumberSequenceNumber = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"InterfaceDeviceSerialNumber");
					InterfaceDeviceSerialNumber = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"CardVerificationResults");
					CardVerificationResults = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"CardholderVerificationMethodResults");
					CardholderVerificationMethodResults = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"MDK");
					MDK = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TerminalType");
					TerminalType = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"TransactionType");
					TransactionType = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"ApplicationVersionNumber");
					ApplicationVersionNumber = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"PosEntryMode");
					PosEntryMode = v.elementAt(0).toString();
				v = xp.getXMLTagValue(Buf,"Nap");
					Nap = v.elementAt(0).toString();
			}
		}
	}

	public void connectDB()  throws Exception
	{
		try
		{
			DriverManager.registerDriver(new OracleDriver());
			connection = DriverManager.getConnection( Url, UserName, Password );
		}
		catch( Exception e) 
		{
			tracing.log( 1, " Oracle Connection Exception here..." );
			e.printStackTrace();
		}
		tracing.log( 1, "Connected to Oracle");
	}

	public Vector getXMLTagValue(String xml, String section) throws Exception
	{
		String xmlString = new String(xml);
		Vector v = new Vector();
		String beginTagToSearch = "<" + section + ">";
		String endTagToSearch = "</" + section + ">";

		// Look for the first occurrence of begin tag
		int index = xmlString.indexOf(beginTagToSearch);
		while(index != -1)
		{
			// Look for end tag
			// DOES NOT HANDLE
			int lastIndex = xmlString.indexOf(endTagToSearch);
	
			// Make sure there is no error
			if((lastIndex == -1) || (lastIndex < index))
				throw new Exception("Parse Error");
	
			// extract the substring
			String subs = xmlString.substring((index + beginTagToSearch.length()), lastIndex) ;
	
			// Add it to our list of tag values
			v.addElement(subs);
	
			// Try it again. Narrow down to the part of string which is not
			// processed yet.
			try
			{
				xmlString = xmlString.substring(lastIndex + endTagToSearch.length());
			}
			catch(Exception e)
			{
				xmlString = "";
			}
	
			// Start over again by searching the first occurrence of the begin tag
			// to continue the loop.
	
			index = xmlString.indexOf(beginTagToSearch);
		}	
		return v;
	}

	public String prepareEMVData() throws Exception
	{
		String ChipData = "";

		TerminalType = dfu.getPadString( '0', 2 ).substring( TerminalType.length() ).concat( TerminalType );
		ChipData = "9F35" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TerminalType.length()/2 ).length() ).concat( dfu.intToHex( TerminalType.length()/2 ) ).concat( TerminalType );

		TerminalCountry = dfu.getPadString( '0', 4 ).substring( TerminalCountry.length() ).concat( TerminalCountry );
		ChipData = ChipData + "9F1A" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TerminalCountry.length()/2 ).length() ).concat( dfu.intToHex( TerminalCountry.length()/2 ) ).concat( TerminalCountry );

        TransactionCurrencyCode = dfu.getPadString( '0', 4 ).substring( TransactionCurrencyCode.length() ).concat( TransactionCurrencyCode );
		ChipData = ChipData  + "5F2A" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TransactionCurrencyCode.length()/2 ).length() ).concat( dfu.intToHex( TransactionCurrencyCode.length()/2 ) ).concat( TransactionCurrencyCode );

		int CVN = dfu.hexToInt( IssuerApplicationData.substring( 4 ).substring(0, 2) );
		tracing.log( 1, "CVN " + CVN );
		Cryptogram cvn = null;
		if( CVN == 10 )
			cvn = new CVN10( MDK, PAN, ApplicationPrimaryAccountNumberSequenceNumber, AmountAuthorized, AmountOther, TerminalCountry, TerminalVerificationResults, TransactionCurrencyCode, new DateFormatter("yyMMdd").getFormattedDate(), "0", UnpredictableNumber, ApplicationInterchangeProfile, CardVerificationResults, String.valueOf( atc ) );

		ApplicationCryptogram = new String( dfu.BCDToChar( cvn.getMAC() ) );
		tracing.log( 1, "AC: " + ApplicationCryptogram );
		ChipData = ChipData  + "9F26" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationCryptogram.length()/2 ).length() ).concat( dfu.intToHex( ApplicationCryptogram.length()/2 ) ).concat( ApplicationCryptogram );

        ApplicationInterchangeProfile = dfu.getPadString( '0', 4 ).substring( ApplicationInterchangeProfile.length() ).concat( ApplicationInterchangeProfile );
		ChipData = ChipData  + "82" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationInterchangeProfile.length()/2 ).length() ).concat( dfu.intToHex( ApplicationInterchangeProfile.length()/2 ) ).concat( ApplicationInterchangeProfile );

		ApplicationTransactionCounter = dfu.getPadString( '0', 4 ).substring( ApplicationTransactionCounter.length() ).concat( ApplicationTransactionCounter );
		ChipData = ChipData  + "9F36" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationTransactionCounter.length()/2 ).length() ).concat( dfu.intToHex( ApplicationTransactionCounter.length()/2 ) ).concat( ApplicationTransactionCounter );

		UnpredictableNumber = dfu.getPadString( '0', 8 ).substring( UnpredictableNumber.length() ).concat( UnpredictableNumber );
		ChipData = ChipData  + "9F37" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( UnpredictableNumber.length()/2 ).length() ).concat( dfu.intToHex( UnpredictableNumber.length()/2 ) ).concat( UnpredictableNumber );

		TerminalVerificationResults = dfu.getPadString( '0', 10 ).substring( TerminalVerificationResults.length() ).concat( TerminalVerificationResults );
		ChipData = ChipData  + "95" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TerminalVerificationResults.length()/2 ).length() ).concat( dfu.intToHex( TerminalVerificationResults.length()/2 ) ).concat( TerminalVerificationResults );

		TransactionType = dfu.getPadString( '0', 2 ).substring( TransactionType.length() ).concat( TransactionType );
		ChipData = ChipData  + "9C" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TransactionType.length()/2 ).length() ).concat( dfu.intToHex( TransactionType.length()/2 ) ).concat( TransactionType );

		ChipData = ChipData  + "9F10" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( IssuerApplicationData.length()/2 ).length() ).concat( dfu.intToHex( IssuerApplicationData.length()/2 ) ).concat( IssuerApplicationData );

		ChipData = ChipData  + "84" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( DedicatedFileName.length()/2 ).length() ).concat( dfu.intToHex( DedicatedFileName.length()/2 ) ).concat( DedicatedFileName );

		ChipData = ChipData  + "9F09" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationVersionNumber.length()/2 ).length() ).concat( dfu.intToHex( ApplicationVersionNumber.length()/2 ) ).concat( ApplicationVersionNumber );

		CryptogramInformationData = dfu.getPadString( '0', 2 ).substring( CryptogramInformationData.length() ).concat( CryptogramInformationData );
		ChipData = ChipData  + "9F27" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( CryptogramInformationData.length()/2 ).length() ).concat( dfu.intToHex( CryptogramInformationData.length()/2 ) ).concat( CryptogramInformationData );

		ChipData = ChipData  + "9F33" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TerminalCapabilities.length()/2 ).length() ).concat( dfu.intToHex( TerminalCapabilities.length()/2 ) ).concat( TerminalCapabilities );

		ChipData = ChipData  + "9F34" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( CardholderVerificationMethodResults.length()/2 ).length() ).concat( dfu.intToHex( CardholderVerificationMethodResults.length()/2 ) ).concat( CardholderVerificationMethodResults );

		TransactionDate = dfu.getPadString( '0', 6 ).substring( new DateFormatter("yyMMdd").getFormattedDate().length() ).concat( new DateFormatter("yyMMdd").getFormattedDate() );
		ChipData = ChipData  + "9A" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( new DateFormatter("yyMMdd").getFormattedDate().length()/2 ).length() ).concat( dfu.intToHex( new DateFormatter("yyMMdd").getFormattedDate().length()/2 ) ).concat( new DateFormatter("yyMMdd").getFormattedDate() );

		AmountAuthorized = dfu.getPadString( '0', 12 ).substring( AmountAuthorized.length() ).concat( AmountAuthorized );
		ChipData = ChipData  + "9F02" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( AmountAuthorized.length()/2 ).length() ).concat( dfu.intToHex( AmountAuthorized.length()/2 ) ).concat( AmountAuthorized );

		AmountOther = dfu.getPadString( '0', 12 ).substring( AmountOther.length() ).concat( AmountOther );
		ChipData = ChipData  + "9F03" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( AmountOther.length()/2 ).length() ).concat( dfu.intToHex( AmountOther.length()/2 ) ).concat( AmountOther );

		ApplicationPrimaryAccountNumberSequenceNumber = dfu.getPadString( '0', 2 ).substring( ApplicationPrimaryAccountNumberSequenceNumber.length() ).concat( ApplicationPrimaryAccountNumberSequenceNumber );
		ChipData = ChipData  + "5F34" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationPrimaryAccountNumberSequenceNumber.length()/2 ).length() ).concat( dfu.intToHex( ApplicationPrimaryAccountNumberSequenceNumber.length()/2 ) ).concat( ApplicationPrimaryAccountNumberSequenceNumber );

		InterfaceDeviceSerialNumber = dfu.getPadString( '0', 16 ).substring( InterfaceDeviceSerialNumber.length() ).concat( InterfaceDeviceSerialNumber );
		ChipData = ChipData  + "9F1E" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( InterfaceDeviceSerialNumber.length()/2 ).length() ).concat( dfu.intToHex( InterfaceDeviceSerialNumber.length()/2 ) ).concat( InterfaceDeviceSerialNumber );

		tracing.log( 1, ChipData );
		int EMVDataLength = ChipData.length()/2;
		tracing.log( 1, String.valueOf( EMVDataLength ) );
		return dfu.getPadString( '0', 4 ).substring( String.valueOf( EMVDataLength ).length() ).concat( String.valueOf( EMVDataLength ) ).concat( ChipData );
	
	}
}
