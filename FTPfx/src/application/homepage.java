package application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;

public class homepage extends Application {
	static Button LISTbtn;
	static Button Quitbtn;
	static Button connectbtn;
	static ScrollPane LogscrollPane;
	Cursor oldCursor;
	static Group group;
	Scene scene;
	static RadioButton treeon;
	TreeView<String> treeViews;
	static TextFlow LogtextFlow;
	static boolean firstimee = false;
	// private final static Node rootIcon = new ImageView(new
	// Image(ClassLoader.getSystemResourceAsStream("folder.png")));
	static TreeItem<String> rootNode = new TreeItem<String>("...");
	static ArrayList<Items> directory = new ArrayList<Items>();

	private final static Image depIcon = new Image(ClassLoader.getSystemResourceAsStream("folder.png"));
	private final static Image fileIcon = new Image(ClassLoader.getSystemResourceAsStream("file.png"));

	// launch the application
	public void start(Stage stage) throws FileNotFoundException {

		group = new Group();
		/////////
		Label IPtext = new Label("IP:");
		IPtext.setLayoutX(10);
		IPtext.setLayoutY(25);
		IPtext.setFont(Font.font(null, FontWeight.BOLD, 14));
		group.getChildren().addAll(IPtext);

		TextField IPtextfield = new TextField("127.0.0.1");
		IPtextfield.setLayoutX(50);
		IPtextfield.setLayoutY(25);
		IPtextfield.setPrefWidth(130);
		IPtextfield.setId("IPtextfieldblue");
		group.getChildren().addAll(IPtextfield);
		IPtextfield.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				try {
					String partialBlock = "(([01]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
					String subsequentPartialBlock = "(\\." + partialBlock + ")";
					String ipAddress = partialBlock + subsequentPartialBlock + subsequentPartialBlock
							+ subsequentPartialBlock;

					if (!newValue.matches(ipAddress)) {
						IPtextfield.setId("IPtextfieldred");
						connectbtn.setDisable(true);
					} else {
						IPtextfield.setId("IPtextfieldgreen");
						connectbtn.setDisable(false);
					}
				} catch (Exception e) {

				}
			}
		});
		///////////////
		Label Usertext = new Label("User:");
		Usertext.setLayoutX(10);
		Usertext.setLayoutY(75);
		Usertext.setFont(Font.font(null, FontWeight.BOLD, 14));
		group.getChildren().addAll(Usertext);

		TextField Usertextfield = new TextField("akram");
		Usertextfield.setLayoutX(50);
		Usertextfield.setLayoutY(75);
		Usertextfield.setPrefWidth(80);
		Usertextfield.setId("IPtextfieldblue");
		group.getChildren().addAll(Usertextfield);

		///////////////
		Label Passtext = new Label("Password:");
		Passtext.setLayoutX(205);
		Passtext.setLayoutY(75);
		Passtext.setFont(Font.font(null, FontWeight.BOLD, 14));
		group.getChildren().addAll(Passtext);
		PasswordField Passtextfield = new PasswordField();
		Passtextfield.setPromptText("Your password");
		Passtextfield.setLayoutX(280);
		Passtextfield.setLayoutY(75);
		Passtextfield.setPrefWidth(60);
		Passtextfield.setId("IPtextfieldblue");
		group.getChildren().addAll(Passtextfield);
		///////////////
		Label Porttext = new Label("Port:");
		Porttext.setLayoutX(205);
		Porttext.setLayoutY(25);
		Porttext.setFont(Font.font(null, FontWeight.BOLD, 14));
		group.getChildren().addAll(Porttext);
		TextField PORTtextfield = new TextField("21");
		PORTtextfield.setLayoutX(280);
		PORTtextfield.setLayoutY(25);
		PORTtextfield.setPrefWidth(60);
		PORTtextfield.setId("IPtextfieldblue");
		group.getChildren().addAll(PORTtextfield);
		PORTtextfield.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				try {
					int newValueINT = Integer.parseInt(newValue);
					if (newValueINT < 65535 && newValueINT >= 0) {
						PORTtextfield.setId("IPtextfieldgreen");

					} else {
						PORTtextfield.setId("IPtextfieldred");
					}
				} catch (Exception e) {

				}
			}
		});
		// Creating a Text object
		LogtextFlow = new TextFlow();
		LogtextFlow.setMaxWidth(600);

		// ScrollPane
		LogscrollPane = new ScrollPane();
		LogscrollPane.setContent(LogtextFlow);
		LogscrollPane.setLayoutX(10);
		LogscrollPane.setLayoutY(580);
		LogscrollPane.setPrefHeight(110);
		LogscrollPane.setPrefWidth(360);
		LogscrollPane.setVvalue(10.0);
		LogscrollPane.setPannable(true);
		group.getChildren().addAll(LogscrollPane);
		// Group
		ToggleGroup groupRadioDebug = new ToggleGroup();
		Label radioLabel = new Label("debug:");
		radioLabel.setLayoutX(190);
		radioLabel.setLayoutY(200);
		// Radio 1: ON
		RadioButton debugON = new RadioButton("ON");
		debugON.setToggleGroup(groupRadioDebug);
		debugON.setSelected(true);
		debugON.setLayoutX(240);
		debugON.setLayoutY(200);

		// Radio 2: OFF.
		RadioButton debugOF = new RadioButton("OFF");
		debugOF.setToggleGroup(groupRadioDebug);
		debugOF.setLayoutX(310);
		debugOF.setLayoutY(200);
		group.getChildren().addAll(radioLabel, debugON, debugOF);
		//////////////

		// Group 3
		ToggleGroup groupRadiotree = new ToggleGroup();
		Label radioLabeltree = new Label("tree(beta):");
		radioLabeltree.setLayoutX(170);
		radioLabeltree.setLayoutY(170);
		// Radio 3: treeon
		treeon = new RadioButton("ON");
		treeon.setToggleGroup(groupRadiotree);
		treeon.setLayoutX(240);
		treeon.setLayoutY(170);

		// Radio 4: treeoff.
		RadioButton treeoff = new RadioButton("OFF");
		treeoff.setToggleGroup(groupRadiotree);
		treeoff.setSelected(true);
		treeoff.setLayoutX(310);
		treeoff.setLayoutY(170);
		group.getChildren().addAll(radioLabeltree, treeon, treeoff);
		///////////////
		connectbtn = new Button("connect");
		connectbtn.setLayoutX(25);
		connectbtn.setLayoutY(130);
		connectbtn.setId("button");
		group.getChildren().addAll(connectbtn);
		connectbtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				LogtextFlow.getChildren().clear();
				/////////
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Cursor oldCursor = scene.getCursor();
						scene.setCursor(Cursor.WAIT);
						connectbtn.setDisable(true);
						transferText("connection may take a while, please wait\n", Color.BLACK);

						loginmethode(IPtextfield.getText(), Integer.valueOf(PORTtextfield.getText()),
								Usertextfield.getText(), Passtextfield.getText(), debugON.isSelected());
						scene.setCursor(oldCursor);
						connectbtn.setDisable(false);

					}
				}).start();

			}
		});
		//////////////
		LISTbtn = new Button("LIST");
		LISTbtn.setLayoutX(25);
		LISTbtn.setLayoutY(175);
		LISTbtn.setDisable(true);
		LISTbtn.setId("buttonlist");
		group.getChildren().addAll(LISTbtn);

		Quitbtn = new Button("disconnect");
		Quitbtn.setLayoutX(200);
		Quitbtn.setLayoutY(130);
		Quitbtn.setDisable(true);
		Quitbtn.setId("buttondisconnect");
		group.getChildren().addAll(Quitbtn);
		/////////////

		scene = new Scene(group, 380, 700);

		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		stage.setTitle("FTP client");
		stage.getIcons().add(new Image("ftpicon.png"));
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String args[]) throws InterruptedException {

		launch(args);
	}

	public static void loginmethode(String ip, int port, String user, String pass, boolean debug) {

		try {
			Ftp clientFtp = new Ftp(debug);

			if (clientFtp.connect(ip, port)) {
				if (clientFtp.login(user, pass)) {
					LISTbtn.setDisable(false);
					Quitbtn.setDisable(false);
					LISTbtn.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {

							try {
								creatTreeView(clientFtp);
							} catch (IOException e) {
								e.printStackTrace();

							}

						}
					});
					Quitbtn.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {

							LISTbtn.setDisable(true);
							connectbtn.setDisable(false);
							clientFtp.disconnect();
							rootNode.getChildren().clear();
							directory.clear();
						}
					});
				}
			} else {
				if (debug)
					transferText("incorrecte ip adress\n", Color.RED);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public ImageView getRootIcon() {
		return new ImageView(new Image(ClassLoader.getSystemResourceAsStream("folder.png")));
	}

	private static void beginfunc(ArrayList<Items> getchilderns, TreeItem<String> rootNode2) {

		for (int i = 0; i < getchilderns.size(); i++) {
			Items itm = getchilderns.get(i);
			if (itm.isFolder() == true) {
				TreeItem<String> dosLeaf = new TreeItem<String>(itm.getPath(), new ImageView(depIcon));
				rootNode2.getChildren().add(dosLeaf);
				directory.add(new Items(true, itm.getPath()));

				beginfunc(itm.getchilderns(), dosLeaf);
			} else {
				TreeItem<String> empLeaff = new TreeItem<String>(itm.getPath(), new ImageView(fileIcon));
				rootNode2.getChildren().add(empLeaff);

				directory.add(new Items(false, itm.getPath()));
			}
		}
	}

	public static void creatTreeView(Ftp clientFtp) throws IOException {

		if (firstimee) {
			rootNode.getChildren().clear();
			directory.clear();
		}
		ArrayList<Items> Folders = clientFtp.listFiles("", clientFtp, treeon.isSelected());
		beginfunc(Folders, rootNode);
		rootNode.setExpanded(true);

		TreeView<String> treeView = new TreeView<String>(rootNode);
		treeView.setPrefSize(360, 350);
		treeView.setTranslateX(10);
		treeView.setTranslateY(220);
		treeView.setEditable(true);
		treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
			@Override
			public TreeCell<String> call(TreeView<String> p) {
				return new TextFieldTreeCellImpl(clientFtp, depIcon, fileIcon, rootNode, directory,
						treeon.isSelected());
			}
		});
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				group.getChildren().addAll(treeView);
			}
		});
		firstimee = true;
	}

	public static void transferText(String message, Color color) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Text t1 = new Text();
				t1.setText(message);
				t1.setFill(color);
				LogtextFlow.getChildren().addAll(t1);
				LogscrollPane.vvalueProperty().bind(LogtextFlow.heightProperty());
			}
		});

	}

}