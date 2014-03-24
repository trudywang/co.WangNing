package cpmp;

public class State
{

	public Layout layout;
	public Solution sol;
	public Solution best;

	public void undo() throws Exception
	{
		Operation move = sol.removeLastOperation();
		layout.undoMove(move);
	}

	public void goOneStep(Operation move) throws Exception
	{
		sol.append(move);
		layout.doMove(move);
	}

	public boolean isAllFixed()
	{
		return layout.isAllFixed();
	}

	public int tryFixings() throws Exception
	{
		if (isAllFixed())
			return 0;

		int count = 0;
		while (isAllFixed() == false)
		{
			Container ut = UrgentTargetSelection.fixable(layout);
			if (ut == null)
				break;
			else
			{
				int n = ut.uniqueContainerIndex;
				int s = layout.atStack[n];
				// int t = layout.atTier[n];
				Operation fix = new Operation(ut, s, 0);
				goOneStep(fix);
				count++;
			}
		}

		return count;
	}

	public boolean updateBest()
	{
		Solution rs = sol;
		if (best == null || best.relocationCount > rs.relocationCount)
		{
			best = rs.copy();
			return true;
		}
		else
			return false;

	}

	public int size()
	{
		return sol.operations.size();
	}
}
