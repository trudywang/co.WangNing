package com.jinbostar.cpmp.common;

public class Tuple
{

	public static int compare(int[] a, int[] b)
	{
		assert a.length == b.length;
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return Integer.compare(a[i], b[i]);
		return 0;
	}


	public static boolean prune(int[] best, int[] curr, int len)
	{
		if (best == null)
			return false;
		for (int i = 0; i < len; i++)
		{
			if (best[i] > curr[i])
				return false;
			if (best[i] < curr[i])
				return true;
		}
		return false;
	}
}
