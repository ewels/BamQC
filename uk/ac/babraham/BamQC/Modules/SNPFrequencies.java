/**
 * Copyright Copyright 2015 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
/*
 * Changelog: 
 * - Piero Dalle Pezze: Class creation.
 */
package uk.ac.babraham.BamQC.Modules;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.LineGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;



/** 
 * This class re-uses the computation collected by the class VariantCallDetection
 * and plots the SNP Frequencies.
 * @author Piero Dalle Pezze
 */
public class SNPFrequencies extends AbstractQCModule {

	private static Logger log = Logger.getLogger(SNPFrequencies.class);	
	
	
	double[] dFirstSNPPos = null;
	double[] dSecondSNPPos = null;
	
	// threshold for the plot y axis.
	private double firstMaxY=0.0d;
	private double secondMaxY=0.0d; 
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	// data fields for plotting
	private static String[] snpName = {"SNPs"};
	
	
	// Constructors
//	/**
//	 * Default constructor
//	 */
//	public SNPFrequencies() {	}

	
	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public SNPFrequencies(VariantCallDetection vcd) {
		super();
		variantCallDetection = vcd;
	}
	
	// Private methods
	
	/**
	 * Computes the maximum value for the x axis.
	 * @return xMaxValue
	 */
	private int computeXMaxValue() {
		HashMap<Integer, Long> hm = variantCallDetection.getContributingReadsPerPos();
		Integer[] readLengths = hm.keySet().toArray(new Integer[0]);
		Long[] readCounts = hm.values().toArray(new Long[0]);
		int xMaxValue = 5; // sequences long at least 5.
		long moreFrequentReadLength = 0;
		// Computes a variable threshold depending on the read length distribution of read library
		for(int i=0; i<readCounts.length; i++) {
			if(readCounts[i] > moreFrequentReadLength) {
				moreFrequentReadLength = readCounts[i];
			}
		}
		double threshold = moreFrequentReadLength * ModuleConfig.getParam("VariantCallPosition_snp_seqpercent_xaxis_threshold", "ignore").intValue() / 100d;
		// Filters the reads to show based on a the threshold computed previously.
		for(int i=0; i<readCounts.length; i++) {
			if(readCounts[i] >= threshold && xMaxValue < readLengths[i]) {
				xMaxValue = readLengths[i];
			}
			log.debug("Read Length: " + readLengths[i] + ", Num Reads: " + readCounts[i] + ", Min Accepted Length: " + threshold);
		}
		return xMaxValue+1;	//this will be used for array sizes (so +1).	
	}
	
	
	// @Override methods

	@Override
	public void processSequence(SAMRecord read) { }

	@Override	
	public void processFile(SequenceFile file) { }

	@Override	
	public void processAnnotationSet(AnnotationSet annotation) {

	}		

	@Override	
	public JPanel getResultsPanel() {
		
		// compute the totals
		variantCallDetection.computeTotals();

		long totSNPs = variantCallDetection.getTotalMutations(),
			 totBases = variantCallDetection.getTotal();
		
		log.debug("Total SNPs: " + totSNPs + " ( " + totSNPs*100f/totBases + "% )");
		
		
		
		JPanel resultsPanel = new JPanel();
		// We do not need a BaseGroup here
		// These two arrays have same length.
		// first/second identify the first or second segments respectively. 
		long[] totalPos = variantCallDetection.getTotalPos();
     	// initialise and configure the LineGraph
		// compute the maximum value for the X axis
		int maxX = computeXMaxValue();
		String[] xCategories = new String[maxX];
		
		
		
		// compute statistics from the FIRST segment data
		// We do not need a BaseGroup here
		long[] firstSNPPos = variantCallDetection.getFirstSNPPos();
		dFirstSNPPos = new double[maxX];
		for(int i=0; i<maxX && i<firstSNPPos.length; i++) {
			dFirstSNPPos[i]= (firstSNPPos[i] * 100d) / totalPos[i];
			if(dFirstSNPPos[i] > firstMaxY) { firstMaxY = dFirstSNPPos[i]; }
			xCategories[i] = String.valueOf(i+1);
		}
		double[][] firstSNPData = new double [][] {dFirstSNPPos};
		
		// compute statistics from the SECOND segment data if there are paired reads.
		if(variantCallDetection.existPairedReads()) {
			resultsPanel.setLayout(new GridLayout(2,1));
			// We do not need a BaseGroup here
			long[] secondSNPPos = variantCallDetection.getSecondSNPPos();
			dSecondSNPPos = new double[maxX];
			for(int i=0; i<maxX && i<secondSNPPos.length; i++) {
				dSecondSNPPos[i]= (secondSNPPos[i] * 100d) / totalPos[i];
				if(dSecondSNPPos[i] > secondMaxY) { secondMaxY = dSecondSNPPos[i]; }
			}
			double[][] secondSNPData = new double [][] {dSecondSNPPos};
			
			String title = String.format("First Read SNP frequencies ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(firstSNPData, 0d, firstMaxY+firstMaxY*0.1, "Position in Read (bp)", "Frequency (%)", snpName, xCategories, title));
			
			String title2 = "Second Read SNP frequencies";
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(secondSNPData, 0d, secondMaxY+secondMaxY*0.1, "Position in Read (bp)", "Frequency (%)", snpName, xCategories, title2));
		} else {
			resultsPanel.setLayout(new GridLayout(1,1));
			String title = String.format("Read SNP frequencies ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(firstSNPData, 0d, firstMaxY+firstMaxY*0.1, "Position in Read (bp)", "Frequency (%)", snpName, xCategories, title));
		}
		
		return resultsPanel;
	}

	@Override	
	public String name() {
		return "SNP Frequencies";
	}

	@Override	
	public String description() {
		return "Looks at the SNP frequencies in the data";
	}

	@Override	
	public void reset() { }

	@Override	
	public boolean raisesError() {
		double snpPercent = 100.0d*(variantCallDetection.getTotalMutations()) / variantCallDetection.getTotal();
		if(snpPercent > ModuleConfig.getParam("VariantCallPosition_snp_threshold", "error").doubleValue())
			return true;		
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		double snpPercent = 100.0d*(variantCallDetection.getTotalMutations()) / variantCallDetection.getTotal();
		if(snpPercent > ModuleConfig.getParam("VariantCallPosition_snp_threshold", "warn").doubleValue())
			return true;		
		return false;
	}

	@Override	
	public boolean needsToSeeSequences() {
		return false;
	}

	@Override	
	public boolean needsToSeeAnnotation() {
		return false;
	}

	@Override	
	public boolean ignoreInReport() {
		if(ModuleConfig.getParam("SNPFrequencies", "ignore") > 0 || 
		   variantCallDetection == null) 
			return true; 
		
		// compute the totals
		variantCallDetection.computeTotals();
		if(variantCallDetection.getTotalMutations() == 0) 
			return true; 
		
		return false;
	}

	@Override	
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "snp_frequencies.png", "SNP Frequencies", 800, 600);
		
		// write raw data in a report
		if(dFirstSNPPos == null) { return; }
		
		StringBuffer sb = report.dataDocument();
		if(dSecondSNPPos != null) {
			sb.append("Position\t1st_read_SNP_freq\t2nd_read_SNP_freq\n");
			for (int i=0;i<dFirstSNPPos.length;i++) {
				sb.append((i+1));
				sb.append("\t");
				sb.append(dFirstSNPPos[i]);
				sb.append("\t");
				sb.append(dSecondSNPPos[i]);
				sb.append("\n");
			}
		} else {
			sb.append("Position\tRead_SNP_freq\n");
			for (int i=0;i<dFirstSNPPos.length;i++) {
				sb.append((i+1));
				sb.append("\t");
				sb.append(dFirstSNPPos[i]);
				sb.append("\n");
			}
		}
	}
	
}
