package application;

import java.io.*;
import java.net.*;
import java.util.*;

import javafx.concurrent.Task;
import javafx.scene.paint.Color;

public class Ftp extends Task<List<File>> {

	private Socket connectionSocket = null;
	private Socket DataSocket = null;
	private BufferedReader reader = null;
	private PrintStream writer = null;
	private ArrayList<Items> Global_List = new ArrayList<Items>();
	private boolean isLoggedIn = false;
	private String lineTerm = "\n";
	private static String CurrentDir = "";
	private static int BLOCK_SIZE = 4096;
	private static boolean Debug = false;

	public Ftp() {
	}

	// debug mode status. (true = debug)
	public Ftp(boolean debug) {
		Debug = debug;
	}

	public boolean connect(String host, int port) throws UnknownHostException, IOException, ConnectException {
		try {
			connectionSocket = new Socket(host, port);
			writer = new PrintStream(connectionSocket.getOutputStream());
		} catch (Exception e) {
			return false;
		}

		reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

		if (!PositiveCompletionreply(getServerReplyCode())) {
			disconnect();
			return false;
		}

		return true;
	}

	public boolean login(String username, String password) throws IOException {
		int response = executeCommand("user " + username);
		if (!PositiveIntermediatereply(response))
			return false;
		response = executeCommand("pass " + password);
		isLoggedIn = PositiveCompletionreply(response);
		return isLoggedIn;
	}

	public boolean logout() throws IOException {
		int response = executeCommand("quit");
		isLoggedIn = !PositiveCompletionreply(response);
		return !isLoggedIn;
	}

	public void disconnect() {
		if (writer != null) {
			try {
				if (isLoggedIn) {
					logout();
				}
				writer.close();
				reader.close();
				connectionSocket.close();
			} catch (IOException e) {
			}
			writer = null;
			reader = null;
			connectionSocket = null;
		}
	}

	public boolean changeDirectory(String directory) throws IOException {
		int response = executeCommand("cwd " + directory);
		return PositiveCompletionreply(response);
	}

	public boolean renameFile(String oldName, String newName) throws IOException {
		int response = executeCommand("rnfr " + oldName);
		if (!PositiveIntermediatereply(response))
			return false;
		response = executeCommand("rnto " + newName);
		return PositiveCompletionreply(response);
	}

	public String changecurrentdirectory(String filename) {
		CurrentDir = CurrentDir + "/" + filename;
		return CurrentDir;
	}

	public String olddirectory() {
		String[] splitedCurrentDir = CurrentDir.split("/");
		int splitedsize = splitedCurrentDir.length;
		if (CurrentDir.length() >= (splitedCurrentDir[splitedsize - 1].length() + 1))
			CurrentDir = CurrentDir.substring(0,
					CurrentDir.length() - (splitedCurrentDir[splitedsize - 1].length() + 1));

		return CurrentDir;
	}

	public String currentdirectory() {
		return CurrentDir;
	}

	public boolean makeDirectory(String directory) throws IOException {
		int response = executeCommand("mkd " + directory);
		return PositiveCompletionreply(response);
	}

	/// Removes an empty directory
	public boolean removeDirectory(String directory) throws IOException {
		int response = executeCommand("rmd " + directory);
		return PositiveCompletionreply(response);
	}

	/// Removes a non-empty directory
	public void removeDirectory(Ftp clientFtp, String currentDir) throws Exception {
		if (currentDir == "Root" || currentDir == "...") {
			currentDir = "";
		}
		ArrayList<Items> subFiles = clientFtp.listFiles(currentDir, clientFtp);
		File currentDirFilePC = new File(currentDir);
		if (subFiles != null && subFiles.size() > 0) {
			for (Items aFile : subFiles) {
				String currentFileName = aFile.getPath();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				File resultFile = new File(currentFileName);
				currentFileName = resultFile.getName();
				currentFileName = currentFileName.replace("\n", "").replace("\r", "");
				String filePath = currentDirFilePC + "/" + currentFileName;
				/*
				 * if (currentDirFilePC.equals("")) { filePath = parentDir + "/" +
				 * currentFileName; newDirPathPC = saveDir + parentDir + File.separator +
				 * currentFileName; }
				 */
				if (aFile.isFolder()) {
					// delete the sub directory
					debugPrint("trying to delete the directory: " + currentFileName, Color.BLUE);
					removeDirectory(clientFtp, filePath);
				} else {
					// delete the file
					boolean success = deleteFile(filePath);
					if (success) {
						debugPrint("DELETED the file: " + currentFileName, Color.GREEN);
					} else {
						debugPrint("COULD NOT delete the file: " + currentFileName, Color.RED);
					}
				}
			}
		}
		// finally, remove the directory itself

		boolean removed = removeDirectory(currentDir);
		if (removed) {
			debugPrint("REMOVED the directory: " + currentDir, Color.GREEN);
		} else {
			debugPrint("CANNOT remove the directory: " + currentDir, Color.RED);
			File ppp = new File(currentDir);
			String pppeParent = ppp.getParent();
			removeDirectory(clientFtp, pppeParent);
		}
	}

	public boolean deleteFile(String fileName) throws IOException {
		int response = executeCommand("dele " + fileName);
		return PositiveCompletionreply(response);
	}

	public boolean downloadFile(String serverPath, String fileName) throws IOException {
		File newDir = new File(fileName);
		String newDirParent = newDir.getName();

		String command = "retr " + fileName;
		fileName = serverPath + File.separator + newDirParent;

		// open the local file
		RandomAccessFile outfile = new RandomAccessFile(fileName, "rw");

		// Convert the RandomAccessFile to an OutputStream
		FileOutputStream fileStream = new FileOutputStream(outfile.getFD());
		boolean resultt = executeDataCommand(command, fileStream);

		outfile.close();

		if (resultt) {
			debugPrint("DOWNLOADED the file: " + fileName, Color.GREEN);
		} else {
			debugPrint("COULD NOT download the file: " + fileName, Color.RED);
		}
		return resultt;
	}

	private void copy(File file) throws Exception {
		this.updateMessage("Transferring: " + file.getName());
	}

	int count = 0;
	int i = 0;

	public List<File> downloadDirectory(Ftp clientFtp, String currentDir, String saveDir) throws Exception {
		if (currentDir == "Root" || currentDir == "...") {
			currentDir = "";
		}
		ArrayList<Items> subFiles = clientFtp.listFiles(currentDir, clientFtp);
		List<File> copied = new ArrayList<File>();
		File currentDirFilePC = new File(currentDir);
		File firstdirectoryPC = new File(saveDir + File.separator + currentDirFilePC.getName());
		boolean created = firstdirectoryPC.mkdirs();
		debugPrint("trying to creat the directory: " + saveDir + File.separator + currentDirFilePC.getName(),
				Color.BLUE);
		if (created) {
			debugPrint("CREATED the directory: " + saveDir + File.separator + currentDirFilePC.getName(), Color.GREEN);
		} else {
			debugPrint("COULD NOT create the directory: " + saveDir + File.separator + currentDirFilePC.getName(),
					Color.RED);
		}

		count += subFiles.size();

		if (subFiles != null && subFiles.size() > 0) {
			for (Items aFile : subFiles) {
				String currentFileName = aFile.getPath();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				File resultFile = new File(currentFileName);
				currentFileName = resultFile.getName();
				currentFileName = currentFileName.replace("\n", "").replace("\r", "").replace(" ", "");
				String filePath = currentDirFilePC + File.separator + currentFileName;
				String newDirPathPC = firstdirectoryPC.getAbsolutePath();
				/*
				 * if (currentDirFilePC.equals("")) { filePath = parentDir + "/" +
				 * currentFileName; newDirPathPC = saveDir + parentDir + File.separator +
				 * currentFileName; }
				 */
				if (aFile.isFolder()) {
					debugPrint("trying to creat the directory: " + newDirPathPC + File.separator + currentFileName,
							Color.BLUE);
					File newDir = new File(newDirPathPC + File.separator + currentFileName);
					boolean createdSubDir = newDir.mkdirs();
					if (createdSubDir) {
						debugPrint("CREATED the directory: " + newDirPathPC + File.separator + currentFileName,
								Color.GREEN);
					} else {
						debugPrint("COULD NOT create the directory: " + newDirPathPC + File.separator + currentFileName,
								Color.RED);
					}
					// download the sub directory
					copied.addAll(downloadDirectory(clientFtp, filePath, newDirPathPC));
				} else {
					// download the file
					boolean success = downloadFile(newDirPathPC, filePath);
					if (success) {
						debugPrint("DOWNLOADED the file: " + currentFileName, Color.GREEN);
						this.copy(resultFile);
						copied.add(resultFile);
					} else {
						debugPrint("COULD NOT download the file: " + currentFileName, Color.RED);
					}

				}
				i++;
				this.updateProgress(i, count);
			}

		}
		return copied;
	}

	public boolean uploadFile(String path) throws IOException {
		File f = new File(path);
		FileInputStream fis = new FileInputStream(path);

		boolean success = executeDataCommand("stor " + f.getName(), fis);
		return success;
	}

	public boolean uploadFile2(String home, String path) throws IOException {
		File f = new File(path);
		FileInputStream fis = new FileInputStream(path);
		boolean success = executeDataCommand("stor " + home + "/" + f.getName(), fis);
		return success;
	}

	public boolean uploadFile(String ftplocation, String locallocation) throws IOException {
		FileInputStream fis = new FileInputStream(locallocation);
		ftplocation = ftplocation.replace("\\", "/");
		boolean success = executeDataCommand("stor " + ftplocation, fis);
		return success;
	}

	public List<File> uploadDirectory(String remoteDirPath, String localParentDir, String remoteParentDir)
			throws Exception {

		debugPrint("LISTING directory: " + localParentDir);
		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();

		List<File> copied = new ArrayList<File>();
		int count = subFiles.length;
		int i = 0;
		this.updateProgress(i, count);
		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {
				String remoteFilePath = remoteDirPath + remoteParentDir + File.separator + item.getName();
				if (remoteParentDir.equals("")) {
					remoteFilePath = remoteDirPath + item.getName();
				}
				if (item.isFile()) {
					// upload the file
					String localFilePath = item.getAbsolutePath();
					debugPrint("About to upload the file: " + localFilePath);
					File resultFile = new File(localFilePath);
					this.copy(resultFile);
					boolean uploaded = uploadFile(remoteFilePath, localFilePath);
					if (uploaded) {
						debugPrint("UPLOADED a file " + localFilePath + " to: " + remoteFilePath, Color.GREEN);
						copied.add(resultFile);
					} else {
						debugPrint("COULD NOT upload the file: " + localFilePath, Color.RED);
					}
				} else {
					// create directory on the server
					boolean created = makeDirectory(remoteFilePath);
					if (created) {
						debugPrint("CREATED the directory: " + remoteFilePath, Color.GREEN);
					} else {
						debugPrint("COULD NOT create the directory: " + remoteFilePath, Color.RED);
					}
					// upload the sub directory
					String parent = remoteParentDir + File.separator + item.getName();
					if (remoteParentDir.equals("")) {
						parent = item.getName();
					}

					localParentDir = item.getAbsolutePath();
					copied.addAll(uploadDirectory(remoteDirPath, localParentDir, parent));
				}
				i++;
				this.updateProgress(i, count);
			}
		}
		return copied;
	}

	/// Retrieve the response code from the FTP server to identify the type of the
	/// response.
	private int getServerReplyCode() throws IOException {
		return Integer.parseInt(getServerReplyMessage().substring(0, 3));
	}

	private String getServerReplyMessage() throws IOException {
		String reply;

		do {
			reply = reader.readLine();
			debugPrint(reply);
		} while (!(Character.isDigit(reply.charAt(0)) && Character.isDigit(reply.charAt(1))
				&& Character.isDigit(reply.charAt(2)) && reply.charAt(3) == ' '));

		return reply;
	}

	boolean tree;

	public ArrayList<application.Items> listFiles(String ftplocation, Ftp clientFtp, boolean Tree) throws IOException {
		tree = Tree;
		return listFiles(ftplocation, clientFtp);
	}

	public ArrayList<application.Items> listFiles(String params, Ftp clientFtp) throws IOException {
		StringBuffer files = new StringBuffer();
		StringBuffer dirs = new StringBuffer();

		return getAndParseDirList(params, files, dirs, clientFtp);
	}

	private String processFileListCommand(String command) throws IOException {
		StringBuffer reply = new StringBuffer();
		String replyString;

		boolean success = executeDataCommand(command, reply);

		if (!success) {
			return "";
		}

		replyString = reply.toString();

		if (reply.length() > 0) {
			return replyString.substring(0, reply.length() - 1);
		} else {
			return replyString;
		}
	}

	/// recover the list of files and subfolders.
	private ArrayList<application.Items> getAndParseDirList(String params, StringBuffer files, StringBuffer dirs,
			Ftp clientFtp) throws IOException {
		files.setLength(0);
		dirs.setLength(0);
		// List requests are made with the NLST and LIST commands
		String shortList = processFileListCommand("NLST " + params);
		String longList = processFileListCommand("LIST " + params);

		// We tokenize the recovered lines
		StringTokenizer sList = new StringTokenizer(shortList, "\n");
		StringTokenizer lList = new StringTokenizer(longList, "\n");

		String sString;
		String lString;

		ArrayList<Items> Directorie = new ArrayList<Items>();

		while ((sList.hasMoreTokens()) && (lList.hasMoreTokens())) {
			sString = sList.nextToken();
			lString = lList.nextToken();

			if (lString.length() > 0) {
				if (lString.startsWith("d")) {
					dirs.append(sString.trim() + lineTerm);
					if (sString.startsWith("..")) {
						sString = sString.substring(3);
					}
					Directorie.add(new Items(true, sString));

				} else if (lString.startsWith("-")) {
					files.append(sString.trim() + lineTerm);
					Directorie.add(new Items(false, sString));
				}
			}
		}

		if (tree)
			fillList(Directorie, clientFtp);

		if (files.length() > 0) {
			files.setLength(files.length() - lineTerm.length());
		}
		if (dirs.length() > 0) {
			dirs.setLength(dirs.length() - lineTerm.length());
		}
		return Directorie;

	}

	void addList(ArrayList<Items> lpere, int l, ArrayList<Items> lfills) {
		for (int j = 0; j < lfills.size(); j++) {
			lpere.get(l).addChild(new Items(lfills.get(j).isFolder(), lfills.get(j).getPath()));
		}
		for (int j = 0; j < lfills.size(); j++) {
			if (lfills.get(j).isFolder() == true) {
				ArrayList<Items> newlpere = lpere.get(l).getchilderns();
				lfills = lfills.get(j).getchilderns();
				Global_List.addAll(newlpere);
				addList(newlpere, j, lfills);
			}
		}

	}

	void fillList(ArrayList<Items> Lpere, Ftp clientFtp) throws IOException {
		Global_List = Lpere;
		for (int l = 0; l < Lpere.size(); l++) {
			if (Lpere.get(l).isFolder() == true) {
				ArrayList<Items> Lfills = clientFtp.listFiles(Lpere.get(l).getPath(), clientFtp);
				addList(Lpere, l, Lfills);
			}
		}
	}

	public int executeCommand(String command) throws IOException {
		if (Debug)
			debugPrint(command, Color.BLUE);
		writer.println(command);
		return getServerReplyCode();
	}

	public boolean executeDataCommand(String command, OutputStream out) throws IOException {

		if (!setupDataPasv(command))
			return false;
		InputStream in = DataSocket.getInputStream();
		Write(in, out);
		in.close();
		DataSocket.close();

		return PositiveCompletionreply(getServerReplyCode());
	}

	public boolean executeDataCommand(String command, InputStream in) throws IOException {

		if (!setupDataPasv(command))
			return false;
		OutputStream out = DataSocket.getOutputStream();
		Write(in, out);
		out.close();
		DataSocket.close();

		return PositiveCompletionreply(getServerReplyCode());
	}

	public boolean executeDataCommand(String command, StringBuffer sb) throws IOException {

		if (!setupDataPasv(command))
			return false;
		InputStream in = DataSocket.getInputStream();
		Write(in, sb);
		in.close();
		DataSocket.close();

		return PositiveCompletionreply(getServerReplyCode());
	}

	private void Write(InputStream in, OutputStream out) throws IOException {
		byte block[] = new byte[BLOCK_SIZE];
		int len;
		// Store the data in a file
		while ((len = in.read(block)) > 0) {
			try {
				out.write(block, 0, len);

			} catch (SocketException e) {
				return;
			}
		}
	}

	private void Write(InputStream in, StringBuffer sb) throws IOException {
		byte block[] = new byte[BLOCK_SIZE];
		int len;
		// Store the data in a buffer
		while ((len = in.read(block)) > 0) {
			sb.append(new String(block, 0, len));
		}
	}

	private boolean setupDataPasv(String command) throws IOException {

		if (Debug)
			debugPrint("PASV", Color.BLUE);
		writer.println("PASV");
		String tmp = getServerReplyMessage();
		String pasv = excludeCode(tmp);
		/// get the IP and PORT for the connection
		pasv = pasv.substring(pasv.indexOf("(") + 1, pasv.indexOf(")"));
		String[] splitedPasv = pasv.split(",");
		int port1 = Integer.parseInt(splitedPasv[4]);
		int port2 = Integer.parseInt(splitedPasv[5]);
		int port = (port1 * 256) + port2;
		String ip = splitedPasv[0] + "." + splitedPasv[1] + "." + splitedPasv[2] + "." + splitedPasv[3];
		DataSocket = new Socket(ip, port);

		boolean Pasv = PositiveCompletionreply(Integer.parseInt(tmp.substring(0, 3)));

		if (!Pasv)
			return false;

		// Start binary mode for data reception
		if (Debug)
			debugPrint("TYPE i", Color.BLUE);
		writer.println("TYPE i");
		if (!PositiveCompletionreply(getServerReplyCode())) {
			debugPrint("Could not set transfer type", Color.RED);
			return false;
		}
		// send command
		if (Debug)
			debugPrint(command, Color.BROWN);
		writer.println(command);

		return PositivePreliminaryreply(getServerReplyCode());
	}

	// display the FTP messages
	private void debugPrint(String message) {
		if (Debug)
			homepage.transferText(message + "\n", Color.BLACK);
	}

	private void debugPrint(String message, Color color) {
		if (Debug)
			homepage.transferText(message + "\n", color);
	}

	/// The requested action is being initiated, expect another reply before
	/// proceeding with a new command.
	private boolean PositivePreliminaryreply(int replay) {
		return (replay >= 100 && replay < 200);
	}

	/// The requested action has been successfully completed.
	private boolean PositiveCompletionreply(int replay) {
		return (replay >= 200 && replay < 300);
	}

	/// The command has been accepted, but the requested action is on hold, pending
	/// receipt of further information.
	private boolean PositiveIntermediatereply(int replay) {
		return (replay >= 300 && replay < 400);
	}

	// Deletes the response code at the start of a string.
	private String excludeCode(String replay) {
		if (replay.length() < 5)
			return replay;
		return replay.substring(4);
	}

	@Override
	protected List<File> call() throws Exception {
		return null;
	}
}