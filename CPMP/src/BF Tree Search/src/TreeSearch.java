import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by jinbo on 8/30/14.
 */
public class TreeSearch
{
	public Solution s_star;
	public int lower_bound;
	public int nSucc = 5;
	public double p_ub = 1.75;
	public int threshold;
	public long time_limit;
	public long runtime = 60L * 1000L;

	public Solution solve(Layout L)
	{
		time_limit = System.currentTimeMillis() + runtime;

		lower_bound = L.lower_bound();
		threshold = (int) (0.5 + p_ub * lower_bound);
		s_star = null;
		perform_compound_moves(new Solution(), L);
		return s_star;
	}

	public void perform_compound_moves(Solution s, Layout L)
	{
		if (s_star != null && s_star.size() == lower_bound || System.currentTimeMillis() >= time_limit)
		{
			return;
		}

		if (L.number_of_badly_placed_containers() == 0)
		{
			if (s_star == null || s.size() < s_star.size())
				s_star = s.copy();
			return;
		}
		Move forbid=s.lastMove();
		ArrayList<CompoundMove> Cm;
		if (L.exist_BG(forbid))
		{
			Cm = determine_normal_compound_moves(s, L);
		}
		else
		{
			Cm = determine_extra_compound_moves(s, L);
		}
		for (CompoundMove cm : Cm)
		{
			s.perform(cm);
			L.perform(cm);

			perform_compound_moves(s, L);

			s.undo(cm);
			L.undo(cm);
		}
	}

	public ArrayList<CompoundMove> determine_normal_compound_moves(Solution s_c, Layout L)
	{
		ArrayList<CompoundMove> Cm = new ArrayList<CompoundMove>();

		for (int s = 1; s <= L.S; s++)
		{
			CompoundMove cm = new CompoundMove();

			Move forbid=s_c.lastMove();
			while (L.exist_BG(s,forbid))
			{
				Move m_best = L.get_best_BG(s,forbid);
				cm.append(m_best);
				L.perform(m_best);
				forbid=m_best;
			}
			int lb = s_c.size() + cm.size() + L.lower_bound();
			if (cm.size() != 0 && (s_star == null && lb < threshold || s_star != null && lb < s_star.size()))
			{
				cm.clean_supply = L.weighted_clean_supply();
				Cm.add(cm);
			}
			L.undo(cm);
		}
		Collections.sort(Cm, CompoundMove.normal_comparator);
		ArrayList<CompoundMove> ret = new ArrayList<CompoundMove>();
		for (int i = 0; i < nSucc && i < Cm.size(); i++)
			ret.add(Cm.get(i));
		return ret;
	}

	public ArrayList<CompoundMove> determine_extra_compound_moves(Solution s_c, Layout L)
	{
		ArrayList<CompoundMove> Cm = new ArrayList<CompoundMove>();

		for (int s = 1; s <= L.S; s++)
		{
			CompoundMove cm = new CompoundMove();
			Move forbid=s_c.lastMove();
			while (true)
			{
				if (L.exist_non_BG(s,forbid))
				{
					Move m_best = L.get_best_non_BG(s,forbid);

					cm.append(m_best);
					L.perform(m_best);
					forbid=m_best;

					int lb = s_c.size() + cm.size() + L.lower_bound();
					if (s_star == null && lb < threshold || s_star != null && lb < s_star.size())
					{
						if (L.weighted_clean_supply() >= 1)
						{
							CompoundMove mm = cm.copy();
							mm.clean_supply = L.weighted_clean_supply();
						//	if(mm.clean_supply>0)
							Cm.add(mm);
						}
						continue;
					}
				}

				break;
			}
			L.undo(cm);
		}
		Collections.sort(Cm, CompoundMove.extra_comparator);

		int max_clean_supply_index = -1;
		for (int i = 0; i < Cm.size(); i++)
		{
			if (max_clean_supply_index == -1 || Cm.get(max_clean_supply_index).clean_supply < Cm.get(i).clean_supply)
				max_clean_supply_index = i;
		}

		if (Cm.size() > nSucc && max_clean_supply_index >= nSucc)
		{
			Cm.set(nSucc - 1, Cm.get(max_clean_supply_index));
		}

		ArrayList<CompoundMove> ret = new ArrayList<CompoundMove>();
		for (int i = 0; i < nSucc && i < Cm.size(); i++)
			ret.add(Cm.get(i));
		return ret;
	}
}
