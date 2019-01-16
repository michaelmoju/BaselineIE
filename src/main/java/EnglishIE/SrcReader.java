package EnglishIE;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class SrcReader {

    private static final String ACE2005CORPUS = "/media/moju/data/Data/Corpus/LDC2006T06/data/English";
    private static final String NYTCORPUS = "/media/moju/data/Data/Corpus/NYT";
    private String corpusHomeDir;
    private String corpusName;
    private ACEReader aceReader;

    public SrcReader(String corpusName) throws Exception{
        this.corpusName = corpusName;
        if(this.corpusName.equals("ACE05")) {
            this.corpusHomeDir = ACE2005CORPUS;
        } else if(this.corpusName.equals("nyt")) {
            this.corpusHomeDir = NYTCORPUS;
        }

        this.aceReader = new ACEReader(this.corpusHomeDir, false);

    }

    public static void write2json(String docID, String documentID, String headLine, List sentences) throws Exception{


        JSONObject obj = new JSONObject();
        JSONArray snts = new JSONArray();


        for (int n=0; n<sentences.size(); n++) {
            snts.add(n, sentences.get(n));
        }

        obj.put("docID", docID);
        obj.put("documentID", documentID);
        obj.put("sentences", snts);
//        obj.put("headLine", headLine);
//        obj.put("sentences", sentences);
        JSONValue.toJSONString ( obj );

        //Create file directory
        File file = new File("./" + docID + ".json");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter myWriter = new FileWriter(file);
        myWriter.write(obj.toJSONString());
        System.out.print(".");
        myWriter.close();

    }

    public void writeAll() throws Exception {
        for (TextAnnotation doc: this.aceReader) {
            write2json(
                    doc.getId(), doc.getAttribute("documentID"),
                    doc.getAttribute("headLine"), doc.sentences());
        }
    }

    public static void main(String[] args) {

        try {
            SrcReader mySrcReader = new SrcReader("ACE05");
            mySrcReader.writeAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
