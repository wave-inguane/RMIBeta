import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class ServerData implements Serializable {
	private static final long serialVersionUID = 995624507044214456L;
	private static final String FILE = "calendar.dat";

	private ArrayList<String> calendarExist;

	public ServerData(ArrayList<String> existingCalendar) {
		calendarExist  = existingCalendar;
	}

	public ArrayList<String> getUsers() {
		return calendarExist ;
	}

	public static ServerData load() {
		if ((new File(FILE)).exists()) {
			try {
				FileInputStream fin = new FileInputStream(FILE);
				ObjectInputStream ois = new ObjectInputStream(fin);
				ServerData calendarData = (ServerData) ois.readObject();
				ois.close();

				return calendarData;

			} catch (Exception e) {
				System.err.println("Can't load data file.");
			}
		}

		return null;
	}

	public static boolean save(ArrayList<String> calendarExist) {
		try {
			FileOutputStream fout = new FileOutputStream(FILE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(new ServerData(calendarExist));
			oos.close();

			return true;

		} catch (Exception e) {
			return false;
		}

	}
}