/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.initialize;
import java.io.*; 
/**
 *
 * @author stephan
 */
public class ImageReader {
    public byte[] read(String directory, String imageFileName){
        File image = new File(directory, imageFileName); 
        byte[] result = null;
        
        FileInputStream fis = null; 
        try {
            try{
                fis = new FileInputStream(image); 
                BufferedInputStream bis = new BufferedInputStream(fis); 
                result = new byte[bis.available()];
                bis.read(result); 
            }
            finally{
                if (fis!=null)
                fis.close(); 
            }
        } catch (IOException e){
            throw new RuntimeException(e); 
        }          
        
        
        return result; 
        }
    
}
