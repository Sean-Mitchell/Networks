
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
import java.net.SocketException;
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
	boolean isFirstTurn;
	boolean isPlaying;
	String location, direction;

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
		isPlaying = true;
		isFirstTurn = true;
		rack1 = new char[7];
		rack2 = new char[7];
		try {
			loadDictionary();
			this.openStreams(clientSocket1, clientSocket2);
			name1 = in1.readUTF();
			name2 = in2.readUTF();

			if (isPlayerTurn2) {
				state = State.I6;
				out2.writeUTF("GO");
			} else {
				state = State.I3;
				out1.writeUTF("GO");
			}

			for (int i = 0; i < rack1.length; i++) {
				rack1[i] = tiles[rnd.nextInt(tiles.length)];
			}

			for (int i = 0; i < rack2.length; i++) {
				rack2[i] = tiles[rnd.nextInt(tiles.length)];
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		turn = 0;
	}// constructor

	/*
	 * implement the Scribble FSM given in the handout. The output sent to the
	 * console by this method is also specified in the handout, namely in the
	 * provided traces.
	 */
	public void run() {
		while (true) {
			try {
				if (state == State.I3 && isPlaying) {
					turn = 1;
					out1.writeUTF(getGameState(1));
				}
				else if (state == State.I6 && isPlaying) {
					turn = 1;
					out2.writeUTF(getGameState(2));
				}
				isPlaying = false;
			} catch (IOException e) {
				
			}

			if (turn == 0 && isFirstTurn) {
				try {
					if (state == State.I3)
						out1.writeUTF("GO");
					else if (state == State.I6)
						out2.writeUTF("GO");

					isFirstTurn = false;
				} catch (IOException e) {
					e.getStackTrace();
				}
			}

			String input = "";

			boolean[] inHand = new boolean[7];
			boolean hasLetter = false;
			char missingLetter = '-';
			char[] rack = {};

			try {
				if ((state == State.I3 || state == State.I6) && turn < 4) { // ask for a location
					if (state == State.I3)
						input = in1.readUTF();
					else
						input = in2.readUTF();

					if (input.length() != 2) {
						if (state == State.I3)
							out1.writeUTF(getGameState(2) + "Invalid location!");
						else
							out2.writeUTF(getGameState(2) + "Invalid location!");
					} else {
						String a = input.substring(0, 1);
						String b = input.substring(1);

						if (!Pattern.matches(".*[A-J].*", a.toUpperCase())) {
							if (state == State.I3)
								out1.writeUTF(getGameState(1) + "Invalid location!");
							else
								out2.writeUTF(getGameState(2) + "Invalid location!");
						} else if (!Pattern.matches(".*[0-9].*", b)) {
							if (state == State.I3)
								out1.writeUTF(getGameState(1) + "Invalid location!");
							else
								out2.writeUTF(getGameState(2) + "Invalid location!");
						} else {
							location = input; // valid location

							if (state == State.I3) {
								state = State.I4;
								out1.writeUTF("OK");
							} else {
								state = State.I7;
								out2.writeUTF("OK");
							}
						}
					}
				} else if ((state == State.I4 || state == State.I7) && turn < 4) { // ask for a direction
					if (state == State.I4)
						input = in1.readUTF();
					else
						input = in2.readUTF();

					if (input.length() != 1) { // not the right length
						if (state == State.I4)
							out1.writeUTF("Invalid direction!");
						else
							out2.writeUTF("Invalid direction!");
					} else {
						if (!input.toUpperCase().trim().equals("A") && !input.toUpperCase().trim().equals("D")) { // not
																													// matching
																													// A
																													// or
																													// D
							if (state == State.I4)
								out1.writeUTF("Invalid direction!");
							else
								out2.writeUTF("Invalid direction!");
						} else {

							direction = input; // valid direction

							if (state == State.I4) {
								state = State.I5;
								out1.writeUTF("OK");
							} else {
								state = State.I8;
								out2.writeUTF("OK");
							}
						}
					}

				} else if ((state == State.I5 || state == State.I8) && turn < 4) { // ask for a word					
					if (state == State.I5) {
						input = in1.readUTF();
						rack = rack1;
					} else {
						input = in2.readUTF();
						rack = rack2;
					}
					if (!isInDictionary(input)) {
						if(state == State.I5)
							out1.writeUTF(getGameState(2) + "\nThe word " + input + " is not in the dictionary.");
						if(state == State.I8)
							out2.writeUTF(getGameState(2) + "\nThe word " + input + " is not in the dictionary.");
						turn++;
					} else {
						for (int i = 0; i < input.length(); i++) {
							for (int j = 0; j < rack.length; j++) {
								if (input.charAt(i) == rack[j] && !inHand[j]) {
									inHand[j] = true;
									hasLetter = true;
									break;
								}
								missingLetter = Character.toUpperCase(input.charAt(i));
							}
							if (!hasLetter)
								break;
							hasLetter = false;
							missingLetter = '-';
						}

						if (missingLetter != '-') { // no letter available
							if (state == State.I5)
								out1.writeUTF(getGameState(1) + "You do not have the letter " + missingLetter
										+ " in your rack!");
							else
								out2.writeUTF(getGameState(2) + "You do not have the letter " + missingLetter
										+ " in your rack!");
							turn++;
						} else {
							int row, col;
							char c = location.charAt(0); // row
							boolean isSuccessful = false;

							row = c == 'A' ? 0
									: c == 'B' ? 1
											: c == 'C' ? 2
													: c == 'D' ? 3
															: c == 'E' ? 4
																	: c == 'F' ? 5
																			: c == 'G' ? 6
																					: c == 'H' ? 7 : c == 'I' ? 8 : 9;

							col = Integer.parseInt(location.charAt(1) + "");

							if (direction.equals("A")) {
								if (input.length() > (board.length - c)) {
									if (state == State.I5)
										out1.writeUTF(input + " is too long to fit on the board.");
									else
										out2.writeUTF(input + " is too long to fit on the board.");
									turn++;
								} else {
									for (int i = 0; i < input.length(); i++) {
										if (board[row][col + i] == input.charAt(i)) { // match, don't take out from rack
											for (int j = 0; j < rack.length; j++) {
												if (rack[j] == input.charAt(i))
													inHand[j] = false;
											}
										} else if (board[row][col + i] == ' ') { // insert
											board[row][col + i] = input.charAt(i);
										} else { // collision & no match
											if (state == State.I5)
												out1.writeUTF(input.charAt(i) + " in " + input
														+ " conflicts with a different letter on the board.");
											else
												out2.writeUTF(input.charAt(i) + " in " + input
														+ " conflicts with a different letter on the board.");
											turn++;
											break;
										}
									}
								}
							} else {
								if (input.length() > (board.length - c)) {
									if (state == State.I5)
										out1.writeUTF(input + " is too long to fit on the board.");
									else
										out2.writeUTF(input + " is too long to fit on the board.");
									turn++;
								} else {
									for (int i = 0; i < input.length(); i++) {
										if (board[row + 1][col] == input.charAt(i)) { // match, don't take out from rack
											for (int j = 0; j < rack.length; j++) {
												if (rack[j] == input.charAt(i))
													inHand[j] = false;
											}
										} else if (board[row][col + i] == ' ') { // insert
											board[row][col + i] = input.charAt(i);
										} else { // collision & no match
											if (state == State.I5)
												out1.writeUTF(input.charAt(i) + " in " + input
														+ " conflicts with a different letter on the board.");
											else
												out2.writeUTF(input.charAt(i) + " in " + input
														+ " conflicts with a different letter on the board.");
											turn++;
											break;
										}
									}
								}
							}

							if (isSuccessful) {
								for (int i = 0; i < inHand.length; i++) { // refil the rack
									if (inHand[i]) {
										rack[i] = tiles[rnd.nextInt(tiles.length)];
									}
								}
								turn = 0;
								if (state == State.I5 && isSuccessful) {
									state = State.I6;
									rack1 = rack;
								} else if (state == State.I8 && isSuccessful) {
									state = State.I3;
									rack2 = rack;
								}
								isPlaying = true;
							}
						}
					}
				} 

				if(turn > 3){ // gameover
					if(score1 > score2) {
						out1.writeUTF("You won - GAME OVER!");
						out2.writeUTF("You lose - GAME OVER!");
					}else if(score1 < score2) {
						out2.writeUTF("You won - GAME OVER!");
						out1.writeUTF("You lose - GAME OVER!");
					}else {
						out1.writeUTF("You tied - GAME OVER!");
						out2.writeUTF("You tied - GAME OVER!");
					}
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

		if (player == 1) {
			output = this.toString() + "Turn: " + turn + "\nScores: " + score1 + " (opponent: " + score2
					+ ")\nRack: ";
			for (char c : rack1) {
				output += c + "";
			}
		} else {
			output = this.toString() + "Turn: " + turn + "\nScores: " + score2 + " (opponent: " + score1
					+ ")\nRack: ";
			for (char c : rack2) {
				output += c + "";
			}
		}

		return output;
	}// getGameState method

	/*
	 * Initialize dict with the contents of the dict.txt file (stored in the same
	 * directory as this file). Each word must be in all uppercase. Duplicates (if
	 * any) must be removed, yielding a total of 276,643 distinct words that must be
	 * sorted in alphabetical order within dict.
	 */
	static void loadDictionary() {

		BufferedReader reader = null;

		// To be completed
		try {
			reader = new BufferedReader(new FileReader(new File("dict.txt")));

			String line;
			int count = 0;

			dict = new String[(int) reader.lines().count()];

			reader = new BufferedReader(new FileReader(new File("dict.txt")));

			while ((line = reader.readLine()) != null) {
				dict[count++] = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}// loadDictionary method

	/*
	 * return true if and only if the given word (assumed to be all uppercase) is in
	 * dict
	 */
	static boolean isInDictionary(String word) {
		for (String s : dict) {
			return s.toLowerCase().equals(word.toLowerCase());
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
