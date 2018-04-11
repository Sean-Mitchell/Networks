
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

	boolean isPlayerTurn2, switchTurn, isFirstWord, isStandingAlone;
	boolean[] inHand;
	String location1, location2, direction1, direction2;
	State playerState1, playerState2;

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
		switchTurn = false;
		isFirstWord = true;
		isStandingAlone = true;
		inHand = new boolean[7];
		rack1 = new char[7];
		rack2 = new char[7];
		location1 = "";
		direction1 = "";
		location2 = "";
		direction2 = "";
		try {
			loadDictionary();
			this.openStreams(clientSocket1, clientSocket2);
			name1 = in1.readUTF();
			name2 = in2.readUTF();

			for (int i = 0; i < rack1.length; i++) {
				rack1[i] = tiles[rnd.nextInt(tiles.length)];
			}

			for (int i = 0; i < rack2.length; i++) {
				rack2[i] = tiles[rnd.nextInt(tiles.length)];
			}

			if (isPlayerTurn2) {
				state = State.I6;
				out2.writeUTF(getGameState(2));
			} else {
				state = State.I3;
				out1.writeUTF(getGameState(1));
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

		playerState1 = State.I3;
		playerState2 = State.I6;

		while (true) {

			System.out.println("Switch: " + switchTurn);
			
			if (switchTurn) {
				try {
					if (state == State.I5 || state == State.I3) {
						System.out.println("Switched to Player 2 as " + playerState2);
						playerState1 = state;
						state = playerState2;
						out2.writeUTF(getGameState(2));
						out1.writeUTF(getGameState(1));
					} else if (state == State.I8 || state == State.I6) {
						System.out.println("Switched to Player 1 as " + playerState1);
						playerState2 = state;
						state = playerState1;
						out2.writeUTF(getGameState(2));
						out1.writeUTF(getGameState(1));
					}

					switchTurn = false;
				} catch (IOException e) {
					e.getStackTrace();
				}
			}

			String input = "";

			char[] rack;

			try {
				if ((state == State.I3 || state == State.I6) && turn < 6) { // ask for a location
					if (state == State.I3)
						input = in1.readUTF().toUpperCase();
					else
						input = in2.readUTF().toUpperCase();

					if (input.length() != 2) {
						if (state == State.I3)
							out1.writeUTF(getGameState(2) + "\nInvalid location!");
						else
							out2.writeUTF(getGameState(2) + "\nInvalid location!");
					} else {
						String a = input.substring(0, 1);
						String b = input.substring(1);

						if (!Pattern.matches(".*[A-J].*", a)) {
							if (state == State.I3)
								out1.writeUTF(getGameState(1) + "\nInvalid location!");
							else
								out2.writeUTF(getGameState(2) + "\nInvalid location!");
						} else if (!Pattern.matches(".*[0-9].*", b)) {
							if (state == State.I3)
								out1.writeUTF(getGameState(1) + "\nInvalid location!");
							else
								out2.writeUTF(getGameState(2) + "\nInvalid location!");
						} else {
							if (state == State.I3) {
								location1 = input;
								state = State.I4;
								out1.writeUTF("OK");
							} else {
								location2 = input;
								state = State.I7;
								out2.writeUTF("OK");
							}
						}
					}
				} else if ((state == State.I4 || state == State.I7) && turn < 6) { // ask for a direction
					if (state == State.I4)
						input = in1.readUTF().toUpperCase();
					else
						input = in2.readUTF().toUpperCase();

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
							if (state == State.I4) {
								direction1 = input;
								state = State.I5;
								out1.writeUTF("OK");
							} else {
								direction2 = input;
								state = State.I8;
								out2.writeUTF("OK");
							}
						}
					}

				} else if ((state == State.I5 || state == State.I8) && turn < 6) { // ask for a word
					if (state == State.I5) {
						input = in1.readUTF().toUpperCase();
						rack = rack1;
					} else {
						input = in2.readUTF().toUpperCase();
						rack = rack2;
					}
					if (!isInDictionary(input)) {
						if (state == State.I5 && turn < 6)
							out1.writeUTF(getGameState(2) + "\nThe word " + input + " is not in the dictionary.");
						else if (turn < 6)
							out2.writeUTF(getGameState(2) + "\nThe word " + input + " is not in the dictionary.");

						switchTurn = true;
						turn++;
					} else {
						int row, col;
						char c = ' ';

						if (state == State.I5)
							c = location1.charAt(0);
						else
							c = location2.charAt(0);

						boolean isSuccessful = false;
						boolean standsAlone = false;
						boolean[] notFromHand = new boolean[input.length()];

						row = c == 'A' ? 0
								: c == 'B' ? 1
										: c == 'C' ? 2
												: c == 'D' ? 3
														: c == 'E' ? 4
																: c == 'F' ? 5
																		: c == 'G' ? 6
																				: c == 'H' ? 7 : c == 'I' ? 8 : 9;

						if (state == State.I5)
							col = Integer.parseInt(location1.charAt(1) + "");
						else
							col = Integer.parseInt(location2.charAt(1) + "");

						String direction = "";

						if (state == State.I5)
							direction = direction1;
						else
							direction = direction2;

						if (direction.equals("A")) {
							if (input.length() > (board.length - col)) {
								if (state == State.I5 && turn < 6)
									out1.writeUTF(input + " is too long to fit on the board.");
								else if (turn < 6)
									out2.writeUTF(input + " is too long to fit on the board.");
							} else {
								for (int i = 0; i < input.length(); i++) {
									if (board[row][col + i] == input.charAt(i)) { // match, don't take out from rack
										for (int j = 0; j < rack.length; j++) {
											if (rack[j] == input.charAt(i))
												notFromHand[i] = true;
										}
									} else if (board[row][col + i] == '\u0000') { // insert
										isSuccessful = isValidSpot(input.charAt(i), row, col + i, direction);
									} else { // collision & no match
										turn++;
										if (state == State.I5 && turn < 6)
											out1.writeUTF(input.charAt(i) + " in " + input
													+ " conflicts with a different letter on the board.");
										else if (turn < 6)
											out2.writeUTF(input.charAt(i) + " in " + input
													+ " conflicts with a different letter on the board.");
										isSuccessful = false;
										break;
									}
								}
							}
						} else {
							if (input.length() > (board.length - row)) {
								turn++;
								if (state == State.I5 && turn < 6)
									out1.writeUTF(input + " is too long to fit on the board.");
								else if (turn < 6)
									out2.writeUTF(input + " is too long to fit on the board.");
							} else {
								for (int i = 0; i < input.length(); i++) {
									if (board[row + i][col] == input.charAt(i)) { // match, don't take out from rack
										for (int j = 0; j < rack.length; j++) {
											if (rack[j] == input.charAt(i))
												notFromHand[j] = true;
										}
									} else if (board[row + i][col] == '\u0000') { // insert
										isSuccessful = isValidSpot(input.charAt(i), row + i, col, direction);
									} else { // collision & no match
										if (state == State.I5 && turn < 6)
											out1.writeUTF(input.charAt(i) + " in " + input
													+ " conflicts with a different letter on the board.");
										else if (turn < 6)
											out2.writeUTF(input.charAt(i) + " in " + input
													+ " conflicts with a different letter on the board.");
										isSuccessful = false;
										break;
									}
								}
							}
						}

						if (isStandingAlone && !isFirstWord && !isSuccessful) {
							if (state == State.I5 && turn < 6)
								out1.writeUTF(input + " does not build on an existing word.");
							else if (turn < 6)
								out2.writeUTF(input + " does not build on an existing word.");
							switchTurn = true;
							turn++;
						} else if (isSuccessful) {
							isFirstWord = false;
							for (int i = 0; i < input.length(); i++) {
								if (direction.equals("D"))
									board[row + i][col] = input.charAt(i);
								else if (direction.equals("A"))
									board[row][col + i] = input.charAt(i);
							}

							System.out.println("Successful");
							for (int i = 0; i < rack.length; i++) { // refil the rack
								for (int j = 0; j < notFromHand.length; j++) {
									if (!notFromHand[j] && input.charAt(j) == rack[i]) {
										rack[i] = tiles[rnd.nextInt(tiles.length)];
										notFromHand[j] = false;
									}
								}
							}
							if (state == State.I5) {
								state = State.I3;
								rack1 = rack;
								location1 = "";
								direction1 = "";
							} else if (state == State.I8) {
								state = State.I6;
								rack2 = rack;
								location2 = "";
								direction2 = "";
							}
						}
						isStandingAlone = true;
						switchTurn = true;
						turn++;
					}
				}

				if (turn >= 6) { // gameover
					if (score1 > score2) {
						out1.writeUTF("You won - GAME OVER!");
						out2.writeUTF("You lose - GAME OVER!");
					} else if (score1 < score2) {
						out2.writeUTF("You won - GAME OVER!");
						out1.writeUTF("You lose - GAME OVER!");
					} else {
						out1.writeUTF("You tied - GAME OVER!");
						out2.writeUTF("You tied - GAME OVER!");
					}
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}// run method

	/**
	 * 
	 * @param col
	 * @param row
	 * @return whether the col or row is within the board
	 */
	private boolean isWithinBoard(int col, int row) {
		return (col < 10 && col > -1 && row < 10 && row > -1);
	}

	/**
	 * Determines whether the current spot for a letter is valid.
	 * 
	 * @param addedChar
	 * @param row
	 * @param col
	 * @param direction
	 * @return
	 */
	private boolean isValidSpot(char addedChar, int row, int col, String direction) {
		int tempRow = row;
		int tempCol = col;

		String s = "", reverse = "";

		if (direction.equals("D") && board[tempRow][tempCol - 1] != '\u0000') {
			while (isWithinBoard(tempRow, tempCol + 1) && board[tempRow][tempCol + 1] != '\u0000') {
				s += board[tempRow][tempCol];
				tempCol++;
			} // left
			for (int i = s.length() - 1; i >= 0; i--) {
				reverse = reverse + s.charAt(i);
			}
			if (!isInDictionary(reverse))
				return false;

			tempRow = row;
			tempCol = col;
			s = "";
			s += addedChar;
			reverse = "";
		}

		if (board[tempRow][tempCol - 1] != '\u0000') {
			while (isWithinBoard(tempRow, tempCol - 1) && board[tempRow][tempCol - 1] != '\u0000') {
				s += board[tempRow][tempCol];
				tempCol--;
			} // left
			for (int i = s.length() - 1; i >= 0; i--) {
				reverse = reverse + s.charAt(i);
			}
			if (!isInDictionary(reverse))
				return false;

			tempRow = row;
			tempCol = col;
			s = "";
			s += addedChar;
			reverse = "";
		}

		if (direction.equals("A") && board[tempRow + 1][tempCol] != '\u0000') {
			while (isWithinBoard(tempRow + 1, tempCol) && board[tempRow + 1][tempCol] != '\u0000') {
				s += board[tempRow + 1][tempCol];
				tempRow++;
			} // down
			if (!isInDictionary(s))
				return false;

			tempRow = row;
			tempCol = col;
			s = "";
			s += addedChar;
			reverse = "";
		}

		if (board[tempRow - 1][tempCol] != '\u0000') {
			while (isWithinBoard(tempRow - 1, tempCol) && board[tempRow - 1][tempCol] != '\u0000') {
				s += board[tempRow - 1][tempCol];
				tempRow--;
			} // up

			for (int i = s.length() - 1; i >= 0; i--) {
				reverse = reverse + s.charAt(i);
			}
			if (!isInDictionary(reverse))
				return false;
		}
		return true;
	}

	/*
	 * return the string representation of the current game state from the
	 * perspective of the given player (i.e., 1 or 2). More precisely, the returned
	 * string must contain, in order: + the state of the board + the turn number +
	 * the scores of both players + the rack of the given player The format of the
	 * returned string is fully specified in the traces included in the handout.
	 */
	String getGameState(int player) {

		String output = "";

		int score = 0;

		if (player == 1)
			score = score1;
		else
			score = score2;

		String turns = "";

		System.out.println(turn);

		if (turn < 2)
			turns = "1";
		else if (turn < 4)
			turns = "2";
		else if (turn < 6)
			turns = "3";

		output = this.toString() + "Turn: " + turns + "\nScores: " + score1 + " (opponent: " + score2 + ")\nRack: ";

		if (player == 1) {
			for (char c : rack1) {
				output += c + "";
			}
		} else {
			for (char c : rack2) {
				output += c + "";
			}
		}

		String location = "";
		String direction = "";

		if (player == 1) {
			location = location1;
			direction = direction1;
		} else {
			location = location2;
			direction = direction2;
		}

		if (!location.equals(""))
			output += "\nStart location of your word (e.g., B3?) " + location;
		if (!direction.equals(""))
			output += "\nDirection of your word (A or D): " + direction;

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
				dict[count++] = line.trim();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
			if (s.toLowerCase().equals(word.toLowerCase()))
				return true;
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
