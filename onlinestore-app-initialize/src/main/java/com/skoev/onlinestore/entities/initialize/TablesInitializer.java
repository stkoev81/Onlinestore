package com.skoev.onlinestore.entities.initialize;

import java.io.File;
import org.w3c.dom.*;
import com.skoev.onlinestore.entities.product.*;
import com.skoev.onlinestore.entities.user.*;
import java.util.*;
import java.math.BigDecimal;

/**
 * This is the main class in the package 
 * {@link com.skoev.onlinestore.entities.initialize}. 
 * It is responsible for initializing the database. When the {@link #main} 
 * method is executed, it populates the database 
 * with products, user groups, users, and settings. Initialization data is 
 * read from a directory that is provided as a command line argument to {@link #main}
 * 
 * <br/><br/>
 * The JPA persistence unit for this class (persistence.xml) must be named "PU_Clean". 
 * It should be set up to use the "Drop and create tables" strategy. That way, 
 * even if the database is not empty, it will be overwritten with the proper
 * initialization data. 
 */
public class TablesInitializer {

    private EntityAccessor entityAccessor = new EntityAccessor("PU_Clean");
    private XmlParser xmlParser = new XmlParser();
    private ImageReader imageReader = new ImageReader();

    /**
     * Initializes database with products, user groups, users, and settings. 
     * Initialization data is read from a directory given as a command line 
     * argument. The directory must contain the following files: 
     * groups.xml, users.xml, and products.xml. In addition, it must contain
     * product images 
     * whose names match the fileName tags in products.xml. Examples for 
     * these XML files follow. Basically, their structrue follows the 
     * structrue of {@link com.skoev.onlinestore.entities.user.GroupEntity}, 
     * {@link com.skoev.onlinestore.entities.user.UserEntity}, and 
     * {@link com.skoev.onlinestore.entities.product.ProductEntity}. 
     * <pre>
     * {@literal
     * --------------------Required groups.xml content-------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <groups>
        <groupname>CUSTOMER</groupname>
        <groupname>MANAGER</groupname>
        <groupname>WAREHOUSE_WORKER</groupname>
        <groupname>ACCOUNTANT</groupname>
        <groupname>DEMO</groupname>
     * </groups>
     * 
     * --------------------Example users.xml content---------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <users>
     *       <user>
            <username>Stefan</username>
            <passwd>somepasswd</passwd>	
            <groupname>CUSTOMER</groupname>
            <groupname>MANAGER</groupname>
            <groupname>WAREHOUSE_WORKER</groupname>
            <groupname>ACCOUNTANT</groupname>
      *      </user>
      *</users>
     *      
     * --------------------Example products.xml content-------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <products>
      <product>
            <productType>Book</productType>
            <price>10.35</price>
            <numberAvailable>20</numberAvailable>
            <shippingWeight>1.0</shippingWeight>	
            <image>
                    <fileName>9780375760303.png</fileName>
                    <imageName>cover</imageName>			
            </image>
            <author>Alexandre Dumas </author>
            <title> The Count of Monte Cristo</title>
            <summary> A popular bestseller ...</summary>
            <isbn>9780375760303</isbn>			
        </product>

      <product>
            <productType>CD</productType>
            <price>9.00</price>
            <numberAvailable>13</numberAvailable>
            <shippingWeight>1.0</shippingWeight>	
            <image>
                    <fileName>sinatra.jpg</fileName>
                    <imageName>cover</imageName>			
            </image>
            <artist>Frank Sinatra</artist>
            <title>Sinatra: Best of the Best</title>
            <biography>Only ...</biography>
       </product>
     * </products>
     * 
     * }</pre>
     * @param args Directory containing the initialization data. The directory
     * syntax is platform dependent. 
     */
    public static void main(String[] args) {
        String initDir = "/media/Data/Current/CS/project_ideas/bookstore"
                + "/images";
        if (args.length == 0) {
            System.out.println("Warning: No directory provided for the xml "
                    + "initializtion files. The default will be used.");
        } else {
            initDir = args[0];
        }
        System.out.println("The xml initialization files will be loaded from the"
                + "following directory: ");
        System.out.println(initDir);
        //  new TablesInitializer();

        new TablesInitializer().initialize(initDir);

    }
    /**
     * Initializes the database; this method is called by {@link #main}
     *  
     */
    private void initialize(String initDir) {
        initializeProducts(initDir, "products.xml");
        initializeSettings();
        if (initializeGroups(initDir, "groups.xml")) {
            initializeUsers(initDir, "users.xml");
        }
    }

    /**
     * Initializes the products table in the database; this method is called by
     * {@link #initialize}
     *  @return true if the initialization file was found, false otherwise 
     */
    private boolean initializeProducts(String dir, String filename) {
        File file = new File(dir, filename);
        if (!file.exists()) {
            return false;
        }
        NodeList nodeList = xmlParser.parseFile(file).getElementsByTagName(
                "product");
        //loop through all product XML elements
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elementNode = (Element) nodeList.item(i);
            ProductEntity product = null;
            String value = null;

            String type = getSubElementValue(elementNode, "productType");
            if ("Book".equals(type)) {
                BookEntity entity = new BookEntity();
                entity.setProductType("Book");
                value = getSubElementValue(elementNode, "author");
                entity.setAuthor(value);
                value = getSubElementValue(elementNode, "title");
                entity.setTitle(value);
                entity.setName(value);
                value = getSubElementValue(elementNode, "summary");
                entity.setSummary(value);
                value = getSubElementValue(elementNode, "isbn");
                entity.setIsbn(value);
                product = entity;
            }
            if ("CD".equals(type)) {
                CDEntity entity = new CDEntity();
                entity.setProductType("CD");
                value = getSubElementValue(elementNode, "artist");
                entity.setArtist(value);
                value = getSubElementValue(elementNode, "title");
                entity.setTitle(value);
                entity.setName(value);
                value = getSubElementValue(elementNode, "biography");
                entity.setBiography(value);
                product = entity;
            }

            // retrieve values from XML to set values in entity
            value = getSubElementValue(elementNode, "price");
            product.setPrice(BigDecimal.valueOf(Double.valueOf(value)));
            product.setLastModifiedDate(new Date());
            product.setLastModifiedBy("Stefan");
            if (product.getName() == null) {
                value = getSubElementValue(elementNode, "name");
                product.setName(value);
            }
            value = getSubElementValue(elementNode, "numberAvailable");
            Integer initNumber = Integer.valueOf(value);
            ProductAvailabilityEntity numbers = product.getNumbers();
            numbers.setNumberInWarehouse(initNumber);
            numbers.setNumberAvailable(initNumber);
            numbers.setNumberInCarts(0);
            numbers.setNumberInUnprocessedOrders(0);
            entityAccessor.persistEntity(numbers);
            value = getSubElementValue(elementNode, "shippingWeight");
            product.setShippingWeight(Double.valueOf(value));
            //loop throuth all the property XML elements in the current 
            //productTemplate element
            NodeList properties = elementNode.getElementsByTagName("property");
            Map<String, String> propertyMap = new HashMap<String, String>();
            for (int j = 0; j < properties.getLength(); j++) {
                Element property = (Element) properties.item(j);
                String propKey = getSubElementValue(property, "key");
                String propValue = getSubElementValue(property, "value");
                propertyMap.put(propKey, propValue);
            }
            product.setProperties(propertyMap);
            Element elmentNode = (Element) elementNode.getElementsByTagName(
                    "image").item(0);
            String imageName = getSubElementValue(elementNode, "imageName");
            String imageFileName = getSubElementValue(elementNode, "fileName");
            ImageEntity image = new ImageEntity();
            image.setFileName(imageFileName);
            image.setImageName(imageName);
            byte[] content = imageReader.read(dir, imageFileName);
            image.setContent(content);
            image.setFileLength((long) content.length);
            product.setImage(image);
            entityAccessor.persistEntity(product);
        }
        System.out.println("Products Initialized");
        return true;
    }

    /**
     * Initializes the groups table in the database; this method is called by 
     * {@link #initialize}
     *  @return true if the initialization file was found, false otherwise 
     */
    private boolean initializeGroups(String dir, String filename) {
        File file = new File(dir, filename);
        if (!file.exists()) {
            return false;
        }
        NodeList nodeList = xmlParser.parseFile(file).getElementsByTagName(
                "groupname");

        //loop through all groupname XML elements
        for (int i = 0; i < nodeList.getLength(); i++) {
            GroupEntity entity = new GroupEntity();
            Element elementNode = (Element) nodeList.item(i); //groupname
            String value = elementNode.getTextContent();
            entity.setGroupname(value);
            entityAccessor.persistEntity(entity);
        }
        System.out.println("Groups Initialized");
        return true;
    }

    /**
     * Initializes the settings entry in the database; this method is called by 
     * {@link #initialize}. 
     *  
     */
    private void initializeSettings() {
        SettingsEntity settings = new SettingsEntity();
        settings.setId(1);
        settings.setAlertsOn(false);
        settings.setAlertEmailAddress("stkoev81@gmail.com");
        settings.setNotificationsOn(true);
        entityAccessor.persistEntity(settings);
    }

    /**
     * Initializes the groups table in the database; this method is called by 
     * {@link #initialize}
     * @return true if the initialization file was found, false otherwise 
     */    
    private boolean initializeUsers(String dir, String filename) {
        File file = new File(dir, filename);
        if (!file.exists()) {
            return false;
        }
        NodeList nodeList = xmlParser.parseFile(file).getElementsByTagName(
                "user");

        //loop through all user XML elements
        for (int i = 0; i < nodeList.getLength(); i++) {
            UserEntity entity = new UserEntity();
            Element elementNode = (Element) nodeList.item(i); //user nodes

            String value = getSubElementValue(elementNode, "username");
            entity.setUsername(value);
            System.out.println(">>>>>>>>>" + value);

            value = getSubElementValue(elementNode, "passwd");
            entity.setPasswd(value);

            NodeList groups = elementNode.getElementsByTagName("groupname");
            List<GroupEntity> groupMemberships = new LinkedList<GroupEntity>();

            //loop through all group XML elements under user
            for (int j = 0; j < groups.getLength(); j++) {
                Element group = (Element) groups.item(j);
                String groupname = group.getTextContent();
                groupMemberships.add(new GroupEntity(groupname));
                System.out.println("--------" + groupname + "---------------");
            }
            entity.setGroupMemberships(groupMemberships);
            entity.setAcctCreationDate(new Date());
            entity.setActivated(true);
            if (groupMemberships.contains(new GroupEntity("DEMO"))) {
                addDummyUserInfo(entity);
            }
            entityAccessor.persistEntity(entity);
        }
        System.out.println("Users Initialized");
        return true;
    }

    /** 
     * Fills in userEntity with some dummy info. This method id called by 
     * {@link #initializeUsers} for user accounts that are members of the DEMO 
     * group. 
     * @param userEntity 
     */
    private void addDummyUserInfo(UserEntity userEntity) {
        AddressEntity address = new AddressEntity();
        address.setStreet("123 City Str, Apt. 3 ");
        address.setCity("New York");
        address.setAddressState("New York");
        address.setZip("12345");
        address.setCountry("USA");
        address.setPhone("123 123 1234");
        UserInfoEntity ui = new UserInfoEntity();
        ui.setShippingAddress(address);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2019);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        ui.setCardExpirationDate(cal.getTime());
        ui.setCardNumber("123456789");
        ui.setCardType("MasterCard");
        ui.setEmail("email@example.com");
        ui.setFirstName("Joe");
        ui.setLastName("Shopper");
        ui.setSameAsShipping(true);
        ui.setHasUser(true);
        userEntity.setUi(ui);
    }

    /**
     * Helper method for getting data from the XML DOM.
     * @param element
     * @param subElementName
     * @return 
     */
    private String getSubElementValue(Element element, String subElementName) {
        NodeList nodeList = element.getElementsByTagName(subElementName);
        Node subElement = nodeList.item(0);
        if (subElement == null) {
            return null;
        }
        return subElement.getTextContent().trim();
    }
}
