package com.skoev.onlinestore.web;
import java.util.*; 

/**
 * This class represents a pager component, that is, a collection of hyperlinks
 * used to scroll through a large list of items. 
 * 
 * @see PagerLink
 */
public class Pager {
    public Pager(){
    }
    
    /**
     * Constructs a pager object
     * @param numItems total number of items in the list
     * @param ipp  number of items to display per page
     */
    public Pager(int numItems, int ipp){
        this.numItems = numItems;
        this.ipp = ipp;
        
    }
    
    /**
     * The first item from the list to show on the current page; if 
     * firstItem=1, then shows list from 
     * the beginning. 
     */
    private Integer firstItem = 1;     
    /**
     * Total number of items in the list; the combination of numItems and ipp 
     * will determine how many pages are necessary to display the whole list 
     * (and therefore how many links are in the pager)
     * 
     */
    private int numItems; 
    /**
     * Number of items per page to display
     */
    private Integer ipp;
    
    /**
     * Returns last item from the list to show on the current page; 
     * @return 
     */    
    public Integer getLastItem() {
        return (firstItem + ipp -1)>numItems?numItems:(firstItem + ipp -1);
    }
    
    /**
     * If there is only one page to show, this  will return true; 
     * this method can be queried in the UI so that the pager is shown only if
     * there are multiple pages
     * 
     */
    public boolean showPager(){
        if (numItems <= ipp)
            return false; 
        return true; 
    }; 
    
    /**
     * Returns a list of PagerLink objects for this pager; the firstItem and ipp
     * values are obtained from a query string (or default values are used if no
     * query string) and the PagerLink properties are calculated from that. 
     * @return 
     */
    public List<PagerLink> getPageList() {
        List<PagerLink> pageList = new LinkedList<PagerLink>();        
        Integer firstPageParam = 1; 
        Integer numPages = (int) Math.ceil(((double)numItems)/ipp);
        Integer lastPageParam = (numPages -1)*ipp + 1; 
        Integer currentPage = (firstItem-1)/ipp +1; 
        
        Integer prevPageParam, nextPageParam; 
        boolean prevPageRendered, nextPageRendered;  
        
        if (firstItem - ipp < 1){
            prevPageParam = 1; 
            prevPageRendered = false; 
            
        }
        else  {
            prevPageParam = firstItem - ipp; 
            prevPageRendered = true; 
        }
        
        if(firstItem + ipp > numItems){
            nextPageParam = lastPageParam; 
            nextPageRendered = false; 
        }
        else {
            nextPageParam = firstItem + ipp; 
            nextPageRendered = true; 
        }
        
        pageList.add(new PagerLink(0, "Pages: ", true)); 
        pageList.add(new PagerLink(firstPageParam, "first",!prevPageRendered ) ); 
        pageList.add(new PagerLink(lastPageParam, "last",!nextPageRendered )) ;     
        pageList.add(new PagerLink(prevPageParam, "<",!prevPageRendered )); 
        pageList.add(new PagerLink(nextPageParam, ">",!nextPageRendered ));      
        
        for (int i=1; i<=numPages; i++){
            pageList.add(new PagerLink((i-1)*ipp+1, i, i==currentPage)); 
        }    
        
        return pageList; 
    }
      
    public void setFirstItem(Integer firstItem) {
        this.firstItem = firstItem;
    }

    public Integer getFirstItem() {
        return firstItem;
    } 
     
    public Integer getIpp() {
        return ipp;
    }

    public void setIpp(Integer ipp) {
        this.ipp = ipp;
    }

    public int getNumItems() {
        return numItems;
    }

    public void setNumItems(int numItems) {
        this.numItems = numItems;
    }
}
            
    