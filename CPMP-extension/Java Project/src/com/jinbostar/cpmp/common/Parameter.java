package com.jinbostar.cpmp.common;

public class Parameter
{

	public static enum TopFilter
	{
		TrickyAvoid, FullyFixedAvoid, NoTopFilter, ExtremeAvoid
	}

	public static enum TaskPreference
	{
		MoveIdeal,
		BlockingAboveTarget,
		BlockingInAimStack,
		MoveActual,
		DemandAffected,
		Gap,
		RevisedGap,
		SmallerPriority,
		LargerPriority,
		Capability,
		RevisedCapability,
		BlockingNice,
		BlockingNiceInAimStack,
		BlockingNiceInCurrentStack,
		InternalPrefer,
		MoveOverall,
		LeftmostAimStack,
		RightmostAimStack,
		LeftmostCurrentStack,
		RightmostCurrentStack,
		HigherCurrentTier,
		LowerCurrentTier,
		LowerAimTier,
		HigherAimTier,
	}

	public static enum DualSenderOrder
	{
		SmallerEvalFirstOrder,
		LargerPriorityFirstOrder,
		SmallerPriorityFirstOrder
	}

}
