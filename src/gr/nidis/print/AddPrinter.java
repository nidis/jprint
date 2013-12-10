package gr.nidis.print;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wb.swt.SWTResourceManager;

public  class AddPrinter extends Dialog {

	protected Object result;
	protected Shell shell;
	private static Display display;
	public static Text textDirectory;
	private static Combo cmbSelectPrinter;
	public static String printerName;

	public AddPrinter(Shell parent, int style) {
		super(parent, style);
		setText("Add Printer");
	}

	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		//shell.setImage(SWTResourceManager.getImage("E:\\Users\\Alex\\Desktop\\printer_landscape.png"));
		shell.setImage(SWTResourceManager.getImage("printer_blue.png"));
		shell.setSize(498, 300);
	    shell.setMinimumSize(498, 300);
		shell.setText(getText());
		shell.setLayout(new FormLayout());
		
		display = Display.getDefault();
		shell.setSize(498, 300);
		shell.setText("Add Printer");

		Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 3;
	    
	    shell.setLocation(x, y);	
	    
		Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.dispose();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.right = new FormAttachment(100, -10);
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("&Cancel");
		
		Button btnOK = new Button(shell, SWT.PUSH);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				printerName = cmbSelectPrinter.getItem(cmbSelectPrinter.getSelectionIndex());
				if(Main.list.getItem(Main.idx).indexOf("[Printer Set]") == -1){
					if(Main.list.getItem(Main.idx).indexOf(Main.outString)>-1){
						String item = Main.list.getItem(Main.idx);
						Main.list.setItem(Main.idx, "[Printer Set]  " + item);
						
						//removes the entry and puts another entry on top
						Main.pairPathPrinter.remove(item);
						Main.pairPathPrinter.put("[Printer Set]  " + item, printerName);
					}
				}
				Main.listItems.clear();
				for (int loopIndex = 0; loopIndex < Main.list.getItems().length; loopIndex++){
					Main.listItems.add(Main.list.getItem(loopIndex));
		        }
				
				
				Main.btnStart.setEnabled(true);	
        		Main.btnStop.setEnabled(false);		
				shell.dispose();
			}
		});
		fd_btnCancel.left = new FormAttachment(btnOK, 6);
		FormData fd_btnOK = new FormData();
		fd_btnOK.top = new FormAttachment(btnCancel, 0, SWT.TOP);
		fd_btnOK.right = new FormAttachment(100, -106);
		fd_btnOK.left = new FormAttachment(100, -196);
		btnOK.setLayoutData(fd_btnOK);
		btnOK.setText("&OK");
		
		Label lblDirectory = new Label(shell, SWT.NONE);
		FormData fd_lblDirectory = new FormData();
		fd_lblDirectory.top = new FormAttachment(0, 10);
		fd_lblDirectory.left = new FormAttachment(0, 10);
		lblDirectory.setLayoutData(fd_lblDirectory);
		lblDirectory.setText("Directory");
		
		Label lblPrinter = new Label(shell, SWT.NONE);
		FormData fd_lblPrinter = new FormData();
		fd_lblPrinter.top = new FormAttachment(lblDirectory, 12);
		fd_lblPrinter.left = new FormAttachment(lblDirectory, 0, SWT.LEFT);
		lblPrinter.setLayoutData(fd_lblPrinter);
		lblPrinter.setText("Printer");
		
		textDirectory = new Text(shell, SWT.BORDER);
		textDirectory.setEditable(false);
		FormData fd_textDirectory = new FormData();
		fd_textDirectory.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_textDirectory.left = new FormAttachment(lblDirectory, 6);
		fd_textDirectory.top = new FormAttachment(0, 10);
		textDirectory.setLayoutData(fd_textDirectory);
		if(Main.outString.indexOf("[Printer Set]")==-1){
	    	textDirectory.setText(Main.outString);
		}else{
			textDirectory.setText(Main.outString.substring(15));
		}
		
		Label labelLine = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_labelLine = new FormData();
		fd_labelLine.top = new FormAttachment(100, -45);
		fd_labelLine.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_labelLine.bottom = new FormAttachment(100, -43);
		fd_labelLine.left = new FormAttachment(lblDirectory, 0, SWT.LEFT);
		labelLine.setLayoutData(fd_labelLine);
		
		cmbSelectPrinter = new Combo(shell, SWT.NONE|SWT.READ_ONLY);
		FormData fd_cmbChoosePrinter = new FormData();
		fd_cmbChoosePrinter.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_cmbChoosePrinter.top = new FormAttachment(textDirectory, 6);
		fd_cmbChoosePrinter.left = new FormAttachment(textDirectory, 0, SWT.LEFT);
		cmbSelectPrinter.setLayoutData(fd_cmbChoosePrinter);
		
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		
		
		if(printServices.length > 0){
			for(int i=0; i<printServices.length; i++){
				cmbSelectPrinter.add(printServices[i].getName());
				if(Main.printName.equals(printServices[i].getName())){
					cmbSelectPrinter.select(i);		
				}
				cmbSelectPrinter.select(i);
			}
		}else{
			cmbSelectPrinter.setText("No printers available.");
			cmbSelectPrinter.setEnabled(false);
		}

		cmbSelectPrinter.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(cmbSelectPrinter.getItem(cmbSelectPrinter.getSelectionIndex()));
				printerName = cmbSelectPrinter.getItem(cmbSelectPrinter.getSelectionIndex());
			}
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
	}
}
