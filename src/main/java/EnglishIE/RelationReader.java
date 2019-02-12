package EnglishIE;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
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

public class RelationReader {
    private static int m_debug = 1;

    private static final String ACE2005CORPUS = "/media/moju/data/Data/Corpus/LDC2006T06/data/English";
    private static final String NYTCORPUS = "/media/moju/data/Data/Corpus/NYT";
    private String corpusHomeDir;
    private String corpusName;

    public RelationReader(String corpusName) {
        this.corpusName = corpusName;
        if(this.corpusName.equals("ACE05")) {
            this.corpusHomeDir = ACE2005CORPUS;
        } else if(this.corpusName.equals("nyt")) {
            this.corpusHomeDir = NYTCORPUS;
        }
    }


    public void read() throws Exception{
        if(this.corpusName.equals("ACE05")) {
            ACEReader myReader = new ACEReader(this.corpusHomeDir, false);
            ReadACEAnnotation.is2004mode = myReader.Is2004Mode();

            if (myReader.hasNext()) {

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


                List<ACEEntityMention> entityMentionList = new ArrayList<>();
                for (ACEEntity entity : annotation.entityList) {
                    entityMentionList.addAll(entity.entityMentionList);
                }

                /*Relation mentions*/
                int relationMentions = 0;
                for (ACERelation relation : annotation.relationList) {
                    relationMentions += relation.relationMentionList.size();

                }


                // Sort entityMention annotation based on their extent starts
                Collections.sort(entityMentionList, new Comparator<ACEEntityMention>() {
                    @Override
                    public int compare(ACEEntityMention o1, ACEEntityMention o2) {
                        return Integer.compare(o1.extentStart, o2.extentStart);
                    }
                });


                /*Entity*/
                for (ACEEntity entity : annotation.entityList) {

                    if(m_debug>0) {
                        System.out.println(entity.id);
                        System.out.println(entity.type);
                        System.out.println(entity.subtype);
                    }

                    for (ACEEntityMention eMention:entity.entityMentionList) {

                        if(m_debug>0) {
                            System.out.println(eMention.extent);
                            System.out.println(eMention.extentStart);
                            System.out.println(eMention.extentEnd);
                        }
                    }
                }

                /*Relation*/
                for (ACERelation relation : annotation.relationList) {

                    if(m_debug>0) {
                        System.out.println(relation.id);
                        System.out.println(relation.type);
                        System.out.println(relation.subtype);
                    }

                    for (ACERelationMention m: relation.relationMentionList) {
                        if(m_debug>0) {
                            System.out.println(m.extent);
                            System.out.println(m.extentStart);
                            System.out.println(m.extentEnd);
                            System.out.println(doc.getTokenIdFromCharacterOffset(m.extentStart));
                            System.out.println(doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(m.extentStart)).getSentenceId());
                            System.out.println(m.relationArgumentMentionList);
                        }
                    }


                    for (ACERelationArgument a: relation.relationArgumentList) {
                        if(m_debug>0) {
                            System.out.println(a.toString());
                            System.out.println(a.id);
                            System.out.println(a.role);
                        }
                    }
                }
            }
        }
    }

    public void write2json(TextAnnotation doc, String docID, List<ACERelation> relationList) throws Exception{
        JSONArray relationMentionList = new JSONArray();

        for (ACERelation relation: relationList) {
            for (ACERelationMention relationMention: relation.relationMentionList){

                int sentence_start = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(relationMention.extentStart)).getStartSpan();
                int sentence_end = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(relationMention.extentStart)).getEndSpan();
                int sentence_length = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(relationMention.extentStart)).getTokens().length;
                String sentence = doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(relationMention.extentStart)).toString();

                assert sentence_length == sentence_end - sentence_start + 1;

                JSONObject relationMenitonObj = new JSONObject();
                relationMenitonObj.put("id", relationMention.id);
                relationMenitonObj.put("relationID", relation.id);
                relationMenitonObj.put("relationType", relation.type);
                relationMenitonObj.put("relationSubType", relation.subtype);
                relationMenitonObj.put("extent", relationMention.extent.replaceAll("\n", " "));
                relationMenitonObj.put("Sentence", sentence.replaceAll("\n", " "));
                JSONArray tokens = new JSONArray();
                for (String token: doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(relationMention.extentStart)).getTokens()){
                    tokens.add(token);
                }
                relationMenitonObj.put("Tokens", tokens);
                relationMenitonObj.put("sentence_length", sentence_length);
                relationMenitonObj.put("sentence_index", doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(relationMention.extentStart)).getSentenceId());
                for (ACERelationArgumentMention argMention : relationMention.relationArgumentMentionList) {
                    if (argMention.role.equals("Arg-1")) {
                        JSONObject mentionArgObj1 = new JSONObject();
                        mentionArgObj1.put("argMentionid",argMention.id);
                        mentionArgObj1.put("extent", argMention.argStr.replaceAll("\n", " "));
                        mentionArgObj1.put("start", doc.getTokenIdFromCharacterOffset(argMention.start) - sentence_start);
                        mentionArgObj1.put("end", doc.getTokenIdFromCharacterOffset(argMention.end) - sentence_start);
                        relationMenitonObj.put("mentionArg1", mentionArgObj1);
                    } else if (argMention.role.equals("Arg-2")) {
                        JSONObject mentionArgObj2 = new JSONObject();
                        mentionArgObj2.put("argMentionid",argMention.id);
                        mentionArgObj2.put("extent", argMention.argStr.replaceAll("\n", " "));
                        mentionArgObj2.put("start", doc.getTokenIdFromCharacterOffset(argMention.start) - sentence_start);
                        mentionArgObj2.put("end", doc.getTokenIdFromCharacterOffset(argMention.end) - sentence_start);
                        relationMenitonObj.put("mentionArg2", mentionArgObj2);
                    }
                }
                relationMentionList.add(relationMenitonObj);
            }
        }

        //Create file directory
        File file = new File("./" + docID + ".relationMention.json");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter myWriter = new FileWriter(file);
        ObjectMapper mapper = new ObjectMapper();
        myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(relationMentionList));
        System.out.println(".");
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

                write2json(doc, doc.getId(), annotation.relationList);
            }

        }
    }


    public static void main(String[] args) {

        RelationReader myRelationReader = new RelationReader("ACE05");
        try {
//            myBenchReader.read();
            myRelationReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
