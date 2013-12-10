package gr.nidis.print;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.print.PrintException;

public class PrintThread implements Runnable{
	private static boolean keepPrinting = true;
	String inputPath, printer;
	static long getId=0;
	
	public PrintThread(String inputPath, String printer){
		//this.keepPrinting = keepPrinting;
		this.inputPath = inputPath;
		this.printer = printer;
	}
	
	@Override
	public void run() {
		Helper helper = new Helper();//
		getId = Thread.currentThread().getId();

//		for (Map.Entry<String, Boolean> kpPrinting : Main.keepPrintingStatus.entrySet()){
//			//	if(keepPrinting.getKey().equals(list.getItem(selectedItems[0]))){
//		}
		
		///new whileLoop(keepPrinting);
	       while (keepPrinting) {
	        	try{
					//printFiles(inputPath, printer);
	        		System.out.println(Thread.currentThread().getId());		
	        		getId = Thread.currentThread().getId();
					helper.printFiles(inputPath, printer);
					//printFiles(inputPath, inputPath);
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}catch (PrintException e) {
					e.printStackTrace();
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
	       }
	}
	
	public static void shutdown(){
		if(Main.getThreadId == getId){
			keepPrinting = false;
		}
		
	}
//http://www.youtube.com/watch?v=_aNO6x8HXZ0
	

}
