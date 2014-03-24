package cpmp;

import java.util.ArrayList;

public class Solution
{

	public ArrayList<Operation> operations;
	public int relocationCount;
	public int fixingandunfixingCount;

	public Solution()
	{
		operations = new ArrayList<Operation>();
		relocationCount = 0;
		fixingandunfixingCount = 0;
	}

	public void append(Operation move)
	{
		operations.add(move);
		if (move.isRelocation())
			relocationCount++;
		else
			fixingandunfixingCount++;
	}

	public Operation getLastOperation()
	{
		return operations.get(operations.size() - 1);
	}

	public Operation removeLastOperation()
	{
		if (operations.get(operations.size() - 1).isRelocation())
			relocationCount--;
		else
			fixingandunfixingCount--;
		return operations.remove(operations.size() - 1);
	}

	public Solution copy()
	{
		Solution c = new Solution();
		for (Operation move : operations)
			c.append(move);
		c.relocationCount = relocationCount;
		c.fixingandunfixingCount = fixingandunfixingCount;
		return c;
	}

	public String toString()
	{
		String r = "";
		for (int i = 0; i < operations.size(); i++)
		{
			if (i != 0)
				r += " ";
			r += "[" + operations.get(i) + "]";
		}
		return r;
	}

}
