package util;

public class Allowed
{

        String[] AllowedValues;

        public boolean isAllowed( String Value )
        {
                for ( int i=0; i<AllowedValues.length; i++ )
                        if( AllowedValues[i].equals( Value ) )
                                return true;
                return false;
        }

        public Allowed( String AllowedValues, String delimiter )
        {
                this.AllowedValues = AllowedValues.split( delimiter );
        }
}
