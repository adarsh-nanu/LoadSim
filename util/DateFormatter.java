package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatter
{
    SimpleDateFormat format;
    Date today;
    public DateFormatter( String format )
    {
        this.format = new SimpleDateFormat( format );
        today = new Date();
    }

    public String getFormattedDate()
    {
        return format.format( today );
    }

    public DateFormatter( String format, String Tz )
    {
        this.format = new SimpleDateFormat( format );
		this.format.setTimeZone( TimeZone.getTimeZone( Tz ) );
        today = new Date();
    }
}
