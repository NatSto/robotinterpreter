package robotinterpreter.variables.conditional;

import robotinterpreter.Code;
import robotinterpreter.RobotInterpreter;
import robotinterpreter.terminals.Terminals;
import robotinterpreter.variables.BODY;
import robotinterpreter.variables.Variable;

public class CONDITIONLIST extends Variable 
{
	//Can be either a CONDITION or a CONDITIONLIST
	private Object con;
	private String conType;
	private String logOp;
	private CONDITIONLIST nextCon;
	
	private final String CONDITIONLIST = "CONDITIONLIST";
	private final String CONDITION = "CONDITION";
	
	public CONDITIONLIST(BODY b, Code c, String s)
	{
		body = b;
		code = s;
		lineNum = c.currentLineNum();
		
		String[] tokens = Code.tokenize(code);
		
		if(tokens[0] != Terminals.OPENBRACKET)
			RobotInterpreter.halt("CONDITIONLIST", lineNum, code, "CONDITIONLIST must begin with [");
		
		int closeBracket = findCloseBracket(tokens);
		if(closeBracket == -1)
			RobotInterpreter.halt("CONDITIONLIST", lineNum, code, "Missing ]! CONDITIONLIST must have matching brackets!");

		boolean hasLogOp = false;
		for(int x = 0; x < closeBracket; x++)
		{
			if(tokens[x].equals(Terminals.AND) || tokens[x].equals(Terminals.OR))
				hasLogOp = true;
		}
		
		//If hasLogOp is true, then c is a CONDITIONLIST.
		//If false, c is a CONDITION
		//If there is a token following tokens[closeBracket], it must be a logop, and there is a sequential CONDITIONLIST
		if(hasLogOp)
		{
			conType = CONDITIONLIST;
			con = new CONDITIONLIST(body, c, Code.implode(tokens, " ", 1, closeBracket - 1));
		}
		else
		{
			conType = CONDITION;
			con = new CONDITION(body, c, Code.implode(tokens, " ", 1, closeBracket - 1));

		}
		
		
		if(tokens.length - 1 > closeBracket)
		{
			if(Terminals.logOps.contains(tokens[closeBracket + 1]))
			{
				logOp = tokens[closeBracket + 1];
				if(tokens.length - 1 > closeBracket + 1)
				{
					nextCon = new CONDITIONLIST(body, c, Code.implode(tokens, " ", closeBracket + 2, tokens.length - 1));
				}
				else RobotInterpreter.halt("CONDITIONLIST", lineNum, code, "A CONDITION or CONDITIONLIST must follow an AND or OR");
			}
			else RobotInterpreter.halt("CONDITIONLIST", lineNum, code, "Only AND or OR may follow a CONDITIONLIST");

		}
	}
	
	private int findCloseBracket(String[] tokens)
	{
		int openBrackets = 0;
		int tokenNum = 0;
		for(String token : tokens)
		{
			if(token == Terminals.OPENBRACKET)
				openBrackets++;
			else if(token == Terminals.CLOSEBRACKET)
				openBrackets--;
			
			if(openBrackets == 0)
			{
				return tokenNum;
			}
			else tokenNum++;
		}
		return -1;
	}
	
	public void print() 
	{
		if(conType.equals(CONDITIONLIST))
		{		
			RobotInterpreter.write("parse", "[");
			((CONDITIONLIST)con).print();
			RobotInterpreter.write("parse", "]");
		}
		else if(conType.equals(CONDITION))
		{
			((CONDITION)con).print();

		}
		
		if(logOp != null)
		{
			RobotInterpreter.write("parse", " " + logOp + " ");
			nextCon.print();
		}
	}

	//Validate the con and the nextCon, if it exists
	public void validate() 
	{ 
		RobotInterpreter.writeln("validate", "Validating CONDITIONLIST");

		if(conType.equals(CONDITIONLIST))
		{		
			((CONDITIONLIST)con).validate();
		}
		else if(conType.equals(CONDITION))
		{
			((CONDITION)con).validate();
		}
		
		if(nextCon != null)
		{
			nextCon.validate();
		}
	}

	@Override
	public Object execute(Object args[]) 
	{
		boolean go = false;
		if(conType.equals(CONDITIONLIST))
		{		
			go = (boolean) ((CONDITIONLIST)con).execute(null);
		}
		else if(conType.equals(CONDITION))
		{
			go = (boolean) ((CONDITION)con).execute(null);
		}
		
		if(nextCon != null)
		{
			//Not sure about this variable name, it may be harmful.
			boolean go2 = false;
			go2 = (boolean) ((CONDITIONLIST)nextCon).execute(null);
			
			if(logOp.equals(Terminals.AND))
				return go && go2;
			else if(logOp.equals(Terminals.OR))
				return go || go2;
		}
		return go;
	}
}
