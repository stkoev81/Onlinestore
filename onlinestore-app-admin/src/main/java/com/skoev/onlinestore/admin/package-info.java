/**
 * This package contains a single class for administering the database. 
 * 
 * To configure the database connection settings, 
 * a persistence.xml file is needed on the classpath. If building with Maven, this 
 * file should be placed in the directory /src/main/resources/META-INF. 
 * 
 * 
 * This package and the package {@link com.skoev.onlinestore.entities.initialize}
 * are not part
 * of the web application and are meant to be executed from a Java SE environment
 * in order to configure the database. They are meant for a technical user, such 
 * as a system administrator, and not for the customer or employee of the online
 * store. 
 * 
 * @see com.skoev.onlinestore.entities.initialize
 */
package com.skoev.onlinestore.admin;
