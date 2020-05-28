package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.collections.ObservableList;

final class TextFieldTreeCellImpl extends TreeCell<String> {

	private TextField textField;
	int nbroffolder = 0;
	private final ContextMenu menuForRootItem = new ContextMenu();
	private final ContextMenu menuForFolder = new ContextMenu();
	private final ContextMenu menuForFiles = new ContextMenu();

	private ArrayList<Items> dossiers1;
	private Ftp ftp1;
	String cDIR;
	boolean treeon;

	public TextFieldTreeCellImpl(Ftp ftp, Image depIcon, Image fileIcon, TreeItem<String> rootNode,
			ArrayList<Items> doss, boolean b) {

		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		MenuItem addNewfoldertoRoot = new MenuItem("Creat new Folder");
		MenuItem addNewfoldertoFolder = new MenuItem("Creat new Folder");
		MenuItem uploadtoRoot = new MenuItem("Upload File");
		MenuItem uploadtoFolder = new MenuItem("Upload File");
		MenuItem uploadFoldertoRoot = new MenuItem("Upload Folder");
		MenuItem uploadFoldertoFolder = new MenuItem("Upload Folder");
		MenuItem deleteFile = new MenuItem("Delete");
		MenuItem deleteFolder = new MenuItem("deleteFolder");
		MenuItem downloadFolder = new MenuItem("download");
		MenuItem downloadRoot = new MenuItem("download");
		MenuItem downloadFile = new MenuItem("download");
		MenuItem back = new MenuItem("back");
		MenuItem enter = new MenuItem("enter");
		dossiers1 = doss;
		treeon = b;

		ftp1 = ftp;
		menuForRootItem.getItems().addAll(back, downloadRoot, addNewfoldertoRoot, uploadtoRoot, uploadFoldertoRoot);
		menuForFiles.getItems().addAll(downloadFile, deleteFile);
		menuForFolder.getItems().addAll(enter, downloadFolder, addNewfoldertoFolder, uploadtoFolder,
				uploadFoldertoFolder, deleteFolder);

		back.setOnAction((ActionEvent t) -> {

			try {

				cDIR = ftp.olddirectory();
				ftp.changeDirectory("..");
				homepage.creatTreeView(ftp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		enter.setOnAction((ActionEvent t) -> {
			String ftplocation = getTreeItem().getValue();
			ftplocation = ftplocation.replace("\n", "").replace("\r", "");
			try {
				cDIR = ftp.changecurrentdirectory(ftplocation);
				ftp.changeDirectory(ftplocation);
				homepage.creatTreeView(ftp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		downloadFile.setOnAction((ActionEvent t) -> {

			try {
				File selectedDirectory = directoryChooser.showDialog(null);
				if (selectedDirectory != null) {
					String locallocation = selectedDirectory.getAbsolutePath();
					new Thread(new Runnable() {
						@Override
						public void run() {
							final Cursor oldCursor = getScene().getCursor();
							getScene().setCursor(Cursor.WAIT);
							homepage.transferText("downloading starts, please wait\n", Color.BLACK);

							try {
								String ftplocation = getTreeItem().getValue();
								ftplocation = ftplocation.replace("\n", "").replace("\r", "");
								ftp.downloadFile(locallocation, ftplocation);
							} catch (IOException e) {

								e.printStackTrace();
							}
							getScene().setCursor(oldCursor);
						}
					}).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		downloadFolder.setOnAction((ActionEvent t) -> {
			File selectedDirectory = directoryChooser.showDialog(null);
			if (selectedDirectory != null) {
				String saveDir = selectedDirectory.getAbsolutePath();
				String currentDir = getTreeItem().getValue();
				currentDir = currentDir.replace("\n", "").replace("\r", "");

				try {
					Stage stagee = new Stage();
					downloadwindow dd = new downloadwindow("down", ftp, currentDir, saveDir);
					dd.start(stagee);

				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});

		downloadRoot.setOnAction((ActionEvent t) -> {
			File selectedDirectory = directoryChooser.showDialog(null);
			if (selectedDirectory != null) {
				String saveDir = selectedDirectory.getAbsolutePath();
				String currentDir = getTreeItem().getValue();
				currentDir = currentDir.replace("\n", "").replace("\r", "");

				try {
					Stage stagee = new Stage();
					downloadwindow dd = new downloadwindow("down", ftp, currentDir, saveDir);
					dd.start(stagee);

				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});

		addNewfoldertoRoot.setOnAction((ActionEvent t) -> {

			String foldernamee = "New Folder ";
			for (int i = 0; i < dossiers1.size(); i++) {

				String dir = dossiers1.get(i).getPath().replace("\n", "").replace("\r", "");
				do {
					if (dir.equals(foldernamee + nbroffolder)) {
						nbroffolder++;
					} else {
						break;
					}

				} while (true);

			}
			try {
				if (treeon) {

					ftp.makeDirectory("/New Folder " + nbroffolder);
				} else {

					ftp.makeDirectory(ftp.currentdirectory() + "/New Folder " + nbroffolder);
				}
				homepage.creatTreeView(ftp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		addNewfoldertoFolder.setOnAction((ActionEvent t) -> {
			String dir = getTreeItem().getValue();
			ObservableList<TreeItem<String>> dirchildren = getTreeItem().getChildren();
			dir = dir.replace("\n", "").replace("\r", "");
			String foldernamee = "New Folder ";
			for (int i = 0; i < dossiers1.size(); i++) {
				String dirT = dossiers1.get(i).getPath().replace("\n", "").replace("\r", "");
				for (int j = 0; j < dirchildren.size(); j++) {
					String dirch = dirchildren.get(j).getValue().replace("\n", "").replace("\r", "");
					if (dirch.equals(dirT + "/" + foldernamee + nbroffolder)) {
						nbroffolder++;
					} else {
						break;
					}
				}
			}
			try {

				ftp.makeDirectory("/" + dir + "/New Folder " + nbroffolder);
				homepage.creatTreeView(ftp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		uploadtoRoot.setOnAction((ActionEvent t) -> {
			File selectedFile = fileChooser.showOpenDialog(null);
			if (selectedFile != null) {
				String locallocation = selectedFile.getAbsolutePath();
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Cursor oldCursor = getScene().getCursor();
						getScene().setCursor(Cursor.WAIT);
						homepage.transferText("uploading starts, please wait\n", Color.BLACK);
						String home = ftp.currentdirectory();
						try {
							if (treeon || home.isEmpty()) {

								ftp.uploadFile(locallocation);
							} else {
								ftp.uploadFile2(home, locallocation);
							}
							homepage.creatTreeView(ftp);
						} catch (IOException e) {

							e.printStackTrace();
						}
						getScene().setCursor(oldCursor);

					}
				}).start();
			}
		});
		uploadtoFolder.setOnAction((ActionEvent t) -> {

			File selectedFile = fileChooser.showOpenDialog(null);
			if (selectedFile != null) {
				String locallocation = selectedFile.getAbsolutePath();
				File f = new File(locallocation);
				String filename = f.getName();
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Cursor oldCursor = getScene().getCursor();
						getScene().setCursor(Cursor.WAIT);
						homepage.transferText("uploading starts, please wait\n", Color.BLACK);
						try {
							String ftplocation = getTreeItem().getValue();
							ftplocation = ftplocation.replace("\n", "").replace("\r", "");
							String home = ftp.currentdirectory();
							if (treeon || home.isEmpty()) {

								ftp.uploadFile(File.separator + ftplocation + File.separator + filename, locallocation);
							} else {
								ftp.uploadFile(home + File.separator + ftplocation + File.separator + filename,
										locallocation);
							}
							homepage.creatTreeView(ftp);
						} catch (IOException e) {

							e.printStackTrace();
						}

						getScene().setCursor(oldCursor);

					}
				}).start();
			}
		});

		uploadFoldertoRoot.setOnAction((ActionEvent t) -> {
			File selectedDirectory = directoryChooser.showDialog(null);
			if (selectedDirectory != null) {
				String locallocation = selectedDirectory.getAbsolutePath();
				Stage stagee = new Stage();
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Cursor oldCursor = getScene().getCursor();
						getScene().setCursor(Cursor.WAIT);
						homepage.transferText("uploading starts, please wait\n", Color.BLACK);

						// ftp.makeDirectory(File.separator + selectedDirectory.getName());
						downloadwindow dd = new downloadwindow("up", ftp, locallocation,
								selectedDirectory.getName() + "\\");
						dd.start(stagee);

						getScene().setCursor(oldCursor);

					}
				}).start();
			}
		});
		uploadFoldertoFolder.setOnAction((ActionEvent t) -> {
			File selectedDirectory = directoryChooser.showDialog(null);
			if (selectedDirectory != null) {
				String locallocation = selectedDirectory.getAbsolutePath();
				Stage stagee = new Stage();
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Cursor oldCursor = getScene().getCursor();
						getScene().setCursor(Cursor.WAIT);
						homepage.transferText("uploading starts, please wait\n", Color.BLACK);
						try {
							String ftplocation = getTreeItem().getValue();
							ftplocation = ftplocation.replace("\n", "").replace("\r", "");
							ftp.makeDirectory(
									File.separator + ftplocation + File.separator + selectedDirectory.getName());

							downloadwindow dd = new downloadwindow("up", ftp, locallocation,
									ftplocation + "\\" + selectedDirectory.getName() + "\\");
							dd.start(stagee);
						} catch (Exception e) {

							e.printStackTrace();
						}
						getScene().setCursor(oldCursor);

					}
				}).start();
			}
		});

		deleteFile.setOnAction((ActionEvent t) -> {
			String ftplocation = getTreeItem().getValue();
			ftplocation = ftplocation.replace("\n", "").replace("\r", "");
			try {
				ftp.deleteFile(ftplocation);
				homepage.creatTreeView(ftp);
			} catch (IOException e) {

				e.printStackTrace();
			}

		});
		deleteFolder.setOnAction((ActionEvent t) -> {
			String ftplocation = getTreeItem().getValue();
			ftplocation = ftplocation.replace("\n", "").replace("\r", "");
			try {
				ftp.removeDirectory(ftp, ftplocation);
				homepage.creatTreeView(ftp);
			} catch (Exception e) {

				e.printStackTrace();
			}

		});

	}

	@Override
	public void startEdit() {
		super.startEdit();
		if (getTreeItem().getParent() != null) {
			if (textField == null) {
				createTextField();
			}
			setText(null);
			setGraphic(textField);
			textField.selectAll();
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText((String) getItem());
		setGraphic(getTreeItem().getGraphic());
	}

	@Override
	public void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				setText(getString());
				setGraphic(getTreeItem().getGraphic());
				if (getTreeItem().getParent() == null) {
					setContextMenu(menuForRootItem);
				}

				for (Items search : dossiers1) {
					if (getTreeItem().getValue().contentEquals(search.getPath())) {
						if (search.isFolder()) {
							setContextMenu(menuForFolder);

						} else {
							setContextMenu(menuForFiles);
						}
					}
				}

			}
		}
	}

	private void createTextField() {
		textField = new TextField(getString());
		textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			String OldName = getString();

			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					commitEdit(textField.getText());
					try {
						if (treeon) {

							ftp1.renameFile(OldName, textField.getText());
						} else {
							ftp1.renameFile(OldName, ftp1.currentdirectory() + "/" + textField.getText());
						}
						homepage.creatTreeView(ftp1);
					} catch (IOException e) {

						e.printStackTrace();
					}
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
				}
			}
		});
	}

	private String getString() {
		return getItem() == null ? "" : getItem().toString();
	}
}
