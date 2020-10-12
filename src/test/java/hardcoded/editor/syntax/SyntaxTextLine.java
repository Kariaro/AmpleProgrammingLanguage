package hardcoded.editor.syntax;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class SyntaxTextLine extends JTextPane {
	private static final long serialVersionUID = -9006564469388489531L;
	private StringBuilder lineText;
	private int lineIndex;
	
	public SyntaxTextLine() {
		setBorder(new EmptyBorder(2, 4, 2, 10));
		setBackground(new Color(0xF0F0F0));
		setDisabledTextColor(Color.DARK_GRAY);
		setRequestFocusEnabled(false);
		setFocusCycleRoot(false);
		setFocusTraversalKeysEnabled(false);
		setFocusable(false);
		setDoubleBuffered(true);
		setEditable(false);
		setEnabled(false);
		setText("0");
		
		
		DefaultCaret caret = new DefaultCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		setCaret(caret);
		
		lineText = new StringBuilder("0");
		lineIndex = 0;
	}
	
	private int size(int number) {
		return String.valueOf(number).length();
	}
	
	private void addLines(int amount) {
		if(amount < 0) {
			if(lineIndex + amount < 1) {
				if(lineText.length() != 0) {
					lineText.delete(0, lineText.length());
				}
				
				// ???
				
				lineText.append("0");
				lineIndex = 0;
				return;
			}
			
			for(int i = 0; i < -amount; i++) {
				int size = size(lineIndex - i) + 1;
				lineText = lineText.delete(lineText.length() - size, lineText.length());
			}
		} else {
			for(int i = 0; i < amount; i++) {
				lineText.append('\n').append(lineIndex + i + 1);
			}
		}
		
		lineIndex += amount;
	}
	
	public void setLines(int num) {
		int change = num - lineIndex;
		
		if(change != 0) {
			addLines(change);
			setText(lineText.toString());
		}
	}
}
