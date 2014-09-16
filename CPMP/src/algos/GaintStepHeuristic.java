package algos;

import cpmp.Constant;
import cpmp.Container;
import cpmp.Layout;
import cpmp.Operation;
import cpmp.Pair;
import cpmp.State;
import cpmp.UrgentTargetSelection;

//FewerAbove = 0;
//FewerAbove_LowerTier = 1;
//FewerAbove_LargerSum = 2;

public class GaintStepHeuristic
{

	public static void probing(State state, int probingevaluation) throws Exception
	{
		// FewerAbove = 0;
		// FewerAbove_LowerTier = 1;
		// FewerAbove_LowerTier_LargerSum = 2;
		state.tryFixings();

		Layout inst = state.layout;
		// no retrieval can be done
		while (!inst.isAllFixed())
		{
			Pair<Container, Integer> pair = null;
			switch (probingevaluation)
			{
			case Constant.FewerAbove:
				pair = UrgentTargetSelection.fewerAbove(inst);
				break;
			case Constant.FewerAbove_LowerTier:
				pair = UrgentTargetSelection.fewerAbove_lowerTier(inst);
				break;
			case Constant.FewerAbove_LowerTier_LargerSum:
				pair = UrgentTargetSelection.fewerAbove_lowerTier_largerSum(inst);
				break;
			case Constant.FewerAbove_LargerSum:
				pair = UrgentTargetSelection.fewerAbove_largerSum(inst);
				break;
			}
			

			fixOneGaintMove(state, pair);

		}

		state.updateBest();

	}

	public static void fixOneGaintMove(State state, Pair<Container, Integer> pair) throws Exception
	{
		Container ut = pair.getL();
		int settleStack = pair.getR();
		int utIndex = ut.uniqueContainerIndex;
		int utStack = state.layout.atStack[utIndex];

		if (utStack == settleStack)
			fixSameOneGaintMove(state, ut);
		else
			fixDiffOneGaintMove(state, ut, settleStack);

		state.tryFixings();
	}

	public static boolean inside(int a, int[] as)
	{
		if (as == null)
			return false;
		for (int v : as)
			if (a == v)
				return true;
		return false;
	}

	public static int selectDestination(Layout layout, Container cp, int[] forbid)
	{
		// return stack index;
		/*
		 * type
		 * 
		 * 1 clean, support cp, min {top group} found 2 soiled, maxSoiled<=cp,
		 * max{maxSoild} found 3 soiled, maxSoild>cp and maxSoild \neq
		 * nextGroup, min {maxSoild} found 4 clean, cannot support cp, max{top
		 * group} found 5 soiled, maxSoild>cp and maxSoild=nextGroup,
		 */

		int type = 6;// smaller better
		int flag = Constant.FLOOR_PRIORITYLABEL;// smaller better
		int stack = -1;

		int cpin = layout.atStack[cp.uniqueContainerIndex];
		for (int s = 1; s <= layout.S; s++)
		{
			int t;
			int f;
			if (layout.stackHeight[s] == layout.T || cpin == s || inside(s, forbid))
				continue;
			if (layout.isClean(s))
			{
				if (layout.supportCapacity(s) >= cp.groupLabel)
				{
					t = 1;
					f = layout.supportCapacity(s);
				}
				else
				{
					t = 4;
					f = -layout.supportCapacity(s);
				}
			}
			else
			{
				if (layout.maxSoiled(s) <= cp.groupLabel)
				{
					t = 2;
					f = -layout.maxSoiled(s);
				}
				else if (layout.maxSoiled(s) != layout.nextGroup)
				{
					t = 3;
					f = layout.maxSoiled(s);
				}
				else
				{
					t = 5;
					f = layout.nextGroup;
				}
			}

			if (type > t)
			{
				type = t;
				flag = f;
				stack = s;
			}
			else if (type == t && flag > f)
			{
				flag = f;
				stack = s;
			}

		}
		return stack;

	}

	public static int selectTemporary(Layout layout, Container ut, int[] forbid)
	{
		// select a non-full stack with the highest height, prefer a dirty
		// stack;
		int utStack = layout.atStack[ut.uniqueContainerIndex];
		int ret = -1;// selected stack
		int hei = -1;// selected height
		int isSoiled = 0;
		for (int s = 1; s <= layout.S; s++)
		{
			if (layout.stackHeight[s] == layout.T || utStack == s || inside(s, forbid))
				continue;
			int soiled = layout.isClean(s) ? 0 : 1;
			if (ret == -1 || hei < layout.stackHeight[s] || hei == layout.stackHeight[s] && isSoiled < soiled)
			{
				ret = s;
				hei = layout.stackHeight[s];
				isSoiled = soiled;
			}

		}
		return ret;
	}

	public static boolean fulfill(State state, Operation op, int[] forbid) throws Exception
	{
		Layout layout = state.layout;

		int from = op.from;
		int to = op.to;
		Container c = op.container;
		if (!(layout.isClean(to) && layout.supportCapacity(to) >= c.groupLabel))
			return false;

		int I = -1;
		for (int s = 1; s <= layout.S; s++)
		{
			if (s == from || s == to || layout.stackHeight[s] == 0 || inside(s, forbid))
				continue;
			Container ctop = layout.topContainer(s);
			if (!(layout.isClean(ctop) == false && ctop.groupLabel > c.groupLabel && layout.supportCapacity(to) >= ctop.groupLabel))
				continue;

			if (I == -1 || layout.topContainer(I).groupLabel < ctop.groupLabel)
				I = s;
		}
		if (I == -1)
			return false;

		Operation ful = new Operation(layout.topContainer(I), I, to);
		state.goOneStep(ful);

		return true;
		// wangning just do one relocation for the fulfillment
		// however the Operation next to be determined doesnot change
		// i think the fulfillment can repeat for op.destination until it's full
	}

	public static void relocateObstructor(State state, Container cp, int[] reloforbid, int[] fulforbid) throws Exception
	{
		Layout layout = state.layout;
		int s;
		Operation after;
		do
		{
			s = selectDestination(layout, cp, reloforbid);
			after = new Operation(cp, layout.atStack[cp.uniqueContainerIndex], s);

		} while (fulfill(state, after, fulforbid));

		state.goOneStep(after);
	}

	public static void fixSameOneGaintMove(State state, Container ut) throws Exception
	{

		Layout layout = state.layout;
		int utIndex = ut.uniqueContainerIndex;
		int utStack = layout.atStack[utIndex];

		while (layout.topContainer(utStack).equals(ut) == false)
		{
			Container cp = layout.topContainer(utStack);
			relocateObstructor(state, cp, null, null);
		}
		int ts = selectTemporary(layout, ut, null);
		int es = layout.numberOfEmptySlotsWithout(new int[] { ts, utStack });
		int belowUT = layout.numberOfUnfixed(utStack) - 1;
		if (belowUT <= es)
		{
			Operation tempStore = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore);

			while (layout.stackHeight[utStack] > layout.fixedHeight[utStack])
			{
				Container cp = layout.topContainer(utStack);
				relocateObstructor(state, cp, new int[] { ts }, new int[] { ts });
			}
			Operation move = new Operation(ut, ts, utStack);
			state.goOneStep(move);

			Operation fix = new Operation(ut, utStack, 0);
			state.goOneStep(fix);
		}
		else if (es > 0)
		{
			Operation tempStore1 = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore1);

			while (layout.numberOfEmptySlotsWithout(new int[] { ts, utStack }) > 1)
			{
				Container cp = layout.topContainer(utStack);
				relocateObstructor(state, cp, new int[] { ts }, new int[] { ts });
			}
			int ts2 = selectTemporary(layout, ut, new int[] { ts });
			Operation tempStore2 = new Operation(ut, ts, ts2);
			state.goOneStep(tempStore2);

			while (layout.stackHeight[utStack] > layout.fixedHeight[utStack])
			{
				Container cp = layout.topContainer(utStack);
				relocateObstructor(state, cp, new int[] { ts2 }, new int[] { ts2 });
			}
			Operation move = new Operation(ut, ts2, utStack);
			state.goOneStep(move);

			Operation fix = new Operation(ut, utStack, 0);
			state.goOneStep(fix);
		}
		else
		{
			int ts2 = selectTemporary2(layout, ut, ts, null);
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

				relocateObstructor(state, cp, new int[] { ts2 }, new int[] { ts2 });// jinbo
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
		//		if (layout.isClean(c2) && ts!=layout.dummyStack)
		//			throw new Exception("c2 is already clean!!");

				while (layout.topContainer(ts).equals(c2) == false)
				{
					relocateObstructor(state, layout.topContainer(ts), new int[] { ts2 }, new int[] { ts2 });
				}

				Operation back = new Operation(c2, ts, ts2);
				state.goOneStep(back);

				Operation fixback = new Operation(c2, ts2, 0);
				state.goOneStep(fixback);
			}

		}

	}

	public static int selectTemporary2(Layout layout, Container ut, int ts, int[] forbid)
	{
		// Select a top container of a full stack
		int utStack = layout.atStack[ut.uniqueContainerIndex];
		if (layout.isClean(ts))
		{
			// if stack 'ts' is clean
			// then we prefer to find 'ts2' the top of which can be clean if it
			// goes to ts
			// 1. Dirty -> Clean, the greater the better;
			// 2. Clean -> Clean, the greater the better;
			// 3. Dirty -> Dirty, the smaller the better;
			// 4. Clean -> Dirty, the smaller the better;
			int ret = -1;
			int type = -1;

			for (int s = 1; s <= layout.S; s++)
			{
				if (s == utStack || ts == s || inside(s, forbid))
					continue;
				int t;
				Container c = layout.topContainer(s);
				if (layout.isClean(s))
				{
					if (c.groupLabel <= layout.supportCapacity(ts))
						t = 2;
					else
						t = 4;
				}
				else
				{
					if (c.groupLabel <= layout.supportCapacity(ts))
						t = 1;
					else
						t = 3;
				}

				if (ret == -1)
				{
					ret = s;
					type = t;
				}
				else
				{
					if (type > t)
					{
						ret = s;
						type = t;
					}
					else if (type == t)
					{
						if (t == 1 || t == 2)
						{
							if (c.groupLabel > layout.topContainer(ret).groupLabel)
								ret = s;
						}
						else
						{
							if (c.groupLabel < layout.topContainer(ret).groupLabel)
								ret = s;
						}
					}

				}
			}
			return ret;
		}
		else
		{
			// ts is soiled
			// 1. ts2 is dirty; the smaller the better;
			// 2. ts2 is clean, the smaller the better;
			int ret = -1;
			for (int s = 1; s <= layout.S; s++)
			{
				if (s == utStack || ts == s || inside(s, forbid))
					continue;

				if (ret == -1)
					ret = s;
				else
				{
					if (layout.isClean(ret) == false)
					{

						if (layout.isClean(s) == false && layout.topContainer(s).groupLabel < layout.topContainer(ret).groupLabel)// jinbo
						{
							ret = s;
						}
					}
					else
					{
						// ret clean
						if (layout.isClean(s) == false)
							ret = s;
						else
						{
							// ret clean, s clean
							if (layout.topContainer(s).groupLabel < layout.topContainer(ret).groupLabel)// jinbo
								ret = s;
						}
					}
				}

			}
			return ret;
		}
	}

	public static void fixDiffOneGaintMove(State state, Container ut, int settleStack) throws Exception
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
			while (k1 != 0 || k2 != 0)
			{
				if (k1 != 0 && k2 != 0)
				{
					Container c1 = layout.topContainer(utStack);
					Container c2 = layout.topContainer(settleStack);

					if (c1.groupLabel >= c2.groupLabel)
					{
						k1--;
						relocateObstructor(state, c1, new int[] { settleStack }, new int[] { settleStack });
					}
					else
					{
						k2--;
						relocateObstructor(state, c2, new int[] { utStack }, new int[] { utStack });
					}
				}
				else if (k1 != 0)
				{
					k1--;
					Container c1 = layout.topContainer(utStack);
					relocateObstructor(state, c1, new int[] { settleStack }, new int[] { settleStack });
				}
				else
				{
					k2--;
					Container c2 = layout.topContainer(settleStack);
					relocateObstructor(state, c2, new int[] { utStack }, new int[] { utStack });
				}
			}
			Operation move = new Operation(ut, utStack, settleStack);
			state.goOneStep(move);

			Operation fix = new Operation(ut, settleStack, 0);
			state.goOneStep(fix);
		}
		else if (num1 <= es - 1)
		{
			int k1 = num1;
			int k2 = es - 1 - k1;

			while (k1 != 0 || k2 != 0)
			{
				if (k1 != 0 && k2 != 0)
				{
					Container c1 = layout.topContainer(utStack);
					Container c2 = layout.topContainer(settleStack);

					if (c1.groupLabel >= c2.groupLabel)
					{
						k1--;
						relocateObstructor(state, c1, new int[] { settleStack }, new int[] { settleStack });
					}
					else
					{
						k2--;
						relocateObstructor(state, c2, new int[] { utStack }, new int[] { utStack });
					}
				}
				else if (k1 != 0)
				{
					k1--;
					Container c1 = layout.topContainer(utStack);
					relocateObstructor(state, c1, new int[] { settleStack }, new int[] { settleStack });
				}
				else
				{
					k2--;
					Container c2 = layout.topContainer(settleStack);
					relocateObstructor(state, c2, new int[] { utStack }, new int[] { utStack });
				}
			}

			int ts = selectTemporary(layout, ut, new int[] { settleStack });
			Operation tempStore = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore);

			while (layout.stackHeight[settleStack] > layout.fixedHeight[settleStack])
			{
				Container c2 = layout.topContainer(settleStack);
				relocateObstructor(state, c2, new int[] { ts }, new int[] { ts });
			}
			Operation move = new Operation(ut, ts, settleStack);
			state.goOneStep(move);

			Operation fix = new Operation(ut, settleStack, 0);
			state.goOneStep(fix);
		}
		else if (es - 1 >= 0)
		{
			while (layout.numberOfEmptySlotsWithout(new int[] { utStack, settleStack }) > 1)
			{
				Container c1 = layout.topContainer(utStack);
				relocateObstructor(state, c1, new int[] { settleStack }, new int[] { settleStack });

			}
			while (layout.numberOfAbove(ut) > 0)
			{
				Container c1 = layout.topContainer(utStack);
				Operation move = new Operation(c1, utStack, settleStack);
				state.goOneStep(move);
			}
			int ts = selectTemporary(layout, ut, new int[] { settleStack });
			Operation tempStore = new Operation(ut, utStack, ts);
			state.goOneStep(tempStore);
			while (layout.numberOfUnfixed(settleStack) > 0)
			{
				Container c2 = layout.topContainer(settleStack);
				relocateObstructor(state, c2, new int[] { ts }, new int[] { ts });
			}
			Operation move = new Operation(ut, ts, settleStack);
			state.goOneStep(move);

			Operation fix = new Operation(ut, settleStack, 0);
			state.goOneStep(fix);

		}
		else
		{
			int ts2 = selectTemporary2(layout, ut, settleStack, null);
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
				relocateObstructor(state, cp, new int[] { ts2 }, new int[] { ts2 });
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
				
				if (layout.isClean(v) &&vnow!=layout.dummyStack)
					throw new Exception("v is already clean!!");

				

				while (layout.topContainer(vnow).equals(v) == false)
				{
					relocateObstructor(state, layout.topContainer(vnow), new int[] { ts2 }, new int[] { ts2 });
				}
				Operation back = new Operation(v, vnow, ts2);
				state.goOneStep(back);

				Operation fixback = new Operation(v, ts2, 0);
				state.goOneStep(fixback);
			}

		}

	}
}
