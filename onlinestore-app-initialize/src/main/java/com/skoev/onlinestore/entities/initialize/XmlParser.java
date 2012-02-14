/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.initialize;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File; 

/**
 *
 * @author stephan
 */
public class XmlParser {
    private DocumentBuilderFactory dbf; 
    private DocumentBuilder db; 
    
    
    public XmlParser()  {
        try{
            dbf = DocumentBuilderFactory.newInstance(); 
            dbf.setNamespaceAware(true); 
            dbf.setIgnoringElementContentWhitespace(true);
            db = dbf.newDocumentBuilder();        
        } catch (Exception e){
            throw new RuntimeException(e); 
        }
    }
    
    public Document getNodeList (File file) {
        Document doc; 
      
        try {
            doc = db.parse(file); 
        } catch (Exception e){
            throw new RuntimeException(e); 
        }
        return doc;
    }
}
