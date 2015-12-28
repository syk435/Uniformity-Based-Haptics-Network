
public class SynchronizedLibrary {
	
	int books[];
	int timeStamp = 0;
	//ids of books in library are defined as book b1...bz. b1 = books[1]..b[z] = books[z];
	
	public SynchronizedLibrary(int numBooks){
		books = new int[numBooks+1];
		for(int i = 0;i<numBooks+1;i++){
			books[i] = -1;
		}
	}
	
	public synchronized boolean returnBook(int clientId, int bookId){
		try{
			Thread.sleep(1500);
			if(books[bookId]==clientId){
				books[bookId] = -1;
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	}
	
	public synchronized boolean reserveBook(int clientId, int bookId){
		try{
			Thread.sleep(1500);
			if(books[bookId]==-1){
				books[bookId] = clientId;
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	}
	
	@Override
	public String toString(){
		String result = timeStamp + ",";
		for(int q = 0;q<books.length;q++){
			if(q!=books.length-1){
				result = result+books[q]+",";
			}else{
				result = result+books[q];
			}
		}
		return result;
	}
	
	public synchronized void setTimeStamp(int time){
		timeStamp = time;
	}
	
	public int getTimeStamp(){
		return timeStamp;
	}
	
	public synchronized void updateLibrary(String tokenUpdatedLibString){
		String[] tempS = tokenUpdatedLibString.split(",");
		timeStamp = Integer.parseInt(tempS[0]);
		for(int q = 1;q<tempS.length;q++){
			books[q-1] = Integer.parseInt(tempS[q]);
		}
	}

}
