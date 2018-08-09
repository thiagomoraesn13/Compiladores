import java.lang.Exception;
import java.util.LinkedList;
import java.util.Hashtable;

class Frame{

  public Frame next;
  public Hashtable<String, ASTExp> simbols;

  public Frame(){
    this(null);
  }

  public Frame(Frame next){
    this.next = next;
    this.simbols = new Hashtable<String, ASTExp>();
  }
}

class ASTExp{

  public String value;
  public LinkedList<ASTExp> list;
  public LinkedList<String> paramNames;

  public ASTExp(String value){
    this.value = value;
    this.list = new LinkedList<ASTExp> ();
  }

  public void addLast(ASTExp e){
    this.list.addLast(e);
  }

  public String toString(){
    return this.value;
  }
}

class Interpreter{

  public Frame global = new Frame();

  public ASTExp eval(ASTExp exp, Frame local) throws Exception{

    String a, b;
    Double ra, rb;

    if(exp.list.size() == 0){
      try{
        Double.parseDouble(exp.value);
      }catch(Exception e){
        exp = local.simbols.get(exp.value);
      }

      return exp;
    }

    a = eval(exp.list.get(0), local).value;
    b = eval(exp.list.get(1), local).value;
    ra = Double.parseDouble(a);
    rb = Double.parseDouble(b);

    switch(exp.value){
      case "+":
        ra += rb;
        break;
      case "-":
        ra -= rb;
        break;
      case "*":
        ra *=rb;
        break;
      case "/":
        ra /=rb;
        break;
      case "%":
        ra %=rb;
        break;
      case ">":
        if(ra>rb){
          return new ASTExp(String.valueOf(true));
        }
        else{return new ASTExp(String.valueOf(false));}
      case "<":
        if(ra<rb){
          return new ASTExp(String.valueOf(true));
        }
        else{return new ASTExp(String.valueOf(false));}
      case "==":
        if(ra.equals(rb)){
          return new ASTExp(String.valueOf(true));
        }
        else{return new ASTExp(String.valueOf(false));}
      case ">=":
        if(ra>=rb){
          return new ASTExp(String.valueOf(true));
        }
        else{return new ASTExp(String.valueOf(false));}
      case "<=":
        if(ra<=rb){
          return new ASTExp(String.valueOf(true));
        }
        else{return new ASTExp(String.valueOf(false));}
      case "!=":
        if(ra!=rb){
          return new ASTExp(String.valueOf(true));
        }
        else{return new ASTExp(String.valueOf(false));}
      default:
        return exp;
    }
    return new ASTExp(ra.toString());
  }
}
