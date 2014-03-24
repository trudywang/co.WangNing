package cpmp.lowerbound.achieve;

import java.util.ArrayList;
import java.util.Collections;

import cpmp.Constant;
import cpmp.Layout;
import cpmp.Pair;

public class LowerBoundHeap
{

	public static int WangNingLB1(Layout inst)
	{
		int lb = inst.remainSoild;

		int min = inst.numberOfSoiled(1);
		for (int s = 2; s <= inst.S; s++)
		{
			min = Math.min(min, inst.numberOfSoiled(s));
		}
		lb += min;

		DFS dfs = new DFS(inst);
		dfs.tryFit();
		int top = dfs.outside.top();
		int num = Constant.MAX_RELOCATION_NUMBER;
		for (int s = 1; s <= dfs.S; s++)
		{
			int retain = dfs.cleanHeight[s];
			while (retain > 0)
			{
				if (dfs.getCapa(s, retain) >= top)
					break;
				else
					retain--;
			}

			if (dfs.T - retain == 0)
				continue;
			int cost = dfs.cleanHeight[s] - retain;
			num = Math.min(num, cost);
		}

		lb += num;
		return lb;
	}

	public static int WangNingLB2IDA(Layout inst) throws CloneNotSupportedException
	{

		// IDA* does not outperform DFS in CVS instances, it does in BF
		// instances
		// because IDA* prefers situations where feasible solutions are hard to
		// find
		// so IDA* can dig the solution space level by level, until a feasible
		// solution, i.e. the best one, is found
		// However, when the depth of the whole solution space is not extremely
		// large
		// We prefer to use DFS to search it, best solution is always recorded,
		// and useless branches are directly cut by the best solution ever found
		int lb = inst.remainSoild;

		int min = inst.numberOfSoiled(1);
		for (int s = 2; s <= inst.S; s++)
		{
			min = Math.min(min, inst.numberOfSoiled(s));
		}
		lb += min;

		DFS dfs = new DFS(inst);
		dfs.tryFit();
		int num = 0;
		while (false == findToAccomodate(0, dfs, num))
		{
			num++;

		}
		lb += num;
		return lb;
	}

	public static int WangNingLB2DFS(Layout inst) throws CloneNotSupportedException
	{

		int lb = inst.remainSoild;

		int min = inst.numberOfSoiled(1);
		for (int s = 2; s <= inst.S; s++)
		{
			min = Math.min(min, inst.numberOfSoiled(s));
		}
		lb += min;

		DFS dfs = new DFS(inst);
		dfs.tryFit();
		clearToAccomodate(0, dfs);
		int num = dfs.best;
		lb += num;
		return lb;
	}

	private static class DFS implements Cloneable
	{
		public MaxHeap outside;
		public int[][] bay;
		public int S;
		public int T;
		public int[] cleanHeight;
		public int best;

		public DFS(Layout inst)
		{
			outside = new MaxHeap(inst.N);
			S = inst.S;
			T = inst.T;
			bay = new int[S + 1][T + 1];
			cleanHeight = inst.cleanHeight.clone();
			for (int s = 1; s <= S; s++)
			{

				for (int t = 1; t <= inst.stackHeight[s]; t++)
				{
					if (inst.isClean(s, t))
						bay[s][t] = inst.bay[s][t].groupLabel;
					else
						outside.add(inst.bay[s][t].groupLabel);
				}
			}
			best = Constant.MAX_RELOCATION_NUMBER;
		}

		public int getCapa(int s, int t)
		{
			if (t == 0)
				return Constant.FLOOR_PRIORITYLABEL;
			else
				return bay[s][t];
		}

		public int getCapa(int s)
		{
			int h = cleanHeight[s];
			return getCapa(s, h);
		}

		public DFS clone() throws CloneNotSupportedException
		{
			DFS dfs = (DFS) super.clone();

			dfs.outside = outside.clone();
			dfs.cleanHeight = cleanHeight.clone();
			dfs.bay = new int[S + 1][T + 1];
			for (int i = 1; i <= S; i++)
			{
				for (int j = 1; j <= cleanHeight[i]; j++)
					dfs.bay[i][j] = bay[i][j];
			}
			return dfs;
		}

		public void tryFit()
		{
			if (outside.size == 0)
				return;

			ArrayList<Pair<Integer, Integer>> emptySlots = new ArrayList<Pair<Integer, Integer>>();
			for (int s = 1; s <= S; s++)
			{
				int capa = getCapa(s);
				int num = T - cleanHeight[s];
				if (num != 0)
				{
					emptySlots.add(new Pair<Integer, Integer>(s, -capa));
				}
			}
			Collections.sort(emptySlots);
			for (Pair<Integer, Integer> pair : emptySlots)
			{
				if (outside.size == 0)
					return;

				int capa = -pair.getR();
				int s = pair.getL();
				if (capa >= outside.top())
				{
					int num = T - cleanHeight[s];
					if (num >= outside.size)
					{
						while (outside.size > 0)
						{
							int f = outside.pop();
							bay[s][++cleanHeight[s]] = f;
						}
						return;
					}
					else
					{
						while (cleanHeight[s] < T)
						{
							int f = outside.pop();
							bay[s][++cleanHeight[s]] = f;
						}
					}
				}
				else
					return;
			}

		}

		public void updateBest(int v)
		{
			best = Math.min(v, best);
		}
	}

	private static void clearToAccomodate(int already, DFS dfs) throws CloneNotSupportedException
	{
		if (dfs.outside.size == 0)
		{
			dfs.updateBest(already);
			return;
		}
		int top = dfs.outside.top();

		for (int s = 1; s <= dfs.S; s++)
		{
			int retain = dfs.cleanHeight[s];
			while (retain > 0)
			{
				if (dfs.getCapa(s, retain) >= top)
					break;
				else
					retain--;
			}

			int num = dfs.T - retain;
			int cost = dfs.cleanHeight[s] - retain;
			if (num == 0)
				continue;

			if (already + cost >= dfs.best)
				continue;

			DFS son = dfs.clone();
			for (int t = son.cleanHeight[s]; t > retain; t--)
			{
				son.outside.add(son.bay[s][t]);

				son.bay[s][t] = 0;
			}
			son.cleanHeight[s] = retain;
			son.tryFit();
			clearToAccomodate(already + cost, son);
			dfs.updateBest(son.best);
		}

	}

	private static boolean findToAccomodate(int already, DFS dfs, int bound) throws CloneNotSupportedException
	{
		if (dfs.outside.size == 0)
		{
			return true;
		}
		int top = dfs.outside.top();

		for (int s = 1; s <= dfs.S; s++)
		{
			int retain = dfs.cleanHeight[s];
			while (retain > 0)
			{
				if (dfs.getCapa(s, retain) >= top)
					break;
				else
					retain--;
			}

			int num = dfs.T - retain;

			if (num == 0)
				continue;
			int cost = dfs.cleanHeight[s] - retain;
			if (already + cost > bound)
				continue;
			DFS son = dfs.clone();
			for (int t = son.cleanHeight[s]; t > retain; t--)
			{
				son.outside.add(son.bay[s][t]);
				son.bay[s][t] = 0;
			}
			son.cleanHeight[s] = retain;
			son.tryFit();
			boolean x = findToAccomodate(already + cost, son, bound);
			if (x)
				return true;
		}
		return false;

	}

}
