package com.example.movie.review.analyser.service;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class SentimentAnalysisService {
    private StanfordCoreNLP pipeline;

    @PostConstruct
    public void init() {
        System.out.println("Initializing Stanford CoreNLP (this may take a moment on first data load)...");
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
        System.out.println("Stanford CoreNLP initialized successfully.");
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty() || pipeline == null) return "Neutral";
        int mainSentiment = 0;
        Annotation annotation = pipeline.process(text);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            mainSentiment = sentiment;
        }
        switch (mainSentiment) {
            case 0: return "Negative";
            case 1: return "Negative";
            case 2: return "Neutral";
            case 3: return "Positive";
            case 4: return "Positive";
            default: return "Neutral";
        }
    }
}