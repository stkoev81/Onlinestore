/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.user;

import java.io.Serializable;
import javax.persistence.*; 
import java.util.*; 
/**
 *
 * @author stephan
 */
@Entity @Table(name="GROUPS")
public class GroupEntity implements Serializable {    
    
    @Id     
    private String groupname;    
        
    public String getGroupname() {
        return groupname;
    }

    public GroupEntity() {
    } 
      

    public GroupEntity(String groupname) {
        this.groupname = groupname;
    }
    

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }
    
    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof GroupEntity))
                return false; 
        else if (groupname==null)
                return false; 
        else 
            return groupname.equals(((GroupEntity) obj).getGroupname()); 
    }
    
    @Override
    public int hashCode(){        
        if (groupname==null)
            return super.hashCode();
        else 
            return groupname.hashCode(); 
    }
    

    
}
