package com.letbyte.contact.utility;

import com.letbyte.contact.data.model.Contact;

import java.util.ArrayList;
import java.util.Collections;


public class UniqueArrayList extends ArrayList<Contact> {
	
	private ArrayList<Long> cIDList;
	
	UniqueArrayList() {
		cIDList = new ArrayList<>();
	}
	

    @Override
    public boolean add(Contact contactModel) {
    	long cID = contactModel.getId();
    	int index = Collections.binarySearch(cIDList, cID);
    	if(index < 0) {
    		//Not present in list, we can add it
    		cIDList.add(-index-1, cID);
    		return super.add(contactModel);
    	}
    	return false;
    }
}
