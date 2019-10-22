package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.sql.DataSource;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.SlidingValidation;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.GenericObjectEditor;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.TaskLogger;
import weka.gui.WekaTaskMonitor;
import weka.gui.explorer.ClassifierErrorsPlotInstances;
import weka.gui.explorer.Explorer;
import weka.gui.explorer.Explorer.CapabilitiesFilterChangeEvent;
import weka.gui.explorer.Explorer.CapabilitiesFilterChangeListener;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.explorer.ExplorerDefaults;
import weka.core.Attribute;


@SuppressWarnings("serial")
public class SlidingValidationPanel extends JPanel implements weka.gui.explorer.Explorer.LogHandler, ExplorerPanel, CapabilitiesFilterChangeListener {

	/** the parent frame */ 
	protected Explorer m_Explorer = null;

	/** The panel for log and status messages */
	protected LogPanel m_LogPanel = new LogPanel(new WekaTaskMonitor());

	/** sends notifications when the set of working instances gets changed*/ 
	protected PropertyChangeSupport m_Support = new PropertyChangeSupport(this);

	String[] attribNames;

	/** The main set of instances we're playing with. */
	protected Instances m_Instances;

	/** Lets the user select the class column. */
	protected JComboBox m_ClassCombo = new JComboBox(); //loaded at setInstances method

	protected boolean isNominal;

	private JFormattedTextField firstTrainField;
	private JFormattedTextField testField;
	private JTextArea outArea;
	private JFormattedTextField stepField;	//jon
	private JFormattedTextField topField;	//jon

	//private HashMap<String, Integer> classes = new HashMap<>();

	/** Button to start the classification thread. */
	protected JButton btnStart = new JButton("Start");

	/** Button to start the classification thread. */
	protected JButton btnStop = new JButton("Stop");

	/** Lets the user configure the classifier. */
	protected GenericObjectEditor m_ClassifierEditor = new GenericObjectEditor();
	
	/** The panel showing the current classifier selection. */
	protected PropertyPanel m_CEPanel = new PropertyPanel(m_ClassifierEditor);

	/** A thread that classification runs in. */
	protected Thread m_RunThread;

	private JLabel labelFirstTrain;
	private JLabel labelTest;
	private JCheckBox checkAccumulate;
	private JLabel labelStep;	//jon
	private JLabel labelTop;	//jon
	
	private JList historyList;
	private LinkedHashMap<String, StringBuffer> historys = new LinkedHashMap<>();

	public SlidingValidationPanel(){
		setLayout(new BorderLayout(0, 0));
		JPanel pNorth = new JPanel();
		pNorth.setLayout(new BorderLayout(0, 0));
		m_CEPanel.setBorder(BorderFactory.createTitledBorder("Classifier"));
		pNorth.add(m_CEPanel);
		add(pNorth, BorderLayout.NORTH);

		JPanel pCenter = new JPanel();
		add(pCenter, BorderLayout.CENTER);
		GridBagLayout gbl_pCenter = new GridBagLayout();
		gbl_pCenter.columnWidths = new int[] {150, 563};
		gbl_pCenter.rowHeights = new int[] {380};
		gbl_pCenter.columnWeights = new double[]{0.0, 1.0};
		gbl_pCenter.rowWeights = new double[]{1.0};
		pCenter.setLayout(gbl_pCenter);

		JPanel pMenu = new JPanel();
		pMenu.setAlignmentX(0.0f);
		GridBagConstraints gbc_pMenu = new GridBagConstraints();
		gbc_pMenu.fill = GridBagConstraints.VERTICAL;
		gbc_pMenu.insets = new Insets(0, 0, 5, 5);
		gbc_pMenu.gridx = 0;
		gbc_pMenu.gridy = 0;
		pCenter.add(pMenu, gbc_pMenu);
		GridBagLayout gbl_pMenu = new GridBagLayout();
		gbl_pMenu.columnWidths = new int[] {150};
		gbl_pMenu.rowHeights = new int[] {150, 75, 168, 0};
		gbl_pMenu.columnWeights = new double[]{1.0};
		gbl_pMenu.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		pMenu.setLayout(gbl_pMenu);

		JPanel pLabels = new JPanel();
		pLabels.setBorder(new TitledBorder(null, "Test options", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, new Color(59, 59, 59)));
		GridBagConstraints gbc_pLabels = new GridBagConstraints();
		gbc_pLabels.fill = GridBagConstraints.HORIZONTAL;
		gbc_pLabels.anchor = GridBagConstraints.NORTH;
		gbc_pLabels.insets = new Insets(0, 0, 5, 0);
		gbc_pLabels.gridx = 0;
		gbc_pLabels.gridy = 0;
		pMenu.add(pLabels, gbc_pLabels);
		GridBagLayout gbl_pLabels = new GridBagLayout();
		gbl_pLabels.columnWidths = new int[] {189};
		gbl_pLabels.rowHeights = new int[] {30, 30, 30};
		gbl_pLabels.columnWeights = new double[]{1.0};
		gbl_pLabels.rowWeights = new double[]{1.0, 0.0, 0.0};
		pLabels.setLayout(gbl_pLabels);

		ButtonGroup radioGroup = new ButtonGroup();

		NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(1);
		formatter.setMaximum(100);
		formatter.setAllowsInvalid(true);
		// If you want the value to be committed on each keystroke instead of focus lost
		formatter.setCommitsOnValidEdit(true);
		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		pLabels.add(panel_3, gbc_panel_3);
		panel_3.setLayout(new GridLayout(0, 2, 0, 0));

		JButton btnMore = new JButton("More");
		btnMore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String text = "NAME\r\n" +
						"Training-Test Sliding Validation Weka Extension\r\n" +
						"\r\n"+
						"OPTIONS\r\n" +
						"Window train size -- the percent of instances used to train the classifier\r\n"+
						"Windows test size -- the percent of instances used to test the classifier\r\n"+
						"Accumulate the training window --  accumulate or not the window size for each new model";
								
				showPopUpInfoWindow("More", text, btnMore);
			}
		});
		panel_3.add(btnMore);

		JButton btnCapabilities = new JButton("Capabilities");
		btnCapabilities.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String text = "CAPABILITIES\r\n" +
						"Tasks -- Classification, Regression\r\n" +
						"\r\n" +
						"Attributes --  Binary attributes, Numeric attributes, Nominal attributes";
				showPopUpInfoWindow("Capacilities", text, btnCapabilities);
			}
		});
		panel_3.add(btnCapabilities);

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		pLabels.add(panel_2, gbc_panel_2);
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] {30, 30, 48};
		gbl_panel_2.rowHeights = new int[] {20, 20};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0};
		panel_2.setLayout(gbl_panel_2);

		//trainamento		
		labelFirstTrain = new JLabel("Training window size");
		labelFirstTrain.setToolTipText("the percent of instances used to train the classifier");
		labelFirstTrain.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelFirstTrain = new GridBagConstraints();
		gbc_labelFirstTrain.fill = GridBagConstraints.BOTH;
		gbc_labelFirstTrain.insets = new Insets(0, 0, 5, 5);
		gbc_labelFirstTrain.gridx = 0;
		gbc_labelFirstTrain.gridy = 0;
		panel_2.add(labelFirstTrain, gbc_labelFirstTrain);

		JLabel label = new JLabel("%");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		panel_2.add(label, gbc_label);

		firstTrainField = new JFormattedTextField(formatter);
		firstTrainField.setToolTipText("the percent of instances used to train the classifier");
		firstTrainField.setText("0");
		GridBagConstraints gbc_firstTrainField = new GridBagConstraints();
		gbc_firstTrainField.fill = GridBagConstraints.BOTH;
		gbc_firstTrainField.insets = new Insets(0, 0, 5, 5);
		gbc_firstTrainField.gridx = 2;
		gbc_firstTrainField.gridy = 0;
		panel_2.add(firstTrainField, gbc_firstTrainField);
		firstTrainField.setColumns(10);
		//teste
		labelTest = new JLabel("Test window size");
		labelTest.setToolTipText("the percent of instances used to test the classifier");
		labelTest.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTest = new GridBagConstraints();
		gbc_labelTest.fill = GridBagConstraints.BOTH;
		gbc_labelTest.insets = new Insets(0, 0, 0, 5);
		gbc_labelTest.gridx = 0;
		gbc_labelTest.gridy = 1;
		panel_2.add(labelTest, gbc_labelTest);

		JLabel label_1 = new JLabel("%");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 0, 5);
		gbc_label_1.anchor = GridBagConstraints.EAST;
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 1;
		panel_2.add(label_1, gbc_label_1);

		testField = new JFormattedTextField(formatter);
		testField.setToolTipText("the percent of instances used to test the classifier");
		testField.setText("0");
		GridBagConstraints gbc_testField = new GridBagConstraints();
		gbc_testField.insets = new Insets(0, 0, 0, 5);
		gbc_testField.fill = GridBagConstraints.BOTH;
		gbc_testField.gridx = 2;
		gbc_testField.gridy = 1;
		panel_2.add(testField, gbc_testField);

		//inicio-step
		labelStep = new JLabel("Training step size");
		labelStep.setToolTipText("Step iterator");
		labelStep.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelStep = new GridBagConstraints();
		gbc_labelStep.fill = GridBagConstraints.BOTH;
		gbc_labelStep.insets = new Insets(5, 0, 5, 5);
		gbc_labelStep.gridx = 0;
		gbc_labelStep.gridy = 2;
		panel_2.add(labelStep, gbc_labelStep);

		JLabel label_step = new JLabel();
		GridBagConstraints gbc_label_step = new GridBagConstraints();
		gbc_label_step.insets = new Insets(5, 0, 5, 5);
		gbc_label_step.anchor = GridBagConstraints.EAST;
		gbc_label_step.gridx = 1;
		gbc_label_step.gridy = 2;
		panel_2.add(label_step, gbc_label_step);

		stepField = new JFormattedTextField(formatter);
		stepField.setToolTipText("Step iterator");
		stepField.setText("0");
		GridBagConstraints gbc_stepField = new GridBagConstraints();
		gbc_stepField.insets = new Insets(5, 0, 5, 5);
		gbc_stepField.fill = GridBagConstraints.BOTH;
		gbc_stepField.gridx = 2;
		gbc_stepField.gridy = 2;
		panel_2.add(stepField, gbc_stepField);
		//fim-step

		//inicio-top
		labelTop = new JLabel("Training top size");
		labelTop.setToolTipText("Top iterator");
		labelTop.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTop = new GridBagConstraints();
		gbc_labelTop.fill = GridBagConstraints.BOTH;
		gbc_labelTop.insets = new Insets(0, 0, 5, 5);
		gbc_labelTop.gridx = 0;
		gbc_labelTop.gridy = 3;
		panel_2.add(labelTop, gbc_labelTop);

		JLabel label_top = new JLabel();
		GridBagConstraints gbc_label_top = new GridBagConstraints();
		gbc_label_top.insets = new Insets(0, 0, 5, 5);
		gbc_label_top.anchor = GridBagConstraints.EAST;
		gbc_label_top.gridx = 1;
		gbc_label_top.gridy = 3;
		panel_2.add(label_top, gbc_label_top);

		topField = new JFormattedTextField(formatter);
		topField.setToolTipText("Top iterator");
		topField.setText("0");
		GridBagConstraints gbc_topField = new GridBagConstraints();
		gbc_topField.insets = new Insets(0, 0, 5, 5);
		gbc_topField.fill = GridBagConstraints.BOTH;
		gbc_topField.gridx = 2;
		gbc_topField.gridy = 3;
		panel_2.add(topField, gbc_topField);
		//fim-step
		
		checkAccumulate = new JCheckBox("Accumulate the training window");
		GridBagConstraints gbc_checkAccumulate = new GridBagConstraints();
		gbc_checkAccumulate.insets = new Insets(0, 0, 5, 0);
		gbc_checkAccumulate.gridx = 0;
		gbc_checkAccumulate.gridy = 2;
		pLabels.add(checkAccumulate, gbc_checkAccumulate);
		checkAccumulate.setToolTipText("Accumulate the training window?");
		checkAccumulate.setFont(new Font("Tahoma", Font.PLAIN, 11));

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		pMenu.add(panel, gbc_panel);
		panel.setLayout(new GridLayout(2, 2));
		
		JPanel panel_5 = new JPanel();
		panel.add(panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] {217, 0};
		gbl_panel_5.rowHeights = new int[]{38, 0};
		gbl_panel_5.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);
		GridBagConstraints gbc_m_ClassCombo = new GridBagConstraints();
		gbc_m_ClassCombo.fill = GridBagConstraints.BOTH;
		gbc_m_ClassCombo.gridx = 0;
		gbc_m_ClassCombo.gridy = 0;
		m_ClassCombo.setToolTipText("Dependent variable");
		panel_5.add(m_ClassCombo, gbc_m_ClassCombo);
		m_ClassCombo.setBorder(new EmptyBorder(5, 5, 5, 5));
		
				m_ClassCombo.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int selected = m_ClassCombo.getSelectedIndex();
						if (selected != -1) {
							isNominal = m_Instances.attribute(selected).isNominal();
							//m_OutputPerClassBut.setEnabled(isNominal);
							//m_OutputConfusionBut.setEnabled(isNominal);
						}
						updateCapabilitiesFilter(m_ClassifierEditor.getCapabilitiesFilter());
					}
				});

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(panel_1);
		panel_1.setLayout(new GridLayout(0, 2, 0, 0));
		btnStart.setToolTipText("Start evaluation");
		panel_1.add(btnStart);

		btnStart.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(Integer.parseInt(testField.getText()) + Integer.parseInt(firstTrainField.getText()) >100){
					JOptionPane.showMessageDialog(SlidingValidationPanel.this,"The sum of Training and Test sizes can't be more than 100%",
							"Error", JOptionPane.ERROR_MESSAGE);
				}else{
					//jonathan
					//for(int i= 10; i<=90; i++) {
					if(Integer.parseInt(stepField.getText()) > 0) {
						startClassifierJon();
					} else {
						startClassifier();
						//startClassifier();
					}
				}

			}
		});
		btnStop.setToolTipText("Stop evaluation");
		panel_1.add(btnStop);

		btnStop.setEnabled(false);

		btnStop.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stopClassifier();

			}
		});

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "History", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 2;
		pMenu.add(panel_4, gbc_panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_4.add(scrollPane_1, BorderLayout.CENTER);

		historyList = new JList();
		historyList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(historyList.getSelectedValue() != null)
					outArea.setText(historys.get(historyList.getSelectedValue().toString()).toString());
			}
		});

		scrollPane_1.setViewportView(historyList);

		JPanel pOut = new JPanel();
		pOut.setBackground(UIManager.getColor("ArrowButton.background"));
		GridBagConstraints gbc_pOut = new GridBagConstraints();
		gbc_pOut.insets = new Insets(0, 0, 5, 0);
		gbc_pOut.fill = GridBagConstraints.BOTH;
		gbc_pOut.gridx = 1;
		gbc_pOut.gridy = 0;
		pCenter.add(pOut, gbc_pOut);
		pOut.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		pOut.add(scrollPane);

		outArea = new JTextArea(20, 40);
		outArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		outArea.setEditable(false);
		outArea.setBorder(new LineBorder(new Color(0, 0, 0)));
		scrollPane.setViewportView(outArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Result output"));
		// m_History.setBorder(BorderFactory.createTitledBorder("Result list (right-click for options)"));
		m_ClassifierEditor.setClassType(Classifier.class);
		m_ClassifierEditor.setValue(ExplorerDefaults.getClassifier());
		m_ClassifierEditor.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				btnStart.setEnabled(true);
				// Check capabilities
				Capabilities currentFilter = m_ClassifierEditor.getCapabilitiesFilter();
				Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
				Capabilities currentSchemeCapabilities = null;
				if (classifier != null && currentFilter != null
						&& (classifier instanceof CapabilitiesHandler)) {
					currentSchemeCapabilities =
							((CapabilitiesHandler) classifier).getCapabilities();

					if (!currentSchemeCapabilities.supportsMaybe(currentFilter)
							&& !currentSchemeCapabilities.supports(currentFilter)) {
						btnStart.setEnabled(false);
					}
				}
				repaint();
			}
		});

	}
	
	void showPopUpInfoWindow(String tittle, String text, JButton btn){
		btn.setEnabled(false);
		JFrame frame = new JFrame(tittle);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        JTextArea textArea = new JTextArea(15, 35);
        textArea.setText(text);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(Font.getFont(Font.SANS_SERIF));
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroller);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                btn.setEnabled(true);
            }
        });
	}

	protected void updateCapabilitiesFilter(Capabilities filter) {
		Instances tempInst;
		Capabilities filterClass;
		if (filter == null) {
			m_ClassifierEditor.setCapabilitiesFilter(new Capabilities(null));
			return;
		}

		if (!ExplorerDefaults.getInitGenericObjectEditorFilter()) {
			tempInst = new Instances(m_Instances, 0);
		} else {
			tempInst = new Instances(m_Instances);
		}
		tempInst.setClassIndex(m_ClassCombo.getSelectedIndex());

		try {
			filterClass = Capabilities.forInstances(tempInst);
		} catch (Exception e) {
			filterClass = new Capabilities(null);
		}

		// set new filter
		m_ClassifierEditor.setCapabilitiesFilter(filterClass);

		// Check capabilities
		btnStart.setEnabled(true);
		Capabilities currentFilter = m_ClassifierEditor.getCapabilitiesFilter();
		Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
		Capabilities currentSchemeCapabilities = null;
		if (classifier != null && currentFilter != null
				&& (classifier instanceof CapabilitiesHandler)) {
			currentSchemeCapabilities =
					((CapabilitiesHandler) classifier).getCapabilities();

			if (!currentSchemeCapabilities.supportsMaybe(currentFilter)
					&& !currentSchemeCapabilities.supports(currentFilter)) {
				btnStart.setEnabled(false);
			}
		}
	}

	/**
	 * Starts running the currently configured classifier with the current
	 * settings. This is run in a separate thread, and will only start if there is
	 * no classifier already running. 
	 */
	protected void startClassifier() {
		if (m_RunThread == null) {
			synchronized (this) {
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
			}
			m_RunThread = new Thread() {
				@Override
				public void run() {

					if (m_LogPanel instanceof TaskLogger) {
						((TaskLogger) m_LogPanel).taskStarted();
					}
					m_ClassCombo.setEnabled(false);
					// Copy the current state of things
					m_LogPanel.statusMessage("Setting up...");
					CostMatrix costMatrix = null;
					Instances inst = new Instances(m_Instances);
					DataSource source = null;
					Instances userTestStructure = null;
					ClassifierErrorsPlotInstances plotInstances = null;

					// for timing
					long trainTimeStart = 0, trainTimeElapsed = 0;
					long testTimeStart = 0, testTimeElapsed = 0;

					
					int classIndex = m_ClassCombo.getSelectedIndex();
					inst.setClassIndex(classIndex);
					//System.out.println("Class: "+m_ClassCombo.getSelectedItem().toString());
					//inst.setClass(new Attribute(m_ClassCombo.getSelectedItem().toString()));
					Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
					Classifier template = null;
					try {
						template = AbstractClassifier.makeCopy(classifier);
					} catch (Exception ex) {
						m_LogPanel.logMessage("Problem copying classifier: " + ex.getMessage());
					}
					Classifier fullClassifier = null;
					StringBuffer outBuff = new StringBuffer();
					
					String name =
							(new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
					String cname = "";
					String cmd = "";
					//   Evaluation eval = null;
					try {		            
						cname = classifier.getClass().getName();
						if (cname.startsWith("weka.classifiers.")) {
							name += cname.substring("weka.classifiers.".length());
						} else {
							name += cname;
						}
						cmd = classifier.getClass().getName();
						if (classifier instanceof OptionHandler) {
							cmd +=
									" "
											+ Utils
											.joinOptions(((OptionHandler) classifier).getOptions());
						}
						// Output some header information
						m_LogPanel.logMessage("Started " + cname);
						m_LogPanel.logMessage("Command: " + cmd);
						
						outBuff.append("=== Run information ===\n\n");
						outBuff.append("Scheme:       " + cname);
						if (classifier instanceof OptionHandler) {
							String[] o = ((OptionHandler) classifier).getOptions();
							outBuff.append(" " + Utils.joinOptions(o));
						}
						outBuff.append("\n");
						outBuff.append("Relation:     " + inst.relationName() + '\n');
						outBuff.append("Instances:    " + inst.numInstances() + '\n');
						outBuff.append("Attributes:   " + inst.numAttributes() + '\n');
						if (inst.numAttributes() < 100) {
							for (int i = 0; i < inst.numAttributes(); i++) {
								outBuff.append("              " + inst.attribute(i).name()
										+ '\n');
							}
						} else {
							outBuff.append("              [100+ attributes, the list was omitted]\n");
						}

						outBuff.append("Test mode:    ");
						outBuff.append("Training Test Sliding Validation \n");
						historys.put(name, new StringBuffer(outBuff));
						historyList.setListData(  historys.keySet().toArray());
						outArea.setText(outBuff.toString());
						
						
						//Start the Training Test Sliding Validation Experiment
						int totalInstances = inst.numInstances(); 

						int test = Integer.parseInt(testField.getText());
						int firstTrain = Integer.parseInt(firstTrainField.getText());

						SlidingValidation slideEval = new SlidingValidation(inst);

						int windowTest = (int) (totalInstances * test / 100);
						int windowTrain = (int) (totalInstances * firstTrain / 100);
						boolean minimumValueToWindowTest = false;
						boolean minimumValueToWindowTrain = false;
						
//						System.out.println("totalInstances = " + totalInstances);
//						System.out.println("windowTrain = " + windowTrain);
//						System.out.println("windowTest = " + windowTest);
						if(windowTest <1){
							windowTest = 1;
							minimumValueToWindowTest = true;
						}
						if(windowTrain <1){
							windowTrain =1;
							minimumValueToWindowTrain = true;
						}
						int trainSizeInicio = 0;
						int window = (int) (windowTest);
						int totalModels = (totalInstances -windowTrain) / windowTest;
						int count =0;
						boolean isAccumulate = checkAccumulate.isSelected();

						outBuff.append("\n=== Summary ===\n");
						outBuff.append("\nModels:                        "+totalModels);
						outBuff.append("\nAccumulate training windows   "+ isAccumulate);
						if(isAccumulate){
							outBuff.append("\nFirst training window size:    "+windowTrain +" ("+firstTrain+"%)");
							//outBuff.append("\nTotal trained instances:       "+ (windowTest*(totalModels-1) + (windowTrain)));
						}
						else{
							outBuff.append("\nTraining window size:          "+windowTrain +" ("+firstTrain+"%)");
							//outBuff.append("\nTotal trained instances:       "+ windowTrain*totalModels);
						}
						if(windowTrain <= windowTest && !isAccumulate){
							outBuff.append("\nTotal trained instances:       "+ windowTrain*totalModels);
						}else{
							outBuff.append("\nTotal trained instances:       "+ (windowTest*(totalModels-1) + (windowTrain)));
						}
						
						if(minimumValueToWindowTrain){
							outBuff.append("\n*using the minimum value to window training size, as only "+firstTrain+"% of "+totalInstances+" means less than 1 instance");
						}
						outBuff.append("\nTest window size:              "+windowTest +" ("+test+"%)");
						outBuff.append("\nTotal tested instances:        "+(windowTest * totalModels));
						outBuff.append("\nTotal not tested instances:    "+(windowTrain+ ((totalInstances -windowTrain) % windowTest)) +" ("+ "First "+windowTrain+" instance(s) and last "+(totalInstances -windowTrain) % windowTest +")");
						if(minimumValueToWindowTest){
							outBuff.append("\n*using the minimum value to window test size, as only "+test+"% of "+totalInstances+" means less than 1 instance");
						}
						outBuff.append("\nTotal number of instances:     "+totalInstances);
						outBuff.append("\n");
						
						System.out.println("windowTrain = " + windowTrain);
						System.out.println("totalInstances = " + totalInstances);
						System.out.println("windowTest = " + windowTest);
						System.out.println("window = " + window);
						for(int w=windowTrain;w<=totalInstances-windowTest;w+=window){
							count++;
							m_LogPanel.statusMessage("Model "+count+" of "+totalModels);
							if(isAccumulate){
								windowTrain= w;
							}else{
								trainSizeInicio = w-windowTrain;
							}			
							
							//jonathan
							
							//Print on console the window's slides
//							printSlideOnConsole(count, trainSizeInicio, windowTrain, windowTest, w, totalInstances);
							Instances trainInstances = new Instances(inst, trainSizeInicio, windowTrain); 
							Instances testInstances = new Instances(inst, w, windowTest); 

							trainInstances.setClassIndex(classIndex); 
							testInstances.setClassIndex(classIndex);

							classifier.buildClassifier(trainInstances);
							weka.classifiers.evaluation.Evaluation eval = new weka.classifiers.evaluation.Evaluation(testInstances);
							eval.evaluateModel(classifier, testInstances);
							if (isNominal){
								slideEval.accumlateClassifyMetrics(eval.confusionMatrix());
							}else{
								slideEval.accumulateRegressionMetrics(testInstances, classifier, eval);
							}							

						}
			
						if (isNominal){
							//alterei o toClassifyMetricsString para receber o passo
							outBuff.append(slideEval.toClassifyMetricsString(totalModels,windowTest)+"\n");
							outBuff.append(slideEval.toClassDetailsString("=== Detailed Accuracy By Class (models' average) ===\n", totalModels));
							outBuff.append(slideEval.toMatrixString("=== Confusion Matrix ===\n"));

						}else{
							outBuff.append(slideEval.toRegressionMetricsString(totalModels));
						}



					} catch (Exception ex) {
						ex.printStackTrace();
						m_LogPanel.logMessage(ex.getMessage());
						JOptionPane.showMessageDialog(SlidingValidationPanel.this,
								"Problem evaluating classifier:\n" + ex.getMessage(),
								"Evaluate classifier", JOptionPane.ERROR_MESSAGE);
						m_LogPanel.statusMessage("Problem evaluating classifier");


					} finally {
						
						if (isInterrupted()) {
							m_LogPanel.logMessage("Interrupted " + cname);
							m_LogPanel.statusMessage("Interrupted");

						}else{
							historys.put(name, outBuff);
							outArea.setText(outBuff.toString());
							m_LogPanel.statusMessage("Finished");
						}
						repaint();

						synchronized (this) {
							btnStart.setEnabled(true);
							btnStop.setEnabled(false);
							m_RunThread = null;
						}
						if (m_LogPanel instanceof TaskLogger) {
							((TaskLogger) m_LogPanel).taskFinished();
						}
						m_ClassCombo.setEnabled(true);
					}
				}
			};
			m_RunThread.setPriority(Thread.MIN_PRIORITY);
			m_RunThread.start();
		}
	}

/**
	 * Starts running the currently configured classifier with the current
	 * settings. This is run in a separate thread, and will only start if there is
	 * no classifier already running. 
	 */
	//jonathan
	protected void startClassifierJon() {
		if (m_RunThread == null) {
			synchronized (this) {
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
			}
			m_RunThread = new Thread() {
				@Override
				public void run() {
				
					if (m_LogPanel instanceof TaskLogger) {
						((TaskLogger) m_LogPanel).taskStarted();
					}
					m_ClassCombo.setEnabled(false);
					// Copy the current state of things
					m_LogPanel.statusMessage("Setting up...");
					CostMatrix costMatrix = null;
					Instances inst = new Instances(m_Instances);
					DataSource source = null;
					Instances userTestStructure = null;
					ClassifierErrorsPlotInstances plotInstances = null;

					// for timing
					long trainTimeStart = 0, trainTimeElapsed = 0;
					long testTimeStart = 0, testTimeElapsed = 0;
				
					int classIndex = m_ClassCombo.getSelectedIndex();
					inst.setClassIndex(classIndex);
					//System.out.println("Class: "+m_ClassCombo.getSelectedItem().toString());
					//inst.setClass(new Attribute(m_ClassCombo.getSelectedItem().toString()));
					Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
					Classifier template = null;
					try {
						template = AbstractClassifier.makeCopy(classifier);
					} catch (Exception ex) {
						m_LogPanel.logMessage("Problem copying classifier: " + ex.getMessage());
					}
					Classifier fullClassifier = null;
					StringBuffer outBuff = new StringBuffer();
					
					String name =
							(new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
					String cname = "";
					String cmd = "";
					//   Evaluation eval = null;
					try {		            
						cname = classifier.getClass().getName();
						if (cname.startsWith("weka.classifiers.")) {
							name += cname.substring("weka.classifiers.".length());
						} else {
							name += cname;
						}
						cmd = classifier.getClass().getName();
						if (classifier instanceof OptionHandler) {
							cmd +=
									" "
											+ Utils
											.joinOptions(((OptionHandler) classifier).getOptions());
						}
						// Output some header information
						m_LogPanel.logMessage("Started " + cname);
						m_LogPanel.logMessage("Command: " + cmd);
						
						outBuff.append("=== Run information ===\n\n");
						outBuff.append("Scheme:       " + cname);
						if (classifier instanceof OptionHandler) {
							String[] o = ((OptionHandler) classifier).getOptions();
							outBuff.append(" " + Utils.joinOptions(o));
						}
						outBuff.append("\n");
						outBuff.append("Relation:     " + inst.relationName() + '\n');
						outBuff.append("Instances:    " + inst.numInstances() + '\n');
						outBuff.append("Attributes:   " + inst.numAttributes() + '\n');
						if (inst.numAttributes() < 100) {
							for (int i = 0; i < inst.numAttributes(); i++) {
								outBuff.append("              " + inst.attribute(i).name()
										+ '\n');
							}
						} else {
							outBuff.append("              [100+ attributes, the list was omitted]\n");
						}

						outBuff.append("Test mode:    ");
						outBuff.append("Training Test Sliding Validation \n");
						historys.put(name, new StringBuffer(outBuff));
						historyList.setListData(  historys.keySet().toArray());
						outArea.setText(outBuff.toString());

						//novo-teste
						int step = Integer.parseInt(stepField.getText());//jon
						int top = Integer.parseInt(topField.getText());//jon
						int firstTrainStart = Integer.parseInt(firstTrainField.getText());
						
						//for(int tr=10; tr<=90; tr++){
						for(int tr=firstTrainStart; tr<=top; tr=tr+step){
							//Start the Training Test Sliding Validation Experiment
							int totalInstances = inst.numInstances(); 

							int test = Integer.parseInt(testField.getText());
							//funcionando
							////int firstTrain = Integer.parseInt(firstTrainField.getText());
							int firstTrain = tr;	
							//fim funcionando
							SlidingValidation slideEval = new SlidingValidation(inst);

							int windowTest = (int) (totalInstances * test / 100);
							int windowTrain = (int) (totalInstances * firstTrain / 100);
							boolean minimumValueToWindowTest = false;
							boolean minimumValueToWindowTrain = false;
							if(windowTest <1){
								windowTest = 1;
								minimumValueToWindowTest = true;
							}
							if(windowTrain <1){
								windowTrain =1;
								minimumValueToWindowTrain = true;
							}
							int trainSizeInicio = 0;
							int window = (int) (windowTest);
							int totalModels = (totalInstances -windowTrain) / windowTest;
							int count =0;
							boolean isAccumulate = checkAccumulate.isSelected();				
							// --------- SUMMARY-------------
							outBuff.append("\n=== Summary ===\n");
							outBuff.append("\nModels:                        "+totalModels);
							outBuff.append("\nAccumulate training windows   "+ isAccumulate);
							if(isAccumulate){
								outBuff.append("\nFirst training window size:    "+windowTrain +" ("+firstTrain+"%)");
								//outBuff.append("\nTotal trained instances:       "+ (windowTest*(totalModels-1) + (windowTrain)));
							}
							else{
								outBuff.append("\nTraining window size:          "+windowTrain +" ("+firstTrain+"%)");
								//outBuff.append("\nTotal trained instances:       "+ windowTrain*totalModels);
							}
							if(windowTrain <= windowTest && !isAccumulate){
								outBuff.append("\nTotal trained instances:       "+ windowTrain*totalModels);
							}else{
								outBuff.append("\nTotal trained instances:       "+ (windowTest*(totalModels-1) + (windowTrain)));
							}
							
							if(minimumValueToWindowTrain){
								outBuff.append("\n*using the minimum value to window training size, as only "+firstTrain+"% of "+totalInstances+" means less than 1 instance");
							}
							outBuff.append("\nTest window size:              "+windowTest +" ("+test+"%)");
							outBuff.append("\nTotal tested instances:        "+(windowTest * totalModels));
							outBuff.append("\nTotal not tested instances:    "+(windowTrain+ ((totalInstances -windowTrain) % windowTest)) +" ("+ "First "+windowTrain+" instance(s) and last "+(totalInstances -windowTrain) % windowTest +")");
							if(minimumValueToWindowTest){
								outBuff.append("\n*using the minimum value to window test size, as only "+test+"% of "+totalInstances+" means less than 1 instance");
							}
							outBuff.append("\nTotal number of instances:     "+totalInstances);
							outBuff.append("\n");
						
							for(int w=windowTrain;w<=totalInstances-windowTest;w+=window){
								count++;
								m_LogPanel.statusMessage("Model "+count+" of "+totalModels);
								if(isAccumulate){
									windowTrain= w;
								}else{
									trainSizeInicio = w-windowTrain;
								}			
								//Print on console the window's slides
	//							printSlideOnConsole(count, trainSizeInicio, windowTrain, windowTest, w, totalInstances);
								Instances trainInstances = new Instances(inst, trainSizeInicio, windowTrain); 
								Instances testInstances = new Instances(inst, w, windowTest); 

								trainInstances.setClassIndex(classIndex); 
								testInstances.setClassIndex(classIndex);

								classifier.buildClassifier(trainInstances);
								weka.classifiers.evaluation.Evaluation eval = new weka.classifiers.evaluation.Evaluation(testInstances);
								eval.evaluateModel(classifier, testInstances);
								if (isNominal){
									slideEval.accumlateClassifyMetrics(eval.confusionMatrix());
								}else{
									slideEval.accumulateRegressionMetrics(testInstances, classifier, eval);
								}							
							}	
							if (isNominal){
								outBuff.append(slideEval.toClassifyMetricsString(totalModels,tr)+"\n");
								outBuff.append(slideEval.toClassDetailsString("=== Detailed Accuracy By Class (models' average) ===\n", totalModels));
								outBuff.append(slideEval.toMatrixString("=== Confusion Matrix ===\n"));

							}else{
								//jonathan - imprime resultados
								outBuff.append(slideEval.toRegressionMetricsStringJon(totalModels));
							}
							//-------------SUMMARY END
						}//jon
					} catch (Exception ex) {
						ex.printStackTrace();
						m_LogPanel.logMessage(ex.getMessage());
						JOptionPane.showMessageDialog(SlidingValidationPanel.this,
								"Problem evaluating classifier:\n" + ex.getMessage(),
								"Evaluate classifier", JOptionPane.ERROR_MESSAGE);
						m_LogPanel.statusMessage("Problem evaluating classifier");


					} finally {
						
						if (isInterrupted()) {
							m_LogPanel.logMessage("Interrupted " + cname);
							m_LogPanel.statusMessage("Interrupted");

						}else{
							historys.put(name, outBuff);
							outArea.setText(outBuff.toString());
							m_LogPanel.statusMessage("Finished");
						}
						repaint();

						synchronized (this) {
							btnStart.setEnabled(true);
							btnStop.setEnabled(false);
							m_RunThread = null;
						}
						if (m_LogPanel instanceof TaskLogger) {
							((TaskLogger) m_LogPanel).taskFinished();
						}
						m_ClassCombo.setEnabled(true);
					}
				
				}
			};
			m_RunThread.setPriority(Thread.MIN_PRIORITY);
			m_RunThread.start();
		}
	}

	protected void printSlideOnConsole(int count, int trainSizeInicio, int windowTrain, int windowTest, int w, int totalInstances) {
		String isAccumulated = checkAccumulate.isSelected() ? "(Accumulating)":"";
		System.out.println("Model "+count +" " + isAccumulated );
		System.out.println("InicioTreino "+trainSizeInicio+" | InstanciasDeTreino "+windowTrain+" | FimTreino "+(trainSizeInicio+windowTrain-1));
		System.out.println("InicioDoTeste "+w+" | InstanciasDeTeste "+windowTest+" |FimTeste "+(w+windowTest-1));
		for (int t = 0; t< totalInstances;t++){
			if(t >= trainSizeInicio && t <=(trainSizeInicio+windowTrain-1)){
				System.out.print("x");
			}
			else{
				System.out.print("=");
			}
		}
		System.out.println();
		for (int t = 0; t< totalInstances;t++){
			if(t >= w && t <=(w+windowTest-1)){
				System.out.print("x");
			}
			else{
				System.out.print("=");
			}
		}
		System.out.println("\n");


	}

	/**
	 * Stops the currently running classifier (if any).
	 */
	@SuppressWarnings("deprecation")
	protected void stopClassifier() {

		if (m_RunThread != null) {
			m_RunThread.interrupt();

			// This is deprecated (and theoretically the interrupt should do).
			m_RunThread.stop();
		}
	}

	/** Sets the Explorer to use as parent frame */ 
	public void setExplorer(Explorer parent) { 
		m_Explorer = parent; 
	}

	/** returns the parent Explorer frame */ 
	public Explorer getExplorer() { 
		return m_Explorer; 
	}

	/** Returns the title for the tab in the Explorer */ 
	public String getTabTitle() { 
		return "Training Test Sliding Validation"; // Name of the tab at Explorer, e.g. Classify 
	}

	/** Returns the tooltip for the tab in the Explorer */ 
	public String getTabTitleToolTip() { 
		return "Classify instances with the training test sliding validation method"; // A tooltip for the tab
	}

	/**
	 * Tells the panel to use a new set of instances.
	 * 
	 * @param inst a set of Instances
	 */
	@Override
	public void setInstances(Instances inst) {
		m_Instances = inst;

		String[] attribNames = new String[m_Instances.numAttributes()];
		for (int i = 0; i < attribNames.length; i++) {
			String type =
					"(" + Attribute.typeToStringShort(m_Instances.attribute(i)) + ") ";
			attribNames[i] = type + m_Instances.attribute(i).name();
		}
		m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
		if (attribNames.length > 0) {
			if (inst.classIndex() == -1) {
				m_ClassCombo.setSelectedIndex(attribNames.length - 1);
			} else {
				m_ClassCombo.setSelectedIndex(inst.classIndex());
			}
			m_ClassCombo.setEnabled(true);
			updateCapabilitiesFilter(m_ClassifierEditor.getCapabilitiesFilter());			
			btnStart.setEnabled(m_RunThread == null);
			btnStop.setEnabled(m_RunThread != null);
		} else {
			btnStart.setEnabled(false);
			btnStop.setEnabled(false);
		}
	}

	// main for tests purpose
	public static void main(String[] args) {

		try {
			final javax.swing.JFrame jf =
					new javax.swing.JFrame("Weka Explorer: Classifier");
			jf.getContentPane().setLayout(new BorderLayout());
			final SlidingValidationPanel bp = new SlidingValidationPanel();
			jf.getContentPane().add(bp, BorderLayout.CENTER);
			jf.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent e) {
					jf.dispose();
					System.exit(0);
				}
			});
			jf.pack();
			jf.setSize(800, 600);
			jf.setVisible(true);
			if(true){
				FileReader f = new FileReader("/home/manoel/Dropbox/Time/data/all-attributes/experiment/new-discrete-p/testing-p/akka-new-discrete-p.arff");
				java.io.Reader r =
						new java.io.BufferedReader(f);
				Instances i = new Instances(r);
				bp.setInstances(i);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}

	@Override
	public void setLog(Logger newLog) {
		m_LogPanel = (LogPanel) newLog;

	}

	@Override
	public void capabilitiesFilterChanged(CapabilitiesFilterChangeEvent e) {
		System.out.println("oi");
		
	}


}
