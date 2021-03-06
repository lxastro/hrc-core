package xlong.urlclassify.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.Classifier;
import xlong.util.MyWriter;
import xlong.wm.evaluater.OntologySingleLabelEvaluater;
import xlong.wm.ontology.OntologyTree;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Texts;
import xlong.wm.sample.converter.TextToSparseVectorConverter;
import xlong.nlp.tokenizer.SingleWordTokenizer;
import xlong.nlp.tokenizer.Tokenizer;
import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.classifier.StuckPachinkoSVMClassifier;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;

public class StuckPachinkoSVMTest {

	public static void main(String[] args) throws Exception {
		
		if (args.length != 3) {
			throw new Exception("Error! Args should be: ontologyFile resultDir parsedFile");
		}
		String ontologyFile = args[0]; //"E:\\longx\\data\\dbpedia_2014.owl";
		String resultDir = args[1]; //"result";
		String parsedFile = args[2]; 
		
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		
		String stopWordsFile = "/data/stopwords.txt";
		TextToSparseVectorConverter.addStopwords(new BufferedReader(new InputStreamReader(StuckPachinkoSVMTest.class.getResourceAsStream(stopWordsFile))));
		
		ClassifierPartsFactory factory = new ClassifierPartsFactory() {
			protected final Tokenizer tokenizer = new SingleWordTokenizer();
			@Override
			public TextToSparseVectorConverter getNewConverter() {
				return new TextToSparseVectorConverter(tokenizer)
					.enableLowerCaseToken()
					.enableStopwords()
					//.enableIDF()
					//.enableTF()
					.enableDetemineByDocFreq()
					.setMinTermFreq(2)
					.setFilterShortWords(1)
					.setIgnoreSmallFeatures(0)
					//.setWordToKeep(100000)
					;
			}
			@Override
			public Classifier getNewClassifier() {
				weka.classifiers.Classifier classifier = new xlong.urlclassify.others.LibLINEAR(); //weka 3-7
				return classifier;
			}
		};	
		

		
		Composite treeComposite, train, test;
		treeComposite = new Composite(parsedFile, new Texts());
		System.out.println(treeComposite.countSample());
		System.out.println(treeComposite.getComposites().size());
		Vector<Composite> composites;
		
		composites = treeComposite.split(new int[] {70, 30}, new Random(123));
		train = composites.get(0);
		System.out.println(train.countSample());
		train.save(resultDir + "/trainText");	
		test = composites.get(1);
		System.out.println(test.countSample());
		test.save(resultDir + "/testText");
		
		train = new Composite(resultDir + "/trainText", new Texts());
		test = new Composite(resultDir + "/testText", new Texts());
		
		SingleLabelClassifier singleLabelClassifier = new StuckPachinkoSVMClassifier(factory);
		System.out.println("train");
		singleLabelClassifier.train(train);
		
		OntologySingleLabelEvaluater evaluater = new OntologySingleLabelEvaluater(singleLabelClassifier, tree);
		System.out.println("test");
		evaluater.evaluate(test);	

		MyWriter.setFile(resultDir + "/evaluate.txt", false);
		MyWriter.writeln("accuracy: " + evaluater.getAccuracy());
		MyWriter.writeln("hamming loss: " + evaluater.getAverHammingLoss());
		MyWriter.writeln("precision: " + evaluater.getAverPrecision());
		MyWriter.writeln("recall: " + evaluater.getAverRecall());
		MyWriter.writeln("f1: " + evaluater.getAverF1());
		MyWriter.close();
		
		MyWriter.writeln("accuracy: " + evaluater.getAccuracy());
		MyWriter.writeln("hamming loss: " + evaluater.getAverHammingLoss());
		MyWriter.writeln("precision: " + evaluater.getAverPrecision());
		MyWriter.writeln("recall: " + evaluater.getAverRecall());
		MyWriter.writeln("f1: " + evaluater.getAverF1());
	}

}
