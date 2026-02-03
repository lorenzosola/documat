package arcmanagement;

import java.io.*;

public class FileContent implements Serializable {
	private static final long serialVersionUID = 9130482791079863929L;
	public byte[] buffer;
	private String absolutePath, path, name;

	public FileContent(File file) throws IOException {
		
		absolutePath = file.getAbsolutePath();
		path = file.getPath();
		name = file.getName();
		
		int nbytes, boffset = 0, len;
		len = 1024;

		buffer = new byte[(int) file.length()];//Il buffer assume la stessa lunghezza del file.
		FileInputStream is;
		is = new FileInputStream(file);
		while(is.available() > 0) {
			if(boffset + len >= buffer.length) len = buffer.length - boffset;
			nbytes = is.read(buffer, boffset, len);
			boffset += nbytes;
		}
	}

	/*
	public File getSourceFileObject() {
		return file;
	}
	*/

	public String getSourceFilePath() {
		return path;
	}

	public String getSourceFileName() {
		return name;
	}

	public File recreateLocalFile(String localPath) throws IOException {
		File localFile = new File(localPath);
		localFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(localFile);
		fos.write(buffer, 0, buffer.length);
		fos.close();
		return localFile;
	}
	
	public String getSourceAbsolutePath() {
		return absolutePath;
	}

	public int getSize() {
		return buffer.length;
	}
}
