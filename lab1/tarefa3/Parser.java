

public class Parser {
	public static final int _EOF = 0;
	public static final int _nomeProprio = 1;
	public static final int _palavra = 2;
	public static final int maxT = 11;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	

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
	
	void Familia() {
		Expect(3);
		Expect(4);
		Expect(1);
		Secoes();
	}

	void Secoes() {
		Secao();
		while (la.kind == 5 || la.kind == 7) {
			Secao();
		}
	}

	void Secao() {
		if (la.kind == 5) {
			Pais();
		} else if (la.kind == 7) {
			Filhos();
		} else SynErr(12);
	}

	void Pais() {
		Expect(5);
		Expect(4);
		NomeComentado();
		while (la.kind == 6) {
			Get();
			NomeComentado();
		}
	}

	void Filhos() {
		Expect(7);
		Expect(4);
		NomeComentado();
		while (la.kind == 6) {
			Get();
			NomeComentado();
		}
	}

	void NomeComentado() {
		Nome();
		if (la.kind == 8) {
			Get();
		}
		if (la.kind == 9) {
			Comentario();
		}
	}

	void Nome() {
		Expect(1);
		while (la.kind == 1) {
			Get();
		}
	}

	void Comentario() {
		Expect(9);
		if (la.kind == 2) {
			Get();
		} else if (la.kind == 1) {
			Get();
		} else SynErr(13);
		while (la.kind == 1 || la.kind == 2) {
			if (la.kind == 2) {
				Get();
			} else {
				Get();
			}
		}
		Expect(10);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Familia();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x}

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
			case 1: s = "nomeProprio expected"; break;
			case 2: s = "palavra expected"; break;
			case 3: s = "\"Sobrenome\" expected"; break;
			case 4: s = "\":\" expected"; break;
			case 5: s = "\"Pais\" expected"; break;
			case 6: s = "\",\" expected"; break;
			case 7: s = "\"Filhos\" expected"; break;
			case 8: s = "\"(falecido)\" expected"; break;
			case 9: s = "\"[\" expected"; break;
			case 10: s = "\"]\" expected"; break;
			case 11: s = "??? expected"; break;
			case 12: s = "invalid Secao"; break;
			case 13: s = "invalid Comentario"; break;
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
