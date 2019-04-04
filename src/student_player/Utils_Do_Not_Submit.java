package student_player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Scanner;

public class Utils_Do_Not_Submit {
	static class Data {
		int win;
		int total;
	}

	public static void main(String args[]) throws FileNotFoundException {
		processData();
	}

	/**
	 * Used to process data from the result
	 * 
	 * @throws FileNotFoundException
	 */
	public static void processData() throws FileNotFoundException {
		String file = "data/table.txt";
		String sample = "logs/game";
		Hashtable<String, Data> map = new Hashtable<String, Data>();
		//read old table
		Scanner s = new Scanner(new File(file));
		long t = System.currentTimeMillis();
		while (s.hasNextLine()) {
			String[] line = s.nextLine().split("\\s+");
			int win = Integer.parseInt(line[1]);
			int total = Integer.parseInt(line[2]);
			Data data = new Data();
			data.win = win;
			data.total = total;
			map.put(line[0], data);
		}
		System.out.println("read uses +"+ (System.currentTimeMillis()-t));
		
		for (int i = 1006; i <= 1297; i++) {
			String name = sample + String.format("%05d", i) + ".log";
			Scanner f = new Scanner(new File(name));

			// store intermediate states
			ArrayList<String> states = new ArrayList<String>();
			Hashtable<String, StringBuilder> coordEncoding = new Hashtable<String, StringBuilder>();
			coordEncoding.put("TR", new StringBuilder("NNNNNNNNN"));
			coordEncoding.put("TL", new StringBuilder("NNNNNNNNN"));
			coordEncoding.put("BR", new StringBuilder("NNNNNNNNN"));
			coordEncoding.put("BL", new StringBuilder("NNNNNNNNN"));

			int winner = -1;
			// check for number of starts
			int numberOfStarts = 0;
			while (f.hasNextLine()) {
				String info = f.nextLine();
				if (info.contains("GAMEOVER")) {
					String[] winWords = info.split("\\s+");
					if ("WINNER".equals(winWords[1])) {
						winner = Integer.parseInt(winWords[2]);
					}
					// game ends, no start
					numberOfStarts = 0;
				}
				
				if (numberOfStarts >= 2) {
					// start process data
					String[] datas = info.split("\\s+");
					int x = Integer.parseInt(datas[0]);
					int y = Integer.parseInt(datas[1]);
					int player = Integer.parseInt(datas[4]);
					processCoord(x, y, coordEncoding, player);
					StringBuilder tmp = coordEncoding.get(datas[2]);
					
					//switch coordinates
					coordEncoding.put(datas[2], coordEncoding.get(datas[3]));
					coordEncoding.put(datas[3], tmp);
					
					//add state to history
					String state = coordEncoding.get("TL").toString() + coordEncoding.get("TR").toString()
							  +coordEncoding.get("BL") + coordEncoding.get("BR");
					states.add(state);
				}
				if (info.contains("START"))
					numberOfStarts++;
			}
			f.close();
			//post game processing
			//update win only if there is a winner
			if(winner >= 0) {
				int win = winner == 0? 1:0;
				for(String state : states) {
					Data stateData = map.get(state);

					if(stateData != null) {
						stateData.total += 1;
						stateData.win += win;
					}else {
						stateData = new Data();
						stateData.total += 1;
						stateData.win += win;
						map.put(state, stateData);
					}
				}
			}
		}
		
		// write to file
		PrintStream out = new PrintStream(new File(file));
		PrintStream system = System.out;
		System.setOut(out);
		for(Entry<String, Data> entry : map.entrySet()) {
			System.out.println(entry.getKey()+" "+entry.getValue().win+" "+entry.getValue().total);
		}
		System.setOut(system);
		System.out.println(map.size());
		System.out.println("finished");
	}

	private static void processCoord(int x, int y, 
									Hashtable<String, StringBuilder> coordEncoding, int player) {
		if (x <= 2) {
			if (y <= 2) {
				int index = x + y * 3;
				setMove(coordEncoding, index, "TL", player);
			}else {
				int index = x + (y-3)*3;
				setMove(coordEncoding, index, "BL", player);
			}
		}else {
			if (y <= 2) {
				int index = (x-3) + y * 3;
				setMove(coordEncoding, index, "TR", player);
			}else {
				int index = (x-3) + (y-3)*3;
				setMove(coordEncoding, index, "BR", player);
			}
		}
	}

	private static void setMove(Hashtable<String, StringBuilder> coordEncoding, 
								int index, String block, int player) {
		StringBuilder current = coordEncoding.get(block);
		current.setCharAt(index, (char)(player+'0'));
		coordEncoding.put(block, current);
	}

	public static void calculateWinRatio() {
		String file = "logs/outcomes.txt";
		int[] list = new int[2];
		int first = 0;
		try {
			Scanner f = new Scanner(new File(file));
			int i = 0;
			while (f.hasNextLine()) {
				i++;
				String[] ln = f.nextLine().split(",");
				if (ln[4].equals("V0")) {
					list[0]++;
				} else if (ln[4].equals("V1")) {
					list[1]++;
				} else {
					throw new IllegalArgumentException("unexpected winner");
				}
				if (ln[1].equals(ln[4])) {
					first++;
				}
				System.out.println(ln[4]);
			}
			System.out.println((double) first / i);
			System.out.println((double) list[1] / i);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
