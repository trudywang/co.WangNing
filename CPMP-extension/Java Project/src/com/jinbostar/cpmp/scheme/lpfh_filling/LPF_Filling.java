package com.jinbostar.cpmp.scheme.lpfh_filling;

import com.jinbostar.cpmp.common.Operation;
import com.jinbostar.cpmp.common.Task;
import com.jinbostar.cpmp.scheme.fbh.Node;

import java.util.TreeSet;

import static com.jinbostar.cpmp.common.Parameter.DualSenderOrder.SmallerEvalFirstOrder;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.LargerPriority;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.MoveActual;
import static com.jinbostar.cpmp.common.Parameter.TopFilter.NoTopFilter;


/*
 * Orderliness, Extreme state avoidance, Speedy task accomplishment
 *
 * select next task by fewest moves needed (greedy)
 * select destination by farther influence
 *
 */
public class LPF_Filling extends Node
{


	public LPF_Filling(int S, int H, int P, int N, int[][] bay) throws Exception
	{
		super(S, H, P, N, bay);
		topFilter = NoTopFilter;
		stability = true;
		preferences = new TaskPreference[]{
				LargerPriority,
				MoveActual,
		};
		order = SmallerEvalFirstOrder;
		bottomFilter = null;
	}

	private void filling(int receiver)
	{
		while (h[receiver] != H)
		{
			int largest = 0;
			int sender = -1;
			for (int s = 1; s <= S; s++)
			{
				if (s == receiver || isNiceStack(s))
					continue;
				int p = p(s, h[s]);
				if (becomeNiceByMove(s, receiver))
				{
					if (sender == -1 || largest < p)
					{
						sender = s;
						largest = p;
					}
				}
			}
			if (sender == -1)
				break;

			move(sender, receiver);

		}
	}

	public int solve()
	{
		for (int p = P; p >= 1; p--)
		{
			TreeSet<Integer> list = new TreeSet<Integer>();
			while (demand[p] > 0)
			{
				//System.out.println(this.sideView());
				Task task = nextTask();

				if (task.internal())
				{
					internal(task);
				} else if (task.external())
				{
					external(task);
				}
				fix(task.aim_stack);
				list.add(task.aim_stack);
			}
			for (int s : list)
				filling(s);
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

}
