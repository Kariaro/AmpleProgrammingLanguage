package hardcoded.editor.syntax;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextPane;
import javax.swing.text.*;

import hardcoded.editor.syntax.SyntaxHighlighter.Highlight;

public class SyntaxTextPanel extends JTextPane {
	private static final long serialVersionUID = 2912516642205882647L;
	private SyntaxTextLine lineIndexComponent;
	private SyntaxHighlighter highlighter;
	
	public SyntaxTextPanel(SyntaxTextLine lineIndex) {
		this.lineIndexComponent = lineIndex;
		
		setFont(new Font("Consolas", Font.PLAIN, 14));
		
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if(lineIndexComponent != null) {
					lineIndexComponent.setLines(getNumLines());
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// Ideas
				//   Fix auto complete with '{' '[' '(' '"' '\''
				
//				if(e.getKeyChar() == '"') {
//					try {
//						getDocument().insertString(getCaretPosition(), "\"\"", null);
//					} catch(BadLocationException e1) {
//						e1.printStackTrace();
//					}
//					e.consume();
//				}
				
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					int pos = getCaretPosition() - 1;
					String string = getText();
					int lineIndex = 0;
					for(int i = pos; i >= 0; i--) {
						char c = string.charAt(i);
						
						if(c == '\n') {
							lineIndex = i + 1;
							break;
						}
					}
					
					String tabsS = "";
					for(int i = lineIndex; i < string.length(); i++) {
						if(string.charAt(i) != '\t') break;
						tabsS += '\t';
					}
					
					try {
						getDocument().insertString(pos + 1, "\n" + tabsS, null);
					} catch(BadLocationException e1) {
						e1.printStackTrace();
					}
					
					e.consume();
				} else {
					
				}
			}
		});
		
		highlighter = new SyntaxHighlighter();
		
		fixTabSize();
	}
	
	
	private void fixTabSize() {
		int w = getFontMetrics(getFont()).charWidth(' ');
		
		int size = 100;
		TabStop[] tabs = new TabStop[size];
		for(int i = 0; i < size; i++) {
			tabs[i] = new TabStop(i * w * 4, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
		}
		
		TabSet tabset = new TabSet(tabs);

		style = StyleContext.getDefaultStyleContext();
		AttributeSet aset = style.addAttribute(style.getEmptySet(), StyleConstants.TabSet, tabset);
		
		setParagraphAttributes(aset, false);
	}
	
	private AttributeSet textStyle;
	private StyleContext style;
	private void addHighlight(Highlight c) {
		style = StyleContext.getDefaultStyleContext();
		textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, c.color);
		
		getStyledDocument().setCharacterAttributes(c.start, c.length, textStyle, true);
	}
	
	private void doHighlight() {
		style = StyleContext.getDefaultStyleContext();
		textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
		getStyledDocument().setCharacterAttributes(0, getStyledDocument().getLength(), textStyle, true);
		
		String text = getText();
		for(Highlight hl : highlighter.createHighlight(text)) addHighlight(hl);
	}
	
	public void setFont(Font font) {
		super.setFont(font);
		
		if(lineIndexComponent != null) {
			lineIndexComponent.setFont(font);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		doHighlight();
		super.paint(g);
	}
	
	public int getNumLines() {
		byte[] bytes = getText().getBytes(); // How slow is this line?
		
		int line = 0;
		for(int i = 0; i < bytes.length; i++) {
			byte c = bytes[i];
			
			if(c == '\n') {
				line++;
			} else if(c == '\r') {
				line++;
				
				if(i + 1 == bytes.length) break;
				if(bytes[i + 1] == '\n') i++;
			}
		}
		
		return line;
	}
	
	@Override
	public void setText(String t) {
		t = t.replaceAll("(\r\n|\r)", "\n");
		super.setText(t);
		
		if(lineIndexComponent != null) {
			lineIndexComponent.setLines(getNumLines());
			
			doHighlight();
		}
	}
}
