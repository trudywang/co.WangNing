import java.util.ArrayList;

/**
 * Created by jinbo on 8/30/14.
 */
public class Solution
{
	public ArrayList<Move> cms = new ArrayList<Move>();

	public int size()
	{
		return cms.size();
	}

	public Solution copy()
	{
		Solution s = new Solution();
		for (Move m : cms)
			s.cms.add(m);
		return s;
	}

	public void perform(CompoundMove cm)
	{
		for (Move m : cm.moves)
			cms.add(m);
	}

	public void perform(Move m)
	{
		cms.add(m);
	}

	public Solution perform_new(CompoundMove cm)
	{
		Solution s = copy();
		s.perform(cm);
		return s;
	}

	public void undo(CompoundMove cm)
	{
		for (int i = cm.size() - 1; i >= 0; i--)
		{
			cms.remove(cms.size() - 1);
		}
	}
}
