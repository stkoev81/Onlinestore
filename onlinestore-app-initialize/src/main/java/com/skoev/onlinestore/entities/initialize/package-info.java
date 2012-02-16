/**
 * This package is responsible for initializing the database; the main class is
 * {@link TablesInitializer}. The method {@link TablesInitializer.main()} 
 * must be executed from a plain Java SE
 * environment to populate the online store's database with some products, users, 
 * and user groups before running the web application on the server for the first 
 * time. If the web application is run with an empty database, there would be 
 * errors because the user groups are not defined (and of course, there will be 
 * no products to display). 
 * 
 * <br/><br/>
 * 
 * To configure the database connection settings, 
 * a persistence.xml file is needed on the classpath. If building with Maven, this 
 * file should be placed in the directory /src/main/resources/META-INF.
 * 
 * <br/><br/>
 *
 * This package and the package {@link com.skoev.onlinestore.admin} are not part
 * of the web application and are meant to be executed from a Java SE environment
 * in order to configure the database. They are meant for a technical user, such 
 * as a system administrator, and not for the customer or employee of the online
 * store. 
 * 
 * 
 * @see com.skoev.onlinestore.admin
 * 
 */
package com.skoev.onlinestore.entities.initialize;
