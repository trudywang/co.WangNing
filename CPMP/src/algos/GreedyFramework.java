package algos;

import java.lang.reflect.Method;

import cpmp.Container;
import cpmp.Layout;
import cpmp.Pair;
import cpmp.Solution;
import cpmp.SolutionReport;
import cpmp.State;

public class GreedyFramework
{

	public GreedyFramework(Layout inst, Method lowerBound, int lookahead_depth, int WIDTH, int probingevaluation)
	{
		instance = inst;
		this.lowerBound = lowerBound;
		this.lookahead_depth = lookahead_depth;
		this.WIDTH = WIDTH;
		this.probingevaluation = probingevaluation;
	}

	public Method lowerBound;
	public Layout instance;
	public int lookahead_depth;
	public int WIDTH;
	public int probingevaluation;

	public SolutionReport solve1() throws Exception
	{
		long startTime = System.currentTimeMillis();

		int LB = (Integer) lowerBound.invoke(null, instance);

		State state = new State();
		state.layout = instance.copy();
		state.sol = new Solution();
		GaintStepHeuristic.probing(state, probingevaluation);
		Solution IS = state.best.copy();

		state.layout = instance.copy();
		state.sol = new Solution();

		// best is always updated automaticly at the end of probing

		Lookahead la = new Lookahead(lookahead_depth, lowerBound, WIDTH, probingevaluation);

		// Lookahead.probingTime=0;
		// Lookahead.undoTime=0;
		// Lookahead.lbTime=0;

		// long lookaheadtime=0;
		// raw data must do this first before going into lookahead
		state.tryFixings();
		while (!state.isAllFixed())
		{
			int LBnow = (Integer) lowerBound.invoke(null, state.layout);

			if (LBnow + state.sol.relocationCount >= state.best.relocationCount)
				break;
			// long mark=System.currentTimeMillis();
			Pair<Container, Integer> pair = la.mostPromisingRelocation(state);
			// lookaheadtime+=System.currentTimeMillis()-mark;
			if (pair == null)
				break;
			GaintStepHeuristic.fixOneGaintMove(state, pair);
			// state.tryFixings();//it's done after fix
		}
		// System.err.println(lookaheadtime/1000);
		// System.err.println(Lookahead.probingTime/1000 +" "+
		// Lookahead.undoTime/1000+" "+Lookahead.lbTime/1000);
		SolutionReport r = new SolutionReport();

		r.bestEverFound = state.best;

		r.lowerBound = LB;
		r.initialSolution = IS;
		r.timeUsed = System.currentTimeMillis() - startTime;
		return r;

	}

}
