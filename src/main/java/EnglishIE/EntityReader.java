package EnglishIE;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.JsonObject;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 *
 * ACE_2005 corpus sections:
 *
 *  bn : Broadcast News
 *  nw : Newswire
 *  bc : Broadcast Conversation
 *  wl : Weblog
 *  un : Usenet Newsgroups/Discussion Forum
 *  cts : Conversational Telephone Speech
 *
 *
 * **Put adf.dtd into all the sub-directories.**
 *corpusHomeDir
 * ├── bc
 * │   └── apf.dtd
 * |   └── <other files (*.apf.xml, *.sgm)>
 * ├── bn
 * │   └── apf.dtd
 * |   └── <other files (*.apf.xml, *.sgm)>
 * ├── cts
 * │   └── apf.dtd
 * |   └── <other files (*.apf.xml, *.sgm)>
 * └── nw
 *     └── apf.dtd
 *     └── <other files (*.apf.xml, *.sgm)>
 * ...
 *
 *
 *
 */

public class EntityReader {

    private static int m_debug = 1;

    private static final String ACE2005CORPUS = "/media/moju/data/Data/Corpus/LDC2006T06/data/English";
    private static final String NYTCORPUS = "/media/moju/data/Data/Corpus/NYT";
    private String corpusHomeDir;
    private String corpusName;

    public EntityReader(String corpusName) {
        this.corpusName = corpusName;
        if(this.corpusName.equals("ACE05")) {
            this.corpusHomeDir = ACE2005CORPUS;
        } else if(this.corpusName.equals("nyt")) {
            this.corpusHomeDir = NYTCORPUS;
        }
    }

    public void write2json(TextAnnotation doc, String docID, List<ACEEntity> entityList) throws Exception{

        JSONObject entityObj = new JSONObject();

        //Put entity object
        for (ACEEntity entity: entityList) {
            JSONObject entityObjdata = new JSONObject();
            JSONArray entityMentionList = new JSONArray();
            entityObjdata.put("entityID", entity.id);
            entityObjdata.put("entityType", entity.type);
            entityObjdata.put("entitySubType", entity.subtype);

            for (ACEEntityMention mention: entity.entityMentionList) {
                JSONObject entityMenitonObj = new JSONObject();

                int sentence_start = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(mention.extentStart)).getStartSpan();
                int sentence_end = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(mention.extentStart)).getEndSpan();
                int sentence_length = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(mention.extentStart)).getTokens().length;
                String sentence = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(mention.extentStart)).toString();

                assert sentence_length == sentence_end - sentence_start + 1;
                entityMenitonObj.put("mention_id", mention.id);
                entityMenitonObj.put("sentence_index", doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(mention.extentStart)).getSentenceId());
                entityMenitonObj.put("Sentence", sentence.replaceAll("\n", " "));
                entityMenitonObj.put("sentence_length", sentence_length);
                JSONArray tokens = new JSONArray();
                for (String token: doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(mention.extentStart)).getTokens()){
                    tokens.add(token);
                }
                entityMenitonObj.put("tokens", tokens);
                entityMenitonObj.put("extent", mention.extent);
                entityMenitonObj.put("start", doc.getTokenIdFromCharacterOffset(mention.extentStart) - sentence_start);
                entityMenitonObj.put("end", doc.getTokenIdFromCharacterOffset(mention.extentEnd) - sentence_start);

                entityMentionList.add(entityMenitonObj);
            }
            entityObjdata.put("entityMentionList", entityMentionList);
            entityObj.put(entity.id, entityObjdata);
        }

        //Create file directory
        File file = new File("./" + docID + ".entity.json");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter myWriter = new FileWriter(file);
        ObjectMapper mapper = new ObjectMapper();
        myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entityObj));
        System.out.print(".");
        myWriter.close();
    }

    public void readAll() throws Exception {
        if(this.corpusName.equals("ACE05")) {
            ACEReader myReader = new ACEReader(this.corpusHomeDir, false);
            ReadACEAnnotation.is2004mode = myReader.Is2004Mode();

            while (myReader.hasNext()) {
                /*doc => plain text*/
                TextAnnotation doc = myReader.next();

                /*annotation => annotated data*/
                ACEDocumentAnnotation annotation =
                        ReadACEAnnotation
                                .readDocument(corpusHomeDir + File.separatorChar + doc.getId());
                if(m_debug>2) {
                    System.out.println(doc.getId());
                    System.out.println(doc.getAttributeKeys());
                    System.out.println(doc.getAttribute("documentID"));
                    System.out.println(doc.getAttribute("headLine"));
                    System.out.println(doc.getAvailableViews());

                    System.out.println(doc.getView("SENTENCE"));
                    System.out.println(doc.getView("COREF_EXTENT"));
                }

                write2json(doc, doc.getId(), annotation.entityList);
            }

        }
    }


    public static void main(String[] args) throws Exception{

        EntityReader myEntityReader = new EntityReader("ACE05");
        try {
            myEntityReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
