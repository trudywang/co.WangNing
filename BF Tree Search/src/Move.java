/**
 * Created by jinbo on 8/30/14.
 */
public class Move
{
	public int s1, s2;

	public Move(int s1, int s2)
	{
		this.s1 = s1;
		this.s2 = s2;
	}

	public Move reverse()
	{
		return new Move(s2, s1);
	}
}
