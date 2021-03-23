package com.hardcoded.visualizer;

import java.awt.*;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.expression.Expr;
import com.hardcoded.compiler.impl.statement.ExprStat;
import com.hardcoded.compiler.impl.statement.ProgramStat;
import com.hardcoded.compiler.impl.statement.Stat;
import com.hardcoded.compiler.lexer.Token;
import com.hardcoded.compiler.parsetree.AmpleParseTree;
import com.hardcoded.compiler.parsetree.AmpleTreeValidator;
import com.hardcoded.utils.FileUtils;

/**
 * Test visualizer for parse trees
 * @author HardCoded
 * @since 0.2.0
 */
public class ParseTreeVisualizer extends JPanel {
	private static final long serialVersionUID = -648826653395842049L;
	
	public static void main(String[] args) {
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
		add(area);
		
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
	
	private ProgramStat stat;
	private byte[] bytes = new byte[0];
	public void start() {
		boolean need_update = false;
		
		try {
			byte[] last = bytes;
			bytes = FileUtils.readFileBytes("res/main.ample");
			
			if(!Arrays.equals(last, bytes)) {
				area.setText(bytes);
				
				AmpleParseTree tree = new AmpleParseTree();
				stat = tree.process(null, bytes);
				AmpleTreeValidator validator = new AmpleTreeValidator();
				validator.process(null, stat);
				
				updateStatRect(stat);
				need_update = true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(need_update) area.repaint();
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
	}
	
	private void updateStatRect(Statement stat) {
		if(stat instanceof Stat) {
			Stat st = (Stat)stat;
			Token s = st.getToken();
			Token e = st.getEnd();
			
			//int hash = stat.getClass().toString().hashCode() & 0xffffff;
			Color c = new Color(0x00 | (0x20 << 24), true);
			area.addRange(Range.get(s.offset, e.offset + e.value.length(), c, st.toString()));
		}
		
		if(stat instanceof ExprStat) {
			ExprStat st = (ExprStat)stat;
			for(Expression e : st.getExpressions()) {
				updateExprRect(e);
			}
		}
		
		for(Statement s : stat.getStatements()) {
			updateStatRect(s);
		}
	}
	
	private void updateExprRect(Expression expr) {
		if(expr instanceof Expr) {
			Expr ex = (Expr)expr;
			Token s = ex.getToken();
			Token e = ex.getEnd();
			
			if(e == Token.EMPTY) {
				e = s;
			}
			
			//int hash = expr.getClass().toString().hashCode() & 0xffffff;
			Color c = new Color(0x0000ff | (0x20 << 24), true);
			area.addRange(Range.get(s.offset, e.offset + e.value.length(), c, ex.toString()));
		}
		
		for(Expression s : expr.getExpressions()) {
			updateExprRect(s);
		}
	}
}
