import java.io.*;
import java.util.ArrayList;

public class word {
	public static void main(String [] args) {
		ArrayList<String> arraylist = new ArrayList<String>();
		File file = new File("./test.txt");
		try {
			BufferedReader bufferedreader = new BufferedReader(new FileReader(file));
			String str;
			while((str = bufferedreader.readLine() )!= null) {
				String[] temp = str.split(" ");
				for(int i = 0 ; i < temp.length ; i++) {
					System.out.print(temp[i] + " ");
					arraylist.add(temp[i]);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n찾을 text : D");
		int index = index(arraylist, "D");
		System.out.println("index는 " + index+"입니다.");
		
	}
	public static int index(ArrayList<String> arraylist , String word) {
		int index = arraylist.indexOf(word);
		return index;
	}
}
