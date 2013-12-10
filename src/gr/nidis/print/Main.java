//Notes - Things for possible changes
//1. Instead of using List, use Table. This will solve the problem of setting foreground for a selected Item
//2. Tooltip for selected item
//3. Log4j

package gr.nidis.print;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.print.PrintException;

import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.List;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.TrayItem;

public class Main {
	public static final Shell shlJprint  = new Shell();
	public static final Display display= Display.getDefault();
	public static List list;
	private static Button btnAddPrinter;
	public static String outString;
	public static Button btnStart;
	public static Button btnStop;
	public static int idx;
	private static Button btnRemoveDirectory;
	public static Set<String> listItems;
	public  static String filename = "tmp.txt";
	public static Map<Integer, Integer> mapStartStop = new HashMap<Integer, Integer>();
	public static Map<String, String> pairPathPrinter = new HashMap<String, String>();
	private static int[] selectedItems;
	public static boolean keepPrinting;
	public static String inputPath, printer;
	public static String printName;
	public static Map<String, Boolean> keepPrintingStatus = new HashMap<String, Boolean>();
	public static long getThreadId = 0;
	public static int i=0;
	public static Thread thread[] = new Thread[200];
	
	public static void main(String[] args) {
		shlJprint.setImage(SWTResourceManager.getImage("printer_blue.png"));
		shlJprint.setSize(498, 300);
	    shlJprint.setMinimumSize(498, 300);
		shlJprint.setText("JPrint");
		shlJprint.setLayout(new FormLayout());
		Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shlJprint.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 6;
	    shlJprint.setLocation(x, y);	

	    list = new List(shlJprint, SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
	    listItems = new LinkedHashSet<String>();
	    
	    //Read from file
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));){
			String sCurrentLine, subLinePath, subLinePrinter;
			
			while ((sCurrentLine = in.readLine()) != null) {
				if(sCurrentLine.indexOf("=") > -1){
					subLinePath = sCurrentLine.substring(0, sCurrentLine.indexOf("="));
					subLinePrinter = sCurrentLine.substring(sCurrentLine.indexOf("=")+1);
	 				list.add(subLinePath);
	 				listItems.add(subLinePath);
					pairPathPrinter.put(subLinePath, subLinePrinter);
				}
			}
			
 			in.close();
 		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		Button btnAddDirectory = new Button(shlJprint, SWT.NONE);
		btnAddDirectory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog dirDialog = new DirectoryDialog(shlJprint, SWT.OPEN);
				String dir="";
				dir = dirDialog.open ();
				if(dir != null){
					for(String listItem: listItems){
						if(listItem.equals(dir) || listItem.equals("[Printer Set]  "+ dir)){
							return;
						}
					}
					list.add(dir);
					listItems.add(dir);
					pairPathPrinter.put(dir, "");
				}
				
				while (!shlJprint.isDisposed ()) {
					if (!display.readAndDispatch ()) display.sleep ();
				}
				display.dispose ();
			}
		});
	
		FormData fd_btnAddDirectory = new FormData();
		fd_btnAddDirectory.left = new FormAttachment(100, -112);
		fd_btnAddDirectory.right = new FormAttachment(100, -6);
		fd_btnAddDirectory.top = new FormAttachment(0, 8);
		btnAddDirectory.setLayoutData(fd_btnAddDirectory);
		btnAddDirectory.setText("Add Directory");
		
		Menu menu = new Menu(shlJprint);
		shlJprint.setMenu(menu);
		
		btnRemoveDirectory = new Button(shlJprint, SWT.NONE);
		btnRemoveDirectory.setEnabled(false);
		btnRemoveDirectory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0){
				if(list.getItemCount() >0 && list.getSelectionIndex() != -1){
					String item = list.getItem(list.getSelectionIndex());
					listItems.remove(item);
					list.remove(list.getSelectionIndex());
					pairPathPrinter.remove(item);
				}
				if(list.getItemCount() ==0 || list.getSelectionIndex() ==-1){
					btnRemoveDirectory.setEnabled(false);
					btnAddPrinter.setEnabled(false);
					btnAddPrinter.setText("Add Printer");
					btnStart.setEnabled(false);	
	        		btnStop.setEnabled(false);
				}
			}
		});
		FormData fd_btnRemoveDirectory = new FormData();
		fd_btnRemoveDirectory.top = new FormAttachment(btnAddDirectory, 4);
		fd_btnRemoveDirectory.left = new FormAttachment(list, 6);
		fd_btnRemoveDirectory.right = new FormAttachment(100, -6);
		btnRemoveDirectory.setLayoutData(fd_btnRemoveDirectory);
		btnRemoveDirectory.setText("Remove Directory");
						
		FormData fd_list = new FormData();
		fd_list.top = new FormAttachment(0, 9);
		fd_list.right = new FormAttachment(btnAddDirectory, -6);
		fd_list.bottom = new FormAttachment(100, -10);
		fd_list.left = new FormAttachment(0, 10);
		list.setLayoutData(fd_list);
		
		list.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				selectedItems = list.getSelectionIndices();
		        if(selectedItems.length ==0){
					btnRemoveDirectory.setEnabled(false);
					btnAddPrinter.setEnabled(false);
					btnStart.setEnabled(false);	
	        		btnStop.setEnabled(false);
				}else{
					outString = "";
		        	outString += list.getItem(selectedItems[0]);
		        	idx = selectedItems[0];
			        	
		        	if(outString.indexOf("[Printer Set]")>-1){
		        		btnRemoveDirectory.setEnabled(true);
		        		btnAddPrinter.setEnabled(true);
		        		btnAddPrinter.setText("Change Printer");
				        	
		        		if(mapStartStop.size()>0){
		        			for (Map.Entry<Integer, Integer> entry : mapStartStop.entrySet()){
		        				if(entry.getKey().equals(idx) && entry.getValue().equals(1)){
		        					btnStart.setEnabled(false);	
		        					btnStop.setEnabled(true);
		        					btnRemoveDirectory.setEnabled(false);
		        					break;
		        				}else{
		        					btnStart.setEnabled(true);	
		        					btnStop.setEnabled(false);
		        				}
		        			}
		        		}else{
		        			btnStart.setEnabled(true);	
		        			btnStop.setEnabled(false);
		        		}

		        	}else{
		        		btnStart.setEnabled(false);	
		        		btnStop.setEnabled(false);
		        		btnRemoveDirectory.setEnabled(true);
		        		btnAddPrinter.setEnabled(true);
		        		btnAddPrinter.setText("Add Printer");
		        	}
				}
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		list.addMouseListener(new MouseListener() {
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				if (e.button == 3) {
					System.out.println("Right click !");
					list.setToolTipText(list.getItem(idx));
				}
			}
			
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
			}
		
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
			}
		});
		
		btnAddPrinter = new Button(shlJprint, SWT.NONE);
		btnAddPrinter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (Map.Entry<String, String> pairEntry : pairPathPrinter.entrySet()){
					if(pairEntry.getKey().equals(list.getItem(selectedItems[0]))){
						printName = pairEntry.getValue();
					}
				}
				AddPrinter addPrinterDialog = new AddPrinter(shlJprint, SWT.SHELL_TRIM|SWT.APPLICATION_MODAL);
				addPrinterDialog.open();

				if(outString.indexOf("[Printer Set]")>-1){
	        		if(mapStartStop.size()>0){
		        		for (Map.Entry<Integer, Integer> entry : mapStartStop.entrySet()){
		        			if(entry.getKey().equals(idx) && entry.getValue().equals(1)){
				        		btnStart.setEnabled(false);	
				        		btnStop.setEnabled(true);
				        		break;
				        	}else{
				        		btnStart.setEnabled(true);	
				        		btnStop.setEnabled(false);
				        	}
				        }
	        		}else{
	        			btnStart.setEnabled(true);	
		        		btnStop.setEnabled(false);
	        		}
	        	}else{
        			btnStart.setEnabled(true);	
	        		btnStop.setEnabled(false);
        		}
				
				btnAddPrinter.setText("Change Printer");
				while (!shlJprint.isDisposed ()) {
					if (!display.readAndDispatch ()) display.sleep ();
				}
				display.dispose ();
			}
		});
		
		
		btnAddPrinter.setEnabled(false);
		FormData fd_btnAddPrinter = new FormData();
		fd_btnAddPrinter.top = new FormAttachment(btnRemoveDirectory, 3);
		fd_btnAddPrinter.left = new FormAttachment(list, 6);
		fd_btnAddPrinter.right = new FormAttachment(btnAddDirectory, 0, SWT.RIGHT);
		btnAddPrinter.setLayoutData(fd_btnAddPrinter);
		btnAddPrinter.setText("Add Printer");
		
		btnStart = new Button(shlJprint, SWT.NONE);
		btnStart.setEnabled(false);
		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				mapStartStop.put((Integer) idx,(Integer)1);
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
				btnRemoveDirectory.setEnabled(false);
				btnAddPrinter.setEnabled(false);
				
				/*************Main Logic*************/
				//printFiles(AddPrinter.printerName);
				inputPath = list.getItem(idx).substring(15);
				printer = AddPrinter.printerName;
				
				if(btnAddPrinter.getText().equals("Change Printer")){
					for (Map.Entry<String, String> pairEntry : pairPathPrinter.entrySet()){
						if(pairEntry.getKey().equals(list.getItem(selectedItems[0]))){
							printer = pairEntry.getValue();
						}
					}
					
				}

				//////////////START EXPERIMENT
				
				keepPrintingStatus.put(list.getItem(selectedItems[0]), true);
				
				
				
				
				//http://stackoverflow.com/questions/11915968/java-executorservice-pause-resume-a-specific-thread	*
				//http://stackoverflow.com/questions/3194545/how-to-stop-a-java-thread-gracefully
				//http://javatalks.ru/topics/21330			
				//E:\\Users\\Alex\\Desktop\\src\\java\\util\\concurrent
				//
				//http://stackoverflow.com/questions/1323408/get-a-list-of-all-threads-currently-running-in-java/3018672#3018672 *
				//
				//http://docs.oracle.com/javase/6/docs/api/java/lang/management/ThreadMXBean.html
				//http://www.avajava.com/tutorials/lessons/how-do-i-identify-a-thread.html
				//http://stackoverflow.com/questions/3179356/control-thread-through-button
				
				//for (Map.Entry<String, Boolean> keepPrinting : keepPrintingStatus.entrySet()){
				//	if(keepPrinting.getKey().equals(list.getItem(selectedItems[0]))){
					
					//thread[i] = new Thread(new PrintThread(keepPrinting.getValue(), inputPath, printer));
				thread[i] = new Thread(new PrintThread(inputPath, printer));
					getThreadId = thread[i].getId();
					thread[i].start();
					System.out.println("start "+i);
					i++;
				//	}
				//}
				
			
				//////////////END EXPERIMENT
			
				while(!btnStart.isDisposed ()){
					if(!display.readAndDispatch ()) display.sleep ();
				}
				display.dispose ();
				
			}
		});
		
		FormData fd_btnStart = new FormData();
		fd_btnStart.left = new FormAttachment(list, 6);
		fd_btnStart.right = new FormAttachment(btnAddDirectory, 0, SWT.RIGHT);
		btnStart.setLayoutData(fd_btnStart);
		btnStart.setText("Start");
		
		btnStop = new Button(shlJprint, SWT.NONE);
		fd_btnStart.bottom = new FormAttachment(btnStop, -3);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				keepPrinting=false;
				mapStartStop.put((Integer) idx,(Integer)0);
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				btnRemoveDirectory.setEnabled(true);
				btnAddPrinter.setEnabled(true);
				System.out.println(keepPrinting);
				int i=0;
				Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
				Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
				for (Map.Entry<String, Boolean> keepPrinting : keepPrintingStatus.entrySet()){
					if(keepPrinting.getKey().equals(list.getItem(selectedItems[0]))){
						//exec.shutdown();
//						System.out.println(keepPrinting.getValue().getClass());
						//System.out.println(threadArray[i].getClass().getMethods().toString());
						//i++;
						//PrintThread.shutdown();
						
					}
				}
				
				try {thread[1].interrupt();
					thread[1].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		});
		btnStop.setEnabled(false);
		FormData fd_btnStop = new FormData();
		fd_btnStop.right = new FormAttachment(btnAddDirectory, 0, SWT.RIGHT);
		fd_btnStop.bottom = new FormAttachment(100, -10);
		fd_btnStop.left = new FormAttachment(btnAddDirectory, 0, SWT.LEFT);
		btnStop.setLayoutData(fd_btnStop);
		btnStop.setText("Stop");
		
		
		shlJprint.addShellListener(new ShellListener() {
			
			@Override
			public void shellIconified(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void shellDeiconified(ShellEvent arg0) {
				
			}
			
			@Override
			public void shellDeactivated(ShellEvent arg0) {
				
			}
			
			@Override
			public void shellClosed(ShellEvent arg0) {
				arg0.doit = false;
				shlJprint.setVisible(false);
				
				CreateTray.tip.setVisible(true);
				
				//http://www.javaneverdie.com/java/how-to-close-swt-application-to-system-tray/
				
			}
			
			@Override
			public void shellActivated(ShellEvent arg0) {
				
			}
		});
		
		CreateTray.createTray();
		shlJprint.open();
		shlJprint.layout();
		while (!shlJprint.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
				

			}	else{
				
			}
		}
		display.dispose();
	}//end of main
	public static void printFiles(String inputPath, String printer) throws PrintException, FileNotFoundException, InterruptedException {
		//while(keepPrinting){
			Thread.sleep(3000);
			for(String file: getFileName(inputPath)){
				System.out.println(file);
				try {
					System.out.println("Printing started-file:"+file);
					Runtime.getRuntime().exec("\"C:\\Program Files (x86)\\Foxit Software\\FOXIT READER\\Foxit Reader.exe \" /t "+"\""+file+"\"" +" "+ "\""+ printer+"\"");
					System.out.println("Printing finished");
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
	
	public static ArrayList<String> getFileName(String inputPath){
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
}
