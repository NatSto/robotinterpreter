/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import robotinterpreter.RobotInterpreter;
import robotinterpreter.RobotListener;

/*
 * FileChooserDemo.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 */
@SuppressWarnings("serial")
public class FileChooserDemo extends JPanel
                             implements ActionListener, RobotListener {

    static public final String newline = "\n";
    JButton openButton, /*saveButton,*/ clearButton, executeButton, stopButton;
    public static JTextArea log;
    JFileChooser fc;
    private File file;
    private RobotInterpreter r;
    SwingWorker<Void, Void> executor;

    public FileChooserDemo() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(50,150);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        DefaultCaret caret = (DefaultCaret)log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();

        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openButton = new JButton("Open a File...",
                                 createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);

        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        /*saveButton = new JButton("Save a File...",
                                 createImageIcon("images/Save16.gif"));
        saveButton.addActionListener(this);*/
        
        clearButton = new JButton("Clear Log");
        clearButton.addActionListener(this);
        
        executeButton = new JButton("Execute");
        executeButton.addActionListener(this);
        executeButton.setEnabled(false);
        
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        stopButton.setEnabled(false);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        //buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(executeButton);
        buttonPanel.add(stopButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }
    
    public static void writeLog(String s)
    {
    	log.append(s);
    }

    public void loadFile()
    {
		try 
		{
			FileReader fr = new FileReader(file);
		    BufferedReader br = new BufferedReader(fr);
		    String line = "";
            String code = "";
            
            while((line = br.readLine()) != null)
            {
                 code += line + newline;
            }
             
            br.close();
            fr.close();
             
    		r = new RobotInterpreter();
    		r.addRobotListener(this);
    		r.load(code);
    		if(r.isReady())
    		{
    			executeButton.setEnabled(true);
    		}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
    
    public void actionPerformed(ActionEvent e) 
    {
        //Handle open button action.
        if (e.getSource() == openButton) 
        {
            int returnVal = fc.showOpenDialog(FileChooserDemo.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Opening: " + file.getName() + "." + newline);
                loadFile();
            } 
            else 
            {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        //Handle save button action.
        } 
        /*else if (e.getSource() == saveButton) 
        {
            int returnVal = fc.showSaveDialog(FileChooserDemo.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                File file = fc.getSelectedFile();
                //This is where a real application would save the file.
                log.append("Saving: " + file.getName() + "." + newline);
            } 
            else 
            {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }*/
        else if (e.getSource() == clearButton) 
        {
                log.setText(null);
        }
        else if(e.getSource() == executeButton)
        {
            executor = new SwingWorker<Void, Void>()
            {
            	@Override
            	public Void doInBackground()
            	{
                    stopButton.setEnabled(true);
            		executeButton.setEnabled(false);

            	    r.execute();
					return null;
            	}
            	
            	public void done()
            	{
            		executeButton.setEnabled(true);
                    stopButton.setEnabled(false);
            	}
            };
            executor.execute();

        }
        else if(e.getSource() == stopButton)
        {
        	executor.cancel(true);
            log.append("Program Halted!\n\n");
            stopButton.setEnabled(false);
            loadFile();
        }
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FileChooserDemo.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("G Robot Interpreter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new FileChooserDemo());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

	@Override
	public void print(String s) 
	{
		writeLog(s);		
	}

	@Override
	public void println(String s) 
	{
		writeLog(s + newline);
	}

	@Override
	public void error(String var, String e) 
	{
		JOptionPane.showMessageDialog(null, e, var + " ERROR", JOptionPane.ERROR_MESSAGE);		
	}

	@Override
	public void driveForward() 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
	
	}

	@Override
	public void driveBackwards() 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
	}

	@Override
	public void turnLeft() 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
	}

	@Override
	public void turnRight() 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
	}

	@Override
	public void stop() 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);			
	}

	@Override
	public int getSonarData(int num) 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
		return 0;
	}

	@Override
	public int getBearing() 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
		return 0;
	}

	@Override
	public void driveDistance(int dist) 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
	}

	@Override
	public void turnAngle(int angle) 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
	}

	@Override
	public void turnToBearing(int bearing) 
	{
		writeLog("HERP DERP CAN'T EXECUTE ROBOT COMMANDS IN THIS MODE." + newline);		
	}
}


