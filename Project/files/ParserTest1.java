import java.io.*;
import java.util.Hashtable;

import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.Start;

public class ParserTest1
{
  public static void main(String[] args)
  {
    try
    {
      Parser parser =
        new Parser(
        new Lexer(
        new PushbackReader(
        new FileReader(args[0].toString()), 1024)));

      Hashtable symtable = new Hashtable<>();
      Start ast = parser.parse();
      MyVisitor first = new MyVisitor(symtable);
      ast.apply(first);
      if(!first.getErrorExist())
        ast.apply(new MyVisitor2(symtable));
      System.out.println("Symbol table has: " + symtable);
    }
    catch (Exception e)
    {
      System.err.println(e);
    }
  } 
}

