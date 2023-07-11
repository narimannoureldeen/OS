import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Parser {

	static SystemCall systemCall;
	static Scheduler scheduler;

	static String memory[] = new String[40];

	static File HardDisk = new File("Disk");


	static String file3;

	static String input1;
	static String input2;
	static String input3;

	static Queue<Integer> Ready = new LinkedList<Integer>();
	static Queue<Integer> GeneralBlocked = new LinkedList<Integer>();
	static Queue<Integer> userInputBlocked = new LinkedList<Integer>();
	static Queue<Integer> userOutputBlocked = new LinkedList<Integer>();
	static Queue<Integer> fileBlocked = new LinkedList<Integer>();

	static Mutex userInput = new Mutex();
	static Mutex userOutput = new Mutex();
	static Mutex file = new Mutex();

	static File temp = new File("temp.txt");



	static int t1, t2, t3;

	static int Q;

	public Queue getReady() {
		return Ready;
	}

	public void addReady(Queue Ready, int a) {
		Ready.add(a);
	}

	public void removeReady(Queue Ready) {
		Ready.remove();
	}

	public String getFile3() {
		return file3;
	}

	public void setFile3(String file3) {
		Parser.file3 = file3;
	}

	public void putMemory(Hashtable<String, String> memory, String x, String y) {
		memory.put(x, y);

	}
	public static void changeState(PCB pcb,String state) {
		pcb.setState(state);
		if (pcb.getpID() == 1) {

		if (memory[0].equals("1")) {
			memory[1] = state;
		} else if (memory[20].equals("1")) {
			memory[21] = state;
		}
	} else if (pcb.getpID() == 2) {
		
		if (memory[0].equals("2")) {
			memory[1] = state;
		} else if (memory[20].equals("2")) {
			memory[21] = state;
		}

	} else if (pcb.getpID() == 3) {
		
		if (memory[0].equals("3")) {
			memory[1] = state;
		} else if (memory[20].equals("3")) {
		    memory[21] = state;
		}
	}
		
	}
	public static void changePC(PCB pcb) {
		int x= pcb.getPc();
		if (pcb.getpID() == 1) {

		if (memory[0].equals("1")) {
			memory[2] =(x+1)+"" ;
		} else if (memory[20].equals("1")) {
			memory[22] = (x+1)+"";
		}
	} else if (pcb.getpID() == 2) {
		
		if (memory[0].equals("2")) {
			memory[2] = (x+1)+"";
		} else if (memory[20].equals("2")) {
			memory[22] = (x+1)+"";
		}

	} else if (pcb.getpID() == 3) {
		
		if (memory[0].equals("3")) {
			memory[2] = (x+1)+"";
		} else if (memory[20].equals("3")) {
		    memory[22] =(x+1)+"";
		}
	}
		pcb.setPc(pcb.getPc()+1);
		
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	public static int execute(PCB pcb) throws IOException {

		String x;
		int c = pcb.getPc();//0
		int a = 0;

		if (Integer.parseInt(memory[0]) == pcb.getpID()) {
			a = 8 + pcb.getPc();
		} else if (Integer.parseInt(memory[20]) == pcb.getpID()) {
			a = 28 + pcb.getPc();
		}

		for (int i = a; i < a + Q && i < pcb.getEnd(); i++) {
			// ----------------------------------
			if (memory[i] == null ) {
				pcb.setPc(pcb.getEnd());
				memory[a-3]=pcb.getEnd()+"";
				memory[a-4]="Finished";		
				break;
			}
			String[] y = memory[i].split(" ");
			System.out.println(
					"The Instruction that's currently executing is " + memory[i] + " in Process " + pcb.getpID());
			System.out.println("*******************");

			if (y[0].equals("print")) {
				systemCall.print(y[1], pcb);
			}
			// Storing variables
			else if (y[0].equals("input")) {
				System.out.println("Please enter an input: ");
				Scanner sc = new Scanner(System.in);
				x = sc.nextLine();
				if (pcb.getpID() == 1) {
					input1 = x;
				} else if (pcb.getpID() == 2) {
					input2 = x;
				} else if (pcb.getpID() == 3) {
					input3 = x;
				}
			} else if (y[0].equals("readFile")) {
				systemCall.readFile(readMemory(pcb, y[1]));

			} else if (y[0].equals("assign")) {
				if (y[2].equals("input")) {
					if (pcb.getpID() == 1) {
						systemCall.assign(y[1], input1, pcb);
					} else if (pcb.getpID() == 2) {
						systemCall.assign(y[1], input2, pcb);
					} else if (pcb.getpID() == 3) {
						systemCall.assign(y[1], input3, pcb);
					}
				} else if (y[2].equals("readFile")) {
					systemCall.assign(y[1], file3, pcb);
				}
			} else if (y[0].equals("writeFile")) {

				systemCall.writeFile(readMemory(pcb, y[1]), readMemory(pcb, y[2]));

			} else if (y[0].equals("printFromTo")) {
				systemCall.printFromTo(y[1], y[2], pcb);
			}

			else if (y[0].equals("semWait")) {
				if (y[1].equals("userInput")) {
					userInput.semWait(y[1], pcb);
				} else if (y[1].equals("userOutput")) {
					userOutput.semWait(y[1], pcb);
				} else if (y[1].equals("file")) {
					file.semWait(y[1], pcb);
				}

				if (GeneralBlocked.contains(pcb.getpID())) {
					pcb.setPc(pcb.getPc() + 1);
					changeState(pcb,"Blocked");
					break;
				}
			} else if (y[0].equals("semSignal")) {
				if (y[1].equals("userInput")) {
					userInput.semSignal(y[1], pcb);
				} else if (y[1].equals("userOutput")) {
					userOutput.semSignal(y[1], pcb);
				} else if (y[1].equals("file")) {
					file.semSignal(y[1], pcb);
				}
			}
			t1--;
			t2--;
			t3--;
			if (t1 == 0) {
				System.out.println("Process 1" + " arrived.");
				Scheduler.arrivedWhileExecute(1);
			} else if (t2 == 0) {
				System.out.println("Process 2" + " arrived.");
				Scheduler.arrivedWhileExecute(2);
			} else if (t3 == 0) {
				System.out.println("Process 3" + " arrived.");
				Scheduler.arrivedWhileExecute(3);
			}
			
			

		  changePC(pcb);
		}
		
	    changeState(pcb,"Ready");
		
		return pcb.getPc();

	}

	public static int ifSpace(String[] mem) {
		if (mem[0].equals("")) {
			return 0;
		} else if (mem[20] == null || mem[20].equals("")) {
			return 20;
		} else
			return -1;
	}

	public static String readMemory(PCB pcb, String var) {

		if (pcb.getpID() == Integer.parseInt(Parser.memory[0])) {
			for (int i = 5; i < 8; i++) {
				String[] y = memory[i].split(" ");
				if (y[0].equals(var)) {
					return y[1];
				}
			}
		} else if (pcb.getpID() == Integer.parseInt(Parser.memory[20])) {
			for (int i = 25; i < 28; i++) {
				String[] y = memory[i].split(" ");
				if (y[0].equals(var)) {
					return y[1];
				}
			}

		}
		return var;
	}

	public static int SwapMemToDisk() throws IOException {
		int var = -1;

		FileWriter writer = new FileWriter(HardDisk);
		if (!(memory[1].equals("Running"))) {
			for (int i = 0; i < 20; i++) {
				String data = memory[i];
				memory[i] = "";
				writer.write(data + System.lineSeparator());
				var = 0;
			}
		} else {
			for (int i = 20; i < 40; i++) {
				String data = memory[i];
				memory[i] = "";
				writer.write(data + System.lineSeparator());
				var = 20;
			}

		}
		writer.close();
		return var;

	}

	public static void swapTemp(File temp) throws IOException {

		FileWriter writer = new FileWriter(temp);
		if (!(memory[1].equals("Running"))) {
			for (int i = 0; i < 20; i++) {
				String data = memory[i];
				memory[i] = "";
				writer.write(data + System.lineSeparator());
			}
		} else {
			for (int i = 20; i < 40; i++) {
				String data = memory[i];
				memory[i] = "";
				writer.write(data + System.lineSeparator());
			}

		}
		writer.close();

	}

	public static void SwapDiskToMem() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(HardDisk));
		String st;
		int space = ifSpace(memory);

		if (space == 0) {
			// file will be inserted from 0 to 20
			for (int i = 0; i < 20 && ((st = br.readLine()) != null); i++) {
				memory[i] = st;
			}
			memory[3] = "0";
			memory[4] = "19";

		} else if (space == 20) {
			// 20 to 39
			for (int i = 20; i < 40 && ((st = br.readLine()) != null); i++) {
				memory[i] = st;
			}
			memory[23] = "20";
			memory[24] = "39";
		} else {
			// check not running, swap with it
			File temp = new File("Temp");
			if (!(memory[1].equals("Running"))) {
				swapTemp(temp);
				// Disk >> Mem
				for (int i = 0; i < 20 && ((st = br.readLine()) != null); i++) {
					memory[i] = st;
				}

				// temp >> Disk
				swapFiletoFile(temp);

			} else if (!(memory[21].equals("Running"))) {
				// Disk >> Mem
				for (int i = 20; i < 40 && ((st = br.readLine()) != null); i++) {
					memory[i] = st;
				}
				// temp >> Disk
				swapFiletoFile(temp);

			}

		}
	}

	public static void swapFiletoFile(File temp) throws IOException {

		// delete contents of Hard Disk
		BufferedReader br = new BufferedReader(new FileReader(HardDisk));
		String x;
		FileWriter diskWriter = new FileWriter(HardDisk);
		while (((x = br.readLine()) != null)) {
			diskWriter.write("" + System.lineSeparator());
		}
		diskWriter.close();

		// read from temp
		BufferedReader buffer = new BufferedReader(new FileReader(temp));
		String st;

		// write temp into disk
		FileWriter writer = new FileWriter(HardDisk);

		while (((st = buffer.readLine()) != null)) {
			writer.write(st + System.lineSeparator());
		}
		writer.close();
	}

	public static int numberOFInstr(String path) throws IOException {
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		int c = 0;

		while ((st = br.readLine()) != null) {
			c++;
		}
		return c;
	}

	public static void saveToMemory(String path, PCB pcb) throws IOException {
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		String x;
		int counter = pcb.getStart();

		memory[counter++] = pcb.getpID() + "";
		memory[counter++] = pcb.getState();
		memory[counter++] = pcb.getPc() + "";
		memory[counter++] = pcb.getStart() + "";
		memory[counter++] = pcb.getEnd() + "";

		memory[counter++] = "";
		memory[counter++] = "";
		memory[counter++] = "";

		while ((st = br.readLine()) != null) {

			String[] y = st.split(" ");
			if (y[0].equals("assign") && y[2].equals("readFile")) {
				memory[counter++] = y[2] + " " + y[3];
				memory[counter] = y[0] + " " + y[1] + " " + y[2];
			} else if (y[0].equals("assign") && y[2].equals("input")) {
				memory[counter++] = y[2];
				memory[counter] = y[0] + " " + y[1] + " " + y[2];
			} else {
				memory[counter] = st;
			}
			counter++;
		}

	}

	public static void clearMem(PCB pcb) {
		for (int i = pcb.getStart(); i < pcb.getEnd(); i++) {
			memory[i] = "";
		}
	}

	public static void printQueues(Queue<Integer> q) {
		for (Integer item : q) {
			System.out.print(item + " ");
		}
		System.out.println();
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws IOException {

//		saveToMemory("D:\\GUC\\CODING\\ProjectOS\\Program_1.txt", 1);
//		saveToMemory("D:\\GUC\\CODING\\ProjectOS\\Program_2.txt", 2);
//		saveToMemory("D:\\GUC\\CODING\\ProjectOS\\Program_3.txt", 3);

		Scanner sc = new Scanner(System.in);
		System.out.println("Please Enter The Arrival Time Of The First Process: ");
		t1 = sc.nextInt();

		System.out.println("Please Enter The Arrival Time Of The Second Process: ");
		t2 = sc.nextInt();

		System.out.println("Please Enter The Arrival Time Of The Third Process: ");
		t3 = sc.nextInt();

		System.out.println("Please Enter The Quanta: ");
		Q = sc.nextInt();

//		t1 = 0;
//		t2 = 1;
//		t3 = 4;
//		Q = 2;

		scheduler = new Scheduler();

		scheduler.schedule(t1, t2, t3, Q);

	}
}
