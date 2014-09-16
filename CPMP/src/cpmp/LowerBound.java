package cpmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class LowerBound
{
	public static int WangNingMaxKnapsack(Layout inst)
	{
		/*
		 * 36629: 290ms 21456: 160ms
		 */
		int lb = inst.remainSoild;

		int min = inst.numberOfSoiled(1);
		for (int s = 2; s <= inst.S; s++)
		{
			min = Math.min(min, inst.numberOfSoiled(s));
		}
		lb += min;

		DFS dfs = new DFS(inst);

		int num = 0;
		int[] cumulativeDemand = new int[inst.G + 1];
		for (int g = inst.G; g >= 1; g--)
		{
			cumulativeDemand[g] = (g == inst.G ? 0 : cumulativeDemand[g + 1]);
			cumulativeDemand[g] += dfs.demand[g];
		}

		for (int g = inst.G; g >= 1; g--)
		{

			int totalDemand = cumulativeDemand[g];

			int totalSupply = 0;
			for (int s = 1; s <= inst.S; s++)
			{
				if (dfs.get(s, dfs.cleanHeight[s]) >= g)
					totalSupply += dfs.T - dfs.cleanHeight[s];
			}

			if (totalDemand > totalSupply)
			{
				int surplus = totalDemand - totalSupply;
				ArrayList<Pair<Integer, Integer>> slotcost = new ArrayList<Pair<Integer, Integer>>();
				for (int s = 1; s <= inst.S; s++)
				{
					if (dfs.get(s, dfs.cleanHeight[s]) >= g)
						continue;

					int retain = dfs.retain[g][s];
					int slot = inst.T - retain;
					int cost = dfs.cleanHeight[s] - retain;

					slotcost.add(new Pair<Integer, Integer>(slot, cost));

				}
				int knap = knapsack(slotcost, surplus);
				num = Math.max(num, knap);
			}
		}

		lb += num;
		return lb;

	}

	public static int LBFB(Layout inst)
	{

		int lb = inst.remainSoild;

		int min = inst.numberOfSoiled(1);
		for (int s = 2; s <= inst.S; s++)
		{
			min = Math.min(min, inst.numberOfSoiled(s));
		}
		lb += min;

		DFS dfs = new DFS(inst);

		int num = 0;
		int[] cumulativeDemand = new int[inst.G + 1];
		for (int g = inst.G; g >= 1; g--)
		{
			cumulativeDemand[g] = (g == inst.G ? 0 : cumulativeDemand[g + 1]);
			cumulativeDemand[g] += dfs.demand[g];
		}
		int gstar = -1;
		int maxsurplus = 0;
		for (int g = inst.G; g >= 1; g--)
		{

			int totalDemand = cumulativeDemand[g];

			int totalSupply = 0;
			for (int s = 1; s <= inst.S; s++)
			{
				if (dfs.get(s, dfs.cleanHeight[s]) >= g)
					totalSupply += dfs.T - dfs.cleanHeight[s];
			}

			if (totalDemand > totalSupply)
			{

				int surplus = totalDemand - totalSupply;

				if (surplus >= maxsurplus)//prefer small g when surplus=maxsurplus
				{
					maxsurplus = surplus;
					gstar = g;
				}
			}
		}
		int stackNeeded = maxsurplus / inst.T + (maxsurplus % inst.T == 0 ? 0 : 1);

		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int s = 1; s <= inst.S; s++)
		{
			if (dfs.get(s, dfs.cleanHeight[s]) >= gstar)
				continue;

			int retain = dfs.retain[gstar][s];

			int cost = dfs.cleanHeight[s] - retain;

			list.add(cost);
		}
		Collections.sort(list);
		for (int i = 0; i < stackNeeded; i++)
			num += list.get(i);

		lb += num;
		return lb;

	}

	private static int knapsack(ArrayList<Pair<Integer, Integer>> slotcost, int surplus)
	{
		int[] minCost = new int[surplus + 1];
		Arrays.fill(minCost, Constant.MAX_RELOCATION_NUMBER);
		minCost[0] = 0;
		for (Pair<Integer, Integer> p : slotcost)
		{
			for (int v = surplus; v >= 0; v--)
			{
				int v2 = v + p.getL();
				if (v2 > surplus)
					v2 = surplus;
				int c2 = minCost[v] + p.getR();
				if (c2 < minCost[v2])
					minCost[v2] = c2;
			}
		}
		return minCost[surplus];
	}

	public static int WangNingLB2(Layout inst)
	{
		int lb = inst.remainSoild;

		int min = inst.numberOfSoiled(1);
		for (int s = 2; s <= inst.S; s++)
		{
			min = Math.min(min, inst.numberOfSoiled(s));
		}
		lb += min;

		DFS dfs = new DFS(inst);
		clearToAccomodate(0, dfs, inst.G, dfs.demand[inst.G]);
		int num = dfs.best;
		lb += num;
		return lb;

	}

	public static class DFS implements Cloneable
	{
		public int[] demand;
		public int[][] retain;// number of clean containers with group label>=g
		public Layout inst;
		public int S;
		public int T;
		public int[] cleanHeight;
		public int best;

		public int get(int s, int t)
		{
			if (t == 0)
				return Constant.FLOOR_PRIORITYLABEL;
			else
				return inst.bay[s][t].groupLabel;
		}

		public DFS(Layout inst)
		{
			demand = new int[inst.G + 1];
			S = inst.S;
			T = inst.T;
			this.inst = inst;
			cleanHeight = inst.cleanHeight.clone();
			for (int s = 1; s <= S; s++)
			{
				for (int t = 1; t <= inst.stackHeight[s]; t++)
				{
					if (!inst.isClean(s, t))
						demand[inst.bay[s][t].groupLabel]++;
				}
			}
			best = Constant.MAX_RELOCATION_NUMBER;

			retain = new int[inst.G + 1][inst.S + 1];
			for (int g = inst.G; g >= 1; g--)
			{

				for (int s = 1; s <= inst.S; s++)
				{
					if (g != inst.G)
						retain[g][s] = retain[g + 1][s];
					while (retain[g][s] < cleanHeight[s] && get(s, retain[g][s] + 1) >= g)
					{
						retain[g][s]++;
					}
				}
			}
		}

		public void updateBest(int v)
		{
			best = Math.min(v, best);
		}
	}

	private static void clearToAccomodate(int already, DFS dfs, int next, int demand)
	{
		if (next == 0)
		{
			dfs.updateBest(already);
			return;
		}
		int supply = 0;
		for (int s = 1; s <= dfs.S; s++)
		{
			if (dfs.get(s, dfs.cleanHeight[s]) >= next)
			{
				supply += dfs.T - dfs.cleanHeight[s];
			}
		}
		if (supply >= demand)
		{
			clearToAccomodate(already, dfs, next - 1, demand + dfs.demand[next - 1]);
		}
		else
		{
			int surplus = demand - supply;
			for (int s = 1; s <= dfs.S; s++)
			{
				if (dfs.get(s, dfs.cleanHeight[s]) >= next)
				{
					continue;
				}
				int retain = Math.min(dfs.cleanHeight[s], dfs.retain[next][s]);
				int num = dfs.T - retain;
				int cost = dfs.cleanHeight[s] - retain;
				if (already + cost >= dfs.best)
					continue;
				int originalHeight = dfs.cleanHeight[s];
				for (int t = originalHeight; t > retain; t--)
					dfs.demand[dfs.get(s, t)]++;

				dfs.cleanHeight[s] = retain;

				if (surplus <= num)
					clearToAccomodate(already + cost, dfs, next - 1, demand + dfs.demand[next - 1]);
				else
				{
					clearToAccomodate(already + cost, dfs, next, demand);
				}
				for (int t = originalHeight; t > retain; t--)
					dfs.demand[dfs.get(s, t)]--;
				dfs.cleanHeight[s] = originalHeight;
			}
		}

	}
}
