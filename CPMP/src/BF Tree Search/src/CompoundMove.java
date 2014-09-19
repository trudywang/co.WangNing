import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by jinbo on 8/30/14.
 */
public class CompoundMove
{
	public ArrayList<Move> moves = new ArrayList<Move>();

	public int clean_supply;
	public static Comparator<CompoundMove> normal_comparator = new Comparator<CompoundMove>()
	{
		public int compare(CompoundMove a, CompoundMove b)
		{
			if (a.size() != b.size())
				return b.size() - a.size();
			else
				return b.clean_supply - a.clean_supply;
		}
	};
	public static Comparator<CompoundMove> extra_comparator = new Comparator<CompoundMove>()
	{
		public int compare(CompoundMove a, CompoundMove b)
		{
			if (a.size() != b.size())
				return a.size() - b.size();
			else
				return b.clean_supply - a.clean_supply;
		}
	};

	public void append(Move m)
	{
		moves.add(m);
	}

	public int size()
	{
		return moves.size();
	}

	public CompoundMove copy()
	{
		CompoundMove ret = new CompoundMove();
		for (Move m : moves)
			ret.append(m);
		ret.clean_supply = clean_supply;
		return ret;
	}
}
