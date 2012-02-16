package com.skoev.onlinestore.entities.initialize;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File; 

/**
 * 
 * Class responsible for parsing an XML file. Exceptions thrown in the methods
 * or constructors of this class are caught and rethrown as runtime exception. 
 */
public class XmlParser {
    private DocumentBuilderFactory dbf; 
    private DocumentBuilder db; 
    
    /**
     * Initializes the document builder factory and document builder fields. 
     */
    public XmlParser()  {
        try {
            dbf = DocumentBuilderFactory.newInstance(); 
            dbf.setNamespaceAware(true); 
            dbf.setIgnoringElementContentWhitespace(true);
            db = dbf.newDocumentBuilder();        
        } catch (Exception e){
            throw new RuntimeException(e); 
        }
    }
    
    /**
     * Parses an XML file
     * @param file The file object to be parsed. 
     * @return The generated XML document
     */
    public Document parseFile (File file) {
        Document doc; 
        try {
            doc = db.parse(file); 
        } catch (Exception e){
            throw new RuntimeException(e); 
        }
        return doc;
    }
}
