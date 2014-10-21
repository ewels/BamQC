package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class RpkmReference extends AbstractQCModule {

	private static final int MEAGBASE = 1000000;
	private static final int KILOBASE = 1000;
	
	private static Logger log = Logger.getLogger(RpkmReference.class);
	
	private boolean raiseError = false;
	private boolean raiseWarning = false;

	private int binNumber = 0;
	private double[] coverage;
	private List<Long> sequenceStarts = new ArrayList<Long>();
	private long binNucleotides = MEAGBASE;
	
	private int readNumber = 0;
	private int errorReads = 0;
	private double maxCoverage;
	private boolean isBinNucleotidesSet = false;
	
	public RpkmReference() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reset() {
		raiseError = false;
		raiseWarning = false;
		readNumber = 0;
		errorReads = 0;
	}
	
	private void setBinNumber(SAMSequenceDictionary samSequenceDictionary) {
		List<SAMSequenceRecord> samSequenceRecords = samSequenceDictionary.getSequences();
		long totalNucleotideNumber = 0;

		for (SAMSequenceRecord samSequenceRecord : samSequenceRecords) {
			sequenceStarts.add(totalNucleotideNumber);

			totalNucleotideNumber += samSequenceRecord.getSequenceLength();

			log.info(String.format("%s sequence length = %d, total = %d", samSequenceRecord.getSequenceName(), samSequenceRecord.getSequenceLength(), totalNucleotideNumber));
		}
		binNumber = (int) (totalNucleotideNumber / MEAGBASE);

		binNumber++;
		
		coverage = new double[binNumber];
		
		log.info(String.format("%d / %d = %d", totalNucleotideNumber, binNumber, binNucleotides));

		isBinNucleotidesSet = true;
	}

	private void recordCoverage(long alignmentStartAbsolute, long alignmentEndAbsolute) {
		int startIndex = (int) (alignmentStartAbsolute / binNucleotides);
		int endIndex = (int) (alignmentEndAbsolute / binNucleotides);
		int index = startIndex;

		log.debug(String.format("startIndex %d endIndex %d", startIndex, endIndex));

		while (index <= endIndex) {
			long binStart = index * binNucleotides;
			long binEnd = (index + 1) * binNucleotides;
			long start = alignmentStartAbsolute > binStart ? alignmentStartAbsolute : binStart;
			long end = alignmentEndAbsolute > binEnd ? binEnd : alignmentEndAbsolute;
			double length = end - start;

			log.info(String.format("binStart %d binEnd %d, start = %d, end %d, length = %d", binStart, binEnd, start, end, (end - start)));
			log.info("index = " + index);
			
			double binCoverage = length / (double) binNucleotides;

			coverage[index] += binCoverage;

			if (coverage[index] > maxCoverage) maxCoverage = coverage[index];

			log.debug(String.format("Start %d - End %d, length %d, index %d, binCoverage %f, ", alignmentStartAbsolute, alignmentEndAbsolute, (end - start), index, binCoverage, coverage[index]));

			if (binCoverage < 0.0) throw new RuntimeException("negative binCoverage");

			index++;
		}
	}

	@Override
	public void processSequence(SAMRecord read) {
		SAMFileHeader header = read.getHeader();
		SAMSequenceDictionary samSequenceDictionary = header.getSequenceDictionary();
		int referenceIndex = read.getReferenceIndex();
		long alignmentStart = read.getAlignmentStart();
		long alignmentEnd = read.getAlignmentEnd();

		log.debug("header = " + header);
		log.info("referenceIndex = " + referenceIndex);
		
		readNumber++;

		if (referenceIndex > -1) {
			if (!isBinNucleotidesSet) {
				setBinNumber(samSequenceDictionary);
			}
			if (alignmentEnd > alignmentStart) {
				long referenceStart = sequenceStarts.get(referenceIndex);
				long alignmentStartAbsolute = alignmentStart + referenceStart;
				long alignmentEndAbsolute = alignmentEnd + referenceStart;

				recordCoverage(alignmentStartAbsolute, alignmentEndAbsolute);
			}
			else {
				errorReads++;
			}
		}
	}

	public double[] getCoverage() {
		return coverage;
	}

	@Override
	public void processFile(SequenceFile file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public JPanel getResultsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		return "Reads per kilobase per Megabase";
	}

	@Override
	public String description() {
		return "Reads per kilobase per Megabase";
	}

	@Override
	public boolean raisesError() {
		return raiseError;
	}

	@Override
	public boolean raisesWarning() {
		return raiseWarning;
	}

	@Override
	public boolean needsToSeeSequences() {
		return true;
	}

	@Override
	public boolean needsToSeeAnnotation() {
		return false;
	}

	@Override
	public boolean ignoreInReport() {
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub
	}

}