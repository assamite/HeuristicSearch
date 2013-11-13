package ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Main menu bar of the UI. Sets menus, menu items and keyboard shortcuts to 
 * some of the menu items.
 * @author slinkola
 *
 */
public class MenuBar extends JMenuBar implements ActionListener {
	/** Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	private static final JMenu mapMenu = new JMenu("Map");
	private static final JMenuItem zoomIn = new JMenuItem("Zoom In");
	private static final JMenuItem zoomOut = new JMenuItem("Zoom Out");
	private static final JMenuItem load = new JMenuItem("Load Map");
	private static final JFileChooser chooser = new JFileChooser();
	private static final JMenu helpMenu = new JMenu("Help");
	private static final JMenuItem instructions= new JMenuItem("Instructions");
	
	private static final String msg = 
			"CTRL-click sets root\n" +
			"ALT-click sets goal\n" +
			"Any image (jpg/png) can be loaded as a map.\n" +
			"The image is converted to the gray scale in memory.\n" +
			"Travel cost to each pixel is depended on the\n" +
			"darkness of the pixel.\n" +
			"Straight lines can be drawn with currently selected\n" +
			"gray scale color by clicking starting point and dragging\n" +
			"the mouse to the ending point of the line.";	

	/** Default constructor. */
	public MenuBar() {
		super();
		load.addActionListener(this);	
		mapMenu.add(load);
		load.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.add(mapMenu);
		instructions.addActionListener(this);
		instructions.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		helpMenu.add(instructions);
		this.add(helpMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == load) {
			int ret = chooser.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (!EventHandler.loadMap(file)) {
					JOptionPane.showMessageDialog(null, "Could not load map.");
				}
			}
		}
		if (e.getSource() == instructions) {
			JOptionPane.showMessageDialog(null, msg, "Instructions", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
