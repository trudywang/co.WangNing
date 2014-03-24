package cpmp;

public class Pair<L, R extends Comparable<R>> implements Comparable<Pair<L, R>>
{
	private L l;
	private R r;

	public Pair(L l, R r)
	{
		this.l = l;
		this.r = r;
	}

	public L getL()
	{
		return l;
	}

	public R getR()
	{
		return r;
	}

	public void setL(L l)
	{
		this.l = l;
	}

	public void setR(R r)
	{
		this.r = r;
	}

	public int compareTo(Pair<L, R> o)
	{
		return r.compareTo(o.r);
	}
}
