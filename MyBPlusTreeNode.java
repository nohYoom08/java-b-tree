package org.dfpl.lecture.database.assignment2.assignment1;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MyBPlusTreeNode {
	
	// Data Abstraction은 예시일 뿐 자유롭게 B+ Tree의 범주 안에서 어느정도 수정가능
	protected MyBPlusTreeNode parent;
	protected ArrayList<Integer> keyList;
	protected ArrayList<MyBPlusTreeNode> children;

    public MyBPlusTreeNode(int m) {
        this.parent = null;
        this.keyList = new ArrayList<Integer>(m);
        this.children = new ArrayList<MyBPlusTreeNode>(m+1);
    }
}
