package robotinterpreter.variables.methods.external;

import robotinterpreter.RobotListener;
import robotinterpreter.RobotInterpreter;
import robotinterpreter.terminals.Terminals;

public class drive extends ExtMethod 
{
	
	public drive()
	{
		id = "drive";
		type = Terminals.VOID;
		paramTypes = new String[1];
		paramTypes[0] = Terminals.STRING;
	}
	
	public Object execute(Object[] args) 
	{
		for(RobotListener l : RobotInterpreter.getRobotListeners())
		{
			if(args[0].equals("f"))
			{
				l.driveForward();
			}
			else if(args[0].equals("b"))
			{
				l.driveBackwards();
			}
		}
		return null;
	}
}