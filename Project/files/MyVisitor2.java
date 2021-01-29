/*
Παναγιώτης Τσουβελεκάκης p3130212
Φώτης Τσουβελεκάκης p3130213
Μιχαήλ Βαζαίος p3170013
*/

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

import javax.lang.model.util.ElementScanner6;

import minipython.node.*;
import minipython.analysis.DepthFirstAdapter;

public class MyVisitor2 extends DepthFirstAdapter
{
    private Hashtable symtable;
    private String key = "";
    private String currentFuction = "";
    private Stack<String> operationStack;
    private Stack<String> functionCallStack = new Stack<>();
    private Hashtable<String, Boolean> allCheckReturnStatements = new Hashtable<>();
    private String returnStatementFunctionCallName = "";
    private int line = 0;
    private int currentFunctionLine, currentFunctionColumn = 0;



    MyVisitor2(Hashtable symtable)
    {
        this.symtable = symtable;
    }


    public void inAAssignStatement(AAssignStatement node)
	{	
		TId vTid = ((AIdentifierExpression)node.getIdExp()).getId();
		String vName = vTid.toString().trim();
		if (currentFuction == "")
		{
			key = vName + "GVar";
		}
		else
		{
			key = vName + "LVar";
        }
        if (!symtable.containsKey(key))
		{
            symtable.put(key, "Unknown");
		}
    }

    public void outAAssignStatement(AAssignStatement node)
	{
        if (symtable.containsKey(key))
		{
            String type = getExpressionType(node.getExp());
            if(symtable.get(key).equals("Unknown"))
            {
                symtable.put(key, getExpressionType(node.getExp()));
            }
            else if(!symtable.get(key).equals(type))
            {
                if(!symtable.get(key).equals("Error") && !type.equals("Error") && !type.equals("Unknown"))
                    print("In line " + ((AIdentifierExpression)node.getIdExp()).getId().getLine() + " : " + "Can't assign " + type + " to a variable of type " + (String)symtable.get(key));
            }     
        }
    }

    public void outAMinusAssignStatement(AMinusAssignStatement node)
    {
        PExpression left = node.getIdExp();
        PExpression right = node.getExp();
        getType(left, right, null, node, false);
    }

    public void outADivAssignStatement(ADivAssignStatement node)
    {
        PExpression left = node.getIdExp();
        PExpression right = node.getExp();
        getType(left, right, null, node, false);
    }

    public void inAIdentifierExpression(AIdentifierExpression node)
    {
        if(!(allCheckReturnStatements.get(returnStatementFunctionCallName) != null))
        {
            boolean identifierInAFunctionPar = false;
            String vName = node.getId().toString().trim();
            if (currentFuction == "")
            {
                if(node.parent() instanceof AFunctionCallExpression)
                {
                    if(!symtable.containsKey(vName + "GVar") && !(node == ((AFunctionCallExpression)node.parent()).getIdExp()))
                    {
                        print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                    }
                }
                else
                {
                    if(!symtable.containsKey(vName + "GVar"))
                    {
                        print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                    }
                }
            }
            else
            {
                if(!(node.parent() instanceof ADefFunction) && !(node.parent() instanceof AFirstArgument) && !(node.parent() instanceof AAfterFirstArgNextArgs))
                {
                    Function function = ((Hashtable<String, Function>)symtable.get(currentFuction)).get(currentFuction + currentFunctionLine + currentFunctionColumn);
                    for(Hashtable<String, Object> arg : function.getArgsInfo())
                    {
                        if(arg.get("argName").equals(vName))
                        {
                            identifierInAFunctionPar = true;
                        }
                    }
                    if(!identifierInAFunctionPar)
                    {
                        if(node.parent() instanceof AFunctionCallExpression)
                        {
                            if(!symtable.containsKey(vName + "GVar") && !symtable.containsKey(vName + "LVar") && !(node == ((AFunctionCallExpression)node.parent()).getIdExp()))
                            {
                                print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                            }
                        }
                        else
                        {
                            if(!symtable.containsKey(vName + "GVar") && !symtable.containsKey(vName + "LVar"))
                            {
                                print(String.format("[%d,%d] : Name %s is not defined", node.getId().getLine(), node.getId().getPos(), vName));
                            }
                        }
                    }
                }
            }
        }
    }

    public String getExpressionType(PExpression node)
    {
        return ckeckInstanceOf(node);
    }

    public void outAAdditionExpression(AAdditionExpression node)
    {
        PExpression left = node.getLeftExp();
        PExpression right = node.getRightExp();
        getType(left, right, node, null, true);
        
    }

    public void outAMultiplicationExpression(AMultiplicationExpression node)
    {
        PExpression left = node.getLeftExp();
        PExpression right = node.getRightExp();
        getType(left, right, node, null, true);
    }

    public void outADivisionExpression(ADivisionExpression node)
    {
        PExpression left = node.getLeftExp();
        PExpression right = node.getRightExp();
        getType(left, right, node, null, true);
    }

    public void outAModuloExpression(AModuloExpression node)
    {
        PExpression left = node.getLeftExp();
        PExpression right = node.getRightExp();
        getType(left, right, node, null, true);
    }

    public void outAPowerExpression(APowerExpression node)
    {
        PExpression left = node.getLeftExp();
        PExpression right = node.getRightExp();
        getType(left, right, node, null, true);
    }

    public void getType(PExpression left, PExpression right, PExpression node, PStatement stateNode, boolean storeResult)
    {
        if(operationStack == null){operationStack = new Stack<>();}
        String rightType = ckeckInstanceOf(right);
        String leftType = ckeckInstanceOf(left);

        if(leftType != "Unknown" && rightType != "Unknown" && leftType != "Error" && rightType != "Error")
        {
            if (!(node instanceof AMultiplicationExpression) || leftType == "None" || rightType == "None")
            {
                if(leftType != rightType)
                {
                    if(node instanceof AAdditionExpression)
                        print(String.format("In line %d you can't add type %s with type %s",line, leftType, rightType));
                    if(node instanceof ASubtractionExpression)
                        print(String.format("In line %d you can't sub type %s with type %s",line, leftType, rightType));
                    if(node instanceof AMultiplicationExpression)
                        print(String.format("In line %d you can't mult type %s with type %s",line, leftType, rightType));
                    if(node instanceof ADivisionExpression)
                        print(String.format("In line %d you can't div type %s with type %s",line, leftType, rightType));
                    if(node instanceof AModuloExpression)
                        print(String.format("In line %d you can't mod type %s with type %s",line, leftType, rightType));
                    if(node instanceof APowerExpression)
                        print(String.format("In line %d you can't power type %s with type %s",line, leftType, rightType));
                    if(stateNode != null && stateNode instanceof ADivAssignStatement)
                        print(String.format("In line %d you can't div type %s with type %s",line, leftType, rightType));
                    if(stateNode != null && stateNode instanceof ADivAssignStatement)
                        print(String.format("In line %d you can't div type %s with type %s",line, leftType, rightType));
                    leftType = "Error";
                }
            }
        }
        if(storeResult)
        {
            if(leftType == "Unknown" || rightType == "Unknown")
            {
                operationStack.add("Unknown");
            }
            else if(leftType == "Error" || rightType == "Error")
            {
                operationStack.add("Error");
            }
            else
            {
                operationStack.add(leftType);
            }
        }
        //print(operationStack);
    }

    public String ckeckInstanceOf(PExpression x)
    {
        String xType = "";
        if(x instanceof AIdentifierExpression)
        {
            boolean findInPar = false;
            String currentFunctionCallName = "";
            if(currentFuction != "")
            {
                currentFunctionCallName = currentFuction;
            }
            if((allCheckReturnStatements.get(returnStatementFunctionCallName) != null))
            {
                currentFunctionCallName = returnStatementFunctionCallName;
            }
            if(symtable.containsKey(currentFunctionCallName))
            {
                Function function;
                String functionName;
                if(currentFuction == "")
                {
                    functionName = (String)((Hashtable<String, Object>)symtable.get(currentFunctionCallName)).get("functionName");
                    int callPos = currentFunctionCallName.lastIndexOf("Call");
                    String temp = currentFunctionCallName.substring(0, callPos);
                    function = ((Hashtable<String, Function>)symtable.get(temp)).get(functionName);
                }
                else
                {
                    if((allCheckReturnStatements.get(returnStatementFunctionCallName) != null))
                    {
                        functionName = (String)((Hashtable<String, Object>)symtable.get(currentFunctionCallName)).get("functionName");
                        int callPos = currentFunctionCallName.lastIndexOf("Call");
                        String temp = currentFunctionCallName.substring(0, callPos);
                        function = ((Hashtable<String, Function>)symtable.get(temp)).get(functionName);
                    }
                    else{
                        function = ((Hashtable<String, Function>)symtable.get(currentFunctionCallName)).get(currentFunctionCallName + currentFunctionLine + currentFunctionColumn);
                    }
                }
                for(int i = 0; i < function.getArgsInfo().size(); i++)
                {
                    if(function.getArgsInfo().get(i).get("argName").equals(((AIdentifierExpression)x).getId().toString().trim()))
                    {
                        xType = (String)function.getArgsInfo().get(i).get("type");
                        findInPar = true;
                    }
                }
            }
            if(symtable.containsKey(x.toString().trim() + "GVar") && !findInPar)
            {
                xType = (String)symtable.get(x.toString().trim() + "GVar");
                line = ((AIdentifierExpression)x).getId().getLine();
            }
            else if(!symtable.containsKey(x.toString().trim() + "GVar") && !findInPar)
            {
                xType = "Error";
            }
        }
        else if(x instanceof AMaxsExpression)
        {
            xType = getExpressionType(((AMaxsExpression)x).getValue());
        }
        else if(x instanceof AMinsExpression)
        {
            xType = getExpressionType(((AMinsExpression)x).getValue());
        }
        else if(x instanceof AArrayExpression)
        {
            xType = getExpressionType((PExpression)(((AArrayExpression)x).getExpression().get(0)));
        }
        else if(x instanceof ANumExpression)
        {
            xType = "INTEGER_LITERAL";
            line = ((ANumExpression)x).getNumber().getLine();
        }
        else if(x instanceof ANoneExpression)
        {
            xType = "None";
            line = ((ANoneExpression)x).getNone().getLine();
        }
        else if(x instanceof AStringExpression)
        {
            xType = "STRING_LITERAL";
            line = ((AStringExpression)x).getString().getLine();
        }
        else if(x instanceof ATypesExpression)
        {
            xType = "Type";
        }
        else if(x instanceof AFunctionCallExpression)
        {
            line = ((AIdentifierExpression)((AFunctionCallExpression)x).getIdExp()).getId().getLine();
            int col = ((AIdentifierExpression)((AFunctionCallExpression)x).getIdExp()).getId().getPos();
            if(symtable.containsKey(((AIdentifierExpression)((AFunctionCallExpression)x).getIdExp()).getId().toString().trim() + "Call" + line + col))
            {
                xType = (String)((Hashtable<String, Object>)symtable.get(((AIdentifierExpression)((AFunctionCallExpression)x).getIdExp()).getId().toString().trim() + "Call" + line + col)).get("returnType");
            }
            else
            {
                xType = "Unknown";
            }
        }
        else
        {
            if(operationStack.size() > 0)
            {
                xType = operationStack.pop();
            }
            else
            {
                xType = "";
            }
        }
        return xType;
    }

    public void inADefFunction(ADefFunction node)
	{
        String fName = ((AIdentifierExpression)node.getExpression()).getId().toString().trim();
        currentFuction = fName;
        currentFunctionLine = ((AIdentifierExpression)node.getExpression()).getId().getLine();
        currentFunctionColumn = ((AIdentifierExpression)node.getExpression()).getId().getPos();
    }

    public void outADefFunction(ADefFunction node)
	{
        currentFuction = "";
    }
    
    public void inAFunctionCallExpression(AFunctionCallExpression node)
	{
        boolean findFunction = false;
		LinkedList args = node.getArglistExps();
		TId fCallTId = ((AIdentifierExpression)node.getIdExp()).getId();
        String fCallname = fCallTId.toString().trim();
        functionCallStack.add(fCallname + "Call" + fCallTId.getLine() + fCallTId.getPos());
		if(symtable.containsKey(fCallname))
		{
			for(Function function : ((Hashtable<String, Function>)symtable.get(fCallname)).values())
			{
				if(args.size() <= function.getArgs() && args.size() >= (function.getArgs() - function.getDefaultArgs()))
				{
					Hashtable<String, Object> fCallData = new Hashtable<>();
					fCallData.put("functionName", function.getName());
                    fCallData.put("returnType", "Unknown");
                    fCallData.put("line", fCallTId.getLine());
					fCallData.put("pos", fCallTId.getPos());
					symtable.put(fCallname + "Call" + fCallTId.getLine() + fCallTId.getPos(), fCallData);
                    findFunction = true;
				}
			}
			if(!findFunction)
			{
                if(!(allCheckReturnStatements.get(returnStatementFunctionCallName) != null))
				    print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is defined with different number of arguments");
			}
		}
		else
		{
            if(!(allCheckReturnStatements.get(returnStatementFunctionCallName) != null))
			    print("[" + fCallTId.getLine() + "," + fCallTId.getPos() + "]" + ": " +" Function " + fCallname +" is not defined");
        }
    }
    
    public void outAFunctionCallExpression(AFunctionCallExpression node)
    {
        int count = 0;
        TId fCallTId = ((AIdentifierExpression)node.getIdExp()).getId();
        String callName = fCallTId.toString().trim();
        Function function = null;
        if(symtable.containsKey(callName) && symtable.containsKey(functionCallStack.get(functionCallStack.size()-1)))
        {
            function = ((Hashtable<String, Function>)symtable.get(callName)).get(((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1))).get("functionName"));
        }
        if(function != null)
        {
            int args = function.getArgs();
            int defaultArgs = function.getDefaultArgs();
            for(PExpression arg : ((LinkedList<PExpression>)node.getArglistExps()))
            {
                if(count < (args - defaultArgs))
                    function.getArgsInfo().get(count).put("type", getExpressionType(arg));
                if(count >= (args - defaultArgs))
                {
                    print("In line " + fCallTId.getLine() + " : Pass a value of type " + getExpressionType(arg) + " to an argument of type " + function.getArgsInfo().get(count).get("type"));
                }
                count++;
            }
            if(function.getReturnStatement() != null)
            {
                if(isAnOperationReturnExp(function))
                {
                    allCheckReturnStatements.put(callName + "Call" + fCallTId.getLine() + fCallTId.getPos(), true);
                    returnStatementFunctionCallName = callName + "Call" + fCallTId.getLine() + fCallTId.getPos();;
                    ckeckAReturnExpOperation(function);
                    allCheckReturnStatements.remove(callName + "Call" + fCallTId.getLine() + fCallTId.getPos());
                    ((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1))).put("returnType", getExpressionType(function.getReturnStatement().getExpression()));
                    
                }
                else
                {
                    allCheckReturnStatements.put(callName + "Call" + fCallTId.getLine() + fCallTId.getPos(), true);
                    returnStatementFunctionCallName = callName + "Call" + fCallTId.getLine() + fCallTId.getPos();
                    ((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1))).put("returnType", getExpressionType(function.getReturnStatement().getExpression()));
                    allCheckReturnStatements.remove(callName + "Call" + fCallTId.getLine() + fCallTId.getPos());
                }
            }
            else
            {
                ((Hashtable<String, Object>)symtable.get(functionCallStack.get(functionCallStack.size()-1))).put("returnType", "None");
            }
        }
        functionCallStack.pop();
        if(functionCallStack.size() > 0)
            returnStatementFunctionCallName = functionCallStack.get(functionCallStack.size()-1);
    }

    public boolean isAnOperationReturnExp(Function function)
    {
        if(function.getReturnStatement().getExpression() instanceof AAdditionExpression)
            return true;
        if(function.getReturnStatement().getExpression() instanceof AMultiplicationExpression)
            return true;
        if(function.getReturnStatement().getExpression() instanceof ADivisionExpression)
            return true;
        if(function.getReturnStatement().getExpression() instanceof AModuloExpression)
            return true;
        if(function.getReturnStatement().getExpression() instanceof APowerExpression)
            return true;
        return false;
    }

    public void ckeckAReturnExpOperation(Function function)
    {
        if(function.getReturnStatement().getExpression() instanceof AAdditionExpression)
            caseAAdditionExpression((AAdditionExpression)function.getReturnStatement().getExpression());
        if(function.getReturnStatement().getExpression() instanceof AMultiplicationExpression)
            caseAMultiplicationExpression((AMultiplicationExpression)function.getReturnStatement().getExpression());
        if(function.getReturnStatement().getExpression() instanceof ADivisionExpression)
            caseADivisionExpression((ADivisionExpression)function.getReturnStatement().getExpression());
        if(function.getReturnStatement().getExpression() instanceof AModuloExpression)
            caseAModuloExpression((AModuloExpression)function.getReturnStatement().getExpression());
        if(function.getReturnStatement().getExpression() instanceof APowerExpression)
            caseAPowerExpression((APowerExpression)function.getReturnStatement().getExpression());
    }
    
    public void print(Object objectToPrint)
	{
		System.out.println(objectToPrint);
	}
}