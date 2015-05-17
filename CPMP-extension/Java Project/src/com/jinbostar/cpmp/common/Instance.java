package com.jinbostar.cpmp.common;

public class Instance
{
	public int S;
	public int H;
	public int P;
	public int N;
	public int[][] bay;


	public Instance(int S, int H, int P, int N, int[][] bay)
	{
		this.S = S;
		this.H = H;
		this.P = P;
		this.N = N;
		this.bay = new int[S + 1][H + 1];
		for (int s = 1; s <= S; s++)
			for (int t = 1; t <= H; t++)
				this.bay[s][t] = bay[s][t];
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (int s = 1; s <= S; s++)
		{
			if (s != 1)
				builder.append(" ");
			builder.append("[");
			for (int t = 1; t <= H; t++)
			{
				if (bay[s][t] == 0)
				{
					builder.append(" ]");
					break;
				} else
				{
					builder.append(" " + bay[s][t]);
				}
				if (t == H)
				{
					builder.append(" ]");
				}
			}

		}
		return builder.toString();
	}

	public String sideView()
	{
		StringBuilder builder = new StringBuilder();

		for (int t = H; t > 0; t--)
		{
			builder.append(String.format("%2d|", t));
			for (int s = 1; s <= S; s++)
			{
				if (bay[s][t] != 0)
				{
					builder.append(String.format(" %2d", bay[s][t]));
				} else
				{
					builder.append("   ");
				}
			}
			builder.append("\n");
		}

		builder.append("--+");
		for (int s = 1; s <= S; s++)
		{
			builder.append("---");
		}
		builder.append("\n");

		builder.append("  |");
		for (int s = 1; s <= S; s++)
		{
			builder.append(String.format(" %2d", s));
		}
		builder.append("\n");
		return builder.toString();
	}

}
