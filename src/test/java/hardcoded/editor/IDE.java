package hardcoded.editor;

import hardcoded.editor.syntax.SyntaxTextLine;
import hardcoded.editor.syntax.SyntaxTextPanel;
import hardcoded.utils.FileUtils;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

/**
 * Custom (IDE) for my own programing language.<br>
 * 
 * <i>Integrated development environment</i>
 * 
 * @author HardCoded
 */
public class IDE extends JFrame {
	private static final long serialVersionUID = 8431695233762144631L;
	
	static {
		
		try {
			// Fix syntax highighligting.
			System.setProperty("line.separator", "\n");
			
			// UIManager look and Feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			System.err.println("Failed to setLookAndFeel!");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		IDE ide = new IDE();
		ide.setVisible(true);
	}

	private JFileChooser fileChooser;
	private SyntaxTextPanel textPane;
	
	public IDE() {
		setTitle("IDE - testing");
		setSize(640, 360);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		initFx();
		initLayout();
	}
	
	private void initFx() {
		SwingUtilities.invokeLater(() -> {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("C:/Users/Admin/git/HCProgrammingLanguage/res/project/src"));
			fileChooser.setDialogTitle("Open");
		});
	}
	
	private void initLayout() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					File file = fileChooser.getSelectedFile();
					
					if(file != null) {
						byte[] bytes = FileUtils.readFileBytes(file);
						textPane.setText(new String(bytes));
						
						System.out.println("File: " + file);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		mnNewMenu.add(mntmOpen);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnNewMenu.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmReplace = new JMenuItem("Replace");
		mnEdit.add(mntmReplace);
		
		JMenuItem mntmGotoLine = new JMenuItem("Goto Line");
		mnEdit.add(mntmGotoLine);
		
		JMenu mnAout = new JMenu("Help");
		menuBar.add(mnAout);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnAout.add(mntmAbout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new BorderLayout(0, 0));

		SyntaxTextLine syntaxTextLine = new SyntaxTextLine();
		textPane = new SyntaxTextPanel(syntaxTextLine);
		panel.add(textPane);
		panel.add(syntaxTextLine, BorderLayout.WEST);
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
	}
}
