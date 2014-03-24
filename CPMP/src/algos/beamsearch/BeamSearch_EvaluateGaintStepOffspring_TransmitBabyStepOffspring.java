package algos.beamsearch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import algos.BabyStepHeuristic;
import algos.GaintStepHeuristic;
import algos.Lookahead;
import cpmp.Container;
import cpmp.Layout;
import cpmp.Pair;
import cpmp.Solution;
import cpmp.SolutionReport;
import cpmp.State;
import cpmp.UrgentTargetSelection;

public class BeamSearch_EvaluateGaintStepOffspring_TransmitBabyStepOffspring extends BeamSearch
{

	public Method lowerBound;
	public Layout instance;
	public int maxPoolSize;
	public int lookaheadDepth;
	public int lookaheadWidth;
	public int probingevaluation;

	public BeamSearch_EvaluateGaintStepOffspring_TransmitBabyStepOffspring(Layout inst, Method lowerBound, int maxPoolSize, int lookaheadDepth, int lookaheadWidth, int probingevaluation)
	{
		instance = inst;
		this.lowerBound = lowerBound;
		this.maxPoolSize = maxPoolSize;
		this.lookaheadDepth = lookaheadDepth - 1;
		this.lookaheadWidth = lookaheadWidth;
		this.probingevaluation = probingevaluation;
	}

	public SolutionReport solve() throws Exception
	{
		long startTime = System.currentTimeMillis();

		int LB = (Integer) lowerBound.invoke(null, instance);

		State init_state = new State();
		init_state.layout = instance.copy();
		init_state.sol = new Solution();
		GaintStepHeuristic.probing(init_state, probingevaluation);
		// best is always updated automatically at the end of probing

		Solution IS = init_state.best.copy();
		Solution best = init_state.best.copy();

		init_state.layout = instance.copy();
		init_state.sol = new Solution();

		// raw data must do this first before going into lookahead
		init_state.tryFixings();

		State[] pool = new State[maxPoolSize];
		int poolSize = 1;
		pool[0] = init_state;

		Lookahead la = new Lookahead(lookaheadDepth, lowerBound, lookaheadWidth, probingevaluation);
		int iter = 1;
		
		while (poolSize > 0)
		{
			long mark = System.currentTimeMillis();
			TreeSet<StateScore> offspring = new TreeSet<StateScore>();

			for (int __i = 0; __i < poolSize; __i++)
			{
				State state = pool[__i];
				int LBnow = (Integer) lowerBound.invoke(null, state.layout);

				if (LBnow + state.sol.relocationCount >= best.relocationCount)
					continue;
				ArrayList<Pair<Container, Integer>> list = UrgentTargetSelection.allAvailableNextSettlement(state.layout,lookaheadWidth,probingevaluation);

				for (Pair<Container, Integer> move : list)
				{
					State next = new State();
					next.layout = state.layout.copy();
					next.sol = state.sol.copy();
					next.best = null;
					BabyStepHeuristic.fixOnlyOneBabyMove(next, move);

					State dummy = new State();
					dummy.layout = state.layout.copy();
					dummy.sol = state.sol.copy();
					dummy.best = null;
					GaintStepHeuristic.fixOneGaintMove(dummy, move);

					int score = la.evaluateScore(dummy);

					if (best == null || best.relocationCount > dummy.best.relocationCount)
					{
						best = dummy.best;
					}

					if (score != -1)
					{
						update(offspring, maxPoolSize, new StateScore(next, score));
					}
				}
				
			}
//			System.out.println("Iter " + iter++ + ": poolSize=" + poolSize + ", time use=" + (System.currentTimeMillis() - mark) / 1000);
			
			poolSize = 0;
			for (StateScore s : offspring)
				pool[poolSize++] = s.candidate;
		}

		SolutionReport r = new SolutionReport();

		r.bestEverFound = best;

		r.lowerBound = LB;
		r.initialSolution = IS;
		r.timeUsed = System.currentTimeMillis() - startTime;
		return r;
	}

	private void update(TreeSet<StateScore> offspring, int WIDTH, StateScore v)
	{

		if (offspring.contains(v))
		{
			StateScore s = offspring.floor(v);
			if (s.relo > v.relo)
			{
				offspring.remove(s);
				offspring.add(v);
			}
		}
		else
		{
			offspring.add(v);
			if (offspring.size() > WIDTH)
			{
				offspring.remove(offspring.last());
			}
		}
	}

	public class StateScore implements Comparable<StateScore>
	{
		public State candidate;
		public int hashcode = -1;
		public int relo;

		public StateScore(State a, int b)
		{
			candidate = a;
			relo = b;
			buildHash();
		}

		public class ArrayComp implements Comparator<int[]>
		{
			public int compare(int[] a, int[] b)
			{
				if (a.length != b.length)
					return a.length - b.length;
				for (int i = 0; i < a.length; i++)
					if (a[i] != b[i])
						return a[i] - b[i];
				return 0;
			}
		}

		public void buildHash()
		{
			if (hashcode == -1)
			{
				int S = candidate.layout.S;

				int[][] v = new int[S][];
				for (int i = 1; i <= S; i++)
				{
					v[i - 1] = new int[candidate.layout.stackHeight[i]];
					for (int j = 1; j <= candidate.layout.fixedHeight[i]; j++)
					{
						v[i - 1][j - 1] = -1;
					}
					for (int j = candidate.layout.fixedHeight[i] + 1; j <= candidate.layout.stackHeight[i]; j++)
					{
						v[i - 1][j - 1] = candidate.layout.bay[i][j].groupLabel;
					}
				}

				Arrays.sort(v, new ArrayComp());
				hashcode = Arrays.deepToString(v).hashCode();
			}
		}

		@Override
		public int compareTo(StateScore o)
		{
			// this.buildHash();
			// o.buildHash();

			if (hashcode == o.hashcode)
				return 0;
			else if (relo != o.relo)
				return relo - o.relo;
			else
				return hashcode < o.hashcode ? -1 : 1;

		}

	}
}
