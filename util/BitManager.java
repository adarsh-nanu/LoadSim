package util;

public class BitManager {
    byte[] bitmask;
    public BitManager()
    {
        bitmask = new byte[9];
        bitmask[8] = (byte) 128;
        bitmask[7] = 64;
        bitmask[6] = 32;
        bitmask[5] = 16;
        bitmask[4] = 8;
        bitmask[3] = 4;
        bitmask[2] = 2;
        bitmask[1] = 1;
    }

    public boolean checkIsBitOn( byte data, int bit )
    {
        /*
        if( (int)( data & bitmask[bit] ) == 0 )
            return false;
        return true;
        */
        int res = 0;
        res = data & bitmask[bit];
        if( res == 0 )
            return false;
        else return true;
    }

    public byte setBiton( byte Byte, int Bit)
    {
        return ( Byte |= bitmask[Bit] );
    }

    public byte getComplementMask( int bit )
    {
        return (byte) ~bitmask[ bit ];
    }

    public byte setBitOff( byte Byte, int Bit )
    {
        return Byte &= getComplementMask( Bit );
    }

}
