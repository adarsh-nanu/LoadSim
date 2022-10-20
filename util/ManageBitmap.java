package util;

public class ManageBitmap {
    byte[] bitMask;

    public ManageBitmap() {
        bitMask = new byte[9];
        bitMask[1] = (byte) 128;
        bitMask[2] = 64;
        bitMask[3] = 32;
        bitMask[4] = 16;
        bitMask[5] = 8;
        bitMask[6] = 4;
        bitMask[7] = 2;
        bitMask[8] = 1;
    }

    public boolean checkIsBitOn(byte[] bitmap, int bit) {
        int Bit = 0;
        int Byte = 0;
        Byte = bit / 8;
        if ((Bit = bit % 8) == 0) {
            Bit = 8;
            Byte--;
        }
        //System.out.println("checkIsBitOn " +  bit + " = Byte[" + Byte + "] Bit[" + Bit +"]" );
        if ((bitmap[Byte] & bitMask[Bit]) == 0)
            return false;
        return true;
    }

    public void setBitOn(byte[] bitmap, int bit) {
        int Bit = 0;
        int Byte = 0;
        Byte = bit / 8;
        if ((Bit = bit % 8) == 0) {
            Bit = 8;
            Byte--;
        }
        //System.out.println( "setBitOn "  + bit + " = Byte[" + Byte + "] Bit[" + Bit +"]");
        bitmap[Byte] |= bitMask[Bit];
    }

    public byte getComplementMask(int bit) {
        return (byte) ~bitMask[bit];
    }

    public void setBitOff(byte[] bitmap, int bit) {
        int Bit = 0;
        int Byte = 0;
        Byte = bit / 8;
        if ((Bit = bit % 8) == 0) {
            Bit = 8;
            Byte--;
        }
        //System.out.println( "setBitOff " + bit + " = Byte[" + Byte + "] Bit[" + Bit +"]");
        byte mask = getComplementMask(Bit);
        bitmap[Byte] &= mask;
    }
}
