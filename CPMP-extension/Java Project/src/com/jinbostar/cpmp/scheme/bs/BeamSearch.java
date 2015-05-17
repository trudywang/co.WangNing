package com.jinbostar.cpmp.scheme.bs;

import com.jinbostar.cpmp.common.Tuple;
import com.jinbostar.cpmp.scheme.fbh.Node;

import java.util.*;

public class BeamSearch
{

	public int upperBound;
	public int iteration;
	public int bestFountAtIteration;

	public int solve(Node init, int width) throws CloneNotSupportedException
	{

		iteration=0;

		upperBound = Integer.MAX_VALUE;
		bestFountAtIteration=0;
		evaluate(init);


		if (init.lowerBound() == upperBound)
			return upperBound;


		ArrayList<Node> beam = new ArrayList<Node>();

		beam.add(init);

		while (beam.size() > 0)
		{
			iteration++;
			int save=upperBound;
			beam = nextLevel(beam, width);
			if(upperBound<save)
				bestFountAtIteration=iteration;
		}
		return upperBound;
	}


	public class Stack implements Comparable<Stack>
	{
		public Node node;
		public int index;

		public Stack(Node n, int i)
		{
			index = i;
			node = n;
		}

		public int height()
		{
			return node.h[index];
		}

		public int get(int t)
		{
			return node.p(index, t);
		}

		@Override
		public int compareTo(Stack o)
		{
			int height = node.h[index];
			if (height != o.height())
				return Integer.compare(height, o.height());
			for (int k = 1; k <= height; k++)
				if (get(k) != o.get(k))
					return Integer.compare(get(k), o.get(k));

			return 0;
		}
	}

	public int hash(Node node)
	{
		StringBuilder sb = new StringBuilder();
		Stack[] stacks = new Stack[node.S];
		for (int i = 0; i < node.S; i++)
			stacks[i] = new Stack(node, i + 1);
		Arrays.sort(stacks);

		for (int i = 0; i < node.S; i++)
		{
			int s = stacks[i].index;

			sb.append("[ ");
			for (int t = 1; t <= node.h[s]; t++)
				sb.append(node.p(s, t) + " ");
			sb.append("]");

		}
		return sb.toString().hashCode();
	}

	public static Random rd = new Random();

	public class Successor implements Comparable<Successor>
	{
		public Node node;

		public int hash;


		public Successor(Node t)
		{
			node = t;
			hash = hash(node);
		}

		@Override
		public int compareTo(Successor o)
		{
			return Integer.compare(hash, o.hash);
		}
	}

	public class Candidate implements Comparable<Candidate>
	{
		public Node node;

		public int pen;


		public Candidate(Node t, int p)
		{
			node = t;

			pen = p;
		}

		@Override
		public int compareTo(Candidate o)
		{


			return Integer.compare(pen, o.pen);

		}
	}


	public int[] receiver(Node node, int s1)
	{

		int[] best = null;


		int[] sec = null;


		int p = node.p(s1, node.h[s1]);
		for (int s2 = 1, empty = 0; s2 <= node.S; s2++)
		{
			if (s1 == s2 || node.h[s2] == node.H)
				continue;
			if (node.h[s2] == 0)
			{
				if (empty == 1)
					continue;
				else
					empty = 1;
			}

			int[] temp = new int[3];
			if (node.becomeNiceByMove(s1, s2))
			{
				temp[0] = 1;
				//	temp[1] = q(s, h[s]) - p;
				temp[1] = node.demandAffect(p, node.q(s2, node.h[s2]));
			} else
			{
				int m = node.messiness(s2);
				if (!node.isNiceStack(s2) && p >= m)
				{
					temp[0] = 2;
					temp[1] = p - m;
				} else if (!node.isNiceStack(s2) && p < m)
				{
					temp[0] = 3;
					temp[1] = m - p;
				} else
				{
					temp[0] = 4;
					temp[1] = node.q(s2, node.h[s2]);
				}
			}
			temp[2] = s2;

			if (best == null || Tuple.compare(best, temp) > 0)
			{
				sec = best;
				best = temp;
			} else if (sec == null || Tuple.compare(sec, temp) > 0)
			{
				sec = temp;
			}
		}

		if (sec != null)
			return new int[]{best[2], sec[2]};
		else if (best != null)
			return new int[]{best[2]};
		else
			return new int[]{};
	}

	public ArrayList<Node> nextLevel(ArrayList<Node> nodes, int width) throws CloneNotSupportedException
	{
		TreeSet<Successor> list = new TreeSet<Successor>();

		for (Node node : nodes)
		{
			if (node.lowerBound() + node.realizedMoves >= upperBound)
				continue;


			for (int s1 = 1; s1 <= node.S; s1++)
			{
				if (node.h[s1] == 0)
					continue;

				int[] rec = receiver(node, s1);

				for (int s2 : rec)
				{
					Node dummy = node.clone();
					dummy.move(s1, s2);
					if (dummy.lowerBound() + dummy.realizedMoves >= upperBound)
						continue;
					list.add(new Successor(dummy));
				}
			}
		}


		ArrayList<Candidate> off = new ArrayList<Candidate>();
		for (Successor sc : list)
		{
			Node node = sc.node;
			if (node.lowerBound() + node.realizedMoves >= upperBound)
				continue;
			int score = evaluate(node);
			off.add(new Candidate(node, score));
		}

		Collections.sort(off);

		ArrayList<Node> ret = new ArrayList<Node>();
		for (int i = 0; i < off.size() && ret.size() < width; i++)
		{
			Node node = off.get(i).node;
			if (node.lowerBound() + node.realizedMoves >= upperBound)
				continue;

			ret.add(node);
		}
		return ret;
	}

	public int evaluate(Node x) throws CloneNotSupportedException
	{
		Node c = x.clone();
		c.solve();
		int sol = c.realizedMoves;
		if (sol < upperBound)
			upperBound = sol;
		return sol;
	}


}
