package gr.nidis.print;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.print.PrintException;

public class Helper {
	public void printFiles(String inputPath, String printer) throws PrintException, FileNotFoundException, InterruptedException {
		//while(keepPrinting){
			Thread.sleep(3000);
			for(String file: getFileName(inputPath)){
				//System.out.println(file);
				try {
					//System.out.println("Printing started-file:"+file);
				synchronized(this){
					Runtime.getRuntime().exec("\"C:\\Program Files (x86)\\Foxit Software\\FOXIT READER\\Foxit Reader.exe \" /t "+"\""+file+"\"" +" "+ "\""+ printer+"\"");
				}
					
					//System.out.println("Printing finished");
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
	}
	
	public ArrayList<String> getFileName(String inputPath){
		String path = inputPath; 
		ArrayList<String> files = new ArrayList<String>();
		
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(); 
		
		for (int i = 0; i < listOfFiles.length; i++){
			if (listOfFiles[i].isFile()){
				files.add(i, listOfFiles[i].getAbsolutePath());
//				if (files[i].endsWith(".txt") || files[i].endsWith(".TXT")){
//					
//				}
			}
		}
		return files;
	}
	
	public static void removeFile(String file){
		File folder = new File(file);
		folder.delete(); 
	}
	
	
}
