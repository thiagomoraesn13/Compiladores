

import java.lang.Exception;
import java.util.LinkedList;



public class Parser {
	public static final int _EOF = 0;
	public static final int _number = 1;
	public static final int _ident = 2;
	public static final int maxT = 22;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	Interpreter aci;

public void setInterpreter (Interpreter aci){
  this.aci = aci;
}



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void AdvCalc() {
		ASTExp adv = null;
		aci.global.simbols.put("pi", new ASTExp("3.1415927"));
		aci.global.simbols.put("e", new ASTExp("2.7182817"));
		
		while (StartOf(1)) {
			adv = CExp();
		}
		Expect(0);
	}

	ASTExp  CExp() {
		ASTExp  cexp;
		cexp = null;
		if (la.kind == 3) {
			Def();
		} else if (StartOf(2)) {
			cexp = Exp();
			try{
			 System.out.println(aci.eval(cexp, aci.global));
			}catch(Exception e){
			 System.out.println("Error!");
			}
			
		} else SynErr(23);
		return cexp;
	}

	void Def() {
		ASTExp expr = null;
		LinkedList<String> parametros = null;
		String nome = "";
		
		Expect(3);
		Expect(2);
		nome = t.val; 
		if (la.kind == 4) {
			Get();
			parametros = ParamNames();
			Expect(5);
		}
		Expect(6);
		expr = Exp();
		try{
		 // Se for variavel
		 if(parametros == null){
		   aci.global.simbols.put(nome, aci.eval(expr, aci.global));
		 }else{
		   // Se for uma funcao
		   expr.paramNames = parametros;
		   aci.global.simbols.put(nome, expr);
		 }
		  System.out.print(nome);
		 System.out.println(": " + aci.global.simbols.get(nome));
		}catch(Exception e){}
		
	}

	ASTExp  Exp() {
		ASTExp  exp;
		ASTExp tmp = null;
		ASTExp signal = null; 
		exp = T();
		while (la.kind == 8 || la.kind == 9) {
			if (la.kind == 8) {
				Get();
				tmp = T();
				signal = new ASTExp("+"); 
			} else {
				Get();
				tmp = T();
				signal = new ASTExp("-"); 
			}
			signal.addLast(exp);
			signal.addLast(tmp);
			exp = signal;
			
		}
		return exp;
	}

	LinkedList  ParamNames() {
		LinkedList  paramNames;
		paramNames = new LinkedList<String>();
		Expect(2);
		paramNames.addLast(t.val); 
		while (la.kind == 7) {
			Get();
			Expect(2);
			paramNames.addLast(t.val); 
		}
		return paramNames;
	}

	ASTExp  T() {
		ASTExp  t;
		t = null;
		ASTExp u = null;
		ASTExp signal = null;
		
		t = U();
		while (StartOf(3)) {
			switch (la.kind) {
			case 10: {
				Get();
				u = U();
				signal = new ASTExp("*"); 
				break;
			}
			case 11: {
				Get();
				u = U();
				signal = new ASTExp("/"); 
				break;
			}
			case 12: {
				Get();
				u = U();
				signal = new ASTExp("%"); 
				break;
			}
			case 13: {
				Get();
				u = U();
				signal = new ASTExp(">"); 
				break;
			}
			case 14: {
				Get();
				u = U();
				signal = new ASTExp("<"); 
				break;
			}
			case 15: {
				Get();
				u = U();
				signal = new ASTExp("=="); 
				break;
			}
			case 16: {
				Get();
				u = U();
				signal = new ASTExp(">="); 
				break;
			}
			case 17: {
				Get();
				u = U();
				signal = new ASTExp("<="); 
				break;
			}
			case 18: {
				Get();
				u = U();
				signal = new ASTExp("!="); 
				break;
			}
			}
			signal.addLast(t);
			signal.addLast(u);
			t = signal;
			
		}
		return t;
	}

	ASTExp  U() {
		ASTExp  u;
		u = null; 
		if (la.kind == 9) {
			Get();
			u = F();
			ASTExp signal = new ASTExp("*");
			signal.addLast(new ASTExp("-1"));
			signal.addLast(u);
			u = signal;
			
		} else if (StartOf(4)) {
			u = F();
		} else SynErr(24);
		return u;
	}

	ASTExp  F() {
		ASTExp  f;
		f = null; 
		if (la.kind == 1) {
			Get();
			f = new ASTExp(t.val) ;
		} else if (la.kind == 2) {
			f = VarOrFunc();
		} else if (la.kind == 19) {
			IFExp();
		} else if (la.kind == 4) {
			Get();
			f = Exp();
			Expect(5);
		} else SynErr(25);
		return f;
	}

	ASTExp  VarOrFunc() {
		ASTExp  var;
		LinkedList<String> paramNames = null;
		Expect(2);
		var = new ASTExp(t.val);
		if (la.kind == 4) {
			Get();
			paramNames = Params();
			Expect(5);
		}
		if(paramNames != null) var.paramNames = paramNames; 
		return var;
	}

	void IFExp() {
		ASTExp p = null;
		Expect(19);
		ExpL();
		Expect(20);
		p = Exp();
		Expect(21);
		p = Exp();
	}

	LinkedList  Params() {
		LinkedList  paramNames;
		ASTExp p = null;
		paramNames = new LinkedList<String>();
		
		p = Exp();
		try{paramNames.addLast(aci.eval(p, aci.global).value); } catch(Exception e){}
		while (la.kind == 7) {
			Get();
			p = Exp();
			try{paramNames.addLast(aci.eval(p, aci.global).value); } catch(Exception e){}
		}
		return paramNames;
	}

	void ExpL() {
		ASTExp p = null;
		p = Exp();
		OpRel();
		p = Exp();
	}

	void OpRel() {
		switch (la.kind) {
		case 13: {
			Get();
			break;
		}
		case 16: {
			Get();
			break;
		}
		case 14: {
			Get();
			break;
		}
		case 17: {
			Get();
			break;
		}
		case 6: {
			Get();
			break;
		}
		case 18: {
			Get();
			break;
		}
		default: SynErr(26); break;
		}
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		AdvCalc();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,T,T, T,x,x,x, x,T,x,x, x,x,x,x, x,x,x,T, x,x,x,x},
		{x,T,T,x, T,x,x,x, x,T,x,x, x,x,x,x, x,x,x,T, x,x,x,x},
		{x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,x},
		{x,T,T,x, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "number expected"; break;
			case 2: s = "ident expected"; break;
			case 3: s = "\"def\" expected"; break;
			case 4: s = "\"(\" expected"; break;
			case 5: s = "\")\" expected"; break;
			case 6: s = "\"=\" expected"; break;
			case 7: s = "\",\" expected"; break;
			case 8: s = "\"+\" expected"; break;
			case 9: s = "\"-\" expected"; break;
			case 10: s = "\"*\" expected"; break;
			case 11: s = "\"/\" expected"; break;
			case 12: s = "\"%\" expected"; break;
			case 13: s = "\">\" expected"; break;
			case 14: s = "\"<\" expected"; break;
			case 15: s = "\"==\" expected"; break;
			case 16: s = "\">=\" expected"; break;
			case 17: s = "\"<=\" expected"; break;
			case 18: s = "\"!=\" expected"; break;
			case 19: s = "\"if\" expected"; break;
			case 20: s = "\"then\" expected"; break;
			case 21: s = "\"else\" expected"; break;
			case 22: s = "??? expected"; break;
			case 23: s = "invalid CExp"; break;
			case 24: s = "invalid U"; break;
			case 25: s = "invalid F"; break;
			case 26: s = "invalid OpRel"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
