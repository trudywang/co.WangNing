package com.jinbostar.cpmp.common;

import java.util.ArrayList;

public class State extends Bay implements Cloneable
{
	public int stable[];//
	public int fixed[];//fixed height
	public ArrayList<Operation> path;
	public int realizedMoves;
	public int U; //unreachable tiers 
	public int F; //Fixed Number
	public int[] resource, demand, RESOURCE, DEMAND;
	public int[][] minDelta;

	@SuppressWarnings("unchecked")
	public State clone() throws CloneNotSupportedException
	{
		State ret = (State) super.clone();
		ret.stable = stable.clone();
		ret.fixed = fixed.clone();
		ret.path = (ArrayList<Operation>) path.clone();

		ret.resource = resource.clone();
		ret.RESOURCE = RESOURCE.clone();
		ret.demand = demand.clone();
		ret.DEMAND = DEMAND.clone();
		ret.minDelta = new int[P + 1][];
		for (int p = 1; p <= P; p++)
			ret.minDelta[p] = minDelta[p].clone();
		return ret;
	}

	public State(int S, int H, int P, int N, int[][] bay) throws Exception
	{
		super(S, H, P, N, bay);

		fixed = new int[S + 1];
		stable = new int[S + 1];
		resource = new int[P + 1];
		RESOURCE = new int[P + 2];
		demand = new int[P + 1];
		DEMAND = new int[P + 2];
		minDelta = new int[P + 1][P + 1];
		path = new ArrayList<Operation>();
		realizedMoves = 0;
		U = 0;
		F = 0;
		if (!initialize())
			throw new Exception("Unsolvable Instance Error");

	}


	private boolean initialize()
	{
		U = H - Math.min(H, S * H - N);

		for (int s = 1; s <= S; s++)
		{
			if (orderly[s] < U)
				return false;
			fixed[s] = U;
		}
		F = U * S;

		for (int s = 1; s <= S; s++)
		{
			int q = q(s, fixed[s]);
			resource[q] += H - U;
			for (int t = fixed[s] + 1; t <= h[s]; t++)
			{
				int p = p(s, t);
				demand[p]++;
			}
		}

		RESOURCE[P + 1] = DEMAND[P + 1] = 0;
		for (int g = P; g >= 1; g--)
		{

			RESOURCE[g] = RESOURCE[g + 1] + resource[g];
			DEMAND[g] = DEMAND[g + 1] + demand[g];

			if (RESOURCE[g] < DEMAND[g])
				return false;
		}


		rebuildStability();
		return true;
	}

	public boolean preTricky()
	{
		return preExtreme() && N - F >= 3;
	}

	public boolean preExtreme()
	{
		int countH = 0;
		int countHm1 = 0;
		for (int s = 1; s <= S; s++)
		{
			if (fixed[s] == H)
				countH++;
			else if (fixed[s] == H - 1)
				countHm1++;
		}
		return (countH == S - 3 && countHm1 >= 1);
	}

	public void rebuildStability()
	{

		for (int p = 1; p <= P; p++)
		{
			minDelta[p][p] = Integer.MAX_VALUE;
			for (int q = p + 1; q <= P; q++)
			{

				minDelta[p][q] = Math.min(minDelta[p][q - 1], RESOURCE[q] - DEMAND[q]);

			}
		}


		for (int s = 1; s <= S; s++)
		{
			//	boolean nonNegativity = true;
			for (stable[s] = fixed[s]; stable[s] + 1 <= orderly[s]; stable[s]++)
			{
				int t = stable[s] + 1;
				int p = p(s, t);
				int q = q(s, t - 1);

				assert p <= q;


				if (minDelta[p][q] < H - t + 1)
					break;

				//		for (int g = p + 1; nonNegativity && g <= q; g++)
				//			if (RESOURCE[g] - DEMAND[g] < H - t + 1)
				//				nonNegativity = false;
				//		if (!nonNegativity)
				//			break;
			}
		}

		if (preExtreme())
		{
			int[] three = new int[3];
			for (int s = 1, k = 0; s <= S; s++)
			{
				if (fixed[s] != H)
					three[k++] = s;
			}
			for (int k = 0; k < 3; k++)
			{
				int s = three[k];
				if (fixed[s] == H - 1 && stable[s] == H)
				{
					int s1 = three[(k + 1) % 3];
					int s2 = three[(k + 2) % 3];
					int h1 = h[s1];
					int h2 = h[s2];
					if (!shuffleExtreme(s1, h1, s2, h2))
						stable[s] = H - 1;
				}
			}
		}
	}

	private boolean isStable(int s, int t)
	{
		assert 1 <= s && s <= S && 1 <= t && t <= h[s];
		return t <= stable[s];
	}

	private boolean isOrderly(int s, int t)
	{
		assert t <= h[s];
		return t <= orderly[s];
	}

	private boolean shuffleExtreme(int s1, int h1, int s2, int h2)
	{
		if (isOrderly(s1, h1) && isOrderly(s2, h2))
			return true;
		else if (!isOrderly(s1, h1) && !isOrderly(s2, h2))
			return false;
		else if (!isOrderly(s1, h1) && isOrderly(s2, h2))
			return shuffleExtreme(s2, h2, s1, h1);
		else// isOrderly(s1,h1) && !isOrderly(s2,h2)
		{
			int bad = h2 - orderly[s2];
			if (h1 + bad > H)
				return false;
			int q = q(s1, h1);
			int p = p(s2, h2);
			if (q < p)
				return false;
			for (int t = h2; t >= orderly[s2] + 2; t--)
			{
				if (p(s2, t) < p(s2, t - 1))
					return false;
			}
			return true;
		}
	}

	public boolean becomeStableByMove(int s1, int s2)
	{
		assert s1 != s2;
		assert h[s1] > 0 && h[s2] < H;


		if (stable[s2] != h[s2])
			return false;

		int p = p(s1, h[s1]);
		int q = q(s2, h[s2]);
		if (p > q)
			return false;

		if (minDelta[p][q] < H - h[s2])
			return false;
		//	for (int g = p + 1; g <= q; g++)
		//		if (RESOURCE[g] - DEMAND[g] < H - h[s2])
		//			return false;

		if (fixed[s2] == H - 1 && preExtreme())
		{
			int s3 = -1;
			for (int s = 1; s3 == -1 && s <= S; s++)
			{
				if (fixed[s] != H && s != s1 && s != s2)
					s3 = s;
			}
			int h1 = h[s1] - 1;
			int h3 = h[s3];
			if (!shuffleExtreme(s1, h1, s3, h3))
			{
				return false;
			}
		}
		return true;
	}

	public int largestDisorderlyPriority(int s)
	{
		int l = largestBadlyPlacedPriority(s);
		if (stable[s] != orderly[s])
		{
			if (stable[s] + 1 > h[s])
				System.out.println();
			l = Math.max(p(s, stable[s] + 1), l);
		}
		return l;
	}

	public void fix(int s)
	{

		assert fixed[s] != H && isStable(s, fixed[s] + 1);

		F++;
		int p = p(s, fixed[s] + 1);
		demand[p]--;
		for (int g = 1; g <= p; g++)
			DEMAND[g]--;

		int q = q(s, fixed[s]);
		resource[q] -= H - fixed[s];
		for (int g = 1; g <= q; g++)
			RESOURCE[g] -= H - fixed[s];
		resource[p] += H - fixed[s] - 1;
		for (int g = 1; g <= p; g++)
			RESOURCE[g] += H - fixed[s] - 1;


		fixed[s]++;
		rebuildStability();

		path.add(new Operation(s, s, Operation.Type.Fix));
	}

	public void unfix(int s)
	{
		assert fixed[s] > 0;
		F--;

		int p = p(s, fixed[s]);
		demand[p]++;
		for (int g = 1; g <= p; g++)
			DEMAND[g]++;

		resource[p] -= H - fixed[s];
		for (int g = 1; g <= p; g++)
			RESOURCE[g] -= H - fixed[s];

		int q = q(s, fixed[s] - 1);
		resource[q] += H - fixed[s] + 1;
		for (int g = 1; g <= q; g++)
			RESOURCE[g] += H - fixed[s] + 1;


		fixed[s]--;
		rebuildStability();

		path.add(new Operation(s, s, Operation.Type.Unfix));
	}


	public void recover(int size)
	{
		while (path.size() > size)
		{
			Operation op = path.remove(path.size() - 1);
			if (op.type == Operation.Type.Fix)
			{
				int s = op.s1;
				assert fixed[s] > 0;
				F--;

				int p = p(s, fixed[s]);
				demand[p]++;
				for (int g = 1; g <= p; g++)
					DEMAND[g]++;

				resource[p] -= H - fixed[s];
				for (int g = 1; g <= p; g++)
					RESOURCE[g] -= H - fixed[s];

				int q = q(s, fixed[s] - 1);
				resource[q] += H - fixed[s] + 1;
				for (int g = 1; g <= q; g++)
					RESOURCE[g] += H - fixed[s] + 1;


				fixed[s]--;
				rebuildStability();
			} else if (op.type == Operation.Type.Unfix)
			{
				int s = op.s1;
				assert fixed[s] != H && isStable(s, fixed[s] + 1);

				F++;
				int p = p(s, fixed[s] + 1);
				demand[p]--;
				for (int g = 1; g <= p; g++)
					DEMAND[g]--;

				int q = q(s, fixed[s]);
				resource[q] -= H - fixed[s];
				for (int g = 1; g <= q; g++)
					RESOURCE[g] -= H - fixed[s];
				resource[p] += H - fixed[s] - 1;
				for (int g = 1; g <= p; g++)
					RESOURCE[g] += H - fixed[s] - 1;


				fixed[s]++;
				rebuildStability();
			} else
			{
				realizedMoves--;
				int s1 = op.s2;
				int s2 = op.s1;
				assert s1 != s2;
				assert fixed[s1] != h[s1];
				assert h[s1] > 0 && h[s2] < H;

				if (stable[s1] == h[s1])
					stable[s1]--;

				boolean tobe = becomeStableByMove(s1, s2);
				super.move(s1, s2);
				if (tobe)
					stable[s2]++;
			}
		}
	}


	public void move(int s1, int s2)
	{
		assert s1 != s2;
		assert fixed[s1] != h[s1];
		assert h[s1] > 0 && h[s2] < H;

		if (stable[s1] == h[s1])
			stable[s1]--;

		boolean tobe = becomeStableByMove(s1, s2);
		super.move(s1, s2);
		if (tobe)
			stable[s2]++;

		realizedMoves++;
		path.add(new Operation(s1, s2, Operation.Type.Move));
	}


	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (int s = 1; s <= S; s++)
		{
			if (s != 1)
				builder.append(" ");

			for (int t = 1; t <= H; t++)
			{
				if (t == 1)
					builder.append("[");

				if (fixed[s] >= 1 && t == 1)
					builder.append("(");

				if (bay[s][t] == 0)
				{
					builder.append(" ]");
					break;
				} else
				{
					builder.append(" " + bay[s][t]);
					if (t == fixed[s])
						builder.append(")");
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
					if (t <= fixed[s])
					{
						if (bay[s][t] < 10)
							builder.append(String.format(" (%d)", bay[s][t]));
						else
							builder.append(String.format("(%d)", bay[s][t]));
					} else
						builder.append(String.format(" %2d ", bay[s][t]));
				} else
				{
					builder.append("    ");
				}
			}
			builder.append("\n");
		}

		builder.append("--+");
		for (int s = 1; s <= S; s++)
		{
			builder.append("----");
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

	public int lowerBound()
	{
		int min = Integer.MAX_VALUE;
		int res = 0;
		for (int s = 1; s <= S; s++)
		{
			res += h[s] - stable[s];
			min = Math.min(min, h[s] - stable[s]);
		}
		return res + min;
	}
}
