package com.jinbostar.cpmp.scheme.tgh;


import com.jinbostar.cpmp.common.Operation;
import com.jinbostar.cpmp.common.State;
import com.jinbostar.cpmp.common.Task;
import com.jinbostar.cpmp.common.Tuple;

import static com.jinbostar.cpmp.common.Parameter.TopFilter;
import static com.jinbostar.cpmp.common.Parameter.TopFilter.*;

public class TargetGuidedHeuristic extends State
{

	public boolean stability;
	public TopFilter topFilter;

	public TargetGuidedHeuristic(int S, int H, int P, int N, int[][] bay) throws Exception
	{
		super(S, H, P, N, bay);
		stability = false;
		topFilter = NoTopFilter;
	}

	private void move(int s1, int k, int s2)
	{
		for (int i = 1; i <= k; i++)
			move(s1, s2);
	}

	public int solve()
	{

		while (F < N)
		{
//			System.out.println(this.sideView());
			Task task = nextTask();

			if (task.immediate())
				immediateFix(task);
			else if (task.internal())
			{
				internalFix(task);
			} else if (task.external())
			{
				externalFix(task);
			}


		}

		int res = 0;
		for (Operation op : path)
		{
			if (op.type == Operation.Type.Move)
				res++;

		}
		return res;
	}

	private boolean isNiceStack(int s)
	{
		if (stability)
			return stable[s] == h[s];
		else
			return orderly[s] == h[s];
	}

	private int messiness(int s)
	{
		if (stability)
			return largestDisorderlyPriority(s);
		else
			return largestBadlyPlacedPriority(s);
	}

	private Task nextTask()
	{
		Task best = null;
		int[] cost = null;
		for (int sPlus = 1; sPlus <= S; sPlus++)
		{
			for (int tPlus = fixed[sPlus] + 1; tPlus <= h[sPlus]; tPlus++)
			{
				for (int sMinus = 1; sMinus <= S; sMinus++)
				{
					if (fixed[sMinus] == H)
						continue;
					int p = p(sPlus, tPlus);
					int q = q(sMinus, fixed[sMinus]);
					if (p > q)
						continue;

					Task task = new Task(p, sPlus, tPlus, sMinus, fixed[sMinus] + 1);
					int[] temp = taskCost(task);
					if (best == null || Tuple.compare(cost, temp) > 0)
					{
						best = task;
						cost = temp;
					}
				}
			}
		}

		return best;
	}

	private int[] taskCost(Task task)
	{
		int p = p(task.current_stack, task.current_tier);


		int[] cost = new int[4];

		cost[0] = 0;

		if (topFilter == TrickyAvoid)
		{
			if (fixed[task.aim_stack] == H - 1 && preTricky())
				cost[0] = 2;

		} else if (topFilter == FullyFixedAvoid)
		{
			if (fixed[task.aim_stack] == H - 1)
				cost[0] = 2;

		} else if (topFilter == ExtremeAvoid)
		{
			if (fixed[task.aim_stack] == H - 1 && preExtreme())
				cost[0] = 2;
		}


		cost[1] = -p;

		cost[2] = movesIdeal(task);
		cost[3] = fixed[task.aim_stack];

		return cost;
	}

	private int movesIdeal(Task task)
	{
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;
		int sMinus = task.aim_stack;

		if (task.immediate())
			return 0;
		else if (task.internal())
		{
			int b1 = h[sPlus] - tPlus;
			int b2 = tPlus - fixed[sPlus] - 1;
			return b1 + b2 + 2;
		} else
		{
			int b1 = h[sPlus] - tPlus;
			int b2 = h[sMinus] - fixed[sMinus];
			return b1 + b2 + 1;
		}
	}

	private int reselectInterimI3(int sPlus, int interim)
	{
		if (isNiceStack(interim))
		{


			int result = -1;
			int[] cost = null;
			for (int s = 1; s <= S; s++)
			{
				if (s == sPlus || s == interim)
					continue;
				assert h[s] == H;


				int[] temp = new int[2];

				boolean nice = isNiceStack(s);

				if (!nice && becomeNiceByMove(s, interim))
				{
					temp[0] = 1;
					temp[1] = -p(s, H);
				} else if (nice && becomeNiceByMove(s, interim))
				{
					temp[0] = 2;
					temp[1] = -p(s, H);
				} else if (!nice && !becomeNiceByMove(s, interim))
				{
					temp[0] = 3;
					temp[1] = p(s, H);
				} else
				{
					temp[0] = 4;
					temp[1] = p(s, H);
				}

				if (result == -1 || Tuple.compare(cost, temp) > 0)
				{
					result = s;
					cost = temp;
				}
			}
			return result;
		} else
		{
			int result = -1;
			int[] cost = null;

			for (int s = 1; s <= S; s++)
			{
				if (s == sPlus || s == interim)
					continue;
				assert h[s] == H;
				int p = p(s, H);

				boolean nice = isNiceStack(s);

				int[] temp = new int[2];
				if (!nice)
				{
					temp[0] = 1;
					temp[1] = p;
				} else
				{
					temp[0] = 2;
					temp[1] = p;
				}

				if (result == -1 || Tuple.compare(cost, temp) > 0)
				{
					result = s;
					cost = temp;
				}
			}
			return result;
		}

	}

	private int selectInterimE4(int sPlus, int sMinus)
	{
		int result = -1;
		int[] cost = null;

		for (int s = 1; s <= S; s++)
		{
			if (s == sPlus || s == sMinus)
				continue;
			assert h[s] == H;
			int p = p(s, H);

			boolean nice = isNiceStack(s);

			int[] temp = new int[2];
			if (!nice)
			{
				temp[0] = 1;
				temp[1] = p;
			} else
			{
				temp[0] = 2;
				temp[1] = p;
			}

			if (result == -1 || Tuple.compare(cost, temp) > 0)
			{
				result = s;
				cost = temp;
			}
		}
		return result;
	}

	private int internalInterim(int sPlus)
	{
		int result = -1;
		int[] cost = null;
		for (int s = 1; s <= S; s++)
		{
			if (s == sPlus || h[s] == H)
				continue;

			int[] temp = new int[2];
			temp[0] = -h[s];
			temp[1] = (isNiceStack(s) ? 1 : 0);

			if (result == -1 || Tuple.compare(cost, temp) > 0)
			{
				result = s;
				cost = temp;
			}
		}

		return result;

	}

	private boolean forbidden(int a, int[] f)
	{
		for (int k : f)
			if (a == k)
				return true;
		return false;
	}

	private void relocate(int sender, int[] forbid, int targetPriority)
	{

		int block = p(sender, h[sender]);

		int receiver = -1;
		int[] cost = null;
		for (int r = 1; r <= S; r++)
		{
			if (h[r] == H || forbidden(r, forbid))
				continue;

			int[] temp = new int[2];

			if (isNiceStack(r))
			{
				if (becomeNiceByMove(sender, r))
				{
					temp[0] = 1;
					temp[1] = q(r, h[r]);
				} else
				{
					temp[0] = 4;
					temp[1] = -q(r, h[r]);
				}
			} else
			{
				int m = messiness(r);
				if (m <= block)
				{
					temp[0] = 2;
					temp[1] = -m;
				} else if (block < m && m < targetPriority)
				{
					temp[0] = 3;
					temp[1] = m;
				} else
				{
					temp[0] = 5;
					temp[1] = m;
				}
			}
			if (receiver == -1 || Tuple.compare(cost, temp) > 0)
			{
				receiver = r;
				cost = temp;
			}
		}


		if (cost[0] == 1 && fulfill(sender, receiver, forbid))
		{
			relocate(sender, forbid, targetPriority);
		} else
		{
			move(sender, receiver);
		}
	}

	private boolean becomeNiceByMove(int s1, int s2)
	{
		assert s1 != s2;
		assert h[s1] > 0 && h[s2] < H;

		if (stability)
			return becomeStableByMove(s1, s2);
		else
			return q(s2, h[s2]) >= p(s1, h[s1]);
	}

	private boolean fulfill(int sender, int receiver, int[] forbid)
	{
		int block = p(sender, h[sender]);


		int mid = -1;
		int largest = -1;

		for (int s = 1; s <= S; s++)
		{
			if (s == sender || s == receiver || h[s] == 0 || forbidden(s, forbid))
				continue;
			int top = p(s, h[s]);
			if (!isNiceStack(s) && top > block && becomeNiceByMove(s, receiver))
			{
				if (mid == -1 || largest < top)
				{
					mid = s;
					largest = top;
				}
			}
		}
		if (mid == -1)
			return false;
		else
		{
			move(mid, receiver);
			return true;
		}
	}

	private void immediateFix(Task task)
	{
		fix(task.aim_stack);
	}

	private void internalFix(Task task)
	{
		int pStar = task.target_priority;
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;


		int b1 = h[sPlus] - tPlus;
		int b2 = tPlus - 1 - fixed[sPlus];
		for (int i = 0; i < b1; i++)
		{
			relocate(sPlus, new int[]{sPlus}, pStar);
		}

		int tmp = internalInterim(sPlus);

		int e = S * H - N - (H - h[sPlus]) - (H - h[tmp]);

		if (e >= b2)
		{
			move(sPlus, tmp);

			for (int i = 0; i < b2; i++)
			{
				relocate(sPlus, new int[]{sPlus, tmp}, pStar);
			}
			move(tmp, sPlus);
			fix(sPlus);
		} else if (e >= 1)
		{
			move(sPlus, tmp);

			for (int i = 0; i < e - 1; i++)
			{
				relocate(sPlus, new int[]{sPlus, tmp}, pStar);
			}
			for (int s = 1; s <= S; s++)
			{
				if (s == sPlus || s == tmp || h[s] == H)
					continue;

				move(tmp, s);
				tmp = s;
				break;
			}
			for (int i = e - 1; i < b2; i++)
			{
				relocate(sPlus, new int[]{sPlus, tmp}, pStar);
			}
			move(tmp, sPlus);
			fix(sPlus);
		} else//e=0
		{
			int old = tmp;
			tmp = reselectInterimI3(sPlus, tmp);

			boolean regression = (fixed[tmp] == H);

			if (regression)
				unfix(tmp);
			move(tmp, old);

			int at = h[old];

			move(sPlus, tmp);

			for (int i = 0; i < b2; i++)
			{
				relocate(sPlus, new int[]{sPlus, tmp}, pStar);
			}
			move(tmp, sPlus);
			fix(sPlus);

			if (regression)
			{

				while (h[old] > at)
					relocate(old, new int[]{tmp, old}, pStar);
				move(old, tmp);
				fix(tmp);
			}
		}

	}

	public void externalFix(Task task)
	{


		int pStar = task.target_priority;
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;
		int sMinus = task.aim_stack;

		assert fixed[sMinus] != H;

		int b1 = h[sPlus] - tPlus;
		int b2 = h[sMinus] - fixed[sMinus];


		int e = S * H - N - (H - h[sPlus]) - (H - h[sMinus]);

		if (e >= b1 + b2)
		{

			for (int i = 0, j = 0; i < b1 || j < b2; )
			{
				if (j == b2 || i != b1 && p(sPlus, h[sPlus]) > p(sMinus, h[sMinus]))
				{
					relocate(sPlus, new int[]{sPlus, sMinus}, pStar);
					i++;
				} else
				{
					relocate(sMinus, new int[]{sPlus, sMinus}, pStar);
					j++;
				}
			}
			move(sPlus, sMinus);
			fix(sMinus);

		} else if (e > b1)
		{

			for (int i = 0, j = 0; i < b1 || j < e - 1 - b1; )
			{
				if (j == e - 1 - b1 || i != b1 && p(sPlus, h[sPlus]) > p(sMinus, h[sMinus]))
				{
					relocate(sPlus, new int[]{sPlus, sMinus}, pStar);
					i++;
				} else
				{
					relocate(sMinus, new int[]{sPlus, sMinus}, pStar);
					j++;
				}
			}
			int tmp = -1;
			for (int s = 1; s <= S; s++)
			{
				if (s == sPlus || s == sMinus || h[s] == H)
					continue;
				move(sPlus, s);

				tmp = s;
				break;
			}
			for (int j = e - 1 - b1; j < b2; j++)
			{
				relocate(sMinus, new int[]{sMinus, tmp}, pStar);
			}
			move(tmp, sMinus);
			fix(sMinus);

		} else if (e >= 1)
		{


			for (int i = 0; i < e - 1; i++)
			{
				relocate(sPlus, new int[]{sPlus, sMinus}, pStar);
			}
			for (int i = e - 1; i < b1; i++)
			{
				move(sPlus, sMinus);
			}
			int tmp = -1;
			for (int s = 1; s <= S; s++)
			{
				if (s == sPlus || s == sMinus || h[s] == H)
					continue;

				move(sPlus, s);
				tmp = s;
				break;
			}
			for (int i = e - 1; i < b1 + b2; i++)
			{
				relocate(sMinus, new int[]{sMinus, tmp}, pStar);
			}
			move(tmp, sMinus);
			fix(sMinus);

		} else
		{

			int tmp = selectInterimE4(sPlus, sMinus);

			boolean regression = (fixed[tmp] == H);

			if (regression)
				unfix(tmp);
			move(tmp, sMinus);

			for (int i = 0; i < b1; i++)
			{
				move(sPlus, sMinus);
			}
			move(sPlus, tmp);

			for (int i = 0; i < b1 + 1 + b2; i++)
			{
				move(sMinus, sPlus);
			}
			move(tmp, sMinus);
			fix(sMinus);

			if (regression)
			{

				for (int i = 0; i < b2; i++)
					relocate(sPlus, new int[]{sPlus, tmp}, pStar);
				move(sPlus, tmp);
				fix(tmp);
			}
		}
	}


}
