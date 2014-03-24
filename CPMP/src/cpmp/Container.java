package cpmp;

public class Container implements Comparable<Container>
{
	public int groupLabel;
	public int uniqueContainerIndex;

	public Container(int g, int l)
	{
		groupLabel = g;
		uniqueContainerIndex = l;
	}

	@Override
	public int compareTo(Container o)
	{
		// TODO Auto-generated method stub
		return uniqueContainerIndex - o.uniqueContainerIndex;
	}

	public boolean equals(Container o)
	{
		return uniqueContainerIndex == o.uniqueContainerIndex;
	}

	public boolean equals(Object o)
	{
		Container c = (Container) o;
		return uniqueContainerIndex == c.uniqueContainerIndex;
	}
}
