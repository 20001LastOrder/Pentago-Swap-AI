package student_player;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils_Do_Not_Submit {
	public static void main(String args[]) {
		String file = "logs/outcomes.txt";
		int[] list = new int[2];
		int first= 0;
		try {
			Scanner f = new Scanner(new File(file));
			int i = 0;
			while(f.hasNextLine()) {
				i++;
				String[] ln = f.nextLine().split(","); 
				if(ln[4].equals("V0")) {
					list[0]++;
				}else if(ln[4].equals("V1")) {
					list[1]++;
				}else {
					throw new IllegalArgumentException("unexpected winner");
				}
				if(ln[1].equals(ln[4])) {
					first++;
				}
				System.out.println(ln[4]);
			}
			System.out.println((double)first / i);
			System.out.println((double)list[1] / i);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
