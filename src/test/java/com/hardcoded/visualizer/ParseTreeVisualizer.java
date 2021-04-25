package com.hardcoded.visualizer;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.LinkerScope;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.expression.AtomExpr;
import com.hardcoded.compiler.impl.expression.Expr;
import com.hardcoded.compiler.impl.serial.SerialParseTree;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.compiler.lexer.AmpleLexer;
import com.hardcoded.compiler.lexer.Lang;
import com.hardcoded.compiler.lexer.Token;
import com.hardcoded.compiler.parsetree.AmpleParseTreeWorker;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;
import com.hardcoded.utils.FileUtils;

/**
 * Test visualizer for parse trees
 * @author HardCoded
 * @since 0.2.0
 */
public class ParseTreeVisualizer extends JPanel {
	private static final long serialVersionUID = -648826653395842049L;
	
	public static void main(String[] args) {
		Log.setLogLevel(Log.Level.ALL);
		Toolkit.getDefaultToolkit().setDynamicLayout(false); 
		
		JFrame frame = new JFrame("ParseTreeVisualizer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(true);
		ParseTreeVisualizer panel = new ParseTreeVisualizer();
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		panel.start();
	}
	
	private TextArea area;
	public ParseTreeVisualizer() {
		Dimension dim = new Dimension(1000, 640);
		setMaximumSize(dim);
		setMinimumSize(dim);
		setPreferredSize(dim);
		area = new TextArea();
		
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		add(area, BorderLayout.CENTER);
		
		Thread t = new Thread(() -> {
			while(true) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				
				start();
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	private byte[] bytes = new byte[0];
	public void start() {
		boolean need_update = false;
		
		try {
			byte[] last = bytes;
			bytes = FileUtils.readFileBytes("res/main.ample");
			
			if(!Arrays.equals(last, bytes)) {
				area.setText(bytes);
				boolean serial = false;
				
				ProgramStat stat;
				LinkerScope link;
				if(serial) {
					FileInputStream stream = new FileInputStream(new File("res/test/oos.serial"));
					SerialParseTree ser = SerialParseTree.read(stream);
					stream.close();
					
					stat = (ProgramStat)ser.getStatement();
					link = ser.getLinkerScope();
				} else {
					Lang lang = Lang.wrap(AmpleLexer.getLexer().parse(bytes));
					Options options = Options.parse(new String[0]);
					
					AmpleParseTreeWorker worker = new AmpleParseTreeWorker();
					worker.process(options, lang);
					stat = worker.getProgram();
					link = worker.getLink();
				}
				
				setStat(stat, link);
				need_update = true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(need_update) area.repaint();
		}
	}
	
	private boolean setStat(ProgramStat stat, LinkerScope link) {
		updateStatRect(stat, link);
		System.out.println("----------------");
		System.out.println("Imported:");
		for(Reference ref : link.getImport()) {
			System.out.printf("  %4d: (%s) %s\n", ref.getUniqueIndex(), ref.getType(), ref.getName());
		}
		System.out.println("\nExported:");
		for(Reference ref : link.getExport()) {
			System.out.printf("  %4d: (%s) %s\n", ref.getUniqueIndex(), ref.getType(), ref.getName());
		}
		
		return true;
	}
	
	private static final Color stat_color = new Color(0x00 | (0x40 << 24), true);
	private static final Color expr_color = new Color(0x0000ff | (0x40 << 24), true);
	private void updateStatRect(Statement stat, LinkerScope link) {
		if(EmptyStat.isEmpty(stat)) return;
		
		area.addRange(Range.get(stat.getStartOffset(), stat.getEndOffset(), stat_color, stat.toString()));
		
		if(stat instanceof ExprStat) {
			ExprStat st = (ExprStat)stat;
			for(Expression e : st.getExpressions()) {
				updateExprRect(e, link);
			}
		}
		
		for(Statement s : stat.getStatements()) {
			updateStatRect(s, link);
		}
		
		if(stat instanceof IRefContainer) {
			IRefContainer st = (IRefContainer)stat;
			Reference ref = st.getReference();
			Token t = st.getRefToken();
			area.addRange(Range.get(t.offset, t.offset + t.value.length(), ref.getUniqueIndex(), stat_color, ref.toString()));
		}
		
		if(stat instanceof FuncStat) {
			FuncStat st = (FuncStat)stat;
			for(Statement s : st.getArguments()) {
				updateStatRect(s, link);
			}
		}
	}
	
	private void updateExprRect(Expression expr, LinkerScope link) {
		if(expr instanceof Expr) {
			Expr ex = (Expr)expr;
			int s = ex.getStartOffset();
			int e = ex.getEndOffset();
			
//			{
//				Token tok = ex.getToken();
//				System.out.printf("{value:\"%s\", group:\"%s\", offset:%d, line:%d, column:%d}\n", tok.value, tok.group, tok.offset, tok.line, tok.column);
//			}
			
			if(ex instanceof AtomExpr && ((AtomExpr)ex).isReference()) {
				Reference ref = ((AtomExpr)ex).getReference();
				String tip = ex.toString();
				
				if(link.hasImported(ref.getName(), ref.getType())) {
					Reference rf = link.getImported(ref.getName(), ref.getType());
					if(rf.getUniqueIndex() == ref.getUniqueIndex()) {
						tip = "import " + tip;
					}
				}
				
				area.addRange(Range.get(s, e, ref.getUniqueIndex(), expr_color, tip));
			} else {
				area.addRange(Range.get(s, e, expr_color, ex.toString()));
			}
		}
		
		for(Expression s : expr.getExpressions()) {
			updateExprRect(s, link);
		}
	}
}
