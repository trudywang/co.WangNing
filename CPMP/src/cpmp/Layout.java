package cpmp;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

public class Layout implements Cloneable
{
	public int S;
	public int T;
	public int N;
	public int G;

	public int[] groupOf;
	public boolean[] fixed;

	public Container[][] bay;
	public int[] stackHeight;
	public int[] cleanHeight;
	public int[] fixedHeight;
	public int[][] maxSoiledUnderInclusive;

	public int[] atStack;
	public int[] atTier;

	public ArrayList<TreeSet<Container>> containerListOfGroup;// unfixed
																// container of
																// each group
	public int nextGroup;

	public int remainUnfixed;
	public int remainSoild;

	public int dummyStack;

	public Layout copy() throws CloneNotSupportedException
	{

		Layout inst = null;

		inst = (Layout) super.clone();

		inst.bay = new Container[S + 1][T + 1];
		inst.maxSoiledUnderInclusive = new int[S + 1][T + 1];
		for (int i = 1; i <= S; i++)
		{
			inst.bay[i] = bay[i].clone();
			inst.maxSoiledUnderInclusive[i] = maxSoiledUnderInclusive[i].clone();
		}
		inst.fixed = fixed.clone();
		inst.stackHeight = stackHeight.clone();
		inst.cleanHeight = cleanHeight.clone();
		inst.fixedHeight = fixedHeight.clone();
		inst.atStack = atStack.clone();
		inst.atTier = atTier.clone();
		inst.containerListOfGroup = new ArrayList<TreeSet<Container>>();
		for (int g = 0; g <= G; g++)
		{
			TreeSet<Container> ts = new TreeSet<Container>();
			ts.addAll(containerListOfGroup.get(g));
			inst.containerListOfGroup.add(ts);
		}
		inst.dummyStack = dummyStack;
		return inst;
	}

	public Layout()
	{

	}

	public Layout(Scanner scn)
	{
		String[] str;
		scn.nextLine();
		str = scn.nextLine().split(" ");
		S = Integer.parseInt(str[2]);
		str = scn.nextLine().split(" ");
		dummyStack = Integer.parseInt(str[2]);
		str = scn.nextLine().split(" ");
		T = Integer.parseInt(str[2]);
		str = scn.nextLine().split(" ");
		N = Integer.parseInt(str[2]);

		groupOf = new int[N + 1];
		bay = new Container[S + 1][T + 1];
		stackHeight = new int[S + 1];
		cleanHeight = new int[S + 1];
		fixedHeight = new int[S + 1];
		fixed = new boolean[N + 1];
		maxSoiledUnderInclusive = new int[S + 1][T + 1];
		atStack = new int[N + 1];
		atTier = new int[N + 1];
		remainUnfixed = N;
		remainSoild = 0;
		G = 0;
		int n = 0;
		for (int s = 1; s <= S; s++)
		{
			str = scn.nextLine().split(" ");
			stackHeight[s] = str.length - 3;

			bay[s][0] = Constant.FLOOR;

			maxSoiledUnderInclusive[s][0] = 0;

			for (int i = 3; i < str.length; i++)
			{
				int t = i - 2;
				n++;
				int g = Integer.parseInt(str[i]);
				if (G < g)
					G = g;
				Container block = new Container(g, n);
				groupOf[n] = g;
				bay[s][t] = block;

				boolean clean = (isClean(s, t - 1) && bay[s][t - 1].groupLabel >= block.groupLabel);
				if (clean)
				{
					maxSoiledUnderInclusive[s][t] = 0;
					cleanHeight[s] = t;
				}
				else
				{
					remainSoild++;
					maxSoiledUnderInclusive[s][t] = Math.max(maxSoiledUnderInclusive[s][t - 1], block.groupLabel);
				}
				atStack[n] = s;
				atTier[n] = t;
				fixed[n] = false;
			}
		}
		// assert(n==N);

		containerListOfGroup = new ArrayList<TreeSet<Container>>();
		for (int g = 0; g <= G; g++)
			containerListOfGroup.add(new TreeSet<Container>());
		for (int s = 1; s <= S; s++)
		{
			for (int t = 1; t <= stackHeight[s]; t++)
			{
				int g = bay[s][t].groupLabel;

				containerListOfGroup.get(g).add(bay[s][t]);
			}
		}
		nextGroup = G;
		while (nextGroup >= 1 && containerListOfGroup.get(nextGroup).isEmpty())
			nextGroup--;
	}

	public void printBay()
	{

		for (int t = T; t > 0; t--)
		{
			System.err.printf("%2d|", t);
			for (int s = 1; s <= S; s++)
			{
				if (bay[s][t] != null)
				{
					System.err.printf(" %2d", bay[s][t].groupLabel);
				}
				else
				{
					System.err.printf("   ");
				}
			}
			System.err.printf("\n");
		}

		System.err.printf("--+");
		for (int s = 1; s <= S; s++)
		{
			System.err.printf("---");
		}
		System.err.printf("\n");

		System.err.printf("  |");
		for (int s = 1; s <= S; s++)
		{
			System.err.printf(" %2d", s);
		}
		System.err.printf("\n");
	}

	public String toString()
	{
		String str = "";
		for (int t = T; t > 0; t--)
		{
			str += String.format("%2d|", t);
			for (int s = 1; s <= S; s++)
			{
				if (bay[s][t] != null)

				{
					if (isFixed(s, t))
						str += "  -";
					else
						str += String.format(" %2d", bay[s][t].groupLabel);
				}
				else
				{
					str += String.format("   ");
				}
			}
			str += String.format("\n");
		}

		str += String.format("--+");
		for (int s = 1; s <= S; s++)
		{
			str += String.format("---");
		}
		str += String.format("\n");

		str += String.format("  |");
		for (int s = 1; s <= S; s++)
		{
			str += String.format(" %2d", s);
		}
		str += String.format("\n");
		return str;
	}

	public void printMaxSoiled()
	{

		for (int t = T; t > 0; t--)
		{
			System.err.printf("%d|", t);
			for (int s = 1; s <= S; s++)
			{
				if (bay[s][t] != null)
				{
					System.err.printf(" %2d", maxSoiledUnderInclusive[s][t]);
				}
				else
				{
					System.err.printf("   ");
				}
			}
			System.err.printf("\n");
		}

		System.err.printf("-+");
		for (int s = 1; s <= S; s++)
		{
			System.err.printf("---");
		}
		System.err.printf("\n");

		System.err.printf(" |");
		for (int s = 1; s <= S; s++)
		{
			System.err.printf(" %2d", s);
		}
		System.err.printf("\n");
	}

	public boolean isAllFixed()
	{
		return remainUnfixed == 0;
	}

	public void doMove(Operation move) throws Exception
	{
		Container block = move.container;
		int g = block.groupLabel;
		int n = block.uniqueContainerIndex;

		if (move.isFixing())
		{
			if (this.isClean(block) == false)
				throw new Exception("Container fixed is not clean");
			if (fixed[n])
				throw new Exception("Container fixed is already fixed");
			fixed[n] = true;
			fixedHeight[move.from]++;
			remainUnfixed--;
			containerListOfGroup.get(g).remove(block);
			while (nextGroup >= 1 && containerListOfGroup.get(nextGroup).isEmpty())
				nextGroup--;

		}

		else if (move.isUnfixing())
		{
			if (fixed[n] == false)
				throw new Exception("Container can not be unfixed");
			fixed[n] = false;
			remainUnfixed++;
			fixedHeight[-move.from]--;
			containerListOfGroup.get(g).add(block);
			nextGroup = Math.max(g, nextGroup);
		}
		else
		{

			if (this.topContainer(move.from).equals(block) == false)
				throw new Exception("Container moved is not at the top of Stack from");

			if (this.isFixed(topContainer(move.from)))
				throw new Exception("Hey, it's fixed");

			if (isClean(move.container))
			{
				cleanHeight[move.from]--;
			}
			else
			{
				remainSoild--;
			}

			int s = move.from;
			int t = stackHeight[s]--;

			maxSoiledUnderInclusive[s][t] = 0;
			bay[s][t] = null;

			int s2 = move.to;
			int t2 = ++stackHeight[s2];
			bay[s2][t2] = block;

			boolean clean = (isClean(s2, t2 - 1) && bay[s2][t2 - 1].groupLabel >= block.groupLabel);
			if (clean)
			{
				maxSoiledUnderInclusive[s2][t2] = 0;
				cleanHeight[s2] = t2;
			}
			else
			{
				remainSoild++;
				maxSoiledUnderInclusive[s2][t2] = Math.max(maxSoiledUnderInclusive[s2][t2 - 1], block.groupLabel);
			}

			atStack[n] = s2;
			atTier[n] = t2;
		}

	}

	public void undoMove(Operation move) throws Exception
	{

		if (move.isFixing() || move.isUnfixing())
		{
			Operation opposite = new Operation(move.container, -move.from, 0);
			this.doMove(opposite);
		}
		else
		{
			Operation opposite = new Operation(move.container, move.to, move.from);
			doMove(opposite);
		}

	}

	public Container getContainer(int n)
	{
		int s = atStack[n];
		int t = atTier[n];
		return bay[s][t];
	}

	public boolean isClean(int s, int t)
	{
		if (s == dummyStack)
			return false;
		else
			return maxSoiledUnderInclusive[s][t] == 0;
	}

	public boolean isClean(Container c)
	{
		int s = atStack[c.uniqueContainerIndex];
		int t = atTier[c.uniqueContainerIndex];
		return isClean(s, t);
	}

	public boolean isClean(int s)
	{
		int t = stackHeight[s];
		return isClean(s, t);
	}

	public Container topContainer(int s)
	{
		if (stackHeight[s] == 0)
			return null;

		return bay[s][stackHeight[s]];
	}

	public int numberOfUnfixed(int s)
	{
		return stackHeight[s] - fixedHeight[s];
	}

	public int numberOfAbove(Container c)
	{
		int s = atStack[c.uniqueContainerIndex];
		int t = atTier[c.uniqueContainerIndex];
		return numberOfAbove(s, t);
	}

	public int numberOfAbove(int s, int t)
	{
		return stackHeight[s] - t;
	}

	public int numberOfEmptySlots(int s)
	{
		return T - stackHeight[s];
	}

	public int numberOfEmptySlotsWithout(int[] forbid)
	{
		int num = S * T - N;
		for (int s : forbid)
			num -= numberOfEmptySlots(s);
		return num;
	}

	public int supportCapacity(int s)
	{
		if (isClean(s))
		{
			int t = stackHeight[s];
			return bay[s][t].groupLabel;
		}
		else
		{
			return 0;
		}
	}

	public int maxSoiled(int s)
	{
		int t = stackHeight[s];
		return maxSoiledUnderInclusive[s][t];
	}

	public int numberOfSoiled(int s)
	{
		return stackHeight[s] - cleanHeight[s];
	}

	public boolean isFixed(Container c)
	{
		int s = atStack[c.uniqueContainerIndex];
		int t = atTier[c.uniqueContainerIndex];
		return isFixed(s, t);
	}

	public boolean isFixed(int s, int t)
	{
		return t <= fixedHeight[s];
	}

}
