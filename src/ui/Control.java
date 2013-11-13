package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import searchs.SearchType;

/**
 * Robot's piloting panel for UI. 
 * 
 * @author slinkola
 *
 */
public class Control extends JPanel implements ActionListener, ChangeListener {
	/** Serialization UID. */
	static final long serialVersionUID = 1L;
	/** Fixed width of the utility panel. */
	private final int width = 300;	
	/** Height of the utility panel. */
	private final int height = 220;
	/** Commands robot to start traveling and searching towards goal. */
	private final JButton startSearchButton = new JButton();
	/** Clear the current search. */
	private final JButton clearSearchButton = new JButton();
	/** Commands robot to make observations. */
	private final JSlider colorSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	/** Group for different search type buttons. */
	private final ButtonGroup searchTypeButtons = new ButtonGroup();
	/** Button for A* search */
	private final JRadioButton AStarButton = new JRadioButton();
	/** Button for A* search */
	private final JRadioButton DLiteButton = new JRadioButton();
	/** Button for A* search */
	private final JRadioButton ARAButton = new JRadioButton();
	/** Button for A* search */
	private final JRadioButton ADStarButton = new JRadioButton();

	public Control() {
		//this.setBackground(new Color(240, 230, 245));
		this.setOpaque(false);
		this.setLayout(new GridLayout(0, 1, 0, 0));
		Dimension d = new Dimension(this.width, this.height);
		this.setMaximumSize(d);
		this.setPreferredSize(d);
		this.setBorder(UIScheme.createTitledBorder("Control"));
		JPanel controlWrapper = new JPanel();
		controlWrapper.setOpaque(false);
		
		JPanel searchButtonWrapper = new JPanel();
		searchButtonWrapper.setLayout(new GridLayout(1, 0, 20, 0));
		searchButtonWrapper.setPreferredSize(new Dimension(270, 30));
		searchButtonWrapper.setBounds(0, 0, this.width, 30);
		searchButtonWrapper.setOpaque(false);
		UIScheme.initButton(this.startSearchButton, 125, 30, searchButtonWrapper, "Start", this);
		UIScheme.initButton(this.clearSearchButton, 125, 30, searchButtonWrapper, "Clear", this);
		controlWrapper.add(searchButtonWrapper);

		JPanel searchTypeWrapper = new JPanel();
		searchTypeWrapper.setLayout(new GridLayout(2, 2, 0, 0));
		searchTypeWrapper.setPreferredSize(new Dimension(280, 70));
		searchTypeWrapper.setBounds(0, 0, this.width, 70);
		searchTypeWrapper.setOpaque(false);
		searchTypeWrapper.setBorder(UIScheme.createTitledBorder("Search Type"));
		
		this.searchTypeButtons.add(this.AStarButton);
		this.searchTypeButtons.add(this.DLiteButton);
		this.searchTypeButtons.add(this.ARAButton);
		this.searchTypeButtons.add(this.ADStarButton);
		this.AStarButton.setSelected(true);
		UIScheme.initButton(this.AStarButton, 55, 25, searchTypeWrapper, "A*", this);
		UIScheme.initButton(this.DLiteButton, 55, 25, searchTypeWrapper, "D* Lite", this);
		UIScheme.initButton(this.ARAButton, 55, 25, searchTypeWrapper, "ARA*", this);
		UIScheme.initButton(this.ADStarButton, 55, 25, searchTypeWrapper, "AD*", this);
		controlWrapper.add(searchTypeWrapper);
		
		JPanel sliderWrapper = new JPanel();
		sliderWrapper.setPreferredSize(new Dimension(280, 70));
		sliderWrapper.setLayout(new BorderLayout());
		sliderWrapper.setBounds(0, 0, this.width, 70);
		sliderWrapper.setOpaque(false);
		sliderWrapper.setBorder(UIScheme.createTitledBorder("Paint Color"));
		
		this.colorSlider.setPreferredSize(new Dimension(255, 50));
		this.colorSlider.setSnapToTicks(true);
		this.colorSlider.setOpaque(false);
		Hashtable<Integer, JLabel> ht = new Hashtable<Integer, JLabel>();
		JLabel black = new JLabel("black");
		black.setForeground(UIScheme.GRAYBLUE4);
		JLabel white = new JLabel("white");
		white.setForeground(UIScheme.GRAYBLUE4);
		ht.put(0, black);
		ht.put(255, white);
		this.colorSlider.setLabelTable(ht);
		this.colorSlider.setPaintLabels(true);
		this.colorSlider.addChangeListener(this);
		sliderWrapper.add(this.colorSlider, BorderLayout.CENTER);
		controlWrapper.add(sliderWrapper);
		
		this.add(controlWrapper);
	}
	
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * Set robot's control buttons' enabled status.
	 * @param enabled new enabled status of the buttons
	 */
	public void setControlsEnabled(boolean enabled) {
		this.startSearchButton.setEnabled(enabled);
		this.clearSearchButton.setEnabled(enabled);
		//this.observeButton.setEnabled(enabled);
	}
	
	public void actionPerformed(ActionEvent e) { 
		String ac = e.getActionCommand();
		
		if (ac.equals("Start")) {
			EventHandler.startSearch();
		}
		else if (ac.equals("Clear")) {
			EventHandler.clearSearch();
		}
		else if (ac.equals("A*")) {
			EventHandler.printInfo("A* search selected.");
			EventHandler.changeSearchType(SearchType.ASTAR);
		}
		else if (ac.equals("D* Lite")) {
			EventHandler.printInfo("D* Lite search selected.");
			EventHandler.changeSearchType(SearchType.DLITE);
		}
		else if (ac.equals("ARA*")) {
			EventHandler.printInfo("ARA* search selected.");
			EventHandler.changeSearchType(SearchType.ARA);
		}
		else if (ac.equals("AD*")) {
			EventHandler.printInfo("AD* search selected.");
			EventHandler.changeSearchType(SearchType.ADSTAR);
		}
		
		MainUI.map.requestFocusInWindow();
	}
	
	public SearchType getSelectedSearchType() {
		String actionCommand = this.searchTypeButtons.getSelection().getActionCommand();
		if (actionCommand == "A*") return SearchType.ASTAR;
		if (actionCommand == "D* Lite") return SearchType.DLITE;
		if (actionCommand == "ARA*") return SearchType.ARA;
		else return SearchType.ADSTAR;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.colorSlider) {
			int val = this.colorSlider.getValue();
			EventHandler.setDrawingColor(val);
		}
		
	}
}
