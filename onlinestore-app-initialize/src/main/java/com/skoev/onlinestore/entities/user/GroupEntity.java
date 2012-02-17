package com.skoev.onlinestore.entities.user;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Class that represents a group of users of the online store. These groups are
 * used to administer user access roles via container managed authentication (
 * this approach was tested on Glassfish 3.1.1 server, but it should work for 
 * any Java
 * EE compatible application server). Details follow. 
 * 
 * <br/><br/>
 * The GroupEntity and UserEntity classes are mapped to the database using a
 * ManyToMany JPA relationship with a JoinTable strategy. The UserEntity class
 * results in a table called USERS, where the user name column is called USERNAME
 * and the password column is called PASSWD.
 * The join table is called USERS_GROUPS, where the group name column is called 
 * GROUPNAME.
 * 
 * <br/><br/>
 * On the Glassfish server,a JDBC authentication domain is set up to point to 
 * the abovementioned tables and columns. This domain is configured to use 
 * SHA256Hex hashing for the passwords. 
 * 
 * <br/><br/>
 * The groupnames are mapped to roles in a glassfish-web.xml file, and 
 * security constraints are defined for these roles in the web.xml file. Both of
 * these files are under /WEB-INF/ in the .war module. 
 * 
 * <br/> <br/>
 * Based on these security constraints, the server automatically denies access to 
 * certain resources unless the user is part of a certain group. In addition, 
 * the application programmatically checks the roles of the user and renders
 * the appropriate content for that role. For example, an employee sees different
 * content than a customer. 
 * 
 * @see UserEntity
 */
@Entity
@Table(name = "GROUPS")
public class GroupEntity implements Serializable {

    /**
     * The groupname is the unique ID for this entity
     */
    @Id
    private String groupname;

    public String getGroupname() {
        return groupname;
    }

    public GroupEntity() {
    }

    /**
     * Copy constructor
     * @param groupname 
     */
    public GroupEntity(String groupname) {
        this.groupname = groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    /**
     * Returns true if the argument is a GroupEntity, is not null, 
     * and its groupname field equals this object's groupname field; false
     * otherwise.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupEntity)) {
            return false;
        } else if (groupname == null) {
            return false;
        } else {
            return groupname.equals(((GroupEntity) obj).getGroupname());
        }
    }

    /**
     * Return the hashCode of the groupname field; if that field is null,
     * returns the the parent's hashCode. 
     * @return 
     */
    @Override
    public int hashCode() {
        if (groupname == null) {
            return super.hashCode();
        } else {
            return groupname.hashCode();
        }
    }
}
