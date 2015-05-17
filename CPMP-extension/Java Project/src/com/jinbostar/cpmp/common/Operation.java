package com.jinbostar.cpmp.common;

public class Operation
{
	public Type type;
	public int s1, s2;

	public Operation(int s1, int s2, Type type)
	{
		assert (s1 == s2) == (type != Type.Move);
		this.s1 = s1;
		this.s2 = s2;
		this.type = type;
	}

	public String toString()
	{
		if (type == Type.Fix)
		{
			return "[fix " + s1 + "]";
		} else if (type == Type.Unfix)
		{
			return "[unfix " + s1 + "]";
		} else
		{
			return "[" + s1 + "->" + s2 + "]";
		}
	}

	public static enum Type
	{
		Fix, Unfix, Move
	}
}
