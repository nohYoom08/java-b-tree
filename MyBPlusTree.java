package org.dfpl.lecture.database.assignment2.assignment1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.SortedSet;

@SuppressWarnings("unused")
public class MyBPlusTree implements NavigableSet<Integer> {

	// Data Abstraction은 예시일 뿐 자유롭게 B+ Tree의 범주 안에서 어느정도 수정가능
	public MyBPlusTreeNode root;
	public LinkedList<MyBPlusTreeNode> leafList;
	private int m;
	private int middle,leafMiddle;	//잎새노드가 아닌 경우의 중간점, 잎새노드일 경우의 중간점
	private int height;	//트리의 높이
	private int min_key;	//노드에 들어가야할 최소 노드 개수

	public MyBPlusTree(int m) {
		this.m = m;
        this.root = new MyBPlusTreeNode(m);
        this.leafList = new LinkedList<>();
        this.leafList.add(this.root);
        this.middle = (m-1)/2;
        this.leafMiddle= Math.round((m-1)/2.0f);
        this.height=0;
        this.min_key = Math.round(m /2.0f)-1;
	}

	
	public MyBPlusTreeNode getNode(Integer key) {
		MyBPlusTreeNode tmp=root;	//트리를 순회할 매개 MyBPlusTreeNode 참조변수 tmp선언
		int size=0;
		
		while(tmp!=null) {
			size=tmp.keyList.size();		//해당 노드에 들어있는 키값들 개수
			
			if(!tmp.children.isEmpty())	//자식노드가 있는 경우 (잎새노드가 아닌 경우)
				for(int i=0;i<size;i++) {	//해당노드의 키값이 들어있는 배열 순회
					if(key<tmp.keyList.get(i)) {		//key보다 더 큰 해당노드의 키값을 발견했을 경우
						System.out.print("less than ");
						System.out.println(tmp.keyList.get(i));	//문제 조건 출력
						tmp=tmp.children.get(i);		//해당 키값에서 왼쪽 자식노드로 이동
						break;	//for 루프 종료
					}
					else if (key==tmp.keyList.get(i) || i==size-1) {		
							//key보다 크거나 같은 해당노드의 키값을 발견했을 경우 + 마지막 키값일 경우
						System.out.print("larger than or equal to ");
						System.out.println(tmp.keyList.get(i));	//문제 조건 출력
						tmp=tmp.children.get(i+1);	//해당 키값에서 오른쪽 자식노드로 이동
						break;	//for 루프 종료
					}
				}
			else {		//자식노드가 없는 경우 (잎새노드인 경우)
				for(int i=0;i<size;i++) {
					if (key==tmp.keyList.get(i)) {	//key값과 같은 해당노드의 키값을 발견할 경우
						System.out.print(key);
						System.out.println(" found");	//문제 조건 출력
						return tmp;	//해당노드 반환
					}
				}
				break;	//해당 잎새노드를 순회했는데도 같은 키값을 발견 못한경우 반복문 탈출
			}
		}
		System.out.print(key);
		System.out.println(" not found");
		return null;		//문제 조건 출력 후 null 반환
	}
	
	public void inorderTraverse() {
		inorderTraverse(root);		//재귀 시작 (오버로딩을 활용) 
		System.out.println();
	}
	
	public void inorderTraverse(MyBPlusTreeNode node) {
		
			if(node.children.isEmpty()) {	//잎새노드일 경우
				for(Integer key : node.keyList)		//해당 잎새노드의 키값배열 순회 및 출력
					System.out.println(key);
			}
			
			else {	// 내부노드인 경우
				for(int i=0;i<node.keyList.size()+1;i++) 	
					//자식노드들 전체 대상으로 중위순회 (출력하지 않고 순회만), (키값들 수 + 1) = (자식노드 수)
					inorderTraverse(node.children.get(i));
			}
	}
	
	public int getHeight(MyBPlusTreeNode node) {		//인자의 node부터 잎새노드까지의 높이 계산
		MyBPlusTreeNode tmp=node;
		int tmpHeight=0;		//초기 높이는 0
		
		while(!tmp.children.isEmpty()) {
			tmpHeight++;
			tmp=tmp.children.getFirst();		
			//자식 노드로 이동 (맨 왼쪽 자식으로. b+트리에서 잎새노드를 제외한 나머지 노드들에서 맨 왼쪽 자식은 항상 존재하기에)
		}
		
		return tmpHeight;
	}

	@Override
	public boolean add(Integer e) {
		
		MyBPlusTreeNode lastNode = this.leafList.getLast();
		
		lastNode.keyList.add(e);		//마지막 잎새노드에 키값 추가
		Collections.sort(lastNode.keyList);	//추가 후 정렬
		if (lastNode.keyList.size() == m){
			splitUpLeaf(this.leafList.getLast());	//잎새노드의 키 개수가 b+tree조건을 넘는다면, 잎새노드 분할 시작
		}
		return false;
	}
	
	public void splitUp(MyBPlusTreeNode node) {   //내부노드 분할메소드
		if(node.parent==null) {	//부모노드가 없다면 새로 생성 
			node.parent = new MyBPlusTreeNode(m);
			node.parent.children.add(node);		//생성된 부모노드의 첫번째 자식은 분할될 node
		}
		
		
		
		MyBPlusTreeNode sibling = new MyBPlusTreeNode(m);	//새로운 형제노드 생성
	
		
		
		sibling.keyList= new ArrayList<>(node.keyList.subList(middle+1,m));	
		sibling.children = new ArrayList<>(node.children.subList(middle+1,m+1));
			//분할되는 node의 중간점 다음부터 끝부분까지 새로운 형제노드에 키값들과 자식들 대입
		sibling.parent=node.parent;	//형제노드의 부모자리에 node.parent 대입 
		
		
		
		for(int i=middle+1;i<node.children.size();i++) {		
			node.children.get(i).parent=sibling;		
			//분할될 노드들의 자식들 중, 중간점 다음부터 끝부분까지의 자식들의 부모를 새로운 형제노드로 업데이트
		}
		
	
		node.parent.keyList.add(node.keyList.get(middle)); 	//부모노드에 값 추가
		node.parent.children.add(sibling); //부모노드의 자식으로 새로 생성된 형제노드 추가
		

		
		
		
		node.keyList.subList(middle, m).clear();	 // 분할된 부분 중 형제노드로 복사된 부분들 기존 node에서 삭제 (keyList) 
		node.children.subList(middle+1, m+1).clear();	// 분할된 부분 중 형제노드로 복사된 부분들 기존 node에서 삭제 (children) 
		
		
		int tmpHeight=getHeight(node.parent);	//새로운 부모노드에서 잎새노드까지의 높이 계산
		if(height<tmpHeight) {		//계산한 높이가 현재 트리의 높이보다 높을 시 root와 현재 트리높이 업데이트
			height=tmpHeight;
			root=node.parent;
		}
		
		Collections.sort(node.parent.keyList);	//부모노드 정렬
		if(node.parent.keyList.size()>=m) 	//부모노드의 키값들 size가 방금 전 업데이트로 m값 이상이 된다면 (b+tree의 조건에 어긋난다면)
			splitUp(node.parent); //부모노드의 분할 재귀
	}
	

	public void splitUpLeaf(MyBPlusTreeNode node) {	//잎새노드 분할메소드
		if(node.parent==null) {	//부모노드가 없다면 새로 생성 
			node.parent = new MyBPlusTreeNode(m);
			node.parent.children.add(node);	
		}
		

		
		MyBPlusTreeNode sibling = new MyBPlusTreeNode(m);
		sibling.keyList= new ArrayList<>(node.keyList.subList(leafMiddle,m));	//새로운 형제노드 생성 
		sibling.parent=node.parent;	//형제노드의 부모자리에 node.parent 대입 
	
		
		node.parent.keyList.add(node.keyList.get(leafMiddle)); 	//부모노드에 값 추가
		node.parent.children.add(sibling);	//부모노드의 자식으로 새로 생성된 형제노드 추가
		
		
		node.keyList.subList(leafMiddle, m).clear();		// 분할된 부분 중 형제노드로 복사된 부분들 기존 node에서 삭제 (keyList) 
		
		this.leafList.add(sibling);	//새로생긴 형제노드를 리프노드리스트에 추가
		

		
		int tmpHeight=getHeight(node.parent);	//새로운 부모노드에서 잎새노드까지의 높이 계산
		if(height<tmpHeight) {		//계산한 높이가 현재 트리의 높이보다 높을 시 root와 현재 트리높이 업데이트
			height=tmpHeight;
			root=node.parent;
		}
		
		Collections.sort(node.parent.keyList);	//부모노드 정렬
		if (node.parent.keyList.size()>=m)  //부모노드의 키값들 size가 방금 전 업데이트로 m값 이상이 된다면 (b+tree의 조건에 어긋난다면)
			splitUp(node.parent);	//부모노드의 분할 재귀 (잎새노드는 한번 분할되면 더이상 분할 될 일이 없으므로 splitUpLeaf()가 아닌 splitUp()실행)
	}
	
	
	
	
	

	public MyBPlusTreeNode getNodeForDelete(Integer key) {	//삭제 알고리즘에서 활용하기 위한 getNode() (Assignment4에서의 출력이 없음)
	    MyBPlusTreeNode tmp = root; // 탐색을 시작할 임시 변수로 루트 노드 설정
	    int size = 0; // 현재 노드의 키 리스트 크기를 저장할 변수

	    while (tmp != null) {
	        size = tmp.keyList.size(); // 현재 노드의 키 리스트 크기를 얻음

	        if (!tmp.children.isEmpty()) { // 자식 노드가 있는 경우 (내부 노드)
	            for (int i = 0; i < size; i++) { // 현재 노드의 키 리스트를 순회
	                if (key < tmp.keyList.get(i)) { // 찾는 키가 현재 키보다 작으면
	                    tmp = tmp.children.get(i); //해당 키값에서 왼쪽 자식노드로 이동
	                    break; // for 루프 탈출
	                } else if (key == tmp.keyList.get(i) || i == size - 1) { // 찾는 키와 같거나 마지막 키인 경우
	                    tmp = tmp.children.get(i + 1); //해당 키값에서 오른쪽 자식노드로 이동
	                    break; // for 루프 탈출
	                }
	            }
	        } 
	        else { // 자식 노드가 없는 경우 (리프 노드인 경우)
	            return tmp; // 해당노드 반환 (키가 무조건 있다고 가정)
	        }
	    }
	    return null; // 예외처리
	}
	

	@Override
	public Iterator<Integer> iterator() {	//MyBPlusTree용 이터레이터 오버라이딩
	    return new Iterator<Integer>() {
	        private int leafIndex = 0; // 현재 순회 중인 리프 노드의 인덱스
	        private int keyIndex = 0; // 현재 순회 중인 리프 노드의 키 리스트 인덱스

	        @Override
	        public boolean hasNext() {
	            // 다음 키가 있는지 확인
	            return leafIndex < leafList.size() && keyIndex < leafList.get(leafIndex).keyList.size();
	        }

	        @Override
	        public Integer next() {
	            // 현재 리프 노드에서 키를 가져옴
	            MyBPlusTreeNode currentLeaf = leafList.get(leafIndex);
	            Integer value = currentLeaf.keyList.get(keyIndex); // 현재 키를 가져옴
	            keyIndex++; // 키 인덱스를 증가시킴
	            // 현재 리프 노드의 키를 모두 순회하면 다음 리프 노드로 이동
	            if (keyIndex >= currentLeaf.keyList.size()) {
	                keyIndex = 0; // 키 인덱스를 초기화
	                leafIndex++; // 리프 노드 인덱스를 증가시킴
	            }
	            return value; // 현재 키 값을 반환
	        }
	    };
	}

	@Override
	public boolean remove(Object o) {
	    Integer key = (Integer) o; // 입력을 Integer로 변환
	    
	    MyBPlusTreeNode node = getNodeForDelete(key); // 삭제할 키가 포함된 노드를 찾음
	    
	    if (node == null || !node.keyList.contains(key)) 
	    		return false; // 노드가 없거나 키를 포함하지 않으면 false 반환
	    
	    node.keyList.remove(key); // 키를 리스트에서 제거
	   

	    if (node.keyList.size() < min_key) { // 키 제거 후 노드의 크기가 중간값보다 작아지는 경우
	        balance(node); // 트리 균형 맞추기
	    }
	    
	    setPLV();	//삭제 후 내부노드의 키값들 리프노드에 맞게 수정
	    
	    return true; // 키 제거 성공 시 true 반환
	}
	
	

	private void balance(MyBPlusTreeNode node) {	//노드가 min_key 조건을 어길 경우 이를 맞춰주는 메소드
		
		if(root==node)	//node가 root라면 balance 필요없음
			return;
		
		int index = node.parent.children.indexOf(node);	//node가 node.parent의 몇번 째 자식인지 인덱스 조사
		int leafIndex = leafList.indexOf(node);
		MyBPlusTreeNode LS, RS, tmp;	//오른쪽 형제노드인 RS(Right Sibling), 합병에 사용되어서 새로 추가될 매개노드 tmp
		LS = RS = tmp = null;
		
		
		
		if(index==0)	//node가 node.parent의 맨 첫번째 자식이라면
			RS = node.parent.children.get(index+1);	//RS는 그 다음 인덱스의 자식이므로 작성된 코드와 같이 추가
		else if (index==node.parent.children.size()-1)
			LS = node.parent.children.get(index-1);
		else {
			RS = node.parent.children.get(index+1);	//RS는 그 다음 인덱스의 자식이므로 작성된 코드와 같이 추가
			LS = node.parent.children.get(index-1);
		}
		
		
		if(node.children.isEmpty()) {	//잎새노드에서 balance가 일어나는 경우
			if(LS!=null || RS!=null) {	
				if(LS != null && LS.keyList.size()>min_key) {	//LS에 키값 개수가 min_key보다 많을 때 가져오는 상황
					node.keyList.add(0,LS.keyList.getLast());	//LS의 마지막 키값을 node에 추가
					
					LS.keyList.remove(LS.keyList.size()-1);	//리프노드에서는 맨 왼쪽 키값이 PLV값이므로
					node.parent.keyList.set(index-1,node.keyList.getFirst());	//node.parent에서의 PLV값과 LS의 첫번째 키값 동일하게 설정
				}
				else if(RS != null && RS.keyList.size()>min_key) {	//RS에 키값 개수가 min_key보다 많을 때 가져오는 상황
					node.keyList.add(RS.keyList.getFirst());	//RS의 첫번째 키값을 node에 추가
					
					RS.keyList.remove(0);	//리프노드에서는 맨 왼쪽 키값이 PLV값이므로
					node.parent.keyList.set(index,RS.keyList.getFirst());	//node.parent에서의 PLV값과 RS의 첫번째 키값 동일하게 설정
				}

				else if(LS != null && LS.keyList.size()==min_key){	//LS에 키값 개수가 min_key보다 적을 때 합병하는 상황
					
					tmp = new MyBPlusTreeNode(m);	//합병에 쓰일 매개노드 tmp 동적할당
					
					
					// T, PLV, LS의 키값 합병
					node.parent.keyList.remove(index-1);	//node.parent에남아있는 PLV 삭제 (잎새노드에선 PLV추가가 사실상 없음)
					for(int i=0;i<LS.keyList.size();i++) 	//LS의 키값 추가
						tmp.keyList.add(LS.keyList.get(i));
					
					for(int i=0;i<node.keyList.size();i++) 	//T의 키들 추가
						tmp.keyList.add(node.keyList.get(i));
					
					
					
					if(node.parent==root && node.parent.keyList.size()==0){ 	//node.parent가 root였고 삭제된 이후 키값이 존재하지 않을 때
						root=tmp;	//tmp가 새로운 root가 됨
						height--;
					}
					
					else {
						tmp.parent=node.parent; //tmp의 부모 설정
						
						node.parent.children.remove(index);
						node.parent.children.remove(index-1); //기존의 node.parent에 있던 자식들(node, LS) 삭제
						node.parent.children.add(index-1,tmp);	//tmp를 자식으로 새로 추가 (마지막 자식으로)
					}
					
						leafList.remove(leafIndex);	
						leafList.remove(leafIndex-1);	//기존의 leafList에 있던 노드들(node, RS) 삭제
						leafList.add(leafIndex-1,tmp);	//tmp를 새로 leafList의 해당 인덱스의 노드로 추가
					
					if(root != tmp && root != tmp.parent)	//첫 번째 리프노드와 첫 번째 리프노드의 부모가 모두 root가 아닌 경우
						if(tmp.parent.keyList.size()<min_key)	//부모가 min_key를 지키지 못할 경우, 부모노드에 대해 balance 다시 실행
							balance(tmp.parent);
				}

				else if(RS!=null && RS.keyList.size()==min_key){	//RS에 키값 개수가 min_key보다 적을 때 합병하는 상황
					
					tmp = new MyBPlusTreeNode(m);	//합병에 쓰일 매개노드 tmp 동적할당
					
					// T, PRV, RS의 키값 합병
					for(int i=0;i<node.keyList.size();i++) 	//T의 키들 합병
						tmp.keyList.add(node.keyList.get(i));
					
					for(int i=0;i<RS.keyList.size();i++) 	//RS의 키값 합병 (PRV와 RS의 첫번째 키값과 동일하므로 PRV합병과정은 생략)
						tmp.keyList.add(RS.keyList.get(i));
					
					
					node.parent.keyList.remove(index);	//합병된 PRV 키 삭제
					if(node.parent==root && node.parent.keyList.size()==0){ 	//node.parent가 root였고 삭제된 이후 키값이 존재하지 않을 때
						root=tmp;	//tmp가 새로운 root가 됨
						height--;
					}
					
					else {
						tmp.parent=node.parent; //tmp의 부모 설정
						node.parent.children.remove(index);
						node.parent.children.remove(index); //기존의 node.parent에 있던 자식들(node, RS) 삭제
						node.parent.children.add(index,tmp);	//tmp를 자식으로 새로 추가 (첫 번째 자식으로)
					}
					leafList.remove(leafIndex);	
					leafList.remove(leafIndex);	//기존의 leafList에 있던 노드들(node, RS) 삭제
					leafList.add(leafIndex,tmp);	//tmp를 새로 leafList의 해당 인덱스의 노드로 추가
					
					if(root != tmp && root != tmp.parent)	//첫 번째 리프노드와 첫 번째 리프노드의 부모가 모두 root가 아닌 경우
						if(tmp.parent.keyList.size()<min_key)	//부모가 min_key를 지키지 못할 경우, 부모노드에 대해 balance 다시 실행
							balance(tmp.parent);
				}
			}
		}
		else {	//내부노드에서 balance가 일어나는 경우
			if(LS!=null || RS!=null) {
				if(LS!=null && LS.keyList.size()>min_key) {	//LS에 키값 개수가 min_key보다 많을 때 가져오는 상황
					node.keyList.add(0,node.parent.keyList.getLast());	//형제노드의 첫번째 키값 가져옴
					node.children.add(0,LS.children.getLast());	//형제노드의 첫번째 자식과 연결
					LS.children.getLast().parent=node;
					
					node.parent.keyList.set(index-1,LS.keyList.getFirst());	//부모노드에 오른쪽 자식노드 첫 번째 키값 옮김
				
					
					LS.keyList.remove(LS.keyList.size()-1);	//node에다 갖다준 RS의 키와 자식 삭제
					LS.children.remove(LS.children.size()-1);
				}
				else if(RS!=null && RS.keyList.size()>min_key) {	//RS에 키값 개수가 min_key보다 많을 때 가져오는 상황
					node.keyList.add(node.parent.keyList.get(index));	//형제노드의 첫번째 키값 가져옴
					node.children.add(RS.children.getFirst());	//형제노드의 첫번째 자식과 연결
					RS.children.getFirst().parent=node;
					
					node.parent.keyList.set(index,RS.keyList.getFirst());	//부모노드에 오른쪽 자식노드 첫 번째 키값 옮김
				
					
					RS.keyList.remove(0);	//node에다 갖다준 RS의 키와 자식 삭제
					RS.children.remove(0);
				}
				else if (LS!=null && LS.keyList.size()==min_key) {	//LS에 키값 개수가 min_key보다 적을 때 합병하는 상황
					tmp = new MyBPlusTreeNode(m);
					
					for(int i=0;i<LS.keyList.size();i++)	//LS 추가
						tmp.keyList.add(LS.keyList.get(i));
					
					tmp.keyList.add(node.parent.keyList.get(index-1));	//PLV 추가
					
					//T, LS, PLV 모두 합친 키리스트를 tmp.keyList에 추가 (합병)
					for(int i=0;i<node.keyList.size();i++) 	//T 추가
						tmp.keyList.add(node.keyList.get(i));
					
					
					
					//합병과 동시에 T, RS의 자식들의 부모를 모두 새로운 노드 tmp로 연결
					
					for(int i=0;i<LS.children.size();i++) {
						tmp.children.add(LS.children.get(i));
						LS.children.get(i).parent=tmp;
					}
					for(int i=0;i<node.children.size();i++) {
						tmp.children.add(node.children.get(i));
						node.children.get(i).parent=tmp;
					}
					
					node.parent.keyList.remove(index-1);	//합병된 키 PLV 삭제
					if(node.parent==root && node.parent.keyList.size()==0) { 	//node.parent가 루트였고 아무런 키가 없을 경우
						root=tmp;	//오른쪽 자식이 새로운 루트가 됨
						height--;						
					}
					
					else {
						tmp.parent=node.parent;
						

						node.parent.children.remove(index);
						node.parent.children.remove(index-1); //기존의 node.parent에 있던 자식들(node, LS) 삭제
						node.parent.children.add(index-1,tmp); //tmp를 자식으로 새로 추가 (해당 인덱스의 자식으로)
						
						if(node.parent.keyList.size()<min_key) //부모의 키값개수가 min_key 조건을 만족 못할 시 => 재귀
							balance(node.parent);
					}
					
					
				}
				else if (RS!=null && RS.keyList.size()==min_key) {	//RS에 키값 개수가 min_key보다 적을 때 합병하는 상황
					tmp = new MyBPlusTreeNode(m);
					
					//T, RS, PRV 모두 합친 키리스트를 tmp.keyList에 추가 (합병)
					for(int i=0;i<node.keyList.size();i++) 	//T 추가
						tmp.keyList.add(node.keyList.get(i));
					
					tmp.keyList.add(node.parent.keyList.get(index));	//PRV 추가
					
					for(int i=0;i<RS.keyList.size();i++)	//RS 추가
						tmp.keyList.add(RS.keyList.get(i));
					
					
					//합병과 동시에 T, RS의 자식들의 부모를 모두 새로운 노드 tmp로 연결
					for(int i=0;i<node.children.size();i++) {
						tmp.children.add(node.children.get(i));
						node.children.get(i).parent=tmp;
					}
					
					for(int i=0;i<RS.children.size();i++) {
						tmp.children.add(RS.children.get(i));
						RS.children.get(i).parent=tmp;
					}
					
					
					node.parent.keyList.remove(index);	//합병된 키 PRV 삭제
					if(node.parent==root && node.parent.keyList.size()==0) { 	//node.parent가 루트였고 아무런 키가 없을 경우
						root=tmp;	//왼쪽 자식이 새로운 루트가 됨
						height--;							
					}
					
					else {
						tmp.parent=node.parent;
						node.parent.children.remove(index);
						node.parent.children.remove(index); //기존의 node.parent에 있던 자식들(node, RS) 삭제
						node.parent.children.add(index,tmp); //tmp를 자식으로 새로 추가 (해당 인덱스의 자식으로)
											
						if(node.parent.keyList.size()<min_key)	//부모의 키값개수가 min_key 조건을 만족 못할 시 => 재귀
							balance(node.parent);
					}
					
					
				}
			}
		}
		
	}
	
	public void setPLV() {	//내부노드의 키값들을 잎새노드에 들어있는 데이터에 맞게 정렬
		MyBPlusTreeNode tmp=null;	//순회할 매개노드 tmp
		int index = 0;	//부모의 몇번째 자식인지 알려줄 index
		int key=0;	//내부노드와 비교할 key값
		for(int i=0;i<leafList.size();i++) {
			if(i==0)	//첫번째 자식의 첫 번째 키값은 내부노드에 없음
				continue;
			else if(!leafList.get(i).keyList.isEmpty()){	//비어있는 노드면 일단 넘기기
				key=leafList.get(i).keyList.getFirst(); //각 자식의 첫번째 키값 (내부노드에 기록되는 키값)
				index = leafList.get(i).parent.children.indexOf(leafList.get(i)); //현재 잎새노드가 몇번째 자식인지
				tmp = leafList.get(i).parent;	//현재 잎새노드의 부모 대입
				
				while(index==0 && tmp!=null && tmp!=root) {	//index가 0이라면 그 부모노드에선 자식 키값 존재x
					index=tmp.parent.children.indexOf(tmp);	//다음 계층(부모)으로 넘어감
					tmp=tmp.parent;
				}
				if(index>0)	//index가 0보다 클 때 => 해당 내부노드에 잎새노드 데이터 키값 존재
					if(tmp.keyList.get(index-1)!=key)	//해당 키값 자리에 다른 값이 들어있다면 자리에 맞는 잎새노드의 키값으로 수정
						tmp.keyList.set(index-1,key);
				
			}
		}
	}

    @Override
    public int size() {	// 트리의 데이터수를 계산해주는 메소드 (리프노드의 키값들 개수 반환)
        int tmpSize = 0; // 트리의 전체 크기를 저장할 변수
        // 모든 리프 노드를 순회하며 크기를 계산
        for (MyBPlusTreeNode leaf : leafList) {
            tmpSize += leaf.keyList.size(); // 각 리프 노드의 키 리스트 크기를 더함
        }
        return tmpSize; // 전체 크기 반환
    }
    
	@Override
	public Comparator<? super Integer> comparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer first() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer last() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
	
    

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer lower(Integer e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer floor(Integer e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer ceiling(Integer e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer higher(Integer e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer pollFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer pollLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<Integer> descendingSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Integer> descendingIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<Integer> subSet(Integer fromElement, boolean fromInclusive, Integer toElement,
			boolean toInclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<Integer> headSet(Integer toElement, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<Integer> tailSet(Integer fromElement, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Integer> headSet(Integer toElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Integer> tailSet(Integer fromElement) {
		// TODO Auto-generated method stub
		return null;
	}

}
