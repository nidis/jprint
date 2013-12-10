package gr.nidis.print;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class CreateTray{
	//http://www.eclipsezone.com/eclipse/forums/t66093.html
	public final static Tray tray = Main.display.getSystemTray();;
	public final static TrayItem item = new TrayItem(tray, SWT.NONE);
	final static Menu menu = new Menu(Main.shlJprint, SWT.POP_UP);

	static MenuItem openMenuItem = new MenuItem(menu, SWT.PUSH);
	static MenuItem exitMenuItem = new MenuItem(menu, SWT.PUSH);
	static final ToolTip tip = new ToolTip(Main.shlJprint, SWT.BALLOON | SWT.ICON_INFORMATION);
	
	public final static void createTray() {
        Image image;
        //tray = Main.display.getSystemTray();

        if (tray == null) {
            System.out.println("The system tray is not available");
        } else {
        	tip.setMessage("JPrint is working on silent mode!");
        	
            item.setToolTipText("JPrint");
            item.setToolTip(tip);
            
            item.addListener(SWT.Show, new Listener() {
                public void handleEvent(Event event) {
                    System.out.println("show");

                }
            });
            
            item.addListener(SWT.Hide, new Listener() {
                public void handleEvent(Event event) {
                    System.out.println("hide");
                }
            });

            item.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    System.out.println("selection");
                }
            });

            item.addListener(SWT.DefaultSelection, new Listener() {
                public void handleEvent(Event event) {
                    System.out.println("default selection");
                    Main.shlJprint.setVisible(true);
                	Main.shlJprint.setActive();
                }
            });
      

            openMenuItem.setText("Show JPrint");
            openMenuItem.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                	Main.shlJprint.setVisible(true);
                	Main.shlJprint.setActive();
                    
                }

            });

            
            exitMenuItem.setText("Exit");
            exitMenuItem.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                	try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.filename), "UTF-8"));){
    					if(Main.pairPathPrinter.size()>0){
    						for (Map.Entry<String, String> pairEntry : Main.pairPathPrinter.entrySet()){
    							System.out.println(pairEntry.getValue());
    							out.write(pairEntry.getKey()+"="+pairEntry.getValue()+"\n");
    						}
    					}
    					out.close();	
    					Main.mapStartStop.clear();
    				} catch (IOException ex) {
    					
    				}
                    CreateTray.item.dispose();
                 	System.exit(0);
                }
            });
 
            item.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    menu.setVisible(true);
                    
                }
            });

            image = SWTResourceManager.getImage("printer_blue.png");
            item.setImage(image);
        }
    }
}
