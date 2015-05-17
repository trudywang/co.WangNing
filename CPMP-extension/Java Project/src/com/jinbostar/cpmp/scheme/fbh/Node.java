package com.jinbostar.cpmp.scheme.fbh;

import com.jinbostar.cpmp.common.Operation;
import com.jinbostar.cpmp.common.State;
import com.jinbostar.cpmp.common.Task;
import com.jinbostar.cpmp.common.Tuple;

import java.util.ArrayList;

import static com.jinbostar.cpmp.common.Parameter.*;
import static com.jinbostar.cpmp.common.Parameter.DualSenderOrder.*;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.*;
import static com.jinbostar.cpmp.common.Parameter.TopFilter.TrickyAvoid;

/*
 * Orderliness, Extreme state avoidance, Speedy task accomplishment
 *
 * select next task by fewest moves needed (greedy)
 * select destination by farther influence
 *
 */
public class Node extends State implements Cloneable
{

	public TopFilter topFilter;
	public BottomFilter bottomFilter;
	public boolean stability;
	public TaskPreference[] preferences;
	public DualSenderOrder order;

	public Node(int S, int H, int P, int N, int[][] bay) throws Exception
	{
		super(S, H, P, N, bay);
		topFilter = TrickyAvoid;
		stability = true;
		preferences = new TaskPreference[]{
				MoveActual, BlockingNiceInAimStack, DemandAffected,
				LowerAimTier, LargerPriority,
		};
		order = SmallerEvalFirstOrder;
		bottomFilter = new BottomFilter(2, S);
	}

	public Node clone() throws CloneNotSupportedException
	{
		Node ret = (Node) super.clone();
		return ret;
	}

	private void move(int s1, int k, int s2)
	{
		for (int i = 1; i <= k; i++)
			move(s1, s2);
	}

	public int demandAffect(int p, int q)
	{
		return DEMAND[p + 1] - DEMAND[q + 1];
	}

	public void STAP(Task task)
	{
		if (task.internal())
		{
			internal(task);
		} else if (task.external())
		{
			external(task);
		}

		fix(task.aim_stack);
	}

	public int solve()
	{

		while (F < N)
		{
			//		System.out.println(this.sideView());

			STAP(nextTask());


		}

		//	System.out.println(this.sideView());

		int res = 0;
		for (Operation op : path)
		{
			if (op.type == Operation.Type.Move)
				res++;

		}
		return res;
	}

	public int[] evalTaskPrune(Task task, int[] best)
	{
		int p = p(task.current_stack, task.current_tier);
		int q = q(task.aim_stack, fixed[task.aim_stack]);

		int qq = q;
		while (DEMAND[qq] == 0)
			qq--;


		int[] cost = new int[preferences.length + 1];

		cost[0] = 0;
		switch (topFilter)
		{
			case TrickyAvoid:
				if (fixed[task.aim_stack] == H - 1 && preTricky())
					cost[0] = 2;
				break;

			case FullyFixedAvoid:
				if (fixed[task.aim_stack] == H - 1)
					cost[0] = 2;
				break;

			case ExtremeAvoid:
				if (fixed[task.aim_stack] == H - 1 && preExtreme())
					cost[0] = 2;
				break;
		}


		if (bottomFilter != null && fixed[task.aim_stack] < bottomFilter.tier && demandAffect(p, q) >= bottomFilter.threshold)
		{
			assert cost[0] == 0;
			cost[0] = 1;
		}

		if (cost[0] > 0)
			return null;


		if (Tuple.prune(best, cost, 1))
			return null;


		for (int i = 1; i <= preferences.length; i++)
		{
			TaskPreference pref = preferences[i - 1];
			switch (pref)
			{
				case MoveActual:
					cost[i] = movesActual(task);
					break;
				case BlockingAboveTarget:
					cost[i] = blockingAboveTarget(task);
					break;
				case BlockingInAimStack:
					cost[i] = blockingInAimStack(task);
					break;
				case Gap:
					cost[i] = q - p;
					break;

				case RevisedGap:
					cost[i] = qq - p;
					break;

				case LargerPriority:
					cost[i] = -p;
					break;
				case SmallerPriority:
					cost[i] = p;
					break;

				case MoveIdeal:
					cost[i] = movesIdeal(task);
					break;

				case DemandAffected:
					cost[i] = demandAffect(p, q);
					break;

				case Capability:
					cost[i] = q;
					break;

				case RevisedCapability:
					cost[i] = qq;
					break;

				case BlockingNice:
					cost[i] = niceBlocking(task);
					break;

				case BlockingNiceInAimStack:
					cost[i] = niceBlockingInAimStack(task);
					break;
				case BlockingNiceInCurrentStack:
					cost[i] = niceBlockingInCurrentStack(task);
					break;

				case LowerAimTier:
					cost[i] = fixed[task.aim_stack];
					break;

				case HigherAimTier:
					cost[i] = -fixed[task.aim_stack];
					break;

				case LeftmostAimStack:
					cost[i] = task.aim_stack;
					break;

				case RightmostAimStack:
					cost[i] = -task.aim_stack;
					break;

				case LeftmostCurrentStack:
					cost[i] = task.current_stack;
					break;

				case RightmostCurrentStack:
					cost[i] = -task.current_stack;
					break;

				case HigherCurrentTier:
					cost[i] = -task.current_tier;
					break;

				case LowerCurrentTier:
					cost[i] = task.current_tier;
					break;

				case MoveOverall:
					cost[i] = movesActual(task) + niceBlocking(task);
					break;
				case InternalPrefer:
					cost[i] = task.immediate() ? 0 : task.internal() ? 1 : 2;
					break;

				default:
					cost[i] = 0;
			}

			if (Tuple.prune(best, cost, 1 + i))
				return null;
		}
		return cost;
	}

	private int niceBlockingInAimStack(Task task)
	{
		if (task.immediate())
			return 0;
		else if (stability)
			return stable[task.aim_stack] - fixed[task.aim_stack];
		else
			return orderly[task.aim_stack] - fixed[task.aim_stack];
	}

	private int niceBlockingInCurrentStack(Task task)
	{
		if (task.immediate())
			return 0;
		else if (stability)
		{
			if (stable[task.current_stack] >= task.current_tier)
				return stable[task.current_stack] - task.current_tier + 1;
			else
				return 0;
		} else
		{
			if (orderly[task.current_stack] >= task.current_tier)
				return orderly[task.current_stack] - task.current_tier + 1;
			else
				return 0;
		}
	}


	private int niceBlocking(Task task)
	{
		if (task.immediate())
			return 0;
		else if (task.external())
			return niceBlockingInAimStack(task) + niceBlockingInCurrentStack(task);
		else
		{
			return niceBlockingInCurrentStack(task);
		}
	}

	public Task nextTask()
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


					if (minDelta[p][q] < H - fixed[sMinus])
						continue;
					//			boolean keep = true;
					//		for (int g = p + 1; g <= q && keep; g++)
					//			{
					//				if (RESOURCE[g]-DEMAND[g] < H - fixed[sMinus])
					//					keep = false;
					//			}

					//			if (!keep)
					//				continue;

					Task task = new Task(p, sPlus, tPlus, sMinus, fixed[sMinus] + 1);


					int[] temp = evalTaskPrune(task, cost);
					if (temp != null)
					{
						if (best == null || Tuple.compare(cost, temp) > 0)
						{
							best = task;
							cost = temp;
						}
					}

				}
			}
		}


		return best;
	}


	protected void relocate(int s1, int k, Integer[] D)
	{
		for (int i = 1; i <= k; i++)
		{
			int[] v = minMovePenalty(s1, D);
			int s2 = v[v.length - 1];
			move(s1, s2);
		}
	}

	private void dualSender(int s1, int k1, int s2, int k2, Integer[] D)
	{
		int i = k1, j = k2;
		while (i + j > 0)
		{
			if (j == 0)
			{
				relocate(s1, i, D);
				i = 0;
			} else if (i == 0)
			{
				relocate(s2, j, D);
				j = 0;
			} else
			{
				if (order == SmallerEvalFirstOrder)
				{
					int[] v1 = minMovePenalty(s1, D);
					int[] v2 = minMovePenalty(s2, D);
					if (Tuple.compare(v1, v2) <= 0)
					{
						int s = v1[v1.length - 1];
						move(s1, s);
						i--;
					} else
					{
						int s = v2[v2.length - 1];
						move(s2, s);
						j--;
					}
				} else if (order == SmallerPriorityFirstOrder)
				{
					if (p(s1, h[s1]) <= p(s2, h[s2]))
					{
						int[] v = minMovePenalty(s1, D);
						move(s1, v[v.length - 1]);
						i--;
					} else
					{
						int[] v = minMovePenalty(s2, D);
						move(s2, v[v.length - 1]);
						j--;
					}
				} else//order==largerPri
				{
					assert order == LargerPriorityFirstOrder;
					if (p(s1, h[s1]) >= p(s2, h[s2]))
					{
						int[] v = minMovePenalty(s1, D);
						move(s1, v[v.length - 1]);
						i--;
					} else
					{
						int[] v = minMovePenalty(s2, D);
						move(s2, v[v.length - 1]);
						j--;
					}
				}
			}
		}
	}

	private void dualReceiver(int s1, int k1, Integer[] D1, int k2, Integer[] D2)
	{
		int i = k1, j = k2;
		while (i + j > 0)
		{
			if (j == 0)
			{
				relocate(s1, i, D1);
				i = 0;
			} else if (i == 0)
			{
				relocate(s1, j, D2);
				j = 0;
			} else
			{
				int[] v1 = minMovePenalty(s1, D1);
				int[] v2 = minMovePenalty(s1, D2);
				if (Tuple.compare(v1, v2) <= 0)
				//if(false)
				{
					int s = v1[v1.length - 1];
					move(s1, s);
					i--;
				} else
				{
					int s = v2[v2.length - 1];
					move(s1, s);
					j--;
				}
			}
		}
	}


	private int blockingAboveTarget(Task task)
	{
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;
		int sMinus = task.aim_stack;

		if (task.immediate())
		{
			return 0;
		} else
		{

			return h[sPlus] - tPlus;
		}

	}

	private int blockingInAimStack(Task task)
	{
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;
		int sMinus = task.aim_stack;

		if (task.immediate())
		{
			return 0;
		} else
		{


			return h[sMinus] - fixed[sMinus];
		}

	}

	private int movesIdeal(Task task)
	{
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;
		int sMinus = task.aim_stack;

		if (task.immediate())
		{
			return 0;
		} else if (task.internal())
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

	private int movesActual(Task task)
	{
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;
		int sMinus = task.aim_stack;

		if (task.immediate())
		{
			return 0;
		} else if (task.internal())
		{
			int highest = 0;
			for (int s = 1; s <= S; s++)
			{
				if (s == sPlus || h[s] == H)
					continue;
				highest = Math.max(highest, h[s]);
			}
			int a = (S * H - N) - (H - h[sPlus]) - (H - highest);


			int b1 = h[sPlus] - tPlus;
			int b2 = tPlus - fixed[sPlus] - 1;
			if (a >= b2)
				return b1 + b2 + 2;
			else
			{
				Integer[] nonFull = nonFullStacks(sPlus);
				if (nonFull.length > 1)
				{
					return b1 + b2 + 3;
				} else
				{
					int last = nonFull[0];
					int interim = interimFull(stacksExcept(sPlus, last));

					boolean regression = (fixed[interim] == H);
					if (!regression)
					{
						return b1 + b2 + 3;
					} else
					{
						return b1 + b2 + 3 + b2 + 1;
					}
				}
			}
		} else
		{
			int a = (S * H - N) - (H - h[sPlus]) - (H - h[sMinus]);

			int b1 = h[sPlus] - tPlus;
			int b2 = h[sMinus] - fixed[sMinus];
			if (a >= b1 + b2)
				return b1 + b2 + 1;
			else if (b1 + 1 <= a && a < b1 + b2)
				return b1 + b2 + 2;
			else if (1 <= a && a < b1 + b2 && a < b1 + 1)
				return 2 * b1 + b2 - a + 3;
			else
			{
				int interim = interimFull(stacksExcept(sPlus, sMinus));
				boolean regression = (fixed[interim] == H);
				if (!regression)
				{
					return 2 * b1 + b2 + 4;
				} else
				{
					return 2 * b1 + b2 + 4 + b2 + 1;
				}
			}
		}
	}

	private Integer[] destinationDetect(int sPlus, int b2)
	{

		int highest = 0;
		int second = 0;
		int count = 0;
		for (int s = 1; s <= S; s++)
		{
			if (s == sPlus || h[s] == H)
				continue;

			if (h[s] > highest)
			{
				second = highest;
				highest = h[s];
				count = 1;
			} else if (h[s] == highest)
			{
				count++;
			} else if (h[s] > second)
			{
				second = h[s];
			}
		}

		ArrayList<Integer> A = new ArrayList<Integer>();
		for (int s = 1; s <= S; s++)
		{
			if (s == sPlus || h[s] == H)
				continue;
			int a;
			if (h[s] == highest && highest == H - 1 && count == 1)
			{
				a = (S * H - N) - (H - h[sPlus]) - 1 - (H - second);
			} else if (h[s] == highest && highest == H - 1 && count > 1)
			{
				a = (S * H - N) - (H - h[sPlus]) - 2;
			} else if (h[s] < highest)
			{
				a = (S * H - N) - (H - h[sPlus]) - 1 - (H - highest);
			} else //if(height[s]==highest && highest!=H-1)
			{
				a = (S * H - N) - (H - h[sPlus]) - (H - h[s]);
			}
			if (a >= b2)
				A.add(s);
		}
		return A.toArray(new Integer[0]);
	}

	private Integer[] interimDetect(int sPlus, int b2)
	{
		ArrayList<Integer> A = new ArrayList<Integer>();
		for (int s = 1; s <= S; s++)
		{
			if (s == sPlus || h[s] == H)
				continue;
			int a = (S * H - N) - (H - h[sPlus]) - (H - h[s]);
			if (a >= b2)
				A.add(s);
		}
		return A.toArray(new Integer[0]);
	}

	protected void internal(Task task)
	{
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;

		int highest = 0;
		for (int s = 1; s <= S; s++)
		{
			if (s == sPlus || h[s] == H)
				continue;
			highest = Math.max(highest, h[s]);
		}
		int a = (S * H - N) - (H - h[sPlus]) - (H - highest);

		int b1 = h[sPlus] - tPlus;
		int b2 = tPlus - fixed[sPlus] - 1;
		if (a >= b2)
		{
			for (int i = 1; i <= b1; i++)
			{
				Integer[] D = destinationDetect(sPlus, b2);
				relocate(sPlus, 1, D);
			}

			Integer[] I = interimDetect(sPlus, b2);
			int interim = interim(I);
			move(sPlus, interim);
			relocate(sPlus, b2, stacksExcept(sPlus, interim));
			move(interim, sPlus);
		} else
		{
			Integer[] nonFull = nonFullStacks(sPlus);
			if (nonFull.length > 1)
			{
				relocate(sPlus, b1, stacksExcept(sPlus));
				int interim = interim(stacksExcept(sPlus));
				move(sPlus, interim);
				int k = (S * H - N) - (H - h[sPlus]) - (H - h[interim]);
				relocate(sPlus, k - 1, stacksExcept(sPlus, interim));
				int interim2 = nonFullStacks(sPlus, interim)[0];
				move(interim, interim2);
				move(sPlus, b2 - k + 1, interim);
				move(interim2, sPlus);
			} else
			{
				int last = nonFull[0];
				int interim = interimFull(stacksExcept(sPlus, last));

				boolean regression = (fixed[interim] == H);
				if (!regression)
				{
					dualSender(interim, 1, sPlus, b1, new Integer[]{last});
					move(sPlus, interim);
					move(sPlus, b2, last);
					move(interim, sPlus);
				} else
				{
					move(sPlus, b1, last);
					unfix(interim);
					move(interim, last);
					move(sPlus, interim);
					move(sPlus, b2, last);
					move(interim, sPlus);
					move(last, b2, sPlus);
					move(last, interim);
					fix(interim);
				}
			}
		}
	}

	public boolean isNiceStack(int s)
	{
		if (stability)
			return stable[s] == h[s];
		else
			return orderly[s] == h[s];
	}

	public int messiness(int s)
	{
		if (stability)
			return largestDisorderlyPriority(s);
		else
			return largestBadlyPlacedPriority(s);
	}

	public boolean becomeNiceByMove(int s1, int s2)
	{
		assert s1 != s2;
		assert h[s1] > 0 && h[s2] < H;

		if (stability)
			return becomeStableByMove(s1, s2);
		else
			return orderly[s2] == h[s2] && q(s2, h[s2]) >= p(s1, h[s1]);
	}

	private int interim(Integer[] I)
	{

		int best = -1;

//		int maxH = -1;
//		for (int s : I)
//		{
//			if (h[s] == H)
//				continue;
//			if (isNiceStack(s) && DEMAND[1]-DEMAND[q(s,h[s])+1]==0)
//			{
//				int hei = h[s];
//				if (best == -1 || maxH < hei)
//				{
//					best = s;
//					maxH=hei;
//				}
//			}
//		}
//		if (best != -1)
//			return best;


		int maxM = -1;
		for (int s : I)
		{
			if (h[s] == H)
				continue;
			if (!isNiceStack(s))
			{
				int m = messiness(s);
				if (best == -1 || maxM < m)
				{
					best = s;
					maxM = m;
				}
			}


		}
		if (best != -1)
			return best;
		int minQ = -1;
		for (int s : I)
		{
			if (h[s] == H)
				continue;
			if (isNiceStack(s))
			{
				int q = q(s, h[s]);
				if (best == -1 || minQ > q)
				{
					best = s;
					minQ = q;
				}
			}
		}
		return best;
	}

	private int interimFull(Integer[] I)
	{
		int best = -1;
		int minP = -1;
		for (int s : I)
		{
			if (!isNiceStack(s))
			{
				int p = p(s, H);
				if (best == -1 || minP > p)
				{
					best = s;
					minP = p;
				}
			}
		}
		if (best != -1)
			return best;

		for (int s : I)
		{
			if (isNiceStack(s))
			{
				int p = p(s, H);
				if (best == -1 || minP > p)
				{
					best = s;
					minP = p;
				}
			}
		}
		return best;
	}

	protected Integer[] stacksExcept(int s1, int s2)
	{

		ArrayList<Integer> A = new ArrayList<Integer>();
		for (int s = 1; s <= S; s++)
			if (s != s1 && s != s2)
				A.add(s);
		return A.toArray(new Integer[0]);
	}

	public Integer[] stacksExcept(int s1)
	{
		return stacksExcept(s1, -1);
	}

	private Integer[] nonFullStacks(int except)
	{
		return nonFullStacks(except, -1);
	}

	private Integer[] nonFullStacks(int except1, int except2)
	{
		ArrayList<Integer> A = new ArrayList<Integer>();
		for (int s = 1; s <= S; s++)
			if (s != except1 && s != except2 && h[s] < H)
				A.add(s);
		return A.toArray(new Integer[0]);
	}

	public void external(Task task)
	{
		int sPlus = task.current_stack;
		int tPlus = task.current_tier;
		int sMinus = task.aim_stack;

		int a = (S * H - N) - (H - h[sPlus]) - (H - h[sMinus]);

		int b1 = h[sPlus] - tPlus;
		int b2 = h[sMinus] - fixed[sMinus];
		if (a >= b1 + b2)
		{
			dualSender(sPlus, b1, sMinus, b2, stacksExcept(sPlus, sMinus));
			move(sPlus, sMinus);
		} else if (b1 + 1 <= a && a < b1 + b2)
		{
			dualSender(sPlus, b1, sMinus, a - b1 - 1, stacksExcept(sPlus, sMinus));
			int interim = nonFullStacks(sPlus, sMinus)[0];
			move(sPlus, interim);
			move(sMinus, b1 + b2 - a + 1, sPlus);
			move(interim, sMinus);
		} else if (1 <= a && a < b1 + b2 && a < b1 + 1)
		{
			dualReceiver(sPlus, a - 1, stacksExcept(sPlus, sMinus), b1 - a + 1, new Integer[]{sMinus});
			int interim = nonFullStacks(sPlus, sMinus)[0];
			move(sPlus, interim);
			move(sMinus, b1 + b2 - a + 1, sPlus);
			move(interim, sMinus);
		} else
		{
			int interim = interimFull(stacksExcept(sPlus, sMinus));
			boolean regression = (fixed[interim] == H);
			if (!regression)
			{
				dualSender(interim, 1, sPlus, b1, new Integer[]{sMinus});
				move(sPlus, interim);
				move(sMinus, b1 + b2 + 1, sPlus);
				move(interim, sMinus);
			} else
			{
				unfix(interim);
				move(interim, sMinus);
				move(sPlus, b1, sMinus);
				move(sPlus, interim);
				move(sMinus, b1 + b2 + 1, sPlus);
				move(interim, sMinus);
				move(sPlus, b2, sMinus);
				move(sPlus, interim);
				fix(interim);
			}
		}
	}

	public int[] minMovePenalty(int s1, Integer[] D)
	{
		int p = p(s1, h[s1]);
		int[] best = null;

		for (int s : D)
		{
			if (h[s] == H)
				continue;

			int[] temp = new int[3];
			temp[temp.length - 1] = s;

			if (becomeNiceByMove(s1, s))
			{
				temp[0] = 1;
				//	temp[1] = q(s, h[s]) - p;
				temp[1] = demandAffect(p, q(s, h[s]));
			} else
			{
				int m = messiness(s);
				if (!isNiceStack(s) && p >= m)
				{
					temp[0] = 2;
					temp[1] = p - m;
				} else if (!isNiceStack(s) && p < m)
				{
					temp[0] = 3;
					temp[1] = m - p;
				} else
				{
					temp[0] = 4;
					temp[1] = q(s, h[s]);
				}
			}
			if (best == null || Tuple.compare(best, temp) > 0)
			{
				best = temp;
			}
		}
		return best;
	}

	public void setBottomFilter(int tier, int thr)
	{
		bottomFilter.tier = tier;
		bottomFilter.threshold = thr;
	}

	private class BottomFilter
	{
		public int tier;
		public int threshold;

		public BottomFilter(int ti, int th)
		{
			tier = ti;
			threshold = th;
		}
	}


}