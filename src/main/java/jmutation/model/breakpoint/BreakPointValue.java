/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package jmutation.model.breakpoint;

import java.util.List;

import jmutation.model.value.GraphNode;
import jmutation.model.value.VarValue;

/**
 * @author LLT
 * 
 */
public class BreakPointValue extends VarValue {
	private static final long serialVersionUID = -8762384056186966652L;
	private String name;
	
	public BreakPointValue(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof BreakPointValue){
			BreakPointValue otherVal = (BreakPointValue)obj;
			return otherVal.name.equals(this.name);
		}
		
		return false;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public void setChildren(List<VarValue> children){
		this.children = children;
	}

	@Override
	protected boolean needToRetrieveValue() {
		return false;
	}
	

	public int getNumberOfAvailableVariables() {
		if (children == null || children.isEmpty()) {
			return 0;
		} else {
			return children.size();
		}
	}

	@Override
	public boolean match(GraphNode node) {
		return true;
	}
	
	@Override
	public boolean isTheSameWith(GraphNode nodeAfter) {
		return true;
	}
	
	@Override 
	public BreakPointValue clone(){
		return this;
	}

	@Override
	public String getHeapID() {
		// TODO Auto-generated method stub
		return null;
	}
}
