package weka.gui.explorer;

import java.awt.BorderLayout;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SlidingValidation;
import weka.core.Attribute;
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
import weka.gui.explorer.Explorer;
import weka.gui.explorer.Explorer.CapabilitiesFilterChangeEvent;
import weka.gui.explorer.Explorer.CapabilitiesFilterChangeListener;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.explorer.ExplorerDefaults;

import javax.swing.JLabel;
import javax.swing.BoxLayout;

import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;

import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

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
	protected Instances instConfig;

	/** Lets the user select the class column. */
	@SuppressWarnings("rawtypes")
	protected JComboBox m_ClassCombo = new JComboBox(); //loaded at setInstances method
	@SuppressWarnings("rawtypes")
	protected JComboBox I_ClassCombo = new JComboBox(); //loaded at setInstances method
	protected boolean isNominal;

	protected JFormattedTextField firstTrainField;
	protected JFormattedTextField testField;
	protected JFormattedTextField firstTrainFieldInstances;
	protected JFormattedTextField testFieldInstances;
	protected String locateDate;
	protected JFormattedTextField trainingDateStart;
	protected JFormattedTextField trainingDateEnd;
	protected JFormattedTextField testDateStart;
	protected JFormattedTextField testDateEnd;
	protected JTextArea outArea;
	protected int NumberInstances;
	//private HashMap<String, Integer> classes = new HashMap<>();

	protected JRadioButton btnPercent = new JRadioButton("Percent");
	protected JRadioButton btnInstances = new JRadioButton("Instances"); 
	protected JRadioButton btnDate = new JRadioButton("Date");

	protected String[] datesFormat = { "dd/MM/yyyy",  "dd-MM-yyyy", "dd/MM/yy", "dd-mm-yy", "dd/MM/yyyy HH:mm:ss",   "dd-MM-yyyy HH:mm:ss", "dd/MM/yyyy HH:mm",
			"dd-MM-yyyy HH:mm",};
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected JComboBox dates = new JComboBox(datesFormat);
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

	protected JLabel labelFirstTrain;
	protected JLabel labelTest;
	protected JLabel labelFirstTrainInstances;
	protected JLabel labelTestInstances;
	protected JLabel numberInstanceLabel;
	protected JLabel labelNumberOfModels;
	protected JLabel labelLocateDate;
	protected JLabel labelTypeDate;
	protected JLabel labeldatepickertestStart;
	protected JLabel labeldatepickertestEnd;
	protected JLabel labeldatepickertraingStart;
	protected JLabel labeldatepickertraingEnd;
	protected JCheckBox checkAccumulate;
	protected int btnStartIsInstance  = 1;  

	@SuppressWarnings("rawtypes")
	private JList historyList;
	private LinkedHashMap<String, StringBuffer> historys = new LinkedHashMap<>();

	protected String dateSelected = "dd/mm/yy";
	protected int trainInstance =1;
	protected int testInstance =1;
	@SuppressWarnings({ "rawtypes", "deprecation" })
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

		NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(1);
		formatter.setMaximum(100);
		formatter.setAllowsInvalid(true);
		// If you want the value to be committed on each keystroke instead of focus lost
		formatter.setCommitsOnValidEdit(true);

		NumberFormatter formatterInstances = new NumberFormatter(NumberFormat.getInstance());
		formatterInstances.setValueClass(Integer.class);
		formatterInstances.setMinimum(1);
		//formatterInstances.setMaximum(NumberInstances);
		formatterInstances.setAllowsInvalid(true);
		// If you want the value to be committed on each keystroke instead of focus lost
		formatterInstances.setCommitsOnValidEdit(true);

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
		String numberInstances =  Integer.toString(NumberInstances);
		numberInstanceLabel = new JLabel("Total of instances: "+numberInstances);

		panel_3.add(numberInstanceLabel);

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(btnPercent);
		radioGroup.add(btnInstances);		
		radioGroup.add(btnDate);	

		JPanel panel_laber = new JPanel();
		GridBagConstraints gbc_panel_radio = new GridBagConstraints();
		gbc_panel_radio.insets = new Insets(0, 0, 5, 0);
		gbc_panel_radio.fill = GridBagConstraints.BOTH;
		gbc_panel_radio.gridx = 0;
		gbc_panel_radio.gridy = 1;
		pLabels.add(panel_laber, gbc_panel_radio);
		gbc_panel_radio.insets = new Insets(0, 0, 5, 5);
		gbc_panel_radio.fill = GridBagConstraints.BOTH;
		gbc_panel_radio.gridx = 0;
		gbc_panel_radio.gridy = 0;
		GridBagLayout gbl_panel_radio = new GridBagLayout();
		gbl_panel_radio.columnWidths = new int[] {30, 30, 48, 0};
		gbl_panel_radio.rowHeights = new int[] {20, 20};
		gbl_panel_radio.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_panel_radio.rowWeights = new double[]{0.0, 0.0};
		panel_laber.setLayout(gbl_panel_radio);

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 1;
		gbc_panel_2.gridy = 1;
		pLabels.add(panel_2, gbc_panel_2);
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] {30, 30, 48};
		gbl_panel_2.rowHeights = new int[] {20, 20};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0};
		panel_2.setLayout(gbl_panel_2);

		GridBagConstraints gbc_panel_radiol = new GridBagConstraints();
		gbc_panel_radiol.insets = new Insets(0, 0, 5, 0);
		gbc_panel_radiol.fill = GridBagConstraints.BOTH;
		gbc_panel_radiol.gridx = 0;
		gbc_panel_radiol.gridy = 0;
		panel_laber.add(btnPercent, gbc_panel_radiol);
		GridBagConstraints gbc_panel_radio_2 = new GridBagConstraints();
		gbc_panel_radio_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_radio_2.fill = GridBagConstraints.BOTH;
		gbc_panel_radio_2.gridx = 0;
		gbc_panel_radio_2.gridy = 1;
		panel_laber.add(btnInstances, gbc_panel_radio_2);
		GridBagConstraints gbc_panel_radio_3 = new GridBagConstraints();
		gbc_panel_radio_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_radio_3.fill = GridBagConstraints.BOTH;
		gbc_panel_radio_3.gridx = 0;
		gbc_panel_radio_3.gridy = 2;
		panel_laber.add(btnDate, gbc_panel_radio_3);

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

		labelFirstTrainInstances = new JLabel("Training window size with instances");
		labelFirstTrainInstances.setToolTipText("the number of instances used to train the classifier");
		labelFirstTrainInstances.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelFirstTrainInstances = new GridBagConstraints();
		gbc_labelFirstTrainInstances.fill = GridBagConstraints.BOTH;
		gbc_labelFirstTrainInstances.insets = new Insets(0, 0, 5, 5);
		gbc_labelFirstTrainInstances.gridx = 0;
		gbc_labelFirstTrainInstances.gridy = 2;
		panel_2.add(labelFirstTrainInstances, gbc_labelFirstTrainInstances);

		firstTrainFieldInstances = new JFormattedTextField(formatterInstances);
		firstTrainFieldInstances.setToolTipText("the number of instances used to train the classifier");
		firstTrainFieldInstances.setText("1");
		firstTrainFieldInstances.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased (java.awt.event.KeyEvent evt) {
				String test = firstTrainFieldInstances.getText();
				if(test == null) {
					test = "1";
				}else {
					if(test =="0") {
						test = "1";
					}
				}
				trainInstance = Integer.parseInt(test);
				Instances inst = new Instances(m_Instances);
				int label = (inst.size() - trainInstance) / testInstance;
				labelNumberOfModels.setText("will be created " +label+" models"); 
			}
		});
		GridBagConstraints gbc_firstTrainFieldInstances = new GridBagConstraints();
		gbc_firstTrainFieldInstances.fill = GridBagConstraints.BOTH;
		gbc_firstTrainFieldInstances.insets = new Insets(0, 0, 5, 5);
		gbc_firstTrainFieldInstances.gridx = 2;
		gbc_firstTrainFieldInstances.gridy = 2;
		panel_2.add(firstTrainFieldInstances, gbc_firstTrainFieldInstances);
		firstTrainFieldInstances.setColumns(10);

		labelTestInstances = new JLabel("Test window size with instances");
		labelTestInstances.setToolTipText("the number of instances used to test the classifier");
		labelTestInstances.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTestInstances = new GridBagConstraints();
		gbc_labelTestInstances.fill = GridBagConstraints.BOTH;
		gbc_labelTestInstances.insets = new Insets(0, 0, 0, 5);
		gbc_labelTestInstances.gridx = 0;
		gbc_labelTestInstances.gridy = 3;
		panel_2.add(labelTestInstances, gbc_labelTestInstances);

		testFieldInstances = new JFormattedTextField(formatterInstances);
		testFieldInstances.setToolTipText("the number of instances used to test the classifier");
		testFieldInstances.setText("1");
		testFieldInstances.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased (java.awt.event.KeyEvent evt) {
				String test = testFieldInstances.getText();
				if(test == null) {
					test = "1";
				}else {
					if(test == "0") {
						test = "1";
					}
				}
				testInstance = Integer.parseInt(test);
				Instances inst = new Instances(m_Instances);
				int label = (inst.size() - trainInstance) / testInstance;
				labelNumberOfModels.setText("will be created " +label+" models");

			}
		});
		GridBagConstraints gbc_testFieldInstances = new GridBagConstraints();
		gbc_testFieldInstances.insets = new Insets(0, 0, 0, 5);
		gbc_testFieldInstances.fill = GridBagConstraints.BOTH;
		gbc_testFieldInstances.gridx = 2;
		gbc_testFieldInstances.gridy = 3;
		panel_2.add(testFieldInstances, gbc_testFieldInstances);
		
		JPanel panel_2_date = new JPanel();
		GridBagConstraints gbc_panel_2_date = new GridBagConstraints();
		gbc_panel_2_date.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2_date.fill = GridBagConstraints.BOTH;
		gbc_panel_2_date.gridx = 1;
		gbc_panel_2_date.gridy = 1;
		pLabels.add(panel_2_date, gbc_panel_2_date);
		gbc_panel_2_date.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2_date.fill = GridBagConstraints.BOTH;
		gbc_panel_2_date.gridx = 0;
		gbc_panel_2_date.gridy = 0;
		GridBagLayout gbl_panel_2_date = new GridBagLayout();
		gbl_panel_2_date.columnWidths = new int[] {30, 30, 48};
		gbl_panel_2_date.rowHeights = new int[] {};
		gbl_panel_2_date.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_panel_2_date.rowWeights = new double[]{0.0, 0.0};
		panel_2_date.setLayout(gbl_panel_2_date);

		labelLocateDate = new JLabel("Attribute name");
		labelLocateDate.setToolTipText("the name of the bd attribute containing the dates");
		labelLocateDate.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelLocateDate_date = new GridBagConstraints();
		gbc_labelLocateDate_date.fill = GridBagConstraints.BOTH;
		gbc_labelLocateDate_date.insets = new Insets(0, 0, 5, 5);
		gbc_labelLocateDate_date.gridx = 0;
		gbc_labelLocateDate_date.gridy = 0;
		panel_2_date.add(labelLocateDate, gbc_labelLocateDate_date);

		I_ClassCombo.setToolTipText("The name of attribute contains date");
		GridBagConstraints gbc_locateDate_date = new GridBagConstraints();
		gbc_locateDate_date.fill = GridBagConstraints.BOTH;
		gbc_locateDate_date.insets = new Insets(0, 0, 5, 5);
		gbc_locateDate_date.gridx = 1;
		gbc_locateDate_date.gridy = 0;
		gbc_locateDate_date.gridwidth = 2;
		panel_2_date.add(I_ClassCombo, gbc_locateDate_date);
		I_ClassCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = I_ClassCombo.getSelectedIndex();
				if (selected != -1) {
					locateDate = m_Instances.attribute(selected).name();
					//m_OutputPerClassBut.setEnabled(isNominal);
					//m_OutputConfusionBut.setEnabled(isNominal);
					System.out.println(locateDate);
				}
			}
		});
		//I_ClassCombo.setColumns(10);

		labelTypeDate = new JLabel("Format date");
		labelTypeDate.setToolTipText("the format of database dates");
		labelTypeDate.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTypeDate = new GridBagConstraints();
		gbc_labelTypeDate.fill = GridBagConstraints.BOTH;
		gbc_labelTypeDate.insets = new Insets(0, 0, 5, 5);
		gbc_labelTypeDate.gridx = 0;
		gbc_labelTypeDate.gridy = 1;
		panel_2_date.add(labelTypeDate, gbc_labelTypeDate);

		dates = new JComboBox<>(datesFormat);
		GridBagConstraints gbc_typeDateCombo = new GridBagConstraints();
		gbc_typeDateCombo.fill = GridBagConstraints.BOTH;
		gbc_typeDateCombo.insets = new Insets(0, 0, 5, 5);
		gbc_typeDateCombo.gridx = 1;
		gbc_typeDateCombo.gridy = 1;
		dates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dateSelected = dates.getSelectedItem().toString();
				String dateMasked = dateSelected;
				String[] nulos = {"d", "M", "y", "H", "m", "s"};
				
				for (int i = 0; i < nulos.length; i++) {
				dateMasked = dateMasked.replaceAll(nulos[i], "#");
				}
				
				try{  
					   trainingDateEnd.setFormatterFactory(
					            new DefaultFormatterFactory(
					                    new MaskFormatter(dateMasked)));  
					   trainingDateEnd.setValue(null);
					   trainingDateStart.setFormatterFactory(
					            new DefaultFormatterFactory(
					                    new MaskFormatter(dateMasked)));  
					   trainingDateStart.setValue(null);
					   testDateEnd.setFormatterFactory(
					            new DefaultFormatterFactory(
					                    new MaskFormatter(dateMasked)));
					   testDateEnd.setValue(null);
					   
					}  
					   catch (Exception eee){ 
						   eee.printStackTrace();
					   }
					}
			});
	
		panel_2_date.add(dates, gbc_typeDateCombo);

		labeldatepickertraingStart = new JLabel("Date first training start");
		labeldatepickertraingStart.setToolTipText("the starting date of the first training model");
		labeldatepickertraingStart.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_daterpicker = new GridBagConstraints();
		gbc_daterpicker.fill = GridBagConstraints.BOTH;
		gbc_daterpicker.insets = new Insets(0, 0, 5, 5);
		gbc_daterpicker.gridx = 0;
		gbc_daterpicker.gridy = 2;
		panel_2_date.add(labeldatepickertraingStart, gbc_daterpicker);
			
		try{  
			javax.swing.text.MaskFormatter data= new javax.swing.text.MaskFormatter("##/##/####");  
			trainingDateStart = new javax.swing.JFormattedTextField(data);  
		}  
		catch (Exception eee){ 
		}
		trainingDateStart.setToolTipText("The first start date training");
		trainingDateStart.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTrainingDateStart = new GridBagConstraints();
		gbc_labelTrainingDateStart.fill = GridBagConstraints.BOTH;
		gbc_labelTrainingDateStart.insets = new Insets(0, 0, 5, 5);
		gbc_labelTrainingDateStart.gridx = 1;
		gbc_labelTrainingDateStart.gridy = 2;
		gbc_labelTrainingDateStart.gridwidth = 2;
		panel_2_date.add(trainingDateStart, gbc_labelTrainingDateStart);

		/*UtilDateModel model = new UtilDateModel();
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		GridBagConstraints gbc_datepicker = new GridBagConstraints();
		gbc_datepicker.fill = GridBagConstraints.BOTH;
		gbc_datepicker.insets = new Insets(0, 0, 5, 5);
		gbc_datepicker.gridx = 1;
		gbc_datepicker.gridy = 2;
		gbc_datepicker.gridwidth = 2;
		panel_2_date.add(datePicker, gbc_datepicker);*/

		labeldatepickertraingEnd = new JLabel("Date first training end ");
		labeldatepickertraingEnd.setToolTipText("the ending date of the first training model");
		labeldatepickertraingEnd.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_daterpicker_end = new GridBagConstraints();
		gbc_daterpicker_end.fill = GridBagConstraints.BOTH;
		gbc_daterpicker_end.insets = new Insets(0, 0, 5, 5);
		gbc_daterpicker_end.gridx = 0;
		gbc_daterpicker_end.gridy = 3;
		panel_2_date.add(labeldatepickertraingEnd, gbc_daterpicker_end);

		try{  
			javax.swing.text.MaskFormatter data= new javax.swing.text.MaskFormatter("##/##/####");  
			trainingDateEnd = new javax.swing.JFormattedTextField(data);  
		}  
		catch (Exception eee){ 
		}
		trainingDateEnd.setToolTipText("The end date traing");
		trainingDateEnd.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTrainingDateEnd = new GridBagConstraints();
		gbc_labelTrainingDateEnd.fill = GridBagConstraints.BOTH;
		gbc_labelTrainingDateEnd.insets = new Insets(0, 0, 5, 5);
		gbc_labelTrainingDateEnd.gridx = 1;
		gbc_labelTrainingDateEnd.gridy = 3;
		gbc_labelTrainingDateEnd.gridwidth = 2;
		panel_2_date.add(trainingDateEnd, gbc_labelTrainingDateEnd);

		/*labeldatepickertestStart = new JLabel("Date test start");
		labeldatepickertestStart.setToolTipText("the name of the bd instance containing the dates");
		labeldatepickertestStart.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_daterpicker_testeStart = new GridBagConstraints();
		gbc_daterpicker_testeStart.fill = GridBagConstraints.BOTH;
		gbc_daterpicker_testeStart.insets = new Insets(0, 0, 5, 5);
		gbc_daterpicker_testeStart.gridx = 0;
		gbc_daterpicker_testeStart.gridy = 4;
		panel_2_date.add(labeldatepickertestStart, gbc_daterpicker_testeStart);

		testDateStart = new JFormattedTextField();
		testDateStart.setToolTipText("The start traing date");
		testDateStart.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTestDateStart = new GridBagConstraints();
		gbc_labelTestDateStart.fill = GridBagConstraints.BOTH;
		gbc_labelTestDateStart.insets = new Insets(0, 0, 5, 5);
		gbc_labelTestDateStart.gridx = 1;
		gbc_labelTestDateStart.gridy = 4;
		gbc_labelTestDateStart.gridwidth = 2;
		panel_2_date.add(testDateStart, gbc_labelTestDateStart);*/

		labeldatepickertestEnd = new JLabel("Test limit date");
		labeldatepickertestEnd.setToolTipText("the limite date of the first test model");
		labeldatepickertestEnd.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_daterpickertestEnd = new GridBagConstraints();
		gbc_daterpickertestEnd.fill = GridBagConstraints.BOTH;
		gbc_daterpickertestEnd.insets = new Insets(0, 0, 5, 5);
		gbc_daterpickertestEnd.gridx = 0;
		gbc_daterpickertestEnd.gridy = 5;
		panel_2_date.add(labeldatepickertestEnd, gbc_daterpickertestEnd);

		try{  
			javax.swing.text.MaskFormatter data= new javax.swing.text.MaskFormatter("##/##/####");  
			testDateEnd = new javax.swing.JFormattedTextField(data);  
		}  
		catch (Exception eee){ 
		}
		testDateEnd.setToolTipText("The end test date");
		testDateEnd.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_labelTestDateEnd = new GridBagConstraints();
		gbc_labelTestDateEnd.fill = GridBagConstraints.BOTH;
		gbc_labelTestDateEnd.insets = new Insets(0, 0, 5, 5);
		gbc_labelTestDateEnd.gridx = 1;
		gbc_labelTestDateEnd.gridy = 5;
		gbc_labelTestDateEnd.gridwidth = 2;
		panel_2_date.add(testDateEnd, gbc_labelTestDateEnd);

		//panel_2.setVisible(false);
		btnPercent.setSelected(true);
		firstTrainField.setEnabled(true);
		testField.setEnabled(true);
		label.setEnabled(true);
		labelFirstTrain.setEnabled(true);
		label_1.setEnabled(true);
		labelTest.setEnabled(true);
		firstTrainFieldInstances.setEnabled(false);
		testFieldInstances.setEnabled(false);
		labelTestInstances.setEnabled(false);
		labelFirstTrainInstances.setEnabled(false);

		panel_2_date.hide();

		btnPercent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				checkAccumulate.setSelected(false);
				btnStartIsInstance = 1;
				// enable radion buttons.
				firstTrainField.setEnabled(true);
				testField.setEnabled(true);
				label.setEnabled(true);
				labelFirstTrain.setEnabled(true);
				label_1.setEnabled(true);
				labelTest.setEnabled(true);
				firstTrainFieldInstances.setEnabled(false);
				testFieldInstances.setEnabled(false);
				labelTestInstances.setEnabled(false);
				labelFirstTrainInstances.setEnabled(false);

				labelNumberOfModels.hide();
				panel_2_date.hide();
				panel_2.setVisible(true);

			}
		});

		btnInstances.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				checkAccumulate.setSelected(true);
				btnStartIsInstance = 2;
				// enable radion buttons.
				firstTrainField.setEnabled(false);
				testField.setEnabled(false);
				label.setEnabled(false);
				labelFirstTrain.setEnabled(false);
				label_1.setEnabled(false);
				labelTest.setEnabled(false);
				firstTrainFieldInstances.setEnabled(true);
				testFieldInstances.setEnabled(true);
				labelTestInstances.setEnabled(true);
				labelFirstTrainInstances.setEnabled(true);

				panel_2_date.hide();
				panel_2.setVisible(true);

				labelNumberOfModels.setVisible(true);
			}
		});

		btnDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				btnStartIsInstance = 3;
				// enable radion buttons.
				panel_2_date.setVisible(true);
				panel_2.hide();

				labelNumberOfModels.hide();
			}
		});

		checkAccumulate = new JCheckBox("Accumulate the training window");
		GridBagConstraints gbc_checkAccumulate = new GridBagConstraints();
		gbc_checkAccumulate.insets = new Insets(0, 0, 5, 100);
		gbc_checkAccumulate.gridx = 0;
		gbc_checkAccumulate.gridy = 2;
		pLabels.add(checkAccumulate, gbc_checkAccumulate);
		checkAccumulate.setToolTipText("Accumulate the training window?");
		checkAccumulate.setFont(new Font("Tahoma", Font.PLAIN, 11));

		labelNumberOfModels = new JLabel("Total number of models"); 
		labelNumberOfModels.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_numberOfModels = new GridBagConstraints();
		gbc_numberOfModels.insets = new Insets(0, 0, 5, 0);
		gbc_numberOfModels.gridx = 1;
		gbc_numberOfModels.gridy = 2;
		pLabels.add(labelNumberOfModels, gbc_numberOfModels);	
		labelNumberOfModels.hide();

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
		gbl_panel_5.columnWidths = new int[] {210, 0};
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
				if(btnStartIsInstance == 1) {
					if(Integer.parseInt(testField.getText()) + Integer.parseInt(firstTrainField.getText()) >100){
						JOptionPane.showMessageDialog(SlidingValidationPanel.this,"The sum of Training and Test sizes can't be more than 100%",
								"Error", JOptionPane.ERROR_MESSAGE);
					}else {
						startClassifier();
					}
				}else if(btnStartIsInstance == 2) {
					String test = testFieldInstances.getText().replace( "." , "");
					String train = firstTrainFieldInstances.getText().replace( "." , "");
					if(Integer.parseInt(test) + Integer.parseInt(train) >NumberInstances) {
						JOptionPane.showMessageDialog(SlidingValidationPanel.this,"The sum of Training and Test sizes can't be more than "+ NumberInstances,
								"Error", JOptionPane.ERROR_MESSAGE);
					}else {
						startClassifierInstances();
					}
				}else if(btnStartIsInstance == 3){
					Instances inst = new Instances(m_Instances);
					String nameInstance = locateDate;
					Attribute dateAttributes = inst.attribute(nameInstance);
					SimpleDateFormat formatter = new SimpleDateFormat(dateSelected);
					
					if(dateAttributes == null) {
						JOptionPane.showMessageDialog(SlidingValidationPanel.this,"ERROR in the field: Attribute name. No attributes with this name were found in the database",
								"Error", JOptionPane.ERROR_MESSAGE);
					}else if(trainingDateStart.getText().equals("")||trainingDateEnd.getText().equals("")||testDateEnd.getText().equals("")){
							JOptionPane.showMessageDialog(SlidingValidationPanel.this,"ERROR in the fields dates. All date fields date training start, date training end, date test end must be complete",
									"Error", JOptionPane.ERROR_MESSAGE);
						}else{
							try{
								formatter.parse(trainingDateStart.getText()); 
								formatter.parse(trainingDateEnd.getText());
								formatter.parse(testDateEnd.getText());
								startClassifierDate();
							} catch(Exception ife){
								JOptionPane.showMessageDialog(SlidingValidationPanel.this,"\r\n" + 
									"Some of the dates entered do not match the selected format",
									"Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					//Limitar a entrada para valores que estão dentro da faixa de tempo do bd
						/*try {
							Date lastDate = formatter.parse(inst.lastInstance().stringValue(dateAttributes));
							Date firstDate =  formatter.parse(inst.firstInstance().stringValue(dateAttributes));
							if(formatter.parse(trainingDateStart.getText()).before(firstDate)) {
								JOptionPane.showMessageDialog(SlidingValidationPanel.this,"ERROR in the field date first training start. The date is less than the first date of the bd",
										"Error", JOptionPane.ERROR_MESSAGE);
							}else {
								if(formatter.parse(testDateEnd.getText()).before(lastDate)) {
									JOptionPane.showMessageDialog(SlidingValidationPanel.this,"ERROR in the field date test end. The date is less than the last date of the bd",
											"Error", JOptionPane.ERROR_MESSAGE);
								}
							}
						} catch (ParseException e1) {
							e1.printStackTrace();
						}*/
						
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
				@SuppressWarnings("unchecked")
				@Override
				public void run() {

					if (m_LogPanel instanceof TaskLogger) {
						((TaskLogger) m_LogPanel).taskStarted();
					}
					m_ClassCombo.setEnabled(false);
					// Copy the current state of things
					m_LogPanel.statusMessage("Setting up...");
					Instances inst = new Instances(m_Instances);
					int classIndex = m_ClassCombo.getSelectedIndex();
					inst.setClassIndex(classIndex);
					//System.out.println("Class: "+m_ClassCombo.getSelectedItem().toString());
					//inst.setClass(new Attribute(m_ClassCombo.getSelectedItem().toString()));
					Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
					try {
						AbstractClassifier.makeCopy(classifier);
					} catch (Exception ex) {
						m_LogPanel.logMessage("Problem copying classifier: " + ex.getMessage());
					}
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
							outBuff.append(slideEval.toClassifyMetricsString(totalModels)+"\n");
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


	protected void startClassifierInstances() {
		if (m_RunThread == null) {
			synchronized (this) {
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
			}
			m_RunThread = new Thread() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {

					if (m_LogPanel instanceof TaskLogger) {
						((TaskLogger) m_LogPanel).taskStarted();
					}
					m_ClassCombo.setEnabled(false);
					// Copy the current state of things
					m_LogPanel.statusMessage("Setting up...");
					Instances inst = new Instances(m_Instances);
					int classIndex = m_ClassCombo.getSelectedIndex();
					inst.setClassIndex(classIndex);
					//System.out.println("Class: "+m_ClassCombo.getSelectedItem().toString());
					//inst.setClass(new Attribute(m_ClassCombo.getSelectedItem().toString()));
					Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
					try {
						AbstractClassifier.makeCopy(classifier);
					} catch (Exception ex) {
						m_LogPanel.logMessage("Problem copying classifier: " + ex.getMessage());
					}
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
						String testString = testFieldInstances.getText().replace( "." , "");
						String trainString = firstTrainFieldInstances.getText().replace( "." , "");
						
						int test = Integer.parseInt(testString);
						int firstTrain = Integer.parseInt(trainString);

						SlidingValidation slideEval = new SlidingValidation(inst);

						int windowTest = test;
						int windowTrain = firstTrain;
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

						outBuff.append("\n=== Summary ===\n");
						outBuff.append("\nModels:                        "+totalModels);
						outBuff.append("\nAccumulate training windows   "+ isAccumulate);
						if(isAccumulate){
							outBuff.append("\nFirst training window size:    "+windowTrain );
							//outBuff.append("\nTotal trained instances:       "+ (windowTest*(totalModels-1) + (windowTrain)));
						}
						else{
							outBuff.append("\nTraining window size:          "+windowTrain );
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
						outBuff.append("\nTest window size:              "+windowTest );
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
							outBuff.append(slideEval.toClassifyMetricsString(totalModels)+"\n");
							outBuff.append(slideEval.toClassDetailsString("=== Detailed Accuracy By Class (models' average) ===\n", totalModels));
							outBuff.append(slideEval.toMatrixString("=== Confusion Matrix ===\n"));

						}else{
							outBuff.append(slideEval.toRegressionMetricsString(totalModels));
						}



					} catch (Exception ex) {
						ex.printStackTrace();
						m_LogPanel.logMessage(ex.getMessage());
						JOptionPane.showMessageDialog(SlidingValidationPanel.this,
								"Problem ing classifier:\n" + ex.getMessage(),
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

	protected void startClassifierDate() {
		Instances inst = new Instances(m_Instances);
		String nameInstance = locateDate;
		Attribute dateAttributes = inst.attribute(nameInstance);
		String a =inst.lastInstance().stringValue(dateAttributes);
		SimpleDateFormat formatter = new SimpleDateFormat(dateSelected);
		boolean error = false;
		
		try {
			formatter.parse(a);
		} catch (ParseException e1) {
			error = true;
		}
		
		if(error == false) {
			if (m_RunThread == null) {
				synchronized (this) {
					btnStart.setEnabled(false);
					btnStop.setEnabled(true);
				}
				m_RunThread = new Thread() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {

						if (m_LogPanel instanceof TaskLogger) {
							((TaskLogger) m_LogPanel).taskStarted();
						}
						m_ClassCombo.setEnabled(false);
						// Copy the current state of things
						m_LogPanel.statusMessage("Setting up...");
						instConfig = new Instances(m_Instances);
						String nameInstance = locateDate;
						Attribute dateAttributes = instConfig.attribute(nameInstance);
						//int numberIntance = inst.numInstances();
			
						int test = 0 ;
						int firstTrain = 0 ;
						int numbertotal =0;
						for(int i=1; i<instConfig.numInstances();i++) {
							String a =instConfig.instance(i).stringValue(dateAttributes);
							SimpleDateFormat formatter = new SimpleDateFormat(dateSelected);
							try {
								Date date = formatter.parse(a);
								Date dateTraningStart = formatter.parse(trainingDateStart.getText());
								Date dateTraningEnd = formatter.parse(trainingDateEnd.getText());
								//Date dateTestStart = formatter.parse(testDateStart.getText());
								Date dateTestEnd = formatter.parse(testDateEnd.getText());

								if(dateTraningStart.after(date)) {	
									instConfig.delete(i);
									i=i-1;
								}else {
									if(date.after(dateTraningEnd) && date.before(dateTestEnd) || date.equals(dateTestEnd)) {
										test = test+1;
										numbertotal = numbertotal+1;
									}else {
										if(date.before(dateTraningEnd) ||date.equals(dateTraningEnd) )
											firstTrain = firstTrain+1;
										numbertotal = numbertotal+1;
										//System.out.println(date);
									}
								}
							}
							catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						try {
							System.out.println(dataDiff(formatter.parse(trainingDateEnd.getText()),formatter.parse(testDateEnd.getText())));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						int classIndex = m_ClassCombo.getSelectedIndex();
						instConfig.setClassIndex(classIndex);
						//System.out.println("Class: "+m_ClassCombo.getSelectedItem().toString());
						//inst.setClass(new Attribute(m_ClassCombo.getSelectedItem().toString()));
						Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
						try {
							AbstractClassifier.makeCopy(classifier);
						} catch (Exception ex) {
							m_LogPanel.logMessage("Problem copying classifier: " + ex.getMessage());
						}
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
							outBuff.append("Relation:     " + instConfig.relationName() + '\n');
							outBuff.append("Instances:    " + inst.numInstances() + '\n');
							outBuff.append("Attributes:   " + instConfig.numAttributes() + '\n');
							if (instConfig.numAttributes() < 100) {
								for (int i = 0; i < instConfig.numAttributes(); i++) {
									outBuff.append("              " + instConfig.attribute(i).name()
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
							int totalInstances = instConfig.numInstances(); 

							SlidingValidation slideEval = new SlidingValidation(instConfig);

							int windowTest =  test ;
							int windowTrain = firstTrain;

							if(windowTest <1){
								windowTest = 1;
							}
							if(windowTrain <1){
								windowTrain =1;
							}
							boolean isAccumulate = checkAccumulate.isSelected();
							int count =0;
							int trainSizeInicio = 0;
							int window = (int) (windowTrain);
							int totaltested = 0;
							int totaltrained = 0;
							Date dateTraingStart = formatter.parse(trainingDateStart.getText());;
							Date dateTraing = formatter.parse(trainingDateEnd.getText());
							Date dateTest = formatter.parse(testDateEnd.getText());
							int diferencaTraining = dataDiff(dateTraingStart,dateTraing) +1;
							int diferenca = dataDiff(dateTraing,dateTest);
							String fim =instConfig.lastInstance().stringValue(dateAttributes);
							Date dateFim = formatter.parse(fim);
							
							boolean outputPrint = true;
							int coity = 0;
							for(int w=windowTrain;w<=totalInstances-windowTest;w=window){
								count++;
								int testWindow = 0;
								Calendar c = new  GregorianCalendar();
								Date dateTraingLazz = dateTraing;
								c.setTime(dateTraingLazz); 
								c.add(Calendar.DATE, 1);
								dateTraingLazz = c.getTime();
								
								//System.out.println(diferencaTraining);
								//System.out.println(diferenca);
								//System.out.println(c.getTime());
								if(isAccumulate){
									windowTrain= w;
								}else{
									trainSizeInicio = w-windowTrain;
								}	
								for(int i=1; i<instConfig.numInstances();i++) {
									String b =instConfig.instance(i).stringValue(dateAttributes);
									Date dateT = formatter.parse(b);
									if(dateT.after(dateTraingLazz) && dateT.before(dateTest) || dateT.equals(dateTest)) {
										testWindow = testWindow+1;
									}
								}
								if(testWindow == 0) {
									JOptionPane.showMessageDialog(SlidingValidationPanel.this,"PROBLEM TEST SIZE\nThere are no instances between "+ dateTraingLazz +
											" and " +dateTest +"\n As the test set becomes 0 it is impossible to carry out the evaluation, please select a larger test window that "+diferenca+" days",
											"Error", JOptionPane.WARNING_MESSAGE);
									outputPrint = false;
									break;
								}
								//System.out.println(dateTraingStart);
								//System.out.println(dateTraing+"\n");
								//System.out.println(dateTraingLazz);
								//System.out.println(dateTest);
								dateTraing = dateTest;
								
								c.setTime(dateTraing);  
								c.add(Calendar.DATE, -diferencaTraining +1);
								dateTraingStart = c.getTime();
								
								c.setTime(dateTest);  
								c.add(Calendar.DATE, diferenca);
								dateTest = c.getTime();
								
								if(dateTest.after(dateFim)) {
									break;
								}
								window += testWindow;
								coity= coity +1;
								//Print on console the window's slides
								//							printSlideOnConsole(count, trainSizeInicio, windowTrain, windowTest, w, totalInstances);
								//System.out.println("Model: "+coity);
								//System.out.println("trainSizeInicio: "+trainSizeInicio);
								//System.out.println("windowTrain: "+windowTrain);
								//System.out.println("w: "+w);
								//System.out.println("testWindow: "+testWindow+"\n");
								m_LogPanel.statusMessage("Model "+count);
								Instances trainInstances = new Instances(instConfig, trainSizeInicio, windowTrain); 
								Instances testInstances = new Instances(instConfig, w, testWindow); 
								totaltested = totaltested + testInstances.numInstances();
								if(isAccumulate){
									totaltrained= trainInstances.numInstances();
								}else{
									totaltrained = totaltrained + trainInstances.numInstances();
								}	
								trainInstances.numInstances();
								testInstances.numInstances();
								windowTrain = 0;
								if(!isAccumulate){
									for(int i=1; i<instConfig.numInstances();i++) {
										String b =instConfig.instance(i).stringValue(dateAttributes);
										Date dateT = formatter.parse(b);
										if(dateT.after(dateTraingStart) && dateT.before(dateTraing) || dateT.equals(dateTraingStart) || dateT.equals(dateTraing)) {
											windowTrain = windowTrain+1;
										}
									}
									if(windowTrain == 0) {
										JOptionPane.showMessageDialog(SlidingValidationPanel.this,"ERROR TRAINING SIZE\nThere are no instances between "+ dateTraingStart +
												" and " +dateTraing +"\n As the training set becomes 0 it is impossible to carry out the evaluation, please select a larger training window that "+diferencaTraining+" days",
												"Error", JOptionPane.WARNING_MESSAGE);
										outputPrint = false;
										break;
									}
								}
								
								if(isAccumulate){
									outBuff.append("\nModel: "+count+ " | Total training instânces: "+trainInstances.numInstances() +" | Total tested instânces: "+testInstances.numInstances());
								}else{
									outBuff.append("\nModel: "+count+ " | Total training instânces: "+trainInstances.numInstances() + " | Total tested instânces: "+testInstances.numInstances());
								}	

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

							if(outputPrint == true) {
								outBuff.append("\n=== Summary ===\n");
								outBuff.append("\nModels:                        "+(count -1));
								outBuff.append("\nAccumulate training windows   "+ isAccumulate);
								/*if(isAccumulate){
									outBuff.append("\nTraining window size:       "+windowTrain );
									//outBuff.append("\nTotal trained instances:       "+ (windowTest*(totalModels-1) + (windowTrain)));
								}
								else{
									outBuff.append("\nFirst training window size:       "+windowTrain );
									//outBuff.append("\nTotal trained instances:       "+ windowTrain*totalModels);
								}*/
								
								outBuff.append("\nTotal trained instances:       "+ totaltrained );

								//outBuff.append("\nTest window size:              "+windowTest );
								outBuff.append("\nTotal tested instances:        "+totaltested);
								outBuff.append("\nTotal not tested instances:    "+(totalInstances - totaltested));
								
								outBuff.append("\nTotal number of valid instances:     "+totalInstances);
								outBuff.append("\n");
								
								if (isNominal){
									outBuff.append(slideEval.toClassifyMetricsString(count)+"\n");
									outBuff.append(slideEval.toClassDetailsString("=== Detailed Accuracy By Class (models' average) ===\n", count));
									outBuff.append(slideEval.toMatrixString("=== Confusion Matrix ===\n"));

								}else{
									outBuff.append(slideEval.toRegressionMetricsString(count));
								}	
							}
							else {
								outBuff.append("\n=== Summary ===\n");
								outBuff.append("\n Evaluation canceled");
								outBuff.append("\n Model " +count+" failed");
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							/*m_LogPanel.logMessage(ex.getMessage());
							JOptionPane.showMessageDialog(SlidingValidationPanel.this,
									"Problem evaluating classifier:\n" + ex.getMessage(),
									"Evaluate classifier", JOptionPane.ERROR_MESSAGE);*/
							m_LogPanel.statusMessage("Problem evaluating classifier");
							ex.printStackTrace();


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
		}else {
			JOptionPane.showMessageDialog(SlidingValidationPanel.this,"ERROR in the field Format date. The dates in the database are not in the selected format "+dateSelected,
					"Error", JOptionPane.ERROR_MESSAGE);
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setInstances(Instances inst) {
		m_Instances = inst;

		String[] attribNames = new String[m_Instances.numAttributes()];
		for (int i = 0; i < attribNames.length; i++) {
			String type =
					"(" + Attribute.typeToStringShort(m_Instances.attribute(i)) + ") ";
			attribNames[i] = type + m_Instances.attribute(i).name();
		}
		NumberInstances = inst.size();
		numberInstanceLabel.setText("Total of instances: "+NumberInstances);
		//System.out.print(NumberInstances);

		m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
		I_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
		if (attribNames.length > 0) {
			if (inst.classIndex() == -1) {
				m_ClassCombo.setSelectedIndex(attribNames.length - 1);
				I_ClassCombo.setSelectedIndex(attribNames.length - 1);
			} else {
				m_ClassCombo.setSelectedIndex(inst.classIndex());
				I_ClassCombo.setSelectedIndex(inst.classIndex());
			}
			m_ClassCombo.setEnabled(true);
			I_ClassCombo.setEnabled(true);
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
				//FileReader f = new FileReader("/home/manoel/Dropbox/Time/data/all-attributes/experiment/new-discrete-p/testing-p/akka-new-discrete-p.arff");
				FileReader f = new FileReader("E://Bruno//bruno//Desktop//bases-experimento//fonte//netty.arff");
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
	public class DateLabelFormatter extends AbstractFormatter {

		private String datePattern = "yyyy-MM-dd";
		private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

		@Override
		public Object stringToValue(String text) throws ParseException {
			return dateFormatter.parseObject(text);
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if (value != null) {
				Calendar cal = (Calendar) value;
				return dateFormatter.format(cal.getTime());
			}

			return "";
		}

	}
	public static int dataDiff(java.util.Date dataLow, java.util.Date dataHigh){
        GregorianCalendar startTime = new GregorianCalendar();
        GregorianCalendar endTime = new GregorianCalendar();
        GregorianCalendar curTime = new GregorianCalendar();
        GregorianCalendar baseTime = new GregorianCalendar();
        startTime.setTime(dataLow);
        endTime.setTime(dataHigh);
        int dif_multiplier = 1;
        
        // Verifica a ordem de inicio das datas
        if( dataLow.compareTo( dataHigh ) < 0 ){
            baseTime.setTime(dataHigh);
            curTime.setTime(dataLow);
            dif_multiplier = 1;
        }else{
            baseTime.setTime(dataLow);
            curTime.setTime(dataHigh);
            dif_multiplier = -1;
        }
        int result_years = 0;
        int result_months = 0;
        int result_days = 0;
        
        // Para cada mes e ano, vai de mes em mes pegar o ultimo dia para import acumulando
        // no total de dias. Ja leva em consideracao ano bissesto
        while( curTime.get(GregorianCalendar.YEAR) < baseTime.get(GregorianCalendar.YEAR) ||
               curTime.get(GregorianCalendar.MONTH) < baseTime.get(GregorianCalendar.MONTH)  )
        {
            int max_day = curTime.getActualMaximum( GregorianCalendar.DAY_OF_MONTH );
            result_months += max_day;
            curTime.add(GregorianCalendar.MONTH, 1);
        }
        // Marca que é um saldo negativo ou positivo
        result_months = result_months*dif_multiplier;
        // Retirna a diferenca de dias do total dos meses
        result_days += (endTime.get(GregorianCalendar.DAY_OF_MONTH) - startTime.get(GregorianCalendar.DAY_OF_MONTH));
        return result_years+result_months+result_days;
    }
}
