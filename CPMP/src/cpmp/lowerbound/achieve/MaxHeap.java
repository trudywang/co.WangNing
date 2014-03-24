package cpmp.lowerbound.achieve;

public class MaxHeap implements Cloneable
{
	private int[] data;
	public int size;

	public MaxHeap(int n)
	{
		size = 0;
		data = new int[n];

	}

	public MaxHeap clone() throws CloneNotSupportedException
	{

		MaxHeap mh = (MaxHeap) super.clone();
		mh.data = data.clone();

		return mh;

	}

	public void add(int v)
	{
		data[size++] = v;
		lift(size - 1);
	}

	private void lift(int k)
	{
		if (k == 0)
			return;
		int f = (k - 1) / 2;

		while (data[f] < (data[k]))
		{
			swap(f, k);
			k = f;
			if (k == 0)
				break;
			f = (k - 1) / 2;
		}
	}

	public int top()
	{
		return data[0];
	}

	private void setValue(int k, int p)
	{
		data[k] = p;

	}

	public int pop()
	{
		int ret = data[0];
		data[0] = data[--size];
		sink(0);
		return ret;
	}

	private void sink(int k)
	{
		int l = k * 2 + 1;
		int r = l + 1;
		if (l >= size)
		{
			return;
		}
		else if (r == size)
		{
			if (data[k] < (data[l]))
			{
				swap(k, l);
				return;
			}
		}
		else
		{
			if (data[k] >= (data[l]) && data[k] >= (data[r]))
				return;
			if (data[l] > (data[r]))
			{
				swap(k, l);
				sink(l);
			}
			else
			{
				swap(k, r);
				sink(r);
			}
		}

	}

	private void swap(int i, int j)
	{
		int pi = data[i];
		int pj = data[j];
		setValue(i, pj);
		setValue(j, pi);
	}

}
