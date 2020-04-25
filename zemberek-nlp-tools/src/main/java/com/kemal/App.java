package com.kemal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class App {
	
	public static void main(String[] args) {
		
		Morphology morph = new Morphology();
		
		//tryZemberek(morph);
		//morph.tryDisambiguation();
		
	 // Connect mongodb on localhost 27017
 		MongoClient connection = MongoClients.create("mongodb://localhost:27017");
 		MongoDatabase database = connection.getDatabase("covid_hack");
 		MongoCollection<Document> collection = database.getCollection("tweets");
 		
 		System.out.println(collection.countDocuments());
 		
 		//MongoCursor<Document> cursor = collection.find(Filters.eq("hashtag_id", "#şüpheduymuyorum")).iterator();
 		MongoCursor<Document> cursor = collection.find().iterator();
 		
 		try {
 			int i = 0;
 		    while (cursor.hasNext()) {
 		    	
 		    	
 		    	Document doc = cursor.next();
 		    	
 		    	if(doc.containsKey("zemberek_nlp")) {
 		    		continue;
 		    	}
 		    	
 		    	Document zemberekDoc = new Document();
 		    	
 		    	String fullText = (String) doc.get("full_text");
 		    	ObjectId docId = (ObjectId) doc.get("_id");
 		        
 		    	if (fullText.startsWith("RT")) {
 		        	continue;
 		        }
 		    	System.out.println(++i);
 		    	 		    	
 		    	String normalizedText = morph.normalizeSentence(fullText);
 		    	
 		    	zemberekDoc.append("normalized_text", normalizedText);
 		    	
 		    	List<String> tokens = morph.tokenizeSentence(normalizedText);
 		    	
 		    	String tokenizedText = "";
 		    	String clearedText = "";
 		    	String hashtagStr = "";
 		    	String mentionStr = "";
 		    	 		    	
 		    	for(String word: tokens) {
 		    		// Ignore hashtags and mentions
 		    		tokenizedText += word + " ";
 		    		if(word.startsWith("#")){
 		    			hashtagStr += word + " ";
 		    			continue;
 		    		} else if (word.startsWith("@")) {
 		    			mentionStr += word + " ";
 		    			continue;
 		    		}
 		    		clearedText += word + " ";
 		    		
 		    		
 		    	}
 		    	String lemmatizedText = "";
 		    	String lemmatizedTextV2 = "";
 		    	
 		    	if(clearedText.length() > 0) { 		    		
 		    		String[] results = morph.lemmatizeTweetWithDisambiguation(clearedText);	
 		    		lemmatizedText = results[0].replace("UNK", "");
 		    		lemmatizedTextV2 = results[1].replace("UNK", "");
 		    				
 		    	}
 		    	zemberekDoc.append("lemmatized_text", lemmatizedText);
 		    	zemberekDoc.append("lemmatized_text_v2", lemmatizedTextV2);
 		    	
 		    	if (mentionStr.length() < 2) {
 		    		mentionStr = " ";
 		    	} else {
 		    		lemmatizedText += " " + mentionStr;
 		    		lemmatizedTextV2 += " " + mentionStr;
 		    	}
 		    	if (hashtagStr.length() < 2) {
 		    		hashtagStr = " ";
 		    	} else {
 		    		lemmatizedText += " " + hashtagStr;
 		    		lemmatizedTextV2 += " " + hashtagStr;
 		    	}
 		    	
 		    	zemberekDoc.append("mentions", mentionStr);
 		    	zemberekDoc.append("hashtags", hashtagStr);
 		    	
 		    	zemberekDoc.append("tokenized_text", tokenizedText);
 		    	 
 		    	collection.updateOne(Filters.eq("_id", docId), Updates.set("zemberek_nlp", zemberekDoc));
 		    	
 		    }
 		} finally {
 		    cursor.close();
 		}
 		
	    

	}
	
	public static void tryZemberek(Morphology morph) {
		
	    String words = "hemen hemen hergün 70’li sayılar veriliyor ölenler için bu bir tesadüf müdür #süepheduymuyorum diyemeyeceğim kanun vs bir hüküm mü var acaba ayar veriliyor";
	    
	    for(String word: words.split(" ")) {	    	
	    	String analysis = morph.analyze(word);
	    	System.out.println("---------------");
	    	System.out.println(analysis);
	    	System.out.println("---------------");
	    }
	    
	    String sentence = "hemen hemen hergün 70’li sayılar veriliyor ölenler için bu bir tesadüf müdür #süepheduymuyorum diyemeyeceğim kanun vs bir hüküm mü var acaba ayar veriliyor";
	    String analysisInformal = morph.analyzeInformal(sentence);
	    System.out.println(analysisInformal);
	    
	    for(String word: sentence.split(" ")) {	    	
	    	String analysis = morph.normalizeWord(word);
	    	System.out.println("---------------");
	    	System.out.println(analysis);
	    	System.out.println("---------------");
	    }
	    
	    String sentenceToNormalize = "teknıker aliminda magdurum";
	    String normalizedSentence = morph.normalizeSentence(sentenceToNormalize);
	    System.out.println(normalizedSentence);

	}
	
	public static String removeUrl(String commentstr)
    {
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        int i = 0;
        while (m.find()) {
            commentstr = commentstr.replaceAll(m.group(i),"").trim();
            i++;
        }
        return commentstr;
    }

}
