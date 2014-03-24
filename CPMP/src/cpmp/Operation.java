package cpmp;

public class Operation
{
	public Container container;
	public int from;// (from,0): fix; (-from,0):unfix;
	public int to;

	public boolean equalTo(Operation x)
	{
		if (x.container.uniqueContainerIndex != container.uniqueContainerIndex || x.from != from || x.to != to)
			return false;
		return true;
	}

	public Operation(Container c, int f, int t)
	{
		container = c;
		from = f;
		to = t;
	}

	public boolean isFixing()
	{
		return to == 0 && from > 0;
	}

	public boolean isUnfixing()
	{
		return to == 0 && from < 0;
	}

	public boolean isRelocation()
	{
		return to != 0;
	}

	public String toString()
	{
		return "" + container.groupLabel + "(" + container.uniqueContainerIndex + ") : " + from + "->" + to;
	}
}
