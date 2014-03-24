package algos;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import cpmp.Constant;
import cpmp.Container;
import cpmp.Layout;
import cpmp.Pair;
import cpmp.State;
import cpmp.UrgentTargetSelection;

public class Lookahead
{
	public Lookahead(int depthLimit, Method lowerBound, int WIDTH, int probingevaluation)
	{
		this.depthLimit = depthLimit;
		this.lowerBound = lowerBound;
		this.WIDTH = WIDTH;
		this.probingevaluation = probingevaluation;
	}

	public int WIDTH;
	public Method lowerBound;
	public int depthLimit;
	public int probingevaluation;

	public Pair<Container, Integer> mostPromisingRelocation(State state) throws Exception
	{
		Pair<Pair<Container, Integer>, Integer> pair = getBestBranch(state, 0);
		if (pair == null)
			return null;
		return pair.getL();
	}

	public int evaluateScore(State state) throws Exception
	{
		Pair<Pair<Container, Integer>, Integer> pair = getBestBranch(state, 0);
		if (pair == null)
			return -1;
		return pair.getR();
	}

	// public static long probingTime;
	// public static long undoTime;
	// public static long lbTime;
	public Pair<Pair<Container, Integer>, Integer> getBestBranch(State state, int depth) throws Exception
	{
		// long mark=System.currentTimeMillis();
		int LBnow = (Integer) lowerBound.invoke(null, state.layout);
		// lbTime+=System.currentTimeMillis()-mark;
		if (state.best!=null && LBnow + state.sol.relocationCount >= state.best.relocationCount)
			return null;

		int backup = state.size();

		if (state.isAllFixed() || depth >= depthLimit)
		{
			// mark=System.currentTimeMillis();
			GaintStepHeuristic.probing(state, probingevaluation);
			// probingTime+=System.currentTimeMillis()-mark;
			Pair<Pair<Container, Integer>, Integer> laa = new Pair<Pair<Container, Integer>, Integer>(null, state.sol.relocationCount);
			// mark=System.currentTimeMillis();
			while (state.size() > backup)
				state.undo();
			// undoTime+=System.currentTimeMillis()-mark;

			return laa;
		}
		else
		{
			Pair<Pair<Container, Integer>, Integer> res = null;

			ArrayList<Pair<Container, Integer>> list = UrgentTargetSelection.allAvailableNextSettlement(state.layout,WIDTH,probingevaluation);

			for (Pair<Container, Integer> op : list)
			{

				GaintStepHeuristic.fixOneGaintMove(state, op);

				Pair<Pair<Container, Integer>, Integer> next = getBestBranch(state, depth + 1);

				if (next != null && (res == null || res.getR() > next.getR()))
					res = new Pair<Pair<Container, Integer>, Integer>(op, next.getR());

				while (state.size() > backup)
					state.undo();

			}
			return res;
		}
	}

	

}
