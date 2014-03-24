package algos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import cpmp.Layout;
import cpmp.LowerBound;
import cpmp.Operation;
import cpmp.Solution;

/*
 * This is for CVS 1-8
 */
public class IDAStar
{
	public static boolean dfs(Layout layout,int depth, int level,Solution s) throws Exception
	{
		boolean allclean=true;
		for(int i=1;i<=layout.S && allclean;i++)
			allclean = allclean && layout.isClean(i);
		
		if(depth==level)
		{
			return allclean;
		}
		else
		{
			if(LowerBound.WangNingMaxKnapsack(layout)+level>depth)
				return false;
			for(int i=1;i<=layout.S;i++)
			{
				if(layout.stackHeight[i]==0)
					continue;
				for(int j=1;j<=layout.S;j++)
				{
					if(i==j || layout.stackHeight[j]==layout.T)
						continue;
					Operation op=new Operation(layout.topContainer(i),i,j);
					layout.doMove(op);
					s.append(op);
					boolean down=dfs(layout,depth,level+1,s);
					
					if(down)
					{
						return true;
					}
					layout.undoMove(op);
					s.removeLastOperation();
				}
			}
			return false;
		}
	}
	public static boolean astar(Layout layout,int depth,Solution s) throws Exception
	{
		return dfs(layout,depth,0,s);
	}
	public static Solution idastar(Layout layout) throws Exception
	{
		Solution s=new Solution();
		for(int depth=0;;depth++)
		{
			
			if(astar(layout,depth,s))
				return s;
		}
		
	}

	public static void main(String[] args) throws Exception
	{
		for (int testgroup = 1; testgroup <= 8; testgroup++)
		{

			File dir = new File("data/HCVSwTRUCK/HCVS" + testgroup);
			File files[] = dir.listFiles();

			for (int testcase = 1; testcase <= files.length; testcase++)
			{

				File file = new File("data/HCVSwTRUCK/HCVS" + testgroup + "/HCVS" + testgroup + "_" + testcase + ".bay");
				Scanner scn = new Scanner(file);
				Layout layout = new Layout(scn);
				scn.close();
				
				System.out.print( testgroup + "," + testcase + "," );
				long mark=System.currentTimeMillis();
				Solution s=idastar(layout);
				System.out.print(s.relocationCount);
				System.out.print(","+(System.currentTimeMillis()-mark)/1000);
				System.out.println(","+s);
			}
		}
	}
}
