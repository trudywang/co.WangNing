package cpmp.lowerbound.achieve;

import java.util.Arrays;

import cpmp.Pair;

public class MaxHeapRevertable implements Cloneable
{
	private Pair<Integer, Integer>[] data;
	public int size;
	public int maxID;
	private int[] idToIndex;

	@SuppressWarnings("unchecked")
	public MaxHeapRevertable(int n)
	{
		size = 0;

		data = new Pair[n];
		idToIndex = new int[n + 1];
		Arrays.fill(idToIndex, -1);
	}

	public MaxHeapRevertable clone() throws CloneNotSupportedException
	{

		MaxHeapRevertable mh = (MaxHeapRevertable) super.clone();
		mh.data = data.clone();
		mh.idToIndex = idToIndex.clone();
		return mh;

	}

	public void add(Pair<Integer, Integer> v)
	{
		idToIndex[v.getL()] = size;
		data[size++] = v;
		lift(size - 1);
	}

	private void lift(int k)
	{
		if (k == 0)
			return;
		int f = (k - 1) / 2;
		if (data[k] == null || data[f] == null)
			data[k] = null;
		while (data[f].compareTo(data[k]) < 0)
		{
			swap(f, k);
			k = f;
			if (k == 0)
				break;
			f = (k - 1) / 2;
		}
	}

	public Pair<Integer, Integer> top()
	{
		return data[0];
	}

	private void setValue(int k, Pair<Integer, Integer> p)
	{
		data[k] = p;
		idToIndex[p.getL()] = k;
	}

	public Pair<Integer, Integer> pop()
	{
		return deleteIndex(0);
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
			if (data[k].compareTo(data[l]) < 0)
			{
				swap(k, l);
				return;
			}
		}
		else
		{
			if (data[k].compareTo(data[l]) >= 0 && data[k].compareTo(data[r]) >= 0)
				return;
			if (data[l].compareTo(data[r]) > 0)
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

	public Pair<Integer, Integer> deleteID(int id)
	{
		return deleteIndex(idToIndex[id]);
	}

	private Pair<Integer, Integer> deleteIndex(int k)
	{

		Pair<Integer, Integer> p = data[k];
		idToIndex[p.getL()] = -1;

		setValue(k, data[--size]);
		data[size] = null;
		if (k < size)
		{
			lift(k);
			sink(k);
		}
		return p;
	}

	private void swap(int i, int j)
	{
		Pair<Integer, Integer> pi = data[i];
		Pair<Integer, Integer> pj = data[j];
		setValue(i, pj);
		setValue(j, pi);
	}

}
