package cpmp.lowerbound.achieve;

import cpmp.Constant;
import cpmp.Layout;

public class LowerBoundCumulative
{
	public static int WangNingLB2(Layout inst) throws CloneNotSupportedException
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

	private static class DFS implements Cloneable
	{
		public int[] demand;
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
		}

		public DFS clone() throws CloneNotSupportedException
		{
			DFS dfs = (DFS) super.clone();

			dfs.demand = demand.clone();
			dfs.cleanHeight = cleanHeight.clone();

			return dfs;
		}

		public void updateBest(int v)
		{
			best = Math.min(v, best);
		}
	}

	private static void clearToAccomodate(int already, DFS dfs, int next, int demand) throws CloneNotSupportedException
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
				int retain = dfs.cleanHeight[s];
				while (retain > 0)
				{
					if (dfs.get(s, retain) >= next)
						break;
					else
						retain--;
				}
				int num = dfs.T - retain;
				int cost = dfs.cleanHeight[s] - retain;
				if (already + cost >= dfs.best)
					continue;
				DFS son = dfs.clone();
				for (int t = son.cleanHeight[s]; t > retain; t--)
				{
					son.demand[son.get(s, t)]++;
				}
				son.cleanHeight[s] = retain;
				if (surplus <= num)
					clearToAccomodate(already + cost, son, next - 1, demand + son.demand[next - 1]);
				else
					clearToAccomodate(already + cost, son, next, demand);

				dfs.updateBest(son.best);
			}

		}

	}
}
