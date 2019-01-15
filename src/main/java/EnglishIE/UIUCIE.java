package EnglishIE;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReaderWithTrueCaseFixer;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.MentionAnnotator;
import org.cogcomp.re.IOHelper;
import org.cogcomp.re.RelationAnnotator;

import java.io.File;
import java.util.Properties;
import java.util.List;

import static org.junit.Assert.*;

public class UIUCIE {

    public static void testAnnotator(){
        File modelDir = null;
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            modelDir = ds.getDirectory("org.cogcomp.re", "ACE_TEST_DOCS", 1.1, false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            ACEReaderWithTrueCaseFixer aceReader = new ACEReaderWithTrueCaseFixer(modelDir.getAbsolutePath() + File.separator + "ACE_TEST_DOCS", false);
            POSAnnotator pos_annotator = new POSAnnotator();
            ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            RelationAnnotator relationAnnotator = new RelationAnnotator();
            for (TextAnnotation ta : aceReader){
                ta.addView(pos_annotator);
                chunker.addView(ta);
                stanfordDepHandler.addView(ta);
                mentionAnnotator.addView(ta);
                relationAnnotator.addView(ta);
                View mentionView = ta.getView(ViewNames.MENTION);
                assertTrue(mentionView.getConstituents().size() > 0);
                View relationView = ta.getView(ViewNames.RELATION);
                assertTrue(relationView.getRelations().size() > 0);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

//        testAnnotator();


        String text = "The Davao Medical Center, a regional government hospital, recorded 19\n" +
                "deaths with 50 wounded. Medical evacuation workers however said the\n" +
                "injured list was around 114, spread out at various hospitals.";

//        String text = "Morris is employed by TSMC."

        text = text.replaceAll("\n", "");

//        String corpus = "story";
//        String textId = "001";

//        TextAnnotation ta = stab.createTextAnnotation(corpus, textId, text);

        //Use Annotators or pipeline to annotate required Views:
        //POS, SHALLOW_PARSE, DEPENDENCY_STANFORD

        POSAnnotator pos_annotator = new POSAnnotator();
        ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
        chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
        stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
        MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
        RelationAnnotator relationAnnotator = new RelationAnnotator();

        // Create a TextAnnotation From Text
        TextAnnotationBuilder stab =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = stab.createTextAnnotation(text);

        try {
            ta.addView(pos_annotator);
            chunker.addView(ta);
            stanfordDepHandler.addView(ta);
            mentionAnnotator.addView(ta);
            relationAnnotator.addView(ta);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        View mentionView = ta.getView(ViewNames.MENTION);
        View relationView = ta.getView(ViewNames.RELATION);

        List<Constituent> predictedMentions = mentionView.getConstituents();
        List<Relation> predictedRelations = relationView.getRelations();

//        for (Constituent m: predictedMentions) {
//            System.out.println(m);
//        }

        for (Relation r : predictedRelations){
            IOHelper.printRelation(r);
            r.toString();
        }

    }

}
