package weka.classifiers;

import java.text.DecimalFormat;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.Utils;

public class SlidingValidation {
	
	private double[][] confusionMatrix;
	private int numClasses;
	private String[] classNames;
	private boolean isNominal;
	
	//Classify Metrics
	double[] truePositiveRateSUM;
	double[] falsePositiveRateSUM;
	double[] precisionSUM;
	double[] recallSUM;
	double[] fMeasureSUM;
	double[] matthewsCorrelationCoefficientSUM;
	int somaDP = 0, somaTotal = 0;
	
	//Regression Metrics
	//double somaR2 = 0;
	//double somaR22 = 0;
	//double R2 = 0;
	double somaPearson = 0;
	String somaSpearman = "0";
	double somaMAE = 0;
	double somaRMSE = 0;
	double somaNRMSE1 = 0;
	double somaRAE = 0;
	double somaRRSE = 0;
	
	public SlidingValidation(Instances data) {

		numClasses = data.numClasses();
		isNominal = data.classAttribute().isNominal();
		confusionMatrix = new double[numClasses][numClasses];
	    if (isNominal) {
	      classNames = new String[numClasses];
	      for (int i = 0; i < numClasses; i++) {
	    	  classNames[i] = data.classAttribute().value(i);
	      }
	    }
	    truePositiveRateSUM = new double[numClasses];
		falsePositiveRateSUM= new double[numClasses];
		precisionSUM= new double[numClasses];
		recallSUM= new double[numClasses];
		fMeasureSUM= new double[numClasses];
		matthewsCorrelationCoefficientSUM= new double[numClasses];
	}
	
	public double[][] getConfusionMatrix(){
		return confusionMatrix;
	}

	public void accumulateConfusionMatrix(double [][] matrix){
		//acumula valores na matriz de confusao
		if(confusionMatrix == null){
			confusionMatrix = matrix;
		}else{
			for(int x=0;x<matrix.length;x++)
				for(int y=0;y<matrix.length;y++){
					confusionMatrix[x][y] += matrix[x][y];
				}
		}
		
	}
	public void attMatrixClassifiedData(){
		for(int x=0;x<confusionMatrix.length;x++)
			for(int y=0;y<confusionMatrix.length;y++){
				if(x==y)
					somaDP += confusionMatrix[x][y];//Diagonal Principal
			}
		
		for(int x=0;x<confusionMatrix.length;x++)
			for(int y=0;y<confusionMatrix.length;y++){
				somaTotal += confusionMatrix[x][y];//Soma total
			}
	}
	public double confusionMatrixAccuracy() throws Exception{
		if(confusionMatrix == null){
			
			throw new Exception("Error on calculate confusion matrix accuracy: null confusion matrix");
		}
		double accMatriz = ((double)somaDP/somaTotal)*100; 
		return Double.parseDouble(new DecimalFormat("0.####").format((double)(accMatriz)).replace(',', '.'));
	}
	
	public double confusionMatrixKappa() throws Exception{
		if(confusionMatrix == null){
			throw new Exception("Error on calculate confusion matrix kappa: null confusion matrix");
		}
		int somaDP = 0, somaTotal = 0;
		double accMatriz = 0;
		
		for(int x=0;x<confusionMatrix.length;x++)
			for(int y=0;y<confusionMatrix.length;y++){
				if(x==y)
					somaDP += confusionMatrix[x][y];//Diagonal Principal
			}
		for(int x=0;x<confusionMatrix.length;x++)
			for(int y=0;y<confusionMatrix.length;y++){
				somaTotal += confusionMatrix[x][y];//Soma total
			}
		
		//Calculo da medida kappa
		int [] linha = new int[confusionMatrix.length];
		int [] coluna = new int[confusionMatrix.length];
		
		for(int x=0;x<confusionMatrix.length;x++){
			int somaParcial=0;
			for(int y=0;y<confusionMatrix.length;y++){
				somaParcial += (int)confusionMatrix[x][y];
			}
			linha[x] = somaParcial;
		}
		
		for(int x=0;x<confusionMatrix.length;x++){
			int somaParcial=0;
			for(int y=0;y<confusionMatrix.length;y++){
				somaParcial += (int)confusionMatrix[y][x];
			}
			coluna[x] = somaParcial;
		}
		
		//vetores com as somas parciais
		double [] parcial = new double[confusionMatrix.length];
		for(int x=0;x<confusionMatrix.length;x++){
			parcial[x] = ((double)linha[x]/somaTotal * (double)coluna[x]/somaTotal);
		}
		double acaso = 0, kappaFinal;
		for(int x=0;x<confusionMatrix.length;x++){
			acaso += parcial[x];
		}
		
		accMatriz = ((double)somaDP/somaTotal)*100;
		
		kappaFinal = (((double)somaDP/somaTotal) - acaso) / (1 - acaso); 
		return Double.parseDouble(new DecimalFormat("0.####").format((double)(kappaFinal)).replace(',', '.'));
	}
	
	public String toMatrixString(String title) throws Exception {

	    StringBuffer text = new StringBuffer();
	    char[] IDChars =
	      { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
	        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	    int IDWidth;
	    boolean fractional = false;
	    if (numClasses == 0 || classNames ==null) {
			  throw new Exception("Evaluation not initialized!");
			}
	    if (!isNominal) {
	      throw new Exception("Evaluation: No confusion matrix possible!");
	    }
	    
	    // Find the maximum value in the matrix
	    // and check for fractional display requirement/*
	    double maxval = 0;
	    for (int i = 0; i < numClasses; i++) {
	      for (int j = 0; j < numClasses; j++) {
	        double current = confusionMatrix[i][j];
	        if (current < 0) {
	          current *= -10;
	        }
	        if (current > maxval) {
	          maxval = current;
	        }
	        double fract = current - Math.rint(current);
	        if (!fractional && ((Math.log(fract) / Math.log(10)) >= -2)) {
	          fractional = true;
	        }
	      }
	    }

	    IDWidth =
	      1 + Math.max(
	        (int) (Math.log(maxval) / Math.log(10) + (fractional ? 3 : 0)),
	        (int) (Math.log(numClasses) / Math.log(IDChars.length)));
	    text.append(title).append("\n");
	    for (int i = 0; i < numClasses; i++) {
	      if (fractional) {
	        text.append(" ").append(num2ShortID(i, IDChars, IDWidth - 3))
	          .append("   ");
	      } else {
	        text.append(" ").append(num2ShortID(i, IDChars, IDWidth));
	      }
	    }
	    text.append("   <-- classified as\n");
	    for (int i = 0; i < numClasses; i++) {
	      for (int j = 0; j < numClasses; j++) {
	        text.append(" ").append(
	          Utils.doubleToString(confusionMatrix[i][j], IDWidth,
	            (fractional ? 2 : 0)));
	      }
	      text.append(" | ").append(num2ShortID(i, IDChars, IDWidth)).append(" = ")
	        .append(classNames[i]).append("\n");
	    }
	    return text.toString();
	  }
	
	protected String num2ShortID(int num, char[] IDChars, int IDWidth) {

	    char ID[] = new char[IDWidth];
	    int i;

	    for (i = IDWidth - 1; i >= 0; i--) {
	      ID[i] = IDChars[num % IDChars.length];
	      num = num / IDChars.length - 1;
	      if (num < 0) {
	        break;
	      }
	    }
	    for (i--; i >= 0; i--) {
	      ID[i] = ' ';
	    }

	    return new String(ID);
	  }
	
	public String confusionMatrixToPrint(){
		//imprime matriz de confusão final
		String matrix = "";
		for(int x=0;x<confusionMatrix.length;x++)
			for(int y=0;y<confusionMatrix.length;y++){
				if(y==confusionMatrix.length-1)
					matrix = matrix + (int)confusionMatrix[x][y]+" \n";
				else
					matrix  = matrix + (int)confusionMatrix[x][y]+",";
			}
		return matrix;
	}
	
	public void accumlateClassifyMetrics(double[][] modelConfusionMatrix){
		accumulateConfusionMatrix(modelConfusionMatrix);
		for (int classIndex = 0; classIndex < numClasses; classIndex++) {
			
			truePositiveRateSUM[classIndex]  			  =+ truePositiveRate(classIndex, modelConfusionMatrix);
			falsePositiveRateSUM[classIndex]			  =+ falsePositiveRate(classIndex, modelConfusionMatrix);
			precisionSUM[classIndex]					  =+ precision(classIndex, modelConfusionMatrix);
			recallSUM[classIndex]						  =+ recall(classIndex, modelConfusionMatrix);
			fMeasureSUM[classIndex]						  =+ fMeasure(classIndex, modelConfusionMatrix);
			matthewsCorrelationCoefficientSUM[classIndex] =+ matthewsCorrelationCoefficient(classIndex, modelConfusionMatrix);
		}
	}
	
	public String toClassifyMetricsString(int totalModels) throws Exception{
		StringBuffer text = new StringBuffer();
		attMatrixClassifiedData();
		//double precision = confusionMatrixPrecision(), recall = confusionMatrixRecall();
		text.append("\nCorrectly Classified Instances:   "+ somaDP +" ("+(somaDP*100)/somaTotal+"%)");
		text.append("\nIncorrectly Classified Instances: "+ (somaTotal - somaDP)+" ("+((somaTotal - somaDP)*100)/somaTotal+"%)");
		text.append("\nAccuracy:                         "+ confusionMatrixAccuracy()+"%");
		text.append("\nKappa statistic:                  "+ confusionMatrixKappa());
		//text.append("\nRecall avg:                       "+ recall);
		//text.append("\nPrecision avg:                    "+ precision);
		//text.append("\nfMeasure avg:                     "+ new DecimalFormat("0.####").format((double) (2 * precision * recall) / (precision + recall)).replace(',', '.'));
		
		text.append("\n");
		return text.toString();
	}
	
	public String toClassDetailsString(String title, int totalModels) throws Exception {
		//return "";

	    /*if (!m_ClassIsNominal) {
	      throw new Exception("Evaluation: No per class statistics possible!");
	    }*/ 
		
	    boolean displayTP = true;// m_metricsToDisplay.contains("tp rate");
	    boolean displayFP = true;// m_metricsToDisplay.contains("fp rate");
	    boolean displayP = true;// m_metricsToDisplay.contains("precision");
	    boolean displayR = true;// m_metricsToDisplay.contains("recall");
	    boolean displayFM = true;// m_metricsToDisplay.contains("f-measure");
	    boolean displayMCC = true;// m_metricsToDisplay.contains("mcc");
	    boolean displayROC = false;// m_metricsToDisplay.contains("roc area");
	    boolean displayPRC = false;// m_metricsToDisplay.contains("prc area");

	    StringBuffer text =
	      new StringBuffer(title + "\n"
	        + (displayTP ? "TP Rate  " : "") + (displayFP ? "FP Rate  " : "")
	        + (displayP ? "Precision  " : "") + (displayR ? "Recall   " : "")
	        + (displayFM ? "F-Measure  " : "") + (displayMCC ? "MCC      " : "")
	        + (displayROC ? "ROC Area  " : "") + (displayPRC ? "PRC Area  " : ""));

	    /*if (m_pluginMetrics != null && m_pluginMetrics.size() > 0) {
	      for (AbstractEvaluationMetric m : m_pluginMetrics) {
	        if (m instanceof InformationRetrievalEvaluationMetric
	          && m.appliesToNominalClass()) {
	          String metricName = m.getMetricName().toLowerCase();
	          if (m_metricsToDisplay.contains(metricName)) {
	            List<String> statNames = m.getStatisticNames();
	            for (String name : statNames) {
	              if (m_metricsToDisplay.contains(name.toLowerCase())) {
	                if (name.length() < 7) {
	                  name = Utils.padRight(name, 7);
	                }
	                text.append(name).append("  ");
	              }
	            }
	          }
	        }
	      }
	    }*/

	    text.append("Class\n");
	    for (int i = 0; i < numClasses; i++) {
	      //text.append("                 ");
	      if (displayTP) {
	        text.append(String.format("%-9.3f", truePositiveRateSUM[i]/totalModels));
	      }
	      if (displayFP) {
	        text.append(String.format("%-9.3f", falsePositiveRateSUM[i]/totalModels));
	      }
	      if (displayP) {
	        text.append(String.format("%-11.3f", precisionSUM[i]/totalModels));
	      }
	      if (displayR) {
	        text.append(String.format("%-9.3f", recallSUM[i]/totalModels));
	      }
	      if (displayFM) {
	        text.append(String.format("%-11.3f", fMeasureSUM[i]/totalModels));
	      }
	      if (displayMCC) {
	        double mat = matthewsCorrelationCoefficientSUM[i]/totalModels;
	        if (Utils.isMissingValue(mat)) {
	          text.append("?       ");
	        } else {
	          text.append(String
	            .format("%-9.3f", matthewsCorrelationCoefficientSUM[i]/totalModels));
	        }
	      }

	      /*if (displayROC) {
	        double rocVal = areaUnderROC(i);
	        if (Utils.isMissingValue(rocVal)) {
	          text.append("?         ");
	        } else {
	          text.append(String.format("%-10.3f", rocVal));
	        }
	      }
	      if (displayPRC) {
	        double prcVal = areaUnderPRC(i);
	        if (Utils.isMissingValue(prcVal)) {
	          text.append("?         ");
	        } else {
	          text.append(String.format("%-10.3f", prcVal));
	        }
	      }*/

	      /*if (m_pluginMetrics != null && m_pluginMetrics.size() > 0) {
	        for (AbstractEvaluationMetric m : m_pluginMetrics) {
	          if (m instanceof InformationRetrievalEvaluationMetric
	            && m.appliesToNominalClass()) {
	            String metricName = m.getMetricName().toLowerCase();
	            if (m_metricsToDisplay.contains(metricName)) {
	              List<String> statNames = m.getStatisticNames();
	              for (String name : statNames) {
	                if (m_metricsToDisplay.contains(name.toLowerCase())) {
	                  double stat =
	                    ((InformationRetrievalEvaluationMetric) m).getStatistic(
	                      name, i);
	                  if (name.length() < 7) {
	                    name = Utils.padRight(name, 7);
	                  }
	                  if (Utils.isMissingValue(stat)) {
	                    Utils.padRight("?", name.length());
	                  } else {
	                    text.append(
	                      String.format("%-" + name.length() + ".3f", stat))
	                      .append("  ");
	                  }
	                }
	              }
	            }
	          }
	        }
	      }*/

	      text.append(classNames[i]).append('\n');
	    }
	    /*
	    text.append("Weighted Avg.    ");
	    if (displayTP) {
	      text.append(String.format("%-9.3f", weightedTruePositiveRate()));
	    }
	    if (displayFP) {
	      text.append(String.format("%-9.3f", weightedFalsePositiveRate()));
	    }
	    if (displayP) {
	      text.append(String.format("%-11.3f", weightedPrecision()));
	    }
	    if (displayR) {
	      text.append(String.format("%-9.3f", weightedRecall()));
	    }
	    if (displayFM) {
	      text.append(String.format("%-11.3f", weightedFMeasure()));
	    }
	    if (displayMCC) {
	      text.append(String.format("%-9.3f", weightedMatthewsCorrelation()));
	    }
	    */
	    /*if (displayROC) {
	      text.append(String.format("%-10.3f", weightedAreaUnderROC()));
	    }
	    if (displayPRC) {
	      text.append(String.format("%-10.3f", weightedAreaUnderPRC()));
	    }*/
	    

	    /*if (m_pluginMetrics != null && m_pluginMetrics.size() > 0) {
	      for (AbstractEvaluationMetric m : m_pluginMetrics) {
	        if (m instanceof InformationRetrievalEvaluationMetric
	          && m.appliesToNominalClass()) {
	          String metricName = m.getMetricName().toLowerCase();
	          if (m_metricsToDisplay.contains(metricName)) {
	            List<String> statNames = m.getStatisticNames();
	            for (String name : statNames) {
	              if (m_metricsToDisplay.contains(name.toLowerCase())) {
	                double stat =
	                  ((InformationRetrievalEvaluationMetric) m)
	                    .getClassWeightedAverageStatistic(name);
	                if (name.length() < 7) {
	                  name = Utils.padRight(name, 7);
	                }
	                if (Utils.isMissingValue(stat)) {
	                  Utils.padRight("?", name.length());
	                } else {
	                  text
	                    .append(String.format("%-" + name.length() + ".3f", stat))
	                    .append("  ");
	                }
	              }
	            }
	          }
	        }
	      }
		}*/
	    text.append("\n");

	    return text.toString();
	
	  }
	
/**
 * Calculate the true positive rate with respect to a particular class. This
 * is defined as
 * <p/>
 * 
 * <pre>
 * correctly classified positives
 * ------------------------------
 *       total positives
 * </pre>
 * 
 * @param classIndex the index of the class to consider as "positive"
 * @return the true positive rate
 */
public double truePositiveRate(int classIndex, double[][] modelConfusionMatrix) {

  double correct = 0, total = 0;
  for (int j = 0; j < numClasses; j++) {
    if (j == classIndex) {
      correct += modelConfusionMatrix[classIndex][j];
    }
    total += modelConfusionMatrix[classIndex][j];
  }
  if (total == 0) {
    return 0;
  }
  return correct / total;
}

/**
 * Calculate the false positive rate with respect to a particular class. This
 * is defined as
 * <p/>
 * 
 * <pre>
 * incorrectly classified negatives
 * --------------------------------
 *        total negatives
 * </pre>
 * 
 * @param classIndex the index of the class to consider as "positive"
 * @return the false positive rate
 */
public double falsePositiveRate(int classIndex, double[][] modelConfusionMatrix) {

  double incorrect = 0, total = 0;
  for (int i = 0; i < numClasses; i++) {
    if (i != classIndex) {
      for (int j = 0; j < numClasses; j++) {
        if (j == classIndex) {
          incorrect += modelConfusionMatrix[i][j];
        }
        total += modelConfusionMatrix[i][j];
      }
    }
  }
  if (total == 0) {
    return 0;
  }
  return incorrect / total;
}


/**
 * Calculate the precision with respect to a particular class. This is defined
 * as
 * <p/>
 * 
 * <pre>
 * correctly classified positives
 * ------------------------------
 *  total predicted as positive
 * </pre>
 * 
 * @param classIndex the index of the class to consider as "positive"
 * @return the precision
 */
public double precision(int classIndex, double[][] modelConfusionMatrix) {

  double correct = 0, total = 0;
  for (int i = 0; i < numClasses; i++) {
    if (i == classIndex) {
      correct += modelConfusionMatrix[i][classIndex];
    }
    total += modelConfusionMatrix[i][classIndex];
  }
  if (total == 0) {
    return 0;
  }
  return correct / total;
}


/**
 * Calculate the recall with respect to a particular class. This is defined as
 * <p/>
 * 
 * <pre>
 * correctly classified positives
 * ------------------------------
 *       total positives
 * </pre>
 * <p/>
 * (Which is also the same as the truePositiveRate.)
 * 
 * @param classIndex the index of the class to consider as "positive"
 * @return the recall
 */
public double recall(int classIndex, double[][] modelConfusionMatrix) {

  return truePositiveRate(classIndex, modelConfusionMatrix);
}

/**
 * Calculate the F-Measure with respect to a particular class. This is defined
 * as
 * <p/>
 * 
 * <pre>
 * 2 * recall * precision
 * ----------------------
 *   recall + precision
 * </pre>
 * 
 * @param classIndex the index of the class to consider as "positive"
 * @return the F-Measure
 */
public double fMeasure(int classIndex, double[][] modelConfusionMatrix) {

  double precision = precision(classIndex, modelConfusionMatrix);
  double recall = recall(classIndex, modelConfusionMatrix);
  if ((precision + recall) == 0) {
    return 0;
  }
  return 2 * precision * recall / (precision + recall);
}

/**
 * Calculates the matthews correlation coefficient (sometimes called phi
 * coefficient) for the supplied class
 * 
 * @param classIndex the index of the class to compute the matthews
 *          correlation coefficient for
 * 
 * @return the mathews correlation coefficient
 */
public double matthewsCorrelationCoefficient(int classIndex, double[][] modelConfusionMatrix) {
  double numTP = numTruePositives(classIndex, modelConfusionMatrix);
  double numTN = numTrueNegatives(classIndex, modelConfusionMatrix);
  double numFP = numFalsePositives(classIndex, modelConfusionMatrix);
  double numFN = numFalseNegatives(classIndex, modelConfusionMatrix);
  double n = (numTP * numTN) - (numFP * numFN);
  double d =
    (numTP + numFP) * (numTP + numFN) * (numTN + numFP) * (numTN + numFN);
  d = Math.sqrt(d);
  if (d == 0) {
    d = 1;
  }

  return n / d;
}
public double numTruePositives(int classIndex, double[][] modelConfusionMatrix) {

    double correct = 0;
    for (int j = 0; j < numClasses; j++) {
      if (j == classIndex) {
        correct += modelConfusionMatrix[classIndex][j];
      }
    }
    return correct;
  }

public double numTrueNegatives(int classIndex, double[][] modelConfusionMatrix) {

    double correct = 0;
    for (int i = 0; i < numClasses; i++) {
      if (i != classIndex) {
        for (int j = 0; j < numClasses; j++) {
          if (j != classIndex) {
            correct += modelConfusionMatrix[i][j];
          }
        }
      }
    }
    return correct;
  }

public double numFalsePositives(int classIndex, double[][] modelConfusionMatrix) {

    double incorrect = 0;
    for (int i = 0; i < numClasses; i++) {
      if (i != classIndex) {
        for (int j = 0; j < numClasses; j++) {
          if (j == classIndex) {
            incorrect += modelConfusionMatrix[i][j];
          }
        }
      }
    }
    return incorrect;
  }

public double numFalseNegatives(int classIndex, double[][] modelConfusionMatrix) {

    double incorrect = 0;
    for (int i = 0; i < numClasses; i++) {
      if (i == classIndex) {
        for (int j = 0; j < numClasses; j++) {
          if (j != classIndex) {
            incorrect += modelConfusionMatrix[i][j];
          }
        }
      }
    }
    return incorrect;
  }

/**
 * Calculates the weighted (by class size) true positive rate.
 * 
 * @return the weighted true positive rate.
 */
public double weightedTruePositiveRate(double[][] modelConfusionMatrix) {
  double[] classCounts = new double[numClasses];
  double classCountSum = 0;

  for (int i = 0; i < numClasses; i++) {
    for (int j = 0; j < numClasses; j++) {
      classCounts[i] += modelConfusionMatrix[i][j];
    }
    classCountSum += classCounts[i];
  }

  double truePosTotal = 0;
  for (int i = 0; i < numClasses; i++) {
    double temp = truePositiveRate(i, modelConfusionMatrix);
    truePosTotal += (temp * classCounts[i]);
  }

  return truePosTotal / classCountSum;
}

/**
 * Calculates the weighted (by class size) false positive rate.
 * 
 * @return the weighted false positive rate.
 */
public double weightedFalsePositiveRate(double[][] modelConfusionMatrix) {
  double[] classCounts = new double[numClasses];
  double classCountSum = 0;

  for (int i = 0; i < numClasses; i++) {
    for (int j = 0; j < numClasses; j++) {
      classCounts[i] += modelConfusionMatrix[i][j];
    }
    classCountSum += classCounts[i];
  }

  double falsePosTotal = 0;
  for (int i = 0; i < numClasses; i++) {
    double temp = falsePositiveRate(i, modelConfusionMatrix);
    falsePosTotal += (temp * classCounts[i]);
  }

  return falsePosTotal / classCountSum;
}

/**
 * Calculates the weighted (by class size) precision.
 * 
 * @return the weighted precision.
 */
public double weightedPrecision( double[][] modelConfusionMatrix) {
  double[] classCounts = new double[numClasses];
  double classCountSum = 0;

  for (int i = 0; i < numClasses; i++) {
    for (int j = 0; j < numClasses; j++) {
      classCounts[i] += modelConfusionMatrix[i][j];
    }
    classCountSum += classCounts[i];
  }

  double precisionTotal = 0;
  for (int i = 0; i < numClasses; i++) {
    double temp = precision(i, modelConfusionMatrix);
    precisionTotal += (temp * classCounts[i]);
  }

  return precisionTotal / classCountSum;
}

/**
 * Calculates the weighted (by class size) recall.
 * 
 * @return the weighted recall.
 */
public double weightedRecall(double[][] modelConfusionMatrix) {
  return weightedTruePositiveRate(modelConfusionMatrix);
}
/**
 * Calculates the weighted (by class size) matthews correlation coefficient.
 * 
 * @return the weighted matthews correlation coefficient.
 */
public double weightedMatthewsCorrelation(double[][] modelConfusionMatrix) {
  double[] classCounts = new double[numClasses];
  double classCountSum = 0;

  for (int i = 0; i < numClasses; i++) {
    for (int j = 0; j < numClasses; j++) {
      classCounts[i] += modelConfusionMatrix[i][j];
    }
    classCountSum += classCounts[i];
  }

  double mccTotal = 0;
  for (int i = 0; i < numClasses; i++) {
    double temp = matthewsCorrelationCoefficient(i, modelConfusionMatrix);
    if (!Utils.isMissingValue(temp)) {
      mccTotal += (temp * classCounts[i]);
    }
  }

  return mccTotal / classCountSum;
}

double confusionMatrixPrecision(){
	double TP = 0, somaFP = 0, precisionAgv = 0, precision = 0;
	for(int x=0;x<confusionMatrix.length;x++){
		for(int y=0;y<confusionMatrix.length;y++){
			if(x==y){
				TP = confusionMatrix[x][y];//TP
				somaFP = 0;
				for(int z=0;z<confusionMatrix.length;z++)
					somaFP += confusionMatrix[z][y];//Soma FP
				precision += TP/somaFP;
			}
		}
	}
	precisionAgv = precision/confusionMatrix.length;
	return Double.parseDouble(new DecimalFormat("0.####").format((double)(precisionAgv)).replace(',', '.'));
}

double confusionMatrixRecall(){
	double TP = 0, somaP = 0, recallAgv = 0, recall = 0;
	for(int x=0;x<confusionMatrix.length;x++){
		for(int y=0;y<confusionMatrix.length;y++){
			if(x==y){
				TP = confusionMatrix[x][y];//TP
				somaP = 0;
				for(int z=0;z<confusionMatrix.length;z++)
					somaP += confusionMatrix[x][z];//Soma P
				recall += TP/somaP;
				
			}
		}
	}
	recallAgv = recall/confusionMatrix.length;
	return Double.parseDouble(new DecimalFormat("0.####").format((double)(recallAgv)).replace(',', '.'));
}

	public void accumulateRegressionMetrics(Instances testInstances, Classifier classifier, Evaluation eval) throws NumberFormatException, Exception{

		double [] classes = testInstances.attributeToDoubleArray(testInstances.numAttributes() - 1);
		double sumClasses = 0;
		double avgClasses = 0;
		double maxClasses = Double.MIN_VALUE;
		double minClasses = Double.MAX_VALUE;
		for (int i = 0; i < testInstances.numInstances(); i++){
			if(classes[i] > maxClasses)
				maxClasses = classes[i];
			if(classes[i] < minClasses)
				minClasses = classes[i];
			sumClasses += Double.parseDouble(new DecimalFormat("0.####").format(classes[i]).replace(',', '.')); 
		}
		avgClasses = Double.parseDouble(new DecimalFormat("0.####").format(sumClasses/testInstances.numInstances()).replace(',', '.'));
		double [] predictClasses = new double[testInstances.numInstances()];
		for (int i = 0; i <testInstances.numInstances(); i++){
			predictClasses[i] = Double.parseDouble(new DecimalFormat("0.####").format(classifier.classifyInstance(testInstances.instance(i))).replace(',', '.'));  //Classe prevista pelo classificaador
		}
								
		double [] residuals = new double[testInstances.numInstances()];
		double sumSquaredResiduals = 0;
		double sumSquaredNRMSE1 = 0;
//		double maxPredict = Double.MIN_VALUE;
//		double minPredict = Double.MAX_VALUE;
		
		for (int i = 0; i <testInstances.numInstances(); i++){
			residuals[i] = Math.pow(Double.parseDouble(new DecimalFormat("0.####").format(classes[i]-predictClasses[i]).replace(',', '.')) ,2);
			
			if(classes[i]>0)
				sumSquaredNRMSE1 += Math.pow(Double.parseDouble(new DecimalFormat("0.####").format(Math.abs(predictClasses[i]-classes[i])/classes[i]).replace(',', '.')) ,2);
			else
				sumSquaredNRMSE1 = 0;
//			if(predictClasses[i] > maxPredict)
//				maxPredict = predictClasses[i];
//			if(predictClasses[i] < minPredict)
//				minPredict = predictClasses[i];
//			sumSquaredResiduals += residuals[i];
		}
		
		/*double [] expl = new double[testInstances.numInstances()];
		double sumSquaredExpl = 0;
		for (int i = 0; i <testInstances.numInstances(); i++){
			expl[i] = Double.parseDouble(new DecimalFormat("0.####").format(Math.pow(predictClasses[i]-avgClasses,2)).replace(',', '.'));
			sumSquaredExpl += expl[i];
		}
		
								
		double [] total = new double[testInstances.numInstances()];
		double sumSquaredTotal = 0;
		for (int i = 0; i <testInstances.numInstances(); i++){
			total[i] = Double.parseDouble(new DecimalFormat("0.####").format(Math.pow(classes[i]-avgClasses,2)).replace(',', '.'));
			sumSquaredTotal += total[i];
		}
		
		somaR2 += Double.parseDouble(new DecimalFormat("0.####").format(sumSquaredExpl/sumSquaredTotal).replace(',', '.'));
		R2 = Double.parseDouble(new DecimalFormat("0.####").format(sumSquaredExpl/sumSquaredTotal).replace(',', '.'));
		*/
		double spearman;
		//Spearman
		try {
			SpearmansCorrelation spearmanCorr = new SpearmansCorrelation();
			spearman = Math.abs(spearmanCorr.correlation(predictClasses, classes));
			somaSpearman = somaSpearman.contains("Error") || somaSpearman.contains("?") ? somaSpearman:(new DecimalFormat("0.####").format((double)(Double.parseDouble(somaSpearman) + spearman)).replace(',', '.')) +"";

		} catch (Exception e) {
			somaSpearman = "Error: " +e.getMessage();
		}
		
		
		somaPearson += eval.correlationCoefficient();
		somaRMSE += Math.abs(eval.rootMeanSquaredError());
		somaRAE += Math.abs(eval.relativeAbsoluteError());
		somaMAE += Math.abs(eval.meanAbsoluteError());
		somaRRSE += Math.abs(eval.rootRelativeSquaredError());
		//somaR22 += Math.pow(eval.correlationCoefficient(), 2);
		somaNRMSE1 += Math.sqrt(sumSquaredNRMSE1/testInstances.numInstances());
	}
	
	public String toRegressionMetricsString(int totalModels){
		StringBuffer text = new StringBuffer();
		//double R2Avg = somaR2/count;
		
		double pearsonAvg = somaPearson/totalModels;
		String spearmanAvg ="";
		try {
			spearmanAvg = (Double.parseDouble(somaSpearman)/totalModels)+"";
		} catch (Exception e) {
			spearmanAvg = "Not possible to be calculated";
		}
		 
		double MAEAvg = somaMAE/totalModels;
		double RMSEAvg = somaRMSE/totalModels;
		double NRMSEAvg1 = somaNRMSE1/totalModels;
		double RAEAvg = somaRAE/totalModels;
		double RRSEAvg = somaRRSE/totalModels;
		
		text.append("\nCorrelation coefficient:       "+new DecimalFormat("0.####").format((double)(pearsonAvg)).replace(',', '.'));
		text.append("\nSpearman coefficient:          "+spearmanAvg);
		//text.append("\nR2 avg:        "+new DecimalFormat("0.####").format((double)(R2Avg)).replace(',', '.'));
		text.append("\nRoot mean squared error:       "+new DecimalFormat("0.##").format((double)(RMSEAvg)).replace(',', '.'));
		text.append("\nNormalized RMSE:               "+new DecimalFormat("0.####").format((double)(NRMSEAvg1)).replace(',', '.'));
		text.append("\nMean absolute error:           "+new DecimalFormat("0.####").format((double)(MAEAvg)).replace(',', '.'));
		
		return text.toString();
	}
	
}
