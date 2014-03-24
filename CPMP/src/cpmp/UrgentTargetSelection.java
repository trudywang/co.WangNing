package cpmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class UrgentTargetSelection implements Comparable<UrgentTargetSelection>
{
	public Container urgentTarget;
	public int settleStack;
	public int[] scores;// smaller is better
	public UrgentTargetSelection(Container c,int s,int[] ss)
	{
		urgentTarget=c;
		settleStack=s;
		scores=ss;
	}
	@Override
	public int compareTo(UrgentTargetSelection o)
	{
		if(urgentTarget==null)
			return 1;
		if(o.urgentTarget==null)
			return -1;
		int n=scores.length;
		for(int i=0;i<n;i++)
		{
			if(scores[i]!=o.scores[i])
				return scores[i]-o.scores[i];
		}
		return 0;
	}
	public static HashMap<Integer, Container> toppestOnesOfAllStacks(Layout layout)
	{

		HashMap<Integer, Container> mark = new HashMap<Integer, Container>();
		for (Container c : layout.containerListOfGroup.get(layout.nextGroup))
		{
			int n = c.uniqueContainerIndex;
			int s = layout.atStack[n];
			int t = layout.atTier[n];

			if (mark.containsKey(s))
			{
				Container p = mark.get(s);
				if (layout.atTier[p.uniqueContainerIndex] < t)
				{
					mark.put(s, c);
				}
			}
			else
			{
				mark.put(s, c);
			}
		}
		return mark;
	}

	public static Container fixable(Layout layout)
	{
		for (Container c : layout.containerListOfGroup.get(layout.nextGroup))
		{
			int n = c.uniqueContainerIndex;
			int s = layout.atStack[n];
			int t = layout.atTier[n];
			if (s != layout.dummyStack && t == layout.fixedHeight[s] + 1)
			{
				return c;
			}
		}
		return null;
	}

	public static Pair<Container, Integer> fewerAbove(Layout layout)
	{

		HashMap<Integer, Container> mark = toppestOnesOfAllStacks(layout);

		
		UrgentTargetSelection star=new UrgentTargetSelection(null,-1,null);
		


		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (s == layout.dummyStack || layout.fixedHeight[s] == layout.T)
					continue;
				int needed = obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed});
					if(t.compareTo(star)<0)
						star=t;
				}
			}
		}
		if (star.settleStack == -1)
		{
			return null;
		}
		else
		{
			return new Pair<Container, Integer>(star.urgentTarget, star.settleStack);
		}
	}

	public static Pair<Container, Integer> fewerAbove_lowerTier(Layout layout)
	{

		HashMap<Integer, Container> mark = toppestOnesOfAllStacks(layout);

		UrgentTargetSelection star=new UrgentTargetSelection(null,-1,null);

		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (s == layout.dummyStack || layout.fixedHeight[s] == layout.T)
					continue;
				int needed = obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed,layout.fixedHeight[s]});
					if(t.compareTo(star)<0)
						star=t;
				}
			}
		}
		if (star.settleStack == -1)
		{
			return null;
		}
		else
		{
			return new Pair<Container, Integer>(star.urgentTarget, star.settleStack);
		}
	}
	public static ArrayList<Pair<Container, Integer>> fewerAbove(Layout layout,int width)
	{

		ArrayList<UrgentTargetSelection> list = new ArrayList<UrgentTargetSelection>();

		HashMap<Integer, Container> mark = UrgentTargetSelection.toppestOnesOfAllStacks(layout);

		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (layout.fixedHeight[s] == layout.T || s == layout.dummyStack)
					continue;
				int needed = UrgentTargetSelection.obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed});
					list.add(t);
				}
			}
		}
		Collections.sort(list);
	
		ArrayList<Pair<Container, Integer>> ret = new ArrayList<Pair<Container, Integer>>();

		for (int i = 0; i < width && i < list.size(); i++)
			ret.add(new Pair<Container,Integer>(list.get(i).urgentTarget,list.get(i).settleStack));
		return ret;
	}
	public static ArrayList<Pair<Container, Integer>> fewerAbove_lowerTier(Layout layout,int width)
	{

		ArrayList<UrgentTargetSelection> list = new ArrayList<UrgentTargetSelection>();

		HashMap<Integer, Container> mark = UrgentTargetSelection.toppestOnesOfAllStacks(layout);

		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (layout.fixedHeight[s] == layout.T || s == layout.dummyStack)
					continue;
				int needed = UrgentTargetSelection.obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed,layout.fixedHeight[s]});
					list.add(t);
				}
			}
		}
		Collections.sort(list);
	
		ArrayList<Pair<Container, Integer>> ret = new ArrayList<Pair<Container, Integer>>();

		for (int i = 0; i < width && i < list.size(); i++)
			ret.add(new Pair<Container,Integer>(list.get(i).urgentTarget,list.get(i).settleStack));
		return ret;
	}
	public static ArrayList<Pair<Container, Integer>> fewerAbove_lowerTier_largerSum(Layout layout,int width)
	{
		ArrayList<UrgentTargetSelection> list = new ArrayList<UrgentTargetSelection>();
		HashMap<Integer, Container> mark = toppestOnesOfAllStacks(layout);

		 
		
		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (s == layout.dummyStack || layout.fixedHeight[s] == layout.T)
					continue;
				int needed = obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					int sum = obstructorSum(layout, c, s);
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed,layout.fixedHeight[s],-sum});
					list.add(t);
				}
			}
		}
		Collections.sort(list);
		
		ArrayList<Pair<Container, Integer>> ret = new ArrayList<Pair<Container, Integer>>();

		for (int i = 0; i < width && i < list.size(); i++)
			ret.add(new Pair<Container,Integer>(list.get(i).urgentTarget,list.get(i).settleStack));
		return ret;
	}
	public static ArrayList<Pair<Container, Integer>> fewerAbove_largerSum(Layout layout,int width)
	{
		ArrayList<UrgentTargetSelection> list = new ArrayList<UrgentTargetSelection>();
		HashMap<Integer, Container> mark = toppestOnesOfAllStacks(layout);

		 
		
		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (s == layout.dummyStack || layout.fixedHeight[s] == layout.T)
					continue;
				int needed = obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					int sum = obstructorSum(layout, c, s);
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed,-sum});
					list.add(t);
				}
			}
		}
		Collections.sort(list);
		
		ArrayList<Pair<Container, Integer>> ret = new ArrayList<Pair<Container, Integer>>();

		for (int i = 0; i < width && i < list.size(); i++)
			ret.add(new Pair<Container,Integer>(list.get(i).urgentTarget,list.get(i).settleStack));
		return ret;
	}
	public static Pair<Container, Integer> fewerAbove_lowerTier_largerSum(Layout layout)
	{

		HashMap<Integer, Container> mark = toppestOnesOfAllStacks(layout);

		UrgentTargetSelection star=new UrgentTargetSelection(null,-1,null);
		
		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (s == layout.dummyStack || layout.fixedHeight[s] == layout.T)
					continue;
				int needed = obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					int sum = obstructorSum(layout, c, s);
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed,layout.fixedHeight[s],-sum});
					if(t.compareTo(star)<0)
						star=t;
				}
			}
		}
		if (star.settleStack == -1)
		{
			return null;
		}
		else
		{
			return new Pair<Container, Integer>(star.urgentTarget, star.settleStack);
		}
	}
	public static Pair<Container, Integer> fewerAbove_largerSum(Layout layout)
	{

		HashMap<Integer, Container> mark = toppestOnesOfAllStacks(layout);

		UrgentTargetSelection star=new UrgentTargetSelection(null,-1,null);
		
		for (Entry<Integer, Container> en : mark.entrySet())
		{
			Container c = en.getValue();

			for (int s = 1; s <= layout.S; s++)
			{
				if (s == layout.dummyStack || layout.fixedHeight[s] == layout.T)
					continue;
				int needed = obstructor(layout, c, s);
				if (needed != Constant.MAX_RELOCATION_NUMBER)
				{
					int sum = obstructorSum(layout, c, s);
					UrgentTargetSelection t=new UrgentTargetSelection(c,s,new int[]{needed,-sum});
					if(t.compareTo(star)<0)
						star=t;
				}
			}
		}
		if (star.settleStack == -1)
		{
			return null;
		}
		else
		{
			return new Pair<Container, Integer>(star.urgentTarget, star.settleStack);
		}
	}
	public static int obstructor(Layout layout, Container c, int s)
	{
		// Return f(c,s)
		if (s == layout.dummyStack)
			return Constant.MAX_RELOCATION_NUMBER;
		int n = c.uniqueContainerIndex;

		int needed;

		if (layout.atStack[n] == s)
		{

			int num = layout.numberOfUnfixed(s);
			int es = layout.numberOfEmptySlotsWithout(new int[] { s });
			if (num > es)
				needed = Constant.MAX_RELOCATION_NUMBER;
			else
				needed = num;
		}
		else
		{
			int num1 = layout.numberOfUnfixed(s);
			int es1 = layout.numberOfEmptySlotsWithout(new int[] { s });

			int num2 = layout.numberOfAbove(c);
			int es2 = layout.numberOfEmptySlotsWithout(new int[] { layout.atStack[n] });
			if (num1 > es1 || num2 + 1 > es2)
				needed = Constant.MAX_RELOCATION_NUMBER;
			else
				needed = num1 + num2;
		}

		return needed;
	}

	public static int obstructorSum(Layout layout, Container c, int s)
	{
		// Return f(c,s)
		if (s == layout.dummyStack)
			return Constant.MAX_RELOCATION_NUMBER;
		int n = c.uniqueContainerIndex;

		int sum = 0;

		if (layout.atStack[n] == s)
		{
			for (int j = layout.fixedHeight[s] + 1; j <= layout.stackHeight[s]; j++)
				sum += layout.bay[s][j].groupLabel;

		}
		else
		{
			for (int j = layout.fixedHeight[s] + 1; j <= layout.stackHeight[s]; j++)
				sum += layout.bay[s][j].groupLabel;
			for (int j = layout.atTier[n] + 1; j <= layout.stackHeight[layout.atStack[n]]; j++)
				sum += layout.bay[layout.atStack[n]][j].groupLabel;

		}

		return sum;
	}
	public static ArrayList<Pair<Container, Integer>> allAvailableNextSettlement(Layout layout, int width,int probingevaluation)
	{
		switch (probingevaluation)
		{
		case Constant.FewerAbove: return fewerAbove(layout, width);
		case Constant.FewerAbove_LowerTier: return fewerAbove_lowerTier(layout, width);
		case Constant.FewerAbove_LowerTier_LargerSum: return fewerAbove_lowerTier_largerSum(layout, width);
		case Constant.FewerAbove_LargerSum: return fewerAbove_largerSum(layout, width);
		}
		return null;
	}


}
