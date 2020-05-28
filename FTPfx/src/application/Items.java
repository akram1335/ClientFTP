package application;

import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;

public class Items {
	boolean isFolder;
	private final SimpleStringProperty path;
	ArrayList<Items> MyContents = new ArrayList<Items>();

	public Items(boolean isFolder, String path) {
		this.isFolder = isFolder;
		this.path = new SimpleStringProperty(path);
	}

	public void addChild(Items Child) {

		MyContents.add(Child);
	}

	public ArrayList<Items> getchilderns() {
		return MyContents;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public String getPath() {
		return path.get();
	}

}
