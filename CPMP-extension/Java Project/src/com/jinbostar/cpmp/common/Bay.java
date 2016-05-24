package com.jinbostar.cpmp.common;

public class Bay implements Cloneable
{
	public int S;
	public int H;
	public int P;
	public int N;
	public int[][] bay;
	public int[] h;//height
	public int[] orderly;
	private int[][] maxDisorderly;//


	public Bay clone() throws CloneNotSupportedException
	{

		Bay ret = (Bay) super.clone();
		ret.bay = new int[S + 1][H + 1];
		ret.maxDisorderly = new int[S + 1][H + 1];
		ret.h = h.clone();
		ret.orderly = orderly.clone();
		for (int s = 1; s <= S; s++)
		{
			ret.bay[s] = bay[s].clone();
			ret.maxDisorderly[s] = maxDisorderly[s].clone();
		}

		return ret;
	}


	public Bay(int S, int H, int P, int N, int[][] dataBay)
	{
		this.S = S;
		this.H = H;
		this.P = P;
		this.N = N;
		bay = new int[S + 1][H + 1];
		for (int s = 1; s <= S; s++)
			for (int t = 1; t <= H; t++)
				bay[s][t] = dataBay[s][t];

		h = new int[S + 1];
		orderly = new int[S + 1];
		maxDisorderly = new int[S + 1][H + 1];
		for (int s = 1; s <= S; s++)
		{
			bay[s][0] = P;
			h[s] = 0;
			while (h[s] != H && bay[s][h[s] + 1] != 0)
				h[s]++;
			orderly[s] = 0;
			while (orderly[s] != h[s] && bay[s][orderly[s]] >= bay[s][orderly[s] + 1])
				orderly[s]++;

			for (int t = orderly[s] + 1; t <= h[s]; t++)
			{
				maxDisorderly[s][t] = Math.max(maxDisorderly[s][t - 1], bay[s][t]);
			}
		}
	}

	public int p(int s, int t)
	{
		assert 1 <= s && s <= S && 1 <= t && t <= h[s];
		assert bay[s][t] > 0;
		return bay[s][t];
	}

	public int q(int s, int t)
	{
		assert 1 <= s && s <= S && 0 <= t && t <= orderly[s];

		if (t == 0)
			return P;
		else
			return bay[s][t];

	}

	public int largestBadlyPlacedPriority(int s)
	{
		assert 1 <= s && s <= S;
		return maxDisorderly[s][h[s]];
	}

	public void move(int s1, int s2)
	{
		assert s1 != s2;
		assert h[s1] > 0 && h[s2] < H;

		int p = p(s1, h[s1]);


		bay[s1][h[s1]] = 0;
		maxDisorderly[s1][h[s1]] = 0;
		if (orderly[s1] == h[s1])
			orderly[s1]--;
		h[s1]--;

		h[s2]++;
		bay[s2][h[s2]] = p;

		if (orderly[s2] == h[s2] - 1 && p <= q(s2, orderly[s2]))
			orderly[s2]++;
		else
			maxDisorderly[s2][h[s2]] = Math.max(maxDisorderly[s2][h[s2] - 1], p);
	}

	public int M()
	{
		int m = 0;
		for (int s = 1; s <= S; s++)
		{
			m += h[s] - orderly[s];
		}
		return m;
	}

	public boolean check(int SS, int HH, int NN, int PP, int MM)
	{

		return SS == S && HH == H && NN == N && PP == P && MM == M();
	}

}
