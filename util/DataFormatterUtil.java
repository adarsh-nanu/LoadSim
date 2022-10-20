package util;

public class DataFormatterUtil {

    public String leftPad( String origString, int totLength, char padWith )
    {
        return getPadString( padWith, totLength ).substring( 0, totLength - origString.length() ).concat( origString );
    }

    public String rightPad( String origString, int totLength, char padWith )
    {
        return origString.concat( getPadString( padWith, totLength ).substring( 0, totLength - origString.length() ) );
    }

    public Byte[] byteToByte(byte[] b) {
        Byte[] B = new Byte[b.length];
        for (int i = 0; i < b.length; i++)
            B[i] = b[i];
        return B;
    }

    public byte[] ByteTobyte(Byte[] B) {
        byte[] b = new byte[B.length];
        for (int i = 0; i < B.length; i++)
            b[i] = B[i];
        return b;
    }

    public char[] makeReadable(byte[] sourceData) {
        char[] convString = new char[sourceData.length];
        for (int i = 0; i < sourceData.length; i++) {
            if (sourceData[i] >= 0x00 && sourceData[i] <= 0x09)
                convString[i] += 0x30;
            else if (sourceData[i] >= 0x0A && sourceData[i] <= 0x0F)
                convString[i] += 0x31;
            else convString[i] = (char) sourceData[i];
        }
        return convString;
    }

    public byte[] charToByte(char[] source) {
        byte[] destBytes = new byte[source.length];
        for (int i = 0; i < source.length; i++)
            destBytes[i] = (byte) source[i];
        return destBytes;
    }

    public char[] byteToChar(byte[] source) {
        char[] destBytes = new char[source.length];
        for (int i = 0; i < source.length; i++)
            destBytes[i] = (char) source[i];
        return destBytes;
    }

    public String intToHex(int integer) {
        return Integer.toHexString(integer);
    }

    public String getPadString(char padWith, int stringSize) {
        return new String(new char[stringSize]).replace('\0', padWith);
    }

    public int hexToInt(String string) {
        return Integer.parseInt(string, 16);
    }

    public byte[] BCDToHex(byte[] BCD) {
        char[] charArray = new char[BCD.length * 2];
        for (int i = 0, j = 0; i < BCD.length; i++, j += 2) {
            byte lnibble = (byte) (BCD[i] >> 4);
            lnibble = (byte) (lnibble & 0x0F);
            byte rnibble = (byte) (BCD[i] & 0x0F);

            if (lnibble >= (byte) 0x00 && lnibble <= (byte) 0x09)
                lnibble += 0x30;
            else if (lnibble >= (byte) 0x0a && lnibble <= (byte) 0x0f)
                lnibble += 0x37;

            if (rnibble >= (byte) 0x00 && rnibble <= (byte) 0x09)
                rnibble += 0x30;
            else if (rnibble >= (byte) 0x0a && rnibble <= (byte) 0x0f)
                rnibble += 0x37;

            charArray[j] = (char) lnibble;
            charArray[j + 1] = (char) rnibble;
        }
        return charToByte( charArray );
    }

    public char[] BCDToHex(byte BCD) {
        char[] charArray = new char[2];
        byte lnibble = (byte) (BCD >> 4);
        lnibble = (byte) (lnibble & 0x0F);
        byte rnibble = (byte) (BCD & 0x0F);

        if (lnibble >= (byte) 0x00 && lnibble <= (byte) 0x09)
            lnibble += 0x30;
        else if (lnibble >= (byte) 0x0a && lnibble <= (byte) 0x0f)
            lnibble += 0x37;

        if (rnibble >= (byte) 0x00 && rnibble <= (byte) 0x09)
            rnibble += 0x30;
        else if (rnibble >= (byte) 0x0a && rnibble <= (byte) 0x0f)
            rnibble += 0x37;


        charArray[0] = (char) lnibble;
        charArray[1] = (char) rnibble;
        return charArray;
    }

    public byte[] hexToBCD(String Hex, int minSize) {
        int size = Hex.length();
        String padString = getPadString('0', minSize);
        if (minSize > 0)
            Hex = padString.substring(size).concat(Hex);
        char[] hexArray = Hex.toCharArray();
        byte[] byteArray = new byte[Hex.length()];
        byte[] bcdArray = new byte[Hex.length() / 2];
        for (int i = 0; i < Hex.length(); i++) {
            if (hexArray[i] >= '0' && hexArray[i] <= '9')
                hexArray[i] -= '0';
            else if (hexArray[i] >= 'a' && hexArray[i] <= 'f')
                hexArray[i] -= 'W';
            byteArray[i] = (byte) hexArray[i];
        }
        for (int i = 0, j = 0; i < Hex.length(); i += 2, j++) {
            bcdArray[j] = (byte) (byteArray[i] << 4 | byteArray[i + 1]);
        }
        return bcdArray;
    }

    public String asciiToHex(String asciiString) {
        char[] charArray = asciiString.toCharArray();
        for (int i = 0; i < asciiString.length(); i++) {
            if (charArray[i] >= '0' && charArray[i] <= '9')
                charArray[i] -= '0';
            else if (charArray[i] >= 'A' && charArray[i] <= 'F')
                charArray[i] -= '7';
            else if (charArray[i] >= 'a' && charArray[i] <= 'f')
                charArray[i] -= 'W';
        }
        return new String(charArray);
    }

    public byte[] stringToBytes(String string) {
        char charArray[] = string.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        return byteArray;
    }

    public char[] BCDToChar(byte[] BCD) {
        char[] charArray = new char[BCD.length * 2];
        for (int i = 0, j = 0; i < BCD.length; i++, j += 2) {
            byte lnibble = (byte) (BCD[i] >> 4);
            lnibble = (byte) (lnibble & 0x0F);
            byte rnibble = (byte) (BCD[i] & 0x0F);

            if (lnibble >= (byte) 0x00 && lnibble <= (byte) 0x09)
                lnibble += 0x30;
            else if (lnibble >= (byte) 0x0a && lnibble <= (byte) 0x0f)
                lnibble += 0x37;

            if (rnibble >= (byte) 0x00 && rnibble <= (byte) 0x09)
                rnibble += 0x30;
            else if (rnibble >= (byte) 0x0a && rnibble <= (byte) 0x0f)
                rnibble += 0x37;


            charArray[j] = (char) lnibble;
            charArray[j + 1] = (char) rnibble;
        }
        return charArray;
    }

    public char[] BCDToChar(byte BCD) {
        char[] charArray = new char[2];
        byte lnibble = (byte) (BCD >> 4);
        lnibble = (byte) (lnibble & 0x0F);
        byte rnibble = (byte) (BCD & 0x0F);

        if (lnibble >= (byte) 0x00 && lnibble <= (byte) 0x09)
            lnibble += 0x30;
        else if (lnibble >= (byte) 0x0a && lnibble <= (byte) 0x0f)
            lnibble += 0x37;

        if (rnibble >= (byte) 0x00 && rnibble <= (byte) 0x09)
            rnibble += 0x30;
        else if (rnibble >= (byte) 0x0a && rnibble <= (byte) 0x0f)
            rnibble += 0x37;

        charArray[0] = (char) lnibble;
        charArray[1] = (char) rnibble;
        return charArray;
    }

    public byte[] BCDToUnpacked(byte[] BCD) {
        char[] charArray = new char[BCD.length * 2];
        for (int i = 0, j = 0; i < BCD.length; i++, j += 2) {
            byte lnibble = (byte) (BCD[i] >> 4);
            lnibble = (byte) (lnibble & 0x0F);
            byte rnibble = (byte) (BCD[i] & 0x0F);

            charArray[j] = (char) lnibble;
            charArray[j + 1] = (char) rnibble;
        }
        return charToByte( charArray );
    }

	public byte[] UnpackedToAscii( byte[] source )
	{
		byte[] dest = new byte[ source.length ];
		for( int i=0; i<source.length; i++ )
		{
			if( source[i] >= 0x00 && source[i] <= 0x09 )
				dest[i] = (byte)( source[i] + (byte)0x30 );
			else if( source[i] >= 0x0A && source[i] <= 0x0F )
				dest[i] = (byte)( source[i] + (byte)0x30 );

		}
		return dest;
	}

    public byte[] hexToBCD( byte[] source )
    {
        byte[] converted = new byte[source.length/2];
		int j=0;
        for( int i=0; i<source.length; i++ )
        {
            if( source[i] >= 'A' && source[i] <= 'F' )
                converted[j] = (byte)( source[i]-(byte)55 );
            else if( source[i] >= '0' && source[i] <= '9' )
                converted[j] = (byte)( source[i]-(byte)48 );
            converted[j] <<= 4;

            if( source[i+1] >= 'A' && source[1+i] <= 'F' )
                converted[j] |= (byte)( source[i+1]-(byte)55 );
            else if( source[i+1] >= '0' && source[i+1] <= '9' )
                converted[j] |= (byte)( source[i+1]-(byte)48 );

			j++;
            i++;
        }
        return converted;
    }

}
