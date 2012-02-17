package com.skoev.onlinestore.entities.initialize;
import java.io.*; 

/**
 * Class responsible for reading image file
 */
public class ImageReader {
    /**
     * Reads an image file as a byte array. If there is an IOException during 
     * the operation, it is rethrown as a RuntimeException. 
     * @param directory Directory where the file is located
     * @param imageFileName The name of the file
     * @return 
     */
    public byte[] read(String directory, String imageFileName){
        File image = new File(directory, imageFileName); 
        byte[] result = null;
        
        FileInputStream fis = null; 
        try {
            try {
                fis = new FileInputStream(image); 
                BufferedInputStream bis = new BufferedInputStream(fis); 
                result = new byte[bis.available()];
                bis.read(result); 
            }
            finally {
                if (fis != null)
                fis.close(); 
            }
        } catch (IOException e){
            throw new RuntimeException(e); 
        }          
        
        return result; 
        }
}
