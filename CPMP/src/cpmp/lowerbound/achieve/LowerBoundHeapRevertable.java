package cpmp.lowerbound.achieve;

import java.util.ArrayList;
import java.util.Collections;

import cpmp.Constant;
import cpmp.Layout;
import cpmp.Pair;

public class LowerBoundHeapRevertable
{
	public static int WangNingLB2IDA(Layout inst)
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

	public static int WangNingLB2DFS(Layout inst)
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
		public MaxHeapRevertable outside;
		public Pair<Integer, Integer>[][] bay;
		public int S;
		public int T;
		public int[] cleanHeight;
		public int best;
		public ArrayList<Pair<Integer, Integer>> moves;
		public ArrayList<Integer> direction;

		@SuppressWarnings("unchecked")
		public DFS(Layout inst)
		{
			outside = new MaxHeapRevertable(inst.N);
			S = inst.S;
			T = inst.T;
			bay = new Pair[S + 1][T + 1];
			cleanHeight = inst.cleanHeight.clone();
			for (int s = 1; s <= S; s++)
			{

				for (int t = 1; t <= inst.stackHeight[s]; t++)
				{
					if (inst.isClean(s, t))
						bay[s][t] = new Pair<Integer, Integer>(inst.bay[s][t].uniqueContainerIndex, inst.bay[s][t].groupLabel);
					else
						outside.add(new Pair<Integer, Integer>(inst.bay[s][t].uniqueContainerIndex, inst.bay[s][t].groupLabel));
				}
			}
			best = Constant.MAX_RELOCATION_NUMBER;
			moves = new ArrayList<Pair<Integer, Integer>>();
			direction = new ArrayList<Integer>();
		}

		public int getCapa(int s, int t)
		{
			if (t == 0)
				return Constant.FLOOR_PRIORITYLABEL;
			else
				return bay[s][t].getR();
		}

		public int getCapa(int s)
		{
			int h = cleanHeight[s];
			return getCapa(s, h);
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
				if (capa >= outside.top().getR())
				{
					int num = T - cleanHeight[s];
					if (num >= outside.size)
					{
						while (outside.size > 0)
						{
							Pair<Integer, Integer> f = outside.pop();
							bay[s][++cleanHeight[s]] = f;
							moves.add(f);
							direction.add(s);
						}
						return;
					}
					else
					{
						while (cleanHeight[s] < T)
						{
							Pair<Integer, Integer> f = outside.pop();
							bay[s][++cleanHeight[s]] = f;
							moves.add(f);
							direction.add(s);
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

		public void getOut(int s)
		{
			outside.add(bay[s][cleanHeight[s]]);
			moves.add(bay[s][cleanHeight[s]]);
			direction.add(-s);
			bay[s][cleanHeight[s]] = null;
			cleanHeight[s]--;
		}

		public void backUp(int size)
		{
			while (moves.size() > size)
			{
				Pair<Integer, Integer> p = moves.remove(moves.size() - 1);
				int d = direction.remove(direction.size() - 1);
				if (d > 0)
				{
					int s = d;
					outside.add(bay[s][cleanHeight[s]]);
					bay[s][cleanHeight[s]] = null;
					cleanHeight[s]--;
				}
				else
				{
					int s = -d;
					bay[s][++cleanHeight[s]] = p;
					outside.deleteID(p.getL());
				}
			}
		}
	}

	private static void clearToAccomodate(int already, DFS dfs)
	{
		if (dfs.outside.size == 0)
		{
			dfs.updateBest(already);
			return;
		}
		Pair<Integer, Integer> top = dfs.outside.top();

		for (int s = 1; s <= dfs.S; s++)
		{
			int retain = dfs.cleanHeight[s];
			while (retain > 0)
			{
				if (dfs.getCapa(s, retain) >= top.getR())
					break;
				else
					retain--;
			}

			int num = dfs.T - retain;
			if (num == 0)
				continue;
			int cost = dfs.cleanHeight[s] - retain;
			if (already + cost >= dfs.best)
				continue;

			int backup = dfs.moves.size();

			while (dfs.cleanHeight[s] > retain)
			{
				dfs.getOut(s);
			}

			dfs.tryFit();
			clearToAccomodate(already + cost, dfs);

			dfs.backUp(backup);
		}
	}

	private static boolean findToAccomodate(int already, DFS dfs, int bound)
	{
		if (dfs.outside.size == 0)
		{
			return true;
		}
		Pair<Integer, Integer> top = dfs.outside.top();

		for (int s = 1; s <= dfs.S; s++)
		{
			int retain = dfs.cleanHeight[s];
			while (retain > 0)
			{
				if (dfs.getCapa(s, retain) >= top.getR())
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

			int backup = dfs.moves.size();

			while (dfs.cleanHeight[s] > retain)
			{
				dfs.getOut(s);
			}

			dfs.tryFit();
			boolean x = findToAccomodate(already + cost, dfs, bound);
			dfs.backUp(backup);
			if (x)
				return true;
		}
		return false;

	}

}
