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

public class BenchReader {
    private static int m_debug = 1;

    private static final String ACE2005CORPUS = "/media/moju/data/Data/Corpus/LDC2006T06/data/English";
    private static final String NYTCORPUS = "/media/moju/data/Data/Corpus/NYT";
    private String corpusHomeDir;
    private String corpusName;

    public BenchReader(String corpusName) {
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


//                SpanLabelView entityView = (SpanLabelView) doc.getView(ViewNames.MENTION_ACE);
//
//                CoreferenceView coreferenceView = (CoreferenceView) doc.getView(ViewNames.COREF_HEAD);
//
//                CoreferenceView coreferenceExtentView = (CoreferenceView) doc.getView(ViewNames.COREF_EXTENT);
//
//                System.out.println(coreferenceView);
//
//                System.out.println(coreferenceExtentView);

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

//                for (ACEEntityMention m: entityMentionList) {
//                    System.out.println(m.extent);
//                    System.out.println(m.extentStart + " " +m.extentEnd);
//                    System.out.println(m.ldcType);
//                }

//                for (Constituent mention : entityView.getConstituents()) {
////                    int startTokenId =
////                            doc.getTokenIdFromCharacterOffset(
////                                    Integer.parseInt(
////                                            mention.getAttribute(ACEReader.EntityHeadStartCharOffset)));
////                    int endTokenId =
////                            doc.getTokenIdFromCharacterOffset(
////                                    Integer.parseInt(
////                                            mention.getAttribute(ACEReader.EntityHeadEndCharOffset)) - 1) + 1;
////                    System.out.println(mention);
////                    System.out.println(startTokenId + " " + endTokenId);
////
////                }




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

    public void write2json(TextAnnotation doc, String docID, List<ACEEntity> entityList, List<ACERelation> relationList, List<ACEEvent> eventList) throws Exception{

        JSONObject obj = new JSONObject();

        JSONArray entityObjList = new JSONArray();
        JSONArray relationObjList = new JSONArray();
        JSONArray eventObjList = new JSONArray();

        //Put entity object
        for (ACEEntity entity: entityList) {
            JSONObject entityObj = new JSONObject();
            JSONArray entityMentionList = new JSONArray();
            entityObj.put("entityID", entity.id);
            entityObj.put("entityType", entity.type);
            entityObj.put("entitySubType", entity.subtype);

            for (ACEEntityMention mention: entity.entityMentionList) {
                JSONObject entityMenitonObj = new JSONObject();
                entityMenitonObj.put("id", mention.id);
                entityMenitonObj.put("extent", mention.extent);
                entityMenitonObj.put("position", doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(mention.extentStart)).getSentenceId());
                entityMentionList.add(entityMenitonObj);
            }
            entityObj.put("entityMentionList", entityMentionList);
            entityObjList.add(entityObj);
        }

        //Put relation object
        for (ACERelation relation: relationList) {
            JSONObject relationObj = new JSONObject();
            JSONArray relationMentionList = new JSONArray();

            relationObj.put("relationID", relation.id);
            relationObj.put("relationType", relation.type);
            relationObj.put("relationSubType", relation.subtype);

            //Put relationArgument List
            for (ACERelationArgument relationArg: relation.relationArgumentList) {
                if (relationArg.role.equals("Arg-1")) {
                    relationObj.put("relationArg1", relationArg.id);
                } else if (relationArg.role.equals("Arg-2")) {
                    relationObj.put("relationArg2", relationArg.id);
                }
            }

            assert relationObj.containsKey("relationArg1");
            assert relationObj.containsKey("relationArg2");

            //Put relationMentionList
            for (ACERelationMention relationMention: relation.relationMentionList){
                JSONObject relationMenitonObj = new JSONObject();
                relationMenitonObj.put("id", relationMention.id);
                relationMenitonObj.put("extent", relationMention.extent);
                relationMenitonObj.put("position", doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(relationMention.extentStart)).getSentenceId());
                for (ACERelationArgumentMention argMention : relationMention.relationArgumentMentionList) {
                    if (argMention.role.equals("Arg-1")) {
                        JSONObject mentionArgObj1 = new JSONObject();
                        mentionArgObj1.put("id",argMention.id);
                        mentionArgObj1.put("extent", argMention.argStr);
                        mentionArgObj1.put("position", doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(argMention.start)).getSentenceId());
                        relationMenitonObj.put("mentionArg1", mentionArgObj1);
                    } else if (argMention.role.equals("Arg-2")) {
                        JSONObject mentionArgObj2 = new JSONObject();
                        mentionArgObj2.put("id",argMention.id);
                        mentionArgObj2.put("extent", argMention.argStr);
                        mentionArgObj2.put("position", doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(argMention.start)).getSentenceId());
                        relationMenitonObj.put("mentionArg2", mentionArgObj2);
                    }
                }
                relationMentionList.add(relationMenitonObj);
            }
            relationObj.put("relationMentionList", relationMentionList);

            relationObjList.add(relationObj);
        }

        //Put event object
        for (ACEEvent event: eventList) {
            JSONObject eventObj = new JSONObject();
            eventObj.put("eventID", event.id);
            eventObj.put("eventType", event.type);
            eventObj.put("eventSubType", event.subtype);

            //Put eventArgObj in eventArgList
            JSONArray eventArgList = new JSONArray();
            for (ACEEventArgument eventArg: event.eventArgumentList) {
                JSONObject eventArgObj = new JSONObject();
                eventArgObj.put("id", eventArg.id);
                eventArgObj.put("role", eventArg.role);
                eventArgList.add(eventArgObj);
            }
            eventObj.put("eventArgList", eventArgList);

            //Put eventMentionObj in eventMentionList
            JSONArray eventMentionList = new JSONArray();
            for (ACEEventMention eventMention: event.eventMentionList) {
                JSONObject eventMentionObj = new JSONObject();
                eventMentionObj.put("id", eventMention.id);
                eventMentionObj.put("extent", eventMention.extent);
                eventMentionObj.put("position", doc.getSentenceFromToken(doc.getTokenIdFromCharacterOffset(eventMention.extentStart)).getSentenceId());
                eventMentionObj.put("anchor", eventMention.anchor);

                JSONArray eventMentionArgList = new JSONArray();
                for (ACEEventArgumentMention eventMentionArg: eventMention.eventArgumentMentionList) {
                    JSONObject eventMentionArgObj = new JSONObject();
                    eventMentionArgObj.put("id", eventMentionArg.id);
                    eventMentionArgObj.put("role", eventMentionArg.role);
                    eventMentionArgObj.put("extent", eventMentionArg.argStr);
                    eventMentionArgList.add(eventMentionArgObj);
                }
                eventMentionObj.put("eventMentionArgList", eventMentionArgList);

                eventMentionList.add(eventMentionObj);
            }
            eventObj.put("eventMentionList", eventMentionList);
            eventObjList.add(eventObj);
        }

        obj.put("docID", docID);
        obj.put("entityList", entityObjList);
        obj.put("relationList", relationObjList);
        obj.put("eventList", eventObjList);

        JSONValue.toJSONString ( obj );

        //Create file directory
        File file = new File("./" + docID + ".annotation.json");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter myWriter = new FileWriter(file);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
        myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
//        myWriter.write(obj.toJSONString());
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

                write2json(doc, doc.getId(), annotation.entityList, annotation.relationList, annotation.eventList);
            }

        }
    }


    public static void main(String[] args) {

        BenchReader myBenchReader = new BenchReader("ACE05");
        try {
//            myBenchReader.read();
            myBenchReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
