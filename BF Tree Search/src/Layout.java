import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by jinbo on 8/30/14.
 */
public class Layout
{
	public int S, H, N, G;
	public int[][] bay_data;
	public int[] height;
	public int[] good_count;

	public int number_of_badly_placed_containers;
	public int clean_supply;

	public Layout(int S, int H, int N, int G, int[][] init)
	{
		this.S = S;
		this.H = H;
		this.N = N;
		this.G = G;
		height = new int[S + 1];
		good_count = new int[S + 1];
		bay_data = new int[S + 1][H + 1];
		number_of_badly_placed_containers = 0;
		clean_supply = 0;
		for (int i = 1; i <= S; i++)
		{
			for (int j = 1; j <= H && init[i][j] != 0; j++)
			{
				bay_data[i][j] = init[i][j];
				height[i] = j;
			}

			if (height[i] <= 1)
				good_count[i] = height[i];
			else
			{
				good_count[i] = 1;
				for (int j = 2; j <= height[i]; j++)
				{
					if (bay_data[i][j] <= bay_data[i][j - 1])
						good_count[i] = j;
					else
						break;
				}
			}
			number_of_badly_placed_containers += height[i] - good_count[i];
			if (height[i] == good_count[i])
				clean_supply += H - height[i];
		}
	}

	public int bay(int i, int j)
	{
		assert i >= 1 && i <= S && j >= 1 && j <= height[i];
		return bay_data[i][j];
	}

	public void set_bay(int i, int j, int c)
	{
		assert i >= 1 && i <= S && j >= 1 && j <= H;
		bay_data[i][j] = c;
	}

	public int clean_supply()
	{
		return clean_supply;
	}

	public int number_of_badly_placed_containers()
	{
		return number_of_badly_placed_containers;
	}


	public boolean exist_BG()
	{
		for (int s = 1; s <= S; s++)
			if (exist_BG(s))
				return true;
		return false;
	}

	public boolean exist_BG(int s1)
	{
		if (height[s1] == 0)
			return false;
		if (height[s1] == good_count[s1])
			return false;
		int c = bay(s1, height[s1]);
		for (int s2 = 1; s2 <= S; s2++)
		{
			if (s2 == s1 || height[s2] == H || height[s2] != good_count[s2])
				continue;
			int capa = height[s2] == 0 ? G : bay(s2, height[s2]);
			if (capa >= c)
				return true;
		}
		return false;
	}

	public Move get_best_BG(int s1)
	{
		assert height[s1] != good_count[s1];
		assert height[s1] > 0;
		int c = bay(s1, height[s1]);

		int min = Integer.MAX_VALUE;
		int hei = 0;
		int ret = -1;
		for (int s2 = 1; s2 <= S; s2++)
		{
			if (s2 == s1 || height[s2] == H || height[s2] != good_count[s2])
				continue;
			int capa = height[s2] == 0 ? G : bay(s2, height[s2]);
			if (capa >= c)
			{
				if (ret == -1 || min > capa || min == capa && hei < height[s2])
				{
					ret = s2;
					min = capa;
					hei = height[s2];
				}
			}
		}

		return new Move(s1, ret);
	}

	public boolean exist_non_BG(int s1)
	{
		if (height[s1] == 0)
			return false;
		if (height[s1] == good_count[s1])
			return true;//Good

		// bad
		int c = bay(s1, height[s1]);
		for (int s2 = 1; s2 <= S; s2++)
		{
			if (s2 == s1 || height[s2] == H)
				continue;

			if (height[s2] != good_count[s2])
			{
				// to be bad=true;
				return true;
			}
			else
			{
				int capa = (height[s2] == 0 ? G : bay(s2, height[s2]));
				if (capa < c)
				{
					//to be bad
					return true;
				}
			}
		}
		return false;
	}
	public boolean exist_GG(int s1)
	{
		if (height[s1] == 0)
			return false;
		boolean good = height[s1] == good_count[s1];
		if(good ==false)
			return false;
		int c=bay(s1,height[s1]);
		for(int s2=1;s2<=S;s2++)
		{
			if(s2==s1||height[s2]==H || height[s2]!=good_count[s2])
				continue;
			int capa = (height[s2] == 0 ? G : bay(s2, height[s2]));
			if(capa>=c)
				return true;
		}
		return false;
	}
	public Move get_best_GG(int s1)
	{
		int c=bay(s1,height[s1]);
		int ret=-1;
		int minCapa=Integer.MAX_VALUE;
		int hei=0;
		for(int s2=1;s2<=S;s2++)
		{
			if(s2==s1||height[s2]==H || height[s2]!=good_count[s2])
				continue;
			int capa = (height[s2] == 0 ? G : bay(s2, height[s2]));
			if(capa>=c)
			{
				if(ret==-1 || minCapa>capa || (minCapa==capa && hei<height[s2]))
				{
					ret=s2;
					minCapa=capa;
					hei=height[s2];
				}
			}
		}
		return new Move(s1,ret);
	}
	public Move get_best_XB(int s1)
	{
		int c=bay(s1,height[s1]);
		int ret=-1;
		int delta=0;
		int hei=0;
		for(int s2=1;s2<=S;s2++)
		{
			if(s2==s1||height[s2]==H )
				continue;

			if(height[s2]!=good_count[s2] || bay(s2, height[s2])<c)
			{
				// to be bad
				int t_delta=bay(s2, height[s2])-c;
				if(ret==-1 || t_delta <= 0 && delta >0)
				{
					ret=s2;
					delta=t_delta;
					hei=height[s2];
				}
				else if(t_delta <=0 && delta<=0 && t_delta>delta || t_delta> 0 && delta>0 && t_delta<delta)
				{
					ret=s2;
					delta=t_delta;
					hei=height[s2];
				}
				else if(delta==t_delta && hei<height[s2])
				{
					ret=s2;
					delta=t_delta;
					hei=height[s2];
				}
			}
		}
		return new Move(s1,ret);
	}
	public Move get_best_non_BG(int s1)
	{
		if(exist_GG(s1))
			return get_best_GG(s1);
		else
			return get_best_XB(s1);
	}

	public void perform(CompoundMove cm)
	{
		for (Move m : cm.moves)
			perform(m);
	}

	public void undo(CompoundMove cm)
	{
		for (int i = cm.size() - 1; i >= 0; i--)
		{
			Move m = cm.moves.get(i);
			perform(m.reverse());
		}
	}

	public Layout perform_new(CompoundMove cm)
	{
		Layout ret = copy();
		for (Move m : cm.moves)
			ret.perform(m);
		return ret;
	}

	public Layout perform_new(Move m)
	{
		Layout ret = copy();
		ret.perform(m);
		return ret;
	}

	public Layout copy()
	{
		return new Layout(S, H, N, G, bay_data);
	}


	public void perform(Move m)
	{
		int s1 = m.s1;
		int s2 = m.s2;

		assert height[s1] > 0 && height[s2] < H;

		int c = bay(s1, height[s1]);
		set_bay(s1, height[s1], 0);
		if (height[s1] != good_count[s1])
		{
			number_of_badly_placed_containers--;
			height[s1]--;

			if (height[s1] == good_count[s1])
				clean_supply += H - height[s1];
		}
		else
		{
			height[s1]--;
			good_count[s1]--;
			clean_supply++;
		}

		set_bay(s2, height[s2] + 1, c);
		if (height[s2] == 0 ||
				height[s2] == good_count[s2] && c <= bay(s2, height[s2]))
		{
			height[s2]++;
			good_count[s2]++;
			clean_supply--;
		}
		else
		{
			if (height[s2] == good_count[s2])
				clean_supply -= H - height[s2];
			height[s2]++;
			number_of_badly_placed_containers++;
		}
	}


	public int lower_bound()
	{
		int min_nb = height[1] - good_count[1];
		int BX = min_nb;
		for (int s = 2; s <= S; s++)
		{
			BX += height[s] - good_count[s];
			min_nb = Math.min(min_nb, height[s] - good_count[s]);
		}
		BX += min_nb;

		int[] d = new int[G + 1];
		int[] s_p = new int[G + 1];
		int n_empty = 0;
		for (int s = 1; s <= S; s++)
		{
			for (int j = good_count[s] + 1; j <= height[s]; j++)
			{
				d[bay(s, j)]++;
			}
			if (height[s] != 0)
			{

				int g = bay(s, good_count[s]);
				s_p[g] += H - good_count[s];
			}
			else
			{
				n_empty++;
			}
		}
		int[] D = new int[G + 1];
		D[G] = d[G];
		int[] S_p = new int[G + 1];
		S_p[G] = s_p[G] + n_empty * H;

		for (int g = G - 1; g >= 1; g--)
		{
			D[g] = d[g] + D[g + 1];
			S_p[g] = s_p[g] + S_p[g + 1];
		}

		int[] D_s = new int[G + 1];
		int g_star = 0;
		for (int g = 1; g <= G; g++)
		{
			D_s[g] = D[g] - S_p[g];
			if (g_star == 0 || D_s[g] > D_s[g_star])
				g_star = g;
		}


		int n_s_GX = 0;
		if (D_s[g_star] > 0)
		{
			n_s_GX = (D_s[g_star] / H) + (D_s[g_star] % H == 0 ? 0 : 1);
		}
		int GX = 0;
		if (n_s_GX > 0)
		{
			ArrayList<Integer> arr = new ArrayList<Integer>();
			for (int s = 1; s <= S; s++)
			{
				int r = 0;
				for (int t = good_count[s]; t >= 1; t--)
				{
					if (bay(s, t) < g_star)
						r++;
					else break;
				}
				if (r != 0)
					arr.add(r);
			}
			Collections.sort(arr);

			for (int i = 0; i < n_s_GX; i++)
				GX += arr.get(i);
		}

		return BX + GX;

	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= S; i++)
			sb.append(Arrays.toString(bay_data[i]));
		return sb.toString();
	}

}
