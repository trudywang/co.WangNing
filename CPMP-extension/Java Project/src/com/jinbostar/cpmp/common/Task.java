package com.jinbostar.cpmp.common;

public class Task
{
	public int target_priority;
	public int current_stack;
	public int current_tier;
	public int aim_stack;
	public int aim_tier;

	public Task(int cStar, int sPlus, int tPlus, int sMinus, int tMinus)
	{
		target_priority = cStar;
		current_stack = sPlus;
		current_tier = tPlus;
		aim_stack = sMinus;
		aim_tier = tMinus;
	}

	public boolean immediate()
	{
		return (current_stack == aim_stack) && (current_tier == aim_tier);
	}

	public boolean internal()
	{
		return (current_stack == aim_stack) && (current_tier != aim_tier);
	}

	public boolean external()
	{
		return current_stack != aim_stack;
	}

	public String toString()
	{

		return target_priority + "(" + current_stack + "," + current_tier + ")->(" + aim_stack + "," + aim_tier + ")";
	}
}
