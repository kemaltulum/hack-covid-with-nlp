package com.kemal;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.AnalysisFormatters;
import zemberek.morphology.analysis.InformalAnalysisConverter;
import zemberek.morphology.analysis.SentenceAnalysis;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.lexicon.RootLexicon;
import zemberek.normalization.TurkishSentenceNormalizer;
import zemberek.normalization.TurkishSpellChecker;
import zemberek.tokenization.Token;
import zemberek.tokenization.TurkishTokenizer;
import zemberek.tokenization.antlr.TurkishLexer;

public class Morphology {
	TurkishMorphology morphology;
	TurkishSentenceNormalizer normalizer;
	TurkishTokenizer tokenizer;
	TurkishSpellChecker spellChecker;
	
	public Morphology() {
		// TODO Auto-generated constructor stub
		this.morphology = TurkishMorphology.createWithDefaults();

		Path lookupRoot = Paths.get("/home/kemal/.nlp/zemberek/normalization");
		Path lmFile = Paths.get("/home/kemal/.nlp/zemberek/lm/lm.2gram.slm");
		try {
			this.normalizer = new
			    TurkishSentenceNormalizer(this.morphology, lookupRoot, lmFile);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.tokenizer = TurkishTokenizer.builder().
	    		ignoreTypes(Token.Type.Punctuation,Token.Type.NewLine, Token.Type.SpaceTab).build();
		
		try {
			this.spellChecker = new TurkishSpellChecker(this.morphology);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String analyze(String word) {
		String resultString = "";
		WordAnalysis results = morphology.analyze(word);
		
		resultString += word + ":\n";
		
		for (SingleAnalysis result : results) {
			resultString += result.formatLong() + "\n";
			resultString += "Stems: " + result.getStems() + "\n";
			resultString += "Lemmas: " + result.getLemmas() + "\n";
			resultString += "Lexical Format: " + result.formatLexical() + "\n";
			resultString += "Oflazer Style: " + AnalysisFormatters.OFLAZER_STYLE.format(result) + "\n";
		}
		
		return resultString;
	}
	
	public String analyzeInformal(String sentence) {
		
		String resultString = "";
		resultString += sentence + ":\n";
		
		TurkishMorphology morphology = TurkishMorphology.builder()
		        .setLexicon(RootLexicon.getDefault())
		        .useInformalAnalysis()
		        .build();

	    List<SingleAnalysis> analyses = morphology
	        .analyzeAndDisambiguate(sentence)
	        .bestAnalysis();

	    for (SingleAnalysis a : analyses) {
	      resultString += a.surfaceForm() + "\t:  " + a + "\n";
	    }

	    resultString += "Converting formal surface form:\n";

	    InformalAnalysisConverter converter =
	        new InformalAnalysisConverter(morphology.getWordGenerator());

	    for (SingleAnalysis a : analyses) {
	      resultString += converter.convert(a.surfaceForm(), a) + "\n";
	    }
	    
	    return resultString;
	}
	
	public String normalizeWord(String word) {
		String resultString = "";
		resultString += word + ":\n";
		
		resultString += "Correct form -> " + this.spellChecker.check(word) + "\n";
		
		resultString += "Suggestions - > " + this.spellChecker.suggestForWord(word) + "\n";
		
		return resultString;
	}
	
	public boolean spellCheck(String word) {
		return this.spellChecker.check(word);
	}
	
	public List<String> suggestForWord(String word) {
		return this.spellChecker.suggestForWord(word);
	}
	
	public String normalizeSentence(String sentence) {
		String resultString = "";
		//resultString += sentence + ":\n";
			
		resultString += this.normalizer.normalize(sentence);
				
		return resultString;
	}
	
	public List<String> tokenizeSentence(String sentence) {
		List<String> tokenList = new ArrayList<String>();
		
	    List<Token> tokens = this.tokenizer.tokenize(sentence);
	    for (Token token : tokens) {
	        tokenList.add(token.getText());
	    }
	    	   
	    return tokenList;
	}
	
	public String tokenizeSentenceTest(String sentence) {
		String resultString = "";
		
		resultString += sentence + ":\n";

	    List<Token> tokens = this.tokenizer.tokenize(sentence);
	    for (Token token : tokens) {
	        resultString += token.getText() + " ";
	        //resultString += token.getType() + " ";
	        //System.out.println("Type = " + TurkishLexer.VOCABULARY.getDisplayName(token.getType()));
	    }
	    
	    resultString += "\n";
	    
	    return resultString;
	}
	
	/**
	 * Method to try some words
	 * @param word
	 * @return
	 */
	public List<String> findLemmasOfGivenWord(String word) {
		
		try {
			WordAnalysis results = morphology.analyze(word);
			
			SingleAnalysis result = results.getAnalysisResults().get(0);
			
			List<String> lemmas =  result.getLemmas();
			
			return lemmas;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ArrayList<String>();
		}
		
	}
	
	/**
	 * Method to try some words
	 * @param word
	 * @return
	 */
	public String findLemmaOfGivenWord(String word) {
		
		try {
			WordAnalysis results = morphology.analyze(word);
			
			SingleAnalysis result = results.getAnalysisResults().get(0);
			
			List<String> lemmas =  result.getLemmas();
			
			return lemmas.get(lemmas.size() - 1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return word;
		}
	}
	
	public String lemmatizeTweetWithDisambiguation(String tweet) {
		String resultString = "";
		List<WordAnalysis> analysis = this.morphology.analyzeSentence(tweet);
		SentenceAnalysis after = morphology.disambiguate(tweet, analysis);
		for(SingleAnalysis newAnalysis: after.bestAnalysis()) {
			List<String> lemmas = newAnalysis.getLemmas();
			String lemma = newAnalysis.getDictionaryItem().lemma;
			resultString += lemma + " ";
		}
		return resultString;
	}
	
	public void tryDisambiguation(String sentence) {
		System.out.println("Sentence  = " + sentence);
		List<WordAnalysis> analysis = this.morphology.analyzeSentence(sentence);

		System.out.println("Before disambiguation.");
		for (WordAnalysis entry : analysis) {
		  System.out.println("Word = " + entry.getInput());
		  for (SingleAnalysis single : entry) {
		    System.out.println(single.formatLong());
		  }
		}

		System.out.println("\nAfter disambiguation.");
		SentenceAnalysis after = morphology.disambiguate(sentence, analysis);
		after.bestAnalysis().forEach(s-> System.out.println(s.formatLong()));

	}
}
