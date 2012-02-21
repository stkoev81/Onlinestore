package com.skoev.onlinestore.web;

import com.skoev.onlinestore.ejb.EntityAccessorStateless;
import com.skoev.onlinestore.entities.product.ImageEntity;

import java.io.IOException;
import java.io.OutputStream;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet serves images from the database; it reads the image ID
 * from the request path and looks up the corresponding entity in the database; 
 * then it writes the content type, content length, and binary image content 
 * to the response. 
 * 
 */
@WebServlet(name = "ImageServlet", urlPatterns = {"/ImageServlet/*"})
public class ImageServlet extends HttpServlet {
    /**
     * Injected EJB used for persistence operations. 
     */
    @EJB
    private EntityAccessorStateless entityAccessor;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> 
     * methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request
        , HttpServletResponse response) throws ServletException, IOException {

        Long imageID = Long.valueOf(request.getPathInfo().substring(1));
        ImageEntity imageEntity = entityAccessor.findEntity(
                ImageEntity.class, imageID);
        response.setContentType(getServletContext().getMimeType(
                imageEntity.getFileName()));
        response.setContentLength(imageEntity.getFileLength().intValue());
        OutputStream out = response.getOutputStream();

        try {
            out.write(imageEntity.getContent());
            out.flush();
        } finally {
            out.close();
        }
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request
         , HttpServletResponse response) throws ServletException, IOException {
            
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request
          , HttpServletResponse response) throws ServletException, IOException {
            
        processRequest(request, response);
    }
}
