
/* Game thread for the Scribble app

   @author YOUR FULL NAME GOES HERE

   @version CS 391 - Spring 2018 - A3
*/

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.regex.Pattern;

class Scribble implements Runnable {
	static char[] tiles = { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'B', 'B', 'C', 'C', 'D', 'D', 'D', 'D', 'E',
			'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'F', 'F', 'G', 'G', 'G', 'H', 'H', 'I', 'I', 'I',
			'I', 'I', 'I', 'I', 'I', 'I', 'J', 'K', 'L', 'L', 'L', 'L', 'M', 'M', 'N', 'N', 'N', 'N', 'N', 'N', 'O',
			'O', 'O', 'O', 'O', 'O', 'O', 'O', 'P', 'P', 'Q', 'R', 'R', 'R', 'R', 'R', 'R', 'S', 'S', 'S', 'S', 'T',
			'T', 'T', 'T', 'T', 'T', 'U', 'U', 'U', 'U', 'V', 'V', 'W', 'W', 'X', 'Y', 'Y', 'Z' };
	static final int WIDTH = 10; // width of board
	static final int HEIGHT = 10; // height of board
	static final int MAX_TURNS = 3; // number of turns in a game
	static String[] dict; // dictionary of allowed words
	char[][] board; // the Scribble board
	State state; // current state in the Scribble FSM
	Socket player1, player2; // player sockets
	DataInputStream in1, in2; // input streams of player sockets
	DataOutputStream out1, out2; // output streams of player sockets
	String name1, name2; // player names
	int turn; // the current turn in the game
	char[] rack1, rack2; // tile rack for both players
	int score1, score2; // scores for both players
	Random rnd; // random generator for the whole game

	/*
	 * add your instance/class variables, if any, after this point and before the
	 * constructor that follows
	 */
	
	boolean isPlayerTurn2;
	
	/*
	 * initialize a Scribble game: + load the dictionary + open the sockets' streams
	 * + create an empty board + initialize the two racks with 7 random tiles each +
	 * initialize other variables, as needed, including rnd with the given seed
	 */
	Scribble(Socket clientSocket1, Socket clientSocket2, int seed) {

		// To be completed
		player1 = clientSocket1;
		player2 = clientSocket2;
		rnd = new Random();
		board = new char[WIDTH][HEIGHT];
		isPlayerTurn2 = rnd.nextBoolean();
		rack1 = new char[7];
		rack2 = new char[7];
		try {
			this.openStreams(clientSocket1, clientSocket2);

			out1.writeUTF(toString());
			
			out1.writeUTF("Welcome to Scribble!\n\nPlease wait for your opponent...");
			name1 = in1.readUTF();
			out2.writeUTF("Welcome to Scribble!\n\nPlease wait for your opponent...");
			name2 = in2.readUTF();
			
			if(isPlayerTurn2)
				state = State.I6;
			else
				state = State.I3;
			
			for(int i = 0; i < rack1.length; i++) {
				int index = rnd.nextInt(tiles.length);
				
				while(tiles[index] == '-')
					index = rnd.nextInt(tiles.length);
				
				rack1[i] = tiles[index];
			}
			
			for(int i = 0; i < rack2.length; i++) {
				int index = rnd.nextInt(tiles.length);
				
				while(tiles[index] == '-')
					index = rnd.nextInt(tiles.length);
				
				rack2[i] = tiles[index];
				tiles[index] = '-';
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		turn = 1;
	}// constructor

	/*
	 * implement the Scribble FSM given in the handout. The output sent to the
	 * console by this method is also specified in the handout, namely in the
	 * provided traces.
	 */
	public void run() {
		while (true) {
			String input = "";
			
			boolean[] inHand = new boolean[7];
			boolean hasLetter = false;
			char missingLetter = '-';
			char[] rack = {};
			
			try {
				if((state == State.I3 || state == State.I6) && turn < 3) {
					
					turn ++;
					
					int matchingLetters = 0;
					
					if(state == State.I3) {
						input = in1.readUTF();
						rack = rack1;
					}
					else {
						input = in2.readUTF();
						rack = rack2;
					}
						if(!isInDictionary(input)) {
							out2.writeUTF(getGameState(2) + "The word "+input+" is not in the dictionary.");
						}
						
						for(int i = 0; i < input.length(); i ++) {
							for(int j = 0; j < rack.length; j++) {
								if(input.charAt(i) == rack[j] && !inHand[j]) {
									inHand[j] = true;
									matchingLetters++;
									hasLetter = true;
									break;
								}
								missingLetter = Character.toUpperCase(input.charAt(i));
							}
							if(!hasLetter)
								break;
							hasLetter = false;
							missingLetter = '-';
						}
						
						if(missingLetter != '-') {
							if(state == State.I3)
								out1.writeUTF(getGameState(1) + "You do not have the letter " + missingLetter + " in your rack!");
							else
								out2.writeUTF(getGameState(2) + "You do not have the letter " + missingLetter + " in your rack!");
						}else if(matchingLetters != input.length()) {
							if(state == State.I3)
								out1.writeUTF(getGameState(1) + "The word "+input+" is not in the dictionary.");
							else
								out2.writeUTF(getGameState(2) + "The word "+input+" is not in the dictionary.");
						}
						
						if(state == State.I3)
							state = State.I4;
						else
							state = State.I7;
						
				}else if((state == State.I4 || state == State.I7) && turn < 3) {
					turn++;
					
					if(state == State.I4)
						input = in1.readUTF();
					else
						input = in2.readUTF();
					
					if(input.length() != 2) {
						if(state == State.I4)
							out1.writeUTF(getGameState(2) + "Invalid location!");
						if(state == State.I7)
							out2.writeUTF(getGameState(2) + "Invalid location!");
					}
					
					String a = input.substring(0, 1);
					String b = input.substring(1);
					
					if(!Pattern.matches(".*[A-Z].*", a)) {
						if(state == State.I4)
							out1.writeUTF(getGameState(1) + "Invalid location!");
						if(state == State.I7)
							out2.writeUTF(getGameState(2) + "Invalid location!");
					}else if(!Pattern.matches(".*[0-9].*", b)) {
						if(state == State.I4)
							out1.writeUTF(getGameState(1) + "Invalid location!");
						if(state == State.I7)
							out2.writeUTF(getGameState(2) + "Invalid location!");
					}
					
					if(state == State.I4)
						state = State.I5;
					else
						state = State.I8;
				}else if((state == State.I5 || state == State.I8) && turn < 3) {
					turn++;
					
					if(state == State.I5)
						input = in1.readUTF();
					else
						input = in2.readUTF();
					
					if(input.length() != 1) {
						if(state == State.I5)
							out1.writeUTF(getGameState(2) + "Invalid direction!");
						else
							out2.writeUTF(getGameState(2) + "Invalid direction!");
					}
					
					if(!input.equals("A") || !input.equals("D")) {
						if(state == State.I5)
							out1.writeUTF(getGameState(1) + "Invalid direction!");
						else
							out2.writeUTF(getGameState(2) + "Invalid direction!");
					}
					
					if(state == State.I5)
						state = State.I3;
					else
						state = State.I6;
				}else { //gameover
					
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}// run method

	/*
	 * return the string representation of the current game state from the
	 * perspective of the given player (i.e., 1 or 2). More precisely, the returned
	 * string must contain, in order: + the state of the board + the turn number +
	 * the scores of both players + the rack of the given player The format of the
	 * returned string is fully specified in the traces included in the handout.
	 */
	String getGameState(int player) {
		
		String output;
		
		if(player == 1) {
			output = this.toString()+"\nTurn: " + turn + "\nScores: " + score1 + " (opponent: " + score2 + ")\nRack: ";
			for(char c : rack1) {
				output += c + "";
			}
		}else {
			output = this.toString() + "\nTurn: " + turn + "\nScores: " + score2 + " (opponent: " + score1 + ")\nRack: ";
			for(char c : rack2) {
				output += c + "";
			}
		}

		return output + "\n";
	}// getGameState method

	/*
	 * Initialize dict with the contents of the dict.txt file (stored in the same
	 * directory as this file). Each word must be in all uppercase. Duplicates (if
	 * any) must be removed, yielding a total of 276,643 distinct words that must be
	 * sorted in alphabetical order within dict.
	 */
	static void loadDictionary() {

		// To be completed
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("dict.txt")));

			String line;
			int count = 0;

			dict = new String[(int) reader.lines().count()];

			while ((line = reader.readLine()) != null) {
				dict[count++] = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}// loadDictionary method

	/*
	 * return true if and only if the given word (assumed to be all uppercase) is in
	 * dict
	 */
	static boolean isInDictionary(String word) {
		for (String s : dict) {
			return s.equals(word);
		}
		return false;
	}// isInDictionary method

	/*
	 * convert the Scribble board to a string The format of this string is fully
	 * specified in the traces included in the handout. Here is what the empty board
	 * must look like:
	 * 
	 * |0|1|2|3|4|5|6|7|8|9| -+-+-+-+-+-+-+-+-+-+-+ A| | | | | | | | | | |
	 * -+-+-+-+-+-+-+-+-+-+-+ B| | | | | | | | | | | -+-+-+-+-+-+-+-+-+-+-+ C| | | |
	 * | | | | | | | -+-+-+-+-+-+-+-+-+-+-+ D| | | | | | | | | | |
	 * -+-+-+-+-+-+-+-+-+-+-+ E| | | | | | | | | | | -+-+-+-+-+-+-+-+-+-+-+ F| | | |
	 * | | | | | | | -+-+-+-+-+-+-+-+-+-+-+ G| | | | | | | | | | |
	 * -+-+-+-+-+-+-+-+-+-+-+ H| | | | | | | | | | | -+-+-+-+-+-+-+-+-+-+-+ I| | | |
	 * | | | | | | | -+-+-+-+-+-+-+-+-+-+-+ J| | | | | | | | | | |
	 * -+-+-+-+-+-+-+-+-+-+-+
	 * 
	 * The traces in the handout also show that some of the vertical and horizontal
	 * bars must be omitted, namely when they appear between two letters. Similarly,
	 * an interior '+' may NOT be displayed when it is surrounded by 4 letters.
	 */
	public String toString() {

		// To be completed

		StringBuilder builder = new StringBuilder();

		builder.append(" |0|1|2|3|4|5|6|7|8|9|\n");
		builder.append("-+-+-+-+-+-+-+-+-+-+-+\n");
		for (int i = 0; i < HEIGHT; i++) {

			if (i == 0)
				builder.append("A");
			else if (i == 1)
				builder.append("B");
			else if (i == 2)
				builder.append("C");
			else if (i == 3)
				builder.append("D");
			else if (i == 4)
				builder.append("E");
			else if (i == 5)
				builder.append("F");
			else if (i == 6)
				builder.append("G");
			else if (i == 7)
				builder.append("H");
			else if (i == 8)
				builder.append("I");
			else
				builder.append("J");

			for (int j = 0; j < HEIGHT; j++) {
				builder.append("|");
				builder.append(board[i][j]);
			}

			builder.append("|\n");
			builder.append("-+-+-+-+-+-+-+-+-+-+-+\n");
		}

		return builder.toString();
	}// toString method

	/*
	 * open the I/O streams of the given sockets and assign them to the
	 * corresponding instance variables of this object
	 */
	void openStreams(Socket socket1, Socket socket2) throws IOException {

		this.player1 = socket1;
		this.player2 = socket2;
		try {
			this.in1 = new DataInputStream(socket1.getInputStream());
			this.out1 = new DataOutputStream(socket1.getOutputStream());
			this.in2 = new DataInputStream(socket2.getInputStream());
			this.out2 = new DataOutputStream(socket2.getOutputStream());
		} catch (IOException e) {
			System.out.println("hehehe");
		}

	}// openStreams method

	/*
	 * states of the Scribble FSM. You MUST use this data type, e.g., State.I1
	 *** 
	 * do NOT modify this enum type ***
	 */
	public enum State {
		I1, I2, I3, I4, I5, I6, I7, I8
	}

	/*
	 * exception that must be raised when a player puts down an invalid word, that
	 * is, a word w that meets at least one of the following requirements: + at
	 * least one of the words formed using w is not in the dictionary + w is too
	 * long to fit on the board, i.e., it goes off the right side of the board when
	 * placed in the 'across' direction or it goes off the bottom of the board when
	 * placed in the 'down' direction + one of the letters in w does not match an
	 * existing letter already on the board at that position + one of the letters in
	 * w is not on the player's rack and the position of that letter on the board is
	 * empty (i.e., w is not reusing an existing letter on the board) + w does not
	 * build on an existing word and this is not the first word of the game
	 * 
	 * You must instantiate this exception class in your solution whenever
	 * appropriate.
	 *** 
	 * do NOT modify this class ***
	 */
	class BadWordPlacementException extends RuntimeException {
		BadWordPlacementException(String message) {
			super(message);
		}
	}// BadWordPlacementException class

	/* add your instance methods after this point */

}// Scribble class
