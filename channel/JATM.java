package channel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.io.HexDump;
import adssim.util.*;
import adssim.util.crypto.*;

class JATM
{
	DataFormatterUtil dfu;
	boolean TxnAvailable;
	static Logger logger = Logger.getLogger( adssim.channel.JATM.class );
	utilities o_utilities;

	public JATM()
	{
		dfu = new DataFormatterUtil();
		o_utilities = new utilities();
		PropertyConfigurator.configure("log4j.properties");
		logger.info("parent class print start");
		o_utilities.connectDB( "jatm.cfg" );
		o_utilities.ConnectSwitch( "jatm.cfg" );
		o_utilities.initISOFields( "jatm.cfg" );
		o_utilities.initCard( "jatm.cfg" );
		o_utilities.initTerminal( "jatm.cfg" );
		o_utilities.initChip( "jatm.cfg" );
		logger.info( "OK" );
	}

	void prepareMsg()
	{
		o_utilities.initMessageBuffer(1024);
		o_utilities.updateTransactionData();
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "00000000" ), 8 ) );
		//o_utilities.appendToRecord( dfu.charToByte( "0000".toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "11".toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getATMUnit().toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( "12".toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( ";".toCharArray() ) );
		String TrackII = o_utilities.getTrackII();
		o_utilities.appendToRecord( dfu.charToByte( TrackII.toCharArray() ) );
		o_utilities.appendToRecord( dfu.charToByte( "?".toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getOpKey().toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getAmountAuthorized().toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );

                //byte[] encPinBlock = o_utilities.getPINBlock();
                byte[] encPinBlock = dfu.UnpackedToAscii( dfu.BCDToUnpacked( o_utilities.getPINBlock() ) );;
		if( encPinBlock.length >= 8 )
		{
			o_utilities.appendToRecord( encPinBlock, "PIN" );
			logger.info( "PIN present" );
                }

		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getGenB().toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getGenC().toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		//t1
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		if( ( Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 5 ) || ( Integer.parseInt( o_utilities.getPosEntryMode() ) /10 == 7 ) )
		{
			String dynamicChipData = "5CAM0004"; /*.concat( o_utilities.prepareEMVData() );
			logger.debug( "Add 5F34 for terminal" );
			dynamicChipData = dynamicChipData + "5F3401" + o_utilities.getApplicationPrimaryAccountNumberSequenceNumber();
			//int EMVDataLength = dynamicChipData.length()/2;
			//dynamicChipData = dfu.getPadString( '0', 4 ).substring( String.valueOf( EMVDataLength ).length() ).concat( String.valueOf( EMVDataLength ) ).concat( dynamicChipData );
			//o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( dynamicChipData ), 0 ), "DE55" );
			o_utilities.appendToRecord( dfu.charToByte( dynamicChipData.toCharArray() ) );
			*/
		}

		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getBNA().toCharArray() ) );

		byte[] hexaLength = dfu.charToByte( dfu.leftPad( String.valueOf( o_utilities.getMessageSize() ), 4, '0' ).toCharArray() );
		o_utilities.appendToRecord( hexaLength, 0 );
		//tracing.hexDump( "outgoing", Record, Used );
		o_utilities.printhexdump( o_utilities.getCurrentBuffer(), "outgoing message" );
	}

	void prepareSolMsg()
	{
		o_utilities.initMessageBuffer(512);
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "00000000" ), 8 ) );
		o_utilities.appendToRecord( dfu.charToByte( "22".toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.charToByte( o_utilities.getATMUnit().toCharArray() ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "1C" ), 0 ) );
		o_utilities.appendToRecord( dfu.hexToBCD( dfu.asciiToHex( "39" ), 0 ) );
		byte[] hexaLength = dfu.charToByte( dfu.leftPad( String.valueOf( o_utilities.getMessageSize() ), 4, '0' ).toCharArray() );
		o_utilities.appendToRecord( hexaLength, 0 );
		o_utilities.printhexdump( o_utilities.getCurrentBuffer(), "outgoing message" );
		//tracing.hexDump( "outgoing", Record, Used );
		//setRandomAmount();
		try { Thread.sleep( Integer.parseInt( o_utilities.getNap() ) ); } catch( Exception E ) { logger.error( "Error : ", E ); }
	}

        public void process( int counter )
        {
		logger.info( "-----Start-----" );
		for( int i=0; i<counter; i++ )
		{
			prepareMsg();
			o_utilities.send();
			o_utilities.receive();
			prepareSolMsg();
			o_utilities.send();
		}
	}

	public static void main( String[] args )
	{
		int counter = 0;
		JATM obj = new JATM();
		if( args.length == 0 )
			counter = 1;
		else
			counter = Integer.parseInt( args[0] );
		try
		{
			obj.process( counter );
		}
		catch( Exception E )
		{
			logger.error( "Error : ", E );
		}
	}

}
