package util;

import oracle.jdbc.driver.*;
import java.sql.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Vector;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import org.apache.log4j.Logger;
import util.*;
import util.crypto.*;
import java.util.Random;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.io.HexDump;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class utilities
{
	static Logger logger = Logger.getLogger( util.utilities.class);
	String Pcode;
	public Connection connection;
	String cfgBuf;
	XMLParser xp;
	byte[] Record;
	Statement statement;
	ResultSet resultSet;
	String TerminalCountry;
	String TransactionCurrencyCode;
	String ApplicationInterchangeProfile;
	String ApplicationTransactionCounter;
	String UnpredictableNumber;
	String TerminalVerificationResults;
	String IssuerApplicationData;
	String DedicatedFileName;
	String CryptogramInformationData;
	String TerminalCapabilities;
	String AmountAuthorized;
	String AmountOther;
	String ApplicationPrimaryAccountNumberSequenceNumber;
	String InterfaceDeviceSerialNumber;
	String PaymentScheme;
	String CardholderVerificationMethodResults;
	String MDK;
	String ApplicationVersionNumber;
	String CVN;
	String tpk;
	String TPK;
	String TID;
	String MID;
	String AcceptorName;
	String AcquirerId;
	String PosMCC;
	String PosEntryMode;
	String Nap;
	String Withdrawal;
	String Purchase;
	String BalanceEnq;
	String RandomTransaction;
	String RandomReversal;
	String ForceReversal;
	String RandomAmount;
	String BelowCVMLimit;
	String PAN;
	String PIN;
	String TrackII;
	String ExpiryDate;
	String DE60;
	String DE61;
	String DE49;
	String IP;
	int Port;
	DataFormatterUtil dfu;
	Socket clientSocket;
	String SQL;
	int atc;
	String ApplicationCryptogram;
	String TerminalType;
	String TransactionDate;
	String TransactionType;
	int Used;
	boolean MandatoryPIN;
	Random rand;
	String MCC;
	String Attended;
	String OnPremises;
	String CustomerPresent;
	String CardPresent;
	String CardCaptureCapablity;
	String Suspect;
	String CardDataInputCapability;
	String ATMUnit;
	String OpKey;
	String WaitForResponse;
	String ForcePcode;
	String BNAData;
	String GenB;
	String GenC;
	String Deposit;
	String DE125;
	String PosConditionCode;
	String MsgType;

	public String getPosConditionCode(){ return PosConditionCode; }
	public String getMsgType(){ return MsgType; }
	public String getstaticChipData() { return ""; }

	public String isWaitForResponse()
	{
		return ( WaitForResponse == null ? "Y" : WaitForResponse );
		/*
		if ( WaitForResponse == null )
			WaitForResponse = "Y";
		return WaitForResponse;
		*/
	}

	public  void updateTransactionData()
	{
		MandatoryPIN = false;
		boolean Repeat = true;

		if( Boolean.parseBoolean( RandomTransaction ) == true )
		{
			while( Repeat )
			{
				logger.debug( "Random transaction. Toss coin" );
				rand = new Random();
				int random = rand.nextInt(9);
				if( ( random >=0 && random <=2 ) && Boolean.parseBoolean( Withdrawal ) == true )
				{
					setWithdrawal();
					Repeat = false;
				}
				else if( ( random >=3 && random <=5 ) && Boolean.parseBoolean( BalanceEnq ) == true )
				{
					setBalanceEnq();
					Repeat = false;
				}
				else if( ( random >=6 && random <=8 ) && Boolean.parseBoolean( Purchase ) == true )
				{
					setPurchase();
					Repeat = false;
				}
			}
		}
		else if( Boolean.parseBoolean( Withdrawal ) == true )
			setWithdrawal();
		else if( Boolean.parseBoolean( BalanceEnq ) == true )
			setBalanceEnq();
		else if( Boolean.parseBoolean( Purchase ) == true )
			setPurchase();
		else if( Boolean.parseBoolean( Deposit ) == true )
			setDeposit();
		if( ForcePcode.length() != 0 )
			Pcode = ForcePcode;
		logger.info( "AmountAuthorized " +  AmountAuthorized );
		logger.info( "TerminalType " +  TerminalType );
		logger.info( "TransactionType " +  TransactionType );
		logger.info( "Pcode " +  Pcode );
	}

	public void setTrackII( String TrackII )
	{ this.TrackII = TrackII; parseTrack( this.TrackII ); };

	public void setPIN( String PIN )
	{ this.PIN = PIN; logger.debug( "PIN : " + this.PIN ); };

	public void setPaymentScheme( String PaymentScheme )
	{ this.PaymentScheme = PaymentScheme; };

	public void setIssuerApplicationData( String IssuerApplicationData )
	{ this.IssuerApplicationData = IssuerApplicationData; };

	public void setDedicatedFileName( String DedicatedFileName )
	{ this.DedicatedFileName = DedicatedFileName; };

	public void setCVN( String CVN )
	{ this.CVN = CVN; };

	public boolean isWithdrawal()
	{
		return Boolean.parseBoolean( Withdrawal );
	}

	public boolean isForceReversal()
	{	return Boolean.parseBoolean( ForceReversal ); }

	public boolean isRandomReversal()
	{	return Boolean.parseBoolean( RandomReversal ); }

	public boolean isAttended()
	{	return Boolean.parseBoolean( Attended ); }
	
	public boolean isOnPremises()
	{	return Boolean.parseBoolean( OnPremises ); }
	
	public boolean isCustomerPresent()
	{	return Boolean.parseBoolean( CustomerPresent ); }
	
	public boolean isCardPresent()
	{	return Boolean.parseBoolean( CardPresent ); }
	
	public boolean isCardCaptureCapablity()
	{	return Boolean.parseBoolean( CardCaptureCapablity ); }
	
	public boolean isSuspect()
	{	return Boolean.parseBoolean( Suspect ); }
	
	public String getCardDataInputCapability()
	{	return CardDataInputCapability; }

	public String getATMUnit()
	{	return ATMUnit; }

	public String getOpKey()
	{	return OpKey; }

	public String getApplicationPrimaryAccountNumberSequenceNumber()
	{ return  ApplicationPrimaryAccountNumberSequenceNumber; }

	public void setApplicationPrimaryAccountNumberSequenceNumber( String ApplicationPrimaryAccountNumberSequenceNumber )
	{ this.ApplicationPrimaryAccountNumberSequenceNumber = ApplicationPrimaryAccountNumberSequenceNumber; }

	public String getTrackII()
	{ return TrackII; }

	public String getNap()
	{ return Nap; }

	public boolean isDCC()
	{ if (TransactionCurrencyCode.equals( "634" ) ) return false; else return true; }

	public String getPAN()
	{ return PAN; }

	public String getTerminalCountry()
	{ return TerminalCountry; }

	public String getTransactionCurrencyCode()
	{ return TransactionCurrencyCode; }

	public String getTID()
	{ return TID; }

	public void setTID( String TID )
	{ this.TID = TID; logger.debug( "TID : " + this.TID ); }

	public String getMID()
	{ return MID; }

	public String getAcceptorName()
	{ return AcceptorName; }

	public int getMessageSize()
	{ logger.debug( "Current buffer size " + Used ); return Used; }

	public String getDE49()
	{ return  DE49; }

	public String getDE60()
	{ return  DE60; }

	public String getDE61()
	{ return  DE61; }

	public String getDE125()
	{ return  DE125; }

	public void setDE125( String DE125 )
	{ this.DE125 = DE125; }

	public String getBNA()
	{ return  BNAData; }

	public String getGenB()
	{ return  GenB; }

	public String getGenC()
	{ return  GenC; }

	public byte[] getPINBlock()
	{
		byte[] encPinBlock = new byte[1];
		
		if( MandatoryPIN == true || getAcquirerId().equals("63493000000") )
		{
			if(  PIN.isEmpty() )
			{
				logger.error( "Mandatory PIN missing " );
				System.exit(0);
			}
			else
			{
				ANSIPin ansipinobj = new ANSIPin();
				byte[] clearPinBlock = ansipinobj.format0( PAN, PIN );
				TDes tdesobj = new TDes( TPK, clearPinBlock );
				encPinBlock = tdesobj.encrypt();
			}
		}
		return encPinBlock;
	}
	
	void setRandomAmount( boolean Withdrawal )
	{
		logger.debug( "Random amount. Toss a bunch of coins" );
		rand = new Random();
		int random = 0;
		
		random = rand.nextInt( 30 );
		if( random >0 && random <= 9 )
		{
			random *= 100;
				if( Withdrawal == true )
						random *= 100;
		}
		else if( random >10 && random <= 20 )
		{
			random *= 1000;
			if( Withdrawal == true )
				random *= 10;
		}
		else if( random >20 && random <= 30 )
		{
			random *= 10000;
		}
		else
		{
			random = 100;
			if( Withdrawal == true )
				random *= 100;
		}
		AmountAuthorized = String.valueOf( random );
	}
	
	public  void setWithdrawal()
	{
		String fromAccountType= null;
		logger.info( "Withdrawal is the choice" );
		MCC = "6011";
		rand = new Random();
		int random = rand.nextInt(3);
		if( random == 0 )
			random = 1;
		if( random % 2 == 0 )
		{
			if( ( random == 1 ) || ( random == 2 ) )
				fromAccountType = String.valueOf( random * 10 );
			else
				fromAccountType = "00";
		}
		else
			fromAccountType = "00";
		TransactionType = "01";
		Pcode = "01" + fromAccountType + "00";
		if( Boolean.parseBoolean( RandomAmount ) == true )
			setRandomAmount( true );
		MandatoryPIN = true;
		TerminalType = "14";
	}
	
	public  void setBalanceEnq()
	{
		String fromAccountType= null;
		logger.info( "Balance Enquiry is the choice" );
		rand = new Random();
		int random = rand.nextInt(10);
		if( random == 0 )
			random = 1;
		if( random % 2 == 0 )
		{
			if( ( random == 1 ) || ( random == 2 ) )
				fromAccountType = String.valueOf( random * 10 );
			else
				fromAccountType = "00";
		}
		else
			fromAccountType = "00";
		Pcode = "31" + fromAccountType + "00";
		MCC = "6011";
		TransactionType = "31";
		MandatoryPIN = true;
		TerminalType = "14";
	}
	
	public  void setDeposit()
	{
		String fromAccountType= null;
		logger.info( "Deposit is the choice" );
		rand = new Random();
		int random = rand.nextInt(10);
		if( random == 0 )
			random = 1;
		if( random % 2 == 0 )
		{
			if( ( random == 1 ) || ( random == 2 ) )
				fromAccountType = String.valueOf( random * 10 );
			else
				fromAccountType = "00";
		}
		else
			fromAccountType = "00";
		Pcode = "26" + fromAccountType + "00";
		MCC = "6011";
		TransactionType = "26";
		MandatoryPIN = true;
		TerminalType = "14";
	}

	public  void setPurchase()
	{
		logger.info( "Purchase is the choice" );
		Pcode = "000000";
		MCC = PosMCC;
		TransactionType = "00";
		if( Boolean.parseBoolean( RandomAmount ) == true )
			setRandomAmount( false );
		try
		{
		if( ( Double.parseDouble( AmountAuthorized ) > Double.parseDouble( BelowCVMLimit ) & PosEntryMode.equals( "71" ) ) | PosEntryMode.equals( "51" ) )
		{
			MandatoryPIN = true;
			logger.info( "Tap transaction above Below CVM Limit or on Contact interface" );
		}
		}catch( Exception E ){ 
			logger.error( "Exception ", E);
			MandatoryPIN = false;
		}
		TerminalType = "22";
	}
	
	public boolean isPINRequired()
	{
		return MandatoryPIN;
	}

	public String getAcquirerId()
	{ return AcquirerId; }

	public String getPcode()
	{ return Pcode; }

	public String getMCC()
	{ return MCC; }

	public String getExpiryDate()
	{ return ExpiryDate; }

	public String getAmountAuthorized()
	{ return AmountAuthorized; }

	public int getBelowCVMLimit()
	{ return Integer.parseInt( BelowCVMLimit ) ; }

	public String getPosEntryMode()
	{ return PosEntryMode; }

/*
	public String getTerminalType()
	{ return TerminalType; }

	public String getTransactionType()
	{ return TransactionType; }
*/
	public utilities()
	{
		dfu = new DataFormatterUtil();
	}

	public void initMessageBuffer( int size )
	{
		Record = new byte[size];
		Used = 0;
		logger.debug( "Buffer cleared. Counter set to 0" );
	}

	public void initMessageBuffer()
	{
		Record = new byte[2048];
		Used = 0;
	}

	public byte[] getCurrentBuffer()
	{ return Record; }

	public int getLuhnCheckDigit( String ccNumber )
	{
		int sum = 0;
		boolean alternate = true;
		for (int i = ccNumber.length() - 1; i >= 0; i--)
		{
			int n = Integer.parseInt(ccNumber.substring(i, i + 1));
			if (alternate)
			{
				n *= 2;
				if (n > 9)
				{
					n = (n % 10) + 1;
				}
			}
			sum += n;
			alternate = !alternate;
		}
		sum *= 9;
		return ( sum % 10 );
	}

	public void connectDB( String CfgFile )
	{
		getAllParameters( CfgFile );
		String Url = getParameterValue( "Config", "url" );
		String UserName = getParameterValue( "Config", "user" );
		String Password = getParameterValue( "Config", "pass" );
		try
		{
			DriverManager.registerDriver(new OracleDriver());
			connection = DriverManager.getConnection( Url, UserName, Password );
		}
		catch( Exception E)
		{
			logger.error( "Oracle Connection Exception here...", E );
		}
		logger.info( "Connected to Oracle" );
	}

	public Connection getDataBaseConnection()
	{
		return connection;
	}


	public void getAllParameters( String FileName )
	{
		try
		{
			FileReader cfgFile = new FileReader( FileName );
			BufferedReader cfgRead = new BufferedReader( cfgFile );
			String line ="";
			cfgBuf ="";

			xp = new XMLParser();

			while (( line = cfgRead.readLine() ) != null )
			{
				cfgBuf += line;
			}
			cfgRead.close();
		}
		catch( Exception E)
		{
			logger.error( "Exception ", E );
			System.exit(0);
		}
	}

	public String getParameterValue( String Section, String Parameter )
	{
		Vector v = null;
		String Value = null;
		try
		{
			v = xp.getXMLTagValue(cfgBuf, Section );
			if ( v.size() > 0 )
			{
				String Buf ="";
				Buf = v.elementAt(0).toString();
				v = xp.getXMLTagValue( Buf, Parameter );
				Value = v.elementAt(0).toString();
			}
		}
		catch( Exception E )
		{
			logger.error( "Exception ", E );
		}
		logger.debug( Parameter + " = " + Value );
		return Value;
	}

	public void ConnectSwitch( String CfgFile )
	{
		getAllParameters( CfgFile );
		String IP = getParameterValue( "TcpIP", "IP" );
		String Port = getParameterValue( "TcpIP", "Port" );

		try
		{
			clientSocket = new Socket( IP, Integer.parseInt( Port ) );
			logger.info( "Connected " + IP + ": " + Port );
		}
		catch( Exception E )
		{
			logger.error( "Exception ", E );
			System.exit(1);
		}
	}
	
	public String getCurrentDateTime( String format )
	{
		DateFormatter logDate = new DateFormatter( format );
		return logDate.getFormattedDate();
	}

	public String getTrace()
	{
		String Trace = null;
		try
		{
			statement = connection.createStatement();
			SQL = "select JPOSSEQ.Nextval trace from dual";
			ResultSet TraceResultSet = statement.executeQuery( SQL );
			while( TraceResultSet.next() )
			{
				Trace = TraceResultSet.getString( "trace" );
				break;
			}
			TraceResultSet.close();
			statement.close();
		}
		catch( Exception E )
		{
			logger.error( "Unable to get Trace number: ", E );
			System.exit(0);
		}
		return Trace;
	}

	public void initISOFields( String CfgFile )
	{
		getAllParameters( CfgFile );
		try { DE60 = getParameterValue( "ISO", "DE60"); } catch( Exception E ){ logger.error( "DE60 Exception ", E ); }
		try { DE61 = getParameterValue( "ISO", "DE61"); } catch( Exception E ){ logger.error( "DE61 Exception ", E ); }
		try { DE49 = getParameterValue( "ISO", "DE49"); } catch( Exception E ){ logger.error( "DE49 Exception ", E ); }
		try { DE125 = getParameterValue( "ISO", "DE125"); } catch( Exception E ){ logger.error( "DE125 Exception ", E ); }
	}

	public void parseTrack( String Track2 )
	{
		int delimiter = TrackII.indexOf( 'D' );
		if( delimiter == -1 )
			delimiter = TrackII.indexOf( '=' );
		PAN = TrackII.substring( 0, delimiter );
		ExpiryDate  = TrackII.substring( delimiter+1, delimiter+5 );
		logger.debug( "PAN " + PAN + " PIN " + PIN );
	}

	public void initCard( String CfgFile )
	{
		getAllParameters( CfgFile );
		try 
		{
			TrackII = getParameterValue( "Card", "TrackII"); 
			parseTrack( TrackII );
			/*
			int delimiter = TrackII.indexOf( 'D' );
			if( delimiter == -1 )
				delimiter = TrackII.indexOf( '=' );
			PAN = TrackII.substring( 0, delimiter );
			ExpiryDate  = TrackII.substring( delimiter+1, delimiter+5 );
			*/
		}
		catch( Exception E ) { logger.error( "TrackII Exception ", E);}
		try { if( PAN.isEmpty() ) PAN = getParameterValue( "Card", "PAN"); } catch( Exception E ){ logger.error( "PAN Exception ", E);}
		try { if( ExpiryDate.isEmpty() ) ExpiryDate = getParameterValue( "Card", "ExpiryDate"); } catch( Exception E ){ logger.error( "ExpiryDate Exception ", E);}
		try { PIN = getParameterValue( "Card", "PIN"); } catch( Exception E ){ logger.error( "PIN Exception ", E);}
	}

	public void initTerminal( String CfgFile )
	{
		getAllParameters( CfgFile );
		try { TPK = getParameterValue( "Terminal", "TPK"); TPK = TPK.concat( TPK.substring(0, 16)); logger.info( TPK ); } catch( Exception E ){ logger.error( "TPK Exception", E ); }
		try { TID = getParameterValue( "Terminal", "TID"); } catch( Exception E ){ logger.error( "TID Exception ", E);}
		try { MID = getParameterValue( "Terminal", "MID"); } catch( Exception E ){ logger.error( "MID Exception ", E);}
		try { AcceptorName = getParameterValue( "Terminal", "AcceptorName"); } catch( Exception E ){ logger.error( "AcceptorName Exception ", E);}
		try { AcquirerId = getParameterValue( "Terminal", "AcquirerId"); } catch( Exception E ){ logger.error( "AcquirerId Exception ", E);}
		try { PosMCC = getParameterValue( "Terminal", "PosMCC"); } catch( Exception E ){ 
		PosMCC = "5999"; logger.info( "Default 5999" );
		logger.error( "PosMCC Exception ", E);
		}
		try { PosEntryMode = getParameterValue( "Terminal", "PosEntryMode"); } catch( Exception E ){ logger.error( "PosEntryMode Exception ", E);}
		try { Nap = getParameterValue( "Terminal", "Nap"); } catch( Exception E ) { 
		Nap = "0";
		logger.error( "Nap Exception ", E);
		}
		try { Withdrawal = getParameterValue( "Terminal", "Withdrawal" ); } catch( Exception E ){ Withdrawal = "false"; logger.error( "Withdrawal Exception ", E);}
		try { Purchase = getParameterValue( "Terminal", "Purchase"); } catch( Exception E ){ Purchase = "false"; logger.error( "Purchase Exception ", E);}
		try { BalanceEnq = getParameterValue( "Terminal", "BalanceEnq"); } catch( Exception E ){ BalanceEnq = "false"; logger.error( "BalanceEnq Exception ", E);}
		try { RandomTransaction = getParameterValue( "Terminal", "RandomTransaction"); } catch( Exception E ){ RandomTransaction = "false"; logger.error( "RandomTransaction Exception ", E);}
		try { RandomReversal = getParameterValue( "Terminal", "RandomReversal"); } catch( Exception E ){RandomReversal = "false"; logger.error( "RandomReversal Exception ", E);}
		try { ForceReversal = getParameterValue( "Terminal", "ForceReversal"); } catch( Exception E ){ ForceReversal = "false"; logger.error( "ForceReversal Exception ", E);}
		try { RandomAmount = getParameterValue( "Terminal", "RandomAmount"); } catch( Exception E ){ RandomAmount = "false"; logger.error( "RandomAmount Exception ", E);}
		try { BelowCVMLimit = getParameterValue( "Terminal", "BelowCVMLimit" ); } catch( Exception E ){ BelowCVMLimit = "0"; logger.error( "BelowCVMLimit Exception ", E);}
		try { TerminalType = getParameterValue( "Terminal", "TerminalType" ); } catch( Exception E ){ logger.error( "TerminalType Exception ", E);}
		try { TerminalCountry = getParameterValue( "Terminal", "TerminalCountry"); } catch( Exception E ){ TerminalCountry = "634"; logger.error( "TerminalCountry Exception ", E);}
		try { TransactionCurrencyCode = getParameterValue( "Terminal", "TransactionCurrencyCode"); } catch( Exception E ){ TransactionCurrencyCode = "634"; logger.error( "TransactionCurrencyCode Exception ", E);}
		try { TerminalVerificationResults = getParameterValue( "Terminal", "TerminalVerificationResults"); } catch( Exception E ){ logger.error( "TerminalVerificationResults Exception ", E);}
		try { TerminalCapabilities = getParameterValue( "Terminal", "TerminalCapabilities"); } catch( Exception E ){ logger.error( "TerminalCapabilities Exception ", E);}
		try { AmountAuthorized = getParameterValue( "Terminal", "AmountAuthorized"); } catch( Exception E ){ logger.error( "AmountAuthorized Exception ", E);}
		try { AmountOther = getParameterValue( "Terminal", "AmountOther"); } catch( Exception E ){ logger.error( "AmountOther Exception ", E);}
		try { InterfaceDeviceSerialNumber = getParameterValue( "Terminal", "InterfaceDeviceSerialNumber"); } catch( Exception E ){ logger.error( "InterfaceDeviceSerialNumber Exception ", E);}
		try { CardholderVerificationMethodResults = getParameterValue( "Terminal", "CardholderVerificationMethodResults"); } catch( Exception E ){ logger.error( "CardholderVerificationMethodResults Exception ", E);}
		try { Attended = getParameterValue( "Terminal", "Attended"); } catch( Exception E ){ Attended = "false"; logger.error( "Attended Exception ", E);}
		try { OnPremises = getParameterValue( "Terminal", "Attended"); } catch( Exception E ){ OnPremises = "false"; logger.error( "OnPremises Exception ", E);}
		try { CustomerPresent = getParameterValue( "Terminal", "CustomerPresent"); } catch( Exception E ){ CustomerPresent = "false"; logger.error( "CustomerPresent Exception ", E);}
		try { CardPresent = getParameterValue( "Terminal", "CardPresent"); } catch( Exception E ){ CardPresent = "false"; logger.error( "CardPresent Exception ", E);}
		try { CardCaptureCapablity = getParameterValue( "Terminal", "CardCaptureCapablity"); } catch( Exception E ){ CardCaptureCapablity = "false"; logger.error( "CardCaptureCapablity Exception ", E);}
		try { Suspect = getParameterValue( "Terminal", "Suspect"); } catch( Exception E ){ Suspect = "false"; logger.error( "Suspect Exception ", E);}
		try { CardDataInputCapability = getParameterValue( "Terminal", "CardDataInputCapability"); } catch( Exception E ){ logger.error( "CardDataInputCapability Exception ", E);}
		try { ATMUnit = getParameterValue( "Terminal", "ATMUnit"); } catch( Exception E ){ logger.error( "ATMUnit Exception ", E);}
		try { OpKey = getParameterValue( "Terminal", "OpKey"); } catch( Exception E ){ logger.error( "OpKey Exception ", E);}
		try { WaitForResponse = getParameterValue( "Terminal", "WaitForResponse"); } catch( Exception E ){ WaitForResponse = "Y"; logger.error( "WaitForResponse Exception ", E);}
		try { ForcePcode = getParameterValue( "Terminal", "ForcePcode"); } catch( Exception E ){ ForcePcode = ""; logger.error( "ForcePcode Exception ", E);}
		try { BNAData = getParameterValue( "Terminal", "BNAData"); } catch( Exception E ){ BNAData = null; logger.error( "BNAData Exception ", E);}
		try { GenB = getParameterValue( "Terminal", "GenB"); } catch( Exception E ){ GenB = null; logger.error( "GenB Exception ", E); }
		try { GenC = getParameterValue( "Terminal", "GenC"); } catch( Exception E ){ GenC = null; logger.error( "GenC Exception ", E); }
		try { Deposit = getParameterValue( "Terminal", "Deposit"); } catch( Exception E ){ Deposit = "false"; logger.error( "Deposit Exception ", E);}
		try { PosConditionCode = getParameterValue( "Terminal", "PosConditionCode"); } catch( Exception E ){ PosConditionCode = "00"; logger.error( "PosConditionCode Exception ", E);}
		try { MsgType = getParameterValue( "Terminal", "MsgType"); } catch( Exception E ){ MsgType = "0200"; logger.error( "Msgtype Exception ", E);}
	}

	public void initChip( String CfgFile )
	{
		getAllParameters( CfgFile );
		try { ApplicationInterchangeProfile = getParameterValue( "Chip", "ApplicationInterchangeProfile"); } catch( Exception E ){ logger.error( "ApplicationInterchangeProfile Exception ", E);}
		try { ApplicationTransactionCounter = getParameterValue( "Chip", "ApplicationTransactionCounter"); } catch( Exception E ){ logger.error( "ApplicationTransactionCounter Exception ", E);}
		try { atc = Integer.parseInt( ApplicationTransactionCounter ); } catch( Exception E ){ logger.error( "atc Exception ", E);}
		try { IssuerApplicationData = getParameterValue( "Chip", "IssuerApplicationData"); } catch( Exception E ){ logger.error( "IssuerApplicationData Exception ", E);}
		try { PaymentScheme = getParameterValue( "Chip", "PaymentScheme"); } catch( Exception E ){ logger.error( "PaymentScheme Exception ", E);}
		try { DedicatedFileName = getParameterValue( "Chip", "DedicatedFileName"); } catch( Exception E ){ logger.error( "DedicatedFileName Exception ", E);}
		try { CryptogramInformationData = getParameterValue( "Chip", "CryptogramInformationData"); } catch( Exception E ){ logger.error( "CryptogramInformationData Exception ", E);}
		try { ApplicationPrimaryAccountNumberSequenceNumber = getParameterValue( "Chip", "ApplicationPrimaryAccountNumberSequenceNumber"); } catch( Exception E ){ logger.error( "ApplicationPrimaryAccountNumberSequenceNumber Exception ", E);}
		try { MDK = getParameterValue( "Chip", "MDK"); MDK = MDK.concat( MDK.substring(0, 16)); logger.info( MDK ); } catch( Exception E ){ logger.error( "MDK Exception ", E);}
		try { ApplicationVersionNumber = getParameterValue( "Chip", "ApplicationVersionNumber"); } catch( Exception E ){ logger.error( "ApplicationVersionNumber Exception ", E);}
		try { CVN = getParameterValue( "Chip", "CVN"); } catch( Exception E ){ logger.error( "CVN Exception ", E);}
	}

	public String prepareEMVData()
	{
		String ChipData = "";
		int CVN = dfu.hexToInt( this.CVN );
		logger.info( "CVN " + CVN );
		Cryptogram cvn = null;
		UnpredictableNumber = getUnpredicatableNumber();

		if( CVN == 10 )
			cvn = new CVN10( MDK, PAN, ApplicationPrimaryAccountNumberSequenceNumber, AmountAuthorized, AmountOther, TerminalCountry, TerminalVerificationResults, TransactionCurrencyCode, new DateFormatter("yyMMdd").getFormattedDate(), TransactionType, UnpredictableNumber, ApplicationInterchangeProfile, getCVR( IssuerApplicationData, PaymentScheme ), String.valueOf( atc ) );
		else if( CVN == 17 )
			cvn = new CVN17( MDK, PAN, ApplicationPrimaryAccountNumberSequenceNumber, AmountAuthorized, UnpredictableNumber, getCVR( IssuerApplicationData, PaymentScheme ), String.valueOf(atc ) );
		else if( CVN == 16 )
			cvn = new CVN16( MDK, PAN, ApplicationPrimaryAccountNumberSequenceNumber, AmountAuthorized, AmountOther, TerminalCountry, TerminalVerificationResults, TransactionCurrencyCode, new DateFormatter("yyMMdd").getFormattedDate(), TransactionType, UnpredictableNumber, ApplicationInterchangeProfile, getCVR( IssuerApplicationData, PaymentScheme ), String.valueOf( atc ) );
		else if( CVN == 5 )
			cvn = new CVN5( MDK, PAN, ApplicationPrimaryAccountNumberSequenceNumber, AmountAuthorized, AmountOther, TerminalCountry, TerminalVerificationResults, TransactionCurrencyCode, new DateFormatter("yyMMdd").getFormattedDate(), TransactionType, UnpredictableNumber, ApplicationInterchangeProfile, getCVR( IssuerApplicationData, PaymentScheme ), String.valueOf( atc ) );

		try
		{
			ApplicationCryptogram = new String( dfu.BCDToChar( cvn.getMAC() ) );
		}
		catch( Exception E )
		{
			logger.error( "Exception getMAC : ", E );
			System.exit(0);
		}
		logger.info( "AC: " + ApplicationCryptogram );
		ChipData = ChipData  + "9F26" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationCryptogram.length()/2 ).length() ).concat( dfu.intToHex( ApplicationCryptogram.length()/2 ) ).concat( ApplicationCryptogram );
		if( CryptogramInformationData.length() > 0 )
		{
			CryptogramInformationData = dfu.getPadString( '0', 2 ).substring( CryptogramInformationData.length() ).concat( CryptogramInformationData );
			ChipData = ChipData  + "9F27" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( CryptogramInformationData.length()/2 ).length() ).concat( dfu.intToHex( CryptogramInformationData.length()/2 ) ).concat( CryptogramInformationData );
		}

		if( DedicatedFileName.length() > 0 )
			ChipData = ChipData  + "84" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( DedicatedFileName.length()/2 ).length() ).concat( dfu.intToHex( DedicatedFileName.length()/2 ) ).concat( DedicatedFileName );

		if( CardholderVerificationMethodResults.length() > 0 )
			ChipData = ChipData  + "9F34" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( CardholderVerificationMethodResults.length()/2 ).length() ).concat( dfu.intToHex( CardholderVerificationMethodResults.length()/2 ) ).concat( CardholderVerificationMethodResults );

		if( TerminalType.length() > 0 )
		{
			TerminalType = dfu.getPadString( '0', 2 ).substring( TerminalType.length() ).concat( TerminalType );
			ChipData = ChipData  + "9F35" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TerminalType.length()/2 ).length() ).concat( dfu.intToHex( TerminalType.length()/2 ) ).concat( TerminalType );
		}

		if( ApplicationVersionNumber.length() > 0 )
			ChipData = ChipData  + "9F09" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationVersionNumber.length()/2 ).length() ).concat( dfu.intToHex( ApplicationVersionNumber.length()/2 ) ).concat( ApplicationVersionNumber );

		if( IssuerApplicationData.length() > 0 )
			ChipData = ChipData  + "9F10" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( IssuerApplicationData.length()/2 ).length() ).concat( dfu.intToHex( IssuerApplicationData.length()/2 ) ).concat( IssuerApplicationData );

		if( UnpredictableNumber.length() > 0 )
		{
			UnpredictableNumber = dfu.getPadString( '0', 8 ).substring( UnpredictableNumber.length() ).concat( UnpredictableNumber );
			ChipData = ChipData  + "9F37" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( UnpredictableNumber.length()/2 ).length() ).concat( dfu.intToHex( UnpredictableNumber.length()/2 ) ).concat( UnpredictableNumber );
		}

		if( ApplicationTransactionCounter.length() > 0 )
		{
			ApplicationTransactionCounter = dfu.getPadString( '0', 4 ).substring( ApplicationTransactionCounter.length() ).concat( ApplicationTransactionCounter );
			ChipData = ChipData  + "9F36" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationTransactionCounter.length()/2 ).length() ).concat( dfu.intToHex(ApplicationTransactionCounter.length()/2 ) ).concat( ApplicationTransactionCounter );
		}

		if( TerminalVerificationResults.length() > 0 )
		{
			TerminalVerificationResults = dfu.getPadString( '0', 10 ).substring( TerminalVerificationResults.length() ).concat( TerminalVerificationResults );
			ChipData = ChipData  + "95" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TerminalVerificationResults.length()/2 ).length() ).concat( dfu.intToHex( TerminalVerificationResults.length()/2 ) ).concat( TerminalVerificationResults );
		}

		TransactionDate = dfu.getPadString( '0', 6 ).substring( new DateFormatter("yyMMdd").getFormattedDate().length() ).concat( new DateFormatter("yyMMdd").getFormattedDate() );
		ChipData = ChipData  + "9A" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( new DateFormatter("yyMMdd").getFormattedDate().length()/2 ).length() ).concat( dfu.intToHex( new DateFormatter("yyMMdd").getFormattedDate().length()/2 ) ).concat( new DateFormatter("yyMMdd").getFormattedDate() );

		if( TransactionType.length() > 0 )
		{
			ChipData = ChipData  + "9C" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TransactionType.length()/2 ).length() ).concat( dfu.intToHex( TransactionType.length()/2 ) ).concat( TransactionType );
		}

		if( AmountAuthorized.length() > 0 )
		{
			AmountAuthorized = dfu.getPadString( '0', 12 ).substring( AmountAuthorized.length() ).concat( AmountAuthorized );
			ChipData = ChipData  + "9F02" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( AmountAuthorized.length()/2 ).length() ).concat( dfu.intToHex( AmountAuthorized.length()/2 ) ).concat( AmountAuthorized );
		}

		if( TransactionCurrencyCode.length() > 0 )
		{
			TransactionCurrencyCode = dfu.getPadString( '0', 4 ).substring( TransactionCurrencyCode.length() ).concat( TransactionCurrencyCode );
			ChipData = ChipData  + "5F2A" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TransactionCurrencyCode.length()/2 ).length() ).concat( dfu.intToHex( TransactionCurrencyCode.length()/2 ) ).concat( TransactionCurrencyCode );
		}

		if( ApplicationInterchangeProfile.length() > 0 )
		{
			ApplicationInterchangeProfile = dfu.getPadString( '0', 4 ).substring( ApplicationInterchangeProfile.length() ).concat( ApplicationInterchangeProfile );
			ChipData = ChipData  + "82" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( ApplicationInterchangeProfile.length()/2 ).length() ).concat( dfu.intToHex( ApplicationInterchangeProfile.length()/2 ) ).concat( ApplicationInterchangeProfile );
		}

		if( TerminalCountry.length() > 0 )
		{
			TerminalCountry = dfu.getPadString( '0', 4 ).substring( TerminalCountry.length() ).concat( TerminalCountry );
			ChipData = ChipData + "9F1A" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( TerminalCountry.length()/2 ).length() ).concat( dfu.intToHex( TerminalCountry.length()/2 ) ).concat( TerminalCountry );
		}

		if( AmountOther.length() > 0 )
		{
			AmountOther = dfu.getPadString( '0', 12 ).substring( AmountOther.length() ).concat( AmountOther );
			ChipData = ChipData  + "9F03" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( AmountOther.length()/2 ).length() ).concat( dfu.intToHex( AmountOther.length()/2 ) ).concat( AmountOther );
		}

		if( InterfaceDeviceSerialNumber.length() > 0 )
		{
			InterfaceDeviceSerialNumber = dfu.getPadString( '0', 16 ).substring( InterfaceDeviceSerialNumber.length() ).concat( InterfaceDeviceSerialNumber );
			ChipData = ChipData  + "9F1E" + dfu.getPadString( '0', 2 ).substring( dfu.intToHex( InterfaceDeviceSerialNumber.length()/2 ).length() ).concat( dfu.intToHex( InterfaceDeviceSerialNumber.length()/2 ) ).concat( InterfaceDeviceSerialNumber );
		}

		logger.info( "ChipData " + ChipData );
		int EMVDataLength = ChipData.length()/2;
		logger.info( "Chip Data Length " + EMVDataLength );
		return ChipData;
	}
	
	public void send()
	{
		try
		{
			DataOutputStream dOut = new DataOutputStream( clientSocket.getOutputStream() );
			dOut.write( Record, 0, Used );
		}
		catch( Exception E )
		{
			logger.error( "send() Error : ", E);
			return;
		}
	}

	public void getISOFields( byte[] dest )
	{
		System.arraycopy( Record, Used, dest, 0, dest.length );
		Used += dest.length;
	}

	public void getISOFields( byte[] dest, String Desc )
	{
		System.arraycopy( Record, Used, dest, 0, dest.length );
		Used += dest.length;
		printhexdump( dest, Desc );
	}

	public void receive()
	{
		int ResponseDataLength = 0;
		try
		{
			DataInputStream dIn = new DataInputStream( clientSocket.getInputStream() );
			Record = new byte[2];
			dIn.read( Record, 0, 2 );
			Used = 2;
			//hexdump( "Length", Record, Used );
			ResponseDataLength = dfu.hexToInt( new String( dfu.BCDToHex( Record ) ) );
			logger.info( "Length " + ResponseDataLength );
			Record = new byte[ResponseDataLength];
			dIn.read( Record );
			//Used = ResponseDataLength;
			//tracing.hexDump( "Response ", Record, Used );
			//tracing.hexDump( "Response ", Record );
			//parseResponse();
			Used = 0;
		}
		catch( Exception E )
		{
			logger.error( "receive Error : ", E);
			return;
		}
	}

	public int appendToRecord( byte[] dataToAppend )
	{
		//logger.debug( "Append at position " + Used );
		System.arraycopy( dataToAppend, 0,  Record, Used, dataToAppend.length );
		Used += dataToAppend.length;
		return Used;
	}

	public int appendToRecord( byte[] dataToAppend, int insPosition )
	{
		//logger.debug( "Append at position " + insPosition );
		System.arraycopy( dataToAppend, 0,  Record, insPosition, dataToAppend.length );
		//Used += dataToAppend.length;
		return Used;
	}

	public int appendToRecord( byte[] dataToAppend, String Desc )
	{
		printhexdump( dataToAppend, Desc );
		System.arraycopy( dataToAppend, 0,  Record, Used, dataToAppend.length );
		Used += dataToAppend.length;
		return Used;
	}

	public int appendToRecord( byte[] dataToAppend, int insPosition, String Desc  )
	{
		printhexdump( dataToAppend, Desc );
		logger.debug( "Append at position " + insPosition );
		System.arraycopy( dataToAppend, 0,  Record, insPosition, dataToAppend.length );
		return Used;
	}

	/*
	public int appendToRecord( byte dataToAppend )
	{
		Record[Used] = dataToAppend;
		Used++;
		return Used;
	}

	public int appendToRecord( byte dataToAppend, int insPosition  )
	{
		Record[insPosition] = dataToAppend;
		Used++;
		return Used;
	}
	*/

	String getCVR( String IAD, String PS )
	{
		int Length = 0;
		String Value = null;
		if( PS.equals( "VIS" ) )
		{
			Length = Integer.parseInt( IAD.substring(0, 2), 16 );
			Value = IAD.substring( 2, Length*2+2 );
			logger.debug( "DKI " + Value.substring(0,2) );
			logger.debug( "CVN " + Integer.parseInt( Value.substring(2,4), 16) );
			int CVN = Integer.parseInt( Value.substring(2,4), 16);
			Value = Value.substring( 4 );
			Length = Integer.parseInt( Value.substring(0, 2), 16 );
			String CVRLength = Value.substring(0, 2);
			Value = Value.substring( 2, Length*2+2 );
			logger.debug( "CVR " + Value );
			if( CVN == 10 )
				return CVRLength+Value;
			else if( CVN == 17 )
				return Value;
			return null;
		}
		else if( PS.equals( "MAS" ) )
		{
			logger.debug( "DKI " + IAD.substring(0, 2) );
			logger.debug( "CVN " + IAD.substring(2, 4) );
			/*
			Value = IAD.substring( 4 );
			Length = Integer.parseInt( Value.substring(0, 2), 16 );
			Value = Value.substring( 2, Length*2+2 );
			return "";
			*/
			logger.debug( "CVR " + IAD.substring( 4, 16 ) );
			return IAD.substring( 4, 16 );
		}
		else if( PS.equals( "DIN" ) )
		{
			//Value = IAD.substring( 4 );
			logger.debug( "DKI " + IAD.substring(0,2) );
			logger.debug( "CVN " + Integer.parseInt( IAD.substring(2,4), 16) );
			//Length = Integer.parseInt( IAD.substring(0, 2), 16 );
			return IAD;
		}
		return "";
	}

	public double getExchangeRate( String AcqCurrency )
	{
		double exchg_rate = 0;
		String SQL = "select to_char( ( 1/sell_rate), '0.9999') sell_rate from istcurr where currency = to_number( '" + AcqCurrency + "', '999' )";
		logger.info( SQL );
		try
		{
			statement = connection.createStatement();
			resultSet = statement.executeQuery( SQL );
			while( resultSet.next() )
			{
				//exchg_rate = resultSet.getFloat( "sell_rate" );
				exchg_rate =  BigDecimal.valueOf( new Double( resultSet.getFloat( "sell_rate" ) ).doubleValue() ).setScale( 4, RoundingMode.HALF_UP ).doubleValue();
				resultSet.close();
				break;
			}
			resultSet.close();
			statement.close();
		}
		catch( Exception E )
		{
			logger.error( "Exception in getExchangeRate", E);
		}
		logger.info( "exchange rate " + exchg_rate );
		return exchg_rate;
	}
	
	public int getCurrencyDecimals( String AcqCurrency )
	{
		int decimal = 0;
		SQL = "select o_decimal from istcurpnt where currency = to_number('" + AcqCurrency + "', '999' )";
		logger.info( SQL );
		try
		{
			statement = connection.createStatement();
			resultSet = statement.executeQuery( SQL );
			while( resultSet.next() )
			{
				decimal = resultSet.getInt( "o_decimal" );
				logger.info( "decimal " + decimal );
				resultSet.close();
				break;
			}
			resultSet.close();
			statement.close();
		}
		catch( Exception E )
		{
			logger.error( "Exception in getCurrencyDecimals", E);
		}
		
		logger.info( "Decimal " + decimal );
		return decimal;
	}

	public String fmtAmtNoDecimal( String Amount, int decimal )
	{
		return String.valueOf( new Double( BigDecimal.valueOf( Double.valueOf( Amount ).doubleValue() * Math.pow( 10, -1*decimal ) ).setScale( decimal, RoundingMode.HALF_UP ).doubleValue() * Math.pow( 10, decimal ) ).intValue() );
	}

	public String ConvertAmtNoDecimal( String Amount, double exchg_rate, int decimal )
	{
		return String.valueOf( new Double( BigDecimal.valueOf( Double.valueOf( Amount ).doubleValue() * Math.pow( 10, -1*decimal ) / Double.valueOf( exchg_rate ).doubleValue() ).setScale( decimal, RoundingMode.HALF_UP ).doubleValue() * Math.pow( 10, decimal ) ).intValue() );
	}

	public String fmtNoDecimal( String Source )
	{
		int pos = Source.indexOf( '.' );
		int exponent = Source.length() - pos;
		return( String.valueOf( new Double( Double.parseDouble( Source ) * Math.pow( 10, exponent ) ).intValue() ) );
	}
	
	public int getDecimalization( String Source )
	{
		int pos = Source.indexOf( '.' );
		return Source.length() - pos;
	}

	public boolean isReversalRequired()
	{
		Random rand = new Random();
		int random = rand.nextInt(20);
		if( random == 0 )
			random = 1;
		if( random == 5 || random == 15 )
			return true;
		else
			return false;
	}

	public void printhexdump( byte[] source, String Desc )
	{
		logger.debug( "------" + Desc + "---------" );
		OutputStream os = new ByteArrayOutputStream();
		try{ HexDump.dump( source, 0, os, 0 );}catch( Exception E ) {}
		logger.debug( os.toString() );
		logger.debug( "------" + Desc + "---------" );
	}

	public void DBcleanup() throws Exception
	{
		connection.close();
		statement.close();
	}

	public String getUnpredicatableNumber()
	{
		rand = new Random();
		String unpredictable = "";
		for( int i=0; i<8; i++)
				unpredictable = unpredictable.concat( dfu.intToHex( rand.nextInt(16) ) );
		logger.debug( "Unpredicatble number generated : " + unpredictable );
		return unpredictable;
	}

	public Statement getstatement()
	{ return statement; }

	public void setAcceptorName( String AcceptorName)
	{
		this.AcceptorName = AcceptorName;
		logger.debug( "AcceptorName : " + this.AcceptorName );
	}
	
	public void setPosEntryMode( String PosEntryMode )
	{
		this.PosEntryMode = PosEntryMode;
		logger.debug( "PosEntryMode : " + this.PosEntryMode );
	}
	
	public void setDE60( String DE60 )
	{
		this.DE60 = DE60;
		logger.debug( "DE60 : " + this.DE60 );
	}
	
	public void setDE61( String DE61)
	{
		this.DE61 = DE61;
		logger.debug( "DE61 : " + this.DE61 );
	}
	public void setAcquirerId( String AcquirerId )
	{
		this.AcquirerId = AcquirerId;
		logger.debug( "AcquirerId : " + this.AcquirerId );
	}
	public void setPosMCC( String PosMCC )
	{
		this.PosMCC = PosMCC;
		logger.debug( "PosMCC : " + this.PosMCC );
	}
}
