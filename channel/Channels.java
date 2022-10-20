package channel;

public interface Channels
{
	public void parseResponse();
	public void prepareMsg();
	public void process( int counter );
}
