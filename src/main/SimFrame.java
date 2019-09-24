package main;

import javax.swing.JFrame;

public class SimFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimFrame() {
		addKeyListener(new Exit());
		SimPanel s = new SimPanel(1080, 720, this);
		//s.setPreferredSize(new Dimension(1080, 720));
		setResizable(false);
		add(s);
		pack();
		setDefaultCloseOperation(3);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
