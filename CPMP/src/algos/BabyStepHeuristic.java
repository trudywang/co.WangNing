package algos;

import cpmp.Container;
import cpmp.Layout;
import cpmp.Operation;
import cpmp.Pair;
import cpmp.State;

public class BabyStepHeuristic
{
	public static void fixOnlyOneBabyMove(State state, Pair<Container, Integer> pair) throws Exception
	{
		Container ut = pair.getL();
		int settleStack = pair.getR();
		int utIndex = ut.uniqueContainerIndex;
		int utStack = state.layout.atStack[utIndex];

		if (utStack == settleStack)
			fixSameOnlyOneBabyMove(state, ut);
		else
			fixDiffOnlyOneBabyMove(state, ut, settleStack);

		state.tryFixings();
	}

	private static void relocateObstructorOneMove(State state, Container cp, int[] reloforbid, int[] fulforbid) throws Exception
	{
		Layout layout = state.layout;

		int s = GaintStepHeuristic.selectDestination(layout, cp, reloforbid);
		Operation after = new Operation(cp, layout.atStack[cp.uniqueContainerIndex], s);

		if (GaintStepHeuristic.fulfill(state, after, fulforbid))
			;
		else
			state.goOneStep(after);
	}

	public static void fixSameOnlyOneBabyMove(State state, Container ut) throws Exception
	{

		Layout layout = state.layout;
		int utIndex = ut.uniqueContainerIndex;
		int utStack = layout.atStack[utIndex];

		if (layout.topContainer(utStack).equals(ut) == false)
		{
			Container cp = layout.topContainer(utStack);
			relocateObstructorOneMove(state, cp, null, null);
			return;
		}
		int ts = GaintStepHeuristic.selectTemporary(layout, ut, null);
		int es = layout.numberOfEmptySlotsWithout(new int[] { ts, utStack });
		int belowUT = layout.numberOfUnfixed(utStack) - 1;
		if (belowUT <= es)
		{
			Operation tempStore = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore);
			return;

		}
		else if (es > 0)
		{
			Operation tempStore1 = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore1);

			return;
		}
		else
		{
			int ts2 = GaintStepHeuristic.selectTemporary2(layout, ut, ts, null);
			Container c2 = layout.topContainer(ts2);
			boolean isFixed = layout.isFixed(c2);

			if (isFixed)
			{
				Operation unfix = new Operation(c2, -ts2, 0);
				state.goOneStep(unfix);
			}
			Operation vacate = new Operation(c2, ts2, ts);
			state.goOneStep(vacate);

			Operation tempStore = new Operation(ut, utStack, ts2);
			state.goOneStep(tempStore);

			while (layout.stackHeight[utStack] > layout.fixedHeight[utStack])
			{
				Container cp = layout.topContainer(utStack);

				GaintStepHeuristic.relocateObstructor(state, cp, new int[] { ts2 }, new int[] { ts2 });// jinbo
				// fix
			}

			// wn
			Operation move = new Operation(ut, ts2, utStack);
			state.goOneStep(move);

			Operation fix = new Operation(ut, utStack, 0);
			state.goOneStep(fix);
			// wn

			if (isFixed)
			{
				if (layout.isClean(c2) && ts!=layout.dummyStack)
					throw new Exception("c2 is already clean!!");

				while (layout.topContainer(ts).equals(c2) == false)
				{
					GaintStepHeuristic.relocateObstructor(state, layout.topContainer(ts), new int[] { ts2 }, new int[] { ts2 });
				}

				Operation back = new Operation(c2, ts, ts2);
				state.goOneStep(back);

				Operation fixback = new Operation(c2, ts2, 0);
				state.goOneStep(fixback);
			}

		}

	}

	public static void fixDiffOnlyOneBabyMove(State state, Container ut, int settleStack) throws Exception
	{
		Layout layout = state.layout;
		int utIndex = ut.uniqueContainerIndex;
		int utStack = layout.atStack[utIndex];

		int num1 = layout.numberOfAbove(ut);
		int num2 = layout.numberOfUnfixed(settleStack);

		int es = layout.numberOfEmptySlotsWithout(new int[] { utStack, settleStack });
		if (es >= num1 + num2)
		{
			int k1 = num1;
			int k2 = num2;
			if (k1 != 0 || k2 != 0)
			{
				if (k1 != 0 && k2 != 0)
				{
					Container c1 = layout.topContainer(utStack);
					Container c2 = layout.topContainer(settleStack);

					if (c1.groupLabel >= c2.groupLabel)
					{
						k1--;
						relocateObstructorOneMove(state, c1, new int[] { settleStack }, new int[] { settleStack });
						return;
					}
					else
					{
						k2--;
						relocateObstructorOneMove(state, c2, new int[] { utStack }, new int[] { utStack });
						return;
					}
				}
				else if (k1 != 0)
				{
					k1--;
					Container c1 = layout.topContainer(utStack);
					relocateObstructorOneMove(state, c1, new int[] { settleStack }, new int[] { settleStack });
					return;
				}
				else
				{
					k2--;
					Container c2 = layout.topContainer(settleStack);
					relocateObstructorOneMove(state, c2, new int[] { utStack }, new int[] { utStack });
					return;
				}
			}
			Operation move = new Operation(ut, utStack, settleStack);
			state.goOneStep(move);
			return;
		}

		else if (num1 <= es - 1)
		{
			int k1 = num1;
			int k2 = es - 1 - k1;

			if (k1 != 0 || k2 != 0)
			{
				if (k1 != 0 && k2 != 0)
				{
					Container c1 = layout.topContainer(utStack);
					Container c2 = layout.topContainer(settleStack);

					if (c1.groupLabel >= c2.groupLabel)
					{
						k1--;
						relocateObstructorOneMove(state, c1, new int[] { settleStack }, new int[] { settleStack });
						return;
					}
					else
					{
						k2--;
						relocateObstructorOneMove(state, c2, new int[] { utStack }, new int[] { utStack });
						return;
					}
				}
				else if (k1 != 0)
				{
					k1--;
					Container c1 = layout.topContainer(utStack);
					relocateObstructorOneMove(state, c1, new int[] { settleStack }, new int[] { settleStack });
					return;
				}
				else
				{
					k2--;
					Container c2 = layout.topContainer(settleStack);
					relocateObstructorOneMove(state, c2, new int[] { utStack }, new int[] { utStack });
					return;
				}
			}

			int ts = GaintStepHeuristic.selectTemporary(layout, ut, new int[] { settleStack });
			Operation tempStore = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore);
			return;

		}
		else if (es - 1 >= 0)
		{
			if (layout.numberOfEmptySlotsWithout(new int[] { utStack, settleStack }) > 1)
			{
				Container c1 = layout.topContainer(utStack);
				relocateObstructorOneMove(state, c1, new int[] { settleStack }, new int[] { settleStack });
				return;
			}
			if (layout.numberOfAbove(ut) > 0)
			{
				Container c1 = layout.topContainer(utStack);
				Operation move = new Operation(c1, utStack, settleStack);
				state.goOneStep(move);
				return;
			}
			int ts = GaintStepHeuristic.selectTemporary(layout, ut, new int[] { settleStack });
			Operation tempStore = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore);
			return;

		}
		else
		{
			int ts2 = GaintStepHeuristic.selectTemporary2(layout, ut, settleStack, null);
			Container v = layout.topContainer(ts2);
			boolean isFixed = layout.isFixed(v);

			if (isFixed)
			{
				Operation unfix = new Operation(v, -ts2, 0);
				state.goOneStep(unfix);
			}
			Operation vacate = new Operation(v, ts2, settleStack);
			state.goOneStep(vacate);

			while (layout.numberOfAbove(ut) > 0)
			{
				Container c1 = layout.topContainer(utStack);
				Operation move = new Operation(c1, utStack, settleStack);
				state.goOneStep(move);
			}

			Operation tempStore = new Operation(ut, utStack, ts2);
			state.goOneStep(tempStore);

			while (layout.stackHeight[settleStack] > layout.fixedHeight[settleStack])
			{
				Container cp = layout.topContainer(settleStack);
				GaintStepHeuristic.relocateObstructor(state, cp, new int[] { ts2 }, new int[] { ts2 });
			}
			// wn
			Operation move = new Operation(ut, ts2, settleStack);
			state.goOneStep(move);

			Operation fix = new Operation(ut, settleStack, 0);
			state.goOneStep(fix);
			// wn

			if (isFixed)
			{
				int vnow = layout.atStack[v.uniqueContainerIndex];
				
				if (layout.isClean(v) && vnow!=layout.dummyStack)
					throw new Exception("v is already clean!!");

				

				while (layout.topContainer(vnow).equals(v) == false)
				{
					GaintStepHeuristic.relocateObstructor(state, layout.topContainer(vnow), new int[] { ts2 }, new int[] { ts2 });
				}
				Operation back = new Operation(v, vnow, ts2);
				state.goOneStep(back);

				Operation fixback = new Operation(v, ts2, 0);
				state.goOneStep(fixback);
			}

		}

	}
}
