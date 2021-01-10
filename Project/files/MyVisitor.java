import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import minipython.node.*;

import minipython.analysis.DepthFirstAdapter;

public class MyVisitor extends DepthFirstAdapter
{
	private Hashtable symtable;
	private String currentFuction = "";
	private Function fData;
	private boolean errorExist = false;

	MyVisitor(Hashtable symtable) 
	{
		this.symtable = symtable;
	}

	public void inADefFunction(ADefFunction node)
	{
		/*
			This method create fData Function object which contains all info we need about current function

			fname : String
				The name of defined function

			Used Methods:
				getExpression() : PExpression object
					PExpression object can be cast to all alternatives (AIdentifierExpression is PExpression)
				getArgument() : LinkedList with AFirstArgument object (Exist in ADefFunction class)
					Return the arguments-default values of function in a raw - Could be empty or size 1
		*/
		String fName = ((AIdentifierExpression)node.getExpression()).getId().toString().trim();
		fData = new Function();
		currentFuction = fName;
		int args = node.getArgument().size();
		fData.setArgs(args);
		fData.setDefaultArgs(0);
	}

	public void outADefFunction(ADefFunction node)
	{
		/*
			This method compare the args of current function with the args of function with the same name 
			If a function with the same name exist and has same args - print an error message else add Data of this function

			allFuctionWithSameName : String
				All functions with same name and different args have already visited
			currentFunctionArgs : int
				The number of args which check function contains
			currentFunctionDefaultArgs : int
				The number of default args which check fuction contains
			line, pos : int
				The position where function is defined
			fuctionArgs : int
				The number of args which function with same name has
			fuctionDefaultArgs : int
				The number of default args which function with same name has

			Used Methods:
				getExpression() : PExpression object
					PExpression object can be cast to all alternatives (AIdentifierExpression is PExpression)
		*/
		Hashtable<String, Function> allFuctionWithSameName;
		int line = ((TId)((AIdentifierExpression)node.getExpression()).getId()).getLine();
		int pos = ((TId)((AIdentifierExpression)node.getExpression()).getId()).getPos();
		if(!symtable.containsKey(currentFuction))
		{
			allFuctionWithSameName = new Hashtable<>();
			symtable.put(currentFuction, allFuctionWithSameName);
			fData.setName(currentFuction +  line + pos);
			allFuctionWithSameName.put(currentFuction +  line + pos, fData);
		}
		else
		{
			allFuctionWithSameName = (Hashtable<String, Function>)symtable.get(currentFuction);

			for(Function function : allFuctionWithSameName.values())
			{
				int currentFunctionArgs = fData.getArgs();
				int currentFunctionDefaultArgs = fData.getDefaultArgs();
				int fuctionArgs = function.getArgs();
				int fuctionDefaultArgs = function.getDefaultArgs();
				if((currentFunctionArgs >= (fuctionArgs - fuctionDefaultArgs) && currentFunctionArgs <= fuctionArgs)
					|| (fuctionArgs >= (currentFunctionArgs - currentFunctionDefaultArgs) && fuctionArgs <= currentFunctionArgs))
				{
					print("[" + line + "," + pos + "]" + ": " +" Function " + currentFuction +" is already defined");
					errorExist = true;
					return;
				}
			}
			fData.setName(currentFuction + line + pos);
			allFuctionWithSameName.put(currentFuction +  line + pos, fData);
		}
		if(node.getStatement() instanceof AReturnStatement)
		{
			fData.setReturnStatement((AReturnStatement)node.getStatement());
		}
		currentFuction = "";
	}

	public void inAFirstArgument(AFirstArgument node)
	{
		/*
			Check if first argument have default value - add +1 or 0 to fData's defaultValue attribute
			Check the number of children(NextArgs) - add +number to fData's args attribute

			nextArgsSize : int
				The number of all next arguments(with default and non default values)
			defaultArgs : int
				The number of first argument with default value(0 or 1)
			nextArgs : LinkedList with AAfterFirstArgNextArgs objects
				Contains all next args after first arg

			Used Methods:
				getNextArgs() : LinkedList with AAfterFirstArgNextArgs objects
					Return all next args after first arg - Could be greater or equal than 0
				getValue() : LinkedList with default values
					Return the value of an arg - Could be empty or size 1
		*/
		int defaultArgs = node.getValue().size();
		defaultArgs = defaultArgs + fData.getDefaultArgs();
		fData.setDefaultArgs(defaultArgs);
		LinkedList nextArgs = node.getNextArgs();
		int nextArgsSize = fData.getArgs() + nextArgs.size();
		fData.setArgs(nextArgsSize);
	}

	public void outAFirstArgument(AFirstArgument node)
	{
		initializeArgsType((AIdentifierExpression)node.getId(), node.getValue(), true);
	}

	public void inAAfterFirstArgNextArgs(AAfterFirstArgNextArgs node)
	{
		/*
			Check if next argument have default value - add +1 or 0 to fData's defaultValue attribute

			Variables:
				defaultValueOfNextArgs : int
					The number of next argument with default value(0 or 1)

			Used Methods:
				getValue() : LinkedList with default values
					Return the value of an arg - Could be empty or size 1
		*/
		int defaultValueOfNextArgs = node.getValue().size();
		defaultValueOfNextArgs = defaultValueOfNextArgs + fData.getDefaultArgs();
		fData.setDefaultArgs(defaultValueOfNextArgs);
	}

	public void outAAfterFirstArgNextArgs(AAfterFirstArgNextArgs node)
	{
		initializeArgsType((AIdentifierExpression)node.getId(), node.getValue(), false);
	}

	public void inAFunctionCallExpression(AFunctionCallExpression node)
	{
		/*
			Check if the function name of the function call exists
			If exists then check the number of attributes with the list of all functions with same name
			If a function with same name and same attributes doesn't exist then print an error message
			Example:
				function y(a, b = 0){} and the function call y(1)
				-args of define function are 2 and default args are 1
				-args of function call are 1
				if 1<=2 and 1 >= 2 - 1 then the function call is right.Is true

			Variables:
				findFunction : boolean
					A helper flag that show if a function with same name and same args exists
				args : LinkedList
					Contains all args from function call
				fCallTId : TId
					The identidier objct
				fCallname : String
					The name of function call

			Used Methods:
				getArglistExps() : LinkedList
					Return a list the args of function call
				getIdExp() : PExpression
					Return a PExpression which here is an AIdentifierExpression
		*/
		boolean findFunction = false;
		LinkedList args = node.getArglistExps();
		TId fCallTId = ((AIdentifierExpression)node.getIdExp()).getId();
		String fCallname = fCallTId.toString().trim();
		if(symtable.containsKey(fCallname))
		{
			for(Function function : ((Hashtable<String, Function>)symtable.get(fCallname)).values())
			{
				if(args.size() <= function.getArgs() && args.size() >= (function.getArgs() - function.getDefaultArgs()))
				{
					Hashtable<String, Object> fCallData = new Hashtable<>();
					fCallData.put("functionName", function.getName());
					fCallData.put("returnType", "");
					symtable.put(fCallname + "Call", fCallData);
					findFunction = true;
				}
			}
			if(!findFunction)
			{
				print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is defined with different number of arguments");
				errorExist = true;
			}
		}
		else
		{
			if(currentFuction != "")
			{
				return;
			}
			print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is not defined");
			errorExist = true;
		}
	}

	public void initializeArgsType(AIdentifierExpression id ,LinkedList value, boolean firstArgFlag)
	{
		/*
			This method checks if an argument has default value and finds the type of this argument
			If argument doesn't have default value -> type is Unknown
			Store the argument and the type of it
		*/
		Hashtable<String, Object> firstArg = new Hashtable<>();
		firstArg.put("argName", id.getId().toString().trim());
		if(value.size() == 1)
		{
			if(value.get(0) instanceof ANumExpression)
			{
				firstArg.put("type", "INTEGER_LITERAL");
			}
			else if(value.get(0) instanceof AStringExpression)
			{
				firstArg.put("type", "STRING_LITERAL");
			}
			else if(value.get(0) instanceof ANoneExpression)
			{
				firstArg.put("type", "None");
			}
			else if(value.get(0) instanceof AIdfunctioncallExpression)
			{
				String nameOfFuctionCall = ((AIdentifierExpression)((AFunctionCallExpression)((AIdfunctioncallExpression)value.get(0)).getFunctionCall()).getIdExp()).getId().toString().trim();
				if(symtable.get(nameOfFuctionCall + "Call") != null)
				{
					firstArg.put("type", ((Hashtable<String, Object>)symtable.get(nameOfFuctionCall + "Call")).get("returnType"));
				}
				else
				{
					firstArg.put("type", "Unknown");
				}
			}
		}
		else
		{
			firstArg.put("type", "Unknown");
		}
		if(firstArgFlag)
		{
			fData.getArgsInfo().add(0, firstArg);;
		}
		else{
			fData.getArgsInfo().add(firstArg);
		}
	}

	public void print(Object objectToPrint)
	{
		System.out.println(objectToPrint);
	}

	public boolean getErrorExist()
	{
		return this.errorExist;
	}
}