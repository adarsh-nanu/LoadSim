package channel;

import java.util.*;
import java.io.FileReader;
import java.sql.*;
import java.io.BufferedReader;
import oracle.jdbc.driver.*;
import java.util.GregorianCalendar;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;

class UploadClose
{
	DataFormatterUtil dfu;
	byte[] Record;
	int Used;
	long rowCounter;
	ManageBitmap manageBM;
	String Url;
	String UserName;
	String Password;
	Connection connection;
	Statement statement;
	String SQL;
	public static ResultSet resultSet;
	Boolean PrintHexDump;
	int HexDumpLength;
	String LogFileName;
	String TPK;
	String PAN;
	String PIN;
	String TrackII;
	String TID;
	String MID;
	Socket clientSocket;
	byte TPDU[];
	byte MsgType[];
	byte PrimaryBitmap[];
	byte SecondaryBitmap[];
	int ResponseDataLength;
	String IP;
	Integer Port;
	String DE55;
	Trace tracing;
	String TerminalCountry;
	String TransactionCurrencyCode;
	String ApplicationCryptogram;
	String ApplicationInterchangeProfile;
	String ApplicationTransactionCounter;
	String UnpredictableNumber;
	String TerminalVerificationResults;
	String IssuerApplicationData;
	String DedicatedFileName;
	String CryptogramInformationData;
	String TerminalCapabilities;
	String TransactionDate;
	String AmountAuthorized;
	String AmountOther;
	String ApplicationPrimaryAccountNumberSequenceNumber;
	String InterfaceDeviceSerialNumber;
	String CardVerificationResults;
	String CardholderVerificationMethodResults;
	int atc;
	String MDK;
	String TerminalType;
	String TransactionType;
	String ApplicationVersionNumber;
	String PosEntryMode;
	String Nap;
	double DBC_NUM_DB;
	double DBC_AMT_DB;
	double DBC_NUM_CR;
	double DBC_AMT_CR;
	int respcode;
	String Respcode;
	String Pan;

	public String module()
	{
		return ( "[" + this.getClass().getSimpleName() + "]" );
	}

	public void cleanup() throws Exception
	{
		tracing.log( 1, module() + "inside cleanup" );
		connection.close();
		statement.close();
	}

	public void parseResponse() throws Exception
	{
		tracing.log( 1, module() + "inside parseResponse" );
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
						tracing.log( 1, new String( dfu.byteToChar( RESPCODE ) ) );
						Respcode = new String( dfu.byteToChar( RESPCODE ) );
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
	}

	public void ConnectSwitch() throws Exception
	{
		try
		{
			clientSocket = new Socket( IP, Port );
			tracing.log( 1, "Connected " + IP + ": " + Port );
		}
		catch( Exception E )
		{
			tracing.log( 1, "Exception " + E );
			System.exit(1);
		}
	}

	public UploadClose()  throws Exception
	{
		dfu = new DataFormatterUtil();
		Record = new byte[4096];
		Used = 0;
		manageBM = new ManageBitmap();

		try
		{
			getParam( "Config" );
		}
		catch( Exception E )
		{
			System.out.println( "connectDB failed: " + E );
			E.printStackTrace();
		}
		tracing = new Trace( LogFileName, PrintHexDump, HexDumpLength );
		ResponseDataLength = 0;
		TPDU = new byte[5];
		MsgType = new byte[2];
		PrimaryBitmap = new byte[8];
		SecondaryBitmap = new byte[8];
		connectDB();
		statement = connection.createStatement();
		ConnectSwitch();
	}

	String getCurrentDateTime()
	{
		DateFormatter logDate = new DateFormatter( "dd-MM-yyyy HH:mm:ss" );
		return logDate.getFormattedDate();
	}

	String getCurrentDateTime( String format )
	{
		DateFormatter logDate = new DateFormatter( format );
		return logDate.getFormattedDate();
	}

	String getTrace() throws Exception
	{
		String Trace = null;
		SQL = "select JPOSSEQ.Nextval trace from dual";
		ResultSet TraceResultSet = statement.executeQuery( SQL );
		while( TraceResultSet.next() )
		{
			Trace = TraceResultSet.getString( "trace" );
			break;
		}
		TraceResultSet.close();
		//statement.close();
		return Trace;
	}

	void process() throws Exception
	{
		prepare500Msg_1();
		send();
		receive();
		parseResponse();
		if( !Respcode.equals( "95" ) )
		{
			tracing.log( 1, "ERROR - Some other error" );
			System.exit(0);
			return;
		}
		tracing.log( 1, "Start uploading individual transactions" );
		process320Msgs();
		prepare500Msg_2();
		send();
		receive();
		parseResponse();
		cleanup();
	}

	void prepare500Msg_2() throws Exception
	{
		Record = new byte[4096];
		Used = 0;
		int msglength_pos = Used;
		Used += 2;
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "600026137E" ), 0 ) );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0500" ), 0 ) );
		byte[] bitmap = new byte[8];
		int bitmap_pos = Used;
		Used += 8;
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "960000" ), 0 ) );
		manageBM.setBitOn( bitmap, 3 );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( getTrace(), 6, '0' ) ), 0 ) );
		manageBM.setBitOn( bitmap, 11 );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0026" ), 0 ) );
		manageBM.setBitOn( bitmap, 24 );
		appendToRecord( dfu.charToByte( dfu.rightPad( TID, 8, ' ' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 41 );
		appendToRecord( dfu.charToByte( dfu.rightPad( MID, 15, ' ' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 42 );

		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0006" ), 0 ) );
		appendToRecord( dfu.charToByte( "000001".toCharArray() ) );
		manageBM.setBitOn( bitmap, 60 );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0120" ), 0 ) );
		SQL = "select '000000000000000000000000000000'|| lpad( DBC_NUM_DB, 3, '0') || lpad( DBC_AMT_DB*100, 12, '0' )|| lpad( DBC_NUM_CR, 3, '0' ) || lpad( DBC_AMT_CR*100, 12, '0' ) || '000000000000000000000000000000000000000000000000000000000000' DE63 from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'T' and close_flag = 'P' and extract_flag = 'N' and batch_id = ( select max( batch_id ) from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'T' and close_flag = 'P' and extract_flag = 'N' )";
		tracing.log( 1, SQL );
		ResultSet TraceResultSet = statement.executeQuery( SQL );
		while( TraceResultSet.next() )
		{
			appendToRecord( dfu.charToByte( TraceResultSet.getString( "DE63" ).toCharArray() ) );
			tracing.log( 1, TraceResultSet.getString( "DE63" ) );
		}
		TraceResultSet.close();
		manageBM.setBitOn( bitmap, 63 );

		System.arraycopy( bitmap, 0, Record, bitmap_pos, bitmap.length );
		byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( dfu.intToHex( Used - 2 ), 4, '0' ) ), 4 );
		System.arraycopy( hexaLength, 0, Record, msglength_pos, hexaLength.length );
		tracing.hexDump( "outgoing", Record, Used );
		tracing.log( 1, "outgoing" );
	}

	void prepare500Msg_1() throws Exception
	{
		Record = new byte[4096];
		Used = 0;
		int msglength_pos = Used;
		Used += 2;
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "600026137E" ), 0 ) );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0500" ), 0 ) );
		byte[] bitmap = new byte[8];
		int bitmap_pos = Used;
		Used += 8;
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "920000" ), 0 ) );
		manageBM.setBitOn( bitmap, 3 );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( getTrace(), 6, '0' ) ), 0 ) );
		manageBM.setBitOn( bitmap, 11 );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0026" ), 0 ) );
		manageBM.setBitOn( bitmap, 24 );
		appendToRecord( dfu.charToByte( dfu.rightPad( TID, 8, ' ' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 41 );
		appendToRecord( dfu.charToByte( dfu.rightPad( MID, 15, ' ' ).toCharArray() ) );
		manageBM.setBitOn( bitmap, 42 );

		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0006" ), 0 ) );
		appendToRecord( dfu.charToByte( "000001".toCharArray() ) );
		manageBM.setBitOn( bitmap, 60 );
		appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0120" ), 0 ) );
		SQL = "select '000000000000000000000000000000'|| lpad( DBC_NUM_DB, 3, '0') || lpad( DBC_AMT_DB*1000, 12, '0' )|| lpad( DBC_NUM_CR, 3, '0' ) || lpad( DBC_AMT_CR*100, 12, '0' ) || '000000000000000000000000000000000000000000000000000000000000' DE63 from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'H' and close_flag = 'O' and extract_flag = 'N' and batch_id = ( select max( batch_id ) from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'H' and close_flag = 'O' and extract_flag = 'N' )";
		tracing.log( 1, SQL );
		ResultSet TraceResultSet = statement.executeQuery( SQL );
		while( TraceResultSet.next() )
		{
			appendToRecord( dfu.charToByte( TraceResultSet.getString( "DE63" ).toCharArray() ) );
			tracing.log( 1, TraceResultSet.getString( "DE63" ) );
		}
		TraceResultSet.close();
		manageBM.setBitOn( bitmap, 63 );

		System.arraycopy( bitmap, 0, Record, bitmap_pos, bitmap.length );
		byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( dfu.intToHex( Used - 2 ), 4, '0' ) ), 4 );
		System.arraycopy( hexaLength, 0, Record, msglength_pos, hexaLength.length );
		tracing.hexDump( "outgoing", Record, Used );
		tracing.log( 1, "outgoing" );
	}

	void process320Msgs() throws Exception
	{
		SQL = "with maxbatch as ( select batch_id, open_date, close_date from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'H' and close_flag = 'M' and extract_flag = 'N' and batch_id in ( select max( batch_id ) from pos_batch where term_record_seq = " + TID + " and entity_id = '" + MID + "' and capture_type = 'H' and close_flag = 'M' and extract_flag = 'N' ) ) select length( trim( pan ) )||trim( pan ) pan, lpad( pcode, 6, '0' ) pcode, lpad(amount*100, 12, '0' ) amount, lpad( mod(trace, 1000000), 6, '0' ) trace, to_char( local_date, 'MMDD') local_date, lpad( local_time, 6, '0' ) local_time, substr(Shc_Data_Buffer, 74, 4) expirydate, lpad( bitand(pos_entry_code, 1023), 4, '0' ) PEM, CARD_SEQNO, refnum, authnum, TERM_COUNTRY_CODE, CRYPTO_CURR_CODE, APP_ICHG_PROFILE, APP_TXN_COUNTER, UNPREDICTABLE_NUM, TVR, ISSUER_APP_DATA, ISSUER_DISCRE_DATA, CCARD_CRYPTO_TRACE, TERM_CAP_PROFILE, CRYPTO_AMOUNT*100 CRYPTO_AMOUNT, CRYPTO_CB_AMOUNT*100 CRYPTO_CB_AMOUNT, TERM_SER_NUM, CVR, CRYPTO_TXN_TYPE, CRYPTOGRAM, TERM_TYPE from shclog s, maxbatch, shcchiplog c where s.local_date between '01-Mar-2019' and trunc(sysdate) and termid = '" + TID + "' and entityid = '" + MID + "' and acquirer = '0000494048' and s.batch_id = maxbatch.batch_id and msgtype = 210 and respcode = 0 and shcerror = 0 and s.chip_index = c.chip_index";
		tracing.log( 1, SQL );
		ResultSet rs = statement.executeQuery( SQL );
		while( rs.next() )
		{
			tracing.log( 1, "individual transacton exists" );
			TerminalCountry = rs.getString( "TERM_COUNTRY_CODE" ).trim();
			TransactionCurrencyCode = rs.getString( "CRYPTO_CURR_CODE" ).trim();
			ApplicationInterchangeProfile = rs.getString( "APP_ICHG_PROFILE" ).trim();
			ApplicationTransactionCounter = rs.getString( "APP_TXN_COUNTER" ).trim();
			UnpredictableNumber = rs.getString( "UNPREDICTABLE_NUM" ).trim();
			TerminalVerificationResults = rs.getString( "TVR" ).trim();
			IssuerApplicationData = rs.getString( "ISSUER_APP_DATA" ).trim();
			DedicatedFileName = rs.getString( "ISSUER_DISCRE_DATA" ).trim();
			CryptogramInformationData = rs.getString( "CCARD_CRYPTO_TRACE" ).trim();
			TerminalCapabilities = rs.getString( "TERM_CAP_PROFILE" ).trim();
			AmountAuthorized = rs.getString( "CRYPTO_AMOUNT" ).trim();
			AmountOther = rs.getString( "CRYPTO_CB_AMOUNT" ).trim();
			ApplicationPrimaryAccountNumberSequenceNumber = rs.getString( "CARD_SEQNO" ).trim();
			InterfaceDeviceSerialNumber = rs.getString( "TERM_SER_NUM" ).trim();
			CardholderVerificationMethodResults = rs.getString( "CVR" ).trim();
			TransactionType = rs.getString( "CRYPTO_TXN_TYPE" ).trim();
			TerminalType = rs.getString( "TERM_TYPE" ).trim();
			ApplicationCryptogram = rs.getString( "CRYPTOGRAM" ).trim();

			Record = new byte[4096];
			Used = 0;
			int msglength_pos = Used;
			Used += 2;
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "600026137E" ), 0 ) );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0320" ), 0 ) );
			byte[] bitmap = new byte[8];
			int bitmap_pos = Used;
			Used += 8;
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "pan" ) ), 0 ) );
			manageBM.setBitOn( bitmap, 2 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "pcode" ) ), 0 ) );
			manageBM.setBitOn( bitmap, 3 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "amount" ) ), 12 ) );
			manageBM.setBitOn( bitmap, 4 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "trace" ) ), 0 ) );
			manageBM.setBitOn( bitmap, 11 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "local_time" ) ), 0 ) );
			manageBM.setBitOn( bitmap, 12 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "local_date" ) ), 0 ) );
			manageBM.setBitOn( bitmap, 13 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "expirydate" ) ), 0 ) );
			manageBM.setBitOn( bitmap, 14 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( rs.getString( "PEM" ) ), 4 ) );
			manageBM.setBitOn( bitmap, 22 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0026" ), 0 ) );
			manageBM.setBitOn( bitmap, 24 );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "00" ), 0 ) );
			manageBM.setBitOn( bitmap, 25 );
			appendToRecord( dfu.charToByte( rs.getString( "refnum" ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 37 );
			appendToRecord( dfu.charToByte( rs.getString( "authnum" ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 38 );
			appendToRecord( dfu.charToByte( "00".toCharArray() ) );
			manageBM.setBitOn( bitmap, 39 );
			appendToRecord( dfu.charToByte( dfu.rightPad( TID, 8, ' ' ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 41 );
			appendToRecord( dfu.charToByte( dfu.rightPad( MID, 15, ' ' ).toCharArray() ) );
			manageBM.setBitOn( bitmap, 42 );
	
			if( Integer.parseInt( PosEntryMode ) /10 == 5 || Integer.parseInt( PosEntryMode ) /10 == 7 )
			{
				if( DE55.length() > 0 )
					appendToRecord( dfu.hexToBCD( dfu.asciiToHex( DE55 ), 0 ) );
				else
					appendToRecord( dfu.hexToBCD( dfu.asciiToHex( prepareEMVData() ), 0 ) );
				manageBM.setBitOn( bitmap, 55 );
			}
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0022" ), 0 ) );
			manageBM.setBitOn( bitmap, 60 );
			appendToRecord( dfu.charToByte( "0200".concat( rs.getString( "trace" ) ).concat( "            " ).toCharArray() ) );
			appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "0006" ), 0 ) );
			manageBM.setBitOn( bitmap, 62 );
			appendToRecord( dfu.charToByte( rs.getString( "trace" ).toCharArray() ) );
	
			System.arraycopy( bitmap, 0, Record, bitmap_pos, bitmap.length );
			byte[] hexaLength = dfu.hexToBCD( dfu.asciiToHex( dfu.leftPad( dfu.intToHex( Used - 2 ), 4, '0' ) ), 4 );
			System.arraycopy( hexaLength, 0, Record, msglength_pos, hexaLength.length );
			tracing.hexDump( "outgoing", Record, Used );
			tracing.log( 1, "outgoing" );
			send();
			receive();
			parseResponse();
		}
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
		UploadClose obj = new UploadClose();
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
				//obj.prepareMsg();
				//obj.send();
				//obj.receive();
				obj.process();
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
